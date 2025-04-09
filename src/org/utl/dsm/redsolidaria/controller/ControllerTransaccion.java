package org.utl.dsm.redsolidaria.controller;

import org.utl.dsm.redsolidaria.bd.ConexionMySql;
import org.utl.dsm.redsolidaria.model.Transaccion;
import org.utl.dsm.redsolidaria.model.Usuario;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.utl.dsm.redsolidaria.model.TransaccionDTO;

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
        try ( Connection conn = conexion.open();  PreparedStatement ps = conn.prepareStatement(query)) {
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

            try ( ResultSet rs = ps.executeQuery()) {
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

    public boolean verificarTransaccion(Transaccion transaccion) throws SQLException {
        String query = "UPDATE Transaccion SET ";
        boolean isVerificadoOferente = transaccion.isVerificadoOferente();
        boolean isVerificadoSolicitante = transaccion.isVerificadoSolicitante();

        // Determinar qué campo actualizar basado en los datos recibidos
        if (isVerificadoOferente && !isVerificadoSolicitante) {
            query += "verificadoOferente = ? ";
        } else if (!isVerificadoOferente && isVerificadoSolicitante) {
            query += "verificadoSolicitante = ? ";
        } else {
            query += "verificadoOferente = ?, verificadoSolicitante = ? ";
        }

        query += "WHERE idTransaccion = ?";

        try ( Connection conn = conexion.open();  PreparedStatement ps = conn.prepareStatement(query)) {

            if (isVerificadoOferente && !isVerificadoSolicitante) {
                ps.setBoolean(1, isVerificadoOferente);
                ps.setInt(2, transaccion.getIdTransaccion());
            } else if (!isVerificadoOferente && isVerificadoSolicitante) {
                ps.setBoolean(1, isVerificadoSolicitante);
                ps.setInt(2, transaccion.getIdTransaccion());
            } else {
                ps.setBoolean(1, isVerificadoOferente);
                ps.setBoolean(2, isVerificadoSolicitante);
                ps.setInt(3, transaccion.getIdTransaccion());
            }

            int filasAfectadas = ps.executeUpdate();

            // Actualizar saldos de horas de usuarios si ambos verifican
            if ((isVerificadoOferente && isVerificadoSolicitante)
                    || checkTransaccionCompleta(transaccion.getIdTransaccion(), conn)) {
                actualizarSaldosHoras(transaccion.getIdTransaccion(), conn);
            }

            return filasAfectadas > 0;
        }
    }

    // Verificar si una transacción ya está completa (ambos usuarios han verificado)
    private boolean checkTransaccionCompleta(int idTransaccion, Connection conn) throws SQLException {
        String query = "SELECT verificadoOferente, verificadoSolicitante FROM Transaccion WHERE idTransaccion = ?";

        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idTransaccion);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean verificadoOferente = rs.getBoolean("verificadoOferente");
                    boolean verificadoSolicitante = rs.getBoolean("verificadoSolicitante");
                    return verificadoOferente && verificadoSolicitante;
                }
                return false;
            }
        }
    }

    // Actualizar saldos de horas de usuarios cuando una transacción se completa
    private void actualizarSaldosHoras(int idTransaccion, Connection conn) throws SQLException {
        String query = "SELECT idUsuarioOferente, idUsuarioSolicitante, horasIntercambiadas FROM Transaccion WHERE idTransaccion = ?";

        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idTransaccion);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idOferente = rs.getInt("idUsuarioOferente");
                    int idSolicitante = rs.getInt("idUsuarioSolicitante");
                    float horas = rs.getFloat("horasIntercambiadas");

                    // Actualizar saldo del oferente (recibe horas)
                    actualizarSaldoUsuario(idOferente, horas, conn);

                    // Actualizar saldo del solicitante (gasta horas)
                    actualizarSaldoUsuario(idSolicitante, -horas, conn);

                    // Actualizar estadísticas en la transacción
                    String updateTransaccion = "UPDATE Transaccion SET horasRecibidas = ?, horasOfrecidas = ?, intercambiosCompletados = 1 WHERE idTransaccion = ?";
                    try ( PreparedStatement psUpdate = conn.prepareStatement(updateTransaccion)) {
                        psUpdate.setFloat(1, horas);
                        psUpdate.setFloat(2, horas);
                        psUpdate.setInt(3, idTransaccion);
                        psUpdate.executeUpdate();
                    }
                }
            }
        }
    }

    // Actualizar el saldo de horas de un usuario
    private void actualizarSaldoUsuario(int idUsuario, float horasDelta, Connection conn) throws SQLException {
        String query = "UPDATE Usuario SET saldoHoras = saldoHoras + ? WHERE idUsuario = ?";

        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setFloat(1, horasDelta);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public TransaccionDTO crearTransaccion(Transaccion transaccion) throws SQLException {
    String query = "INSERT INTO Transaccion (idIntercambio, idUsuarioOferente, idUsuarioSolicitante, " +
                  "horasIntercambiadas, fecha, detalles, verificadoOferente, verificadoSolicitante, " +
                  "horasRecibidas, horasOfrecidas, intercambiosCompletados) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = conexion.open();
         PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        
        // Configurar los parámetros del PreparedStatement
        ps.setInt(1, transaccion.getIntercambio() != null ? transaccion.getIntercambio().getIdIntercambio() : 0);
        ps.setInt(2, transaccion.getOferente().getIdUsuario());
        ps.setInt(3, transaccion.getSolicitante().getIdUsuario());
        ps.setFloat(4, transaccion.getHorasIntercambiadas());
        ps.setDate(5, transaccion.getFecha() != null ? java.sql.Date.valueOf(transaccion.getFecha()) : java.sql.Date.valueOf(LocalDate.now()));
        ps.setString(6, transaccion.getDetalles());
        ps.setBoolean(7, transaccion.isVerificadoOferente());
        ps.setBoolean(8, transaccion.isVerificadoSolicitante());
        ps.setFloat(9, transaccion.getHorasRecibidas());
        ps.setFloat(10, transaccion.getHorasOfrecidas());
        ps.setInt(11, transaccion.getIntercambiosCompletados());
        
        int filasAfectadas = ps.executeUpdate();
        
        if (filasAfectadas > 0) {
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    transaccion.setIdTransaccion(rs.getInt(1));
                }
            }
            // Convertir a DTO antes de retornar
            return TransaccionDTO.fromTransaccion(transaccion);
        } else {
            throw new SQLException("No se pudo crear la transacción");
        }
    }
}
    // Método auxiliar para verificar si un intercambio existe (opcional)
    public boolean intercambioExiste(int idIntercambio, Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM Intercambio WHERE idIntercambio = ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idIntercambio);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    // Método auxiliar para verificar el saldo del solicitante (esqueleto)
    public boolean tieneSaldoSuficiente(int idUsuarioSolicitante, float horas, Connection conn) throws SQLException {
        String query = "SELECT saldoHoras FROM Usuario WHERE idUsuario = ?";
        try ( PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idUsuarioSolicitante);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    float saldo = rs.getFloat("saldoHoras");
                    return saldo >= horas;
                }
                throw new SQLException("Usuario no encontrado");
            }
        }
    }
}
