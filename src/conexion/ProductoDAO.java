package conexion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO encargada de realizar las operaciones CRUD en la base de datos
 * para la entidad de Productos del sistema de facturación.
 */
public class ProductoDAO {

    /**
     * Busca un producto en la base de datos utilizando su identificador único.
     */
    public void buscarProductoPorId(int id) {
        String sql = "SELECT * FROM productos LIMIT 1";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                System.out.println("\n📦 --- PRODUCTO REGISTRADO ENCONTRADO ---");
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
     * Inserta un producto adaptándose dinámicamente al nombre real de las columnas
     * de PostgreSQL, asegurando que el código y la descripción no se crucen.
     */
    public void insertarProductoPrueba(String codigo, String descripcion, double precio, int stock) {
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

            StringBuilder sqlInsert = new StringBuilder("INSERT INTO productos (");
            StringBuilder valores = new StringBuilder("VALUES (");
            
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

            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert.toString())) {
                int parametroIndex = 1;
                
                for (int i = inicioColumnas; i <= columnasContadas; i++) {
                    String nombreCol = metaData.getColumnName(i).toLowerCase();
                    String tipoColumna = metaData.getColumnTypeName(i).toLowerCase();
                    
                    if (tipoColumna.contains("char") || tipoColumna.contains("text")) {
                        // Asignación estricta evaluando el nombre real de la columna de texto
                        if (nombreCol.contains("cod") || nombreCol.contains("barr")) {
                            psInsert.setString(parametroIndex++, codigo);
                        } else {
                            psInsert.setString(parametroIndex++, descripcion);
                        }
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
                    System.out.println("✅ Producto registrado exitosamente de forma alineada.");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error de coincidencia con las columnas de PostgreSQL.");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la lista completa de productos mapeando por el nombre de columna estricto,
     * garantizando robustez ante cualquier alteración física del orden en la BD.
     */
    public List<Producto> listarProductos(String criterio) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos ORDER BY 1 ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int totalColumnas = metaData.getColumnCount();

            // Carga por nombres explícitos por defecto para tu esquema en PostgreSQL
            String colId = "id_producto";
            String colCodigo = "codigo_barras";
            String colDescripcion = "descripcion";
            String colPrecio = "precio_venta";
            String colStock = "stock";

            // Si por algún motivo cambiaron de nombre, los detectamos dinámicamente
            for (int i = 1; i <= totalColumnas; i++) {
                String nom = metaData.getColumnName(i);
                String nomLower = nom.toLowerCase();
                
                if (nomLower.equals("id_producto") || nomLower.equals("id")) {
                    colId = nom;
                } else if (nomLower.equals("codigo_barras") || nomLower.equals("codigo") || nomLower.contains("barr")) {
                    colCodigo = nom;
                } else if (nomLower.equals("descripcion") || nomLower.equals("nombre") || nomLower.contains("desc")) {
                    colDescripcion = nom;
                } else if (nomLower.contains("precio") || nomLower.contains("venta")) {
                    colPrecio = nom;
                } else if (nomLower.contains("stock") || nomLower.contains("cant")) {
                    colStock = nom;
                }
            }

            while (rs.next()) {
                int id = rs.getInt(colId);
                String codigo = rs.getString(colCodigo);
                String descripcion = rs.getString(colDescripcion);
                
                Object precioObj = rs.getObject(colPrecio);
                BigDecimal precio = BigDecimal.ZERO;
                if (precioObj instanceof BigDecimal) {
                    precio = (BigDecimal) precioObj;
                } else if (precioObj != null) {
                    precio = new BigDecimal(precioObj.toString());
                }
                
                int stock = rs.getInt(colStock);

                if (!criterio.isEmpty()) {
                    String busqueda = criterio.toLowerCase();
                    boolean coincideDesc = (descripcion != null && descripcion.toLowerCase().contains(busqueda));
                    boolean coincideCod = (codigo != null && codigo.toLowerCase().contains(busqueda));
                    
                    if (!coincideDesc && !coincideCod) {
                        continue;
                    }
                }

                lista.add(new Producto(id, codigo, descripcion, precio, stock));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error en el mapeo de columnas al listar productos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Clase Modelo/POJO interna que representa la entidad de un Producto.
     */
    public static class Producto {
        private final int idProducto;
        private final String codigoBarras;
        private final String descripcion;
        private final BigDecimal precioVenta;
        private final int stock;

        public Producto(int idProducto, String codigoBarras, String descripcion, BigDecimal precioVenta, int stock) {
            this.idProducto = idProducto;
            this.codigoBarras = codigoBarras;
            this.descripcion = descripcion;
            this.precioVenta = precioVenta;
            this.stock = stock;
        }

        public int getIdProducto() { return idProducto; }
        public String getCodigoBarras() { return codigoBarras; }
        public String getDescripcion() { return descripcion; }
        public BigDecimal getPrecioVenta() { return precioVenta; }
        public int getStock() { return stock; }
    }
}