/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.rest.controller;

import io.camunda.service.TopologyServices;
import io.camunda.service.TopologyServices.Broker;
import io.camunda.service.TopologyServices.Partition;
import io.camunda.util.EnumUtil;
import io.camunda.zeebe.gateway.protocol.rest.BrokerInfo;
import io.camunda.zeebe.gateway.protocol.rest.Partition.HealthEnum;
import io.camunda.zeebe.gateway.protocol.rest.Partition.RoleEnum;
import io.camunda.zeebe.gateway.protocol.rest.TopologyResponse;
import io.camunda.zeebe.gateway.rest.annotation.CamundaGetMapping;
import io.camunda.zeebe.gateway.rest.util.KeyUtil;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;

@CamundaRestController
@RequestMapping(path = {"/v1", "/v2"})
public final class TopologyController {

  private final TopologyServices topologyServices;

  public TopologyController(final TopologyServices topologyServices) {
    this.topologyServices = topologyServices;
  }

  @CamundaGetMapping(path = "/topology")
  public TopologyResponse get() {

    final var topology = topologyServices.getTopology();

    final var response = new TopologyResponse();
    response
        .clusterId(topology.clusterId())
        .clusterSize(topology.clusterSize())
        .gatewayVersion(topology.gatewayVersion())
        .partitionsCount(topology.partitionsCount())
        .replicationFactor(topology.replicationFactor())
        .lastCompletedChangeId(KeyUtil.keyToString(topology.lastCompletedChangeId()));

    topology
        .brokers()
        .forEach(
            broker -> {
              final var brokerInfo = new BrokerInfo();
              addBrokerInfo(brokerInfo, broker);
              addPartitionInfoToBrokerInfo(brokerInfo, broker.partitions());

              response.addBrokersItem(brokerInfo);
            });

    return response;
  }

  private void addBrokerInfo(final BrokerInfo brokerInfo, final Broker broker) {
    brokerInfo.setNodeId(broker.nodeId());
    brokerInfo.setHost(broker.host());
    brokerInfo.setPort(broker.port());
    brokerInfo.setVersion(broker.version());
  }

  private void addPartitionInfoToBrokerInfo(
      final BrokerInfo brokerInfo, final List<Partition> partitions) {
    partitions.forEach(
        partition -> {
          final var partitionDto = new io.camunda.zeebe.gateway.protocol.rest.Partition();

          partitionDto.setPartitionId(partition.partitionId());
          partitionDto.setRole(EnumUtil.convert(partition.role(), RoleEnum.class));
          partitionDto.setHealth(EnumUtil.convert(partition.health(), HealthEnum.class));
          brokerInfo.addPartitionsItem(partitionDto);
        });
  }
}
