module main

import time

fn test_connection() {
  server_endpoint := "localhost:8081"
  b := i8(4)
  short := i16(32_767)
  unsigned_short := u16(32_767)
  i := i32(0x7fffffff)
  long := i64(0x7fffffffffffffff)
  s := "hello world"

  t := spawn fn [server_endpoint] () !(i8, i16, u16, i32, i64, string) {
    println("starting socket")
    mut server := new_peer_server_ipv4(endpoint: server_endpoint)!

    assert server.address == 'localhost'
    assert server.port == 8081
    println("Accepting connection")
    mut connection := server.accept()!
    println("Accepted connection")

    actual_b := connection.read_byte()!
    actual_short := connection.read_short()!
    actual_unsigned_short := connection.read_unsigned_short()!
    actual_int := connection.read_int()!
    actual_long := connection.read_long()!
    actual_string := connection.read_string()!
    return actual_b, actual_short, actual_unsigned_short, actual_int, actual_long, actual_string
  }()

  // gives time for server to start
  time.sleep(1 * time.second)


  mut connection := new_peer_connection(endpoint: server_endpoint)!
  connection.write_byte(b)!
  connection.write_short(short)!
  connection.write_unsigned_short(unsigned_short)!
  connection.write_int(i)!
  connection.write_long(long)!
  connection.write_string(s)!

  actual_b, actual_short, actual_unsigned_short, actual_int, actual_long, actual_string := t.wait()!
  assert b == actual_b
  assert short == actual_short
  assert i == actual_int
  assert long == actual_long
  assert unsigned_short == actual_unsigned_short
  assert s == actual_string
}


fn test_client_read() ! {
  mut connection := new_peer_connection(address: "127.0.0.1", port: 8081)!
  read_test(mut connection)!
  connection.close()!
}

fn test_server_read() ! {
  mut server := new_peer_server(address: "127.0.0.1", port: 8081)!
  mut connection := server.accept()!
  read_test(mut connection)!
  connection.close()!
}

fn test_client_write() ! {
  mut connection := new_peer_connection(address: "127.0.0.1", port: 8081)!
  write_test(mut connection)!
  connection.close()!
}

fn test_server_write() ! {
  mut server := new_peer_server(address: "127.0.0.1", port: 8081)!
  mut connection := server.accept()!
  write_test(mut connection)!
  time.sleep(1 * time.second)
  connection.close()!
}

fn read_test(mut connection &PeerConnection) ! {
  assert connection.read_int()! == 0x7fffffff
  assert connection.read_short()! == 1
  assert connection.read_byte()! == 8
  assert connection.read_string()! == "hello world"
}

 fn write_test(mut connection &PeerConnection) ! {
   connection.write_int(0x7fffffff)!
   connection.write_short(1)!
   connection.write_byte(8)!
   connection.write_string('hello world')!
 }