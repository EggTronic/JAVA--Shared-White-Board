package ClientUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Exceptions.AbnormalCommunicationException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import Shape.*;
import Utils.*;

public class ClientUI {
	
	private static final String DEFAULT_USERNAME = "ChenHaoNan";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "8002";
	
	private static Thread clientThread;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");  
	private static int time = 60000;
	private static Client client;
	
	private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static MessageAppender messageAppender = new MessageAppender();
	private static ArrayList<String> tempUserList;
	private static JList<Object> userList;
	private static DefaultListModel<Object> users = new DefaultListModel<Object>();;
	private static BoardState state = new BoardState(new ArrayList<MyShape>());
	private String [] options = {"free draw", "line", "rectangle", "circle", "oval", "text", "eraser"};
	protected static Color color;
	private Color [] colors = {Color.GRAY, Color.LIGHT_GRAY, Color.darkGray, Color.black, Color.orange, Color.green, 
			                   Color.red, Color.pink, Color.blue, Color.cyan, Color.magenta, Color.YELLOW, 
			                   new Color(125, 55, 237), new Color(255, 99, 71), new Color(240, 230, 140), 
			                   new Color(0, 250, 154), new Color(0, 206, 209), new Color(238, 130, 238)};
	
	private String shape = "free draw";

	private int x1, y1, x2 , y2;
	private static BasicStroke strock;
	private JSpinner thicknessSelector;
	private JCheckBox fillSelector;
	private boolean fill;
	private static String username = "";
	
	private volatile static boolean boardOwner = false;
	private volatile static boolean enterBoard = false;
	private volatile static boolean pending = false;
	private volatile static boolean connected = false;
	
	protected volatile static boolean error;
	protected volatile static String errorMsg;
	
	private static JFrame frame;
	private static JPanel mainPanel;
	private static JPanel homePanel;
	private JPanel drawPanelHeader;
	private JPanel boardInfoPanel;
	private JPanel drawControlPanel;
	private JPanel drawPanelBoard;
	private JTextField messageInputPanel;
	private static JTextPane messageShowPanel;
	private static Graphics2D g;

	private JButton sendBtn;
	private JButton returnBtn;
	private JButton openBtn;
	private JButton saveBtn;
	private JButton saveAsBtn;
	private JButton newBtn;

