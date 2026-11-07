package com.mancomunidad.tenant;

import java.util.Optional;

/**
 * Mancomunidad activa de la petición actual.
 *
 * <p>El contexto se rellena en el filtro de tenant y se limpia siempre al
 * terminar la petición. Ningún controlador ni servicio debe aceptar el
 * identificador de mancomunidad desde el cliente.</p>
 */
public final class TenantContext {

    private static final ThreadLocal<Long> MANCOMUNIDAD_ACTUAL = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Long mancomunidadId) {
        MANCOMUNIDAD_ACTUAL.set(mancomunidadId);
    }

    public static Optional<Long> get() {
        return Optional.ofNullable(MANCOMUNIDAD_ACTUAL.get());
    }

    public static boolean hayTenant() {
        return MANCOMUNIDAD_ACTUAL.get() != null;
    }

    public static Long require() {
        Long mancomunidadId = MANCOMUNIDAD_ACTUAL.get();
        if (mancomunidadId == null) {
            throw new TenantNoResueltoException("No hay una mancomunidad activa en la petición");
        }
        return mancomunidadId;
    }

    public static void clear() {
        MANCOMUNIDAD_ACTUAL.remove();
    }
}
