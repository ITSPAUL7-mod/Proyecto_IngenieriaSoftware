CREATE TABLE IF NOT EXISTS institucion (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    direccion VARCHAR(250),
    telefono VARCHAR(30),
    contacto_principal VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rol (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(250),
    nivel_acceso SMALLINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_rol_nivel CHECK (nivel_acceso BETWEEN 0 AND 100)
);

CREATE TABLE IF NOT EXISTS permiso (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    modulo VARCHAR(60) NOT NULL,
    descripcion VARCHAR(250),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    institucion_id BIGINT REFERENCES institucion(id),
    rol_id BIGINT NOT NULL REFERENCES rol(id),
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(180) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    telefono_cifrado TEXT,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'INACTIVO')),
    CONSTRAINT ck_usuario_password_hash CHECK (password_hash LIKE '$2a$%' OR password_hash LIKE '$2b$%')
);

CREATE TABLE IF NOT EXISTS rol_permiso (
    id BIGSERIAL PRIMARY KEY,
    rol_id BIGINT NOT NULL REFERENCES rol(id),
    permiso_id BIGINT NOT NULL REFERENCES permiso(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    asignado_por BIGINT REFERENCES usuario(id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_rol_permiso UNIQUE (rol_id, permiso_id)
);

CREATE TABLE IF NOT EXISTS dispositivo_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    fingerprint_hash VARCHAR(128) NOT NULL,
    nombre_dispositivo VARCHAR(120),
    estado VARCHAR(20) NOT NULL,
    ultimo_acceso TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    revocado_en TIMESTAMPTZ,
    CONSTRAINT ck_dispositivo_estado CHECK (estado IN ('CONFIABLE', 'BLOQUEADO', 'PENDIENTE'))
);

CREATE TABLE IF NOT EXISTS ubicacion (
    id BIGSERIAL PRIMARY KEY,
    institucion_id BIGINT NOT NULL REFERENCES institucion(id),
    ubicacion_padre_id BIGINT REFERENCES ubicacion(id),
    nombre VARCHAR(120) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    direccion VARCHAR(250),
    referencia VARCHAR(250),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_ubicacion_tipo CHECK (tipo IN ('SEDE', 'EDIFICIO', 'PISO', 'ZONA', 'BODEGA'))
);

CREATE TABLE IF NOT EXISTS categoria_objeto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion VARCHAR(250),
    icono VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS objeto_encontrado (
    id BIGSERIAL PRIMARY KEY,
    institucion_id BIGINT NOT NULL REFERENCES institucion(id),
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    caracteristicas_privadas TEXT,
    categoria_id BIGINT NOT NULL REFERENCES categoria_objeto(id),
    ubicacion_id BIGINT NOT NULL REFERENCES ubicacion(id),
    encontrado_por_id BIGINT NOT NULL REFERENCES usuario(id),
    fecha_encontrado TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL,
    codigo_qr UUID UNIQUE NOT NULL,
    foto_ruta TEXT,
    foto_hash VARCHAR(64),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_objeto_estado CHECK (estado IN ('REGISTRADO', 'EN_CUSTODIA', 'RECLAMADO', 'ENTREGADO')),
    CONSTRAINT ck_objeto_foto_hash CHECK (foto_hash IS NULL OR length(foto_hash) = 64)
);

CREATE TABLE IF NOT EXISTS reporte_perdida (
    id BIGSERIAL PRIMARY KEY,
    institucion_id BIGINT NOT NULL REFERENCES institucion(id),
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    caracteristicas_privadas TEXT,
    categoria_id BIGINT NOT NULL REFERENCES categoria_objeto(id),
    ubicacion_perdida_id BIGINT REFERENCES ubicacion(id),
    fecha_perdida TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reporte_estado CHECK (estado IN ('ACTIVO', 'COINCIDENCIA', 'RECUPERADO', 'CERRADO'))
);

CREATE TABLE IF NOT EXISTS coincidencia (
    id BIGSERIAL PRIMARY KEY,
    objeto_encontrado_id BIGINT NOT NULL REFERENCES objeto_encontrado(id),
    reporte_perdida_id BIGINT NOT NULL REFERENCES reporte_perdida(id),
    puntaje_categoria NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (puntaje_categoria BETWEEN 0 AND 100),
    puntaje_descripcion NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (puntaje_descripcion BETWEEN 0 AND 100),
    puntaje_ubicacion NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (puntaje_ubicacion BETWEEN 0 AND 100),
    puntaje_fecha NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (puntaje_fecha BETWEEN 0 AND 100),
    puntaje_caracteristicas NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (puntaje_caracteristicas BETWEEN 0 AND 100),
    puntaje_total NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (puntaje_total BETWEEN 0 AND 100),
    estado VARCHAR(30) NOT NULL,
    verificado_por_id BIGINT REFERENCES usuario(id),
    fecha_coincidencia TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_coincidencia_estado CHECK (estado IN ('SUGERIDA', 'REVISADA', 'DESCARTADA', 'RECLAMADA')),
    CONSTRAINT uq_coincidencia_par UNIQUE (objeto_encontrado_id, reporte_perdida_id)
);

CREATE TABLE IF NOT EXISTS reclamacion (
    id BIGSERIAL PRIMARY KEY,
    coincidencia_id BIGINT UNIQUE NOT NULL REFERENCES coincidencia(id),
    usuario_solicitante_id BIGINT NOT NULL REFERENCES usuario(id),
    estado VARCHAR(30) NOT NULL,
    motivo_rechazo TEXT,
    fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_resolucion TIMESTAMPTZ,
    atendido_por_id BIGINT REFERENCES usuario(id),
    documento_verificado_hash VARCHAR(128),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reclamacion_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'ENTREGADA'))
);

