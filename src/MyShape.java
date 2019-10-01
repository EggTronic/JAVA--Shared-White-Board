import java.awt.Color;
import java.awt.Shape;

public class MyShape {
	private Shape shape;
	private Color color;
	private String author;
	
	public MyShape(Shape shape, Color color, String author) {
		this.shape = shape;
		this.color = color;
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
}
