package com.vulncheck;

public record SonatypeCredentials(
        String username,
        String password,
        String apiKey,
        String baseUrl,
        String requestTimeout,
        String scanTimeout
) {
}
