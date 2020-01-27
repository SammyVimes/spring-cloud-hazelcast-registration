package com.github.sammyvimes.hazelcast;

import com.ecwid.consul.v1.agent.model.NewService;
import com.github.sammyvimes.HazelcastNewServiceCustomization;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConsulHazelcastDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

    private final ConsulServiceRegistry registry;

    private final ConsulDiscoveryProperties consulDiscoveryProperties;

    private final ConsulDiscoveryClient client;
    private List<HazelcastNewServiceCustomization> customizers;

    private final SpringCloudHazelcastProperties hazelcastProperties;

    public ConsulHazelcastDiscoveryStrategyFactory(final ConsulServiceRegistry registry,
                                                   final ConsulDiscoveryProperties consulDiscoveryProperties,
                                                   final SpringCloudHazelcastProperties hazelcastProperties,
                                                   final ConsulDiscoveryClient client,
                                                   final List<HazelcastNewServiceCustomization> customizers) {
        this.registry = registry;
        this.consulDiscoveryProperties = consulDiscoveryProperties;
        this.hazelcastProperties = hazelcastProperties;
        this.client = client;
        this.customizers = customizers;
    }

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return ConsulHazelcastDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(final DiscoveryNode discoveryNode, final ILogger logger,
                                                  final Map<String, Comparable> properties) {
        return new ConsulHazelcastDiscoveryStrategy(discoveryNode, logger, properties, registry,
                hazelcastProperties, consulDiscoveryProperties, client, customizers);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return null;
    }
}
