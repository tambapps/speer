package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.seek.strategy.SeekingStrategy;
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

  public Optional<Peer> seekFirst(SeekingStrategy seekingStrategy, int howManyTimes) {
    for (int i = 0; i < howManyTimes; i++) {
      Optional<Peer> peer = seekFirst(seekingStrategy);
      if (peer.isPresent()) {
        return peer;
      }
    }
    return Optional.empty();
  }

  public Optional<Peer> seekFirst(SeekingStrategy seekingStrategy) {
    seekingStrategy.reset();
    for (Peer peer : seekingStrategy) {
      List<Peer> seekedPeers = seek(peer);
      if (!seekedPeers.isEmpty()) {
        return Optional.of(seekedPeers.get(0));
      }
    }
    return Optional.empty();
  }

  public Set<Peer> seek(SeekingStrategy seekingStrategy, int howManyTimes) {
    Set<Peer> seekedPeers = new HashSet<>();
    for (int i = 0; i < howManyTimes; i++) {
      seekedPeers.addAll(seek(seekingStrategy));
    }
    return seekedPeers;
  }

  public Set<Peer> seek(SeekingStrategy seekingStrategy) {
    seekingStrategy.reset();
    Set<Peer> seekedPeers = new HashSet<>();
    for (Peer peer : seekingStrategy) {
      seekedPeers.addAll(seek(peer));
    }
    return seekedPeers;
  }

  public List<Future<List<Peer>>> seek(SeekingStrategy seekingStrategy, ExecutorService executor) {
    seekingStrategy.reset();
    List<Future<List<Peer>>> futures = new ArrayList<>();
    for (Peer peer : seekingStrategy) {
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
