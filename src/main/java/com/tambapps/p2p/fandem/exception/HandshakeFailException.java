package com.tambapps.p2p.fandem.exception;

import java.io.IOException;

public class HandshakeFailException extends IOException {

  public HandshakeFailException(String message) {
    super(message);
  }
}
