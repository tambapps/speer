module main

import time

fn test_connection() {
  server_endpoint := "localhost:8081"
  b := byte(4)

  t := spawn fn [server_endpoint] () !byte {
    println("starting socket")
    mut server := new_peer_server_ipv4(endpoint: server_endpoint)!

    assert server.address == 'localhost'
    assert server.port == 8081
    println("Accepting connection")
    mut connection := server.accept()!
    println("Accepted connection")

    actual_b := connection.read_byte()!
    return actual_b
  }()

  // gives time for server to start
  time.sleep(1 * time.second)


  mut connection := new_peer_connection(endpoint: server_endpoint)!
  connection.write_byte(b)!

  assert b == t.wait()!
}