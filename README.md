# Speer - a P2P communication library

Speer is a library used to perform P2P communication easier. It uses the plain Java
Socket API to allow communicating between two devices.

You can consult the [wiki](https://github.com/tambapps/speer/wiki) for a detailed documentation

## Use cases
Take a look at [Fandem](https://github.com/tambapps/P2P-File-Sharing) a P2P file sharing app.
This project uses speer to send/receive files between devices in P2P, and also to discover Fandem peers with UDP.


## Examples

Server-side

```groovy
import com.tambapps.p2p.speer.PeerServer

try (PeerServer server = peerServer();
     PeerConnection connection = server.accept()) {
  connection.writeUTF("Hello! do you want to talk to me?");
  if ("yes".equals(connection.readUTF())) {
    talk(connection);
  }
}
```

Client-side

```groovy
try (PeerConnection connection = PeerConnection.from(peer)) {
  connection.readUTF()
  connection.writeUTF("yes")
  talk(connection)
}
```
