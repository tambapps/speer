module main

import time

fn test_connection() {

  println("starting socket")
  server_endpoint := "localhost:8081"
  mut server := new_peer_server_ipv4(endpoint: server_endpoint)!
  b := byte(4)

  assert server.address == 'localhost'
  assert server.port == 8081

  t := spawn fn [mut server] () ! {
    mut connection := server.accept()!
    println(connection.read_byte()!)
    connection.close()!
    server.close()!
  }()

  time.sleep(2 * time.millisecond)


  mut connection := new_peer_connection(endpoint: server_endpoint)!
  connection.write_byte(b)!
  //listener.accept()!

  t.wait()!
}