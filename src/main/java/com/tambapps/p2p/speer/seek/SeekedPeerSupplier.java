package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;
import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

@AllArgsConstructor
public class SeekedPeerSupplier {

  private BlockingQueue<Peer> peersQueue = new LinkedBlockingDeque<>();
  private final PeerSeeker seeker;
  private final ExecutorService executorService;

  public Peer get() throws InterruptedException {
    return peersQueue.take();
  }
}
