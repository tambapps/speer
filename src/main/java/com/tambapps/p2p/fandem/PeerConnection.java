package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.handshake.Handshake;
import com.tambapps.p2p.fandem.util.FileProvider;
import lombok.Getter;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;

public class PeerConnection implements Closeable {


  /**
   * Creates a new peer connection
   *
   * @param peer   the remote peer
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(Peer peer) throws IOException {
    return from(peer, new Socket(peer.getIp(), peer.getPort()), null);
  }

  /**
   * Creates a new peer connection
   *
   * @param peer   the remote peer
   * @param socket the socket handling the connection
   * @return a peer connection
   * @throws IOException in case of I/O errors
   */
  public static PeerConnection from(Peer peer, Socket socket) throws IOException {
    return from(peer, socket, null);
  }

  /**
   * Creates a new peer connection
   *
   * @param peer      the remote peer
   * @param socket    the socket handling the connection
   * @param handshake a nullable handshake
   * @return a peer connection
   * @throws IOException in case of I/O errors or handshake fail (if provided)
   */
  public static PeerConnection from(Peer peer, Socket socket,
      Handshake handshake) throws IOException {
    DataInputStream dis = new DataInputStream(socket.getInputStream());
    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
    Map<String, String> attributes = handshake != null ? handshake.apply(dos, dis) :
        Collections.emptyMap();
    return new PeerConnection(peer, socket, dis, dos, attributes);
  }

  // TODO peer field might be useless
  private final Peer peer;
  private final Socket socket;
  private final DataInputStream dis;
  private final DataOutputStream dos;
  @Getter
  private final Map<String, String> attributes;

  public PeerConnection(Peer peer, Socket socket, DataInputStream dis, DataOutputStream dos,
      Map<String, String> attributes) {
    this.peer = peer;
    this.socket = socket;
    this.dis = dis;
    this.dos = dos;
    this.attributes = attributes;
  }

  public void sendFile(File file) throws IOException {
    dos.writeLong(file.length());
    dos.writeUTF(file.getName());
    try (FileInputStream is = new FileInputStream(file)) {
      // TODO make buffer size configurable
      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) != -1) {
        dos.write(buffer, 0, len);
      }
    }
  }

  public void receiveFile(File outputFile) throws IOException {
    receiveFile((name -> outputFile));
  }

  public void receiveFile(FileProvider fileProvider) throws IOException {
    long fileSize = dis.readLong();
    String fileName = dis.readUTF();
    try (FileOutputStream os = new FileOutputStream(fileProvider.newFile(fileName))) {
      // TODO make buffer size configurable
      byte[] buffer = new byte[1024];
      int nbBytesToRead = fileSize < buffer.length ? (int) fileSize : buffer.length;
      long bytesRead = 0L;
      while (bytesRead < fileSize) {
        bytesRead += dis.read(buffer, 0, nbBytesToRead);
        os.write(buffer, 0, nbBytesToRead);
        long remainingBytes = fileSize - bytesRead;
        nbBytesToRead = remainingBytes < buffer.length ? (int) remainingBytes : buffer.length;
      }
    }
  }


  public DataInputStream getInputStream() {
    return dis;
  }

  public DataOutputStream getOutputStream() {
    return dos;
  }

  public boolean isClosed() {
    return socket.isClosed();
  }

  @Override
  public String toString() {
    return "PeerConnection{" +
        "peer=" + peer +
        ", socket=" + socket +
        '}';
  }

  @Override
  public void close() throws IOException {
    socket.close();
  }
}
