package org.swesonga.math;

import java.math.BigInteger;

public class PrimalityTest {
    static boolean isPrime(BigInteger number) {
        if (!number.isProbablePrime(500)) {
            return false;
        }

        // TODO: perform an actual primality test here.
        // https://annals.math.princeton.edu/wp-content/uploads/annals-v160-n2-p12.pdf
        return true;
    }
}
