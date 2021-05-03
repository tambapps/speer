package com.tambapps.p2p.fandem.util;

import com.tambapps.p2p.fandem.Peer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public final class IPUtils {

  /**
   * from https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
   *
   * @return return the ip address of the device
   */
  public static InetAddress getIpAddress() throws IOException {
    ArrayList<NetworkInterface> interfaces =
        Collections.list(NetworkInterface.getNetworkInterfaces());
    return interfaces.stream()
        .filter(i -> !i.getName().contains("docker"))
        .flatMap(i -> Collections.list(i.getInetAddresses()).stream())
        .filter(addr -> !addr.isLoopbackAddress() &&
            // ipV4
            !addr.getHostAddress().contains(":"))
        .findFirst()
        .orElseThrow(() -> new IOException("Couldn't find IP"));
  }

  public static InetAddress getIpAddressOrNull() {
    try {
      return getIpAddress();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Get an available port
   *
   * @param inetAddress the address of the host
   * @return an available port
   */
  public static int getAvailablePort(InetAddress inetAddress) {
    int port = Peer.DEFAULT_PORT;
    while (port < Peer.DEFAULT_PORT + 16 * 16) {
      try (ServerSocket serverSocket = new ServerSocket(port, 0, inetAddress)) {
      } catch (IOException e) {
        port++;
        continue;
      }
      return port;
    }
    throw new IllegalStateException("No available port was found");
  }

  /**
   * Returns a well formatted string of the given ip
   *
   * @param address the address
   * @return a well formatted string of the given ip
   */
  public static String toString(InetAddress address) {
    return address.getHostAddress().replace("/", "");
  }

  /**
   * Returns the hex string of the given ip
   *
   * @param address the address
   * @return the hex string of the given ip
   */
  public static String toHexString(InetAddress address) {
    return Arrays.stream(toString(address).split("\\."))
        .map(IPUtils::toHexString)
        .collect(Collectors.joining());
  }

  public static String toHexString(int i) {
    return toHexString(Integer.toString(i, 16));
  }

  public static String toHexString(String n) {
    n = n.toUpperCase();
    return n.length() > 1 ? n : "0" + n;
  }


}