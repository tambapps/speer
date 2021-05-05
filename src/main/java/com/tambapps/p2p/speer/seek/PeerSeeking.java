package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public interface PeerSeeking {

  List<Peer> read(DataInputStream inputStream) throws IOException;

}
