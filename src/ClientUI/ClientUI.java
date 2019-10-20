package ClientUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

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

public class ClientUI {
	
	private static Thread clientThread;
	private Dimension screenSize;
	private static JList<Object> userList;
	private static DefaultListModel<Object> users = new DefaultListModel<Object>();;
	private static boolean boardOwner = false;
	private static boolean enterBoard = false;
	private static boolean pending = false;
	private static boolean connected = false;
	private static JFrame frame;
	private JButton sendBtn;
	private JPanel drawPanelHeader;
	private static JPanel mainPanel;
	private static JPanel homePanel;
	private JPanel boardInfoPanel;
	private JPanel drawControlPanel;
	private JPanel drawPanelBoard;
	private JTextField messageInputPanel;
	private static JTextPane messageShowPanel;
	private static Graphics2D g;
	private String [] options = {"free draw", "line", "rectangle", "circle", "oval", "text", "eraser"};
	protected static Color color;
	private Color [] colors = {Color.GRAY, Color.LIGHT_GRAY, Color.darkGray, Color.black, Color.orange, Color.green, 
			                   Color.red, Color.pink, Color.blue, Color.cyan, Color.magenta, Color.YELLOW, 
			                   new Color(125, 55, 237), new Color(255, 99, 71), new Color(240, 230, 140), 
			                   new Color(0, 250, 154), new Color(0, 206, 209), new Color(238, 130, 238)};
	
	private String shape = "free draw";
	private static BoardState state = new BoardState(new ArrayList<MyShape>());
	private ArrayList<MyShape> shapesPreview = new ArrayList<MyShape>();
	private int x1, y1, x2 , y2;
	private static BasicStroke strock;
	private JComboBox<Integer> thicknessSelector;
	private JCheckBox fillSelector;
	private Boolean fill;
	private static String username = "";
	
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");  
	private static int time = 60000;
	private static Client client;
	
	private JButton returnBtn;
	private JButton openBtn;
	private JButton saveBtn;
	private JButton saveAsBtn;
	private JButton newBtn;
	
	protected static boolean error;
	protected static String errorMsg;
	
