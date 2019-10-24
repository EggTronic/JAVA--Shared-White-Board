/**
 * 
 */
package Shape;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyLine extends MyShape implements Serializable {

	private static final long serialVersionUID = 6260494015899899494L;
	
	/**
	 * @param shape
	 * @param color
	 * @param author
	 */
	public MyLine(Shape shape, Color color, String author, int thickness, Boolean fill) {
		super(shape, color, author, thickness, fill);
	}
	
	
	
    private void writeObject(ObjectOutputStream oos) throws Exception { 	
    	Line2D.Double line = (Line2D.Double) shape;
    	oos.defaultWriteObject();
    	oos.writeUTF(Integer.toString(color.getRGB()));
    	oos.writeUTF(author);
    	oos.writeInt(thickness);
    	oos.writeBoolean(fill);
		oos.writeDouble(line.getX1());
		oos.writeDouble(line.getY1());
		oos.writeDouble(line.getX2());
		oos.writeDouble(line.getY2());
    } 
  
    private void readObject(ObjectInputStream ois) throws Exception { 
        ois.defaultReadObject(); 
        color = new Color(Integer.parseInt(ois.readUTF()));
        author = ois.readUTF();
        thickness = ois.readInt();
        fill = ois.readBoolean();
		shape = new Line2D.Double(ois.readDouble(), ois.readDouble(), ois.readDouble(), ois.readDouble());
    } 

}
