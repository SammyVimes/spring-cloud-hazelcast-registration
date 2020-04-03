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
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Semyon Danilov
 */
@Configuration
@EnableConfigurationProperties(SpringCloudHazelcastProperties.class)
public class ZookeeperHazelcastConfig {

	@Bean
	@Autowired
	public ZookeeperHazelcastDiscoveryStrategyFactory zookeeperRegistrationFactory(final ZookeeperServiceRegistry registry,
		final ZookeeperDiscoveryClient client,
		final SpringCloudHazelcastProperties hazelcastProperties,
		final ZookeeperDiscoveryProperties discoveryProperties,
		final List<HazelcastServiceCustomization<ZookeeperRegistration>> customizers) {
		return new ZookeeperHazelcastDiscoveryStrategyFactory(registry,
			discoveryProperties, hazelcastProperties, client, customizers);
	}

}
