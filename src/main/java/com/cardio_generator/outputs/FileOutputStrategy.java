package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements OutputStrategy for file output.
 * Ensures thread-safe file writing operations for different labels.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Changed variable name to camelCase as per Java naming conventions
    private String baseDirectory;

    // Changed variable name to UPPER_SNAKE_CASE because it is a constant (final) field
    public final ConcurrentHashMap<String, String> FILE_MAP = new ConcurrentHashMap<>();

    /**
     * Constructor to set the base directory for file output.
     * @param baseDirectory the base directory path
     */
    public FileOutputStrategy(String baseDirectory) {
        // Changed parameter name to camelCase and assigned it to instance variable
        this.baseDirectory = baseDirectory;
    }

    /**
     * Outputs formatted data to files, organized by labels.
     * @param patientId the ID of the patient
     * @param timestamp the timestamp of the data
     * @param label the data label
     * @param data the data to output
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Ensure directories are created if not existing
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            // Use of System.err.println for error logging; consider using a logging framework in production
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Changed variable name to camelCase
        // Computes file path only if absent, then retrieves it
        String filePath = FILE_MAP.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Use try-with-resources for automatic resource management
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (IOException e) {
            // Use of System.err.println for error logging; consider using a logging framework in production
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
