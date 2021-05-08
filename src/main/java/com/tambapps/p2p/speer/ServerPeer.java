package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.handshake.Handshake;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class ServerPeer implements Closeable {

  private final ServerSocket serverSocket;
  private final Handshake handshake;

  public ServerPeer() throws IOException {
    this(new ServerSocket());
  }
  public ServerPeer(Peer peer) throws IOException {
    this(peer, null);
  }

  public ServerPeer(Peer peer, Handshake handshake) throws IOException {
    this(new ServerSocket(peer.getPort(), 10, peer.getIp()), handshake);
  }

  public ServerPeer(ServerSocket serverSocket) {
    this(serverSocket, null);
  }

  public ServerPeer(ServerSocket serverSocket, Handshake handshake) {
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

  @Override
  public void close() throws IOException {
    serverSocket.close();
  }
}
