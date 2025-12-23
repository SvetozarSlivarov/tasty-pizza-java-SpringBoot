INSERT INTO ingredient_types (name)
SELECT x.name
FROM (
         SELECT 'Cheese' AS name
         UNION ALL SELECT 'Meat'
         UNION ALL SELECT 'Sauce'
         UNION ALL SELECT 'Veggies'
         UNION ALL SELECT 'Seafood'
         UNION ALL SELECT 'HerbsSpices'
     ) x
WHERE NOT EXISTS (SELECT 1 FROM ingredient_types);


INSERT INTO ingredients (name, type_id, is_deleted, deleted_at)
SELECT x.name, it.id, false, NULL
FROM (
         SELECT 'Mozzarella' AS name, 'Cheese' AS type_name
         UNION ALL SELECT 'Parmesan', 'Cheese'
         UNION ALL SELECT 'Cheddar', 'Cheese'
         UNION ALL SELECT 'Gouda', 'Cheese'
         UNION ALL SELECT 'Blue Cheese', 'Cheese'
         UNION ALL SELECT 'Provolone', 'Cheese'
         UNION ALL SELECT 'Ricotta', 'Cheese'
         UNION ALL SELECT 'Feta', 'Cheese'
         UNION ALL SELECT 'Emmental', 'Cheese'
         UNION ALL SELECT 'Goat Cheese', 'Cheese'

         UNION ALL SELECT 'Pepperoni', 'Meat'
         UNION ALL SELECT 'Ham', 'Meat'
         UNION ALL SELECT 'Bacon', 'Meat'
         UNION ALL SELECT 'Chicken', 'Meat'
         UNION ALL SELECT 'Beef', 'Meat'
         UNION ALL SELECT 'Sausage', 'Meat'
         UNION ALL SELECT 'Salami', 'Meat'
         UNION ALL SELECT 'Prosciutto', 'Meat'
         UNION ALL SELECT 'Chorizo', 'Meat'
         UNION ALL SELECT 'Turkey', 'Meat'
         UNION ALL SELECT 'Pancetta', 'Meat'
         UNION ALL SELECT 'Meatballs', 'Meat'

         UNION ALL SELECT 'Tomato Sauce', 'Sauce'
         UNION ALL SELECT 'Spicy Tomato Sauce', 'Sauce'
         UNION ALL SELECT 'BBQ Sauce', 'Sauce'
         UNION ALL SELECT 'Pesto Sauce', 'Sauce'
         UNION ALL SELECT 'Garlic Sauce', 'Sauce'
         UNION ALL SELECT 'Cream Sauce', 'Sauce'
         UNION ALL SELECT 'Olive Oil', 'Sauce'
         UNION ALL SELECT 'Chili Sauce', 'Sauce'
         UNION ALL SELECT 'Ranch Sauce', 'Sauce'

         UNION ALL SELECT 'Mushrooms', 'Veggies'
         UNION ALL SELECT 'Onion', 'Veggies'
         UNION ALL SELECT 'Red Onion', 'Veggies'
         UNION ALL SELECT 'Green Peppers', 'Veggies'
         UNION ALL SELECT 'Roasted Peppers', 'Veggies'
         UNION ALL SELECT 'Jalapeno', 'Veggies'
         UNION ALL SELECT 'Black Olives', 'Veggies'
         UNION ALL SELECT 'Green Olives', 'Veggies'
         UNION ALL SELECT 'Tomatoes', 'Veggies'
         UNION ALL SELECT 'Cherry Tomatoes', 'Veggies'
         UNION ALL SELECT 'Spinach', 'Veggies'
         UNION ALL SELECT 'Corn', 'Veggies'
         UNION ALL SELECT 'Pineapple', 'Veggies'
         UNION ALL SELECT 'Arugula', 'Veggies'

         UNION ALL SELECT 'Tuna', 'Seafood'
         UNION ALL SELECT 'Shrimp', 'Seafood'

         UNION ALL SELECT 'Oregano', 'HerbsSpices'
         UNION ALL SELECT 'Basil', 'HerbsSpices'
         UNION ALL SELECT 'Chili Flakes', 'HerbsSpices'
         UNION ALL SELECT 'Black Pepper', 'HerbsSpices'
         UNION ALL SELECT 'Garlic', 'HerbsSpices'
         UNION ALL SELECT 'Parsley', 'HerbsSpices'
     ) x
         JOIN ingredient_types it ON it.name = x.type_name
