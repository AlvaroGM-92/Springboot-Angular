package com.mancomunidad.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Traduce los fallos de resolución del tenant a respuestas RFC 7807.
 *
 * <p>Un recurso sin mancomunidad activa responde {@code 404}: no se revela si
 * falta la cabecera, si la mancomunidad no existe o si pertenece a otro
 * tenant.</p>
 */
@RestControllerAdvice
public class TenantExceptionHandler {

	@ExceptionHandler(TenantNoResueltoException.class)
	public ProblemDetail manejarTenantNoResuelto(TenantNoResueltoException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problema.setType(URI.create("https://api.mancomunidad.local/errors/tenant-no-resuelto"));
		problema.setTitle("Mancomunidad no disponible");
		problema.setProperty("code", "TENANT_NO_RESUELTO");
		return problema;
	}

}
