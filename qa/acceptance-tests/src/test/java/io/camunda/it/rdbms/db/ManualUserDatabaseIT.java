/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.it.rdbms.db;

import static io.camunda.zeebe.test.util.testcontainers.TestSearchContainers.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.db.rdbms.RdbmsService;
import io.camunda.db.rdbms.write.RdbmsWriter;
import io.camunda.db.rdbms.write.domain.ProcessInstanceDbModel;
import io.camunda.it.rdbms.db.fixtures.ProcessInstanceFixtures;
import io.camunda.it.rdbms.db.util.CamundaRdbmsTestApplication;
import io.camunda.it.rdbms.db.util.RdbmsTestConfiguration;
import io.camunda.search.entities.ProcessInstanceEntity.ProcessInstanceState;
import io.camunda.zeebe.test.util.testcontainers.ManualUserDatabaseContainerWrapper;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * Integration test that verifies Camunda works with manually created database users that have
 * restricted privileges. This simulates production-like setups where the database user is not a
 * superuser with all privileges.
 */
@Tag("rdbms")
public class ManualUserDatabaseIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(ManualUserDatabaseIT.class);
  private static final List<TestDatabase> TEST_DATABASES = initializeDatabases();

  private static List<TestDatabase> initializeDatabases() {
    return List.of(
        new TestDatabase("PostgreSQL", createPostgresContainerWithManualUser()),
        new TestDatabase("MySQL", createMySQLContainerWithManualUser()),
        new TestDatabase("MariaDB", createMariaDBContainerWithManualUser()),
        new TestDatabase("Oracle", createOracleContainerWithManualUser()),
        new TestDatabase("MSSQL", createMSSQLServerContainerWithManualUser()));
  }

  @BeforeAll
  static void startContainers() {
    LOGGER.info("Starting database containers with manual users...");
    TEST_DATABASES.forEach(
        db -> {
          LOGGER.info("Starting {} container...", db.name);
          db.container.start();
          LOGGER.info("{} container started", db.name);
        });
  }

  @AfterAll
  static void stopContainers() {
    LOGGER.info("Stopping database containers...");
    TEST_DATABASES.forEach(
        db -> {
          LOGGER.info("Stopping {} container...", db.name);
          db.container.stop();
          LOGGER.info("{} container stopped", db.name);
        });
  }

  @Test
  void shouldConnectToPostgresWithManualUser() {
    testDatabaseConnection(TEST_DATABASES.get(0));
  }

  @Test
  void shouldConnectToMySQLWithManualUser() {
    testDatabaseConnection(TEST_DATABASES.get(1));
  }

  @Test
  void shouldConnectToMariaDBWithManualUser() {
    testDatabaseConnection(TEST_DATABASES.get(2));
  }

  @Test
  void shouldConnectToOracleWithManualUser() {
    testDatabaseConnection(TEST_DATABASES.get(3));
  }

  @Test
  void shouldConnectToMSSQLWithManualUser() {
    testDatabaseConnection(TEST_DATABASES.get(4));
  }

  private void testDatabaseConnection(final TestDatabase testDb) {
    LOGGER.info("Testing {} with manual user...", testDb.name);

    final String jdbcUrl = ManualUserDatabaseContainerWrapper.getJdbcUrl(testDb.container);
    final String username = ManualUserDatabaseContainerWrapper.getUsername(testDb.container);
    final String password = ManualUserDatabaseContainerWrapper.getPassword(testDb.container);

    LOGGER.info("Connecting to {} at {} with user {}", testDb.name, jdbcUrl, username);

    try (final CamundaRdbmsTestApplication app =
        new CamundaRdbmsTestApplication(RdbmsTestConfiguration.class)
            .withRdbms()
            .withUnifiedConfig(
                config -> {
                  final var rdbms = config.getData().getSecondaryStorage().getRdbms();
                  rdbms.setUrl(jdbcUrl);
                  rdbms.setUsername(username);
                  rdbms.setPassword(password);
                })
            .start()) {

      // Verify we can get the RdbmsService
      final RdbmsService rdbmsService = app.getRdbmsService();
      assertThat(rdbmsService).isNotNull();

      // Verify we can create a writer
      final RdbmsWriter rdbmsWriter = rdbmsService.createWriter(0);
      assertThat(rdbmsWriter).isNotNull();

      // Verify we can write and read data
      final ProcessInstanceDbModel processInstance =
          ProcessInstanceFixtures.createRandomized(
              b ->
                  b.processInstanceKey(1000L)
                      .processDefinitionKey(100L)
                      .processDefinitionId("test-process")
                      .state(ProcessInstanceState.ACTIVE));

      rdbmsWriter.getProcessInstanceWriter().create(processInstance);
      rdbmsWriter.flush();

      // Verify we can read the data back
      final var reader = rdbmsService.getProcessInstanceReader();
      final var result = reader.findOne(1000L);

      assertThat(result).isNotNull();
      assertThat(result.processInstanceKey()).isEqualTo(1000L);
      assertThat(result.processDefinitionId()).isEqualTo("test-process");

      LOGGER.info("Successfully validated {} with manual user", testDb.name);
    } catch (final Exception e) {
      LOGGER.error("Failed to test {} with manual user", testDb.name, e);
      throw new RuntimeException("Failed to test " + testDb.name + " with manual user", e);
    }
  }

  private static class TestDatabase {
    final String name;
    final JdbcDatabaseContainer<?> container;

    TestDatabase(final String name, final JdbcDatabaseContainer<?> container) {
      this.name = name;
      this.container = container;
    }
  }
}