CREATE TABLE IF NOT EXISTS evidencia_devolucion (
    id BIGSERIAL PRIMARY KEY,
    reclamacion_id BIGINT NOT NULL REFERENCES reclamacion(id),
    tipo_evidencia VARCHAR(50) NOT NULL,
    ruta_archivo TEXT NOT NULL,
    hash_archivo VARCHAR(64) NOT NULL,
    descripcion VARCHAR(300),
    subido_por_id BIGINT NOT NULL REFERENCES usuario(id),
    clasificacion VARCHAR(20) NOT NULL DEFAULT 'SENSIBLE',
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_evidencia_tipo CHECK (tipo_evidencia IN ('FACTURA', 'FOTO', 'SEÑA', 'FIRMA', 'OTRA')),
    CONSTRAINT ck_evidencia_hash CHECK (length(hash_archivo) = 64)
);

CREATE TABLE IF NOT EXISTS punto_entrega (
    id BIGSERIAL PRIMARY KEY,
    institucion_id BIGINT NOT NULL REFERENCES institucion(id),
    ubicacion_id BIGINT NOT NULL REFERENCES ubicacion(id),
    nombre_punto VARCHAR(120) NOT NULL,
    encargado_id BIGINT REFERENCES usuario(id),
    horario_atencion VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS valoracion_servicio (
    id BIGSERIAL PRIMARY KEY,
    reclamacion_id BIGINT UNIQUE NOT NULL REFERENCES reclamacion(id),
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    calificacion SMALLINT NOT NULL,
    comentario VARCHAR(500),
    fecha_valoracion TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_valoracion_calificacion CHECK (calificacion BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS auditoria_sistema (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuario(id),
    accion VARCHAR(80) NOT NULL,
    entidad_afectada VARCHAR(80) NOT NULL,
    registro_id VARCHAR(80),
    origen VARCHAR(120),
    detalles JSONB,
    hash_integridad VARCHAR(64) NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_auditoria_hash CHECK (length(hash_integridad) = 64)
);

CREATE TABLE IF NOT EXISTS error_sistema (
    id BIGSERIAL PRIMARY KEY,
    codigo_error VARCHAR(60) NOT NULL,
    mensaje_error TEXT NOT NULL,
    traza_pila TEXT,
    modulo VARCHAR(80) NOT NULL,
    origen VARCHAR(120),
    usuario_id BIGINT REFERENCES usuario(id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notificacion (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    titulo VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio TIMESTAMPTZ NOT NULL DEFAULT now(),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_notificacion_tipo CHECK (tipo IN ('MATCH', 'RECLAMO', 'ENTREGA', 'SISTEMA'))
);

CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,
    institucion_id BIGINT REFERENCES institucion(id),
    clave VARCHAR(100) NOT NULL,
    valor_cifrado TEXT NOT NULL,
    descripcion VARCHAR(250),
    categoria VARCHAR(60) NOT NULL,
    actualizado_por_id BIGINT NOT NULL REFERENCES usuario(id),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_configuracion_categoria CHECK (categoria IN ('SEGURIDAD', 'MATCHING', 'OPERACION'))
);

CREATE TABLE IF NOT EXISTS termino_condicion (
    id BIGSERIAL PRIMARY KEY,
    version VARCHAR(30) UNIQUE NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    contenido TEXT NOT NULL,
    contenido_hash VARCHAR(64) NOT NULL,
    fecha_vigencia TIMESTAMPTZ NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por_id BIGINT REFERENCES usuario(id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_termino_hash CHECK (length(contenido_hash) = 64)
);

CREATE TABLE IF NOT EXISTS aceptacion_terminos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    termino_id BIGINT NOT NULL REFERENCES termino_condicion(id),
    fecha_aceptacion TIMESTAMPTZ NOT NULL DEFAULT now(),
    dispositivo_hash VARCHAR(128),
    origen VARCHAR(120),
    aceptado BOOLEAN NOT NULL,
    revocado_en TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- INDICES sobre claves foraneas y campos de consulta frecuente
-- =============================================================================
CREATE INDEX IF NOT EXISTS idx_usuario_institucion ON usuario(institucion_id);
CREATE INDEX IF NOT EXISTS idx_usuario_rol ON usuario(rol_id);
CREATE INDEX IF NOT EXISTS idx_usuario_estado ON usuario(estado);
CREATE INDEX IF NOT EXISTS idx_rolpermiso_rol ON rol_permiso(rol_id);
CREATE INDEX IF NOT EXISTS idx_rolpermiso_permiso ON rol_permiso(permiso_id);
CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario ON dispositivo_usuario(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ubicacion_institucion ON ubicacion(institucion_id);
CREATE INDEX IF NOT EXISTS idx_ubicacion_padre ON ubicacion(ubicacion_padre_id);
CREATE INDEX IF NOT EXISTS idx_objeto_institucion ON objeto_encontrado(institucion_id);
CREATE INDEX IF NOT EXISTS idx_objeto_estado ON objeto_encontrado(estado);
CREATE INDEX IF NOT EXISTS idx_objeto_categoria ON objeto_encontrado(categoria_id);
CREATE INDEX IF NOT EXISTS idx_objeto_ubicacion ON objeto_encontrado(ubicacion_id);
CREATE INDEX IF NOT EXISTS idx_reporte_usuario ON reporte_perdida(usuario_id);
CREATE INDEX IF NOT EXISTS idx_reporte_institucion ON reporte_perdida(institucion_id);
CREATE INDEX IF NOT EXISTS idx_reporte_estado ON reporte_perdida(estado);
CREATE INDEX IF NOT EXISTS idx_coincidencia_objeto ON coincidencia(objeto_encontrado_id);
CREATE INDEX IF NOT EXISTS idx_coincidencia_reporte ON coincidencia(reporte_perdida_id);
CREATE INDEX IF NOT EXISTS idx_reclamacion_solicitante ON reclamacion(usuario_solicitante_id);
CREATE INDEX IF NOT EXISTS idx_evidencia_reclamacion ON evidencia_devolucion(reclamacion_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria_sistema(usuario_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_creado ON auditoria_sistema(creado_en);
CREATE INDEX IF NOT EXISTS idx_notificacion_usuario ON notificacion(usuario_id);
CREATE INDEX IF NOT EXISTS idx_aceptacion_usuario ON aceptacion_terminos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_aceptacion_termino ON aceptacion_terminos(termino_id);

-- La bitacora es inmutable para el usuario de aplicacion (Regla "Auditoria").
-- REVOKE UPDATE, DELETE ON auditoria_sistema FROM <usuario_aplicacion>;
