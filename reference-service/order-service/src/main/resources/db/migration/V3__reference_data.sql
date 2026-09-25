-- Small, fixed reference data present in every environment (tests, CI smoke test, local).
-- Customers 1-10 and products 1-50. Product REF-050 has only 5 units, for insufficient-stock scenarios.
INSERT INTO customer (email, name, tier)
SELECT 'reference' || g || '@shopflow.test',
       'Reference Customer ' || g,
       (ARRAY ['STANDARD', 'SILVER', 'GOLD'])[1 + g % 3]
FROM generate_series(1, 10) g;

INSERT INTO product (sku, name, category, price, description)
SELECT 'REF-' || lpad(g::text, 3, '0'),
       'Reference Product ' || g,
       (ARRAY ['Books', 'Electronics', 'Home', 'Toys', 'Garden'])[1 + g % 5],
       (g * 1.25)::numeric(12, 2),
       'Reference catalog item ' || g
FROM generate_series(1, 50) g;

INSERT INTO inventory (product_id, available)
SELECT id, CASE WHEN sku = 'REF-050' THEN 5 ELSE 100000 END
FROM product;
