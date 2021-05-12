package com.tambapps.p2p.speer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Serializer<T> {

  void serialize(T object, OutputStream outputStream) throws IOException;

  default byte[] serializeToBytes(T object) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      serialize(object, outputStream);
      outputStream.flush();
      return outputStream.toByteArray();
    }
  }
}
