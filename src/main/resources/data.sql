INSERT INTO effect_types (code, name, icon, color, display_order, created_at, updated_at)
SELECT 'COOLING', '냉각력', 'snowflake', 'BLUE', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM effect_types WHERE code = 'COOLING');

INSERT INTO effect_types (code, name, icon, color, display_order, created_at, updated_at)
SELECT 'MENTAL', '정신력', 'brain', 'PURPLE', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM effect_types WHERE code = 'MENTAL');

INSERT INTO effect_types (code, name, icon, color, display_order, created_at, updated_at)
SELECT 'STAMINA', '체력', 'lightning', 'GREEN', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM effect_types WHERE code = 'STAMINA');

INSERT INTO effect_types (code, name, icon, color, display_order, created_at, updated_at)
SELECT 'MONEY', '자본력', 'coin', 'ORANGE', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM effect_types WHERE code = 'MONEY');

INSERT INTO effect_types (code, name, icon, color, display_order, created_at, updated_at)
SELECT 'PATIENCE', '인내력', 'fire', 'RED', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM effect_types WHERE code = 'PATIENCE');
