/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import io.camunda.configuration.UnifiedConfigurationHelper.BackwardsCompatibilityMode;
import io.camunda.configuration.beanoverrides.GatewayBasedPropertiesOverride;
import io.camunda.configuration.beans.GatewayBasedProperties;
import java.io.File;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig({
  UnifiedConfiguration.class,
  UnifiedConfigurationHelper.class,
  GatewayBasedPropertiesOverride.class,
})
public class TlsClusterGatewayTest {

  @Nested
  @TestPropertySource(
      properties = {
        "camunda.security.transport-layer-security.cluster.enabled=true",
        "camunda.security.transport-layer-security.cluster.certificate-chain-path=certificateChainPath",
        "camunda.security.transport-layer-security.cluster.certificate-private-key-path=certificatePrivateKeyPath",
        "camunda.security.transport-layer-security.cluster.key-store.file-path=keyStoreFilePath",
        "camunda.security.transport-layer-security.cluster.key-store.password=keyStorePassword",
      })
  class WithOnlyUnifiedConfigSet {
    final GatewayBasedProperties gatewayBasedProperties;

    WithOnlyUnifiedConfigSet(@Autowired final GatewayBasedProperties gatewayBasedProperties) {
      this.gatewayBasedProperties = gatewayBasedProperties;
    }

    @Test
    void testCamundaGatewayProperties() {
      assertThat(gatewayBasedProperties.getCluster().getSecurity())
          .returns(true, io.camunda.zeebe.gateway.impl.configuration.SecurityCfg::isEnabled)
          .returns(
              new File("certificateChainPath"),
              io.camunda.zeebe.gateway.impl.configuration.SecurityCfg::getCertificateChainPath)
          .returns(
              new File("certificatePrivateKeyPath"),
              io.camunda.zeebe.gateway.impl.configuration.SecurityCfg::getPrivateKeyPath);

      assertThat(gatewayBasedProperties.getCluster().getSecurity().getKeyStore())
          .returns(
              new File("keyStoreFilePath"),
              io.camunda.zeebe.gateway.impl.configuration.KeyStoreCfg::getFilePath)
          .returns(
              "keyStorePassword",
              io.camunda.zeebe.gateway.impl.configuration.KeyStoreCfg::getPassword);
    }
  }

  @Nested
  @TestPropertySource(
      properties = {
        // new
        "camunda.security.transport-layer-security.cluster.enabled=true",
        "camunda.security.transport-layer-security.cluster.certificate-chain-path=certificateChainPath",
        "camunda.security.transport-layer-security.cluster.certificate-private-key-path=certificatePrivateKeyPath",
        "camunda.security.transport-layer-security.cluster.key-store.file-path=keyStoreFilePath",
        "camunda.security.transport-layer-security.cluster.key-store.password=keyStorePassword",
        // legacy
        "zeebe.gateway.cluster.security.enabled=false",
        "zeebe.gateway.cluster.security.certificateChainPath=certificateChainPathLegacy",
        "zeebe.gateway.cluster.security.privateKeyPath=certificatePrivateKeyPathLegacy",
        "zeebe.gateway.cluster.security.keyStore.filePath=certificateKeyStoreFilePathLegacy",
        "zeebe.gateway.cluster.security.keyStore.password=certificateKeyStorePasswordLegacy",
      })
  class WithNewAndLegacySet {
    final GatewayBasedProperties gatewayBasedProperties;

    WithNewAndLegacySet(@Autowired final GatewayBasedProperties gatewayBasedProperties) {
      this.gatewayBasedProperties = gatewayBasedProperties;
    }

    @Test
    void testCamundaGatewayProperties() {
      assertThat(gatewayBasedProperties.getCluster().getSecurity())
          .returns(true, io.camunda.zeebe.gateway.impl.configuration.SecurityCfg::isEnabled)
          .returns(
              new File("certificateChainPath"),
              io.camunda.zeebe.gateway.impl.configuration.SecurityCfg::getCertificateChainPath)
          .returns(
              new File("certificatePrivateKeyPath"),
              io.camunda.zeebe.gateway.impl.configuration.SecurityCfg::getPrivateKeyPath);

      assertThat(gatewayBasedProperties.getCluster().getSecurity().getKeyStore())
          .returns(
              new File("keyStoreFilePath"),
              io.camunda.zeebe.gateway.impl.configuration.KeyStoreCfg::getFilePath)
          .returns(
              "keyStorePassword",
              io.camunda.zeebe.gateway.impl.configuration.KeyStoreCfg::getPassword);
    }
  }

  @Nested
  class WithOnlyLegacySet {

    private static final TlsCluster TLS_CLUSTER = new TlsCluster();

    @ParameterizedTest
    @MethodSource("legacyPropertyProvider")
    void shouldSetBackwardCompatibilityModeNotSupported(
        final Object expectedValue, final Supplier<?> getter) {
      try (final MockedStatic<UnifiedConfigurationHelper> unifiedConfigurationHelper =
          mockStatic(UnifiedConfigurationHelper.class)) {

        // given
        unifiedConfigurationHelper
            .when(
                () ->
                    UnifiedConfigurationHelper.validateLegacyConfiguration(
                        any(String.class), any(), any(Class.class), any(), any()))
            .thenReturn(expectedValue);

        // when
        getter.get();

        // then
        unifiedConfigurationHelper.verify(
            () ->
                UnifiedConfigurationHelper.validateLegacyConfiguration(
                    any(),
                    any(),
                    any(Class.class),
                    eq(BackwardsCompatibilityMode.NOT_SUPPORTED),
                    any()));
      }
    }

    static Stream<Arguments> legacyPropertyProvider() {
      return Stream.of(
          Arguments.of(
              true,
              (Supplier<Boolean>) () -> TLS_CLUSTER.withGatewayTlsClusterProperties().isEnabled()),
          Arguments.of(
              new File("certificateChainPath"),
              (Supplier<File>)
                  () -> TLS_CLUSTER.withGatewayTlsClusterProperties().getCertificateChainPath()),
          Arguments.of(
              new File("certificatePrivateKeyPath"),
              (Supplier<File>)
                  () ->
                      TLS_CLUSTER.withGatewayTlsClusterProperties().getCertificatePrivateKeyPath()),
          Arguments.of(
              new File("certificateKeyStoreFilePath"),
              (Supplier<File>)
                  () ->
                      TLS_CLUSTER
                          .getKeyStore()
                          .withGatewayTlsClusterKeyStoreProperties()
                          .getFilePath()),
          Arguments.of(
              "certificateKeyStorePassword",
              (Supplier<String>)
                  () ->
                      TLS_CLUSTER
                          .getKeyStore()
                          .withGatewayTlsClusterKeyStoreProperties()
                          .getPassword()));
    }
  }
}
