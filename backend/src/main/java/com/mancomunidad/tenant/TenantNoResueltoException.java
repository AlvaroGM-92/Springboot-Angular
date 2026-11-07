package com.mancomunidad.tenant;

/**
 * La petición no tiene una mancomunidad activa o la indicada no existe.
 */
public class TenantNoResueltoException extends RuntimeException {

	public TenantNoResueltoException(String mensaje) {
		super(mensaje);
	}

}
