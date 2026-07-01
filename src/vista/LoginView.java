package vista;

import conexion.UsuarioDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Interfaz Gráfica de Usuario (GUI) corregida con contraste de alto rendimiento.
 */
public class LoginView extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private final UsuarioDAO usuarioDAO;

    public LoginView() {
        this.usuarioDAO = new UsuarioDAO();
        initComponentes();
    }

    private void initComponentes() {
        setTitle("Sistema de Facturación - Control de Acceso");
        setSize(420, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- Panel Superior: Encabezado ---
        JPanel panelTitulo = new JPanel();
        panelTitulo.setBackground(new Color(25, 118, 210)); // Azul oscuro empresarial
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel lblTitulo = new JLabel("INICIAR SESIÓN");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panelTitulo.add(lblTitulo);
        add(panelTitulo, BorderLayout.NORTH);

        // --- Panel Central: Formulario con GridBagLayout ---
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 35, 10, 35));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Fila 0: Usuario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelFormulario.add(lblUsuario, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(txtUsuario, gbc);

        // Fila 1: Contraseña
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelFormulario.add(lblContrasena, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtContrasena = new JPasswordField();
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(txtContrasena, gbc);

        add(panelFormulario, BorderLayout.CENTER);

        // --- Panel Inferior: Botones Corregidos para Windows 11 ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        
        // Botón Ingresar: Para evitar el problema de Windows, usamos texto oscuro de alto contraste
        btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setForeground(new Color(27, 94, 32)); // Verde oscuro muy legible
        btnIngresar.setPreferredSize(new Dimension(110, 32));
        btnIngresar.setFocusPainted(false);

        // Botón Cancelar: Texto rojo oscuro de alto contraste
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setForeground(new Color(183, 28, 28)); // Rojo oscuro muy legible
        btnCancelar.setPreferredSize(new Dimension(110, 32));
        btnCancelar.setFocusPainted(false);

        panelBotones.add(btnIngresar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- Control de Eventos ---
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarAutenticacion();
            }
        });

        txtContrasena.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejecutarAutenticacion();
            }
        });
    }

    private void ejecutarAutenticacion() {
        String username = txtUsuario.getText().trim();
        String password = new String(txtContrasena.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, complete todos los campos de acceso.", 
                "Campos Vacíos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("🔐 Intentando conectar al backend para el usuario: " + username);
        UsuarioDAO.Usuario usuario = usuarioDAO.buscarUsuarioPorUsername(username);

        if (usuario != null && password.equals(usuario.getPasswordHash())) { 
            System.out.println("✅ Acceso concedido a: " + usuario.getNombreCompleto());
            
            JOptionPane.showMessageDialog(this, 
                "¡Bienvenido, " + usuario.getNombreCompleto() + "!\nRol: " + usuario.getNombreRol(), 
                "Autenticación Exitosa", 
                JOptionPane.INFORMATION_MESSAGE);
            
            this.dispose(); // Cerramos la ventana de Login de forma limpia
            
            // Instanciamos y desplegamos la pantalla principal pasándole el usuario logueado
            SwingUtilities.invokeLater(() -> {
                new MainView(usuario).setVisible(true);
            });
            
        } else {
            System.err.println("❌ Credenciales inválidas en la interfaz para: " + username);
            JOptionPane.showMessageDialog(this, 
                "Usuario o contraseña incorrectos.\nO la cuenta se encuentra inactiva.", 
                "Error de Autenticación", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}