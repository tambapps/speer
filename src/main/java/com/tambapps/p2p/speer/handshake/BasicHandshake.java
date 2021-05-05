package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.util.Version;

import java.io.DataOutputStream;
import java.io.IOException;

public class BasicHandshake extends AbstractBasicHandshake {

  @Override
  protected void writeAttributes(DataOutputStream outputStream) throws IOException {
    outputStream.writeUTF(VERSION_ATTRIBUTE_KEY + ATTRIBUTE_SEPARATOR + Version.PROTOCOL_VERSION);
  }

}
