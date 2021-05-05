package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.PeerConnection;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake2;
import com.tambapps.p2p.fandem.sniff.strategy.SniffingStrategy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO make sniff handler be the sniffer and the sniffed with non blocking IO
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
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.bind(peer.toSocketAddress());
      while (active.get() && !Thread.interrupted()) {
        try {
          handleSniff(serverSocketChannel);
        } catch (IOException e) {
          // TODO use logger (and add listener to exception?)
          System.err.println(e);
        }
        if (sniff()) {
          active.set(false);
        }
      }
    }
  }

  private void handleSniff(ServerSocketChannel serverSocketChannel) throws IOException {
    SocketChannel socketChannel = serverSocketChannel.accept(); // non-blocking
    if (socketChannel != null) {
      socketChannel.configureBlocking(false);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      handshake.write(peer, new DataOutputStream(outputStream));
      socketChannel.write(ByteBuffer.wrap(outputStream.toByteArray())); // can be non-blocking
      socketChannel.close();
    }
  }

  // return true if must stop loop
  private boolean sniff() {
    if (!sniffingStrategy.hasNext()) {
      sniffingStrategy.reset();
      // if there still isn't no sniff after having reset the strategy,
      // there is no peer to sniff at all
      if (!sniffingStrategy.hasNext()) {
        return false;
      }
    }
    Peer sniffPeer = sniffingStrategy.next();
    if (sniffPeer.equals(peer)) {
      // prevent sniffing itself
      return false;
    }
    try (PeerConnection connection = PeerConnection.from(sniffPeer)) {
      Peer peer = handshake.read(connection.getInputStream());
      return listener.onPeerFound(peer);
    } catch (IOException e) {
      // connection or handshake failed
    }
    return false;
  }
}
