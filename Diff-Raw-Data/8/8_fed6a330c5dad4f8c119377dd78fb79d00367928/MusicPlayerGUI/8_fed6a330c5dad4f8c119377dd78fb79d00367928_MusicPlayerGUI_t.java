 /**
  * Projekt - INDA12 - V�rterminen 2013
  *
  * @author Marcus Heine & Mark Hobro
  *
  * 	Saker som kan vara värda att tänka på:
  * 	- Ibland får vi NullPointerException lite �verallt; t.ex om man trycker p� "Play" utan att ha markerat en l�t.
  * 
  * 
  * 	MAIN TODO
  * 	Panel med knappar och sliders
  * 	- Fixa så att JSlidern uppdateras allt eftersom l�ten spelas.
  * 	- JSlider (eller liknande) f�r att �ndra volym
  * 	- Knapparna - Placering och design
  * 
  * 	Uppspelning
  * 	- Synkronisering; hur funkar det och beh�ver vi det?
  * 	- Fixa s� att Repeat-All funkar
  * 	- Implementera Repeat-One?
  * 	- Implementera att man ska kunna dra JSlidern och v�lja vartifr�n i l�ten man ska spela? Sv�rt.
  * 	- K�-funktion
  * 
  * 	Menubar
  * 	- Preferences?
  * 	- Help?
  *  	- "Back to Main List"
  *  
  *  	Övrigt
  *  	- Se till att JLabeln skriver ut vilken l�t som spelas atm.
  * 	 - Search/Filter-funktion
  *  	- Fixa så att man skriver in DIRECTORY via en JInputPane (?)
  *  	- Reklam? ;D
  * 
  */
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.io.*;
 import java.util.Arrays;
 import java.net.*;
 
 import java.util.*;
 import java.util.Timer;
 
 import javax.sound.sampled.*;
 import javax.swing.*;
 import javax.swing.border.Border;
 
 import javazoom.jl.decoder.JavaLayerException;
 
 
 
 public class MusicPlayerGUI extends JFrame implements ActionListener, MouseListener, WindowListener, KeyListener, ComponentListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private Timer tim;
 
 	/** Define the colours */
 	public static final Color TRACKLIST_GREY = new Color(55, 55, 55);
 	public static final Color LABEL_GREY = new Color(63, 63, 63);
 	public static final Color PLAYLIST_GREY = new Color(70, 70, 70);
 	public static final Color WINDOW_GREY = new Color(150, 150, 150);
 
 	public static final Font MAIN_FONT = new Font("Tahoma", Font.PLAIN, 13);
 
 	public String DIRECTORY = "";
 	
 	public JList tracklist;
 	public JList playlist;
 
 	//Arrar of strings, playlists and tracks respectively
 	public String[] playlistStrings;
 	public String[] tracklistStrings;
 
 	Dimension scrollbarDimension = new Dimension(0, 0);
 
 	private JScrollPane trackGUI;
 
 	private JScrollPane playlistGUI;
 
 	public PlaylistHandler ph = new PlaylistHandler();
 	public TrackHandler th;
 
 	private JMenuBar menuBar = new JMenuBar();
 
 	public PopupListener popupListener;
 
 	private MusicPlayer player;
 
 	private JPanel trackInfoBar = new JPanel();
 
 	private JLabel titleLabel;
 	private JLabel trackInfoLabel;
 	private JLabel placeholder;
 
 	private JMenu menuList = new JMenu("Add to playlist");
 	private JMenu fileMenu = new JMenu("File");
 	private JMenu optionsMenu = new JMenu("Options");
 	private JMenu helpMenu = new JMenu("Help");
 
 	public JMenuItem menuPlay;
 	public JMenuItem menuQueue;
 	public JMenuItem playMenuItem;
 	public JMenuItem backMenuItem;
 	public JMenuItem exitMenuItem;
 	public JMenuItem cdMenuItem;
 	public JMenuItem scrollbarMenuItem;
 	public JMenuItem helpMenuItem;
 
 	private ImageIcon playIcon;
 	private ImageIcon pauseIcon;
 	private ImageIcon nextIcon;
 	private ImageIcon previousIcon;
 	private ImageIcon shuffleTrueIcon;
 	private ImageIcon shuffleFalseIcon;
 	private ImageIcon repeatTrueIcon;
 	private ImageIcon repeatFalseIcon;
 	private ImageIcon playlistIcon;
 
 	private JButton playpauseButton;
 	private JButton nextButton;
 	private JButton previousButton;
 	private JButton shuffleButton;
 	private JButton repeatButton;
 	private JButton playlistButton;
 
 	private JSlider trackTimeSlider;
 
 	private boolean playing;
 	private boolean newTrack;
 	private boolean shuffle;
 	private boolean repeat;
 
 	private String trackName = "";
 	private int TRACK_INDEX;
 
 	//Variabel som anv�nds f�r att h�lla kolla p� f�reg�ende index i listan av l�tar
 	private ArrayList<Integer> previousTrackIndex = new ArrayList<Integer>();
 
 	//List for the queue-function
 	private ArrayList<Integer> queueList = new ArrayList<Integer>();
 
 	//List for the buttons to simplify the code
 	private ArrayList<JButton> listOfButtons = new ArrayList<JButton>();
 
 
 	public MusicPlayerGUI() throws IOException {
 
 
 		setVisible(true);
 
 		setLayout(new GridBagLayout());
 
 		setMinimumSize(new Dimension(800, 580));
 		setSize(new Dimension(1100, 660));
 
 
 		setLocationRelativeTo(null);
 
 		setTitle("MM Music Player");
 
 		attemptToLoadPreferences();
 
 		askForDirectory();
 
 		initMenuBar();
 
 		initIcons();
 
 		initComponents();
 
 		mainLayout();
 
 		updateBarLayout();
 
 		setColours();
 
 		setBorders();
 
 		createPopupMenu();
 
 		updatePopupMenu();
 
 		addComponentListener(this);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					new MusicPlayerGUI();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Initiate the components of the main window.
 	 * @throws IOException 
 	 */
 	private void initComponents() {
 
 		player = new MusicPlayer();
 
 		th = new TrackHandler(DIRECTORY);
 		//ph = new PlaylistHandler();
 
 		playlistStrings = ph.getPlaylists();
 		tracklistStrings = th.getTracks();
 
 		tracklist = new JList();
 		playlist = new JList();
 
 		trackGUI = new JScrollPane(tracklist);
 		playlistGUI = new JScrollPane(playlist);
 
 		tracklist.setListData(tracklistStrings);
 		playlist.setListData(playlistStrings);
 
 		titleLabel = new JLabel();
 		trackInfoLabel = new JLabel();
 		placeholder = new JLabel();
 
 		titleLabel.setText("Main Playlist");
 		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
 		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
 
 		trackInfoBar = new JPanel();
 
 		menuQueue = new JMenuItem("Queue");
 		menuPlay = new JMenuItem("Play");
 
 		//TODO - �ndra f�r att se scrollbaren
 		//Om scrollbaren ska vara synlig eller inte; jag gillar n�r den inte �r det ^^ fak uuuuu, mdi is the shit :D
 		trackGUI.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
 		trackGUI.addMouseListener(this);
 
 		playlistGUI.addMouseListener(this);
 
 		try {
 			tracklist.setSelectedIndex(0);
 		} catch(NullPointerException nullExcept) {
 			System.out.println("There are no files in the directory.");
 		}
 
 		tracklist.addMouseListener(this);
 		playlist.addMouseListener(this);
 		trackInfoLabel.addMouseListener(this);
 
 		playlist.addKeyListener(this);
 
 		int w = this.getBounds().width;
 		trackTimeSlider = new JSlider(0, 5000, 0);
 		trackTimeSlider.setPreferredSize(new Dimension(w-50, 20));
 		trackTimeSlider.setEnabled(false);
 
 		playpauseButton.addActionListener(this);
 		nextButton.addActionListener(this);
 		previousButton.addActionListener(this);
 		shuffleButton.addActionListener(this);
 		repeatButton.addActionListener(this);
 		playlistButton.addActionListener(this);
 
 		this.addWindowListener(this);
 
 		playing = false;
 		shuffle = false;
 		repeat = false;
 		newTrack = true;
 
 		setJMenuBar(menuBar);
 
 	}
 
 
 	private void initMenuBar() { 
 
 		playMenuItem 		= new JMenuItem("Play");
 		backMenuItem		= new JMenuItem("Main list");
 		exitMenuItem 		= new JMenuItem("Exit");
 		cdMenuItem 			= new JMenuItem("Change directory");
 		helpMenuItem 		= new JMenuItem("Help");
 		scrollbarMenuItem	= new JMenuItem("Toggle scrollbar");
 
 		playMenuItem.addActionListener(this);
 		backMenuItem.addActionListener(this);
 		exitMenuItem.addActionListener(this);
 		cdMenuItem.addActionListener(this);
 		helpMenuItem.addActionListener(this);
 		scrollbarMenuItem.addActionListener(this);
 
 		fileMenu.add(playMenuItem);
 		fileMenu.add(backMenuItem);
 		fileMenu.add(exitMenuItem);
 
 		optionsMenu.add(cdMenuItem);
 		optionsMenu.add(scrollbarMenuItem);
 
 		helpMenu.add(helpMenuItem);
 
 		menuBar.add(fileMenu);
 		menuBar.add(optionsMenu);
 		menuBar.add(helpMenu);
 
 		menuBar.setBackground(WINDOW_GREY);
 		menuBar.setBorder(BorderFactory.createMatteBorder(3, 3, 0, 3, Color.BLACK));
 
 		fileMenu.setForeground(Color.black);
 		optionsMenu.setForeground(Color.black);
 		helpMenu.setForeground(Color.black);
 
 		System.out.println("Finished the menu.");
 
 	}
 
 	public void initIcons() throws MalformedURLException {
 
 		//TODO - This part of the code is far from obvious. Please consult http://vimeo.com/20685294 to see how we solved this.
 		playIcon = new ImageIcon(getClass().getResource("play.png"));
 		pauseIcon = new ImageIcon(getClass().getResource("pause.png"));
 		nextIcon = new ImageIcon(getClass().getResource("next.png"));
 		previousIcon = new ImageIcon(getClass().getResource("previous.png"));
 		shuffleTrueIcon = new ImageIcon(getClass().getResource("shuffle_true.png"));
 		shuffleFalseIcon = new ImageIcon(getClass().getResource("shuffle_false.png"));
 		repeatTrueIcon = new ImageIcon(getClass().getResource("repeat_all.png"));
 		repeatFalseIcon = new ImageIcon(getClass().getResource("repeat_false.png"));
 		playlistIcon = new ImageIcon(getClass().getResource("new_playlist.png"));
 
 		playpauseButton = new JButton(playIcon);
 		nextButton = new JButton(nextIcon);
 		previousButton = new JButton(previousIcon);
 		shuffleButton = new JButton(shuffleFalseIcon);
 		repeatButton = new JButton(repeatFalseIcon);
 		playlistButton = new JButton(playlistIcon);
 
 		listOfButtons.add(nextButton);
 		listOfButtons.add(playpauseButton);
 		listOfButtons.add(previousButton);
 		listOfButtons.add(shuffleButton);
 		listOfButtons.add(repeatButton);
 
 		for(JButton b : listOfButtons) {
 			b.setBackground(WINDOW_GREY);
 			b.setPreferredSize(new Dimension(65, 65));
 			b.setBorder(null);
 		}
 
 
 		playlistButton.setBackground(WINDOW_GREY);
 		playlistButton.setPreferredSize(new Dimension(208, 48));
 		playlistButton.setBorder(null);
 
 		repeatButton.setBackground(WINDOW_GREY);
 		repeatButton.setPreferredSize(new Dimension(35, 35));
 		repeatButton.setBorder(null);
 
 		shuffleButton.setBackground(WINDOW_GREY);
 		shuffleButton.setPreferredSize(new Dimension(35, 35));
 		shuffleButton.setBorder(null);
 
 
 
 	}
 
 	private void askForDirectory() { //TODO - Fis this; this is the first message we get
 		
 		//If the DIRECTORY is *not* empty it means it's been changed by the preferences
 		if(DIRECTORY.equals("")) {
 			String dir = JOptionPane.showInputDialog("HELLO AND WELCOME F�R FAN. WRITE THE DIRECTRY LOL");
 			if(dir == null)
 				System.exit(0);
 
 			//If the user doesn't write anything
 			while(dir.equals("")) 
 				dir = JOptionPane.showInputDialog("HELLO AND WELCOME F�R FAN. WRITE THE DIRECTRY LOgsdfk\nL");
 
 			//If you're using Windows and the directory "seems" incomplete, finish it.
 			if(System.getProperty("os.name").startsWith("Win") && !dir.endsWith("\\"))
 				dir = dir.concat("\\");
 
 			this.DIRECTORY = dir;
 		}
 	}
 
 	private void updatePopupMenu() {
 
 		String [] playlists = ph.getPlaylists();
 
 		menuList.removeAll();
 
 		for(int i = 0; i<playlists.length; i++) {
 			JMenuItem listItem = new JMenuItem(playlists[i]);
 			menuList.add(listItem);
 			listItem.setActionCommand(playlists[i]);
 			listItem.addActionListener(this);
 
 		}
 	}
 
 	/** Method that defines where the components should be placed. */
 	private void mainLayout() {
 
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.fill = GridBagConstraints.BOTH;
 		c.ipady = 400;
 		c.ipadx = 200;
 		c.gridheight = 3;
 		// c.anchor = GridBagConstraints.LINE_START;
 		c.weightx = 0.0;
 		c.weighty = 10.0;
 		c.gridx = 0;
 		c.gridy = 0;
 		add(playlistGUI, c);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.ipady = 60;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.gridheight = 1;
 		// /c.anchor = GridBagConstraints.PAGE_START;
 		c.weightx = 0.0;
 		c.weighty = 0.0;
 		c.gridx = 1;
 		c.gridy = 0;
 		add(titleLabel, c);
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.ipady = 18;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.gridheight = 1;
 		c.anchor = GridBagConstraints.PAGE_START;
 		c.weightx = 0.0;
 		c.weighty = 0.0;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(placeholder, c);
 
 		c.fill = GridBagConstraints.BOTH;
 		c.ipady = 40;
 		c.ipadx = 20;
 		c.weightx = 1.0;
 		c.weighty = 1.0;
 		c.gridheight = 2;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(trackGUI, c);
 
 		// c.fill = GridBagConstraints.NONE;
 		c.ipady = 1;
 		c.ipadx = 1;
 		c.weightx = 0.0;
 		c.weighty = 1.0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.anchor = GridBagConstraints.PAGE_END;
 		c.gridheight = 1;
 		c.gridx = 0;
 		c.gridy = 3;
 		add(trackInfoLabel, c);
 
 		// c.fill = GridBagConstraints.BOTH;
 		c.ipady = 110;
 		c.ipadx = 20;
 		c.weightx = 0.0;
 		c.weighty = 1.0;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.gridheight = GridBagConstraints.RELATIVE;
 		c.gridx = 0;
 		c.gridy = 4;
 		add(trackInfoBar, c);
 
 	}
 
 
 	private void updateBarLayout() {
 		SpringLayout sl = new SpringLayout();
 		trackInfoBar.setLayout(sl);
 
 		//trackInfoBar.setPreferredSize(new Dimension(4000, 200));
 
 		int width = this.getWidth();
 
 		trackInfoBar.add(repeatButton);
 
 		trackInfoBar.add(shuffleButton);
 
 
 		trackInfoBar.add(shuffleButton);
 
 		trackInfoBar.add(previousButton);
 
 		trackInfoBar.add(playpauseButton);
 		trackInfoBar.add(nextButton);
 		trackInfoBar.add(playlistButton);
 
 		trackInfoBar.add(trackTimeSlider);
 
 		sl.putConstraint(SpringLayout.WEST, repeatButton, 55, SpringLayout.WEST, this);
 		sl.putConstraint(SpringLayout.NORTH, repeatButton, 25, SpringLayout.NORTH, this);
 
 		sl.putConstraint(SpringLayout.WEST, shuffleButton, 10, SpringLayout.EAST, repeatButton);
 		sl.putConstraint(SpringLayout.NORTH, shuffleButton, 25, SpringLayout.NORTH, this);
 
 		sl.putConstraint(SpringLayout.NORTH, previousButton, 5, SpringLayout.NORTH, this);
 		sl.putConstraint(SpringLayout.WEST, previousButton, (width/2)-150, SpringLayout.WEST, this);
 
 		sl.putConstraint(SpringLayout.NORTH, playpauseButton, 5, SpringLayout.NORTH, this);
 		sl.putConstraint(SpringLayout.WEST, playpauseButton, 20, SpringLayout.EAST, previousButton);
 
 		sl.putConstraint(SpringLayout.NORTH, nextButton, 5, SpringLayout.NORTH, this);
 		sl.putConstraint(SpringLayout.WEST, nextButton, 20, SpringLayout.EAST, playpauseButton);
 
 		sl.putConstraint(SpringLayout.NORTH, playlistButton, 15, SpringLayout.NORTH, this);
 		sl.putConstraint(SpringLayout.WEST, playlistButton, width-250, SpringLayout.WEST, this);
 
 		sl.putConstraint(SpringLayout.NORTH, trackTimeSlider, 80, SpringLayout.NORTH, this);
 		sl.putConstraint(SpringLayout.WEST, trackTimeSlider, 20, SpringLayout.WEST, this);
 
 		trackTimeSlider.setPreferredSize(new Dimension(width-60, 20));
 
 	}
 
 	/** Method to set all the colours */
 	private void setColours() {
 
 		tracklist.setBackground(TRACKLIST_GREY);
 		tracklist.setForeground(Color.WHITE);
 		tracklist.setSelectionBackground(WINDOW_GREY);
 		tracklist.setFixedCellHeight(18);
 		tracklist.setFont(MAIN_FONT);
 		tracklist.setCellRenderer(new MyCellRenderer());
 
 		playlist.setBackground(PLAYLIST_GREY);
 		playlist.setForeground(Color.WHITE);
 		tracklist.setFixedCellHeight(20);
 
 		trackTimeSlider.setBackground(WINDOW_GREY);
 		trackTimeSlider.setForeground(TRACKLIST_GREY);
 
 		titleLabel.setBackground(TRACKLIST_GREY);
 		titleLabel.setForeground(Color.WHITE);
 		titleLabel.setOpaque(true);
 
 		trackInfoLabel.setBackground(LABEL_GREY);
 		trackInfoLabel.setForeground(Color.white);
 		trackInfoLabel.setOpaque(true);
 
 		placeholder.setBackground(WINDOW_GREY);
 		placeholder.setOpaque(true);
 
 		trackInfoBar.setBackground(WINDOW_GREY);
 
 
 	}
 
 	/** Method to set all the borders */
 	private void setBorders() {
 
 		// Create a border for each component. This is probably not the best
 		// solution, but it is a solution that works.
 		Border listBorder = BorderFactory.createMatteBorder(3, 3, 3, 0, Color.BLACK);
 		Border tracklistBorder = BorderFactory.createMatteBorder(3, 3, 3, 3,Color.BLACK);
 		Border infoLabelBorder = BorderFactory.createMatteBorder(0, 3, 3, 0, Color.BLACK);
 		Border titleLabelBorder = BorderFactory.createMatteBorder(3, 3, 0, 3, Color.BLACK);
 		Border barBorder = BorderFactory.createMatteBorder(0, 3, 3, 3, Color.BLACK);
 		Border placeBorder = BorderFactory.createMatteBorder(3, 3, 0, 3, Color.BLACK);
 
 		Border emptyBorder = BorderFactory.createMatteBorder(0, 3, 0, 0, TRACKLIST_GREY);
 
 		playlistGUI.setBorder(listBorder);
 		trackInfoLabel.setBorder(infoLabelBorder);
 
 		trackGUI.setBorder(tracklistBorder);
 		titleLabel.setBorder(titleLabelBorder);
 		trackInfoBar.setBorder(barBorder);
 		placeholder.setBorder(placeBorder);
 
 		tracklist.setBorder(emptyBorder);
 	}
 
 	/** Method to instantiate the PopupMenu class*/
 	public void createPopupMenu() {
 
 		// Create the popup menu.
 		JPopupMenu popup = new JPopupMenu();
 
 		menuPlay.addActionListener(this);
 		menuQueue.addActionListener(this);
 		menuList.addActionListener(this);
 
 		popup.add(menuPlay);
 		popup.add(menuQueue);
 		popup.add(menuList);
 
 		// Add listener to the text area so the popup menu can come up.
 		popupListener = new PopupListener(popup);
 		tracklist.addMouseListener(popupListener);
 		titleLabel.addMouseListener(popupListener);
 	}
 
 	/** Method used to save the preferences between sessions. */
 	private void savePreferences() throws FileNotFoundException {
 
 		String [] playlists = ph.getPlaylists();
 
 		//Clear the file; this is probably inefficient, but it works.
 		PrintWriter writer = new PrintWriter("config.properties");
 		writer.print("");
 		writer.close();
 
 
 
 		try
 		{
 			//Create a FileWriter; i.e a tool that writes text to a file.
 			FileWriter fw = new FileWriter("config.properties", true);
 
 			fw.write("directory=" + DIRECTORY + "\n"); //Appends the directory name to the file
 
 			for(int i = 0; i < playlists.length; i++) {
 
 				//Append the number and name of the playlist
 				fw.write("playlist=" + playlists[i] + "\n");
 				String [] s = ph.getSpecificPlaylist(playlists[i]);
 
 				//Append the string name of each song in the 'current' playlist
 				for(String song : s) {
 					fw.write("song=" + song + "\n");
 				}
 
 			}
 
 			fw.write("Done."); //Not really necessary, but makes for easier programming.
 
 			System.out.println("Preferences and playlists saved.");
 			fw.close();
 		}
 		catch(IOException ioe)
 		{
 			System.err.println("IOException: " + ioe.getMessage());
 		}
 
 
 	}
 
 	/** Method which attempts to read the file set by savePreferences(). */
 	private void attemptToLoadPreferences() throws IOException {
 
 		try {
 			BufferedReader br = new BufferedReader(new FileReader("config.properties"));
 			String line = "";
 			String nameOfPlaylist = "";
 			String nameOfSong = "";
 
 			ph = new PlaylistHandler();
 
 			//As long as there is more to read, attempt to parse the information.
 			while ((line = br.readLine()) != null) {
 
 				if(line.startsWith("directory=")) {
 					line = line.replace("directory=", "");
 					DIRECTORY = line;
 
 					if(System.getProperty("os.name").startsWith("Win") && !DIRECTORY.endsWith("\\"))
 						DIRECTORY = DIRECTORY.concat("\\");
 				}
 
 				//If a playlist is found; get the name of the list and add it to the GUI.
 				if(line.startsWith("playlist=")) {
 					nameOfPlaylist = line.substring(9);
 					ph.createAndAddPlaylist(nameOfPlaylist);
 				}
 
 				//If a song is found, add it to the playlist. This works because of the way we write the song names to this file.
 				else if(line.startsWith("song=")) {
 					nameOfSong = line.substring(5);
 					ph.addToPlaylist(nameOfPlaylist, nameOfSong);
 				}
 
 				else if(line.equals("Done."))
 					break;
 			}
 
 
 			System.out.println("Preferences loaded.");
 			br.close();
 		}
 		catch(IOException exc) {
 			System.out.println("No file found lol.");
 		}
 
 	}
 
 
 	private void changeDirectory(){
 		this.DIRECTORY = JOptionPane.showInputDialog("Enter the directory in which you keep your .mp3 files.\nWhen you have done this, please restart the program.");
 
 		if(System.getProperty("os.name").startsWith("Win") && !DIRECTORY.endsWith("\\"))
 			DIRECTORY = DIRECTORY.concat("\\");
 	}
 
 	/** Method to help the shuffle-function. */
 	private int getRandomTrackIndex(){
 		Random rand = new Random(); 
 		return rand.nextInt(tracklist.getModel().getSize());
 	}
 
 	public void playFromQueueList(){
 		tracklist.setSelectedIndex(queueList.get(0));
 		playTrack();
 		queueList.remove(0);
 	}
 
 	/** Play the next check, while checking if the list is shuffled or not. */
 	private void playNextTrack() {
 
 		if (queueList.size() != 0) {
 			playFromQueueList();
 		} else {
 			if (shuffle == true) {
 				setPreviousTrackIndex();
 				tracklist.setSelectedIndex(getRandomTrackIndex());
 				playTrack();
 			} else if (shuffle == false) {
 				setPreviousTrackIndex();
 				// TODO Detta m�ste testas!
 				if (repeat == true && tracklist.getSelectedIndex() == tracklist.getModel().getSize() - 1) {
 					tracklist.setSelectedIndex(0);
 					playTrack();
 				} else {
 					tracklist.setSelectedIndex(tracklist.getSelectedIndex() + 1);
 					playTrack();
 				}
 			}
 		}
 	}
 
 	/** Method to keep track of and play the previously played tracks */
 	private void playPreviousTrack() {
 		tracklist.setSelectedIndex(previousTrackIndex.get(previousTrackIndex.size() - 1));
 		previousTrackIndex.remove(previousTrackIndex.size() - 1);
 		repaint();
 		playTrack();
 	}
 
 	/** Adds the previously played songs to the appropriate list. */
 	private void setPreviousTrackIndex() {
 		//Unless you are playing and pausing the same track over and over again, this sets the previous track index
 		if (tracklist.getSelectedIndex() != TRACK_INDEX) {
 			previousTrackIndex.add(tracklist.getSelectedIndex());
 			TRACK_INDEX = previousTrackIndex.get(previousTrackIndex.size() - 1);
 		}
 	}
 
 	/** Method which basically check if you're playing or not, defining whether to play or pause.*/
 	private void pauseOrPlay() {
 
 		if (!newTrack && !playing) {
 			player.resumePlaying();
 			playing = true;
 			playpauseButton.setIcon(pauseIcon);
 		} else if (playing && !newTrack) {
 			playpauseButton.setIcon(playIcon);
 			player.pause();
 			playing = false;
 
 		}
 		else
 			playpauseButton.setIcon(pauseIcon);
 
 	}
 
 	/** Method invoked when music is to be played */
 	private void playTrack() {
 		stopTimer();
 
 		if (!trackName.equals(tracklist.getSelectedValue() + ".mp3"))
 			newTrack = true;
 
 		trackName = tracklist.getSelectedValue() + ".mp3";
 
 		if (newTrack) {
 			try {
 				player.play(DIRECTORY + trackName);
 			} catch (JavaLayerException e) {
 				e.printStackTrace();
 			}
 			newTrack = false;
 			playing = true;
 		}
 
 		setLabelText();
 
 		startTimer();
 
 	}
 
 
 	private void setLabelText() {
 
 		String trackName = (String) tracklist.getSelectedValue();
 		trackInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);		
 		trackInfoLabel.setText(trackName);
 
 		if(trackName.length() < 20)
 			trackInfoLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
 		else if(trackName.length() < 28)
 			trackInfoLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
 		else if(trackName.length() < 36)
 			trackInfoLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
 		else
 			trackInfoLabel.setFont(new Font("Tahoma", Font.BOLD, 10));
 
 		setTitle("MM Music Player - " + (String) tracklist.getSelectedValue());
 
 		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 30));
 		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		if(playlist.getSelectedValue() == null)
 			titleLabel.setText("Main Playlist");
 		else
 			titleLabel.setText((String) playlist.getSelectedValue());
 
 	}
 
 	/** Methods that "moves" the JSlider to represent a position in the song*/
 	private void moveSlider(){
 		int totalFrames = player.getLengthInFrames();
 		int currentFrame = player.getPlayingPosition();
 		trackTimeSlider.setMaximum(totalFrames + 1);
 		trackTimeSlider.setValue(currentFrame);
 	}
 
 	/** Create a playlist*/
 	public void createPlaylist() {
 
 		String playlistName = JOptionPane.showInputDialog(null, "Name your list");
 		ph.createAndAddPlaylist(playlistName);
 
 		playlistStrings = ph.getPlaylists();
 		playlist.setListData(playlistStrings);
 
 		updatePopupMenu();
 
 	}
 
 
 	@Override /** Usual ActionPerformed class, this is where the stuff happens. */
 	public void actionPerformed(ActionEvent a) {
 
 
 		if(a.getSource() == playlistButton) {
 			createPlaylist();
 		}
 
 		if (a.getSource() == menuPlay) {
 			setPreviousTrackIndex();
 			playTrack();
 		}
 
 		if (a.getSource() == menuQueue) {
 			queueList.add(tracklist.getSelectedIndex());
 			System.out.println("Queued the track " + tracklist.getSelectedValue());
 		}
 
 		if (a.getSource() == playpauseButton) {
			if(tracklist.getSelectedValue() != null) {
				setPreviousTrackIndex();
				pauseOrPlay();
				playTrack();
			}
 		}
 
 		if (a.getSource() == nextButton) {
 			setPreviousTrackIndex();
 			playNextTrack();
 		}
 
 		if (a.getSource() == previousButton) {
 			//If you have no songs that you previously have played or if you are back at the first song you played, the button won't do anything
 			if(previousTrackIndex.size() != 0){
 				playPreviousTrack();
 			}
 		}
 
 		if (a.getSource() == shuffleButton) {
 			if(shuffle == false){
 				shuffle = true;
 				shuffleButton.setIcon(shuffleTrueIcon);
 			}
 			else if(shuffle == true){
 				shuffle = false;
 				shuffleButton.setIcon(shuffleFalseIcon);
 			}
 		}
 		//Repeats the whole tracklist when it's done
 		if (a.getSource() == repeatButton){
 			if(repeat == false){
 				repeat = true;
 				repeatButton.setIcon(repeatTrueIcon);
 			}
 			else if(repeat == true){
 				repeat = false;
 				repeatButton.setIcon(repeatFalseIcon);
 			}
 		}
 
 		//TODO - Fix this help menu
 		if(a.getSource() == helpMenuItem) {
 			JOptionPane.showMessageDialog(this, "Hj�lp f�r fan.");
 		}
 
 		if(a.getSource() == exitMenuItem) {
 			try {
 				savePreferences();
 			} catch (FileNotFoundException e) {}
 
 			System.exit(0);
 		}
 
 
 		if(a.getSource() == cdMenuItem) {
 			changeDirectory();
 		}
 
 		if(a.getSource() == scrollbarMenuItem) {
 			if(trackGUI.getVerticalScrollBar().getSize().width == 0) {
 				trackGUI.getVerticalScrollBar().setPreferredSize(new Dimension(18, this.getHeight()));
 				trackGUI.getVerticalScrollBar().setValue(trackGUI.getVerticalScrollBar().getValue() + 1);
 			}
 
 			else {
 				trackGUI.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
 				trackGUI.getVerticalScrollBar().setValue(trackGUI.getVerticalScrollBar().getValue() - 1);
 			}
 		}
 
 		if(a.getSource() == backMenuItem) {
 			System.out.println("Attempting to restore main list.");
 			titleLabel.setText("Main Playlist");
 			th.listTracks();
 			tracklist.setListData(th.getTracks());
 		}
 
 		if(a.getSource() == playMenuItem) {
 			setPreviousTrackIndex();
 			playTrack();
 		}
 
 		/*	This checks if we have clicked on any of the "Add to:" playlists.
 		 * 	Because the PopupMenu is based the playlists (and is continously updated)
 		 * 	we check if the returned command is equal to he name of the playlist.
 		 * 	If yes, add the selected song to said playlist.
 		 */
 		for(String listChoice : ph.getPlaylists()) {
 			if(a.getActionCommand().equals(listChoice)) {
 				System.out.println("Attempting to add " + (String) tracklist.getSelectedValue() + " to " + listChoice);
 				ph.addToPlaylist(listChoice, (String) tracklist.getSelectedValue());
 			}
 		}
 
 		//This removes the "border" around the buttons
 		for(JButton b : listOfButtons)
 			b.setFocusPainted(false);
 
 
 
 	}
 
 	/** Method for the timer, which checks if a song is done and also moves the JSlider.*/
 	public void startTimer() {
 
 		//Create a new timer and set it to check if the song has ended and to move the track JSlider
 		tim = new Timer();
 		tim.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 
 				moveSlider();
 
 				if(tracklist.getSelectedValue() == null) {
 					playpauseButton.setEnabled(false);
 					nextButton.setEnabled(false);
 					previousButton.setEnabled(false);
 				}
 				else {
 					playpauseButton.setEnabled(true);
 					nextButton.setEnabled(true);
 					previousButton.setEnabled(true);
 				}
 
 				if(player.hasSongEnded()) {
 					if(repeat == true && tracklist.getSelectedIndex() == tracklist.getModel().getSize()-1){
 						tracklist.setSelectedIndex(0);
 						playTrack();
 					}
 					else{
 						setPreviousTrackIndex();
 						playNextTrack();
 					}
 				}
 
 			}
 		}, 100, 100); //TODO - �ndra h�r om vi vill att det ska uppdateras snabbare | Prestanda?
 
 	}
 
 	public void stopTimer() {
 		if(tim != null)
 			tim.cancel();
 	}
 
 
 	@Override
 	public void mouseClicked(MouseEvent me) {
 		if (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() >= 2) {
 			if(me.getSource() == tracklist) { //Double-clicked somewhere within the tracklist.
 				System.out.println("Du har dubbelklickat p� en l�t: " + tracklist.getSelectedValue());
 				setPreviousTrackIndex();
 				playTrack();
 				playpauseButton.setIcon(pauseIcon);
 			}
 			else if(me.getSource() == playlist) { //Double-clicked somewhere within the list of playlists.
 				if(playlist.getSelectedValue() != null) {
 					System.out.println("Attempting to switch playlists");
 
 					titleLabel.setText((String) playlist.getSelectedValue());
 
 					//Update the tracklist so that it shows the list of tracks in the specified playlist.
 					tracklist.setListData(ph.getSpecificPlaylist((String) playlist.getSelectedValue()));
 				}
 			}
 			else {
 				JScrollBar bar = trackGUI.getVerticalScrollBar();
 				String [] tracks = th.getTracks();
 
 				int trackPos = Arrays.asList(tracks).indexOf(tracklist.getSelectedValue());
 
 				bar.setValue((trackPos*19));
 			}
 		}
 	}
 
 
 
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		// This code snippet check that if the right mouse is clicked, the item
 		// in the JList where you clicked is selected
 		if (SwingUtilities.isRightMouseButton(me)) {
 			tracklist.setSelectedIndex(tracklist.locationToIndex(me.getPoint()));
 		}
 	}
 
 
 
 	public void windowClosing(WindowEvent we) {
 
 		try {
 			savePreferences();
 		} catch (FileNotFoundException e) {}
 
 		System.exit(0);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent ke) {
 		if(ke.getKeyCode() == KeyEvent.VK_DELETE) {
 			if(ph.removeListFromPlaylists((String) playlist.getSelectedValue())) {
 				playlist.setListData(ph.getPlaylists());
 				updatePopupMenu();
 			}
 
 		}
 	}
 
 	@Override
 	public void componentResized(ComponentEvent arg0) {
 		updateBarLayout();		
 	}
 
 
 	//Methods required by the Interfaces
 	public void mouseEntered(MouseEvent me) {}
 	public void mouseExited(MouseEvent me) {}
 	public void mouseReleased(MouseEvent me) {}
 
 	public void windowActivated(WindowEvent arg0) {}
 	public void windowClosed(WindowEvent e){}
 	public void windowDeactivated(WindowEvent arg0) {}
 	public void windowDeiconified(WindowEvent arg0) {}
 	public void windowIconified(WindowEvent arg0) {}
 	public void windowOpened(WindowEvent arg0) {}
 
 	public void keyPressed(KeyEvent ke) {}
 	public void keyTyped(KeyEvent ke) {}
 
 	public void componentHidden(ComponentEvent arg0) {}
 	public void componentMoved(ComponentEvent arg0) {}
 	public void componentShown(ComponentEvent arg0) {}
 
 
 }
 
 
 class PopupListener extends MouseAdapter {
 	public int mouseX = 0;
 	public int mouseY = 0;
 	private JPopupMenu menu;
 
 	public PopupListener(JPopupMenu m) {
 		menu = m;
 	}
 
 	public void mousePressed(MouseEvent e) {
 		showPopup(e);
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		showPopup(e);
 	}
 
 	private void showPopup(MouseEvent e) {
 		if (e.isPopupTrigger()) {
 			menu.show(e.getComponent(), e.getX(), e.getY());
 		}
 	}
 
 }
 
 class MyCellRenderer extends DefaultListCellRenderer {
 
 	private static final long serialVersionUID = 1L;
 	private final Color LIGHT = new Color(47, 47, 47);
 	private final Color DARK = new Color(57, 57, 57);
 	private final Color BLUE = new Color(175, 220, 255);
 
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 
 		if(isSelected)
 			c.setBackground(BLUE);
 
 		else if (index%2 == 0)
 			c.setBackground(LIGHT);
 
 		else 
 			c.setBackground(DARK);
 
 		return c;
 	}
 }
 
