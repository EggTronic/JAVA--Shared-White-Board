package Test;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Shape.MyText;

public class MyTextTest {
	public static void main(String[] args) {
		
		MyText myText = new MyText("text", (float) 5, (float) 10, Color.black, 20, "yang");
		
		 // Serialization code
        try
        {
            FileOutputStream fileOut = new FileOutputStream("text.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(myText);
            out.close();
            fileOut.close();
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        
        // De-serialization code
        MyText deserializedText = null;

        try
        {
            FileInputStream fileIn = new FileInputStream("text.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            deserializedText = (MyText) in.readObject();
            in.close();
            fileIn.close();
 
            // verify the object state
            System.out.println("==========text========");
            System.out.println(deserializedText.getText());
            System.out.println(deserializedText.getX());
            System.out.println(deserializedText.getY());
            System.out.println(deserializedText.getColor());
            System.out.println(deserializedText.getThickness());
            System.out.println(deserializedText.getAuthor());
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        catch (ClassNotFoundException cnfe)
        {
            cnfe.printStackTrace();
        }
	}
}
