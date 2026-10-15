CREATE TABLE products (id BIGINT PRIMARY KEY, name VARCHAR(100) NOT NULL, category VARCHAR(40) NOT NULL, price DECIMAL(12,2) NOT NULL);
INSERT INTO products (id, name, category, price)
SELECT X, 'Producto ' || X,
 CASE MOD(X,4) WHEN 0 THEN 'Tecnologia' WHEN 1 THEN 'Hogar' WHEN 2 THEN 'Deporte' ELSE 'Libros' END,
 CAST(5 + MOD(X * 37, 100000) / 100.0 AS DECIMAL(12,2))
FROM SYSTEM_RANGE(1,100000);
