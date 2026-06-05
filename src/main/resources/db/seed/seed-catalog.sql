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

         UNION ALL SELECT 'PASTA', 'Spaghetti', 'Classic Italian spaghetti pasta.', 8.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1779545014/ChatGPT_Image_23.05.2026_%D0%B3._16_58_00_iit6tm.png'
         UNION ALL SELECT 'PASTA', 'Tagliatelle', 'Flat ribbon pasta.', 9.50, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1779545014/ChatGPT_Image_23.05.2026_%D0%B3._16_59_23_rtcxbz.png'
         UNION ALL SELECT 'PASTA', 'Penne', 'Tube-shaped pasta.', 8.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1779545014/ChatGPT_Image_23.05.2026_%D0%B3._17_03_12_nh1edp.png'
         UNION ALL SELECT 'PASTA', 'Fusilli', 'Spiral-shaped pasta.', 8.90, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1779545014/ChatGPT_Image_23.05.2026_%D0%B3._17_03_07_skjckq.png'
         UNION ALL SELECT 'PASTA', 'Rigatoni', 'Large tube-shaped pasta.', 9.20, 'https://res.cloudinary.com/dea47xrrc/image/upload/v1779545014/ChatGPT_Image_23.05.2026_%D0%B3._17_02_46_r3dkzq.png'
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
WHERE NOT EXISTS (
    SELECT 1
    FROM pizzas pi
    WHERE pi.product_id = p.id
);


INSERT INTO pizza_variants (pizza_id, size, dough, extra_price)
SELECT pi.product_id, x.size, x.dough, x.extra_price
FROM (
         SELECT 'Margherita' AS pizza_name, 'SMALL' AS size, 'CLASSIC' AS dough, 0.00 AS extra_price
         UNION ALL SELECT 'Margherita', 'MEDIUM', 'CLASSIC', 2.00
         UNION ALL SELECT 'Margherita', 'LARGE', 'CLASSIC', 4.00
         UNION ALL SELECT 'Margherita', 'MEDIUM', 'THIN', 2.00
         UNION ALL SELECT 'Margherita', 'MEDIUM', 'WHOLEGRAIN', 2.50

         UNION ALL SELECT 'Pepperoni', 'SMALL', 'CLASSIC', 0.00
         UNION ALL SELECT 'Pepperoni', 'MEDIUM', 'CLASSIC', 2.50
         UNION ALL SELECT 'Pepperoni', 'LARGE', 'CLASSIC', 4.80
         UNION ALL SELECT 'Pepperoni', 'MEDIUM', 'THIN', 2.50
         UNION ALL SELECT 'Pepperoni', 'MEDIUM', 'WHOLEGRAIN', 3.00

         UNION ALL SELECT 'BBQ Chicken', 'SMALL', 'CLASSIC', 0.00
         UNION ALL SELECT 'BBQ Chicken', 'MEDIUM', 'CLASSIC', 2.70
         UNION ALL SELECT 'BBQ Chicken', 'LARGE', 'CLASSIC', 5.00
         UNION ALL SELECT 'BBQ Chicken', 'MEDIUM', 'THIN', 2.70
         UNION ALL SELECT 'BBQ Chicken', 'MEDIUM', 'WHOLEGRAIN', 3.20

         UNION ALL SELECT 'Hawaiian', 'SMALL', 'CLASSIC', 0.00
         UNION ALL SELECT 'Hawaiian', 'MEDIUM', 'CLASSIC', 2.40
         UNION ALL SELECT 'Hawaiian', 'LARGE', 'CLASSIC', 4.60
         UNION ALL SELECT 'Hawaiian', 'MEDIUM', 'THIN', 2.40
         UNION ALL SELECT 'Hawaiian', 'MEDIUM', 'WHOLEGRAIN', 2.90

         UNION ALL SELECT 'Veggie', 'SMALL', 'CLASSIC', 0.00
         UNION ALL SELECT 'Veggie', 'MEDIUM', 'CLASSIC', 2.20
         UNION ALL SELECT 'Veggie', 'LARGE', 'CLASSIC', 4.20
         UNION ALL SELECT 'Veggie', 'MEDIUM', 'THIN', 2.20
         UNION ALL SELECT 'Veggie', 'MEDIUM', 'WHOLEGRAIN', 2.70

         UNION ALL SELECT 'Four Cheese', 'SMALL', 'CLASSIC', 0.00
         UNION ALL SELECT 'Four Cheese', 'MEDIUM', 'CLASSIC', 2.80
         UNION ALL SELECT 'Four Cheese', 'LARGE', 'CLASSIC', 5.20
         UNION ALL SELECT 'Four Cheese', 'MEDIUM', 'THIN', 2.80
         UNION ALL SELECT 'Four Cheese', 'MEDIUM', 'WHOLEGRAIN', 3.30
     ) x
         JOIN products p ON p.name = x.pizza_name AND p.type = 'PIZZA'
         JOIN pizzas pi ON pi.product_id = p.id
