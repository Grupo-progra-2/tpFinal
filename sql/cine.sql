-- Script para crear la base de datos del cine
CREATE DATABASE IF NOT EXISTS cine;
USE cine;

CREATE TABLE IF NOT EXISTS pelicula (
    id_pelicula INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
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
