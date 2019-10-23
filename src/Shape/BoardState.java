package Shape;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BoardState implements Serializable {
	
	private static final long serialVersionUID = -6319277311434675916L;
	private ArrayList<MyShape> shapes;

	public BoardState(ArrayList<MyShape> shapes) {
		this.shapes = shapes;
	}
	
	public ArrayList<MyShape> getShapes() {
		return shapes;
	}
	
	public synchronized void addShapes(MyShape shape) {
		shapes.add(shape);
	}

	public void setShapes(ArrayList<MyShape> shapes) {
		this.shapes = shapes;
	}
	
	public void New() {
		shapes = new ArrayList<MyShape>();
	}
	
	public void Save() {
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
		String date = df.format(new Date());
		try {
		    FileOutputStream fileOut = new FileOutputStream(date + ".ser");
		    ObjectOutputStream out = new ObjectOutputStream(fileOut);
		    out.writeObject(shapes);
		    out.close();
		    fileOut.close();
		}
		catch (IOException i) {
		    i.printStackTrace();
		}
	}
	
	public void SaveAs(String filename) {
		try {
		    FileOutputStream fileOut = new FileOutputStream(filename + ".ser");
		    ObjectOutputStream out = new ObjectOutputStream(fileOut);
		    out.writeObject(shapes);
		    out.close();
		    fileOut.close();
		}
		catch (IOException i) {
		    i.printStackTrace();
		}
	}
	
	public BoardState Open(String filename) throws ClassNotFoundException {
		BoardState state = null;
		System.out.println(filename);
		try {
			FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            @SuppressWarnings("unchecked")
			ArrayList<MyShape> shapes = (ArrayList<MyShape>) in.readObject();
            state = new BoardState(shapes);
		    in.close();
            fileIn.close();
		}
		catch (IOException i) {
		    i.printStackTrace();
		}
		return state;
	}

}
