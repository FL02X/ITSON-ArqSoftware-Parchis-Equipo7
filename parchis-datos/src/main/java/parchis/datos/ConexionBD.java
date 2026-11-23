package parchis.datos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones a MySQL con los datos de db.properties.
 *
 * <p>El archivo se busca primero en la carpeta desde donde se corre el programa
 * y luego en el classpath. NUNCA se sube a GitHub (esta en .gitignore);
 * en el repositorio solo va db.properties.ejemplo.</p>
 */
public final class ConexionBD {

    private static final String ARCHIVO = "db.properties";
    private static Properties config;

    private ConexionBD() {
    }

    public static Connection obtener() throws SQLException {
        Properties p = configuracion();
        return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.usuario"),
                p.getProperty("db.contrasena"));
    }

    private static synchronized Properties configuracion() throws SQLException {
        if (config != null) {
            return config;
        }
        Properties p = new Properties();
        try (InputStream in = abrir()) {
            if (in == null) {
                throw new SQLException("No se encontro " + ARCHIVO
                        + ". Copia db.properties.ejemplo como db.properties y pon tu usuario y contrasena de MySQL.");
            }
            p.load(in);
        } catch (IOException ex) {
            throw new SQLException("No se pudo leer " + ARCHIVO, ex);
        }
        if (p.getProperty("db.url") == null) {
            throw new SQLException(ARCHIVO + " no tiene db.url");
        }
        config = p;
        return config;
    }

    private static InputStream abrir() throws IOException {
        File local = new File(ARCHIVO);
        if (local.isFile()) {
            return new FileInputStream(local);
        }
        File padre = new File("..", ARCHIVO); // al correr un modulo desde NetBeans
        if (padre.isFile()) {
            return new FileInputStream(padre);
        }
        return ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO);
    }
}
