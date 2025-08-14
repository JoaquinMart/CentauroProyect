package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionMySQL {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USUARIO = dotenv.get("DB_USER");
    private static final String CLAVE = dotenv.get("DB_PASSWORD");

    private static HikariDataSource dataSource;
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USUARIO);
        config.setPassword(CLAVE);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    // Este método ahora es mucho más rápido porque no crea la conexión, solo la pide del pool
    public static Connection conectar() throws SQLException {
        return dataSource.getConnection();
    }

    // Método para cerrar el pool de conexiones cuando la aplicación termine
    public static void cerrarDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // Método para crear las tablas de la base de datos si no existen
    public static void crearTablas(Connection conn) {
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

        // neto, iva y otros
        String sqlCuentaCorriente = """
            CREATE TABLE IF NOT EXISTS cuenta_corriente (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cliente_id INT NULL,
                proveedor_id INT NULL,
                fecha DATE,
                tipo VARCHAR(50),
                comprobante VARCHAR(50),
                neto DECIMAL(15, 2), 
                iva DECIMAL(15,2),
                otros DECIMAL(15,2),
                venta DECIMAL(15,2),
                monto DECIMAL(15,2),
                saldo DECIMAL(15,2),
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
                precio DECIMAL(15,2) NOT NULL,
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
        try {
            conectar();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


