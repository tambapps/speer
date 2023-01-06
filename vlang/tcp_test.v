module main

import time

fn test_connection() {

  println("starting socket")
  server_endpoint := "localhost:8081"
  mut server := new_peer_server_ipv4(endpoint: server_endpoint)!
  b := byte(4)

  assert server.address == 'localhost'
  assert server.port == 8081

  t := spawn fn [mut server] () !byte {
    println("Accepting connection")
    mut connection := server.accept()!
    println("Accepted connection")
    if b := connection.read_byte() {
      println(b)
      connection.close()!
      server.close()!
      return b
    } else {
      println("Error: ${err.str()}")
      connection.close()!
      server.close()!
      return byte(0)
    }
    return 1
  }()

  time.sleep(2 * time.millisecond)


  mut connection := new_peer_connection(endpoint: server_endpoint)!
  connection.write_byte(b)!
  //listener.accept()!

  assert b == t.wait()!
}