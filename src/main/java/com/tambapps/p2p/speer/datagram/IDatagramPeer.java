package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.util.Deserializer;
import com.tambapps.p2p.speer.util.Serializer;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public interface IDatagramPeer extends Closeable {

  void send(DatagramPacket packet) throws IOException;
  void receive(DatagramPacket packet) throws IOException;

  default void send(String string, Peer peer) throws IOException {
    send(string.getBytes(StandardCharsets.UTF_8), peer);
  }

  default void send(byte[] bytes, Peer peer) throws IOException {
    send(new DatagramPacket(bytes, bytes.length, peer.getAddress(), peer.getPort()));
  }

  default void send(String string, InetAddress address, int port) throws IOException {
    send(string.getBytes(StandardCharsets.UTF_8), address, port);
  }

  default void send(byte[] bytes, InetAddress address, int port) throws IOException {
    send(new DatagramPacket(bytes, bytes.length, address, port));
  }

  default <T> void send(T data, Serializer<T> serializer, Peer peer) throws IOException {
    send(data, serializer, peer.getAddress(), peer.getPort());
  }

  default <T> void send(T data, Serializer<T> serializer, InetAddress address, int port) throws IOException {
    byte[] bytes = serializer.serializeToBytes(data);
    send(bytes, address, port);
  }

  default <T> T receiveObject(Deserializer<T> deserializer, int bufferSize) throws IOException {
    return deserializer.deserialize(new ByteArrayInputStream(receiveBytes(bufferSize)));
  }

  default <T> T receiveObject(Deserializer<T> deserializer) throws IOException {
    return deserializer.deserialize(new ByteArrayInputStream(receiveBytes()));
  }

  default byte[] receiveBytes() throws IOException {
    return receiveBytes(getDefaultBufferSize());
  }

  default byte[] receiveBytes(int bufferSize) throws IOException {
    return receiveBytes(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

  default byte[] receiveBytes(DatagramPacket packet) throws IOException {
    receive(packet);
    if (packet.getData().length == packet.getLength()) {
      return packet.getData();
    } else {
      byte[] response = new byte[packet.getLength()];
      System.arraycopy(packet.getData(), 0, response, 0, packet.getLength());
      return response;
    }
  }

  default InputStream receiveStream() throws IOException {
    return receiveStream(getDefaultBufferSize());
  }

  default InputStream receiveStream(int bufferSize) throws IOException {
    return receiveStream(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

  default InputStream receiveStream(DatagramPacket packet) throws IOException {
    receive(packet);
    return packet.getData().length == packet.getLength() ? new ByteArrayInputStream(packet.getData()) :
        new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
  }

  default String receiveString() throws IOException {
    return receiveString(getDefaultBufferSize());
  }

  default String receiveString(int bufferSize) throws IOException {
    return receiveString(new DatagramPacket(new byte[bufferSize], bufferSize));
  }

  default String receiveString(DatagramPacket packet) throws IOException {
    receive(packet);
    if (packet.getData().length == packet.getLength()) {
      return new String(packet.getData());
    } else {
      return new String(packet.getData(), 0, packet.getLength());
    }
  }

  default DatagramPacket receive() throws IOException {
    return receive(getDefaultBufferSize());
  }

  default DatagramPacket receive(int bufferSize) throws IOException {
    DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
    receive(packet);
    return packet;
  }

  int getDefaultBufferSize();
  void setDefaultBufferSize(int bufferSize);

  boolean isClosed();

  @Override
  void close();
}
