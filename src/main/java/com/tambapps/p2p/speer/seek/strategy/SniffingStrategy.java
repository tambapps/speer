package com.tambapps.p2p.speer.seek.strategy;

import com.tambapps.p2p.speer.Peer;

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
