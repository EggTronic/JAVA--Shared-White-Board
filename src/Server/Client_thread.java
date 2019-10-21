package Server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ClientUI.BoardState;
import PublishSubscribeSystem.ClientInfo;
import PublishSubscribeSystem.PublishSubscribeSystem;
import Shape.*;



public class Client_thread implements Runnable {



    private Socket clientsocket;
    private int clientnumber;
    private String username;
    private long time;


//    Client_thread (Socket client, int clientnumber) throws IOException{
    	 Client_thread (Socket client) throws IOException{
        this.clientsocket = client;
        this.clientnumber = clientnumber;

        System.out.println("client_thread "+clientnumber+" is going");
    }

    @Override
    public void run() {
        try(Socket socket = clientsocket){

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader ois = new BufferedReader(isr);
            OutputStreamWriter oos = new OutputStreamWriter(socket.getOutputStream());
            JSONParser parser = new JSONParser();

            String result;


            while(!socket.isClosed()){

            	


                if((result = ois.readLine()) != null){
                        System.out.println("Received from client: "+clientnumber+" "+result);

                        JSONObject command = (JSONObject) parser.parse(result);

                        if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Draw"))
                        {
                            String obj = command.get("ObjectString").toString();
                            String type = command.get("Class").toString();
                            byte[] bytes= Base64.getDecoder().decode(obj);
                            Object object;

                            JSONObject reply = new JSONObject();
                            reply.put("Source","Server");
                            reply.put("Goal","Reply");
                            reply.put("ObjectString","Successfully received!");

                            oos.write(reply.toJSONString()+"\n");
                            oos.flush();

                            switch(type) {
                                case "MyShape.MyLine":
                                    object = (MyLine) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().getShapes().add((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object);
                                    break;
                                case "MyShape.MyEllipse":
                                    object = (MyEllipse) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().getShapes().add((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object);
                                    break;
                                case "MyShape.MyRectangle":
                                    object = (MyRectangle) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().getShapes().add((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object);
                                    break;
                                case "MyShape.MyText":
                                    object = (MyText) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().getShapes().add((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object);
                                    break;
                                default:
                                    break;
                            }
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Create")) {
                            String username = command.get("Username").toString();                            
                            JSONObject reply = new JSONObject();
                            reply.put("Source","Server");
                            reply.put("Goal","Create");
                            
                            boolean res = false;
                            
                            synchronized(PublishSubscribeSystem.getInstance()) {
                            if (PublishSubscribeSystem.getInstance().getNumOfPeople()==0) {
                            	res = true;
                            }
                            res = PublishSubscribeSystem.getInstance().registerClient(username, socket);                          
                            }
                            
                            if(res) {
                            reply.put("ObjectString","Success");}
                            else {
                            reply.put("ObjectString","Failure");	
                            }
                            
                            oos.write(reply.toJSONString()+"\n");
                            oos.flush();
                            
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Chat")) {
                            String username = command.get("Username").toString();   
                            String target = command.get("Target").toString();  
                            String message = command.get("ObjectString").toString();
                            JSONObject reply = new JSONObject();
                            
                            if(PublishSubscribeSystem.getInstance().getUsermap().containsKey(target)){
                            	Socket receiver = PublishSubscribeSystem.getInstance().getUsermap().get(target);
                            	OutputStreamWriter receiverOos = new OutputStreamWriter(receiver.getOutputStream());
                                reply.put("Source","Server");
                                reply.put("Goal","Chat");
                                reply.put("ObjectString", message);
                                reply.put("SourceClient", username);
                                receiverOos.write(reply.toJSONString()+"\n");
                                receiverOos.flush();
                            	
                            }
                            else {
                                reply.put("Source","Server");
                                reply.put("Goal","Chat");
                                reply.put("ObjectString", "Receiver doesn't exist");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                           	
                            }
                            
                        }
                        // a user leaves the board and chat room (not being kicked out)
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Leave")) {
                            String username = command.get("Username").toString();   
                            JSONObject reply = new JSONObject();
                            PublishSubscribeSystem.getInstance().deregisterClient(username);
                            reply.put("Source","Server");
                            reply.put("Goal","Leave");
                            reply.put("ObjectString", username + "leaves the room.");
                            
                            PublishSubscribeSystem.getInstance().broadcastJSON(reply);
                  
                            
                        }       
                        
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Close")) {
                            String username = command.get("Username").toString();   
                            JSONObject reply = new JSONObject();
                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
                            	reply.put("Source","Server");
                                reply.put("Goal","Close");
                                reply.put("ObjectString", "Unauthorized manager");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                            	
                            }
                            else {
                            	reply.put("Source","Server");
                                reply.put("Goal","Close");
                                reply.put("ObjectString", "Manager " + username + " is closing the board");
                            	
                                PublishSubscribeSystem.getInstance().broadcastJSON(reply);
                                
                                
                                LinkedBlockingQueue<ClientInfo> queue = PublishSubscribeSystem.getInstance().getQueue();


                                Iterator<ClientInfo> listOfClients = queue.iterator();
                                while (listOfClients.hasNext()) {
                                    ClientInfo current = listOfClients.next();
                                    Socket wait = current.getClient();
                                    if(!wait.isClosed()){
                                        OutputStream out = wait.getOutputStream();
                                        OutputStreamWriter woos =new OutputStreamWriter(out, "UTF8");
                                        woos.write(reply.toJSONString()+"\n");
                                        woos.flush();
                                    }
                                }
                                
                                PublishSubscribeSystem.getInstance().disconnectServer();
                            }
                                             
                            
                        }
                        
                        
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Remove")) {
                            String username = command.get("Username").toString(); 
                            String removename = command.get("Kickuser").toString(); 
                            JSONObject reply = new JSONObject();
                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
                            	reply.put("Source","Server");
                                reply.put("Goal","Remove");
                                reply.put("ObjectString", "Unauthorized manager");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                            	
                            }
                            else {
                            	boolean addsocket = false;
                            	if(PublishSubscribeSystem.getInstance().getUsermap().containsKey(removename)) {
                            		addsocket = true;
                            	}
                            	reply.put("Source","Server");
                                reply.put("Goal","Remove");
                                reply.put("ObjectString", "User " + removename + " has been kicked out");
                                
                                PublishSubscribeSystem.getInstance().deregisterClient(removename);
                            	
                                PublishSubscribeSystem.getInstance().broadcastJSON(reply);
                                
                                LinkedBlockingQueue<ClientInfo> q = PublishSubscribeSystem.getInstance().getQueue();
                                
                                synchronized(q) {
                                	if(q.size()!=0 && addsocket) {
                                
                                		ClientInfo clientinfo;
                                
                                		clientinfo = q.poll();
                                	
                                		String name = clientinfo.getName();
                                		Socket s = clientinfo.getClient();
                                		PublishSubscribeSystem.getInstance().getUsermap().put(name, s);
                                
                                		JSONObject replyAll = new JSONObject();
                                		replyAll.put("Source","Server");
                                		replyAll.put("Goal","Enter");
                                		replyAll.put("ObjectString", name + " has entered the room");
                                		PublishSubscribeSystem.getInstance().broadcastJSON(replyAll);
                                
                                }
                              }

                            }
                                             
                            
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("New")) {
                            String username = command.get("Username").toString(); 
 
                            JSONObject reply = new JSONObject();
                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
                            	reply.put("Source","Server");
                                reply.put("Goal","New");
                                reply.put("ObjectString", "Unauthorized manager");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                            	
                            }
                            else {
                            	reply.put("Source","Server");
                                reply.put("Goal","New");
                                reply.put("ObjectString", "Manager " + username + " has cleaned the board");

                                PublishSubscribeSystem.getInstance().broadcastJSON(reply);

                            }
                                             
                            
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Enter")) {
                            String username = command.get("Username").toString();  
                            PublishSubscribeSystem.getInstance().getApplicants().put(username, socket);
                            JSONObject reply = new JSONObject();
                        	reply.put("Source","Server");
                            reply.put("Goal","Authorize");
                            reply.put("ObjectString", "Need to authorize the applicant");
                            reply.put("Applicant", username);
                            
                            PublishSubscribeSystem.getInstance().sendToManger(reply);                       
                            
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Accept")) {
                        	String username = command.get("Username").toString();  
                            String applicant = command.get("Applicant").toString(); 
                            JSONObject reply = new JSONObject();
                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
                            	reply.put("Source","Server");
                                reply.put("Goal","Accept");
                                reply.put("ObjectString", "Unauthorized manager");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                            	
                            }
                            else {
                            	Socket client  = PublishSubscribeSystem.getInstance().getApplicants().get(applicant);
                            	
                            	if(!client.isClosed()) {
                            	boolean res = PublishSubscribeSystem.getInstance().registerClient(applicant, client);
                            	
                            	reply.put("Source","Server");
                                reply.put("Goal","Accept");
                                if (res) {
                                reply.put("ObjectString", "Allows to Enter");
                                PublishSubscribeSystem.getInstance().getApplicants().remove(applicant);
                                
                                JSONObject replyAll = new JSONObject();
                            	replyAll.put("Source","Server");
                                replyAll.put("Goal","Enter");
                                replyAll.put("ObjectString", applicant + " has entered the room");
                                PublishSubscribeSystem.getInstance().broadcastJSON(replyAll);
                                
                                }
                                else {
                                reply.put("ObjectString", "Room is full");	
                                }
                                
                                OutputStream aout = client.getOutputStream();
                                OutputStreamWriter aoos =new OutputStreamWriter(aout, "UTF8");
                                aoos.write(reply.toJSONString()+"\n");
                                aoos.flush();

                            	}
                            
                            }                    
                            
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Decline")) {
                        	String username = command.get("Username").toString();  
                            String applicant = command.get("Applicant").toString(); 
                            JSONObject reply = new JSONObject();
                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
                            	reply.put("Source","Server");
                                reply.put("Goal","Decline");
                                reply.put("ObjectString", "Unauthorized manager");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                            	
                            }
                            else {
                            	Socket client  = PublishSubscribeSystem.getInstance().getApplicants().get(applicant);
                            	
                            	if(!client.isClosed()) {
                            	
                            	PublishSubscribeSystem.getInstance().getApplicants().remove(applicant);
                            	
                            	reply.put("Source","Server");
                                reply.put("Goal","Decline");
                                reply.put("ObjectString", "Unauthorized Entry");
                                
                                OutputStream aout = client.getOutputStream();
                                OutputStreamWriter aoos =new OutputStreamWriter(aout, "UTF8");
                                aoos.write(reply.toJSONString()+"\n");
                                aoos.flush();

                            	}
                            
                            }                    
                            
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Load")) {
                        	String username = command.get("Username").toString();  
                            JSONObject reply = new JSONObject();
                            String boardstate = command.get("ObjectString").toString();
                            
                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
                            	reply.put("Source","Server");
                                reply.put("Goal","Load");
                                reply.put("ObjectString", "Unauthorized manager");
                                oos.write(reply.toJSONString()+"\n");
                                oos.flush();                            	
                            }
                            
