package Shape;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyShape implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1491858299155945874L;
	
	private Shape shape;
	private String type;
	private Color color;
	private String author;
	
	public MyShape(Shape shape, Color color, String type, String author) {
		this.shape = shape;
		this.color = color;
		this.type = type;
		this.author = author;
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getType() {
		return type;
	}
	
    private void writeObject(ObjectOutputStream oos) throws Exception 
    { 	
    	oos.defaultWriteObject();
    	
    	oos.writeUTF(type);
    	oos.writeUTF(Integer.toString(color.getRGB()));
    	oos.writeUTF(author);
    	
    	switch(type) {
    		case "java.awt.geom.Line2D$Double":
    			Line2D.Double line = (Line2D.Double) shape;
    			oos.writeDouble(line.getX1());
    			oos.writeDouble(line.getY1());
    			oos.writeDouble(line.getX2());
    			oos.writeDouble(line.getY2());
    			break;
    	}
    } 
  
    private void readObject(ObjectInputStream ois) throws Exception 
    { 
        ois.defaultReadObject(); 
        type = ois.readUTF();
        color = new Color(Integer.parseInt(ois.readUTF()));
        author = ois.readUTF();
    	switch(type) {
			case "java.awt.geom.Line2D$Double":
				shape = new Line2D.Double(ois.readDouble(), ois.readDouble(), ois.readDouble(), ois.readDouble());
				break;
    	}
    } 
}
