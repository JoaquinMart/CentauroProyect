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
    public boolean crearProducto(Producto producto) {

        String sql = "INSERT INTO productos (codigo, nombre, precio) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = ConexionMySQL.conectar();
            if (conn == null) {
                System.out.println("Error: No se pudo obtener conexión para crear producto.");
                return false;
            }
            pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    producto.setId(generatedKeys.getInt(1));
                }
                System.out.println("Producto '" + producto.getNombre() + "' creado exitosamente con ID: " + producto.getId());
                return true;
            } else {
                System.out.println("No se pudo crear el producto: " + producto.getNombre());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error al crear el producto en la base de datos: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos al crear producto: " + e.getMessage());
            }
        }
    }

    public boolean modificarProducto(Producto producto) {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, precio = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConexionMySQL.conectar();
            if (conn == null) {
                System.out.println("Error: No se pudo obtener conexión para modificar producto.");
                return false;
            }

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, producto.getPrecio());
            pstmt.setInt(4, producto.getId()); // Establecer el ID para la cláusula WHERE

            // Ejecutar la actualización
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Producto con ID " + producto.getId() + " modificado exitosamente.");
                return true;
            } else {
                System.out.println("No se encontró el producto con ID " + producto.getId() + " para modificar.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error al modificar el producto en la base de datos: " + e.getMessage());
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos al modificar producto: " + e.getMessage());
            }
        }
    }

    public List<Producto> obtenerProductos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        // La clave aquí es la cláusula ORDER BY codigo ASC
        String sql = "SELECT id, codigo, nombre, precio FROM productos ORDER BY codigo ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConexionMySQL.conectar();
            if (conn == null) {
                System.out.println("Error: No se pudo obtener conexión para listar productos.");
                return productos;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

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

        } catch (SQLException e) {
            System.err.println("Error al listar productos ordenados por código: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos al listar productos: " + e.getMessage());
            }
        }
        return productos;
    }

    public Producto buscarProductoPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT id, codigo, nombre, precio FROM productos WHERE codigo = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Producto producto = null;

        try {
            conn = ConexionMySQL.conectar();
            if (conn == null) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos.");
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, codigo); // Establecer el código como parámetro
            rs = pstmt.executeQuery();

            if (rs.next()) { // Si se encuentra una fila
                producto = new Producto(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getDouble("precio")
                );
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos al buscar producto por código: " + e.getMessage());
            }
        }
        return producto;
    }

    public boolean eliminarProducto(String codigoProducto) {
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
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}