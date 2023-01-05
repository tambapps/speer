package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.handshake.Handshake;
import com.tambapps.p2p.speer.io.BoundedInputStream;
import lombok.Getter;
import lombok.ToString;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@ToString
@Getter
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
    return from(new Socket(peer.getAddress(), peer.getPort()));
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
    return from(new Socket(peer.getAddress(), peer.getPort()), handshake);
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
    Object handshakeData = null;
    if (handshake != null) {
      try {
        handshakeData = handshake.apply(dos, dis);
      } catch (IOException e) {
        throw e instanceof HandshakeFailException ? e : new HandshakeFailException(e.getMessage(), e);
      }
    }
    return new PeerConnection(socket, dis, dos, handshakeData);
  }

  private final Socket socket;
  private final DataInputStream inputStream;
  private final DataOutputStream outputStream;
  private final Object handshakeData;

  private PeerConnection(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,
      Object handshakeData) {
    this.socket = socket;
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.handshakeData = handshakeData;
  }

  // methods from DataOutputStream
  public void write(byte[] b, int off, int len) throws IOException {
    outputStream.write(b, off, len);
  }

  public void flush() throws IOException {
    outputStream.flush();
  }

  public void writeBoolean(boolean b) throws IOException {
    outputStream.writeBoolean(b);
  }

  public final void writeByte(int v) throws IOException {
    outputStream.writeByte(v);
  }

  public final void writeShort(int v) throws IOException {
    outputStream.writeShort(v);
  }

  public final void writeChar(int v) throws IOException {
    outputStream.writeChar(v);
  }

  public final void writeInt(int v) throws IOException {
    outputStream.writeInt(v);
  }

  public final void writeLong(long v) throws IOException {
    outputStream.writeLong(v);
  }

  public final void writeFloat(float v) throws IOException {
    outputStream.writeFloat(v);
  }

  public final void writeDouble(double v) throws IOException {
    outputStream.writeDouble(v);
  }

  public final void writeBytes(String s) throws IOException {
    outputStream.writeBytes(s);
  }

  public final void writeChars(String s) throws IOException {
    outputStream.writeChars(s);
  }

  public final void writeUTF(String str) throws IOException {
    outputStream.writeUTF(str);
  }

  public int nbBytesWritten() {
    return outputStream.size();
  }

  public void write(byte[] b) throws IOException {
    outputStream.write(b);
  }

  public void writeInputStream(InputStream inputStream) throws IOException {
    writeInputStream(inputStream, 8192);
  }

  public void writeInputStream(InputStream inputStream, int bufferSize) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int count;
    while ((count = inputStream.read(buffer)) > 0) {
      outputStream.write(buffer, 0, count);
    }
  }

  // methods from DataInputStream
  public final int read(byte[] b) throws IOException {
    return inputStream.read(b);
  }

  public final int read(byte[] b, int off, int len) throws IOException {
    return inputStream.read(b, off, len);
  }

  public final int skipBytes(int n) throws IOException {
    return inputStream.skipBytes(n);
  }

  public final boolean readBoolean() throws IOException {
    return inputStream.readBoolean();
  }

  public final byte readByte() throws IOException {
    return inputStream.readByte();
  }

  public final int readUnsignedByte() throws IOException {
    return inputStream.readUnsignedByte();
  }

  public final short readShort() throws IOException {
    return inputStream.readShort();
  }

  public final int readUnsignedShort() throws IOException {
    return inputStream.readUnsignedShort();
  }

  public final char readChar() throws IOException {
    return inputStream.readChar();
  }

  public final int readInt() throws IOException {
    return inputStream.readInt();
  }

  public final long readLong() throws IOException {
    return inputStream.readLong();
  }

  public final float readFloat() throws IOException {
    return inputStream.readFloat();
  }

  public final double readDouble() throws IOException {
    return inputStream.readDouble();
  }

  public final String readUTF() throws IOException {
    return inputStream.readUTF();
  }

  public long skip(long n) throws IOException {
    return inputStream.skip(n);
  }

  public InputStream inputStream(long size) {
    BoundedInputStream boundedInputStream = new BoundedInputStream(inputStream, size);
    boundedInputStream.setPropagateClose(false);
    return boundedInputStream;
  }

  public <T> T getHandshakeData() {
    return (T) handshakeData;
  }

  public Peer getSelfPeer() {
    return Peer.of(socket.getLocalAddress(), socket.getLocalPort());
  }

  public Peer getRemotePeer() {
    return Peer.of(socket.getInetAddress(), socket.getPort());
  }

  public boolean isClosed() {
    return socket.isClosed();
  }

  @Override
  public void close() throws IOException {
    socket.close();
  }
}
