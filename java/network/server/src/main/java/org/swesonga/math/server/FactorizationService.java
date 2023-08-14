package org.swesonga.math.server;
import java.math.BigInteger;
import java.util.stream.*;

import org.swesonga.math.ExecutionMode;
import org.swesonga.math.Factorize;

public class FactorizationService {
    public static String factorize(String input) throws InterruptedException {
        ExecutionMode executionMode = ExecutionMode.CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS;
        int threads = 4;
        var number = new BigInteger(input);
        var factorizer = new Factorize(number, threads);
        factorizer.StartFactorization(executionMode);

        var primeFactors = factorizer.getPrimeFactors().stream().map(x -> x.toString()).collect(Collectors.toList());
        var result = String.join(",", primeFactors);

        return result;
    }
}
