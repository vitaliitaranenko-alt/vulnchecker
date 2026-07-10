package com.vulncheck;

import java.util.Objects;

record VulnScannerConfiguration(SonatypeCredentials credentials) {

    VulnScannerConfiguration {
        Objects.requireNonNull(credentials, "credentials must not be null");
    }
}
