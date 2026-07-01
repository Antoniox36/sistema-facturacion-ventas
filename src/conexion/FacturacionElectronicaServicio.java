package conexion;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FacturacionElectronicaServicio {

    private static final String BASE_URL = "https://api.facturaelectronicasv.com/api/v1/public";
    private static final String API_KEY = "YOUR_API_KEY"; // Reemplazar por tu credencial real de Hacienda

    private final HttpClient httpClient;

    public FacturacionElectronicaServicio() {
        // Inicializar un cliente HTTP moderno con soporte para HTTP/2 y tiempos de espera
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Coordina todo el flujo asíncrono de emisión exigido por el Ministerio de Hacienda
     */
    public void emitirFacturaConsumidorFinal() {
        try {
            System.out.println("🚀 1) Creando el DTE (Factura Consumidor Final) en el Ministerio de Hacienda...");

            // Definición del Payload exacto usando Bloques de Texto de Java
            String payload = """
            {
              "pointOfSaleId": "0a9d2b1c-3e4f-4a5b-8c6d-7e8f9a0b1c2d",
              "establishmentId": "1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e",
              "sendEmail": true,
              "dte": {
                "environment": "01",
                "recipient": { "customerId": "f47ac10b-58cc-4372-a567-0e02b2c3d479" },
                "items": [
                  {
                    "productId": 1024,
                    "itemType": 1,
                    "quantity": 2,
                    "documentNumber": null,
                    "internalCode": null,
                    "taxCode": null,
                    "discountAmount": 0,
                    "nonTaxableSales": 0,
                    "exemptSales": 0,
                    "taxableSales": 1799.98,
                    "taxes": ["20"],
                    "suggestedSalePrice": 0,
                    "taxFree": 0,
                    "itemVat": 207.06
                  }
                ],
                "summary": {
                  "totalNonTaxableSales": 0,
                  "totalExemptSales": 0,
                  "totalTaxableSales": 1799.98,
                  "subTotalSales": 1799.98,
                  "discountNonTaxableSales": 0,
                  "discountExemptSales": 0,
                  "discountTaxableSales": 0,
                  "discountPercentage": 0,
                  "totalDiscount": 0,
                  "taxes": [{ "code": "20", "description": "IVA 13%", "value": 207.06 }],
                  "subTotal": 1799.98,
                  "totalVat": 207.06,
                  "vatWithholding1": 0,
                  "incomeTaxWithholding": 0,
                  "totalOperationAmount": 1799.98,
                  "nonTaxableTotal": 0,
                  "totalToPay": 1799.98,
                  "totalInLetters": "MIL SETECIENTOS NOVENTA Y NUEVE 98/100",
                  "balanceInFavor": 0,
                  "operationCondition": 1,
                  "payments": null,
                  "electronicPaymentNumber": null,
                  "observations": null
                }
              }
            }
            """;

            // Construir petición POST para crear el documento
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/dtes/fc"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> responsePost = httpClient.send(requestPost, HttpResponse.BodyHandlers.ofString());

            // Validar que la petición inicial haya sido aceptada
            if (responsePost.statusCode() != 200 && responsePost.statusCode() != 21) {
                String errorMsg = extraerValorJson(responsePost.body(), "message");
                throw new RuntimeException("Error devuelto por la API del MH: " + errorMsg);
            }

            // Extraer ID y estado inicial de la respuesta de Hacienda
            String dteId = extraerValorJson(responsePost.body(), "id");
            String status = extraerValorJson(responsePost.body(), "status");

            System.out.println("✅ DTE registrado. ID generado: " + dteId + " | Estado actual: " + status);

            // 2) Consultar de forma asíncrona (Polling) hasta que el MH termine la firma y transmisión
            while ("PENDING".equals(status) || "SIGNED".equals(status)) {
                System.out.println("⏳ Transmisión en proceso... Esperando 3 segundos.");
                Thread.sleep(3000); // Pausa el hilo de ejecución de forma segura

                HttpRequest requestGet = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/dtes/" + dteId))
                        .header("Authorization", "Bearer " + API_KEY)
                        .GET()
                        .build();

                HttpResponse<String> responseGet = httpClient.send(requestGet, HttpResponse.BodyHandlers.ofString());
                status = extraerValorJson(responseGet.body(), "status");
            }

            System.out.println("🏁 Firma y transmisión finalizada. Estado final en Hacienda: " + status);

            // 3) Descargar el PDF oficial firmado
            System.out.println("📥 Generando y descargando la representación gráfica (PDF)...");
            HttpRequest requestPdf = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/dtes/" + dteId + "/pdf"))
                    .header("Authorization", "Bearer " + API_KEY)
                    .GET()
                    .build();

            // Descargar la respuesta HTTP directamente como un arreglo de bytes (Binary Blob)
            HttpResponse<byte[]> responsePdf = httpClient.send(requestPdf, HttpResponse.BodyHandlers.ofByteArray());

            if (responsePdf.statusCode() == 200) {
                String rutaHome = System.getProperty("user.home");
                String rutaDescarga = Paths.get(rutaHome, "Downloads", "factura-" + dteId + ".pdf").toString();

                try (FileOutputStream fos = new FileOutputStream(rutaDescarga)) {
                    fos.write(responsePdf.body());
                }

                System.out.println("💾 ¡Documento guardado con éxito!\n👉 Ubicación: " + rutaDescarga);
            } else {
                System.out.println("⚠️ No se pudo procesar el PDF. Código de estado HTTP: " + responsePdf.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Ocurrió un error de comunicación con el servidor: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("❌ Error en el flujo de facturación electrónica: " + e.getMessage());
        }
    }

    /**
     * Utilidad ligera mediante Expresiones Regulares para extraer valores específicos del JSON
     * sin necesidad de acoplar librerías de terceros (Jackson/Gson) en esta etapa.
     */
    private String extraerValorJson(String json, String llave) {
        Pattern pattern = Pattern.compile("\"" + llave + "\":\\s*\"?([^,\"}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}