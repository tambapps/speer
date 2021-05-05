package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.PeerConnection;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake2;
import com.tambapps.p2p.fandem.sniff.strategy.SniffingStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO make sniff handler be the sniffer and the sniffed with non blocking IO
@Slf4j
public class SniffHandler {

  interface SniffListener {
    // TODO add more event

    /**
     * Callback invoked when a peer has been sniffed.
     *
     * @param peer the sniffed peer
     * @return true if the handler should stop handling sniffs
     */
    boolean onPeerFound(Peer peer);
  }

  private final Peer peer;
  private final SniffHandshake2 handshake;
  private final AtomicBoolean active = new AtomicBoolean();
  private final SniffingStrategy sniffingStrategy;
  private final SniffListener listener;

  public SniffHandler(Peer peer, SniffHandshake2 handshake,
      SniffingStrategy sniffingStrategy, SniffListener listener) {
    this.peer = peer;
    this.handshake = handshake;
    this.sniffingStrategy = sniffingStrategy;
    this.listener = listener;
  }

  public void tryStart() {
    try {
      start();
    } catch (IOException e) {
      // TODO add logger library and log errors
    }
  }
  public void start(Executor executor) {
    executor.execute(this::tryStart);
  }

  public void stop() {
    active.set(false);
  }

  // blocking
  public void start() throws IOException {
    sniffingStrategy.reset();
    active.set(true);
    try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
      LOGGER.trace("Opened server socket channel");
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.bind(peer.toSocketAddress());
      while (active.get() && !Thread.interrupted()) {
        try {
          handleSniff(serverSocketChannel);
        } catch (IOException e) {
          LOGGER.warn("An error occurred", e);
        }
        if (sniff()) {
          active.set(false);
        }
      }
    }
  }

  private void handleSniff(ServerSocketChannel serverSocketChannel) throws IOException {
    LOGGER.trace("Will handle sniff");
    SocketChannel socketChannel = serverSocketChannel.accept(); // non-blocking
    if (socketChannel != null) {
      try {
        LOGGER.debug("Peer {} sniffed me!", socketChannel.getRemoteAddress());
        socketChannel.configureBlocking(false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        handshake.write(peer, new DataOutputStream(outputStream));
        socketChannel.write(ByteBuffer.wrap(outputStream.toByteArray())); // can be non-blocking
      } finally {
        socketChannel.close();
      }
    }
    LOGGER.trace("Sniff handled");
  }

  // return true if must stop loop
  private boolean sniff() {
    LOGGER.trace("Will sniff");
    if (!sniffingStrategy.hasNext()) {
      sniffingStrategy.reset();
      // if there still isn't no sniff after having reset the strategy,
      // there is no peer to sniff at all
      if (!sniffingStrategy.hasNext()) {
        LOGGER.trace("There is no peers to sniff");
        return false;
      }
    }
    Peer sniffPeer = sniffingStrategy.next();
    if (sniffPeer.equals(peer)) {
      // prevent sniffing itself
      LOGGER.trace("Won't sniff myself");
      return false;
    }
    try (PeerConnection connection = PeerConnection.from(sniffPeer)) {
      Peer peer = handshake.read(connection.getInputStream());
      LOGGER.debug("Found peer {} while sniffing", peer);
      return listener.onPeerFound(peer);
    } catch (IOException e) {
      // connection or handshake failed
      if (!(e instanceof ConnectException)) {
        LOGGER.debug("Couldn't connect to {}", sniffPeer, e);
      }
    }
    LOGGER.trace("Sniffing finished");
    return false;
  }
}
