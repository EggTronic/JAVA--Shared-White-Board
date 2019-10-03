package ClientUI;

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

import Shape.MyShape;
import Text.MyText;

public class BoardState implements Serializable {
	
	private static final long serialVersionUID = -6319277311434675916L;
	private ArrayList<MyShape> shapes;
	private ArrayList<MyText> texts;

	public BoardState(ArrayList<MyShape> shapes, ArrayList<MyText> texts) {
		this.shapes = shapes;
		this.texts = texts;
	}
	
	public ArrayList<MyShape> getShapes() {
		return shapes;
	}
	
	public ArrayList<MyText> getTexts() {
		return texts;
	}
	
	public void setShapes(ArrayList<MyShape> shapes) {
		this.shapes = shapes;
	}
	
	public void setTexts(ArrayList<MyText> texts) {
		this.texts = texts;
	}
	
	
	public void Save() {
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
		String date = df.format(new Date());
		try {
		    FileOutputStream fileOut = new FileOutputStream(date + ".ser");
		    ObjectOutputStream out = new ObjectOutputStream(fileOut);
		    out.writeObject(this);
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
		    out.writeObject(this);
		    out.close();
		    fileOut.close();
		}
		catch (IOException i) {
		    i.printStackTrace();
		}
	}
	
	public BoardState Open() throws ClassNotFoundException {
		String filename = "asd.ser";
		BoardState state = null;
		try {
			FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
		    state = (BoardState)in.readObject();
		    in.close();
            fileIn.close();
		}
		catch (IOException i) {
		    i.printStackTrace();
		}
		return state;
	}

}
