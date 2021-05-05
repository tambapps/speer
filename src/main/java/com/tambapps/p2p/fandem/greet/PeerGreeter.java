package com.tambapps.p2p.fandem.greet;

import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.PeerConnection;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
@Slf4j
public class PeerGreeter {

  // the peer from which to greet
  private final List<Peer> greetingPeers;
  private final PeerGreetings greetings;

  // server socket needs to be provided because the only way to interrupt serverSocket.accept()
  // is by calling serverSocket.close() from another thread
  public void greetOne(ServerSocket serverSocket) throws IOException {
    try (Socket socket = serverSocket.accept();
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
      greetings.write(greetingPeers, outputStream);
    }
  }

  public void greet(ServerSocket serverSocket, AtomicBoolean interrupt) throws IOException {
    while (!interrupt.get() && !Thread.interrupted()) {
      greetOne(serverSocket);
    }
  }

  public void greet(ServerSocket serverSocket) throws IOException {
    while (!Thread.interrupted()) {
      greetOne(serverSocket);
    }
  }

  public void greet(PeerConnection connection) throws IOException {
    while (!Thread.interrupted()) {
      greetings.write(greetingPeers, connection.getOutputStream());
    }
  }

  public void greet(PeerConnection connection, AtomicBoolean interrupt) throws IOException {
    while (!interrupt.get() && !Thread.interrupted()) {
      greetings.write(greetingPeers, connection.getOutputStream());
    }
  }

  public void checkGreet(ServerSocketChannel serverSocketChannel) throws IOException {
    checkGreet(greetings, greetingPeers, serverSocketChannel);
  }

  /**
   * method that greets peer from a non blocking socket, if a connection was found
   * @param greetings the greetings
   * @param greetingPeers the peers to greet with
   * @param serverSocketChannel the socket from which to check if a seeker has seeked
   * @throws IOException in case of I/O errors
   */
  public static void checkGreet(PeerGreetings greetings, List<Peer> greetingPeers,
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
