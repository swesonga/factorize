# factorize
factorize is a simple program for factorizing arbitrarily large natural numbers.
This program is useful for exploring performance monitoring tools
using a simple app that does something non-trivial.

To compile from directory containing this file:

```
$JAVA_HOME/bin/javac -d . PrimalityTest.java FactorizationUtils.java Factorize.java
```

To compile using Maven:

```
mvn package
```

To run from the executable JAR file:

```
$JAVA_HOME/bin/java -jar target/factorize-1.0.0-jar-with-dependencies.jar 4388802055429773100203728822125 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 6
```

More usage examples:

```
$JAVA_HOME/bin/java org.swesonga.math.Factorize 65
$JAVA_HOME/bin/java org.swesonga.math.Factorize 438880205542
$JAVA_HOME/bin/java org.swesonga.math.Factorize 43888020554297731
$JAVA_HOME/bin/java org.swesonga.math.Factorize 4388802055429773100203726550535118822125
$JAVA_HOME/bin/java org.swesonga.math.Factorize 42039582593802342572091
$JAVA_HOME/bin/java org.swesonga.math.Factorize 42039582593802342572091 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 6
```

To generate a random 13-byte integer to factorize using 4 threads:

```
$JAVA_HOME/bin/java Factorize 13 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 4 0
```

To time the process and get context switching, page fault, and other stats on Linux:

```
/usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize 42039582593802342572091 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 4
/usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize 4388802055429773100203726550535118822125 CUSTOM_THREAD_COUNT_VIA_EXECUTOR_SERVICE 6
```

BigInteger documentation: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigInteger.html

Java command documentation: https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html

