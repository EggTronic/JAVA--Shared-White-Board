package Shape;
import java.awt.Color;
import java.awt.Shape;
import java.io.Serializable;

public class MyShape implements Serializable {
	
	private static final long serialVersionUID = -1061519381093537563L;
	protected Shape shape;
	protected Color color;
	protected String author;
	protected int thickness;
	protected Boolean fill;
	
	public MyShape(Shape shape, Color color, String author, int thickness, Boolean fill) {
		this.shape = shape;
		this.color = color;
		this.author = author;
		this.thickness = thickness;
		this.fill = fill;
	}
	
	public MyShape(Color color, String author, int thickness) {
		this.color = color;
		this.author = author;
		this.thickness = thickness;
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
	
	public int getThickness() {
		return thickness;
	}
	
	public Boolean getFill() {
		return fill;
	}
}
