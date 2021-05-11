package com.tambapps.p2p.speer.datagram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.util.DangerousConsumer;
import com.tambapps.p2p.speer.util.PeerUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MulticastDatagramPeerTest {

  private static final InetAddress ADDRESS1 = PeerUtils.getAddress("127.0.0.1");
  private static final InetAddress ADDRESS2 = PeerUtils.getAddress("127.0.0.2");
  private static final InetAddress MULTICAST_ADDRESS = PeerUtils.getAddress("230.0.0.0");
  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
  private static MulticastDatagramPeer datagramPeer;
  private Future<?> future;

  @BeforeAll
  public static void initCommunicator() throws IOException {
    datagramPeer = new MulticastDatagramPeer(ADDRESS1, 5000);
    datagramPeer.getSocket().setBroadcast(true);
  }

  @AfterEach
  public void dispose() {
    if (future != null) {
      future.cancel(true);
    }
  }
  private void runInBackground(DangerousConsumer<MulticastDatagramPeer> consumer) {
    this.future = EXECUTOR.submit(() -> {
      while (!Thread.interrupted()) {
        try {
          consumer.accept(datagramPeer);
          Thread.sleep(1000L);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          break;
        }
      }
    });
  }
  @Test
  public void testCommunication() throws IOException {
    runInBackground(communicator -> communicator.send("Hello World", ADDRESS2, 5000));
    MulticastDatagramPeer client = new MulticastDatagramPeer(Peer.of(ADDRESS2, 5000));
    assertEquals("Hello World", client.receiveString(1024));
  }

  @Disabled("Can't be tested with only one device (?)")
  @Test
  public void testMulticast() throws IOException {
    runInBackground(communicator -> communicator.send("Hello World", MULTICAST_ADDRESS, 5000));
    MulticastDatagramPeer client = new MulticastDatagramPeer(5000);
    client.joinGroup(MULTICAST_ADDRESS);
    assertEquals("Hello World", client.receiveString(1024));
  }

  @AfterAll
  public static void disposeExecutor() {
    EXECUTOR.shutdownNow();
  }
}
