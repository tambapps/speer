package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.io.Deserializer;
import com.tambapps.p2p.speer.io.Serializer;
import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class AttributeHandshake implements Handshake {

  private final Map<String, Object> properties;
  private final Serializer<Map<String, Object>> serializer;
  private final Deserializer<Map<String, Object>> deserializer;

  @Override
  public Object apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    serializer.serialize(properties, outputStream);
    Map<String, Object> attributes = deserializer.deserialize(inputStream);
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
