module main

import time

fn test_connection() {

  println("starting socket")
  mut server := new_peer_server_ipv4("localhost:8081")!

  assert server.address == 'localhost'
  assert server.port == 8081

  t := spawn fn [mut server] () {
    connection := server.accept()
    time.sleep(2 * time.millisecond)
    println(connection.read_byte()!)
  }()


  //listener.accept()!

  t.wait()
}