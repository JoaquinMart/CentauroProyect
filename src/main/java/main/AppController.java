package main;

import dao.ProductoDAO;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.*;

import controllers.*;
import model.*;
import util.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppController {
    private ProductoDAO productoDAO;
    private Label nombreProductoPreviewLabel;
    private Button botonEjecutar;

    public void init(Stage stage) {
        ConexionMySQL.conectar();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        TextFlow resumenClienteText = new TextFlow();
        resumenClienteText.setMaxWidth(Double.MAX_VALUE);
        resumenClienteText.getChildren().add(new Text("Seleccioná un cliente/proveedor..."));

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        Alert advertencia = new Alert(Alert.AlertType.WARNING);

        stage.getIcons().add(new Image("file:src/main/resources/icono.jpg"));

        ComboBox<String> tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Cliente", "Proveedor", "Cuenta Corriente", "Remito", "Producto");
        tipoCombo.setPromptText("Tipo (Cliente)");

        ComboBox<String> accionCombo = new ComboBox<>();
        accionCombo.getItems().addAll("Alta", "Baja", "Modificación");
        accionCombo.setPromptText("Accion");

        ComboBox<String> imprimirCombo = new ComboBox<>();
        imprimirCombo.getItems().addAll("Clientes", "Proveedores", "Productos", "Deudores");
        imprimirCombo.setValue("Clientes");
        imprimirCombo.setPromptText("Listar");

        // Campos compartidos
        TextField nombreField = new TextField(); nombreField.setPromptText("Nombre");
        TextField razonField = new TextField(); razonField.setPromptText("Razon social");
        TextField domicilioField = new TextField(); domicilioField.setPromptText("Domicilio");
        TextField localidadField = new TextField(); localidadField.setPromptText("Localidad");
        TextField codigoPostalField = new TextField(); codigoPostalField.setPromptText("Codigo Postal");
        TextField telefonoField = new TextField(); telefonoField.setPromptText("Teléfono");
        TextField cuitField = new TextField(); cuitField.setPromptText("CUIT");

        // Campos Cliente
        TextField condicionField = new TextField(); condicionField.setPromptText("Condicion (Cliente)");
        TextField altaField = new TextField(); altaField.setPromptText("Fecha de alta (Cliente)");
        TextField proveedorField = new TextField(); proveedorField.setPromptText("Observaciones (Cliente)");

        // Campos Proveedor
        TextField categoriaField = new TextField(); categoriaField.setPromptText("Categoria (Proveedor)");
        TextField contactoField = new TextField(); contactoField.setPromptText("Contacto (Proveedor)");

        // Campos Cuenta Corriente
        TextField nombreCuentaField = new TextField(); nombreCuentaField.setPromptText("Nombre de Cliente / Proveedor");
        TextField fechaField = new TextField(); fechaField.setPromptText("Fecha (Cuenta Corriente)");
        TextField comprobanteField = new TextField(); comprobanteField.setPromptText("Numero Remito (Cuenta Corriente)");
        TextField tipoField = new TextField(); tipoField.setPromptText("Tipo (Cuenta Corriente)");
        TextField ventaField = new TextField(); ventaField.setPromptText("Venta (Cuenta Corriente)");
        TextField montoField = new TextField(); montoField.setPromptText("Pago (Cuenta Corriente)");
        TextField saldoField = new TextField(); saldoField.setPromptText("Saldo (Cuenta Corriente)");
        TextField observacionField = new TextField(); observacionField.setPromptText("Observacion (Cuenta Corriente)");

        // Campor Comprobante
        TextField remitoField = new TextField(); remitoField.setPromptText("Numero Remito");
        TextField codProdu = new TextField(); codProdu.setPromptText("Codigo");
        TextField cantidadField = new TextField(); cantidadField.setPromptText("Cantidad");

        // Campor Producto
        TextField codigoField = new TextField(); codigoField.setPromptText("Codigo del producto");
        TextField nomProduField = new TextField(); nomProduField.setPromptText("Producto");
        TextField preioUnitarioField = new TextField(); preioUnitarioField.setPromptText("Precio Unitario");

        List<TextField> textosProveedor = List.of(
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField, cuitField,
                categoriaField, contactoField
        );

        List<TextField> textosClientes = List.of(
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField, cuitField,
                condicionField, altaField, proveedorField
        );

        List<TextField> textosCuentaCorriente = List.of(
                nombreCuentaField, fechaField, comprobanteField, tipoField, ventaField, montoField, observacionField
        );

        List<TextField> textosComprobante = List.of(
                remitoField, codProdu, cantidadField
        );

        List<TextField> textosProductos = List.of(
                codigoField, nomProduField, preioUnitarioField
        );

        TableView<CuentaCorriente> tablaCuentas = new TableView<>();

        TableColumn<CuentaCorriente, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(data -> {
            LocalDate fecha = data.getValue().getFecha();
            String fechaStr = (fecha != null) ? fecha.format(formatter) : "";
            return new javafx.beans.property.SimpleStringProperty(fechaStr);
        });
        fechaCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, String> numCol = new TableColumn<>("N° Remito");
        numCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getComprobante()));
        numCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, String> tipoCol = new TableColumn<>("Tipo");
        tipoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTipo()));
        tipoCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, Number> ventaCol = new TableColumn<>("Venta");
        ventaCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getVenta()));
        ventaCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, Number> montoCol = new TableColumn<>("Pago");
        montoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMonto()));
        montoCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, Number> saldoCol = new TableColumn<>("Saldo");
        saldoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getSaldo()));
        saldoCol.getStyleClass().add("custom-table-column");

        TableColumn<CuentaCorriente, String> obsCol = new TableColumn<>("Observación");
        obsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getObservacion()));
        obsCol.setCellFactory(tc -> {
            TableCell<CuentaCorriente, String> cell = new TableCell<>() {
                private final Text text = new Text();
                {
                    text.wrappingWidthProperty().bind(obsCol.widthProperty().subtract(10));
                    setGraphic(text);
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText("");
                    } else {
                        text.setText(item);
                    }
                }
            };
            return cell;
        });
        obsCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.2));
        obsCol.getStyleClass().add("custom-table-column"); // <--- AÑADE ESTO

        tablaCuentas.getColumns().addAll(fechaCol, numCol, tipoCol, ventaCol, montoCol, saldoCol, obsCol);
        tablaCuentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//
