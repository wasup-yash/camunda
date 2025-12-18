/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.exporter.handlers.auditlog;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.search.entities.AuditLogEntity.AuditLogOperationType;
import io.camunda.webapps.schema.entities.auditlog.AuditLogEntity;
import io.camunda.webapps.schema.entities.auditlog.AuditLogTenantScope;
import io.camunda.zeebe.exporter.common.auditlog.AuditLogInfo;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.GroupIntent;
import io.camunda.zeebe.protocol.record.value.EntityType;
import io.camunda.zeebe.protocol.record.value.GroupRecordValue;
import io.camunda.zeebe.protocol.record.value.ImmutableGroupRecordValue;
import io.camunda.zeebe.test.broker.protocol.ProtocolFactory;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GroupEntityAuditLogHandlerTest {

  private final ProtocolFactory factory = new ProtocolFactory();
  private final GroupEntityAuditLogTransformer transformer = new GroupEntityAuditLogTransformer();

  public static Stream<Arguments> getIntentMappings() {
    return Stream.of(
        Arguments.of(GroupIntent.ENTITY_ADDED, AuditLogOperationType.ASSIGN),
        Arguments.of(GroupIntent.ENTITY_REMOVED, AuditLogOperationType.UNASSIGN));
  }

  public static Stream<Arguments> getEntityTypeMappings() {
    return Stream.of(
        Arguments.of(GroupIntent.ENTITY_ADDED, EntityType.USER, "user-456"),
        Arguments.of(GroupIntent.ENTITY_REMOVED, EntityType.GROUP, "group-101"),
        Arguments.of(GroupIntent.ENTITY_ADDED, EntityType.MAPPING_RULE, "mapping-rule-123"));
  }

  @MethodSource("getIntentMappings")
  @ParameterizedTest
  void shouldTransformGroupEntityRecord(
      final GroupIntent intent, final AuditLogOperationType operationType) {
    // given
    final GroupRecordValue recordValue =
        ImmutableGroupRecordValue.builder()
            .from(factory.generateObject(GroupRecordValue.class))
            .withGroupId("group-123")
            .withEntityId("entity-456")
            .withEntityType(EntityType.USER)
            .build();

    final Record<GroupRecordValue> record =
        factory.generateRecord(ValueType.GROUP, r -> r.withIntent(intent).withValue(recordValue));

    // when
    final AuditLogEntity entity = new AuditLogEntity();
    transformer.transform(record, entity);

    // then
    assertThat(entity.getEntityKey()).isEqualTo("entity-456");
    assertThat(entity.getTenantScope()).isEqualTo(AuditLogTenantScope.GLOBAL);

    final AuditLogInfo auditLogInfo = AuditLogInfo.of(record);
    assertThat(auditLogInfo.operationType()).isEqualTo(operationType);
  }

  @MethodSource("getEntityTypeMappings")
  @ParameterizedTest
  void shouldTransformGroupEntityRecordWithDifferentEntityTypes(
      final GroupIntent intent, final EntityType entityType, final String entityId) {
    // given
    final GroupRecordValue recordValue =
        ImmutableGroupRecordValue.builder()
            .from(factory.generateObject(GroupRecordValue.class))
            .withGroupId("group-123")
            .withEntityId(entityId)
            .withEntityType(entityType)
            .build();

    final Record<GroupRecordValue> record =
        factory.generateRecord(ValueType.GROUP, r -> r.withIntent(intent).withValue(recordValue));

    // when
    final AuditLogEntity entity = new AuditLogEntity();
    transformer.transform(record, entity);

    // then
    assertThat(entity.getEntityKey()).isEqualTo(entityId);
    assertThat(entity.getTenantScope()).isEqualTo(AuditLogTenantScope.GLOBAL);
  }
}
