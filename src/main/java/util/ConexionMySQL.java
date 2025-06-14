package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionMySQL {

    private static final String URL = "jdbc:mysql://localhost:3306/centauro?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USUARIO = "joaquin";
    private static final String CLAVE = "Colore2015";

    public static Connection conectar() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("Conexión exitosa a MySQL.");
            crearTablas(conn);
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conn;
    }

    private static void crearTablas(Connection conn) {
        String sqlClientes = """
            CREATE TABLE IF NOT EXISTS clientes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(255) NOT NULL,
                razonSocial VARCHAR(255),
                domicilio VARCHAR(255),
                localidad VARCHAR(255),
                codigoPostal VARCHAR(20),
                telefono VARCHAR(50),
                CUIT VARCHAR(20),
                condicion VARCHAR(50),
                fechaAlta DATE,
                proveedor VARCHAR(255)
            );
        """;

        String sqlProveedores = """
            CREATE TABLE IF NOT EXISTS proveedores (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(255) NOT NULL,
                razonSocial VARCHAR(255),
                domicilio VARCHAR(255),
                localidad VARCHAR(255),
                codigoPostal VARCHAR(20),
                telefono VARCHAR(50),
                CUIT VARCHAR(20),
                categoria VARCHAR(50),
                contacto VARCHAR(100)
            );
        """;

        String sqlCuentaCorriente = """
            CREATE TABLE IF NOT EXISTS cuenta_corriente (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cliente_id INT NULL,
                proveedor_id INT NULL,
                fecha DATE,
                tipo VARCHAR(50),
                comprobante VARCHAR(50),
                venta DECIMAL(10,2),
                monto DECIMAL(10,2),
                saldo DECIMAL(10,2),
                observacion TEXT,
                FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
                FOREIGN KEY (proveedor_id) REFERENCES proveedores(id) ON DELETE CASCADE
            );
        """;

        String sqlComprobantes = """
            CREATE TABLE IF NOT EXISTS comprobantes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cuenta_corriente_id INT NOT NULL,
                cantidad INT NOT NULL,
                nombre VARCHAR(255) NOT NULL,
                precio DECIMAL(10,2) NOT NULL,
                FOREIGN KEY (cuenta_corriente_id) REFERENCES cuenta_corriente(id) ON DELETE CASCADE
                );
        """;

        String sqlProductos = """
            CREATE TABLE IF NOT EXISTS productos (
                id INT AUTO_INCREMENT PRIMARY KEY,
                codigo INT NOT NULL,
                nombre VARCHAR(255) NOT NULL,
                precio DECIMAL(10,2) NOT NULL
                );
        """;


        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlClientes);
            stmt.execute(sqlProveedores);
            stmt.execute(sqlCuentaCorriente);
            stmt.execute(sqlComprobantes);
            stmt.execute(sqlProductos);
            System.out.println("Tablas creadas exitosamente.");
        } catch (SQLException e) {
            System.out.println("Error al crear las tablas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        conectar();
    }
}


