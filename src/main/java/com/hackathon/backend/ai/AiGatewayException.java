package com.hackathon.backend.ai;

/**
 * Any failure between "prompt sent" and "usable recommendation in hand" —
 * network/timeout, malformed JSON, a hallucinated agent id, or a payload
 * that fails Bean Validation. Always caught by {@link AiRoutingStrategy},
 * never allowed to propagate past this package.
 */
class AiGatewayException extends RuntimeException {

    private final AiFailureReason reason;

    AiGatewayException(AiFailureReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    AiGatewayException(AiFailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    AiFailureReason reason() {
        return reason;
    }
}
