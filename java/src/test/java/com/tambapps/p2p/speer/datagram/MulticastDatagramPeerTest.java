package com.tambapps.p2p.speer.datagram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.util.DangerousConsumer;
import com.tambapps.p2p.speer.util.PeerUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MulticastDatagramPeerTest {

  private static final InetAddress ADDRESS = PeerUtils.getAddress("127.0.0.2");
  private static final InetAddress MULTICAST_ADDRESS = PeerUtils.getAddress("230.0.0.0");
  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
  private Future<?> future;

  @AfterEach
  public void dispose() {
    if (future != null) {
      future.cancel(true);
    }
  }

  @SneakyThrows
  private MulticastDatagramPeer runInBackground(DangerousConsumer<MulticastDatagramPeer> consumer) {
    MulticastDatagramPeer datagramPeer = new MulticastDatagramPeer();
    this.future = EXECUTOR.submit(() -> {
      while (!Thread.interrupted()) {
        try {
          consumer.accept(datagramPeer);
          Thread.sleep(1000L);
        } catch (IOException | InterruptedException e) {
          break;
        }
      }
    });
    return datagramPeer;
  }

  @Test
  public void testCommunication() throws IOException {
    try (MulticastDatagramPeer datagramPeer = runInBackground(communicator -> communicator.send("Hello World", ADDRESS, 5000));
        MulticastDatagramPeer client = new MulticastDatagramPeer(Peer.of(ADDRESS, 5000))) {
      assertEquals("Hello World", client.receiveString());
    }
  }

  @Test
  public void testMulticast() throws IOException {
    try (MulticastDatagramPeer datagramPeer = runInBackground(communicator -> communicator.send("Hello World", MULTICAST_ADDRESS, 5000));
        MulticastDatagramPeer client = new MulticastDatagramPeer(5000)) {
      client.joinGroup(MULTICAST_ADDRESS);
      assertEquals("Hello World", client.receiveString());
    }
  }

  @AfterAll
  public static void disposeExecutor() {
    EXECUTOR.shutdownNow();
  }
}
