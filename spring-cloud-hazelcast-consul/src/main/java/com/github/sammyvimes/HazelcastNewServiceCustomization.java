package com.github.sammyvimes;

import com.ecwid.consul.v1.agent.model.NewService;
import com.hazelcast.spi.discovery.DiscoveryNode;

@FunctionalInterface
public interface HazelcastNewServiceCustomization {

    public void customize(NewService service, DiscoveryNode discoveryNode);

}
