package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import lombok.Getter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class DatagramPeer implements IDatagramPeer {

  @Getter
  private final DatagramSocket socket;

  public DatagramPeer(Peer peer) throws SocketException {
    this(new DatagramSocket(peer.toSocketAddress()));
  }

  public DatagramPeer(InetAddress address, int port) throws SocketException {
    this(new DatagramSocket(new InetSocketAddress(address, port)));
  }

  public DatagramPeer(int port) throws SocketException {
    this(new DatagramSocket(port));
  }

  // constructor for sender when not caring about from which address:port sending
  public DatagramPeer() throws SocketException {
    this(new DatagramSocket());
  }

  public DatagramPeer(DatagramSocket socket) {
    this.socket = socket;
  }

  @Override
  public void send(DatagramPacket packet) throws IOException {
    socket.send(packet);
  }

  @Override
  public void receive(DatagramPacket packet) throws IOException {
    socket.receive(packet);
  }

  public Peer getSelfPeer() {
    return Peer.of(socket.getLocalAddress(), socket.getLocalPort());
  }

  @Override
  public void setReceiveBufferSize(int bufferSize) throws IOException {
    socket.setReceiveBufferSize(bufferSize);
  }

  @Override
  public int getReceiveBufferSize() throws IOException {
    return socket.getReceiveBufferSize();
  }

  @Override
  public void setSendBufferSize(int bufferSize) throws IOException {
    socket.setSendBufferSize(bufferSize);
  }

  @Override
  public int getSendBufferSize() throws IOException {
    return socket.getSendBufferSize();
  }

  @Override
  public boolean isClosed() {
    return socket.isClosed();
  }

  @Override
  public void close() {
    socket.close();
  }
}
