package com.mancomunidad.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTests {

    @AfterEach
    void limpiarContexto() {
        TenantContext.clear();
    }

    @Test
    void debeGuardarYDevolverLaMancomunidadActiva() {
        TenantContext.set(7L);

        assertThat(TenantContext.hayTenant()).isTrue();
        assertThat(TenantContext.get()).contains(7L);
        assertThat(TenantContext.require()).isEqualTo(7L);
    }

    @Test
    void debeFallarSiNoHayMancomunidadActiva() {
        assertThat(TenantContext.get()).isEmpty();
        assertThat(TenantContext.hayTenant()).isFalse();
        assertThatThrownBy(TenantContext::require).isInstanceOf(TenantNoResueltoException.class);
    }

    @Test
    void debeLimpiarElContexto() {
        TenantContext.set(3L);
        TenantContext.clear();

        assertThat(TenantContext.get()).isEmpty();
    }
}
