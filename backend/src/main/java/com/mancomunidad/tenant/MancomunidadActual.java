package com.mancomunidad.tenant;

/**
 * Datos públicos de la mancomunidad activa en la petición.
 */
public record MancomunidadActual(Long id, String nombre, String codigo, String tipo) {}
