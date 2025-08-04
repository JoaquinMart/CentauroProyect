package dao;

import util.ConexionMySQL; // Importa tu utilidad de conexión
import model.Producto;     // Importa el modelo Producto (asegúrate de tenerlo en el paquete 'model')
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet; // Necesario para obtener las claves generadas (ID)
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
    public boolean crearProducto(Producto producto) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, precio) VALUES (?, ?, ?)";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        producto.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Producto '" + producto.getNombre() + "' creado exitosamente con ID: " + producto.getId());
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean modificarProducto(Producto producto) throws SQLException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, precio = ? WHERE id = ?";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto con ID " + producto.getId() + " modificado exitosamente.");
                return true;
            } else {
                return false;
            }
        }
    }

    public List<Producto> obtenerProductos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, precio FROM productos ORDER BY codigo ASC";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Producto producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio")
                );
                productos.add(producto);
            }
            System.out.println("Productos listados y ordenados por código exitosamente.");
        }
        return productos;
    }

    public Producto buscarProductoPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT id, codigo, nombre, precio FROM productos WHERE codigo = ?";
        Producto producto = null;
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto = new Producto(
                            rs.getInt("id"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getDouble("precio")
                    );
                }
            }
        }
        return producto;
    }

    public boolean eliminarProducto(String codigoProducto) throws SQLException {
        String query = "DELETE FROM productos WHERE codigo = ?";
        try (Connection conn = ConexionMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("Error: No se pudo obtener conexión para eliminar producto.");
                return false;
            }
            pstmt.setString(1, codigoProducto);
            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Producto con código " + codigoProducto + " eliminado físicamente.");
                return true;
            } else {
                System.out.println("No se encontró el producto con código " + codigoProducto + " para eliminar.");
                return false;
            }
        }
    }
}