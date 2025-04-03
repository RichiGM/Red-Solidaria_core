package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Usuario;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ControllerUsuario {

    private ConexionMySql conexion;

    public ControllerUsuario() {
        this.conexion = new ConexionMySql();
    }

    public void registrarUsuario(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Usuario (nombre, apellidos, correo, contrasenia, idCiudad, preferenciasEmail) VALUES (?, ?, ?, sha2(?, 256), ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getContrasenia());
            ps.setObject(5, usuario.getCiudad() != null ? usuario.getCiudad().getIdCiudad() : null); // Cambiado a setObject
            ps.setBoolean(6, usuario.isPreferenciasEmail());

            ps.executeUpdate();
        } catch (SQLException e) {
            // Manejo de excepciones
            e.printStackTrace(); // O lanza una excepción personalizada
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close(); // Asegúrate de cerrar la conexión correcta
            }
        }
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM Usuario WHERE correo = ?";
        try ( Connection conn = conexion.open();  PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Si el conteo es mayor que 0, el email ya existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Si no se encontró el email
    }
}
