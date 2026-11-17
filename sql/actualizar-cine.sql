-- Solo para bases creadas con el script original de feature/modelos.
-- Sacar una copia de seguridad antes de aplicarlo. Revisar los duplicados
-- con el SELECT y resolverlos manualmente si devuelve filas.
USE cine;

SELECT id_funcion, numero_asiento, COUNT(*) AS confirmadas
FROM reserva
WHERE estado = 'CONFIRMADA'
GROUP BY id_funcion, numero_asiento
HAVING COUNT(*) > 1;

ALTER TABLE reserva
    ADD COLUMN asiento_confirmado INT GENERATED ALWAYS AS (
        CASE WHEN estado = 'CONFIRMADA' THEN numero_asiento ELSE NULL END
    ) STORED,
    ADD UNIQUE KEY uq_reserva_asiento_confirmado (id_funcion, asiento_confirmado);

ALTER TABLE cliente
    ADD COLUMN apellido VARCHAR(100),
    ADD COLUMN clave_acceso VARCHAR(255);

ALTER TABLE funcion ADD INDEX idx_funcion_sala_hora (id_sala, fecha_hora);
