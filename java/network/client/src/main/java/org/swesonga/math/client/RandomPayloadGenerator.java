package org.swesonga.math.client;

import java.math.BigInteger;

import org.swesonga.math.FactorizationUtils;

public class RandomPayloadGenerator {
    public static String generateRandomNumber() {
        long seed = 0;
        int bytes = 8;
        byte[] array = FactorizationUtils.getRandomBytes(seed, bytes);
        var number = new BigInteger(array).abs();

        return number.toString();
    }
}
