package com.tambapps.p2p.speer.datagram;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.io.Deserializer;
import lombok.AllArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;

@AllArgsConstructor
public class DatagramSupplier<T> implements Closeable {

  private final IDatagramPeer datagramPeer;
  private final Deserializer<T> deserializer;

  public DatagramSupplier(Peer peer, Deserializer<T> deserializer) throws SocketException {
    this(new DatagramPeer(peer), deserializer);
  }

  public T get() throws IOException {
    return datagramPeer.receiveObject(deserializer);
  }

  @Override
  public void close() {
    datagramPeer.close();
  }
}
