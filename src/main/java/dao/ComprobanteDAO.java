package dao;

import model.*;
import util.ConexionMySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteDAO {
    private Connection connection;

    public ComprobanteDAO() {
        this.connection = ConexionMySQL.conectar();
    }

    public void crearComprobante(Comprobante comprobante, int cuentaCorrienteId) {
        String query = "INSERT INTO comprobantes(cuenta_corriente_id, cantidad, nombre, precio) VALUES(?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) {
            System.err.println("Error al crear comprobante: " + e.getMessage());
        }
    }

    public List<Comprobante> obtenerComprobantesPorCuentaCorrienteId(int cuentaCorrienteId) {
        List<Comprobante> comprobantes = new ArrayList<>();
        String query = "SELECT * FROM comprobantes WHERE cuenta_corriente_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
}
