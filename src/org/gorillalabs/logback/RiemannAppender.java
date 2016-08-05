package org.gorillalabs.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import io.riemann.riemann.client.EventDSL;
import io.riemann.riemann.client.RiemannClient;
import io.riemann.riemann.client.SimpleUdpTransport;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class RiemannAppender<E> extends AppenderBase<E> {
  public static final int DEFAULT_PORT = 5555;
  public static final String DEFAULT_HOST = "localhost";
  private final String className = getClass().getSimpleName();

  private String serviceName = "*no-service-name*";
  private Level riemannLogLevel = Level.ERROR;
  private String riemannHostname = determineRiemannHostname();
  private int riemannPort = DEFAULT_PORT;
  private String hostname = determineHostname();
  private Map<String, String> customAttributes = new HashMap<String, String>();
  private boolean tcp = false;

  public static AtomicLong timesCalled = new AtomicLong(0);

  private static boolean debug = false;

  private RiemannClient riemannClient = null;

  private String determineRiemannHostname() {
    return System.getProperty("riemann.hostname", DEFAULT_HOST);
  }

  private String determineHostname() {
    String hn = System.getProperty("hostname");
    if (hn == null) {
      try {
        return InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      }
    } else {
      return hn;
    }
  }

  public void start() {
    try {
      if (debug) {
        printError("%s.start()", this);
      }

      riemannClient = tcp
              ? RiemannClient.tcp(riemannHostname, riemannPort)
              : new RiemannClient(new SimpleUdpTransport(riemannHostname, riemannPort));

      if (debug) {
        printError("%s.start: connecting", className);
      }

      riemannClient.connect();

      printError("%s.start: connected to %s, using hostname of %s", className, riemannHostname, hostname);
    } catch (IOException ex) {
      if (debug) {
        printError("%s: Error initializing: %s", className, ex);
      }
      throw new RuntimeException(ex);
    }
    super.start();
  }

  public void stop() {
    if (debug) {
      printError("%s.stop()", this);
    }
    if (riemannClient != null) {
        riemannClient.close();
    }
    super.stop();
  }

  public String toString() {
    return String.format(
      "RiemannAppender{hashCode=%s;serviceName=%s;transport=%s;riemannHostname=%s;riemannPort=%d;hostname=%s}",
      hashCode(),
      serviceName,
      tcp ? "tcp" : "udp",
      riemannHostname,
      riemannPort,
      hostname);
  }

  private String getStackTraceFromEvent(ILoggingEvent logEvent) {
    String result = null;
    IThrowableProxy throwable = logEvent.getThrowableProxy();
    if (null != throwable) {
      String firstLine = String.format("%s: %s\n", throwable.getClassName(), throwable.getMessage());
      StringBuilder sb = new StringBuilder(firstLine);
      if (null != throwable.getStackTraceElementProxyArray()) {
        for (StackTraceElementProxy elt : throwable.getStackTraceElementProxyArray()) {
          sb.append("\t")
            .append(elt.toString())
            .append("\n");
        }
      }
      result = sb.toString();
    }
    return result;
  }

  // Invoked from the Clojure code, but is only used for testing.
  public void forceAppend(E event) {
    append(event);
  }

  boolean isMinimumLevel(ILoggingEvent logEvent) {
    return logEvent.getLevel().isGreaterOrEqual(riemannLogLevel);
  }

  protected synchronized void append(E event) {
    timesCalled.incrementAndGet();
    ILoggingEvent logEvent = (ILoggingEvent) event;

    if(debug) {
      printError("Original log event: %s", asString(logEvent));
    }

    if (isMinimumLevel(logEvent)) {
      EventDSL rEvent = createRiemannEvent(logEvent);
      try {
        try {
          if (debug) {
            printError("%s.append: sending riemann event: %s", className, rEvent);
          }
          rEvent.send();
          if (debug) {
            printError("%s.append(logEvent): sent to riemann %s:%s", className, riemannHostname, riemannPort);
          }
        } catch (Exception ex) {
          if (debug) {
            printError("%s: Error sending event %s", this, ex);
            ex.printStackTrace(System.err);
          }

          riemannClient.reconnect();
          rEvent.send();
        }
      } catch (Exception ex) {
        // do nothing
        if (debug) {
          printError("%s.append: Error during append(): %s", className, ex);
          ex.printStackTrace(System.err);
        }
      }
    }
  }

  private String asString(ILoggingEvent logEvent) {
    Map<String, String> mdc = logEvent.getMDCPropertyMap();
    StringBuilder mdcContents = new StringBuilder();
    for (String key : mdc.keySet()) {
      mdcContents.append(String.format(", %s:%s", key, mdc.get(key)));
    }
    return String.format("{level:%s, message:%s, logger:%s, thread:%s%s}",
                         logEvent.getLevel().toString(),
                         logEvent.getMessage(),
                         logEvent.getLoggerName(),
                         logEvent.getThreadName(),
                         mdcContents.toString());
  }

  private EventDSL createRiemannEvent(ILoggingEvent logEvent) {
    EventDSL event = riemannClient.event()
                                  .host(hostname)
                                  .service(serviceName)
                                  .state(resolveState(logEvent))
	                          // timestamp is expressed in millis,
	                          // `time` is expressed in seconds
                                  .time(logEvent.getTimeStamp() / 1000)
                                  .description(logEvent.getMessage())
                                  .attribute("log/level", logEvent.getLevel().levelStr)
                                  .attribute("log/logger", logEvent.getLoggerName())
                                  .attribute("log/thread", logEvent.getThreadName())
	                          .attribute("log/message", logEvent.getMessage());

    if (logEvent.getThrowableProxy() != null) {
      event.attribute("log/stacktrace", getStackTraceFromEvent(logEvent));
    }
    if (logEvent.getMarker() != null) {
      event.tag("log/" + logEvent.getMarker().getName());
    }

    copyAttributes(event, logEvent.getMDCPropertyMap());
    copyAttributes(event, customAttributes);

    return event;
  }

  /**
   * Copy attributes out of the source and add them to `target`,
   * making sure to prefix the keys with `log/` -- this puts the
   * keywords in that namespace, preventing any collisions with the
   * Riemann schema.
   * @param target
   * @param source
   */
  private void copyAttributes(EventDSL target, Map<String, String> source) {
    for (String key : source.keySet()) {
       target.attribute("log/" + key, source.get(key));
    }
  }

  private void printError(String format, Object... params) {
    System.err.println(String.format(format, params));
  }

  public void setServiceName(String s) {
    serviceName = s;
  }

  public void setRiemannHostname(String s) {
    riemannHostname = s;
  }

  public void setRiemannPort(int i) {
    riemannPort = i;
  }

  public void setHostname(String s) {
    hostname = s;
  }

  public void setRiemannLogLevel(String s) {
    riemannLogLevel = Level.toLevel(s);
  }

  public void setCustomAttributes(String s) {
    customAttributes.putAll(parseCustomAttributes(s));
  }

  Map<String, String> parseCustomAttributes(String attributesString) {
    HashMap<String, String> result = new HashMap<String, String>();
    try {
      for (String kvPair : attributesString.split(",")) {
        String[] splitKvPair = kvPair.split(":");
        result.put(splitKvPair[0], splitKvPair[1]);
      }
    } catch (Throwable t) {
      printError("Encountered error while parsing attribute string: %s", attributesString);
    }
    return result;
  }

  /**
   * Set to true to enable TCP requests to the Riemann server.  The default, false, is to
   * use UDP.
   */
  public void setTcp(boolean b) {
    tcp = b;
  }

  /**
   * Enable additional logging when setting up the connection, and on each event logged.
   */
  public void setDebug(boolean b) {
    debug = b;
  }

  /**
   * Use Logging Event-Level as state value.
   *
   * @param logEvent
   * @return
     */
  public String resolveState(ILoggingEvent logEvent){
    return logEvent.getLevel().toString().toLowerCase();
  }
}
