package com.mfvanek.hibernate.consts;

public abstract class Const {

    public static final String PROPERTY_FILE_NAME = "config.properties";
    public static final String HIBERNATE_CONFIG_FILE_NAME = "hibernate.cfg.xml";
    public static final String URL_PROPERTY_NAME = "hibernate.connection.url";
    public static final String USERNAME_PROPERTY_NAME = "hibernate.connection.username";
    public static final String PASSWORD_PROPERTY_NAME = "hibernate.connection.password";
    public static final String DRIVER_PROPERTY_NAME = "hibernate.connection.driver_class";
    public static final String LIQUIBASE_CHANGELOG_FILE = "db/changelog/liquibase-changelog.xml";

    /**
     * There is another approach
     * https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#multitenacy
     */
    public static final String SCHEMA_NAME = "alien";
}
