package com.vulncheck;

public record ComponentCoordinate(
        String groupId,
        String artifactId,
        String version
) {

    public String toPkg(String pkgManager) {
        return "pkg:%s/%s@%s".formatted(groupId, artifactId, version);
    }
}
