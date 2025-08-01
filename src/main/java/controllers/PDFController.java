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

    // Declaración como miembros de clase
    private DatePicker fechaInicioPickerProveedor;
    private DatePicker fechaFinPickerProveedor;

    public PDFController() {
        this.clienteDAO = new ClienteDAO();
        this.cuentaCorrienteDAO = new CuentaCorrienteDAO();
        this.proveedorDAO = new ProveedorDAO();
    }

    public void handleGuardarPdfButton() {
        Stage pdfStage = new Stage();
        pdfStage.setTitle("Generar Reporte de Cuenta Corriente");
        pdfStage.getIcons().add(new Image(getClass().getResourceAsStream("/icono.png")));

        // Campos de entrada para Clientes
        TextField nombreClienteField = new TextField();
        nombreClienteField.setPromptText("Nombre del Cliente");
        nombreClienteField.setPrefWidth(300);
        nombreClienteField.getStyleClass().add("pdf-input-field");

        DatePicker fechaInicioPickerCliente = new DatePicker();
        fechaInicioPickerCliente.setPromptText("Fecha Inicio");
        fechaInicioPickerCliente.setPrefWidth(300);
        fechaInicioPickerCliente.getStyleClass().add("pdf-date-picker");

        DatePicker fechaFinPickerCliente = new DatePicker();
        fechaFinPickerCliente.setPromptText("Fecha Fin");
        fechaFinPickerCliente.setPrefWidth(300);
        fechaFinPickerCliente.getStyleClass().add("pdf-date-picker");

        Button generarPdfButtonCliente = new Button("Generar PDF Cliente");
        generarPdfButtonCliente.getStyleClass().add("pdf-generate-button");
        generarPdfButtonCliente.setPrefWidth(300);

        // Labels para Clientes
        Label clienteLabel = new Label("Cliente:");
        clienteLabel.getStyleClass().add("pdf-label");
        Label desdeLabelCliente = new Label("Desde:");
        desdeLabelCliente.getStyleClass().add("pdf-label");
        Label hastaLabelCliente = new Label("Hasta:");
        hastaLabelCliente.getStyleClass().add("pdf-label");

        // VBox para agrupar Label y su campo de entrada para Clientes
        VBox clienteVBox = new VBox(5);
        clienteVBox.setAlignment(Pos.TOP_LEFT);
        clienteVBox.getChildren().addAll(clienteLabel, nombreClienteField);

        VBox desdeVBoxCliente = new VBox(5);
        desdeVBoxCliente.setAlignment(Pos.TOP_LEFT);
        desdeVBoxCliente.getChildren().addAll(desdeLabelCliente, fechaInicioPickerCliente);

        VBox hastaVBoxCliente = new VBox(5);
        hastaVBoxCliente.setAlignment(Pos.TOP_LEFT);
        hastaVBoxCliente.getChildren().addAll(hastaLabelCliente, fechaFinPickerCliente);

        // Layout para los campos de entrada y el botón de Clientes
        HBox inputLayoutCliente = new HBox(10);
        inputLayoutCliente.getStyleClass().add("pdf-input-layout");
        inputLayoutCliente.setPadding(new Insets(15));
        inputLayoutCliente.setAlignment(Pos.CENTER);
        inputLayoutCliente.getChildren().addAll(
                clienteVBox,
                desdeVBoxCliente,
                hastaVBoxCliente,
                generarPdfButtonCliente
        );

        // --------------- Componentes para el reporte de Proveedores ---------------
        Label tituloProveedores = new Label("Reporte de Facturas por Proveedor");
        tituloProveedores.getStyleClass().add("pdf-label");
        tituloProveedores.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        // Inicialización de los DatePickers de Proveedores (ahora son miembros de clase)
        fechaInicioPickerProveedor = new DatePicker();
        fechaInicioPickerProveedor.setPromptText("Fecha Inicio Proveedor");
        fechaInicioPickerProveedor.setPrefWidth(300);
        fechaInicioPickerProveedor.getStyleClass().add("pdf-date-picker");

        fechaFinPickerProveedor = new DatePicker();
        fechaFinPickerProveedor.setPromptText("Fecha Fin Proveedor");
        fechaFinPickerProveedor.setPrefWidth(300);
        fechaFinPickerProveedor.getStyleClass().add("pdf-date-picker");

        Button generarPdfProveedoresFacturasButton = new Button("Generar PDF Proveedores Facturas");
        generarPdfProveedoresFacturasButton.getStyleClass().add("pdf-generate-button");
        generarPdfProveedoresFacturasButton.setPrefWidth(400);

        Label desdeLabelProveedor = new Label("Desde:");
        desdeLabelProveedor.getStyleClass().add("pdf-label");
        Label hastaLabelProveedor = new Label("Hasta:");
        hastaLabelProveedor.getStyleClass().add("pdf-label");

        VBox desdeVBoxProveedor = new VBox(5);
        desdeVBoxProveedor.setAlignment(Pos.TOP_LEFT);
        desdeVBoxProveedor.getChildren().addAll(desdeLabelProveedor, fechaInicioPickerProveedor);

        VBox hastaVBoxProveedor = new VBox(5);
        hastaVBoxProveedor.setAlignment(Pos.TOP_LEFT);
        hastaVBoxProveedor.getChildren().addAll(hastaLabelProveedor, fechaFinPickerProveedor);

        HBox inputLayoutProveedor = new HBox(10);
        inputLayoutProveedor.getStyleClass().add("pdf-input-layout");
        inputLayoutProveedor.setPadding(new Insets(15));
        inputLayoutProveedor.setAlignment(Pos.CENTER);
        inputLayoutProveedor.getChildren().addAll(
                desdeVBoxProveedor,
                hastaVBoxProveedor,
                generarPdfProveedoresFacturasButton
        );
        // --------------- Fin Componentes para el reporte de Proveedores ---------------


        // Contenedor principal para la escena
        VBox pdfRoot = new VBox(20);
        pdfRoot.getStyleClass().add("pdf-root-pane");
        pdfRoot.setAlignment(Pos.TOP_CENTER);
        pdfRoot.setPadding(new Insets(20));
        pdfRoot.getChildren().addAll(inputLayoutCliente, new Label(""), tituloProveedores, inputLayoutProveedor); // Añadir los componentes de proveedores

        Scene pdfScene = new Scene(pdfRoot, 1000, 450); // Aumentar alto para nuevos componentes
        try {
            pdfScene.getStylesheets().add(getClass().getResource("/stylePDF.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Advertencia: No se encontró el archivo style.css en src/main/resources/. " + e.getMessage());
        }
        pdfStage.setScene(pdfScene);
        pdfStage.show();

//        new CSSFileWatcherPDF("src/main/resources/stylePDF.css", pdfScene).start();

        // Lógica del botón Generar PDF (Clientes)
        generarPdfButtonCliente.setOnAction(event -> {
            String nombreCliente = nombreClienteField.getText();
            LocalDate fechaInicio = fechaInicioPickerCliente.getValue();
            LocalDate fechaFin = fechaFinPickerCliente.getValue();

            // Validaciones básicas
            if (nombreCliente.isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "El nombre del cliente no puede estar vacío.");
                return;
            }
            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "Ambas fechas (Inicio y Fin) deben ser seleccionadas para el reporte de cliente.");
                return;
            }
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "La fecha de inicio no puede ser posterior a la fecha de fin para el reporte de cliente.");
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
                        System.err.println("Error al generar el PDF: " + e.getMessage());
                        e.printStackTrace();
                        mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Hubo un error al generar el PDF: " + e.getMessage());
                    }
                } else {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Cancelada", "La generación del PDF ha sido cancelada.");
                }
            }
        });

        // Lógica del botón Generar PDF (Proveedores)
        generarPdfProveedoresFacturasButton.setOnAction(event -> {
            LocalDate fechaInicio = fechaInicioPickerProveedor.getValue();
            LocalDate fechaFin = fechaFinPickerProveedor.getValue();

            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "Ambas fechas (Inicio y Fin) deben ser seleccionadas para el reporte de proveedores.");
                return;
            }
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "La fecha de inicio no puede ser posterior a la fecha de fin para el reporte de proveedores.");
                return;
            }

            try { // Añadido try-catch para manejar IOException
                handleGenerarPdfProveedoresFacturas(pdfStage, fechaInicio, fechaFin);
            } catch (IOException e) {
                System.err.println("Error al manejar la generación del PDF de proveedores: " + e.getMessage());
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Generación", "Ocurrió un error al intentar generar el PDF de facturas de proveedores: " + e.getMessage());
            }
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
        PdfFont FONT_NORMAL;
        PdfFont FONT_BOLD;

        try {
            FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            System.err.println("Error al cargar las fuentes PDF en el método generateCuentaCorrientePdf: " + e.getMessage());
            FONT_NORMAL = PdfFontFactory.createFont(StandardFonts.HELVETICA); // Fallback
            FONT_BOLD = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD); // Fallback
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

    // Modificación: se agregó 'throws IOException' a la firma del método
    private void handleGenerarPdfProveedoresFacturas(Stage parentStage, LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Facturas por Proveedor");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialFileName("reporte_facturas_proveedores_" +
                fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf"); // Nombre con fechas

        File file = fileChooser.showSaveDialog(parentStage);

        if (file != null) {
            try {
                // Modificación: se pasan las fechas a generateProveedoresFacturasPdf
                generateProveedoresFacturasPdf(file.getAbsolutePath(), fechaInicio, fechaFin);

                mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de facturas por proveedor se ha generado exitosamente en:\n" + file.getAbsolutePath());

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
                // Relanzar la excepción para que sea manejada por el llamador
                throw e;
            }
        } else {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Cancelada", "La generación del PDF de facturas por proveedor ha sido cancelada.");
        }
    }

    // Modificación: se agregaron los parámetros fechaInicio y fechaFin
    private void generateProveedoresFacturasPdf(String outputPath, LocalDate fechaInicio, LocalDate fechaFin) throws IOException {
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
        Document document = new Document(pdf, PageSize.A4);

        document.setMargins(30, 30, 30, 30);

        document.add(new Paragraph("Reporte de Facturas por Proveedor")
                .setFont(FONT_BOLD)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Añadir el rango de fechas al título del reporte
        document.add(new Paragraph(
                "Período: " + fechaInicio.format(dateFormatter) + " - " + fechaFin.format(dateFormatter))
                .setFont(FONT_NORMAL)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));


        List<Proveedor> proveedores = null;

        try {
            proveedores = proveedorDAO.obtenerTodosLosProveedores();
        } catch (SQLException e) {
            System.err.println("Error de base de datos al obtener proveedores: " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudieron cargar los proveedores debido a un error en la base de datos.");
            document.add(new Paragraph("Error: No se pudieron cargar los datos de los proveedores."));
            document.close();
            return;
        }
        if (proveedores.isEmpty()) {
            document.add(new Paragraph("No se encontraron proveedores para generar el reporte de facturas.")
                    .setFont(FONT_NORMAL)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));
        } else {
            for (Proveedor proveedor : proveedores) {
                document.add(new Paragraph("PROVEEDOR: " + proveedor.getNombre() + " (CUIT: " + (proveedor.getCUIT() != null ? proveedor.getCUIT() : "N/A") + ")")
                        .setFont(FONT_BOLD)
                        .setFontSize(14)
                        .setMarginTop(20)
                        .setMarginBottom(10)
                        .setTextAlignment(TextAlignment.LEFT));

                // Modificación: Llamar a un nuevo método en CuentaCorrienteDAO que incluya el filtro por fechas
                List<CuentaCorriente> facturas = cuentaCorrienteDAO.obtenerMovimientosPorProveedorYTipoYFechas(proveedor.getId(), "Factura", fechaInicio, fechaFin);

                if (facturas.isEmpty()) {
                    document.add(new Paragraph("  - Sin facturas registradas para este proveedor en el período seleccionado.")
                            .setFont(FONT_ITALIC)
                            .setFontSize(10)
                            .setMarginLeft(20)
                            .setMarginBottom(10));
                } else {
                    Table facturasTable = new Table(UnitValue.createPercentArray(new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 2f}));
                    facturasTable.setWidth(UnitValue.createPercentValue(100));
                    facturasTable.setMarginLeft(20);
                    facturasTable.setMarginBottom(15);

                    String[] facturaHeaders = {"Fecha", "N°Factura", "Tipo", "Neto", "IVA", "Otros", "Monto", "Saldo", "Observación"};
                    for (String header : facturaHeaders) {
                        facturasTable.addHeaderCell(new Cell().add(new Paragraph(header)
                                        .setFont(FONT_BOLD)
                                        .setFontSize(9)
                                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE))
                                .setBackgroundColor(new DeviceRgb(70, 130, 180))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setPadding(4));
                    }

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