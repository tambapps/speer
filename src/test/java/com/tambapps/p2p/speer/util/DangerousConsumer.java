package com.tambapps.p2p.speer.util;

import java.io.IOException;

public interface DangerousConsumer<T> {

  void accept(T t) throws IOException;

}
