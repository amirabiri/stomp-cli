# STOMP CLI

`stomp` is a lightweight command line development tool for interacting with STOMP servers or clients. It is written in
Kotlin and requires an installation of the Java Runtime Environment (JRE) to run (version 17+).

```console
$ stomp --server ws://localhost:8080/
CONN - Listening
CONN - Client connected: 127.0.0.1:64001
SEND - /foo : {"message": "Hello World!"}
```

```console
$ stomp --client ws://localhost:8080/
CONN - Connected
RECV - /foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}'
```

### Multiple Endpoints

Multiple `--server` options can be specified to set up multiple listening STOMP servers, and multiple `--client` options
can be specified to connect to multiple STOMP servers.

```console
$ stomp --server ws://localhost:8080/ --server ws://localhost:8081/
CONN [ws://localhost:8080/] - Listening
CONN [ws://localhost:8081/] - Listening
CONN [ws://localhost:8080/] - Client connected: 127.0.0.1:64033
CONN [ws://localhost:8081/] - Client connected: 127.0.0.1:64034
SEND [ws://localhost:8080/] - /foo : {"message": "Hello World!"}
```

```console
$ stomp --client ws://localhost:8080/ --client ws://localhost:8081/
CONN [ws://localhost:8081/] - Connected
CONN [ws://localhost:8080/] - Connected
RECV [ws://localhost:8080/] - /foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}' 
```

### Named Endpoints

Both `--server` and `--client` endpoints can be named using a name prefix followed by a colon.

```console
$ stomp --server stomp1:ws://localhost:8080/ --server stomp2:ws://localhost:8081/
CONN [stomp1] - Listening
CONN [stomp2] - Listening
CONN [stomp1] - Client connected: 127.0.0.1:64330
CONN [stomp2] - Client connected: 127.0.0.1:64329
SEND [stomp1] - /foo : {"message": "Hello World!"}
```

```console
$ stomp --client stomp1:ws://localhost:8080/ --client stomp2:ws://localhost:8081/
CONN [stomp1] - Connected
CONN [stomp2] - Connected
RECV [stomp1] - /foo : {"message": "Hello World!"}
```

### Sending Messages

For `--server`s, messages can be sent using a simple HTTP interface - anything sent as an HTTP POST will be interpreted
as a message to broadcast with the URL path being the STOMP destination, and the body being the STOMP message payload.

```console
# STOMP SEND to destination /foo with payload {"message": "Hello World!"}
$ curl -X POST http://localhost:8080/foo --json '{"message": "Hello World!"}' 
```

For `--client` endpoints an HTTP interface can be created for sending messages the same way as with `--server`s using
the `--http-port` option. However, for `--client` endpoints the HTTP interface will not be created by default.

```console
$ stomp --client ws://localhost:8080/ --http-port 9090
CONN - Connected
SEND - /foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:9090/foo --json '{"message": "Hello World!"}' 
```

### Limiting Messages By Endpoint Name

By default, messages will be sent to all `--client` endpoints. To limit the message to a specific endpoint, the
endpoint name can be used as a prefix in the HTTP url.

```console
$ stomp --client stomp1:ws://localhost:8080/ --client stomp2:ws://localhost:8081/ --http-port 9090
CONN [stomp1] - Connected
CONN [stomp2] - Connected
SEND [stomp1] - /foo : {"message": "Hello World!"}
```

```console
$ curl -X POST http://localhost:9090/stomp1:foo --json '{"message": "Hello World!"}' 
```
