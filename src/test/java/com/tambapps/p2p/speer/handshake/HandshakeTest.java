package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.AbstractConnectionTest;
import org.junit.jupiter.api.Test;

public class HandshakeTest extends AbstractConnectionTest {

  @Test
  public void testBasicHandShake() {
    Handshake handshake = new BasicHandshake();
    runTest(handshake::apply,
        handshake::apply);
  }

}
