package com.tambapps.p2p.speer.seek.handshake;


// TODO remove me and SniffHandler
import com.tambapps.p2p.speer.Peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface SniffHandshake2 {

  Peer read(DataInputStream inputStream) throws IOException;

  void write(Peer peer, DataOutputStream outputStream) throws IOException;

}