	/**
	 * Launch the application.
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) {
		client = new Client();
		
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
		
		Runnable listeningServer = new Runnable() {
			 @Override
	            public void run() {
				 	String content;	
					try {
						while(true) {
							if (!connected || !client.getBufferReader().ready()) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							System.out.println(1);
							if(connected && client.getBufferReader().ready()) {
								  content = client.getBufferReader().readLine();
								  System.out.println(content.toString());
							  	  JSONParser parser = new JSONParser();
							      JSONObject temp = (JSONObject) parser.parse(content);
							      
							      if (error == true) {
							    	  JOptionPane.showMessageDialog(null, errorMsg);
							    	  error = false;
							      }
							      
							      if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Info")) {
							    	  String obj = temp.get("ObjectString").toString();
							    	  String type = temp.get("Class").toString();
							    	  byte[] bytes= Base64.getDecoder().decode(obj);
							    	  Object object;
							    	  
							    	  switch(type) {
										case "Shape.MyLine":
											object = (MyLine)client.deserialize(bytes);
											state.getShapes().add((MyShape) object);
											break;
										case "Shape.MyEllipse":
											object = (MyEllipse)client.deserialize(bytes);
											state.getShapes().add((MyShape) object);
											break;
										case "Shape.MyRectangle":
											object = (MyRectangle)client.deserialize(bytes);
											state.getShapes().add((MyShape) object);
											break;
										case "Text.MyText":
											object = (MyText)client.deserialize(bytes);
											state.getShapes().add((MyText) object);
											break;	
										default:
											break;
									}
							    	Clear((int) (Window.WIDTH), (int) (Window.HEIGHT));
							    	Draw(); 
							      
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("New")) {
							    	  state.New();
							    	  Clear((int) (Window.WIDTH), (int) (Window.HEIGHT));
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Load")) {
							    	  String obj = temp.get("ObjectString").toString();
							    	  String type = temp.get("Class").toString();
							    	  
							    	  byte[] bytes= Base64.getDecoder().decode(obj);
							    	  state = (BoardState)client.deserialize(bytes);
							    	  Draw();
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Chat")) {
							    	  String name = temp.get("username").toString();
							    	  String msg = temp.get("message").toString();
							    	  
									  appendToPane(messageShowPanel, name + " ", Color.WHITE, true);
									  appendToPane(messageShowPanel, dtf.format(LocalDateTime.now()) + "\n", Color.WHITE, true);
									  appendToPane(messageShowPanel, msg + "\n\n", Color.WHITE, false);
							      } 
							      
							      else if (boardOwner && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Authorize")) {
							    	  String name = temp.get("username").toString();
							    	  
							    	  int reply = JOptionPane.showConfirmDialog(null, name, "Allow following user to join?", JOptionPane.YES_NO_OPTION);
							          if (reply == JOptionPane.YES_OPTION) {
							        	  // send accept request
							              try {
											  client.requestAccept(name, time);
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
							      
							      else if (pending && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Accept")) {
							    	  String name = temp.get("username").toString();
							    	  String obj = temp.get("ObjectString").toString();
							    	  String type = temp.get("Class").toString();
							    	  // get users;
							    	  pending = false;
							    	  enterBoard = true;
							    	  
							    	  byte[] bytes= Base64.getDecoder().decode(obj);
							    	  state = (BoardState)client.deserialize(bytes);
							    	  Draw();
							    	  
							    	  // add users to board
							      } 
							      
							      else if (pending && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Decline")) {
							          pending = false;
							          enterBoard = false;
							      } 
							      
							      else if (pending && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Created")) {
							    	  String msg = temp.get("msg").toString();
							    	  
							    	  // if message success
							    	  pending = false;
							    	  enterBoard = true;
							    	 
							    	  // updateUserList(name, "add")
							    	  
							    	  // if message failed
							    	  pending = false;
							    	  enterBoard = false;
							    	  
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Enter")) {
							    	  String name = temp.get("username").toString();

							    	  // add new user to user list and display
							    	  updateUserList(name, "add");
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Leave")) {
							    	  String name = temp.get("username").toString();
							    	  
							    	  // remove user from user list and display
							    	  updateUserList(name, "remove");
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Remove")) {
							    	  ReturnHome();
							      } 
							      
							      else if (!pending && enterBoard && temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Close")) {
							    	  ReturnHome();
							      } 
							      
							      else if (temp.get("Source").toString().equals("Server") && temp.get("Goal").toString().equals("Reply")){
							    	  System.out.println("success");
							      } else {
							    	  continue;
							      }
							}
						}

					} catch (IOException e1) {
						JOptionPane.showConfirmDialog(null, e1.getMessage(), e1.getMessage(), JOptionPane.YES_NO_OPTION);
					} catch (ParseException e1) {
						JOptionPane.showConfirmDialog(null, "Parse Exception", "Parse Exception", JOptionPane.YES_NO_OPTION);

					} catch (ClassNotFoundException e1) {
						JOptionPane.showConfirmDialog(null, e1.getMessage(), e1.getMessage(), JOptionPane.YES_NO_OPTION);
					} finally {
						pending = false;
						connected = false;
						enterBoard = false;
					}
					
			 	}
		};
		clientThread = new Thread(listeningServer);
		clientThread.start();
		//waitThread();

	}

	/**
	 * Create the application.
	 */
	private ClientUI() {
		initialize();
	}
	
//	private synchronized static void waitThread () {
//		try {
//			clientThread.wait();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private synchronized static void notifyThread () {
//		clientThread.notify();
//	}

	private void connectToServer(String host, int port) {
		try {
			client.initiate(host, port);	
			connected = true;
		} catch (ConnectException e1) {
			connected = false;
			JOptionPane.showConfirmDialog(null, e1.getMessage(), e1.getMessage(), JOptionPane.YES_NO_OPTION);
		} catch (UnknownHostException e1) {
			connected = false;
			JOptionPane.showConfirmDialog(null, e1.getMessage(), e1.getMessage(), JOptionPane.YES_NO_OPTION);
		} catch (IOException e1) {
			connected = false;
			JOptionPane.showConfirmDialog(null, e1.getMessage(), e1.getMessage(), JOptionPane.YES_NO_OPTION);
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
	    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(0, 0, screenSize.width, screenSize.height); // full screen 
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // full screen
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		
		initHomePanel();

		frame.getContentPane().add(homePanel);
		frame.setVisible(true);

	}
	
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
	}
	
