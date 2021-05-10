package com.tambapps.p2p.speer.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public final class PeerUtils {

  /**
   * from https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
   *
   * @return return the ip address of the device
   * @throws IOException in case of I/O errors
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
   * @param startingPort the port from which to start looking for
   * @return an available port
   */
  public static int getAvailablePort(InetAddress inetAddress, int startingPort) {
    int port = startingPort;
    while (port < startingPort + 16 * 16) {
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

  public static InetAddress getAddress(String address) {
    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Unknown host", e);
    }
  }
}