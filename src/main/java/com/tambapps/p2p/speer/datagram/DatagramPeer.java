package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

@AllArgsConstructor
public class DatagramPeer implements IDatagramPeer {

  @Getter
  private final DatagramSocket socket;
  @Getter
  @Setter
  private int defaultBufferSize;

  public DatagramPeer(Peer peer) throws SocketException {
    this(new DatagramSocket(peer.toSocketAddress()));
  }

  public DatagramPeer(InetAddress address, int port) throws SocketException {
    this(new DatagramSocket(new InetSocketAddress(address, port)));
  }

  public DatagramPeer(int port) throws SocketException {
    this(new DatagramSocket(port));
  }

  public DatagramPeer(DatagramSocket socket) {
    this.socket = socket;
    this.defaultBufferSize = 1024;
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
  public void close() {
    socket.close();
  }
}
