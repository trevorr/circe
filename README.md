Circe: A High-Performance Hash Algorithm Framework & Library
----

Circe is a hash algorithm framework that supports pluggable hash implementations discovered at runtime. Its primary goal is to enable the usage of high-performance native-code and hardware-accelerated hash algorithms, while also providing pure Java implementations when native libraries or hardware acceleration are not available.

Circe provides hash functions in both stateful and stateless forms. The stateful form accumulates state internally from potentially multiple input segments and provides access to the output as various primitive types and byte arrays. The stateless form is available when the output can be represented by an `int` or a `long` and provides the output immediately for a given input. Additionally, for algorithms like CRC where the state and output are essentially interchangeable, an incremental stateless form is provided that allows more data to be hashed using the output from hashing a previous segment. To facilitate access from native code, all hash interfaces accept input from direct (and non-direct) `java.nio.ByteBuffer` objects and (unsafe) `long` memory addresses, in addition to Java byte arrays.

Currently Implemented Algorithms
----

All of Circe's hash algorithms are implemented in optional provider modules:

* `circe-crc`: Pure Java implementations of any CRC function up to 64 bits, including CRC-32, CRC-32C, and CRC-64. CRC-32 can be provided by `java.util.zip.CRC32`, which uses the native implementation in `zlib`.
* `circe-crc32c-sse42`: Hardware-accelerated CRC-32C using the x86-64 SSE 4.2 `crc32` and (if available) `pclmulqdq` instructions. Processes almost 15 GB/s on a desktop Intel Core i7-2600, compared to 1 GB/s with `java.util.zip.CRC32`. Currently supports MSVC on Windows and GCC on Linux and Mac OS X, with runtime detection of processor support.
* `circe-digest`: Wrapper around `javax.security.MessageDigest`, providing MD5, SHA-1, SHA-256, SHA-384, and SHA-512.
* `circe-guava`: Wrapper around `com.google.common.hash.Hashing`, providing [MurmurHash3](https://code.google.com/p/smhasher/wiki/MurmurHash3) and [SipHash-2-4](https://131002.net/siphash/).

Dependencies
----

Circe is designed to be very modular and have limited runtime dependencies. Most of its modules have no required external dependencies. The only exception so far is a [Google Guava](https://code.google.com/p/guava-libraries/) dependency in `circe-cache`, an optional cache for hash functions using tables generated at runtime, and `circe-guava`, an optional provider for the hash algorithms implemented within Guava.

For unit testing, Circe uses JUnit 4, [JMockit](https://code.google.com/p/jmockit/), and [Hamcrest](https://code.google.com/p/hamcrest/).

Building
----

Circe is built using [Maven](http://maven.apache.org/), with native code built using the [Maven NAR Plugin](http://maven-nar.github.io/). To build the entire project and run all unit tests, simply run `mvn install`. To perform a full release build, including source and Javadoc JARs, and local Maven install, run `mvn install -P release`. To build aggregated Javadoc (placed under `target/site/apidocs`), run `mvn site`.

License
----
Circe is released under the [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).