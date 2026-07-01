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
     * Simulación global de la capa de Servicio con desglose de IVA integrado.
     */
    public static void main(String[] args) {
        System.out.println("Iniciando entorno de pruebas con cálculo impositivo...");
        
        FacturacionService facturacionService = new FacturacionService();

        // Datos de control para el comprobante[cite: 1]
        String correlativoDTE = "DTE-2026-10065";
        int idCliente = 1; // Consumidor Final[cite: 1]
        int idCajero = 1;  // Administrador[cite: 1]
        String formaPago = "EFECTIVO";

        // Simulamos un carrito de compras
        List<FacturacionService.ItemCarritoDTO> carrito = new ArrayList<>();
        
        // Compramos 3 unidades del Producto 1 (Precio: $1.50 c/u)[cite: 1]
        // Total de la operación debería ser $4.50
        carrito.add(new FacturacionService.ItemCarritoDTO(1, 3, new BigDecimal("1.50")));

        System.out.println("\n--- PROCESANDO CARRITO EN EL SERVICE CAPA ---");
        boolean exito = facturacionService.procesarNuevaVenta(correlativoDTE, idCliente, idCajero, formaPago, carrito);

        if (exito) {
            System.out.println("\n🎉 ¡Cálculo de impuestos verificado e insertado correctamente!");
        } else {
            System.err.println("\n⚠️ Error al procesar la venta con impuestos.");
        }
    }
}