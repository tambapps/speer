package com.tambapps.p2p.fandem.handshake;

import com.tambapps.p2p.fandem.PeerConnection;
import com.tambapps.p2p.fandem.exception.HandshakeFailException;
import com.tambapps.p2p.fandem.util.Version;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBasicHandshake implements Handshake {

  protected static final String ATTRIBUTE_SEPARATOR = "|";
  protected static final String VERSION_ATTRIBUTE_KEY = "VERSION";
  protected static final String HEADER = "SPEER_HANDSHAKE";
  protected static final String ATTRIBUTES_END = "|ATTRIBUTE_END|";

  @Override
  public final Map<String, String> apply(DataOutputStream outputStream,
      DataInputStream inputStream) throws IOException {
    outputStream.writeUTF(HEADER);
    writeAttributes(outputStream);
    outputStream.writeUTF(ATTRIBUTES_END);
    return read(inputStream);
  }

  /**
   * Reads attributes of a handshake
   * @param outputStream the output stream from which to read data
   *
   * @throws IOException in case of I/0 errors or handshake fail
   */
  protected abstract void writeAttributes(DataOutputStream outputStream) throws IOException;

  /**
   * Reads attributes of a handshake
   * @param inputStream the input stream from which to read data
   * @return a map of attributes read from the handshake
   * @throws IOException in case of I/0 errors or handshake fail
   */
  protected Map<String, String> read(DataInputStream inputStream) throws IOException {
    if (!inputStream.readUTF().equals(HEADER)) {
      throw new HandshakeFailException("Remote peer doesn't uses speer");
    }
    Map<String, String> attributes = new HashMap<>();
    String s;
    while (!(s = inputStream.readUTF()).equals(ATTRIBUTES_END)) {
      int separator = s.indexOf(ATTRIBUTE_SEPARATOR);
      if (separator < 0 || separator >= s.length() - 1) {
        throw new HandshakeFailException("Remote peer doesn't uses speer");
      }
      attributes.put(s.substring(0, separator), s.substring(separator + 1, s.length()));
    }
    verifyAttributes(attributes);
    return attributes;
  }

  protected void verifyAttributes(Map<String, String> attributes) throws IOException {
    String version = attributes.get(VERSION_ATTRIBUTE_KEY);
    if (version == null) {
      throw new HandshakeFailException("Remote peer didn't provide handshake version");
    }
    String[] fields = version.split("\\.");
    if (fields.length != 3 ||
        !fields[0].equals(Version.PROTOCOL_VERSION.split("\\.")[0])) {
      throw new HandshakeFailException(String.format("Incompatible version '%s'", version));
    }
  }
}
