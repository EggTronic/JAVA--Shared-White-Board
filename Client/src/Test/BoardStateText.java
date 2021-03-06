package Test;
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
import java.util.ArrayList;

import Shape.BoardState;
import Shape.MyEllipse;
import Shape.MyLine;
import Shape.MyRectangle;
import Shape.MyShape;
import Shape.MyText;

public class BoardStateText {
	public static void main(String[] args) {
		ArrayList<MyShape> shapes = new ArrayList<MyShape>();
		
		Shape line = new Line2D.Double(10, 2, 3, 4);
		Shape rectangle = new Rectangle2D.Double(10, 2, 3, 4);
		Shape ellipse = new Ellipse2D.Double(10, 2, 3, 4);
		
		MyShape myline = new MyLine(line, Color.red, "wow", 1, false);
		MyShape myrectangle = new MyRectangle(rectangle, Color.white, "lol", 2, true);
		MyShape myellipse = new MyEllipse(ellipse, Color.black, "=w=", 3, true);
		MyShape myText = new MyText("text", (float) 5, (float) 10, Color.black, 20, "yang");
		
		shapes.add(myline);
		shapes.add(myrectangle);
		shapes.add(myellipse);
		shapes.add(myText);
		
		BoardState state = new BoardState(shapes);
		
		 // Serialization code
        try
        {
            FileOutputStream fileOut = new FileOutputStream("state.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(state);
            out.close();
            fileOut.close();
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        
        // De-serialization code
        BoardState deserializedState = null;

        try
        {
            FileInputStream fileIn = new FileInputStream("state.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            deserializedState = (BoardState) in.readObject();
            in.close();
            fileIn.close();
 
            // verify the object state
            System.out.println("==========state-shapes========");
            shapes = deserializedState.getShapes();
            for (MyShape s: shapes) {
            	System.out.println(s.getClass().toString());
            	if (s.getClass().toString().equals(MyText.class.toString())) {
            		MyText t = (MyText) s;
            		System.out.println(t.getAuthor());
                    System.out.println(t.getColor());
                    System.out.println(t.getText());
                    System.out.println(t.getThickness());
                    System.out.println(t.getX());
                    System.out.println(t.getY());
                    
            	} else {
                	System.out.println(s.getAuthor());
                    System.out.println(s.getColor());
                    System.out.println(s.getShape());
                    System.out.println(s.getThickness());
                    System.out.println(s.getFill());
            	}

            }

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
