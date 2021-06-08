package com.tambapps.p2p.speer.util;

import static com.tambapps.p2p.speer.util.PeerUtils.getPrivateNetworkIpAddress;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

public class PeerUtilsTest {

  @Test
  public void testGetIpAddresses() throws IOException {

    InetAddress o = getPrivateNetworkIpAddress();
    System.out.println(o);
  }
}
