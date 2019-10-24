package Shape;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 */
public class MyRectangle extends MyShape implements Serializable {

	private static final long serialVersionUID = 816998434138837305L;

	/**
	 * @param shape
	 * @param color
	 * @param author
	 */
	public MyRectangle(Shape shape, Color color, String author, int thickness, Boolean fill) {
		super(shape, color, author, thickness, fill);
	}
	
    private void writeObject(ObjectOutputStream oos) throws Exception { 	
    	oos.defaultWriteObject();
    	oos.writeUTF(Integer.toString(color.getRGB()));
    	oos.writeUTF(author);
    	oos.writeInt(thickness);
    	oos.writeBoolean(fill);
		Rectangle2D.Double rectangle = (Rectangle2D.Double) shape;
		oos.writeDouble(rectangle.getX());
		oos.writeDouble(rectangle.getY());
		oos.writeDouble(rectangle.getWidth());
    	oos.writeDouble(rectangle.getHeight());
    } 
  
    private void readObject(ObjectInputStream ois) throws Exception { 
        ois.defaultReadObject(); 
        color = new Color(Integer.parseInt(ois.readUTF()));
        author = ois.readUTF();
        thickness = ois.readInt();
        fill = ois.readBoolean();
		shape = new Rectangle2D.Double(ois.readDouble(), ois.readDouble(), ois.readDouble(), ois.readDouble());
    } 

}
