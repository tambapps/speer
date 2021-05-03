package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.AbstractConnectionTest;
import org.junit.jupiter.api.Test;

public class HandshakeTest extends AbstractConnectionTest {

  @Test
  public void testBasicHandShake() {
    Handshake handshake = new BasicHandshake();
    runTest(handshake::apply,
        handshake::apply);
  }

}
