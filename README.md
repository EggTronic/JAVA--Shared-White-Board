# COMP90015-Shared-White-Board
### Run client
```
java -jar client.jar
```
### Run server
```
java -jar server.jar
```

### Simple introduction
- Allow multiple users draw on the same white board online.
- Allow multiple users chat online.
- Normal users can:
  - draw (toggle color, shape, fill, text, rubber, size,)
  - send chat message
  - enter an existing board (enter ip address, port and username)
  - leave board
  - save board graph
  - download board graph as byte code
- Board owner can:
  - can do what all normal users can do
  - kick out a normal user
  - load board from byte code
  - clean board
  - recall an operation
- Technology
  - java swing application
  - socket TCP
  - multi-thread thread pool
  - synchronized
  - event queue


  
