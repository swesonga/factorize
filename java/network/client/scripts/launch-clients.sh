#!/bin/bash
#
# Sample usage
#
#   ./scripts/launch-clients.sh 5 localhost 12345 987654321987654321
#

num_clients=1
value_to_factorize=0
host=localhost
port=12345

if [ $# -gt 0 ]
then
    num_clients=$1
    if [ $# -gt 1 ]
    then
        host=$2
    fi
    if [ $# -gt 2 ]
    then
        port=$3
    fi
    if [ $# -gt 3 ]
    then
        value_to_factorize=$4
    fi
fi

# Download JDKs from
#     https://adoptium.net/temurin/releases/?version=20
#     https://learn.microsoft.com/en-us/java/openjdk/download
#
# export JAVA_HOME=/c/java/binaries/jdk/x64/jdk-20+36
# export JAVA_HOME=~/java/binaries/jdk/x64/jdk-20+36
# export JAVA_HOME=~/java/binaries/jdk/aarch64/jdk-17.0.8+7/Contents/Home

echo "Launching $num_clients clients"

for ((i=0; i<$num_clients; i++))
do
    $JAVA_HOME/bin/java -jar target/factorization-client-0.1.0-jar-with-dependencies.jar $host $port $value_to_factorize &
    #pid=$!
    #echo "Launched PID $pid"
done
