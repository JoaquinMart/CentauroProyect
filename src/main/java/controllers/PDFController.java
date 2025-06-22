package controllers;

import dao.*;
import model.*;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.application.Platform; // Importación para Platform.runLater

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.awt.Desktop; // ¡NUEVA IMPORTACIÓN! Para abrir archivos

// -- Importaciones de iText 7 --
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import util.CSSFileWatcherPDF;
// -- Fin de Importaciones de iText 7 --

public class PDFController {

    private ClienteDAO clienteDAO;
    private CuentaCorrienteDAO cuentaCorrienteDAO;
    private ProveedorDAO proveedorDAO;

    public PDFController() {
        this.clienteDAO = new ClienteDAO();
        this.cuentaCorrienteDAO = new CuentaCorrienteDAO();
        this.proveedorDAO = new ProveedorDAO();
    }

    public void handleGuardarPdfButton() {
        Stage pdfStage = new Stage();
        pdfStage.setTitle("Generar Reporte de Cuenta Corriente");
        pdfStage.getIcons().add(new Image("file:src/main/resources/icono.jpg"));

        // Campos de entrada
        TextField nombreClienteField = new TextField();
        nombreClienteField.setPromptText("Nombre del Cliente");
        nombreClienteField.setPrefWidth(300);
        nombreClienteField.getStyleClass().add("pdf-input-field");

        DatePicker fechaInicioPicker = new DatePicker();
        fechaInicioPicker.setPromptText("Fecha Inicio");
        fechaInicioPicker.setPrefWidth(300);
        fechaInicioPicker.getStyleClass().add("pdf-date-picker");

        DatePicker fechaFinPicker = new DatePicker();
        fechaFinPicker.setPromptText("Fecha Fin");
        fechaFinPicker.setPrefWidth(300);
        fechaFinPicker.getStyleClass().add("pdf-date-picker");

        Button generarPdfButton = new Button("Generar PDF");
        generarPdfButton.getStyleClass().add("pdf-generate-button");
        generarPdfButton.setPrefWidth(300);

        // Labels
        Label clienteLabel = new Label("Cliente:");
        clienteLabel.getStyleClass().add("pdf-label"); // CLASE DE ESTILO
        Label desdeLabel = new Label("Desde:");
        desdeLabel.getStyleClass().add("pdf-label"); // CLASE DE ESTILO
        Label hastaLabel = new Label("Hasta:");
        hastaLabel.getStyleClass().add("pdf-label");

        // Creamos VBox para agrupar Label y su campo de entrada
        VBox clienteVBox = new VBox(5); // Espacio de 5 entre label y textfield
        clienteVBox.setAlignment(Pos.TOP_LEFT); // Alineación de los elementos en el VBox
        clienteVBox.getChildren().addAll(clienteLabel, nombreClienteField);

        VBox desdeVBox = new VBox(5);
        desdeVBox.setAlignment(Pos.TOP_LEFT);
        desdeVBox.getChildren().addAll(desdeLabel, fechaInicioPicker);

        VBox hastaVBox = new VBox(5);
        hastaVBox.setAlignment(Pos.TOP_LEFT);
        hastaVBox.getChildren().addAll(hastaLabel, fechaFinPicker);

        // Layout para los campos de entrada y el botón
        HBox inputLayout = new HBox(10);
        inputLayout.getStyleClass().add("pdf-input-layout");
        inputLayout.setPadding(new Insets(15));
        inputLayout.setAlignment(Pos.CENTER);
        inputLayout.getChildren().addAll(
                clienteVBox,
                desdeVBox,
                hastaVBox,
                generarPdfButton
        );

        // --------------- Componentes para el reporte de Proveedores ---------------
        Button generarPdfProveedoresFacturasButton = new Button("Generar PDF Proveedores Facturas"); // Nuevo botón
        generarPdfProveedoresFacturasButton.getStyleClass().add("pdf-generate-button");
        generarPdfProveedoresFacturasButton.setPrefWidth(400); // Un poco más ancho para el texto

        // --------------- Fin Componentes para el reporte de Proveedores ---------------

        // Contenedor principal para la escena
        VBox pdfRoot = new VBox(20);
        pdfRoot.getStyleClass().add("pdf-root-pane");
        pdfRoot.setAlignment(Pos.TOP_CENTER);
        pdfRoot.setPadding(new Insets(20));
        pdfRoot.getChildren().addAll(inputLayout, generarPdfProveedoresFacturasButton);

        Scene pdfScene = new Scene(pdfRoot, 1000, 300);
        try {
            pdfScene.getStylesheets().add(getClass().getResource("/stylePDF.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Advertencia: No se encontró el archivo style.css en src/main/resources/. " + e.getMessage());
        }
        pdfStage.setScene(pdfScene);
        pdfStage.show();

        new CSSFileWatcherPDF("src/main/resources/stylePDF.css", pdfScene).start();

        // Lógica del botón Generar PDF
        generarPdfButton.setOnAction(event -> {
            String nombreCliente = nombreClienteField.getText();
            LocalDate fechaInicio = fechaInicioPicker.getValue();
            LocalDate fechaFin = fechaFinPicker.getValue();

            // Validaciones básicas
            if (nombreCliente.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "El nombre del cliente no puede estar vacío.");
                return;
            }
            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "Ambas fechas (Inicio y Fin) deben ser seleccionadas.");
                return;
            }
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "La fecha de inicio no puede ser posterior a la fecha de fin.");
                return;
            }

            // 1. Buscar el ID del cliente por su nombre
            Cliente cliente = clienteDAO.obtenerClientePorNombre(nombreCliente);
            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Cliente No Encontrado", "No se encontró ningún cliente con el nombre '" + nombreCliente + "'. Por favor, verifica el nombre.");
                return;
            }
            int clienteId = cliente.getId();

            // 2. Obtener los movimientos de cuenta corriente
            List<CuentaCorriente> movimientos = cuentaCorrienteDAO.obtenerMovimientosPorClienteYFechas(clienteId, fechaInicio, fechaFin);

            if (movimientos.isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sin Movimientos", "No se encontraron movimientos de cuenta corriente para el cliente '" + nombreCliente + "' en el rango de fechas seleccionado.");
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Guardar Reporte de Cuenta Corriente");

                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);

                String clienteNombreArchivo = cliente.getNombre().replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_.-]", "");
                String defaultFileName = "reporte_cc_" + clienteNombreArchivo + "_" +
                        fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                        fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
                fileChooser.setInitialFileName(defaultFileName);

                File file = fileChooser.showSaveDialog(pdfStage);

                if (file != null) {
                    try {
                        generateCuentaCorrientePdf(movimientos, cliente, fechaInicio, fechaFin, file.getAbsolutePath());

                        mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de cuenta corriente se ha generado exitosamente en:\n" + file.getAbsolutePath());
                        pdfStage.close();

                        // --- LÓGICA PARA ABRIR EL PDF AUTOMÁTICAMENTE ---
                        if (Desktop.isDesktopSupported()) { // Verifica si la API de Desktop es compatible con el sistema
                            new Thread(() -> { // Se ejecuta en un nuevo hilo para no bloquear la interfaz de usuario
                                try {
                                    Desktop.getDesktop().open(file); // Abre el archivo PDF
                                } catch (IOException e) {
                                    System.err.println("Error al intentar abrir el PDF: " + e.getMessage());
                                    // Muestra una alerta en el hilo de JavaFX si no se puede abrir el PDF
                                    Platform.runLater(() -> mostrarAlerta(Alert.AlertType.ERROR, "Error al Abrir PDF", "No se pudo abrir el PDF automáticamente. Por favor, ábrelo manualmente desde:\n" + file.getAbsolutePath()));
                                }
                            }).start(); // Inicia el nuevo hilo
                        } else {
                            System.out.println("Desktop API no soportada en este sistema. No se puede abrir el PDF automáticamente.");
                        }
                        // --- FIN LÓGICA PARA ABRIR EL PDF AUTOMÁTICAMENTE ---

                    } catch (IOException e) {
                        System.err.println("Error al generar el PDF: " + e.getMessage());
                        e.printStackTrace();
                        mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Hubo un error al generar el PDF: " + e.getMessage());
                    }
                } else {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Cancelada", "La generación del PDF ha sido cancelada.");
                }
            }
        });

        generarPdfProveedoresFacturasButton.setOnAction(event -> {
            handleGenerarPdfProveedoresFacturas(pdfStage); // Llama al nuevo método handler
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void generateCuentaCorrientePdf(List<CuentaCorriente> movimientos, Cliente cliente, LocalDate fechaInicio, LocalDate fechaFin, String outputPath) throws IOException {
        // Inicialización de fuentes para CADA NUEVO DOCUMENTO PDF
        PdfFont FONT_NORMAL;
        PdfFont FONT_BOLD;
        PdfFont FONT_ITALIC;

        try {
            FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            FONT_ITALIC = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        } catch (IOException e) {
            System.err.println("Error al cargar las fuentes PDF en el método generateCuentaCorrientePdf: " + e.getMessage());
            FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA); // Fallback
            FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD); // Fallback
            FONT_ITALIC = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE); // Fallback
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        document.setMargins(20, 20, 20, 20);

        // -- Título y cabecera del documento --
        document.add(new Paragraph("Reporte de Cuenta Corriente")
                .setFont(FONT_BOLD)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("Cliente: " + cliente.getNombre() + " (Razón Social: " + cliente.getRazonSocial() + ")")
                .setFont(FONT_BOLD)
                .setFontSize(12)
                .setMarginBottom(5));

        document.add(new Paragraph(
                "CUIT: " + (cliente.getCUIT() != null ? cliente.getCUIT() : "N/A") +
                        " | Domicilio: " + (cliente.getDomicilio() != null ? cliente.getDomicilio() : "N/A") +
                        " | Localidad: " + (cliente.getLocalidad() != null ? cliente.getLocalidad() : "N/A"))
                .setFont(FONT_NORMAL)
                .setFontSize(9)
                .setMarginBottom(5));

        document.add(new Paragraph(
                "Período: " + fechaInicio.format(dateFormatter) + " - " + fechaFin.format(dateFormatter))
                .setFont(FONT_NORMAL)
                .setFontSize(9)
                .setMarginBottom(15));

        // -- Tabla de movimientos --
        // 7 columnas (sin ID, Neto, IVA)
        Table table = new Table(UnitValue.createPercentArray(new float[]{1.2f, 0.8f, 1.5f, 1f, 1f, 1.2f, 2.5f}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Cabecera de la tabla
        String[] headers = {"Fecha", "Tipo", "Comprobante", "Venta", "Monto", "Saldo", "Observación"};
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header)
                            .setFont(FONT_BOLD)
                            .setFontSize(10)
                            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(52, 152, 219))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5));
        }

        // Filas de datos
        for (CuentaCorriente cc : movimientos) {
            table.addCell(new Cell().add(new Paragraph(cc.getFecha().format(dateFormatter))
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cc.getTipo())
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cc.getComprobante())
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", cc.getVenta()))
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", cc.getMonto()))
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", cc.getSaldo()))
                    .setFont(FONT_BOLD)
                    .setFontSize(9)));
            table.addCell(new Cell().add(new Paragraph(cc.getObservacion())
                    .setFont(FONT_NORMAL)
                    .setFontSize(9)));
        }

        document.add(table);

        // Pie de página
        document.add(new Paragraph(
                "Reporte generado el: " + LocalDate.now().format(dateFormatter) + " a las " +
                        java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .setFont(FONT_NORMAL)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));

        document.close();
    }

    private void handleGenerarPdfProveedoresFacturas(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Facturas por Proveedor");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("reporte_facturas_proveedores_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(parentStage);

        if (file != null) {
            try {
                generateProveedoresFacturasPdf(file.getAbsolutePath()); // Llama al método de generación

                mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de facturas por proveedor se ha generado exitosamente en:\n" + file.getAbsolutePath());
                // parentStage.close(); // Decisión: ¿cierras la ventana de diálogo o la mantienes abierta?
                // La he comentado para permitir al usuario generar múltiples reportes.

                if (Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException e) {
                            System.err.println("Error al intentar abrir el PDF: " + e.getMessage());
                            Platform.runLater(() -> mostrarAlerta(Alert.AlertType.ERROR, "Error al Abrir PDF", "No se pudo abrir el PDF automáticamente. Por favor, ábrelo manualmente desde:\n" + file.getAbsolutePath()));
                        }
                    }).start();
                } else {
                    System.out.println("Desktop API no soportada en este sistema. No se puede abrir el PDF automáticamente.");
                }

            } catch (IOException e) {
                System.err.println("Error al generar el PDF de facturas por proveedor: " + e.getMessage());
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Hubo un error al generar el PDF de facturas por proveedor: " + e.getMessage());
            }
        } else {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Cancelada", "La generación del PDF de facturas por proveedor ha sido cancelada.");
        }
    }

    // --------------------------------------------------------------------------------------------------
    // ¡NUEVO MÉTODO para generar el PDF de Facturas por Proveedor!
    // --------------------------------------------------------------------------------------------------
    private void generateProveedoresFacturasPdf(String outputPath) throws IOException {
        PdfFont FONT_NORMAL;
        PdfFont FONT_BOLD;
        PdfFont FONT_ITALIC;

        try {
            FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            FONT_ITALIC = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        } catch (IOException e) {
            System.err.println("Error al cargar las fuentes PDF en el método generateProveedoresFacturasPdf: " + e.getMessage());
            FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            FONT_ITALIC = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4); // Tamaño A4 estándar, puedes usar A4.rotate() si lo prefieres ancho

        document.setMargins(30, 30, 30, 30); // Márgenes

        // Título principal del documento
        document.add(new Paragraph("Reporte de Facturas por Proveedor")
                .setFont(FONT_BOLD)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Obtener todos los proveedores
        List<Proveedor> proveedores = null; // Inicializar a null

        try {
            // Obtener todos los proveedores
            // ¡Aquí se llama al método de instancia en el objeto `proveedorDAO`!
            proveedores = proveedorDAO.obtenerTodosLosProveedores();
        } catch (SQLException e) {
            System.err.println("Error de base de datos al obtener proveedores: " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudieron cargar los proveedores debido a un error en la base de datos.");
            document.add(new Paragraph("Error: No se pudieron cargar los datos de los proveedores."));
            document.close();
            return; // Salir del método si hay un error de base de datos
        }
        if (proveedores.isEmpty()) {
            document.add(new Paragraph("No se encontraron proveedores para generar el reporte de facturas.")
                    .setFont(FONT_NORMAL)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));
        } else {
            for (Proveedor proveedor : proveedores) {
                // Información del proveedor
                document.add(new Paragraph("PROVEEDOR: " + proveedor.getNombre() + " (CUIT: " + (proveedor.getCUIT() != null ? proveedor.getCUIT() : "N/A") + ")")
                        .setFont(FONT_BOLD)
                        .setFontSize(14)
                        .setMarginTop(20) // Espacio antes de cada nuevo proveedor
                        .setMarginBottom(10)
                        .setTextAlignment(TextAlignment.LEFT));

                // Obtener facturas para este proveedor usando el método actualizado
                List<CuentaCorriente> facturas = cuentaCorrienteDAO.obtenerMovimientosPorProveedorYTipo(proveedor.getId(), "Factura"); // ¡Aquí se llama con "Factura"!

                if (facturas.isEmpty()) {
                    document.add(new Paragraph("  - Sin facturas registradas para este proveedor.")
                            .setFont(FONT_ITALIC)
                            .setFontSize(10)
                            .setMarginLeft(20)
                            .setMarginBottom(10));
                } else {
                    // Tabla para las facturas del proveedor
                    Table facturasTable = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f})); // Columnas: Fecha, Tipo, Comprobante, Monto, Saldo, Observación
                    facturasTable.setWidth(UnitValue.createPercentValue(100)); // Un poco menos ancho para margen
                    facturasTable.setMarginLeft(20);
                    facturasTable.setMarginBottom(15);

                    // Cabecera de la tabla de facturas
                    String[] facturaHeaders = {"Fecha", "N°Factura", "Tipo", "Neto", "IVA", "Otros", "Monto", "Saldo", "Observación"};
                    for (String header : facturaHeaders) {
                        facturasTable.addHeaderCell(new Cell().add(new Paragraph(header)
                                        .setFont(FONT_BOLD)
                                        .setFontSize(9)
                                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE))
                                .setBackgroundColor(new DeviceRgb(70, 130, 180)) // Otro color para diferenciar
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(4));
                    }

                    // Filas de datos de facturas
                    for (CuentaCorriente factura : facturas) {
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getFecha().format(dateFormatter))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getComprobante())
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getTipo())
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getNeto()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getIva()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getOtros()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getMonto()))
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", factura.getSaldo()))
                                .setFont(FONT_BOLD)
                                .setFontSize(8)));
                        facturasTable.addCell(new Cell().add(new Paragraph(factura.getObservacion())
                                .setFont(FONT_NORMAL)
                                .setFontSize(8)));
                    }
                    document.add(facturasTable);
                }
            }
        }

        // Pie de página general del reporte de proveedores
        document.add(new Paragraph(
                "Reporte de Facturas por Proveedor generado el: " + LocalDate.now().format(dateFormatter) + " a las " +
                        java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .setFont(FONT_NORMAL)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20));

        document.close();
    }
}