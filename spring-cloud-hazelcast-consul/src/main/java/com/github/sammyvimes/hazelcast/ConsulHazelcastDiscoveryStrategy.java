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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ecwid.consul.v1.agent.model.NewService;
import com.github.sammyvimes.HazelcastNewServiceCustomization;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.util.StringUtil;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.lang.NonNull;

/**
 * @author Semyon Danilov
 */
public class ConsulHazelcastDiscoveryStrategy extends AbstractDiscoveryStrategy {

	private final DiscoveryNode discoveryNode;

	private final ConsulServiceRegistry registry;

	private ConsulRegistration hazelcastRegistration;

	private final ConsulDiscoveryClient client;

	private final List<HazelcastNewServiceCustomization> customizers;

	private final SpringCloudHazelcastProperties hazelcastProperties;

	private final ConsulDiscoveryProperties consulDiscoveryProperties;

	private boolean discoverNodesInvoked = false;

	public ConsulHazelcastDiscoveryStrategy(final DiscoveryNode discoveryNode,
			final ILogger logger, final Map<String, Comparable> properties,
			final ConsulServiceRegistry registry,
			final SpringCloudHazelcastProperties hazelcastProperties,
			final ConsulDiscoveryProperties consulDiscoveryProperties,
			final ConsulDiscoveryClient client,
			final List<HazelcastNewServiceCustomization> customizers) {
		super(logger, properties);
		this.discoveryNode = discoveryNode;
		this.registry = registry;
		this.hazelcastProperties = hazelcastProperties;
		this.consulDiscoveryProperties = consulDiscoveryProperties;
		this.client = client;
		this.customizers = customizers;
	}

	@Override
	public void start() {
		super.start();

		this.hazelcastRegistration = createRegistration();

		this.registry.register(this.hazelcastRegistration);

		Thread shutdownThread = new Thread(this::shutdownHook);
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	@NonNull
	private ConsulRegistration createRegistration() {
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

		customizers.forEach(customizer -> customizer.customize(hz, discoveryNode));

		return new ConsulRegistration(hz, consulDiscoveryProperties);
	}

	@Override
	public Iterable<DiscoveryNode> discoverNodes() {

		List<DiscoveryNode> toReturn = new ArrayList<>();

		try {
			// // discover healthy nodes only? (and its NOT the first invocation...)
			// if (this.consulHealthyOnly && discoverNodesInvoked) {
			//
			// List<ServiceHealth> nodes =
			// consulHealthClient.getHealthyServiceInstances(consulServiceName,
			// ConsulUtility.getAclToken(this.consulAclToken)).getResponse();
			//
			// for (ServiceHealth node : nodes) {
			// toReturn.add(new SimpleDiscoveryNode(
			// new Address(node.getService().getAddress(),node.getService().getPort())));
			// getLogger().info("Discovered healthy node: " +
			// node.getService().getAddress()+":"+node.getService().getPort());
			// }
			//
			// // discover all services, regardless of health or this is the first
			// invocation
			// } else {

			final String serviceName = hazelcastRegistration.getServiceId();
			List<ServiceInstance> response = this.client.getInstances(serviceName);

			for (final ServiceInstance service : response) {

				String discoveredAddress = null;
				String rawServiceAddress = service.getHost();
				String rawAddress = service.getUri().toString();

				if (rawServiceAddress != null && !rawServiceAddress.trim().isEmpty()) {
					discoveredAddress = rawServiceAddress;

				}
				else if (rawAddress != null && !rawAddress.trim().isEmpty()) {
					getLogger().warning("discoverNodes() ServiceAddress was null/blank! "
							+ "for service: " + serviceName
							+ " falling back to Address value");
					discoveredAddress = rawAddress;

				}
				else {
					getLogger().warning("discoverNodes() could not discover an address, "
							+ "both ServiceAddress and Address were null/blank! "
							+ "for service: " + serviceName);
				}

				toReturn.add(new SimpleDiscoveryNode(
						new Address(discoveredAddress, service.getPort())));
				getLogger().info("Discovered healthy node: " + discoveredAddress + ":"
						+ service.getPort());
			}
			// }

		}
		catch (Exception e) {
			getLogger().severe("discoverNodes() unexpected error: " + e.getMessage(), e);
		}

		// flag we were invoked
		discoverNodesInvoked = true;

		return toReturn;
	}

	@Override
	public void destroy() {
		super.destroy();
		shutdownHook();
	}

	private void shutdownHook() {

		try {
			getLogger().info("Deregistering myself from Consul: "
					+ this.hazelcastRegistration.getServiceId());
			this.registry.deregister(this.hazelcastRegistration);
		}
		catch (Throwable e) {
			this.getLogger().severe("Unexpected error in ConsulRegistrator.deregister(): "
					+ e.getMessage(), e);
		}
	}

}
