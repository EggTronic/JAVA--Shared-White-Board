/**
 * 
 */
package Shape;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyEllipse extends MyShape implements Serializable {

	private static final long serialVersionUID = -8109931526025198031L;

	/**
	 * @param shape
	 * @param color
	 * @param author
	 */
	public MyEllipse(Shape shape, Color color, String author, int thickness, Boolean fill) {
		super(shape, color, author, thickness, fill);
	}

	private void writeObject(ObjectOutputStream oos) throws Exception { 	
    	oos.defaultWriteObject();
    	oos.writeUTF(Integer.toString(color.getRGB()));
    	oos.writeUTF(author);
    	oos.writeInt(thickness);
    	oos.writeBoolean(fill);

    	Ellipse2D.Double ellipse = (Ellipse2D.Double) shape;
		oos.writeDouble(ellipse.getX());
		oos.writeDouble(ellipse.getY());
		oos.writeDouble(ellipse.getWidth());
    	oos.writeDouble(ellipse.getHeight());
    } 
  
    private void readObject(ObjectInputStream ois) throws Exception { 
        ois.defaultReadObject(); 
        color = new Color(Integer.parseInt(ois.readUTF()));
        author = ois.readUTF();
        thickness = ois.readInt();
        fill = ois.readBoolean();
		shape = new Ellipse2D.Double(ois.readDouble(), ois.readDouble(), ois.readDouble(), ois.readDouble());
    } 
    
}
