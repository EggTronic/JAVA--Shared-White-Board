package Server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Shape.*;

import org.json.simple.JSONObject;


public class Server {

    private static  ArrayList<Socket> connectedClient = new ArrayList<>();
    private static ArrayList<MyShape> shapes = new ArrayList<>();
    private static ArrayList<MyText> texts = new ArrayList<>();
    private String roomowner;
    private static String hostname = "localhost";
    private static int portnumber = 8002;

    public static void main(String[] args) throws Exception {

        // to test the port number
        try {
            if (args.length == 1) {
                portnumber = Integer.parseInt(args[0]);
            } else if (args.length == 0) {

                System.out.println("using default hostname and portnumber = 8002");

            } else {
                System.out.println("the default hostname and portnumber is used");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerSocket listeningSocket = new ServerSocket(portnumber);
        ExecutorService threadpool_receive = Executors.newCachedThreadPool();
        int clientnumber = 0;


        try {
//
//
//
//                updateGraphs updateGraphs = new updateGraphs(); // this thread will get the latest Myshape and MyText List and broadcast to all the connected clients
//                Thread t = new Thread(updateGraphs);
//                t.start();
            while (true) {
                System.out.println("Server listening on port " + portnumber + " for a connection");
                //Accept an incoming client connection request
                Socket clientsocket = listeningSocket.accept(); //This method will block until a connection request is received
                System.out.println("someone wants to share your whiteboard");
                connectedClient.add(clientsocket);
                for(Socket client : connectedClient){
                    if(!client.isClosed())
                        System.out.println(client.toString());
                }
                clientnumber++;

                Client_thread client = new Client_thread(clientsocket, clientnumber);

                Thread t = new Thread(client);

                threadpool_receive.execute(t);// use this thread to receive the update from the client

                System.out.println("running");

                ServerParameters.getClientstate().clientConnected(client);
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
            System.out.println("the client is gone");
        }
		catch (IOException e)
        {
            e.printStackTrace();

        }
		finally
        {      try
                {
                    for(Socket connectedClient1 : connectedClient)
                    {   if(!connectedClient1.isClosed()) {
                        OutputStream out = connectedClient1.getOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(out);
                        oos.writeUTF("Manager leaving , session closed");
                        }
                        else
                            connectedClient.remove(connectedClient1);
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


    static synchronized ArrayList<Socket> getConnectedClient(){
        return (ArrayList<Socket>)connectedClient.clone();
    }

    static synchronized ArrayList<MyShape> getShapes(){
        return (ArrayList<MyShape>) shapes.clone();
    }

    static synchronized void  updateShapes(ArrayList<MyShape> source){
        shapes = source;
    }

    static synchronized void updateTexts(ArrayList<MyText> source){
        texts = source;
    }

    static synchronized void removeShape(MyShape shape){
        shapes.remove(shape);
    }

    static synchronized void removeText(MyText text){
        texts.remove(text);
    }

    static synchronized void addShape(MyShape shape){
        shapes.add(shape);
    }

    static synchronized void addText(MyText text){
        texts.add(text);
    }


   static synchronized ArrayList<MyText> getTexts(){
        return (ArrayList<MyText>) texts.clone();
    }





    static synchronized void broadcast(MyShape item) throws IOException {

        String shapestr = Base64.getEncoder().encodeToString(serialize(item));

        JSONObject reply = new JSONObject();

        reply.put("Source", "Server");
        reply.put("Goal", "Info");
        reply.put("ObjectString", shapestr);
        reply.put("Class", item.getClass().getName());



        for (Socket connectedClient : connectedClient) {
            OutputStream out = connectedClient.getOutputStream();
            OutputStreamWriter oos =new OutputStreamWriter(out, "UTF8");
            oos.write(reply.toJSONString()+"\n");
            oos.flush();
        }

        System.out.println("done");

    }

    static synchronized void broadcast(MyText item) throws IOException {

        String str = Base64.getEncoder().encodeToString(serialize(item));

        JSONObject reply = new JSONObject();

        reply.put("Source", "Server");
        reply.put("Goal", "Info");
        reply.put("ObjectString", str);
        reply.put("Class", item.getClass().getName());

            for(Socket connectedClient : connectedClient)
            {
                OutputStream out = connectedClient.getOutputStream();
                OutputStreamWriter oos =new OutputStreamWriter(out, "UTF8");
                oos.write(reply.toJSONString()+"\n");
                oos.flush();
            }

            System.out.println("done");


    }

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
