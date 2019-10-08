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
	    
	    public JSONObject request(Object obj, int threshod) throws AbnormalCommunicationException, IOException{
	        
	    	  JSONObject response = new JSONObject();
	          try {

	              if((System.currentTimeMillis() - this.time)<= threshod) {
	            	  
	            	  String str = Base64.getEncoder().encodeToString(serialize(obj));
	            	  
	                  this.time = System.currentTimeMillis();
	                  JSONObject request = new JSONObject();
	                  request.put("Source", "Client");
	                  request.put("Goal", "Draw");
	                  request.put("ObjectString", str);
	                  request.put("Class", obj.getClass().getName());
	                  osw.write(request.toString()+'\n');
	                  osw.flush();
	                  
//	                  String content;
//	                  content= br.readLine();
//	                  System.out.println(1);
//	                  
//	                  while(content!=null) {
//	                	  System.out.println(2);
//	                	  JSONParser parser = new JSONParser();
//		                  JSONObject temp = (JSONObject) parser.parse(content);
//		                  if (temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Reply")) {
//		                	  response = temp;
//		                	  break;
//		                  }
//		                  else {
//		                	  continue;
//		                  }
//	                  }
//	                            
//					System.out.println(3);
					
	              }else {
	                  response.put("Timeout", String.valueOf(System.currentTimeMillis() - this.time));
	                  throw new AbnormalCommunicationException("Request timeout, check the connection");
	              }

	          }catch(SocketException e){
	              response.put("Status","Failure: Connection is lost or Server is down");
	              response.put("Action","Terminate the client and restart later");
	          }catch (IOException e) {
	              response.put("Status","Failure: IO Exception, check input or output streams");
	              response.put("Action","Terminate the client and restart later");

	          }finally {
	              return response;
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