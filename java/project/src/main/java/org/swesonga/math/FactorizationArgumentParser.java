package org.swesonga.math;

import java.math.BigInteger;

// https://github.com/apache/commons-cli
// https://commons.apache.org/proper/commons-cli/apidocs/org/apache/commons/cli/package-summary.html
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FactorizationArgumentParser {

    private static void showUsage() {
        System.out.println("Usage: Factorize -number integer [-mode ExecutionMode -threads threads -seed RNGSeed]");
    }

    public static FactorizationArguments parseFromStrings(String[] args) {
        final String numberOption      = "number";
        final String modeOption        = "mode";
        final String threadsOption     = "threads";
        final String seedOption        = "seed";
        final String randNumSizeOption = "randNumSize";
        final String valuesHeldOption  = "valuesHeldPerThread";
        final String progressMsgFrequencyOption  = "progressMsgFrequency";
        final String systemGCFrequencyOption  = "systemGCFrequency";

        Options options = new Options();
        options.addOption(numberOption,      true, "number to factorize. use 'rand' to generate a random number to factorize");
        options.addOption(modeOption,        true, "execution mode");
        options.addOption(threadsOption,     true, "number of threads");
        options.addOption(seedOption,        true, "random number generator seed to use when number is set to 'rand'");
        options.addOption(randNumSizeOption, true, "size in bytes of the random number generated when number is set to 'rand'");
        options.addOption(valuesHeldOption,  true, "number of processed integers each thread will add to a set to increase memory usage");
        options.addOption(progressMsgFrequencyOption,  true, "how often messages are written to the standard output");
        options.addOption(systemGCFrequencyOption,  true, "how often System.gc() is invoked. Default to off (-1) by default");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.err.println("Error parsing command line arguments.");
            showUsage();
            System.exit(-1);
        }

        if (!commandLine.hasOption(numberOption)) {
            showUsage();
            System.exit(-1);
        }

        BigInteger input = null;

        try {
            String number = commandLine.getOptionValue(numberOption);

            if (!"rand".equals(number.toLowerCase())) {
                input = new BigInteger(number);
            } else {
                long seed = 0;
                int randNumSize = 16;

                if (commandLine.hasOption(randNumSizeOption)) {
                    String randNumSizeAsStr = commandLine.getOptionValue(randNumSizeOption);
                    try {
                        randNumSize = Integer.parseInt(randNumSizeAsStr);
                    }
                    catch (NumberFormatException nfe) {
                        System.err.println("Error: " + randNumSizeAsStr + " is not a valid number of threads.");
                        System.exit(-1);
                    }
                }

                if (commandLine.hasOption(seedOption)) {
                    String seedAsStr = commandLine.getOptionValue(seedOption);
                    try {
                        seed = Long.parseLong(seedAsStr);
                    }
                    catch (NumberFormatException nfe) {
                        System.err.println("Error: " + seedAsStr + " is not a valid long value.");
                        System.exit(-1);
                    }
                }

                byte[] inputArray = FactorizationUtils.getRandomBytes(seed, randNumSize);

                FactorizationUtils.logMessage("Random number generation complete. Creating a BigInteger.");
                input = new BigInteger(inputArray).abs();

                String numberAsString = input.toString();
                FactorizationUtils.logMessage(String.format("Integer to factorize: %s (%d digits)",
                    numberAsString, numberAsString.length()));
            }
        }
        catch (NumberFormatException nfe) {
            System.err.println("Error: " + args[0] + " is not a valid base 10 number.");
            System.exit(-1);
        }

        if (input.compareTo(BigInteger.TWO) < 1) {
            System.err.println("The specified number must be greater than 2.");
            System.exit(-1);
        }

        int threads = 1;

        if (commandLine.hasOption(threadsOption)) {
            String threadsAsStr = commandLine.getOptionValue(threadsOption);

            if (threadsAsStr.toLowerCase().equals("matchcpus")) {
                threads = 0;
            } else {
                try {
                    threads = Integer.parseInt(threadsAsStr);
                }
                catch (NumberFormatException nfe) {
                    System.err.println("Error: " + threadsAsStr + " is not a valid number of threads.");
                    System.exit(-1);
                }
            }

            // Create a thread for every available processor if the user specified 0 threads.
            if (threads == 0) {
                threads = Runtime.getRuntime().availableProcessors();
            }
        }

        ExecutionMode executionMode = ExecutionMode.CUSTOM_THREAD_COUNT_VIA_THREAD_CLASS;

        if (commandLine.hasOption(modeOption)) {
            String executionModeAsStr = commandLine.getOptionValue(modeOption);
            try {
                executionMode = ExecutionMode.valueOf(executionModeAsStr);
            }
            catch (IllegalArgumentException ex) {
                System.err.println("Error: " + executionModeAsStr + " is not a valid execution mode.");
                System.exit(-1);
            }

            if (executionMode == ExecutionMode.SINGLE_THREAD && threads != 1) {
                System.err.println("Error: " + executionMode + " is not a valid execution mode when the thread count is specified.");
                System.exit(-1);
            }
        }

        int valuesHeldPerThread = 0;
        if (commandLine.hasOption(valuesHeldOption)) {
            String valuesHeldAsStr = commandLine.getOptionValue(valuesHeldOption);

            try {
                valuesHeldPerThread = Integer.parseInt(valuesHeldAsStr);
            }
            catch (NumberFormatException nfe) {
                System.err.println("Error: " + valuesHeldAsStr + " is not a valid number of values to save per thread.");
                System.exit(-1);
            }
        }

        long progressMsgFrequency = 1L << 30;
        if (commandLine.hasOption(progressMsgFrequencyOption)) {
            String progressMsgFrequencyAsStr = commandLine.getOptionValue(progressMsgFrequencyOption);

            try {
                progressMsgFrequency = Long.parseLong(progressMsgFrequencyAsStr);
            }
            catch (NumberFormatException nfe) {
                System.err.println("Error: " + progressMsgFrequencyAsStr + " is not a valid number of values to save per thread.");
                System.exit(-1);
            }
        }

        long systemGCFrequency = -1;
        if (commandLine.hasOption(systemGCFrequencyOption)) {
            String systemGCFrequencyAsStr = commandLine.getOptionValue(systemGCFrequencyOption);

            try {
                systemGCFrequency = Long.parseLong(systemGCFrequencyAsStr);
            }
            catch (NumberFormatException nfe) {
                System.err.println("Error: " + systemGCFrequencyAsStr + " is not a valid frequency for System.gc() invocations.");
                System.exit(-1);
            }
        }

        var factorizationArgs = new FactorizationArguments();
        factorizationArgs.number = input;
        factorizationArgs.threads = threads;
        factorizationArgs.executionMode = executionMode;
        factorizationArgs.progressMsgFrequency = progressMsgFrequency;
        factorizationArgs.systemGCFrequency = systemGCFrequency;
        factorizationArgs.valuesHeldPerThread = valuesHeldPerThread;

        return factorizationArgs;
    }
}
