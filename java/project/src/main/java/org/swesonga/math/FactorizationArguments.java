package org.swesonga.math;

import java.math.BigInteger;

public class FactorizationArguments {
    public BigInteger number;
    public int threads;
    public int valuesHeldPerThread;
    public long progressMsgFrequency;
    public long systemGCFrequency;
    public ExecutionMode executionMode;
}
