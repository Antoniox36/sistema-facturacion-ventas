package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de gestionar la conexión transaccional y eficiente 
 * entre la aplicación de Java y el motor de base de datos PostgreSQL.
 */
public class ConexionBD {

    // Configuración de los parámetros de conexión local
    private static final String SERVIDOR = "localhost";
    private static final String PUERTO = "5432";
    private static final String BASE_DATOS = "db_facturacion_ventas";
    private static final String URL = "jdbc:postgresql://" + SERVIDOR + ":" + PUERTO + "/" + BASE_DATOS;
    private static final String USUARIO = "postgres";
    
    // IMPORTANTE: Modifica esta contraseña por la que asignaste en la instalación
    private static final String CONTRASENA = "Bykarsten2134."; 

    /**
     * Obtiene una conexión activa a la base de datos de facturación.
     * @return Objeto Connection si la conexión fue exitosa, null en caso contrario.
     */
    public static Connection conectar() {
        Connection conexion = null;
        try {
            // Registrar el driver de PostgreSQL (opcional en versiones modernas de Java, pero buena práctica)
            Class.forName("org.postgresql.Driver");
            
            // Establecer el puente de conexión
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
            System.out.println("✅ Conexión exitosa a la base de datos: " + BASE_DATOS);
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: No se encontró el Driver JDBC de PostgreSQL en la carpeta lib.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Error de SQL: No se pudo conectar a la base de datos. Verifica credenciales o que el servicio esté activo.");
            e.printStackTrace();
        }
        return conexion;
    }

    /**
     * Método principal (Main) para realizar una prueba de conexión rápida desde VS Code.
     */
    public static void main(String[] args) {
        System.out.println("Iniciando prueba de conexión...");
        Connection prueba = ConexionBD.conectar();
        
        if (prueba != null) {
            try {
                // Cerrar la conexión de prueba de forma segura
                prueba.close();
                System.out.println("🔒 Conexión de prueba cerrada correctamente.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión de prueba.");
                e.printStackTrace();
            }
        }
    }
}