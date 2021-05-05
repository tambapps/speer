package com.tambapps.p2p.speer.util;

import java.io.File;
import java.io.IOException;

public interface FileProvider {

  File newFile(String name) throws IOException;

}
