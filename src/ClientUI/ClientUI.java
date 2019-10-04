package ClientUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import Shape.*;
import Text.MyText;

public class ClientUI {
	
	Dimension screenSize;
	
	private JList<Object> userList;
	private JFrame frame;
	private JButton sendBtn;
	private JPanel drawPanelHeader;
	private JPanel mainPanel;
	private JPanel drawControlPanel;
	private JPanel drawPanelBoard;
	private JTextField messageInputPanel;
	private JTextArea messageShowPanel;
	private Graphics2D g;
	private String [] options = {"free draw", "line", "rectangle", "circle", "oval", "text"};
	private Color color;
	private Color [] colors = {Color.GRAY, Color.LIGHT_GRAY, Color.darkGray, Color.black, Color.orange, Color.green, 
			                   Color.red, Color.pink, Color.blue, Color.cyan, Color.magenta, Color.YELLOW, 
			                   new Color(125, 55, 237), new Color(255, 99, 71), new Color(240, 230, 140), 
			                   new Color(0, 250, 154), new Color(0, 206, 209), new Color(238, 130, 238),
			                   Color.WHITE};
	
	private String shape = "free draw";
	private BoardState state = new BoardState(new ArrayList<MyShape>(), new ArrayList<MyText>());
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
	    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(0, 0, screenSize.width, screenSize.height); // full screen 
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen
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
		drawPanelHeader = new JPanel();
		drawPanelHeader.setBounds(0, 0, 1536, 54);
		drawPanelHeader.setPreferredSize(new Dimension(0, 20));
		mainPanel.add(drawPanelHeader);
		drawPanelHeader.setLayout(null);
		
