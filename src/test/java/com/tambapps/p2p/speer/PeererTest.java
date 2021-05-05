package com.tambapps.p2p.speer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class PeererTest extends AbstractConnectionTest {

  @Test
  public void testConnect() {
    runTest(connection -> {
        },
        connection -> {
        });
  }

  @Test
  public void testFileTransfer() throws Exception {
    File temp = Files.createTempFile("temp", ".txt").toFile();
    temp.deleteOnExit();

    File file = new File(URLDecoder.decode(
        PeererTest.class.getResource("/file.txt").getFile(), StandardCharsets.UTF_8.name()));
    runTest(connection -> connection.sendFile(file),
        connection -> connection.receiveFile(temp));

    PeererTest.class.getResource("/file.txt").openStream();
    assertTrue(contentEquals(temp, file));
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
