-- ===========================================================================
-- 1. CREACIÓN DE TABLAS (Estructura Relacional)
-- ===========================================================================

-- Tabla de Roles para control de acceso
CREATE TABLE roles (
    id_rol SERIAL PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla de Usuarios (Administradores, Cajeros, etc.)
CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    id_rol INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- Tabla de Clientes (Soporta Consumidor Final y Contribuyentes DTE)
CREATE TABLE clientes (
    id_cliente SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE, -- DUI o NIT para la facturación local
    registro_contribuyente VARCHAR(20) UNIQUE, -- NRC si aplica para crédito fiscal
    direccion TEXT,
    telefono VARCHAR(15)
);

-- Tabla de Productos / Inventario
CREATE TABLE productos (
    id_producto SERIAL PRIMARY KEY,
    codigo_barras VARCHAR(50) UNIQUE NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    precio_venta NUMERIC(10, 2) NOT NULL CHECK (precio_venta >= 0),
    stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INT NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0)
);

-- Tabla de Facturas (Encabezado)
CREATE TABLE facturas (
    id_factura SERIAL PRIMARY KEY,
    numero_documento VARCHAR(50) UNIQUE NOT NULL, -- Correlativo o código DTE
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL, -- Cajero que realizó la venta
    tipo_pago VARCHAR(20) NOT NULL DEFAULT 'EFECTIVO', -- EFECTIVO, TARJETA, TRANSFERENCIA
    total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- Tabla de Detalle de Factura (Cuerpo)
CREATE TABLE detalle_factura (
    id_detalle SERIAL PRIMARY KEY,
    id_factura INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(10, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal NUMERIC(10, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    FOREIGN KEY (id_factura) REFERENCES facturas(id_factura) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

-- ===========================================================================
-- 2. TRIGGER PARA AUTOMATIZAR EL INVENTARIO
-- ===========================================================================

-- Función que ejecutará el Trigger para restar stock tras una venta
CREATE OR REPLACE FUNCTION fn_actualizar_inventario_venta()
RETURNS TRIGGER AS $$
BEGIN
    -- Validar si hay suficiente stock disponible antes de realizar la venta
    IF (SELECT stock_actual FROM productos WHERE id_producto = NEW.id_producto) < NEW.cantidad THEN
        RAISE EXCEPTION 'Stock insuficiente para el producto con ID %', NEW.id_producto;
    END IF;

    -- Restar la cantidad vendida del stock actual
    UPDATE productos
    SET stock_actual = stock_actual - NEW.cantidad
    WHERE id_producto = NEW.id_producto;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Creación del Trigger conectado a la tabla detalle_factura
CREATE TRIGGER tr_descontar_stock_venta
AFTER INSERT ON detalle_factura
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_inventario_venta();

-- ===========================================================================
-- 3. PROCEDIMIENTO ALMACENADO / FUNCIÓN DE NEGOCIO
-- ===========================================================================

-- Función para registrar una venta completa de forma transaccional y segura
CREATE OR REPLACE FUNCTION sp_registrar_factura_completa(
    p_numero_documento VARCHAR(50),
    p_id_cliente INT,
    p_id_usuario INT,
    p_tipo_pago VARCHAR(20),
    p_total_venta NUMERIC(10,2)
) RETURNS INT AS $$
DECLARE
    v_id_factura INT;
BEGIN
    -- Insertar el encabezado de la factura
    INSERT INTO facturas (numero_documento, id_cliente, id_usuario, tipo_pago, total)
    VALUES (p_numero_documento, p_id_cliente, p_id_usuario, p_tipo_pago, p_total_venta)
    RETURNING id_factura INTO v_id_factura;

    -- Retornamos el ID generado para que el backend sepa dónde insertar los detalles
    RETURN v_id_factura;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al registrar la factura: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================================
-- 4. INSERCIONES INICIALES DE PRUEBA (Seed Data)
-- ===========================================================================
INSERT INTO roles (nombre_rol) VALUES ('ADMINISTRADOR'), ('CAJERO');
INSERT INTO usuarios (username, password_hash, nombre_completo, id_rol) VALUES ('admin', 'hash_seguro_aqui', 'Administrador General', 1);
INSERT INTO clientes (nombre, documento_identidad, direccion) VALUES ('Consumidor Final', '00000000-0', 'Sin Dirección');
INSERT INTO productos (codigo_barras, descripcion, precio_venta, stock_actual) VALUES ('7401001122334', 'Producto de Prueba A', 1.50, 100);