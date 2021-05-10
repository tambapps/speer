package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public abstract class AttributeReadFirstHandshake<T> extends AbstractAttributeHandshake {

  public AttributeReadFirstHandshake(Map<String, Object> properties) {
    super(properties);
  }

  @Override
  public T apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    Map<String, Object> attributes = readAttributes(inputStream);
    validate(attributes);
    T data = map(attributes);
    onAttributeRead(data);
    writeAttributes(properties, outputStream);
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
