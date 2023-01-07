package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.util.DangerousConsumer;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractConnectionTest {

  private final ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(
      Executors.newFixedThreadPool(2)
  );

  @SneakyThrows
  public void runTest(DangerousConsumer<PeerConnection> serverConsumer,
      DangerousConsumer<PeerConnection> clientConsumer) {
    Peer peer = Peer.of("127.0.0.1", 8081);
    System.out.println("Peer " + peer);
    try (PeerServer server = new PeerServer(peer)) {
      executor.submit(() -> {
        try (PeerConnection connection = server.accept()) {
          System.out.println("Server: found connection " + connection);
          serverConsumer.accept(connection);
        } catch (Exception e) {
          System.out.println("ERROR: " + e.getMessage());
          e.printStackTrace();
        }
        return null;
      });

      // leave a little time for the server to start
      Thread.sleep(250);

      try (PeerConnection connection = PeerConnection.from(peer)) {
        System.out.println("Client: found connection " + connection);
        clientConsumer.accept(connection);
      }

      executor.poll(4, TimeUnit.SECONDS);
    }
  }
}
