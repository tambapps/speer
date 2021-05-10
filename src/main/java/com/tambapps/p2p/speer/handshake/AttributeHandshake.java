package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class AttributeHandshake extends AbstractAttributeHandshake {

  public AttributeHandshake(Map<String, Object> properties) {
    super(properties);
  }

  @Override
  public Object apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    writeAttributes(properties, outputStream);
    Map<String, Object> attributes = readAttributes(inputStream);
    validate(attributes);
    return map(attributes);
  }

  // overridable
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
  }

  // overridable
  protected Object map(Map<String, Object> properties) {
    return properties;
  }
}
