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

package com.github.sammyvimes.app2;

import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.github.sammyvimes.hazelcast.ConsulHazelcastDiscoveryStrategyFactory;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hazelcast.spi.properties.GroupProperty.DISCOVERY_SPI_ENABLED;
import static com.hazelcast.spi.properties.GroupProperty.HTTP_HEALTHCHECK_ENABLED;

@RestController
@SpringBootApplication
@PropertySource("classpath:application-app2.properties")
public class Application2 {

	public static void main(String[] args) {
		SpringApplication.run(Application2.class, args);
	}


	@Autowired
	@Lazy
	private HazelcastInstance instance;

	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getValue() {
		final IMap<String, String> map = instance.getMap("some-map");
		return map.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining("\n"));
	}

	@Bean
	public HazelcastInstance instance(final ConsulHazelcastDiscoveryStrategyFactory factory) {
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

		return Hazelcast.newHazelcastInstance(config);
	}

}
