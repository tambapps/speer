module main

import net

struct Peer {
pub:
	address string
	port int
}
struct PeerServer {
pub:
	Peer
mut:
	listener &net.TcpListener

}
struct PeerConnection {
pub:
	Peer
mut:
	connection &net.TcpConn
}

[params]
struct EndpointParams {
	endpoint string
	address string
	port int
	family net.AddrFamily = net.AddrFamily.ip
}
pub fn new_peer_server_ipv6(params EndpointParams) !&PeerServer {
	endpoint_params := EndpointParams{
		...params
		family: net.AddrFamily.ip6
	}
	return new_peer_server(endpoint_params)
}

pub fn new_peer_server_ipv4(params EndpointParams) !&PeerServer {
	endpoint_params := EndpointParams{
		...params
		family: net.AddrFamily.ip6
	}
	return new_peer_server(endpoint_params)
}

pub fn new_peer_server(endpoint_params EndpointParams) !&PeerServer {
	endpoint_address, address,port := endpoint_params.get_endpoint_address()!
	mut listener := net.listen_tcp(endpoint_params.family, endpoint_address)!

	return &PeerServer {
		address: address
		port: port
		listener: listener
	}
}

pub fn new_peer_connection(endpoint_params EndpointParams) !&PeerConnection {
	endpoint_address, address,port := endpoint_params.get_endpoint_address()!
	connection := net.dial_tcp(endpoint_address)!
	return &PeerConnection {
		address: address
		port: port
		connection: connection
	}
}

fn (self &EndpointParams) get_endpoint_address() !(string, string, int) {
	endpoint_address := if self.endpoint.len > 0 {
		 self.endpoint
	} else if self.address.len > 0 {
		'${self.address}:${self.port}'
	} else {
		error('You must set endpoint, or address and port')
		''
	}
	address,port := net.split_address(endpoint_address)!
	return endpoint_address, address, port
}

fn (mut self PeerServer) accept() !&PeerConnection {
	conn := self.listener.accept()!
	// TODO add get self peer and get remote peer properly
	return &PeerConnection{connection: conn}
}
fn (mut self PeerServer) close() ! {
	self.listener.close()!
}
fn (mut self PeerConnection) close() ! {
	self.connection.close()!
}

fn (self &PeerConnection) read_byte() !byte {
	mut buf := []byte{len: 1}
	self.connection.read(mut &buf)!
	return buf[0]
}

fn (mut self PeerConnection) write_byte(b byte) ! {
	self.connection.write([b])!
}

fn (self &PeerConnection) read_short() !u16 {
	mut buf := []byte{len: 2}
	self.connection.read(mut &buf)!
	// short val=(short)(((hi & 0xFF) << 8) | (lo & 0xFF));
	return u16(((u16(buf[0]) & 0xff) << 8) | (buf[1] & 0xFF))
}

fn (mut self PeerConnection) write_short(b u16) ! {
	self.connection.write([u8(b >>> 8), u8(b >>> 0)])!
}

fn (mut self PeerConnection) read_line() !string {
	return self.connection.read_line()
}

fn (mut self PeerConnection) write_line(s string) ! {
	self.connection.write_string(s)!
}
