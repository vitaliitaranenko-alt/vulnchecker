package com.vulncheck;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Properties;

final class VulnScannerConfigStore {

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String BASE_URL = "sonatype.base-url";
    private static final String USERNAME = "sonatype.username";
    private static final String PASSWORD = "sonatype.password";
    private static final String API_KEY = "sonatype.api-key";
    private static final String REQUEST_TIMEOUT = "sonatype.request-timeout";
    private static final String SCAN_TIMEOUT = "sonatype.scan-timeout";

    Path save(VulnScannerConfiguration configuration, boolean overwrite) {
        Path configPath = configPath();
        try {
            Files.createDirectories(configPath.getParent());
            if (!overwrite && Files.exists(configPath)) {
                throw new IllegalStateException("Configuration already exists at " + configPath + ". Use --force to overwrite it.");
            }
            setOwnerOnlyDirectoryPermissions(configPath.getParent());

            Properties properties = new Properties();
            SonatypeCredentials credentials = configuration.credentials();
            properties.setProperty(BASE_URL, credentials.baseUrl());
            properties.setProperty(USERNAME, credentials.username());
            properties.setProperty(PASSWORD, credentials.password());
            properties.setProperty(API_KEY, nullToEmpty(credentials.apiKey()));
            properties.setProperty(REQUEST_TIMEOUT, nullToEmpty(credentials.requestTimeout()));
            properties.setProperty(SCAN_TIMEOUT, nullToEmpty(credentials.scanTimeout()));

            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "VulnScanner local configuration");
            }
            setOwnerOnlyFilePermissions(configPath);
            return configPath;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save configuration to " + configPath, e);
        }
    }

    VulnScannerConfiguration load() {
        Path configPath = configPath();
        if (!Files.isRegularFile(configPath)) {
            throw new IllegalStateException("Configuration not found at " + configPath + ". Run 'vulnfix init' first.");
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read configuration from " + configPath, e);
        }

        return new VulnScannerConfiguration(
                new SonatypeCredentials(
                        required(properties, USERNAME),
                        required(properties, PASSWORD),
                        properties.getProperty(API_KEY),
                        required(properties, BASE_URL),
                        emptyToNull(properties.getProperty(REQUEST_TIMEOUT)),
                        emptyToNull(properties.getProperty(SCAN_TIMEOUT))
                )
        );
    }

    private Path configPath() {
        return Path.of(System.getProperty("user.home"), ".vulnscanner", CONFIG_FILE_NAME);
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration value: " + key);
        }
        return value;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void setOwnerOnlyDirectoryPermissions(Path path) {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            ));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows and file systems without POSIX permissions use their default protection.
        }
    }

    private void setOwnerOnlyFilePermissions(Path path) {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
            ));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows and file systems without POSIX permissions use their default protection.
        }
    }
}
