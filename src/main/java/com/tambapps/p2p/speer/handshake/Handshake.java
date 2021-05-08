package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.PeerConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public interface Handshake {

  Map<String, Object> apply(DataOutputStream outputStream,
      DataInputStream inputStream) throws IOException;

  default Map<String, Object> apply(PeerConnection connection) throws IOException {
    return apply(connection.getOutputStream(), connection.getInputStream());
  }

  default Map<String, Object> apply(Socket socket) throws IOException {
    return apply(new DataOutputStream(socket.getOutputStream()),
        new DataInputStream(socket.getInputStream()));
  }
}
