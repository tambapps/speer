package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.handshake.Handshake;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class PeerServer implements Closeable {

  public interface ConnectionListener {

    void onConnection(PeerConnection connection) throws IOException;

  }

  private final ServerSocket serverSocket;
  private final Handshake handshake;

  public PeerServer() throws IOException {
    this(new ServerSocket());
  }
  public PeerServer(Peer peer) throws IOException {
    this(peer, null);
  }

  public PeerServer(Peer peer, Handshake handshake) throws IOException {
    this(new ServerSocket(peer.getPort(), 10, peer.getAddress()), handshake);
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

  public void bind(Peer peer) throws IOException {
    bind(peer.toSocketAddress());
  }

  public void bind(SocketAddress address) throws IOException {
    serverSocket.bind(address);
  }

  public boolean isClosed() {
    return serverSocket.isClosed();
  }

  public ServerSocket getServerSocket() {
    return serverSocket;
  }

  public int getPort() {
    return serverSocket.getLocalPort();
  }

  public void run(ConnectionListener listener) throws IOException {
    while (!Thread.interrupted()) {
      PeerConnection connection = accept();
      listener.onConnection(connection);
    }
  }

  @Override
  public void close() throws IOException {
    serverSocket.close();
  }
}
