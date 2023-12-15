package com.mfvanek.hibernate.utils;

import com.mfvanek.hibernate.consts.Const;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertiesUtil.class);

    public static Properties load() {
        try {
            final Properties properties = new Properties();
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = classLoader.getResourceAsStream(Const.PROPERTY_FILE_NAME)) {
                properties.load(inputStream);
            }
            return properties;
        } catch (final java.lang.Throwable $ex) {
            throw lombok.Lombok.sneakyThrow($ex);
        }
    }

    private PropertiesUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
