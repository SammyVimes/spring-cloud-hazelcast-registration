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

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Semyon Danilov
 */
@ConfigurationProperties(prefix = "spring.cloud.hazelcast")
public class SpringCloudHazelcastProperties {

	/**
	 * Advertised Hazelcast host. Defaults to Hazelcast node's public address.
	 */
	private String host;

	/**
	 * Advertised Hazelcast port. Defaults to Hazelcast node's port.
	 */
	private Integer port;

	/**
	 * Consul service configuration.
	 */
	private ConsulProperties consul = new ConsulProperties();

	/**
	 * Zookeeper service configuration.
	 */
	private ZookeeperProperties zookeeper = new ZookeeperProperties();

	public ConsulProperties getConsul() {
		return consul;
	}

	public ZookeeperProperties getZookeeper() {
		return zookeeper;
	}

	public void setConsul(final ConsulProperties consul) {
		this.consul = consul;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(final Integer port) {
		this.port = port;
	}

	public static class ConsulProperties {

		/**
		 * Consul healthcheck configuration for Hazelcast service.
		 */
		private HealthCheckProperties healthcheck = new HealthCheckProperties();

		/**
		 * Hazelcast service name in Consul.
		 */
		private String serviceName;

		/**
		 * Hazelcast service tags in Consul.
		 */
		private List<String> tags = Collections.emptyList();

		public List<String> getTags() {
			return tags;
		}

		public void setTags(final List<String> tags) {
			this.tags = tags;
		}

		public String getServiceName() {
			return serviceName;
		}

		public void setServiceName(final String serviceName) {
			this.serviceName = serviceName;
		}

		public HealthCheckProperties getHealthcheck() {
			return healthcheck;
		}

		public void setHealthcheck(final HealthCheckProperties healthcheck) {
			this.healthcheck = healthcheck;
		}

		public static class HealthCheckProperties {

			/**
			 * If health check is enabled. Defaults to false.
			 */
			private boolean enabled = false;

			/**
			 * Address for health checks of Hazelcast service. Defaults to Hazelcast
			 * node's public address.
			 */
			private String host;

			/**
			 * Port for health checks of Hazelcast service. Defaults to Hazelcast node's
			 * port.
			 */
			private Integer port;

			/**
			 * Automatic deregistration flag.
			 */
			private boolean deregisterEnabled = true;

			/**
			 * Automatic deregistration timeout.
			 */
			private String deregisterTimeout = "2m";

			/**
			 * Health check interval.
			 */
			private String interval = "10s";

			/**
			 * Health check timeout.
			 */
			private String timeout = "30s";

			public boolean isEnabled() {
				return enabled;
			}

			public void setEnabled(final boolean enabled) {
				this.enabled = enabled;
			}

			public String getHost() {
				return host;
			}

			public void setHost(final String host) {
				this.host = host;
			}

			public Integer getPort() {
				return port;
			}

			public void setPort(final Integer port) {
				this.port = port;
			}

			public boolean isDeregisterEnabled() {
				return deregisterEnabled;
			}

			public void setDeregisterEnabled(final boolean deregisterEnabled) {
				this.deregisterEnabled = deregisterEnabled;
			}

			public String getDeregisterTimeout() {
				return deregisterTimeout;
			}

			public void setDeregisterTimeout(final String deregisterTimeout) {
				this.deregisterTimeout = deregisterTimeout;
			}

			public String getInterval() {
				return interval;
			}

			public void setInterval(final String interval) {
				this.interval = interval;
			}

			public String getTimeout() {
				return timeout;
			}

			public void setTimeout(final String timeout) {
				this.timeout = timeout;
			}
		}

	}

	public static class ZookeeperProperties {

		/**
		 * Hazelcast service name in Consul.
		 */
		private String serviceName;

		String getServiceName() {
			return serviceName;
		}

		void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}

	}

}
