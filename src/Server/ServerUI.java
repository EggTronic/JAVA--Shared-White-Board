package Server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import Utils.ImageResizer;

public class ServerUI {
	
	private static JFrame frame;
	
	private Dimension screenSize;
	
	private static JPanel homePanel;
	
	protected static JTextPane logPane;
	
	public static void main(String[] args) {		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerUI window = new ServerUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private ServerUI() {
		initialize();
	}
	
	private void initialize() {
		frame = new JFrame();
	    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(0, 0, screenSize.width, screenSize.height); // full screen 
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		
		initHomePanel();

		frame.getContentPane().add(homePanel);
		frame.setVisible(true);
	}
	
	private void initHomePanel() {

		homePanel = new JPanel();
		homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
		JPanel boardInfoPanel = new JPanel();
		boardInfoPanel.setLayout(null);
		
		JLabel background = new JLabel();
		background.setBounds(0, 0, (int) (screenSize.width), (int) (screenSize.height));
		background.setIcon(ImageResizer.reSizeForLabel(new ImageIcon("images/home.png"), background));
		homePanel.add(background);
		
		Font font = new Font("TimesRoman", Font.BOLD, 20);

		JTextArea roomSize= new JTextArea();
		JTextArea ipInput= new JTextArea();
		JTextArea portInput= new JTextArea();
		JLabel roomSizeLabel = new JLabel("Room Size: ");
		JLabel ipInputLabel = new JLabel("IP Address: ");
		JLabel portInputLabel = new JLabel("Port: ");
		
		roomSize.setFont(font);
		ipInput.setFont(font);
		portInput.setFont(font);
		roomSizeLabel.setFont(font);
		ipInputLabel.setFont(font);
		portInputLabel.setFont(font);
		
		roomSize.setBackground(Color.black);
		roomSize.setForeground(Color.white);
		ipInput.setBackground(Color.black);
		ipInput.setForeground(Color.white);
		portInput.setBackground(Color.black);
		portInput.setForeground(Color.white);
		
		roomSize.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.3), (int) (screenSize.height*0.2), 25);
		ipInput.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.4), (int) (screenSize.height*0.2), 25);
		portInput.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.5), (int) (screenSize.height*0.2), 25);
		roomSizeLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.3), (int) (screenSize.height*0.2), 25);
		ipInputLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.4), (int) (screenSize.height*0.2), 25);
		portInputLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.5), (int) (screenSize.height*0.2), 25);

		boardInfoPanel.add(roomSize);
		boardInfoPanel.add(ipInput);
		boardInfoPanel.add(portInput);
		boardInfoPanel.add(roomSizeLabel);
		boardInfoPanel.add(ipInputLabel);
		boardInfoPanel.add(portInputLabel);
		
		logPane = new JTextPane();
		logPane.setBounds((int) (screenSize.width*0.4), (int) (screenSize.height*0.1), (int) (screenSize.width*0.5), (int) (screenSize.height*0.7));
		logPane.setBackground(Color.BLACK);
		boardInfoPanel.add(logPane);
		
		homePanel.add(boardInfoPanel);
		frame.getContentPane().add(homePanel);
		boardInfoPanel.setComponentZOrder(background, 7);
	}
	

}