	private void initHomePanel() {

		homePanel = new JPanel();
		homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
		boardInfoPanel = new JPanel();
		boardInfoPanel.setLayout(null);
		
		JLabel background = new JLabel();
		background.setBounds(0, 0, (int) (screenSize.width), (int) (screenSize.height));
		background.setIcon(reSizeForLabel(new ImageIcon("images/home.png"), background));
		homePanel.add(background);
		
		Font font = new Font("TimesRoman", Font.BOLD, 20);

		JTextArea userNameInput= new JTextArea();
		JTextArea ipInput= new JTextArea();
		JTextArea portInput= new JTextArea();
		JLabel userNameInputLabel = new JLabel("Username: ");
		JLabel ipInputLabel = new JLabel("IP Address: ");
		JLabel portInputLabel = new JLabel("Port: ");
		
		userNameInput.setFont(font);
		ipInput.setFont(font);
		portInput.setFont(font);
		userNameInputLabel.setFont(font);
		ipInputLabel.setFont(font);
		portInputLabel.setFont(font);
		
		userNameInput.setBackground(Color.black);
		userNameInput.setForeground(Color.white);
		ipInput.setBackground(Color.black);
		ipInput.setForeground(Color.white);
		portInput.setBackground(Color.black);
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
		enterBtn.setIcon(reSizeForButton(new ImageIcon("images/enter.png"), enterBtn));
		enterBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Enter board");
				// validate username, address, port
				// connect to server
				pending = true;
				connectToServer(ipInput.getText(), Integer.parseInt(portInput.getText()));
				// notifyThread();
				enterBoard = false;
				username = userNameInput.getText();
				
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
				}

