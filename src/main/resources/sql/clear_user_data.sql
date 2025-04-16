-- Clean test data with dependencies
TRUNCATE TABLE
    meal_dishes,
    meals,
    dishes,
    users
    RESTART IDENTITY CASCADE;