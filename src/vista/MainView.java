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

        // --- 1. BARRA SUPERIOR (ENCABEZADO) ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(25, 118, 210));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblLogo = new JLabel("🚀 CONSOLE ERP SYSTEM");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelSuperior.add(lblLogo, BorderLayout.WEST);

        JPanel panelInfoUsuario = new JPanel(new GridLayout(2, 1));
        panelInfoUsuario.setBackground(new Color(25, 118, 210));
        
        JLabel lblUsuario = new JLabel("Cajero: " + usuarioActivo.getNombreCompleto() + " (" + usuarioActivo.getNombreRol() + ")", SwingConstants.RIGHT);
        lblUsuario.setForeground(Color.WHITE);
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        JLabel lblFecha = new JLabel("Ingreso: " + dtf.format(LocalDateTime.now()), SwingConstants.RIGHT);
        lblFecha.setForeground(new Color(224, 224, 224));
        lblFecha.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        panelInfoUsuario.add(lblUsuario);
        panelInfoUsuario.add(lblFecha);
        panelSuperior.add(panelInfoUsuario, BorderLayout.EAST);

        add(panelSuperior, BorderLayout.NORTH);

        // --- 2. BARRA LATERAL IZQUIERDA (MENÚ NAVEGACIÓN COMPLETAMENTE FORZADO) ---
        JPanel panelMenuLateral = new JPanel();
        panelMenuLateral.setLayout(new BoxLayout(panelMenuLateral, BoxLayout.Y_AXIS));
        panelMenuLateral.setBackground(new Color(33, 33, 33)); // Fondo oscuro del contenedor lateral
        panelMenuLateral.setPreferredSize(new Dimension(210, 0));
        panelMenuLateral.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        String[] opcionesMenu = {"Nueva Venta", "Inventario", "Clientes", "Reportes", "Configuración"};
        for (String opcion : opcionesMenu) {
            
            // Usamos una subclase anónima de JButton para anular el pintado nativo del LookAndFeel de Windows
            JButton btnMenu = new JButton(opcion) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Forzamos el fondo oscuro nítido para el botón
                    g2.setColor(new Color(55, 55, 55));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Pintamos el texto encima con máxima calidad y contraste
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(getText(), x, y);
                    
                    g2.dispose();
                }
            };
            
            // Dimensiones uniformes
            btnMenu.setMaximumSize(new Dimension(185, 42));
            btnMenu.setPreferredSize(new Dimension(185, 42));
            btnMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Propiedades de la Fuente (Letra grande, blanca y clara)
            btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnMenu.setForeground(Color.WHITE);
            
            // Desactivar decoraciones nativas que causan transparencia o errores de renderizado
            btnMenu.setOpaque(false);
            btnMenu.setContentAreaFilled(false);
            btnMenu.setBorderPainted(true);
            btnMenu.setFocusPainted(false);
            
            // Borde perimetral fino para darle un acabado elegante
            btnMenu.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
            btnMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Manejo dinámico de las vistas del CardLayout
            btnMenu.addActionListener(e -> {
                String comando = e.getActionCommand();
                if (comando.equals("Inventario")) {
                    cargarDatosInventario(""); // Refrescar stock al entrar
                    cardLayout.show(panelCentralCartas, "Inventario");
                } else if (comando.equals("Nueva Venta")) {
                    cardLayout.show(panelCentralCartas, "NuevaVenta");
                } else {
                    cardLayout.show(panelCentralCartas, comando);
                }
            });

            panelMenuLateral.add(btnMenu);
            panelMenuLateral.add(Box.createRigidArea(new Dimension(0, 12))); // Espacio vertical entre botones
        }

        add(panelMenuLateral, BorderLayout.WEST);

        // --- 3. PANEL CENTRAL CONTENEDOR (CARDLAYOUT MULTIPANTALLA) ---
        cardLayout = new CardLayout();
        panelCentralCartas = new JPanel(cardLayout);

        // Registro de los Paneles Modulares en el CardLayout
        panelCentralCartas.add(crearPanelInventario(), "Inventario");
        panelCentralCartas.add(new PanelNuevaVenta(), "NuevaVenta"); 
        panelCentralCartas.add(crearPanelMensaje("Módulo de Clientes", "Administración de cuentas corrientes y NRC de contribuyentes."), "Clientes");
        panelCentralCartas.add(crearPanelMensaje("Módulo de Reportes", "Auditoría de cierres de caja (X/Z) y libros de IVA correspondientes."), "Reportes");
        panelCentralCartas.add(crearPanelMensaje("Módulo de Configuración", "Parámetros globales del sistema e integración de llaves API Hacienda."), "Configuración");

        add(panelCentralCartas, BorderLayout.CENTER);

        // Mostrar por defecto el módulo de Inventario al levantar el sistema
        cardLayout.show(panelCentralCartas, "Inventario");
    }

    private JPanel crearPanelInventario() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Subpanel Superior: Filtro interactivo de búsqueda
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltro.setBackground(Color.WHITE);
        panelFiltro.add(new JLabel("Filtrar Producto:"));

        txtBuscarProducto = new JTextField(25);
        txtBuscarProducto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Listener para buscar al presionar ENTER en la caja de texto
        txtBuscarProducto.addActionListener(e -> cargarDatosInventario(txtBuscarProducto.getText().trim()));
        
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(25, 118, 210));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.addActionListener(e -> cargarDatosInventario(txtBuscarProducto.getText().trim()));

        panelFiltro.add(txtBuscarProducto);
        panelFiltro.add(btnBuscar);
        panel.add(panelFiltro, BorderLayout.NORTH);

        // Estructura de la Tabla del Inventario
        String[] columnas = {"ID", "Código Barras", "Descripción del Artículo", "Precio Venta (IVA Inc.)", "Stock Físico"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        tablaInventario = new JTable(modeloTabla);
        tablaInventario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaInventario.setRowHeight(22);
        tablaInventario.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaInventario.getTableHeader().setBackground(new Color(238, 238, 238));
        tablaInventario.setGridColor(new Color(224, 224, 224));

        JScrollPane scrollPane = new JScrollPane(tablaInventario);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void cargarDatosInventario(String criterio) {
        if (modeloTabla == null) return;

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