		openBtn = new JButton("Open");
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser= new JFileChooser();
				String path = Paths.get("").toAbsolutePath().toString();
                chooser.setCurrentDirectory(new File(path));
                chooser.setFileFilter(new FileNameExtensionFilter("ser","SER"));
                int value = chooser.showOpenDialog(null);
                File f= chooser.getSelectedFile();
                String filename= f.getAbsolutePath();
                try {
					state = state.Open(filename);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
                Draw();
			}
		});
		openBtn.setBounds((int) (screenSize.width*0.63), 11, 89, 23);
		drawPanelHeader.add(openBtn);
		
		saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				state.Save();
			}
		});
		saveBtn.setBounds((int) (screenSize.width*0.63) + 100, 11, 89, 23);
		drawPanelHeader.add(saveBtn);
		
		saveAsBtn = new JButton("Save As");
		saveAsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser= new JFileChooser();
				String path = Paths.get("").toAbsolutePath().toString();
                chooser.setCurrentDirectory(new File(path));
                chooser.setFileFilter(new FileNameExtensionFilter("ser","SER"));
                chooser.setApproveButtonText("Save As");
                chooser.setDialogTitle("Save As");
                int value = chooser.showOpenDialog(null);
                File f= chooser.getSelectedFile();
                String filename= f.getAbsolutePath();
				state.SaveAs(filename);
                Draw();
			}
		});
		saveAsBtn.setBounds((int) (screenSize.width*0.63) + 200, 11, 89, 23);
		drawPanelHeader.add(saveAsBtn);
	}
	
	private void initMessagePanel() {
		JPanel messagePanel = new JPanel();
		messagePanel.setBounds((int) (screenSize.width*0.8), 0, (int) (screenSize.width*0.2), (int) (screenSize.height*0.87));
		messagePanel.setPreferredSize(new Dimension(200, 0));
		messagePanel.setLayout(null);
		userList = new JList<Object>();
		userList.setBounds((int) (screenSize.width*0.1), 0, (int) (screenSize.width*0.1), (int) (screenSize.height*0.87));
		messagePanel.add(userList);
		messageShowPanel = new JTextArea();
		messageShowPanel.setBounds(0, 0, (int) (screenSize.width*0.1), (int) (screenSize.height*0.87));
		messagePanel.add(messageShowPanel);
		messageShowPanel.setBackground(Color.DARK_GRAY);
		messageShowPanel.setLineWrap(true);
		mainPanel.add(messagePanel);
	}
	
	private void initDrawControlPanel() {
		drawControlPanel = new JPanel();
		drawControlPanel.setBounds(0, (int) (screenSize.height*0.87), (int) (screenSize.width*0.8), (int) (screenSize.height*0.13));
		drawControlPanel.setLayout(null);
		drawControlPanel.setBackground(SystemColor.controlHighlight);
		drawControlPanel.setPreferredSize(new Dimension(0,60));
		
		for (int i = 0; i < colors.length; i++) {
			JButton btn = new JButton();
			btn.setBackground(colors[i]);
			btn.addActionListener(colorSelectAL);
			btn.setBounds(20+i*45, 10, 40, 30);
			drawControlPanel.add(btn);
		}
		
		for (int i = 0; i < options.length; i++) {
			JButton btn = new JButton();
			btn.setText(options[i]);;
			btn.addActionListener(shapeSelectAL);
			btn.setBounds(20+i*104, 45, 90, 25);
			drawControlPanel.add(btn);
		}
		
		thicknessSelector =new JComboBox<Integer>();
		thicknessSelector.setBounds((int) (screenSize.width*0.7), 10, 80, 30);
		drawControlPanel.add(thicknessSelector);
		for (int i = 0; i < 10; i++) {
			Integer intdata = new Integer(i+1);
			thicknessSelector.addItem(intdata);
		}

		fillSelector = new JCheckBox("Fill");
		fillSelector.setBackground(Color.LIGHT_GRAY);
		fillSelector.setBounds((int) (screenSize.width*0.7), 45, 80, 23);
		drawControlPanel.add(fillSelector);
		mainPanel.add(drawControlPanel);
	}
	
	private void initMessageControlPanel() {
		JPanel messageControlPanel = new JPanel();
		messageControlPanel.setBackground(SystemColor.activeCaptionBorder);
		messageControlPanel.setBounds((int) (screenSize.width*0.8), (int) (screenSize.height*0.87), (int) (screenSize.width*0.2), (int) (screenSize.height*0.13));
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
		drawPanelBoard.setBounds(0, (int) (screenSize.height*0.05), (int) (screenSize.width*0.8), (int) (screenSize.height*0.82));
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
			switch(shape) {
				case "text":
					String text = JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
		                    "Input your text", "");
					int size = (int)thicknessSelector.getSelectedItem()*10;
					state.getTexts().add(new MyText(text, (float) x1, (float) y1, color, size, username));
					Draw();
				default:
			}
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
				case "free draw":
					Shape line = new Line2D.Double(x1, y1, x2, y2);
					state.getShapes().add(new MyLine(line, color, username, thickness, fill));
					shapesPreview.add(new MyLine(line, color, username, thickness, fill));
					DrawPreview();
					
					// set current point as the start point of next point
					x1 = x2;
					y1 = y2;
					break;
				
				case "text":
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
			shapesPreview.clear();
			
			switch(shape) {
				case "free draw":
					shapesPreview.clear();
					Draw();
					break;
					
				case "line":
					s =  new Line2D.Double(x1, y1, e.getX(), e.getY());
					state.getShapes().add(new MyLine(s, color, username, (int)strock.getLineWidth(), fill));
					Draw();
					break;
					
				case "rectangle":
					s = ShapeMaker.makeRectangle(x1, y1, e.getX(), e.getY());
					state.getShapes().add(new MyRectangle(s, color, username, (int)strock.getLineWidth(), fill));
					Draw();
					break;
					
				case "circle":
					s = ShapeMaker.makeCircle(x1, y1, e.getX(), e.getY());
					state.getShapes().add(new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill));
					Draw();
					break;
				
				case "oval":
					s = ShapeMaker.makeOval(x1, y1, e.getX(), e.getY());
					state.getShapes().add(new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill));
					Draw();
					break;
					
				default:
					System.out.println("Unsupported Shape");
			}
	    }
		
	};
	private JButton openBtn;
	private JButton saveBtn;
	private JButton saveAsBtn;

	private void Draw() {
		Clear();
		for (MyShape s : state.getShapes()) {
			strock = new BasicStroke(s.getThickness());
			g.setStroke(strock);
			g.setPaint(s.getColor());
	        g.draw(s.getShape());
	        if (s.getFill()) {
	        	g.fill(s.getShape());
	        }
	     }
		for (MyText t : state.getTexts()) {
			g.setFont(new Font("TimesRoman", Font.PLAIN, t.getSize()));
			g.setPaint(t.getColor());
			g.drawString(t.getText(), t.getX(), t.getY());
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
		Shape s = ShapeMaker.makeRectangle(0, 0, (int) (screenSize.width), (int) (screenSize.height));
		g.draw(s);
		g.fill(s);
	}
}
