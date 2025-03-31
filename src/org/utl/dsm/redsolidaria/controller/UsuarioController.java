package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Usuario;
import org.utl.dsm.redsolidaria.model.Ciudad;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioController {

    private ConexionMySql conexion;

    public UsuarioController() {
        this.conexion = new ConexionMySql();
    }

    public void registrarUsuario(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Usuario (nombre, apellidos, correo, contrasena, idCiudad, configuracionPrivacidad, preferenciasEmail) VALUES (?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getContrasena());
            ps.setInt(5, usuario.getCiudad() != null ? usuario.getCiudad().getIdCiudad() : null);
            ps.setString(6, usuario.getConfiguracionPrivacidad());
            ps.setBoolean(7, usuario.isPreferenciasEmail());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public Usuario login(String correo, String contrasena) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Usuario WHERE correo = ? AND contrasena = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            rs = ps.executeQuery();
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("idUsuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasena(rs.getString("contrasena"));
                usuario.setConfiguracionPrivacidad(rs.getString("configuracionPrivacidad"));
                usuario.setSaldoHoras(rs.getFloat("saldoHoras"));
                usuario.setReputacion(rs.getFloat("reputacion"));
                return usuario;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public Usuario getUsuarioById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Usuario WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("idUsuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasena(rs.getString("contrasena"));
                usuario.setConfiguracionPrivacidad(rs.getString("configuracionPrivacidad"));
                usuario.setSaldoHoras(rs.getFloat("saldoHoras"));
                usuario.setReputacion(rs.getFloat("reputacion"));
                return usuario;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public void actualizarUsuario(Usuario usuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Usuario SET nombre = ?, apellidos = ?, correo = ?, contrasena = ?, idCiudad = ?, configuracionPrivacidad = ?, preferenciasEmail = ? WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getContrasena());
            ps.setInt(5, usuario.getCiudad() != null ? usuario.getCiudad().getIdCiudad() : null);
            ps.setString(6, usuario.getConfiguracionPrivacidad());
            ps.setBoolean(7, usuario.isPreferenciasEmail());
            ps.setInt(8, usuario.getIdUsuario());
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public void eliminarUsuario(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "DELETE FROM Usuario WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }

    public List<Usuario> getTodosUsuarios() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Usuario> usuarios = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Usuario";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("idUsuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setApellidos(rs.getString("apellidos"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setContrasena(rs.getString("contrasena"));
                usuario.setConfiguracionPrivacidad(rs.getString("configuracionPrivacidad"));
                usuario.setSaldoHoras(rs.getFloat("saldoHoras"));
                usuario.setReputacion(rs.getFloat("reputacion"));
                usuarios.add(usuario);
            }
            return usuarios;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conexion.close();
        }
    }
}