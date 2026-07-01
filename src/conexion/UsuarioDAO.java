package conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase DAO encargada de gestionar las consultas y autenticaciones de usuarios.
 */
public class UsuarioDAO {

    /**
     * Busca un usuario activo por su nombre de usuario (username).
     * 
     * @param username El alias de acceso del usuario.[cite: 1]
     * @return Objeto Usuario o null si no se encuentra o está inactivo.
     */
    public Usuario buscarUsuarioPorUsername(String username) {
        String sql = "SELECT u.id_usuario, u.username, u.password_hash, u.nombre_completo, r.nombre_rol " +
                     "FROM usuarios u " +
                     "INNER JOIN roles r ON u.id_rol = r.id_rol " +
                     "WHERE u.username = ? AND u.activo = TRUE";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("nombre_completo"),
                        rs.getString("nombre_rol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error al buscar el usuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Clase POJO/Modelo interna para representar la entidad Usuario con su Rol.
     */
    public static class Usuario {
        private final int idUsuario;
        private final String username;
        private final String passwordHash;
        private final String nombreCompleto;
        private final String nombreRol;

        public Usuario(int idUsuario, String username, String passwordHash, String nombreCompleto, String nombreRol) {
            this.idUsuario = idUsuario;
            this.username = username;
            this.passwordHash = passwordHash;
            this.nombreCompleto = nombreCompleto;
            this.nombreRol = nombreRol;
        }

        public int getIdUsuario() { return idUsuario; }
        public String getUsername() { return username; }
        public String getPasswordHash() { return passwordHash; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getNombreRol() { return nombreRol; }
    }
}