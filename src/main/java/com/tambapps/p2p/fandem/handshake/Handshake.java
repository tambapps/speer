package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.PeerConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public interface Handshake {

  Map<String, String> apply(DataOutputStream outputStream,
      DataInputStream inputStream) throws IOException;

  default Map<String, String> apply(PeerConnection connection) throws IOException {
    return apply(connection.getOutputStream(), connection.getInputStream());
  }
}
