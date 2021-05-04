package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.exception.NoPeerFoundException;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake;
import com.tambapps.p2p.fandem.sniff.strategy.SniffingStrategy;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class PeerSniffer {

  private final SniffingStrategy strategy;
  private final SniffHandshake handshake;

  public Peer sniffOne() {
    strategy.reset();
    while (strategy.hasNext()) {
      Peer sniffPeer = strategy.next();
      try (Socket socket = new Socket(sniffPeer.getIp(), sniffPeer.getPort())) {
        return handshake.apply(socket);
      } catch (IOException e) {
        // connection failed
      }
    }
    throw new NoPeerFoundException();
  }

  public List<Peer> sniff() {
    List<Peer> peers = new ArrayList<>();
    sniff(peers::add);
    return peers;
  }

  public void sniff(Consumer<Peer> peerConsumer) {
    strategy.reset();
    for (Peer sniffPeer: strategy) {
      try (Socket socket = new Socket(sniffPeer.getIp(), sniffPeer.getPort())) {
        peerConsumer.accept(handshake.apply(socket));
      } catch (IOException e) {
        // connection failed
      }
    }
  }
}
