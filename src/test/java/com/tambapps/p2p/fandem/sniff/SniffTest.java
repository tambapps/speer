package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake2;
import com.tambapps.p2p.fandem.sniff.strategy.LastOctetSniffingStrategy;
import com.tambapps.p2p.fandem.sniff.strategy.SniffingStrategy;
import com.tambapps.p2p.fandem.util.IPUtils;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class SniffTest {

  private static final InetAddress ADDRESS_1;
  private static final InetAddress ADDRESS_2;

  static {
    try {
      ADDRESS_1 = InetAddress.getByName("127.0.0.1");
      ADDRESS_2 = InetAddress.getByName("127.0.0.1");
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  private final ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(
      Executors.newFixedThreadPool(2)
  );

  private final SniffHandshake2 handshake = new SniffHandshake2() {
    @Override
    public Peer read(DataInputStream inputStream) throws IOException {
      return Peer.parse(inputStream.readUTF());
    }

    @Override
    public void write(Peer peer, DataOutputStream outputStream) throws IOException {
      outputStream.writeUTF(peer.toString());
    }
  };

  @Test
  public void test() throws Exception {
    Peer peer = Peer.of(ADDRESS_1, 8081);
    SniffHandler handler = new SniffHandler(peer, handshake,
        new LastOctetSniffingStrategy(ADDRESS_2, 8081),
        (p) -> System.out.println("HANDLER1: found peer " + p));
    executor.submit(() -> {
      handler.start();
      return null;
    });

    Thread.sleep(250L);

    SniffHandler handler2 = new SniffHandler(Peer.of(InetAddress.getByName("127.0.0.2"), 8081), handshake,
        new LastOctetSniffingStrategy(ADDRESS_1, 8081),
        (p) -> System.out.println("HANDLER2: found peer " + p));
    executor.submit(() -> {
      handler2.start();
      return null;
    });


    executor.take().get();
    executor.take().get();
  }
}
