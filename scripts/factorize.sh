#!/bin/bash
#
lower_lim=3
upper_lim=100

if [ $# -gt 1 ]
then
    lower_lim=$1
    upper_lim=$2
elif [ $# -gt 0 ]
then
    upper_lim=$1
fi

# Download JDKs from https://adoptium.net/temurin/releases/?version=20
#
# export JAVA_HOME=/c/java/binaries/jdk/x64/jdk-20+36
# export JAVA_HOME=~/java/binaries/jdk/x64/jdk-20+36
# export JAVA_HOME=~/java/binaries/jdk/aarch64/jdk-17.0.8+7/Contents/Home

$JAVA_HOME/bin/javac Factorize.java

for ((i=$lower_lim; i<=$upper_lim; i++))
do
    echo "Factorizing $i"
    $JAVA_HOME/bin/java Factorize $i
    # Use the next line to generate a random number with $i bytes
    # $JAVA_HOME/bin/java Factorize $i CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 4 0
    echo "---------------------------------------------------"
done
