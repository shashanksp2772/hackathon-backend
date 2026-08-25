-- Seed data adapted from the brief's Addendum A: 5 agents, 8 pre-assigned orders.

-- active_order_count must match the number of orders actually assigned to
-- each agent below (ORD-001/002/008 -> AGT-001, ORD-003/007 -> AGT-003,
-- ORD-004/005/006 -> AGT-005) - it's a denormalized counter, not derived
-- live from the orders table, so it has to be seeded consistently by hand.
INSERT INTO agents (id, name, active_order_count, status) VALUES
    ('AGT-001', 'Priya Sharma',  3, 'BUSY'),
    ('AGT-002', 'Rahul Verma',   0, 'AVAILABLE'),
    ('AGT-003', 'Ananya Iyer',   2, 'BUSY'),
    ('AGT-004', 'Kiran Nair',    0, 'AVAILABLE'),
    ('AGT-005', 'Deepak Mehta',  3, 'BUSY');

INSERT INTO orders (id, description, assigned_agent_id, status, created_at) VALUES
    ('ORD-001', 'Electronics - Koramangala to Indiranagar', 'AGT-001', 'ASSIGNED', now()),
    ('ORD-002', 'Groceries - HSR Layout to BTM',            'AGT-001', 'ASSIGNED', now()),
    ('ORD-003', 'Pharma - Whitefield to Marathahalli',      'AGT-003', 'ASSIGNED', now()),
    ('ORD-004', 'Documents - MG Road to Jayanagar',         'AGT-005', 'ASSIGNED', now()),
    ('ORD-005', 'Food - Bellandur to Electronic City',      'AGT-005', 'ASSIGNED', now()),
    ('ORD-006', 'Apparel - Malleshwaram to Rajajinagar',    'AGT-005', 'ASSIGNED', now()),
    ('ORD-007', 'Books - Banashankari to JP Nagar',         'AGT-003', 'ASSIGNED', now()),
    ('ORD-008', 'Hardware - Peenya to Yeshwanthpur',        'AGT-001', 'ASSIGNED', now());
