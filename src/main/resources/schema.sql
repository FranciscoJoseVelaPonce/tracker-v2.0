CREATE TABLE IF NOT EXISTS empresa (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    sector          VARCHAR(60)  NOT NULL,
    web             VARCHAR(255),
    fecha_registro  DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS oferta (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa         INT           NOT NULL,
    titulo             VARCHAR(150)  NOT NULL,
    descripcion        TEXT,
    estado             VARCHAR(20)   NOT NULL,
    fecha_postulacion  DATE          NOT NULL,
    enlace             VARCHAR(255),
    salario            DECIMAL(10,2),
    notas              TEXT,
    CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa (id),
    CONSTRAINT ck_oferta_estado CHECK (estado IN ('pendiente', 'entrevista', 'rechazado', 'aceptado')),
    CONSTRAINT ck_oferta_salario CHECK (salario IS NULL OR salario >= 0)
);

CREATE TABLE IF NOT EXISTS entrevista (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_oferta  INT          NOT NULL,
    fecha      DATE         NOT NULL,
    tipo       VARCHAR(20)  NOT NULL,
    notas      TEXT,
    resultado  VARCHAR(20)  NOT NULL DEFAULT 'pendiente',
    CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta) REFERENCES oferta (id),
    CONSTRAINT ck_entrevista_tipo CHECK (tipo IN ('telefónica', 'técnica', 'presencial', 'online')),
    CONSTRAINT ck_entrevista_resultado CHECK (resultado IN ('pendiente', 'positivo', 'negativo'))
);
