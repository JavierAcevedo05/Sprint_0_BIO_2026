-- ----------------------------------------------------------
-- ----------------------------------------------------------
-- Base de datos: bd_codigos
-- Guarda los códigos que llegan del beacon.
-- ----------------------------------------------------------

CREATE DATABASE IF NOT EXISTS bd_codigos
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE bd_codigos;

-- ----------------------------------------------------------
-- ----------------------------------------------------------
-- id:Entero, valor:Entero, fecha:Fecha -> tabla CODIGO()
-- id    : clave primaria, autoincrementa
-- valor : código del beacon (entero entre 0 y 65535)
-- fecha : momento en el que el servidor recibe el código
-- ----------------------------------------------------------

CREATE TABLE IF NOT EXISTS CODIGO (
  id     INT      NOT NULL AUTO_INCREMENT,
  valor  INT      NOT NULL,
  fecha  DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT chk_valor CHECK (valor BETWEEN 0 AND 65535)
) ENGINE=InnoDB;
