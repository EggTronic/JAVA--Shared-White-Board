package Shape;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
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
		Shape ellipse = new Ellipse2D.Double(10, 2, 3, 4);
		MyShape myline = new MyLine(line, Color.red, "wow", 1, false);
		MyShape myrectangle = new MyRectangle(rectangle, Color.white, "lol", 2, true);
		MyShape myellipse = new MyEllipse(ellipse, Color.black, "=w=", 3, true);
		
		 // Serialization code
        try
        {
            FileOutputStream fileOut = new FileOutputStream("shape.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(myline);
            out.writeObject(myrectangle);
            out.writeObject(myellipse);
            out.close();
            fileOut.close();
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        
        // De-serialization code
        MyShape deserializedLine = null;
        MyShape deserializedRectangle = null;
        MyShape deserializedEllipse = null;
        try
        {
            FileInputStream fileIn = new FileInputStream("shape.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            deserializedLine = (MyShape) in.readObject();
            deserializedRectangle = (MyShape) in.readObject();
            deserializedEllipse = (MyShape) in.readObject();
            in.close();
            fileIn.close();
 
            // verify the object state
            System.out.println("==========Line========");
            System.out.println(deserializedLine.getAuthor());
            System.out.println(deserializedLine.getColor());
            System.out.println(deserializedLine.getShape());
            System.out.println(deserializedLine.getThickness());
            System.out.println(deserializedLine.getFill());
            System.out.println("==========Rectangle========");
            System.out.println(deserializedRectangle.getAuthor());
            System.out.println(deserializedRectangle.getColor());
            System.out.println(deserializedRectangle.getShape());
            System.out.println(deserializedRectangle.getThickness());
            System.out.println(deserializedRectangle.getFill());
            System.out.println("==========Ellipse========");
            System.out.println(deserializedEllipse.getAuthor());
            System.out.println(deserializedEllipse.getColor());
            System.out.println(deserializedEllipse.getShape());
            System.out.println(deserializedEllipse.getThickness());
            System.out.println(deserializedEllipse.getFill());
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