	/**
	 * Launch the application.
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new ClientUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		client = new Client();
		
		Runnable listeningServer = new Runnable() {
			 @Override
	            public void run() {
				 	String content;	
					try {
						while(true) {
							if (!connected || !client.getBufferReader().ready()) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}

							if(connected && client.getBufferReader().ready()) {
								  content = client.getBufferReader().readLine();
								  System.out.println(content.toString());
							  	  JSONParser parser = new JSONParser();
							      JSONObject temp = (JSONObject) parser.parse(content);
							      
							      if (error == true) {
							    	  System.out.println("Alert: " + errorMsg);
							    	  error = false;
							      }
							      
							      // receive shape from other user
							      if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Info")) {
							    	  String obj = temp.get("ObjectString").toString();
							    	  String type = temp.get("Class").toString();
							    	  byte[] bytes= Base64.getDecoder().decode(obj);
							    	  Object object;
							    	  
							    	  switch(type) {
										case "Shape.MyLine":
											object = (MyLine)client.deserialize(bytes);
											state.addShapes((MyShape) object);
											draw((MyShape) object); 
											break;
										case "Shape.MyEllipse":
											object = (MyEllipse)client.deserialize(bytes);
											state.addShapes((MyShape) object);
											draw((MyShape) object); 
											break;
										case "Shape.MyRectangle":
											object = (MyRectangle)client.deserialize(bytes);
											state.addShapes((MyShape) object);
											draw((MyShape) object); 
											break;
										case "Shape.MyText":
											object = (MyText)client.deserialize(bytes);
											state.addShapes((MyText) object);
											draw((MyShape) object); 
											break;	
										default:
											break;	
							    	  }   
							      } 
							      
							      // receive new board request from board owner
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("New")) {
							    	  state.New();
							    	  clearBoard((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
							      } 
							      
							      // receive load board request from board owner
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Load")) {
							    	  String obj = temp.get("ObjectString").toString();
							    	  // String type = temp.get("Class").toString();
							    	  
							    	  byte[] bytes= Base64.getDecoder().decode(obj);
							    	  state = (BoardState)client.deserialize(bytes);
							    	  rePaint(g);
							      } 
							      
							      // receive chat messages from other user
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Chat")) {
							    	  String name = temp.get("username").toString();
							    	  String msg = temp.get("message").toString();
							    	  
							    	  messageAppender.appendToMessagePane(messageShowPanel, name + " ", Color.WHITE, true);
							    	  messageAppender.appendToMessagePane(messageShowPanel, dtf.format(LocalDateTime.now()) + "\n", Color.WHITE, true);
							    	  messageAppender.appendToMessagePane(messageShowPanel, msg + "\n\n", Color.WHITE, false);
							      } 
							      
							      // receive authorize request from enter user
							      else if (boardOwner && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Authorize")) {
							    	  String name = temp.get("username").toString();
							    	  
							    	  int reply = JOptionPane.showConfirmDialog(null, name, "Allow following user to join?", JOptionPane.YES_NO_OPTION);
							          if (reply == JOptionPane.YES_OPTION) {
							        	  // send accept request
							              try {
											  client.requestAccept(name, time);
											  // add new user to user list and display
									    	  updateUserList(name, "add");
										  } catch (AbnormalCommunicationException e) {
											  e.printStackTrace();
										  }
							          } else {
							              // send decline request
							        	  try {
											  client.requestDecline(name, time);
										  } catch (AbnormalCommunicationException e) {
											  e.printStackTrace();
										  }
							          }
							      } 
							      
							      // receive accept enter from board owner
							      else if (pending && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Accept")) {
							    	  tempUserList = (ArrayList<String>) temp.get("UserList");
							    	  String boardStateStr = temp.get("BoardState").toString();
							    	  
							    	  enterBoard = true;
							    	  pending = false;
							    	  
							    	  // update board state
							    	  byte[] boardStateByte= Base64.getDecoder().decode(boardStateStr);
							    	  state = (BoardState)client.deserialize(boardStateByte);
							    	  
							      }
							      
							      // receive decline enter from board owner
							      else if (pending && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Decline")) {
							          pending = false;
							          enterBoard = false;
							      } 
							      
							      
							      // receive create message from server
							      else if (pending && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Create")) {
							    	  String msg = temp.get("ObjectString").toString();
							    	  
							    	  if (msg.equals("Success")) {
							    		  pending = false;
								    	  enterBoard = true;
								    	  boardOwner = true;
							    	  } else {
								    	  pending = false;
								    	  enterBoard = false;
							    	  }							    	  
							      } 
							      
							      // receive enter of other users
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Enter")) {
							    	  String name = temp.get("username").toString();
							    	  
							    	  messageAppender.appendToMessagePane(messageShowPanel, name + " enter the board \n", Color.WHITE, true);
							    	  
							    	  // add new user to user list and display
							    	  updateUserList(name, "add");
							      } 
							      
							      // receive leave of other users
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Leave")) {
							    	  String name = temp.get("username").toString();
							    	  
							    	  messageAppender.appendToMessagePane(messageShowPanel, name + " leave the board \n", Color.WHITE, true);
							    	  
							    	  // remove user from user list and display
							    	  updateUserList(name, "remove");
							      } 
							      
							      // receive remove operation from board owner
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Remove")) {
							    	  resetBoardState();
							    	  JOptionPane.showMessageDialog(null, "You have been kicked by board owner", "Alert", JOptionPane.WARNING_MESSAGE);
							      } 
							      
							      // receive close board request from board owner
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Close")) {
							    	  resetBoardState();
							      } 
							      
							      // receive general success message from server
							      else if (temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Reply")){
							    	  System.out.println("success");
							      } else {
							    	  continue;
							      }
							}
						}

					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Alert", JOptionPane.WARNING_MESSAGE);
					} catch (ParseException e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Alert", JOptionPane.WARNING_MESSAGE);
					} catch (ClassNotFoundException e1) {
						JOptionPane.showMessageDialog(null, e1.getMessage(), "Alert", JOptionPane.WARNING_MESSAGE);
					} finally {
						resetBoardState();
					}
					
			 	}
		};
		
		// Start listening server
		clientThread = new Thread(listeningServer);
		clientThread.start();
	}

	/**
	 * Create the application.
	 */
	private ClientUI() {
		initialize();
	}
	
