// TODO LO QUE TENGA (M) SON COSAS A MODIFICAR
package controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.AppController;
import model.*;
import dao.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                    // Se podria simplificar sacando del if los campos que comparten (M)
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

                    // TODOS LOS !verificarCampos, JUSTAMENTE CONTORLAN QUE CAMPOS SI SON OBLIGATORIOS
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
                    // Lo mismo que el cliente (M)
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

                    // Creo al proveedor
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
                            new String[]{fecha, nombrePersona},
                            new String[]{"Fecha", "Nombre"}
                    )) return;

                    try {
                        // Valores que pueden ser 0
                        double neto = netoCC.isEmpty() ? 0.0 : Double.parseDouble(netoCC);
                        double iva = ivaCC.isEmpty() ? 0.0 : Double.parseDouble(ivaCC);
                        double otros = otrosCC.isEmpty() ? 0.0 : Double.parseDouble(otrosCC);
                        double monto = montoStr.isEmpty() ? 0.0 : Double.parseDouble(montoStr);
                        LocalDate fechaLocalDate = LocalDate.parse(fecha, formatter);

                        // Declaramos las variables que pueden cambiar su valor
                        double valorVenta;
                        double valorNeto;
                        double valorIva;
                        double valorOtros;

                        CuentaCorrienteDAO ccDAO = new CuentaCorrienteDAO();

                        // Creo un cliente para ver si existe, de ser asi le agrego la cuenta corriente
                        ClienteDAO clienteDAO = new ClienteDAO();
                        Cliente cliente = clienteDAO.obtenerClientePorNombre(nombrePersona);

                        if (cliente != null) {
                            valorVenta = neto;
                            valorNeto = neto;
                            valorIva = 0.0;
                            valorOtros = 0.0;

                            // Creo la CC a agregar al cliente
                            CuentaCorriente cc = new CuentaCorriente(fechaLocalDate, tipoCC, comprobante, valorVenta, monto, 0,
                                    desc, valorNeto, valorIva, valorOtros);

                            // Le seteo el id del cliente, y no del proveedor para una mejor relacion
                            cc.setClienteId(cliente.getId());
                            cc.setProveedorId(null);

                            cc.setSaldo(0);
                            ccDAO.crearTransaccion(cc);

                            // Automaticamente se actualiza el saldo de la CC con el de las anteriores del cliente
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

                            // Esto es auxiliar, ya que es para que se acutalice la tabla con los nuevos cambios
                            BuscarController buscador = new BuscarController();
                            buscador.mostrarCuentasYResumen(nombrePersona, tablaCuentas, tablaProveedores, resumenClienteText);

                            // Limpio los campos que no sirven
                            fechaField.clear();
                            comprobanteField.clear();
                            tipoField.clear();
                            netoField.clear();
                            ivaField.clear();
                            otrosField.clear();
                            montoField.clear();
                            observacionField.clear();
                            return;
                        }

                        // Dado el caso de que no se haya encontrado el cliente, todo es igual pero con el proveedor
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
                            BuscarController buscador = new BuscarController();
                            buscador.mostrarCuentasYResumen(nombrePersona, tablaCuentas, tablaProveedores, resumenClienteText);

                            fechaField.clear();
                            comprobanteField.clear();
                            tipoField.clear();
                            netoField.clear();
                            ivaField.clear();
                            otrosField.clear();
                            montoField.clear();
                            observacionField.clear();
                            return;
                        }

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

                        // Mostrar comprovantes
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

                    // Creo el propducto, lo agrego y limpio los campos
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
                if (tipo.equals("Cliente")) {
                    String nameDelete = nombreField.getText().trim();

                    if (nameDelete.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Escribe un nombre para eliminar al cliente.");
                        alerta.showAndWait();
                        return;
                    }

                    // Si se encontro el cliente, confirmo le eliminacion
                    advertencia.setTitle("Advertencia");
                    advertencia.setContentText("Estás a punto de eliminar al cliente " + nameDelete + ". ¿Confirmas?");
                    Optional<ButtonType> result = advertencia.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        new ClienteDAO().eliminarCliente(nameDelete);
                        info.setTitle("Éxito");
                        info.setContentText("Cliente " + nameDelete + " eliminado.");
                        info.showAndWait();
                    } else {
                        info.setTitle("Cancelado");
                        info.setContentText("Operación de eliminación de Cliente cancelada.");
                        info.showAndWait();
                    }

                } else if (tipo.equals("Proveedor")) {
                    String nameDelete = nombreField.getText().trim();

                    if (nameDelete.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Escribe un nombre para eliminar al proveedor.");
                        alerta.showAndWait();
                        return;
                    }

                    // Lo mismo que con el cliente
                    advertencia.setTitle("Advertencia");
                    advertencia.setContentText("Estás a punto de eliminar al proveedor " + nameDelete + ". ¿Confirmas?");
                    Optional<ButtonType> result = advertencia.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        new ProveedorDAO().eliminarProveedor(nameDelete);
                        info.setTitle("Éxito");
                        info.setContentText("Proveedor " + nameDelete + " eliminado.");
                        info.showAndWait();
                    } else {
                        info.setTitle("Cancelado");
                        info.setContentText("Operación de eliminación de Proveedor cancelada.");
                        info.showAndWait();
                    }

                } else if (tipo.equals("Cuenta Corriente")) {
                    String nombreEntidad = nombreCuentaField.getText().trim();
                    String fechaStr = fechaField.getText().trim();
                    String comprobanteStr = comprobanteField.getText().trim();

                    if (nombreEntidad.isEmpty() || fechaStr.isEmpty() || comprobanteStr.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Para eliminar una Cuenta Corriente, debes ingresar el nombre del cliente/proveedor, la fecha y el número de comprobante.");
                        alerta.showAndWait();
                        return;
                    }

                    try {
                        LocalDate fecha = LocalDate.parse(fechaStr, formatter);

                        int entidadId = -1;
                        String tipoEntidadEncontrada = "";

                        ClienteDAO clienteDAO = new ClienteDAO();
                        Cliente cliente = clienteDAO.obtenerClientePorNombre(nombreEntidad);

                        if (cliente != null) {
                            entidadId = cliente.getId();
                            tipoEntidadEncontrada = "Cliente";
                        } else {
                            ProveedorDAO proveedorDAO = new ProveedorDAO();
                            Proveedor proveedor = proveedorDAO.obtenerProveedorPorNombre(nombreEntidad);
                            if (proveedor != null) {
                                entidadId = proveedor.getId();
                                tipoEntidadEncontrada = "Proveedor";
                            }
                        }

                        if (entidadId == -1) {
                            alerta.setTitle("Error");
                            alerta.setContentText("No se encontró ningún cliente o proveedor con el nombre: " + nombreEntidad);
                            alerta.showAndWait();
                            return;
                        }

                        CuentaCorrienteDAO cuentaCorrienteDAO = new CuentaCorrienteDAO();
                        CuentaCorriente transaccionAEliminar = null;

                        if (tipoEntidadEncontrada.equals("Cliente")) {
                            transaccionAEliminar = cuentaCorrienteDAO.obtenerTransaccionEspecifica(entidadId, null, fecha, comprobanteStr);
                        } else {
                            transaccionAEliminar = cuentaCorrienteDAO.obtenerTransaccionEspecifica(null, entidadId, fecha, comprobanteStr);
                        }

                        if (transaccionAEliminar == null) {
                            alerta.setTitle("Error");
                            alerta.setContentText("No se encontró ninguna transacción de Cuenta Corriente para " + nombreEntidad +
                                    " en la fecha " + fecha + " con comprobante " + comprobanteStr + ".");
                            alerta.showAndWait();
                            return;
                        }

                        advertencia.setTitle("Advertencia Crítica");
                        advertencia.setContentText("¡ADVERTENCIA! Estás a punto de ELIMINAR PERMANENTEMENTE la transacción de Cuenta Corriente (Comprobante: " + transaccionAEliminar.getComprobante() + ", Fecha: " + transaccionAEliminar.getFecha() + ", Entidad: " + nombreEntidad + "). Esta acción NO SE PUEDE DESHACER. ¿Estás absolutamente seguro?");
                        Optional<ButtonType> result = advertencia.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            // Elimino la transacción de la base de datos
                            boolean eliminada = cuentaCorrienteDAO.eliminarTransaccionPorId(transaccionAEliminar.getId());

                            if (eliminada) {
                                // Recalculo saldos si la eliminación fue exitosa
                                List<CuentaCorriente> todas;
                                if (tipoEntidadEncontrada.equals("Cliente")) {
                                    todas = cuentaCorrienteDAO.obtenerTodasLasTransaccionesOrdenadasPorFechaCliente(entidadId);
                                } else {
                                    todas = cuentaCorrienteDAO.obtenerTodasLasTransaccionesOrdenadasPorFechaProveedor(entidadId);
                                }

                                // Vuelvo a calcular el saldo de las cuentas
                                double saldoAcumulado = 0;
                                for (CuentaCorriente c : todas) {
                                    saldoAcumulado += c.getVenta() - c.getMonto();
                                    c.setSaldo(saldoAcumulado);
                                    cuentaCorrienteDAO.actualizarTransaccion(c);
                                }

                                info.setTitle("Éxito");
                                info.setContentText("Transacción de Cuenta Corriente (Comprobante: " + transaccionAEliminar.getComprobante() + ") eliminada permanentemente y saldos recalculados.");
                                info.showAndWait();

                                //Limpiar los campos de entrada
                                nombreCuentaField.clear();
                                fechaField.clear();
                                comprobanteField.clear();

                                // Vuelvo a mostrar la tabla modificada
                                BuscarController buscador = new BuscarController();
                                buscador.mostrarCuentasYResumen(nombreEntidad, tablaCuentas, tablaProveedores, resumenClienteText); // Pasa tus tablas y textflow
                            } else {
                                alerta.setTitle("Error");
                                alerta.setContentText("No se pudo eliminar la transacción de Cuenta Corriente.");
                                alerta.showAndWait();
                            }
                        } else {
                            info.setTitle("Cancelado");
                            info.setContentText("Operación de eliminación de Cuenta Corriente cancelada.");
                            info.showAndWait();
                        }

                    } catch (DateTimeParseException e) {
                        alerta.setTitle("Error de Fecha");
                        alerta.setContentText("El formato de fecha no es válido. Por favor, usa el formato " + formatter + ".");
                        alerta.showAndWait();
                    } catch (Exception e) {
                        System.err.println("Error general al procesar eliminación de Cuenta Corriente: " + e.getMessage());
                        e.printStackTrace();
                        alerta.setTitle("Error Interno");
                        alerta.setContentText("Ocurrió un error inesperado al intentar eliminar la Cuenta Corriente.");
                        alerta.showAndWait();
                    }
                } else if (tipo.equals("Producto")) {
                    String codigoProducto = codigoField.getText().trim();

                    if (codigoProducto.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Escribe un código de producto para eliminar.");
                        alerta.showAndWait();
                        return;
                    }

                    advertencia.setTitle("Advertencia");
                    advertencia.setContentText("Estás a punto de eliminar el producto con código " + codigoProducto + ". ¿Confirmas?");
                    Optional<ButtonType> result = advertencia.showAndWait();

                    // Todas las eliminaciones hacen lo mismo, piden confirmacion para una eliminacion
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        new ProductoDAO().eliminarProducto(codigoProducto);
                        info.setTitle("Éxito");
                        info.setContentText("Producto con código " + codigoProducto + " eliminado.");
                        info.showAndWait();
                    } else {
                        info.setTitle("Cancelado");
                        info.setContentText("Operación de eliminación de Producto cancelada.");
                        info.showAndWait();
                    }

                } else if (tipo.equals("Remito")) {
                    String numeroRemito = remitoField.getText().trim();

                    if (numeroRemito.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Escribe un número de remito para anular.");
                        alerta.showAndWait();
                        return;
                    }

                    advertencia.setTitle("Advertencia");
                    advertencia.setContentText("Estás a punto de eliminar el remito número " + numeroRemito + ". Deseas eliminar?");
                    Optional<ButtonType> result = advertencia.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            CuentaCorrienteDAO ccDAO = new CuentaCorrienteDAO();
                            CuentaCorriente cuentaCorrienteAEliminar = ccDAO.obtenerPorComprobante(numeroRemito);

                            // Antes de eliminar, busco que ese remito pertenezca a una cuenta corriente
                            if (cuentaCorrienteAEliminar != null) {
                                int idCuentaCorriente = cuentaCorrienteAEliminar.getId();
                                ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                                boolean cuentaCorrienteEliminada = comprobanteDAO.eliminarComprobantesPorCuentaCorrienteId(idCuentaCorriente);

                                if (cuentaCorrienteEliminada) {
                                    info.setTitle("Éxito");
                                    info.setContentText("Remito número " + numeroRemito + " eliminado.");
                                    info.showAndWait();
                                    remitoField.clear();
                                } else {
                                    alerta.setTitle("Error");
                                    alerta.setContentText("Error al eliminar " + numeroRemito + ". Inténtalo de nuevo.");
                                    alerta.showAndWait();
                                }
                            } else {
                                advertencia.setTitle("Advertencia");
                                advertencia.setContentText("No se encontró ninguna cuenta corriente con el número de remito: " + numeroRemito + ".");
                                advertencia.showAndWait();
                            }
                        } catch (Exception e) {
                            alerta.setTitle("Error");
                            alerta.setContentText("Ocurrió un error al intentar eliminar el remito: " + e.getMessage());
                            alerta.showAndWait();
                            e.printStackTrace();
                        }
                    } else {
                        info.setTitle("Cancelado");
                        info.setContentText("Operación de anulación de Remito cancelada.");
                        info.showAndWait();
                    }
                }
            } else if (accion.equals("Modificación")) {
                if (tipo.equals("Cliente")) {

                    String nombreClienteProveedorABuscar = getText(nombreField);

                    if (nombreClienteProveedorABuscar.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Debe ingresar el nombre del cliente/proveedor a modificar.");
                        alerta.showAndWait();
                        return;
                    }
                    ClienteDAO dao = new ClienteDAO();
                    Cliente existente = dao.obtenerClientePorNombre(nombreClienteProveedorABuscar);

                    if (existente == null) {
                        alerta.setTitle("Error");
                        alerta.setContentText("No se encontró el cliente " + nombreClienteProveedorABuscar);
                        alerta.showAndWait();
                    } else {

                        // Confirmación antes de modificar
                        advertencia.setTitle("Confirmar Modificación");
                        advertencia.setContentText("Estás a punto de modificar el cliente: " + existente.getNombre() + ". ¿Confirmas?");
                        Optional<ButtonType> result = advertencia.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            // Todos los campos verifican que se modifique todo lo vacio
                            // Tambien se puede modificar (M) automatizar la verificacion, y simplificar con el proveedor
                            String nuevoNombre = getText(nombreField);
                            if (!nuevoNombre.isEmpty()) existente.setNombre(nuevoNombre);
                            String razonSocial = getText(razonField);
                            if (!razonSocial.isEmpty()) existente.setRazonSocial(razonSocial);
                            String domicilio = getText(domicilioField);
                            if (!domicilio.isEmpty()) existente.setDomicilio(domicilio);
                            String localidad = getText(localidadField);
                            if (!localidad.isEmpty()) existente.setLocalidad(localidad);
                            String codigoPostal = getText(codigoPostalField);
                            if (!codigoPostal.isEmpty()) existente.setCodigoPostal(codigoPostal);
                            String telefono = getText(telefonoField);
                            if (!telefono.isEmpty()) existente.setTelefono(telefono);
                            String cuit = getText(cuitField);
                            if (!cuit.isEmpty()) existente.setCUIT(cuit);
                            String condicion = getText(condicionField);
                            if (!condicion.isEmpty()) existente.setCondicion(condicion);

                            String fechaTexto = getText(altaField);
                            if (!fechaTexto.isEmpty()) {
                                try {
                                    LocalDate fechaAlta = LocalDate.parse(fechaTexto, formatter);
                                    existente.setFechaAlta(fechaAlta);
                                } catch (DateTimeParseException e) {
                                    alerta.setTitle("Error de Formato");
                                    alerta.setContentText("El formato de fecha de alta no es válido. Por favor, use el formato " + formatter.toString() + ".");
                                    alerta.showAndWait();
                                    return; // Detiene la modificación si la fecha es inválida
                                }
                            }

                            String proveedor = getText(proveedorField);
                            if (!proveedor.isEmpty()) existente.setProveedor(proveedor);

                            dao.actualizarCliente(existente);
                            info.setTitle("Éxito");
                            info.setContentText("Cliente '" + existente.getNombre() + "' actualizado exitosamente.");
                            info.showAndWait();
                        } else {
                            info.setTitle("Cancelado");
                            info.setContentText("Operación de modificación de Cliente cancelada.");
                            info.showAndWait();
                        }
                    }

                } else if (tipo.equals("Proveedor")) {
                    // MISMAS ACLARACIONES QUE EL CLIENTE
                    String nombreClienteProveedorABuscar = getText(nombreField);

                    if (nombreClienteProveedorABuscar.isEmpty()) {
                        alerta.setTitle("Error");
                        alerta.setContentText("Debe ingresar el nombre del cliente/proveedor a modificar.");
                        alerta.showAndWait();
                        return;
                    }
                    ProveedorDAO dao = new ProveedorDAO();
                    Proveedor existente = dao.obtenerProveedorPorNombre(nombreClienteProveedorABuscar);

                    if (existente == null) {
                        alerta.setTitle("Error");
                        alerta.setContentText("No se encontró el proveedor " + nombreClienteProveedorABuscar);
                        alerta.showAndWait();
                    } else {

                        advertencia.setTitle("Confirmar Modificación");
                        advertencia.setContentText("Estás a punto de modificar el proveedor: " + existente.getNombre() + ". ¿Confirmas?");
                        Optional<ButtonType> result = advertencia.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            String nuevoNombre = getText(nombreField);
                            if (!nuevoNombre.isEmpty()) existente.setNombre(nuevoNombre);
                            String razonSocial = getText(razonField);
                            if (!razonSocial.isEmpty()) existente.setRazonSocial(razonSocial);
                            String domicilio = getText(domicilioField);
                            if (!domicilio.isEmpty()) existente.setDomicilio(domicilio);
                            String localidad = getText(localidadField);
                            if (!localidad.isEmpty()) existente.setLocalidad(localidad);
                            String codigoPostal = getText(codigoPostalField);
                            if (!codigoPostal.isEmpty()) existente.setCodigoPostal(codigoPostal);
                            String telefono = getText(telefonoField);
                            if (!telefono.isEmpty()) existente.setTelefono(telefono);
                            String cuit = getText(cuitField);
                            if (!cuit.isEmpty()) existente.setCUIT(cuit);
                            String categoria = getText(categoriaField);
                            if (!categoria.isEmpty()) existente.setCategoria(categoria);
                            String contacto = getText(contactoField);
                            if (!contacto.isEmpty()) existente.setContacto(contacto);

                            dao.actualizarProveedor(existente);
                            info.setTitle("Éxito");
                            info.setContentText("Proveedor '" + existente.getNombre() + "' actualizado exitosamente.");
                            info.showAndWait();
                        } else {
                            info.setTitle("Cancelado");
                            info.setContentText("Operación de modificación de Proveedor cancelada.");
                            info.showAndWait();
                        }
                    }
                } else if (tipo.equals("Producto")) {
                    // En todos los campos de verificacion para modificar, se podria reemplazar con un metodo (M)
                    String codigoProductoABuscar = getText(codigoField).trim();
                    ProductoDAO dao = new ProductoDAO();
                    Producto existente = dao.buscarProductoPorCodigo(codigoProductoABuscar);

                    if (existente == null) {
                        alerta.setTitle("Error");
                        alerta.setContentText("No se encontró el producto con código " + codigoProductoABuscar);
                        alerta.showAndWait();
                    } else {
                        advertencia.setTitle("Confirmar Modificación");
                        advertencia.setContentText("Estás a punto de modificar el producto: " + existente.getNombre() + " (Código: " + existente.getCodigo() + "). ¿Confirmas?");
                        Optional<ButtonType> result = advertencia.showAndWait();

                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            String nuevoCodigo = getText(codigoField).trim();
                            if (!nuevoCodigo.isEmpty() && !nuevoCodigo.equals(existente.getCodigo())) existente.setCodigo(nuevoCodigo);
                            String nuevoNombre = getText(nomProduField).trim();
                            if (!nuevoNombre.isEmpty()) existente.setNombre(nuevoNombre);

                            String precioStr = getText(preioUnitarioField).trim();
                            if (!precioStr.isEmpty()) {
                                try {
                                    double nuevoPrecio = Double.parseDouble(precioStr);
                                    existente.setPrecio(nuevoPrecio);
                                } catch (NumberFormatException e) {
                                    alerta.setTitle("Error de Formato");
                                    alerta.setContentText("El precio unitario debe ser un valor numérico válido.");
                                    alerta.showAndWait();
                                    return;
                                }
                            }

                            dao.modificarProducto(existente);
                            info.setTitle("Éxito");
                            info.setContentText("Producto '" + existente.getNombre() + "' actualizado exitosamente.");
                            info.showAndWait();

                            // Limpiar los campos después de la modificación exitosa
                            codigoField.clear();
                            nomProduField.clear();
                            preioUnitarioField.clear();

                        } else {
                            info.setTitle("Cancelado");
                            info.setContentText("Operación de modificación de Producto cancelada.");
                            info.showAndWait();
                        }
                    }

                } else if (tipo.equals("Cuenta Corriente")) {
                    alerta.setTitle("Error");
                    alerta.setContentText("No se permite modificar registros de Cuenta Corriente. Utilice la función de Baja si desea eliminar una transacción.");
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
    }

    // Método para el manejo de errores
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

    // Método que muestra los comprobantes de los clientes
    private void mostrarVentanaComprobantes(int movimientoId, String fecha, String persona) {
        ComprobanteDAO comprobanteDAO = null;
        List<Comprobante> comprobantes = null;

        try {
            comprobanteDAO = new ComprobanteDAO();
            comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(movimientoId);

            if (comprobantes == null) {
                comprobantes = new ArrayList<>();
            }

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
            precioCol.setCellFactory(AppController.getCurrencyFormatCellFactory());

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

        } catch (SQLException e) {
            System.err.println("Error de base de datos al mostrar comprobantes: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Ocurrió un error al cargar los comprobantes: " + e.getMessage());
        }
    }

    // Método que muestra los comprobantes de los proveedores
    private void mostrarVentanaComprobantesProveedores(int movimientoId, String fecha, String persona) {
        ComprobanteDAO comprobanteDAO = null;
        List<Comprobante> comprobantes = null;

        try {
            // Estas dos líneas pueden lanzar SQLException
            comprobanteDAO = new ComprobanteDAO();
            comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(movimientoId);

            // Asegurar que la lista no sea null antes de usarla, si no hubo excepción
            if (comprobantes == null) {
                comprobantes = new ArrayList<>();
            }

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
            totalCol.setCellFactory(AppController.getCurrencyFormatCellFactory());

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

        } catch (SQLException e) {
            System.err.println("Error de base de datos al mostrar comprobantes de proveedores: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Ocurrió un error al cargar los comprobantes de proveedores: " + e.getMessage());
        }
    }
}