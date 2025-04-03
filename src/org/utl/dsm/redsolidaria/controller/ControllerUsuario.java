package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Usuario;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.utl.dsm.redsolidaria.model.Ciudad;

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

    public Integer obtenerIdUsuarioPorEmail(String email) {
        String query = "SELECT idUsuario FROM Usuario WHERE correo = ?";
        try ( Connection conn = conexion.open();  PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idUsuario"); // Retorna el ID del usuario
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Si no se encontró el usuario
    }

    public void modificarUsuario(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query;

            if (usuario.getContrasenia() != null && !usuario.getContrasenia().isEmpty()) {
                // Si hay contraseña, incluirla en la actualización con encriptación
                query = "UPDATE Usuario SET nombre = ?, apellidos = ?, correo = ?, idCiudad = ?, descripcion = ?, configuracionPrivacidad = ?, contrasenia = SHA2(?, 256) WHERE idUsuario = ?";
                ps = conn.prepareStatement(query);
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getApellidos());
                ps.setString(3, usuario.getCorreo());
                ps.setObject(4, usuario.getCiudad() != null ? usuario.getCiudad().getIdCiudad() : null);
                ps.setString(5, usuario.getDescripcion());
                ps.setInt(6, usuario.getConfiguracionPrivacidad());
                ps.setString(7, usuario.getContrasenia());
                ps.setInt(8, usuario.getIdUsuario());
            } else {
                // Sin contraseña, usar la consulta actual
                query = "UPDATE Usuario SET nombre = ?, apellidos = ?, correo = ?, idCiudad = ?, descripcion = ?, configuracionPrivacidad = ? WHERE idUsuario = ?";
                ps = conn.prepareStatement(query);
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getApellidos());
                ps.setString(3, usuario.getCorreo());
                ps.setObject(4, usuario.getCiudad() != null ? usuario.getCiudad().getIdCiudad() : null);
                ps.setString(5, usuario.getDescripcion());
                ps.setInt(6, usuario.getConfiguracionPrivacidad());
                ps.setInt(7, usuario.getIdUsuario());
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el usuario con idUsuario: " + usuario.getIdUsuario());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

// En ControllerUsuario.java, añadir este método
    public void logoutUser(String email) throws SQLException {
        String query = "UPDATE Usuario SET lastToken = NULL WHERE correo = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = conexion.open();
            ps = conn.prepareStatement(query);
            ps.setString(1, email);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el usuario con correo: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al hacer logout del usuario: " + e.getMessage(), e);
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
    
        public Usuario obtenerDatosPorEmail(String email) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Usuario usuario = null;

        try {
            conn = conexion.open();
            String query = "SELECT nombre, apellidos, correo, contrasenia, idCiudad, foto, descripcion, configuracionPrivacidad, reputacion, saldoHoras, estadoVerificacion, estatus, preferenciasEmail FROM Usuario WHERE correo = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, email);
            rs = ps.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasenia(rs.getString("contrasenia"));
                
                Ciudad ciudad = new Ciudad();
                ciudad.setIdCiudad(rs.getInt("idCiudad"));
                usuario.setCiudad(ciudad);
                
                usuario.setFoto(rs.getString("foto"));
                usuario.setDescripcion(rs.getString("descripcion"));
                usuario.setConfiguracionPrivacidad(rs.getInt("configuracionPrivacidad"));
                usuario.setReputacion(rs.getFloat("reputacion"));
                usuario.setSaldoHoras(rs.getFloat("saldoHoras"));
                usuario.setEstadoVerificacion(rs.getInt("estadoVerificacion"));
                usuario.setEstatus(rs.getInt("estatus"));
                usuario.setPreferenciasEmail(rs.getBoolean("preferenciasEmail"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener los datos del usuario: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
        return usuario;
    }
}
