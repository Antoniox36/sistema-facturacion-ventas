package vista;

import conexion.ProductoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Panel autónomo para la gestión de Nueva Venta.
 * Interfaz corregida con contraste de texto en los botones inferiores.
 */
public class PanelNuevaVenta extends JPanel {

    // Componentes de la interfaz
    private JTextField txtBuscarProducto;
    private JTextField txtDocCliente;
    private JTextField txtNombreCliente;
    private JComboBox<String> cbTipoPrecio;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;

    // Etiquetas de Totales
    private JLabel lblSubtotal;
    private JLabel lblIva;
    private JLabel lblTotal;

    private JButton btnGenerarVenta;
    private JButton btnCancelarVenta;

    // Instancia del DAO para conectar con PostgreSQL
    private final ProductoDAO productoDAO;

    public PanelNuevaVenta() {
        this.productoDAO = new ProductoDAO();
        initComponents();
        initEvents(); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // ==========================================
        // 1. PANEL SUPERIOR: BÚSQUEDA DE PRODUCTOS
        // ==========================================
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Búsqueda Interactiva"));

        txtBuscarProducto = new JTextField(30);
        txtBuscarProducto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JButton btnAgregar = new JButton("Buscar y Agregar");
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelBusqueda.add(new JLabel("Código o Descripción:"));
        panelBusqueda.add(txtBuscarProducto);
        panelBusqueda.add(btnAgregar);

        // ==========================================
        // 2. PANEL LATERAL DERECHO: CLIENTE Y CONFIGURACIÓN
        // ==========================================
        JPanel panelLateralDerecho = new JPanel();
        panelLateralDerecho.setLayout(new BoxLayout(panelLateralDerecho, BoxLayout.Y_AXIS));
        panelLateralDerecho.setPreferredSize(new Dimension(280, 0));
        panelLateralDerecho.setBackground(new Color(245, 245, 245));
        panelLateralDerecho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                BorderFactory.createEmptyBorder(15, 12, 15, 12)
        ));

        // --- SECCIÓN: DATOS DEL COMPROBANTE ---
        JLabel lblTituloCliente = new JLabel("Datos del Comprobante");
        lblTituloCliente.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloCliente.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(lblTituloCliente);
        panelLateralDerecho.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblDui = new JLabel("DUI / NIT Cliente:");
        lblDui.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(lblDui);
        
        txtDocCliente = new JTextField();
        txtDocCliente.setMaximumSize(new Dimension(260, 30));
        txtDocCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDocCliente.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(txtDocCliente);
        panelLateralDerecho.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel lblNombre = new JLabel("Nombre / Razón Social:");
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(lblNombre);

        txtNombreCliente = new JTextField("CONSUMIDOR FINAL");
        txtNombreCliente.setMaximumSize(new Dimension(260, 30));
        txtNombreCliente.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombreCliente.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(txtNombreCliente);
        panelLateralDerecho.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- SECCIÓN: LISTA DE PRECIOS ---
        JLabel lblTituloPrecio = new JLabel("Lista de Precios (Margen)");
        lblTituloPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(lblTituloPrecio);
        panelLateralDerecho.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] tiposPrecios = {
            "Precio 1 (10%)",
            "Precio 2 (15%)",
            "Precio 3 (25%)",
            "Precio 4 (35%)"
        };
        cbTipoPrecio = new JComboBox<>(tiposPrecios);
        cbTipoPrecio.setSelectedIndex(0); 
        cbTipoPrecio.setMaximumSize(new Dimension(260, 35));
        cbTipoPrecio.setPreferredSize(new Dimension(260, 35));
        cbTipoPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTipoPrecio.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelLateralDerecho.add(cbTipoPrecio);

        panelLateralDerecho.add(Box.createVerticalGlue()); 

        // ==========================================
        // PANEL DE TOTALES (RESUMEN MONETARIO)
        // ==========================================
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 5, 5));
        panelTotales.setBackground(new Color(230, 242, 255));
        panelTotales.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 245), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panelTotales.setMaximumSize(new Dimension(260, 100));
        panelTotales.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelTotales.add(new JLabel("Subtotal Neto:"));
        lblSubtotal = new JLabel("$0.00", SwingConstants.RIGHT);
        lblSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelTotales.add(lblSubtotal);

        panelTotales.add(new JLabel("IVA (13%):"));
        lblIva = new JLabel("$0.00", SwingConstants.RIGHT);
        lblIva.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelTotales.add(lblIva);

        JLabel lblT = new JLabel("TOTAL BRUTO:");
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelTotales.add(lblT);
        lblTotal = new JLabel("$0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(25, 118, 210));
        panelTotales.add(lblTotal);

        panelLateralDerecho.add(panelTotales);
        panelLateralDerecho.add(Box.createRigidArea(new Dimension(0, 15)));

        // ==========================================
        // CORRECCIÓN TOTAL DE CONTRASTE EN BOTONES
        // ==========================================
        
        // Botón Generar Venta
        btnGenerarVenta = new JButton("Generar Venta");
        btnGenerarVenta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGenerarVenta.setBackground(new Color(25, 118, 210)); // Azul oscuro
        btnGenerarVenta.setForeground(Color.WHITE);               // CORRECCIÓN: Letra blanca legible
        btnGenerarVenta.setContentAreaFilled(true);
        btnGenerarVenta.setBorderPainted(false);
        btnGenerarVenta.setFocusPainted(false);
        btnGenerarVenta.setMaximumSize(new Dimension(260, 40));
        btnGenerarVenta.setPreferredSize(new Dimension(260, 40));
        btnGenerarVenta.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGenerarVenta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Botón Cancelar Venta
        btnCancelarVenta = new JButton("Cancelar Operación");
        btnCancelarVenta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelarVenta.setBackground(new Color(66, 66, 66));   // Gris oscuro
        btnCancelarVenta.setForeground(Color.WHITE);               // CORRECCIÓN: Letra blanca legible
        btnCancelarVenta.setContentAreaFilled(true);
        btnCancelarVenta.setBorderPainted(false);
        btnCancelarVenta.setFocusPainted(false);
        btnCancelarVenta.setMaximumSize(new Dimension(260, 35));
        btnCancelarVenta.setPreferredSize(new Dimension(260, 35));
        btnCancelarVenta.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCancelarVenta.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelLateralDerecho.add(btnGenerarVenta);
        panelLateralDerecho.add(Box.createRigidArea(new Dimension(0, 8)));
        panelLateralDerecho.add(btnCancelarVenta);

        // ==========================================
        // 3. PANEL CENTRAL: TABLA DE ITEMS SELECCIONADOS
        // ==========================================
        String[] columnas = {"ID", "Descripción del Artículo", "Precio Venta", "Cantidad", "Desglose IVA (13%)", "Importe Bruto"};
        modeloCarrito = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; 
            }
        };

        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaCarrito.setRowHeight(24);
        tablaCarrito.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JScrollPane scrollTabla = new JScrollPane(tablaCarrito);

        // Ensamblado Estructural del Módulo
        add(panelBusqueda, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);
        add(panelLateralDerecho, BorderLayout.EAST);
        
        btnAgregar.addActionListener(e -> buscarYAgregarProducto());
    }

    private void initEvents() {
        modeloCarrito.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                if (e.getColumn() == 3) { 
                    recalcularTotales();
                }
            }
        });

        cbTipoPrecio.addActionListener(e -> recalcularTotales());
    }

    private void buscarYAgregarProducto() {
        String criterio = txtBuscarProducto.getText().trim();
        if (criterio.isEmpty()) return;

        List<ProductoDAO.Producto> encontrados = productoDAO.listarProductos(criterio);

        if (!encontrados.isEmpty()) {
            ProductoDAO.Producto p = encontrados.get(0); 

            for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                int idTabla = (int) modeloCarrito.getValueAt(i, 0);
                if (idTabla == p.getIdProducto()) {
                    int cantActual = Integer.parseInt(modeloCarrito.getValueAt(i, 3).toString());
                    modeloCarrito.setValueAt(cantActual + 1, i, 3); 
                    txtBuscarProducto.setText("");
                    return;
                }
            }

            Object[] nuevaFila = {
                p.getIdProducto(),
                p.getDescripcion(),
                "$0.00", 
                1,       
                "$0.00", 
                "$0.00"  
            };
            modeloCarrito.addRow(nuevaFila);
            txtBuscarProducto.setText("");
            recalcularTotales(); 
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró ningún artículo con ese criterio.", "Artículo no registrado", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void recalcularTotales() {
        BigDecimal totalFinal = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        BigDecimal totalSubtotalNeto = BigDecimal.ZERO;

        int seleccion = cbTipoPrecio.getSelectedIndex();
        BigDecimal margenUtilidad = switch (seleccion) {
            case 1 -> new BigDecimal("0.15");
            case 2 -> new BigDecimal("0.25");
            case 3 -> new BigDecimal("0.35");
            default -> new BigDecimal("0.10"); 
        };

        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            int idProducto = (int) modeloCarrito.getValueAt(i, 0);
            int cantidad = Integer.parseInt(modeloCarrito.getValueAt(i, 3).toString());

            List<ProductoDAO.Producto> prods = productoDAO.listarProductos(String.valueOf(idProducto));
            if (!prods.isEmpty()) {
                BigDecimal precioBase = prods.get(0).getPrecioVenta(); 
                
                BigDecimal ganancia = precioBase.multiply(margenUtilidad);
                BigDecimal precioUnitarioFinal = precioBase.add(ganancia).setScale(2, RoundingMode.HALF_UP);

                BigDecimal importeFilaTotal = precioUnitarioFinal.multiply(new BigDecimal(cantidad));
                
                BigDecimal netoFila = importeFilaTotal.divide(new BigDecimal("1.13"), 4, RoundingMode.HALF_UP);
                BigDecimal ivaFila = importeFilaTotal.subtract(netoFila);

                modeloCarrito.setValueAt("$" + precioUnitarioFinal, i, 2);
                modeloCarrito.setValueAt("$" + ivaFila.setScale(2, RoundingMode.HALF_UP), i, 4);
                modeloCarrito.setValueAt("$" + importeFilaTotal.setScale(2, RoundingMode.HALF_UP), i, 5);

                totalFinal = totalFinal.add(importeFilaTotal);
                totalIva = totalIva.add(ivaFila);
                totalSubtotalNeto = totalSubtotalNeto.add(netoFila);
            }
        }

        lblSubtotal.setText("$" + totalSubtotalNeto.setScale(2, RoundingMode.HALF_UP));
        lblIva.setText("$" + totalIva.setScale(2, RoundingMode.HALF_UP));
        lblTotal.setText("$" + totalFinal.setScale(2, RoundingMode.HALF_UP));
    }
}