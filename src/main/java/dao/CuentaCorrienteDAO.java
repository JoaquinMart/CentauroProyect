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

    public CuentaCorriente obtenerTransaccionEspecifica(Integer clienteId, Integer proveedorId, LocalDate fecha, String comprobante) {
        CuentaCorriente cuenta = null;
        String query;
        PreparedStatement pstmt;

        try {
            if (clienteId != null) {
                query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? AND fecha = ? AND comprobante = ?";
                pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, clienteId);
            } else if (proveedorId != null) {
                query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? AND fecha = ? AND comprobante = ?";
                pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, proveedorId);
            } else {
                return null; // O lanzar una excepción si no se proporciona ni clienteId ni proveedorId
            }

            pstmt.setDate(2, Date.valueOf(fecha));
            pstmt.setString(3, comprobante);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                LocalDate dbFecha = rs.getDate("fecha").toLocalDate();
                String tipo = rs.getString("tipo");
                String dbComprobante = rs.getString("comprobante");
                double venta = rs.getDouble("venta");
                double monto = rs.getDouble("monto");
                double saldo = rs.getDouble("saldo");
                String observacion = rs.getString("observacion");
                double neto = rs.getDouble("neto");
                double iva = rs.getDouble("iva");
                double otros = rs.getDouble("otros");

                cuenta = new CuentaCorriente(id, dbFecha, tipo, dbComprobante, venta, monto, saldo, observacion, neto, iva, otros);
                if (clienteId != null) {
                    cuenta.setClienteId(clienteId);
                } else {
                    cuenta.setProveedorId(proveedorId);
                }

                // Cargar comprobantes asociados si tu lógica los necesita al recuperar
                ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cuenta.setComprobantes(comprobantes);
            }
            pstmt.close(); // Cerrar el PreparedStatement
        } catch (SQLException e) {
            System.err.println("Error al buscar CuentaCorriente específica: " + e.getMessage());
            e.printStackTrace();
        }
        return cuenta;
    }

    public boolean eliminarTransaccionPorId(int idTransaccion) {
        String query = "DELETE FROM cuenta_corriente WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idTransaccion);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Transacción con ID " + idTransaccion + " eliminada físicamente de la base de datos.");
                return true;
            } else {
                System.out.println("No se encontró la transacción con ID " + idTransaccion + " para eliminar.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar transacción físicamente: " + e.getMessage());
            // Aquí puedes lanzar una excepción personalizada o manejarla de forma más robusta
            return false;
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

    public List<CuentaCorriente> obtenerMovimientosPorClienteYFechas(int clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? AND fecha BETWEEN ? AND ? ORDER BY fecha ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, clienteId);
            pstmt.setDate(2, Date.valueOf(fechaInicio)); // Convierte LocalDate a java.sql.Date
            pstmt.setDate(3, Date.valueOf(fechaFin));     // Convierte LocalDate a java.sql.Date

            ResultSet rs = pstmt.executeQuery();
            ComprobanteDAO comprobanteDAO = new ComprobanteDAO(); // Asumiendo que tienes un ComprobanteDAO para cargar los comprobantes asociados

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

                // Cargar comprobantes asociados (si aplica y ComprobanteDAO lo permite)
                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cuenta.setComprobantes(comprobantes);

                movimientos.add(cuenta);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos de cuenta corriente por cliente y fechas: " + e.getMessage());
        }
        return movimientos;
    }

    public List<CuentaCorriente> obtenerMovimientosPorProveedorYTipoYFechas(int idProveedor, String tipoMovimiento, LocalDate fechaInicio, LocalDate fechaFin) {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        // Query para filtrar por id_proveedor, tipo y rango de fechas
        String query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? AND LOWER(tipo) = LOWER(?) AND fecha BETWEEN ? AND ? ORDER BY fecha ASC";
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO(); // Instancia para cargar comprobantes asociados

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, idProveedor);
            pstmt.setString(2, tipoMovimiento);
            pstmt.setDate(3, java.sql.Date.valueOf(fechaInicio)); // Convierte LocalDate a java.sql.Date
            pstmt.setDate(4, java.sql.Date.valueOf(fechaFin));     // Convierte LocalDate a java.sql.Date

            try (ResultSet rs = pstmt.executeQuery()) {
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
                    // Asegúrate de establecer el ID del proveedor, ya que es relevante para este método
                    cuenta.setProveedorId(idProveedor);

                    // Cargar comprobantes asociados (si tu lógica lo requiere)
                    List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                    cuenta.setComprobantes(comprobantes);

                    movimientos.add(cuenta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos de cuenta corriente por proveedor, tipo y fechas: " + e.getMessage());
            e.printStackTrace();
        }
        return movimientos;
    }
}
