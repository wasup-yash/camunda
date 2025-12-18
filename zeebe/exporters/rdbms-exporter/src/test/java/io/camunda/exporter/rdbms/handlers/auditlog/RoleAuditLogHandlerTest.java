/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.exporter.rdbms.handlers.auditlog;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.db.rdbms.write.domain.AuditLogDbModel;
import io.camunda.search.entities.AuditLogEntity.AuditLogOperationType;
import io.camunda.search.entities.AuditLogEntity.AuditLogTenantScope;
import io.camunda.zeebe.exporter.common.auditlog.AuditLogInfo;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.RoleIntent;
import io.camunda.zeebe.protocol.record.value.ImmutableRoleRecordValue;
import io.camunda.zeebe.protocol.record.value.RoleRecordValue;
import io.camunda.zeebe.test.broker.protocol.ProtocolFactory;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoleAuditLogHandlerTest {

  private final ProtocolFactory factory = new ProtocolFactory();
  private final RoleAuditLogTransformer transformer = new RoleAuditLogTransformer();

  public static Stream<Arguments> getIntentMappings() {
    return Stream.of(
        Arguments.of(RoleIntent.CREATED, AuditLogOperationType.CREATE),
        Arguments.of(RoleIntent.UPDATED, AuditLogOperationType.UPDATE),
        Arguments.of(RoleIntent.DELETED, AuditLogOperationType.DELETE));
  }

  @MethodSource("getIntentMappings")
  @ParameterizedTest
  void shouldTransformRoleRecord(
      final RoleIntent intent, final AuditLogOperationType operationType) {
    // given
    final RoleRecordValue recordValue =
        ImmutableRoleRecordValue.builder()
            .from(factory.generateObject(RoleRecordValue.class))
            .withRoleId("role-123")
            .withName("Test Role")
            .withDescription("A test role")
            .build();

    final Record<RoleRecordValue> record =
        factory.generateRecord(ValueType.ROLE, r -> r.withIntent(intent).withValue(recordValue));

    // when
    final AuditLogDbModel.Builder builder = new AuditLogDbModel.Builder();
    transformer.transform(record, builder);
    final var entity = builder.build();

    // then
    assertThat(entity.entityKey()).isEqualTo("role-123");
    assertThat(entity.tenantScope()).isEqualTo(AuditLogTenantScope.GLOBAL);

    final AuditLogInfo auditLogInfo = AuditLogInfo.of(record);
    assertThat(auditLogInfo.operationType()).isEqualTo(operationType);
  }
}
