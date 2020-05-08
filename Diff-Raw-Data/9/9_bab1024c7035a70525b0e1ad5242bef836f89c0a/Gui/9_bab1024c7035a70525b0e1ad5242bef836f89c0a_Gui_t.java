 package gamesincommon;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.border.TitledBorder;
 
 import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
 import com.github.koraktor.steamcondenser.steam.community.SteamGame;
 import com.github.koraktor.steamcondenser.steam.community.SteamId;
 
 public class Gui {
 
 	final static Font font = new Font("Tahoma", Font.PLAIN, 18);
 
 	private JFrame gamesInCommonFrame;
 	private JTextArea outputTextArea;
 	private JList<String> playerList;
 	// playerListModel is for display only - playerIdList is the real thing
 	private DefaultListModel<String> playerListModel;
 	private ArrayList<SteamId> playerIdList;
 
 	private JTextField addPlayerText;
 	private FilterPanel filterPanel;
 
 	private GamesInCommon gamesInCommon;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Gui window = new Gui();
 					window.gamesInCommonFrame.setVisible(true);
 					window.gamesInCommon = new GamesInCommon();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Gui() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		gamesInCommonFrame = new JFrame();
 		gamesInCommonFrame.setTitle("Games in Common");
 		gamesInCommonFrame.setBounds(100, 100, 900, 400);
 		gamesInCommonFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		final JPanel playerPanel = new JPanel();
 		playerPanel.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
 		JPanel optionsPanel = new JPanel();
 		optionsPanel.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
 		JPanel consolePanel = new JPanel();
 		consolePanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
 
 		JPanel scanPanel = new JPanel();
 		scanPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
 		JPanel outputPanel = new JPanel();
 		outputPanel.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		GroupLayout groupLayout = new GroupLayout(gamesInCommonFrame.getContentPane());
 		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(
 				groupLayout
 						.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(
 								groupLayout
 										.createParallelGroup(Alignment.TRAILING)
 										.addComponent(consolePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
 										.addGroup(
 												groupLayout
 														.createSequentialGroup()
 														.addGroup(
 																groupLayout
 																		.createParallelGroup(Alignment.TRAILING, false)
 																		.addGroup(
 																				groupLayout
 																						.createSequentialGroup()
 																						.addComponent(optionsPanel,
 																								GroupLayout.DEFAULT_SIZE,
 																								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																						.addPreferredGap(ComponentPlacement.UNRELATED)
 																						.addComponent(scanPanel,
 																								GroupLayout.PREFERRED_SIZE, 134,
 																								GroupLayout.PREFERRED_SIZE))
 																		.addComponent(playerPanel, GroupLayout.PREFERRED_SIZE, 430,
 																				GroupLayout.PREFERRED_SIZE))
 														.addPreferredGap(ComponentPlacement.RELATED)
 														.addComponent(outputPanel, GroupLayout.PREFERRED_SIZE, 421,
 																GroupLayout.PREFERRED_SIZE))).addContainerGap()));
 		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
 				groupLayout
 						.createSequentialGroup()
 						.addGap(15)
 						.addGroup(
 								groupLayout
 										.createParallelGroup(Alignment.LEADING)
 										.addGroup(
 												groupLayout
 														.createSequentialGroup()
 														.addComponent(playerPanel, GroupLayout.PREFERRED_SIZE, 148,
 																GroupLayout.PREFERRED_SIZE)
 														.addGap(18)
 														.addGroup(
 																groupLayout
 																		.createParallelGroup(Alignment.TRAILING)
 																		.addComponent(scanPanel, GroupLayout.PREFERRED_SIZE, 75,
 																				GroupLayout.PREFERRED_SIZE)
 																		.addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, 83,
 																				GroupLayout.PREFERRED_SIZE)))
 										.addComponent(outputPanel, GroupLayout.PREFERRED_SIZE, 249, GroupLayout.PREFERRED_SIZE))
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addComponent(consolePanel, GroupLayout.PREFERRED_SIZE, 27, Short.MAX_VALUE).addContainerGap()));
 		outputPanel.setLayout(new GridLayout(1, 0, 0, 0));
 
 		JScrollPane outputScrollPane = new JScrollPane();
 		outputPanel.add(outputScrollPane);
 
 		outputTextArea = new JTextArea();
 		outputTextArea.setFont(new Font("Tahoma", Font.PLAIN, 18));
 		outputScrollPane.setViewportView(outputTextArea);
 		scanPanel.setLayout(new GridLayout(0, 1, 0, 0));
 
 		JScrollPane consoleScrollPane = new JScrollPane();
 
 		GroupLayout gl_consolePanel = new GroupLayout(consolePanel);
 		gl_consolePanel.setHorizontalGroup(gl_consolePanel.createParallelGroup(Alignment.LEADING).addComponent(consoleScrollPane,
 				GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE));
 		gl_consolePanel.setVerticalGroup(gl_consolePanel.createParallelGroup(Alignment.LEADING).addComponent(consoleScrollPane,
 				GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE));
 
 		JTextPane consoleText = new JTextPane();
 		consoleScrollPane.setViewportView(consoleText);
 		consoleText.setFont(new Font("Tahoma", Font.PLAIN, 18));
 
 		MessageConsole messageConsole = new MessageConsole(consoleText);
 		messageConsole.redirectOut(null, System.out);
 		messageConsole.redirectErr(Color.red, System.err);
 		consolePanel.setLayout(gl_consolePanel);
 
 		filterPanel = new FilterPanel();
 		GroupLayout gl_optionsPanel = new GroupLayout(optionsPanel);
 		gl_optionsPanel.setHorizontalGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING).addGroup(
 				gl_optionsPanel.createSequentialGroup().addContainerGap()
 						.addComponent(filterPanel, GroupLayout.PREFERRED_SIZE, 205, Short.MAX_VALUE).addContainerGap()));
 		gl_optionsPanel.setVerticalGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING).addGroup(
 				gl_optionsPanel.createSequentialGroup().addContainerGap()
 						.addComponent(filterPanel, GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE).addContainerGap()));
 		filterPanel.setLayout(new GridLayout(1, 2, 0, 0));
 		optionsPanel.setLayout(gl_optionsPanel);
 
 		JPanel playerButtonPanel = new JPanel();
 		playerButtonPanel.setLayout(new GridLayout(1, 2, 0, 0));
 
 		JButton scanButton = new JButton("Scan");
 		scanButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
 
 		scanButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Thread scanThread = new Thread() {
 					public void run() {
 						scan();
 					}
 				};
 				scanThread.start();
 			}
 		});
 
 		scanPanel.add(scanButton);
 
 		JButton addButton = new JButton("Add");
 		addButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
 
 		addButton.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				addName();
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 
 		});
 
 		playerButtonPanel.add(addButton);
 
 		JButton removeButton = new JButton("Remove");
 		removeButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
 
 		removeButton.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				removeName();
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 
 		});
 
 		playerButtonPanel.add(removeButton);
 
 		addPlayerText = new JTextField();
 		addPlayerText.setColumns(10);
 
 		addPlayerText.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent e) {
 
 				addPlayerText.setText("");
 				addPlayerText.setFont(new Font("Tahoma", Font.PLAIN, 20));
 
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 
 				if (addPlayerText.getText().equals("")) {
 					addPlayerText.setText("Enter player name...");
 					addPlayerText.setFont(new Font("Tahoma", Font.ITALIC, 20));
 				}
 
 			}
 
 		});
 
 		addPlayerText.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 
 				char c = e.getKeyChar();
 
 				if (c == KeyEvent.VK_ENTER) {
 					addName();
 				}
 
 			}
 
 		});
 
 		JScrollPane playerListScrollPane = new JScrollPane();
 		GroupLayout gl_playerPanel = new GroupLayout(playerPanel);
 		gl_playerPanel.setHorizontalGroup(gl_playerPanel.createParallelGroup(Alignment.LEADING).addGroup(
 				gl_playerPanel
 						.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(
 								gl_playerPanel
 										.createParallelGroup(Alignment.LEADING, false)
 										.addComponent(playerButtonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)
 										.addComponent(addPlayerText, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)).addGap(12)
 						.addComponent(playerListScrollPane, GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE).addContainerGap()));
 		gl_playerPanel.setVerticalGroup(gl_playerPanel.createParallelGroup(Alignment.LEADING).addGroup(
 				gl_playerPanel
 						.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(
 								gl_playerPanel
 										.createParallelGroup(Alignment.LEADING)
 										.addComponent(playerListScrollPane, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
 										.addGroup(
 												gl_playerPanel
 														.createSequentialGroup()
 														.addComponent(addPlayerText, GroupLayout.PREFERRED_SIZE, 49,
 																GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED)
 														.addComponent(playerButtonPanel, GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)))
 						.addContainerGap()));
 
 		// initialise data model for playerList, which tells the JList about the data it expects
 		playerListModel = new DefaultListModel<String>();
 		// initialise ArrayList for player list
 		playerIdList = new ArrayList<SteamId>(10);
 		// initialise JList for player list display
 		playerList = new JList<String>(playerListModel);
 		playerList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		playerList.setLayoutOrientation(JList.VERTICAL);
 		playerList.setVisibleRowCount(-1);
 		playerListScrollPane.setViewportView(playerList);
 		playerPanel.setLayout(gl_playerPanel);
 		gamesInCommonFrame.getContentPane().setLayout(groupLayout);
 	}
 
 	/**
 	 * Displays all games from a collection in a new graphical interface frame.
 	 * 
 	 * @param games
 	 *            Games to show.
 	 */
 	public void showCommonGames(final Collection<SteamGame> games) {
 
 		List<String> gameList = new ArrayList<String>();
 
 		for (SteamGame steamGame : games) {
 			gameList.add(steamGame.getName() + "\n");
 		}
 
 		Collections.sort(gameList);
 
 		// Final count.
 		gameList.add("Total games in common: " + games.size());
 
 		for (final String str : gameList) {
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					outputTextArea.append(str);
 				}
 			});
 
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				outputTextArea.setCaretPosition(0);
 			}
 		});
 
 	}
 
 	private List<SteamId> getPlayerIDs() {
 		return playerIdList;
 	}
 
 	private void addName() {
 		// If text field not empty then add player to list.
 		// verify with Steam that the entry is a valid Steam ID and throw an error if not
 		String name = addPlayerText.getText();
 		if ((!name.isEmpty()) && (!name.equals("Enter player name..."))) {
 			try {
 				SteamId temp = gamesInCommon.checkSteamId(name);
 				// add to the list of SteamIds
 				playerIdList.add(temp);
 				// then add to the JList
 				playerListModel.addElement(temp.getNickname());
 			} catch (SteamCondenserException e) {
 				System.err.println("\"" + name + "\": " + e.getMessage());
 			}
 
 			// Afterwards clear the text field.
 			addPlayerText.setText("");
 		}
 	}
 
 	private void removeName() {
		if (playerList.getSelectedIndex() > -1) {
 			// remove the selected player from the SteamId list
 			playerIdList.remove(playerList.getSelectedIndex());
 			// and also from the JList
 			playerListModel.removeElement(playerList.getSelectedValue());
 		}
 	}
 
 	private void scan() {
 
 		System.out.println("Starting scan.");
 
 		// Find common games.
 		Collection<SteamGame> commonGames = gamesInCommon.findCommonGames(getPlayerIDs());
 
 		// Apply filters, if applicable.
 		List<FilterType> filters = filterPanel.getFilters();
 		if (!filters.isEmpty()) {
 			System.out.println("Filtering games... ");
 			commonGames = gamesInCommon.filterGames(commonGames, filters);
 			System.out.println("Filtering complete.");
 		}
 
 		// Display filtered list of common games.
 		showCommonGames(commonGames);
 	}
 }
