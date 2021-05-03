package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.util.IPUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Peer {

    public static final int DEFAULT_PORT = 8081;

    public static Peer of(InetAddress address, int port) {
        return new Peer(address, port);
    }

    public static Peer of(Socket socket) {
        return new Peer(socket.getInetAddress(), socket.getPort());
    }

    public static Peer parse(String peer) throws UnknownHostException {
      int index = peer.indexOf(":");
      if (index <= 0) {
        throw new IllegalArgumentException(String.format("'%s' is malformed", peer));
      }
      return of(InetAddress.getByName(peer.substring(0, index)), Integer.parseInt(peer.substring(index + 1)));
    }

    public static Peer fromHexString(String hexString) throws UnknownHostException {
        if (hexString.length() != 8 && hexString.length() != 10 ||
            !hexString.chars().allMatch(c -> Character.isDigit(c)
                || c >= 'A' && c <= 'F' ||
                c >= 'a' && c <= 'f')) {
          throw new IllegalArgumentException(String.format("'%s' is malformed"));
        }
        int[] address = new int[4];
      for (int i = 0; i < address.length; i++) {
        address[i] = Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
      }
      int port = hexString.length() == 8 ? DEFAULT_PORT :
          DEFAULT_PORT + Integer.parseInt(hexString.substring(8, 10), 16);
      return of(InetAddress.getByName(Arrays.stream(address)
          .mapToObj(Integer::toString)
          .collect(Collectors.joining("."))), port);
    }

    public static Peer findAvailablePeer() throws IOException {
      return findAvailablePeer(IPUtils.getIpAddress());
    }

    public static Peer findAvailablePeer(InetAddress address) {
      return of(address, IPUtils.getAvailablePort(address));
    }

  InetAddress ip;
  int port;


  @Override
  public String toString() {
    return String.format("%s:%d", getIpString(), port);
  }


  public String toHexString() {
      String ipHex = IPUtils.toHexString(ip);
      return port == DEFAULT_PORT ? ipHex :
          IPUtils.toHexString(port - DEFAULT_PORT);
  }

  public String getIpString() {
    return IPUtils.toString(ip);
  }

  public int[] ipFields() {
    String[] fields = getIpString().split("\\.");
    int[] ipFields = new int[4];
    for (int i = 0; i < fields.length; i++) {
      ipFields[i] = Integer.parseInt(fields[i]);
    }
    return ipFields;
  }
}
