package ClientUI;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

import org.json.simple.JSONObject;


import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import Exceptions.AbnormalCommunicationException;
import Shape.MyShape;

public class Client {

	    private Socket socket;
	    private OutputStreamWriter osw;
	    private BufferedReader br;
	    private long time;

	    public Client() {
	    }

	    public void initiate(String address,int port) throws ConnectException, UnknownHostException, IOException{
	        try{
	        	System.out.println("The client is running !");
	        	System.out.println(address);
	        	System.out.println(port);
	        	
	            this.socket = new Socket(address,port);
	            this.time = System.currentTimeMillis();
	            
	        }catch(ConnectException e) {
	            throw new ConnectException("Connect failed or timeout, check address and port.");
	        }catch (UnknownHostException e){
	            throw new UnknownHostException("Unknow host, check the host address.");
	        }catch(IOException e){
	            e.printStackTrace();
	        }catch(IllegalArgumentException e) {
	            throw new IllegalArgumentException("The argument of the method is invalid");
	        }
	        
	        try {
	            OutputStream os = socket.getOutputStream();
	            this.osw = new OutputStreamWriter(os, "UTF8");
	            InputStream is = socket.getInputStream();
	            InputStreamReader isr = new InputStreamReader(is);
	            this.br = new BufferedReader(isr);

	        	
	        }catch(IOException e) {
	        	throw new IOException("Something wrong happens in InputStream or OutputStream");
	        }
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
	    
	    public void requestDraw(Object obj, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	            	  String str = Base64.getEncoder().encodeToString(serialize(obj));
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Draw");
	                  request.put("ObjectString", str);
	                  request.put("Class", obj.getClass().getName());
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
            	  ClientUI.error = true;
            	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestLoad(Object obj, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	            	  String str = Base64.getEncoder().encodeToString(serialize(obj));
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Load");
	                  request.put("ObjectString", str);
	                  request.put("Class", obj.getClass().getName());
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestNew(int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "New");
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestClose(int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Close");
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestAccept(String username, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Accept");
	                  request.put("String", username);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
		      	  ClientUI.error = true;
		      	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestDecline(String username, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Decline");
	                  request.put("String", username);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
		      	  ClientUI.error = true;
		      	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestRemove(String username, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Remove");
	                  request.put("String", username);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
		      	  ClientUI.error = true;
		      	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestChat(String username, String message, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Chat");
	                  request.put("username", username);
	                  request.put("message", message);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestLeave(String username, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Leave");
	                  request.put("usernme", username);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestEnter(String username, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Enter");
	                  request.put("usernme", username);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public void requestCreate(String username, int threshod) throws AbnormalCommunicationException, IOException{
	          try {
	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Create");
	                  request.put("usernme", username);
	                  osw.write(request.toString()+"\n");
	                  osw.flush();
					
	              }else {
	            	  ClientUI.error = true;
	            	  ClientUI.errorMsg = "Timeout" + String.valueOf(System.currentTimeMillis() - this.time) + " Request timeout, check the connection";
	              }

	          }catch(SocketException e){
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: Connection is lost or Server is down | Terminate the client and restart later";
	          }catch (IOException e) {
	        	  ClientUI.error = true;
	        	  ClientUI.errorMsg = "Failure: IO Exception, check input or output streams | Terminate the client and restart later";
	          }
	    }
	    
	    public BufferedReader getBufferReader() {
	    	return this.br;
	    }
	    
	    public void disconnect() throws IOException {
	    	try{
	        	this.osw.close();
	        	this.br.close();
	    	this.socket.close();
	    	}catch(IOException e) {
	    		throw new IOException("Cannot disconnect the client properly");
	    	}
	    }


	
}