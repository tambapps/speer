package com.tambapps.p2p.fandem.sniff.strategy;

import com.tambapps.p2p.fandem.Peer;

import java.util.Iterator;

public interface SniffingStrategy extends Iterator<Peer>, Iterable<Peer> {

  @Override
  default Iterator<Peer> iterator() {
    return this;
  }

  /**
   * Reset this strategy, allowing to iterate from the start again
   */
  void reset();

}
