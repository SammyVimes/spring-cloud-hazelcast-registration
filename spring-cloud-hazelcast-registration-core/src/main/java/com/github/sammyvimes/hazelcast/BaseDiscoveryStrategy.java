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

import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

/**
 * @author Semyon Danilov
 */
public abstract class BaseDiscoveryStrategy<T extends Registration> extends AbstractDiscoveryStrategy {

	protected final DiscoveryNode discoveryNode;

	protected final SpringCloudHazelcastProperties hazelcastProperties;

	private final ServiceRegistry<T> registry;

	private final DiscoveryClient discoveryClient;

	private T hazelcastRegistration;

	private boolean discoverNodesInvoked = false;

	public BaseDiscoveryStrategy(final ILogger logger, final Map<String, Comparable> properties, ServiceRegistry<T> registry, DiscoveryClient discoveryClient, final DiscoveryNode discoveryNode, final SpringCloudHazelcastProperties hazelcastProperties) {
		super(logger, properties);
		this.registry = registry;
		this.discoveryClient = discoveryClient;
		this.discoveryNode = discoveryNode;
		this.hazelcastProperties = hazelcastProperties;
	}

	abstract T createRegistration();

	@Override
	public void start() {
		super.start();

		this.hazelcastRegistration = createRegistration();

		this.registry.register(this.hazelcastRegistration);

		Thread shutdownThread = new Thread(this::shutdownHook);
		Runtime.getRuntime().addShutdownHook(shutdownThread);
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
			List<ServiceInstance> response = this.discoveryClient.getInstances(serviceName);

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
			getLogger().info("Deregister from service registry: "
				+ this.hazelcastRegistration.getServiceId());
			this.registry.deregister(this.hazelcastRegistration);
		}
		catch (Throwable e) {
			this.getLogger().severe("Unexpected error during service deregister: "
				+ e.getMessage(), e);
		}
	}

}
