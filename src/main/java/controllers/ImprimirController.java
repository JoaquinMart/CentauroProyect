package controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import dao.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import model.*;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont; // Importar PdfFont
import com.itextpdf.kernel.font.PdfFontFactory; // Importar PdfFontFactory
import com.itextpdf.io.font.constants.StandardFonts;

public class ImprimirController {

    public static void imprimirSeleccion(ComboBox<String> imprimirCombo) {
        String seleccion = imprimirCombo.getValue();
        if (seleccion == null) return;

        if (seleccion.equalsIgnoreCase("Clientes")) {
            mostrarClientes();
        } else if (seleccion.equalsIgnoreCase("Proveedores")) {
            mostrarProveedores();
        } else if (seleccion.equalsIgnoreCase("Productos")) {
            mostrarProductos();
        } else if (seleccion.equalsIgnoreCase("Deudores")) {
            mostrarClientesDeudores();
        }
    }

    private static void mostrarProductos() {
        ProductoDAO productoDAO = new ProductoDAO();
        try {
            List<Producto> productos = productoDAO.obtenerProductos();

            TableView<Producto> tablaProductos = new TableView<>(FXCollections.observableArrayList(productos));
            tablaProductos.getStyleClass().add("main-table");

            tablaProductos.getColumns().addAll(
                    crearColumnaNumerica("ID", "id"),
                    crearColumnaTexto("Codigo", "codigo"),
                    crearColumnaTexto("Nombre", "nombre"),
                    crearColumnaNumerica("Precio Unitario", "precio")
            );

            tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            mostrarVentana("Listado de Productos:", "Productos", tablaProductos);

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error al obtener los productos.");
        }
    }

    private static void mostrarClientesDeudores() {
        ClienteDAO clienteDAO = new ClienteDAO();
        try {
            // 1. Obtener TODOS los clientes con sus cuentas corrientes cargadas
            List<Cliente> todosLosClientesConCuentas = clienteDAO.obtenerClientesConCuentas();

            // 2. Filtrar esta lista para obtener solo los clientes con saldo diferente de 0
            List<Cliente> clientesDeudores = todosLosClientesConCuentas.stream()
                    .filter(cliente -> {
                        List<CuentaCorriente> cuentas = cliente.getCuentaCorrientes();
                        // Calcula el saldo del cliente
                        double saldo = 0;
                        if (cuentas != null && !cuentas.isEmpty()) {
                            CuentaCorriente ultima = cuentas.stream()
                                    .max(Comparator.comparing(CuentaCorriente::getFecha))
                                    .orElse(null);
                            if (ultima != null) {
                                saldo = ultima.getSaldo();
                            }
                        }
                        return saldo != 0;
                    })
                    .collect(Collectors.toList());

            // 3. Crear la TableView con la lista filtrada de clientes deudores
            TableView<Cliente> tablaClientes = new TableView<>(FXCollections.observableArrayList(clientesDeudores));
            tablaClientes.getStyleClass().add("main-table");

            tablaClientes.getColumns().addAll(
                    crearColumnaNumerica("ID", "id"),
                    crearColumnaTexto("Nombre", "nombre"),
                    crearColumnaTexto("Razón Social", "razonSocial"),
                    crearColumnaTexto("CUIT", "CUIT"),
                    crearColumnaTexto("Teléfono", "telefono"),
                    crearColumnaTexto("Localidad", "localidad"),
                    crearColumnaTexto("Condición", "condicion"),
                    crearColumnaTexto("Proveedor", "proveedor"),
                    // Usamos crearColumnaSaldoCliente que calcula el saldo para mostrarlo
                    crearColumnaSaldoCliente()
            );

            tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            Button botonGenerarPdf = new Button("Generar PDF de Deudores");
            botonGenerarPdf.getStyleClass().add("action-button");
            botonGenerarPdf.setOnAction(event -> generarPdfDeudores((Stage) tablaClientes.getScene().getWindow(), clientesDeudores));

            VBox layout = new VBox(10, new Label("Listado de Clientes Deudores:"), tablaClientes, botonGenerarPdf);
            VBox.setVgrow(tablaClientes, Priority.ALWAYS);
            layout.setPadding(new Insets(10));

            Stage ventana = new Stage();
            ventana.getIcons().add(new Image(ImprimirController.class.getResourceAsStream("/icono.png")));
            ventana.setTitle("Clientes Deudores");
            Scene scene = new Scene(layout, 1200, 800); // Crear la Scene
            scene.getStylesheets().add(ImprimirController.class.getResource("/style.css").toExternalForm()); // O la clase correcta si este método no está en ImprimirController
            ventana.setScene(scene); // Establecer la Scene
            ventana.show();


        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error al obtener los clientes deudores.");
        }
    }

