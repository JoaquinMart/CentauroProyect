package dao;

import model.*;
import util.ConexionMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.Statement;

public class ClienteDAO {

    // Método para crear un nuevo cliente
    public void crearCliente(Cliente cliente) {
        String sql = "INSERT INTO Clientes(nombre, razonSocial, domicilio, localidad, codigoPostal, telefono, CUIT, condicion, fechaAlta, proveedor) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getRazonSocial());
            pstmt.setString(3, cliente.getDomicilio());
            pstmt.setString(4, cliente.getLocalidad());
            pstmt.setString(5, cliente.getCodigoPostal());
            pstmt.setString(6, cliente.getTelefono());
            pstmt.setString(7, cliente.getCUIT());
            pstmt.setString(8, cliente.getCondicion());
            java.sql.Date sqlDate = java.sql.Date.valueOf(cliente.getFechaAlta());
            pstmt.setDate(9, sqlDate);
            pstmt.setString(10, cliente.getProveedor());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Crear cliente falló, no se insertó ninguna fila.");
            }

            // Obtener el ID generado
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Crear cliente falló, no se obtuvo el ID generado.");
                }
            }

            System.out.println("model.Cliente creado exitosamente. ID: " + cliente.getId());

        } catch (SQLException e) {
            System.out.println("Error al crear cliente: " + e.getMessage());
        }
    }

    // Método para obtener cliente por Nombre
    public Cliente obtenerClientePorNombre(String nombre) {
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clientes WHERE nombre = ?")) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getString("nombre"),
                        rs.getString("razonSocial"),
                        rs.getString("domicilio"),
                        rs.getString("localidad"),
                        rs.getString("codigoPostal"),
                        rs.getString("telefono"),
                        rs.getString("CUIT"),
                        rs.getString("condicion"),
                        rs.getDate("fechaAlta").toLocalDate(),
                        rs.getString("proveedor")
                );
                cliente.setId(rs.getInt("id"));  // Asignás el ID acá
                return cliente;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método para eliminar cliente
    public void eliminarCliente(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre ingresado no es válido.");
            return;
        }

        String sql = "DELETE FROM Clientes WHERE nombre = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("¿Está seguro que desea eliminar al cliente '" + nombre + "'? (s/n)");
            Scanner sc = new Scanner(System.in);
            String confirm = sc.nextLine();

            if (!confirm.equalsIgnoreCase("s")) {
                System.out.println("Operación cancelada.");
                return;
            }

            pstmt.setString(1, nombre);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("model.Cliente eliminado exitosamente.");
            } else {
                System.out.println("model.Cliente no encontrado.");
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar cliente: " + e.getMessage());
        }
    }

    // Método para actualizar cliente
    public void actualizarCliente(Cliente cliente) {
        String sql = "UPDATE Clientes SET nombre = ?, razonSocial = ?, domicilio = ?, localidad = ?, codigoPostal = ?, " +
                "telefono = ?, CUIT = ?, condicion = ?, fechaAlta = ?, proveedor = ? WHERE id = ?";

        try (Connection conn = ConexionMySQL.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getRazonSocial());
            pstmt.setString(3, cliente.getDomicilio());
            pstmt.setString(4, cliente.getLocalidad());
            pstmt.setString(5, cliente.getCodigoPostal());
            pstmt.setString(6, cliente.getTelefono());
            pstmt.setString(7, cliente.getCUIT());
            pstmt.setString(8, cliente.getCondicion());
            pstmt.setDate(9, java.sql.Date.valueOf(cliente.getFechaAlta()));
            pstmt.setString(10, cliente.getProveedor());
            pstmt.setInt(11, cliente.getId());
            pstmt.executeUpdate();
            System.out.println("model.Cliente actualizado exitosamente.");
        } catch (SQLException e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
        }
    }

    // Método para obtener los movimientos de cuenta corriente de un cliente por su ID
    public List<CuentaCorriente> obtenerMovimientosPorClienteId(int clienteId) {
        List<CuentaCorriente> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM cuenta_corriente WHERE cliente_id = ?";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clienteId);
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
                        rs.getString("observacion")
                );
                movimientos.add(movimiento);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener movimientos de cuenta corriente: " + e.getMessage());
        }

        return movimientos;
    }

    // Método para obtener todos los clientes
    public List<Cliente> obtenerTodosLosClientes() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getString("nombre"),
                        rs.getString("razonSocial"),
                        rs.getString("domicilio"),
                        rs.getString("localidad"),
                        rs.getString("codigoPostal"),
                        rs.getString("telefono"),
                        rs.getString("CUIT"),
                        rs.getString("condicion"),
                        rs.getDate("fechaAlta").toLocalDate(),
                        rs.getString("proveedor")
                );
                cliente.setId(rs.getInt("id"));
                clientes.add(cliente);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener todos los clientes: " + e.getMessage());
        }

        return clientes;
    }

    // Método para obtener todas las cuentas de un cliente
    public List<Cliente> obtenerClientesConCuentas() throws SQLException {
        List<Cliente> clientes = obtenerTodosLosClientes(); // Usamos tu método ya hecho
        for (Cliente cliente : clientes) {
            // Supone que ya existe este método en tu DAO
            List<CuentaCorriente> cuentas = obtenerMovimientosPorClienteId(cliente.getId());
            // Asumiendo que tu clase Cliente tiene este método
            cliente.setCuentaCorrientes(cuentas);
        }
        return clientes;
    }

}