WHERE NOT EXISTS (SELECT 1 FROM ingredients);


INSERT INTO products (type, name, description, base_price, image_url, created_at, is_deleted, deleted_at)
SELECT x.type, x.name, x.description, x.base_price, x.image_url, NOW(), false, NULL
FROM (
         SELECT 'PIZZA' AS type, 'Margherita' AS name, 'Tomato sauce, mozzarella, oregano.' AS description, 9.90 AS base_price, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_57_05_ykylt0.png' AS image_url
         UNION ALL SELECT 'PIZZA', 'Pepperoni', 'Tomato sauce, mozzarella, pepperoni.', 12.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516309/ChatGPT_Image_23.12.2025_%D0%B3._20_57_02_nyfivs.png'
         UNION ALL SELECT 'PIZZA', 'BBQ Chicken', 'BBQ sauce, mozzarella, chicken, red onion.', 13.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516309/ChatGPT_Image_23.12.2025_%D0%B3._20_56_58_h2trsb.png'
         UNION ALL SELECT 'PIZZA', 'Hawaiian', 'Tomato sauce, mozzarella, ham, pineapple.', 12.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516309/ChatGPT_Image_23.12.2025_%D0%B3._20_56_54_rezmsh.png'
         UNION ALL SELECT 'PIZZA', 'Veggie', 'Tomato sauce, mozzarella, mixed veggies.', 11.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_56_51_lnj4wn.png'
         UNION ALL SELECT 'PIZZA', 'Four Cheese', 'Mozzarella, cheddar, parmesan, blue cheese.', 13.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_56_47_ecei4u.png'

         UNION ALL SELECT 'DRINK', 'Coca-Cola 330ml', 'Soft drink.', 2.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_56_42_qjjsay.png'
         UNION ALL SELECT 'DRINK', 'Fanta 330ml', 'Orange soft drink.', 2.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_56_39_wbaezk.png'
         UNION ALL SELECT 'DRINK', 'Sprite 330ml', 'Lemon-lime soft drink.', 2.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_56_35_juunwy.png'
         UNION ALL SELECT 'DRINK', 'Water 500ml', 'Still water.', 1.80, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516310/ChatGPT_Image_23.12.2025_%D0%B3._20_56_32_i7w32q.png'
         UNION ALL SELECT 'DRINK', 'Iced Tea Lemon 500ml', 'Iced tea lemon.', 3.00, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516311/ChatGPT_Image_23.12.2025_%D0%B3._20_56_30_gwtn3n.png'
         UNION ALL SELECT 'DRINK', 'Orange Juice 330ml', 'Orange juice.', 3.20, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766516311/ChatGPT_Image_23.12.2025_%D0%B3._20_56_26_yjrta1.png'
     ) x
WHERE NOT EXISTS (SELECT 1 FROM products);

INSERT INTO pizzas (product_id, spicy_level)
SELECT p.id, x.spicy_level
FROM products p
         JOIN (
    SELECT 'Margherita' AS name, 'MILD' AS spicy_level
    UNION ALL SELECT 'Pepperoni', 'MEDIUM'
    UNION ALL SELECT 'BBQ Chicken', 'MILD'
    UNION ALL SELECT 'Hawaiian', 'MILD'
    UNION ALL SELECT 'Veggie', 'MILD'
    UNION ALL SELECT 'Four Cheese', 'MILD'
) x ON x.name = p.name
WHERE NOT EXISTS (SELECT 1 FROM pizzas);


INSERT INTO drinks (product_id)
SELECT p.id
FROM products p
WHERE p.type = 'DRINK'
  AND NOT EXISTS (SELECT 1 FROM drinks);

