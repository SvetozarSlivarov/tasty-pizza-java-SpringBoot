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

         UNION ALL SELECT 'PASTA', 'Spaghetti', 'Classic Italian spaghetti pasta.', 8.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766519001/spaghetti_pasta.png'
         UNION ALL SELECT 'PASTA', 'Tagliatelle', 'Flat ribbon pasta.', 9.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766519002/tagliatelle_pasta.png'
         UNION ALL SELECT 'PASTA', 'Penne', 'Tube-shaped pasta.', 8.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766519003/penne_pasta.png'
         UNION ALL SELECT 'PASTA', 'Fusilli', 'Spiral-shaped pasta.', 8.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766519004/fusilli_pasta.png'
         UNION ALL SELECT 'PASTA', 'Rigatoni', 'Large tube-shaped pasta.', 9.20, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1766519005/rigatoni_pasta.png'
     ) x
WHERE NOT EXISTS (
    SELECT 1
    FROM products p
    WHERE p.type = x.type
      AND p.name = x.name
);


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


INSERT INTO pastas (product_id)
SELECT p.id
FROM products p
WHERE p.type = 'PASTA'
  AND NOT EXISTS (
    SELECT 1
    FROM pastas pa
    WHERE pa.product_id = p.id
);


INSERT INTO pasta_sauces (pasta_id, ingredient_id, extra_price, spicy_level)
SELECT p.id, i.id, x.extra_price, x.spicy_level
FROM (
         SELECT 'Spaghetti' AS pasta_name, 'Tomato Sauce' AS ing_name, 0.00 AS extra_price, 'MILD' AS spicy_level
         UNION ALL SELECT 'Spaghetti', 'Spicy Tomato Sauce', 0.80, 'MEDIUM'
         UNION ALL SELECT 'Spaghetti', 'Pesto Sauce', 1.20, 'MILD'
         UNION ALL SELECT 'Spaghetti', 'Cream Sauce', 1.00, 'MILD'

         UNION ALL SELECT 'Tagliatelle', 'Cream Sauce', 0.00, 'MILD'
         UNION ALL SELECT 'Tagliatelle', 'Pesto Sauce', 1.20, 'MILD'
         UNION ALL SELECT 'Tagliatelle', 'Garlic Sauce', 0.70, 'MILD'
         UNION ALL SELECT 'Tagliatelle', 'Tomato Sauce', 0.80, 'MILD'

         UNION ALL SELECT 'Penne', 'Tomato Sauce', 0.00, 'MILD'
         UNION ALL SELECT 'Penne', 'Spicy Tomato Sauce', 0.80, 'MEDIUM'
         UNION ALL SELECT 'Penne', 'Cream Sauce', 1.00, 'MILD'
         UNION ALL SELECT 'Penne', 'Chili Sauce', 0.90, 'HOT'

         UNION ALL SELECT 'Fusilli', 'Pesto Sauce', 0.00, 'MILD'
         UNION ALL SELECT 'Fusilli', 'Tomato Sauce', 0.80, 'MILD'
         UNION ALL SELECT 'Fusilli', 'Cream Sauce', 1.00, 'MILD'
         UNION ALL SELECT 'Fusilli', 'Garlic Sauce', 0.70, 'MILD'

         UNION ALL SELECT 'Rigatoni', 'Tomato Sauce', 0.00, 'MILD'
         UNION ALL SELECT 'Rigatoni', 'Spicy Tomato Sauce', 0.80, 'MEDIUM'
         UNION ALL SELECT 'Rigatoni', 'Cream Sauce', 1.00, 'MILD'
         UNION ALL SELECT 'Rigatoni', 'BBQ Sauce', 1.00, 'MILD'
     ) x
         JOIN products p ON p.name = x.pasta_name AND p.type = 'PASTA'
         JOIN ingredients i ON i.name = x.ing_name
WHERE NOT EXISTS (
    SELECT 1
    FROM pasta_sauces ps
    WHERE ps.pasta_id = p.id
      AND ps.ingredient_id = i.id
);


INSERT INTO pasta_allowed_ingredients (pasta_id, ingredient_id, extra_price)
SELECT p.id, i.id, x.extra_price
FROM (
         SELECT 'Spaghetti' AS pasta_name, 'Parmesan' AS ing_name, 1.50 AS extra_price
         UNION ALL SELECT 'Spaghetti', 'Mozzarella', 1.30
         UNION ALL SELECT 'Spaghetti', 'Beef', 2.50
         UNION ALL SELECT 'Spaghetti', 'Meatballs', 2.70
         UNION ALL SELECT 'Spaghetti', 'Mushrooms', 1.20
         UNION ALL SELECT 'Spaghetti', 'Basil', 0.50
         UNION ALL SELECT 'Spaghetti', 'Chili Flakes', 0.50

         UNION ALL SELECT 'Tagliatelle', 'Chicken', 2.00
         UNION ALL SELECT 'Tagliatelle', 'Bacon', 2.30
         UNION ALL SELECT 'Tagliatelle', 'Parmesan', 1.50
         UNION ALL SELECT 'Tagliatelle', 'Mushrooms', 1.20
         UNION ALL SELECT 'Tagliatelle', 'Spinach', 1.00
         UNION ALL SELECT 'Tagliatelle', 'Black Pepper', 0.40

         UNION ALL SELECT 'Penne', 'Chicken', 2.00
         UNION ALL SELECT 'Penne', 'Bacon', 2.30
         UNION ALL SELECT 'Penne', 'Parmesan', 1.50
         UNION ALL SELECT 'Penne', 'Mozzarella', 1.30
         UNION ALL SELECT 'Penne', 'Jalapeno', 1.10
         UNION ALL SELECT 'Penne', 'Cherry Tomatoes', 1.10

         UNION ALL SELECT 'Fusilli', 'Parmesan', 1.50
         UNION ALL SELECT 'Fusilli', 'Chicken', 2.00
         UNION ALL SELECT 'Fusilli', 'Cherry Tomatoes', 1.10
         UNION ALL SELECT 'Fusilli', 'Spinach', 1.00
         UNION ALL SELECT 'Fusilli', 'Garlic', 0.60
         UNION ALL SELECT 'Fusilli', 'Black Olives', 1.10

         UNION ALL SELECT 'Rigatoni', 'Beef', 2.50
         UNION ALL SELECT 'Rigatoni', 'Sausage', 2.40
         UNION ALL SELECT 'Rigatoni', 'Parmesan', 1.50
         UNION ALL SELECT 'Rigatoni', 'Mozzarella', 1.30
         UNION ALL SELECT 'Rigatoni', 'Mushrooms', 1.20
         UNION ALL SELECT 'Rigatoni', 'Oregano', 0.40
     ) x
         JOIN products p ON p.name = x.pasta_name AND p.type = 'PASTA'
         JOIN ingredients i ON i.name = x.ing_name
WHERE NOT EXISTS (
    SELECT 1
    FROM pasta_allowed_ingredients pai
    WHERE pai.pasta_id = p.id
      AND pai.ingredient_id = i.id
);