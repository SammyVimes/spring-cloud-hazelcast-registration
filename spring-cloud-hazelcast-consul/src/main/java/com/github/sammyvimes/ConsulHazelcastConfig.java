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

import java.util.List;

import com.github.sammyvimes.hazelcast.ConsulHazelcastDiscoveryStrategyFactory;
import com.github.sammyvimes.hazelcast.SpringCloudHazelcastProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Semyon Danilov
 */
@Configuration
@EnableConfigurationProperties(SpringCloudHazelcastProperties.class)
public class ConsulHazelcastConfig {

	@Bean
	@Primary
	public Registration consulAppRegistration(final ConsulAutoRegistration car) {
		return car;
	}

	@Bean
	@Autowired
	public ConsulHazelcastDiscoveryStrategyFactory consul(
			final ConsulServiceRegistry registry, final ConsulDiscoveryClient client,
			final SpringCloudHazelcastProperties hazelcastProperties,
			final ConsulDiscoveryProperties consulDiscoveryProperties,
			final List<HazelcastNewServiceCustomization> customizers) {
		return new ConsulHazelcastDiscoveryStrategyFactory(registry,
				consulDiscoveryProperties, hazelcastProperties, client, customizers);
	}

}
