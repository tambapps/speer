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
public abstract class AttributeReadFirstHandshake<T> implements Handshake {

  private final Map<String, Object> properties;
  private final Serializer<Map<String, Object>> serializer;
  private final Deserializer<Map<String, Object>> deserializer;

  @Override
  public T apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    Map<String, Object> attributes = deserializer.deserialize(inputStream);
    validate(attributes);
    T data = map(attributes);
    onAttributeRead(data);
    serializer.serialize(properties, outputStream);
    return data;
  }

  // overridable
  private void onAttributeRead(T data) {

  }

  // overridable
  protected void validate(Map<String, Object> properties) throws HandshakeFailException {
  }

  protected abstract T map(Map<String, Object> properties);
}
