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

import PublishSubscribeSystem.ClientInfo;
import PublishSubscribeSystem.PublishSubscribeSystem;
import Shape.*;
import Utils.EncryptDecrypt;
import sun.plugin.com.Utils;


public class Client_thread implements Runnable {



    private Socket clientsocket;
    private int clientnumber;
    private String username;
    private boolean isManager = false;
    private long time;


//    Client_thread (Socket client, int clientnumber) throws IOException{
    	 Client_thread (Socket client,int clientnumber) throws IOException{
        this.clientsocket = client;
        this.clientnumber = clientnumber;

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




                while((result = ois.readLine()) != null){

                        result = EncryptDecrypt.decrypt(result);
                        System.out.println("Received from client: "+" "+result);

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

                            String acknowledgement = EncryptDecrypt.encrypt(reply.toJSONString());

                            oos.write(acknowledgement+"\n");
                            oos.flush();

                            switch(type) {
                                case "Shape.MyLine":
                                    object = (MyLine) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().addShapes((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object,username);
                                    break;
                                case "Shape.MyEllipse":
                                    object = (MyEllipse) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().addShapes((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object,username);
                                    break;
                                case "Shape.MyRectangle":
                                    object = (MyRectangle) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().addShapes((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object,username);
                                    break;
                                case "Shape.MyText":
                                    object = (MyText) deserialize(bytes);
                                    PublishSubscribeSystem.getInstance().getBoardState().addShapes((MyShape) object);
                                    PublishSubscribeSystem.getInstance().broadcastShapes((MyShape) object,username);
                                    break;
                                default:
                                    break;
                            }
                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Create")) {
                            String username = command.get("Username").toString();
                            this.username = username;
                            JSONObject reply = new JSONObject();
                            reply.put("Source","Server");
                            reply.put("Goal","Create");

                            boolean res = false;

                            synchronized(PublishSubscribeSystem.getInstance()) {
                            if (!PublishSubscribeSystem.getInstance().hasManger()) {
                            	res = true;
                            }
                            PublishSubscribeSystem.getInstance().registerClient(username, socket);
                            }

                            if(res) {
                            reply.put("ObjectString","Success");
                            this.isManager = true;
                            PublishSubscribeSystem.getInstance().setManager(command.get("Username").toString());
                            String acknowledgement = EncryptDecrypt.encrypt(reply.toJSONString());
                            oos.write(acknowledgement+"\n");
                            oos.flush();
                            }
                            else {
                            reply.put("ObjectString","Failure");
                            String acknowledgement = EncryptDecrypt.encrypt(reply.toJSONString());
                            oos.write(acknowledgement+"\n");
                            oos.flush();
                            PublishSubscribeSystem.getInstance().deregisterClient(username);
                            oos.close();
                            }



                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Chat")) {
                            String username = command.get("Username").toString();
                            String message = command.get("Message").toString();
                            JSONObject reply = new JSONObject();


                                reply.put("Source","Server");
                                reply.put("Goal","Chat");
                                reply.put("message", message);
                                reply.put("username", username);
                            PublishSubscribeSystem.getInstance().broadcastJSON(reply,this.username);




                        }
                        // a user leaves the board and chat room (not being kicked out)
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Leave")) {
                            String username = command.get("Username").toString();
                            this.username = username;
                            JSONObject reply = new JSONObject();
                            PublishSubscribeSystem.getInstance().deregisterClient(username);
                            reply.put("Source","Server");
                            reply.put("Goal","Leave");
                            reply.put("username", username);

                            PublishSubscribeSystem.getInstance().broadcastJSON(reply,this.username);
                            // Need to get somebody in the waiting list to get in！

                            LinkedBlockingQueue<ClientInfo> q = PublishSubscribeSystem.getInstance().getQueue();

                            synchronized(q) {
                                if(q.size()!=0 && PublishSubscribeSystem.getInstance().getUsermap().size() < 20) {

                                    ClientInfo clientinfo;

                                    clientinfo = q.poll();

                                    String name = clientinfo.getName();
                                    Socket s = clientinfo.getClient();
                                    PublishSubscribeSystem.getInstance().getUsermap().put(name, s);

//                                    JSONObject replyAll = new JSONObject();
//                                    replyAll.put("Source","Server");
//                                    replyAll.put("Goal","Chat");
//                                    replyAll.put("username", "From Server");
//                                    replyAll.put("message",name+" has entered room");
//                                    PublishSubscribeSystem.getInstance().broadcastJSON(replyAll,this.username);

                                    JSONObject updateUserList = new JSONObject();
                                    updateUserList.put("Source","Server");
                                    updateUserList.put("Goal","Enter");
                                    updateUserList.put("username",name);
                                    PublishSubscribeSystem.getInstance().broadcastJSON(updateUserList);

                                }
                            }




                        }

                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Close")) {
//                            String username = command.get("Username").toString();
                            JSONObject reply = new JSONObject();
//                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
//                            	reply.put("Source","Server");
//                                reply.put("Goal","Close");
//                                reply.put("ObjectString", "Unauthorized manager");
//                                oos.write(reply.toJSONString()+"\n");
//                                oos.flush();
//                            }
//                            else {
                            	reply.put("Source","Server");
                                reply.put("Goal","Close");
                                reply.put("ObjectString", "Manager " + username + " is closing the board");

                                PublishSubscribeSystem.getInstance().resetManager();

                                PublishSubscribeSystem.getInstance().broadcastJSON(reply,this.username);


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
//                            }


                        }


                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Remove")) {
                            String removename = command.get("Username").toString();
                            JSONObject reply = new JSONObject();
//                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
//                            	reply.put("Source","Server");
//                                reply.put("Goal","Remove");
//                                reply.put("ObjectString", "Unauthorized manager");
//                                oos.write(reply.toJSONString()+"\n");
//                                oos.flush();
//                            }

                            	boolean addsocket = false;
                            	if(PublishSubscribeSystem.getInstance().getUsermap().containsKey(removename)) {
                            		addsocket = true;
                            	}
                            	reply.put("Source","Server");
                                reply.put("Goal","Remove");
                                reply.put("ObjectString", "User " + removename + " has been kicked out");

                                PublishSubscribeSystem.getInstance().sendtoSpecificUser(reply,removename);

                                PublishSubscribeSystem.getInstance().deregisterClient(removename);


                                // after the remove we need to close the socket based on it

                                JSONObject broadcasttheremove = new JSONObject();

                                broadcasttheremove.put("Source","Server");
                                broadcasttheremove.put("Goal","Leave");
                                broadcasttheremove.put("message","User " + removename + " has been kicked out");
                                broadcasttheremove.put("username",removename);

                                PublishSubscribeSystem.getInstance().broadcastJSON(broadcasttheremove,this.username);

                                LinkedBlockingQueue<ClientInfo> q = PublishSubscribeSystem.getInstance().getQueue();

                                synchronized(q) {
                                	if(q.size()!=0 && addsocket) {

                                		ClientInfo clientinfo;

                                		clientinfo = q.poll();

                                		String name = clientinfo.getName();
                                		Socket s = clientinfo.getClient();
                                		PublishSubscribeSystem.getInstance().getUsermap().put(name, s);

//                                		JSONObject replyAll = new JSONObject();
//                                		replyAll.put("Source","Server");
//                                		replyAll.put("Goal","Chat");
//                                		replyAll.put("username", "From Server");
//                                		replyAll.put("message",name+" has entered room");
//                                		PublishSubscribeSystem.getInstance().broadcastJSON(replyAll,this.username);

                                		JSONObject updateUserList = new JSONObject();
                                		updateUserList.put("Source","Server");
                                		updateUserList.put("Goal","Enter");
                                		updateUserList.put("username",name);
                                		PublishSubscribeSystem.getInstance().broadcastJSON(updateUserList);

                                }
                              }



                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("New")) {
//                            String username = command.get("Username").toString();

                            JSONObject reply = new JSONObject();
//                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
//                            	reply.put("Source","Server");
//                                reply.put("Goal","New");
//                                reply.put("ObjectString", "Unauthorized manager");
//                                oos.write(reply.toJSONString()+"\n");
//                                oos.flush();
//                            }
                            	reply.put("Source","Server");
                                reply.put("Goal","New");
                                reply.put("ObjectString", "Manager " + username + " has cleaned the board");

                                PublishSubscribeSystem.getInstance().resetBoardState();

                                PublishSubscribeSystem.getInstance().broadcastJSON(reply,this.username);




                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Enter")) {
                            String username = command.get("Username").toString();
                            this.username = username;

                            boolean hasBoard = false;
                            boolean norepeatName =true;
                            synchronized(PublishSubscribeSystem.getInstance()) {
                                if (PublishSubscribeSystem.getInstance().hasManger()) {
                                    hasBoard = true;
                                }
                            }
                            synchronized(PublishSubscribeSystem.getInstance()) {
                                if (PublishSubscribeSystem.getInstance().hasrepeatedName(username)) {
                                    norepeatName = false;
                                }
                            }





                            if(hasBoard && norepeatName) {

                                PublishSubscribeSystem.getInstance().getApplicants().put(username, socket);
                                JSONObject reply = new JSONObject();
                                reply.put("Source", "Server");
                                reply.put("Goal", "Authorize");
                                reply.put("ObjectString", "Need to authorize the applicant");
                                reply.put("username", username);

                                PublishSubscribeSystem.getInstance().sendToManger(reply);


                            }
                            else if(!norepeatName){
                                JSONObject reply = new JSONObject();

                                reply.put("Source","Server");
                                reply.put("Goal","Reply");
                                reply.put("ObjectString","repeated Name, double check");

                                String message = EncryptDecrypt.encrypt(reply.toJSONString());

                                oos.write(message+"\n");
                                oos.flush();
                                PublishSubscribeSystem.getInstance().getApplicants().remove(username);
                                oos.close();


                            }
                            else{
                                JSONObject reply = new JSONObject();

                                reply.put("Source","Server");
                                reply.put("Goal","Reply");
                                reply.put("ObjectString","No board yet, try to create one");

                                String message = EncryptDecrypt.encrypt(reply.toJSONString());


                                oos.write(message+"\n");
                                oos.flush();
                                PublishSubscribeSystem.getInstance().getApplicants().remove(username);
                                oos.close();



                            }

                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Accept")) {
                        	String applicant = command.get("Username").toString();
//                            String applicant = command.get("Applicant").toString();
                            JSONObject reply = new JSONObject();
//                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
//                            	reply.put("Source","Server");
//                                reply.put("Goal","Accept");
//                                reply.put("ObjectString", "Unauthorized manager");
//                                oos.write(reply.toJSONString()+"\n");
//                                oos.flush();
//                            }
//                            else {
                            	Socket client  = PublishSubscribeSystem.getInstance().getApplicants().get(applicant);

                            	if(!client.isClosed()) {
                            	boolean res = PublishSubscribeSystem.getInstance().registerClient(applicant, client);

                            	reply.put("Source","Server");
                                reply.put("Goal","Accept");
                                if (res)
                                {
                                    BoardState obj1 = PublishSubscribeSystem.getInstance().getBoardState();
                                    String boarddstr = Base64.getEncoder().encodeToString(serialize(obj1));
                                    ArrayList<String> obj2 = PublishSubscribeSystem.getInstance().getUserList();

                                reply.put("BoardState", boarddstr);
                                reply.put("UserList",obj2);
                                PublishSubscribeSystem.getInstance().getApplicants().remove(applicant);

//                                JSONObject replyAll = new JSONObject();
//                                replyAll.put("Source","Server");
//                                replyAll.put("Goal","Chat");
//                                replyAll.put("message", applicant + " has entered the room");
//                                replyAll.put("username","From Server");
//                                PublishSubscribeSystem.getInstance().broadcastJSON(replyAll,this.username);

                                JSONObject updateUserList = new JSONObject();
                                updateUserList.put("Source","Server");
                                updateUserList.put("Goal","Enter");
                                updateUserList.put("username",applicant);
                                PublishSubscribeSystem.getInstance().broadcastJSON(updateUserList,this.username);

                                }
                                else {
                                reply.remove("Goal");
                                reply.put("Goal","Chat");
                                reply.put("message","the room is full, you are in the waiting list");
                                reply.put("username","From Server");

                                }

                                String message = EncryptDecrypt.encrypt(reply.toJSONString());

                                OutputStream aout = client.getOutputStream();
                                OutputStreamWriter aoos =new OutputStreamWriter(aout, "UTF8");
                                aoos.write(message+"\n");
                                aoos.flush();

                            	}

//                            }

                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Decline")) {
                        	String applicant = command.get("Username").toString();
//                            String applicant = command.get("Applicant").toString();
                            JSONObject reply = new JSONObject();
//                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
//                            	reply.put("Source","Server");
//                                reply.put("Goal","Decline");
//                                reply.put("ObjectString", "Unauthorized manager");
//                                oos.write(reply.toJSONString()+"\n");
//                                oos.flush();
//                            }
//                            else {
                            	Socket client  = PublishSubscribeSystem.getInstance().getApplicants().get(applicant);

                            	if(!client.isClosed()) {

                            	PublishSubscribeSystem.getInstance().getApplicants().remove(applicant);

                            	reply.put("Source","Server");
                                reply.put("Goal","Decline");
//                                reply.put("ObjectString", "Unauthorized Entry");

                                OutputStream aout = client.getOutputStream();
                                OutputStreamWriter aoos =new OutputStreamWriter(aout, "UTF8");

                                String message = EncryptDecrypt.encrypt(reply.toJSONString());

                                aoos.write(message+"\n");
                                aoos.flush();
                                aoos.close();

                            	}

//                            }

                        }
                        else if(command.get("Source").toString().equals("Client") && command.get("Goal").toString().equals("Load")) {
//                        	String username = command.get("Username").toString();
                            JSONObject reply = new JSONObject();
                            String boardstate = command.get("ObjectString").toString();

//                            if(!PublishSubscribeSystem.getInstance().validateManager(username)) {
//                            	reply.put("Source","Server");
//                                reply.put("Goal","Load");
//                                reply.put("ObjectString", "Unauthorized manager");
//                                oos.write(reply.toJSONString()+"\n");
//                                oos.flush();
//                            }

//                            else {

                            	reply.put("Source","Server");
                                reply.put("Goal","Load");
                                reply.put("ObjectString", boardstate);
                                PublishSubscribeSystem.getInstance().broadcastJSON(reply,this.username);

                                byte[] bytes = Base64.getDecoder().decode(boardstate);
                                BoardState bs = (BoardState) PublishSubscribeSystem.getInstance().deserialize(bytes);
                                PublishSubscribeSystem.getInstance().updateBoardState(bs);


                            	}

//                            }

                        }

            }





        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();

        }
        catch (IOException e)
        {
            System.out.println(username+" socket get closed");


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {




        System.out.println("thread "+username+" ended");
        try {

            if(this.isManager){

            JSONObject reply = new JSONObject();

            reply.put("Source","Server");
            reply.put("Goal","Close");
            reply.put("ObjectString", "Manager " + username + " is closing the board");

            PublishSubscribeSystem.getInstance().resetManager();

            PublishSubscribeSystem.getInstance().broadcastJSON(reply,this.username);


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
            if(!clientsocket.isClosed())
                clientsocket.close();
        }
        catch(IOException ex){

            System.out.println("incorrectly end thread");
        }

        }
    }

//    private synchronized void privateText(Object item,Socket otherclient) throws IOException{
//
//        OutputStream out = otherclient.getOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(out);
//        oos.writeObject(item);
//        oos.flush();
//        oos.close();
//
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
