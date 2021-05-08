package com.tambapps.p2p.speer.greet;

import com.tambapps.p2p.speer.Peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public interface PeerGreetings<T extends Peer> {

  void write(List<T> peers, DataOutputStream outputStream) throws IOException;

}
