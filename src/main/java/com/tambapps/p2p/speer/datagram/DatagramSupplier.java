package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.util.Deserializer;
import lombok.AllArgsConstructor;

import java.io.Closeable;
import java.io.IOException;

@AllArgsConstructor
public class DatagramSupplier<T> implements Closeable {

  private final DatagramPeer datagramPeer;
  private final Deserializer<T> deserializer;

  public T get() throws IOException {
    return datagramPeer.receiveObject(deserializer);
  }

  @Override
  public void close() throws IOException {
    datagramPeer.close();
  }
}
