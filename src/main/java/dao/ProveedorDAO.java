package dao;

import model.CuentaCorriente;
import model.Proveedor;
import util.ConexionMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class ProveedorDAO {

    // Método para crear un nuevo proveedor en la base de datos
    public void crearProveedor(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO Proveedores (nombre, razonSocial, domicilio, localidad, codigoPostal, telefono, CUIT, categoria, contacto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, proveedor.getNombre());
            stmt.setString(2, proveedor.getRazonSocial());
            stmt.setString(3, proveedor.getDomicilio());
            stmt.setString(4, proveedor.getLocalidad());
            stmt.setString(5, proveedor.getCodigoPostal());
            stmt.setString(6, proveedor.getTelefono());
            stmt.setString(7, proveedor.getCUIT());
            stmt.setString(8, proveedor.getCategoria());
            stmt.setString(9, proveedor.getContacto());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Crear proveedor falló, no se insertó ninguna fila.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    proveedor.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Crear proveedor falló, no se obtuvo el ID generado.");
                }
            }

            System.out.println("model.Proveedor creado exitosamente. ID: " + proveedor.getId());

        }
    }

    // Método para obtener un proveedor nombre
    public Proveedor obtenerProveedorPorNombre(String nombre) throws SQLException {
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM proveedores WHERE nombre = ?")) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Proveedor proveedor = new Proveedor(
                        rs.getString("nombre"),
                        rs.getString("razonSocial"),
                        rs.getString("domicilio"),
                        rs.getString("localidad"),
                        rs.getString("codigoPostal"),
                        rs.getString("telefono"),
                        rs.getString("CUIT"),
                        rs.getString("categoria"),
                        rs.getString("contacto")
                );
                proveedor.setId(rs.getInt("id")); // <-- esta línea es importante
                return proveedor;
            }
        }
        return null;
    }

    // Método para eliminar un proveedor por su nombre
    public void eliminarProveedor(String nombre) throws SQLException {
        String sql = "DELETE FROM Proveedores WHERE nombre = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("model.Proveedor eliminado exitosamente.");
            } else {
                System.out.println("model.Proveedor no encontrado.");
            }
        }
    }

    // Método para actualizar la información de un proveedor
    public void actualizarProveedor(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE Proveedores SET nombre = ?, razonSocial = ?, domicilio = ?, localidad = ?, codigoPostal = ?, telefono = ?, CUIT = ?, categoria = ?, contacto = ? WHERE id = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, proveedor.getNombre());
            pstmt.setString(2, proveedor.getRazonSocial());
            pstmt.setString(3, proveedor.getDomicilio());
            pstmt.setString(4, proveedor.getLocalidad());
            pstmt.setString(5, proveedor.getCodigoPostal());
            pstmt.setString(6, proveedor.getTelefono());
            pstmt.setString(7, proveedor.getCUIT());
            pstmt.setString(8, proveedor.getCategoria());
            pstmt.setString(9, proveedor.getContacto());
            pstmt.setInt(10, proveedor.getId());

            pstmt.executeUpdate();
        }
    }

    // Método para obtener los movimientos de cuenta corriente de un cliente por su ID
    public List<CuentaCorriente> obtenerMovimientosPorProveedorId(int proveedorId) throws SQLException {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM cuenta_corriente WHERE proveedor_id = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, proveedorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CuentaCorriente movimiento = new CuentaCorriente(
                        rs.getInt("id"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getString("tipo"),
                        rs.getString("comprobante"),
                        rs.getDouble("venta"),
                        rs.getDouble("monto"),
                        rs.getDouble("saldo"),
                        rs.getString("observacion"),
                        rs.getDouble("neto"),
                        rs.getDouble("iva"),
                        rs.getDouble("otros")
                );
                movimientos.add(movimiento);
            }
        }
        return movimientos;
    }

    // Método para obtener todos los proveedores
    public List<Proveedor> obtenerTodosLosProveedores() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM Proveedores";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Proveedor proveedor = new Proveedor(
                        rs.getString("nombre"),
                        rs.getString("razonSocial"),
                        rs.getString("domicilio"),
                        rs.getString("localidad"),
                        rs.getString("codigoPostal"),
                        rs.getString("telefono"),
                        rs.getString("CUIT"),
                        rs.getString("categoria"),
                        rs.getString("contacto")
                );
                proveedor.setId(rs.getInt("id"));
                proveedores.add(proveedor);
            }
        }
        return proveedores;
    }

    // Método proveedores con sus cuentas
    public List<Proveedor> obtenerProveedoresConCuentas() throws SQLException {
        List<Proveedor> proveedores = obtenerTodosLosProveedores(); // Ya lo tenés hecho
        for (Proveedor proveedor : proveedores) {
            List<CuentaCorriente> cuentas = obtenerMovimientosPorProveedorId(proveedor.getId());
            for (CuentaCorriente cuenta : cuentas) {
                proveedor.agregarCuentaCorriente(cuenta);
            }
        }
        return proveedores;
    }

}
