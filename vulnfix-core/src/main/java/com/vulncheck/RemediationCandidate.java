package com.vulncheck;

import java.util.List;

public record RemediationCandidate(
        String type,
        ComponentCoordinate target,
        boolean directDependency,
        List<ComponentCoordinate> parentCandidates
) {
}