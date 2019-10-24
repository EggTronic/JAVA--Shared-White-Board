package Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class ShapeMaker {
	public static Rectangle2D.Double makeRectangle(int x1, int y1, int x2, int y2) {
	    return new Rectangle2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
	}
	
	public static Ellipse2D.Double makeCircle(int x1, int y1, int x2, int y2) {
	    return new Ellipse2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.min(Math.abs(x1 - x2), Math.abs(y1 - y2)), Math.min(Math.abs(x1 - x2), Math.abs(y1 - y2)));
	}
	
	public static Ellipse2D.Double makeOval(int x1, int y1, int x2, int y2) {
	    return new Ellipse2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
	}
}