				if (enterBoard) {
					homePanel.setVisible(false);
					initMainPanel();
					openBtn.setVisible(false);
					newBtn.setVisible(false);
					saveBtn.setVisible(true);
					saveAsBtn.setVisible(true);
					frame.setVisible(true);
				} else if (pending) {
					System.out.println(1);
					JOptionPane.showMessageDialog(null, "Time out");
					if (connected) {
						try {
							connected = false;
							// waitThread();
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
							// waitThread();
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
		createBtn.setIcon(reSizeForButton(new ImageIcon("images/create.png"), createBtn));
		createBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Create board");
				// validate username, address, port
				pending = true;
				// connect to server
				connectToServer(ipInput.getText(), Integer.parseInt(portInput.getText()));
				// notifyThread();
				enterBoard = false;
				username = userNameInput.getText();
				
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
				}
				
				if (enterBoard) {
					homePanel.setVisible(false);
					initMainPanel();
					openBtn.setVisible(false);
					newBtn.setVisible(false);
					saveBtn.setVisible(true);
					saveAsBtn.setVisible(true);
					frame.setVisible(true);
				} else if (pending) {
					JOptionPane.showMessageDialog(null, "Time out");
					if (connected) {
						try {
							connected = false;
							// waitThread();
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
							// waitThread();
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
	
	private void initDrawPanelHeader() {
		drawPanelHeader = new JPanel();
		drawPanelHeader.setBounds(0, 0, (int) (screenSize.width*0.8), (int) (screenSize.height*0.05));
		drawPanelHeader.setPreferredSize(new Dimension(0, 20));
		mainPanel.add(drawPanelHeader);
		drawPanelHeader.setLayout(null);
		
		returnBtn = new JButton();
		returnBtn.setToolTipText("Return to home");
		returnBtn.setBounds(20, 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		returnBtn.setIcon(reSizeForButton(new ImageIcon("images/return.png"), returnBtn));
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
				
				ReturnHome();

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
		openBtn.setIcon(reSizeForButton(new ImageIcon("images/open.png"), openBtn));
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
                Clear((int) (screenSize.width), (int) (screenSize.height));
                Draw();
                
                // send load request
                try {
					client.requestLoad(state, time);
				} catch (AbnormalCommunicationException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		drawPanelHeader.add(openBtn);
		
		saveBtn = new JButton();
		saveBtn.setToolTipText("Save board to current folder");
		saveBtn.setBounds((int) (screenSize.width*0.7), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		saveBtn.setIcon(reSizeForButton(new ImageIcon("images/save.png"), saveBtn));
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				state.Save();
			}
		});
		drawPanelHeader.add(saveBtn);
		
		saveAsBtn = new JButton();
		saveAsBtn.setToolTipText("Save board to customized folder");
		saveAsBtn.setBounds((int) (screenSize.width*0.76), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		saveAsBtn.setIcon(reSizeForButton(new ImageIcon("images/saveAs.png"), saveAsBtn));
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
		drawPanelHeader.add(saveAsBtn);
		
		newBtn = new JButton();
		newBtn.setToolTipText("New");
		newBtn.setBounds((int) (screenSize.width*0.58), 2, (int) (screenSize.height*0.05), (int) (screenSize.height*0.05));
		newBtn.setIcon(reSizeForButton(new ImageIcon("images/new.png"), newBtn));
		newBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				state.New();
				Clear((int) (screenSize.width), (int) (screenSize.height));
				Draw();
				
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
			btn.setIcon(reSizeForButton(new ImageIcon(path), btn));
			drawControlPanel.add(btn);
		}
		
		thicknessSelector = new JComboBox<Integer>();
		thicknessSelector.setBounds((int) (screenSize.width*0.75), 5, (int) (screenSize.height*0.05), (int) (screenSize.height*0.025));
		drawControlPanel.add(thicknessSelector);
		for (int i = 0; i < 10; i++) {
			Integer thicknessVal = new Integer(i+1);
			thicknessSelector.addItem(thicknessVal);
		}

		fillSelector = new JCheckBox("Fill");
		fillSelector.setBackground(Color.LIGHT_GRAY);
		fillSelector.setBounds((int) (screenSize.width*0.75), (int) (screenSize.height*0.03) + 5, (int) (screenSize.height*0.05), (int) (screenSize.height*0.025));
		drawControlPanel.add(fillSelector);
		
		JButton selectColorButton = new JButton();
		selectColorButton.setToolTipText("More colors");
		selectColorButton.setBounds(20 + (options.length) * (int) (screenSize.height*0.05), (int) (screenSize.height*0.03) + 5, (int) (screenSize.height*0.04), (int) (screenSize.height*0.04));
		selectColorButton.setIcon(reSizeForButton(new ImageIcon("images/color.png"), selectColorButton));
		selectColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ColorSelector cs = new ColorSelector();
				cs.createAndShowGUI();
			}
		});
		drawControlPanel.add(selectColorButton);
		mainPanel.add(drawControlPanel);
	}
	
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
		sendBtn.setIcon(reSizeForButton(new ImageIcon("images/send.png"), sendBtn));
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = messageInputPanel.getText();
				if (message != null && !message.equals("")) {
					appendToPane(messageShowPanel, username + " ", Color.WHITE, true);
					appendToPane(messageShowPanel, dtf.format(LocalDateTime.now()) + "\n", Color.WHITE, true);
					appendToPane(messageShowPanel, message + "\n\n", Color.WHITE, false);
					
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
	
	private void initDrawPanelBoard() {
		drawPanelBoard = new JPanel() {
			 @Override
			 protected void paintComponent(Graphics g) {
			        super.paintComponent(g);
			        Graphics2D g2d = (Graphics2D)g;
			        RePaint(g2d);
			 };
		};
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
					int size = (int)thicknessSelector.getSelectedItem()*10;
					MyText mytext = new MyText(text, (float) x1, (float) y1, color, size, username);
					state.getShapes().add(mytext);
					sendDrawRequest(mytext);
					Clear((int) (screenSize.width), (int) (screenSize.height));
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
					MyLine myline = new MyLine(line, color, username, thickness, fill);
					state.getShapes().add(myline);
					shapesPreview.add(myline);
					DrawPreview();
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
					state.getShapes().add(myEraser);
					shapesPreview.add(myEraser);
					DrawPreview();
					sendDrawRequest(myEraser);
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
			shapesPreview.clear();
			Clear((int) (screenSize.width), (int) (screenSize.height));
			
			switch(shape) {
				case "free draw":
					shapesPreview.clear();
					Draw();
					break;
					
				case "line":
					s =  new Line2D.Double(x1, y1, e.getX(), e.getY());
					MyLine myline = new MyLine(s, color, username, (int)strock.getLineWidth(), fill);
					state.getShapes().add(myline);
					Draw();
					sendDrawRequest(myline);
					break;
					
				case "rectangle":
					s = ShapeMaker.makeRectangle(x1, y1, e.getX(), e.getY());
					MyRectangle myRectangle = new MyRectangle(s, color, username, (int)strock.getLineWidth(), fill);
					state.getShapes().add(myRectangle);
					Draw();
					sendDrawRequest(myRectangle);
					break;
					
				case "circle":
					s = ShapeMaker.makeCircle(x1, y1, e.getX(), e.getY());
					MyEllipse myCircle = new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill);
					state.getShapes().add(myCircle);
					Draw();
					sendDrawRequest(myCircle);
					break;
				
				case "oval":
					s = ShapeMaker.makeOval(x1, y1, e.getX(), e.getY());
					MyEllipse myOval = new MyEllipse(s, color, username, (int)strock.getLineWidth(), fill);
					state.getShapes().add(myOval);
					Draw();
					sendDrawRequest(myOval);	
					break;
					
				case "eraser":
					shapesPreview.clear();
					Draw();
					break;
					
				default:
					System.out.println("Unsupported Shape");
			}
	    }
		
	};

	private synchronized static void Draw() {
		for (MyShape s : state.getShapes()) {
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
	}
	
	private void RePaint(Graphics2D g2d) {
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
	
	private void DrawPreview() {
		for (MyShape s : shapesPreview) {
			strock = new BasicStroke(s.getThickness());
			g.setStroke(strock);
	        g.setPaint(s.getColor());
	        g.draw(s.getShape());
	        if (s.getFill()) {
	        	g.fill(s.getShape());
	        }
	    }
	} 
	
	private synchronized static void Clear(int width, int height) {
		g.setPaint(Color.WHITE);
		Shape s = ShapeMaker.makeRectangle(0, 0, width, height);
		g.draw(s);
		g.fill(s);
	}
	
	private synchronized static void ReturnHome() {
		state.New();
		Clear((int) (Window.WIDTH), (int) (Window.HEIGHT));
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
	
	private synchronized static void updateUserList(String name, String option) {
		if (option.equals("add")) {
	    	users.addElement(name);
	        userList.setModel(users);
		} else {
			users.removeElement(name);
	        userList.setModel(users);
		}
	}
	
	private static ImageIcon reSizeForButton(ImageIcon icon, JButton btn) {
		btn.setOpaque(false);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);
		btn.setBorder(null);
		btn.setMargin(new Insets(0, 0, 0, 0));
		Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance((int)(btn.getWidth() * 0.7), (int) (btn.getHeight() * 0.7),  java.awt.Image.SCALE_SMOOTH);  
	    return new ImageIcon(resizedImage);
	}
	
	private static ImageIcon reSizeForLabel(ImageIcon icon, JLabel label) {
		label.setOpaque(false);
		Image img = icon.getImage();  
	    Image resizedImage = img.getScaledInstance(label.getWidth(), label.getHeight(),  java.awt.Image.SCALE_SMOOTH);  
	    return new ImageIcon(resizedImage);
	}
	
    private synchronized static void appendToPane(JTextPane tp, String msg, Color c, Boolean bold) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        
        if (bold) {
        	aset = sc.addAttribute(aset, StyleConstants.Bold, true);
        } else {
        	aset = sc.addAttribute(aset, StyleConstants.Bold, false);
        }
        
        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
   }
   
   private void sendDrawRequest(Object obj) {
	   try {
	       client.requestDraw(obj, time);
	   } catch (AbnormalCommunicationException | IOException e) {
	       e.printStackTrace();
	   } 
   }
}