package com.github.sammyvimes;

import com.ecwid.consul.v1.agent.model.NewService;
import com.hazelcast.spi.discovery.DiscoveryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.github.sammyvimes.hazelcast.ConsulHazelcastDiscoveryStrategyFactory;
import com.github.sammyvimes.hazelcast.SpringCloudHazelcastProperties;

import java.util.List;
import java.util.function.BiConsumer;

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
    public ConsulHazelcastDiscoveryStrategyFactory consul(final ConsulServiceRegistry registry,
                                                          final ConsulDiscoveryClient client,
                                                          final SpringCloudHazelcastProperties hazelcastProperties,
                                                          final ConsulDiscoveryProperties consulDiscoveryProperties,
                                                          final List<HazelcastNewServiceCustomization> customizers) {
        return new ConsulHazelcastDiscoveryStrategyFactory(registry, consulDiscoveryProperties, hazelcastProperties, client, customizers);
    }

}
