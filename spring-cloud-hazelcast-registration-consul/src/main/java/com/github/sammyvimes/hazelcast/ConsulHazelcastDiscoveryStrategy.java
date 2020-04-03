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

import com.ecwid.consul.v1.agent.model.NewService;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.util.StringUtil;

import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.lang.NonNull;

/**
 * @author Semyon Danilov
 */
public class ConsulHazelcastDiscoveryStrategy extends BaseDiscoveryStrategy<ConsulRegistration> {

	private final List<HazelcastServiceCustomization<ConsulRegistration>> customizers;

	private final ConsulDiscoveryProperties consulDiscoveryProperties;

	public ConsulHazelcastDiscoveryStrategy(final DiscoveryNode discoveryNode,
											final ILogger logger, final Map<String, Comparable> properties,
											final ConsulServiceRegistry registry,
											final SpringCloudHazelcastProperties hazelcastProperties,
											final ConsulDiscoveryProperties consulDiscoveryProperties,
											final ConsulDiscoveryClient client,
											final List<HazelcastServiceCustomization<ConsulRegistration>> customizers) {
		super(logger, properties, registry, client, discoveryNode, hazelcastProperties);
		this.consulDiscoveryProperties = consulDiscoveryProperties;
		this.customizers = customizers;
	}

	@NonNull
	ConsulRegistration createRegistration() {
		final Address address = discoveryNode.getPublicAddress();

		final String serviceName = hazelcastProperties.getConsul().getServiceName();

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
						+ "host advertised via Consul [%s] doesn't match. This can lead to an error.",
					discoveryNodeAddress, propertiesHost));
			}
			host = propertiesHost;
		}

		NewService hz = new NewService();
		hz.setAddress(host);
		hz.setPort(port);
		hz.setTags(hazelcastProperties.getConsul().getTags());
		hz.setName(serviceName);
		hz.setId(String.format("%s(%s:%d)", serviceName, host, port));

		final ConsulRegistration consulRegistration = new ConsulRegistration(hz, consulDiscoveryProperties);

		customizers.forEach(customizer -> customizer.customize(consulRegistration, discoveryNode));

		return consulRegistration;
	}

}
