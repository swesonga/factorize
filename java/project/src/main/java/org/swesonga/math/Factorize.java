/*
 * Simple class for factorizing arbitrarily large natural numbers.
 * This program is useful for exploring performance monitoring tools
 * using a simple app that does something non-trivial.
 *
 * Author: Saint Wesonga
 *
 * To compile from directory containing this file:
 * 
 *  $JAVA_HOME/bin/javac -d . PrimalityTest.java FactorizationUtils.java Factorize.java
 *
 * Sample Usage:
 *
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize 65
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize 438880205542
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize 43888020554297731
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize 4388802055429773100203726550535118822125
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize 42039582593802342572091
 *  $JAVA_HOME/bin/java org.swesonga.math.Factorize 42039582593802342572091 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 6
 *
 * Generate a random 13-byte integer to factorize using 4 threads:
 *
 *  $JAVA_HOME/bin/java Factorize 13 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 4 0
 *
 * To time the process and get context switching, page fault, and other stats on Linux:
 *
 *  /usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize 42039582593802342572091 CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS 4
 *  /usr/bin/time -v $JAVA_HOME/bin/java org.swesonga.math.Factorize 4388802055429773100203726550535118822125 CUSTOM_THREAD_COUNT_VIA_EXECUTOR_SERVICE 6
 *
 * BigInteger documentation:
 *
 *  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigInteger.html
 *
 * Java Command:
 * 
 *  https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html
 * 
 */

package org.swesonga.math;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Set;

public class Factorize implements Runnable {
    final static BigInteger ZERO = BigInteger.ZERO;
    final static BigInteger ONE = BigInteger.ONE;
    final static BigInteger TWO = BigInteger.TWO;

    final static int CHUNK_SIZE = 1024;
    final static BigInteger CHUNK_SIZE_BIG_INTEGER = new BigInteger(Integer.toString(CHUNK_SIZE));
    final static int USE_EXECUTOR = 1;

    BigInteger originalInput, input, inputSqrt, sqrt;
    BigInteger chunkStride, offsetOfNextChunk;

    // Next prime factor candidate
    ThreadLocal<BigInteger> nextPrimeFactorCandidateStorage;

    AtomicLong divisibilityTests;

    private long progressMsgFrequency = 1L << 31;
    private int factorizationThreadCount;
    ThreadLocal<Integer> threadId;
    ThreadLocal<Integer> chunkValuesProcessed;
    private AtomicInteger threadCounter;

    private Set<BigInteger> primeFactors;
    private Set<BigInteger> unfactorizedDivisors;

    public Factorize(BigInteger input, int factorizationThreadCount) {
        this.input = input;
        this.originalInput = input;

        FactorizationUtils.logMessage("Computing square root of the input...");
        inputSqrt = input.sqrt();

        FactorizationUtils.logMessage("Square root computation complete.");
        sqrt = inputSqrt;
        this.nextPrimeFactorCandidateStorage = new ThreadLocal<>();
        this.divisibilityTests = new AtomicLong();
        this.factorizationThreadCount = factorizationThreadCount;

        this.threadId = new ThreadLocal<>();
        this.chunkValuesProcessed = new ThreadLocal<>();
        this.threadCounter = new AtomicInteger();
        this.chunkStride = new BigInteger(Integer.toString(factorizationThreadCount * CHUNK_SIZE));
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

    public synchronized Set<BigInteger> getUnfactorizedDivisors() {
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
            System.out.println(number);
        }
        FactorizationUtils.logMessage("**************************************");
    }

    private void LaunchThreadsManually(int numThreads) throws InterruptedException {
        var threads = new ArrayList<Thread>();

        for (int i = 0; i < numThreads - 1; i++) {
            var thread = new Thread(this);
            threads.add(thread);

            thread.start();
        }
        
        run();

        for (int i = 0; i < numThreads - 1; i++) {
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
                    LaunchThreadsViaExecutor(factorizationThreadCount);
                    break;
                case CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS:
                    FactorizationUtils.logMessage("Using Thread.start to run tasks.");
                    LaunchThreadsManually(factorizationThreadCount);
                    break;
            }
        }

        if (validate()) {
            LogCompletion();
        }
    }

    public void factorize() {
        int currentThreadCounter = threadCounter.getAndIncrement();
        // Start at 3, exclude even numbers from chunk size
        int startingNumberForThread = 1 + currentThreadCounter * CHUNK_SIZE * 2;

        FactorizationUtils.logMessage("Thread " + currentThreadCounter + " starting candidate: " + startingNumberForThread);

        threadId.set(currentThreadCounter);
        chunkValuesProcessed.set(0);
        nextPrimeFactorCandidateStorage.set(new BigInteger(Integer.toString(startingNumberForThread)));

        BigInteger i = GetNextPrimeFactorCandidate();

        while (i.compareTo(sqrt) <= 0) {
            boolean showPeriodicMessages = divisibilityTests.getAndIncrement() % progressMsgFrequency == 0;
           boolean foundFactor = false;
           for (var number : getUnfactorizedDivisors()) {
                if (showPeriodicMessages) {
                    FactorizationUtils.logMessage("Testing divisibility of " + number + " by " + i);
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

    public void run() {
        factorize();
        FactorizationUtils.logMessage("Thread completed.");
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

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Usage: Factorize [Number [ExecutionMode [Threads [RNGSeed]]]]");
            return;
        }

        BigInteger input;

        try {
            input = new BigInteger(args[0]);
        }
        catch (NumberFormatException nfe) {
            System.err.println("Error: " + args[0] + " is not a valid base 10 number.");
            return;
        }

        if (input.compareTo(TWO) < 1) {
            System.err.println("The specified number must be greater than 2.");
            return;
        }

        int threads = 1;

        ExecutionMode executionMode = ExecutionMode.SINGLE_THREAD;
        if (args.length > 1) {
            var executionModeAsStr = args[1];
            try {
                executionMode = ExecutionMode.valueOf(executionModeAsStr);
            }
            catch (IllegalArgumentException ex) {
                System.err.println("Error: " + executionModeAsStr + " is not a valid execution mode.");
                return;
            }

            if (args.length > 3) {
                int inputAsIntValue = 0;
                try {
                    inputAsIntValue = input.intValueExact();
                }
                catch (ArithmeticException ex) {
                    System.err.println("Error: invalid random array size.");
                    return;
                }

                var seedAsStr = args[3];
                long seed = 0;
                try {
                    seed = Long.parseLong(seedAsStr);
                    System.out.println("Using " + threads + " threads.");
                }
                catch (NumberFormatException nfe) {
                    System.err.println("Error: " + seedAsStr + " is not a valid long value.");
                    return;
                }

                byte[] inputArray = FactorizationUtils.getRandomBytes(seed, inputAsIntValue);

                FactorizationUtils.logMessage("Random number generation complete. Creating a BigInteger.");
                input = new BigInteger(inputArray).abs();
            }

            if (executionMode != ExecutionMode.SINGLE_THREAD && args.length > 2) {
                var threadsAsStr = args[2];
                try {
                    threads = Integer.parseInt(threadsAsStr);
                    System.out.println("Using " + threads + " threads.");
                }
                catch (NumberFormatException nfe) {
                    System.err.println("Error: " + threadsAsStr + " is not a valid number of threads.");
                    return;
                }
            }
        }

        var factorize = new Factorize(input, threads);

        factorize.StartFactorization(executionMode);
    }
}