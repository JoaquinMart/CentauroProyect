package dao;

import model.*;
import util.ConexionMySQL;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteDAO {

    public ComprobanteDAO() throws SQLException {

    }

    // Método para crear el comprobante
    public void crearComprobante(Comprobante comprobante, int cuentaCorrienteId) throws SQLException {
        String query = "INSERT INTO comprobantes(cuenta_corriente_id, cantidad, nombre, precio) VALUES(?, ?, ?, ?)";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, cuentaCorrienteId);
            pstmt.setInt(2, comprobante.getCantidad());
            pstmt.setString(3, comprobante.getNombre());
            pstmt.setDouble(4, comprobante.getPrecio());

            int filas = pstmt.executeUpdate();

            if (filas > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int comprobanteId = generatedKeys.getInt(1);
                    comprobante.setId(comprobanteId);
                    System.out.println("Comprobante creado exitosamente con ID: " + comprobanteId);
                }
            }
        }
    }

    // Método para obtener todos los comprobantes de una cuenta corriente
    public List<Comprobante> obtenerComprobantesPorCuentaCorrienteId(int cuentaCorrienteId) throws SQLException {
        List<Comprobante> comprobantes = new ArrayList<>();
        String query = "SELECT * FROM comprobantes WHERE cuenta_corriente_id = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, cuentaCorrienteId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int cantidad = rs.getInt("cantidad");
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");

                Comprobante comprobante = new Comprobante(id, cantidad, nombre, precio);
                comprobantes.add(comprobante);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener comprobantes: " + e.getMessage());
        }
        return comprobantes;
    }

    // Método para eliminar todos los comprobantes de una cuenta corriente
    public boolean eliminarComprobantesPorCuentaCorrienteId(int cuentaCorrienteId) throws SQLException {
        String query = "DELETE FROM comprobantes WHERE cuenta_corriente_id = ?";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, cuentaCorrienteId);
            int filasAfectadas = pstmt.executeUpdate();
            System.out.println("Se eliminaron " + filasAfectadas + " comprobantes para la cuenta corriente ID: " + cuentaCorrienteId);
            return filasAfectadas > 0;
        }
    }

    // Método para obtener todos los comprobantes de un proveedor ordenados, en un rango de fechas
    public List<Comprobante> obtenerComprobantesDeProveedoresPorFechasYOrdenados(LocalDate fechaInicio, LocalDate fechaFin, String nombre) throws SQLException {
        List<Comprobante> comprobantes = new ArrayList<>();
        String sql = "SELECT c.id, c.cantidad, c.nombre, c.precio FROM comprobantes c " +
                "JOIN cuenta_corriente cc ON c.cuenta_corriente_id = cc.id " +
                "WHERE cc.proveedor_id IS NOT NULL AND cc.fecha BETWEEN ? AND ? ";

        if (nombre != null && !nombre.trim().isEmpty()) {
            sql += "AND c.nombre LIKE ? ";
        }
        sql += "ORDER BY c.nombre ASC";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(fechaInicio));
            pstmt.setDate(2, Date.valueOf(fechaFin));
            if (nombre != null && !nombre.trim().isEmpty()) {
                pstmt.setString(3, "%" + nombre + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comprobante comprobante = new Comprobante(
                            rs.getInt("id"),
                            rs.getInt("cantidad"),
                            rs.getString("nombre"),
                            rs.getDouble("precio")
                    );
                    comprobantes.add(comprobante);
                }
            }
        }
        return comprobantes;
    }
}
