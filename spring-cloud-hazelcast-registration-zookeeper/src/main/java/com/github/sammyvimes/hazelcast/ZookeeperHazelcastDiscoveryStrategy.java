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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.util.StringUtil;
import org.apache.curator.x.discovery.ServiceType;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;

/**
 * @author Semyon Danilov
 */
public class ZookeeperHazelcastDiscoveryStrategy extends BaseDiscoveryStrategy<ZookeeperRegistration> {

	private final List<HazelcastServiceCustomization<ZookeeperRegistration>> customizers;

	private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

	public ZookeeperHazelcastDiscoveryStrategy(final DiscoveryNode discoveryNode,
		final ILogger logger,
		final Map<String, Comparable> properties,
		final ServiceRegistry<ZookeeperRegistration> registry,
		final SpringCloudHazelcastProperties hazelcastProperties,
		final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties,
		final DiscoveryClient discoveryClient,
		final List<HazelcastServiceCustomization<ZookeeperRegistration>> customizers) {
		super(logger, properties, registry, discoveryClient, discoveryNode, hazelcastProperties);
		this.customizers = customizers;
		this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
	}

	@Override
	ZookeeperRegistration createRegistration() {
		final Address address = discoveryNode.getPublicAddress();

		final String serviceName = hazelcastProperties.getZookeeper().getServiceName();

		final int port;
		final String host;

		if (hazelcastProperties.getPort() == null) {
			port = address.getPort();
		}
		else {
			port = hazelcastProperties.getPort();
		}

		final String discoveryNodeAddress = address.getHost();
		final String propertiesHost = hazelcastProperties.getHost();

		if (StringUtil.isNullOrEmpty(propertiesHost)) {
			host = discoveryNodeAddress;
		}
		else {
			if (!Objects.equals(propertiesHost, discoveryNodeAddress)) {
				getLogger().warning(String.format(
					"Probable misconfiguration, configured hazelcast host [%s] and "
						+ "host advertised via Zookeeper [%s] doesn't match. This can lead to an error.",
					discoveryNodeAddress, propertiesHost));
			}
			host = propertiesHost;
		}

		final ServiceInstanceRegistration registration = ServiceInstanceRegistration.builder()
			.id(String.format("%s(%s:%d)", serviceName, host, port))
			.name(serviceName)
			.address(host)
			.port(port)
			.serviceType(ServiceType.DYNAMIC).build();

		customizers.forEach(customizer -> customizer.customize(registration, discoveryNode));

		return registration;
	}

}
