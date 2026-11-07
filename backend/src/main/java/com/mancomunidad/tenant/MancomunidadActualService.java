package com.mancomunidad.tenant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Consulta los datos de la mancomunidad activa del {@link TenantContext}.
 *
 * <p>La consulta vuelve a comprobar que la mancomunidad existe y está activa:
 * el contexto puede haber sido resuelto antes y la mancomunidad haberse
 * desactivado en el intervalo.</p>
 */
@Service
public class MancomunidadActualService {

	private static final RowMapper<MancomunidadActual> MAPPER = (rs, fila) -> new MancomunidadActual(
		rs.getLong("id"),
		rs.getString("nombre"),
		rs.getString("codigo"),
		rs.getString("tipo"));

	private final JdbcTemplate jdbc;

	public MancomunidadActualService(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public MancomunidadActual obtenerActual() {
		Long mancomunidadId = TenantContext.require();
		List<MancomunidadActual> encontradas = jdbc.query(
			"SELECT id, nombre, codigo, tipo FROM mancomunidad WHERE id = ? AND activo = TRUE",
			MAPPER, mancomunidadId);
		return encontradas.stream()
			.findFirst()
			.orElseThrow(() -> new TenantNoResueltoException(
				"La mancomunidad activa no existe o está desactivada"));
	}

}
