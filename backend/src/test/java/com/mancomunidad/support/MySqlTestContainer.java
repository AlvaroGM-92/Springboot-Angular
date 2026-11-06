package com.mancomunidad.support;

import org.testcontainers.mysql.MySQLContainer;

/**
 * Contenedor MySQL compartido por todas las pruebas de integración del mismo JVM.
 * Testcontainers lo elimina al terminar la ejecución mediante Ryuk.
 */
public final class MySqlTestContainer {

	private static final MySQLContainer INSTANCE = new MySQLContainer("mysql:8.4");

	static {
		INSTANCE.start();
	}

	private MySqlTestContainer() {
	}

	public static MySQLContainer get() {
		return INSTANCE;
	}

}
