package com.tambapps.p2p.speer.greet;

import com.tambapps.p2p.speer.Peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public interface PeerGreetings {

  void write(List<Peer> peers, DataOutputStream outputStream) throws IOException;

}
