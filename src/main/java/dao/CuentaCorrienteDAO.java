package dao;

import model.*;
import util.ConexionMySQL;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CuentaCorrienteDAO {
    private Connection connection;

    public CuentaCorrienteDAO() {
        this.connection = ConexionMySQL.conectar();
    }

    public void crearTransaccion(CuentaCorriente cuentaCorriente) {
        String query = "INSERT INTO cuenta_corriente(cliente_id, proveedor_id, fecha, tipo, comprobante, neto, iva, otros, venta, monto, saldo, observacion) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setObject(1, cuentaCorriente.getClienteId(), java.sql.Types.INTEGER);
            pstmt.setObject(2, cuentaCorriente.getProveedorId(), java.sql.Types.INTEGER);
            pstmt.setDate(3, Date.valueOf(cuentaCorriente.getFecha()));
            pstmt.setString(4, cuentaCorriente.getTipo());
            pstmt.setString(5, cuentaCorriente.getComprobante());
            pstmt.setDouble(6, cuentaCorriente.getNeto());
            pstmt.setDouble(7, cuentaCorriente.getIva());
            pstmt.setDouble(8, cuentaCorriente.getOtros());
            pstmt.setDouble(9, cuentaCorriente.getVenta());
            pstmt.setDouble(10, cuentaCorriente.getMonto());
            pstmt.setDouble(11, cuentaCorriente.getSaldo());
            pstmt.setString(12, cuentaCorriente.getObservacion());

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int transaccionId = generatedKeys.getInt(1);
                    cuentaCorriente.setId(transaccionId);
                    System.out.println("Transacción creada exitosamente con ID: " + transaccionId);

                    // Insertar los comprobantes asociados
                    if (cuentaCorriente.getComprobantes() != null && !cuentaCorriente.getComprobantes().isEmpty()) {
                        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                        for (Comprobante c : cuentaCorriente.getComprobantes()) {
                            comprobanteDAO.crearComprobante(c, transaccionId);
                        }
                    }
                } else {
                    System.err.println("Error: No se pudo obtener el ID generado para la transacción.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear transacción: " + e.getMessage());
        }
    }

    public List<CuentaCorriente> obtenerMovimientosPorClienteId(int clienteId) {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? ORDER BY fecha ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();
            ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String tipo = rs.getString("tipo");
                String comprobante = rs.getString("comprobante");
                double venta = rs.getDouble("venta");
                double monto = rs.getDouble("monto");
                double saldo = rs.getDouble("saldo");
                String observacion = rs.getString("observacion");
                double neto = rs.getDouble("neto");
                double iva = rs.getDouble("iva");
                double otros = rs.getDouble("otros");

                CuentaCorriente cuenta = new CuentaCorriente(id, fecha, tipo, comprobante, venta, monto, saldo,
                        observacion, neto, iva, otros);
                cuenta.setClienteId(clienteId);

                // Cargar comprobantes asociados
                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cuenta.setComprobantes(comprobantes);

                movimientos.add(cuenta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por cliente: " + e.getMessage());
        }
        return movimientos;
    }

    public List<CuentaCorriente> obtenerMovimientosPorProveedorId(int proveedorId) {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? ORDER BY fecha ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, proveedorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String tipo = rs.getString("tipo");
                String comprobante = rs.getString("comprobante");
                double venta = rs.getDouble("venta");
                double monto = rs.getDouble("monto");
                double saldo = rs.getDouble("saldo");
                String observacion = rs.getString("observacion");
                double neto = rs.getDouble("neto");
                double iva = rs.getDouble("iva");
                double otros = rs.getDouble("otros");

                CuentaCorriente cuenta = new CuentaCorriente(id, fecha, tipo, comprobante, venta, monto, saldo,
                        observacion, neto, iva, otros);
                cuenta.setProveedorId(proveedorId);

                // Cargar comprobantes asociados
                ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cuenta.setComprobantes(comprobantes);

                movimientos.add(cuenta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por proveedor: " + e.getMessage());
        }
        return movimientos;
    }

    public CuentaCorriente obtenerPorComprobante(String numeroRemito) {
        CuentaCorriente cuenta = null;

        String query = "SELECT * FROM cuenta_corriente WHERE comprobante = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, numeroRemito);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String tipo = rs.getString("tipo");
                String comprobante = rs.getString("comprobante");
                double venta = rs.getDouble("venta");
                double monto = rs.getDouble("monto");
                double saldo = rs.getDouble("saldo");
                String observacion = rs.getString("observacion");
                double neto = rs.getDouble("neto");
                double iva = rs.getDouble("iva");
                double otros = rs.getDouble("otros");

                cuenta = new CuentaCorriente(id, fecha, tipo, comprobante, venta, monto, saldo, observacion, neto, iva, otros);

                int clienteId = rs.getInt("cliente_id");
                cuenta.setClienteId(clienteId);

                // Cargar comprobantes asociados
                ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cuenta.setComprobantes(comprobantes);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar CuentaCorriente por número de remito: " + e.getMessage());
        }

        return cuenta;
    }

    public void actualizarCuentaCorriente(CuentaCorriente cuenta) {
        String query = "UPDATE cuenta_corriente SET cliente_id = ?, proveedor_id = ?, fecha = ?, tipo = ?, venta = ?, monto = ?, saldo = ?, observacion = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, cuenta.getClienteId(), java.sql.Types.INTEGER);
            pstmt.setObject(2, cuenta.getProveedorId(), java.sql.Types.INTEGER);
            pstmt.setDate(3, Date.valueOf(cuenta.getFecha()));
            pstmt.setString(4, cuenta.getTipo());
            pstmt.setDouble(5, cuenta.getVenta());
            pstmt.setDouble(6, cuenta.getMonto());
            pstmt.setDouble(7, cuenta.getSaldo());
            pstmt.setString(8, cuenta.getObservacion());
            pstmt.setInt(9, cuenta.getId());

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Cuenta corriente actualizada correctamente.");
            } else {
                System.out.println("No se encontró una cuenta corriente con el ID especificado.");
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar cuenta corriente: " + e.getMessage());
        }
    }

    public void actualizarTransaccion(CuentaCorriente cuentaCorriente) {
        String query = "UPDATE cuenta_corriente SET cliente_id = ?, proveedor_id = ?, fecha = ?, tipo = ?, " +
                "comprobante = ?, venta = ?, monto = ?, saldo = ?, observacion = ?, neto = ?, iva = ?, otros = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, cuentaCorriente.getClienteId(), java.sql.Types.INTEGER);
            pstmt.setObject(2, cuentaCorriente.getProveedorId(), java.sql.Types.INTEGER);
            pstmt.setDate(3, Date.valueOf(cuentaCorriente.getFecha()));
            pstmt.setString(4, cuentaCorriente.getTipo());
            pstmt.setString(5, cuentaCorriente.getComprobante());
            pstmt.setDouble(6, cuentaCorriente.getVenta());
            pstmt.setDouble(7, cuentaCorriente.getMonto());
            pstmt.setDouble(8, cuentaCorriente.getSaldo());
            pstmt.setString(9, cuentaCorriente.getObservacion());
            pstmt.setDouble(10, cuentaCorriente.getNeto());
            pstmt.setDouble(11, cuentaCorriente.getIva());
            pstmt.setDouble(12, cuentaCorriente.getOtros());
            pstmt.setInt(13, cuentaCorriente.getId());

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                System.out.println("Transacción actualizada correctamente.");
            } else {
                System.out.println("No se encontró la transacción con el ID especificado.");
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar transacción: " + e.getMessage());
        }
    }

    public List<CuentaCorriente> obtenerTodasLasTransaccionesOrdenadasPorFechaCliente(int clienteId) {
        List<CuentaCorriente> lista = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? ORDER BY fecha ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, clienteId);
            ResultSet rs = pstmt.executeQuery();
            ComprobanteDAO comprobanteDAO = new ComprobanteDAO();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String tipo = rs.getString("tipo");
                String comprobante = rs.getString("comprobante");
                double venta = rs.getDouble("venta");
                double monto = rs.getDouble("monto");
                double saldo = rs.getDouble("saldo");
                String observacion = rs.getString("observacion");
                double neto = rs.getDouble("neto");
                double iva = rs.getDouble("iva");
                double otros = rs.getDouble("otros");

                CuentaCorriente cc = new CuentaCorriente(id, fecha, tipo, comprobante, venta, monto, saldo, observacion,
                        neto, iva, otros);
                cc.setClienteId(clienteId);

                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cc.setComprobantes(comprobantes);

                lista.add(cc);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las transacciones: " + e.getMessage());
        }

        return lista;
    }

    public List<CuentaCorriente> obtenerTodasLasTransaccionesOrdenadasPorFechaProveedor(int proveedorId) {
        List<CuentaCorriente> lista = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? ORDER BY fecha ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, proveedorId);
            ResultSet rs = pstmt.executeQuery();
            ComprobanteDAO comprobanteDAO = new ComprobanteDAO();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                String tipo = rs.getString("tipo");
                String comprobante = rs.getString("comprobante");
                double venta = rs.getDouble("venta");
                double monto = rs.getDouble("monto");
                double saldo = rs.getDouble("saldo");
                String observacion = rs.getString("observacion");
                double neto = rs.getDouble("neto");
                double iva = rs.getDouble("iva");
                double otros = rs.getDouble("otros");

                CuentaCorriente cc = new CuentaCorriente(id, fecha, tipo, comprobante, venta, monto, saldo, observacion,
                        neto, iva, otros);
                cc.setProveedorId(proveedorId);

                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cc.setComprobantes(comprobantes);

                lista.add(cc);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener todas las transacciones: " + e.getMessage());
        }

        return lista;
    }

}
