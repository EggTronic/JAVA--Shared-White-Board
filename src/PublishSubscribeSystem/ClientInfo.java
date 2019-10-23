package PublishSubscribeSystem;

import java.net.Socket;

public class ClientInfo {


	private String name;
	private Socket client;
	public ClientInfo(String name, Socket client) {
		this.name = name;
		this.client = client;			
	}
	public String getName() {
		return name;
	}
	public Socket getClient() {
		return client;
	}

}
