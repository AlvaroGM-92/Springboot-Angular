package com.mancomunidad.tenant;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Obtiene la mancomunidad activa de una petición.
 *
 * <p>En la Fase 2 se sustituirá por una implementación que lea el JWT. Hasta
 * entonces, el perfil de desarrollo usa una cabecera para poder probar el
 * aislamiento sin autenticación.</p>
 */
public interface TenantResolver {

	Optional<Long> resolver(HttpServletRequest request);

}
