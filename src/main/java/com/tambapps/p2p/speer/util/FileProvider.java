package com.tambapps.p2p.speer.util;

import java.io.File;
import java.io.IOException;

// TODO remove me. Too specific to fandem
public interface FileProvider {

  File newFile(String name) throws IOException;

}
