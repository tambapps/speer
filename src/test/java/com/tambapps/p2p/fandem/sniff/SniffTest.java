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
      ADDRESS_2 = InetAddress.getByName("127.0.0.2");
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
    SniffHandler handler = new SniffHandler(Peer.of(ADDRESS_1, 8081), handshake,
        new LastOctetSniffingStrategy(ADDRESS_2, 8081),
        (p) -> {
          System.out.println("HANDLER1: found peer " + p);
          return false;
        });
    executor.submit(() -> {
      try {
        handler.start();
      } catch (Exception e) {
        System.err.println("HANDLER1: an error occurred");
        e.printStackTrace();
      }
      return null;
    });

    Thread.sleep(250L);

    SniffHandler handler2 = new SniffHandler(Peer.of(ADDRESS_2, 8081), handshake,
        new LastOctetSniffingStrategy(ADDRESS_1, 8081),
        (p) -> {
          System.out.println("HANDLER2: found peer " + p);
          return false;
        });
    executor.submit(() -> {
      try {
        handler2.start();
      } catch (Exception e) {
        System.err.println("HANDLER2: an error occurred");
        e.printStackTrace();
      }
      return null;
    });

    executor.take().get();
    executor.take().get();
  }
}
