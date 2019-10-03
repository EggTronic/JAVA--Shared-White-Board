package ClientUI;
import javax.swing.*;

import Shape.MyShape;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import Shape.*;

public class ClientUI {

	private JFrame frame;
	private JButton sendBtn;
	private JLabel drawPanelHeader;
	private JPanel mainPanel;
	private JPanel drawControlPanel;
	private JPanel drawPanelBoard;
	private JTextField messageInputPanel;
	private JTextArea messageShowPanel;
	private Graphics2D g;
	private Color color;
	private Color [] colors = {Color.red,Color.black,Color.orange,Color.green, Color.pink,Color.blue,Color.cyan,Color.magenta,Color.YELLOW};
	private String shape = "line";
	private String [] shapeEnum = {"line", "rectangle", "circle", "oval"};
	private ArrayList<MyShape> shapes = new ArrayList<MyShape>();
	private ArrayList<MyShape> shapesPreview = new ArrayList<MyShape>();
	private int x1, y1, x2 , y2;
	private BasicStroke strock;
	private JComboBox<Integer> thicknessSelector;
	private JCheckBox fillSelector;
	private Boolean fill;
	
	private String username = "default";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientUI window = new ClientUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 775, 551);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));

		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		
		initMessagePanel();
		initDrawControlPanel();
		initMessageControlPanel();
		initDrawPanelBoard();
		initDrawPanelHeader();
		
		frame.getContentPane().add(mainPanel);
		frame.setVisible(true);
		g = (Graphics2D)drawPanelBoard.getGraphics();
		
	}
	
	private void initDrawPanelHeader() {
		drawPanelHeader = new JLabel();
		drawPanelHeader.setBounds(0, 0, 524, 20);
		drawPanelHeader.setPreferredSize(new Dimension(0, 20));
		mainPanel.add(drawPanelHeader);
	}
	
	private void initMessagePanel() {
		JPanel messagePanel = new JPanel();
		messagePanel.setBounds(525, 0, 234, 476);
		messagePanel.setPreferredSize(new Dimension(200, 0));
		messagePanel.setLayout(null);
		userList.setBounds(161, -126, 73, 601);
		messagePanel.add(userList);
		messageShowPanel = new JTextArea();
		messageShowPanel.setBounds(0, 0, 159, 475);
		messagePanel.add(messageShowPanel);
		messageShowPanel.setBackground(Color.DARK_GRAY);
		messageShowPanel.setLineWrap(true);
		mainPanel.add(messagePanel);
	}
	
	private void initDrawControlPanel() {

		drawControlPanel = new JPanel();
		drawControlPanel.setBounds(0, 437, 524, 75);
		
		drawControlPanel.setLayout(null);
		drawControlPanel.setBackground(Color.gray);
		drawControlPanel.setPreferredSize(new Dimension(0,60));
		for (int i = 0; i < colors.length; i++) {
			JButton btn = new JButton();
			btn.setBackground(colors[i]);
			btn.addActionListener(colorSelectAL);
			btn.setBounds(20+i*45, 10, 40, 30);
			drawControlPanel.add(btn);
		}
		
		for (int i = 0; i < shapeEnum.length; i++) {
			JButton btn = new JButton();
			btn.setText(shapeEnum[i]);;
			btn.addActionListener(shapeSelectAL);
			btn.setBounds(20+i*104, 45, 90, 25);
			drawControlPanel.add(btn);
		}
		
		thicknessSelector =new JComboBox<Integer>();
		thicknessSelector.setBounds(434, 10, 80, 30);
		drawControlPanel.add(thicknessSelector);
		for (int i = 0; i < 10; i++) {
			Integer intdata = new Integer(i+1);
			thicknessSelector.addItem(intdata);
		}
		
		mainPanel.add(drawControlPanel);
		
		fillSelector = new JCheckBox("Fill");
		fillSelector.setBackground(Color.LIGHT_GRAY);
		fillSelector.setBounds(434, 45, 80, 23);
		drawControlPanel.add(fillSelector);
		
		
	}
	
	private void initMessageControlPanel() {
		JPanel messageControlPanel = new JPanel();
		messageControlPanel.setBounds(525, 476, 234, 36);
		mainPanel.add(messageControlPanel);
		messageControlPanel.setPreferredSize(new Dimension(0, 50));
		messageInputPanel = new JTextField(11);
		sendBtn = new JButton();
		sendBtn.setText("send");
		messageControlPanel.add(messageInputPanel);
		messageControlPanel.add(sendBtn);
	}
	
	private void initDrawPanelBoard() {
		drawPanelBoard = new JPanel();
		drawPanelBoard.setBounds(0, 22, 524, 417);
		drawPanelBoard.setBackground(Color.WHITE);
		drawPanelBoard.addMouseListener(ma);
		drawPanelBoard.addMouseMotionListener(ma);
		mainPanel.add(drawPanelBoard);
	}
	
	ActionListener colorSelectAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton bt =(JButton)e.getSource();
			color =bt.getBackground();
		}
	};
	
	ActionListener shapeSelectAL = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton bt =(JButton)e.getSource();
			shape =bt.getText();
		}
	};
	
	MouseAdapter ma = new MouseAdapter() {
		
		public void mousePressed(MouseEvent e) {
			x1 = e.getX();
			y1 = e.getY();
		}
		
		public void mouseEntered(MouseEvent e) {
			if(color==null){
				color=Color.black;
			}
			g.setColor(color);
		}
 
		public void mouseDragged(MouseEvent e) {
			int thickness=(int)thicknessSelector.getSelectedItem();
			strock = new BasicStroke(thickness);
			g.setStroke(strock);
			fill = fillSelector.isSelected();
			x2 = e.getX();
			y2 = e.getY();
			
			switch(shape) {
				case "line":
					Shape line = new Line2D.Double(x1, y1, x2, y2);
					shapes.add(new MyLine(line, color, username, thickness, fill));
					shapesPreview.add(new MyLine(line, color, username, thickness, fill));
					DrawPreview();
					
					// set current point as the start point of next point
					x1 = x2;
					y1 = y2;
					break;
				
				default:
					Shape lineY = new Line2D.Double(x1, y1, x1, y2);
					Shape lineX = new Line2D.Double(x1, y1, x2, y1);
					shapesPreview.add(new MyLine(lineY, color, username, thickness, fill));
					shapesPreview.add(new MyLine(lineX, color, username, thickness, fill));
					DrawPreview();
					break;
			}	
//			try {
//				
//				control.sendMsg1(socket.getOutputStream(), x1, y1, x2, y2,g.getColor().getRGB(),width);
//				x1 = x2;
//				y1 = y2;
//			} catch (IOException e1) {
//			}
		}
		
		public void mouseReleased(MouseEvent e) {
			Shape s;
			switch(shape) {
				case "line":
					shapesPreview.clear();
					Draw();
					break;
					
				case "rectangle":
					s = ShapeMaker.makeRectangle(x1, y1, e.getX(), e.getY());
					shapes.add(new MyRectangle(s, color, username, (int)strock.getLineWidth(), fill));
					shapesPreview.clear();
					Draw();
					break;
					
				case "circle":
					s = ShapeMaker.makeCircle(x1, y1, e.getX(), e.getY());
					shapes.add(new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill));
					shapesPreview.clear();
					Draw();
					break;
				
				case "oval":
					s = ShapeMaker.makeOval(x1, y1, e.getX(), e.getY());
					shapes.add(new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill));
					shapesPreview.clear();
					Draw();
					break;
					
				default:
					System.out.println("Unsupported Shape");
			}

	    }
		
	};


	
	private void Draw() {
		Clear();
		for (MyShape s : shapes) {
			strock = new BasicStroke(s.getThickness());
			g.setStroke(strock);
			g.setPaint(s.getColor());
	        g.draw(s.getShape());
	        if (s.getFill()) {
	        	g.fill(s.getShape());
	        }
	     }
	}
	
	private void DrawPreview() {
		for (MyShape s : shapesPreview) {
	        g.setPaint(s.getColor());
	        g.draw(s.getShape());
	        if (s.getFill()) {
	        	g.fill(s.getShape());
	        }
	      }
	} 
	
	private void Clear() {
		g.setPaint(Color.WHITE);
		Shape s = ShapeMaker.makeRectangle(0, 0, 1000, 1000);
		g.draw(s);
		g.fill(s);
	}
	
	private final JList userList = new JList();
}
