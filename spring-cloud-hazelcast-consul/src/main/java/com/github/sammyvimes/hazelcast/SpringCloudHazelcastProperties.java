package com.github.sammyvimes.hazelcast;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "spring.cloud.hazelcast")
public class SpringCloudHazelcastProperties {

    private String host;

    private Integer port;

    private ConsulProperties consul = new ConsulProperties();

    public ConsulProperties getConsul() {
        return consul;
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

        private HealthCheckProperties healthcheck = new HealthCheckProperties();

        private String serviceName;

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
            private boolean enabled;
            private String host;
            private Integer port;

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
        }

    }


}
