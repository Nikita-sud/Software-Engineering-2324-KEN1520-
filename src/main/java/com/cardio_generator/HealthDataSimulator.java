package com.cardio_generator;

// first go java libraries 
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;

import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;
/**
 * HealthDataSimulator simulates health data for a specified number of patients and outputs the data to a specified strategy (console, file, websocket, or TCP).
 */
public class HealthDataSimulator {

    private static HealthDataSimulator instance;
    private static int DEFAULT_PATIENT_COUNT = 50;
    private static ScheduledExecutorService scheduler;
    private static OutputStrategy outputStrategy = new ConsoleOutputStrategy();
    private static final Random RANDOM = new Random();

    /**
     * Private constructor to prevent instantiation.
     */
    private HealthDataSimulator() {}

    /**
     * Returns the singleton instance of HealthDataSimulator.
     *
     * @return the singleton instance of HealthDataSimulator.
     */
    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    /**
     * Sets up and starts the health data simulation based on the provided arguments.
     *
     * @param args command line arguments for configuring the simulation.
     * @throws IOException if an I/O error occurs during setup.
     */
    public void startSimulation(String[] args) throws IOException {
        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(DEFAULT_PATIENT_COUNT * 4);

        List<Integer> patientIds = initializePatientIds(DEFAULT_PATIENT_COUNT);
        Collections.shuffle(patientIds);

        scheduleTasksForPatients(patientIds);
    }

    /**
     * Parses the command-line arguments to configure the simulation parameters.
     *
     * @param args command line arguments.
     * @throws IOException if an error occurs while processing the output argument.
     */
    private void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            DEFAULT_PATIENT_COUNT = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Invalid number of patients. Using default value: " + DEFAULT_PATIENT_COUNT);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                // Initialize your WebSocket output strategy here
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for WebSocket output. Please specify a valid port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                // Initialize your TCP socket output strategy here
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for TCP output. Please specify a valid port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
     * Prints the help message with usage instructions.
     */
    private void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println("  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");
        System.out.println("  --output <type>          Define the output method. Options are:");
        System.out.println("                             'console' for console output,");
        System.out.println("                             'file:<directory>' for file output,");
        System.out.println("                             'websocket:<port>' for WebSocket output,");
        System.out.println("                             'tcp:<port>' for TCP socket output.");
        System.out.println("Example:");
        System.out.println("  java HealthDataSimulator --patient-count 100 --output websocket:8080");
        System.out.println("  This command simulates data for 100 patients and sends the output to WebSocket clients connected to port 8080.");
    }

    /**
     * Initializes a list of patient IDs.
     *
     * @param patientCount The number of patients.
     * @return A shuffled list of patient IDs.
     */
    private List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    /**
     * Schedules the simulation tasks for each patient.
     *
     * @param patientIds The list of patient IDs.
     */
    private void scheduleTasksForPatients(List<Integer> patientIds) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(DEFAULT_PATIENT_COUNT);
        BloodSaturationDataGenerator bloodSaturationDataGenerator = new BloodSaturationDataGenerator(DEFAULT_PATIENT_COUNT);
        BloodPressureDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(DEFAULT_PATIENT_COUNT);
        BloodLevelsDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(DEFAULT_PATIENT_COUNT);
        AlertGenerator alertGenerator = new AlertGenerator(DEFAULT_PATIENT_COUNT);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> alertGenerator.generate(patientId, outputStrategy), 20, TimeUnit.SECONDS);
        }
    }

    /**
     * Schedules a recurring task with a random initial delay.
     *
     * @param task   The task to schedule.
     * @param period The period between successive executions.
     * @param timeUnit The time unit of the period.
     */
    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, RANDOM.nextInt(5), period, timeUnit);
    }
}
