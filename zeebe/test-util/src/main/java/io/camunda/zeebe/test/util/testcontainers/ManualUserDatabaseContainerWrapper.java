/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.test.util.testcontainers;

import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * Wrapper for JDBC database containers that provides manual user credentials. This is used to test
 * Camunda with database users that have restricted privileges, simulating production-like setups.
 *
 * <p>Note: This class uses string matching on container class names to avoid adding dependencies
 * on all testcontainers database modules. The passwords are hardcoded as this is only for testing
 * purposes.
 */
public final class ManualUserDatabaseContainerWrapper {

  private ManualUserDatabaseContainerWrapper() {}

  /**
   * Returns the JDBC URL for connecting with the manual user. For most databases this is the same
   * as the default JDBC URL but with a different database name.
   */
  public static String getJdbcUrl(final JdbcDatabaseContainer<?> container) {
    final String className = container.getClass().getName();
    if (className.contains("PostgreSQL")) {
      return container.getJdbcUrl().replace("/test", "/camunda_manual");
    } else if (className.contains("MySQL")) {
      return container.getJdbcUrl().replace("/test", "/camunda_manual");
    } else if (className.contains("MariaDB")) {
      return container.getJdbcUrl().replace("/test", "/camunda_manual");
    } else if (className.contains("Oracle")) {
      // Oracle: connect to pluggable database with user schema
      // The user schema is the same as the username
      return container.getJdbcUrl();
    } else if (className.contains("MSSQL")) {
      // For MSSQL, replace the default master database with our custom database
      final String originalUrl = container.getJdbcUrl();
      if (originalUrl.contains("database=")) {
        return originalUrl.replaceFirst("database=[^;]+", "database=camunda_manual");
      } else {
        return originalUrl + ";database=camunda_manual";
      }
    }
    return container.getJdbcUrl();
  }

  /**
   * Returns the username for the manual user with restricted privileges.
   */
  public static String getUsername(final JdbcDatabaseContainer<?> container) {
    return "camunda_user";
  }

  /**
   * Returns the password for the manual user with restricted privileges.
   */
  public static String getPassword(final JdbcDatabaseContainer<?> container) {
    if (container.getClass().getName().contains("MSSQL")) {
      return "Camunda_Pass123!";
    }
    return "camunda_pass";
  }
}
