package com.tambapps.p2p.speer.io;


import java.io.IOException;
import java.io.InputStream;

public interface Deserializer<T> {

  T deserialize(InputStream inputStream) throws IOException;

}
