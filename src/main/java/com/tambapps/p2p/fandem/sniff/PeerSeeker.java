package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.PeerConnection;
import com.tambapps.p2p.fandem.sniff.strategy.SniffingStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class PeerSeeker {

  interface SeekListener {
    // TODO add more event?

    /**
     * Callback invoked when a peer has been seeked.
     *
     * @param peer the seeked peer
     *
     */
    void onPeerFound(Peer peer);
  }

  private final SniffingStrategy sniffingStrategy;
  private final PeerSeeking seeking;
  private final SeekListener listener;
  // peers not to sniff
  @Getter
  private final List<Peer> filteredPeers = new ArrayList<>();

  public Optional<Peer> seekFirst(int howManyTimes) {
    for (int i = 0; i < howManyTimes; i++) {
      Optional<Peer> peer = seekFirst();
      if (peer.isPresent()) {
        return peer;
      }
    }
    return Optional.empty();
  }

  public Optional<Peer> seekFirst() {
    sniffingStrategy.reset();
    for (Peer peer : sniffingStrategy) {
      Peer seekedPeer = doSniff(peer);
      if (seekedPeer != null) {
        return Optional.of(seekedPeer);
      }
    }
    return Optional.empty();
  }

  public Set<Peer> seek(int howManyTimes) {
    Set<Peer> seekedPeers = new HashSet<>();
    for (int i = 0; i < howManyTimes; i++) {
      seekedPeers.addAll(seek());
    }
    return seekedPeers;
  }

  public Set<Peer> seek() {
    sniffingStrategy.reset();
    Set<Peer> seekedPeers = new HashSet<>();
    for (Peer peer : sniffingStrategy) {
      Peer seekedPeer = doSniff(peer);
      if (seekedPeer != null) {
        seekedPeers.add(seekedPeer);
      }
    }
    return seekedPeers;
  }

  public void addFilteredPeer(Peer peer) {
    filteredPeers.add(peer);
  }

  private Peer doSniff(Peer sniffPeer) {
    if (filteredPeers.contains(sniffPeer)) {
      return null;
    }
    LOGGER.trace("Will seek {}", sniffPeer);
    try (PeerConnection connection = PeerConnection.from(sniffPeer)) {
      Peer peer = seeking.read(connection.getInputStream());
      LOGGER.debug("Found peer {} while sniffing", peer);
      listener.onPeerFound(peer);
      return peer;
    } catch (IOException e) {
      // connection or handshake failed
      if (!(e instanceof ConnectException)) {
        LOGGER.debug("Couldn't connect to {}", sniffPeer, e);
      }
    }
    return null;
  }
}
