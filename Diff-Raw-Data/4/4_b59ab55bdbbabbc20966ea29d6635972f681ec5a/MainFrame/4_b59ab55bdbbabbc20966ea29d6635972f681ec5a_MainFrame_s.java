 /*
  * Created by JFormDesigner on Tue May 29 18:23:36 CEST 2012
  */
 
 package ui;
 
 import interfaces.Message;
 import interfaces.Orientation;
 import interfaces.User;
 import interfaces.UserMood;
 import interfaces.UserSex;
 import interfaces.UserSize;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import utils.ClientUtils;
 import client.Client;
 
 /**
  * @author Bertrand Pages
  */
 public class MainFrame extends JFrame {
 	public MainFrame() {
 		this.setPreferredSize(new Dimension(500, 500));
 		initComponents();
 	}
 
 	private void envoyerActionPerformed(ActionEvent e) {
 		this.sendMessage();
 	}
 
 	private void moodActionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.contentMenu)) {
 			Client.getUserManager().changeMood(UserMood.CONTENT);
 		} else if (e.getSource().equals(this.inquietMenu)) {
 			Client.getUserManager().changeMood(UserMood.INQUIET);
 		} else if (e.getSource().equals(this.effrayeMenu)) {
 			Client.getUserManager().changeMood(UserMood.EFFRAYE);
 		} else if (e.getSource().equals(this.hilareMenu)) {
 			Client.getUserManager().changeMood(UserMood.HILARE);
 		} else if (e.getSource().equals(this.tristeMenu)) {
 			Client.getUserManager().changeMood(UserMood.TRISTE);
 		}
 	}
 
 	private void tailleActionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.geantMenu)) {
 			Client.getUserManager().changeSize(UserSize.GEANT);
 		} else if (e.getSource().equals(this.grandMenu)) {
 			Client.getUserManager().changeSize(UserSize.GRAND);
 		} else if (e.getSource().equals(this.moyenMenu)) {
 			Client.getUserManager().changeSize(UserSize.MOYEN);
 		} else if (e.getSource().equals(this.petitMenu)) {
 			Client.getUserManager().changeSize(UserSize.PETIT);
 		} else if (e.getSource().equals(this.nainMenu)) {
 			Client.getUserManager().changeSize(UserSize.NAIN);
 		}
 	}
 
 	private void sexeActionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.hommeMenu)) {
 			Client.getUserManager().changeSex(UserSex.MALE);
 		} else if (e.getSource().equals(this.femmeMenu)) {
 			Client.getUserManager().changeSex(UserSex.FEMALE);
 		}
 	}
 
 	private void messageToSendKeyPressed(KeyEvent e) {
 		int key = e.getKeyCode();
 
 		if (key == KeyEvent.VK_ENTER) {
 			this.sendMessage();
 		}
 	}
 
 	private void changeRoomActionPerformed(ActionEvent e) {
 		if (e.getSource().equals(this.northMenu)) {
 			Client.getUserManager().changeRoom(Orientation.NORTH);
 		} else if (e.getSource().equals(this.southMenu)) {
 			Client.getUserManager().changeRoom(Orientation.SOUTH);
 		} else if (e.getSource().equals(this.eastMenu)) {
 			Client.getUserManager().changeRoom(Orientation.EAST);
 		} else if (e.getSource().equals(this.WestMenu)) {
 			Client.getUserManager().changeRoom(Orientation.WEST);
 		}
 	}
 
 	private void quitActionPerformed(ActionEvent e) {
 		Client.getUserManager().logout();
 	}
 
 	private void thisWindowClosing(WindowEvent e) {
 		Client.getUserManager().logout();
 	}
 
 	private void list1MouseClicked(MouseEvent e) {
 		if (!this.list1.isSelectionEmpty()) {
 			String login = (String) this.list1.getSelectedValue();
 			User user = Client.getUserManager().getUserInRoom(login);
 			InformationDialog info = new InformationDialog(this, user);
 			info.setVisible(true);
 		}
 	}
 
 	private void kickItemActionPerformed(ActionEvent e) {
 		Client.getUserManager().adminGetConnectedUsers();
 		
 	}
 
 	private void listUserItemActionPerformed(ActionEvent e) {
 		Client.getUserManager().adminGetAllUsers();
 	}
 
 	private void listRoomActionPerformed(ActionEvent e) {
 		Client.getUserManager().adminGetAllRoomNames();
 	}
 
 	private void initSystemeMenuActionPerformed(ActionEvent e) {
 		Client.getUserManager().adminInitSystem();
 	}
 
 	private void initComponents() {
 		// JFormDesigner - Component initialization - DO NOT MODIFY
 		// //GEN-BEGIN:initComponents
 		// Generated using JFormDesigner Evaluation license - Bertrand Pages
 		menuBar1 = new JMenuBar();
 		menu1 = new JMenu();
 		adminMenu = new JMenu();
 		kickItem = new JMenuItem();
 		listUserItem = new JMenuItem();
 		menuItem2 = new JMenuItem();
 		initSystemeMenu = new JMenuItem();
 		menuItem1 = new JMenuItem();
 		menu2 = new JMenu();
 		northMenu = new JMenuItem();
 		southMenu = new JMenuItem();
 		eastMenu = new JMenuItem();
 		WestMenu = new JMenuItem();
 		menu3 = new JMenu();
 		contentMenu = new JMenuItem();
 		tristeMenu = new JMenuItem();
 		effrayeMenu = new JMenuItem();
 		inquietMenu = new JMenuItem();
 		hilareMenu = new JMenuItem();
 		menu4 = new JMenu();
 		geantMenu = new JMenuItem();
 		grandMenu = new JMenuItem();
 		moyenMenu = new JMenuItem();
 		petitMenu = new JMenuItem();
 		nainMenu = new JMenuItem();
 		menu5 = new JMenu();
 		hommeMenu = new JMenuItem();
 		femmeMenu = new JMenuItem();
 		splitPane1 = new JSplitPane();
 		messageToSend = new JTextField();
 		button1 = new JButton();
 		splitPane2 = new JSplitPane();
 		scrollPane2 = new JScrollPane();
 		chatArea = new JTextArea();
 		scrollPane3 = new JScrollPane();
 		list1 = new JList();
 
 		//======== this ========
 		setTitle("PizzaChat");
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				thisWindowClosing(e);
 			}
 		});
 		Container contentPane = getContentPane();
 		contentPane.setLayout(new BorderLayout());
 
 		//======== menuBar1 ========
 		{
 
 			//======== menu1 ========
 			{
 				menu1.setText("File");
 
 				//======== adminMenu ========
 				{
 					adminMenu.setText("Administration");
 
 					//---- kickItem ----
 					kickItem.setText("Expulser un utilisateur");
 					kickItem.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							kickItemActionPerformed(e);
 						}
 					});
 					adminMenu.add(kickItem);
 
 					//---- listUserItem ----
 					listUserItem.setText("Lister tout les utilisateurs");
 					listUserItem.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							listUserItemActionPerformed(e);
 						}
 					});
 					adminMenu.add(listUserItem);
 
 					//---- menuItem2 ----
 					menuItem2.setText("Lister toutes les salles");
 					menuItem2.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							listRoomActionPerformed(e);
 						}
 					});
 					adminMenu.add(menuItem2);
 
 					//---- initSystemeMenu ----
 					initSystemeMenu.setText("Initialiser le syst\u00e8me");
 					initSystemeMenu.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							initSystemeMenuActionPerformed(e);
 						}
 					});
 					adminMenu.add(initSystemeMenu);
 				}
 				menu1.add(adminMenu);
 
 				//---- menuItem1 ----
 				menuItem1.setText("Quit");
 				menuItem1.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						quitActionPerformed(e);
 					}
 				});
 				menu1.add(menuItem1);
 			}
 			menuBar1.add(menu1);
 
 			//======== menu2 ========
 			{
 				menu2.setText("Room");
 
 				//---- northMenu ----
 				northMenu.setText("Nord");
 				northMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						changeRoomActionPerformed(e);
 					}
 				});
 				menu2.add(northMenu);
 
 				//---- southMenu ----
 				southMenu.setText("Sud");
 				southMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						changeRoomActionPerformed(e);
 					}
 				});
 				menu2.add(southMenu);
 
 				//---- eastMenu ----
 				eastMenu.setText("Est");
 				eastMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						changeRoomActionPerformed(e);
 					}
 				});
 				menu2.add(eastMenu);
 
 				//---- WestMenu ----
 				WestMenu.setText("Ouest");
 				WestMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						changeRoomActionPerformed(e);
 					}
 				});
 				menu2.add(WestMenu);
 			}
 			menuBar1.add(menu2);
 
 			//======== menu3 ========
 			{
 				menu3.setText("Humeur");
 
 				//---- contentMenu ----
 				contentMenu.setText("Content");
 				contentMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						moodActionPerformed(e);
 					}
 				});
 				menu3.add(contentMenu);
 
 				//---- tristeMenu ----
 				tristeMenu.setText("Triste");
 				tristeMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						moodActionPerformed(e);
 					}
 				});
 				menu3.add(tristeMenu);
 
 				//---- effrayeMenu ----
 				effrayeMenu.setText("Effray\u00e9");
 				effrayeMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						moodActionPerformed(e);
 					}
 				});
 				menu3.add(effrayeMenu);
 
 				//---- inquietMenu ----
 				inquietMenu.setText("Inquiet");
 				inquietMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						moodActionPerformed(e);
 					}
 				});
 				menu3.add(inquietMenu);
 
 				//---- hilareMenu ----
 				hilareMenu.setText("Hilare");
 				hilareMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						moodActionPerformed(e);
 					}
 				});
 				menu3.add(hilareMenu);
 			}
 			menuBar1.add(menu3);
 
 			//======== menu4 ========
 			{
 				menu4.setText("Taille");
 
 				//---- geantMenu ----
 				geantMenu.setText("G\u00e9ant");
 				geantMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						tailleActionPerformed(e);
 					}
 				});
 				menu4.add(geantMenu);
 
 				//---- grandMenu ----
 				grandMenu.setText("Grand");
 				grandMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						tailleActionPerformed(e);
 					}
 				});
 				menu4.add(grandMenu);
 
 				//---- moyenMenu ----
 				moyenMenu.setText("Moyen");
 				moyenMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						tailleActionPerformed(e);
 					}
 				});
 				menu4.add(moyenMenu);
 
 				//---- petitMenu ----
 				petitMenu.setText("Petit");
 				petitMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						tailleActionPerformed(e);
 					}
 				});
 				menu4.add(petitMenu);
 
 				//---- nainMenu ----
 				nainMenu.setText("Nain");
 				nainMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						tailleActionPerformed(e);
 					}
 				});
 				menu4.add(nainMenu);
 			}
 			menuBar1.add(menu4);
 
 			//======== menu5 ========
 			{
 				menu5.setText("Sexe");
 
 				//---- hommeMenu ----
 				hommeMenu.setText("Homme");
 				hommeMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sexeActionPerformed(e);
 					}
 				});
 				menu5.add(hommeMenu);
 
 				//---- femmeMenu ----
 				femmeMenu.setText("Femme");
 				femmeMenu.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sexeActionPerformed(e);
 					}
 				});
 				menu5.add(femmeMenu);
 			}
 			menuBar1.add(menu5);
 		}
 		setJMenuBar(menuBar1);
 
 		//======== splitPane1 ========
 		{
 			splitPane1.setResizeWeight(0.8);
 			splitPane1.setEnabled(false);
 
 			//---- messageToSend ----
 			messageToSend.addKeyListener(new KeyAdapter() {
 				@Override
 				public void keyPressed(KeyEvent e) {
 					messageToSendKeyPressed(e);
 				}
 			});
 			splitPane1.setLeftComponent(messageToSend);
 
 			//---- button1 ----
 			button1.setText("Envoyer");
 			button1.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					envoyerActionPerformed(e);
 				}
 			});
 			splitPane1.setRightComponent(button1);
 		}
 		contentPane.add(splitPane1, BorderLayout.SOUTH);
 
 		//======== splitPane2 ========
 		{
 			splitPane2.setEnabled(false);
 			splitPane2.setResizeWeight(0.8);
 			splitPane2.setPreferredSize(new Dimension(132, 46));
 
 			//======== scrollPane2 ========
 			{
 				scrollPane2.setViewportView(chatArea);
 			}
 			splitPane2.setLeftComponent(scrollPane2);
 
 			//======== scrollPane3 ========
 			{
 
 				//---- list1 ----
 				list1.addMouseListener(new MouseAdapter() {
 					@Override
 					public void mouseClicked(MouseEvent e) {
 						list1MouseClicked(e);
 					}
 				});
 				scrollPane3.setViewportView(list1);
 			}
 			splitPane2.setRightComponent(scrollPane3);
 		}
 		contentPane.add(splitPane2, BorderLayout.CENTER);
 		pack();
 		setLocationRelativeTo(getOwner());
 		// //GEN-END:initComponents
 	}
 
 	// JFormDesigner - Variables declaration - DO NOT MODIFY
 	// //GEN-BEGIN:variables
 	// Generated using JFormDesigner Evaluation license - Bertrand Pages
 	private JMenuBar menuBar1;
 	private JMenu menu1;
 	private JMenu adminMenu;
 	private JMenuItem kickItem;
 	private JMenuItem listUserItem;
 	private JMenuItem menuItem2;
 	private JMenuItem initSystemeMenu;
 	private JMenuItem menuItem1;
 	private JMenu menu2;
 	private JMenuItem northMenu;
 	private JMenuItem southMenu;
 	private JMenuItem eastMenu;
 	private JMenuItem WestMenu;
 	private JMenu menu3;
 	private JMenuItem contentMenu;
 	private JMenuItem tristeMenu;
 	private JMenuItem effrayeMenu;
 	private JMenuItem inquietMenu;
 	private JMenuItem hilareMenu;
 	private JMenu menu4;
 	private JMenuItem geantMenu;
 	private JMenuItem grandMenu;
 	private JMenuItem moyenMenu;
 	private JMenuItem petitMenu;
 	private JMenuItem nainMenu;
 	private JMenu menu5;
 	private JMenuItem hommeMenu;
 	private JMenuItem femmeMenu;
 	private JSplitPane splitPane1;
 	private JTextField messageToSend;
 	private JButton button1;
 	private JSplitPane splitPane2;
 	private JScrollPane scrollPane2;
 	private JTextArea chatArea;
 	private JScrollPane scrollPane3;
 	private JList list1;
 	// JFormDesigner - End of variables declaration //GEN-END:variables
 
 	private DefaultListModel<String> connectedList;
 
 	public void initialize() {
 		this.chatArea.setEditable(false);
 	}
 
 	public void hideAdmin() {
 		this.adminMenu.setVisible(false);
 	}
 
 	public void updateListConnected(List<String> logins) {
 		connectedList = new DefaultListModel<String>();
 		for (String login : logins) {
 			connectedList.addElement(login);
 		}
 		this.list1.setModel(connectedList);
 		this.scrollPane3.updateUI();
 	}
 
 	public void newMessage(String username, String message, String date) {
 		this.updateChatArea(username + " [" + date + "] : " + message + "\n");
 	}
 
 	public void newSingleMessage(String username, String message, String date) {
 		this.updateChatArea("From " + username + " [" + date + "] : " + message + "\n");
 	}
 
 	public void newSendSingleMessage(String username, String message, String date) {
 		this.updateChatArea("To " + username + " [" + date + "] : " + message + "\n");
 	}
 
 	public void newLogin(String username) {
 		newUserInformation(username, "s'est connecté à");
 	}
 
 	public void newEnterRoom(String username) {
 		newUserInformation(username, "est entré dans");
 	}
 
 	public void newLogout(String username) {
 		newUserInformation(username, "s'est déconnecté de");
 	}
 
 	public void newLeaveRoom(String username) {
 		newUserInformation(username, "est sorti de");
 	}
 
 	private void newUserInformation(String username, String message) {
 		this.updateChatArea(username + " " + message + " la salle\n");
 	}
 
 	public void setTitleFrame(String room) {
 		this.setTitle("PizzaChat - " + room);
 	}
 
 	public void newMood(String username, UserMood userMood) {
 		String mood = "";
 		if (userMood == UserMood.CONTENT) {
 			mood = "content :)";
 		} else if (userMood == UserMood.EFFRAYE) {
 			mood = "effrayé ! Ahhhhh";
 		} else if (userMood == UserMood.HILARE) {
 			mood = "hilare :D";
 		} else if (userMood == UserMood.INQUIET) {
 			mood = "inquiet :s";
 		} else if (userMood == UserMood.TRISTE) {
 			mood = "triste :(";
 		}
 		updateChatArea(username + " est maintenant " + mood + "\n");
 	}
 
 	public void newSex(String username, UserSex userSex) {
 		String sex = "";
 		if (userSex == UserSex.MALE) {
 			sex = "un homme !";
 		} else if (userSex == UserSex.FEMALE) {
 			sex = "une femme !";
 		}
 		updateChatArea(username + " est maintenant " + sex + "\n");
 	}
 
 	public void newSize(String username, UserSize userSize) {
 		String size = "";
 		if (userSize == UserSize.GEANT) {
 			size = "geant !";
 		} else if (userSize == UserSize.GRAND) {
 			size = "grand !";
 		} else if (userSize == UserSize.MOYEN) {
 			size = " de taille moyenne !";
 		} else if (userSize == UserSize.PETIT) {
 			size = "petit !";
 		} else if (userSize == UserSize.NAIN) {
 			size = "un nain !";
 		}
 		updateChatArea(username + " est maintenant " + size + "\n");
 	}
 
 	private void updateChatArea(String message) {
 		String text = this.chatArea.getText();
 		StringBuffer buffer = new StringBuffer(text);
 		buffer.append(message);
 		this.chatArea.setText(buffer.toString());
 		this.chatArea.updateUI();
 	}
 
 	public void clearChatArea() {
 		this.chatArea.setText(null);
 		this.chatArea.updateUI();
 	}
 
 	public void clearMessageToSend() {
 		this.messageToSend.setText(null);
 		this.messageToSend.setCaretPosition(0);
 		this.messageToSend.updateUI();
 	}
 
 	private void sendMessage() {
 		String message = this.messageToSend.getText();
 		this.clearMessageToSend();
 		if (message.startsWith("/w")) {
 			Client.getUserManager().sendSingleCastMessage(message);
 		} else {
 			Client.getUserManager().sendBroadCastMessage(message);
 		}
 	}
 
 	public void showOldMessages(Message[] messages) {
 		for (Message message : messages) {
 			Date date = new Date(message.timestamp);
 			String formattedDate = ClientUtils.getDateString(date);
 			newSingleMessage(message.sender, message.content, formattedDate);
 		}
 	}
 
 }
