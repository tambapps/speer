package com.tambapps.p2p.speer.util;


import java.io.IOException;
import java.io.InputStream;

public interface Deserializer<T> {

  T deserialize(InputStream inputStream) throws IOException;

}
