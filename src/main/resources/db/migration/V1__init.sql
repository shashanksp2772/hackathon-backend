-- Core domain: agents, orders, reassignment suggestions.
-- Sprint 2/3 columns are added now as nullable placeholders so those
-- features arrive as backfills, not migrations.

CREATE TABLE agents (
    id                  VARCHAR(50) PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    status              VARCHAR(20)  NOT NULL CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE')),
    active_order_count  INTEGER      NOT NULL DEFAULT 0,
    current_zone        VARCHAR(50),   -- sprint 2: zone affinity
    max_capacity        INTEGER        -- sprint 2: capacity constraints
);

CREATE TABLE orders (
    id                  VARCHAR(50) PRIMARY KEY,
    description         VARCHAR(500) NOT NULL,
    assigned_agent_id   VARCHAR(50)  NOT NULL REFERENCES agents(id),
    status              VARCHAR(30)  NOT NULL CHECK (status IN ('ASSIGNED', 'REASSIGNMENT_PENDING', 'REASSIGNED', 'DELIVERED')),
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    pickup_zone         VARCHAR(50),   -- sprint 2: zone affinity
    dropoff_zone        VARCHAR(50),   -- sprint 2: zone affinity
    weight_class        VARCHAR(20),   -- sprint 2: LIGHT / HEAVY
    sla_deadline        TIMESTAMP      -- sprint 3: proactive replanning
);

CREATE INDEX idx_orders_assigned_agent_id ON orders(assigned_agent_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE reassignment_suggestions (
    id                      UUID         PRIMARY KEY,
    order_id                VARCHAR(50)  NOT NULL REFERENCES orders(id),
    recommended_agent_id    VARCHAR(50)  NOT NULL REFERENCES agents(id),
    confidence              DOUBLE PRECISION NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    reasoning               TEXT         NOT NULL,
    status                  VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    trigger_reason          VARCHAR(20)  NOT NULL CHECK (trigger_reason IN ('INITIAL', 'AGENT_OFFLINE')),
    created_at              TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_suggestions_order_id ON reassignment_suggestions(order_id);

-- Idempotency backstop (T-4 / AGT-4): only one PENDING agent-offline
-- suggestion may exist per order at a time, enforced at the DB layer
-- in addition to the application-level check.
CREATE UNIQUE INDEX uq_pending_offline_suggestion
    ON reassignment_suggestions(order_id)
    WHERE status = 'PENDING' AND trigger_reason = 'AGENT_OFFLINE';
