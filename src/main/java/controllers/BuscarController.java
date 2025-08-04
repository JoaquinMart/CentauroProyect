package controllers;

import javafx.scene.text.Text;
import main.AppController;
import model.*;
import dao.*;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BuscarController {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

    public void handleBotonBuscar(
            TextField buscarNombreField,
            TableView<CuentaCorriente> tablaCuentas,
            TableView<CuentaCorriente> tablaProveedores,
            TextFlow resumenClienteText,
            Alert alerta
    ) {
        String nombre = buscarNombreField.getText().trim();
        if (nombre.isEmpty()) {
            alerta.setTitle("Error");
            alerta.setContentText("Escribe un nombre para buscar.");
            alerta.showAndWait();
            return;
        }
        mostrarCuentasYResumen(nombre, tablaCuentas, tablaProveedores, resumenClienteText);
        buscarNombreField.clear();
    }

    private void mostrarVentanaComprobantes(int movimientoId, String fecha, String persona) {
        ComprobanteDAO comprobanteDAO;
        List<Comprobante> comprobantes;
        try {
            comprobanteDAO = new ComprobanteDAO();
            comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(movimientoId);
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error de base de datos al inicializar o acceder a los comprobantes: " + ex.getMessage());
            return;
        }

        Stage ventana = new Stage();
        ventana.setTitle("Dia " + fecha + ", Cliente/Proveedor " + persona);

        TableView<Comprobante> tablaComprobantes = new TableView<>();

        TableColumn<Comprobante, String> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        cantidadCol.getStyleClass().add("custom-table-column");

        TableColumn<Comprobante, String> productoCol = new TableColumn<>("Producto");
        productoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        productoCol.getStyleClass().add("custom-table-column");

        TableColumn<Comprobante, Number> totalCol = new TableColumn<>("Precio Unitario");
        totalCol.setCellValueFactory(data -> {
            int cantidad = data.getValue().getCantidad();
            double precio = data.getValue().getPrecio();
            // Asegurarse de que cantidad no sea cero para evitar error
            return new javafx.beans.property.SimpleDoubleProperty(cantidad != 0 ? precio / cantidad : 0);
        });
        totalCol.getStyleClass().add("custom-table-column");

        TableColumn<Comprobante, Double> precioCol = new TableColumn<>("Total");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));
        precioCol.setCellFactory(AppController.getCurrencyFormatCellFactory());
        precioCol.getStyleClass().add("custom-table-column");

        tablaComprobantes.getColumns().addAll(cantidadCol, productoCol, totalCol, precioCol);
        tablaComprobantes.getItems().addAll(comprobantes);
        tablaComprobantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaComprobantes.getStyleClass().add("main-table");

        double totalGeneral = comprobantes.stream()
                .mapToDouble(c -> c.getPrecio()) // Suma el precio total de cada comprobante
                .sum();

        Label totalLabel = new Label(String.format("TOTAL: $ %.2f", totalGeneral));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        VBox layout = new VBox(10, new Label("Comprobantes:"), tablaComprobantes, totalLabel);
        layout.setPadding(new Insets(10));

        Scene escena = new Scene(layout, 900, 600);
        escena.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ventana.setScene(escena);
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.showAndWait();
    }

    public void mostrarCuentasYResumen(String nombre, TableView<CuentaCorriente> tablaCuentas, TableView<CuentaCorriente> tablaProveedores, TextFlow resumenClienteText) {
        ClienteDAO clienteDAO;
        ProveedorDAO proveedorDAO;
        CuentaCorrienteDAO cuentaDAO;

        try {
            clienteDAO = new ClienteDAO();
            proveedorDAO = new ProveedorDAO();
            cuentaDAO = new CuentaCorrienteDAO();
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error de base de datos al inicializar los objetos de acceso a datos: " + ex.getMessage());
            return;
        }

        // Limpiar ambas tablas al inicio de la búsqueda
        tablaCuentas.getItems().clear();
        tablaProveedores.getItems().clear();

        // Ocultar ambas tablas al inicio y se mostrará la que corresponda
        tablaCuentas.setVisible(false);
        tablaCuentas.setManaged(false);
        tablaProveedores.setVisible(false);
        tablaProveedores.setManaged(false);

        try {
            Cliente cliente = clienteDAO.obtenerClientePorNombre(nombre);
            if (cliente != null) {
                List<CuentaCorriente> cuentas = cuentaDAO.obtenerMovimientosPorClienteId(cliente.getId());
                tablaCuentas.getItems().setAll(cuentas);
                tablaCuentas.setVisible(true);
                tablaCuentas.setManaged(true);

                Text clienteLabel = new Text("Cliente: ");clienteLabel.setStyle("-fx-font-weight: bold");
                Text nombreText = new Text(cliente.getNombre() + ", ");
                Text razonLabel = new Text("Razón Social: ");razonLabel.setStyle("-fx-font-weight: bold");
                Text razonText = new Text(cliente.getRazonSocial() + ", ");
                Text cuitLabel = new Text("CUIT: ");cuitLabel.setStyle("-fx-font-weight: bold");
                Text cuitText = new Text(cliente.getCUIT());

                resumenClienteText.getChildren().setAll(
                        clienteLabel, nombreText,
                        razonLabel, razonText,
                        cuitLabel, cuitText
                );

                // Añadir el manejador de doble clic para la tabla de clientes
                tablaCuentas.setRowFactory(tv -> {
                    TableRow<CuentaCorriente> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && (!row.isEmpty())) {
                            CuentaCorriente cuentaSeleccionada = row.getItem();
                            mostrarVentanaComprobantes(cuentaSeleccionada.getId(), cuentaSeleccionada.getFecha().format(formatter), nombre);
                        }
                    });
                    return row;
                });
            } else {
                Proveedor proveedor = proveedorDAO.obtenerProveedorPorNombre(nombre);
                if (proveedor != null) {
                    List<CuentaCorriente> cuentas = cuentaDAO.obtenerMovimientosPorProveedorId(proveedor.getId());
                    tablaProveedores.getItems().setAll(cuentas);
                    tablaProveedores.setVisible(true);
                    tablaProveedores.setManaged(true);
                    Text proveedorLabel = new Text("Proveedor: ");proveedorLabel.setStyle("-fx-font-weight: bold");
                    Text nombreText = new Text(proveedor.getNombre() + ", ");
                    Text razonLabel = new Text("Razón Social: ");razonLabel.setStyle("-fx-font-weight: bold");
                    Text razonText = new Text(proveedor.getRazonSocial() + ", ");
                    Text cuitLabel = new Text("CUIT: ");cuitLabel.setStyle("-fx-font-weight: bold");
                    Text cuitText = new Text(proveedor.getCUIT());

                    resumenClienteText.getChildren().setAll(
                            proveedorLabel, nombreText,
                            razonLabel, razonText,
                            cuitLabel, cuitText
                    );

                    // Añadir el manejador de doble clic para la tabla de proveedores
                    tablaProveedores.setRowFactory(tv -> {
                        TableRow<CuentaCorriente> row = new TableRow<>();
                        row.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2 && (!row.isEmpty())) {
                                CuentaCorriente cuentaSeleccionada = row.getItem();
                                mostrarVentanaComprobantes(cuentaSeleccionada.getId(), cuentaSeleccionada.getFecha().format(formatter), nombre);
                            }
                        });
                        return row;
                    });
                } else {
                    tablaCuentas.getItems().clear();
                    tablaProveedores.getItems().clear();
                    resumenClienteText.getChildren().setAll(new Text("No se encontró cliente/proveedor con el nombre '" + nombre + "'"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error de base de datos al buscar cliente/proveedor o sus cuentas: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}