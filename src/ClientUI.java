
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
	private Color [] colors={Color.red,Color.black,Color.orange,Color.green, Color.pink,Color.blue,Color.cyan,Color.magenta,Color.YELLOW};
	private int x1, y1;
	private BasicStroke strock;
	private JComboBox<Integer> colorBox;
	
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
			btn.addActionListener(al);
			btn.setBounds(40+i*30, 15, 30, 30);
			drawControlPanel.add(btn);
		}
		colorBox =new JComboBox<Integer>();
		colorBox.setBounds(434, 11, 80, 30);
		drawControlPanel.add(colorBox);
		for (int i = 0; i < 10; i++) {
			Integer intdata = new Integer(i+1);
			colorBox.addItem(intdata);
		}
		mainPanel.add(drawControlPanel);
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
	
	ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton bt =(JButton)e.getSource();
			color =bt.getBackground();
		}
	};
	
	MouseAdapter ma = new MouseAdapter() {
		
		public void mousePressed(MouseEvent e) {
			x1 = e.getX();
			y1 = e.getY();
		};
		
		public void mouseEntered(MouseEvent e) {
			if(color==null){
				color=Color.black;
			}
			g.setColor(color);
		};
 
		public void mouseDragged(MouseEvent e) {
			int width=(int)colorBox.getSelectedItem();
			strock = new BasicStroke(width);
			g.setStroke(strock);
			
			int x2 = e.getX();
			int y2 = e.getY();
			g.drawLine(x1, y1, x2, y2);
			
			// set current point as the start point of next point
			x1 = x2;
			y1 = y2;
			
//			try {
//				
//				control.sendMsg1(socket.getOutputStream(), x1, y1, x2, y2,g.getColor().getRGB(),width);
//				x1 = x2;
//				y1 = y2;
//			} catch (IOException e1) {
//			}
		};
		
	};
	
	private final JList userList = new JList();
}
