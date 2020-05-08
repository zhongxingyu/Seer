 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package View;
 
 import Controller.Controller;
 import Model.Database;
 import Model.Media;
 import Model.MonException;
 import Model.PlayList;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author Quentin
  */
 public class WindowMedia extends JFrame implements Observer, ActionListener, ItemListener, MouseListener, KeyListener, ListSelectionListener {
 	//Elements menu
 
 	private JMenuBar mb_menuBar;
 	private JMenu m_file;
 	private JMenu m_edit;
 	private JMenuItem mi_chooseFolder;
 	private JMenuItem mi_addSort;
 	private JMenuItem mi_addPerson;
 	private JMenuItem mi_addAlbum;
 	private JButton btn_play;
 	private JButton btn_stop;
 	private JButton btn_next;
 	private JButton btn_previous;
 	private JButton btn_repeat;
 	private JButton btn_random;
 	private JComboBox<String> cbb_playList;
 	private JList<String> lb_list;
 	private DefaultTreeModel treeModel;
 	private DefaultMutableTreeNode root;
 	private DefaultMutableTreeNode rootMovie;
 	private DefaultMutableTreeNode rootSong;
 	private JTree tree;
 	private Controller controller;
 	private ArrayList<String> actorslist;
 	private ArrayList<String> directorslist;
 	private ArrayList<String> sortslist;
 	private ArrayList<String> artistslist;
 	private ArrayList<String> styleslist;
 	private ArrayList<String> albumslist;
 	private JTable tableau;
 	private JPanel middle;
 	private JPanel detail;
 	private boolean passer_par_listener;
 	private JButton btn_artist;
 	private JButton btn_sort;
 	private JPanel left;
 
 	public WindowMedia() {
 		controller = new Controller();
 		controller.addObserver(this);
 		
 		JPanel total = new JPanel();
 		this.add(total);
 		this.setTitle("Mound Manager");
 		this.setSize(1000, 600);
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//Gestion menubar
 		mb_menuBar = new JMenuBar();
 		m_file = new JMenu("File");
 		m_edit = new JMenu("Edition");
 		mi_chooseFolder = new JMenuItem("Import folder");
 		mi_addSort = new JMenuItem("Add a sort");
 		mi_addPerson = new JMenuItem("Add a person");
 		mi_addAlbum = new JMenuItem("Add an album");
 
 		mb_menuBar.add(m_file);
 		mb_menuBar.add(m_edit);
 		m_file.add(mi_chooseFolder);
 		m_edit.add(mi_addSort);
 		m_edit.add(mi_addPerson);
 		m_edit.add(mi_addAlbum);
 		mi_chooseFolder.addActionListener(this);
 		mi_addSort.addActionListener(this);
 		mi_addPerson.addActionListener(this);
 		mi_addAlbum.addActionListener(this);
 		this.setJMenuBar(mb_menuBar);
 		
 		//Panel right
 		cbb_playList = new JComboBox<String>();
 		lb_list = new JList<String>();
 		lb_list.addListSelectionListener(this);
 		this.generateCbbPlaylist();
 		cbb_playList.addItemListener(this);
 		
 		//Gestion des Jpanel
 		total.setLayout(new BorderLayout());
 		left = new JPanel();
 		JPanel right = new JPanel();
 		middle = new JPanel();
 		detail = new JPanel();
 		middle.setLayout(new BorderLayout());
 		JPanel up = new JPanel();
 		ButtonBar down = new ButtonBar(this.controller);
 		left.setLayout(new BorderLayout());
 		total.add(left, BorderLayout.WEST);
 		total.add(right, BorderLayout.EAST);
 		total.add(down, BorderLayout.SOUTH);
 		total.add(middle, BorderLayout.CENTER);
 		total.add(up, BorderLayout.NORTH);
 		
 		
 
 		right.setLayout(new BorderLayout());
 		cbb_playList.setPreferredSize(new Dimension(200, 30));
 		cbb_playList.setEditable(true);
 		right.add(cbb_playList, BorderLayout.NORTH);
 		right.add(lb_list, BorderLayout.CENTER);
 
 		generateTable();
 
 		//Panel left
 		this.createTree();
 		tree.setPreferredSize(new Dimension(200, 0));
 		left.add(new JScrollPane(tree), BorderLayout.CENTER);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == mi_chooseFolder) {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			fc.showOpenDialog(this);
 			controller.getAllFiles(fc.getSelectedFile().getAbsolutePath(), this);
 		}
 		else if (e.getSource() == btn_artist){
 			
 		}
 		else if (e.getSource() == btn_sort){
 			AddSort as = new AddSort(controller, this,1);
 		}
 		else if(e.getSource() == mi_addSort){
 			controller.addSort(this);
 		}
 		else if(e.getSource() == mi_addAlbum){
 			controller.addAlbum(this);
 		}
 		else if(e.getSource() == mi_addPerson){
 			controller.addSomeone(this);
 		}
 	}
 
 	/**
 	 * Crée les branches de l'arbre à gauche
 	 *
 	 * @param parent
 	 * @param children
 	 */
 	private void createBranches(DefaultMutableTreeNode parent, ArrayList<String> children) {
 		for (int i = 0; i < children.size(); i++) {
 			DefaultMutableTreeNode node = new DefaultMutableTreeNode(children.get(i));
 			parent.add(node);
 		}
 	}
 
 	/**
 	 * Crée l'arbre à gauche
 	 */
 	public void createTree() {
 		//Noeaud Files
 		root = new DefaultMutableTreeNode("List of files");
 
 		//Movie
 		rootMovie = new DefaultMutableTreeNode("Movies");
 		DefaultMutableTreeNode actors = new DefaultMutableTreeNode("Actor");
 		DefaultMutableTreeNode directors = new DefaultMutableTreeNode("Director");
 		DefaultMutableTreeNode sorts = new DefaultMutableTreeNode("Sort");
 		actorslist = controller.actorsList();
 		directorslist = controller.directorsList();
 		sortslist = controller.sortsList();
 		this.createBranches(actors, actorslist);
 		this.createBranches(directors, directorslist);
 		this.createBranches(sorts, sortslist);
 		rootMovie.add(actors);
 		rootMovie.add(directors);
 		rootMovie.add(sorts);
 
 		//Song
 		rootSong = new DefaultMutableTreeNode("Song");
 		DefaultMutableTreeNode artists = new DefaultMutableTreeNode("Artist");
 		DefaultMutableTreeNode styles = new DefaultMutableTreeNode("Style");
 		DefaultMutableTreeNode album = new DefaultMutableTreeNode("Album");
 		artistslist = controller.artistsList();
 		styleslist = controller.stylesList();
 		albumslist = controller.albumsList();
 		this.createBranches(artists, artistslist);
 		this.createBranches(styles, styleslist);
 		this.createBranches(album, albumslist);
 		rootSong.add(artists);
 		rootSong.add(styles);
 		rootSong.add(album);
 		root.add(rootMovie);
 		root.add(rootSong);
 		treeModel = new DefaultTreeModel(root);
 		tree = new JTree(treeModel);
 		tree.addMouseListener(this);
 		DefaultMutableTreeNode currentNode = root.getNextNode();
 		do {
 			if (currentNode.getLevel() == 1) {
 				tree.expandPath(new TreePath(currentNode.getPath()));
 			}
 			currentNode = currentNode.getNextNode();
 		} while (currentNode != null);
 	}
 
 	/**
 	 * Main pour lancer l'appli
 	 *
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			Database.createDatabase("BddSonVideo.sql");
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(null, ex.getMessage());
 		}
 		WindowMedia windowMedia = new WindowMedia();
 		windowMedia.setVisible(true);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		if (arg instanceof MonException) {
 			MonException ex = (MonException) arg;
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		} else if (arg instanceof String) {
 			String s = (String) arg;
 			if (s.equals("Playlist")) {
 				this.updatePlayList();
 			} else if (s.equals("Middle")) {
 				this.generateTable();
 			} else if (s.equals("maj")){
 				this.updateTree();
 			}
 		}
 	}
 
 	public void updateTree(){
 		this.createTree();
 		left.removeAll();
 		tree.setPreferredSize(new Dimension(200, 0));
 		this.left.add(new JScrollPane(tree), BorderLayout.CENTER);
 		this.revalidate();
 		this.repaint();
 	}
 	
 	public void updatePlayList() {
 		lb_list.removeAll();
 		lb_list.addKeyListener(this);
 		DefaultListModel<String> data = new DefaultListModel<String>();
 		ArrayList<Media> medias = controller.getSelectionPlaylist();
 		for (int i = 0; i < medias.size(); i++) {
 			data.addElement(medias.get(i).getTitle());
 		}
 		lb_list.setModel(data);
 		this.revalidate();
 		this.repaint();
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		if (passer_par_listener && e.getSource() == cbb_playList && e.getStateChange() == ItemEvent.SELECTED) {
 			String playlistName = cbb_playList.getSelectedItem().toString();
 			if (controller.playlistExist(playlistName)) {
 				controller.updatePlayList(cbb_playList.getSelectedItem().toString());
 			} else {
 				controller.savePlaylist(playlistName);
 				this.generateCbbPlaylist();
 			}
 		}
 	}
 
 	/**
 	 * Crée la combobox Playlist
 	 */
 	public void generateCbbPlaylist() {
 		passer_par_listener = false;
 		cbb_playList.removeAllItems();
 		HashMap<String, PlayList> playlists = controller.getAllPlaylist();
 		ArrayList<String> arrayPlaylists = new ArrayList<String>();
 		Set cles = playlists.keySet();
 		Iterator it = cles.iterator();
 		while (it.hasNext()) {
 			String cle = (String) it.next();
 			arrayPlaylists.add(cle);
 		}
 		Collections.sort(arrayPlaylists);
 		for (int i = 0; i < arrayPlaylists.size(); i++) {
 			cbb_playList.addItem(arrayPlaylists.get(i));
 		}
 		cbb_playList.setSelectedItem(controller.getCurrentPlayListName());
 		passer_par_listener = true;
 		this.revalidate();
 		this.repaint();
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
 
 	@Override
 	public void mouseClicked(MouseEvent me) {
 		if (me.getSource() == tree) {
 			TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
 			if (tp != null) { // test si on clique sur un élément
 				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
 				String parent = "";
 				if (!selectedNode.toString().equals("List of files")) {
 					parent = selectedNode.getParent().toString();
 				}
 				controller.fileChanged(selectedNode.toString(), parent);
 			}
 		} else if (me.getSource() == tableau) {
 			Point p = me.getPoint();
 			if (tableau.rowAtPoint(p) != -1) {
 				if (me.getClickCount() == 2) {
 					controller.setCurrentPlayList("");
 					controller.getSelectionPlaylist().add(controller.createMediaByName(tableau.getValueAt(tableau.getSelectedRow(), tableau.getColumnModel().getColumnIndex(new String("Title"))).toString()));
 					this.updatePlayList();
 				}
 				else{
 					this.controller.setSelectedMiddleFile(tableau.getValueAt(tableau.getSelectedRow(),tableau.getColumnModel().getColumnIndex(new String("Title"))).toString());
 					this.generateDetail(this.controller.getSelectedMiddleFile());
 				}
 			}
 		}
 	}
 
 	public void generateTable() {
 		String[] entetes = {"Title", "Release Date", "Length", "Path"};
 		Object[][] donnees = {};
 		if (controller.getSelection() != null) {
 			donnees = new Object[controller.getSelection().size()][4];
 			for (int i = 0; i < controller.getSelection().size(); i++) {
 				donnees[i][0] = controller.getSelection().get(i).getTitle();
 				donnees[i][1] = controller.getSelection().get(i).getDate();
 				donnees[i][2] = controller.getSelection().get(i).getLength();
 				donnees[i][3] = controller.getSelection().get(i).getPath();
 			}
 		}
 		tableau = new JTable(donnees, entetes) {
 			public boolean isCellEditable(int i, int j) {
 				return false;
 			}
 		};
 		tableau.setAutoCreateRowSorter(true);
 		tableau.addMouseListener(this);
 		middle.removeAll();
 		middle.add(new JScrollPane(tableau), BorderLayout.CENTER);
 		this.revalidate();
 		this.repaint();
 	}
 
 	public void generateDetail(String name) {
 		detail.removeAll();
 		btn_artist = new JButton("+");
 		btn_sort = new JButton("+");
 		btn_artist.addActionListener(this);
 		btn_sort.addActionListener(this);
 		Media m = controller.getMediaByName(name);
 		JPanel pnl_genre = new JPanel();
 		JPanel pnl_artists = new JPanel();
 		JLabel lbl_title = new JLabel(" Title : ");
 		lbl_title.setPreferredSize(new Dimension(100,15));
 		JTextField txt_title = new JTextField(m.getTitle());
 		JLabel lbl_date = new JLabel(" Release date : ");
 		lbl_date.setPreferredSize(new Dimension(100,15));
 		JTextField txt_date = new JTextField(m.getDate());
 		JLabel lbl_sort = new JLabel(" Sort(s) : ");
 		lbl_sort.setPreferredSize(new Dimension(50,15));
 		JList<String> jlgenre = new JList<String>();
 		JLabel lbl_artist = new JLabel(" Artist(s) : ");
 		lbl_artist.setPreferredSize(new Dimension(70,15));
 		JList<String> jlartists = new JList<String>();
 		DefaultListModel<String> genres = new DefaultListModel<String>();
 		for (int i = 0; i < m.getSort().size(); i++) {
 			genres.addElement(m.getSort().get(i));
 		}
 		DefaultListModel<String> artists = new DefaultListModel<String>();
 		for (int i = 0; i < m.getArtists().size(); i++) {
 			artists.addElement(m.getArtists().get(i).getPrenom() + " " + m.getArtists().get(i).getNom());
 		}
 		detail.setLayout(new BoxLayout(detail, BoxLayout.PAGE_AXIS));
 		JPanel first = new JPanel();
 		first.setLayout(new BoxLayout(first, BoxLayout.LINE_AXIS));
 		JPanel second = new JPanel();
 		second.setLayout(new BoxLayout(second, BoxLayout.LINE_AXIS));
 		JPanel third = new JPanel();
 		third.setLayout(new BoxLayout(third, BoxLayout.LINE_AXIS));
 		jlgenre.setModel(genres);
 		jlgenre.setPreferredSize(new Dimension(90, 30));
 		jlartists.setModel(artists);
 		jlartists.setPreferredSize(new Dimension(90, 30));
 		first.add(lbl_title);
 		first.add(txt_title);
 		second.add(lbl_date);
 		second.add(txt_date);
 		pnl_genre.add(lbl_sort);
 		pnl_genre.add(new JScrollPane(jlgenre));
		pnl_genre.add(btn_sort);
 		pnl_artists.add(lbl_artist);
 		pnl_artists.add(new JScrollPane(jlartists));
		pnl_artists.add(btn_artist);
 		detail.add(first);
 		detail.add(second);
 		third.add(pnl_genre);
 		third.add(pnl_artists);
 		detail.add(third);
 		middle.add(detail, BorderLayout.SOUTH);
 		this.revalidate();
 		this.repaint();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (e.getKeyCode() == 127) {
 			int index = lb_list.getSelectedIndex();
 			if (index != -1) {
 				controller.getSelectionPlaylist().remove(index);
 				controller.setCurrentPlayList("");
 				this.generateCbbPlaylist();
 				this.updatePlayList();
 			}
 		}
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		controller.setSelectedMiddleFile(lb_list.getSelectedValue());
 	}
 }
