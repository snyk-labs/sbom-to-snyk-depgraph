package com.mfvanek.hibernate.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesUtilTest {

    @Test
    void loadShouldWork() {
        assertThat(PropertiesUtil.load())
                .isNotNull()
                .satisfies(p -> assertThat(p.keySet())
                        .hasSize(5)
                        .containsExactlyInAnyOrder("hibernate.connection.driver_class",
                                "hibernate.connection.url",
                                "hibernate.dialect",
                                "hibernate.connection.password",
                                "hibernate.connection.username"));
    }
}