                            else {
                            	
                            	reply.put("Source","Server");
                                reply.put("Goal","Load");
                                reply.put("ObjectString", boardstate);
                                PublishSubscribeSystem.getInstance().broadcastJSON(reply);
                                
                                byte[] bytes = Base64.getDecoder().decode(boardstate);
                                BoardState bs = (BoardState) PublishSubscribeSystem.getInstance().deserialize(bytes);
                                PublishSubscribeSystem.getInstance().updateBoardState(bs);


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


        System.out.println("exit");


        }
    }

//    static synchronized void updateClients(){
//
//        connectedClient = Server.getConnectedClient();
//
//
//    }
    private synchronized void privateText(Object item,Socket otherclient) throws IOException{

        OutputStream out = otherclient.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(item);
        oos.flush();
        oos.close();

    }
//    synchronized ArrayList<Socket> getConnectedClient(){
//
//        if(connectedClient != null)
//            return (ArrayList<Socket>)connectedClient.clone();
//        else
//            return Server.getConnectedClient();
//
//    }
//    synchronized ArrayList<MyShape> getShapes(){
//        return (ArrayList<MyShape>) shapes.clone();
//    }
//
//    synchronized ArrayList<MyText> getTexts(){
//        return (ArrayList<MyText>) texts.clone();
//    }

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