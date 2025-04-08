-- Reset database sequences
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE dishes_id_seq RESTART WITH 200;
ALTER SEQUENCE meals_id_seq RESTART WITH 300;

-- Insert test users
INSERT INTO users (id, activity_level_id, goal_id, name, email, age, weight, height, gender, daily_calorie_target,
                   created_at)
VALUES (1, 1, 1, 'TestUser1', 'user1@test.com', 30, 70.0, 175, 'MALE', 2000, NOW() - INTERVAL '2 days'),
       (2, 1, 2, 'TestUser2', 'user2@test.com', 25, 60.5, 165, 'FEMALE', 1800, NOW() - INTERVAL '2 days'),
       (3, 1, 3, 'TestUser3', 'user3@test.com', 35, 90.0, 185, 'MALE', 2500, NOW() - INTERVAL '2 days');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Insert dishes for User_1
INSERT INTO dishes (id, user_id, name, protein, fat, carbohydrates, calories)
VALUES (200, 1, 'Oatmeal', 5.0, 3.0, 25.0, 150),
       (201, 1, 'Chicken Breast', 30.0, 5.0, 0.0, 200),
       (202, 1, 'Scrambled Eggs', 12.0, 14.0, 2.0, 180);

-- Insert dishes for User_2
INSERT INTO dishes (id, user_id, name, protein, fat, carbohydrates, calories)
VALUES (203, 2, 'Salad', 2.0, 4.0, 10.0, 80),
       (204, 2, 'Fish', 25.0, 8.0, 0.0, 180),
       (205, 2, 'Yogurt', 8.0, 3.5, 15.0, 120);

-- Insert dishes for User_3
INSERT INTO dishes (id, user_id, name, protein, fat, carbohydrates, calories)
VALUES (206, 3, 'Pasta', 8.0, 10.0, 50.0, 300),
       (207, 3, 'Beef Steak', 35.0, 12.0, 0.0, 250),
       (208, 3, 'Vegetable Soup', 4.0, 5.0, 20.0, 150);

SELECT setval('dishes_id_seq', (SELECT MAX(id) FROM dishes));

-- User_1 meals (Day 1 + Day 3)
INSERT INTO meals (id, user_id, name, created_at)
VALUES
-- Day 1 (2 days ago)
(300, 1, 'Breakfast', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '09:00:00'),
(301, 1, 'Lunch', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '14:00:00'),
(302, 1, 'Dinner', date_trunc('day', NOW()) + INTERVAL '20:00:00'),
-- Day 3 (today)
(303, 1, 'Breakfast', date_trunc('day', NOW()) + INTERVAL '08:00:00'),
(304, 1, 'Lunch', date_trunc('day', NOW()) + INTERVAL '13:00:00'),
(305, 1, 'Dinner', date_trunc('day', NOW()) + INTERVAL '19:00:00');

-- User_2 meals (3 full days)
INSERT INTO meals (id, user_id, name, created_at)
VALUES
-- Day 1 (2 days ago)
(306, 2, 'Breakfast', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '07:30:00'),
(307, 2, 'Lunch', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '12:15:00'),
(308, 2, 'Dinner', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '19:00:00'),
-- Day 2 (yesterday)
(309, 2, 'Breakfast', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '07:45:00'),
(310, 2, 'Lunch', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '12:30:00'),
(311, 2, 'Dinner', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '19:15:00'),
-- Day 3 (today)
(312, 2, 'Breakfast', date_trunc('day', NOW()) + INTERVAL '07:50:00'),
(313, 2, 'Lunch', date_trunc('day', NOW()) + INTERVAL '12:40:00'),
(314, 2, 'Dinner', date_trunc('day', NOW()) + INTERVAL '19:10:00');

-- User_3 meals (3 full days)
INSERT INTO meals (id, user_id, name, created_at)
VALUES
-- Day 1 (2 days ago)
(315, 3, 'Breakfast', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '08:00:00'),
(316, 3, 'Lunch', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '12:45:00'),
(317, 3, 'Dinner', date_trunc('day', NOW()) - INTERVAL '2 days' + INTERVAL '19:30:00'),
-- Day 2 (yesterday)
(318, 3, 'Breakfast', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '08:05:00'),
(319, 3, 'Lunch', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '13:00:00'),
(320, 3, 'Dinner', date_trunc('day', NOW()) - INTERVAL '1 day' + INTERVAL '19:45:00'),
-- Day 3 (today)
(321, 3, 'Breakfast', date_trunc('day', NOW()) + INTERVAL '08:10:00'),
(322, 3, 'Lunch', date_trunc('day', NOW()) + INTERVAL '13:10:00'),
(323, 3, 'Dinner', date_trunc('day', NOW()) + INTERVAL '19:55:00');

SELECT setval('meals_id_seq', (SELECT MAX(id) FROM meals));

-- Meal compositions
INSERT INTO meal_dishes (meal_id, dish_id, servings)

-- User_1 meals
VALUES
    -- Day 1
    (300, 200, 3.25),
    (300, 201, 2.50),
    (300, 202, 3.50),
    (301, 201, 4.00),
    (302, 202, 1.50),

    -- Day 3 (today)
    (303, 200, 1.25),
    (304, 201, 2.50),
    (305, 202, 2.25),

-- User_2 meals
    -- Day 1
    (306, 203, 1.25),
    (306, 205, 1.50),
    (307, 204, 0.50),
    (308, 205, 2.00),

    -- Day 2
    (309, 203, 1.50),
    (310, 204, 2.00),
    (311, 205, 1.00),

    -- Day 3 (today)
    (312, 203, 2.00),
    (313, 204, 1.50),
    (314, 205, 2.50),

-- User_3 meals
    -- Day 1
    (315, 206, 2.00),
    (315, 208, 1.50),
    (316, 207, 1.35),
    (317, 208, 2.00),

    -- Day 2
    (318, 206, 1.50),
    (319, 207, 2.50),
    (320, 208, 1.15),

    -- Day 3 (today)
    (321, 206, 1.00),
    (322, 207, 1.50),
    (323, 208, 2.50);