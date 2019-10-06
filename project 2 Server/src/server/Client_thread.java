package server;

import Text.MyText;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Shape.*;



public class Client_thread implements Runnable {

    private static ArrayList<Socket> connectedClient;
    private InputStream in;
    private OutputStream out;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket clientsocket;
    private String username;
    private String Path;
    private ArrayList<MyShape> shapes = new ArrayList<MyShape>();
    private ArrayList<MyText> texts = new ArrayList<>();
    private long time;


    Client_thread(@NotNull Socket clientsocket, int clientnumber, ArrayList<Socket> connectedClinet) throws IOException{
        this.clientsocket = clientsocket;
        connectedClient = connectedClinet;
        this.time = System.currentTimeMillis();
        out = clientsocket.getOutputStream();
        in = clientsocket.getInputStream();
        oos = new ObjectOutputStream(out);
        ois = new ObjectInputStream(in);

    }

    @Override
    public void run() {
        try{




                while(!clientsocket.isClosed()){


                JSONObject commandReceived = new JSONObject();
                JSONParser parser = new JSONParser();

                while(true){
                    if(ois.available() > 0){

                        String result = ois.readUTF();
                        System.out.println("Received from server: "+result);

                        JSONObject command = (JSONObject) parser.parse(result);

                        if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Draw"))
                        {
                            String obj = command.get("ObjectString").toString();
                            String type = command.get("Class").toString();
                            byte[] bytes= Base64.getDecoder().decode(obj);
                            Object object;

                            oos.writeUTF("Command Received and parsed");
                            oos.flush();

                            switch(type) {
                                case "MyLine":
                                    object = (MyLine) deserialize(bytes);
                                    shapes.add((MyShape) object);
                                    break;
                                case "MyEllipse":
                                    object = (MyEllipse) deserialize(bytes);
                                    shapes.add((MyShape) object);
                                    break;
                                case "MyRectangle":
                                    object = (MyRectangle) deserialize(bytes);
                                    shapes.add((MyShape) object);
                                    break;
                                case "MyText":
                                    object = (MyText) deserialize(bytes);
                                    texts.add((MyText) object);
                                    break;
                                default:
                                    break;
                            }
                        }





                    }



                }

                // then according to the received content update the shapes instance
                // or and then update the text list
                // and then broadcast to all the connected socket

            }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();

        }
        catch (IOException e)
        {

            e.printStackTrace();

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {


        }
    }

    static synchronized void updateClients(){

        connectedClient = Server.getConnectedClient();


    }

    private synchronized void broadcast(Object item) throws IOException{

        for(Socket connectedClient : connectedClient)
        {
            OutputStream out = connectedClient.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(item);
            oos.flush();
        }


    }


    private synchronized void privateText(Object item,Socket otherclient) throws IOException{

        OutputStream out = otherclient.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(item);
        oos.flush();

    }
    synchronized ArrayList<Socket> getConnectedClient(){

        if(connectedClient != null)
            return (ArrayList<Socket>)connectedClient.clone();
        else
            return Server.getConnectedClient();

    }
    synchronized ArrayList<MyShape> getShapes(){
        return (ArrayList<MyShape>) shapes.clone();
    }

    synchronized ArrayList<MyText> getTexts(){
        return (ArrayList<MyText>) texts.clone();
    }

    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bao);
        os.writeObject(obj);
        return bao.toByteArray();
    }

    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}

