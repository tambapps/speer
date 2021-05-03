package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.util.FileProvider;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PeerConnection implements Closeable {

  private final Peer peer;
  private final Socket socket;
  private final DataInputStream dis;
  private final DataOutputStream dos;

  public PeerConnection(Peer peer, Socket socket) throws IOException {
    this.peer = peer;
    this.socket = socket;
    dis = new DataInputStream(socket.getInputStream());
    dos = new DataOutputStream(socket.getOutputStream());
  }

  public void sendFile(File file) throws IOException {
    dos.writeLong(file.length());
    dos.writeUTF(file.getName());
    try (FileInputStream is = new FileInputStream(file))  {
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
