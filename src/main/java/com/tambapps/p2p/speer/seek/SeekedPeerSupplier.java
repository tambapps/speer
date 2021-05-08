package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.seek.strategy.SeekingStrategy;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Supplier of seeked peer. The seeking process will be asynchronous if an executor service is
 * provided
 */
public class SeekedPeerSupplier implements PeerSeeker.SeekListener {

  private final BlockingQueue<Peer> peersQueue = new LinkedBlockingDeque<>();
  private final SeekingStrategy seekingStrategy;
  private final PeerSeeker seeker;
  private final ExecutorService executorService;

  public SeekedPeerSupplier(SeekingStrategy seekingStrategy,
      PeerSeeking seeking) {
    this(null, seekingStrategy, seeking);
  }

  public SeekedPeerSupplier(ExecutorService executorService,
      SeekingStrategy seekingStrategy,
      PeerSeeking seeking) {
    this.executorService = executorService;
    this.seekingStrategy = seekingStrategy;
    this.seeker = new PeerSeeker(seeking, this);
  }

  public Peer get() throws InterruptedException {
    if (peersQueue.isEmpty()) {
      if (executorService != null) {
        asyncSeek();
      } else {
        seeker.seek(seekingStrategy);
      }
    }
    return peersQueue.take();
  }

  private void asyncSeek() throws InterruptedException {
    List<Future<List<Peer>>> seek = seeker.seek(seekingStrategy, executorService);
    for (Future<List<Peer>> future : seek) {
      if (!peersQueue.isEmpty()) {
        return;
      }
      try {
        future.get();
      } catch (ExecutionException e) {
        throw new RuntimeException("Error while executing seeking", e);
      }
    }
  }

  @Override
  public void onPeersFound(List<Peer> peers) {
    peersQueue.addAll(peers);
  }
}
