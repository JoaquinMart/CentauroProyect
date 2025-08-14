package dao;

import model.*;
import util.ConexionMySQL;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CuentaCorrienteDAO {

    public CuentaCorrienteDAO() throws SQLException {

    }

    // Método para crear la cuenta corriente
    public void crearTransaccion(CuentaCorriente cuentaCorriente) throws SQLException {
        String query = "INSERT INTO cuenta_corriente(cliente_id, proveedor_id, fecha, tipo, comprobante, neto, iva, otros, venta, monto, saldo, observacion) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
        }
    }

    // Método para obtener todas las transacciones de un cliente
    public List<CuentaCorriente> obtenerMovimientosPorClienteId(int clienteId) throws SQLException {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? ORDER BY fecha DESC";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
        return movimientos;
    }

    // Método para obtener todas las transacciones de un proveedor
    public List<CuentaCorriente> obtenerMovimientosPorProveedorId(int proveedorId) throws SQLException {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? ORDER BY fecha DESC";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
        return movimientos;
    }

    // Método para obtener una cuenta corriente por su comprobante
    public CuentaCorriente obtenerPorComprobante(String numeroRemito) throws SQLException {
        CuentaCorriente cuenta = null;

        String query = "SELECT * FROM cuenta_corriente WHERE comprobante = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
        return cuenta;
    }

    // Método para actualizar una cuenta corriente
    public void actualizarTransaccion(CuentaCorriente cuentaCorriente) throws SQLException {
        String query = "UPDATE cuenta_corriente SET cliente_id = ?, proveedor_id = ?, fecha = ?, tipo = ?, " +
                "comprobante = ?, venta = ?, monto = ?, saldo = ?, observacion = ?, neto = ?, iva = ?, otros = ? WHERE id = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
    }

    // Método para obtener una cuenta corriente puntual
    public CuentaCorriente obtenerTransaccionEspecifica(Integer clienteId, Integer proveedorId, LocalDate fecha, String comprobante) throws SQLException {
        CuentaCorriente cuenta = null;
        String query;

        if (clienteId == null && proveedorId == null) {
            return null;
        }
        if (clienteId != null) {
            query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? AND fecha = ? AND comprobante = ?";
        } else {
            query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? AND fecha = ? AND comprobante = ?";
        }

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (clienteId != null) {
                pstmt.setInt(1, clienteId);
            } else {
                pstmt.setInt(1, proveedorId);
            }

            pstmt.setDate(2, Date.valueOf(fecha));
            pstmt.setString(3, comprobante);

            try (ResultSet rs = pstmt.executeQuery()) {
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
                    ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
                    List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                    cuenta.setComprobantes(comprobantes);
                }
            }
        }
        return cuenta;
    }

    // Método para eliminar una cuenta corriente
    public boolean eliminarTransaccionPorId(int idTransaccion) throws SQLException {
        String query = "DELETE FROM cuenta_corriente WHERE id = ?";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idTransaccion);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Transacción con ID " + idTransaccion + " eliminada físicamente de la base de datos.");
                return true;
            } else {
                System.out.println("No se encontró la transacción con ID " + idTransaccion + " para eliminar.");
                return false;
            }
        }
    }

    // Método para obtener todas las transacciones ordenadas por fecha de Clientes
    public List<CuentaCorriente> obtenerTodasLasTransaccionesOrdenadasPorFechaCliente(int clienteId) throws SQLException {
        List<CuentaCorriente> lista = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? ORDER BY fecha ASC";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
        return lista;
    }

    // Método para obtener todas las transacciones ordenadas por fecha de Proveedores
    public List<CuentaCorriente> obtenerTodasLasTransaccionesOrdenadasPorFechaProveedor(int proveedorId) throws SQLException {
        List<CuentaCorriente> lista = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? ORDER BY fecha ASC";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
        return lista;
    }

    // Método para obtener todas las transacciones ordenadas por fecha de los Clientes
    public List<CuentaCorriente> obtenerMovimientosPorClienteYFechas(int clienteId, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException { // ¡ÚNICO CAMBIO: Agregado throws SQLException!
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE cliente_id = ? AND fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, clienteId);
            pstmt.setDate(2, java.sql.Date.valueOf(fechaInicio));
            pstmt.setDate(3, java.sql.Date.valueOf(fechaFin));

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

                List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                cuenta.setComprobantes(comprobantes);
                movimientos.add(cuenta);
            }
        }
        return movimientos;
    }

    // Método para obtener todas las transacciones ordenadas por fecha de Proveedores y Tipo
    public List<CuentaCorriente> obtenerMovimientosPorProveedorYTipoYFechas(int idProveedor, String tipoMovimiento, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String query = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ? AND LOWER(tipo) = LOWER(?) AND fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        ComprobanteDAO comprobanteDAO = new ComprobanteDAO();

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idProveedor);
            pstmt.setString(2, tipoMovimiento);
            pstmt.setDate(3, java.sql.Date.valueOf(fechaInicio));
            pstmt.setDate(4, java.sql.Date.valueOf(fechaFin));

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
                    cuenta.setProveedorId(idProveedor);
                    List<Comprobante> comprobantes = comprobanteDAO.obtenerComprobantesPorCuentaCorrienteId(id);
                    cuenta.setComprobantes(comprobantes);

                    movimientos.add(cuenta);
                }
            }
        }
        return movimientos;
    }
}
