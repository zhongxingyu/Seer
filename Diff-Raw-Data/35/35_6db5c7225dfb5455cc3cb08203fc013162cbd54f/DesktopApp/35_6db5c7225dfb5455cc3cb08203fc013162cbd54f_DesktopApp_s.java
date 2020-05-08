 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 
 public class DesktopApp extends JFrame implements ActionListener{
 
 	/**
 	 * @param args
 	 */
 	
 	JButton login = new JButton("login");
 	JButton register = new JButton("register");
 	JButton about = new JButton("about");
 	JButton help = new JButton("help");
 	JTextField searchField = new JTextField("Search for playlists, songs, and more!");
 	JLabel user = new JLabel("Username:");
 	JTextField username = new JTextField(25);
 	JLabel pass = new JLabel("Password:");
 	JTextField password = new JTextField(25);
 	JLabel confirm = new JLabel("Confirm Password:");
 	JTextField confirmPassword = new JTextField(25);
 	JButton search = new JButton("Search!");
 	JCheckBox albums = new JCheckBox("albums");
 	JCheckBox artists = new JCheckBox("artists");
 	JCheckBox playlists = new JCheckBox("playlists");
 	JCheckBox songs = new JCheckBox("songs");
 	JCheckBox members = new JCheckBox("members");
 	JButton loginsubmit = new JButton("Login");
 	JButton registersubmit = new JButton("Register");
 	JTable table = new JTable(new MyTableModel());
 	JScrollPane scrollPane = new JScrollPane(table);
 	ArrayList<Song> songlist = new ArrayList<Song>();
 	Song firstSong = new Song(Boolean.FALSE,new Integer(1),"first song","first artist","first album");
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		DesktopApp desktop = new DesktopApp();
 	}
 
 	public DesktopApp()
     {	
 		super("Music App");
 		setSize(1000,1000);
 		setResizable(false);
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(null);
 		
 		// Get the size of the screen
 	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 	    // Determine the new location of the window
 	    int w = getSize().width;
 	    int h = getSize().height;
 	    int x = (dim.width-w)/2;
 	    int y = (dim.height-h)/2;
 	    // Move the window
 	    setLocation(x, y);
 	    
 	    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
 	    table.setFillsViewportHeight(true);
 	    
	    scrollPane.setBounds(350,300,500,500);
	    
	    Font font = new Font("Trebuchet MS", Font.BOLD, 18);
 	    Color color = new Color(0xA6,0x28,0x11);//red
 	    Color color1 = new Color(0x00,0x33,0x99);//blue
 	    
 	    JLabel logo = new JLabel();
 	    logo.setIcon(new ImageIcon(getClass().getResource("flat logo.png")));
 	    logo.setBounds(80,0,270,100);
 	    getContentPane().add(logo);  
 	    
 	    login.setBounds(590,40,90,20);
 	    login.setFont(font);
 	    login.setForeground(color1);
 	    login.setBackground(getContentPane().getBackground());
 	    login.setBorderPainted(false);
 	    login.addActionListener(this);
 	    login.setContentAreaFilled(false);
 	    getContentPane().add(login);
 	    
 	    register.setBounds(660,40,100,20);
 	    register.setFont(font);
 	    register.setForeground(color1);
 	    register.setBackground(getContentPane().getBackground());
 	    register.setBorderPainted(false);
 	    register.addActionListener(this);
 	    register.setContentAreaFilled(false);
 	    getContentPane().add(register);
 	    
 	    about.setBounds(745,40,85,20);
 	    about.setFont(font);
 	    about.setForeground(color1);
 	    about.setBackground(getContentPane().getBackground());
 	    about.setBorderPainted(false);
 	    about.setContentAreaFilled(false);
 	    getContentPane().add(about);
 	    
 	    help.setBounds(810,40,90,20);
 	    help.setFont(font);
 	    help.setForeground(color1);
 	    help.setBackground(getContentPane().getBackground());
 	    help.setBorderPainted(false);
 	    help.setContentAreaFilled(false);
 	    getContentPane().add(help);
 	    
 	    searchField.setBorder(BorderFactory.createLineBorder(Color.black));
	    searchField.setBounds(200,130,500,20);
 	    getContentPane().add(searchField);
 	    
	    search.setBounds(720,130,80,20);
 	    search.addActionListener(this);
 	    getContentPane().add(search);
 	    
	    albums.setBounds(250,160,90,20);
 	    albums.setFont(font);
 	    albums.setForeground(color);
 	    albums.setBackground(getContentPane().getBackground());
 	    getContentPane().add(albums);
 	    
 	    artists.setBounds(350,160,90,20);
 	    artists.setFont(font);
 	    artists.setForeground(color);
 	    artists.setBackground(getContentPane().getBackground());
 	    getContentPane().add(artists);
 	    
 	    playlists.setBounds(440,160,100,20);
 	    playlists.setFont(font);
 	    playlists.setForeground(color);
 	    playlists.setBackground(getContentPane().getBackground());
 	    getContentPane().add(playlists);
 	    
 	    songs.setBounds(550,160,80,20);
 	    songs.setFont(font);
 	    songs.setForeground(color);
 	    songs.setBackground(getContentPane().getBackground());
 	    getContentPane().add(songs);
 	    
 	    members.setBounds(630,160,110,20);
 	    members.setFont(font);
 	    members.setForeground(color);
 	    members.setBackground(getContentPane().getBackground());
 	    getContentPane().add(members);
 	    
 	    user.setBounds(350,300,90,20);
 	    username.setBounds(450,300,150,20);
 	    
 	    pass.setBounds(350,340,90,20);
 	    password.setBounds(450,340,150,20);
 	    
 	    confirm.setBounds(320,380,110,20);
 	    confirmPassword.setBounds(450,380,150,20);
 	    
 	    loginsubmit.setBounds(420,440,90,20);
 	    loginsubmit.addActionListener(this);
 	    
 	    registersubmit.setBounds(420,440,90,20);
 	    registersubmit.addActionListener(this);
 	    
 	    //Put home screen info in display, call when home button (myfaves) is pushed
 	    /*JLabel display = new JLabel();
 	    display.setText("Welcome to MyFaves, where you can create your own playlists from scratch and search for them!\n\n");
 	    getContentPane().add(display);*/
 	    repaint();
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 		JButton button = (JButton) arg0.getSource();
 		clearDisplay();
 		if(button == login)
 		{
 			getContentPane().add(user);
 			getContentPane().add(username);
 			getContentPane().add(pass);
 			getContentPane().add(password);
 			getContentPane().add(loginsubmit);
 		}
 		if(button == register)
 		{
 			getContentPane().add(user);
 			getContentPane().add(username);
 			getContentPane().add(pass);
 			getContentPane().add(password);
 			getContentPane().add(confirm);
 			getContentPane().add(confirmPassword);
 			getContentPane().add(registersubmit);
 		}
 		if(button == search)
 		{
 			table.getColumnModel().getColumn(0).setPreferredWidth(10);
 		    table.getColumnModel().getColumn(1).setPreferredWidth(10);
 		    table.getColumnModel().getColumn(2).setPreferredWidth(100);
 		    table.getColumnModel().getColumn(3).setPreferredWidth(100);
 		    table.getColumnModel().getColumn(4).setPreferredWidth(100);
 		    table.getColumnModel().getColumn(0).setHeaderRenderer(new CheckBoxHeader(new MyItemListener()));
 			getContentPane().add(scrollPane);
 			((MyTableModel)table.getModel()).removeAllRows();
 			//songlist.addSong(firstSong);
 			//((DefaultTableModel)table.getModel()).addRow(new Object[]{Boolean.FALSE,#,Song,Artist,Album})
 		}
 		if(button == loginsubmit)
 		{
 			//put code here to login to server
 		}
 		if(button == registersubmit)
 		{
 			//put code here to register to server
 		}
 		repaint();
 	}
 	public void clearDisplay()
 	{
 		getContentPane().remove(user);
 		getContentPane().remove(username);
 		getContentPane().remove(pass);
 		getContentPane().remove(password);
 		getContentPane().remove(loginsubmit);
 		getContentPane().remove(confirm);
 		getContentPane().remove(confirmPassword);
 		getContentPane().remove(registersubmit);
 		getContentPane().remove(scrollPane);
 	}
 	class MyItemListener implements ItemListener  
 	{  
 		@Override
 		public void itemStateChanged(ItemEvent e) {  
 			Object source = e.getSource();  
 			if (source instanceof AbstractButton == false) return;  
 			boolean checked = e.getStateChange() == ItemEvent.SELECTED;  
 			for(int x = 0, y = table.getRowCount(); x < y; x++)  
 			{  
 				table.setValueAt(new Boolean(checked),x,0);  
 			}  
 	    }
 	}
 	class MyTableModel extends AbstractTableModel {
 		Object [] columnNames = {"","#","Song","Artist","Album"};
 		
 		Song secondSong = firstSong;
 		
 		//Object [][] data = {{Boolean.FALSE,new Integer(1),"first song","first artist","first album"},{Boolean.FALSE,new Integer(2),"second song","second artist","second album"}};
 
         public int getColumnCount() {
             return columnNames.length;
         }
 
 		public int getRowCount() {
             return songlist.size();
         }
 
         public String getColumnName(int col) {
             return (String) columnNames[col];
         }
 
         public Object getValueAt(int row, int col) {
         	switch(col){
     		case 0:
     			return (Object) songlist.get(row).getBool();
     		case 1:
     			return (Object) songlist.get(row).getAlbumNumber();
     		case 2:
     			return (Object) songlist.get(row).getSongName();
     		case 3:
     			return (Object) songlist.get(row).getArtistName();
     		case 4:
     			return (Object) songlist.get(row).getAlbumName();
     		default:
     			return null;
         	}
         }
 
         /*
          * JTable uses this method to determine the default renderer/
          * editor for each cell.  If we didn't implement this method,
          * then the last column would contain text ("true"/"false"),
          * rather than a check box.
          */
         public Class getColumnClass(int c) {
             return getValueAt(0, c).getClass();
         }
 
         /*
          * Don't need to implement this method unless your table's
          * editable.
          */
         public boolean isCellEditable(int row, int col) {
             //Note that the data/cell address is constant,
             //no matter where the cell appears onscreen.
             if (col == 0) {
                 return true;
             } else {
                 return false;
             }
         }
 
         /*
          * Don't need to implement this method unless your table's
          * data can change.
          */
         public void setValueAt(Object value, int row, int col) {
         	switch(col){
         		case 0:
         			songlist.get(row).setBool((Boolean) value);
         			break;
         		case 1:
         			songlist.get(row).setAlbumNumber((Integer) value);
         			break;
         		case 2:
         			songlist.get(row).setSongName((String) value);
         			break;
         		case 3:
         			songlist.get(row).setArtistName((String) value);
         			break;
         		case 4:
         			songlist.get(row).setAlbumName((String) value);
         			break;
         	}
             fireTableCellUpdated(row, col);
         }
         
         public void removeAllRows()
         {
           for(int i=getRowCount();i>0;i--)
             removeRow(i-1);
         }
 
         public void removeRow(int row)
         {
             songlist.remove(row);
             fireTableRowsDeleted(row, row);
         }
         
         public void addSong(Song mySong)
         {
         	songlist.add(mySong);
         }
     }
 	
 	class Song {
 		private String songName;
 
 		private String artistName;
 
 		private String albumName;
 
 		private Boolean bool;
 		
 		private Integer albumNumber;
 
 		public Song(Boolean bool, Integer albumNumber, String songName, String artistName, String albumName) {
 		    this.bool = bool;
 		    this.albumNumber = albumNumber;
 		    this.songName = songName;
 		    this.artistName = artistName;
 		    this.albumName = albumName;
 		    }
 
 		  public String getSongName() {
 			return this.songName;
 		  }
 
 		  public void setSongName(String songName) {
 		    this.songName = songName;
 		  }
 
 		  public String getArtistName() {
 		    return this.artistName;
 		  }
 
 		  public void setArtistName(String artistName) {
 		    this.artistName = artistName;
 		  }
 
 		  public String getAlbumName() {
 		    return this.albumName;
 		  }
 
 		  public void setAlbumName(String albumName) {
 		    this.albumName = albumName;
 		  }
 		  
 		  public Boolean getBool() {
 			return this.bool;
 		  }
 		  
 		  public void setBool(Boolean bool) {
 			this.bool = bool;
 		  }
 		  
 		  public Integer getAlbumNumber() {
 			return this.albumNumber;
 		  }
 		  
 		  public void setAlbumNumber(Integer albumNumber) {
 			this.albumNumber = albumNumber;
 		  }
 	}
 	
 	class CheckBoxHeader extends JCheckBox  implements TableCellRenderer, MouseListener { 
 		protected CheckBoxHeader rendererComponent;  
 		protected int column;  
 		protected boolean mousePressed = false;  
 		public CheckBoxHeader(ItemListener itemListener) {  
 			rendererComponent = this;  
 			rendererComponent.addItemListener(itemListener);  
 		}  
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
 		{  
 			if (table != null)
 			{  
 				JTableHeader header = table.getTableHeader();  
 				if (header != null)
 				{  
 					rendererComponent.setForeground(header.getForeground());  
 					rendererComponent.setBackground(header.getBackground());  
 					rendererComponent.setFont(header.getFont());  
 					header.addMouseListener(rendererComponent);  
 				}  
 			}  
 			setColumn(column);
 			setBorder(UIManager.getBorder("TableHeader.cellBorder"));  
 			return rendererComponent;  
 		}  
 		protected void setColumn(int column) {  
 			this.column = column;  
 		}  
 		public int getColumn() {  
 			return column;  
 		}  
 		protected void handleClickEvent(MouseEvent e) {  
 			if (mousePressed) {  
 				mousePressed=false;  
 				JTableHeader header = (JTableHeader)(e.getSource());  
 				JTable tableView = header.getTable();  
 				TableColumnModel columnModel = tableView.getColumnModel();  
 				int viewColumn = columnModel.getColumnIndexAtX(e.getX());  
 				int column = tableView.convertColumnIndexToModel(viewColumn);  
 				if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {  
 					doClick();  
 				}  
 			}  
 		}  
 		@Override
 		public void mouseClicked(MouseEvent e) {  
 			handleClickEvent(e);  
 			((JTableHeader)e.getSource()).repaint();  
 		}  
 		@Override
 		public void mousePressed(MouseEvent e) {  
 			mousePressed = true;  
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
 	} 
}
