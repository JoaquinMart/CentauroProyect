package controllers;

import dao.*;
import model.*;
import services.PdfReportService;

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
import javafx.application.Platform;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.awt.Desktop;

public class PDFController {

    private ClienteDAO clienteDAO;
    private CuentaCorrienteDAO cuentaCorrienteDAO;
    private ProveedorDAO proveedorDAO;
    private PdfReportService pdfReportService;

    // Variables para el primer reporte de proveedores
    private DatePicker fechaInicioPickerProveedor;
    private DatePicker fechaFinPickerProveedor;
    private Button generarPdfProveedoresFacturasButton;

    // Nuevas variables para el segundo reporte de proveedores
    private DatePicker fechaInicioPickerProveedor2;
    private DatePicker fechaFinPickerProveedor2;
    private Button generarPdfReporteProveedoresButton;
    private TextField nombreProveedorField;
    private TextField nombreComprobanteField;

    public PDFController() {
        try {
            this.clienteDAO = new ClienteDAO();
            this.cuentaCorrienteDAO = new CuentaCorrienteDAO();
            this.proveedorDAO = new ProveedorDAO();
            this.pdfReportService = new PdfReportService(clienteDAO, cuentaCorrienteDAO, proveedorDAO);
        } catch (SQLException e) {
            System.err.println("Error initializing DAOs: " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudo conectar a la base de datos o inicializar los servicios.");
        }
    }

    // Método principal del BOTON PDF, se podria sub dividir
    public void handleGuardarPdfButton() {
        Stage pdfStage = new Stage();
        pdfStage.setTitle("Generar Reportes de PDF");
        pdfStage.getIcons().add(new Image(getClass().getResourceAsStream("/icono.png")));

        // --------------- Componentes para el reporte de Clientes ---------------
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

        Label clienteLabel = new Label("Cliente:");
        clienteLabel.getStyleClass().add("pdf-label");
        Label desdeLabelCliente = new Label("Desde:");
        desdeLabelCliente.getStyleClass().add("pdf-label");
        Label hastaLabelCliente = new Label("Hasta:");
        hastaLabelCliente.getStyleClass().add("pdf-label");

        VBox clienteVBox = new VBox(5);
        clienteVBox.setAlignment(Pos.TOP_LEFT);
        clienteVBox.getChildren().addAll(clienteLabel, nombreClienteField);

        VBox desdeVBoxCliente = new VBox(5);
        desdeVBoxCliente.setAlignment(Pos.TOP_LEFT);
        desdeVBoxCliente.getChildren().addAll(desdeLabelCliente, fechaInicioPickerCliente);

        VBox hastaVBoxCliente = new VBox(5);
        hastaVBoxCliente.setAlignment(Pos.TOP_LEFT);
        hastaVBoxCliente.getChildren().addAll(hastaLabelCliente, fechaFinPickerCliente);

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

        // Separador visual
        Label separador = new Label("--- Reportes de Proveedores ---");
        separador.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0;");

        // --------------- Componentes para el reporte 1 de Proveedores ---------------
        Label tituloProveedores1 = new Label("Reporte de Facturas por Proveedor");
        tituloProveedores1.getStyleClass().add("pdf-label");
        tituloProveedores1.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        // Inicialización de las variables de clase
        nombreProveedorField = new TextField();
        nombreProveedorField.setPromptText("Nombre del Proveedor");
        nombreProveedorField.setPrefWidth(300);
        nombreProveedorField.getStyleClass().add("pdf-input-field");

        fechaInicioPickerProveedor = new DatePicker();
        fechaInicioPickerProveedor.setPromptText("Fecha Inicio");
        fechaInicioPickerProveedor.setPrefWidth(300);
        fechaInicioPickerProveedor.getStyleClass().add("pdf-date-picker");

        fechaFinPickerProveedor = new DatePicker();
        fechaFinPickerProveedor.setPromptText("Fecha Fin");
        fechaFinPickerProveedor.setPrefWidth(300);
        fechaFinPickerProveedor.getStyleClass().add("pdf-date-picker");

        generarPdfProveedoresFacturasButton = new Button("Generar PDF Proveedor");
        generarPdfProveedoresFacturasButton.getStyleClass().add("pdf-generate-button");
        generarPdfProveedoresFacturasButton.setPrefWidth(400);

        Label proveedorLabel = new Label("Proveedor:");
        proveedorLabel.getStyleClass().add("pdf-label");
        Label desdeLabelProveedor = new Label("Desde:");
        desdeLabelProveedor.getStyleClass().add("pdf-label");
        Label hastaLabelProveedor = new Label("Hasta:");
        hastaLabelProveedor.getStyleClass().add("pdf-label");

        VBox proveedorVBox = new VBox(5);
        proveedorVBox.setAlignment(Pos.TOP_LEFT);
        proveedorVBox.getChildren().addAll(proveedorLabel, nombreProveedorField);

        VBox desdeVBoxProveedor = new VBox(5);
        desdeVBoxProveedor.setAlignment(Pos.TOP_LEFT);
        desdeVBoxProveedor.getChildren().addAll(desdeLabelProveedor, fechaInicioPickerProveedor);

        VBox hastaVBoxProveedor = new VBox(5);
        hastaVBoxProveedor.setAlignment(Pos.TOP_LEFT);
        hastaVBoxProveedor.getChildren().addAll(hastaLabelProveedor, fechaFinPickerProveedor);

        HBox inputLayoutProveedor1 = new HBox(10);
        inputLayoutProveedor1.getStyleClass().add("pdf-input-layout");
        inputLayoutProveedor1.setPadding(new Insets(15));
        inputLayoutProveedor1.setAlignment(Pos.CENTER);
        inputLayoutProveedor1.getChildren().addAll(
                proveedorVBox,
                desdeVBoxProveedor,
                hastaVBoxProveedor,
                generarPdfProveedoresFacturasButton
        );

        // Separador visual
        Label separador2 = new Label("--- Reporte de todos los Comprobantes de Proveedores ---");
        separador2.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0;");

        // --------------- Componentes para el reporte 2 de Proveedores (el 3er PDF en total) ---------------

        nombreComprobanteField = new TextField();
        nombreComprobanteField.setPromptText("Nombre Producto (o vacio)");
        nombreComprobanteField.setPrefWidth(300);
        nombreComprobanteField.getStyleClass().add("pdf-input-field");

        // Inicialización de las nuevas variables de clase
        fechaInicioPickerProveedor2 = new DatePicker();
        fechaInicioPickerProveedor2.setPromptText("Fecha Inicio");
        fechaInicioPickerProveedor2.setPrefWidth(300);
        fechaInicioPickerProveedor2.getStyleClass().add("pdf-date-picker");

        fechaFinPickerProveedor2 = new DatePicker();
        fechaFinPickerProveedor2.setPromptText("Fecha Fin");
        fechaFinPickerProveedor2.setPrefWidth(300);
        fechaFinPickerProveedor2.getStyleClass().add("pdf-date-picker");

        generarPdfReporteProveedoresButton = new Button("Generar PDF Reporte Proveedores");
        generarPdfReporteProveedoresButton.getStyleClass().add("pdf-generate-button");
        generarPdfReporteProveedoresButton.setPrefWidth(400);

        Label proveedorLabel2 = new Label("Producto:");
        proveedorLabel2.getStyleClass().add("pdf-label");
        Label desdeLabelProveedor2 = new Label("Desde:");
        desdeLabelProveedor2.getStyleClass().add("pdf-label");
        Label hastaLabelProveedor2 = new Label("Hasta:");
        hastaLabelProveedor2.getStyleClass().add("pdf-label");

        VBox proveedor2VBox = new VBox(5);
        proveedor2VBox.setAlignment(Pos.TOP_LEFT);
        proveedor2VBox.getChildren().addAll(proveedorLabel2, nombreComprobanteField);

        VBox desdeVBoxProveedor2 = new VBox(5);
        desdeVBoxProveedor2.setAlignment(Pos.TOP_LEFT);
        desdeVBoxProveedor2.getChildren().addAll(desdeLabelProveedor2, fechaInicioPickerProveedor2);

        VBox hastaVBoxProveedor2 = new VBox(5);
        hastaVBoxProveedor2.setAlignment(Pos.TOP_LEFT);
        hastaVBoxProveedor2.getChildren().addAll(hastaLabelProveedor2, fechaFinPickerProveedor2);

        HBox inputLayoutProveedor2 = new HBox(10);
        inputLayoutProveedor2.getStyleClass().add("pdf-input-layout");
        inputLayoutProveedor2.setPadding(new Insets(15));
        inputLayoutProveedor2.setAlignment(Pos.CENTER);
        inputLayoutProveedor2.getChildren().addAll(
                proveedor2VBox,
                desdeVBoxProveedor2,
                hastaVBoxProveedor2,
                generarPdfReporteProveedoresButton
        );

        // Contenedor principal para la escena
        VBox pdfRoot = new VBox(20);
        pdfRoot.getStyleClass().add("pdf-root-pane");
        pdfRoot.setAlignment(Pos.TOP_CENTER);
        pdfRoot.setPadding(new Insets(20));
        pdfRoot.getChildren().addAll(
                inputLayoutCliente,
                separador,
                inputLayoutProveedor1,
                separador2,
                inputLayoutProveedor2
        );

        Scene pdfScene = new Scene(pdfRoot, 1000, 700);
        pdfScene.getStylesheets().add(getClass().getResource("/stylePDF.css").toExternalForm());
        pdfStage.setScene(pdfScene);
        pdfStage.show();

        // Lógica del botón Generar PDF (Clientes)
        generarPdfButtonCliente.setOnAction(event -> {
            String nombreCliente = nombreClienteField.getText();
            LocalDate fechaInicio = fechaInicioPickerCliente.getValue();
            LocalDate fechaFin = fechaFinPickerCliente.getValue();

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

            Cliente cliente = null;
            try {
                cliente = clienteDAO.obtenerClientePorNombre(nombreCliente);
            } catch (SQLException e) {
                System.err.println("Error de base de datos al obtener cliente: " + e.getMessage());
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al acceder a la base de datos para buscar el cliente.");
                return;
            }

            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Cliente No Encontrado", "No se encontró ningún cliente con el nombre '" + nombreCliente + "'. Por favor, verifica el nombre.");
                return;
            }
            int clienteId = cliente.getId();

            List<CuentaCorriente> movimientos = null;
            try {
                movimientos = cuentaCorrienteDAO.obtenerMovimientosPorClienteYFechas(clienteId, fechaInicio, fechaFin);
            } catch (SQLException e) {
                System.err.println("Error de base de datos al obtener movimientos del cliente: " + e.getMessage());
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error al acceder a la base de datos para los movimientos del cliente.");
                return;
            }

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
                        pdfReportService.generateCuentaCorrientePdf(movimientos, cliente, fechaInicio, fechaFin, file.getAbsolutePath());
                        mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de cuenta corriente se ha generado exitosamente en:\n" + file.getAbsolutePath());
                        pdfStage.close();
                        openPdfAutomatically(file);
                    } catch (IOException e) {
                        System.err.println("Error al generar el PDF: " + e.getMessage());
                        e.printStackTrace();
                        mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Hubo un error al generar el PDF: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Un error inesperado ocurrió: " + e.getMessage());
                        e.printStackTrace();
                        mostrarAlerta(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error inesperado: " + e.getMessage());
                    }
                } else {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Cancelada", "La generación del PDF ha sido cancelada.");
                }
            }
        });

        // Lógica del botón Generar PDF (Proveedores Facturas)
        generarPdfProveedoresFacturasButton.setOnAction(event -> {
            LocalDate fechaInicio = fechaInicioPickerProveedor.getValue();
            LocalDate fechaFin = fechaFinPickerProveedor.getValue();

            // Validaciones
            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "Ambas fechas (Inicio y Fin) deben ser seleccionadas para el reporte de proveedores.");
                return;
            }
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "La fecha de inicio no puede ser posterior a la fecha de fin para el reporte de proveedores.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Facturas por Proveedor");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName("reporte_proveedores_facturas_" +
                    fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

            File file = fileChooser.showSaveDialog(pdfStage);

            if (file != null) {
                try {
                    pdfReportService.generateProveedoresFacturasPdf(file.getAbsolutePath(), fechaInicio, fechaFin);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de facturas por proveedor se ha generado exitosamente en:\n" + file.getAbsolutePath());
                    openPdfAutomatically(file);
                } catch (IOException | SQLException e) {
                    System.err.println("Error al generar el PDF de facturas por proveedor: " + e.getMessage());
                    e.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Ocurrió un error al intentar generar el PDF de facturas de proveedores: " + e.getMessage());
                }
            }
        });

        // Lógica del nuevo botón Generar PDF (Reporte Proveedores)
        generarPdfReporteProveedoresButton.setOnAction(event -> {
            LocalDate fechaInicio = fechaInicioPickerProveedor2.getValue();
            LocalDate fechaFin = fechaFinPickerProveedor2.getValue();
            String nombreComprobante = nombreComprobanteField.getText();

            // Validaciones
            if (fechaInicio == null || fechaFin == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "Ambas fechas (Inicio y Fin) deben ser seleccionadas para el reporte de proveedores.");
                return;
            }
            if (fechaInicio.isAfter(fechaFin)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Validación", "La fecha de inicio no puede ser posterior a la fecha de fin para el reporte de proveedores.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Comprobantes de Proveedores");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName("reporte_comprobantes_proveedores_" +
                    fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

            File file = fileChooser.showSaveDialog(pdfStage);

            if (file != null) {
                try {
                    pdfReportService.generateProveedorReportPdf(file.getAbsolutePath(), fechaInicio, fechaFin, nombreComprobante);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de comprobantes de proveedores se ha generado exitosamente en:\n" + file.getAbsolutePath());
                    openPdfAutomatically(file);
                } catch (IOException | SQLException e) {
                    System.err.println("Error al generar el PDF de comprobantes por proveedor: " + e.getMessage());
                    e.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Ocurrió un error al intentar generar el PDF de comprobantes de proveedores: " + e.getMessage());
                }
            }
        });
    }

    // Método auxiliar para mostrar las alertas
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método auxiliar para abrir los pdf luego de su creacion
    private void openPdfAutomatically(File file) {
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
    }
}