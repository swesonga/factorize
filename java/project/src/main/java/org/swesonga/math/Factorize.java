/*
 * Simple class for factorizing arbitrarily large natural numbers.
 * This program is useful for exploring performance monitoring tools
 * using a simple app that does something non-trivial.
 *
 * Author: Saint Wesonga
 *
 * Dependencies:
 * 
 *  https://commons.apache.org/proper/commons-cli/
 * 
 * To compile from directory containing this file:
 * 
 *  export CLASSPATH=/c/java/commons-cli-1.6.0/commons-cli-1.6.0.jar:.
 *  cd java/project/src/main/java/org/swesonga/math
 *  $JAVA_HOME/bin/javac -d . PrimalityTest.java FactorizationUtils.java Factorize.java ExecutionMode.java FactorizationArguments.java FactorizationArgumentParser.java
 *
 * Sample Usage:
 *
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 65
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 45666757028904829064261583846220052692 -threads matchcpus
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 43888020554297731 -threads 4
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 4388802055429773100203726550535118822125
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 42039582593802342572091
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 42039582593802342572091 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 6
 *
 * Generate a random 13-byte integer to factorize using 4 threads:
 *
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize -number rand -randNumSize 13 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 4
 *
 * To time the process and get context switching, page fault, and other stats on Linux:
 *
 *  /usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 42039582593802342572091 -mode CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS -threads 4
 *  /usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize -number 4388802055429773100203726550535118822125 -mode CUSTOM_THREAD_COUNT_VIA_EXECUTOR_SERVICE -threads 6
 *
 * BigInteger documentation:
 *
 *  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigInteger.html
 *
 * Java Commands:
 * 
 *  https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html
 *  https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html
 * 
 */

