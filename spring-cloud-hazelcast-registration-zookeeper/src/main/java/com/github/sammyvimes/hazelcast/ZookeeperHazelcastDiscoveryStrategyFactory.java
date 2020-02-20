/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.sammyvimes.hazelcast;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;

/**
 * @author Semyon Danilov
 */
public class ZookeeperHazelcastDiscoveryStrategyFactory implements DiscoveryStrategyFactory {

	private final ZookeeperServiceRegistry registry;

	private final ZookeeperDiscoveryProperties discoveryProperties;

	private final ZookeeperDiscoveryClient client;

	private List<HazelcastServiceCustomization<ZookeeperRegistration>> customizers;

	private final SpringCloudHazelcastProperties hazelcastProperties;

	public ZookeeperHazelcastDiscoveryStrategyFactory(final ZookeeperServiceRegistry registry,
		final ZookeeperDiscoveryProperties discoveryProperties,
		final SpringCloudHazelcastProperties hazelcastProperties,
		final ZookeeperDiscoveryClient client,
		final List<HazelcastServiceCustomization<ZookeeperRegistration>> customizers) {
		this.registry = registry;
		this.discoveryProperties = discoveryProperties;
		this.hazelcastProperties = hazelcastProperties;
		this.client = client;
		this.customizers = customizers;
	}

	@Override
	public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
		return ZookeeperHazelcastDiscoveryStrategy.class;
	}

	@Override
	public DiscoveryStrategy newDiscoveryStrategy(final DiscoveryNode discoveryNode,
		final ILogger logger, final Map<String, Comparable> properties) {
		return new ZookeeperHazelcastDiscoveryStrategy(discoveryNode, logger, properties,
			registry, hazelcastProperties, discoveryProperties, client,
			customizers);
	}

	@Override
	public Collection<PropertyDefinition> getConfigurationProperties() {
		return null;
	}

}
