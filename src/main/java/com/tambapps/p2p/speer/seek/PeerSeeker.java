package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.handshake.Handshake;
import com.tambapps.p2p.speer.seek.strategy.SeekingStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
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
public class PeerSeeker<T extends Peer> {

  public interface SeekListener<T extends Peer> {
    /**
     * Callback invoked when peers has been seeked.
     *
     * @param peers the seeked peers
     */
    void onPeersFound(List<T> peers);

    /**
     * Callback invoked when an unusual error occured when trying to connect to a peer
     * (ConnectException are filtered)
     * @param e the exception
     */
    void onException(IOException e);

  }


  private final PeerSeeking<T> seeking;
  @Setter
  private SeekListener<T> listener;
  private final Handshake handshake;

  // peers not to sniff
  @Getter
  private final List<InetAddress> filteredAddresses = new ArrayList<>();

  public PeerSeeker(PeerSeeking<T> seeking) {
    this(seeking, null);
  }

  public PeerSeeker(PeerSeeking<T> seeking, SeekListener<T> listener) {
    this(seeking, listener, null);
  }

  public Optional<T> seekFirst(SeekingStrategy seekingStrategy, int howManyTimes) {
    for (int i = 0; i < howManyTimes; i++) {
      Optional<T> peer = seekFirst(seekingStrategy);
      if (peer.isPresent()) {
        return peer;
      }
    }
    return Optional.empty();
  }

  public Optional<T> seekFirst(SeekingStrategy seekingStrategy) {
    seekingStrategy.reset();
    for (Peer peer : seekingStrategy) {
      List<T> seekedPeers = seek(peer);
      if (!seekedPeers.isEmpty()) {
        return Optional.of(seekedPeers.get(0));
      }
    }
    return Optional.empty();
  }

  public Set<T> seek(SeekingStrategy seekingStrategy, int howManyTimes) {
    Set<T> seekedPeers = new HashSet<>();
    for (int i = 0; i < howManyTimes; i++) {
      seekedPeers.addAll(seek(seekingStrategy));
    }
    return seekedPeers;
  }

  public Set<T> seek(SeekingStrategy seekingStrategy) {
    seekingStrategy.reset();
    Set<T> seekedPeers = new HashSet<>();
    for (Peer peer : seekingStrategy) {
      seekedPeers.addAll(seek(peer));
    }
    return seekedPeers;
  }

  public List<Future<List<T>>> seek(SeekingStrategy seekingStrategy, ExecutorService executor) {
    seekingStrategy.reset();
    List<Future<List<T>>> futures = new ArrayList<>();
    for (Peer peer : seekingStrategy) {
      futures.add(executor.submit(() -> seek(peer)));
    }
    return futures;
  }

  public List<T> seek(Peer sniffPeer) {
    if (filteredAddresses.contains(sniffPeer.getIp())) {
      return Collections.emptyList();
    }
    LOGGER.trace("Will seek {}", sniffPeer);
    try (PeerConnection connection = PeerConnection.from(sniffPeer, handshake)) {
      List<T> peers = seeking.read(connection.getInputStream());
      LOGGER.debug("Found peers {}", peers);
      if (listener != null) {
        listener.onPeersFound(peers);
      }
      return peers;
    } catch (IOException e) {
      // connection or handshake failed
      if (!(e instanceof ConnectException)) {
        LOGGER.debug("Couldn't connect to {}", sniffPeer, e);
        if (listener != null) {
          listener.onException(e);
        }
      }
    }
    return Collections.emptyList();
  }

  public void addFilteredAddresses(InetAddress address) {
    filteredAddresses.add(address);
  }

}