	/**
	 * Create connection to server
	 */
	private void connectToServer(String host, int port) {
		try {
			client.initiate(host, port);	
			connected = true;
		} catch (ConnectException e1) {
			connected = false;
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Alert", JOptionPane.WARNING_MESSAGE);
		} catch (UnknownHostException e1) {
			connected = false;
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Alert", JOptionPane.WARNING_MESSAGE);
		} catch (IOException e1) {
			connected = false;
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Alert", JOptionPane.WARNING_MESSAGE);
		} finally {
			if (!connected) {
				pending = false;
				enterBoard = false;
			}
		}
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(0, 0, screenSize.width, screenSize.height); // full screen 
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		initHomePanel();
		frame.getContentPane().add(homePanel);
		frame.setVisible(true);

	}
	
	// initialize components on home panel
	private void initMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		initMessagePanel();
		initDrawControlPanel();
		initMessageControlPanel();
		initDrawPanelBoard();
		initDrawPanelHeader();
		mainPanel.setVisible(true);
		frame.getContentPane().add(mainPanel);
		drawPanelBoard.addMouseListener(ma);
		drawPanelBoard.addMouseMotionListener(ma);
		frame.setVisible(true);
		g = (Graphics2D)drawPanelBoard.getGraphics();
	}
	
	// initialize components on home panel
	private void initHomePanel() {
		homePanel = new JPanel();
		homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
		boardInfoPanel = new JPanel();
		boardInfoPanel.setLayout(null);
		
		JLabel background = new JLabel();
		background.setBounds(0, 0, (int) (screenSize.width), (int) (screenSize.height));
		background.setIcon(ImageResizer.reSizeForLabel(new ImageIcon("images/home.png"), background));
		homePanel.add(background);
		
		JTextArea userNameInput= new JTextArea(DEFAULT_USERNAME);
		JTextArea ipInput= new JTextArea(DEFAULT_HOST);
		JTextArea portInput= new JTextArea(DEFAULT_PORT);
		
		JLabel userNameInputLabel = new JLabel("Username: ");
		JLabel ipInputLabel = new JLabel("IP Address: ");
		JLabel portInputLabel = new JLabel("Port: ");
		
		Font font = new Font("TimesRoman", Font.BOLD, 20);
		
		userNameInput.setFont(font);
		ipInput.setFont(font);
		portInput.setFont(font);
		userNameInputLabel.setFont(font);
		ipInputLabel.setFont(font);
		portInputLabel.setFont(font);
		
		userNameInput.setBackground(Color.black);
		ipInput.setBackground(Color.black);
		portInput.setBackground(Color.black);
		
		userNameInput.setForeground(Color.white);
		ipInput.setForeground(Color.white);
		portInput.setForeground(Color.white);
		
		userNameInput.setBounds((int) (screenSize.width*0.45), (int) (screenSize.height*0.3), (int) (screenSize.height*0.2), 25);
		ipInput.setBounds((int) (screenSize.width*0.45), (int) (screenSize.height*0.4), (int) (screenSize.height*0.2), 25);
		portInput.setBounds((int) (screenSize.width*0.45), (int) (screenSize.height*0.5), (int) (screenSize.height*0.2), 25);
		
		userNameInputLabel.setBounds((int) (screenSize.width*0.35), (int) (screenSize.height*0.3), (int) (screenSize.height*0.2), 25);
		ipInputLabel.setBounds((int) (screenSize.width*0.35), (int) (screenSize.height*0.4), (int) (screenSize.height*0.2), 25);
		portInputLabel.setBounds((int) (screenSize.width*0.35), (int) (screenSize.height*0.5), (int) (screenSize.height*0.2), 25);

		boardInfoPanel.add(userNameInput);
		boardInfoPanel.add(ipInput);
		boardInfoPanel.add(portInput);
		boardInfoPanel.add(userNameInputLabel);
		boardInfoPanel.add(ipInputLabel);
		boardInfoPanel.add(portInputLabel);
		
		JButton enterBtn = new JButton();
		enterBtn.setToolTipText("Enter Board");
		enterBtn.setBounds((int) (screenSize.width*0.6), (int) (screenSize.height*0.3), (int) (screenSize.height*0.1), (int) (screenSize.height*0.1));
		enterBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/enter.png"), enterBtn));
		enterBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Enter board");
				username = userNameInput.getText();
				
				// validate username, address, port
				if (!Validators.checkName(username)) {
					JOptionPane.showMessageDialog(null,"Invalid user name.","Alert",JOptionPane.WARNING_MESSAGE);     
					return;
				}
				
				if (!Validators.checkHost(ipInput.getText())) {
					JOptionPane.showMessageDialog(null,"Invalid host name.","Alert",JOptionPane.WARNING_MESSAGE); 
					return;
				}
				
				if (!Validators.checkPort(portInput.getText())) {
					JOptionPane.showMessageDialog(null,"Invalid port number.","Alert",JOptionPane.WARNING_MESSAGE); 
					return;
				}
				
				pending = true;
				enterBoard = false;
				
				// connect to server
				connectToServer(ipInput.getText(), Integer.parseInt(portInput.getText()));
				
				// send enter request to server
				try {
					client.requestEnter(username, time);
				} catch (AbnormalCommunicationException | IOException e1) {
					e1.printStackTrace();
				}
				
				// count timeout
				Date start = new Date();
				Date end = new Date();
				
				while (pending && (int)((end.getTime() - start.getTime()) / 1000) < 10) {
					end = new Date();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (enterBoard) {
					homePanel.setVisible(false);
					initMainPanel();
			    	rePaint(g);
			    	  
			    	// add users to board
			    	for (String name: tempUserList) {
			    		updateUserList(name, "add");
			    	}
			    	
			    	tempUserList = null;
					openBtn.setVisible(false);
					newBtn.setVisible(false);
					saveBtn.setVisible(true);
					saveAsBtn.setVisible(true);
				} else if (pending) {
					JOptionPane.showMessageDialog(null, "Time out");
					if (connected) {
						try {
							connected = false;
							client.disconnect();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					if (connected) {
						JOptionPane.showMessageDialog(null, "Board Owner Refused Your Request");	
						try {
							connected = false;
							client.disconnect();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		
		JButton createBtn = new JButton();
		createBtn.setToolTipText("Create Board");
		createBtn.setBounds((int) (screenSize.width*0.6), (int) (screenSize.height*0.4) + 25, (int) (screenSize.height*0.1), (int) (screenSize.height*0.1));
		createBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/create.png"), createBtn));
		createBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Create board");
				username = userNameInput.getText();
				
				// validate username, address, port
				if (!Validators.checkName(username)) {
					JOptionPane.showMessageDialog(null,"Invalid user name.","Alert",JOptionPane.WARNING_MESSAGE);     
					return;
				}
				
				if (!Validators.checkHost(ipInput.getText())) {
					JOptionPane.showMessageDialog(null,"Invalid host name.","Alert",JOptionPane.WARNING_MESSAGE); 
					return;
				}
				
				if (!Validators.checkPort(portInput.getText())) {
					JOptionPane.showMessageDialog(null,"Invalid port number.","Alert",JOptionPane.WARNING_MESSAGE); 
					return;
				}
				
				pending = true;
				enterBoard = false;
				
				// connect to server
				connectToServer(ipInput.getText(), Integer.parseInt(portInput.getText()));
				
				// send create request
				try {
					client.requestCreate(username, time);
				} catch (AbnormalCommunicationException | IOException e1) {
					e1.printStackTrace();
				}
				
				// count timeout
				Date start = new Date();
				Date end = new Date();
				
				while (pending && (int)((end.getTime() - start.getTime()) / 1000) < 10) {
					end = new Date();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if (enterBoard) {
					homePanel.setVisible(false);
					initMainPanel();
					openBtn.setVisible(true);
					newBtn.setVisible(true);
					saveBtn.setVisible(true);
					saveAsBtn.setVisible(true);
			    	updateUserList(username, "add");
				} else if (pending) {
					JOptionPane.showMessageDialog(null, "Time out");
					if (connected) {
						try {
							connected = false;
							client.disconnect();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					if (connected) {
						JOptionPane.showMessageDialog(null, "Board Already Exist");	
						try {
							connected = false;
							client.disconnect();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		
		boardInfoPanel.add(enterBtn);
		boardInfoPanel.add(createBtn);
		
		homePanel.add(boardInfoPanel);
		frame.getContentPane().add(homePanel);
		boardInfoPanel.setComponentZOrder(background, 8);
	}
	
	// initialize components on draw panel header
	private void initDrawPanelHeader() {
		drawPanelHeader = new JPanel();
		drawPanelHeader.setBounds(0, 0, (int) (screenSize.width*0.8), (int) (screenSize.height*0.05));
		drawPanelHeader.setPreferredSize(new Dimension(0, 20));
		mainPanel.add(drawPanelHeader);
		drawPanelHeader.setLayout(null);
		
		returnBtn = new JButton();
		returnBtn.setToolTipText("Return to home");
		returnBtn.setBounds(20, 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		returnBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/return.png"), returnBtn));
		returnBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				if (boardOwner) {
					// send close request
					try {
						client.requestClose(time);
					} catch (AbnormalCommunicationException | IOException e) {
						e.printStackTrace();
					}
				} else {
					// send leave request
					try {
						client.requestLeave(username, time);;
					} catch (AbnormalCommunicationException | IOException e) {
						e.printStackTrace();
					}
				}
				
				resetBoardState();

				if (connected) {
					// disconnect
					try {
						connected = false;
						client.disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		drawPanelHeader.add(returnBtn);
		
		openBtn = new JButton();
		openBtn.setToolTipText("Load board from local");
		openBtn.setBounds((int) (screenSize.width*0.64), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		openBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/open.png"), openBtn));
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser= new JFileChooser();
				String path = Paths.get("").toAbsolutePath().toString();
                chooser.setCurrentDirectory(new File(path));
                chooser.setFileFilter(new FileNameExtensionFilter("ser","SER"));
                int value = chooser.showOpenDialog(null);
                if (value == JFileChooser.APPROVE_OPTION) { 
                    File f= chooser.getSelectedFile();
                    String filename= f.getAbsolutePath();
                    try {
    					state = state.Open(filename);
    				} catch (ClassNotFoundException e) {
    					e.printStackTrace();
    				}
                    rePaint(g);
                    
                    // send load request
                    try {
    					client.requestLoad(state, time);
    				} catch (AbnormalCommunicationException | IOException e) {
    					e.printStackTrace();
    				}
                } else {
                	System.out.println("Load command cancelled by user.");
                }

			}
		});
		drawPanelHeader.add(openBtn);
		
		saveBtn = new JButton();
		saveBtn.setToolTipText("Save board to current folder");
		saveBtn.setBounds((int) (screenSize.width*0.7), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		saveBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/save.png"), saveBtn));
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				state.Save();
			}
		});
		drawPanelHeader.add(saveBtn);
		
		saveAsBtn = new JButton();
		saveAsBtn.setToolTipText("Save board to customized folder");
		saveAsBtn.setBounds((int) (screenSize.width*0.76), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		saveAsBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/saveAs.png"), saveAsBtn));
		saveAsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser= new JFileChooser();
				String path = Paths.get("").toAbsolutePath().toString();
                chooser.setCurrentDirectory(new File(path));
                chooser.setFileFilter(new FileNameExtensionFilter("ser","SER"));
                chooser.setApproveButtonText("Save As");
                chooser.setDialogTitle("Save As");
                int value = chooser.showOpenDialog(null);
                if (value == JFileChooser.APPROVE_OPTION) { 
                    File f= chooser.getSelectedFile();
                    String filename= f.getAbsolutePath();
    				state.SaveAs(filename);
                } else {
                	System.out.println("Save command cancelled by user.");
                }

			}
		});
		drawPanelHeader.add(saveAsBtn);
		
		newBtn = new JButton();
		newBtn.setToolTipText("New");
		newBtn.setBounds((int) (screenSize.width*0.58), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		newBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/new.png"), newBtn));
		newBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.New();
				clearBoard((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
				
				// send new (empty board) request
				try {
					client.requestNew(time);
				} catch (AbnormalCommunicationException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		drawPanelHeader.add(newBtn);
	}
	
	// initialize components on message panel
	private void initMessagePanel() {
		userList = new JList<Object>();
		userList.addMouseListener( new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	        	userList.setSelectedIndex(userList.locationToIndex(e.getPoint()));
	        	if (!userList.getSelectedValue().equals(username) && boardOwner) {
		        	JPopupMenu menu = new JPopupMenu();
		            JMenuItem userRemove = new JMenuItem("Remove");
		            userRemove.addActionListener(new ActionListener() {
		                public void actionPerformed(ActionEvent e) {
		                    // send removed user request
		                    try {
								client.requestRemove(userList.getSelectedValue().toString(), time);
							} catch (AbnormalCommunicationException | IOException e1) {
								e1.printStackTrace();
							}
		                    System.out.println("Remove the user: " + userList.getSelectedValue());
		                    
		                 // remove username from list
		                    updateUserList(userList.getSelectedValue().toString(), "remove");
		                }
		            });
		            menu.add(userRemove);
		            menu.show(userList, e.getPoint().x, e.getPoint().y); 
	        	}
	        }
	     });
		userList.setBounds((int) (screenSize.width*0.15), 0, (int) (screenSize.width*0.1), (int) (screenSize.height*0.87));
		
		JPanel messagePanel = new JPanel();
		messagePanel.setBounds((int) (screenSize.width*0.8), 0, (int) (screenSize.width*0.2), (int) (screenSize.height*0.87));
		messagePanel.setPreferredSize(new Dimension(200, 0));
		messagePanel.setLayout(null);
		messagePanel.add(userList);
		
		messageShowPanel = new JTextPane();
		messageShowPanel.setBounds(0, 0, (int) (screenSize.width*0.15), (int) (screenSize.height*0.87));
		messageShowPanel.setBackground(Color.DARK_GRAY);
		messagePanel.add(messageShowPanel);
		
		mainPanel.add(messagePanel);
	}
	
	// initialize components on draw control panel
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
			btn.setBounds(20+i*(int) (screenSize.height*0.04), 5, (int) (screenSize.height*0.03), (int) (screenSize.height*0.03));
			drawControlPanel.add(btn);
		}
		
		for (int i = 0; i < options.length; i++) {
			JButton btn = new JButton();
			btn.setToolTipText(options[i]);
			btn.addActionListener(shapeSelectAL);
			btn.setBounds(20+i*(int) (screenSize.height*0.05), (int) (screenSize.height*0.03) + 5, (int) (screenSize.height*0.04), (int) (screenSize.height*0.04));
			String path = "images/" + (i+1) + ".png";
			btn.setIcon(ImageResizer.reSizeForButton(new ImageIcon(path), btn));
			drawControlPanel.add(btn);
		}
		
		SpinnerNumberModel thicknessModel = new SpinnerNumberModel(1, 1, 15, 1);
		thicknessSelector = new JSpinner(thicknessModel);
		thicknessSelector.setBounds((int) (screenSize.width*0.75), 5, (int) (screenSize.height*0.05), (int) (screenSize.height*0.025));
		drawControlPanel.add(thicknessSelector);

		fillSelector = new JCheckBox("Fill");
		fillSelector.setBackground(Color.LIGHT_GRAY);
		fillSelector.setBounds((int) (screenSize.width*0.75), (int) (screenSize.height*0.03) + 5, (int) (screenSize.height*0.05), (int) (screenSize.height*0.025));
		drawControlPanel.add(fillSelector);
		
		JButton selectColorButton = new JButton();
		selectColorButton.setToolTipText("More colors");
		selectColorButton.setBounds(20 + (options.length) * (int) (screenSize.height*0.05), (int) (screenSize.height*0.03) + 5, (int) (screenSize.height*0.04), (int) (screenSize.height*0.04));
		selectColorButton.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/color.png"), selectColorButton));
		selectColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ColorSelector cs = new ColorSelector();
				cs.createAndShowGUI();
			}
		});
		drawControlPanel.add(selectColorButton);
		mainPanel.add(drawControlPanel);
	}
	
	// initialize components on message control panel
	private void initMessageControlPanel() {
		JPanel messageControlPanel = new JPanel();
		messageControlPanel.setBackground(SystemColor.activeCaptionBorder);
		messageControlPanel.setBounds((int) (screenSize.width*0.8), (int) (screenSize.height*0.87), (int) (screenSize.width*0.2), (int) (screenSize.height*0.13));
		mainPanel.add(messageControlPanel);
		messageControlPanel.setPreferredSize(new Dimension(0, 50));
		messageInputPanel = new JTextField(20);
		sendBtn = new JButton();
		sendBtn.setToolTipText("More colors");
		sendBtn.setBounds(0, 0, (int) (screenSize.height*0.04), (int) (screenSize.height*0.04));
		sendBtn.setIcon(ImageResizer.reSizeForButton(new ImageIcon("images/send.png"), sendBtn));
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageInputPanel.getText();
				if (message != null && !message.equals("")) {
					messageAppender.appendToMessagePane(messageShowPanel, username + " ", Color.WHITE, true);
					messageAppender.appendToMessagePane(messageShowPanel, dtf.format(LocalDateTime.now()) + "\n", Color.WHITE, true);
					messageAppender.appendToMessagePane(messageShowPanel, message + "\n\n", Color.WHITE, false);
					
					// send message to server
					try {
						client.requestChat(username, message, time);
					} catch (AbnormalCommunicationException | IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		messageControlPanel.add(messageInputPanel);
		messageControlPanel.add(sendBtn);
	}
	
	// initialize components on draw panel
	private void initDrawPanelBoard() {
		drawPanelBoard = new JPanel() {
			 /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			 protected void paintComponent(Graphics g) {
			        super.paintComponent(g);
			        Graphics2D g2d = (Graphics2D)g;
			        rePaint(g2d);
			 };
		};
		drawPanelBoard.setBounds(0, (int) (screenSize.height*0.05), (int) (screenSize.width*0.8), (int) (screenSize.height*0.82));
		drawPanelBoard.setBackground(Color.WHITE);
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
			shape =bt.getToolTipText();
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
					int size = (int)thicknessSelector.getValue()*10;
					MyText mytext = new MyText(text, (float) x1, (float) y1, color, size, username);
					state.addShapes(mytext);
					sendDrawRequest(mytext);
					draw(mytext);
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
			int thickness=(int)thicknessSelector.getValue();
			strock = new BasicStroke(thickness);
			g.setStroke(strock);
			fill = fillSelector.isSelected();
			x2 = e.getX();
			y2 = e.getY();
			
			switch(shape) {
				case "free draw":
					Shape line = new Line2D.Double(x1, y1, x2, y2);
					MyLine myline = new MyLine(line, color, username, thickness, fill);
					state.addShapes(myline);
					draw(myline);
					sendDrawRequest(myline);
					// set current point as the start point of next point
					x1 = x2;
					y1 = y2;
					break;
				
				case "text":
					break;
				
				case "eraser":
					Shape eraser = new Line2D.Double(x1, y1, x2, y2);
					MyLine myEraser = new MyLine(eraser, Color.white, username, thickness*10, fill);
					state.addShapes(myEraser);
					draw(myEraser);
					sendDrawRequest(myEraser);
					// set current point as the start point of next point
					x1 = x2;
					y1 = y2;
					break;
					
				default:
					break;
			}	

		}
		
		public void mouseReleased(MouseEvent e) {
			Shape s;
			
			switch(shape) {
				case "free draw":
					break;
					
				case "line":
					s =  new Line2D.Double(x1, y1, e.getX(), e.getY());
					MyLine myline = new MyLine(s, color, username, (int)strock.getLineWidth(), fill);
					state.addShapes(myline);
					draw(myline);
					sendDrawRequest(myline);
					break;
					
				case "rectangle":
					s = ShapeMaker.makeRectangle(x1, y1, e.getX(), e.getY());
					MyRectangle myRectangle = new MyRectangle(s, color, username, (int)strock.getLineWidth(), fill);
					state.addShapes(myRectangle);
					draw(myRectangle);
					sendDrawRequest(myRectangle);
					break;
					
				case "circle":
					s = ShapeMaker.makeCircle(x1, y1, e.getX(), e.getY());
					MyEllipse myCircle = new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill);
					state.addShapes(myCircle);
					draw(myCircle);
					sendDrawRequest(myCircle);
					break;
				
				case "oval":
					s = ShapeMaker.makeOval(x1, y1, e.getX(), e.getY());
					MyEllipse myOval = new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill);
					state.addShapes(myOval);
					draw(myOval);
					sendDrawRequest(myOval);	
					break;
					
				case "eraser":
					break;
					
				default:
					System.out.println("Unsupported Shape");
			}
	    }
		
	};

	// draw single item on board
	private synchronized static void draw(MyShape s) {
		if (s.getClass().toString().equals(MyText.class.toString())) {
			MyText t = (MyText) s;
			g.setFont(new Font("TimesRoman", Font.PLAIN, t.getThickness()));
			g.setPaint(t.getColor());
			g.drawString(t.getText(), t.getX(), t.getY());
		} else {
			strock = new BasicStroke(s.getThickness());
			g.setStroke(strock);
			g.setPaint(s.getColor());
	        g.draw(s.getShape());
	        if (s.getFill()) {
	        	g.fill(s.getShape());
	        }
		}
	}
	
	// repaint the board
	private synchronized static void rePaint(Graphics2D g2d) {
		clearBoard((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
		
		for (MyShape s : state.getShapes()) {
			if (s.getClass().toString().equals(MyText.class.toString())) {
				MyText t = (MyText) s;
				g2d.setFont(new Font("TimesRoman", Font.PLAIN, t.getThickness()));
				g2d.setPaint(t.getColor());
				g2d.drawString(t.getText(), t.getX(), t.getY());
			} else {
				strock = new BasicStroke(s.getThickness());
				g2d.setStroke(strock);
				g2d.setPaint(s.getColor());
				g2d.draw(s.getShape());
		        if (s.getFill()) {
		        	g2d.fill(s.getShape());
		        }
			}	
	     }
	}
	
	// clear the white board
	private synchronized static void clearBoard(int width, int height) {
		g.setPaint(Color.WHITE);
		Shape s = ShapeMaker.makeRectangle(0, 0, width, height);
		g.draw(s);
		g.fill(s);
	}
	
	// reset board state
	private synchronized static void resetBoardState() {
		state.New();
		clearBoard((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
		pending = false;
		enterBoard = false;
	    username = null;
		users.clear();
		userList.setModel(users);
		messageShowPanel.setText("");
		mainPanel.removeAll();
  	    frame.getContentPane().remove(mainPanel);
		homePanel.setVisible(true);
		frame.setVisible(true);
	}
	
	// update user list
	private synchronized static void updateUserList(String name, String option) {
		if (option.equals("add")) {
	    	users.addElement(name);
	        userList.setModel(users);
		} else {
			users.removeElement(name);
	        userList.setModel(users);
		}
	}
   
   // send draw request to server
   private void sendDrawRequest(Object obj) {
	   try {
	       client.requestDraw(obj, time);
	   } catch (AbnormalCommunicationException | IOException e) {
	       e.printStackTrace();
	   } 
   }
}