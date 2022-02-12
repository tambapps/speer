package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import lombok.Getter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class MulticastDatagramPeer implements IDatagramPeer {

  @Getter
  private final MulticastSocket socket;

  public MulticastDatagramPeer(Peer peer) throws IOException {
    this(new MulticastSocket(peer.toSocketAddress()));
  }

  // constructor for when joining group
  public MulticastDatagramPeer(int port) throws IOException {
    this(new MulticastSocket(port));
  }
  public MulticastDatagramPeer() throws IOException {
    this(new MulticastSocket());
  }

  public MulticastDatagramPeer(InetAddress address, int port) throws IOException {
    this(new MulticastSocket(new InetSocketAddress(address, port)));
  }

  public MulticastDatagramPeer(MulticastSocket socket) {
    this.socket = socket;
  }

  public void send(DatagramPacket packet) throws IOException {
    socket.send(packet);
  }

  public void receive(DatagramPacket packet) throws IOException {
    socket.receive(packet);
  }

  public void joinGroup(InetAddress multicastAddress) throws IOException {
    socket.joinGroup(multicastAddress);
  }

  public void leaveGroup(InetAddress multicastAddress) throws IOException {
    socket.leaveGroup(multicastAddress);
  }

  public Peer getSelfPeer() {
    return Peer.of(socket.getLocalAddress(), socket.getLocalPort());
  }

  @Override
  public boolean isClosed() {
    return socket.isClosed();
  }

  @Override
  public void close() {
    socket.close();
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
}
