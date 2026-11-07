package com.mancomunidad.tenant;

import com.mancomunidad.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TenantEndpointTests extends IntegrationTestBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private MancomunidadActualService service;

	@Test
	void debeDevolverLaMancomunidadDeLaCabecera() throws Exception {
		Long demoId = idDe("demo");

		mockMvc.perform(get("/api/v1/tenant/actual").header(HeaderTenantResolver.CABECERA, demoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(demoId))
			.andExpect(jsonPath("$.codigo").value("demo"))
			.andExpect(jsonPath("$.nombre").value("Mancomunidad Demo"))
			.andExpect(jsonPath("$.tipo").value("MUSICA"));
	}

	@Test
	void noDebeMezclarDosMancomunidadesEnPeticionesSeguidas() throws Exception {
		Long demoId = idDe("demo");
		Long demoBId = idDe("demo-b");

		mockMvc.perform(get("/api/v1/tenant/actual").header(HeaderTenantResolver.CABECERA, demoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.codigo").value("demo"));

		mockMvc.perform(get("/api/v1/tenant/actual").header(HeaderTenantResolver.CABECERA, demoBId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.codigo").value("demo-b"))
			.andExpect(jsonPath("$.tipo").value("DANZA"));

		assertThat(TenantContext.hayTenant()).isFalse();
	}

	@Test
	void debeResponder404SinCabeceraDeMancomunidad() throws Exception {
		mockMvc.perform(get("/api/v1/tenant/actual"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith("application/problem+json"))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.code").value("TENANT_NO_RESUELTO"));
	}

	@Test
	void debeResponder404SiLaMancomunidadNoExiste() throws Exception {
		mockMvc.perform(get("/api/v1/tenant/actual").header(HeaderTenantResolver.CABECERA, 999_999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("TENANT_NO_RESUELTO"));
	}

	@Test
	void debeResponder404SiLaCabeceraNoEsUnNumero() throws Exception {
		mockMvc.perform(get("/api/v1/tenant/actual").header(HeaderTenantResolver.CABECERA, "demo"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("TENANT_NO_RESUELTO"));
	}

	@Test
	void elServicioNoDevuelveDatosSinTenantActivo() {
		TenantContext.clear();

		assertThatThrownBy(() -> service.obtenerActual())
			.isInstanceOf(TenantNoResueltoException.class)
			.hasMessageContaining("mancomunidad activa");
	}

	private Long idDe(String codigo) {
		return jdbc.queryForObject(
			"SELECT id FROM mancomunidad WHERE codigo = ?", Long.class, codigo);
	}

}
