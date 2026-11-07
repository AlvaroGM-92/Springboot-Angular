package com.mancomunidad.tenant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone la mancomunidad activa de la petición.
 *
 * <p>Solo se publica mientras el tenant se resuelva por cabecera (desarrollo y
 * pruebas). En la Fase 2 lo sustituye el endpoint de autenticación basado en el
 * JWT.</p>
 */
@RestController
@RequestMapping("/api/v1/tenant")
@ConditionalOnProperty(prefix = "app.tenant", name = "header-enabled", havingValue = "true")
public class TenantController {

	private final MancomunidadActualService service;

	public TenantController(MancomunidadActualService service) {
		this.service = service;
	}

	@GetMapping("/actual")
	public MancomunidadActual actual() {
		return service.obtenerActual();
	}

}
