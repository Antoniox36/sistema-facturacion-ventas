package conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase DAO encargada de las operaciones CRUD para la tabla de clientes.
 * Soporta la identificación de Consumidor Final y Contribuyentes locales.
 */
public class ClienteDAO {

    /**
     * Inserta un nuevo cliente en la base de datos.
     * 
     * @param nombre Nombre completo o Razón Social.
     * @param documentoIdentidad DUI o NIT para la facturación.
     * @param registroContribuyente NRC si aplica (Crédito Fiscal).[cite: 1]
     * @param direccion Dirección física.[cite: 1]
     * @param telefono Teléfono de contacto.[cite: 1]
     * @return true si se registró con éxito, false en caso contrario.
     */
    public boolean registrarCliente(String nombre, String documentoIdentidad, 
                                    String registroContribuyente, String direccion, String telefono) {
        String sql = "INSERT INTO clientes (nombre, documento_identidad, registro_contribuyente, direccion, telefono) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nombre);
            ps.setString(2, documentoIdentidad);
            // Manejo de nulos por si es consumidor final sin NRC o teléfono
            ps.setString(3, registroContribuyente != null ? registroContribuyente : null);
            ps.setString(4, direccion != null ? direccion : null);
            ps.setString(5, telefono != null ? telefono : null);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error al registrar el cliente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca un cliente en la base de datos mediante su DUI o NIT.
     * 
     * @param documento Documento de identidad a buscar.[cite: 1]
     * @return Objeto Cliente o null si no se encuentra.
     */
    public Cliente buscarPorDocumento(String documento) {
        String sql = "SELECT id_cliente, nombre, documento_identidad, registro_contribuyente, direccion, telefono FROM clientes WHERE documento_identidad = ?";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("documento_identidad"),
                        rs.getString("registro_contribuyente"),
                        rs.getString("direccion"),
                        rs.getString("telefono")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar el cliente: " + e.getMessage());
        }
        return null;
    }

    /**
     * Clase POJO/Modelo interna para representar la entidad Cliente.
     */
    public static class Cliente {
        private final int idCliente;
        private final String nombre;
        private final String documentoIdentidad;
        private final String registroContribuyente;
        private final String direccion;
        private final String telefono;

        public Cliente(int idCliente, String nombre, String documentoIdentidad, String registroContribuyente, String direccion, String telefono) {
            this.idCliente = idCliente;
            this.nombre = nombre;
            this.documentoIdentidad = documentoIdentidad;
            this.registroContribuyente = registroContribuyente;
            this.direccion = direccion;
            this.telefono = telefono;
        }

        public int getIdCliente() { return idCliente; }
        public String getNombre() { return nombre; }
        public String getDocumentoIdentidad() { return documentoIdentidad; }
        public String getRegistroContribuyente() { return registroContribuyente; }
        public String getDirección() { return direccion; }
        public String getTelefono() { return telefono; }
    }
}