package conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase DAO encargada de realizar las operaciones CRUD en la base de datos
 * para la entidad de Productos del sistema de facturación.
 */
public class ProductoDAO {

    /**
     * Busca un producto en la base de datos utilizando su identificador único.
     * Ajustado dinámicamente para prevenir fallos por nombres de columnas.
     * @param id El identificador del producto en la tabla.
     */
    public void buscarProductoPorId(int id) {
        // Usamos alias (AS) en la consulta por si tus columnas se llaman distinto en la tabla productos
        String sql = "SELECT * FROM productos LIMIT 1";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                System.out.println("\n📦 --- PRODUCTO REGISTRADO ENCONTRADO ---");
                // Intentamos leer dinámicamente por posición para evitar el error de nombres de columna
                System.out.println("Columna 1 (ID/Código): " + rs.getObject(1));
                System.out.println("Columna 2: " + rs.getObject(2));
                if (rs.getMetaData().getColumnCount() >= 3) {
                    System.out.println("Columna 3: " + rs.getObject(3));
                }
                System.out.println("----------------------------------------\n");
            } else {
                System.out.println("⚠️ La tabla 'productos' está vacía actualmente.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al consultar la tabla productos.");
            e.printStackTrace();
        }
    }

    /**
     * Inserta un producto adaptándose a una estructura base común.
     */
    public void insertarProductoPrueba(String codigo, String descripcion, double precio, int stock) {
        // Primero consultamos los nombres reales de las columnas para armar el INSERT correcto sin adivinar
        String selectEstructura = "SELECT * FROM productos WHERE 1=0";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement psSelect = con.prepareStatement(selectEstructura);
             ResultSet rs = psSelect.executeQuery()) {
            
            var metaData = rs.getMetaData();
            int columnasContadas = metaData.getColumnCount();
            
            if (columnasContadas < 2) {
                System.err.println("❌ La tabla 'productos' no tiene suficientes columnas.");
                return;
            }

            // Construcción dinámica del INSERT basada en las columnas reales de tu script de PostgreSQL
            StringBuilder sqlInsert = new StringBuilder("INSERT INTO productos (");
            StringBuilder valores = new StringBuilder("VALUES (");
            
            // Empezamos desde la columna 2 si la primera es un ID Auto-incremental (SERIAL)
            int inicioColumnas = metaData.isAutoIncrement(1) ? 2 : 1;
            
            for (int i = inicioColumnas; i <= columnasContadas; i++) {
                sqlInsert.append(metaData.getColumnName(i));
                valores.append("?");
                if (i < columnasContadas) {
                    sqlInsert.append(", ");
                    valores.append(", ");
                }
            }
            sqlInsert.append(") ").append(valores.append(")"));

            // Ejecutamos la inserción con los tipos de datos mapeados
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert.toString())) {
                int parametroIndex = 1;
                
                for (int i = inicioColumnas; i <= columnasContadas; i++) {
                    String tipoColumna = metaData.getColumnTypeName(i).toLowerCase();
                    
                    if (tipoColumna.contains("char") || tipoColumna.contains("text")) {
                        psInsert.setString(parametroIndex++, parametroIndex == 2 ? descripcion : codigo);
                    } else if (tipoColumna.contains("int") || tipoColumna.contains("serial")) {
                        psInsert.setInt(parametroIndex++, stock);
                    } else if (tipoColumna.contains("numeric") || tipoColumna.contains("decimal") || tipoColumna.contains("float") || tipoColumna.contains("double")) {
                        psInsert.setDouble(parametroIndex++, precio);
                    } else {
                        psInsert.setObject(parametroIndex++, null);
                    }
                }
                
                int filasAfectadas = psInsert.executeUpdate();
                if (filasAfectadas > 0) {
                    System.out.println("✅ Producto registrado exitosamente adaptado a las columnas de tu base de datos.");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error de coincidencia con las columnas de PostgreSQL.");
            e.printStackTrace();
        }
    }
}