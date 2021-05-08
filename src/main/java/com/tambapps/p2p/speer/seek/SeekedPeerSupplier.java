package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.seek.strategy.SeekingStrategy;

import java.io.IOException;
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
public class SeekedPeerSupplier<T extends Peer> implements PeerSeeker.SeekListener<T> {

  private final BlockingQueue<Peer> peersQueue = new LinkedBlockingDeque<>();
  private final SeekingStrategy seekingStrategy;
  private final PeerSeeker<T> seeker;
  private final ExecutorService executorService;

  public SeekedPeerSupplier(SeekingStrategy seekingStrategy,
      PeerSeeking<T> seeking) {
    this(null, seekingStrategy, seeking);
  }

  public SeekedPeerSupplier(ExecutorService executorService,
      SeekingStrategy seekingStrategy,
      PeerSeeking<T> seeking) {
    this.executorService = executorService;
    this.seekingStrategy = seekingStrategy;
    this.seeker = new PeerSeeker<>(seeking, this);
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
    List<Future<List<T>>> seek = seeker.seek(seekingStrategy, executorService);
    for (Future<List<T>> future : seek) {
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
  public void onPeersFound(List<T> peers) {
    peersQueue.addAll(peers);
  }

  @Override
  public void onException(IOException e) {
    // do nothing?
  }
}
