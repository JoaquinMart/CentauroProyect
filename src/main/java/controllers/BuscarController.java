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

public class  BuscarController {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy"); // si usas formatter

    public void handleBotonBuscar(
        TextField buscarNombreField,
        TableView<CuentaCorriente> tablaCuentas,
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

        mostrarCuentasYResumen(nombre, tablaCuentas, resumenClienteText);

        if (!tablaCuentas.getItems().isEmpty()) {
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

        buscarNombreField.clear();
    }

    // Puedes implementar mostrarVentanaComprobantes aquí, o recibir un callback para eso.
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
            double precio = data.getValue().getPrecio();
            return new javafx.beans.property.SimpleDoubleProperty(precio / cantidad);
        });

        TableColumn<Comprobante, Double> precioCol = new TableColumn<>("Total");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tablaComprobantes.getColumns().addAll(cantidadCol, productoCol, totalCol, precioCol);
        tablaComprobantes.getItems().addAll(comprobantes);
        tablaComprobantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        double totalGeneral = comprobantes.stream()
                .mapToDouble(c -> c.getPrecio())
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

    public void mostrarCuentasYResumen(String nombre, TableView<CuentaCorriente> tablaCuentas, TextFlow resumenClienteText) {
        ClienteDAO clienteDAO = new ClienteDAO();
        ProveedorDAO proveedorDAO = new ProveedorDAO();
        CuentaCorrienteDAO cuentaDAO = new CuentaCorrienteDAO();

        Cliente cliente = clienteDAO.obtenerClientePorNombre(nombre);
        if (cliente != null) {
            List<CuentaCorriente> cuentas = cuentaDAO.obtenerMovimientosPorClienteId(cliente.getId());
            tablaCuentas.getItems().setAll(cuentas);

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
        } else {
            Proveedor proveedor = proveedorDAO.obtenerProveedorPorNombre(nombre);
            if (proveedor != null) {
                List<CuentaCorriente> cuentas = cuentaDAO.obtenerMovimientosPorProveedorId(proveedor.getId());
                tablaCuentas.getItems().setAll(cuentas);

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
            }
        }
    }

}
