package com.tambapps.p2p.speer.datagram.service;

import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.io.Deserializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

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
  private final AtomicReference<MulticastDatagramPeer> datagramPeerReference = new AtomicReference<>();
  @Setter
  @Getter
  private DiscoveryListener<T> listener;

  public MulticastReceiverService(ExecutorService executorService,
      InetAddress multicastAddress, int port, Deserializer<T> deserializer) {
    this(executorService, multicastAddress, port, deserializer, null);
  }

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
    datagramPeer.setReceiveBufferSize(bufferSize);
    start(datagramPeer);
  }

  public void start() throws IOException {
    start(new MulticastDatagramPeer(port));
  }

  public void start(MulticastDatagramPeer datagramPeer) throws IOException {
    if (datagramPeerReference.get() != null) {
      throw new IllegalStateException("Service is already started");
    }
    datagramPeerReference.set(datagramPeer);
    datagramPeer.joinGroup(multicastAddress);
    if (listener == null) {
      throw new IllegalStateException("Listener should not be null");
    }
    executorService.submit(() -> listen(datagramPeer));
  }

  public void stop() {
    MulticastDatagramPeer datagramPeer = datagramPeerReference.get();
    if (datagramPeer != null && !datagramPeer.isClosed()) {
      datagramPeer.close();
      datagramPeerReference.set(null);
    }
  }

  public boolean isRunning() {
    return datagramPeerReference.get() != null;
  }

  protected void listen(MulticastDatagramPeer datagramPeer) {
    while (!Thread.interrupted()) {
      try {
        listener.onDiscovery(datagramPeer.receiveObject(deserializer));
      } catch (IOException e) {
        if (!datagramPeer.isClosed()) {
          listener.onError(e);
        } else {
          break;
        }
      }
    }
  }
}
