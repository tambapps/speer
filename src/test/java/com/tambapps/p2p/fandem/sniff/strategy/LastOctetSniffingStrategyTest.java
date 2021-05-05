package com.tambapps.p2p.fandem.sniff.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.fandem.Peer;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class LastOctetSniffingStrategyTest {

  @Test
  public void test() {
    int port = 23;
    InetAddress address = InetAddress.getLoopbackAddress();
    LastOctetSniffingStrategy strategy = new LastOctetSniffingStrategy(address, port);

    List<Peer> peers = new ArrayList<>();
    for (Peer peer : strategy) {
      assertEquals(port, peer.getPort());
      peers.add(peer);
      System.out.println(peer);
    }
    // this strategy should return unique peers
    assertEquals(peers.stream().distinct().count(), peers.size());

    // when resetting the result returned should be the same
    strategy.reset();

    List<Peer> peers2 = new ArrayList<>();
    for (Peer peer : strategy) {
      peers2.add(peer);
    }
    assertEquals(peers, peers2);
  }

  @Test
  public void testStartingFromMiddle() {
    int port = 23;
    InetAddress address = InetAddress.getLoopbackAddress();
    LastOctetSniffingStrategy strategy = new LastOctetSniffingStrategy(address, port, (byte) 23);

    for (Peer peer : strategy) {
      System.out.println(peer);
    }
  }
}
