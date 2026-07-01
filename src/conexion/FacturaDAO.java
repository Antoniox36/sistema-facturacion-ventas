package conexion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase DAO encargada de gestionar de forma transaccional el registro
 * de comprobantes de venta (Encabezado y Detalle) en PostgreSQL.
 */
public class FacturaDAO {

    /**
     * Registra una venta completa de forma segura utilizando transacciones y BigDecimal.
     * Invoca la función almacenada para el encabezado e inserta cada ítem del detalle.
     * 
     * @param numeroDocumento Correlativo único o código del DTE.[cite: 1]
     * @param idCliente ID del receptor/cliente.[cite: 1]
     * @param idUsuario ID del usuario/cajero que procesa la venta.[cite: 1]
     * @param tipoPago Método de pago (EFECTIVO, TARJETA, etc.).[cite: 1]
     * @param totalVenta Monto total acumulado en formato BigDecimal.[cite: 1]
     * @param detalles Lista de objetos estructurados con los datos de los productos.
     * @return true si la venta se procesó con éxito, false en caso de fallo.
     */
    public boolean registrarVentaCompleta(String numeroDocumento, int idCliente, int idUsuario, 
                                         String tipoPago, BigDecimal totalVenta, List<DetalleVentaDTO> detalles) {
        
        String sqlEncabezado = "SELECT sp_registrar_factura_completa(?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_factura (id_factura, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        
        Connection con = null;
        PreparedStatement psEncabezado = null;
        PreparedStatement psDetalle = null;
        ResultSet rs = null;

        try {
            con = ConexionBD.conectar();
            if (con == null) return false;

            // Desactivar el autocommit para controlar la transacción manualmente
            con.setAutoCommit(false);

            // Ejecutar la función almacenada pasando el BigDecimal explícito para mapear con NUMERIC[cite: 1]
            psEncabezado = con.prepareStatement(sqlEncabezado);
            psEncabezado.setString(1, numeroDocumento);
            psEncabezado.setInt(2, idCliente);
            psEncabezado.setInt(3, idUsuario);
            psEncabezado.setString(4, tipoPago);
            psEncabezado.setBigDecimal(5, totalVenta); 
            
            rs = psEncabezado.executeQuery();
            
            int idFacturaGenerada = 0;
            if (rs.next()) {
                idFacturaGenerada = rs.getInt(1);
            }

            if (idFacturaGenerada == 0) {
                throw new SQLException("No se pudo obtener el ID de la factura generado por el procedimiento.");
            }

            // Preparar el lote de inserciones para el detalle[cite: 1]
            psDetalle = con.prepareStatement(sqlDetalle);
            
            for (DetalleVentaDTO item : detalles) {
                psDetalle.setInt(1, idFacturaGenerada);
                psDetalle.setInt(2, item.getIdProducto());
                psDetalle.setInt(3, item.getCantidad());
                psDetalle.setBigDecimal(4, item.getPrecioUnitario());
                
                psDetalle.addBatch();
            }

            // Ejecutar el lote completo (Dispara el TRIGGER tr_descontar_stock_venta)[cite: 1]
            psDetalle.executeBatch();

            // Consolidar la transacción permanentemente
            con.commit();
            System.out.println("✅ Venta registrada con éxito. Factura ID: " + idFacturaGenerada);
            System.out.println("📦 Inventario actualizado automáticamente mediante Trigger de PostgreSQL.");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error crítico en la transacción de venta. Aplicando Rollback...");
            System.err.println("Motivo del fallo: " + e.getMessage());
            
            if (con != null) {
                try {
                    con.rollback();
                    System.out.println("🛡️ Rollback ejecutado correctamente. La base de datos no sufrió modificaciones.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (psEncabezado != null) psEncabezado.close();
                if (psDetalle != null) psDetalle.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Clase interna de transferencia de datos (DTO) para representar un elemento del detalle de venta.
     */
    public static class DetalleVentaDTO {
        private final int idProducto;
        private final int cantidad;
        private final BigDecimal precioUnitario;

        public DetalleVentaDTO(int idProducto, int cantidad, BigDecimal precioUnitario) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public int getIdProducto() { return idProducto; }
        public int getCantidad() { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
    }
}