WHERE NOT EXISTS (
    SELECT 1
    FROM pizza_variants pv
    WHERE pv.pizza_id = pi.product_id
      AND pv.size = x.size
      AND pv.dough = x.dough
);


INSERT INTO pizza_ingredients (pizza_id, ingredient_id, is_removable)
SELECT pi.product_id, i.id, x.is_removable
FROM (
         SELECT 'Margherita' AS pizza_name, 'Tomato Sauce' AS ing_name, false AS is_removable
         UNION ALL SELECT 'Margherita', 'Mozzarella', false
         UNION ALL SELECT 'Margherita', 'Oregano', true
         UNION ALL SELECT 'Margherita', 'Basil', true

         UNION ALL SELECT 'Pepperoni', 'Tomato Sauce', false
         UNION ALL SELECT 'Pepperoni', 'Mozzarella', false
         UNION ALL SELECT 'Pepperoni', 'Pepperoni', true
         UNION ALL SELECT 'Pepperoni', 'Oregano', true

         UNION ALL SELECT 'BBQ Chicken', 'BBQ Sauce', false
         UNION ALL SELECT 'BBQ Chicken', 'Mozzarella', false
         UNION ALL SELECT 'BBQ Chicken', 'Chicken', true
         UNION ALL SELECT 'BBQ Chicken', 'Red Onion', true

         UNION ALL SELECT 'Hawaiian', 'Tomato Sauce', false
         UNION ALL SELECT 'Hawaiian', 'Mozzarella', false
         UNION ALL SELECT 'Hawaiian', 'Ham', true
         UNION ALL SELECT 'Hawaiian', 'Pineapple', true

         UNION ALL SELECT 'Veggie', 'Tomato Sauce', false
         UNION ALL SELECT 'Veggie', 'Mozzarella', false
         UNION ALL SELECT 'Veggie', 'Mushrooms', true
         UNION ALL SELECT 'Veggie', 'Green Peppers', true
         UNION ALL SELECT 'Veggie', 'Black Olives', true
         UNION ALL SELECT 'Veggie', 'Tomatoes', true

         UNION ALL SELECT 'Four Cheese', 'Mozzarella', false
         UNION ALL SELECT 'Four Cheese', 'Cheddar', true
         UNION ALL SELECT 'Four Cheese', 'Parmesan', true
         UNION ALL SELECT 'Four Cheese', 'Blue Cheese', true
     ) x
         JOIN products p ON p.name = x.pizza_name AND p.type = 'PIZZA'
         JOIN pizzas pi ON pi.product_id = p.id
         JOIN ingredients i ON i.name = x.ing_name
WHERE NOT EXISTS (
    SELECT 1
    FROM pizza_ingredients ping
    WHERE ping.pizza_id = pi.product_id
      AND ping.ingredient_id = i.id
);


