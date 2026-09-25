-- Production-like data volume so database behaviour (plans, indexes, locks) is realistic.
-- Loaded once by SeedDataLoader when lab.seed.enabled=true. Deterministic thanks to setseed.
-- ~10,000 customers, ~5,000 products, 200,000 orders, ~600,000 order lines.
SELECT setseed(0.42);

INSERT INTO customer (email, name, tier, created_at)
SELECT 'customer' || g || '@shopflow.test',
       'Customer ' || g,
       (ARRAY ['STANDARD', 'STANDARD', 'SILVER', 'GOLD'])[1 + g % 4],
       now() - (g || ' minutes')::interval
FROM generate_series(1, 10000) g;

INSERT INTO product (sku, name, category, price, description)
SELECT 'SKU-' || lpad(g::text, 6, '0'),
       (ARRAY ['Classic', 'Smart', 'Compact', 'Premium', 'Eco', 'Pro', 'Mini', 'Ultra'])[1 + g % 8] || ' ' ||
       (ARRAY ['Lamp', 'Chair', 'Kettle', 'Novel', 'Headphones', 'Backpack', 'Drone', 'Planter', 'Puzzle', 'Watch'])[1 + (g / 8) % 10] || ' ' || g,
       (ARRAY ['Books', 'Electronics', 'Home', 'Toys', 'Garden', 'Sports', 'Fashion', 'Beauty', 'Grocery', 'Office',
               'Music', 'Movies', 'Games', 'Pets', 'Baby', 'Automotive', 'Tools', 'Health', 'Kitchen', 'Travel'])[1 + g % 20],
       round((5 + random() * 495)::numeric, 2),
       'Seeded product ' || g || ' with a description long enough to look like real catalog text.'
FROM generate_series(1, 5000) g;

INSERT INTO inventory (product_id, available)
SELECT p.id, 1000000
FROM product p
WHERE NOT EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id);

-- 200k orders over the last 365 days: ~80% PAID, ~10% CREATED, ~10% CANCELLED
INSERT INTO purchase_order (id, customer_id, status, total_amount, created_at, version)
SELECT gen_random_uuid(),
       1 + floor(random() * m.max_customer)::bigint,
       CASE WHEN r < 0.8 THEN 'PAID' WHEN r < 0.9 THEN 'CREATED' ELSE 'CANCELLED' END,
       0,
       now() - random() * interval '365 days',
       0
FROM generate_series(1, 200000) g
         CROSS JOIN (SELECT max(id) AS max_customer FROM customer) m
         CROSS JOIN LATERAL (SELECT random() + 0 * g AS r) rnd;

-- 1 to 5 lines per order
INSERT INTO order_line (order_id, product_id, quantity, unit_price)
SELECT x.order_id, p.id, x.quantity, p.price
FROM (SELECT o.id                                         AS order_id,
             1 + floor(random() * m.max_product)::bigint AS product_id,
             1 + floor(random() * 3)::int                 AS quantity
      FROM purchase_order o
               CROSS JOIN (SELECT max(id) AS max_product FROM product) m
               CROSS JOIN LATERAL generate_series(1, 1 + (abs(hashtext(o.id::text)) % 5)) s
      WHERE o.total_amount = 0) x
         JOIN product p ON p.id = x.product_id;

UPDATE purchase_order o
SET total_amount = t.total
FROM (SELECT order_id, sum(quantity * unit_price) AS total FROM order_line GROUP BY order_id) t
WHERE t.order_id = o.id
  AND o.total_amount = 0;

ANALYZE customer;
ANALYZE product;
ANALYZE inventory;
ANALYZE purchase_order;
ANALYZE order_line;
