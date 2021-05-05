package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.handshake.Handshake;
import com.tambapps.p2p.speer.util.FileProvider;
import lombok.Getter;
import lombok.ToString;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;

@Getter
@ToString
public class PeerConnection implements Closeable {

  /**
   * Creates a new peer connection
   *
   * @param serverSocket the server socket from which to accept a connection
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(ServerSocket serverSocket) throws IOException {
    return from(serverSocket.accept(), null);
  }

  /**
   * Creates a new peer connection
   *
   * @param serverSocket the server socket from which to accept a connection
   * @param handshake    a nullable handshake
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(ServerSocket serverSocket, Handshake handshake)
      throws IOException {
    return from(serverSocket.accept(), handshake);
  }

  /**
   * Creates a new peer connection
   *
   * @param peer the remote peer
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(Peer peer) throws IOException {
    return from(new Socket(peer.getIp(), peer.getPort()));
  }

  /**
   * Creates a new peer connection
   *
   * @param peer      the remote peer
   * @param handshake a nullable handshake
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(Peer peer, Handshake handshake) throws IOException {
    return from(new Socket(peer.getIp(), peer.getPort()), handshake);
  }

  /**
   * Creates a new peer connection
   *
   * @param socket the socket handling the connection
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(Socket socket) throws IOException {
    return from(socket, null);
  }

  /**
   * Creates a new peer connection
   *
   * @param socket    the socket handling the connection
   * @param handshake a nullable handshake
   * @return a peer connection
   * @throws IOException in case of I/O errors or handshake fail (if provided)
   */
  public static PeerConnection from(Socket socket, Handshake handshake) throws IOException {
    DataInputStream dis = new DataInputStream(socket.getInputStream());
    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    Map<String, String> attributes = handshake != null ? handshake.apply(dos, dis) :
        Collections.emptyMap();
    return new PeerConnection(socket, dis, dos, attributes);
  }

  private final Socket socket;
  private final DataInputStream inputStream;
  private final DataOutputStream outputStream;
  private final Map<String, String> attributes;

  private PeerConnection(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,
      Map<String, String> attributes) {
    this.socket = socket;
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.attributes = attributes;
  }

  // TODO remove me. Too specific to fandem
  public void sendFile(File file) throws IOException {
    outputStream.writeLong(file.length());
    outputStream.writeUTF(file.getName());
    try (FileInputStream is = new FileInputStream(file)) {
      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) != -1) {
        outputStream.write(buffer, 0, len);
      }
    }
  }

  public void receiveFile(File outputFile) throws IOException {
    receiveFile((name -> outputFile));
  }

  // TODO remove me. Too specific to fandem
  public void receiveFile(FileProvider fileProvider) throws IOException {
    long fileSize = inputStream.readLong();
    String fileName = inputStream.readUTF();
    try (FileOutputStream os = new FileOutputStream(fileProvider.newFile(fileName))) {
      byte[] buffer = new byte[1024];
      int nbBytesToRead = fileSize < buffer.length ? (int) fileSize : buffer.length;
      long bytesRead = 0L;
      while (bytesRead < fileSize) {
        bytesRead += inputStream.read(buffer, 0, nbBytesToRead);
        os.write(buffer, 0, nbBytesToRead);
        long remainingBytes = fileSize - bytesRead;
        nbBytesToRead = remainingBytes < buffer.length ? (int) remainingBytes : buffer.length;
      }
    }
  }

  public boolean isClosed() {
    return socket.isClosed();
  }

  @Override
  public void close() throws IOException {
    socket.close();
  }
}
