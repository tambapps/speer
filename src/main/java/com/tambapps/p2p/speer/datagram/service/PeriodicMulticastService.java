package com.tambapps.p2p.speer.datagram.service;

import com.tambapps.p2p.speer.datagram.MulticastDatagramPeer;
import com.tambapps.p2p.speer.util.Serializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service used to multicast data periodically
 * @param <T>
 */
public class PeriodicMulticastService<T> {

  private final ScheduledExecutorService executorService;
  private final InetAddress multicastAddress;
  private final int port;
  private final Serializer<T> serializer;
  private Future<?> future;
  private MulticastDatagramPeer datagramPeer;
  @Getter
  @Setter
  private T data;

  public PeriodicMulticastService(ScheduledExecutorService executorService,
      InetAddress multicastAddress,
      int port, Serializer<T> serializer) {
    if (!multicastAddress.isMulticastAddress()) {
      throw new IllegalArgumentException("Address should be multicast");
    }
    this.executorService = executorService;
    this.multicastAddress = multicastAddress;
    this.port = port;
    this.serializer = serializer;
  }

  public void start(int bufferSize, long delayMillis) throws IOException {
    MulticastDatagramPeer datagramPeer = new MulticastDatagramPeer();
    datagramPeer.setDefaultBufferSize(bufferSize);
    start(datagramPeer, delayMillis);
  }

  public void start(long delayMillis) throws IOException {
    start(new MulticastDatagramPeer(port), delayMillis);
  }

  public void start(MulticastDatagramPeer datagramPeer, long delayMillis) throws IOException {
    this.datagramPeer = datagramPeer;
    future = executorService.scheduleAtFixedRate(() -> multicast(datagramPeer),
        delayMillis, delayMillis, TimeUnit.MILLISECONDS);
  }

  public void stop() {
    stop(false);
  }

  public void stop(boolean shutdownExecutor) {
    if (datagramPeer.isClosed()) {
      return;
    }
    datagramPeer.close();
    future.cancel(true);
    if (shutdownExecutor) {
      executorService.shutdown();
    }
  }

  private void multicast(MulticastDatagramPeer datagramPeer) {
    try {
      datagramPeer.send(data, serializer, multicastAddress, port);
    } catch (IOException e) {
      // don't know what to do with it
    }
  }
}
