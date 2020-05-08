 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package View;
 
 import Controller.Controller;
 import Model.Database;
 import Model.FindFiles;
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
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author Quentin
  */
 public class WindowMedia extends JFrame implements Observer, ActionListener, ItemListener, MouseListener {
 	//Elements menu
 
 	private JMenuBar mb_menuBar;
 	private JMenu m_file;
 	private JMenuItem mi_chooseFolder;
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
 	private boolean passer_par_listener;
 	private JMenuItem itm_detail;
 	private JPopupMenu popup;
 
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
 		m_file = new JMenu("Fichier");
 		mi_chooseFolder = new JMenuItem("Choisir dossier");
 
 		mb_menuBar.add(m_file);
 		m_file.add(mi_chooseFolder);
 		mi_chooseFolder.addActionListener(this);
 		this.setJMenuBar(mb_menuBar);
 
 
 		popup = new JPopupMenu();
 		itm_detail = new JMenuItem("Détail");
 		itm_detail.addActionListener(this);
 
 		//Gestion des Jpanel
 		total.setLayout(new BorderLayout());
 		JPanel left = new JPanel();
 		JPanel right = new JPanel();
 		JPanel down = new JPanel();
 		middle = new JPanel();
 		middle.setLayout(new BorderLayout());
 		JPanel up = new JPanel();
 		left.setLayout(new BorderLayout());
 		total.add(left, BorderLayout.WEST);
 		total.add(right, BorderLayout.EAST);
 		total.add(down, BorderLayout.SOUTH);
 		total.add(middle, BorderLayout.CENTER);
 		total.add(up, BorderLayout.NORTH);
 
 		//Panel right
 		cbb_playList = new JComboBox<String>();
 		lb_list = new JList<String>();
 		this.generateCbbPlaylist();
 		cbb_playList.addItemListener(this);
 
 		right.setLayout(new BorderLayout());
 		cbb_playList.setPreferredSize(new Dimension(200, 30));
 		cbb_playList.setEditable(true);
 		right.add(cbb_playList, BorderLayout.NORTH);
 		right.add(lb_list, BorderLayout.CENTER);
 
 		//Panel down
 		btn_play = new JButton("Play");
 		btn_stop = new JButton("Stop");
 		btn_next = new JButton("Next");
 		btn_previous = new JButton("Previous");
 		btn_repeat = new JButton("Repeat");
 		btn_random = new JButton("Random");
 
 		btn_play.addActionListener(this);
 		btn_stop.addActionListener(this);
 		btn_next.addActionListener(this);
 		btn_previous.addActionListener(this);
 		btn_random.addActionListener(this);
 		btn_repeat.addActionListener(this);
 
 		down.add(btn_repeat);
 		down.add(btn_random);
 		down.add(btn_previous);
 		down.add(btn_stop);
 		down.add(btn_play);
 		down.add(btn_next);
 
 		generateTable();
 
 		//Panel left
 		this.createTree();
 		tree.setPreferredSize(new Dimension(200, 0));
 		left.add(new JScrollPane(tree), BorderLayout.CENTER);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == btn_play) {
 			controller.play();
 		} else if (e.getSource() == btn_next) {
 			controller.next();
 		} else if (e.getSource() == btn_previous) {
 			controller.previous();
 		} else if (e.getSource() == btn_random) {
 			controller.random();
 		} else if (e.getSource() == btn_repeat) {
 			controller.repeat();
 		} else if (e.getSource() == btn_stop) {
 			controller.stop();
 		} else if (e.getSource() == mi_chooseFolder) {
 			JFileChooser fc = new JFileChooser();
 			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 			fc.showOpenDialog(this);
 			controller.getAllFiles(fc.getSelectedFile().getAbsolutePath(),this);
 		} else if (e.getSource() == itm_detail){
 			System.out.println("Créer une fenêtre :D");
 		}
 	}
 
 	/**
 	 * Crée les branches de l'arbre à gauche
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
 	 * @param args 
 	 */
 	public static void main(String args[]) {
 		try {
 			Database.createDatabase("BddSonVideo.sql");
 		} catch (MonException ex) {
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
 			}
 		}
 	}
 
 	public void updatePlayList() {
 		lb_list.removeAll();
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
				String parent = selectedNode.getParent().toString();
 				controller.fileChanged(selectedNode.toString(), parent);
 			}
 		}
 		else if (me.getSource() == tableau) {
 			Point p = me.getPoint();
 			if (tableau.rowAtPoint(p) != -1) {
 				if (me.getClickCount() == 2) {
 					controller.setCurrentPlayList("");
 					controller.getSelectionPlaylist().add(controller.createMediaByName(tableau.getValueAt(tableau.getSelectedRow(), 0).toString()));
 					this.updatePlayList();
 				} else if (me.getButton() == MouseEvent.BUTTON3) {
 					popup.add(itm_detail);
 					popup.show(me.getComponent(), me.getX(), me.getY());
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
 		tableau = new JTable(donnees, entetes){
 			public boolean isCellEditable(int i, int j){
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
 }