    private static void mostrarClientes() {
        ClienteDAO clienteDAO = new ClienteDAO();
        try {
            List<Cliente> clientes = clienteDAO.obtenerClientesConCuentas();

            TableView<Cliente> tablaClientes = new TableView<>(FXCollections.observableArrayList(clientes));
            tablaClientes.getStyleClass().add("main-table");

            tablaClientes.getColumns().addAll(
                    crearColumnaNumerica("ID", "id"),
                    crearColumnaTexto("Nombre", "nombre"),
                    crearColumnaTexto("Razón Social", "razonSocial"),
                    crearColumnaTexto("CUIT", "CUIT"),
                    crearColumnaTexto("Teléfono", "telefono"),
                    crearColumnaTexto("Localidad", "localidad"),
                    crearColumnaTexto("Condición", "condicion"),
                    crearColumnaTexto("Proveedor", "proveedor"),
                    crearColumnaSaldoCliente()
            );

            tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            mostrarVentana("Listado de Clientes:", "Clientes", tablaClientes);

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error al obtener los clientes.");
        }
    }

    private static void mostrarProveedores() {
        ProveedorDAO proveedorDAO = new ProveedorDAO();
        try {
            List<Proveedor> proveedores = proveedorDAO.obtenerProveedoresConCuentas();

            TableView<Proveedor> tablaProveedores = new TableView<>(FXCollections.observableArrayList(proveedores));
            tablaProveedores.getStyleClass().add("main-table");

            tablaProveedores.getColumns().addAll(
                    crearColumnaNumerica("ID", "id"),
                    crearColumnaTexto("Nombre", "nombre"),
                    crearColumnaTexto("Razón Social", "razonSocial"),
                    crearColumnaTexto("CUIT", "CUIT"),
                    crearColumnaTexto("Teléfono", "telefono"),
                    crearColumnaTexto("Localidad", "localidad"),
                    crearColumnaTexto("Domicilio", "domicilio"),
                    crearColumnaTexto("CodigoPostal", "codigoPostal"),
                    crearColumnaSaldoProveedor()
            );

            tablaProveedores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            mostrarVentana("Listado de Proveedores:", "Proveedores", tablaProveedores);

        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error al obtener los proveedores.");
        }
    }

    private static TableColumn<Cliente, Number> crearColumnaSaldoCliente() {
        TableColumn<Cliente, Number> saldoCol = new TableColumn<>("Saldo");
        saldoCol.setCellValueFactory(data -> {
            List<CuentaCorriente> cuentas = data.getValue().getCuentaCorrientes();
            if (cuentas == null || cuentas.isEmpty()) {
                return new SimpleDoubleProperty(0);
            }
            CuentaCorriente ultima = cuentas.stream()
                    .max(Comparator.comparing(CuentaCorriente::getFecha))
                    .orElse(null);
            return new SimpleDoubleProperty(ultima != null ? ultima.getSaldo() : 0);
        });
        saldoCol.setCellFactory(ImprimirController.getCurrencyFormatCellFactory());
        saldoCol.getStyleClass().add("custom-table-column");
        return saldoCol;
    }

    private static TableColumn<Proveedor, Number> crearColumnaSaldoProveedor() {
        TableColumn<Proveedor, Number> saldoCol = new TableColumn<>("Saldo");
        saldoCol.setCellValueFactory(data -> {
            List<CuentaCorriente> cuentas = data.getValue().getCuentaCorrientes();
            if (cuentas == null || cuentas.isEmpty()) {
                return new SimpleDoubleProperty(0);
            }
            CuentaCorriente ultima = cuentas.stream()
                    .max(Comparator.comparing(CuentaCorriente::getFecha))
                    .orElse(null);
            return new SimpleDoubleProperty(ultima != null ? ultima.getSaldo() : 0);
        });
        saldoCol.setCellFactory(ImprimirController.getCurrencyFormatCellFactory());
        saldoCol.getStyleClass().add("custom-table-column");
        return saldoCol;
    }

