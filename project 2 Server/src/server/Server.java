package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Shape.*;


public class Server {

    private static  ArrayList<Socket> connectedClient;
    private String roomowner;
    private static String hostname = "localhost";
    private static int portnumber = 5001;

    public static void main(String[] args) throws Exception {

        // to test the port number
        try {
            if (args.length == 1) {
                portnumber = Integer.parseInt(args[0]);
            } else if (args.length == 0) {

                System.out.println("using default hostname and portnumber = 5001");

            } else {
                System.out.println("the default hostname and portnumber is used");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerSocket listeningSocket = new ServerSocket(portnumber);
        ExecutorService threadpool_receive = Executors.newCachedThreadPool();
        ExecutorService threadpool_sync = Executors.newCachedThreadPool();
        int clientnumber = 0;


        try {


            while (true) {
                System.out.println("Server listening on port " + portnumber + " for a connection");
                //Accept an incoming client connection request
                Socket clientsocket = listeningSocket.accept(); //This method will block until a connection request is received
                System.out.println("someone wants to share your whiteboard");
                connectedClient.add(clientsocket);
                clientnumber++;

                Client_thread client = new Client_thread (clientsocket, clientnumber,connectedClient);

                threadpool_receive.execute(client);// use this thread to receive the update from the client

                ServerParameters.getClientstate().clientConnected(client);

                updateGraphs updateGraphs = new updateGraphs(clientsocket,client); // this thread will get the latest Myshape and MyText List and broadcast to all the connected clients
                Thread t = new Thread(updateGraphs);
                threadpool_sync.execute(t);


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
        {
            if(listeningSocket != null)
            {
                try
                {
                    for(Socket connectedClient : connectedClient)
                    {
                        OutputStream out = connectedClient.getOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(out);
                        oos.writeUTF("Manager leaving , session closed");
                    }
                    threadpool_receive.shutdown();
                    threadpool_sync.shutdown();
                    listeningSocket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    static  ArrayList<Socket> getConnectedClient(){
        return (ArrayList<Socket>)connectedClient.clone();
    }

    }

