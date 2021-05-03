package com.tambapps.p2p.fandem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Peerer {

  // me as a peer
  private Peer peer;

  // TODO add a policy to tell whether accepting the accepted socket should be processed or if we
  //   should look for another
  public PeerConnection listen() throws IOException {
    if (peer == null) {
      peer = Peer.findAvailablePeer();
    }
    try (ServerSocket serverSocket = new ServerSocket(peer.getPort(), 1, peer.getIp())) {
      return new PeerConnection(peer, serverSocket.accept());
    }
  }
  public PeerConnection connect(Peer peer) throws IOException {
    return new PeerConnection(peer, new Socket(peer.getIp(), peer.getPort()));
  }

  // TODO add method with peer sniffer
}