    private static <T> TableColumn<T, String> crearColumnaTexto(String titulo, String propiedad) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propiedad));
        col.getStyleClass().add("custom-table-column");
        return col;
    }

    private static <T> TableColumn<T, Number> crearColumnaNumerica(String titulo, String propiedad) {
        TableColumn<T, Number> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propiedad));
        col.getStyleClass().add("custom-table-column");
        return col;
    }

    private static void mostrarVentana(String tituloLabel, String tituloVentana, TableView<?> tabla) {
        VBox layout = new VBox(10, new Label(tituloLabel), tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        layout.setPadding(new Insets(10));

        Stage ventana = new Stage();
        ventana.setTitle(tituloVentana);
        ventana.getIcons().add(new Image(ImprimirController.class.getResourceAsStream("/icono.png")));
        Scene scene = new Scene(layout, 1200, 800);
        scene.getStylesheets().add(ImprimirController.class.getResource("/style.css").toExternalForm());
        ventana.setScene(scene);
        ventana.show();
    }

    private static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void generarPdfDeudores(Stage ownerStage, List<Cliente> clientesDeudores) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF de Clientes Deudores");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        String defaultFileName = "ClientesDeudores_" + LocalDate.now() + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4.rotate());
                document.setMargins(20, 20, 20, 20);

                PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                document.add(new Paragraph("Listado de Clientes Deudores")
                        .setFont(boldFont)
                        .setFontSize(18)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(15));

                float[] columnWidths = {0.5f, 2f, 2f, 1.5f, 1.5f, 2f, 1.5f, 1.5f, 1f};
                Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
                table.setMarginBottom(10);

                DeviceRgb headerBgColor = new DeviceRgb(52, 152, 219);
                table.addHeaderCell(new Cell().add(new Paragraph("ID").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Nombre").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Razón Social").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("CUIT").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Teléfono").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Localidad").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Condición").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Proveedor").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                table.addHeaderCell(new Cell().add(new Paragraph("Saldo").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(headerBgColor).setTextAlignment(TextAlignment.RIGHT).setPadding(5));

                for (Cliente cliente : clientesDeudores) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(cliente.getId())).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getNombre()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getRazonSocial()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getCUIT()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getTelefono()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.CENTER).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getLocalidad()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getCondicion()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));
                    table.addCell(new Cell().add(new Paragraph(cliente.getProveedor()).setFont(font).setFontSize(9)).setTextAlignment(TextAlignment.LEFT).setPadding(5));

                    double saldo = 0;
                    List<CuentaCorriente> cuentas = cliente.getCuentaCorrientes();
                    if (cuentas != null && !cuentas.isEmpty()) {
                        CuentaCorriente ultima = cuentas.stream()
                                .max(Comparator.comparing(CuentaCorriente::getFecha))
                                .orElse(null);
                        if (ultima != null) {
                            saldo = ultima.getSaldo();
                        }
                    }
                    table.addCell(new Cell().add(new Paragraph(String.format("%.2f", saldo)).setFont(boldFont).setFontSize(9)).setTextAlignment(TextAlignment.RIGHT).setPadding(5));
                }

                document.add(table);

                document.add(new Paragraph(
                        "Reporte generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " a las " +
                                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                        .setFont(font)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginTop(10));

                document.close();

                mostrarInformacion("PDF Generado Exitosamente", "El listado de deudores se ha guardado en:\n" + file.getAbsolutePath());

                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        mostrarError("Error al intentar abrir el archivo PDF. Revise la ruta: " + file.getAbsolutePath());
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mostrarError("Error: No se pudo crear el archivo PDF. Asegúrese de que no esté abierto y tenga permisos de escritura.\n" + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                mostrarError("Ocurrió un error inesperado al generar el PDF: " + e.getMessage());
            }
        }
    }

    public static <S> Callback<TableColumn<S, Number>, TableCell<S, Number>> getCurrencyFormatCellFactory() {
        return column -> new TableCell<S, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item.doubleValue()));
                }
            }
        };
    }

}