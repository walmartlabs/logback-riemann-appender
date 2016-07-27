# Release Notes

## v0.3.0 - July 27st, 2016

....



# Release Notes

## v0.2.0 - July 21st, 2016

- [#2](https://github.com/walmartlabs/logback-riemann-appender/pull/2) - Allow configuration via system property ([@dbriones](https://github.com/dbriones))
- [#1](https://github.com/walmartlabs/logback-riemann-appender/pull/1) - 109827790 reimann logback appender tcp support ([@hlship](https://github.com/hlship))

The RiemannAdapter can now be configured to use TCP, with the new `tcp` property.
The default (for now) is to continue using UDP.

Incompatible changes:

Some properties of RiemannAdapter have changed from string to an appropriate type
(int or boolean);

- [80bd900](https://github.com/walmartlabs/logback-riemann-appender/commit/80bd9003e79ad48ae23c946f614aae19276f6ef7) - Revert project version to 0.1.5 in advance of release (Dante Briones)
- [2a54605](https://github.com/walmartlabs/logback-riemann-appender/commit/2a54605a2d17e5f34b1174268ef0079531c9b8c5) - Log the contents of the MDC in debug mode. (Dante Briones)
- [29e3588](https://github.com/walmartlabs/logback-riemann-appender/commit/29e358802d7b01c526ef9b015b8419db190ebd2f) - Revert `riemann-java-client` to 0.2.8 (Dante Briones)

Pivotal issues closed:
- [109827790](https://www.pivotaltracker.com/story/show/109827790) - Add TCP support to `logback-riemann-appender`
