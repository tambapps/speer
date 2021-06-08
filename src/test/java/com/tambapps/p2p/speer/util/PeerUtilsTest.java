package com.tambapps.p2p.speer.util;

import static com.tambapps.p2p.speer.util.PeerUtils.getPrivateNetworkAddress;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

public class PeerUtilsTest {

  @Test
  public void testGetIpAddresses() throws IOException {

    InetAddress o = getPrivateNetworkAddress();
    System.out.println(o);
  }
}
