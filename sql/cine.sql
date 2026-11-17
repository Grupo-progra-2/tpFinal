-- Script para crear la base de datos del cine
CREATE DATABASE IF NOT EXISTS cine;
USE cine;

CREATE TABLE pelicula (
    id_pelicula INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    genero VARCHAR(50),
    duracion_minutos INT NOT NULL,
    clasificacion VARCHAR(10),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sala (
    id_sala INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    capacidad INT NOT NULL,
    habilitada BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100)
);

CREATE TABLE funcion (
    id_funcion INT AUTO_INCREMENT PRIMARY KEY,
    id_pelicula INT NOT NULL,
    id_sala INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    precio_base DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (id_pelicula) REFERENCES pelicula(id_pelicula),
    FOREIGN KEY (id_sala) REFERENCES sala(id_sala)
);

CREATE TABLE reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_funcion INT NOT NULL,
    numero_asiento INT NOT NULL,
    tipo_entrada VARCHAR(20) NOT NULL,
    precio_final DECIMAL(10,2) NOT NULL,
    fecha_reserva DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    FOREIGN KEY (id_funcion) REFERENCES funcion(id_funcion)
);
