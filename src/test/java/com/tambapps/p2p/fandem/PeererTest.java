package com.tambapps.p2p.fandem;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PeererTest {

  private final ExecutorCompletionService<Void> executor = new ExecutorCompletionService<>(
      Executors.newFixedThreadPool(2)
  );
  private static File file;

  @BeforeAll
  public static void init() {
    file = new File(PeererTest.class.getResource("/file.txt").getFile());
  }

  @Test
  public void testConnect() throws Exception {
    Peer peer = Peer.findAvailablePeer();
    System.out.println("Peer " + peer);
    Peerer server = new Peerer(peer);
    executor.submit(() -> {
      try (PeerConnection connection = server.listen()) {
        System.out.println("Server: found connection " + connection);
      }
      return null;
    });

    Peerer client = new Peerer();

    try (PeerConnection connection = client.connect(peer)) {
      System.out.println("Client: found connection " + connection);
    }

    executor.poll(4, TimeUnit.SECONDS);
  }

  @Test
  public void testFileTransfer() throws Exception {
    Peer peer = Peer.findAvailablePeer();
    System.out.println("Peer " + peer);
    Peerer server = new Peerer(peer);
    executor.submit(() -> {
      try (PeerConnection connection = server.listen()) {
        System.out.println("Server: found connection " + connection);
        connection.sendFile(file);
      }
      return null;
    });

    Peerer client = new Peerer();

    File temp = Files.createTempFile("temp", ".txt").toFile();
    temp.deleteOnExit();
    try (PeerConnection connection = client.connect(peer)) {
      System.out.println("Client: found connection " + connection);
      connection.receiveFile(temp);
    }

    assertTrue(contentEquals(temp, file));

    executor.poll(4, TimeUnit.SECONDS);
  }

  private boolean contentEquals(File f1, File f2) throws IOException {
    FileInputStream is1 = new FileInputStream(f1);
    FileInputStream is2 = new FileInputStream(f2);
    assertTrue(f1.length() > 0);
    assertTrue(f2.length() > 0);
    int EOF = -1;
    int i1 = is1.read();
    while (i1 != EOF) {
      int i2 = is2.read();
      if (i2 != i1) {
        return false;
      }
      i1 = is1.read();
    }
    return is2.read() == EOF;
  }
}
