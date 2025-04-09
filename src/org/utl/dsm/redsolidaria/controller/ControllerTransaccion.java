package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import org.utl.dsm.redsolidaria.model.Transaccion;
import org.utl.dsm.redsolidaria.model.Usuario;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ControllerTransaccion {

    private final ConexionMySql conexion = new ConexionMySql();

    public List<Transaccion> getTransaccionesPorUsuario(
        int idUsuario, String tipo, String estado, String fecha) throws SQLException {
    List<Transaccion> transacciones = new ArrayList<>();
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT t.*, ")
            .append("uo.nombre AS nombreOferente, ")
            .append("us.nombre AS nombreSolicitante, ")
            .append("s.titulo AS tituloServicio ")
            .append("FROM Transaccion t ")
            .append("LEFT JOIN Usuario uo ON t.idUsuarioOferente = uo.idUsuario ")
            .append("LEFT JOIN Usuario us ON t.idUsuarioSolicitante = us.idUsuario ")
            .append("LEFT JOIN Intercambio i ON t.idIntercambio = i.idIntercambio ")
            .append("LEFT JOIN ServicioIntercambio si ON i.idIntercambio = si.idIntercambio ")
            .append("LEFT JOIN Servicio s ON si.idServicio = s.idServicio ")
            .append("WHERE (t.idUsuarioSolicitante = ? OR t.idUsuarioOferente = ?) ");

    // Filtro por tipo (recibida/ofrecida)
    if (tipo != null && !tipo.isEmpty()) {
        if ("received".equals(tipo)) {
            queryBuilder.append("AND t.idUsuarioSolicitante = ? ");
        } else if ("given".equals(tipo)) {
            queryBuilder.append("AND t.idUsuarioOferente = ? ");
        }
    }

    // Filtro por estado
    if (estado != null && !estado.isEmpty()) {
        if ("completed".equals(estado)) {
            queryBuilder.append("AND (t.verificadoOferente = 1 AND t.verificadoSolicitante = 1) ");
        } else if ("pending".equals(estado)) {
            queryBuilder.append("AND (t.verificadoOferente = 0 OR t.verificadoSolicitante = 0) ");
        } else if ("cancelled".equals(estado)) {
            queryBuilder.append("AND (t.verificadoOferente = 2 OR t.verificadoSolicitante = 2) ");
        }
    }

    // Filtro por fecha
    if (fecha != null && !fecha.isEmpty()) {
        queryBuilder.append("AND DATE(t.fecha) = ? ");
    }

    // Ordenar por fecha descendente
    queryBuilder.append("ORDER BY t.fecha DESC");

    String query = queryBuilder.toString();
    try (Connection conn = conexion.open(); PreparedStatement ps = conn.prepareStatement(query)) {
        int paramIndex = 1;
        ps.setInt(paramIndex++, idUsuario); // Para idUsuarioSolicitante
        ps.setInt(paramIndex++, idUsuario); // Para idUsuarioOferente

        // Parámetro adicional para filtro de tipo
        if ("received".equals(tipo) || "given".equals(tipo)) {
            ps.setInt(paramIndex++, idUsuario);
        }

        // Parámetro para filtro de fecha
        if (fecha != null && !fecha.isEmpty()) {
            ps.setString(paramIndex++, fecha);
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Transaccion t = new Transaccion();
                t.setIdTransaccion(rs.getInt("idTransaccion"));
                t.setIntercambio(null); // No lo llenamos completo
                t.setOferente(new Usuario());
                t.getOferente().setIdUsuario(rs.getInt("idUsuarioOferente"));
                t.getOferente().setNombre(rs.getString("nombreOferente"));
                t.setSolicitante(new Usuario());
                t.getSolicitante().setIdUsuario(rs.getInt("idUsuarioSolicitante"));
                t.getSolicitante().setNombre(rs.getString("nombreSolicitante"));
                t.setHorasIntercambiadas(rs.getFloat("horasIntercambiadas"));

                // Cambio en la conversión de fecha
                Date sqlDate = rs.getDate("fecha");
                if (sqlDate != null) {
                    t.setFecha(sqlDate.toLocalDate());
                }

                t.setDetalles(rs.getString("detalles"));
                t.setVerificadoOferente(rs.getBoolean("verificadoOferente"));
                t.setVerificadoSolicitante(rs.getBoolean("verificadoSolicitante"));
                t.setEstatus((t.isVerificadoOferente() && t.isVerificadoSolicitante()) ? 1 : 0);
                t.setTituloServicio(rs.getString("tituloServicio"));
                t.setHorasRecibidas(rs.getFloat("horasRecibidas"));
                t.setHorasOfrecidas(rs.getFloat("horasOfrecidas"));
                t.setIntercambiosCompletados(rs.getInt("intercambiosCompletados"));
                transacciones.add(t);
            }
        }
    }
    return transacciones;
}
    
    
}
