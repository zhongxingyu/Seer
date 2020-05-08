 /*
  * CHIMP 1.1.1 - Cyber Helper Internet Monkey Program
  * Copyright (C) 2001-2012 Bill Havanki
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package havanki.chimp;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.datatransfer.*;
 import javax.swing.*;
 import java.io.*;
 import org.w3c.dom.*;
 import org.xml.sax.*;
 
 public class Chimp extends JFrame implements ActionListener, MouseListener
 {
   private static final String NEW = "New";
   private static final String OPEN = "Open...";
   private static final String SAVE = "Save";
   private static final String SAVE_AS = "Save As...";
   private static final String EXIT = "Exit";
 
   private static final String ADD = "Add New Item...";
   private static final String MODIFY = "Modify...";
   private static final String REMOVE = "Remove";
   private static final String COPY = "Copy Item to Clipboard";
   private static final String CLEAR = "Clear Clipboard";
 
   private static final String HP_MODE = "Hide Passwords";
 
   private static final String ABOUT = "About";
 
   File currFile = null;
   SecureItemTable tbl = null;
   JList itemList;
   JCheckBoxMenuItem hpmodeItem;
   boolean hidePasswords = true;
 
   private void fillList()
   {
     if (tbl == null)
       {
 	itemList.setListData (new String[0]);
       }
     else
       {
 	String[] titles = tbl.getTitlesInOrder();
 	itemList.setListData (titles);
       }
     itemList.invalidate();
     return;
   }
 
   public void actionPerformed (ActionEvent e)
   {
     this.repaint();
     String command = e.getActionCommand();
 
     if (command.equals (EXIT))
       System.exit (0);
 
     //    System.out.println ("Doing " + command);
 
     if (command.equals (ABOUT))
       {
 	JOptionPane.showMessageDialog (this,
 				       loadImageIconAsResource ("about.gif"),
 				       "About CHIMP",
 				       JOptionPane.PLAIN_MESSAGE);
 	return;
       }
     if (command.equals (HP_MODE))
       {
 	hidePasswords = hpmodeItem.isSelected();
       }
 
     if (command.equals (NEW))
       {
 	if (tbl != null)
 	  {
 	    int rc = JOptionPane.showConfirmDialog
 	      (this,
 	       "Are you sure you want to start over?",
 	       "Confirm", JOptionPane.YES_NO_OPTION,
 	       JOptionPane.WARNING_MESSAGE);
 	    if (rc == JOptionPane.NO_OPTION) return;  // = abort
 	  }
 
 	currFile = null;
 	tbl = new SecureItemTable();
 	getRootPane().putClientProperty ("Window.documentModified", Boolean.TRUE);
 
 	fillList();
       }
 
     if (command.equals (OPEN))
       {
 	this.repaint();
 	/*
 	JFileChooser chooser =
 	  new JFileChooser (System.getProperty ("user.dir"));
 	int rc = chooser.showOpenDialog (this);
 	if (rc != JFileChooser.APPROVE_OPTION) return;
 
 	currFile = chooser.getSelectedFile();
 	*/
 	FileDialog fdialog =
 	    new FileDialog (this, "Open a password file", FileDialog.LOAD);
 	fdialog.show();
 	String chosenFile = fdialog.getFile();
 	if (chosenFile == null) { return; }
 	currFile = new File (fdialog.getDirectory() +
 			     System.getProperty ("file.separator") +
 			     chosenFile);
 	PassFileReader pfr = new PassFileReader (currFile);
 
 	do {
 	    PasswordDialog pd = new PasswordDialog (this,
 						    "Decrypting file:",
 						    false);
 	    char[] password = pd.getPassword();
 	    if (password == null) return;
 
 	    try {
 		tbl = pfr.read (password);
 		PasswordDialog.cleanCharArray (password);
 		if (tbl == null) {
 		    JOptionPane.showMessageDialog
 			(this,
 			 "I think you entered the wrong password. Try again.",
 			 "Parsing Error", JOptionPane.ERROR_MESSAGE);
 		}
 	    } catch (IOException exc) {
 		System.err.println ("Error reading: " + exc);
 		exc.printStackTrace (System.err);
 	    }
 	} while (tbl == null);
 
 	getRootPane().putClientProperty ("Window.documentModified", Boolean.FALSE);
 	fillList();
       }
 
     if (command.equals (SAVE) || command.equals (SAVE_AS))
       {
 	if (command.equals (SAVE_AS) || currFile == null)
 	  {
 	      /*
 	    JFileChooser chooser =
 	      new JFileChooser (System.getProperty ("user.dir"));
 	    int rc = chooser.showSaveDialog (this);
 	    if (rc != JFileChooser.APPROVE_OPTION) return;
 
 	    currFile = chooser.getSelectedFile();
 	      */
 	      FileDialog fdialog =
 		  new FileDialog (this, "Save a password file",
 				  FileDialog.SAVE);
 	      fdialog.show();
 	      String chosenFile = fdialog.getFile();
 	      if (chosenFile == null) { return; }
 	      currFile = new File (fdialog.getDirectory() +
 				   System.getProperty ("file.separator") +
 				   chosenFile);
 	  }
 	PassFileWriter pfw = new PassFileWriter (currFile);
 
 	PasswordDialog pd = new PasswordDialog (this,
 						"Encrypting file:",
 						true);
 	char[] password = pd.getPassword();
 	if (password == null) return;
 
 	try {
 	  pfw.write (tbl, password);
 	  getRootPane().putClientProperty ("Window.documentModified", Boolean.FALSE);
 	} catch (IOException exc) {
 	  System.err.println ("Error writing: " + exc);
 	}
 	PasswordDialog.cleanCharArray (password);
       }
 
     // - - - - -
 
     if (command.equals (ADD) || command.equals (MODIFY))
       {
 	// Get the item. ADD = new item, MODIFY = selected item if any.
 	SecureItem item;
 	String selectedTitle = null;
 
 	if (command.equals (ADD))
 	  {
 	    item = new SecureItem();
 	  }
 	else
 	  {
 	    selectedTitle = (String) itemList.getSelectedValue();
 	    if (selectedTitle == null) return;
 	    item = tbl.get (selectedTitle);
 	  }
 
 	// Do the dialog.
 	ItemEdit editDialog = new ItemEdit (this, item, tbl,
 					    (command.equals(ADD)),
 					    hidePasswords);
 	if (editDialog.isItemModified()) {
 	    getRootPane().putClientProperty ("Window.documentModified", Boolean.TRUE);
 	}
 	fillList();
       }
 
     if (command.equals (REMOVE))
       {
 	String selectedTitle = (String) itemList.getSelectedValue();
 	if (selectedTitle == null) return;
 
 	int rc = JOptionPane.showConfirmDialog
 	  (this,
 	   "Are you sure you want to delete \"" + selectedTitle + "\"?",
 	   "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 	if (rc == JOptionPane.NO_OPTION) return;  // = abort
 
 	tbl.remove (selectedTitle);
 	getRootPane().putClientProperty ("Window.documentModified", Boolean.TRUE);
 
 	fillList();
       }
 
     // - - - - -
 
     if (command.equals (COPY))
       {
 	String selectedTitle = (String) itemList.getSelectedValue();
 	if (selectedTitle == null) return;
 
 	Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
 	SecureItem item = tbl.get (selectedTitle);
 
 	StringSelection ss = new StringSelection
 	    (new String (item.getPassword()));
 	cb.setContents (ss, ss);
       }
     if (command.equals (CLEAR))
       {
 	Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
 	StringSelection ss = new StringSelection ("");
 	cb.setContents (ss, ss);
       }
   }
 
   public void mouseClicked (MouseEvent e)
   {
     // This is just looking for double clicks in the item list.
     if (e.getClickCount() == 2)
       {
 	// Simulate a MODIFY action.
 	ActionEvent ae = new ActionEvent (itemList, -1, MODIFY);
 	actionPerformed (ae);
       }
   }
   public void mousePressed (MouseEvent e) {}
   public void mouseReleased (MouseEvent e) {}
   public void mouseEntered (MouseEvent e) {}
   public void mouseExited (MouseEvent e) {}
 
   public Chimp()
   {
     super ("CHIMP!");
 
     setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
     addWindowListener (new WindowAdapter() {
 	public void windowClosing (WindowEvent e)
 	{
 	  System.exit (0);
 	}
       }
 		       );
     Container cp = getContentPane();
     JPanel mainPanel = new JPanel();
     mainPanel.setLayout (new BorderLayout());
 
     // OS DETECTION
     String osString = System.getProperty ("os.name");
     boolean imAMac = (osString.toLowerCase (java.util.Locale.US)
 		      .indexOf ("mac") != -1);
 
     // MENU
     JMenuBar menuBar = new JMenuBar();
     setJMenuBar (menuBar);
 
     // MENU - File
     JMenu menu = new JMenu ("File");
     menu.setMnemonic(KeyEvent.VK_F);
     menuBar.add (menu);
 
     JMenuItem menuItem = new JMenuItem (NEW, KeyEvent.VK_N);
     setAccelerator (menuItem, KeyEvent.VK_N, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (NEW);
     menu.add (menuItem);
     menuItem = new JMenuItem (OPEN, KeyEvent.VK_O);
     setAccelerator (menuItem, KeyEvent.VK_O, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (OPEN);
     menu.add (menuItem);
     menuItem = new JMenuItem (SAVE, KeyEvent.VK_S);
     setAccelerator (menuItem, KeyEvent.VK_S, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (SAVE);
     menu.add (menuItem);
     menuItem = new JMenuItem (SAVE_AS, KeyEvent.VK_A);
     //    setAccelerator (menuItem, KeyEvent.VK_A, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (SAVE_AS);
     menu.add (menuItem);
     if (!imAMac) {
 	menu.addSeparator();
 	menuItem = new JMenuItem (EXIT, KeyEvent.VK_X);
 	setAccelerator (menuItem, KeyEvent.VK_X, imAMac);
 	menuItem.addActionListener (this);
 	menuItem.setActionCommand (EXIT);
 	menu.add (menuItem);
     }
 
     // MENU - Edit
     menu = new JMenu ("Edit");
     menu.setMnemonic(KeyEvent.VK_E);
     menuBar.add (menu);
 
     menuItem = new JMenuItem (ADD, KeyEvent.VK_A);
     setAccelerator (menuItem, KeyEvent.VK_A, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (ADD);
     menu.add (menuItem);
     menuItem = new JMenuItem (MODIFY, KeyEvent.VK_M);
     setAccelerator (menuItem, KeyEvent.VK_M, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (MODIFY);
     menu.add (menuItem);
     menuItem = new JMenuItem (REMOVE, KeyEvent.VK_R);
     setAccelerator (menuItem, KeyEvent.VK_R, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (REMOVE);
     menu.add (menuItem);
     menu.addSeparator();
     menuItem = new JMenuItem (COPY, KeyEvent.VK_C);
     setAccelerator (menuItem, KeyEvent.VK_C, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (COPY);
     menu.add (menuItem);
     menuItem = new JMenuItem (CLEAR, KeyEvent.VK_L);
     setAccelerator (menuItem, KeyEvent.VK_L, imAMac);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (CLEAR);
     menu.add (menuItem);
 
     // MENU - Options
     menu = new JMenu ("Options");
     menu.setMnemonic(KeyEvent.VK_O);
     menuBar.add (menu);
 
     hpmodeItem = new JCheckBoxMenuItem (HP_MODE, hidePasswords);
    hpmodeItem.setMnemonic (KeyEvent.VK_P);
    setAccelerator (hpmodeItem, KeyEvent.VK_P, imAMac);
     hpmodeItem.addActionListener (this);
     hpmodeItem.setActionCommand (HP_MODE);
     menu.add (hpmodeItem);
 
     // MENU - Help
     menu = new JMenu ("Help");
     menu.setMnemonic(KeyEvent.VK_H);
     menuBar.add (menu);
 
     menuItem = new JMenuItem (ABOUT, KeyEvent.VK_A);
     menuItem.addActionListener (this);
     menuItem.setActionCommand (ABOUT);
     menu.add (menuItem);
 
     // TOOLBAR
     JToolBar toolbar = new JToolBar();
     JButton tbButton = new JButton (loadImageIconAsResource ("New16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (NEW);
     tbButton.setToolTipText ("New File");
     toolbar.add (tbButton);
     tbButton = new JButton (loadImageIconAsResource ("Open16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (OPEN);
     tbButton.setToolTipText ("Open File");
     toolbar.add (tbButton);
     tbButton = new JButton (loadImageIconAsResource ("Save16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (SAVE);
     tbButton.setToolTipText ("Save File");
     toolbar.add (tbButton);
 
     toolbar.addSeparator();
 
     tbButton = new JButton (loadImageIconAsResource ("Add16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (ADD);
     tbButton.setToolTipText ("Add Item");
     toolbar.add (tbButton);
     tbButton = new JButton (loadImageIconAsResource ("Delete16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (REMOVE);
     tbButton.setToolTipText ("Remove Item");
     toolbar.add (tbButton);
     tbButton = new JButton (loadImageIconAsResource ("Copy16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (COPY);
     tbButton.setToolTipText ("Copy to Clipboard");
     toolbar.add (tbButton);
     tbButton = new JButton (loadImageIconAsResource ("Remove16.gif"));
     tbButton.addActionListener (this);
     tbButton.setActionCommand (CLEAR);
     tbButton.setToolTipText ("Clear Clipboard");
     toolbar.add (tbButton);
 
     // FRAME CONTENTS
     mainPanel.add (toolbar, BorderLayout.NORTH);
 
     itemList = new JList() {
 	    public String getToolTipText (MouseEvent e) {
 		Point p = e.getPoint();
 		int idx = locationToIndex (p);
 		String username = (tbl.getUsernamesInTitleOrder()) [idx];
 		if (username != null && !(username.trim().equals (""))) {
 		    return "Username: " + username;
 		} else {
 		    return "Username unknown";
 		}
 	    }
 	};
    itemList.setVisibleRowCount (25);
     itemList.addMouseListener (this);
     ToolTipManager.sharedInstance().registerComponent (itemList);
     JScrollPane scrollPane = new JScrollPane (itemList);
     scrollPane.setBorder (BorderFactory.createCompoundBorder
 			  (BorderFactory.createEmptyBorder (0,5,5,5),
 			   BorderFactory.createCompoundBorder
 			   (BorderFactory.createEtchedBorder(),
 			    BorderFactory.createEmptyBorder (5,5,5,5))));
     mainPanel.add (scrollPane, BorderLayout.CENTER);
 
     cp.add (mainPanel);
 
     // Do a new to initialize.
     ActionEvent e = new ActionEvent (this, -1, NEW);
     actionPerformed (e);
   }
 
   private ImageIcon loadImageIconAsResource (String filename)
   {
     try {
       InputStream in =
 	ClassLoader.getSystemClassLoader().getResourceAsStream (filename);
       if (in == null) {
 	System.err.println ("Could not open " + filename);
 	return null;
       }
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       int b;
       //      int ct = 0;
       while ((b = in.read()) != -1)
 	{
 	  baos.write (b);
 	  //	  ct++;
 	}
       in.close();
       //      System.err.println ("Read " + ct + " bytes");
       ImageIcon icon = new ImageIcon (baos.toByteArray());
       baos.close();
       return icon;
     } catch (IOException exc) {
       System.err.println ("Could not get image " + filename + ": " +
 			  exc.getMessage());
       return null;
     }
   }
 
   public void setIconImage (String filename)
   {
     ImageIcon i = loadImageIconAsResource (filename);
     if (i != null) super.setIconImage (i.getImage());
   }
 
     private void setAccelerator (JMenuItem jmi, int key, boolean imAMac) {
 	if (imAMac) {
 	    jmi.setAccelerator (KeyStroke.getKeyStroke (key,
 							Event.META_MASK));
 	} else {
 	    jmi.setAccelerator (KeyStroke.getKeyStroke (key,
 							Event.CTRL_MASK));
 	}
     }
 
   public static void main (String args[])
   {
     System.out.println ("CHIMP 1.1, Copyright (C) 2001-2005 Bill Havanki");
     System.out.println ("CHIMP comes with ABSOLUTELY NO WARRANTY. This is free software, and you are");
     System.out.println ("welcome to redistribute it under certain conditions. For details, see");
     System.out.println ("Help, About.");
     System.out.println ("");
 
     java.security.Security.addProvider (new com.sun.crypto.provider.SunJCE());
 
     Chimp chimp = new Chimp();
     //chimp.setIconImage ("chimpy.gif");
     chimp.pack();
 
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     int x = (screenSize.width - chimp.getWidth()) / 2;
     int y = (screenSize.height - chimp.getHeight()) / 2;
     chimp.setLocation (x, y);
 
     chimp.setVisible (true);
   }
 }
