package controllers;

import dao.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import dao.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EjecutarController {

    private DateTimeFormatter formatter;
    private Alert alerta;
    private Alert info;
    private Alert advertencia;
    private ProductoDAO productoDAO;

    public EjecutarController(DateTimeFormatter formatter, Alert alerta, Alert info, Alert advertencia) {
        this.formatter = formatter;
        this.alerta = alerta;
        this.info = info;
        this.advertencia = advertencia;
        this.productoDAO = new ProductoDAO();
    }

    public void handleBotonEjecutar(
            ComboBox<String> tipoCombo,
            ComboBox<String> accionCombo,
            TextField nombreField,
            TextField razonField,
            TextField domicilioField,
            TextField localidadField,
            TextField codigoPostalField,
            TextField telefonoField,
            TextField cuitField,
            TextField condicionField,
            TextField altaField,
            TextField proveedorField,
            TextField categoriaField,
            TextField contactoField,
            TextField codigoField,
            TextField nomProduField,
            TextField preioUnitarioField,
            TextField nombreCuentaField,
            TextField fechaField,
            TextField comprobanteField,
            TextField tipoField,
            TextField netoField,
            TextField ivaField,
            TextField otrosField,
            TextField montoField,
            TextField observacionField,
            TextField cantidadField,
            TextField remitoField,
            TextField codProdu,
            TextField[] textFields,
            TableView<CuentaCorriente> tablaCuentas,
            TableView<CuentaCorriente> tablaProveedores,
            TextFlow resumenClienteText,
            TextField remitoFieldP,
            TextField nomProduFieldP,
            TextField cantidadFieldP,
            TextField precioUnitarioPField
    ) {
        String tipo = tipoCombo.getValue();
        String accion = accionCombo.getValue();

        if (tipo == null || accion == null) {
            alerta.setTitle("Error al seleccionar campos");
            alerta.setContentText("Selecciona tipo y acción.");
            alerta.showAndWait();
            return;
        }

        try {
            if (accion.equals("Alta")) {
                if (tipo.equals("Cliente")) {
                    String nombre = getText(nombreField);
                    String razonSocial = getText(razonField);
                    String domicilio = getText(domicilioField);
                    String localidad = getText(localidadField);
                    String codigoPostal = getText(codigoPostalField);
                    String telefono = getText(telefonoField);
                    String CUIT = getText(cuitField);
                    String condicion = getText(condicionField);
                    String fechaTexto = getText(altaField);
                    LocalDate fechaAlta = LocalDate.parse(fechaTexto, formatter);
                    String proveedor = getText(proveedorField);

                    if (!verificarCampos(
                            new String[]{nombre},
                            new String[]{"Nombre"}
                    )) return;

                    Cliente c = new Cliente(nombre, razonSocial, domicilio, localidad, codigoPostal,
                            telefono, CUIT, condicion, fechaAlta, proveedor);
                    new ClienteDAO().crearCliente(c);
                    info.setTitle("Éxito");
                    info.setContentText("Cliente agregado: " + c + "\n");
                    info.showAndWait();

                } else if (tipo.equals("Proveedor")) {
                    String nombre = getText(nombreField);
                    String razonSocial = getText(razonField);
                    String domicilio = getText(domicilioField);
                    String localidad = getText(localidadField);
                    String codigoPostal = getText(codigoPostalField);
                    String telefono = getText(telefonoField);
                    String CUIT = getText(cuitField);
                    String categoria = getText(categoriaField);
                    String contacto = getText(contactoField);

                    if (!verificarCampos(
                            new String[]{nombre},
                            new String[]{"Nombre"}
                    )) return;

                    Proveedor p = new Proveedor(nombre, razonSocial, domicilio, localidad, codigoPostal,
                            telefono, CUIT, categoria, contacto);
                    new ProveedorDAO().crearProveedor(p);
                    info.setTitle("Éxito");
                    info.setContentText("Proveedor agregado: " + p + "\n");
                    info.showAndWait();

                    // Limpiar todos los campos
                    for (TextField tf : textFields) {
                        tf.clear();
                    }

                } else if (tipo.equals("Cuenta Corriente")) {
                    String nombrePersona = getText(nombreCuentaField);
                    String fecha = getText(fechaField);
                    String comprobante = getText(comprobanteField);
                    String tipoCC = getText(tipoField);
                    String netoCC = getText(netoField);
                    String ivaCC = getText(ivaField);
                    String otrosCC = getText(otrosField);
                    String montoStr = getText(montoField);
                    String desc = getText(observacionField);

                    if (!verificarCampos(
                            new String[]{fecha, comprobante, tipoCC, netoCC, montoStr, nombrePersona},
                            new String[]{"Fecha", "Comprobante", "Tipo", "Neto", "Monto", "Nombre"}
                    )) return;

                    try {
                        double neto = Double.parseDouble(netoCC);
                        // MODIFICACIÓN AQUÍ: Parsea IVA y Otros, si están vacíos, usa 0.0
                        double iva = ivaCC.isEmpty() ? 0.0 : Double.parseDouble(ivaCC);
                        double otros = otrosCC.isEmpty() ? 0.0 : Double.parseDouble(otrosCC);
                        double monto = Double.parseDouble(montoStr);
                        LocalDate fechaLocalDate = LocalDate.parse(fecha, formatter);

                        // Declaramos las variables que pueden cambiar su valor
                        double valorVenta;
                        double valorNeto;
                        double valorIva;
                        double valorOtros;

                        CuentaCorrienteDAO ccDAO = new CuentaCorrienteDAO();

                        ClienteDAO clienteDAO = new ClienteDAO();
                        Cliente cliente = clienteDAO.obtenerClientePorNombre(nombrePersona);

                        if (cliente != null) {
                            valorVenta = neto;
                            valorNeto = neto;
                            valorIva = 0.0;
                            valorOtros = 0.0;

                            CuentaCorriente cc = new CuentaCorriente(fechaLocalDate, tipoCC, comprobante, valorVenta, monto, 0,
                                    desc, valorNeto, valorIva, valorOtros);

                            cc.setClienteId(cliente.getId());
                            cc.setProveedorId(null);

                            cc.setSaldo(0);
                            ccDAO.crearTransaccion(cc);

                            List<CuentaCorriente> todas = ccDAO.obtenerTodasLasTransaccionesOrdenadasPorFechaCliente(cliente.getId());
                            double saldoAcumulado = 0;
                            for (CuentaCorriente c : todas) {
                                saldoAcumulado += c.getVenta() - c.getMonto();
                                c.setSaldo(saldoAcumulado);
                                ccDAO.actualizarTransaccion(c);
                            }

                            clienteDAO.actualizarCliente(cliente);

                            info.setTitle("Éxito");
                            info.setContentText("Cuenta Corriente agregada al Cliente: " + cliente.getNombre());
                            info.showAndWait();
                            BuscarController buscador = new BuscarController();
                            buscador.mostrarCuentasYResumen(nombrePersona, tablaCuentas, tablaProveedores, resumenClienteText);
                            return;
                        }

                        ProveedorDAO proveedorDAO = new ProveedorDAO();
                        Proveedor proveedor = proveedorDAO.obtenerProveedorPorNombre(nombrePersona);

                        if (proveedor != null) {
                            valorVenta = neto + iva + otros;
                            valorNeto = neto;
                            valorIva = iva;
                            valorOtros = otros;

                            CuentaCorriente cc = new CuentaCorriente(fechaLocalDate, tipoCC, comprobante, valorVenta, monto, 0,
                                    desc, valorNeto, valorIva, valorOtros);

                            cc.setProveedorId(proveedor.getId());
                            cc.setClienteId(null);

                            cc.setSaldo(0);
                            ccDAO.crearTransaccion(cc);

                            List<CuentaCorriente> todas = ccDAO.obtenerTodasLasTransaccionesOrdenadasPorFechaProveedor(proveedor.getId());
                            double saldoAcumulado = 0;
                            for (CuentaCorriente c : todas) {
                                saldoAcumulado += c.getVenta() - c.getMonto();
                                c.setSaldo(saldoAcumulado);
                                ccDAO.actualizarTransaccion(c);
                            }

                            proveedorDAO.actualizarProveedor(proveedor);

                            info.setTitle("Éxito");
                            info.setContentText("Cuenta Corriente agregada al Proveedor: " + proveedor.getNombre());
                            info.showAndWait();
                            return;
                        }

                        // Si no se encontró ninguno
                        alerta.setTitle("Error");
                        alerta.setContentText("No se encontró a nadie con el nombre: " + nombrePersona);
                        alerta.showAndWait();

                    } catch (NumberFormatException nfe) {
                        alerta.setTitle("Error de formato");
                        alerta.setContentText("Los campos numéricos (Neto, IVA, Otros, Monto) deben ser valores numéricos válidos.");
                        alerta.showAndWait();
                    }
                } else if (tipo.equals("Remito")) {
                    String detalle = getText(codProdu);
                    String cantidadStr = getText(cantidadField);
                    String numeroRemitoStr = getText(remitoField);

                    if (!verificarCampos(
                            new String[]{detalle, cantidadStr, numeroRemitoStr},
                            new String[]{"Producto", "Cantidad", "Número de Remito"}
                    )) return;

                    try {
                        int cantidad = Integer.parseInt(cantidadStr);
                        String numeroRemito = numeroRemitoStr.trim();
                        Producto productoEncontrado = productoDAO.buscarProductoPorCodigo(detalle);

                        if (productoEncontrado == null) {
                            alerta.setTitle("Error");
                            alerta.setContentText("Producto con código '" + detalle + "' no encontrado.");
                            alerta.showAndWait();
                            return;
                        }

                        // Buscar la CuentaCorriente asociada al número de remito
                        CuentaCorrienteDAO ccDAO = new CuentaCorrienteDAO();
                        CuentaCorriente cc = ccDAO.obtenerPorComprobante(numeroRemito);

                        if (cc == null) {
                            alerta.setTitle("Error");
                            alerta.setContentText("No se encontró una cuenta corriente con número de remito: " + numeroRemito);
                            alerta.showAndWait();
                            return;
                        }

                        // Crear nuevo comprobante
                        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                        Comprobante comprobante = new Comprobante(
                                0,
                                cantidad,
                                productoEncontrado.getNombre(),
                                productoEncontrado.getPrecio() * cantidad
                        );
                        comprobanteDAO.crearComprobante(comprobante, cc.getId());
                        cc.addComprobante(comprobante);

                        mostrarVentanaComprobantes(cc.getId(), cc.getFecha().format(formatter), numeroRemitoStr);

                        info.setTitle("Éxito");
                        info.setContentText("Remito actualizado correctamente (N° " + numeroRemito + ")");
                        info.showAndWait();
                    } catch (NumberFormatException nfe) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Cantidad, Precio y Número de Remito deben ser números válidos.");
                        alerta.showAndWait();
                    }
                } else if (tipo.equals("Remito Proveedor")) {
                    String numeroRemitoPStr = getText(remitoFieldP);
                    String nombreStr = getText(nomProduFieldP);
                    String cantidadPStr = getText(cantidadFieldP);
                    String precioStr = getText(precioUnitarioPField);

                    if (!verificarCampos(
                            new String[]{numeroRemitoPStr, nombreStr, cantidadPStr, precioStr},
                            new String[]{"Numero remito", "Nombre", "Cantidad", "Precio"}
                    )) return;

                    try {
                        int cantidad = Integer.parseInt(cantidadPStr);
                        double precio = Double.parseDouble(precioStr);
                        String numeroRemito = numeroRemitoPStr.trim();

                        // Buscar la CuentaCorriente asociada al número de remito
                        CuentaCorrienteDAO ccDAO = new CuentaCorrienteDAO();
                        CuentaCorriente cc = ccDAO.obtenerPorComprobante(numeroRemito);

                        if (cc == null) {
                            alerta.setTitle("Error");
                            alerta.setContentText("No se encontró una cuenta corriente con número de remito: " + numeroRemito);
                            alerta.showAndWait();
                            return;
                        }

                        // Crear nuevo comprobante
                        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                        Comprobante comprobante = new Comprobante(
                                0,
                                cantidad,
                                nombreStr,
                                precio
                        );
                        comprobanteDAO.crearComprobante(comprobante, cc.getId());
                        cc.addComprobante(comprobante);

                        mostrarVentanaComprobantesProveedores(cc.getId(), cc.getFecha().format(formatter), numeroRemito);

                        info.setTitle("Éxito");
                        info.setContentText("Remito actualizado correctamente (N° " + numeroRemito + ")");
                        info.showAndWait();
                    } catch (NumberFormatException nfe) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Cantidad, Precio y Número de Remito deben ser números válidos.");
                        alerta.showAndWait();
                    }
                } else if (tipo.equals("Producto")) {
                    String codigo = getText(codigoField);
                    String nombre = getText(nomProduField);
                    String precio = getText(preioUnitarioField);

                    double precioUnitario = Double.parseDouble(precio);

                    if (!verificarCampos(
                            new String[]{codigo},
                            new String[]{"Codigo"}
                    )) return;

                    Producto p = new Producto(codigo, nombre, precioUnitario);
                    this.productoDAO.crearProducto(p);
                    info.setTitle("Éxito");
                    info.setContentText("Producto agregado: " + p + "\n");
                    info.showAndWait();
                    for (TextField tf : textFields) {
                        tf.clear();
                    }
                }
            } else if (accion.equals("Baja")) {
                String nameDelete = getText(nombreField);

                if (nameDelete.isEmpty()) {
                    alerta.setTitle("Error");
                    alerta.setContentText("Escribe un nombre para eliminar.");
                    alerta.showAndWait();
                    return;
                }

                if (tipo.equals("Cliente")) {
                    new ClienteDAO().eliminarCliente(nameDelete);
                    advertencia.setTitle("Advertencia");
                    advertencia.setContentText("Estás a punto de eliminar al cliente " + nameDelete);
                    advertencia.showAndWait();
                    info.setTitle("Éxito");
                    info.setContentText("Cliente " + nameDelete + " eliminado.");
                    info.showAndWait();

                } else if (tipo.equals("Proveedor")) {
                    new ProveedorDAO().eliminarProveedor(nameDelete);
                    advertencia.setTitle("Advertencia");
                    advertencia.setContentText("Estás a punto de eliminar al proveedor " + nameDelete);
                    advertencia.showAndWait();
                    info.setTitle("Éxito");
                    info.setContentText("Cliente " + nameDelete + " eliminado.");
                    info.showAndWait();

                } else if (tipo.equals("Cuenta Corriente")) {
                    alerta.setTitle("Error");
                    alerta.setContentText("No se permite eliminar registros de Cuenta Corriente.");
                    alerta.showAndWait();
                }

            } else if (accion.equals("Modificación")) {
                String nameChange = getText(nombreField);

                if (tipo.equals("Cliente")) {
                    ClienteDAO dao = new ClienteDAO();
                    Cliente existente = dao.obtenerClientePorNombre(nameChange);

                    if (existente == null) {
                        alerta.setTitle("Error");
                        alerta.setContentText("No se encontró el cliente " + nameChange);
                        alerta.showAndWait();
                    } else {
                        existente.setNombre(getText(nombreField));
                        existente.setRazonSocial(getText(razonField));
                        existente.setDomicilio(getText(domicilioField));
                        existente.setLocalidad(getText(localidadField));
                        existente.setCodigoPostal(getText(codigoPostalField));
                        existente.setTelefono(getText(telefonoField));
                        existente.setCUIT(getText(cuitField));
                        existente.setCondicion(getText(condicionField));
                        String fechaTexto = getText(altaField);
                        LocalDate fechaAlta = LocalDate.parse(fechaTexto, formatter);
                        existente.setFechaAlta(fechaAlta);
                        existente.setProveedor(getText(proveedorField));

                        dao.actualizarCliente(existente);
                        info.setTitle("Éxito");
                        info.setContentText("Cliente actualizado: " + existente);
                        info.showAndWait();
                    }

                } else if (tipo.equals("Proveedor")) {
                    ProveedorDAO dao = new ProveedorDAO();
                    Proveedor existente = dao.obtenerProveedorPorNombre(nameChange);

                    if (existente == null) {
                        alerta.setTitle("Error");
                        alerta.setContentText("No se encontró el proveedor " + nameChange);
                        alerta.showAndWait();
                    } else {
                        existente.setNombre(getText(nombreField));
                        existente.setRazonSocial(getText(razonField));
                        existente.setDomicilio(getText(domicilioField));
                        existente.setLocalidad(getText(localidadField));
                        existente.setCodigoPostal(getText(codigoPostalField));
                        existente.setTelefono(getText(telefonoField));
                        existente.setCUIT(getText(cuitField));
                        existente.setCategoria(getText(categoriaField));
                        existente.setContacto(getText(contactoField));

                        dao.actualizarProveedor(existente);
                        info.setTitle("Éxito");
                        info.setContentText("Proveedor actualizado: " + existente);
                        info.showAndWait();
                    }

                } else if (tipo.equals("Cuenta Corriente")) {
                    alerta.setTitle("Error");
                    alerta.setContentText("No se permite modificar registros de Cuenta Corriente.");
                    alerta.showAndWait();
                }
            }

            // Limpiar todos los campos
            for (TextField tf : textFields) {
                if (tf != remitoField) {
                    tf.clear();
                }
            }

        } catch (Exception ex) {
            alerta.setTitle("Error");
            alerta.setContentText("Error: " + ex.getMessage());
            alerta.showAndWait();
        }
        // Aquí va TODO el código que está en el setOnAction.
        // Recuerda usar getText() y verificarCampos() como métodos auxiliares de esta clase o que te provean.
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Método auxiliar para obtener texto de un TextField
    private String getText(TextField field) {
        return field.getText().trim();
    }

    // Método auxiliar para validar campos obligatorios
    private boolean verificarCampos(String[] valores, String[] nombres) {
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].isEmpty()) {
                mostrarError("El campo '" + nombres[i] + "' es obligatorio.\n");
                return false;
            }
        }
        return true;
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
            double precio = data.getValue().getPrecio();
            return new javafx.beans.property.SimpleDoubleProperty(precio / cantidad);
        });

        TableColumn<Comprobante, Double> precioCol = new TableColumn<>("Total");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tablaComprobantes.getColumns().addAll(cantidadCol, productoCol, totalCol, precioCol);
        tablaComprobantes.getItems().addAll(comprobantes);
        tablaComprobantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        double totalGeneral = comprobantes.stream()
                .mapToDouble(c -> c.getCantidad() * c.getPrecio())
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

    private void mostrarVentanaComprobantesProveedores(int movimientoId, String fecha, String persona) {
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
        List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(movimientoId);

        Stage ventana = new Stage();
        ventana.setTitle("Dia " + fecha + ", Cliente/Proveedor " + persona);

        TableView<Comprobante> tablaComprobantes = new TableView<>();

        TableColumn<Comprobante, String> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<Comprobante, String> productoCol = new TableColumn<>("Producto");
        productoCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Comprobante, Double> precioCol = new TableColumn<>("Precio");
        precioCol.setCellValueFactory(new PropertyValueFactory<>("precio"));

        TableColumn<Comprobante, Number> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> {
            int cantidad = data.getValue().getCantidad();
            double precio = data.getValue().getPrecio();
            return new javafx.beans.property.SimpleDoubleProperty(precio * cantidad);
        });

        tablaComprobantes.getColumns().addAll(cantidadCol, productoCol, precioCol, totalCol);
        tablaComprobantes.getItems().addAll(comprobantes);
        tablaComprobantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        double totalGeneral = comprobantes.stream()
                .mapToDouble(c -> c.getCantidad() * c.getPrecio())
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
}