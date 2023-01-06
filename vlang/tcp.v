module main

import net

struct EndpointParams {

}
pub fn new_peer_server_ipv6(endpoint string) !&PeerServer {
	return new_peer_server(endpoint, net.AddrFamily.ip6)
}

pub fn new_peer_server_ipv4(endpoint string) !&PeerServer {
	return new_peer_server(endpoint, net.AddrFamily.ip)
}

pub fn new_peer_server(endpoint string, addrFamily net.AddrFamily) !&PeerServer {
	address,port := net.split_address(endpoint)!
	mut listener := net.listen_tcp(addrFamily, endpoint)!

	return &PeerServer {
		address: address
		port: port
		listener: listener
	}
}

pub fn new_peer_connection(endpoint string, addrFamily net.AddrFamily) !&PeerConnection {
	//net.dial_tcp()
}

struct PeerServer {
pub:
	address string
	port int
mut:
	listener &net.TcpListener

}

fn (mut self PeerServer) accept() !&PeerConnection {
	listener := self.listener.accept()!
	return &PeerConnection{connection: listener}
}
fn (mut self PeerServer) close() ! {
	self.listener.close()!
}

struct PeerConnection {
mut:
	connection net.TcpConn
}

fn (self PeerConnection) read_byte() !u8 {
	buf := []u8{}
	self.connection.read(buf)!
}

fn (self PeerConnection) write_byte(b u8) ! {
	self.connection.write([b])!
}
