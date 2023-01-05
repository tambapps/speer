package com.tambapps.p2p.speer.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PeerUtils {

  /**
   *
   * @return return the ip address of the device
   * @throws IOException in case of I/O errors
   */
  public static InetAddress getPrivateNetworkIpAddress() throws IOException {
    List<InetAddress> ipAddresses = getIpAddresses();
    if (ipAddresses.isEmpty()) {
      throw new IOException("No IP address were found");
    }
    if (ipAddresses.size() == 1) {
      return ipAddresses.get(0);
    }
    ipAddresses.sort(PeerUtils::compare);
    return ipAddresses.get(0);
  }

  private static int compare(InetAddress address1, InetAddress address2) {
    if (is16BlockPrivateAddress(address1)) {
      return -1;
    } else if (is16BlockPrivateAddress(address2)) {
      return 1;
    }
    if (is24BlockPrivateAddress(address1)) {
      return -1;
    } else if (is24BlockPrivateAddress(address2)) {
      return 1;
    }
    if (is20BlockPrivateAddress(address1)) {
      return -1;
    } else if (is20BlockPrivateAddress(address2)) {
      return 1;
    }
    return address1.getHostName().compareTo(address1.getHostName());
  }

  public static boolean is16BlockPrivateAddress(InetAddress address) {
    return isInRange(address, "192.168.0.0", "192.168.255.255");
  }

  public static boolean is24BlockPrivateAddress(InetAddress address) {
    return isInRange(address, "10.0.0.0", "10.255.255.255");
  }

  public static boolean is20BlockPrivateAddress(InetAddress address) {
    return isInRange(address, "172.16.0.0", "172.31.255.255");
  }

  public static boolean isInRange(InetAddress address, String lowestAddress,
      String highestAddress) {
    return isInRange(address, getAddress(lowestAddress), getAddress(highestAddress));
  }

  public static boolean isInRange(InetAddress address, InetAddress lowestAddress,
      InetAddress highestAddress) {
    long ipLo = ipToLong(lowestAddress);
    long ipHi = ipToLong(highestAddress);
    long ipToTest = ipToLong(address);
    return ipToTest >= ipLo && ipToTest <= ipHi;
  }

  public static long ipToLong(InetAddress ip) {
    byte[] octets = ip.getAddress();
    long result = 0;
    for (byte octet : octets) {
      result <<= 8;
      result |= octet & 0xff;
    }
    return result;
  }

  /**
   * from https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
   *
   * @return return the ip address of the device
   * @throws IOException in case of I/O errors
   */
  public static List<InetAddress> getIpAddresses() throws IOException {
    ArrayList<NetworkInterface> interfaces =
        Collections.list(NetworkInterface.getNetworkInterfaces());
    return interfaces.stream()

        .flatMap(i -> Collections.list(i.getInetAddresses()).stream())
        .filter(addr -> !addr.isLoopbackAddress() &&
            // ipV4
            !addr.getHostAddress().contains(":"))
        .collect(Collectors.toList());
  }

  public static InetAddress getPrivateNetworkIpAddressOrNull() {
    try {
      return getPrivateNetworkIpAddress();
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

  public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
    List<InetAddress> broadcastList = new ArrayList<>();
    Enumeration<NetworkInterface> interfaces
        = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      NetworkInterface networkInterface = interfaces.nextElement();

      if (networkInterface.isLoopback() || !networkInterface.isUp()) {
        continue;
      }

      networkInterface.getInterfaceAddresses().stream()
          .map(InterfaceAddress::getBroadcast)
          .filter(Objects::nonNull)
          .forEach(broadcastList::add);
    }
    return broadcastList;
  }
}