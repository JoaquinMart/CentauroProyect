// TODO LO QUE TENGA (M) SON COSAS A MODIFICAR
package main;

import dao.ProductoDAO;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.*;

import controllers.*;
import javafx.util.Callback;
import model.*;
import util.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AppController {
    private ProductoDAO productoDAO;
    private Label nombreProductoPreviewLabel;
    private Button botonEjecutar;
    private TableView<SugerenciaBusqueda> tablaSugerencias;

    public void init(Stage stage) {
        try (Connection conn = ConexionMySQL.conectar()) {
            ConexionMySQL.crearTablas(conn);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos o al crear las tablas.");
            e.printStackTrace();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

        TextFlow resumenClienteText = new TextFlow();
        resumenClienteText.setMaxWidth(Double.MAX_VALUE);
        resumenClienteText.getChildren().add(new Text("Seleccioná un cliente/proveedor..."));

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        Alert advertencia = new Alert(Alert.AlertType.WARNING);

        InputStream iconStream = getClass().getResourceAsStream("/icono.png");
        if (iconStream == null) {
            System.err.println("❌ No se encontró el archivo icono.ico");
            Alert all = new Alert(Alert.AlertType.ERROR);
            all.setTitle("Error de Ícono");
            all.setHeaderText("No se pudo cargar el ícono");
            all.setContentText("Verificá que icono.ico esté en src/main/resources y esté bien empaquetado.");
            all.showAndWait();
        } else {
            stage.getIcons().add(new Image(iconStream));
        }

        ComboBox<String> tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Cliente", "Proveedor", "Cuenta Corriente", "Remito", "Producto", "Remito Proveedor");
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
        TextField fechaField = new TextField(); fechaField.setPromptText("Fecha del remito");
        TextField comprobanteField = new TextField(); comprobanteField.setPromptText("Numero Remito");
        TextField tipoField = new TextField(); tipoField.setPromptText("Tipo");
        TextField netoField = new TextField(); netoField.setPromptText("Neto");
        TextField ivaField = new TextField(); ivaField.setPromptText("IVA, cliente dejar en 0");
        TextField otrosField = new TextField(); otrosField.setPromptText("Otros cliente dejar en 0");
        TextField montoField = new TextField(); montoField.setPromptText("Pago");
        TextField saldoField = new TextField(); saldoField.setPromptText("Saldo");
        TextField observacionField = new TextField(); observacionField.setPromptText("Observacion");

        // Campos Comprobante
        TextField remitoField = new TextField(); remitoField.setPromptText("Numero Remito");
        TextField codProdu = new TextField(); codProdu.setPromptText("Codigo");
        TextField cantidadField = new TextField(); cantidadField.setPromptText("Cantidad");

        // Campos Comprobante Proveedor
        TextField remitoFieldP = new TextField(); remitoFieldP.setPromptText("Numero Remito");
        TextField nomProduFieldP = new TextField(); nomProduFieldP.setPromptText("Producto");
        TextField cantidadFieldP = new TextField(); cantidadFieldP.setPromptText("Cantidad");
        TextField precioUnitarioPField = new TextField(); precioUnitarioPField.setPromptText("Precio");

        // Campos Producto
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
                nombreCuentaField, fechaField, comprobanteField, tipoField, netoField, ivaField, otrosField,
                montoField, observacionField
        );

        List<TextField> textosComprobante = List.of(
                remitoField, codProdu, cantidadField
        );

        List<TextField> textosFacturas = List.of(
                remitoFieldP, nomProduFieldP, cantidadFieldP, precioUnitarioPField
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
        fechaCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.11));
        fechaCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, String> numCol = new TableColumn<>("N° Remito");
        numCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getComprobante()));
        numCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.10));
        numCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, String> tipoCol = new TableColumn<>("Tipo");
        tipoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTipo()));
        tipoCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.08));
        tipoCol.getStyleClass().add("custom-table-column");
        TableColumn<CuentaCorriente, Number> ventaCol = new TableColumn<>("Venta");
        ventaCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getVenta()));
        ventaCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.15));
        ventaCol.getStyleClass().add("custom-table-column");
        ventaCol.setCellFactory(AppController.getDecimalFormatCellFactory());
        TableColumn<CuentaCorriente, Number> montoCol = new TableColumn<>("Pago");
        montoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMonto()));
        montoCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.15));
        montoCol.getStyleClass().add("custom-table-column");
        montoCol.setCellFactory(AppController.getCurrencyFormatCellFactory());
        TableColumn<CuentaCorriente, Number> saldoCol = new TableColumn<>("Saldo");
        saldoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getSaldo()));
        saldoCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.21));
        saldoCol.getStyleClass().add("custom-table-column");
        saldoCol.setCellFactory(AppController.getCurrencyFormatCellFactory());

        TableColumn<CuentaCorriente, String> obsCol = new TableColumn<>("Observación");
        obsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getObservacion()));
        obsCol.setCellFactory(tc -> {
            TableCell<CuentaCorriente, String> cell = new TableCell<>() {
                private final Text text = new Text();
                {
                    setGraphic(text);
                    text.setWrappingWidth(0);
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText("");
                        setTooltip(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                        setTooltip(new Tooltip(item));
                    }
                }
            };
            return cell;
        });
        obsCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.20));
        obsCol.setMinWidth(700);
        obsCol.getStyleClass().add("custom-table-column");
        obsCol.getStyleClass().add("center-column-header");

        tablaCuentas.getColumns().addAll(fechaCol, numCol, tipoCol, ventaCol, montoCol, saldoCol, obsCol);
        tablaCuentas.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // TABLA DE PROVEEDORES
        TableView<CuentaCorriente> tablaProveedores = new TableView<>();

        TableColumn<CuentaCorriente, String> fechaProvCol = new TableColumn<>("Fecha");
        fechaProvCol.setCellValueFactory(data -> {
            LocalDate fecha = data.getValue().getFecha();
            String fechaStr = (fecha != null) ? fecha.format(formatter) : "";
            return new javafx.beans.property.SimpleStringProperty(fechaStr);
        });
        fechaProvCol.getStyleClass().add("custom-table-column");
        fechaProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.10));

        TableColumn<CuentaCorriente, String> numProvCol = new TableColumn<>("N° Remito");
        numProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getComprobante()));
        numProvCol.getStyleClass().add("custom-table-column");
        numProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.09));

        TableColumn<CuentaCorriente, String> tipoProvCol = new TableColumn<>("Tipo");
        tipoProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTipo()));
        tipoProvCol.getStyleClass().add("custom-table-column");
        tipoProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.08));

        TableColumn<CuentaCorriente, Number> netoProvCol = new TableColumn<>("Neto");
        netoProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getNeto()));
        netoProvCol.getStyleClass().add("custom-table-column");
        netoProvCol.setCellFactory(AppController.getDecimalFormatCellFactory());
        netoProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.10));

        TableColumn<CuentaCorriente, Number> ivaProvCol = new TableColumn<>("IVA");
        ivaProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getIva()));
        ivaProvCol.getStyleClass().add("custom-table-column");
        ivaProvCol.setCellFactory(AppController.getDecimalFormatCellFactory());
        ivaProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.10));

        TableColumn<CuentaCorriente, Number> otrosProvCol = new TableColumn<>("Otros");
        otrosProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getOtros()));
        otrosProvCol.getStyleClass().add("custom-table-column");
        otrosProvCol.setCellFactory(AppController.getDecimalFormatCellFactory());
        otrosProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.09));

        TableColumn<CuentaCorriente, Number> ventaProvCol = new TableColumn<>("Venta");
        ventaProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getVenta()));
        ventaProvCol.getStyleClass().add("custom-table-column");
        ventaProvCol.setCellFactory(AppController.getDecimalFormatCellFactory());
        ventaProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.10));

        TableColumn<CuentaCorriente, Number> montoProvCol = new TableColumn<>("Pago");
        montoProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getMonto()));
        montoProvCol.getStyleClass().add("custom-table-column");
        montoProvCol.setCellFactory(AppController.getCurrencyFormatCellFactory());
        montoProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.11));

        TableColumn<CuentaCorriente, Number> saldoProvCol = new TableColumn<>("Saldo");
        saldoProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getSaldo()));
        saldoProvCol.getStyleClass().add("custom-table-column");
        saldoProvCol.setCellFactory(AppController.getCurrencyFormatCellFactory());
        saldoProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.13));

        TableColumn<CuentaCorriente, String> obsProvCol = new TableColumn<>("Observación");
        obsProvCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getObservacion()));
        obsProvCol.setCellFactory(tc -> {
            TableCell<CuentaCorriente, String> cell = new TableCell<>() {
                private final Text text = new Text();
                {
                    setGraphic(text);
                    text.setWrappingWidth(0);
                }
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText("");
                        setTooltip(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                        setTooltip(new Tooltip(item));
                    }
                }
            };
            return cell;
        });
        obsProvCol.prefWidthProperty().bind(tablaCuentas.widthProperty().multiply(0.10));
        obsProvCol.setMinWidth(700);
        obsProvCol.getStyleClass().add("custom-table-column");
        obsProvCol.getStyleClass().add("center-column-header");

        tablaProveedores.getColumns().addAll(fechaProvCol, numProvCol, tipoProvCol, netoProvCol, ivaProvCol, otrosProvCol, ventaProvCol, montoProvCol, saldoProvCol, obsProvCol);
        tablaProveedores.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // TABLA DE SUGERENCIAS
        tablaSugerencias = new TableView<>();
        tablaSugerencias.getStyleClass().add("sugerencias-table");
        tablaSugerencias.setVisible(false);
        tablaSugerencias.setPrefWidth(700);
        tablaSugerencias.setMaxWidth(700);

        tablaSugerencias.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<SugerenciaBusqueda, String> tipoSCol = new TableColumn<>("Tipo");
        tipoSCol.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<SugerenciaBusqueda, String> nombreSCol = new TableColumn<>("Nombre");
        nombreSCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<SugerenciaBusqueda, String> razonSSocialCol = new TableColumn<>("Razón Social");
        razonSSocialCol.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        tablaSugerencias.getColumns().addAll(tipoSCol, nombreSCol, razonSSocialCol);
        tablaSugerencias.setPrefHeight(0);
        tablaSugerencias.getItems().addListener((javafx.collections.ListChangeListener<SugerenciaBusqueda>) c -> {
            int rowCount = tablaSugerencias.getItems().size();
            if (rowCount == 0) {
                tablaSugerencias.setPrefHeight(0);
                return;
            }
            double rowHeight = 28;
            double headerHeight = 30;
            double newHeight = (rowCount * rowHeight) + headerHeight;
            tablaSugerencias.setPrefHeight(newHeight);
        });

        // Botones
        botonEjecutar = new Button("Ejecutar");
        TextField buscarNombreField = new TextField();
        buscarNombreField.setPromptText("Buscar por nombre");
        Button botonBuscar = new Button("Buscar");
        Button botonGuardarPdf = new Button("Generar PDF");
        Button botonImprimir = new Button("Imprimir");

        TextField[] textFields = {
                razonField, domicilioField, localidadField,
                codigoPostalField, telefonoField, cuitField, nombreField,
                condicionField, altaField, proveedorField, categoriaField,
                contactoField, nombreCuentaField, fechaField, comprobanteField,
                tipoField, netoField, ivaField, otrosField, montoField,
                saldoField, observacionField, remitoField, codProdu, cantidadField,
                codigoField, nomProduField, preioUnitarioField, remitoFieldP,
                nomProduFieldP, cantidadFieldP, precioUnitarioPField
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
                netoField, ivaField, otrosField,
                montoField, observacionField
        );
        VBox.setVgrow(camposCuentaCorriente, Priority.ALWAYS);

        // --- Campos comprobante ---
        VBox camposComprobante = new VBox(10,
                remitoField, codProdu, nombreProductoPreviewLabel, cantidadField
        );
        VBox.setVgrow(camposComprobante, Priority.ALWAYS);

        // --- Campos factura proveedores ---
        VBox camposProveedores = new VBox(10,
                remitoFieldP, nomProduFieldP, cantidadFieldP, precioUnitarioPField
        );
        VBox.setVgrow(camposProveedores, Priority.ALWAYS);

        // --- Campos de productos ---
        VBox camposProductos = new VBox(10,
                codigoField, nomProduField, preioUnitarioField
        );

        VBox.setVgrow(camposProductos, Priority.ALWAYS);

        // --- Listener para el campo codProdu ---
        codProdu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                nombreProductoPreviewLabel.setText(" ");
                return;
            }
            try {
                Producto productoEncontrado = productoDAO.buscarProductoPorCodigo(newValue);

                if (productoEncontrado != null) {
                    nombreProductoPreviewLabel.setText(productoEncontrado.getNombre());
                } else {
                    nombreProductoPreviewLabel.setText("Producto no encontrado");
                }
            } catch (SQLException e) {
                System.err.println("Error de base de datos al buscar producto por código: " + e.getMessage());
                nombreProductoPreviewLabel.setText("Error de DB");
            }
        });
        // -----------------------------------------------------------------

        tipoCombo.setOnAction(e -> actualizarCamposFormulario(
                tipoCombo, accionCombo,
                camposNormales, camposCuentaCorriente, camposComprobante, camposProductos, camposProveedores,
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField,
                cuitField, condicionField, altaField, proveedorField, categoriaField, contactoField,
                nombreCuentaField, fechaField, comprobanteField, tipoField, montoField, saldoField,
                observacionField, cantidadField, remitoField, codProdu,
                textosClientes, textosProveedor, textosCuentaCorriente, textosComprobante, textosFacturas, textosProductos,
                codigoField, nomProduField, preioUnitarioField,
                remitoFieldP, nomProduFieldP, cantidadFieldP, precioUnitarioPField, netoField, ivaField, otrosField
        ));

        accionCombo.setOnAction(e -> actualizarCamposFormulario(
                tipoCombo, accionCombo,
                camposNormales, camposCuentaCorriente, camposComprobante, camposProductos, camposProveedores,
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField,
                cuitField, condicionField, altaField, proveedorField, categoriaField, contactoField,
                nombreCuentaField, fechaField, comprobanteField, tipoField, montoField, saldoField,
                observacionField, cantidadField, remitoField, codProdu,
                textosClientes, textosProveedor, textosCuentaCorriente, textosComprobante, textosFacturas, textosProductos,
                codigoField, nomProduField, preioUnitarioField,
                remitoFieldP, nomProduFieldP, cantidadFieldP, precioUnitarioPField, netoField, ivaField, otrosField
        ));

        // Llamada inicial
        actualizarCamposFormulario(
                tipoCombo, accionCombo,
                camposNormales, camposCuentaCorriente, camposComprobante, camposProductos, camposProveedores,
                nombreField, razonField, domicilioField, localidadField, codigoPostalField, telefonoField,
                cuitField, condicionField, altaField, proveedorField, categoriaField, contactoField,
                nombreCuentaField, fechaField, comprobanteField, tipoField, montoField, saldoField,
                observacionField, cantidadField, remitoField, codProdu,
                textosClientes, textosProveedor, textosCuentaCorriente, textosComprobante, textosFacturas, textosProductos,
                codigoField, nomProduField, preioUnitarioField,
                remitoFieldP, nomProduFieldP, cantidadFieldP, precioUnitarioPField, netoField, ivaField, otrosField
        );

        // Inicializa el nuevo controlador
        PDFController pdfController = new PDFController();

        // Acción para el nuevo botón "Guardar PDF"
        botonGuardarPdf.setOnAction(e -> {
            pdfController.handleGuardarPdfButton();
        });

        // Boton princial de carga
        EjecutarController controlador = new EjecutarController(formatter, alerta, info, advertencia);
        botonEjecutar.setOnAction(e -> {
            controlador.handleBotonEjecutar(
                    tipoCombo, accionCombo,
                    nombreField, razonField, domicilioField, localidadField, codigoPostalField,
                    telefonoField, cuitField, condicionField, altaField, proveedorField,
                    categoriaField, contactoField, codigoField, nomProduField, preioUnitarioField,
                    nombreCuentaField, fechaField, comprobanteField, tipoField, netoField, ivaField,
                    otrosField, montoField, observacionField, cantidadField, remitoField,
                    codProdu, textFields, tablaCuentas, tablaProveedores, resumenClienteText,
                    remitoFieldP, nomProduFieldP, cantidadFieldP, precioUnitarioPField
            );
        });

        // Boton para buscar e imprimir
        BuscarController controladorBuscar = new BuscarController();
        controladorBuscar.setup(buscarNombreField, tablaSugerencias, tablaCuentas, tablaProveedores, resumenClienteText);
        botonBuscar.setOnAction(e -> {
            controladorBuscar.handleBotonBuscar(tablaSugerencias, tablaCuentas, tablaProveedores, resumenClienteText);
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
        HBox filaCombos2 = new HBox(10, buscarNombreField, botonBuscar, botonGuardarPdf, resumenClienteText);
        filaCombos2.getStyleClass().add("combo-row");
        buscarNombreField.setPrefWidth(anchoComun);
        buscarNombreField.getStyleClass().add("search-field");
        botonBuscar.getStyleClass().add("search-button");
        botonGuardarPdf.getStyleClass().add("search-button");
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

        // --- Contenedor de los botones  ---
        VBox botonesBox = new VBox(10);
        botonesBox.getStyleClass().add("buttons-container");

        // Añadir las filas de botones al VBox en el orden deseado
        botonesBox.getChildren().addAll(filaBotonEjecutar, filaImprimir);
        botonesBox.setAlignment(Pos.CENTER_LEFT);

        // --- Campos dinámicos (cuenta corriente / normal / comprobante) ---
        StackPane camposBox = new StackPane(camposCuentaCorriente, camposNormales, camposComprobante, camposProductos, camposProveedores);
        camposBox.setId("dynamic-fields-stack");
        VBox.setVgrow(camposBox, Priority.ALWAYS);
        camposBox.setMaxHeight(Double.MAX_VALUE);

        // --- Formulario (IZQUIERDA) ---
        VBox formulario = new VBox(combosBox, botonesBox, camposBox);
        formulario.setId("main-form");
        formulario.setMaxWidth(Double.MAX_VALUE);
        formulario.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(formulario, Priority.NEVER);
        formulario.setPrefWidth(700);
        HBox.setHgrow(formulario, Priority.ALWAYS);

        // --- Tablas (DERECHA) ---
        StackPane tablasStackPane = new StackPane(tablaSugerencias, tablaCuentas, tablaProveedores);
        tablasStackPane.setMaxHeight(Double.MAX_VALUE);
        tablasStackPane.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(tablaSugerencias, Pos.TOP_LEFT);
        VBox.setVgrow(tablaSugerencias, Priority.NEVER);

        // Configura la visibilidad inicial
        tablaCuentas.setVisible(true);
        tablaCuentas.setManaged(true);
        tablaProveedores.setVisible(false);
        tablaProveedores.setManaged(false);

        // Asegúro de que las clases CSS se apliquen a ambas tablas
        tablaCuentas.getStyleClass().add("main-table");
        tablaProveedores.getStyleClass().add("main-table");

        VBox tablasBox = new VBox(10, filaCombos2, tablasStackPane);
        tablasBox.getStyleClass().add("tables-container");
        tablasBox.setMaxWidth(Double.MAX_VALUE);
        tablasBox.setPrefWidth(1400);
        VBox.setVgrow(tablasStackPane, Priority.ALWAYS);
        VBox.setVgrow(tablasBox, Priority.ALWAYS);
        HBox.setHgrow(tablasBox, Priority.ALWAYS);
        resumenClienteText.getStyleClass().add("client-summary-text");

        // --- Contenedor HBox para ambas mitades ---
        HBox formularioConTablas = new HBox(formulario, tablasBox);
        formularioConTablas.getStyleClass().add("main-content-area");
        formularioConTablas.setMaxHeight(Double.MAX_VALUE);
        formularioConTablas.setPrefHeight(Double.MAX_VALUE);
        HBox.setHgrow(formulario, Priority.ALWAYS);
        HBox.setHgrow(tablasBox, Priority.ALWAYS);

        // --- Layout principal ---
        BorderPane root = new BorderPane();
        root.setId("root-pane");
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
            VBox camposProveedores, TextField nombreField, TextField razonField, TextField domicilioField,
            TextField localidadField, TextField codigoPostalField, TextField telefonoField, TextField cuitField,
            TextField condicionField, TextField altaField, TextField proveedorField, TextField categoriaField,
            TextField contactoField, TextField nombreCuentaField, TextField fechaField, TextField comprobanteField,
            TextField tipoField, TextField montoField, TextField saldoField, TextField observacionField,
            TextField cantidadField, TextField remitoField, TextField codProdu, List<TextField> textosClientes,
            List<TextField> textosProveedores, List<TextField> textosCuentaCorriente, List<TextField> textosComprobante,
            List<TextField> textosFacturas, List<TextField> textosProductos, TextField codigoField,
            TextField nomProduField, TextField preioUnitarioField, TextField remitoFieldP, TextField nomProduFieldP,
            TextField cantidadFieldP, TextField precioUnitarioPField, TextField netoField, TextField ivaField,
            TextField otrosField)
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
        camposProveedores.setVisible(false);
        camposProveedores.setManaged(false);

        if (nombreProductoPreviewLabel != null) {
            nombreProductoPreviewLabel.setVisible(false);
            nombreProductoPreviewLabel.setManaged(false);
        }

        if ("Cuenta Corriente".equals(tipo)) {
            camposCuentaCorriente.setVisible(true);
            camposCuentaCorriente.setManaged(true);

            if ("Baja".equals(accion)) {
                for (Node nodo : camposCuentaCorriente.getChildren()) {
                    nodo.setVisible(false);
                    nodo.setManaged(false);
                }

                nombreCuentaField.setVisible(true);
                nombreCuentaField.setManaged(true);
                fechaField.setVisible(true);
                fechaField.setManaged(true);
                comprobanteField.setVisible(true);
                comprobanteField.setManaged(true);

            } else {
                for (Node nodo : camposCuentaCorriente.getChildren()) {
                    nodo.setVisible(true);
                    nodo.setManaged(true);
                }
            }
            asignarEnterEntreCampos(textosCuentaCorriente);

        } else if ("Remito Proveedor".equals(tipo)) {
            camposProveedores.setVisible(true);
            camposProveedores.setManaged(true);

            if ("Baja".equals(accion)) {
                for (Node nodo : camposProveedores.getChildren()) {
                    nodo.setVisible(false);
                    nodo.setManaged(false);
                }

            } else {
                for (Node nodo : camposProveedores.getChildren()) {
                    nodo.setVisible(true);
                    nodo.setManaged(true);
                }
            }
            if (nombreProductoPreviewLabel != null) {
                nombreProductoPreviewLabel.setVisible(true);
                nombreProductoPreviewLabel.setManaged(true);
            }
            asignarEnterEntreCampos(textosFacturas);

        } else if ("Remito".equals(tipo)) {
            camposComprobante.setVisible(true);
            camposComprobante.setManaged(true);

            if ("Baja".equals(accion)) {
                for (Node nodo : camposComprobante.getChildren()) {
                    nodo.setVisible(false);
                    nodo.setManaged(false);
                }
                remitoField.setVisible(true);
                remitoField.setManaged(true);
            } else {
                for (Node nodo : camposComprobante.getChildren()) {
                    nodo.setVisible(true);
                    nodo.setManaged(true);
                }
            }
            if (nombreProductoPreviewLabel != null) {
                nombreProductoPreviewLabel.setVisible(true);
                nombreProductoPreviewLabel.setManaged(true);
            }
            asignarEnterEntreCampos(textosComprobante);

        } else if ("Producto".equals(tipo)) {
            camposProductos.setVisible(true);
            camposProductos.setManaged(true);

            if ("Baja".equals(accion)) {
                for (Node nodo : camposProductos.getChildren()) {
                    nodo.setVisible(false);
                    nodo.setManaged(false);
                }
                codigoField.setVisible(true);
                codigoField.setManaged(true);
            } else {
                for (Node nodo : camposProductos.getChildren()) {
                    nodo.setVisible(true);
                    nodo.setManaged(true);
                }
            }
            asignarEnterEntreCampos(textosProductos);

        } else if ("Cliente".equals(tipo) || "Proveedor".equals(tipo)) {
            camposNormales.setVisible(true);
            camposNormales.setManaged(true);

            TextField[] todosCamposNormales = {
                    nombreField, razonField, domicilioField, localidadField,
                    codigoPostalField, telefonoField, cuitField, condicionField,
                    altaField, proveedorField, categoriaField, contactoField
            };
            for (TextField tf : todosCamposNormales) {
                tf.setVisible(false);
                tf.setManaged(false);
            }

            if ("Baja".equals(accion)) {
                nombreField.setVisible(true);
                nombreField.setManaged(true);
            } else {
                if ("Cliente".equals(tipo)) {
                    nombreField.setVisible(true);
                    razonField.setVisible(true);
                    domicilioField.setVisible(true);
                    localidadField.setVisible(true);
                    codigoPostalField.setVisible(true);
                    telefonoField.setVisible(true);
                    cuitField.setVisible(true);
                    condicionField.setVisible(true);
                    altaField.setVisible(true);
                    proveedorField.setVisible(true);
                    nombreField.setManaged(true);
                    razonField.setManaged(true);
                    domicilioField.setManaged(true);
                    localidadField.setManaged(true);
                    codigoPostalField.setManaged(true);
                    telefonoField.setManaged(true);
                    cuitField.setManaged(true);
                    condicionField.setManaged(true);
                    altaField.setManaged(true);
                    proveedorField.setManaged(true);
                    asignarEnterEntreCampos(textosClientes);
                } else if ("Proveedor".equals(tipo)) {
                    nombreField.setVisible(true);
                    razonField.setVisible(true);
                    domicilioField.setVisible(true);
                    localidadField.setVisible(true);
                    codigoPostalField.setVisible(true);
                    telefonoField.setVisible(true);
                    cuitField.setVisible(true);
                    categoriaField.setVisible(true);
                    contactoField.setVisible(true);
                    nombreField.setManaged(true);
                    razonField.setManaged(true);
                    domicilioField.setManaged(true);
                    localidadField.setManaged(true);
                    codigoPostalField.setManaged(true);
                    telefonoField.setManaged(true);
                    cuitField.setManaged(true);
                    categoriaField.setManaged(true);
                    contactoField.setManaged(true);
                    asignarEnterEntreCampos(textosProveedores);
                }
            }
        }
    }

    // Método para moverse con ENTER en los campos
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

    // Método para conversion de numeros
    public static <S, T extends Number> Callback<TableColumn<S, T>, TableCell<S, T>> getDecimalFormatCellFactory() {
        return column -> new TableCell<S, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.getDefault(), "%.2f", item.doubleValue()));
                }
            }
        };
    }

    // Método para conversion de numeros
    public static <S, T extends Number> Callback<TableColumn<S, T>, TableCell<S, T>> getCurrencyFormatCellFactory() {
        return column -> new TableCell<S, T>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR")); // Formato para Argentina

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item.doubleValue()));
                }
            }
        };
    }
}