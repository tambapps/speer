package com.tambapps.p2p.speer.seek;

import com.tambapps.p2p.speer.Peer;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public interface PeerSeeking<T extends Peer> {

  List<T> read(DataInputStream inputStream) throws IOException;

}
