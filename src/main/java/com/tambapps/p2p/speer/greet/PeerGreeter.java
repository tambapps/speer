package com.tambapps.p2p.speer.greet;

import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.ServerPeer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
@Slf4j
public class PeerGreeter<T extends Peer> {

  @Getter
  private final List<T> availablePeers;
  private final PeerGreetings<T> greetings;
  private final AtomicBoolean interrupt = new AtomicBoolean();

  public PeerGreeter(PeerGreetings<T> greetings) {
    this(new ArrayList<>(), greetings);
  }

  // catch SocketException if you want to handle case when serverSocket is closed
  public void greet(ServerPeer serverPeer) throws IOException {
    interrupt.set(false);
    while (!interrupt.get() && !Thread.interrupted()) {
      greetOne(serverPeer);
    }
  }

  public void greet(ServerSocket serverSocket) throws IOException {
    interrupt.set(false);
    while (!interrupt.get() && !Thread.interrupted()) {
      greetOne(serverSocket);
    }
  }

  public void greetOne(ServerPeer serverPeer) throws IOException {
    try (PeerConnection socket = serverPeer.accept()) {
      greetings.write(availablePeers, socket.getOutputStream());
    }
  }

  // server socket needs to be provided because the only way to interrupt serverSocket.accept()
  // is by calling serverSocket.close() from another thread
  public void greetOne(ServerSocket serverSocket) throws IOException {
    try (Socket socket = serverSocket.accept();
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
      greetings.write(availablePeers, outputStream);
    }
  }

  public void greet(PeerConnection connection) throws IOException {
    greetings.write(availablePeers, connection.getOutputStream());
  }

  public void checkGreet(ServerSocketChannel serverSocketChannel) throws IOException {
    checkGreet(greetings, availablePeers, serverSocketChannel);
  }

  public void addAvailablePeer(T peer) {
    availablePeers.add(peer);
  }

  public void setAvailablePeers(List<T> peers) {
    this.availablePeers.clear();
    this.availablePeers.addAll(peers);
  }

  public void stop() {
    interrupt.set(true);
  }

  /**
   * method that greets peer from a non blocking socket, if a connection was found
   *
   * @param greetings           the greetings
   * @param greetingPeers       the peers to greet with
   * @param serverSocketChannel the socket from which to check if a seeker has seeked
   * @param <T>                 a type extending Peer
   * @throws IOException in case of I/O errors
   */
  public static <T extends Peer> void checkGreet(PeerGreetings<T> greetings, List<T> greetingPeers,
      ServerSocketChannel serverSocketChannel) throws IOException {
    SocketChannel socketChannel = serverSocketChannel.accept(); // non-blocking
    if (socketChannel != null) {
      try {
        socketChannel.configureBlocking(false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        greetings.write(greetingPeers, new DataOutputStream(outputStream));
        socketChannel.write(ByteBuffer.wrap(outputStream.toByteArray())); // can be non-blocking
      } finally {
        socketChannel.close();
      }
    }
  }
}
