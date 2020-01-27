package com.github.sammyvimes.hazelcast;

import com.ecwid.consul.v1.agent.model.NewService;
import com.github.sammyvimes.HazelcastNewServiceCustomization;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.util.StringUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import com.github.sammyvimes.hazelcast.SpringCloudHazelcastProperties.ConsulProperties.HealthCheckProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Configuration
@ConditionalOnProperty("spring.cloud.hazelcast.consul.healthcheck.enabled")
public class ConsulHazelcastHealthCheckAutoConfiguration {

    @Bean
    public HazelcastNewServiceCustomization consulHazelcastHealthCheckCustomizer(final SpringCloudHazelcastProperties properties) {
        return (newService, discoveryNode) -> {
            List<NewService.Check> checks = newService.getChecks() != null ? new ArrayList<>(newService.getChecks()) : new ArrayList<>();

            checks.add(healthCheck(discoveryNode, properties));
            newService.setChecks(checks);
        };
    }

    private NewService.Check healthCheck(final DiscoveryNode discoveryNode, final SpringCloudHazelcastProperties properties) {
        final HealthCheckProperties healthCheck = properties.getConsul().getHealthcheck();

        final String host;
        final int port;

        final Address publicAddress = discoveryNode.getPublicAddress();

        if (StringUtil.isNullOrEmpty(healthCheck.getHost())) {
            host = publicAddress.getHost();
        } else {
            host = healthCheck.getHost();
        }

        if (healthCheck.getPort() == null) {
            port = publicAddress.getPort();
        } else {
            port = healthCheck.getPort();
        }

        NewService.Check check = new NewService.Check();
        String healthCheckCriticalTimeout = "2m";
        if (StringUtils.hasText(healthCheckCriticalTimeout)) {
            check.setDeregisterCriticalServiceAfter(
                    healthCheckCriticalTimeout);
        }

        Assert.isTrue(port > 0, "createCheck port must be greater than 0");

        check.setHttp(String.format("http://%s:%d/hazelcast/health", host, port));


        Map<String, List<String>> headers = new HashMap<>();
        final String healthCheckInterval = "10s";
        final String healthCheckTimeout = "30s";
        check.setHeader(headers);
        check.setInterval(healthCheckInterval);
        check.setTimeout(healthCheckTimeout);
        check.setTlsSkipVerify(true);
        return check;
    }

}
