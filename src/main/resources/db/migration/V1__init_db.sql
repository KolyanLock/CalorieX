CREATE
EXTENSION IF NOT EXISTS pg_trgm;

-- Goals reference table
CREATE TABLE goals
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) UNIQUE NOT NULL,
    multiplier NUMERIC(2, 1)      NOT NULL CHECK (multiplier > 0)
);

COMMENT
ON TABLE goals IS 'System-defined fitness goals';
COMMENT
ON COLUMN goals.multiplier IS 'BMR multiplier for daily calorie target';

INSERT INTO goals (name, multiplier)
VALUES ('WEIGHT_LOSS', 0.8),
       ('MAINTENANCE', 1.0),
       ('MUSCLE_GAIN', 1.2);

-- User activity levels table
CREATE TABLE activity_levels
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(50) UNIQUE NOT NULL,
    multiplier NUMERIC(4, 3)      NOT NULL CHECK (multiplier > 0)
);

COMMENT
ON TABLE activity_levels IS 'Activity levels used in Harris-Benedict formula';
COMMENT
ON COLUMN activity_levels.name IS 'Activity level description (e.g., Sedentary, Active)';
COMMENT
ON COLUMN activity_levels.multiplier IS 'BMR multiplier based on activity level';

INSERT INTO activity_levels (name, multiplier)
VALUES ('SEDENTARY', 1.2),
       ('LIGHT', 1.375),
       ('MODERATE', 1.55),
       ('ACTIVE', 1.725),
       ('VERY_ACTIVE', 1.9);

-- User profiles
CREATE TABLE users
(
    id                   BIGSERIAL PRIMARY KEY,
    activity_level_id    BIGINT              NOT NULL REFERENCES activity_levels (id),
    goal_id              BIGINT              NOT NULL REFERENCES goals (id),
    name                 VARCHAR(255)        NOT NULL,
    email                VARCHAR(255) UNIQUE NOT NULL,
    age                  INTEGER             NOT NULL CHECK (age > 0),
    weight               NUMERIC(5, 2)       NOT NULL CHECK (weight > 0),
    height               INTEGER             NOT NULL CHECK (height > 0),
    gender               VARCHAR(15)         NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    daily_calorie_target INTEGER             NOT NULL CHECK (daily_calorie_target > 0),
    created_at           TIMESTAMPTZ         NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON COLUMN users.weight IS 'Weight in kilograms';
COMMENT
ON COLUMN users.height IS 'Height in centimeters';
COMMENT
ON TABLE users IS 'User accounts with calorie tracking';
COMMENT
ON COLUMN users.daily_calorie_target IS 'Calculated using Harris-Benedict formula';

-- Food items
CREATE TABLE dishes
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users (id),
    name          VARCHAR(255) NOT NULL,
    protein       NUMERIC(6, 2) CHECK (protein >= 0),
    fat           NUMERIC(6, 2) CHECK (fat >= 0),
    carbohydrates NUMERIC(6, 2) CHECK (carbohydrates >= 0),
    calories      INTEGER      NOT NULL CHECK (calories > 0),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_dish_name UNIQUE (user_id, name)
);

COMMENT
ON TABLE dishes IS 'Nutritional information for food items';

-- Meal tracking
CREATE TABLE meals
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL DEFAULT '',
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE meals IS 'User meal consumption events';

-- Meal composition
CREATE TABLE meal_dishes
(
    meal_id  BIGINT        NOT NULL REFERENCES meals (id),
    dish_id  BIGINT        NOT NULL REFERENCES dishes (id),
    servings NUMERIC(5, 2) NOT NULL CHECK (servings > 0),
    PRIMARY KEY (meal_id, dish_id)
);

COMMENT
ON TABLE meal_dishes IS 'Dish portions in meals';

-- Indexes
CREATE INDEX idx_meals_user_id ON meals (user_id);
CREATE INDEX idx_meals_created_at ON meals (created_at);
CREATE INDEX idx_dishes_user_id ON dishes (user_id);
CREATE INDEX idx_dishes_created_at ON dishes (created_at);
CREATE INDEX idx_dishes_name_trgm ON dishes USING gin(name gin_trgm_ops);

COMMENT
ON INDEX idx_dishes_name_trgm IS
'GIN trigram index on name for fast case-insensitive substring and similarity search';