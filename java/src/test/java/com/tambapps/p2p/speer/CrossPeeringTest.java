package com.tambapps.p2p.speer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("To be run manually")
public class CrossPeeringTest {

  @Test
  public void testServerRead() throws IOException {
    Peer peer = Peer.of("127.0.0.1", 8081);
    System.out.println("Peer " + peer);
    try (PeerServer server = new PeerServer(peer)) {
      server.setAcceptTimeout(60_000);
      PeerConnection connection = server.accept();
      readTest(connection);
    }
  }

  @Test
  public void testClientRead() throws IOException {
    try (PeerConnection connection = PeerConnection.from("127.0.0.1", 8081)) {
      readTest(connection);
    }
  }

  @Test
  public void testServerWrite() throws IOException {
    Peer peer = Peer.of("127.0.0.1", 8081);
    System.out.println("Peer " + peer);
    try (PeerServer server = new PeerServer(peer)) {
      server.setAcceptTimeout(60_000);
      PeerConnection connection = server.accept();
      writeTest(connection);
    }
  }

  @Test
  public void testClientWrite() throws IOException {
    try (PeerConnection connection = PeerConnection.from("127.0.0.1", 8081)) {
      writeTest(connection);
    }
  }

  private void readTest(PeerConnection connection) throws IOException {
    assertEquals(Integer.MAX_VALUE, connection.readInt());
    assertEquals(1, connection.readShort());
    assertEquals(8, connection.readByte());
    assertEquals("hello world", connection.readString());
  }

  private void writeTest(PeerConnection connection) throws IOException {
    connection.writeInt(Integer.MAX_VALUE);
    connection.writeShort(1);
    connection.writeByte(8);
    connection.writeString("hello world");
  }
}
