package com.tambapps.p2p.speer;

import com.tambapps.p2p.speer.util.PeerUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Representation of a network access point (Ip address + port)
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Peer {

  public static final int DEFAULT_PORT = 8081;

  public static Peer of(InetSocketAddress socketAddress) {
    return of(socketAddress.getAddress(), socketAddress.getPort());
  }
  public static Peer of(String address, int port) {
    return new Peer(PeerUtils.getAddress(address), port);
  }

  public static Peer of(InetAddress address, int port) {
    return new Peer(address, port);
  }

  public static Peer parse(String peer) {
    int index = peer.indexOf(":");
    if (index <= 0) {
      throw new IllegalArgumentException(String.format("'%s' is malformed", peer));
    }
    return of(
        PeerUtils.getAddress(peer.substring(0, index)), Integer.parseInt(peer.substring(index + 1)));
  }

  public static Peer findAvailablePeer() throws IOException {
    return findAvailablePeer(PeerUtils.getPrivateNetworkIpAddress());
  }

  public static Peer findAvailablePeer(InetAddress address) {
    return findAvailablePeer(address, 50000);
  }

  public static Peer findAvailablePeer(int startLookingPort) throws IOException {
    return findAvailablePeer(PeerUtils.getPrivateNetworkIpAddress(), startLookingPort);
  }

  public static Peer findAvailablePeer(InetAddress address, int startLookingPort) {
    return of(address, PeerUtils.getAvailablePort(address, startLookingPort));
  }

  InetAddress address;
  int port;


  @Override
  public String toString() {
    return String.format("%s:%d", getAddressString(), port);
  }

  public String getAddressString() {
    return PeerUtils.toString(address);
  }

  public InetSocketAddress toSocketAddress() {
    return new InetSocketAddress(getAddress(), getPort());
  }

  public int[] ipFields() {
    String[] fields = getAddressString().split("\\.");
    int[] ipFields = new int[4];
    for (int i = 0; i < fields.length; i++) {
      ipFields[i] = Integer.parseInt(fields[i]);
    }
    return ipFields;
  }
}