INSERT INTO pizza_allowed_ingredients (pizza_id, ingredient_id, extra_price)
SELECT pi.product_id, i.id, x.extra_price
FROM (
         SELECT 'Margherita' AS pizza_name, 'Parmesan' AS ing_name, 1.50 AS extra_price
         UNION ALL SELECT 'Margherita', 'Cherry Tomatoes', 1.10
         UNION ALL SELECT 'Margherita', 'Black Olives', 1.10
         UNION ALL SELECT 'Margherita', 'Mushrooms', 1.20
         UNION ALL SELECT 'Margherita', 'Prosciutto', 2.80

         UNION ALL SELECT 'Pepperoni', 'Cheddar', 1.40
         UNION ALL SELECT 'Pepperoni', 'Bacon', 2.30
         UNION ALL SELECT 'Pepperoni', 'Jalapeno', 1.10
         UNION ALL SELECT 'Pepperoni', 'Mushrooms', 1.20
         UNION ALL SELECT 'Pepperoni', 'Chili Flakes', 0.50

         UNION ALL SELECT 'BBQ Chicken', 'Bacon', 2.30
         UNION ALL SELECT 'BBQ Chicken', 'Corn', 1.00
         UNION ALL SELECT 'BBQ Chicken', 'Roasted Peppers', 1.20
         UNION ALL SELECT 'BBQ Chicken', 'Cheddar', 1.40
         UNION ALL SELECT 'BBQ Chicken', 'Ranch Sauce', 0.80

         UNION ALL SELECT 'Hawaiian', 'Bacon', 2.30
         UNION ALL SELECT 'Hawaiian', 'Chicken', 2.00
         UNION ALL SELECT 'Hawaiian', 'Cheddar', 1.40
         UNION ALL SELECT 'Hawaiian', 'Corn', 1.00
         UNION ALL SELECT 'Hawaiian', 'Jalapeno', 1.10

         UNION ALL SELECT 'Veggie', 'Spinach', 1.00
         UNION ALL SELECT 'Veggie', 'Corn', 1.00
         UNION ALL SELECT 'Veggie', 'Red Onion', 0.80
         UNION ALL SELECT 'Veggie', 'Green Olives', 1.10
         UNION ALL SELECT 'Veggie', 'Feta', 1.50

         UNION ALL SELECT 'Four Cheese', 'Goat Cheese', 1.80
         UNION ALL SELECT 'Four Cheese', 'Gouda', 1.50
         UNION ALL SELECT 'Four Cheese', 'Provolone', 1.60
         UNION ALL SELECT 'Four Cheese', 'Ricotta', 1.50
         UNION ALL SELECT 'Four Cheese', 'Bacon', 2.30
     ) x
         JOIN products p ON p.name = x.pizza_name AND p.type = 'PIZZA'
         JOIN pizzas pi ON pi.product_id = p.id
         JOIN ingredients i ON i.name = x.ing_name
WHERE NOT EXISTS (
    SELECT 1
    FROM pizza_allowed_ingredients pai
    WHERE pai.pizza_id = pi.product_id
      AND pai.ingredient_id = i.id
);


INSERT INTO drinks (product_id)
SELECT p.id
FROM products p
WHERE p.type = 'DRINK'
  AND NOT EXISTS (
    SELECT 1
    FROM drinks d
    WHERE d.product_id = p.id
);


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


REPLACE INTO translations (entity_type, entity_id, field_name, language, translated_text, auto_generated, created_at, updated_at)
SELECT 'PRODUCT',
       p.id,
       f.field_name,
       l.lang,
       CASE f.field_name
           WHEN 'name' THEN CASE l.lang WHEN 'bg' THEN x.bg_name WHEN 'de' THEN x.de_name WHEN 'fr' THEN x.fr_name END
           ELSE CASE l.lang WHEN 'bg' THEN x.bg_description WHEN 'de' THEN x.de_description WHEN 'fr' THEN x.fr_description END
           END,
       false,
       NOW(),
       NOW()
