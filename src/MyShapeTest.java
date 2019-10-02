import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MyShapeTest {
	public static void main(String[] args) {
		Shape line = new Line2D.Double(10, 2, 3, 4);
		Shape rectangle = new Rectangle2D.Double(10, 2, 3, 4);
		MyShape myline = new MyShape(line, Color.red, line.getClass().getName() ,"wow");
		MyShape myrectangle = new MyShape(rectangle, Color.white, rectangle.getClass().getName(), "lol");
		
		 // Serialization code
        try
        {
            FileOutputStream fileOut = new FileOutputStream("shape.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(myline);
            out.close();
            fileOut.close();
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        
        // De-serialization code
        MyShape deserializedShape = null;
        try
        {
            FileInputStream fileIn = new FileInputStream("shape.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            deserializedShape = (MyShape) in.readObject();
            in.close();
            fileIn.close();
 
            // verify the object state
            System.out.println(deserializedShape.getAuthor());
            System.out.println(deserializedShape.getColor());
            System.out.println(deserializedShape.getShape());
            System.out.println(deserializedShape.getType());
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
