package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.handshake.Handshake;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class PeerServer implements Closeable {

  private final ServerSocket serverSocket;
  private final Handshake handshake;

  public PeerServer() throws IOException {
    this(new ServerSocket(), null);
  }

  public PeerServer(ServerSocket serverSocket) {
    this(serverSocket, null);
  }

  public PeerServer(ServerSocket serverSocket, Handshake handshake) {
    this.serverSocket = serverSocket;
    this.handshake = handshake;
  }

  public PeerConnection accept() throws IOException {
    return PeerConnection.from(serverSocket.accept(), handshake);
  }

  public void setAcceptTimeout(int timeoutMillis) throws SocketException {
    serverSocket.setSoTimeout(timeoutMillis);
  }

  public boolean isClosed() {
    return serverSocket.isClosed();
  }

  public ServerSocket getServerSocket() {
    return serverSocket;
  }

  @Override
  public void close() throws IOException {
    serverSocket.close();
  }
}
