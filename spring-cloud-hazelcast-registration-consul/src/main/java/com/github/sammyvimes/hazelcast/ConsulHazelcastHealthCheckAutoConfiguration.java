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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.agent.model.NewService;
import com.github.sammyvimes.hazelcast.SpringCloudHazelcastProperties.ConsulProperties.HealthCheckProperties;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.util.StringUtil;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @author Semyon Danilov
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.hazelcast.consul.healthcheck", name = "enabled", havingValue = "true")
public class ConsulHazelcastHealthCheckAutoConfiguration {

	@Bean
	public HazelcastServiceCustomization<ConsulRegistration> consulHazelcastHealthCheckCustomizer(
			final SpringCloudHazelcastProperties properties) {
		return (service, discoveryNode) -> {
			final NewService newService = service.getService();
			List<NewService.Check> checks = newService.getChecks() != null
					? new ArrayList<>(newService.getChecks()) : new ArrayList<>();

			checks.add(healthCheck(discoveryNode, properties));
			newService.setChecks(checks);
		};
	}

	private NewService.Check healthCheck(final DiscoveryNode discoveryNode,
			final SpringCloudHazelcastProperties properties) {
		final HealthCheckProperties healthCheckProperties = properties.getConsul().getHealthcheck();

		final String host;
		final int port;

		final Address publicAddress = discoveryNode.getPublicAddress();

		if (StringUtil.isNullOrEmpty(healthCheckProperties.getHost())) {
			host = publicAddress.getHost();
		}
		else {
			host = healthCheckProperties.getHost();
		}

		if (healthCheckProperties.getPort() == null) {
			port = publicAddress.getPort();
		}
		else {
			port = healthCheckProperties.getPort();
		}

		NewService.Check check = new NewService.Check();
		if (healthCheckProperties.isDeregisterEnabled()) {
			check.setDeregisterCriticalServiceAfter(healthCheckProperties.getDeregisterTimeout());
		}

		Assert.isTrue(port > 0, "createCheck port must be greater than 0");

		check.setHttp(String.format("http://%s:%d/hazelcast/health", host, port));

		Map<String, List<String>> headers = new HashMap<>();
		check.setHeader(headers);
		check.setInterval(healthCheckProperties.getInterval());
		check.setTimeout(healthCheckProperties.getTimeout());
		check.setTlsSkipVerify(true);
		return check;
	}

}