//        TableColumn<Comprobante, Number> cantidadCol = new TableColumn<>("Cantidad");
//        cantidadCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCantidad()));
//
//        TableColumn<Comprobante, String> nombreCol = new TableColumn<>("Nombre");
//        nombreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
//
//        TableColumn<Comprobante, Number> precioCol = new TableColumn<>("Precio");
//        precioCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrecio()));

        // Botones
        botonEjecutar = new Button("Ejecutar");
        TextField buscarNombreField = new TextField();
        buscarNombreField.setPromptText("Buscar por nombre");
        Button botonBuscar = new Button("Buscar");
        Button botonImprimir = new Button("Imprimir");

        TextField[] textFields = {
                razonField, domicilioField, localidadField,
                codigoPostalField, telefonoField, cuitField, nombreField,
                condicionField, altaField, proveedorField, categoriaField,
                contactoField, nombreCuentaField, fechaField, comprobanteField,
                tipoField, ventaField, montoField, saldoField, observacionField,
                remitoField, codProdu, cantidadField,
                codigoField, nomProduField, preioUnitarioField
        };

        for (TextField tf : textFields) {
            tf.setVisible(false);
            tf.setManaged(false);
            tf.getStyleClass().add("text-field");
        }

        // --- INICIALIZACIÓN DE DAO Y LABEL MOVIDA AQUÍ ---
        productoDAO = new ProductoDAO();
        nombreProductoPreviewLabel = new Label(" ");
        nombreProductoPreviewLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        // --- Formulario de campos normales ---
        VBox camposNormales = new VBox(10,
                nombreField, razonField, domicilioField,
                localidadField, codigoPostalField, telefonoField, cuitField,
                condicionField, altaField, proveedorField, categoriaField,
                contactoField
        );
        VBox.setVgrow(camposNormales, Priority.ALWAYS);

        // --- Campos de cuenta corriente ---
        VBox camposCuentaCorriente = new VBox(10,
                nombreCuentaField, fechaField, comprobanteField, tipoField,
                ventaField, montoField, observacionField
        );
        VBox.setVgrow(camposCuentaCorriente, Priority.ALWAYS);

        // --- Campos de cuenta corriente ---
        VBox camposComprobante = new VBox(10,
                remitoField, codProdu, nombreProductoPreviewLabel, cantidadField
        );
        VBox.setVgrow(camposComprobante, Priority.ALWAYS);

        // --- Campos de productos ---
        VBox camposProductos = new VBox(10,
                codigoField, nomProduField, preioUnitarioField
        );
        VBox.setVgrow(camposProductos, Priority.ALWAYS);

        // --- Listener para el campo codProdu ---
        codProdu.textProperty().addListener((observable, oldValue, newValue) -> {
            // Limpiar el label si el campo está vacío
            if (newValue.isEmpty()) {
                nombreProductoPreviewLabel.setText(" ");
                return;
            }
            try {
                // Llama al DAO para buscar el producto por el código (String)
                Producto productoEncontrado = productoDAO.buscarProductoPorCodigo(newValue);

                if (productoEncontrado != null) {
                    nombreProductoPreviewLabel.setText(productoEncontrado.getNombre());
                    // Opcional: auto-rellenar el campo 'produField' con el nombre del producto
                    // produField.setText(productoEncontrado.getNombre());
                } else {
                    nombreProductoPreviewLabel.setText("Producto no encontrado");
                }
            } catch (SQLException e) {
                System.err.println("Error de base de datos al buscar producto por código: " + e.getMessage());
                nombreProductoPreviewLabel.setText("Error de DB"); // Mensaje más genérico para el usuario
                // Puedes usar un método para mostrar un error más detallado si lo deseas:
                // mostrarError("Error al buscar producto: " + e.getMessage());
            }
        });
        // -----------------------------------------------------------------


        tipoCombo.setOnAction(e -> actualizarCamposFormulario(
                tipoCombo, accionCombo,
                camposNormales, camposCuentaCorriente, camposComprobante, camposProductos,
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField,
                cuitField, condicionField, altaField, proveedorField, categoriaField, contactoField,
                nombreCuentaField, fechaField, comprobanteField, tipoField, ventaField, montoField, saldoField,
                observacionField, cantidadField, remitoField, codProdu, // Llamada con codProdu
                textosClientes, textosProveedor, textosCuentaCorriente, textosComprobante, textosProductos,
                codigoField, nomProduField, preioUnitarioField
        ));

        accionCombo.setOnAction(e -> actualizarCamposFormulario(
                tipoCombo, accionCombo,
                camposNormales, camposCuentaCorriente, camposComprobante, camposProductos,
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField,
                cuitField, condicionField, altaField, proveedorField, categoriaField, contactoField,
                nombreCuentaField, fechaField, comprobanteField, tipoField, ventaField, montoField, saldoField,
                observacionField, cantidadField, remitoField, codProdu, // Llamada con codProdu
                textosClientes, textosProveedor, textosCuentaCorriente, textosComprobante, textosProductos,
                codigoField, nomProduField, preioUnitarioField
        ));

        // Llamada inicial
        actualizarCamposFormulario(
                tipoCombo, accionCombo,
                camposNormales, camposCuentaCorriente, camposComprobante, camposProductos,
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField,
                cuitField, condicionField, altaField, proveedorField, categoriaField, contactoField,
                nombreCuentaField, fechaField, comprobanteField, tipoField, ventaField, montoField, saldoField,
                observacionField, cantidadField, remitoField, codProdu,
                textosClientes, textosProveedor, textosCuentaCorriente, textosComprobante, textosProductos,
                codigoField, nomProduField, preioUnitarioField);

        EjecutarController controlador = new EjecutarController(formatter, alerta, info, advertencia);
        botonEjecutar.setOnAction(e -> {
            controlador.handleBotonEjecutar(
                    tipoCombo, accionCombo,
                    nombreField, razonField, domicilioField, localidadField, codigoPostalField,
                    telefonoField, cuitField, condicionField, altaField, proveedorField,
                    categoriaField, contactoField, codigoField, nomProduField, preioUnitarioField,
                    nombreCuentaField, fechaField, comprobanteField, tipoField, ventaField, montoField,
                    observacionField, cantidadField, remitoField, codProdu,
                    textFields, tablaCuentas, resumenClienteText
            );
        });

        BuscarController controladorBuscar = new BuscarController();
        botonBuscar.setOnAction(e -> {
            controladorBuscar.handleBotonBuscar(buscarNombreField, tablaCuentas, resumenClienteText, alerta);
        });

        botonImprimir.setOnAction(e -> ImprimirController.imprimirSeleccion(imprimirCombo));

        double anchoComun = 300;

        // --- Fila 1: tipo y acción ---
        HBox filaCombos1 = new HBox(tipoCombo, accionCombo);
        filaCombos1.getStyleClass().add("combo-row");
        tipoCombo.setPrefWidth(anchoComun);
        tipoCombo.getStyleClass().add("common-combo");
        accionCombo.setPrefWidth(anchoComun);
        accionCombo.getStyleClass().add("common-combo");
        filaCombos1.setAlignment(Pos.CENTER_LEFT);

        // --- Fila para buscar y botón (AHORA SE USARÁ EN LA SECCIÓN DE TABLAS) ---
        HBox filaCombos2 = new HBox(10, buscarNombreField, botonBuscar, resumenClienteText);
        filaCombos2.getStyleClass().add("combo-row");
        buscarNombreField.setPrefWidth(anchoComun);
        buscarNombreField.getStyleClass().add("search-field");
        botonBuscar.setPrefWidth(anchoComun);
        botonBuscar.getStyleClass().add("search-button");
        filaCombos2.setAlignment(Pos.CENTER_LEFT);

        // --- Contenedor vertical para los combos ---
        VBox combosBox = new VBox(filaCombos1);
        combosBox.getStyleClass().add("combos-container");
        combosBox.setAlignment(Pos.CENTER_LEFT);

        // Nueva fila para botonEjecutar (para que esté solo en su fila)
        HBox filaBotonEjecutar = new HBox();
        filaBotonEjecutar.getChildren().add(botonEjecutar);
        filaBotonEjecutar.setAlignment(Pos.CENTER_LEFT);
        double anchoDeseadoEjecutar = (anchoComun * 2) + 15;
        botonEjecutar.setPrefWidth(anchoDeseadoEjecutar);
        botonEjecutar.getStyleClass().add("action-button");
        HBox.setHgrow(botonEjecutar, Priority.ALWAYS);
        // filaBotonEjecutar.getStyleClass().add("single-button-row");

        // --- Botones ---
        HBox filaImprimir = new HBox(10, imprimirCombo, botonImprimir);
        filaImprimir.getStyleClass().add("print-buttons-row");
        imprimirCombo.setPrefWidth(anchoComun);
        imprimirCombo.getStyleClass().add("print-combo");
        botonImprimir.setPrefWidth(anchoComun);
        botonImprimir.getStyleClass().add("print-button");
        filaImprimir.setAlignment(Pos.CENTER_LEFT);

        // --- Contenedor de los botones (AHORA ES UN VBox que contendrá las HBoxes de botones) ---
        VBox botonesBox = new VBox(10); // Spacing de 10px entre las HBox internas (filas)
        botonesBox.getStyleClass().add("buttons-container"); // Mantén esta clase para el fondo/padding del contenedor

        // Añadir las filas de botones al VBox en el orden deseado
        botonesBox.getChildren().addAll(filaBotonEjecutar, filaImprimir);

        // Establecer alineación para el VBox de botones
        botonesBox.setAlignment(Pos.CENTER_LEFT);

        // --- Campos dinámicos (cuenta corriente / normal / comprobante) ---
        StackPane camposBox = new StackPane(camposCuentaCorriente, camposNormales, camposComprobante, camposProductos);
        camposBox.setId("dynamic-fields-stack"); // Un ID ya que es un StackPane único con campos dinámicos
        VBox.setVgrow(camposBox, Priority.ALWAYS);
        camposBox.setMaxHeight(Double.MAX_VALUE);

        // --- Formulario (IZQUIERDA) ---
        VBox formulario = new VBox(combosBox, botonesBox, camposBox);
        formulario.setId("main-form"); // ID para el formulario principal
        formulario.setMaxWidth(Double.MAX_VALUE);
        formulario.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(formulario, Priority.NEVER);
        formulario.setPrefWidth(500);
        HBox.setHgrow(formulario, Priority.ALWAYS);

        // --- Tablas (DERECHA) ---
        tablaCuentas.setMaxHeight(Double.MAX_VALUE);
        tablaCuentas.getStyleClass().add("main-table"); // Clase para la tabla principal
        VBox.setVgrow(tablaCuentas, Priority.ALWAYS);

        VBox tablasBox = new VBox(10, filaCombos2, tablaCuentas);
        tablasBox.getStyleClass().add("tables-container"); // Contenedor de tablas
        tablasBox.setMaxWidth(Double.MAX_VALUE);
        tablasBox.setPrefWidth(1400);
        VBox.setVgrow(tablasBox, Priority.ALWAYS);
        HBox.setHgrow(tablasBox, Priority.ALWAYS);
        resumenClienteText.getStyleClass().add("client-summary-text"); // Clase para el texto de resumen

        // --- Contenedor HBox para ambas mitades ---
        HBox formularioConTablas = new HBox(formulario, tablasBox);
        formularioConTablas.getStyleClass().add("main-content-area"); // Clase para el área de contenido principal
        formularioConTablas.setMaxHeight(Double.MAX_VALUE);
        formularioConTablas.setPrefHeight(Double.MAX_VALUE);
        HBox.setHgrow(formulario, Priority.ALWAYS);
        HBox.setHgrow(tablasBox, Priority.ALWAYS);

        // --- Layout principal ---
        BorderPane root = new BorderPane();
        root.setId("root-pane"); // ID para el panel raíz
        root.setCenter(formularioConTablas);
        BorderPane.setAlignment(formularioConTablas, Pos.CENTER);

        // --- Escena ---
        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Control de clientes y proveedores");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        // --- Watcher del CSS ---
        new CSSFileWatcher("src/main/resources/style.css").start();
    }

    private void actualizarCamposFormulario(
            ComboBox<String> tipoCombo, ComboBox<String> accionCombo,
            VBox camposNormales, VBox camposCuentaCorriente, VBox camposComprobante, VBox camposProductos,
            TextField nombreField, TextField razonField, TextField domicilioField, TextField localidadField,
            TextField codigoPostalField, TextField telefonoField, TextField cuitField, TextField condicionField,
            TextField altaField, TextField proveedorField, TextField categoriaField, TextField contactoField,
            TextField nombreCuentaField, TextField fechaField, TextField comprobanteField, TextField tipoField,
            TextField ventaField, TextField montoField, TextField saldoField, TextField observacionField,
            TextField cantidadField, TextField remitoField, TextField codProdu, List<TextField> textosClientes,
            List<TextField> textosProveedores, List<TextField> textosCuentaCorriente, List<TextField> textosComprobante,
            List<TextField> textosProductos, TextField codigoField, TextField nomProduField, TextField preioUnitarioField)
    {

        String tipo = tipoCombo.getValue();
        String accion = accionCombo.getValue();

        // Ocultar todo inicialmente
        camposNormales.setVisible(false);
        camposNormales.setManaged(false);
        camposCuentaCorriente.setVisible(false);
        camposCuentaCorriente.setManaged(false);
        camposComprobante.setVisible(false);
        camposComprobante.setManaged(false);
        camposProductos.setVisible(false);
        camposProductos.setManaged(false);

        if (nombreProductoPreviewLabel != null) {
            nombreProductoPreviewLabel.setVisible(false);
            nombreProductoPreviewLabel.setManaged(false);
        }

        if ("Cuenta Corriente".equals(tipo)) {
            // Mostrar campos de cuenta corriente completo
            camposCuentaCorriente.setVisible(true);
            camposCuentaCorriente.setManaged(true);

            // Asegurar todos visibles dentro
            for (Node nodo : camposCuentaCorriente.getChildren()) {
                nodo.setVisible(true);
                nodo.setManaged(true);
            }
            asignarEnterEntreCampos(textosCuentaCorriente);

        } else if ("Remito".equals(tipo)) {
            // Mostrar campos de cuenta corriente completo
            camposComprobante.setVisible(true);
            camposComprobante.setManaged(true);

            // Asegurar todos visibles dentro
            for (Node nodo : camposComprobante.getChildren()) {
                nodo.setVisible(true);
                nodo.setManaged(true);
            }
            if (nombreProductoPreviewLabel != null) {
                nombreProductoPreviewLabel.setVisible(true);
                nombreProductoPreviewLabel.setManaged(true);
            }
            asignarEnterEntreCampos(textosComprobante);

        } else if ("Producto".equals(tipo)) {
            // Mostrar campos de cuenta corriente completo
            camposProductos.setVisible(true);
            camposProductos.setManaged(true);

            // Asegurar todos visibles dentro
            for (Node nodo : camposProductos.getChildren()) {
                nodo.setVisible(true);
                nodo.setManaged(true);
            }
            asignarEnterEntreCampos(textosProductos);

        } else if ("Cliente".equals(tipo) || "Proveedor".equals(tipo)) {
            // Mostrar campos normales
            camposNormales.setVisible(true);
            camposNormales.setManaged(true);

            // Primero ocultar todos los campos individuales
            TextField[] todosCamposNormales = {
                    nombreField, razonField, domicilioField, localidadField,
                    codigoPostalField, telefonoField, cuitField, condicionField,
                    altaField, proveedorField, categoriaField, contactoField
            };
            for (TextField tf : todosCamposNormales) {
                tf.setVisible(false);
                tf.setManaged(false);
            }

            // Mostrar solo los que correspondan
            if ("Cliente".equals(tipo)) {
                if ("Baja".equals(accion)) {
                    nombreField.setVisible(true);
                    nombreField.setManaged(true);
                } else {
                    nombreField.setVisible(true);
                    razonField.setVisible(true);
                    domicilioField.setVisible(true);
                    localidadField.setVisible(true);
                    codigoPostalField.setVisible(true);
                    telefonoField.setVisible(true);
                    cuitField.setVisible(true);
                    nombreField.setManaged(true);
                    razonField.setManaged(true);
                    domicilioField.setManaged(true);
                    localidadField.setManaged(true);
                    codigoPostalField.setManaged(true);
                    telefonoField.setManaged(true);
                    cuitField.setManaged(true);
                }
                if ("Alta".equals(accion) || "Modificación".equals(accion)) {
                    condicionField.setVisible(true);
                    altaField.setVisible(true);
                    proveedorField.setVisible(true);
                    condicionField.setManaged(true);
                    altaField.setManaged(true);
                    proveedorField.setManaged(true);
                }
                asignarEnterEntreCampos(textosClientes);
            } else if ("Proveedor".equals(tipo)) {
                if ("Baja".equals(accion)) {
                    nombreField.setVisible(true);
                    nombreField.setManaged(true);
                } else {
                    nombreField.setVisible(true);
                    razonField.setVisible(true);
                    domicilioField.setVisible(true);
                    localidadField.setVisible(true);
                    codigoPostalField.setVisible(true);
                    telefonoField.setVisible(true);
                    cuitField.setVisible(true);
                    nombreField.setManaged(true);
                    razonField.setManaged(true);
                    domicilioField.setManaged(true);
                    localidadField.setManaged(true);
                    codigoPostalField.setManaged(true);
                    telefonoField.setManaged(true);
                    cuitField.setManaged(true);
                }
                if ("Alta".equals(accion) || "Modificación".equals(accion)) {
                    categoriaField.setVisible(true);
                    contactoField.setVisible(true);
                    categoriaField.setManaged(true);
                    contactoField.setManaged(true);
                }
                asignarEnterEntreCampos(textosProveedores);
            }
        }
    }

    private void asignarEnterEntreCampos(List<TextField> campos) {
        for (int i = 0; i < campos.size(); i++) {
            TextField campoActual = campos.get(i);
            int siguienteIndex = i + 1;

            campoActual.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    if (siguienteIndex >= campos.size()) {
                        Platform.runLater(() -> {
                            if (this.botonEjecutar != null && this.botonEjecutar.getOnAction() != null) {
                                this.botonEjecutar.fire();
                            }
                        });
                    } else {
                        campos.get(siguienteIndex).requestFocus();
                    }
                    event.consume();
                }
            });
        }
    }
}