INSERT INTO pizza_variants (pizza_id, size, dough, extra_price)
SELECT p.id, v.size, v.dough, v.extra_price
FROM products p
         JOIN (
    SELECT 'SMALL' AS size, 'CLASSIC' AS dough, 0.00 AS extra_price
    UNION ALL SELECT 'MEDIUM', 'CLASSIC', 2.00
    UNION ALL SELECT 'LARGE', 'CLASSIC', 4.00

    UNION ALL SELECT 'SMALL', 'THIN', 0.50
    UNION ALL SELECT 'MEDIUM', 'THIN', 2.50
    UNION ALL SELECT 'LARGE', 'THIN', 4.50

    UNION ALL SELECT 'SMALL', 'WHOLEGRAIN', 0.80
    UNION ALL SELECT 'MEDIUM', 'WHOLEGRAIN', 2.80
    UNION ALL SELECT 'LARGE', 'WHOLEGRAIN', 4.80
) v
WHERE p.type = 'PIZZA'
  AND NOT EXISTS (SELECT 1 FROM pizza_variants);

INSERT INTO pizza_ingredients (pizza_id, ingredient_id, is_removable)
SELECT p.id, i.id, x.is_removable
FROM (
         SELECT 'Margherita' AS pizza_name, 'Tomato Sauce' AS ing_name, false AS is_removable
         UNION ALL SELECT 'Margherita', 'Mozzarella', false
         UNION ALL SELECT 'Margherita', 'Oregano', true

         UNION ALL SELECT 'Pepperoni', 'Tomato Sauce', false
         UNION ALL SELECT 'Pepperoni', 'Mozzarella', false
         UNION ALL SELECT 'Pepperoni', 'Pepperoni', false
         UNION ALL SELECT 'Pepperoni', 'Oregano', true

         UNION ALL SELECT 'BBQ Chicken', 'BBQ Sauce', false
         UNION ALL SELECT 'BBQ Chicken', 'Mozzarella', false
         UNION ALL SELECT 'BBQ Chicken', 'Chicken', false
         UNION ALL SELECT 'BBQ Chicken', 'Red Onion', true
         UNION ALL SELECT 'BBQ Chicken', 'Basil', true

         UNION ALL SELECT 'Hawaiian', 'Tomato Sauce', false
         UNION ALL SELECT 'Hawaiian', 'Mozzarella', false
         UNION ALL SELECT 'Hawaiian', 'Ham', false
         UNION ALL SELECT 'Hawaiian', 'Pineapple', false

         UNION ALL SELECT 'Veggie', 'Tomato Sauce', false
         UNION ALL SELECT 'Veggie', 'Mozzarella', false
         UNION ALL SELECT 'Veggie', 'Mushrooms', true
         UNION ALL SELECT 'Veggie', 'Onion', true
         UNION ALL SELECT 'Veggie', 'Green Peppers', true
         UNION ALL SELECT 'Veggie', 'Black Olives', true
         UNION ALL SELECT 'Veggie', 'Spinach', true

         UNION ALL SELECT 'Four Cheese', 'Tomato Sauce', true
         UNION ALL SELECT 'Four Cheese', 'Mozzarella', false
         UNION ALL SELECT 'Four Cheese', 'Cheddar', false
         UNION ALL SELECT 'Four Cheese', 'Parmesan', false
         UNION ALL SELECT 'Four Cheese', 'Blue Cheese', false
     ) x
         JOIN products p ON p.name = x.pizza_name AND p.type='PIZZA'
         JOIN ingredients i ON i.name = x.ing_name
WHERE NOT EXISTS (SELECT 1 FROM pizza_ingredients);


