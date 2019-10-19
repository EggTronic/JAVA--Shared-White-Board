package Server;

import Shape.MyShape;
import Shape.MyText;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

public class updateGraphs implements Runnable {



    @Override
    public void run() {

        try {
            while (true) {
//                Client_thread.updateClients();
                ArrayList<Socket> connectedClient1 = Server.getConnectedClient();
                ArrayList<MyShape> shapes = Server.getShapes();
                ArrayList<MyText> texts = Server.getTexts();

                String shapestr = Base64.getEncoder().encodeToString(serialize(shapes));
                String textstr = Base64.getEncoder().encodeToString(serialize(texts));

                JSONObject reply1 = new JSONObject();
                JSONObject reply2 = new JSONObject();

                reply1.put("Source", "Server");
                reply1.put("Goal", "info");
                reply1.put("ObjectString", shapestr);
                reply1.put("Class", shapestr.getClass().getName());

                reply2.put("Source", "Server");
                reply2.put("Goal", "info");
                reply2.put("ObjectString", textstr);
                reply2.put("Class", textstr.getClass().getName());


                for (Socket connectedClient : connectedClient1) {
                    OutputStream out = connectedClient.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(out);

                    oos.writeUTF(reply1.toJSONString()+'\n');
                    oos.flush();
                    oos.writeUTF(reply2.toJSONString()+'\n');
                    oos.flush();



                }

            }
        }
        catch(IOException ex){
            ex.printStackTrace();
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
}