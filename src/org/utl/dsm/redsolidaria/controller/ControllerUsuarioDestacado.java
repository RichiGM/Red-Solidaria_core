package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerUsuarioDestacado {

    private final ConexionMySql conexion = new ConexionMySql();

public List<Map<String, Object>> getUsuariosDestacados(String token) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<Map<String, Object>> usuarios = new ArrayList<>();
    try {
        conn = conexion.open();
        if (conn == null) {
            System.out.println("Error: La conexión a la base de datos es nula");
            throw new SQLException("No se pudo establecer conexión con la base de datos");
        }
        
        String query = "SELECT u.idUsuario, u.nombre, u.apellidos, u.foto, u.descripcion, " +
                       "COALESCE(AVG(c.calificacion), 0) AS reputacion, " +
                       "(SELECT COUNT(*) FROM Servicio s WHERE s.idUsuario = u.idUsuario AND s.estatus = 1) AS servicios, " +
                       "(SELECT COUNT(*) FROM Transaccion t WHERE t.idUsuarioOferente = u.idUsuario OR t.idUsuarioSolicitante = u.idUsuario) AS intercambios " +
                       "FROM Usuario u " +
                       "LEFT JOIN Calificacion c ON u.idUsuario = c.idUsuarioCalificado " +
                       "WHERE u.estatus = 1 " +
                       "AND u.configuracionPrivacidad = 1 " +
                       "AND u.idUsuario != (SELECT idUsuario FROM Usuario WHERE lastToken = ?) " +
                       "GROUP BY u.idUsuario " +
                       "ORDER BY servicios DESC, reputacion DESC, intercambios DESC " +
                       "LIMIT 6";
        
        System.out.println("Ejecutando consulta: " + query);
        ps = conn.prepareStatement(query);
        ps.setString(1, token); // Pasamos el token como parámetro
        rs = ps.executeQuery();
        
        while (rs.next()) {
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("idUsuario", rs.getInt("idUsuario"));
            usuario.put("nombre", rs.getString("nombre") + " " + rs.getString("apellidos"));
            usuario.put("foto", rs.getString("foto"));
            usuario.put("descripcion", rs.getString("descripcion"));
            usuario.put("reputacion", rs.getObject("reputacion") != null ? rs.getFloat("reputacion") : 0.0f);
            usuario.put("servicios", rs.getInt("servicios"));
            usuario.put("intercambios", rs.getInt("intercambios"));
            
            // Obtener habilidades del usuario
            List<Map<String, Object>> habilidades = getHabilidadesUsuario(conn, rs.getInt("idUsuario"));
            usuario.put("habilidades", habilidades);
            
            usuarios.add(usuario);
        }
        
        System.out.println("Usuarios encontrados: " + usuarios.size());
        return usuarios;
    } catch (SQLException e) {
        System.out.println("Error SQL en getUsuariosDestacados: " + e.getMessage());
        e.printStackTrace();
        throw e;
    } catch (Exception e) {
        System.out.println("Error general en getUsuariosDestacados: " + e.getMessage());
        e.printStackTrace();
        throw new SQLException("Error general: " + e.getMessage(), e);
    } finally {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (ps != null) {
            try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}

    private List<Map<String, Object>> getHabilidadesUsuario(Connection conn, int idUsuario) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> habilidades = new ArrayList<>();
        try {
            String query = "SELECT h.idHabilidad, h.nombre FROM Habilidad h " +
                    "JOIN UsuarioHabilidad uh ON h.idHabilidad = uh.idHabilidad " +
                    "WHERE uh.idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> habilidad = new HashMap<>();
                habilidad.put("idHabilidad", rs.getInt("idHabilidad"));
                habilidad.put("nombre", rs.getString("nombre"));
                habilidades.add(habilidad);
            }
            return habilidades;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    public Map<String, Object> obtenerDatosBasicosUsuario(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> usuario = null;
        try {
            conn = conexion.open();
            String query = "SELECT u.idUsuario, u.nombre, u.apellidos, u.correo, u.foto, u.descripcion, " +
                    "c.nombre AS nombreCiudad, e.nombre AS nombreEstado, " +
                    "AVG(cal.calificacion) AS reputacion, " +
                    "(SELECT COUNT(*) FROM Servicio s WHERE s.idUsuario = u.idUsuario AND s.estatus = 1) AS servicios, " +
                    "(SELECT COUNT(*) FROM Transaccion t WHERE t.idUsuarioOferente = u.idUsuario OR t.idUsuarioSolicitante = u.idUsuario) AS intercambios " +
                    "FROM Usuario u " +
                    "LEFT JOIN Ciudad c ON u.idCiudad = c.idCiudad " +
                    "LEFT JOIN Estado e ON c.idEstado = e.idEstado " +
                    "LEFT JOIN Calificacion cal ON u.idUsuario = cal.idUsuarioCalificado " +
                    "WHERE u.idUsuario = ? " +
                    "GROUP BY u.idUsuario";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            if (rs.next()) {
                usuario = new HashMap<>();
                usuario.put("idUsuario", rs.getInt("idUsuario"));
                usuario.put("nombre", rs.getString("nombre"));
                usuario.put("apellidos", rs.getString("apellidos"));
                usuario.put("correo", rs.getString("correo"));
                usuario.put("foto", rs.getString("foto"));
                usuario.put("descripcion", rs.getString("descripcion"));
                usuario.put("ciudad", rs.getString("nombreCiudad"));
                usuario.put("estado", rs.getString("nombreEstado"));
                usuario.put("reputacion", rs.getObject("reputacion") != null ? rs.getFloat("reputacion") : 0.0f);
                usuario.put("servicios", rs.getInt("servicios"));
                usuario.put("intercambios", rs.getInt("intercambios"));
                
                // Verificar si el usuario está en línea
                usuario.put("online", verificarUsuarioEnLinea(conn, idUsuario));
                
                // Obtener habilidades del usuario
                List<Map<String, Object>> habilidades = getHabilidadesUsuario(conn, idUsuario);
                usuario.put("habilidades", habilidades);
            }
            return usuario;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    private boolean verificarUsuarioEnLinea(Connection conn, int idUsuario) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = "SELECT lastToken FROM Usuario WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            if (rs.next()) {
                String lastToken = rs.getString("lastToken");
                return lastToken != null && !lastToken.isEmpty();
            }
            return false;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
    }

    public void actualizarDescripcion(int idUsuario, String descripcion) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Usuario SET descripcion = ? WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, descripcion);
            ps.setInt(2, idUsuario);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el usuario con ID: " + idUsuario);
            }
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public void actualizarHabilidades(int idUsuario, List<Map<String, Integer>> habilidades) throws SQLException {
        Connection conn = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        try {
            conn = conexion.open();
            conn.setAutoCommit(false);
            
            // Eliminar habilidades actuales
            String deleteQuery = "DELETE FROM UsuarioHabilidad WHERE idUsuario = ?";
            psDelete = conn.prepareStatement(deleteQuery);
            psDelete.setInt(1, idUsuario);
            psDelete.executeUpdate();
            
            // Insertar nuevas habilidades
            String insertQuery = "INSERT INTO UsuarioHabilidad (idUsuario, idHabilidad) VALUES (?, ?)";
            psInsert = conn.prepareStatement(insertQuery);
            
            for (Map<String, Integer> habilidad : habilidades) {
                int idHabilidad = habilidad.get("idHabilidad");
                psInsert.setInt(1, idUsuario);
                psInsert.setInt(2, idHabilidad);
                psInsert.addBatch();
            }
            
            psInsert.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (psDelete != null) psDelete.close();
            if (psInsert != null) psInsert.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void actualizarTelefono(int idUsuario, String telefono) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Usuario SET telefono = ? WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, telefono);
            ps.setInt(2, idUsuario);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el usuario con ID: " + idUsuario);
            }
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    public void bloquearUsuario(int idUsuarioBloquea, int idUsuarioBloqueado) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO UsuarioBloqueado (idUsuarioBloquea, idUsuarioBloqueado) VALUES (?, ?)";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuarioBloquea);
            ps.setInt(2, idUsuarioBloqueado);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
}