package com.tambapps.p2p.speer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.greet.PeerGreeter;
import com.tambapps.p2p.speer.greet.PeerGreetings;
import com.tambapps.p2p.speer.seek.PeerSeeker;
import com.tambapps.p2p.speer.seek.PeerSeeking;
import com.tambapps.p2p.speer.seek.SeekedPeerSupplier;
import com.tambapps.p2p.speer.seek.strategy.LastOctetSeekingStrategy;
import com.tambapps.p2p.speer.seek.strategy.SeekingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class PeerSeekGreetTest {
  private static final Peer PEER1 = Peer.of("127.0.0.2", 8081);
  private static final Peer PEER2 = Peer.of("127.0.0.2", 8082);

  private static final int GREETER_PORT = 8081;
  private static final PeerGreetings GREETINGS = (peers, outputStream) -> outputStream.writeUTF(
      peers.stream().map(Peer::toString).collect(Collectors.joining("\n"))
  );
  private static final SeekingStrategy STRATEGY =
      new LastOctetSeekingStrategy("127.0.0.0", GREETER_PORT);
  private static final PeerSeeking<Peer> SEEKING = inputStream ->
      Arrays.stream(inputStream.readUTF().split("\\n"))
          .map(Peer::parse).collect(Collectors.toList());

  private static final ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(
      Executors.newFixedThreadPool(2)
  );

  private static final PeerGreeter GREETER = new PeerGreeter(Arrays.asList(PEER1, PEER2), GREETINGS);
  private static ServerPeer serverPeer;

  @BeforeAll
  public static void initGreeter() throws Exception {
    Peer peer = Peer.of("127.0.0.1", GREETER_PORT);
    serverPeer = new ServerPeer(peer);
    executor.submit(() -> {
      try {
        LOGGER.info("Starting greeting at {}", peer);
        GREETER.greet(serverPeer);
      } catch (SocketException e) {
        LOGGER.info("Server socket was closed, stopped greeting.", e);
      }
      return null;
    });
    Thread.sleep(1000L);
  }

  @AfterAll
  public static void disposeGreeter() throws IOException {
    serverPeer.close();
  }

  @Test
  public void test() {
    PeerSeeker seeker = new PeerSeeker(SEEKING, new PeerSeeker.SeekListener<Peer>() {
      @Override
      public void onPeersFound(List<Peer> peers) {
        LOGGER.info("Found peers {}", peers);
      }

      @Override
      public void onException(IOException e) {

      }
    });

    Set<Peer> peers = seeker.seek(STRATEGY);

    assertEquals(new HashSet<>(Arrays.asList(PEER1, PEER2)), peers);
  }

  @Test
  public void testSupplier() throws Exception {
    SeekedPeerSupplier supplier = new SeekedPeerSupplier(STRATEGY, SEEKING);

    Peer peer1 = supplier.get();
    Peer peer2 = supplier.get();

    assertEquals(Arrays.asList(PEER1, PEER2), Arrays.asList(peer1, peer2));
  }

  @Test
  public void testSupplierAsync() throws Exception {
    ExecutorService seekExecutor = Executors.newFixedThreadPool(4);
    SeekedPeerSupplier supplier = new SeekedPeerSupplier(seekExecutor, STRATEGY, SEEKING);

    Peer peer1 = supplier.get();
    Peer peer2 = supplier.get();

    assertEquals(Arrays.asList(PEER1, PEER2), Arrays.asList(peer1, peer2));

    seekExecutor.shutdownNow();
  }
}
