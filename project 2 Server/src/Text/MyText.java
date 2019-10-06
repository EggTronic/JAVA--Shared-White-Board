package Text;

import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MyText implements Serializable {

    private static final long serialVersionUID = -112212306651363465L;
    private String text;
    private Float xCoordinate;
    private Float yCoordinate;
    private Color color;
    private int size;
    private String author;

    public MyText(String text, Float xCoordinate, Float yCoordinate, Color color, int size, String author) {
        this.text = text;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.color = color;
        this.size = size;
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public Float getX() {
        return xCoordinate;
    }

    public Float getY() {
        return yCoordinate;
    }

    public Color getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public String getAuthor() {
        return author;
    }


    private void writeObject(ObjectOutputStream oos) throws Exception {
        oos.defaultWriteObject();
        oos.writeUTF(text);
        oos.writeFloat(xCoordinate);
        oos.writeFloat(yCoordinate);
        oos.writeUTF(Integer.toString(color.getRGB()));
        oos.writeInt(size);
        oos.writeUTF(author);
    }

    private void readObject(ObjectInputStream ois) throws Exception {
        ois.defaultReadObject();
        text = ois.readUTF();
        xCoordinate = ois.readFloat();
        yCoordinate = ois.readFloat();
        color = new Color(Integer.parseInt(ois.readUTF()));
        size = ois.readInt();
        author = ois.readUTF();
    }
}
