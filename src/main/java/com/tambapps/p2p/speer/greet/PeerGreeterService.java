package com.tambapps.p2p.speer.greet;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.ServerPeer;
import com.tambapps.p2p.speer.handshake.Handshake;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PeerGreeterService<T extends Peer> {

  public interface ErrorListener {

    /**
     * Callback invoked when the seeker encountered an error. If this callback returns true,
     * the service will automatically try to restart the seeking
     * @param e the exception
     * @return whether the service should automatically try to restart the seeking
     */
    boolean onException(IOException e);

  }
  private final ExecutorService executorService;
  @Getter
  private final PeerGreeter<T> greeter;
  private final ErrorListener listener;
  private final Handshake handshake;

  private ServerPeer serverPeer;

  public PeerGreeterService(ExecutorService executorService,
      PeerGreeter<T> greeter) {
    this(executorService, greeter, null, null);
  }

  public PeerGreeterService(ExecutorService executorService,
      PeerGreeter<T> greeter, Handshake handshake) {
    this(executorService, greeter, null, handshake);
  }

  public PeerGreeterService(ExecutorService executorService,
      PeerGreeter<T> greeter, ErrorListener listener) {
    this(executorService, greeter, listener, null);
  }

  public PeerGreeterService(ExecutorService executorService,
      PeerGreeter<T> greeter, ErrorListener listener, Handshake handshake) {
    this.executorService = executorService;
    this.greeter = greeter;
    this.listener = listener;
    this.handshake = handshake;
  }

  public void start(Peer peer) throws IOException {
    serverPeer = new ServerPeer(peer, handshake);
    executorService.submit(() -> greet(serverPeer));
  }

  public void start(ServerPeer serverPeer) {
    this.serverPeer = serverPeer;
    executorService.submit(() -> greet(serverPeer));
  }

  public void stop() {
    if (serverPeer == null) {
      throw new IllegalStateException("Service wasn't started");
    }
    try {
      serverPeer.close();
    } catch (IOException e) {
      // ignore
    }
  }

  public void addAvailablePeer(T peer) {
    greeter.addAvailablePeer(peer);
  }

  public void setAvailablePeers(List<T> peers) {
    greeter.setAvailablePeers(peers);
  }

  private void greet(ServerPeer serverPeer) {
    try {
      greeter.greet(serverPeer);
    } catch (IOException e) {
      if (listener != null && listener.onException(e)) {
        try {
          greeter.greet(serverPeer);
        } catch (IOException ioException) {
          // ignore for real this time
        }
      }
    }
  }
}
