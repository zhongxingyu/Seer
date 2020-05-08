 package de.zigapeda.flowspring.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.TableColumnModelEvent;
 import javax.swing.event.TableColumnModelListener;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.tree.TreePath;
 
 import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
 
 import de.zigapeda.flowspring.Main;
 import de.zigapeda.flowspring.controller.MediaLibraryListener;
 import de.zigapeda.flowspring.controller.Settings;
 import de.zigapeda.flowspring.data.Column;
 import de.zigapeda.flowspring.data.DataNode;
 import de.zigapeda.flowspring.data.Title;
 import de.zigapeda.flowspring.gui.treetable.AbstractTreeTableModel;
 import de.zigapeda.flowspring.gui.treetable.DataModel;
 import de.zigapeda.flowspring.gui.treetable.TreeTable;
 import de.zigapeda.flowspring.gui.treetable.TreeTableCellRenderer;
 import de.zigapeda.flowspring.interfaces.TreeRow;
 import de.zigapeda.flowspring.player.PlayerController;
 
 public class MainWindow extends JFrame implements ActionListener, TableColumnModelListener, KeyListener, DocumentListener, WindowListener, MouseListener {
 	private static final long serialVersionUID = 1L;
 	private AbstractTreeTableModel	medialibrarymodel;
 	private TreeTable medialibrary;
 	private Playlist playlist;
 	private Controllbar controllbar;
 	private Searchbar searchbar;
 	private Progressbar progressbar;
 	private Volumebar volumebar;
 //	private Balancebar balancebar;
 	private PlayerController playercontroller;
 	private JButton menubutton;
 	private JButton playbutton;
 	private JButton previousbutton;
 	private JButton nextbutton;
 	private JButton stopbutton;
 	private DefaultTableColumnModel medialibrarycolumns;
 	private JPopupMenu columnmenu;
 	private JPopupMenu menu;
 	private MedialibraryRenderer medialibraryrenderer;
 	private JSplitPane splitpane;
 	private JPopupMenu medialibrarymenu;
 	private JPanel controlllayout;
 	private boolean sortdirection;
 	private int sortindex;
 	private int sortcolumn;
 	private JButton layoutbutton;
 	private Container oldpane;
 	private JPanel left;
 
 	public MainWindow() {
 		super("flowspring");
         BufferedImage image;
         try {
             image = ImageIO.read(Main.class.getClass().getResource("/de/zigapeda/flowspring/res/icon.png"));
             this.setIconImage(image);
         } catch (IOException e) {
             e.printStackTrace();
         }
 		this.addWindowListener(this);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setLayout(new BorderLayout());
         left = new JPanel();
         left.setLayout(new BorderLayout());
         this.controlllayout = this.setupControlllayout();
         left.add(this.controlllayout, BorderLayout.PAGE_START);
 
 		EmbeddedMediaPlayerComponent empc = new EmbeddedMediaPlayerComponent();
 		empc.setBounds(0, 0, 0, 0);
 		this.add(empc);
         this.playercontroller = new PlayerController(this.progressbar, this.playlist, empc);
         playercontroller.setGain(this.volumebar.getValue());
         left.add(new JScrollPane(this.medialibrary));
 		this.playlist.setMinimumSize(new Dimension(179,560));
         splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, this.playlist);
         splitpane.setResizeWeight(0.9);
         splitpane.setDividerSize(3);
         this.add(splitpane);
         String windowbounds = Settings.loadSettings("window.bounds");
         int dividerlocation = 601;
         if(windowbounds != null) {
         	String[] wba = windowbounds.split(",");
         	this.setBounds(Integer.valueOf(wba[2]), Integer.valueOf(wba[3]), Integer.valueOf(wba[0]), Integer.valueOf(wba[1]));
         	if(Integer.valueOf(wba[4]) != JFrame.ICONIFIED) {
         		this.setExtendedState(Integer.valueOf(wba[4]));
         	}
         	dividerlocation = Integer.valueOf(wba[5]);
         } else {
     		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
     		this.setBounds(screensize.width/2 - 400, screensize.height/2 - 300, 800, 600);
         }
         this.setMinimumSize(new Dimension(800, 600));
         splitpane.setDividerLocation(dividerlocation);
         columnmenu = new JPopupMenu();
         this.menu = new JPopupMenu();
         JMenuItem item = new JMenuItem();
         item.setText("Read Mediafiles");
         item.setActionCommand("read");
         item.addActionListener(this);
         this.menu.add(item);
 	}
 	
 	private JPanel setupControlllayout() {
         JPanel controlls = new JPanel();
 		this.playlist = new Playlist();
 		this.setupMedialibrary();
         controlls.setLayout(null);
         Insets noinset = new Insets(-10, -10, -10, -10);
         this.controllbar = new Controllbar();
         this.searchbar = new Searchbar();
         this.progressbar = new Progressbar();
         this.volumebar = new Volumebar();
 //        this.balancebar = new Balancebar();
         menubutton = new JButton("▾");
         playbutton = new JButton("►");
         previousbutton = new JButton("◄◄");
         nextbutton = new JButton("►►");
         stopbutton = new JButton("■");
         layoutbutton = new JButton("M");
         Font small = new Font(playbutton.getFont().getName(), playbutton.getFont().getStyle(), playbutton.getFont().getSize() - 2);
         Font big = new Font(playbutton.getFont().getName(), playbutton.getFont().getStyle(), playbutton.getFont().getSize() + 11);
         menubutton.setMargin(noinset);
         playbutton.setMargin(noinset);
         playbutton.setFont(big);
         previousbutton.setMargin(noinset);
         previousbutton.setFont(small);
         nextbutton.setMargin(noinset);
         nextbutton.setFont(small);
         stopbutton.setMargin(noinset);
         stopbutton.setFont(small);
         layoutbutton.setMargin(noinset);
         this.searchbar.addKeyListener(this);
         this.searchbar.getDocument().addDocumentListener(this);
         playbutton.addActionListener(this);
         previousbutton.addActionListener(this);
         nextbutton.addActionListener(this);
         stopbutton.addActionListener(this);
         menubutton.addActionListener(this);
         layoutbutton.addActionListener(this);
         controllbar.setBounds(30, 90, 380, 30);
         this.searchbar.setBounds(415,95,185,25);
         this.progressbar.setBounds(100, 56, 500, 28); //this.progressbar.setBounds(100, 70, 500, 14);
         this.volumebar.setBounds(100, 29, 100, 28);
 //        this.balancebar.setBounds(100,29,100,28);
         menubutton.setBounds(5, 95, 25, 25);
         playbutton.setBounds(5, 5, 90, 60);
         previousbutton.setBounds(5,65,30,24);
         nextbutton.setBounds(65,65,30,24);
         stopbutton.setBounds(35,65,30,24);
         layoutbutton.setBounds(580,0,20,20);
         controlls.setPreferredSize(new Dimension(600, 120));
         controlls.setMinimumSize(new Dimension(600, 120));
         controlls.add(controllbar);
         controlls.add(this.progressbar);
         controlls.add(this.volumebar);
 //        controlls.add(this.balancebar);
         controlls.add(this.searchbar);
         controlls.add(menubutton);
         controlls.add(playbutton);
         controlls.add(previousbutton);
         controlls.add(nextbutton);
         controlls.add(stopbutton);
         controlls.add(layoutbutton);
         return controlls;
 	}
 
 	private void setupMedialibrary() {
         this.medialibrarymodel = new DataModel(DataNode.getLibrary());
         this.medialibrarycolumns = new DefaultTableColumnModel();
         this.medialibraryrenderer  = new MedialibraryRenderer();
         this.medialibrary = new TreeTable(this.medialibrarymodel);
         MediaLibraryListener medialibrarylistener = new MediaLibraryListener(this.medialibrary);
         this.medialibrary.setListener(medialibrarylistener);
         this.medialibrary.setColumnModel(this.medialibrarycolumns);
         this.medialibrarycolumns.addColumnModelListener(this);
         this.medialibrary.addMouseListener(medialibrarylistener);
         this.medialibrary.addKeyListener(medialibrarylistener);
         this.medialibrary.getTableHeader().addMouseListener(this);
         this.medialibrary.setShowGrid(true);
 	}
 	
 	public void setPlaybuttonpause(boolean pause) {
 		if(pause == true) {
 			this.playbutton.setText("ǁ");
 		} else {
 			this.playbutton.setText("►");
 		}
 	}
 
 	public Controllbar getControlllayout() {
 		return this.controllbar;
 	}
 	
 	public void setSearch(String search) {
 		this.searchbar.setText(search);
 		this.searchbar.requestFocus();
 	}
 	
 	public void refreshMedialib() {
 		LinkedList<TreePath> list = new LinkedList<>();
 		if(this.medialibrarymodel.getRoot() == DataNode.getLibrary()) {
 			for(int i = 0; i < this.medialibrary.getRowCount(); i++) {
 				if(((TreeTableCellRenderer)this.medialibrary.getCellRenderer(i, 0)).isExpanded(i)) {
 					list.add(this.medialibrary.getPathAt(i));
 				}
 			}
 		}
 		this.searchbar.setText("");
 		this.medialibrarymodel = new DataModel(DataNode.getLibrary());
 		this.medialibrary.setModel(this.medialibrarymodel);
 		this.setColumns();
 		for(TreePath path: list) {
 			this.medialibrary.getTree().expandPath(path);
 		}
 	}
 	
 	public void refreshMedialibAfterRegroup() {
 		this.searchbar.setText("");
 		this.medialibrarymodel = new DataModel(DataNode.getLibrary());
 		this.medialibrary.setModel(this.medialibrarymodel);
 		this.setColumns();
 	}
 	
 	public void refreshSearch() {
 		this.medialibrarymodel = new DataModel(DataNode.getSearch());
 		this.medialibrary.setModel(this.medialibrarymodel);
 		this.setColumns();
 	}
 	
 	private void setColumns() {
 		do {
 			if(Column.getMedialibrarycolumns() == null) {
 				LinkedList<Column> list = new LinkedList<>();
 				System.out.println(this.medialibrarycolumns.getColumnCount());
 				for(int i = 0; i < this.medialibrarycolumns.getColumnCount(); i++) {
 					list.add(new Column(this.medialibrarycolumns.getColumn(i), this.medialibrarycolumns.getColumn(i).getWidth(), true));
 				}
 				System.out.println(list.size());
 				String medialibcolumns = Settings.loadSettings("medialib.columns");
 				if(medialibcolumns != null) {
 //					medialibcolumns = "";
 					if(medialibcolumns.contains("Name")) {
 						System.out.println("load settings succeed");
 						LinkedList<Column> newlist = new LinkedList<>();
 						String[] mca = medialibcolumns.split(",");
 						for(String s: mca) {
 							TableColumn tc = null;
 							for(Column c: list) {
 								if(DataModel.columnNames[c.getColumn().getModelIndex()].equals(s.substring(5))) {
 									tc = c.getColumn();
 									break;
 								}
 							}
 							if(s.substring(0,1).equals("t")) {
 								newlist.add(new Column(tc, Integer.valueOf(s.substring(1,5)), true));
 							} else {
 								newlist.add(new Column(tc, Integer.valueOf(s.substring(1,5)), false));
 							}
 						}
 						System.out.println(newlist.size());
 						Column.setMedialibrarycolumns(newlist);
 					} else {
 						Column.setMedialibrarycolumns(list);
 					}
 				} else {
 					Column.setMedialibrarycolumns(list);
 				}
 				System.out.println(Column.getMedialibrarycolumns().size());
 			}
 			while (this.medialibrarycolumns.getColumnCount() > 0) {
 				if(Column.getMedialibrarycolumns().size() < 10) {
 					System.out.println("fehler");
 					Column.setMedialibrarycolumns(null);
 				}
 				this.medialibrarycolumns.removeColumn(this.medialibrarycolumns.getColumn(0));
 			}
 			if(Column.getMedialibrarycolumns() == null) {
 				for(int i = 0; i < 10; i++) {
 					this.medialibrarycolumns.addColumn(new TableColumn(i));
 				}
 			}
 			System.out.println("durchlauf");
 		} while (Column.getMedialibrarycolumns() == null);
 		
 		System.out.println("---");
 		System.out.println(Column.getMedialibrarycolumns().size());
 		for(Column c: Column.getMedialibrarycolumns()) {
 			if(c.isVisible()) {
 				c.getColumn().setPreferredWidth(c.getWidth());
 				this.medialibrarycolumns.addColumn(c.getColumn());
 				if(c.getColumn().getModelIndex() != 0) {
 					c.getColumn().setCellRenderer(this.medialibraryrenderer);
 				}
 			}
 		}
 	}
 
 	public TreeTable getMediaLibrary() {
 		return this.medialibrary;
 	}
 	
 	public Playlist getPlaylist() {
 		return this.playlist;
 	}
 
 	public PlayerController getPlayercontroller() {
 		return this.playercontroller;
 	}
 	
 	public void showMedialibraryMenu(TreeRow row, int x, int y) {
 		this.medialibrarymenu = new JPopupMenu();
 		this.medialibrarymenu.add(new JMenuItem(row.getType().toString()));
 		this.medialibrarymenu.show(this.medialibrary, x, y);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == this.playbutton) {
 			this.playercontroller.play();
 		} else if(e.getSource() == this.stopbutton) {
 			this.playercontroller.stop();
 		} else if(e.getSource() == this.nextbutton) {
 			this.playercontroller.next();
 		} else if(e.getSource() == this.previousbutton) {
 			this.playercontroller.previous();
 		} else if(e.getSource() == this.menubutton) {
 			this.menu.show(this.menubutton, 10, 10);
 		} else if(e.getSource() == this.layoutbutton) {
 			changeLayout();
 		} else if(e.getSource() instanceof JCheckBoxMenuItem) {
 			if(this.columnmenu.getComponentIndex((Component)e.getSource()) != -1) {
 				Column c = Column.getColumnByName(Column.getMedialibrarycolumns(),((JCheckBoxMenuItem)e.getSource()).getText());
 				if(c != null) {
 					c.setVisible(!c.isVisible());
 					this.setColumns();
 				}
 			}
 		} else if(e.getSource() instanceof JMenuItem) {
 			switch(e.getActionCommand()) {
 				case "read":
 					if(Main.getReadWindow() != null) {
 						Main.getReadWindow().toFront();
 					} else {
 						Main.setReadWindow(new ReadWindow());
 					}
 					break;
 			}
 		}
 	}
 
 	private void changeLayout() {
 		if(this.getMinimumSize().getHeight() == 600) {
 			//big layout to small layout
 			this.setMinimumSize(new Dimension(605, 120));
 			this.setResizable(false);
 			this.setSize(600, 120);
 			this.oldpane = this.getContentPane();
 			this.setContentPane(this.controlllayout);
 		} else {
 			//small layout to big layout
 			this.setResizable(true);
 			this.setMinimumSize(new Dimension(800, 600));
 			this.setContentPane(this.oldpane);
			this.left.add(this.controlllayout,BorderLayout.PAGE_START);
 		}
 	}
 
 	public void columnAdded(TableColumnModelEvent e) {
 		
 	}
 
 	public void columnRemoved(TableColumnModelEvent e) {
 		
 	}
 
 	public void columnMoved(TableColumnModelEvent e) {
 		if(this.medialibrarycolumns.getColumn(0).getModelIndex() == 0) {
 			LinkedList<Column> list = new LinkedList<>();
 			for(int i = 0; i < this.medialibrarycolumns.getColumnCount(); i++) {
 				list.add(new Column(this.medialibrarycolumns.getColumn(i), this.medialibrarycolumns.getColumn(i).getWidth(), true));
 			}
 			list.addAll(Column.getHiddenColumns(Column.getMedialibrarycolumns()));
 			Column.setMedialibrarycolumns(list);
 		} else {
 			this.setColumns();
 		}
 	}
 
 	public void columnMarginChanged(ChangeEvent e) {
 		LinkedList<Column> list = new LinkedList<>();
 		for(int i = 0; i < this.medialibrarycolumns.getColumnCount(); i++) {
 			list.add(new Column(this.medialibrarycolumns.getColumn(i), this.medialibrarycolumns.getColumn(i).getWidth(), true));
 		}
 		list.addAll(Column.getHiddenColumns(Column.getMedialibrarycolumns()));
 		Column.setMedialibrarycolumns(list);
 	}
 
 	public void columnSelectionChanged(ListSelectionEvent e) {
 		
 	}
 
 	public void keyTyped(KeyEvent e) {
 		
 	}
 
 	public void keyPressed(KeyEvent e) {
 		if(e.getSource() == this.searchbar) {
 			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
 				searchbarchange(false);
 				this.medialibrary.getSelectionModel().setSelectionInterval(0, 0);
 				this.medialibrary.requestFocus();
 			}
 		}
 	}
 
 	public void keyReleased(KeyEvent e) {
 		
 	}
 
 	public void insertUpdate(DocumentEvent e) {
 		this.searchbarchange(true);
 	}
 
 	public void removeUpdate(DocumentEvent e) {
 		this.searchbarchange(true);
 	}
 
 	public void changedUpdate(DocumentEvent e) {
 		this.searchbarchange(true);
 	}
 	
 	private void searchbarchange(boolean length) {
 		if(this.searchbar.getText().length() >= 3 || length == false) {
 			DataNode.getSearch().setChildren(Title.getTitlesBySearchstring(this.searchbar.getText()));
 			this.refreshSearch();
 		} else if(this.searchbar.getText().length() == 0) {
 			this.refreshMedialib();
 		}
 	}
 	
 	private String getPositionString() {
 		String temp = new String();
 		int ext = this.getExtendedState();
 		this.setExtendedState(0);
 		temp = temp + String.valueOf(this.getWidth()) + ",";
 		temp = temp + String.valueOf(this.getHeight()) + ",";
 		temp = temp + String.valueOf(this.getX()) + ",";
 		temp = temp + String.valueOf(this.getY()) + ",";
 		temp = temp + String.valueOf(ext) + ",";
 		temp = temp + String.valueOf(this.splitpane.getDividerLocation());
 		return temp;
 	}
 
 	public void windowOpened(WindowEvent e) {
 		
 	}
 
 	public void windowClosing(WindowEvent evt) {
 		Settings.saveSettings("window.bounds", this.getPositionString());
 		Settings.saveSettings("medialib.columns", Column.getColumnlistString(Column.getMedialibrarycolumns()));
 		Settings.saveSettings("volume", String.valueOf(this.volumebar.getValue()));
 //		Settings.saveSettings("balance", String.valueOf(this.balancebar.getValue()));
         Connection c = Main.getDatabase();
         try {
 			Statement s = c.createStatement();
 			s.executeUpdate("shutdown");
 			s.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void windowClosed(WindowEvent e) {
 		
 	}
 
 	public void windowIconified(WindowEvent e) {
 		
 	}
 
 	public void windowDeiconified(WindowEvent e) {
 		
 	}
 
 	public void windowActivated(WindowEvent e) {
 		if(Main.getOntopwindow() != null) {
 			Main.getOntopwindow().requestFocus();
 		}
 	}
 
 	public void windowDeactivated(WindowEvent e) {
 		
 	}
 	
 	public int getSortIndex() {
 		return this.sortindex;
 	}
 	
 	public boolean getSortDirection() {
 		return this.sortdirection;
 	}
 	
 	public void mouseClicked(MouseEvent e) {
 		if(e.getButton() == MouseEvent.BUTTON1) {
 			TableColumnModel cm = ((JTableHeader)e.getSource()).getColumnModel();
 			int colindex = cm.getColumnIndexAtX(e.getX());
 			int modindex = cm.getColumn(colindex).getModelIndex();
 			this.medialibrarycolumns.getColumn(this.sortcolumn).setHeaderValue(DataModel.columnNames[this.sortindex]);
 			if(modindex == this.sortindex) {
 				this.sortdirection = !this.sortdirection;
 			} else {
 				this.sortcolumn = colindex;
 				this.sortindex = modindex;
 				this.sortdirection = false;
 			}
 			if(this.sortdirection) {
 				this.medialibrarycolumns.getColumn(colindex).setHeaderValue(DataModel.columnNames[modindex] + "   ▼");
 			} else {
 				this.medialibrarycolumns.getColumn(colindex).setHeaderValue(DataModel.columnNames[modindex] + "   ▲");
 			}
 			this.refreshMedialib();
 		} else if(e.getButton() == MouseEvent.BUTTON3) {
 			this.columnmenu.removeAll();
 			LinkedList<Column> list = Column.getMedialibrarycolumns();
 			if(list != null) {
 				for(Column c: list) {
 					if(c.getColumn().getModelIndex() != 0) {
 						this.columnmenu.add(new JCheckBoxMenuItem(DataModel.columnNames[c.getColumn().getModelIndex()], c.isVisible())).addActionListener(this);
 					}
 				}
 			}
 			this.columnmenu.show(this.medialibrary.getTableHeader(), e.getX(), e.getY());
 		}
 	}
 
 	public void mousePressed(MouseEvent e) {
 		
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 
 	public void mouseExited(MouseEvent e) {
 	
 	}
 }
