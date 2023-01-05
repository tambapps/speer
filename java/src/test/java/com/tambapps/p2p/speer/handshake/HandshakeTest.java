package com.tambapps.p2p.speer.handshake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tambapps.p2p.speer.AbstractConnectionTest;
import com.tambapps.p2p.speer.PeerConnection;
import com.tambapps.p2p.speer.io.SimpleSerializeHandler;
import com.tambapps.p2p.speer.util.DangerousConsumer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class HandshakeTest extends AbstractConnectionTest {

  private static final SimpleSerializeHandler SERIALIZE_HANDLER = new SimpleSerializeHandler();
  private static final Map<String, Object> ATTRIBUTES1;
  private static final Map<String, Object> ATTRIBUTES2;

  static {
    ATTRIBUTES1 = new HashMap<>();
    ATTRIBUTES1.put("a", 1);
    ATTRIBUTES1.put("b", "b");
    ATTRIBUTES1.put("c", 'c');
    ATTRIBUTES2 = new HashMap<>();
    ATTRIBUTES2.put("a", Long.MAX_VALUE);
    ATTRIBUTES2.put("b", true);
    ATTRIBUTES2.put("c", (short) 5);
  }

  @Test
  public void testBasicHandShake() {
    runTest(doHandshake(new SerializedHandshake<>(SERIALIZE_HANDLER, SERIALIZE_HANDLER, ATTRIBUTES1), ATTRIBUTES2),
        doHandshake(new SerializedHandshake<>(SERIALIZE_HANDLER, SERIALIZE_HANDLER, ATTRIBUTES2), ATTRIBUTES1));
  }

  private DangerousConsumer<PeerConnection> doHandshake(Handshake handshake,
      Map<String, Object> expectedAttributes) {
    return peerConnection -> {
      Object peerAttributes = handshake.apply(peerConnection);
      assertEquals(expectedAttributes, peerAttributes);
    };
  }
}
