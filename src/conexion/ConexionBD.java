package conexion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de gestionar la conexión transaccional y eficiente
 * entre la aplicación de Java y el motor de base de datos PostgreSQL.
 */
public class ConexionBD {

    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "5432";
    private static final String BASE_DATOS = "db_facturacion_ventas";
    private static final String URL = "jdbc:postgresql://" + SERVIDOR + ":" + PUERTO + "/" + BASE_DATOS;
    private static final String USUARIO = "postgres";
    private static final String CONTRASENA = "Bykarsten2134.";

    /**
     * Obtiene una conexión activa a la base de datos de facturación.
     */
    public static Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: No se encontró el Driver JDBC de PostgreSQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Error de SQL: No se pudo conectar. Verifica credenciales.");
            e.printStackTrace();
        }
        return conexion;
    }

    /**
     * Simulación y testing del motor transaccional del backend usando tipos de datos monetarios correctos.
     */
    public static void main(String[] args) {
        System.out.println("Iniciando entorno de pruebas transaccionales...");
        
        FacturaDAO facturaDao = new FacturaDAO();

        // 1. Configurar datos del encabezado basados en el seed data[cite: 1]
        int idCliente = 1; 
        int idUsuario = 1; 
        String tipoPago = "EFECTIVO";

        // --- PRUEBA 1: SIMULANDO VENTA EXITOSA (Dentro del Stock) ---
        System.out.println("\n--- SIMULANDO VENTA EXITOSA (Dentro del Stock) ---");
        String numeroDTE1 = "DTE-2026-00001";
        BigDecimal totalFactura1 = new BigDecimal("15.00"); // 10 unidades * $1.50

        List<FacturaDAO.DetalleVentaDTO> detalleVenta1 = new ArrayList<>();
        // Pasamos: id_producto, cantidad, precio_unitario como BigDecimal[cite: 1]
        detalleVenta1.add(new FacturaDAO.DetalleVentaDTO(1, 10, new BigDecimal("1.50")));

        facturaDao.registrarVentaCompleta(numeroDTE1, idCliente, idUsuario, tipoPago, totalFactura1, detalleVenta1);

        // --- PRUEBA 2: SIMULANDO VENTA FALLIDA (Excediendo el Stock) ---
        System.out.println("\n--- SIMULANDO VENTA FALLIDA (Excediendo el Stock para probar el Trigger) ---");
        String numeroDTE2 = "DTE-2026-00002";
        BigDecimal totalFactura2 = new BigDecimal("225.00"); // 150 unidades * $1.50

        List<FacturaDAO.DetalleVentaDTO> detalleVenta2 = new ArrayList<>();
        // Tu script inserta 100 unidades iniciales; solicitar 150 activará la excepción de tu trigger[cite: 1]
        detalleVenta2.add(new FacturaDAO.DetalleVentaDTO(1, 150, new BigDecimal("1.50")));
        
        facturaDao.registrarVentaCompleta(numeroDTE2, idCliente, idUsuario, tipoPago, totalFactura2, detalleVenta2);
    }
}