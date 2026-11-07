package com.mancomunidad;

import com.mancomunidad.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MigracionesTests extends IntegrationTestBase {

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void debeAplicarElEsquemaYLosDatosDeDesarrollo() {
		var migraciones = jdbc.queryForList(
			"SELECT version, success FROM flyway_schema_history WHERE version IN ('1', '2', '3') ORDER BY installed_rank");

		assertThat(migraciones).hasSize(3);
		assertThat(migraciones).allSatisfy(fila -> assertThat(fila.get("success")).isEqualTo(true));

		var tipo = jdbc.queryForObject(
			"SELECT tipo FROM mancomunidad WHERE codigo = 'demo'", String.class);
		assertThat(tipo).isEqualTo("MUSICA");

		assertThat(jdbc.queryForObject(
			"SELECT COUNT(*) FROM mancomunidad WHERE codigo IN ('demo', 'demo-b')", Integer.class))
			.isEqualTo(2);
		assertThat(jdbc.queryForObject(
			"SELECT tipo FROM mancomunidad WHERE codigo = 'demo-b'", String.class))
			.isEqualTo("DANZA");

		var curso = jdbc.queryForMap(
			"SELECT c.codigo, c.estado, c.fecha_inicio, c.fecha_fin FROM curso_escolar c "
				+ "JOIN mancomunidad m ON m.id = c.mancomunidad_id WHERE m.codigo = 'demo'");
		assertThat(curso.get("codigo")).isEqualTo("2026-2027");
		assertThat(curso.get("estado")).isEqualTo("ACTIVO");
		assertThat(((java.sql.Date) curso.get("fecha_inicio")).toLocalDate()).isEqualTo(LocalDate.of(2026, 9, 1));
		assertThat(((java.sql.Date) curso.get("fecha_fin")).toLocalDate()).isEqualTo(LocalDate.of(2027, 6, 30));

		var admin = jdbc.queryForMap(
			"SELECT u.email, u.debe_cambiar_password, u.email_verificado, um.rol "
				+ "FROM usuario u JOIN usuario_mancomunidad um ON um.usuario_id = u.id "
				+ "JOIN mancomunidad m ON m.id = um.mancomunidad_id "
				+ "WHERE m.codigo = 'demo'");
		assertThat(admin.get("email")).isEqualTo("admin@mancomunidad.local");
		assertThat(admin.get("debe_cambiar_password")).isEqualTo(true);
		assertThat(admin.get("email_verificado")).isEqualTo(true);
		assertThat(admin.get("rol")).isEqualTo("ADMINISTRADOR");
	}

	@Test
	void debePermitirSoloUnCursoActivoPorMancomunidad() {
		for (String codigo : new String[] { "demo", "demo-b" }) {
			var mancomunidadId = jdbc.queryForObject(
				"SELECT id FROM mancomunidad WHERE codigo = ?", Long.class, codigo);

			assertThat(mancomunidadId).as(codigo).isNotNull();
			assertThat(jdbc.queryForObject(
				"SELECT COUNT(*) FROM curso_escolar WHERE mancomunidad_id = ? AND estado = 'ACTIVO'",
				Integer.class, mancomunidadId)).as(codigo).isEqualTo(1);
		}
	}

}