FROM (
         SELECT 'PIZZA' AS type, 'Margherita' AS en_name, 'Маргарита' AS bg_name, 'Margherita' AS de_name, 'Margherita' AS fr_name,
                'Доматен сос, моцарела, риган.' AS bg_description, 'Tomatensauce, Mozzarella, Oregano.' AS de_description, 'Sauce tomate, mozzarella, origan.' AS fr_description
         UNION ALL SELECT 'PIZZA', 'Pepperoni', 'Пеперони', 'Peperoni', 'Pepperoni',
                'Доматен сос, моцарела, пеперони.', 'Tomatensauce, Mozzarella, Peperoni.', 'Sauce tomate, mozzarella, pepperoni.'
         UNION ALL SELECT 'PIZZA', 'BBQ Chicken', 'Барбекю пиле', 'BBQ-Hähnchen', 'Poulet barbecue',
                'Барбекю сос, моцарела, пиле, червен лук.', 'BBQ-Sauce, Mozzarella, Hähnchen, rote Zwiebel.', 'Sauce barbecue, mozzarella, poulet, oignon rouge.'
         UNION ALL SELECT 'PIZZA', 'Hawaiian', 'Хавайска', 'Hawaii', 'Hawaïenne',
                'Доматен сос, моцарела, шунка, ананас.', 'Tomatensauce, Mozzarella, Schinken, Ananas.', 'Sauce tomate, mozzarella, jambon, ananas.'
         UNION ALL SELECT 'PIZZA', 'Veggie', 'Вегетарианска', 'Gemüse', 'Végétarienne',
                'Доматен сос, моцарела, микс от зеленчуци.', 'Tomatensauce, Mozzarella, gemischtes Gemüse.', 'Sauce tomate, mozzarella, légumes variés.'
         UNION ALL SELECT 'PIZZA', 'Four Cheese', 'Четири сирена', 'Vier Käse', 'Quatre fromages',
                'Моцарела, чедър, пармезан, синьо сирене.', 'Mozzarella, Cheddar, Parmesan, Blauschimmelkäse.', 'Mozzarella, cheddar, parmesan, fromage bleu.'
         UNION ALL SELECT 'DRINK', 'Coca-Cola 330ml', 'Кока-Кола 330 мл', 'Coca-Cola 330 ml', 'Coca-Cola 330 ml',
                'Газирана безалкохолна напитка.', 'Erfrischungsgetränk.', 'Boisson gazeuse.'
         UNION ALL SELECT 'DRINK', 'Fanta 330ml', 'Фанта 330 мл', 'Fanta 330 ml', 'Fanta 330 ml',
                'Портокалова безалкохолна напитка.', 'Orangenlimonade.', 'Boisson gazeuse à l''orange.'
         UNION ALL SELECT 'DRINK', 'Sprite 330ml', 'Спрайт 330 мл', 'Sprite 330 ml', 'Sprite 330 ml',
                'Лимон-лайм безалкохолна напитка.', 'Zitronen-Limetten-Limonade.', 'Boisson gazeuse citron-lime.'
         UNION ALL SELECT 'DRINK', 'Water 500ml', 'Вода 500 мл', 'Wasser 500 ml', 'Eau 500 ml',
                'Негазирана вода.', 'Stilles Wasser.', 'Eau plate.'
         UNION ALL SELECT 'DRINK', 'Iced Tea Lemon 500ml', 'Студен чай лимон 500 мл', 'Eistee Zitrone 500 ml', 'Thé glacé citron 500 ml',
                'Студен чай с лимон.', 'Eistee mit Zitrone.', 'Thé glacé au citron.'
         UNION ALL SELECT 'DRINK', 'Orange Juice 330ml', 'Портокалов сок 330 мл', 'Orangensaft 330 ml', 'Jus d''orange 330 ml',
                'Портокалов сок.', 'Orangensaft.', 'Jus d''orange.'
         UNION ALL SELECT 'PASTA', 'Spaghetti', 'Спагети', 'Spaghetti', 'Spaghettis',
                'Класическа италианска паста спагети.', 'Klassische italienische Spaghetti.', 'Pâtes spaghetti italiennes classiques.'
         UNION ALL SELECT 'PASTA', 'Tagliatelle', 'Талиатели', 'Tagliatelle', 'Tagliatelles',
                'Плоска лентовидна паста.', 'Flache Bandnudeln.', 'Pâtes plates en rubans.'
         UNION ALL SELECT 'PASTA', 'Penne', 'Пене', 'Penne', 'Penne',
                'Паста с форма на тръбички.', 'Röhrenförmige Pasta.', 'Pâtes en forme de tubes.'
         UNION ALL SELECT 'PASTA', 'Fusilli', 'Фузили', 'Fusilli', 'Fusilli',
                'Спираловидна паста.', 'Spiralförmige Pasta.', 'Pâtes en spirale.'
         UNION ALL SELECT 'PASTA', 'Rigatoni', 'Ригатони', 'Rigatoni', 'Rigatoni',
                'Голяма паста с форма на тръбички.', 'Große röhrenförmige Pasta.', 'Grosses pâtes en forme de tubes.'
     ) x
         JOIN products p ON p.type = x.type AND p.name = x.en_name
         CROSS JOIN (
    SELECT 'name' AS field_name
    UNION ALL SELECT 'description'
) f
         CROSS JOIN (
    SELECT 'bg' AS lang
    UNION ALL SELECT 'de'
    UNION ALL SELECT 'fr'
) l;

