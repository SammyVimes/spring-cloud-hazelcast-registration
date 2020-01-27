package com.github.sammyvimes;

import com.github.sammyvimes.hazelcast.ConsulHazelcastDiscoveryStrategyFactory;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulHazelcastConfigTest.ApplicationTestConfig.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class ConsulHazelcastConfigTest {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Test
    public void foo() {
        System.out.println(hazelcastInstance);
        Assert.assertTrue(true);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    public static class ApplicationTestConfig {

        @Bean
        public HazelcastInstance newInstance(final ConsulHazelcastDiscoveryStrategyFactory factory) {
            Config config = new Config();
            final NetworkConfig networkConfig = config.getNetworkConfig();
            final JoinConfig join = networkConfig.getJoin();
            final DiscoveryConfig discoveryConfig = join.getDiscoveryConfig();

            final DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(factory, new HashMap<>());
            discoveryConfig.setDiscoveryStrategyConfigs(Collections.singletonList(discoveryStrategyConfig));

            return Hazelcast.newHazelcastInstance(config);
        }

    }

}
