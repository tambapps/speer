package com.tambapps.p2p.speer.exception;

import java.io.IOException;

public class HandshakeFailException extends IOException {

  public HandshakeFailException(String message) {
    super(message);
  }
}
