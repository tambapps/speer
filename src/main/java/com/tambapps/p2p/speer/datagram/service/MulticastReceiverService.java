package com.tambapps.p2p.speer.datagram.service;

import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.io.Deserializer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Service used to be listen when mulitcasted data
 * @param <T> the type of data to receive
 */
public class MulticastReceiverService<T> {

  public interface DiscoveryListener<T> {

    void onDiscovery(T discoveredData);

    void onError(IOException e);

  }

  private final ExecutorService executorService;
  private final InetAddress multicastAddress;
  private final int port;
  private final Deserializer<T> deserializer;
  private final DiscoveryListener<T> listener;
  private Future<?> future;
  private MulticastDatagramPeer datagramPeer;

  public MulticastReceiverService(ExecutorService executorService,
      InetAddress multicastAddress,
      int port, Deserializer<T> deserializer,
      DiscoveryListener<T> listener) {
    if (!multicastAddress.isMulticastAddress()) {
      throw new IllegalArgumentException("Address should be multicast");
    }
    this.executorService = executorService;
    this.multicastAddress = multicastAddress;
    this.port = port;
    this.deserializer = deserializer;
    this.listener = listener;
  }

  public void start(int bufferSize) throws IOException {
    MulticastDatagramPeer datagramPeer = new MulticastDatagramPeer(port);
    datagramPeer.setDefaultBufferSize(bufferSize);
    start(datagramPeer);
  }

  public void start() throws IOException {
    start(new MulticastDatagramPeer(port));
  }

  public void start(MulticastDatagramPeer datagramPeer) throws IOException {
    this.datagramPeer = datagramPeer;
    datagramPeer.joinGroup(multicastAddress);

    future = executorService.submit(() -> listen(datagramPeer));
  }

  public void stop() {
    datagramPeer.close();
    future.cancel(true);
  }

  private void listen(MulticastDatagramPeer datagramPeer) {
    while (!Thread.interrupted()) {
      try {
        listener.onDiscovery(datagramPeer.receiveObject(deserializer));
      } catch (IOException e) {
        if (!datagramPeer.isClosed()) {
          listener.onError(e);
        }
      }
    }
  }
}
