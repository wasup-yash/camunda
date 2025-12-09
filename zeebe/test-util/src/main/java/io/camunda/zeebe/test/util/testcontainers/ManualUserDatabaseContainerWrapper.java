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
   * Returns the JDBC URL for connecting with the manual user. For most databases this connects
   * to the camunda database.
   */
  public static String getJdbcUrl(final JdbcDatabaseContainer<?> container) {
    // All databases use the camunda database with the init script
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
    return "Camunda_Pass123!";
  }
}