package org.swesonga.math;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Factorize implements Runnable {
    final static BigInteger ZERO = BigInteger.ZERO;
    final static BigInteger ONE = BigInteger.ONE;
    final static BigInteger TWO = BigInteger.TWO;

    final static long CHUNK_SIZE = 1L << 16;
    final static BigInteger CHUNK_SIZE_BIG_INTEGER = new BigInteger(Long.toString(CHUNK_SIZE));
    final static int USE_EXECUTOR = 1;

    BigInteger originalInput, input, inputSqrt, sqrt;
    BigInteger chunkStride, offsetOfNextChunk;

    // Next prime factor candidate
    ThreadLocal<BigInteger> nextPrimeFactorCandidateStorage;

    ThreadLocal<Long> divisibilityTests;

    ThreadLocal<Integer> threadId;
    ThreadLocal<Integer> chunkValuesProcessed;
    private AtomicInteger threadCounter;

    private Set<BigInteger> primeFactors;
    private Set<BigInteger> unfactorizedDivisors;

    FactorizationArguments factorizationArgs;

    public Factorize(FactorizationArguments factorizationArgs) {
        this.factorizationArgs = factorizationArgs;
        this.input = factorizationArgs.number;
        this.originalInput = input;

        FactorizationUtils.logMessage("Computing square root of the input...");
        inputSqrt = input.sqrt();

        FactorizationUtils.logMessage("Square root computation complete.");
        sqrt = inputSqrt;
        this.nextPrimeFactorCandidateStorage = new ThreadLocal<>();
        this.divisibilityTests = new ThreadLocal<>();

        this.threadId = new ThreadLocal<>();
        this.chunkValuesProcessed = new ThreadLocal<>();
        this.threadCounter = new AtomicInteger();
        this.chunkStride = new BigInteger(Long.toString(factorizationArgs.threads * CHUNK_SIZE));
        this.offsetOfNextChunk = chunkStride.subtract(CHUNK_SIZE_BIG_INTEGER);

        // https://www.baeldung.com/java-concurrent-hashset-concurrenthashmap
        this.primeFactors = new ConcurrentHashMap<BigInteger, BigInteger>().keySet(ZERO);
        this.unfactorizedDivisors = new ConcurrentHashMap<BigInteger, BigInteger>().keySet(ZERO);
    }

    public boolean ExtractLargestPowerOf2() {
        int trailingZeros = FactorizationUtils.countTrailingZeros(input);
        if (trailingZeros > 0) {
            input = input.divide(TWO.pow(trailingZeros));
            inputSqrt = input.sqrt();
            sqrt = inputSqrt;

            primeFactors.add(TWO);
            FactorizationUtils.logMessage("Found a factor: 2^{" + trailingZeros + "} of " + originalInput);
            if (PrimalityTest.isPrime(input)) {
                FactorizationUtils.logMessage("Found a factor: " + input + " of " + originalInput);
                primeFactors.add(input);
                return true;
            }
        }

        return false;
    }

    public BigInteger GetNextPrimeFactorCandidate() {
        if (unfactorizedDivisors.size() == 0) {
            return ZERO;
        }

        int prev = chunkValuesProcessed.get();
        BigInteger nextPrimeFactorCandidate = nextPrimeFactorCandidateStorage.get();

        if (prev == CHUNK_SIZE) {
            prev = 0;
            nextPrimeFactorCandidate = nextPrimeFactorCandidate.add(offsetOfNextChunk);
        } else {
            nextPrimeFactorCandidate = nextPrimeFactorCandidate.add(TWO);
        }

        nextPrimeFactorCandidateStorage.set(nextPrimeFactorCandidate);
        chunkValuesProcessed.set(prev + 1);

        // https://stackoverflow.com/questions/24691862/java-assert-not-throwing-exception
        // Use the -ea flag to enable assertions, e.g.
        // java -ea Factorize 66904736496665926783368416270084639 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 1
        assert nextPrimeFactorCandidate.testBit(0) : "prime factor candidates cannot be even";
        return nextPrimeFactorCandidate;
    }

    /*
     * This does not need to be a synchronized method. The running threads that are still
     * searching for a prime factor are readers and they can keep reading. Only
     * the write operation (updating the factors left over) needs to be synchronized.
     */
    public Set<BigInteger> getUnfactorizedDivisors() {
        return unfactorizedDivisors;
    }

    public synchronized boolean factorOut(BigInteger i) {
        Set<BigInteger> newUnfactorizedDivisors = new ConcurrentHashMap<BigInteger, BigInteger>().keySet(ZERO);

        for (var number : unfactorizedDivisors) {
            var maxPowerOfi = FactorizationUtils.computeMaxPower(number, i);

            if (maxPowerOfi != ZERO) {
                var factor = i.pow(maxPowerOfi.intValue());
                BigInteger oldNumber = number;
                number = number.divide(factor);
                FactorizationUtils.logMessage("Found a factor: " + i + "^{" + maxPowerOfi + "} = " + factor + " of " + oldNumber + ". Number is now " + number);
            }

            if (PrimalityTest.isPrime(i)) {
                primeFactors.add(i);
            } else {
                newUnfactorizedDivisors.add(i);
            }

            if (PrimalityTest.isPrime(number)) {
                primeFactors.add(number);
            } else {
                newUnfactorizedDivisors.add(number);
            }
        }

        unfactorizedDivisors = newUnfactorizedDivisors;

        outputUnfactorizedDivisors();
        return unfactorizedDivisors.size() == 0;
    }

    private void outputUnfactorizedDivisors() {
        FactorizationUtils.logMessage("**************************************");
        FactorizationUtils.logMessage("Unfactorized Divisors");
        for (var number : unfactorizedDivisors) {
            String numberAsString = number.toString();
            FactorizationUtils.logMessage(String.format("%s (%d digits)",
                numberAsString, numberAsString.length()));
        }
        FactorizationUtils.logMessage("**************************************");
    }

    public Set<BigInteger> getPrimeFactors() {
        return primeFactors;
    }

    public void factorize() {
        int currentThreadCounter = threadCounter.getAndIncrement();
        // Start at 3, exclude even numbers from chunk size
        long startingNumberForThread = 1 + currentThreadCounter * CHUNK_SIZE * 2;

        FactorizationUtils.logMessage(String.format("Thread %3d starting candidate: %15d", currentThreadCounter, startingNumberForThread));

        threadId.set(currentThreadCounter);
        chunkValuesProcessed.set(0);
        divisibilityTests.set(0L);
        nextPrimeFactorCandidateStorage.set(new BigInteger(Long.toString(startingNumberForThread)));

        BigInteger i = GetNextPrimeFactorCandidate();
        Set<BigInteger> prevUnfactorizedDivisors = null;
        BigInteger[] unfactorizedDivisors = new BigInteger[0];

        var setOfAllValuesProcessed = new HashSet<BigInteger>();
        if (setOfAllValuesProcessed.size() < factorizationArgs.valuesHeldPerThread) {
            setOfAllValuesProcessed.add(i);
        }

        while (i.compareTo(sqrt) <= 0) {
            long completedDivisibilityTests = divisibilityTests.get();
            divisibilityTests.set(completedDivisibilityTests + 1);
            boolean showPeriodicMessages = completedDivisibilityTests % factorizationArgs.progressMsgFrequency == 0;
            boolean runSystemGC = factorizationArgs.systemGCFrequency > 0 && completedDivisibilityTests % factorizationArgs.systemGCFrequency == 0;
            boolean foundFactor = false;

            Set<BigInteger> currUnfactorizedDivisors = getUnfactorizedDivisors();

            // Create an array from the set of unfactorized divisors to avoid a bottleneck
            // in java.util.concurrent.ConcurrentHashMap$KeySetView.iterator()
            if (prevUnfactorizedDivisors != currUnfactorizedDivisors) {
                unfactorizedDivisors = currUnfactorizedDivisors.toArray(new BigInteger[0]);
                prevUnfactorizedDivisors = currUnfactorizedDivisors;
            }

            for (int j = 0; j < unfactorizedDivisors.length; j++) {
                BigInteger number = unfactorizedDivisors[j];
                if (showPeriodicMessages) {
                    String numberAsString = number.toString();
                    String iAsString = i.toString();

                    String message;
                    if (factorizationArgs.valuesHeldPerThread > 0) {
                        message = String.format("Testing divisibility of %s (%d digits) by %15s (%d digits) with %d values held",
                        numberAsString, numberAsString.length(), iAsString, iAsString.length(), setOfAllValuesProcessed.size());
                    } else {
                        message = String.format("Testing divisibility of %s (%d digits) by %15s (%d digits)",
                        numberAsString, numberAsString.length(), iAsString, iAsString.length());
                    }
                    FactorizationUtils.logMessage(message);
                }

                if (runSystemGC) {
                    // System.out.println("Running System.gc()");
                    System.gc();
                }

                if (number.remainder(i).compareTo(ZERO) == 0) {
                    if (PrimalityTest.isPrime(i)) {
                        primeFactors.add(i);
                    }

                    foundFactor = true;
                    break;
                }
            }

            if (foundFactor) {
                // break if factorization is complete
                if (factorOut(i)) {
                    break;
                }
            }

            i = GetNextPrimeFactorCandidate();

            if (setOfAllValuesProcessed.size() < factorizationArgs.valuesHeldPerThread) {
                setOfAllValuesProcessed.add(i);
            }

            // break if another thread completed the factorization
            if (i.compareTo(ZERO) == 0) {
                break;
            }
        }

        FactorizationUtils.logMessage("Thread factorization tasks complete.");
    }

    public boolean validate() {
        if (primeFactors.size() > 1) {
            FactorizationUtils.logMessage("Prime factors:");

            BigInteger product = ONE;
            for (var number : primeFactors) {
                var maxPower = FactorizationUtils.computeMaxPower(originalInput, number);
                var maxPowerComputed = number.pow(maxPower.intValue());

                product = product.multiply(maxPowerComputed);
                System.out.println(number + "^{" + maxPower + "} = " + maxPowerComputed);
            }

            if (originalInput.compareTo(product) != 0) {
                System.err.println("Invalid computation! Could probabilistic primality tests be at fault? Found: " + product + " but expected " + originalInput);
                return false;
            } else {
                FactorizationUtils.logMessage("Validation complete.");
            }
        } else if (!PrimalityTest.isPrime(originalInput)) {
            System.err.println("Invalid computation! Could not factorize the input");
            return false;
        }

        return true;
    }

    public void LogCompletion() {
        long totalPrimeFactors = primeFactors.size();
        if (totalPrimeFactors == 0) {
            FactorizationUtils.logMessage(originalInput + " is prime.\n");
        }
        else {
            FactorizationUtils.logMessage(originalInput + " is composite. Found " + totalPrimeFactors + " prime factors. Checked up to floor(sqrt(" + input + ")) = "
                                + inputSqrt + "\n");
        }
    }

    public void run() {
        factorize();
        FactorizationUtils.logMessage("Thread completed.");
    }

    private void LaunchThreadsManually(int numThreads) throws InterruptedException {
        var threads = new ArrayList<Thread>();

        for (int i = 0; i < numThreads; i++) {
            var thread = new Thread(this, "Factorization Thread " + i);
            threads.add(thread);

            thread.start();
        }

        for (int i = 0; i < numThreads; i++) {
            var thread = threads.get(i);
            thread.join();
        }
    }

    private void LaunchThreadsViaExecutor(int numThreads) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            pool.execute(this);
        }

        pool.awaitTermination(365 * 24 * 3600, TimeUnit.SECONDS);
        pool.shutdown();
    }

    public void StartFactorization(ExecutionMode executionMode) throws InterruptedException {
        FactorizationUtils.logMessage("Bit length of the input: " + input.bitLength());

        boolean factorizationComplete = input.testBit(0) ? PrimalityTest.isPrime(input) : ExtractLargestPowerOf2();

        if (!factorizationComplete) {
            unfactorizedDivisors.add(input);
            FactorizationUtils.logMessage("Testing divisibility by odd numbers up to floor(sqrt(" + input + ")) = " + sqrt);

            switch (executionMode) {
                case SINGLE_THREAD:
                    FactorizationUtils.logMessage("Using main thread to run tasks.");
                    run();
                    break;
                case CUSTOM_THREAD_COUNT_VIA_EXECUTOR_SERVICE:
                    FactorizationUtils.logMessage("Using executor service to run tasks.");
                    LaunchThreadsViaExecutor(factorizationArgs.threads);
                    break;
                case CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS:
                    FactorizationUtils.logMessage("Using Thread.start to run tasks.");
                    LaunchThreadsManually(factorizationArgs.threads);
                    break;
            }
        }

        if (validate()) {
            LogCompletion();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.nanoTime();

        FactorizationArguments factorizationArgs = FactorizationArgumentParser.parseFromStrings(args);

        FactorizationUtils.logMessage(String.format("Using %d threads.", factorizationArgs.threads));

        var factorize = new Factorize(factorizationArgs);

        factorize.StartFactorization(factorizationArgs.executionMode);
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        FactorizationUtils.logMessage("Running time: " + ((double)(timeElapsed/1000000))/1000.0 + " seconds");
    }
}