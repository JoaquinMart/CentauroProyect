package controllers;

import javafx.scene.image.Image;
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
import java.util.ArrayList;
import java.util.List;

public class BuscarController {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

    // Método para inicializar el controlador con la lógica de autocompletado
    public void setup(
            TextField buscarNombreField,
            TableView<SugerenciaBusqueda> tablaSugerencias,
            TableView<CuentaCorriente> tablaCuentas,
            TableView<CuentaCorriente> tablaProveedores,
            TextFlow resumenClienteText
    ) {
        // Listener que se activa al escribir
        buscarNombreField.textProperty().addListener((observable, oldValue, newValue) -> {
            mostrarSugerencias(newValue, tablaSugerencias, tablaCuentas, tablaProveedores, resumenClienteText);
        });

        // Listener para la tecla ENTER en el TextField
        buscarNombreField.setOnKeyReleased(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleBotonBuscar(tablaSugerencias, tablaCuentas, tablaProveedores, resumenClienteText);
            }
        });
    }

    // Método que se llama al escribir en el TextField
    private void mostrarSugerencias(
            String nombre,
            TableView<SugerenciaBusqueda> tablaSugerencias,
            TableView<CuentaCorriente> tablaCuentas,
            TableView<CuentaCorriente> tablaProveedores,
            TextFlow resumenClienteText
    ) {
        try {
            ClienteDAO clienteDAO = new ClienteDAO();
            ProveedorDAO proveedorDAO = new ProveedorDAO();

            tablaSugerencias.getItems().clear();
            tablaSugerencias.getColumns().clear();
            tablaCuentas.setVisible(false);
            tablaProveedores.setVisible(false);
            resumenClienteText.getChildren().clear();

            if (nombre.isEmpty()) {
                tablaSugerencias.setVisible(false);
                tablaSugerencias.setManaged(false);
                resumenClienteText.getChildren().add(new Text("Seleccioná un cliente/proveedor..."));
                return;
            }

            List<Cliente> clientesCoincidentes = clienteDAO.obtenerClientesPorNombreParcial(nombre);
            List<Proveedor> proveedoresCoincidentes = proveedorDAO.obtenerProveedoresPorNombreParcial(nombre);

            List<SugerenciaBusqueda> sugerencias = new ArrayList<>();
            clientesCoincidentes.forEach(c -> sugerencias.add(new SugerenciaBusqueda(c)));
            proveedoresCoincidentes.forEach(p -> sugerencias.add(new SugerenciaBusqueda(p)));

            if (!sugerencias.isEmpty()) {
                tablaSugerencias.setVisible(true);
                tablaSugerencias.setManaged(true);
                tablaSugerencias.getItems().setAll(sugerencias);

                TableColumn<SugerenciaBusqueda, String> tipoCol = new TableColumn<>("Tipo");
                tipoCol.setCellValueFactory(new PropertyValueFactory<>("tipo"));
                TableColumn<SugerenciaBusqueda, String> nombreCol = new TableColumn<>("Nombre");
                nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
                TableColumn<SugerenciaBusqueda, String> razonSocialCol = new TableColumn<>("Razón Social");
                razonSocialCol.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));

                tablaSugerencias.getColumns().addAll(tipoCol, nombreCol, razonSocialCol);
            } else {
                tablaSugerencias.setVisible(false);
                resumenClienteText.getChildren().add(new Text("No se encontraron coincidencias..."));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            mostrarError("Error al buscar sugerencias.");
        }
    }

    // Método que se llama al presionar el botón "Buscar"
    public void handleBotonBuscar(
            TableView<SugerenciaBusqueda> tablaSugerencias,
            TableView<CuentaCorriente> tablaCuentas,
            TableView<CuentaCorriente> tablaProveedores,
            TextFlow resumenClienteText
    ) {
        SugerenciaBusqueda seleccion = tablaSugerencias.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            try {
                // Aquí usamos el método principal para cargar las cuentas
                mostrarCuentasYResumen(seleccion.getNombre(), tablaSugerencias, tablaCuentas, tablaProveedores, resumenClienteText);
            } catch (SQLException ex) {
                ex.printStackTrace();
                mostrarError("Error al cargar la cuenta corriente.");
            }
        } else {
            mostrarError("Selecciona un cliente/proveedor de la lista.");
        }
    }

    // Método que muestra las cuentas y resumen del cliente o proveedor seleccionado
    public void mostrarCuentasYResumen(
            String nombre,
            TableView<SugerenciaBusqueda> tablaSugerencias,
            TableView<CuentaCorriente> tablaCuentas,
            TableView<CuentaCorriente> tablaProveedores,
            TextFlow resumenClienteText
    ) throws SQLException {
        tablaSugerencias.setVisible(false);
        tablaSugerencias.setManaged(false);

        tablaCuentas.getItems().clear();
        tablaProveedores.getItems().clear();
        resumenClienteText.getChildren().clear();

        ClienteDAO clienteDAO = new ClienteDAO();
        ProveedorDAO proveedorDAO = new ProveedorDAO();
        CuentaCorrienteDAO cuentaDAO = new CuentaCorrienteDAO();

        Cliente cliente = clienteDAO.obtenerClientePorNombre(nombre);
        if (cliente != null) {
            List<CuentaCorriente> cuentas = cuentaDAO.obtenerMovimientosPorClienteId(cliente.getId());
            tablaCuentas.getItems().setAll(cuentas);
            tablaCuentas.setVisible(true);
            tablaCuentas.setManaged(true);

            Text clienteLabel = new Text("Cliente: ");
            clienteLabel.setStyle("-fx-font-weight: bold");
            Text nombreText = new Text(cliente.getNombre() + ".");
            resumenClienteText.getChildren().setAll(clienteLabel, nombreText);
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
                Text proveedorLabel = new Text("Proveedor: ");
                proveedorLabel.setStyle("-fx-font-weight: bold");
                Text nombreText = new Text(proveedor.getNombre() + ", ");
                resumenClienteText.getChildren().setAll(proveedorLabel, nombreText);
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
                resumenClienteText.getChildren().setAll(new Text("No se encontró cliente/proveedor con el nombre '" + nombre + "'"));
            }
        }
    }

    // Método que muestra los comprobantes de una cuenta corriente seleccionada
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
        ventana.getIcons().add(new Image(ImprimirController.class.getResourceAsStream("/icono.png")));
        escena.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        ventana.setScene(escena);
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.showAndWait();
    }

    // Método que muestra las cuentas y resumen del cliente o proveedor seleccionado (para sugerencias)
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

    // Método para simplificar los mensajes de error
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}