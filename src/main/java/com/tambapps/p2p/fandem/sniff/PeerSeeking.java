package com.tambapps.p2p.fandem.sniff;

import com.tambapps.p2p.fandem.Peer;

import java.io.DataInputStream;
import java.io.IOException;

public interface PeerSeeking {

  Peer read(DataInputStream inputStream) throws IOException;

}
