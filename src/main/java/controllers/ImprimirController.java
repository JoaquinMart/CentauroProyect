package controllers;

import dao.*;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
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

    // Renombramos el método para mayor claridad, ya que ahora solo muestra deudores
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
            botonGenerarPdf.setOnAction(event -> generarPdfDeudores((Stage) tablaClientes.getScene().getWindow(), clientesDeudores));

            VBox layout = new VBox(10, new Label("Listado de Clientes Deudores:"), tablaClientes, botonGenerarPdf);
            layout.setPadding(new Insets(10));

            Stage ventana = new Stage();
            ventana.setTitle("Clientes Deudores");
            ventana.setScene(new Scene(layout, 1000, 600));
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
        return saldoCol;
    }

    private static <T> TableColumn<T, String> crearColumnaTexto(String titulo, String propiedad) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propiedad));
        return col;
    }

    private static <T> TableColumn<T, Number> crearColumnaNumerica(String titulo, String propiedad) {
        TableColumn<T, Number> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propiedad));
        return col;
    }

    private static void mostrarVentana(String tituloLabel, String tituloVentana, TableView<?> tabla) {
        VBox layout = new VBox(10, new Label(tituloLabel), tabla);
        layout.setPadding(new Insets(10));

        Stage ventana = new Stage();
        ventana.setTitle(tituloVentana);
        ventana.setScene(new Scene(layout, 1000, 600));
        ventana.show();
    }

    private static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // --- NUEVO MÉTODO: mostrarInformacion ---
    private static void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    // --- FIN NUEVO MÉTODO ---


    private static void generarPdfDeudores(Stage ownerStage, List<Cliente> clientes) {
        // Usamos FileChooser para que el usuario elija dónde guardar el PDF
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Listado de Deudores como PDF");
        fileChooser.setInitialFileName("ListadoDeudores_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(ownerStage); // Muestra el diálogo para guardar
        if (file == null) {
            return; // El usuario canceló la operación
        }

        String path = file.getAbsolutePath(); // Ruta donde se guardará el PDF

        try {
            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // --- Encabezado del PDF ---
            // Crear una fuente en negrita
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            document.add(new Paragraph("Listado de Clientes Deudores")
                    .setFont(boldFont) // Usar setFont con la fuente en negrita
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)); // Centrar título
            document.add(new Paragraph("Fecha de impresión: " + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFontSize(10));
            document.add(new Paragraph("\n")); // Salto de línea

            // --- Tabla de Contenido ---
            float[] columnWidths = {2, 5, 5, 4, 3, 4, 3, 3, 3}; // Ajusta según tus necesidades
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Añadir encabezados de la tabla con fuente en negrita
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setFont(boldFont).setTextAlignment(TextAlignment.CENTER)));
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Razón Social").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("CUIT").setFont(boldFont).setTextAlignment(TextAlignment.CENTER)));
            table.addHeaderCell(new Cell().add(new Paragraph("Teléfono").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Localidad").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Condición").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Proveedor").setFont(boldFont)));
            table.addHeaderCell(new Cell().add(new Paragraph("Saldo").setFont(boldFont).setTextAlignment(TextAlignment.RIGHT)));

            // Añadir los datos de los clientes
            for (Cliente cliente : clientes) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(cliente.getId())).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(cliente.getNombre())));
                table.addCell(new Cell().add(new Paragraph(cliente.getRazonSocial())));
                table.addCell(new Cell().add(new Paragraph(cliente.getCUIT()).setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(cliente.getTelefono())));
                table.addCell(new Cell().add(new Paragraph(cliente.getLocalidad())));
                table.addCell(new Cell().add(new Paragraph(cliente.getCondicion())));
                table.addCell(new Cell().add(new Paragraph(cliente.getProveedor())));

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
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", saldo)).setTextAlignment(TextAlignment.RIGHT)));
            }

            document.add(table);
            document.close();
            mostrarInformacion("PDF Generado Exitosamente", "El listado de deudores se ha guardado en:\n" + path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mostrarError("Error: No se pudo crear el archivo PDF. Asegúrese de que no esté abierto y tenga permisos de escritura.\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Ocurrió un error inesperado al generar el PDF: " + e.getMessage());
        }
    }
}