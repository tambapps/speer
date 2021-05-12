package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.util.Deserializer;
import com.tambapps.p2p.speer.util.Serializer;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public abstract class AbstractDatagramPeer implements Closeable {

  @Getter
  @Setter
  private int bufferSize = 1024;

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

  public <T> void send(T data, Serializer<T> serializer, Peer peer) throws IOException {
    send(data, serializer, peer.getAddress(), peer.getPort());
  }

  public <T> void send(T data, Serializer<T> serializer, InetAddress address, int port) throws IOException {
    byte[] bytes = serializer.serialize(data);
    send(bytes, address, port);
  }

  public <T> T receiveObject(Deserializer<T> deserializer, int bufferSize) throws IOException {
    return deserializer.deserialize(new ByteArrayInputStream(receiveBytes(bufferSize)));
  }

  public <T> T receiveObject(Deserializer<T> deserializer) throws IOException {
    return deserializer.deserialize(new ByteArrayInputStream(receiveBytes()));
  }

  public byte[] receiveBytes() throws IOException {
    return receiveBytes(bufferSize);
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

  public InputStream receiveStream() throws IOException {
    return receiveStream(bufferSize);
  }

  public InputStream receiveStream(int bufferSize) throws IOException {
    return receiveStream(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

  public InputStream receiveStream(DatagramPacket packet) throws IOException {
    receive(packet);
    return packet.getData().length == packet.getLength() ? new ByteArrayInputStream(packet.getData()) :
        new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
  }

  public String receiveString() throws IOException {
    return receiveString(bufferSize);
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

  public DatagramPacket receive() throws IOException {
    return receive(bufferSize);
  }
  public DatagramPacket receive(int bufferSize) throws IOException {
    DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
    receive(packet);
    return packet;
  }

}
