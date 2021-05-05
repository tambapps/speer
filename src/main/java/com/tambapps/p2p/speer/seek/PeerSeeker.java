package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.seek.strategy.SniffingStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@AllArgsConstructor
public class PeerSeeker {

  interface SeekListener {
    // TODO add more event?

    /**
     * Callback invoked when peers has been seeked.
     *
     * @param peers the seeked peers
     *
     */
    void onPeersFound(List<Peer> peers);
  }

  private final PeerSeeking seeking;
  private final SeekListener listener;
  // peers not to sniff
  @Getter
  private final List<Peer> filteredPeers = new ArrayList<>();

  public Optional<Peer> seekFirst(SniffingStrategy sniffingStrategy, int howManyTimes) {
    for (int i = 0; i < howManyTimes; i++) {
      Optional<Peer> peer = seekFirst(sniffingStrategy);
      if (peer.isPresent()) {
        return peer;
      }
    }
    return Optional.empty();
  }

  public Optional<Peer> seekFirst(SniffingStrategy sniffingStrategy) {
    sniffingStrategy.reset();
    for (Peer peer : sniffingStrategy) {
      List<Peer> seekedPeers = seek(peer);
      if (!seekedPeers.isEmpty()) {
        return Optional.of(seekedPeers.get(0));
      }
    }
    return Optional.empty();
  }

  public Set<Peer> seek(SniffingStrategy sniffingStrategy, int howManyTimes) {
    Set<Peer> seekedPeers = new HashSet<>();
    for (int i = 0; i < howManyTimes; i++) {
      seekedPeers.addAll(seek(sniffingStrategy));
    }
    return seekedPeers;
  }

  public Set<Peer> seek(SniffingStrategy sniffingStrategy) {
    sniffingStrategy.reset();
    Set<Peer> seekedPeers = new HashSet<>();
    for (Peer peer : sniffingStrategy) {
      seekedPeers.addAll(seek(peer));
    }
    return seekedPeers;
  }

  public List<Future<List<Peer>>> seek(SniffingStrategy sniffingStrategy, ExecutorService executor) {
    sniffingStrategy.reset();
    List<Future<List<Peer>>> futures = new ArrayList<>();
    for (Peer peer : sniffingStrategy) {
      futures.add(executor.submit(() -> seek(peer)));
    }
    return futures;
  }

  public List<Peer> seek(Peer sniffPeer) {
    if (filteredPeers.contains(sniffPeer)) {
      return Collections.emptyList();
    }
    LOGGER.trace("Will seek {}", sniffPeer);
    try (PeerConnection connection = PeerConnection.from(sniffPeer)) {
      List<Peer> peers = seeking.read(connection.getInputStream());
      LOGGER.debug("Found peers {}", peers);
      if (listener != null) {
        listener.onPeersFound(peers);
      }
      return peers;
    } catch (IOException e) {
      // connection or handshake failed
      if (!(e instanceof ConnectException)) {
        LOGGER.debug("Couldn't connect to {}", sniffPeer, e);
      }
    }
    return Collections.emptyList();
  }

  public void addFilteredPeer(Peer peer) {
    filteredPeers.add(peer);
  }

}
