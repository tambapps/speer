package com.tambapps.p2p.fandem.greet;

import com.tambapps.p2p.fandem.Peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public interface PeerGreetings {

  void write(List<Peer> peers, DataOutputStream outputStream) throws IOException;

}
