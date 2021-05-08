package com.tambapps.p2p.speer.greet;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.ServerPeer;

import java.io.IOException;
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
  private final PeerGreeter<T> greeter;
  private final ErrorListener listener;

  private ServerPeer serverPeer;

  public PeerGreeterService(ExecutorService executorService,
      PeerGreeter<T> greeter) {
    this(executorService, greeter, null);
  }

  public PeerGreeterService(ExecutorService executorService,
      PeerGreeter<T> greeter, ErrorListener listener) {
    this.executorService = executorService;
    this.greeter = greeter;
    this.listener = listener;
  }

  public void start(Peer peer) throws IOException {
    serverPeer = new ServerPeer(peer);
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
