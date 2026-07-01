package conexion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Servicio encargada de centralizar la lógica de negocio 
 * de la facturación, procesando cálculos monetarios e impuestos (13% IVA)
 * antes de la persistencia.
 */
public class FacturacionService {

    private final FacturaDAO facturaDAO;
    // Constante para el cálculo del 13% de IVA
    private static final BigDecimal TASA_IVA = new BigDecimal("0.13");

    public FacturacionService() {
        this.facturaDAO = new FacturaDAO();
    }

    /**
     * DTO interno para estructurar la solicitud de un producto desde la vista.
     */
    public static class ItemCarritoDTO {
        private final int idProducto;
        private final int cantidad;
        private final BigDecimal precioVenta; // Este precio ya incluye el IVA en góndola

        public ItemCarritoDTO(int idProducto, int cantidad, BigDecimal precioVenta) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
            this.precioVenta = precioVenta;
        }

        public int getIdProducto() { return idProducto; }
        public int getCantidad() { return cantidad; }
        public BigDecimal getPrecioVenta() { return precioVenta; }
    }

    /**
     * Procesa una venta, desglosa el 13% de IVA y ejecuta la persistencia transaccional.
     * 
     * @param numeroDocumento Correlativo o código único de facturación.
     * @param idCliente ID del cliente receptor.
     * @param idUsuario ID del cajero encargado.
     * @param tipoPago Método de pago.
     * @param items Lista de productos agregados en el carrito de compras.
     * @return true si la operación lógica y transaccional fue exitosa.
     */
    public boolean procesarNuevaVenta(String numeroDocumento, int idCliente, int idUsuario, 
                                      String tipoPago, List<ItemCarritoDTO> items) {
        
        if (items == null || items.isEmpty()) {
            System.err.println("⚠️ Error de negocio: No se puede procesar una venta sin ítems.");
            return false;
        }

        BigDecimal totalFactura = BigDecimal.ZERO;
        List<FacturaDAO.DetalleVentaDTO> detallesParaDAO = new ArrayList<>();

        System.out.println("🧮 Calculando importes financieros e impuestos (13% IVA)...");
        
        for (ItemCarritoDTO item : items) {
            BigDecimal cantidadBD = new BigDecimal(item.getCantidad());
            // Subtotal del ítem = cantidad * precio_venta
            BigDecimal subtotalItem = item.getPrecioVenta().multiply(cantidadBD);
            
            totalFactura = totalFactura.add(subtotalItem);

            System.out.printf("   -> Producto ID [%d]: %d u. x $%s = Subtotal: $%s%n", 
                    item.getIdProducto(), item.getCantidad(), item.getPrecioVenta(), subtotalItem);

            detallesParaDAO.add(new FacturaDAO.DetalleVentaDTO(
                item.getIdProducto(), 
                item.getCantidad(), 
                item.getPrecioVenta()
            ));
        }

        // --- DESGLOSE DE IMPUESTOS LOCALES ---
        // 1. Extraemos la base gravada: Gravado = Total / 1.13
        BigDecimal divisorIVA = new BigDecimal("1.13");
        BigDecimal montoGravado = totalFactura.divide(divisorIVA, 2, RoundingMode.HALF_UP);
        
        // 2. Calculamos el IVA usando la constante TASA_IVA para remover el warning: IVA = Gravado * 0.13
        BigDecimal montoIVA = montoGravado.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);

        System.out.println("\n==============================================");
        System.out.println("🧾 DESGLOSE DE COMPROBANTE DE VENTA");
        System.out.println("   Monto Gravado (Neto):   $" + montoGravado);
        System.out.println("   Impuesto (13% IVA):     $" + montoIVA);
        System.out.println("   Total a Pagar (Bruto):  $" + totalFactura);
        System.out.println("==============================================");

        // Pasamos el control a la capa de persistencia transaccional con el total calculado
        return facturaDAO.registrarVentaCompleta(numeroDocumento, idCliente, idUsuario, tipoPago, totalFactura, detallesParaDAO);
    }
}