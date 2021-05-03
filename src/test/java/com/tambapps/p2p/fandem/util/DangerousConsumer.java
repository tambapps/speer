package com.tambapps.p2p.fandem.util;

public interface DangerousConsumer<T> {

  void accept(T t) throws Exception;

}
