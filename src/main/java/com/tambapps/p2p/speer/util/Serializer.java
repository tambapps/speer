package com.tambapps.p2p.speer.util;

import java.io.IOException;

public interface Serializer<T> {

  byte[] serialize(T object) throws IOException;

}
