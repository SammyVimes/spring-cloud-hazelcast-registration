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

import com.github.sammyvimes.HazelcastNewServiceCustomization;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;

/**
 * @author Semyon Danilov
 */
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
	public DiscoveryStrategy newDiscoveryStrategy(final DiscoveryNode discoveryNode,
			final ILogger logger, final Map<String, Comparable> properties) {
		return new ConsulHazelcastDiscoveryStrategy(discoveryNode, logger, properties,
				registry, hazelcastProperties, consulDiscoveryProperties, client,
				customizers);
	}

	@Override
	public Collection<PropertyDefinition> getConfigurationProperties() {
		return null;
	}

}
