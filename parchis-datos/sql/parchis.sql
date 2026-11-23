-- Base de datos del Parchis en red (Hito 2B).
-- Cada computadora guarda los perfiles de quienes juegan en ella.
-- Ejecutar una vez en MySQL Workbench o en la consola de MySQL.

CREATE DATABASE IF NOT EXISTS parchis
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE parchis;

CREATE TABLE IF NOT EXISTS perfil (
    nombre            VARCHAR(30) PRIMARY KEY,
    color_preferido   VARCHAR(10) NOT NULL,
    partidas_jugadas  INT NOT NULL DEFAULT 0,
    partidas_ganadas  INT NOT NULL DEFAULT 0
);
