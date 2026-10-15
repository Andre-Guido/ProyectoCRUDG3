-- Creación de la base de datos.
CREATE DATABASE bdventas_1;

-- Creación de esquema general para base de datos.
CREATE SCHEMA catalogos;

-- Creación de tabla de marcas.
CREATE TABLE catalogos.marcas
(
	id_marca SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL UNIQUE,
	pais_origen VARCHAR(80) NOT NULL,
	estado SMALLINT NOT NULL DEFAULT 1

	CONSTRAINT chk_marca_estado
	CHECK (estado IN (0,1))
);

-- Pruebas para validar los CONSTRAINTS.
-- Prueba 1: No permite valores nulos.
INSERT INTO catalogos.marcas (nombre, pais_origen, estado) 
VALUES (NULL, 'Nicaragua', 1);

-- Prueba 2: No permitir que la columna nombre tenga el mismo valor 2 veces.
INSERT INTO catalogos.marcas (nombre, pais_origen, estado)
VALUES ('Almohada', 'Nicaragua', 1);

-- Prueba 3: Agregar el registro por defecto de una fila en la columna de estado.
INSERT INTO catalogos.marcas (nombre, pais_origen, estado)
VALUES ('Audifonos', 'Nicaragua');

-- TABLA 2: Categorías.
CREATE TABLE catalogos.categorias
(
	id_categoria SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL UNIQUE,
	descripcion VARCHAR(250),
	estado SMALLINT NOT NULL DEFAULT 1

	CONSTRAINT chk_categoria_estado
		CHECK (estado IN (0,1))
);

-- Prueba 1: No permite valores nulos.
INSERT INTO catalogos.categorias (nombre, descripcion, estado)
VALUES (NULL, 'Ropa de segunda mano', 1);

-- Prueba 2: 

-- Tabla 3: Productos.
CREATE TABLE catalogos.productos
(
	id_producto SERIAL PRIMARY KEY,
	codigo VARCHAR(20) NOT NULL UNIQUE,
	nombre VARCHAR(120) NOT NULL UNIQUE,
	descripcion VARCHAR(200),
	precio NUMERIC(10, 2) NOT NULL,
	stock INTEGER NOT NULL DEFAULT 0,
	fecha_creacion DATE NOT NULL DEFAULT CURRENT_DATE,
	id_categoria INTEGER NOT NULL,
	id_marca INTEGER NOT NULL,
	estado SMALLINT NOT NULL DEFAULT 1,

	CONSTRAINT fk_producto_categoria
	FOREIGN KEY (id_categoria)
	REFERENCES catalogos.categorias(id_categoria),

	CONSTRAINT fk_producto_marca
	FOREIGN KEY (id_marca)
	REFERENCES catalogos.marcas(id_marca),

	CONSTRAINT chk_producto_precio
	CHECK (precio > 0),

	CONSTRAINT chk_producto_stock
	CHECK(stock >=0),

	CONSTRAINT chk_producto_estado
	CHECK (estado IN (0,1))
);

-- Marcas (id 1 al 5)
INSERT INTO catalogos.marcas (nombre, pais_origen) VALUES
('Samsung', 'Corea del Sur'),   -- 1
('Apple',   'Estados Unidos'),  -- 2
('Sony',    'Japón'),           -- 3
('Lenovo',  'China'),           -- 4
('Xiaomi',  'China');           -- 5

-- Categorías (id 1 al 5)
INSERT INTO catalogos.categorias (nombre, descripcion) VALUES
('Celulares',   'Teléfonos inteligentes de distintas gamas'),             -- 1
('Laptops',     'Computadoras portátiles para hogar, estudio y oficina'),  -- 2
('Audio',       'Audífonos, auriculares y bocinas'),                       -- 3
('Televisores', 'Pantallas Smart TV de distintas pulgadas'),               -- 4
('Accesorios',  'Cargadores, mouse, controles y otros complementos');      -- 5

-- Productos
INSERT INTO catalogos.productos
    (codigo, nombre, descripcion, precio, stock, id_categoria, id_marca)
VALUES
('CEL-001', 'Samsung Galaxy A55 128GB',     'Pantalla AMOLED de 6.6", 8 GB de RAM y cámara de 50 MP',        389.99,  25, 1, 1),
('CEL-002', 'iPhone 15 128GB',              'Pantalla de 6.1", chip A16 Bionic y puerto USB-C',              799.00,  12, 1, 2),
('CEL-003', 'Xiaomi Redmi Note 13 256GB',   'Pantalla AMOLED de 6.67", 8 GB de RAM y batería de 5000 mAh',   249.90,  40, 1, 5),
('LAP-001', 'Lenovo IdeaPad Slim 3 15"',    'Intel Core i5, 16 GB de RAM y SSD de 512 GB',                   549.00,  10, 2, 4),
('LAP-002', 'MacBook Air 13" M3',           'Chip Apple M3, 8 GB de memoria unificada y SSD de 256 GB',     1099.00,   6, 2, 2),
('LAP-003', 'Samsung Galaxy Book4 15"',     'Intel Core i7, 16 GB de RAM y SSD de 512 GB',                   699.99,   8, 2, 1),
('AUD-001', 'Sony WH-1000XM5',              'Audífonos inalámbricos over-ear con cancelación de ruido',      379.99,  15, 3, 3),
('AUD-002', 'Apple AirPods Pro 2',          'Auriculares inalámbricos con cancelación activa de ruido',      249.00,  20, 3, 2),
('AUD-003', 'Xiaomi Redmi Buds 5',          'Auriculares Bluetooth con estuche de carga',                     39.90,  60, 3, 5),
('TV-001',  'Samsung Crystal UHD 55"',      'Smart TV 4K de 55 pulgadas',                                    479.00,   9, 4, 1),
('TV-002',  'Sony Bravia 65" 4K',           'Smart TV 4K de 65 pulgadas con Google TV',                     1199.00,   4, 4, 3),
('TV-003',  'Xiaomi TV A Pro 43"',          'Smart TV 4K de 43 pulgadas con Google TV',                      299.00,  14, 4, 5),
('ACC-001', 'Cargador Samsung USB-C 25W',   'Cargador de pared de carga rápida con cable incluido',           19.99, 100, 5, 1),
('ACC-002', 'Mouse inalámbrico Lenovo 300', 'Mouse óptico con receptor USB de 2.4 GHz',                       14.50,  75, 5, 4),
('ACC-003', 'Control DualSense PS5',        'Control inalámbrico para PlayStation 5',                         74.99,  30, 5, 3);

-- Consulta 1:
SELECT *
FROM catalogos.productos
WHERE estado = 1 AND stock >=0;

-- Consulta 2:
SELECT * 
FROM catalogos.productos
WHERE precio >= 50;

-- Consulta 3:
SELECT *
FROM catalogos.productos
WHERE precio BETWEEN 20 AND 100;

SELECT * 
FROM catalogos.productos
WHERE precio >= 20 AND precio <=100;

-- Consulta 4:
SELECT *
FROM catalogos.productos
WHERE nombre ILIKE '%samsung%';

-- Consulta 5:
SELECT *
FROM catalogos.productos
WHERE codigo ILIKE '%cel%' OR nombre ILIKE '%sam%';