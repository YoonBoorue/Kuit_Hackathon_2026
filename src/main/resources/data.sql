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

INSERT INTO users (nickname, created_at, updated_at)
SELECT '씨앗유저1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE nickname = '씨앗유저1');

INSERT INTO users (nickname, created_at, updated_at)
SELECT '씨앗유저2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE nickname = '씨앗유저2');

INSERT INTO users (nickname, created_at, updated_at)
SELECT '씨앗유저3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE nickname = '씨앗유저3');

INSERT INTO users (nickname, created_at, updated_at)
SELECT '씨앗유저4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE nickname = '씨앗유저4');

INSERT INTO survival_cards (
    author_user_id, title, description, recommended_situation, difficulty,
    primary_effect_type_id, status, created_at, updated_at
)
SELECT u.id, '얼음물 루틴', '물병을 얼려두고 외출할 때 하나씩 챙긴다.', '폭염 등교길', 2, e.id, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
JOIN effect_types e ON e.code = 'COOLING'
WHERE u.nickname = '씨앗유저1'
  AND NOT EXISTS (
      SELECT 1
      FROM survival_cards c
      WHERE c.author_user_id = u.id
        AND c.title = '얼음물 루틴'
  );

INSERT INTO survival_cards (
    author_user_id, title, description, recommended_situation, difficulty,
    primary_effect_type_id, status, created_at, updated_at
)
SELECT u.id, '삼분 멍때리기', '덥고 짜증날 때 알람을 맞추고 아무것도 하지 않는다.', '번아웃 직전', 1, e.id, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
JOIN effect_types e ON e.code = 'MENTAL'
WHERE u.nickname = '씨앗유저2'
  AND NOT EXISTS (
      SELECT 1
      FROM survival_cards c
      WHERE c.author_user_id = u.id
        AND c.title = '삼분 멍때리기'
  );

INSERT INTO survival_cards (
    author_user_id, title, description, recommended_situation, difficulty,
    primary_effect_type_id, status, created_at, updated_at
)
SELECT u.id, '계단 대신 그늘', '조금 돌아가도 그늘진 길을 골라 체력을 아낀다.', '점심 이동', 2, e.id, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
JOIN effect_types e ON e.code = 'STAMINA'
WHERE u.nickname = '씨앗유저3'
  AND NOT EXISTS (
      SELECT 1
      FROM survival_cards c
      WHERE c.author_user_id = u.id
        AND c.title = '계단 대신 그늘'
  );

INSERT INTO survival_cards (
    author_user_id, title, description, recommended_situation, difficulty,
    primary_effect_type_id, status, created_at, updated_at
)
SELECT u.id, '편의점 얼음컵', '커피 대신 얼음컵과 보리차로 저렴하게 버틴다.', '텅장인 날', 1, e.id, 'SENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
JOIN effect_types e ON e.code = 'MONEY'
WHERE u.nickname = '씨앗유저4'
  AND NOT EXISTS (
      SELECT 1
      FROM survival_cards c
      WHERE c.author_user_id = u.id
        AND c.title = '편의점 얼음컵'
  );

INSERT INTO card_effects (card_id, effect_type_id, level, display_order, created_at, updated_at)
SELECT c.id, c.primary_effect_type_id, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저1'
  AND c.title = '얼음물 루틴'
  AND NOT EXISTS (
      SELECT 1
      FROM card_effects ce
      WHERE ce.card_id = c.id
        AND ce.display_order = 1
  );

INSERT INTO card_effects (card_id, effect_type_id, level, display_order, created_at, updated_at)
SELECT c.id, c.primary_effect_type_id, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저2'
  AND c.title = '삼분 멍때리기'
  AND NOT EXISTS (
      SELECT 1
      FROM card_effects ce
      WHERE ce.card_id = c.id
        AND ce.display_order = 1
  );

INSERT INTO card_effects (card_id, effect_type_id, level, display_order, created_at, updated_at)
SELECT c.id, c.primary_effect_type_id, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저3'
  AND c.title = '계단 대신 그늘'
  AND NOT EXISTS (
      SELECT 1
      FROM card_effects ce
      WHERE ce.card_id = c.id
        AND ce.display_order = 1
  );

INSERT INTO card_effects (card_id, effect_type_id, level, display_order, created_at, updated_at)
SELECT c.id, c.primary_effect_type_id, 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저4'
  AND c.title = '편의점 얼음컵'
  AND NOT EXISTS (
      SELECT 1
      FROM card_effects ce
      WHERE ce.card_id = c.id
        AND ce.display_order = 1
  );

INSERT INTO collection_cards (
    user_id, card_id, exchange_id, source, is_favorite, memo, collected_at, created_at, updated_at
)
SELECT c.author_user_id, c.id, NULL, 'CREATED', FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname IN ('씨앗유저1', '씨앗유저2', '씨앗유저3', '씨앗유저4')
  AND NOT EXISTS (
      SELECT 1
      FROM collection_cards cc
      WHERE cc.user_id = c.author_user_id
        AND cc.card_id = c.id
        AND cc.source = 'CREATED'
  );

INSERT INTO card_mailings (sender_user_id, card_id, message, status, sent_at, matched_at, created_at, updated_at)
SELECT c.author_user_id, c.id, '오늘도 같은 여름을 잘 버텨봐요.', 'WAITING', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저1'
  AND c.title = '얼음물 루틴'
  AND NOT EXISTS (SELECT 1 FROM card_mailings m WHERE m.card_id = c.id);

INSERT INTO card_mailings (sender_user_id, card_id, message, status, sent_at, matched_at, created_at, updated_at)
SELECT c.author_user_id, c.id, '잠깐 멈춰도 괜찮아요.', 'WAITING', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저2'
  AND c.title = '삼분 멍때리기'
  AND NOT EXISTS (SELECT 1 FROM card_mailings m WHERE m.card_id = c.id);

INSERT INTO card_mailings (sender_user_id, card_id, message, status, sent_at, matched_at, created_at, updated_at)
SELECT c.author_user_id, c.id, '조금 돌아가도 살아남는 길이면 충분해요.', 'WAITING', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저3'
  AND c.title = '계단 대신 그늘'
  AND NOT EXISTS (SELECT 1 FROM card_mailings m WHERE m.card_id = c.id);

INSERT INTO card_mailings (sender_user_id, card_id, message, status, sent_at, matched_at, created_at, updated_at)
SELECT c.author_user_id, c.id, '비싸지 않아도 시원할 수 있어요.', 'WAITING', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM survival_cards c
JOIN users u ON u.id = c.author_user_id
WHERE u.nickname = '씨앗유저4'
  AND c.title = '편의점 얼음컵'
  AND NOT EXISTS (SELECT 1 FROM card_mailings m WHERE m.card_id = c.id);