INSERT INTO pizza_allowed_ingredients (pizza_id, ingredient_id, extra_price)
SELECT p.id, i.id, x.extra_price
FROM (
         SELECT 'Margherita' AS pizza_name, 'Basil' AS ing_name, 0.80 AS extra_price
         UNION ALL SELECT 'Margherita', 'Parmesan', 1.50
         UNION ALL SELECT 'Margherita', 'Mushrooms', 1.20
         UNION ALL SELECT 'Margherita', 'Black Olives', 1.10
         UNION ALL SELECT 'Margherita', 'Ham', 1.90
         UNION ALL SELECT 'Margherita', 'Pepperoni', 2.10
         UNION ALL SELECT 'Margherita', 'Garlic', 0.60
         UNION ALL SELECT 'Margherita', 'Olive Oil', 0.50

         UNION ALL SELECT 'Pepperoni', 'Jalapeno', 1.10
         UNION ALL SELECT 'Pepperoni', 'Chili Flakes', 0.70
         UNION ALL SELECT 'Pepperoni', 'Onion', 0.90
         UNION ALL SELECT 'Pepperoni', 'Mushrooms', 1.20
         UNION ALL SELECT 'Pepperoni', 'Cheddar', 1.40
         UNION ALL SELECT 'Pepperoni', 'Bacon', 2.30
         UNION ALL SELECT 'Pepperoni', 'Green Peppers', 0.90
         UNION ALL SELECT 'Pepperoni', 'Ranch Sauce', 0.80

         UNION ALL SELECT 'BBQ Chicken', 'Bacon', 2.30
         UNION ALL SELECT 'BBQ Chicken', 'Corn', 1.00
         UNION ALL SELECT 'BBQ Chicken', 'Roasted Peppers', 1.10
         UNION ALL SELECT 'BBQ Chicken', 'Jalapeno', 1.10
         UNION ALL SELECT 'BBQ Chicken', 'Cheddar', 1.40
         UNION ALL SELECT 'BBQ Chicken', 'Garlic Sauce', 0.90
         UNION ALL SELECT 'BBQ Chicken', 'Green Olives', 1.10
         UNION ALL SELECT 'BBQ Chicken', 'Parsley', 0.50

         UNION ALL SELECT 'Hawaiian', 'Bacon', 2.30
         UNION ALL SELECT 'Hawaiian', 'Chicken', 2.00
         UNION ALL SELECT 'Hawaiian', 'Green Olives', 1.10
         UNION ALL SELECT 'Hawaiian', 'Chili Sauce', 0.90
         UNION ALL SELECT 'Hawaiian', 'Cheddar', 1.40
         UNION ALL SELECT 'Hawaiian', 'Red Onion', 0.90
         UNION ALL SELECT 'Hawaiian', 'Oregano', 0.40
         UNION ALL SELECT 'Hawaiian', 'Garlic', 0.60

         UNION ALL SELECT 'Veggie', 'Arugula', 1.00
         UNION ALL SELECT 'Veggie', 'Cherry Tomatoes', 1.10
         UNION ALL SELECT 'Veggie', 'Green Olives', 1.10
         UNION ALL SELECT 'Veggie', 'Feta', 1.60
         UNION ALL SELECT 'Veggie', 'Pesto Sauce', 1.20
         UNION ALL SELECT 'Veggie', 'Goat Cheese', 1.90
         UNION ALL SELECT 'Veggie', 'Olive Oil', 0.50
         UNION ALL SELECT 'Veggie', 'Black Pepper', 0.40

         UNION ALL SELECT 'Four Cheese', 'Ham', 1.90
         UNION ALL SELECT 'Four Cheese', 'Mushrooms', 1.20
         UNION ALL SELECT 'Four Cheese', 'Spinach', 1.00
         UNION ALL SELECT 'Four Cheese', 'Prosciutto', 2.90
         UNION ALL SELECT 'Four Cheese', 'Olive Oil', 0.50
         UNION ALL SELECT 'Four Cheese', 'Basil', 0.80
         UNION ALL SELECT 'Four Cheese', 'Ricotta', 1.70
         UNION ALL SELECT 'Four Cheese', 'Garlic Sauce', 0.90
     ) x
         JOIN products p ON p.name = x.pizza_name AND p.type='PIZZA'
         JOIN ingredients i ON i.name = x.ing_name
WHERE NOT EXISTS (SELECT 1 FROM pizza_allowed_ingredients);
