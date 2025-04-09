package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.model.Mensaje;
import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerMensaje {

    private final ConexionMySql conexion = new ConexionMySql();

    public int insertarMensaje(Mensaje mensaje) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = conexion.open();
            String query = "INSERT INTO Mensaje (contenido, tipoContenido, estatus, idRemitente, idDestinatario, fechaEnvio) "
                    + "VALUES (?, ?, ?, ?, ?, NOW())";
            ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, mensaje.getContenido());
            ps.setInt(2, mensaje.getTipoContenido());
            ps.setInt(3, mensaje.getEstatus());
            ps.setInt(4, mensaje.getIdRemitente());
            ps.setInt(5, mensaje.getIdDestinatario());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Devolver el ID generado
            }
            throw new SQLException("No se generó un ID para el mensaje");
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

    public List<Mensaje> getConversacion(int idUsuario1, int idUsuario2) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Mensaje> mensajes = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT * FROM Mensaje WHERE (idRemitente = ? AND idDestinatario = ?) OR (idRemitente = ? AND idDestinatario = ?) ORDER BY fechaEnvio ASC";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario1);
            ps.setInt(2, idUsuario2);
            ps.setInt(3, idUsuario2);
            ps.setInt(4, idUsuario1);
            rs = ps.executeQuery();
            while (rs.next()) {
                Mensaje mensaje = new Mensaje();
                mensaje.setIdMensaje(rs.getInt("idMensaje"));
                mensaje.setContenido(rs.getString("contenido"));
                mensaje.setTipoContenido(rs.getInt("tipoContenido"));
                mensaje.setFechaEnvio(rs.getTimestamp("fechaEnvio").toLocalDateTime()); // Cambiar a LocalDateTime
                mensaje.setEstatus(rs.getInt("estatus"));
                mensaje.setIdRemitente(rs.getInt("idRemitente"));
                mensaje.setIdDestinatario(rs.getInt("idDestinatario"));
                mensajes.add(mensaje);
            }
            return mensajes;
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

    public List<Map<String, Object>> getChats(int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> chats = new ArrayList<>();
        try {
            conn = conexion.open();
            String query = "SELECT u.idUsuario, u.nombre, u.foto, "
                    + "(SELECT COUNT(*) FROM Mensaje WHERE idRemitente = u.idUsuario AND idDestinatario = ? AND estatus = 0) AS mensajesNoLeidos, "
                    + "(SELECT contenido FROM Mensaje WHERE (idRemitente = ? AND idDestinatario = u.idUsuario) OR (idRemitente = u.idUsuario AND idDestinatario = ?) "
                    + "ORDER BY fechaEnvio DESC LIMIT 1) AS ultimoMensaje, "
                    + "(SELECT fechaEnvio FROM Mensaje WHERE (idRemitente = ? AND idDestinatario = u.idUsuario) OR (idRemitente = u.idUsuario AND idDestinatario = ?) "
                    + "ORDER BY fechaEnvio DESC LIMIT 1) AS fechaUltimoMensaje "
                    + "FROM Usuario u "
                    + "WHERE u.idUsuario IN (SELECT DISTINCT idRemitente FROM Mensaje WHERE idDestinatario = ? "
                    + "UNION SELECT DISTINCT idDestinatario FROM Mensaje WHERE idRemitente = ?) "
                    + "ORDER BY fechaUltimoMensaje DESC";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idUsuario);
            ps.setInt(5, idUsuario);
            ps.setInt(6, idUsuario);
            ps.setInt(7, idUsuario);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> chat = new HashMap<>();
                chat.put("idUsuario", rs.getInt("idUsuario"));
                chat.put("nombre", rs.getString("nombre"));
                chat.put("foto", rs.getString("foto"));
                chat.put("mensajesNoLeidos", rs.getInt("mensajesNoLeidos"));
                chat.put("ultimoMensaje", rs.getString("ultimoMensaje"));
                chat.put("fechaUltimoMensaje", rs.getTimestamp("fechaUltimoMensaje") != null
                        ? rs.getTimestamp("fechaUltimoMensaje").toLocalDateTime()
                        : null); // Usar Timestamp y convertir a LocalDateTime
                chats.add(chat);
            }
            return chats;
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

    public void marcarMensajesComoLeidos(int idUsuario, int idRemitente) throws SQLException {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
        conn = conexion.open();
        String query = "UPDATE Mensaje SET estatus = 1 WHERE idRemitente = ? AND idDestinatario = ? AND estatus = 0";
        ps = conn.prepareStatement(query);
        ps.setInt(1, idRemitente);
        ps.setInt(2, idUsuario);
        ps.executeUpdate();
    } finally {
        if (ps != null) ps.close();
        if (conn != null) conn.close();
    }
}

    public void limpiarConversacion(int idUsuario, int idOtroUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = conexion.open();
            // En lugar de eliminar los mensajes, podríamos marcarlos como eliminados para el usuario
            String query = "DELETE FROM Mensaje WHERE (idRemitente = ? AND idDestinatario = ?) OR (idRemitente = ? AND idDestinatario = ?)";
            ps = conn.prepareStatement(query);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idOtroUsuario);
            ps.setInt(3, idOtroUsuario);
            ps.setInt(4, idUsuario);
            ps.executeUpdate();
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
}
