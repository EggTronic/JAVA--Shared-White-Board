package server;

import java.util.ArrayList;
import java.util.List;


public class ServerParameters {

    private static ServerParameters clientstate;
    private ArrayList<Client_thread> connectedClients;
    private ArrayList<Object> User;

    private ServerParameters(){
        connectedClients = new ArrayList<>();
    }

    public static synchronized ServerParameters getClientstate(){
        if(clientstate == null){
            clientstate = new ServerParameters();

        }
        return clientstate;
    }

    public synchronized void clientConnected(Client_thread client){
        connectedClients.add(client);

    }

    public synchronized void clientDisconnected(Client_thread client){
        connectedClients.remove(client);
    }

    public synchronized ArrayList<Client_thread> getConnectedClients(){
        return connectedClients;

    }


}
