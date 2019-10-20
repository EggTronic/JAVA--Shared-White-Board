package PublishSubscribeSystem;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import Server.Server;

public class PublishSubscribeSystem {
	private ServerSocket server;
	private ConcurrentHashMap<String, Socket> map;
	private int maxNum = 10;
	private String manager;
	private LinkedBlockingQueue<ClientInfo> queue; 
	
	private PublishSubscribeSystem () {
		map = new ConcurrentHashMap<String, Socket>();
		server = null;
		manager = null;
		queue = new LinkedBlockingQueue<>();
		
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
	
	public boolean registerClient(String username, Socket client) {
		if(this.map.size()<this.maxNum) {
		if(this.map.size()==0) {
			this.manager = username;
		}
		this.map.put(username, client);
		return true; 
		}
		else {
			ClientInfo clietinfo = new ClientInfo(username, client);
			this.queue.add(clietinfo);
			return false;
		}
	}
	
	public void registerServer(ServerSocket newserver) {
		this.server = newserver;
	}
	
	public void deregisterClient(String username) {
		if(this.map.containsKey(username)) {
			this.map.remove(username);
		}
		else {
			Iterator<ClientInfo> listOfClients = this.queue.iterator(); 
			while (listOfClients.hasNext()) {
				ClientInfo current = listOfClients.next();
				if(current.getName().equals(username))
					this.queue.remove(current);
	
			} 
	            
		}

	}
	
	public void deregisterServer() {
		this.server = null;
	}
	
	public int getNumOfPeople() {
		return map.size();
	}

	public ConcurrentHashMap<String, Socket> getUsermap(){

		return map;

	}

	public LinkedBlockingQueue<ClientInfo> getQueue(){

		return queue;
	}
	
	public boolean validateManager(String username) {
		return username.equals(manager);
	}
}