REPLACE INTO translations (entity_type, entity_id, field_name, language, translated_text, auto_generated, created_at, updated_at)
SELECT 'INGREDIENT',
       i.id,
       'name',
       l.lang,
       CASE l.lang WHEN 'bg' THEN x.bg_name WHEN 'de' THEN x.de_name WHEN 'fr' THEN x.fr_name END,
       false,
       NOW(),
       NOW()
FROM (
         SELECT 'Mozzarella' AS en_name, 'Моцарела' AS bg_name, 'Mozzarella' AS de_name, 'Mozzarella' AS fr_name
         UNION ALL SELECT 'Parmesan', 'Пармезан', 'Parmesan', 'Parmesan'
         UNION ALL SELECT 'Cheddar', 'Чедър', 'Cheddar', 'Cheddar'
         UNION ALL SELECT 'Gouda', 'Гауда', 'Gouda', 'Gouda'
         UNION ALL SELECT 'Blue Cheese', 'Синьо сирене', 'Blauschimmelkäse', 'Fromage bleu'
         UNION ALL SELECT 'Provolone', 'Проволоне', 'Provolone', 'Provolone'
         UNION ALL SELECT 'Ricotta', 'Рикота', 'Ricotta', 'Ricotta'
         UNION ALL SELECT 'Feta', 'Фета', 'Feta', 'Feta'
         UNION ALL SELECT 'Emmental', 'Ементал', 'Emmentaler', 'Emmental'
         UNION ALL SELECT 'Goat Cheese', 'Козе сирене', 'Ziegenkäse', 'Fromage de chèvre'
         UNION ALL SELECT 'Pepperoni', 'Пеперони', 'Peperoni', 'Pepperoni'
         UNION ALL SELECT 'Ham', 'Шунка', 'Schinken', 'Jambon'
         UNION ALL SELECT 'Bacon', 'Бекон', 'Speck', 'Bacon'
         UNION ALL SELECT 'Chicken', 'Пиле', 'Hähnchen', 'Poulet'
         UNION ALL SELECT 'Beef', 'Говеждо', 'Rindfleisch', 'Bœuf'
         UNION ALL SELECT 'Sausage', 'Наденица', 'Wurst', 'Saucisse'
         UNION ALL SELECT 'Salami', 'Салам', 'Salami', 'Salami'
         UNION ALL SELECT 'Prosciutto', 'Прошуто', 'Prosciutto', 'Prosciutto'
         UNION ALL SELECT 'Chorizo', 'Чоризо', 'Chorizo', 'Chorizo'
         UNION ALL SELECT 'Turkey', 'Пуйка', 'Pute', 'Dinde'
         UNION ALL SELECT 'Pancetta', 'Панчета', 'Pancetta', 'Pancetta'
         UNION ALL SELECT 'Meatballs', 'Кюфтенца', 'Fleischbällchen', 'Boulettes de viande'
         UNION ALL SELECT 'Tomato Sauce', 'Доматен сос', 'Tomatensauce', 'Sauce tomate'
         UNION ALL SELECT 'Spicy Tomato Sauce', 'Пикантен доматен сос', 'Scharfe Tomatensauce', 'Sauce tomate épicée'
         UNION ALL SELECT 'BBQ Sauce', 'Барбекю сос', 'BBQ-Sauce', 'Sauce barbecue'
         UNION ALL SELECT 'Pesto Sauce', 'Песто сос', 'Pestosauce', 'Sauce pesto'
         UNION ALL SELECT 'Garlic Sauce', 'Чеснов сос', 'Knoblauchsauce', 'Sauce à l''ail'
         UNION ALL SELECT 'Cream Sauce', 'Сметанов сос', 'Sahnesauce', 'Sauce à la crème'
         UNION ALL SELECT 'Olive Oil', 'Зехтин', 'Olivenöl', 'Huile d''olive'
         UNION ALL SELECT 'Chili Sauce', 'Чили сос', 'Chilisauce', 'Sauce chili'
         UNION ALL SELECT 'Ranch Sauce', 'Ранч сос', 'Ranch-Sauce', 'Sauce ranch'
         UNION ALL SELECT 'Mushrooms', 'Гъби', 'Pilze', 'Champignons'
         UNION ALL SELECT 'Onion', 'Лук', 'Zwiebel', 'Oignon'
         UNION ALL SELECT 'Red Onion', 'Червен лук', 'Rote Zwiebel', 'Oignon rouge'
         UNION ALL SELECT 'Green Peppers', 'Зелени чушки', 'Grüne Paprika', 'Poivrons verts'
         UNION ALL SELECT 'Roasted Peppers', 'Печени чушки', 'Geröstete Paprika', 'Poivrons rôtis'
         UNION ALL SELECT 'Jalapeno', 'Халапеньо', 'Jalapeño', 'Jalapeño'
         UNION ALL SELECT 'Black Olives', 'Черни маслини', 'Schwarze Oliven', 'Olives noires'
         UNION ALL SELECT 'Green Olives', 'Зелени маслини', 'Grüne Oliven', 'Olives vertes'
         UNION ALL SELECT 'Tomatoes', 'Домати', 'Tomaten', 'Tomates'
         UNION ALL SELECT 'Cherry Tomatoes', 'Чери домати', 'Kirschtomaten', 'Tomates cerises'
         UNION ALL SELECT 'Spinach', 'Спанак', 'Spinat', 'Épinards'
         UNION ALL SELECT 'Corn', 'Царевица', 'Mais', 'Maïs'
         UNION ALL SELECT 'Pineapple', 'Ананас', 'Ananas', 'Ananas'
         UNION ALL SELECT 'Arugula', 'Рукола', 'Rucola', 'Roquette'
         UNION ALL SELECT 'Tuna', 'Риба тон', 'Thunfisch', 'Thon'
         UNION ALL SELECT 'Shrimp', 'Скариди', 'Garnelen', 'Crevettes'
         UNION ALL SELECT 'Oregano', 'Риган', 'Oregano', 'Origan'
         UNION ALL SELECT 'Basil', 'Босилек', 'Basilikum', 'Basilic'
         UNION ALL SELECT 'Chili Flakes', 'Чили люспи', 'Chiliflocken', 'Flocons de piment'
         UNION ALL SELECT 'Black Pepper', 'Черен пипер', 'Schwarzer Pfeffer', 'Poivre noir'
         UNION ALL SELECT 'Garlic', 'Чесън', 'Knoblauch', 'Ail'
         UNION ALL SELECT 'Parsley', 'Магданоз', 'Petersilie', 'Persil'
     ) x
         JOIN ingredients i ON i.name = x.en_name
         CROSS JOIN (
    SELECT 'bg' AS lang
    UNION ALL SELECT 'de'
    UNION ALL SELECT 'fr'
) l;

