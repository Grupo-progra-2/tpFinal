-- Script para crear la base de datos del cine
CREATE DATABASE IF NOT EXISTS cine;
USE cine;

CREATE TABLE IF NOT EXISTS pelicula (
    id_pelicula INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL UNIQUE,
    genero VARCHAR(50),
    duracion_minutos INT NOT NULL,
    clasificacion VARCHAR(10),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS sala (
    id_sala INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    capacidad INT NOT NULL,
    habilitada BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    clave_acceso VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS empleado (
    id_empleado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    legajo VARCHAR(30) NOT NULL UNIQUE,
    clave_acceso VARCHAR(255),
    es_administrador BOOLEAN NOT NULL DEFAULT FALSE,
    dia_libre VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS funcion (
    id_funcion INT AUTO_INCREMENT PRIMARY KEY,
    id_pelicula INT NOT NULL,
    id_sala INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    precio_base DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    INDEX idx_funcion_sala_hora (id_sala, fecha_hora),
    FOREIGN KEY (id_pelicula) REFERENCES pelicula(id_pelicula),
    FOREIGN KEY (id_sala) REFERENCES sala(id_sala)
);

CREATE TABLE IF NOT EXISTS reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_funcion INT NOT NULL,
    numero_asiento INT NOT NULL,
    tipo_entrada VARCHAR(20) NOT NULL,
    precio_final DECIMAL(10,2) NOT NULL,
    fecha_reserva DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    asiento_confirmado INT GENERATED ALWAYS AS (
        CASE WHEN estado = 'CONFIRMADA' THEN numero_asiento ELSE NULL END
    ) STORED,
    UNIQUE KEY uq_reserva_asiento_confirmado (id_funcion, asiento_confirmado),
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    FOREIGN KEY (id_funcion) REFERENCES funcion(id_funcion)
);

-- Datos de ejemplo: 2 salas y 20 peliculas
-- (INSERT IGNORE evita duplicados si se ejecuta el script mas de una vez)
INSERT IGNORE INTO sala (numero, capacidad, habilitada) VALUES
    (1, 60, TRUE),
    (2, 40, TRUE);

INSERT IGNORE INTO pelicula (titulo, genero, duracion_minutos, clasificacion, activa) VALUES
    ('El Padrino', 'Drama', 175, '+16', TRUE),
    ('Volver al Futuro', 'Ciencia ficción', 116, 'ATP', TRUE),
    ('Jurassic Park', 'Aventura', 127, '+13', TRUE),
    ('Toy Story', 'Animación', 81, 'ATP', TRUE),
    ('Matrix', 'Ciencia ficción', 136, '+16', TRUE),
    ('Titanic', 'Romance', 195, '+13', TRUE),
    ('El Rey León', 'Animación', 88, 'ATP', TRUE),
    ('Gladiador', 'Acción', 155, '+16', TRUE),
    ('Forrest Gump', 'Drama', 142, '+13', TRUE),
    ('Shrek', 'Animación', 90, 'ATP', TRUE),
    ('El Señor de los Anillos', 'Fantasía', 178, '+13', TRUE),
    ('Harry Potter y la piedra filosofal', 'Fantasía', 152, 'ATP', TRUE),
    ('Mad Max: Furia en el camino', 'Acción', 120, '+16', TRUE),
    ('El Resplandor', 'Terror', 146, '+18', TRUE),
    ('Psicosis', 'Terror', 109, '+16', TRUE),
    ('Intensamente', 'Animación', 95, 'ATP', TRUE),
    ('Mi pobre angelito', 'Comedia', 103, 'ATP', TRUE),
    ('Nueve reinas', 'Suspenso', 114, '+13', TRUE),
    ('El secreto de sus ojos', 'Suspenso', 129, '+16', TRUE),
    ('Relatos salvajes', 'Comedia', 122, '+16', TRUE);
