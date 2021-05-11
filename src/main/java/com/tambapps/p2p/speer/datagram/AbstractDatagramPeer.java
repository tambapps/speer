package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import lombok.AllArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public abstract class AbstractDatagramPeer implements Closeable {

  public abstract void send(DatagramPacket packet) throws IOException;
  public abstract void receive(DatagramPacket packet) throws IOException;

  public void send(String string, Peer peer) throws IOException {
    send(string.getBytes(StandardCharsets.UTF_8), peer);
  }

  public void send(byte[] bytes, Peer peer) throws IOException {
    send(new DatagramPacket(bytes, bytes.length, peer.getAddress(), peer.getPort()));
  }

  public void send(String string, InetAddress address, int port) throws IOException {
    send(string.getBytes(StandardCharsets.UTF_8), address, port);
  }

  public void send(byte[] bytes, InetAddress address, int port) throws IOException {
    send(new DatagramPacket(bytes, bytes.length, address, port));
  }

  public byte[] receiveBytes(int bufferSize) throws IOException {
    return receiveBytes(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

  public byte[] receiveBytes(DatagramPacket packet) throws IOException {
    receive(packet);
    if (packet.getData().length == packet.getLength()) {
      return packet.getData();
    } else {
      byte[] response = new byte[packet.getLength()];
      System.arraycopy(packet.getData(), 0, response, 0, packet.getLength());
      return response;
    }
  }

  public String receiveString(int bufferSize) throws IOException {
    return receiveString(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

  public String receiveString(DatagramPacket packet) throws IOException {
    receive(packet);
    if (packet.getData().length == packet.getLength()) {
      return new String(packet.getData());
    } else {
      return new String(packet.getData(), 0, packet.getLength());
    }
  }

  public void receive(int bufferSize) throws IOException {
    receive(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

}
