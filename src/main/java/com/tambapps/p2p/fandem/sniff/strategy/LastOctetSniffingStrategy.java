package com.tambapps.p2p.fandem.sniff.strategy;

import com.tambapps.p2p.fandem.Peer;

public class LastOctetSniffingStrategy implements SniffingStrategy {

  // TODO complete me
  private final int port;
  // TODO allow to specify the starting point
  private int i = 0;

  public LastOctetSniffingStrategy(int port) {
    this.port = port;
  }

  @Override
  public void reset() {
    i = 0;
  }

  @Override
  public boolean hasNext() {
    return i < 255;
  }

  @Override
  public Peer next() {
    return null;
  }
}
