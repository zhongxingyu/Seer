 package mcgill.ui;
 
 import javax.swing.JFrame;
 
 import mcgill.game.Chat;
 import mcgill.game.Client;
 import mcgill.game.Table;
 import mcgill.game.User;
 
 import org.eclipse.wb.swing.FocusTraversalOnArray;
 import java.awt.Component;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import javax.swing.JPanel;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.Font;
 import java.awt.Color;
 import javax.swing.border.LineBorder;
 import java.awt.Insets;
 import java.util.Set;
 
 import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.JList;
 
 
 public class MainWindow {
 	
 	private JFrame frame;
 	private JTextField txtGame;
 	private JTable table_1;
 	
 	private Client client;
 	private JTextField txtChatHere;
 	private JTextField txtBetAmt;
 	private JTextField textFriendName;
 
 	/**
 	 * Create the application.
 	 */
 	public MainWindow(Client client) {
 		this.client = client;
 		initialize();
 	}
 	
 	public void open() {
 		frame.setVisible(true);
 	}
 	
 	public DefaultListModel getGameList() {
 		Table[] tables = client.getTables();
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < tables.length; i++) {
 			listModel.addElement(tables[i].getName() + " --- " + tables[i].getUsers().size() + "/5 players");
 		}
 		
 		return listModel;
 	}
 	
 	public DefaultListModel getFriendList() {
 		User[] friends = client.getFriends(client.getUser().getUsername());
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < friends.length; i++) {
 			listModel.addElement(friends[i].getUsername());
 		}
 		
 		return listModel;
 	}
 	
 	public DefaultListModel getChatList() {
 		String username = client.getUser().getUsername();
 		Chat[] chats = client.getChats(username);
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < chats.length; i++) {
 			String chat_str = "";
 			Set<User> users = chats[i].getUsers();
 			for (User user : users) {
 				if (user.getUsername() != username) {
 					chat_str += user.getUsername() + " ";
 				}
 			}
 			
 			listModel.addElement(chat_str);
 		}
 		
 		return listModel;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setResizable(false);
 		frame.setBounds(100, 100, 900, 700);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("max(74dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("308px:grow"),
 				ColumnSpec.decode("max(90dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(9dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("71px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(54dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				RowSpec.decode("488px"),
 				RowSpec.decode("15px"),
 				RowSpec.decode("max(16dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(14dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("26px"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(13dlu;default)"),}));
 		
 		JLabel lblChat = new JLabel("Chat");
 		lblChat.setFont(new Font("Tahoma", Font.BOLD, 14));
 		frame.getContentPane().add(lblChat, "1, 2, center, bottom");
 		
 		JTabbedPane main = new JTabbedPane(JTabbedPane.TOP);
 		main.setBorder(new LineBorder(new Color(0, 0, 0)));
 		frame.getContentPane().add(main, "1, 1, 4, 1, fill, fill");
 		
 		final JScrollPane allGames = new JScrollPane();
 		main.addTab("All Games", null, allGames, null);
 		allGames.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		
 		JList listAllGames = new JList(getGameList());
 		allGames.setViewportView(listAllGames);
 		
 		JPanel createGame = new JPanel();
 		main.addTab("Create Game", null, createGame, null);
 		GridBagLayout gbl_createGame = new GridBagLayout();
 		gbl_createGame.columnWidths = new int[]{94, 181, 0, 0};
 		gbl_createGame.rowHeights = new int[]{27, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_createGame.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_createGame.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		createGame.setLayout(gbl_createGame);
 		
 		JButton btnCreate = new JButton("Create");
 		btnCreate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String username = client.getUser().getUsername();
 				Table table = client.createTable(username, txtGame.getText());
 				client.joinTable(username, table.getId());
 			}
 		});
 		
 		JLabel lblGameCreationOptions = new JLabel("  Game Creation Options:");
 		lblGameCreationOptions.setFont(new Font("Tahoma", Font.BOLD, 18));
 		GridBagConstraints gbc_lblGameCreationOptions = new GridBagConstraints();
 		gbc_lblGameCreationOptions.gridwidth = 2;
 		gbc_lblGameCreationOptions.insets = new Insets(0, 0, 5, 5);
 		gbc_lblGameCreationOptions.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblGameCreationOptions.gridx = 0;
 		gbc_lblGameCreationOptions.gridy = 1;
 		createGame.add(lblGameCreationOptions, gbc_lblGameCreationOptions);
 		
 		JLabel lblGameName = new JLabel("Game Name:");
 		GridBagConstraints gbc_lblGameName = new GridBagConstraints();
 		gbc_lblGameName.anchor = GridBagConstraints.EAST;
 		gbc_lblGameName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblGameName.gridx = 0;
 		gbc_lblGameName.gridy = 3;
 		createGame.add(lblGameName, gbc_lblGameName);
 		
 		txtGame = new JTextField();
 		txtGame.setText("Game1");
 		GridBagConstraints gbc_txtGame = new GridBagConstraints();
 		gbc_txtGame.insets = new Insets(0, 0, 5, 5);
 		gbc_txtGame.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtGame.gridx = 1;
 		gbc_txtGame.gridy = 3;
 		createGame.add(txtGame, gbc_txtGame);
 		txtGame.setColumns(10);
 		GridBagConstraints gbc_btnCreate = new GridBagConstraints();
 		gbc_btnCreate.insets = new Insets(0, 0, 5, 0);
 		gbc_btnCreate.gridx = 2;
 		gbc_btnCreate.gridy = 3;
 		createGame.add(btnCreate, gbc_btnCreate);
 		
 		JPanel currentGame = new JPanel();
 		main.addTab("Current Game", null, currentGame, null);
 		currentGame.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(3dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(10dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(5dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("30dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(5dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("30dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(10dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("13dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("13dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("12dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel pWhite = new JLabel("");
 		pWhite.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar white.png")));
 		currentGame.add(pWhite, "4, 2, 1, 5, center, bottom");
 		
 		JLabel pWhiteName = new JLabel("Screen Name");
 		currentGame.add(pWhiteName, "6, 4, 3, 1");
 		
 		JLabel pGreyName = new JLabel("Screen Name");
 		currentGame.add(pGreyName, "20, 4, 3, 1");
 		
 		JLabel pGrey = new JLabel("");
 		pGrey.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar grey.png")));
 		currentGame.add(pGrey, "24, 2, 1, 5, center, bottom");
 		
 		JLabel pWhiteCash = new JLabel("$$$$$");
 		currentGame.add(pWhiteCash, "6, 6, 3, 1");
 		
 		JLabel pGreyCash = new JLabel("$$$$$");
 		currentGame.add(pGreyCash, "20, 6, 3, 1");
 		
 		JLabel pWhiteCard1 = new JLabel("10♠");
 		pWhiteCard1.setOpaque(true);
 		pWhiteCard1.setAlignmentX(Component.CENTER_ALIGNMENT);
 		pWhiteCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard1.setBackground(Color.GRAY);
 		pWhiteCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		currentGame.add(pWhiteCard1, "4, 8, fill, fill");
 		
 		JLabel pWhiteCard2 = new JLabel("10♠");
 		pWhiteCard2.setOpaque(true);
 		pWhiteCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard2.setBackground(Color.WHITE);
 		pWhiteCard2.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard2, "6, 8, fill, fill");
 		
 		JLabel pWhiteCard3 = new JLabel("10♠");
 		pWhiteCard3.setOpaque(true);
 		pWhiteCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard3.setBackground(Color.WHITE);
 		pWhiteCard3.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard3, "8, 8, fill, fill");
 		
 		JLabel pWhiteCard4 = new JLabel("10♠");
 		pWhiteCard4.setOpaque(true);
 		pWhiteCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard4.setBackground(Color.WHITE);
 		pWhiteCard4.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard4, "10, 8, fill, fill");
 		
 		JLabel pWhiteCard5 = new JLabel("10♠");
 		pWhiteCard5.setOpaque(true);
 		pWhiteCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard5.setBackground(Color.WHITE);
 		pWhiteCard5.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard5, "12, 8, fill, fill");
 		
 		JLabel pGreyCard5 = new JLabel("10♠");
 		pGreyCard5.setOpaque(true);
 		pGreyCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard5.setBackground(Color.WHITE);
 		pGreyCard5.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard5, "16, 8, fill, fill");
 		
 		JLabel pGreyCard4 = new JLabel("10♠");
 		pGreyCard4.setOpaque(true);
 		pGreyCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard4.setBackground(Color.WHITE);
 		pGreyCard4.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard4, "18, 8, fill, fill");
 		
 		JLabel pGreyCard3 = new JLabel("10♠");
 		pGreyCard3.setOpaque(true);
 		pGreyCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard3.setBackground(Color.WHITE);
 		pGreyCard3.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard3, "20, 8, fill, fill");
 		
 		JLabel pGreyCard2 = new JLabel("10♠");
 		pGreyCard2.setOpaque(true);
 		pGreyCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard2.setBackground(Color.WHITE);
 		pGreyCard2.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard2, "22, 8, fill, fill");
 		
 		JLabel pGreyCard1 = new JLabel("10♠");
 		pGreyCard1.setOpaque(true);
 		pGreyCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard1.setBackground(Color.GRAY);
 		pGreyCard1.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard1, "24, 8, fill, fill");
 		
 		JLabel pRed = new JLabel("");
 		pRed.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar.png")));
 		currentGame.add(pRed, "4, 12, 1, 5, center, bottom");
 		
 		JLabel pYellow = new JLabel("");
 		pYellow.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar yellow.png")));
 		currentGame.add(pYellow, "24, 12, 1, 5, center, bottom");
 		
 		JLabel pRedName = new JLabel("Screen Name");
 		currentGame.add(pRedName, "6, 14, 3, 1");
 		
 		JLabel pYellowName = new JLabel("Screen Name");
 		currentGame.add(pYellowName, "20, 14, 3, 1");
 		
 		JLabel pRedCash = new JLabel("$$$$$");
 		currentGame.add(pRedCash, "6, 16, 3, 1");
 		
 		JLabel pYellowCash = new JLabel("$$$$$");
 		currentGame.add(pYellowCash, "20, 16, 3, 1");
 		
 		JLabel pRedCard1 = new JLabel("10♠");
 		pRedCard1.setOpaque(true);
 		pRedCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard1.setBackground(Color.GRAY);
 		pRedCard1.setAlignmentX(0.5f);
 		currentGame.add(pRedCard1, "4, 18, fill, fill");
 		
 		JLabel pRedCard2 = new JLabel("10♠");
 		pRedCard2.setOpaque(true);
 		pRedCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard2.setBackground(Color.WHITE);
 		pRedCard2.setAlignmentX(0.5f);
 		currentGame.add(pRedCard2, "6, 18, fill, fill");
 		
 		JLabel pRedCard3 = new JLabel("10♠");
 		pRedCard3.setOpaque(true);
 		pRedCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard3.setBackground(Color.WHITE);
 		pRedCard3.setAlignmentX(0.5f);
 		currentGame.add(pRedCard3, "8, 18, fill, fill");
 		
 		JLabel pRedCard4 = new JLabel("10♠");
 		pRedCard4.setOpaque(true);
 		pRedCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard4.setBackground(Color.WHITE);
 		pRedCard4.setAlignmentX(0.5f);
 		currentGame.add(pRedCard4, "10, 18, fill, fill");
 		
 		JLabel pRedCard5 = new JLabel("10♠");
 		pRedCard5.setOpaque(true);
 		pRedCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard5.setBackground(Color.WHITE);
 		pRedCard5.setAlignmentX(0.5f);
 		currentGame.add(pRedCard5, "12, 18, fill, fill");
 		
 		JLabel pYellowCard5 = new JLabel("10♠");
 		pYellowCard5.setOpaque(true);
 		pYellowCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard5.setBackground(Color.WHITE);
 		pYellowCard5.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard5, "16, 18, fill, fill");
 		
 		JLabel pYellowCard4 = new JLabel("10♠");
 		pYellowCard4.setOpaque(true);
 		pYellowCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard4.setBackground(Color.WHITE);
 		pYellowCard4.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard4, "18, 18, fill, fill");
 		
 		JLabel pYellowCard3 = new JLabel("10♠");
 		pYellowCard3.setOpaque(true);
 		pYellowCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard3.setBackground(Color.WHITE);
 		pYellowCard3.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard3, "20, 18, fill, fill");
 		
 		JLabel pYellowCard2 = new JLabel("10♠");
 		pYellowCard2.setOpaque(true);
 		pYellowCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard2.setBackground(Color.WHITE);
 		pYellowCard2.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard2, "22, 18, fill, fill");
 		
 		JLabel pYellowCard1 = new JLabel("10♠");
 		pYellowCard1.setOpaque(true);
 		pYellowCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard1.setBackground(Color.GRAY);
 		pYellowCard1.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard1, "24, 18, fill, fill");
 		
 		JLabel pBlue = new JLabel("");
 		pBlue.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar blue.png")));
 		currentGame.add(pBlue, "10, 24, 1, 3, center, center");
 		
 		JLabel pBlueName = new JLabel("Screen Name");
 		currentGame.add(pBlueName, "12, 24, 3, 1");
 		
 		JLabel pBlueCard1 = new JLabel("10♠");
 		pBlueCard1.setOpaque(true);
 		pBlueCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard1.setBackground(Color.GRAY);
 		pBlueCard1.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard1, "16, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard2 = new JLabel("10♠");
 		pBlueCard2.setOpaque(true);
 		pBlueCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard2.setBackground(Color.WHITE);
 		pBlueCard2.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard2, "18, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard3 = new JLabel("10♠");
 		pBlueCard3.setOpaque(true);
 		pBlueCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard3.setBackground(Color.WHITE);
 		pBlueCard3.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard3, "20, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard4 = new JLabel("10♠");
 		pBlueCard4.setOpaque(true);
 		pBlueCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard4.setBackground(Color.WHITE);
 		pBlueCard4.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard4, "22, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard5 = new JLabel("10♠");
 		pBlueCard5.setOpaque(true);
 		pBlueCard5.setForeground(Color.BLACK);
 		pBlueCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard5.setBackground(Color.WHITE);
 		pBlueCard5.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard5, "24, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCash = new JLabel("$$$$$");
 		currentGame.add(pBlueCash, "12, 26, 3, 1");
 		
 		JButton btnCheck = new JButton("Check");
 		currentGame.add(btnCheck, "10, 30, 3, 1");
 		
 		JButton btnFold = new JButton("Fold");
 		currentGame.add(btnFold, "16, 30, 3, 1");
 		
 		txtBetAmt = new JTextField();
 		txtBetAmt.setText("bet amt");
 		currentGame.add(txtBetAmt, "10, 32, 3, 1, fill, default");
 		txtBetAmt.setColumns(10);
 		
 		JButton btnBet = new JButton("Bet");
 		currentGame.add(btnBet, "16, 32, 3, 1");
 		
		final JTabbedPane friends = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(friends, "6, 1, 7, 1, fill, fill");	
 	
 		final JScrollPane allFriends = new JScrollPane();
 		allFriends.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		allFriends.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		friends.addTab("Friends", null, allFriends, null);
 
 		allFriends.setViewportView(table_1);		
 		
 		JList listFriends = new JList(getFriendList());
 		allFriends.setViewportView(listFriends);
 		
 		JPanel addFriend = new JPanel();
 		friends.addTab("Add Friend", null, addFriend, null);
 		addFriend.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblFriend = new JLabel("Friend:");
 		addFriend.add(lblFriend, "2, 2, right, default");
 		
 		textFriendName = new JTextField();
 		addFriend.add(textFriendName, "4, 2, fill, default");
 		textFriendName.setColumns(10);
 		
 		JButton btnAddFriend = new JButton("Add Friend");
 		btnAddFriend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Boolean result = client.addFriend(client.getUser().getUsername(), textFriendName.getText());
 				
 				if (result) {
 					allFriends.setViewportView(new JList(getFriendList()));
					friends.setSelectedIndex(0);
 				} else {
 					JOptionPane.showMessageDialog(frame, "Friend not found :(");
 				}
 			}
 		});
 		addFriend.add(btnAddFriend, "4, 4");
 		
 		final JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		frame.getContentPane().add(scrollPane, "1, 3, 1, 9, fill, fill");
 		
 		JList listChats = new JList(getChatList());
 		scrollPane.setViewportView(listChats);
 		
 		JScrollPane chatContainer = new JScrollPane();
 		chatContainer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		frame.getContentPane().add(chatContainer, "3, 3, 2, 7, fill, fill");
 		
 		JList listChatArea = new JList();
 		chatContainer.setViewportView(listChatArea);
 		
 		JButton btnRefresh = new JButton("Refresh");
 		btnRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				allGames.setViewportView(new JList(getGameList()));
 				scrollPane.setViewportView(new JList(getChatList()));
 				allFriends.setViewportView(new JList(getFriendList()));
 			}
 		});
 		frame.getContentPane().add(btnRefresh, "10, 5");
 		
 		JLabel label = new JLabel("");
 		label.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar main.png")));
 		frame.getContentPane().add(label, "8, 3, 1, 3, center, bottom");
 		
 		JButton btnOptions = new JButton("Options");
 		frame.getContentPane().add(btnOptions, "10, 7");
 		
 		JLabel lblScreenName = new JLabel(this.client.getUser().getUsername());
 		frame.getContentPane().add(lblScreenName, "8, 9, center, center");
 		
 		JButton btnLogOut = new JButton("Log Out");
 		frame.getContentPane().add(btnLogOut, "10, 9");
 		
 		txtChatHere = new JTextField();
 		txtChatHere.setText("chat here");
 		frame.getContentPane().add(txtChatHere, "3, 11, fill, fill");
 		txtChatHere.setColumns(10);
 		
 		JButton btnSend = new JButton("Send");
 		frame.getContentPane().add(btnSend, "4, 11");
 		
 		JLabel cash = new JLabel(client.getUser().getCredits() + "$");
 		frame.getContentPane().add(cash, "8, 11, center, top");
 		
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		frame.getContentPane().add(btnQuit, "10, 11");
 		frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{frame.getContentPane()}));
 	}
 }
