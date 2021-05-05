package com.tambapps.p2p.speer.util;

public interface DangerousConsumer<T> {

  void accept(T t) throws Exception;

}
