package com.tambapps.p2p.speer.handshake;

import com.tambapps.p2p.speer.exception.HandshakeFailException;
import com.tambapps.p2p.speer.io.Deserializer;
import com.tambapps.p2p.speer.io.Serializer;
import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@AllArgsConstructor
public class SerializedHandshake<T1, T2> implements Handshake {

  private final T1 data;
  protected final Serializer<T1> serializer;
  protected final Deserializer<T2> deserializer;

  @Override
  public Object apply(DataOutputStream outputStream, DataInputStream inputStream)
      throws IOException {
    serializer.serialize(data, outputStream);
    T2 attributes = deserializer.deserialize(inputStream);
    validate(attributes);
    return map(attributes);
  }

  // overridable
  protected void validate(T2 properties) throws HandshakeFailException {
  }

  // overridable
  protected Object map(T2 properties) {
    return properties;
  }
}
