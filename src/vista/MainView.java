package vista;

import conexion.ProductoDAO;
import conexion.UsuarioDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pantalla Principal del Sistema de Facturación y Ventas (Dashboard).
 * Incorpora la renderización de la tabla de inventario en tiempo real con PostgreSQL.
 */
public class MainView extends JFrame {

    private final UsuarioDAO.Usuario usuarioActivo;
    private final ProductoDAO productoDAO;
    
    private JPanel panelCentralCartas; 
    private CardLayout cardLayout;
    
    // Componentes del módulo de Inventario
    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscarProducto;

    public MainView(UsuarioDAO.Usuario usuario) {
        this.usuarioActivo = usuario;
        this.productoDAO = new ProductoDAO(); // Instanciamos el DAO de productos
        initComponentes();
        cargarDatosInventario(""); // Carga inicial de datos al levantar el sistema
    }

    private void initComponentes() {
        setTitle("Sistema de Facturación - Panel Principal");
        setSize(1024, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- 1. BARRA SUPERIOR: Información de Sesión ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(25, 118, 210)); 
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

        // --- 2. MENÚ LATERAL IZQUIERDO ---
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
        panelLateral.add(new JLabel("")); 
        panelLateral.add(btnCerrarSesion);
        add(panelLateral, BorderLayout.WEST);

        // --- 3. PANEL CENTRAL: CardLayout ---
        cardLayout = new CardLayout();
        panelCentralCartas = new JPanel(cardLayout);

        // Instanciación de los paneles estáticos y dinámicos
        JPanel panelBienvenida = crearPanelMensaje("¡Bienvenido al Panel de Control!", 
                "Selecciona una opción del menú de la izquierda para comenzar a trabajar.");
        
        JPanel panelNuevaVenta = crearPanelMensaje("🛒 Módulo de Nueva Venta", 
                "Espacio de trabajo listo para el carrito de compras e integración de IVA.");
        
        JPanel panelHistorial = crearPanelMensaje("📊 Historial de Ventas", 
                "Aquí se consultarán los documentos de facturación emitidos desde PostgreSQL.");
        
        // CARTA DINÁMICA: Cargamos el inventario real conectado a la BD
        JPanel panelInventario = crearPanelInventario();
        
        JPanel panelClientes = crearPanelMensaje("👥 Registro de Clientes", 
                "Búsqueda y gestión de datos fiscales para Consumidor Final o Crédito Fiscal.");

        panelCentralCartas.add(panelBienvenida, "BIENVENIDA");
        panelCentralCartas.add(panelNuevaVenta, "VENTA");
        panelCentralCartas.add(panelHistorial, "HISTORIAL");
        panelCentralCartas.add(panelInventario, "INVENTARIO");
        panelCentralCartas.add(panelClientes, "CLIENTES");

        add(panelCentralCartas, BorderLayout.CENTER);

        // --- 4. BARRA INFERIOR ---
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

        // --- NAVEGACIÓN ENTRE CARTAS ---
        btnNuevaVenta.addActionListener(e -> cardLayout.show(panelCentralCartas, "VENTA"));
        btnHistorial.addActionListener(e -> cardLayout.show(panelCentralCartas, "HISTORIAL"));
        btnProductos.addActionListener(e -> {
            cargarDatosInventario(""); // Refrescar la tabla al entrar a la pestaña
            cardLayout.show(panelCentralCartas, "INVENTARIO");
        });
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
     * Construye de manera limpia la interfaz del módulo de Inventario.
     */
    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Sub-panel Superior: Filtros de Búsqueda ---
        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 0));
        panelBusqueda.setBackground(Color.WHITE);

        JLabel lblBuscar = new JLabel("Buscar Producto:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtBuscarProducto = new JTextField();
        txtBuscarProducto.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnBuscar = new JButton("Buscar / Filtrar");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelBusqueda.add(lblBuscar, BorderLayout.WEST);
        panelBusqueda.add(txtBuscarProducto, BorderLayout.CENTER);
        panelBusqueda.add(btnBuscar, BorderLayout.EAST);
        panel.add(panelBusqueda, BorderLayout.NORTH);

        // --- Sub-panel Central: Tabla de Datos de PostgreSQL ---
        String[] columnas = {"ID", "Código de Barras", "Descripción", "Precio de Venta", "Stock Existente"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Deshabilitar edición directa de celdas por seguridad
            }
        };

        tablaInventario = new JTable(modeloTabla);
        tablaInventario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaInventario.setRowHeight(24);
        tablaInventario.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollTabla = new JScrollPane(tablaInventario);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // --- EVENTOS DEL BUSCADOR ---
        btnBuscar.addActionListener(e -> cargarDatosInventario(txtBuscarProducto.getText().trim()));
        txtBuscarProducto.addActionListener(e -> cargarDatosInventario(txtBuscarProducto.getText().trim()));

        return panel;
    }

    /**
     * Consume el ProductoDAO para vaciar y refrescar los registros de la base de datos dentro de la JTable.
     */
    private void cargarDatosInventario(String criterio) {
        if (modeloTabla == null) return;

        // Limpiar filas viejas de la tabla para evitar duplicaciones visuales
        modeloTabla.setRowCount(0);

        System.out.println("🔄 Solicitando actualización de tabla inventario con criterio: '" + criterio + "'");
        List<ProductoDAO.Producto> productos = productoDAO.listarProductos(criterio);

        for (ProductoDAO.Producto p : productos) {
            Object[] fila = {
                p.getIdProducto(),
                p.getCodigoBarras(),
                p.getDescripcion(),
                "$" + p.getPrecioVenta(),
                p.getStock()
            };
            modeloTabla.addRow(fila);
        }
    }

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