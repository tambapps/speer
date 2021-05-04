package com.tambapps.p2p.fandem.sniff.handshake;

// will try to connect to peers provided by SniffingStrategy (TODO remove the genric type from
//   SniffingStrategy and put it here)


import com.tambapps.p2p.fandem.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A Sniff Handshake allows too sniff peers to communicate to each other where the connection
 * should be made
 */
public interface SniffHandshake {

  // TODO make it return a list of peers
  /**
   * Apply a sniff handshake.
   * @param outputStream
   * @param inputStream
   * @return
   * @throws IOException
   */
  Peer apply(DataOutputStream outputStream,
      DataInputStream inputStream) throws IOException;

  default Peer apply(Socket socket) throws IOException {
    return apply(new DataOutputStream(socket.getOutputStream()),
        new DataInputStream(socket.getInputStream()));
  }
}
