package Server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import Utils.*;
import PublishSubscribeSystem.*;


public class ServerUI {
	
	private static final String DEFAULT_ROOMSIZE = "20";
	private static final String DEFAULT_POOLSIZE = "20";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "8002";
	
	private static MessageAppender messageAppender = new MessageAppender();
	private static JFrame frame;
	private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static JPanel homePanel;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");  
	
	protected static JTextPane logPane;
	
	public static void main(String[] args) {		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new ServerUI();
					frame.setVisible(true);
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

		JTextArea roomSize= new JTextArea(DEFAULT_ROOMSIZE);
		JTextArea poolSize= new JTextArea(DEFAULT_POOLSIZE);
		JTextArea ipInput= new JTextArea(DEFAULT_HOST);
		JTextArea portInput= new JTextArea(DEFAULT_PORT);
		
		JLabel roomSizeLabel = new JLabel("Room Size: ");
		JLabel poolSizeLabel = new JLabel("Pool Size: ");
		JLabel ipInputLabel = new JLabel("IP Address: ");
		JLabel portInputLabel = new JLabel("Port: ");
		
		roomSize.setFont(font);
		poolSize.setFont(font);
		ipInput.setFont(font);
		portInput.setFont(font);
		
		roomSizeLabel.setFont(font);
		poolSizeLabel.setFont(font);
		ipInputLabel.setFont(font);
		portInputLabel.setFont(font);
		
		roomSize.setBackground(Color.black);
		poolSize.setBackground(Color.black);
		ipInput.setBackground(Color.black);
		portInput.setBackground(Color.black);
		
		roomSize.setForeground(Color.white);
		poolSize.setForeground(Color.white);
		ipInput.setForeground(Color.white);
		portInput.setForeground(Color.white);
		
		roomSize.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.2), (int) (screenSize.height*0.2), 25);
		poolSize.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.3), (int) (screenSize.height*0.2), 25);
		ipInput.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.4), (int) (screenSize.height*0.2), 25);
		portInput.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.5), (int) (screenSize.height*0.2), 25);
		
		roomSizeLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.2), (int) (screenSize.height*0.2), 25);
		poolSizeLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.3), (int) (screenSize.height*0.2), 25);
		ipInputLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.4), (int) (screenSize.height*0.2), 25);
		portInputLabel.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.5), (int) (screenSize.height*0.2), 25);

		boardInfoPanel.add(roomSize);
		boardInfoPanel.add(poolSize);
		boardInfoPanel.add(ipInput);
		boardInfoPanel.add(portInput);
		boardInfoPanel.add(roomSizeLabel);
		boardInfoPanel.add(poolSizeLabel);
		boardInfoPanel.add(ipInputLabel);
		boardInfoPanel.add(portInputLabel);
		
		logPane = new JTextPane();
		logPane.setBounds((int) (screenSize.width*0.4), (int) (screenSize.height*0.1), (int) (screenSize.width*0.5), (int) (screenSize.height*0.7));
		logPane.setBackground(Color.BLACK);
		boardInfoPanel.add(logPane);
		
		JButton startButton = new JButton();
		startButton.setToolTipText("Start Server");
		startButton.setBounds((int) (screenSize.width*0.15), (int) (screenSize.height*0.6), (int) (screenSize.height*0.1), (int) (screenSize.height*0.1));
		startButton.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/new.png"), startButton));
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// get port/host...etc
				int rs = Integer.parseInt(roomSize.getText());
				int ps = Integer.parseInt(poolSize.getText());
				String ip = ipInput.getText();
				int port = Integer.parseInt(portInput.getText());
				PublishSubscribeSystem.getInstance().setRoomSize(rs);
				
				// validate port/host...etc
				try {
					Server newserver = new Server(port, ip);
					Thread t = new Thread(newserver);
					t.start();
				}
				catch (IOException ex){
					ex.printStackTrace();
				}


				
				// start server thread here
			}
		});
		boardInfoPanel.add(startButton);
		
		JButton closeButton = new JButton();
		closeButton.setToolTipText("Close Server");
		closeButton.setBounds((int) (screenSize.width*0.25), (int) (screenSize.height*0.6), (int) (screenSize.height*0.1), (int) (screenSize.height*0.1));
		closeButton.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/close.png"), closeButton));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// close server thread here
				try {
					PublishSubscribeSystem.getInstance().disconnectServer();
				}
				catch(IOException ex){

					ex.printStackTrace();

				}
			}
		});
		boardInfoPanel.add(closeButton);
		
		homePanel.add(boardInfoPanel);
		frame.getContentPane().add(homePanel);
		boardInfoPanel.setComponentZOrder(background, 11);
		
		messageAppender.appendToMessagePane(logPane, "Welcome to Board Server | Current Time: ", Color.WHITE, true);
  	    messageAppender.appendToMessagePane(logPane, dtf.format(LocalDateTime.now()) + "\n\n", Color.WHITE, true);
	}
	

}
