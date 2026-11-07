package com.mancomunidad.tenant;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Resolución provisional del tenant por cabecera para desarrollo y pruebas.
 *
 * <p>Se activa con {@code app.tenant.header-enabled=true} y desaparecerá en la
 * Fase 2, cuando el tenant se obtenga del JWT. La cabecera solo se acepta si la
 * mancomunidad existe y está activa.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.tenant", name = "header-enabled", havingValue = "true")
public class HeaderTenantResolver implements TenantResolver {

    public static final String CABECERA = "X-Mancomunidad-Id";

    private final JdbcTemplate jdbc;

    public HeaderTenantResolver(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Long> resolver(HttpServletRequest request) {
        String valor = request.getHeader(CABECERA);
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        try {
            Long mancomunidadId = Long.valueOf(valor);
            Integer encontradas = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM mancomunidad WHERE id = ? AND activo = TRUE", Integer.class, mancomunidadId);
            return encontradas != null && encontradas > 0 ? Optional.of(mancomunidadId) : Optional.empty();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