REPLACE INTO translations (entity_type, entity_id, field_name, language, translated_text, auto_generated, created_at, updated_at)
SELECT 'INGREDIENT_TYPE',
       it.id,
       'name',
       l.lang,
       CASE l.lang WHEN 'bg' THEN x.bg_name WHEN 'de' THEN x.de_name WHEN 'fr' THEN x.fr_name END,
       false,
       NOW(),
       NOW()
FROM (
         SELECT 'Cheese' AS en_name, 'Сирена' AS bg_name, 'Käse' AS de_name, 'Fromages' AS fr_name
         UNION ALL SELECT 'Meat', 'Месо', 'Fleisch', 'Viandes'
         UNION ALL SELECT 'Sauce', 'Сосове', 'Saucen', 'Sauces'
         UNION ALL SELECT 'Veggies', 'Зеленчуци', 'Gemüse', 'Légumes'
         UNION ALL SELECT 'Seafood', 'Морски дарове', 'Meeresfrüchte', 'Fruits de mer'
         UNION ALL SELECT 'HerbsSpices', 'Билки и подправки', 'Kräuter und Gewürze', 'Herbes et épices'
     ) x
         JOIN ingredient_types it ON it.name = x.en_name
         CROSS JOIN (
    SELECT 'bg' AS lang
    UNION ALL SELECT 'de'
    UNION ALL SELECT 'fr'
) l;
