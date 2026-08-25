package com.hackathon.backend.ai;

/**
 * Why an AI recommendation attempt was abandoned in favor of the rule-based
 * fallback. Kept distinct from a generic error message so failures are
 * loggable and, if needed later, countable per-reason.
 */
enum AiFailureReason {
    TIMEOUT,
    PROVIDER_ERROR,
    MALFORMED_RESPONSE,
    HALLUCINATED_AGENT,
    VALIDATION_FAILED
}
