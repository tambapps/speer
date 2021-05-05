package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.handshake.Handshake;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  private Handshake handshake;

  // TODO make listen/connect function with onPeerFound listener (lambda expression)

  // TODO add a policy to tell whether accepting the accepted socket should be processed or if we
  //   should look for another
  public PeerConnection listen() throws IOException {
    if (peer == null) {
      peer = Peer.findAvailablePeer();
    }
    try (ServerSocket serverSocket = new ServerSocket(peer.getPort(), 1, peer.getIp())) {
      return PeerConnection.from(peer, serverSocket.accept(), handshake);
    }
  }

  public PeerConnection connect(Peer peer) throws IOException {
    return PeerConnection.from(peer, new Socket(peer.getIp(), peer.getPort()), handshake);
  }

  // TODO add method with peer sniffer
}
