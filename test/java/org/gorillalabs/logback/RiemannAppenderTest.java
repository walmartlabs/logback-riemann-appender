package org.gorillalabs.logback;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class RiemannAppenderTest {

  @Test
  public void parseCustomAttributesShouldCorrectlyParseAValidStringIntoAMap() throws Exception {
    String attributeString = "foo:bar,baz:quux";

    Map<String, String> result = getCustomAttributes(attributeString);

    assertEquals("bar", result.get("foo"));
    assertEquals("quux", result.get("baz"));
  }

  @Test
  public void parseCustomAttributesShouldCorrectlyParseKvPairsUntilItEncountersAnInvalidPair() throws Exception {

    String attributeString = "foo:bar,wheres-the-value";

    Map<String, String> customAttributes = getCustomAttributes(attributeString);

    assertEquals(1, customAttributes.keySet().size());
    assertEquals("bar", customAttributes.get("foo"));
  }

  @Test
  public void infoEventsExcluded() throws Exception {
    TestErrorEvent errorEvent = new TestErrorEvent();
    TestInfoEvent infoEvent = new TestInfoEvent();

    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();

    assertFalse(appender.isMinimumLevel(infoEvent));
    assertTrue(appender.isMinimumLevel(errorEvent));
  }

  @Test
  public void infoEventsIncluded() throws Exception {
    TestErrorEvent errorEvent = new TestErrorEvent();
    TestInfoEvent infoEvent = new TestInfoEvent();

    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    appender.setRiemannLogLevel("INFO");

    assertTrue(appender.isMinimumLevel(infoEvent));
    assertTrue(appender.isMinimumLevel(errorEvent));
  }

  @Test
  public void hostnameShouldDefaultToAddressOfLocalMachine() throws Exception {
    String hostname = InetAddress.getLocalHost().getHostName();
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    assertThat(appender.toString(), containsString("hostname=" + hostname));
  }

  @Test
  public void serviceShouldDefault() throws Exception {
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    assertThat(appender.toString(), containsString("serviceName=*no-service-name*"));
  }

  @Test
  public void serviceName() throws Exception {
    String serviceName = UUID.randomUUID().toString();
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    appender.setServiceName(serviceName);
    assertThat(appender.toString(), containsString("serviceName=" + serviceName));
  }

  @Test
  public void hostnameShouldBeOverridableViaSystemProperty() throws Exception {
    String hostname = UUID.randomUUID().toString();
    System.setProperty("hostname", hostname);
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    assertThat(appender.toString(), containsString("hostname=" + hostname));
  }

  @Test
  public void hostnamePropertyShouldOverrideDefaultAndSystemProperty() throws Exception {
    String hostname = UUID.randomUUID().toString();
    System.setProperty("hostname", hostname);
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    String hostnameProperty = UUID.randomUUID().toString();
    appender.setHostname(hostnameProperty);
    assertThat(appender.toString(), containsString("hostname=" + hostnameProperty));
  }

  @Test
  public void riemannHostnameShouldDefaultToLocalhost() throws Exception {
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    assertThat(appender.toString(), containsString("riemannHostname=localhost"));
  }

  @Test
  public void riemannHostnameShouldBeOverridableViaSystemProperty() throws Exception {
    String riemannHostname = UUID.randomUUID().toString();
    System.setProperty("riemann.hostname", riemannHostname);
    RiemannAppender<ILoggingEvent> appender = new RiemannAppender<ILoggingEvent>();
    String riemannHostnameProperty = UUID.randomUUID().toString();
    appender.setRiemannHostname(riemannHostnameProperty);
    assertThat(appender.toString(), containsString("riemannHostname=" + riemannHostnameProperty));
  }

  private Map<String, String> getCustomAttributes(String attributeString) {
    return new RiemannAppender<ILoggingEvent>().parseCustomAttributes(attributeString);
  }

  private static class TestLoggingEvent extends LoggingEvent {
    public Level getLevel() { return Level.ALL; }
  }

  private static class TestErrorEvent extends LoggingEvent {
    public Level getLevel() { return Level.ERROR; }
  }

  private static class TestInfoEvent extends LoggingEvent {
    public Level getLevel() { return Level.INFO; }
  }

  private static class TestDebugEvent extends LoggingEvent {
    public Level getLevel() { return Level.DEBUG; }
  }
}
