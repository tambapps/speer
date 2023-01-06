module main

import time

fn test_connection() {
  server_endpoint := "localhost:8081"
  b := i8(4)
  short := i16(32_767)

  t := spawn fn [server_endpoint] () !(i8, i16) {
    println("starting socket")
    mut server := new_peer_server_ipv4(endpoint: server_endpoint)!

    assert server.address == 'localhost'
    assert server.port == 8081
    println("Accepting connection")
    mut connection := server.accept()!
    println("Accepted connection")

    actual_b := connection.read_byte()!
    actual_short := connection.read_short()!
    return actual_b, actual_short
  }()

  // gives time for server to start
  time.sleep(1 * time.second)


  mut connection := new_peer_connection(endpoint: server_endpoint)!
  connection.write_byte(b)!
  connection.write_short(short)!

  actual_b, actual_short := t.wait()!
  assert b == actual_b
  assert short == actual_short
}