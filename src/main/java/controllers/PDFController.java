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

    private DatePicker fechaInicioPickerProveedor;
    private DatePicker fechaFinPickerProveedor;

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

            // 2. Obtener los movimientos de cuenta corriente
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
                    } catch (Exception e) { // Captura cualquier otra excepción inesperada
                        System.err.println("Un error inesperado ocurrió: " + e.getMessage());
                        e.printStackTrace();
                        mostrarAlerta(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error inesperado: " + e.getMessage());
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

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Facturas por Proveedor");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialFileName("reporte_facturas_proveedores_" +
                    fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" +
                    fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

            File file = fileChooser.showSaveDialog(pdfStage);

            if (file != null) {
                try {
                    pdfReportService.generateProveedoresFacturasPdf(file.getAbsolutePath(), fechaInicio, fechaFin);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "PDF Generado", "El reporte de facturas por proveedor se ha generado exitosamente en:\n" + file.getAbsolutePath());
                    openPdfAutomatically(file);
                } catch (IOException e) {
                    System.err.println("Error al generar el PDF de facturas por proveedor: " + e.getMessage());
                    e.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al Generar PDF", "Ocurrió un error al intentar generar el PDF de facturas de proveedores: " + e.getMessage());
                } catch (SQLException e) { // ¡NUEVO CATCH! Maneja errores de DB que vienen del servicio
                    System.err.println("Error de base de datos al generar PDF de proveedores: " + e.getMessage());
                    e.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "Ocurrió un error de base de datos al generar el PDF de proveedores: " + e.getMessage());
                } catch (Exception e) { // Captura cualquier otra excepción inesperada
                    System.err.println("Un error inesperado ocurrió: " + e.getMessage());
                    e.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error inesperado al generar el PDF de proveedores: " + e.getMessage());
                }
            } else {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Generación Cancelada", "La generación del PDF de facturas por proveedor ha sido cancelada.");
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