package controllers;

import javafx.scene.text.Text;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BuscarController {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

    public void handleBotonBuscar(
            TextField buscarNombreField,
            TableView<CuentaCorriente> tablaCuentas, // Ahora recibimos la tabla de clientes
            TableView<CuentaCorriente> tablaProveedores, // Y la nueva tabla de proveedores
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

        // LLAMAR AL MÉTODO ACTUALIZADO
        mostrarCuentasYResumen(nombre, tablaCuentas, tablaProveedores, resumenClienteText); // Pasa ambas tablas

        // El setRowFactory debe aplicarse a la tabla que esté actualmente visible o a ambas
        // Es mejor hacerlo dentro de mostrarCuentasYResumen para asegurarse de que se aplica a la tabla correcta
        // o aplicar a ambas y que se active solo cuando la tabla esté visible.
        // Por simplicidad, lo moveremos o lo haremos específico en mostrarCuentasYResumen.
        // Por ahora, lo dejaré comentado aquí, lo ajustaremos en el método.
        /*
        if (!tablaCuentas.getItems().isEmpty()) { // Esto solo se aplica a tablaCuentas
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
        }
        */

        buscarNombreField.clear();
    }

    private void mostrarVentanaComprobantes(int movimientoId, String fecha, String persona) {
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
        List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(movimientoId);

        Stage ventana = new Stage();
        ventana.setTitle("Dia " + fecha + ", Cliente/Proveedor " + persona);

        TableView<Comprobante> tablaComprobantes = new TableView<>();

        TableColumn<Comprobante, String> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<Comprobante, String> productoCol = new TableColumn<>("Producto");
        productoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Comprobante, Number> totalCol = new TableColumn<>("Precio Unitario");
        totalCol.setCellValueFactory(data -> {
            int cantidad = data.getValue().getCantidad();
            // Asumiendo que el precio en Comprobante es el precio total del item (cantidad * precio_unitario)
            // Si precio es el precio unitario, entonces sería: return new SimpleDoubleProperty(data.getValue().getPrecio());
            double precio = data.getValue().getPrecio(); // Precio total del item
            return new javafx.beans.property.SimpleDoubleProperty(precio / cantidad);
        });

        TableColumn<Comprobante, Double> precioCol = new TableColumn<>("Total"); // Este es el total del item
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tablaComprobantes.getColumns().addAll(cantidadCol, productoCol, totalCol, precioCol);
        tablaComprobantes.getItems().addAll(comprobantes);
        tablaComprobantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        double totalGeneral = comprobantes.stream()
                .mapToDouble(c -> c.getPrecio()) // Suma el precio total de cada comprobante
                .sum();

        Label totalLabel = new Label(String.format("TOTAL: $ %.2f", totalGeneral));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        VBox layout = new VBox(10, new Label("Comprobantes:"), tablaComprobantes, totalLabel);
        layout.setPadding(new Insets(10));

        Scene escena = new Scene(layout, 800, 600);
        ventana.setScene(escena);
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.showAndWait();
    }

    public void mostrarCuentasYResumen(String nombre, TableView<CuentaCorriente> tablaCuentas, TableView<CuentaCorriente> tablaProveedores, TextFlow resumenClienteText) {
        ClienteDAO clienteDAO = new ClienteDAO();
        ProveedorDAO proveedorDAO = new ProveedorDAO();
        CuentaCorrienteDAO cuentaDAO = new CuentaCorrienteDAO();

        // Limpiar ambas tablas al inicio de la búsqueda
        tablaCuentas.getItems().clear();
        tablaProveedores.getItems().clear();

        // Ocultar ambas tablas al inicio y se mostrará la que corresponda
        tablaCuentas.setVisible(false);
        tablaCuentas.setManaged(false);
        tablaProveedores.setVisible(false);
        tablaProveedores.setManaged(false);


        Cliente cliente = clienteDAO.obtenerClientePorNombre(nombre);
        if (cliente != null) {
            List<CuentaCorriente> cuentas = cuentaDAO.obtenerMovimientosPorClienteId(cliente.getId());
            tablaCuentas.getItems().setAll(cuentas);

            // Mostrar solo la tabla de clientes
            tablaCuentas.setVisible(true);
            tablaCuentas.setManaged(true);

            Text clienteLabel = new Text("Cliente: ");
            clienteLabel.setStyle("-fx-font-weight: bold");

            Text nombreText = new Text(cliente.getNombre() + ", ");

            Text razonLabel = new Text("Razón Social: ");
            razonLabel.setStyle("-fx-font-weight: bold");

            Text razonText = new Text(cliente.getRazonSocial() + ", ");

            Text cuitLabel = new Text("CUIT: ");
            cuitLabel.setStyle("-fx-font-weight: bold");

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

                // Mostrar solo la tabla de proveedores
                tablaProveedores.setVisible(true);
                tablaProveedores.setManaged(true);

                Text proveedorLabel = new Text("Proveedor: ");
                proveedorLabel.setStyle("-fx-font-weight: bold");

                Text nombreText = new Text(proveedor.getNombre() + ", ");

                Text razonLabel = new Text("Razón Social: ");
                razonLabel.setStyle("-fx-font-weight: bold");

                Text razonText = new Text(proveedor.getRazonSocial() + ", ");

                Text cuitLabel = new Text("CUIT: ");
                cuitLabel.setStyle("-fx-font-weight: bold");

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
                // Si no se encuentra ni cliente ni proveedor
                tablaCuentas.getItems().clear(); // Asegúrate de que ambas estén vacías
                tablaProveedores.getItems().clear();
                resumenClienteText.getChildren().setAll(new Text("No se encontró cliente/proveedor con el nombre '" + nombre + "'"));
            }
        }
    }
}