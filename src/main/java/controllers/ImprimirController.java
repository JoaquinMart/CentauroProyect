// CASI TODOS LOS METODOS DE ESTA CLASE SE ENCARGAN SOLO DE TRAER INFORMACION Y MOSTRARLA EN UNA VENTANA NUEVA
package controllers;

import dao.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import model.*;
import services.ImprimirPdfService;

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
import java.util.Comparator;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ImprimirController {

    private static ImprimirPdfService imprimirPdfService;
    static {
        try {
            imprimirPdfService = new ImprimirPdfService();
        } catch (IOException e) {
            System.err.println("Error al inicializar ImprimirPdfService: " + e.getMessage());
        }
    }

    // Método para seleccionar que "Entidad" se va a imprimir
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

    // Método para mostrar los productos
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

    // Método para mostrar los clientes deudores (saldo != 0)
    private static void mostrarClientesDeudores() {
        ClienteDAO clienteDAO = new ClienteDAO();
        try {
            List<Cliente> todosLosClientesConCuentas = clienteDAO.obtenerClientesConCuentas();
            List<Cliente> clientesDeudores = todosLosClientesConCuentas.stream()
                    .filter(cliente -> {
                        List<CuentaCorriente> cuentas = cliente.getCuentaCorrientes();
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
            Scene scene = new Scene(layout, 1200, 800);
            scene.getStylesheets().add(ImprimirController.class.getResource("/style.css").toExternalForm());
            ventana.setScene(scene);
            ventana.show();
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error al obtener los clientes deudores.");
        }
    }

    // Método para mostrar los clientes
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

    // Método para mostrar los proveedores
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

    // Método para la columna del saldo cliente
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

    // Método para la columna del saldo proveedor
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

    // Método para la asignacion de texto de cada columna
    private static <T> TableColumn<T, String> crearColumnaTexto(String titulo, String propiedad) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propiedad));
        col.getStyleClass().add("custom-table-column");
        return col;
    }

    // Método para la asignacion de texto de cada columna (que contiene un valor numerico)
    private static <T> TableColumn<T, Number> crearColumnaNumerica(String titulo, String propiedad) {
        TableColumn<T, Number> col = new TableColumn<>(titulo);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propiedad));
        col.getStyleClass().add("custom-table-column");
        return col;
    }

    // Método para mostrar la ventana (de lo que sea)
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

    // Método para mostrar los errores
    private static void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para mostrar alertas de informacion
    private static void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para generar un PDF de los clientes deudores
    public static void generarPdfDeudores(Stage ownerStage, List<Cliente> clientesDeudores) {
        // La parte "logica" de este metodo esta en el PDF service
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF de Clientes Deudores");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        String defaultFileName = "ClientesDeudores_" + LocalDate.now() + ".pdf";
        fileChooser.setInitialFileName(defaultFileName);
        File file = fileChooser.showSaveDialog(ownerStage);
        if (file != null) {
            try {
                imprimirPdfService.generateClientesDeudoresPdf(clientesDeudores, file.getAbsolutePath());
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

    // Método para que los numeros no se vean con notacion cientifica
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