package parchis.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Todo el SQL de la tabla perfil vive aqui. Usa PreparedStatement (evita
 * inyeccion de SQL) y try-with-resources (cierra la conexion aunque falle).
 */
public class PerfilDAOMySQL implements IPerfilDAO {

    @Override
    public PerfilEntidad buscar(String nombre) throws PersistenciaException {
        String sql = "SELECT nombre, color_preferido, partidas_jugadas, partidas_ganadas "
                + "FROM perfil WHERE nombre = ?";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new PerfilEntidad(rs.getString("nombre"), rs.getString("color_preferido"),
                        rs.getInt("partidas_jugadas"), rs.getInt("partidas_ganadas"));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el perfil " + nombre, e);
        }
    }

    @Override
    public void guardar(PerfilEntidad p) throws PersistenciaException {
        String sql = "INSERT INTO perfil (nombre, color_preferido, partidas_jugadas, partidas_ganadas) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection c = ConexionBD.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getColorPreferido());
            ps.setInt(3, p.getPartidasJugadas());
            ps.setInt(4, p.getPartidasGanadas());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el perfil " + p.getNombre(), e);
        }
    }
}
