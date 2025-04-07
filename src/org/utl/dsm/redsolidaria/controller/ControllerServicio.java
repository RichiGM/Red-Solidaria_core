package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Servicio;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.utl.dsm.redsolidaria.model.Habilidad;

public class ControllerServicio {

    private final ConexionMySql conexion = new ConexionMySql();

    // Método para agregar un nuevo servicio
    public void agregarServicio(Servicio servicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Servicio (titulo, descripcion, modalidad, estatus, idUsuario) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, servicio.getTitulo());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getModalidad());
            ps.setInt(4, servicio.getEstatus());
            ps.setInt(5, servicio.getIdUsuario()); // Usar el ID de usuario directamente
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al agregar el servicio: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    // Método para modificar un servicio existente
    public void modificarServicio(Servicio servicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Servicio SET titulo = ?, descripcion = ?, modalidad = ?, estatus = ? WHERE idServicio = ? AND idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, servicio.getTitulo());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getModalidad());
            ps.setInt(4, servicio.getEstatus());
            ps.setInt(5, servicio.getIdServicio());
            ps.setInt(6, servicio.getIdUsuario());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el servicio o no tienes permiso para modificarlo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al modificar el servicio: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    // Método para obtener los servicios de un usuario específico
    public List<Servicio> obtenerMisServicios(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Servicio> servicios = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT idServicio, titulo, descripcion, modalidad, estatus FROM Servicio WHERE idUsuario = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            while (rs.next()) {
                Servicio servicio = new Servicio();
                servicio.setIdServicio(rs.getInt("idServicio"));
                servicio.setTitulo(rs.getString("titulo"));
                servicio.setDescripcion(rs.getString("descripcion"));
                servicio.setModalidad(rs.getInt("modalidad"));
                servicio.setEstatus(rs.getInt("estatus"));
                servicio.setIdUsuario(idUsuario);
                servicios.add(servicio);
            }
            return servicios;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener los servicios: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    // Método para obtener todos los servicios con información del usuario
    public List<Map<String, Object>> getTodosServicios() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> servicios = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT s.idServicio, s.titulo, s.descripcion, s.modalidad, s.estatus, s.idUsuario, " +
                    "u.nombre AS nombreUsuario, u.foto AS fotoUsuario, " +
                    "(SELECT AVG(c.calificacion) FROM Calificacion c WHERE c.idUsuarioCalificado = u.idUsuario) AS calificacionUsuario " +
                    "FROM Servicio s " +
                    "JOIN Usuario u ON s.idUsuario = u.idUsuario " +
                    "WHERE s.estatus = 1 " +
                    "ORDER BY s.idServicio DESC";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> servicio = new HashMap<>();
                servicio.put("idServicio", rs.getInt("idServicio"));
                servicio.put("titulo", rs.getString("titulo"));
                servicio.put("descripcion", rs.getString("descripcion"));
                servicio.put("modalidad", rs.getInt("modalidad"));
                servicio.put("estatus", rs.getInt("estatus"));
                servicio.put("idUsuario", rs.getInt("idUsuario"));
                servicio.put("nombreUsuario", rs.getString("nombreUsuario"));
                servicio.put("fotoUsuario", rs.getString("fotoUsuario"));
                servicio.put("calificacionUsuario", rs.getObject("calificacionUsuario") != null ? rs.getFloat("calificacionUsuario") : 0.0f);
                servicios.add(servicio);
            }
            return servicios;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    // Método para obtener servicios destacados
    public List<Map<String, Object>> getServiciosDestacados() throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<Map<String, Object>> servicios = new ArrayList<>();
    try {
        conn = conexion.open();
        if (conn == null) {
            System.out.println("Error: La conexión a la base de datos es nula");
            throw new SQLException("No se pudo establecer conexión con la base de datos");
        }
        
        // Consulta para obtener servicios destacados
        String query = "SELECT s.idServicio, s.titulo, s.descripcion, s.modalidad, s.estatus, s.idUsuario, " +
                       "u.nombre AS nombreUsuario, u.apellidos AS apellidosUsuario, u.foto AS fotoUsuario " +
                       "FROM Servicio s " +
                       "JOIN Usuario u ON s.idUsuario = u.idUsuario " +
                       "WHERE s.estatus = 1 " +
                       "ORDER BY s.vistas DESC " +
                       "LIMIT 5";
        
        System.out.println("Ejecutando consulta: " + query);
        ps = conn.prepareStatement(query);
        rs = ps.executeQuery();
        
        while (rs.next()) {
            Map<String, Object> servicio = new HashMap<>();
            servicio.put("idServicio", rs.getInt("idServicio"));
            servicio.put("titulo", rs.getString("titulo"));
            servicio.put("descripcion", rs.getString("descripcion"));
            servicio.put("modalidad", rs.getInt("modalidad"));
            servicio.put("estatus", rs.getInt("estatus"));
            servicio.put("idUsuario", rs.getInt("idUsuario"));
            servicio.put("nombreUsuario", rs.getString("nombreUsuario") + " " + rs.getString("apellidosUsuario"));
            servicio.put("fotoUsuario", rs.getString("fotoUsuario"));
            servicios.add(servicio);
        }
        
        System.out.println("Servicios encontrados: " + servicios.size());
        return servicios;
    } catch (SQLException e) {
        System.out.println("Error SQL en getServiciosDestacados: " + e.getMessage());
        e.printStackTrace();
        throw e;
    } catch (Exception e) {
        System.out.println("Error general en getServiciosDestacados: " + e.getMessage());
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

    // Método para buscar servicios
    public List<Map<String, Object>> buscarServicios(String query, Integer modalidad, String orden) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> servicios = new ArrayList<>();
        try {
            conn = conexion.open();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT s.idServicio, s.titulo, s.descripcion, s.modalidad, s.estatus, s.idUsuario, ")
               .append("u.nombre AS nombreUsuario, u.foto AS fotoUsuario, ")
               .append("(SELECT AVG(c.calificacion) FROM Calificacion c WHERE c.idUsuarioCalificado = u.idUsuario) AS calificacionUsuario ")
               .append("FROM Servicio s ")
               .append("JOIN Usuario u ON s.idUsuario = u.idUsuario ")
               .append("LEFT JOIN ServicioHabilidad sh ON s.idServicio = sh.idServicio ")
               .append("LEFT JOIN Habilidad h ON sh.idHabilidad = h.idHabilidad ")
               .append("WHERE s.estatus = 1 ");

            List<Object> params = new ArrayList<>();

            if (query != null && !query.isEmpty()) {
                sql.append("AND (s.titulo LIKE ? OR s.descripcion LIKE ? OR h.nombre LIKE ?) ");
                String searchTerm = "%" + query + "%";
                params.add(searchTerm);
                params.add(searchTerm);
                params.add(searchTerm);
            }

            if (modalidad != null) {
                sql.append("AND s.modalidad = ? ");
                params.add(modalidad);
            }

            sql.append("GROUP BY s.idServicio ");

            if ("recent".equals(orden)) {
                sql.append("ORDER BY s.idServicio DESC ");
            } else if ("rating".equals(orden)) {
                sql.append("ORDER BY calificacionUsuario DESC ");
            } else if ("name".equals(orden)) {
                sql.append("ORDER BY s.titulo ASC ");
            } else {
                sql.append("ORDER BY s.idServicio DESC ");
            }

            ps = conn.prepareStatement(sql.toString());

            // Establecer parámetros
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> servicio = new HashMap<>();
                servicio.put("idServicio", rs.getInt("idServicio"));
                servicio.put("titulo", rs.getString("titulo"));
                servicio.put("descripcion", rs.getString("descripcion"));
                servicio.put("modalidad", rs.getInt("modalidad"));
                servicio.put("estatus", rs.getInt("estatus"));
                servicio.put("idUsuario", rs.getInt("idUsuario"));
                servicio.put("nombreUsuario", rs.getString("nombreUsuario"));
                servicio.put("fotoUsuario", rs.getString("fotoUsuario"));
                servicio.put("calificacionUsuario", rs.getObject("calificacionUsuario") != null ? rs.getFloat("calificacionUsuario") : 0.0f);
                servicios.add(servicio);
            }
            return servicios;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    // Método para obtener el detalle de un servicio
    public Map<String, Object> getDetalleServicio(int idServicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> servicio = null;
        try {
            conn = conexion.open();
            String query = "SELECT s.idServicio, s.titulo, s.descripcion, s.modalidad, s.estatus, s.idUsuario, " +
                    "u.nombre AS nombreUsuario, u.apellidos AS apellidosUsuario, u.foto AS fotoUsuario, " +
                    "(SELECT AVG(c.calificacion) FROM Calificacion c WHERE c.idUsuarioCalificado = u.idUsuario) AS calificacionUsuario " +
                    "FROM Servicio s " +
                    "JOIN Usuario u ON s.idUsuario = u.idUsuario " +
                    "WHERE s.idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idServicio);
            rs = ps.executeQuery();
            if (rs.next()) {
                servicio = new HashMap<>();
                servicio.put("idServicio", rs.getInt("idServicio"));
                servicio.put("titulo", rs.getString("titulo"));
                servicio.put("descripcion", rs.getString("descripcion"));
                servicio.put("modalidad", rs.getInt("modalidad"));
                servicio.put("estatus", rs.getInt("estatus"));
                servicio.put("idUsuario", rs.getInt("idUsuario"));
                servicio.put("nombreUsuario", rs.getString("nombreUsuario") + " " + rs.getString("apellidosUsuario"));
                servicio.put("fotoUsuario", rs.getString("fotoUsuario"));
                servicio.put("calificacionUsuario", rs.getObject("calificacionUsuario") != null ? rs.getFloat("calificacionUsuario") : 0.0f);
                
                // Obtener habilidades asociadas al servicio
                List<Map<String, Object>> habilidades = getHabilidadesServicio(conn, idServicio);
                servicio.put("habilidades", habilidades);
            }
            
            // Incrementar contador de vistas
            incrementarVistas(conn, idServicio);
            
            return servicio;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
    
    // Método para obtener las habilidades de un servicio
    private List<Map<String, Object>> getHabilidadesServicio(Connection conn, int idServicio) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> habilidades = new ArrayList<>();
        try {
            String query = "SELECT h.idHabilidad, h.nombre FROM Habilidad h " +
                    "JOIN ServicioHabilidad sh ON h.idHabilidad = sh.idHabilidad " +
                    "WHERE sh.idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idServicio);
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
    
    // Método para incrementar el contador de vistas de un servicio
    private void incrementarVistas(Connection conn, int idServicio) throws SQLException {
        PreparedStatement ps = null;
        try {
            String query = "UPDATE Servicio SET vistas = COALESCE(vistas, 0) + 1 WHERE idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idServicio);
            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }
    
    // Método para eliminar un servicio
    public void eliminarServicio(int idServicio) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            String query = "UPDATE Servicio SET estatus = 2 WHERE idServicio = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idServicio);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró el servicio con ID: " + idServicio);
            }
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
    
    // Método para obtener todas las habilidades activas
    public List<Habilidad> obtenerTodas() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Habilidad> habilidades = new ArrayList<>();

        try {
            conn = conexion.open();
            String query = "SELECT idHabilidad, nombre FROM Habilidad WHERE estatus = 1";
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                Habilidad habilidad = new Habilidad();
                habilidad.setIdHabilidad(rs.getInt("idHabilidad"));
                habilidad.setNombre(rs.getString("nombre"));
                habilidades.add(habilidad);
            }

            return habilidades;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener las habilidades: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

}