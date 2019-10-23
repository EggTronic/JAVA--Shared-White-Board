package PublishSubscribeSystem;

import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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
		applicants = new ConcurrentHashMap<String, Socket>();
		server = null;
		manager = null;
	}

	private static volatile PublishSubscribeSystem singleton = null;

	public synchronized static PublishSubscribeSystem getInstance() {
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

	public void setManager(String applier) {
		this.manager = applier;
	}

	public void resetManager() {
		this.manager = null;
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
		if (this.map.containsKey(username)) {
			if (!this.map.get(username).isClosed())
				this.map.get(username).close();
			this.map.remove(username);
		} else {
			Iterator<ClientInfo> listOfClients = this.queue.iterator();
			while (listOfClients.hasNext()) {
				ClientInfo current = listOfClients.next();
				if (current.getName().equals(username)) {
					if (!current.getClient().isClosed())
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

	public ArrayList<String> getUserList() {

		ArrayList<String> userlist = new ArrayList<>();

		for (Map.Entry<String, Socket> eachUser : map.entrySet()) {
			userlist.add(eachUser.getKey());
		}

		return (ArrayList<String>) userlist.clone();

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

	public synchronized void sendToManger(JSONObject item) throws IOException {
		if (item != null) {
			String message = PublishSubscribeSystem.Encryptedmessage(item.toJSONString());
			Socket socket = this.map.get(this.manager);
			if (!socket.isClosed()) {
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
				oos.write(message + "\n");
				oos.flush();
			}
		} else
			System.out.println("invalid item to the manager");


	}

	public synchronized void sendtoSpecificUser(JSONObject item, String username) throws IOException {
		if (item != null) {
			String message = PublishSubscribeSystem.Encryptedmessage(item.toJSONString());
			Socket socket = this.map.get(username);
			if (!socket.isClosed()) {
				OutputStream out = socket.getOutputStream();
				OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
				oos.write(message + "\n");
				oos.flush();

			}
		}


	}


	public synchronized void disconnectServer() throws IOException {
		for (Map.Entry<String, Socket> eachUser : this.map.entrySet()) {
			Socket participant = (Socket) eachUser.getValue();
			if (!participant.isClosed()) {
				participant.close();

			}
		}

		Iterator<ClientInfo> listOfClients = queue.iterator();
		while (listOfClients.hasNext()) {
			ClientInfo current = listOfClients.next();
			Socket client = current.getClient();
			if (!client.isClosed()) {

				client.close();
			}

		}

		for (Map.Entry<String, Socket> eachwaiter : this.applicants.entrySet()) {
			Socket participant = (Socket) eachwaiter.getValue();
			if (!participant.isClosed()) {
				participant.close();

			}

		}


		this.map = new ConcurrentHashMap<String, Socket>();
		this.queue = new LinkedBlockingQueue<>();
		this.applicants = new ConcurrentHashMap<String, Socket>();;

		try {
			server.close();
			System.out.println("server close");
		} catch (IOException ex) {
			throw new IOException("Server disconnect unproperly");
		}
	}

	public synchronized void broadcastJSON(JSONObject item, String sender) throws IOException {
		String message = PublishSubscribeSystem.Encryptedmessage(item.toJSONString());
		for (Map.Entry<String, Socket> eachUser : this.map.entrySet()) {
			if (eachUser.getKey().equals(sender))
				continue;
			else {
				Socket participant = (Socket) eachUser.getValue();

				if (!participant.isClosed()) {
					OutputStream out = participant.getOutputStream();
					OutputStreamWriter poos = new OutputStreamWriter(out, "UTF8");
					poos.write(message + "\n");
					poos.flush();

				}
			}


		}


	}

	public synchronized void broadcastJSON(JSONObject item) throws IOException {
		String message = PublishSubscribeSystem.Encryptedmessage(item.toJSONString());
		for (Map.Entry<String, Socket> eachUser : this.map.entrySet()) {

			Socket participant = (Socket) eachUser.getValue();

			if (!participant.isClosed()) {
				OutputStream out = participant.getOutputStream();
				OutputStreamWriter poos = new OutputStreamWriter(out, "UTF8");
				poos.write(message + "\n");
				poos.flush();

			}
		}


	}


	public synchronized boolean hasrepeatedName(String username) {
		boolean hasrepeat = false;
//		if (this.map.containsKey(username.toLowerCase())) {
//			hasrepeat = true;
//			return hasrepeat;
//		}

		for (Map.Entry<String, Socket> eachUser : this.map.entrySet()) {
			if (eachUser.getKey().equalsIgnoreCase(username)) {
				hasrepeat = true;
				break;
			}
		}

		Iterator<ClientInfo> listOfClients = queue.iterator();
		while (listOfClients.hasNext()) {
			ClientInfo current = listOfClients.next();
			String name = current.getName();
			if (username.equalsIgnoreCase(name)) {
				hasrepeat = true;
				break;

			}
		}


		return hasrepeat;

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

	public ConcurrentHashMap<String, Socket> getApplicants() {
		return this.applicants;
	}

	public synchronized void broadcastShapes(MyShape item, String sender) throws IOException {


		String shapestr = Base64.getEncoder().encodeToString(serialize(item));

		JSONObject reply = new JSONObject();

		reply.put("Source", "Server");
		reply.put("Goal", "Info");
		reply.put("ObjectString", shapestr);
		reply.put("Class", item.getClass().getName());

		String message = PublishSubscribeSystem.Encryptedmessage(reply.toJSONString());

		ConcurrentHashMap<String, Socket> connectedClient = PublishSubscribeSystem.getInstance().getUsermap();
		for (Map.Entry<String, Socket> eachUser : connectedClient.entrySet()) {
			System.out.println(eachUser.toString());

			if (eachUser.getKey().equals(sender)) {
				System.out.println("skipped");
				continue;
			} else {
				Socket socket = (Socket) eachUser.getValue();
				String username = (String) eachUser.getKey();

				if (!socket.isClosed()) {
					OutputStream out = socket.getOutputStream();
					OutputStreamWriter oos = new OutputStreamWriter(out, "UTF8");
					oos.write(message+ "\n");
					oos.flush();
				} else
					PublishSubscribeSystem.getInstance().deregisterClient(username);
			}

			System.out.println("done");

		}
	}

	public void resetBoardState() {

		this.boardState = new BoardState(new ArrayList<MyShape>());


	}

	public static String decryptMessage(String message) {
		// Decrypt result
		try {
			String key = "5v8y/B?D(G+KbPeS";
			Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			message = new String(cipher.doFinal(Base64.getDecoder().decode(message.getBytes())));
			System.err.println("Decrypted message: " + message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return message;

	}

	public static String Encryptedmessage(String message){
		// Encrypt first
		String key = "5v8y/B?D(G+KbPeS";
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
		try {
			Cipher cipher = Cipher.getInstance("AES");
			// Perform encryption
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			byte[] encryptedBytes = cipher.doFinal(message.getBytes("UTF-8"));
			message = Base64.getEncoder().encodeToString(encryptedBytes);
			System.err.println("Encrypted text: "+new String(message));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return message;



	}
}





