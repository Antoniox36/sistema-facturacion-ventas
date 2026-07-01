package vista;

import conexion.UsuarioDAO;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Pantalla Principal del Sistema de Facturación y Ventas (Dashboard).
 * Incorpora un diseño interactivo basado en CardLayout para cambiar paneles centrales.
 */
public class MainView extends JFrame {

    private final UsuarioDAO.Usuario usuarioActivo;
    private JPanel panelCentralCartas; // Contenedor principal con CardLayout
    private CardLayout cardLayout;

    public MainView(UsuarioDAO.Usuario usuario) {
        this.usuarioActivo = usuario;
        initComponentes();
    }

    /**
     * Inicializa y estructura el diseño del espacio de trabajo del sistema.
     */
    private void initComponentes() {
        setTitle("Sistema de Facturación - Panel Principal");
        setSize(1024, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. BARRA SUPERIOR: Información de Sesión Activa ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(25, 118, 210)); // Azul corporativo
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblSistema = new JLabel("PUNTO DE VENTA Y FACTURACIÓN");
        lblSistema.setForeground(Color.WHITE);
        lblSistema.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String infoCajero = "Cajero: " + usuarioActivo.getNombreCompleto() + " [" + usuarioActivo.getNombreRol() + "]";
        JLabel lblUsuario = new JLabel(infoCajero);
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panelSuperior.add(lblSistema, BorderLayout.WEST);
        panelSuperior.add(lblUsuario, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);

        // --- 2. MENÚ LATERAL IZQUIERDO: Accesos Rápidos ---
        JPanel panelLateral = new JPanel(new GridLayout(6, 1, 10, 15));
        panelLateral.setBackground(new Color(245, 245, 245));
        panelLateral.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        panelLateral.setPreferredSize(new Dimension(200, 0));

        JButton btnNuevaVenta = new JButton("Nueva Venta");
        JButton btnHistorial = new JButton("Historial Ventas");
        JButton btnProductos = new JButton("Inventario");
        JButton btnClientes = new JButton("Clientes");
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");

        Font fontBotones = new Font("Segoe UI", Font.BOLD, 13);
        btnNuevaVenta.setFont(fontBotones);
        btnHistorial.setFont(fontBotones);
        btnProductos.setFont(fontBotones);
        btnClientes.setFont(fontBotones);
        btnCerrarSesion.setFont(fontBotones);
        btnCerrarSesion.setForeground(new Color(183, 28, 28));

        panelLateral.add(btnNuevaVenta);
        panelLateral.add(btnHistorial);
        panelLateral.add(btnProductos);
        panelLateral.add(btnClientes);
        panelLateral.add(new JLabel("")); // Espaciador estético
        panelLateral.add(btnCerrarSesion);
        add(panelLateral, BorderLayout.WEST);

        // --- 3. PANEL CENTRAL: CardLayout (Mazo de Pantallas) ---
        cardLayout = new CardLayout();
        panelCentralCartas = new JPanel(cardLayout);

        // Carta 1: Vista de Bienvenida por defecto
        JPanel panelBienvenida = crearPanelMensaje("¡Bienvenido al Panel de Control!", 
                "Selecciona una opción del menú de la izquierda para comenzar a trabajar.");
        
        // Carta 2: Marcador para Nueva Venta
        JPanel panelNuevaVenta = crearPanelMensaje("🛒 Módulo de Nueva Venta", 
                "Espacio de trabajo listo para el carrito de compras e integración de IVA.");
        
        // Carta 3: Marcador para Historial de Ventas
        JPanel panelHistorial = crearPanelMensaje("📊 Historial de Ventas", 
                "Aquí se consultarán los documentos de facturación emitidos desde PostgreSQL.");
        
        // Carta 4: Marcador para Inventario
        JPanel panelInventario = crearPanelMensaje("📦 Control de Inventario", 
                "Lista de productos disponibles vinculados al Trigger de actualización automática.");
        
        // Carta 5: Marcador para Clientes
        JPanel panelClientes = crearPanelMensaje("👥 Registro de Clientes", 
                "Búsqueda y gestión de datos fiscales para Consumidor Final o Crédito Fiscal.");

        // Añadimos todas las "cartas" al panel contenedor asignándoles un identificador único de texto
        panelCentralCartas.add(panelBienvenida, "BIENVENIDA");
        panelCentralCartas.add(panelNuevaVenta, "VENTA");
        panelCentralCartas.add(panelHistorial, "HISTORIAL");
        panelCentralCartas.add(panelInventario, "INVENTARIO");
        panelCentralCartas.add(panelClientes, "CLIENTES");

        add(panelCentralCartas, BorderLayout.CENTER);

        // --- 4. BARRA INFERIOR: Estado del Sistema ---
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(230, 235, 240));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel lblEstadoBD = new JLabel("● Conexión Establecida con PostgreSQL (db_facturacion_ventas)");
        lblEstadoBD.setForeground(new Color(46, 125, 50));
        lblEstadoBD.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        JLabel lblFecha = new JLabel("Fecha de Sesión: " + fechaActual);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFecha.setForeground(Color.DARK_GRAY);

        panelInferior.add(lblEstadoBD, BorderLayout.WEST);
        panelInferior.add(lblFecha, BorderLayout.EAST);
        add(panelInferior, BorderLayout.SOUTH);

        // --- INTERCONEXIÓN DE ACCIONES Y EVENTOS ---
        btnNuevaVenta.addActionListener(e -> cardLayout.show(panelCentralCartas, "VENTA"));
        btnHistorial.addActionListener(e -> cardLayout.show(panelCentralCartas, "HISTORIAL"));
        btnProductos.addActionListener(e -> cardLayout.show(panelCentralCartas, "INVENTARIO"));
        btnClientes.addActionListener(e -> cardLayout.show(panelCentralCartas, "CLIENTES"));

        btnCerrarSesion.addActionListener(e -> {
            int respuesta = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro que desea cerrar la sesión actual?", 
                "Cerrar Sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if (respuesta == JOptionPane.YES_OPTION) {
                this.dispose();
                SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
            }
        });
    }

    /**
     * Método utilitario para construir paneles informativos genéricos de forma rápida.
     */
    private JPanel crearPanelMensaje(String titulo, String subtitulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(33, 33, 33));

        JLabel lblSubtitulo = new JLabel(subtitulo, SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(Color.GRAY);

        panel.add(lblTitulo, BorderLayout.CENTER);
        panel.add(lblSubtitulo, BorderLayout.SOUTH);
        return panel;
    }
}