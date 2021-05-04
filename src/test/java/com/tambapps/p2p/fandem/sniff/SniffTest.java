package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class SniffTest {

  private final ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(
      Executors.newFixedThreadPool(2)
  );

  private final SniffHandshake handshake = null;

  @Test
  public void test() throws Exception {
    /*
    Peer peer = Peer.findAvailablePeer();
    System.out.println("Peer " + peer);
    SniffHandler handler = new SniffHandler(peer, handshake, null, handshake, listener);
    executor.submit(() -> {
      handler.start();
      return null;
    });

    Thread.sleep(250L);
    PeerSniffer sniffer = new PeerSniffer();
*/
  }
}
