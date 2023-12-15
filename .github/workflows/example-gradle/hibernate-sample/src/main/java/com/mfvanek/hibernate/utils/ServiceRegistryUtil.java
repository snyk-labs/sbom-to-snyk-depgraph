package com.mfvanek.hibernate.utils;

import com.mfvanek.hibernate.consts.Const;
import lombok.experimental.UtilityClass;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

@UtilityClass
public final class ServiceRegistryUtil {

    public static ServiceRegistry build() {
        return new StandardServiceRegistryBuilder()
                .configure(Const.HIBERNATE_CONFIG_FILE_NAME)
                .loadProperties(Const.PROPERTY_FILE_NAME)
                .build();
    }
}
