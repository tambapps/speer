package com.tambapps.p2p.speer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.greet.PeerGreeter;
import com.tambapps.p2p.speer.greet.PeerGreetings;
import com.tambapps.p2p.speer.seek.PeerSeeker;
import com.tambapps.p2p.speer.seek.PeerSeeking;
import com.tambapps.p2p.speer.seek.strategy.LastOctetSeekingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class PeerSeekGreetTest {
  private static final Peer PEER1 = Peer.of("127.0.0.2", 8081);
  private static final Peer PEER2 = Peer.of("127.0.0.2", 8082);

  private final PeerGreetings greetings = (peers, outputStream) -> outputStream.writeUTF(
      peers.stream().map(Peer::toString).collect(Collectors.joining("\n"))
  );
  private final PeerSeeking seeking = inputStream ->
      Arrays.stream(inputStream.readUTF().split("\\n"))
  .map(Peer::parse).collect(Collectors.toList());

  private final ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(
      Executors.newFixedThreadPool(2)
  );

  @Test
  public void test() throws Exception {
    PeerGreeter greeter = new PeerGreeter(Arrays.asList(PEER1, PEER2), greetings);
    Peer peer = Peer.of("127.0.0.1", 8081);
    executor.submit(() -> {
      try (ServerSocket serverSocket = new ServerSocket(peer.getPort(), 10, peer.getIp())) {
        greeter.greetOne(serverSocket);
      }
      return null;
    });
    PeerSeeker seeker = new PeerSeeker(seeking, (peers) -> LOGGER.info("Found peers {}", peers));

    Set<Peer> peers = seeker
        .seek(new LastOctetSeekingStrategy(InetAddress.getByName("127.0.0.0"), peer.getPort()));

    assertEquals(new HashSet<>(Arrays.asList(PEER1, PEER2)), peers);
  }
}
