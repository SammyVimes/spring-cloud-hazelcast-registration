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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Semyon Danilov
 */
@Configuration
@EnableConfigurationProperties(SpringCloudHazelcastProperties.class)
public class ConsulHazelcastConfig {

	@Bean
	@Autowired
	public ConsulHazelcastDiscoveryStrategyFactory consul(
		final ConsulServiceRegistry registry, final ConsulDiscoveryClient client,
		final SpringCloudHazelcastProperties hazelcastProperties,
		final ConsulDiscoveryProperties discoveryProperties,
		final List<HazelcastServiceCustomization<ConsulRegistration>> customizers) {
		return new ConsulHazelcastDiscoveryStrategyFactory(registry,
			discoveryProperties, hazelcastProperties, client, customizers);
	}

}
