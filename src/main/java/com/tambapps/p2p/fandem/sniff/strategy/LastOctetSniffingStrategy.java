package com.tambapps.p2p.fandem.sniff.strategy;

import com.tambapps.p2p.fandem.Peer;
import lombok.SneakyThrows;

import java.net.InetAddress;

public class LastOctetSniffingStrategy implements SniffingStrategy {

  // TODO complete me
  private final byte[] address;
  private final int port;
  private final byte start;

  // TODO allow to specify the starting point
  private int i;

  public LastOctetSniffingStrategy(InetAddress address, int port) {
    this(address, port, (byte) 0);
  }
  public LastOctetSniffingStrategy(InetAddress address, int port, byte start) {
    this.address = address.getAddress();
    this.port = port;
    this.start = start;
    reset();
  }

  @Override
  public void reset() {
    i = 0;
    this.address[3] = start;
  }

  @Override
  public boolean hasNext() {
    return i <= 255;
  }

  @SneakyThrows
  @Override
  public Peer next() {
    Peer peer = Peer.of(InetAddress.getByAddress(address), port);
    address[3] = (byte) (start + ++i);
    return peer;
  }
}
