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

import com.hazelcast.spi.discovery.DiscoveryNode;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * @author Semyon Danilov
 */
@FunctionalInterface
public interface HazelcastServiceCustomization<T extends Registration> {

	void customize(T service, DiscoveryNode discoveryNode);

}
