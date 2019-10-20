package PublishSubscribeSystem;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import Server.Server;

public class PublishSubscribeSystem {
	private static ServerSocket server;
	private static ConcurrentHashMap<String, Socket> map;
	private static int maxNum = 10;
	private static String manager;


	
	private static LinkedBlockingQueue<ClientInfo> queue = new LinkedBlockingQueue<>();
	
	private PublishSubscribeSystem () {
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
	
	public static boolean registerClient(String username, Socket client) {
		if(map.size()<maxNum) {
		if(map.size()==0) {
			manager = username;
		}
		map.put(username, client);
		return true; 
		}
		else {
			ClientInfo clietinfo = new ClientInfo(username, client);
			queue.add(clietinfo);
			return false;
		}
	}
	
	public static void registerServer(ServerSocket newserver) {
		server = newserver;
	}
	
	public static void deregisterClient(String username) {
		if(map.containsKey(username)) {
			map.remove(username);
		}
		else {
			Iterator<ClientInfo> listOfClients = queue.iterator(); 
			while (listOfClients.hasNext()) {
				ClientInfo current = listOfClients.next();
				if(current.getName().equals(username))
					queue.remove(current);
	
			} 
	            
		}

	}
	
	public static void deregisterServer() {
		server = null;
	}
	
	public static int getNumOfPeople() {
		return map.size();
	}
	
}
