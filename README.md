# logback-riemann-appender  [![Build Status](https://travis-ci.org/gorillalabs/logback-riemann-appender.svg?branch=master)](https://travis-ci.org/gorillalabs/logback-riemann-appender)

Forked from: https://github.com/walmartlabs/logback-riemann-appender

## Logback appender for [Riemann](http://riemann.io/)

- extended by service name (read from logback.xml), state (adapt event log level) and tags (can be add via list in log message)
- for more basic information read in forked projekcts
 https://github.com/walmartlabs/logback-riemann-appender/blob/master/README.md
 https://github.com/kyleburton/logback-riemann-appender


## Usage

Artifacts are available through
[clojars](https://clojars.org/gorillalabs/logback-riemann-appender) which you can add
to your maven repository like so:

```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

See `resources/logback.xml` for a full example configuration.

    <configuration scan="true">
      <appender name="R1" class="com.walmartlabs.logback.RiemannAppender">
        <serviceName>Test Service</serviceName>
        <riemannHostname>127.0.0.1</riemannHostname>
        <riemannPort>5555</riemannPort>
        <hostname>graphene</hostname>
        <customAttributes>application:test-service,datacenter:us-sw</customAttributes>
		<riemannLogLevel>WARN</riemannLogLevel>
      </appender>
      <root level="DEBUG">
        <appender-ref ref="R1"/>
      </root>
    </configuration>

## License

Copyright © 2013 Kyle Burton

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
