package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public interface PeerSeeking {

  List<Peer> read(DataInputStream inputStream) throws IOException;

}
