package com.tambapps.p2p.fandem.sniff.handshake;


// TODO remove SniffHandshake
import com.tambapps.p2p.fandem.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public interface SniffHandshake2 {

  Peer read(DataInputStream inputStream) throws IOException;

  void write(Peer peer, DataOutputStream outputStream) throws IOException;

}
