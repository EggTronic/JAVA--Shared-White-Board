package PublishSubscribeSystem;

import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import Shape.*;
import ClientUI.BoardState;

import Server.Server;
import org.json.simple.JSONObject;

public class PublishSubscribeSystem {
	private ServerSocket server;
	private ConcurrentHashMap<String, Socket> map;
	private ConcurrentHashMap<String, Socket> applicants;
	private BoardState boardState = new BoardState(new ArrayList<MyShape>());
	private int maxNum = 10;
	private String manager;


	private LinkedBlockingQueue<ClientInfo> queue = new LinkedBlockingQueue<>();

	private PublishSubscribeSystem() {
		map = new ConcurrentHashMap<String, Socket>();
		server = null;
		manager = null;
	}

	private static volatile PublishSubscribeSystem singleton = null;

	public static PublishSubscribeSystem getInstance() {
		if (singleton == null) {
			synchronized (PublishSubscribeSystem.class) {
				if (singleton == null) {
					singleton = new PublishSubscribeSystem();
				}
			}
		}
		return singleton;
	}

	public String getManager() {
		return manager;
	}

	public boolean hasManger() {
		return (manager != null);
	}

	public boolean registerClient(String username, Socket client) {
		if (map.size() < maxNum) {
			if (map.size() == 0) {
				manager = username;
			}
			map.put(username, client);
			return true;
		} else {
			ClientInfo clietinfo = new ClientInfo(username, client);
			queue.add(clietinfo);
			return false;
		}
	}

	public void registerServer(ServerSocket newserver) {
		server = newserver;
	}
	
	public void deregisterClient(String username) throws IOException {
		if(this.map.containsKey(username)) {
			this.map.get(username).close();
			this.map.remove(username);
		}
		else {
			Iterator<ClientInfo> listOfClients = this.queue.iterator(); 
			while (listOfClients.hasNext()) {
				ClientInfo current = listOfClients.next();
				if(current.getName().equals(username)) {
					current.getClient().close();
					this.queue.remove(current);
				}
	
			} 
	            
		}
	}

	public void deregisterServer() {
		this.server = null;
	}

	public int getNumOfPeople() {
		return map.size();
	}

	public ConcurrentHashMap<String, Socket> getUsermap() {

		return map;

	}

	public LinkedBlockingQueue<ClientInfo> getQueue() {

		return queue;
	}

	public BoardState getBoardState() {

		return this.boardState;

	}

	public synchronized void updateBoardState(BoardState item) {
		if (item != null) {
			this.boardState = item;
		} else {
			System.out.println("not a valid boardState");
		}


	}

	public synchronized void sendToManger(JSONObject item) throws IOException{
	    if(item != null){
	        Socket socket = this.map.get(this.manager);
            OutputStream out = socket.getOutputStream();
            OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
            oos.write(item.toJSONString()+"\n");
            oos.flush();
	    }
	    else
	        System.out.println("invalid item to the manager");


    }

	public synchronized void disconnectServer() throws IOException {
		try {
			String item = "Manager leaving , session closed";
			String shapestr = Base64.getEncoder().encodeToString(serialize(item));

			JSONObject reply = new JSONObject();

			reply.put("Source", "Server");
			reply.put("Goal", "Close");
			reply.put("ObjectString", shapestr);


			for (Map.Entry<String, Socket> eachUser : this.map.entrySet()) {
				Socket socket = (Socket) eachUser.getValue();
				String username = (String) eachUser.getKey();


				if (!socket.isClosed()) {
					OutputStream out = socket.getOutputStream();
					OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
					oos.write(reply.toJSONString() + "\n");
					oos.flush();
				}
			}


			Iterator<ClientInfo> listOfClients = this.queue.iterator();
			while (listOfClients.hasNext()) {
				ClientInfo current = listOfClients.next();
				Socket socket = current.getClient();
				if (!socket.isClosed()) {
					OutputStream out = socket.getOutputStream();
					OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
					oos.write(reply.toJSONString() + "\n");
					oos.flush();

				}


			}


			server.close();
			System.out.println("server closed");
		} catch (IOException ex) {
			throw new IOException("Server disconnect unproperly");
		}
	}

	public synchronized void broadcastJSON (JSONObject item) throws IOException {
		for (Map.Entry<String, Socket> eachUser : this.map.entrySet()) {
			Socket participant = (Socket) eachUser.getValue();

			if (!participant.isClosed()) {
				OutputStream out = participant.getOutputStream();
				OutputStreamWriter poos = new OutputStreamWriter(out, "UTF8");
				poos.write(item.toJSONString() + "\n");
				poos.flush();

			}
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

	public boolean validateManager(String username) {
		return username.equals(manager);
	}
	
	public ConcurrentHashMap<String, Socket> getApplicants(){
		return this.applicants;
	}

	public synchronized void broadcastShapes(MyShape item) throws IOException {

		String shapestr = Base64.getEncoder().encodeToString(serialize(item));

		JSONObject reply = new JSONObject();

		reply.put("Source", "Server");
		reply.put("Goal", "Info");
		reply.put("ObjectString", shapestr);
		reply.put("Class", item.getClass().getName());

		ConcurrentHashMap<String,Socket> connectedClient = PublishSubscribeSystem.getInstance().getUsermap();

		for(Map.Entry<String,Socket> eachUser : connectedClient.entrySet())

		{   Socket socket = (Socket) eachUser.getValue();
			String username = (String) eachUser.getKey();

			if(!socket.isClosed()) {
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter oos =new OutputStreamWriter(out, "UTF8");
				oos.write(reply.toJSONString()+"\n");
				oos.flush();
			}
			else
				PublishSubscribeSystem.getInstance().deregisterClient(username);
		}

		System.out.println("done");


	}
}





