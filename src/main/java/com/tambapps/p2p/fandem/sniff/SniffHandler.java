package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.sniff.handshake.SniffHandshake;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class SniffHandler {

  interface SniffListener {
    void onHandShakeCompleted(Peer peer);
  }

  private final Peer peer;
  private final SniffHandshake sniffHandshake;
  private final AtomicBoolean active = new AtomicBoolean();
  @Setter
  private SniffListener listener;

  public SniffHandler(Peer peer,
      SniffHandshake sniffHandshake) {
    this.peer = peer;
    this.sniffHandshake = sniffHandshake;
  }

  // blocking
  public void start() throws IOException {
    active.set(true);
    try (ServerSocket serverSocket = new ServerSocket(peer.getPort(), 10, peer.getIp())) {
      while (active.get() && !Thread.interrupted()) {
        Socket socket = serverSocket.accept();
        Peer sniffedPeer = sniffHandshake.apply(socket);
        if (listener != null) {
          listener.onHandShakeCompleted(sniffedPeer);
        }
      }
    }
  }

  public void tryStart() {
    try {
      start();
    } catch (IOException e) {
      // TODO add logger library and log errors
    }
  }
  public void start(Executor executor) throws IOException {
    executor.execute(this::tryStart);
  }

  public void stop() {
    active.set(false);
  }
}
