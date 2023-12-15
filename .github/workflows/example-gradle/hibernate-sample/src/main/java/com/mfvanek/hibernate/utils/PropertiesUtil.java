package com.mfvanek.hibernate.utils;

import com.mfvanek.hibernate.consts.Const;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
@UtilityClass
public final class PropertiesUtil {

    @SneakyThrows
    public static Properties load() {
        final Properties properties = new Properties();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(Const.PROPERTY_FILE_NAME)) {
            properties.load(inputStream);
        }
        return properties;
    }
}
