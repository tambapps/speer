package com.tambapps.p2p.speer.datagram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.util.DangerousConsumer;
import com.tambapps.p2p.speer.util.PeerUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DatagramPeerTest {

  private static final InetAddress ADDRESS1 = PeerUtils.getAddress("127.0.0.1");
  private static final InetAddress ADDRESS2 = PeerUtils.getAddress("127.0.0.2");
  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
  private static DatagramPeer datagramPeer;
  private Future<?> future;

  @BeforeAll
  public static void initCommunicator() throws SocketException {
    datagramPeer = new DatagramPeer(ADDRESS1, 5000);
    datagramPeer.getSocket().setBroadcast(true);
  }

  @AfterEach
  public void dispose() {
    if (future != null) {
      future.cancel(true);
    }
  }
  private void runInBackground(DangerousConsumer<DatagramPeer> consumer) {
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
    DatagramPeer client = new DatagramPeer(Peer.of(ADDRESS2, 5000));
    assertEquals("Hello World", client.receiveString(1024));
  }

  @AfterAll
  public static void disposeExecutor() {
    EXECUTOR.shutdownNow();
  }
}
