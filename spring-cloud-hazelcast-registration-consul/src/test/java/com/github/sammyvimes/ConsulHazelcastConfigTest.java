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

package com.github.sammyvimes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sammyvimes.hazelcast.ConsulHazelcastDiscoveryStrategyFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.Address;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.hazelcast.spi.properties.GroupProperty.DISCOVERY_SPI_ENABLED;
import static com.hazelcast.spi.properties.GroupProperty.HTTP_HEALTHCHECK_ENABLED;

/**
 * @author Semyon Danilov
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = ConsulHazelcastConfigTest.CustomInitializer.class)
@SpringBootTest(classes = ConsulHazelcastConfigTest.ApplicationTestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConsulHazelcastConfigTest {

	public static final int CONSUL_PORT = 8500;

	@ClassRule
	public static GenericContainer zookeeper = new GenericContainer<>("consul")
		.withExposedPorts(CONSUL_PORT);

	@Autowired
	private List<HazelcastInstanceMgr> hazelcastInstances;

	@Test
	public void testSimpleRegistration() {
		Map<String, String> vals = new HashMap<>();
		vals.put("foo", "bar");
		vals.put("test", "value");

		final String mapName = "some-map";
		final HazelcastInstanceMgr hazelcastInstanceMgr = hazelcastInstances.get(0);
		final IMap<String, String> someMap = hazelcastInstanceMgr.getInstance()
				.getMap(mapName);

		vals.entrySet().forEach(e -> someMap.put(e.getKey(), e.getValue()));

		for (HazelcastInstanceMgr hazelcastInstance : hazelcastInstances) {
			final IMap<String, String> mapOfInstance = hazelcastInstance.getInstance()
					.getMap(mapName);

			Assert.assertEquals(vals.size(), mapOfInstance.size());

			for (Map.Entry<String, String> entry : vals.entrySet()) {
				Assert.assertEquals(entry.getValue(), mapOfInstance.get(entry.getKey()));
			}

		}

	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	public static class ApplicationTestConfig {

		@Bean
		public List<HazelcastInstanceMgr> newInstance(
				final ConsulHazelcastDiscoveryStrategyFactory factory) {
			int totalInstancesToTest = 5;

			List<HazelcastInstanceMgr> instances = new ArrayList<>();

			for (int i = 0; i < totalInstancesToTest; i++) {

				Config config = new Config();

				config.setProperty(DISCOVERY_SPI_ENABLED.getName(), "true");
				config.setProperty(HTTP_HEALTHCHECK_ENABLED.getName(), "true");

				final NetworkConfig networkConfig = config.getNetworkConfig();
				final JoinConfig join = networkConfig.getJoin();

				join.getMulticastConfig().setEnabled(false);

				final DiscoveryConfig discoveryConfig = join.getDiscoveryConfig();

				final DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(
						factory, new HashMap<>());
				discoveryConfig.setDiscoveryStrategyConfigs(
						Collections.singletonList(discoveryStrategyConfig));

				final HazelcastInstanceMgr instance = new HazelcastInstanceMgr(config);
				instance.start();
				instances.add(instance);
			}

			return instances;
		}

	}

	private static class HazelcastInstanceMgr {

		private HazelcastInstance hazelcastInstance = null;

		private Config conf = null;

		HazelcastInstanceMgr(final Config config) {
			this.conf = config;
		}

		HazelcastInstance getInstance() {
			return hazelcastInstance;
		}

		void start() {
			hazelcastInstance = Hazelcast.newHazelcastInstance(conf);
		}

		public void shutdown() {
			this.hazelcastInstance.shutdown();
		}

		public Address getAddress() {
			return this.hazelcastInstance.getCluster().getLocalMember().getAddress();
		}

	}

	static class CustomInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(
			ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of("spring.cloud.consul.port=" + zookeeper.getMappedPort(CONSUL_PORT))
				.applyTo(configurableApplicationContext.getEnvironment());
		}

	}

}
