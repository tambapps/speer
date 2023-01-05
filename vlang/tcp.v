module main

import net

fn new_peer_server(addr string) !PeerServer {
	if mut listener := net.listen_tcp(addr) {
		return PeerServer {
			listener: listener
		}
	} else {
		return err
	}
}
struct PeerServer {
mut:
	listener &net.TcpListener

}

fn (self &PeerServer) accept() !PeerConnection {
	return self.listener.
}
fn (self &PeerServer) close() ! {
	self.listener.close()!
}
struct PeerConnection {

}

