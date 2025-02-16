# factorize
factorize is a simple program for factorizing arbitrarily large natural numbers.
This program is useful for exploring performance monitoring tools
using a simple app that does something non-trivial.

## Dependencies:

Download the [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) using these commands:

```
mkdir -p ~/java
cd ~/java
curl -Lo commons-cli-1.9.0-bin.tar.gz https://dlcdn.apache.org//commons/cli/binaries/commons-cli-1.9.0-bin.tar.gz
tar xzf commons-cli-1.9.0-bin.tar.gz
```

# Compilation
To compile in bash from directory containing this file:

```
cd java/project/src/main/java/org/swesonga/math
export CLASSPATH=~/java/commons-cli-1.9.0/commons-cli-1.9.0.jar:.
export JAVA_HOME=~/java/binaries/jdk/x64/jdk-21.0.2+13
export JAVA_HOME=~/java/forks/openjdk/jdk/build/linux-x86_64-server-slowdebug/jdk
$JAVA_HOME/bin/javac -d . PrimalityTest.java FactorizationUtils.java Factorize.java ExecutionMode.java FactorizationArguments.java FactorizationArgumentParser.java

# Run the factorization program
$JAVA_HOME/bin/java org.swesonga.math.Factorize -threads matchcpus -number 4388802055429773100203726550535118822125

# Alternative that does not depend on the CLASSPATH environment variable
$JAVA_HOME/bin/java -cp ~/java/commons-cli-1.9.0/commons-cli-1.9.0.jar:. org.swesonga.math.Factorize -number 4388802055429773100203726550535118822125 -threads matchcpus
```

In the Windows command prompt:

```
cd java/project/src/main/java/org/swesonga/math
set CLASSPATH=C:/java/commons-cli-1.9.0/commons-cli-1.9.0.jar;.
set JAVA_HOME=C:/java/binaries/jdk/x64/jdk-21.0.1+12
set JAVA_HOME=C:/java/forks/openjdk/jdk/build/windows-x86_64-server-slowdebug/jdk

%JAVA_HOME%/bin/javac -d . PrimalityTest.java FactorizationUtils.java Factorize.java ExecutionMode.java
%JAVA_HOME%/bin/java org.swesonga.math.Factorize -threads matchcpus -number 4388802055429773100203726550535118822125

:: Alternative that does not depend on the CLASSPATH environment variable
%JAVA_HOME%/bin/java -cp C:/java/commons-cli-1.9.0/commons-cli-1.9.0.jar;. org.swesonga.math.Factorize -number 4388802055429773100203726550535118822125 -threads matchcpus
```

To compile using Maven:

```
mvn package
```

# Execution
To run from the executable JAR file:

```
$JAVA_HOME/bin/java -jar target/factorize-1.0.0-jar-with-dependencies.jar -number 4388802055429773100203728822125 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 6
```

More usage examples:

```
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number 65
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number 438880205542 -threads matchcpus
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number 43888020554297731 -threads 4
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number 4388802055429773100203726550535118822125
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number 42039582593802342572091
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number 42039582593802342572091 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 6
```

To generate a random 13-byte integer to factorize using 4 threads:

```
$JAVA_HOME/bin/java org.swesonga.math.Factorize -number rand -randNumSize 13 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 4
```

To time the process and get context switching, page fault, and other stats on Linux:

```
/usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 42039582593802342572091 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 4
/usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 4388802055429773100203726550535118822125 -mode CUSTOM_THREAD_COUNT_VIA_EXECUTOR_SERVICE -threads 6
```

# Networked factorization

Factorization is a problem that can be solved in a distributed manner. Multiple clients can connect to a server, which then distributes the trial
division work to the clients. To use this model, first start the server:

```
$JAVA_HOME/bin/java -jar target/factorization-server-0.1.0-jar-with-dependencies.jar -number 12345
```

Open another terminal and then start a client by running:

```
$JAVA_HOME/bin/java -jar target/factorization-client-0.1.0-jar-with-dependencies.jar localhost -number 12345
```

Note: the client and server implementations are WIP.

# Documentation
BigInteger documentation: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigInteger.html

Java command documentation: https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html
