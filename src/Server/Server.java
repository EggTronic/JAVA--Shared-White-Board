package Server;
import java.awt.Color;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;

import Shape.*;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.json.simple.JSONObject;

import PublishSubscribeSystem.PublishSubscribeSystem;
import PublishSubscribeSystem.ClientInfo;


public class Server implements Runnable {

//    private static  ArrayList<Socket> connectedClient = new ArrayList<>();
    private String roomowner;
    private  String hostname = "localhost";
    private int portnumber = 8002;
    private ServerSocket listeningSocket;
    private static int poolLimited = 20;


    public Server(int portnumber,String hostname) throws IOException {
        try{
            this.hostname = hostname;
            this.portnumber = portnumber;
            this.listeningSocket = new ServerSocket(portnumber);
        }
        catch (IOException ex){
            throw new IOException("problem with Server Creating");
        }


    }

    public void run() {

        // to test the port number
//        try {
//            if (args.length == 1) {
//                portnumber = Integer.parseInt(args[0]);
//            } else if (args.length == 0) {
//
//                System.out.println("using default hostname and portnumber = 8002");
//
//            } else {
//                .println("the default hostname and portnumber is used");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



        PublishSubscribeSystem.getInstance().registerServer(listeningSocket);


        ExecutorService threadpool_receive = Executors.newFixedThreadPool(poolLimited);
        int clientnumber = 0;
//



        try {

//
//
//
            while (true) {
                System.out.println("Server listening on port " + portnumber + " for a connection");
          	    ServerUI.messageAppender.appendToMessagePane(ServerUI.logPane, ServerUI.dtf.format(LocalDateTime.now()) + " | ", Color.WHITE, true);
          	    ServerUI.messageAppender.appendToMessagePane(ServerUI.logPane, "Server listening on port " + portnumber + " for a connection" + "\n\n", Color.WHITE, true);
                
                //Accept an incoming client connection request
                Socket clientsocket = listeningSocket.accept(); //This method will block until a connection request is received
                
                System.out.println("someone wants to share your whiteboard");
                ServerUI.messageAppender.appendToMessagePane(ServerUI.logPane, ServerUI.dtf.format(LocalDateTime.now()) + " | ", Color.WHITE, true);
          	    ServerUI.messageAppender.appendToMessagePane(ServerUI.logPane, "Someone wants to share your whiteboard" + "\n\n", Color.WHITE, true);
              
//                connectedClient.add(clientsocket);
//                for(Socket client : connectedClient){
//                    if(!client.isClosed())
//                        System.out.println(client.toString());
//                }
                clientnumber++;

//                Client_thread client = new Client_thread(clientsocket, clientnumber);
                Client_thread client = new Client_thread(clientsocket,clientnumber);

                Thread t = new Thread(client);

                threadpool_receive.execute(t);// use this thread to receive the update from the client

//                System.out.println("running");

            }
        }



        catch (SocketException ex)
        {

            if(!(listeningSocket.isClosed()))
            {
                try
                {
                    listeningSocket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            System.out.println("The server is gone");
            ServerUI.messageAppender.appendToMessagePane(ServerUI.logPane, ServerUI.dtf.format(LocalDateTime.now()) + " | ", Color.WHITE, true);
      	    ServerUI.messageAppender.appendToMessagePane(ServerUI.logPane, "The server is gone" + "\n\n", Color.WHITE, true);
        }
		catch (IOException e)
        {
            e.printStackTrace();

        }
		finally
        {
            try {
            ConcurrentHashMap<String, Socket> connectedClient = PublishSubscribeSystem.getInstance().getUsermap();

            if(!connectedClient.isEmpty()) {

                for (Map.Entry<String, Socket> eachUser : connectedClient.entrySet()) {
                    Socket socket = (Socket) eachUser.getValue();
                    String username = (String) eachUser.getKey();

                    if (!socket.isClosed()) {
                        OutputStream out = socket.getOutputStream();
                        OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
                        oos.write("Manager leaving , session closed");
                        oos.flush();
                    }
                }
            }


            LinkedBlockingQueue<ClientInfo> queue = PublishSubscribeSystem.getInstance().getQueue();

            if(!queue.isEmpty()) {
                Iterator<ClientInfo> listOfClients = queue.iterator();
                while (listOfClients.hasNext()) {
                    ClientInfo current = listOfClients.next();
                    Socket socket = current.getClient();
                    if (!socket.isClosed()) {
                        OutputStream out = socket.getOutputStream();
                        OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
                        oos.write("Manager leaving , session closed");
                        oos.flush();

                    }


                }
            }

                threadpool_receive.shutdown();
                listeningSocket.close();


            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

        }


        }


//    static synchronized ArrayList<Socket> getConnectedClient(){
//        return (ArrayList<Socket>)connectedClient.clone();
//    }

//    static synchronized ArrayList<MyShape> getShapes(){
//        return (ArrayList<MyShape>) shapes.clone();
//    }
//
//    static synchronized void  updateShapes(ArrayList<MyShape> source){
//        shapes = source;
//    }
//
//    static synchronized void updateTexts(ArrayList<MyText> source){
//        texts = source;
//    }
//
//    static synchronized void removeShape(MyShape shape){
//        shapes.remove(shape);
//    }
//
//    static synchronized void removeText(MyText text){
//        texts.remove(text);
//    }
//
//    static synchronized void addShape(MyShape shape){
//        shapes.add(shape);
//    }
//
//    static synchronized void addText(MyText text){
//        texts.add(text);
//    }
//
//
//   static synchronized ArrayList<MyText> getTexts(){
//        return (ArrayList<MyText>) texts.clone();
//    }





//    static synchronized void broadcast(Object item) throws IOException {
//
//        String shapestr = Base64.getEncoder().encodeToString(serialize(item));
//
//        JSONObject reply = new JSONObject();
//
//        reply.put("Source", "Server");
//        reply.put("Goal", "Info");
//        reply.put("ObjectString", shapestr);
//        reply.put("Class", item.getClass().getName());
//
//        ConcurrentHashMap<String,Socket> connectedClient = PublishSubscribeSystem.getInstance().getUsermap();
//
//        for(Map.Entry<String,Socket> eachUser : connectedClient.entrySet())
//
//        {   Socket socket = (Socket) eachUser.getValue();
//            String username = (String) eachUser.getKey();
//
//            if(!socket.isClosed()) {
//                OutputStream out = socket.getOutputStream();
//                OutputStreamWriter oos =new OutputStreamWriter(out, "UTF8");
//                oos.write(reply.toJSONString()+"\n");
//                oos.flush();
//            }
//            else
//                PublishSubscribeSystem.getInstance().deregisterClient(username);
//        }
//
//        System.out.println("done");
//
//    }



    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bao);
        os.writeObject(obj);
        return bao.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }



}
