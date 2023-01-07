package com.tambapps.p2p.speer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class PeererTest extends AbstractConnectionTest {

  @Test
  public void testConnect() {
    runTest(connection -> {
        },
        connection -> {
        });
  }

  @Test
  public void testReadString() {
    runTest(connection -> {
      connection.writeString("hello world");
        },
        connection -> {
      assertEquals("hello world", connection.readString());
        });
  }

  @Test
  public void testConnectReadStream() {
    final byte[] bytes = new byte[] {1, 2, 3, 4};
    runTest(connection -> {
      connection.write(bytes);
      connection.writeUTF("youhou");
        },
        connection -> {
      byte[] actualBytes = new byte[bytes.length];
      InputStream inputStream = connection.inputStream(bytes.length);
      assertEquals(bytes.length, inputStream.read(actualBytes));
      // stream should be finished
      assertEquals(-1, inputStream.read());
      assertArrayEquals(bytes, actualBytes);
      // closing this stream should not affect socket's input stream
      inputStream.close();
      assertEquals("youhou", connection.readUTF());
    });
  }

}
