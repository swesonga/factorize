package org.swesonga.math;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FactorizationUtils {
    final static BigInteger ZERO = BigInteger.ZERO;
    final static BigInteger ONE = BigInteger.ONE;
    final static BigInteger TWO = BigInteger.TWO;
    final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    static int countTrailingZeros(BigInteger number) {
        // Handle special case
        if (number.compareTo(BigInteger.ZERO) == 0) {
            return 0;
        }

        int zeros = 0;

        while (number.remainder(TWO).compareTo(ZERO) == 0) {
            zeros++;
            number = number.divide(TWO);
        }

        return zeros;
    }

    static BigInteger computeMaxPower(BigInteger a, BigInteger b)
    {
        BigInteger power = ZERO;

        if (a.compareTo(ZERO) == 1 && b.compareTo(ZERO) == 1) {
            // A binary search approach would be more efficient
            while (a.remainder(b).compareTo(ZERO) == 0) {
                power = power.add(ONE);
                a = a.divide(b);
            }
        }

        return power;
    }

    // public to allow it to be accessed from outside the package
    public static byte[] getRandomBytes(long seed, int arrayLength) {
        FactorizationUtils.logMessage(String.format("Creating array of %d bytes for the random number.", arrayLength));
        var bytes = new byte[arrayLength];
        
        FactorizationUtils.logMessage("Generating the random number.");
        var random = new Random();

        if (seed != 0) {
            FactorizationUtils.logMessage(String.format("Setting the seed to %d.", seed));
            random.setSeed(seed);
        }
        random.nextBytes(bytes);

        return bytes;
    }

    static void printDate()
    {
        String formattedDate = dateFormat.format(new Date(System.currentTimeMillis()));
        System.out.print(formattedDate);
    }

    static synchronized void logMessage(String message, boolean showThreadId)
    {
        printDate();
        
        if (showThreadId) {
            // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter.html
            message = String.format("tid %3d %s", Thread.currentThread().getId(), message);
        }
        System.out.println(message);
    }

    static void logMessage(String message)
    {
        logMessage(message, true);
    }
}