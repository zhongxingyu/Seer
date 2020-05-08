 /*  mainJTree.java - The JTree for the main-window
  *  Copyright (C) 1999 Fredrik Ehnbom
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.gjt.fredde.yamm.gui.main;
 
 import java.awt.*;
 import java.awt.dnd.*;
 import java.awt.datatransfer.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.File;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.tree.*;
 import javax.swing.event.*;
 import org.gjt.fredde.yamm.YAMM;
 import org.gjt.fredde.yamm.mail.Mailbox;
 import org.gjt.fredde.util.gui.MsgDialog;
 
 /**
  * The tree for the main window
  */
 public class mainJTree extends JTree implements DropTargetListener {
 
   /** The ResourceBundle to get menu names. */
   static protected ResourceBundle res;
 
   /** The Properties that loads and saves information. */
   static protected Properties     props = new Properties();
 
   JPopupMenu treepop;
 
   DefaultMutableTreeNode top;
   protected static YAMM frame;
   protected static mainToolBar tbar;
   DefaultTreeModel tm;
 
   /**
    * Creates the tree and adds all the treestuff to the tree.
    * @param frame2 The JFrame that will be used for error messages etc
    * @param top2 The TreeNode that this tree will use.
    * @param tbar2 The mainToolBar to disable/enable buttons on.
    */
   public mainJTree(YAMM frame2, DefaultMutableTreeNode top2, mainToolBar tbar2) { 
    frame = frame2;
    tbar = tbar2;
    top = top2;
   // tree = this;
 
    new DropTarget(this, // component
      DnDConstants.ACTION_COPY_OR_MOVE, // actions
      this); //DropTargetListener
 
    try {
       InputStream in = new FileInputStream(System.getProperty("user.home") + "/.yamm/.config");
       props.load(in);
       in.close();
     } catch (IOException propsioe) { System.err.println(propsioe); }
 
     try {
       res = ResourceBundle.getBundle("org.gjt.fredde.yamm.resources.YAMM", Locale.getDefault());
     }
     catch (MissingResourceException mre) {
       mre.printStackTrace();
       System.exit(1);
     }
 
     tm = new DefaultTreeModel(top);
     setModel(tm);
     myTreeRenderer rend = new myTreeRenderer();
     rend.setFont(new Font("SansSerif", Font.PLAIN, 12));
     setCellRenderer(rend); //new myTreeRenderer());
     getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
     addMouseListener(mouseListener2);
 
     addTreeSelectionListener(new TreeSelectionListener() {
       public void valueChanged(TreeSelectionEvent e) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)(e.getPath().getLastPathComponent());
 
 
         if(!(node.toString()).equals("Mail Boxes")) {
            File box = new File(node.toString());
 
           if(node.toString().equals("deleted") || !box.exists()) {
             frame.selectedbox = System.getProperty("user.home") + "/.yamm/boxes/inbox";
             Mailbox.createList(frame.selectedbox, frame.listOfMails);
             ((JTable)frame.mailList).updateUI();
           }
 
           else if(!box.isDirectory()) {
             frame.selectedbox = node.toString();
             Mailbox.createList(frame.selectedbox, frame.listOfMails);
             ((JTable)frame.mailList).updateUI();
           }
         }
        if(((mainTable)frame.mailList).getSelectedRow() != -1 && !(((JTable)frame.mailList).getSelectedRow() >= frame.listOfMails.size())) { ((JButton)tbar.reply).setEnabled(true); ((JButton)tbar.forward).setEnabled(true); ((JButton)tbar.print).setEnabled(true); }
        else { ((JButton)tbar.reply).setEnabled(false); ((JButton)tbar.forward).setEnabled(false); ((JButton)tbar.print).setEnabled(false); }
       }
     });
 
     String sep = System.getProperty("file.separator");
 
     top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
     top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
     createNodes(top, new File(System.getProperty("user.home") + sep + ".yamm" + sep + "boxes" + sep));
     top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash"))); 
     treepop = new JPopupMenu("Test");
     treepop.setInvoker(this);
     expandRow(0);
 
     // adds items to the myPopup JPopupMenu
     JMenuItem mi = new JMenuItem(res.getString("tree.new.box")); //, delete = new JMenuItem(res.getString("button.delete"));
 
     mi.addActionListener(treepoplistener);
 //    delete.addActionListener(treepoplistener);
     treepop.add(mi);
  
     mi = new JMenuItem(res.getString("tree.new.group"));
     mi.addActionListener(treepoplistener);
     treepop.add(mi);
     treepop.addSeparator();
 
     mi = new JMenuItem(res.getString("button.delete"));
     mi.addActionListener(treepoplistener);
     treepop.add(mi);
   }
 
   public void drop(DropTargetDropEvent e) {
     try {
       DataFlavor stringFlavor = DataFlavor.stringFlavor;
       Transferable tr = e.getTransferable();
 
       if(e.isDataFlavorSupported(stringFlavor)) {
         String mails = (String)tr.getTransferData(stringFlavor);
 
         e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
         doAction(mails, e.getLocation(), e.getDropAction());
         e.dropComplete(true);
       }
       else {
         e.rejectDrop();
       }
     } catch (IOException ioe) {
         ioe.printStackTrace();
     } catch (UnsupportedFlavorException ufe) {
         ufe.printStackTrace();
     }
   }
 
   public void dragEnter(DropTargetDragEvent e) { }
   public void dragExit(DropTargetEvent e) { }
   public void dragOver(DropTargetDragEvent e) { }
   public void dropActionChanged(DropTargetDragEvent e) { }
 
   protected void doAction(String mails, Point p, int act) {
     String action = (act == DnDConstants.ACTION_MOVE ? "move" : "copy");
     System.out.println("Wanted to " + action + ": " + mails);
     System.out.println("from " + frame.selectedbox);
 
     TreePath tp = getPathForLocation(p.x, p.y);
     String box = null; // = tp.getLastPathComponent().toString();
     if(tp != null) {
       box = tp.getLastPathComponent().toString();
       System.out.println("to " + box);
     }
     else {
       System.out.println("to box was null ");
       box = null;
     }
 
    if(box != null && !box.endsWith(".g") && !box.equals("Mail Boxes")) {
       StringTokenizer tok = new StringTokenizer(mails);
       int[] list = new int[tok.countTokens()];
 
       for(int i = 0; tok.hasMoreTokens(); i++) {
         list[i] = Integer.parseInt(tok.nextToken());
       }
 
       if(list.length == 0) return;
       if(action.equals("move"))
         Mailbox.moveMail(frame.selectedbox, box, list);
       else
         Mailbox.copyMail(frame.selectedbox, box, list);
 
      frame.delUnNeededFiles();
       Mailbox.createList(frame.selectedbox, frame.listOfMails);
       frame.mailList.updateUI();
     }
   }
 
   /**
    * The renderer for the tree
    */
   class myTreeRenderer extends JLabel implements TreeCellRenderer {
 
     /** Whether or not the item that was last configured is selected. */
     protected boolean selected;
 
     public Component getTreeCellRendererComponent(
       JTree   tree,
       Object  value,
       boolean selected,
       boolean expanded,
       boolean leaf,
       int     row,
       boolean hasFocus)
     {
       String s = value.toString();
       StringTokenizer tok = new StringTokenizer(s, System.getProperty("file.separator"));
       String thisbox = null;
 
       while(tok.hasMoreTokens()) thisbox = tok.nextToken();
 
 
       if(leaf && !thisbox.endsWith(".g")) {
           if(thisbox.equals("inbox")) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/inbox.gif"));
           else if(thisbox.equals("outbox")) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/outbox.gif"));
           else if(thisbox.equals("trash")) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/trash.gif"));
           else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/box.gif"));
       }
       else if(!leaf) {
         if(expanded) setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir2.gif"));
         else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir.gif"));
       }
       else setIcon(new ImageIcon("org/gjt/fredde/yamm/images/boxes/dir.gif"));
 
       if(thisbox.endsWith(".g")) setText(thisbox.substring(0, thisbox.length() -2));
       else setText(thisbox);
       setForeground(Color.black);
       this.selected = selected;
       return this;
     }
 
 
     /**
       * paint is subclassed to draw the background correctly.  JLabel
       * currently does not allow backgrounds other than white, and it
       * will also fill behind the icon.  Something that isn't desirable.
       */
     public void paint(Graphics g) {
 	Color            bColor;
 	Icon             currentI = getIcon();
 
 	if(selected)
             bColor = new Color(204, 204, 255);
 	else if(getParent() != null)
             // Pick background color up from parent (which will come from the JTree we're contained in).
 	    bColor = getParent().getBackground();
 	else
 	    bColor = getBackground();
 	g.setColor(bColor);
 
 	if(currentI != null && getText() != null) {
 	    int          offset = (currentI.getIconWidth() + getIconTextGap());
 
 	    g.fillRect(offset, 0, getWidth() - 1 - offset,
 		       getHeight() - 1);
 	}
 	else
 	    g.fillRect(0, 0, getWidth()-1, getHeight()-1);
 	super.paint(g);
     }
   }
 
   public void createNodes(DefaultMutableTreeNode top, File f) {
     DefaultMutableTreeNode dir = null;
     DefaultMutableTreeNode box  = null;
     String sep = System.getProperty("file.separator");
     String home = System.getProperty("user.home");
 
     if((f.toString()).equals(home + sep + ".yamm" + sep + "boxes")) {
       String list[] = f.list();
       for(int i = 0; i < list.length; i++)
         createNodes(top, new File(f, list[i]));
     }
     else if(f.isDirectory()) {
       dir = new DefaultMutableTreeNode(f);
 
       top.add(dir);
 
       String list[] = f.list();
       for(int i = 0; i < list.length; i++)
         createNodes(dir, new File(f, list[i]));
     }
     else if(!(f.toString()).equals(home + sep + ".yamm" + sep + "boxes" + sep + "outbox") &&
             !(f.toString()).equals(home + sep + ".yamm" + sep + "boxes" + sep + "trash") &&
 	    !(f.toString()).equals(home + sep + ".yamm" + sep + "boxes" + sep + "inbox") &&
             (f.toString()).indexOf(sep + ".", (f.toString()).indexOf("boxes")) == -1) {
 
       box = new DefaultMutableTreeNode(f);
       top.add(box);
     }
   }
 
   public void createGroupList(Vector vect, File f) {
     String sep = System.getProperty("file.separator");
     String home = System.getProperty("user.home");
 
     if((f.toString()).equals(home + sep + ".yamm" + sep + "boxes")) {
       vect.add(sep);
       String list[] = f.list();                                      
       for(int i = 0; i < list.length; i++)
         createGroupList(vect, new File(f, list[i]));
     }                                          
     else if(f.isDirectory() && f.toString().endsWith(".g")) {
       String dir = f.toString();
       dir = dir.substring((home + sep + ".yamm" + sep + "boxes").length(), dir.length() -2);
       vect.add(dir);                       
                    
       String list[] = f.list();
       for(int i = 0; i < list.length; i++)
         createGroupList(vect, new File(f, list[i]));
     }                                          
   }  
 
   ActionListener treepoplistener = new ActionListener() {
     public void actionPerformed(ActionEvent ae) {
       String kommando = ((JMenuItem)ae.getSource()).getText();
       
       if(kommando.equals(res.getString("tree.new.box"))) {
         new newBoxDialog(frame);
       }
       else if(kommando.equals(res.getString("tree.new.group"))) {
         new newGroupDialog(frame);
       }
       else {
         if(getLastSelectedPathComponent() != null) {
 
           File del = new File(getLastSelectedPathComponent().toString());
 
 
           if(!del.isDirectory() && del.exists()) {
             String file = getLastSelectedPathComponent().toString();
             String sep  = System.getProperty("file.separator");
  
             if(!file.endsWith(sep + "inbox") && !file.endsWith(sep + "outbox") && !file.endsWith(sep + "trash")) {
               frame.selectedbox = "deleted";
               top.removeAllChildren();
 
               del.delete();
 
               top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
               top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
               createNodes(top, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
               top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash"))); 
               updateUI();
               expandRow(0);
 
               ((mainTable)frame.mailList).popup = new JPopupMenu();
               ((mainTable)frame.mailList).popup.setInvoker(frame.mailList);
               ((mainTable)frame.mailList).createPopup(((mainTable)frame.mailList).popup);
             }
           }
           else if(del.exists()) {
             if(!del.delete()) 
               new MsgDialog(frame, res.getString("msg.error"), res.getString("msg.file.delete-dir") + del.toString() + res.getString("msg.file.delete-dir2"));
             else {
               frame.selectedbox = "deleted";
               top.removeAllChildren();
 
               top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
               top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
               createNodes(top, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
               top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash")));
               updateUI();
               expandRow(0);
 
 //              ((mainTable)frame.mailList).popup = new JPopupMenu();
 //              ((mainTable)frame.mailList).popup.setInvoker(frame.mailList);
 //              ((mainTable)frame.mailList).createPopup(((mainTable)frame.mailList).popup);
             }
           }
         }
       }
     }
   };
 
   MouseListener mouseListener2 = new MouseAdapter() {
     public void mouseReleased(MouseEvent me) {
       if(me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
     }
     public void mousePressed(MouseEvent me) {
       if(me.isPopupTrigger()) treepop.show(getParent(), me.getX(), me.getY());
     }
 
   };
 
   protected void removeDotG(Vector vect) {
     for(int i = 0; i < vect.size(); i++) {
       String temp = vect.elementAt(i).toString();
 
       while(temp.indexOf(".g") != -1) {
         temp = temp.substring(0, temp.indexOf(".g")) + temp.substring(temp.indexOf(".g") + 2, temp.length());
       }
       System.out.println("temp: " + temp);
       vect.setElementAt(temp, i);
     }
   }
 
   class newGroupDialog extends JDialog {
     JButton    b;
     JComboBox  group;
     JTextField jtfield;
 
     public newGroupDialog(JFrame frame) {
       super(frame, true);
       setBounds(0, 0, 300, 100);
 //      setResizable(false);
       setTitle(res.getString("title.new.group"));
 
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
       getContentPane().setLayout(new GridLayout(3, 2));
 
       getContentPane().add(new JLabel(res.getString("options.group")));
       Vector vect = new Vector();
       createGroupList(vect, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
       removeDotG(vect);
       group = new JComboBox( vect );
       getContentPane().add(group);
 
       getContentPane().add(new JLabel(res.getString("options.name")));
       jtfield = new JTextField();
       getContentPane().add(jtfield);
 
 
       b = new JButton(res.getString("button.ok"));
       b.addActionListener(BListener2);
       getContentPane().add(b);
 
       b = new JButton(res.getString("button.cancel"));
       b.addActionListener(BListener2);
       getContentPane().add(b);
 
       show();
     }
 
     ActionListener BListener2 = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         String arg = ((JButton)e.getSource()).getText();
         String sep = System.getProperty("file.separator");
 
         if(arg.equals(res.getString("button.ok"))) {
           String gName = group.getSelectedItem().toString();
           String temp = "";
 
           if(!gName.equals(sep)) {
             StringTokenizer tok = new StringTokenizer(gName, sep);
 
             while(tok.hasMoreTokens()) {
               temp +=  tok.nextToken() + ".g" + sep;
             }
             gName = temp;
  
           }
           System.out.println("gName: "  + gName);
 
           File box = new File(System.getProperty("user.home") + 
                               "/.yamm/boxes/" + 
                               gName + 
                               jtfield.getText() + ".g");
 
           if(box.exists()) new MsgDialog(frame, res.getString("msg.error"), res.getString("msg.file.exists"));
           else {
             if(!box.mkdir()) 
               new MsgDialog(frame, res.getString("msg.error"), res.getString("msg.file.create-error") + box.toString());
 
             top.removeAllChildren();
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
             createNodes(top, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash")));
             updateUI();
 
             dispose();
           }
         }
 
         else if(arg.equals(res.getString("button.cancel"))) {
           dispose();
         }
       }  
     }; 
   }
 
   class newBoxDialog extends JDialog {
     JButton    b;
     JComboBox  group;
     JTextField jtfield;
   
     public newBoxDialog(JFrame frame) {
       super(frame, true);  
       setBounds(0, 0, 300, 100);
 //      setResizable(false);      
       setTitle(res.getString("title.new.box"));
  
       Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       setLocation((screenSize.width - 300) / 2, (screenSize.height - 200) / 2);
       getContentPane().setLayout(new GridLayout(3, 2));
  
       getContentPane().add(new JLabel(res.getString("options.group")));
       Vector vect = new Vector();
       createGroupList(vect, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
       removeDotG(vect);
       group = new JComboBox(vect);
       getContentPane().add(group);
 
       getContentPane().add(new JLabel(res.getString("options.name")));
       jtfield = new JTextField();
       getContentPane().add(jtfield);
  
 
       b = new JButton(res.getString("button.ok"));
       b.addActionListener(BListener2);
       getContentPane().add(b);
  
       b = new JButton(res.getString("button.cancel"));
       b.addActionListener(BListener2);
       getContentPane().add(b);
  
       show();
     }
  
     ActionListener BListener2 = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         String arg = ((JButton)e.getSource()).getText();
         String sep = System.getProperty("file.separator");
  
         if(arg.equals(res.getString("button.ok"))) {
           String gName = group.getSelectedItem().toString();
           String temp = "";
 
           if(!gName.equals(sep)) {
             StringTokenizer tok = new StringTokenizer(gName, sep);
 
             while(tok.hasMoreTokens()) {
               temp +=  tok.nextToken() + ".g" + sep;
             }
             gName = temp;
 
           }
 
           System.out.println("gName: "  + gName);
           File box = new File(System.getProperty("user.home") + 
                               "/.yamm/boxes/" + 
                               gName + 
                               jtfield.getText());
 
           if(box.exists()) new MsgDialog(frame, res.getString("msg.error"), res.getString("msg.file.exists"));
           else {
             try {
               box.createNewFile();
             } catch (IOException ioe) { new MsgDialog(frame, res.getString("msg.error"), ioe.toString()); }
 
             top.removeAllChildren();
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/inbox")));
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/outbox")));
             createNodes(top, new File(System.getProperty("user.home") + "/.yamm/boxes/"));
             top.add(new DefaultMutableTreeNode(new File(System.getProperty("user.home") + "/.yamm/boxes/trash")));
             updateUI();
 
             ((mainTable)frame.mailList).popup = new JPopupMenu();
             ((mainTable)frame.mailList).popup.setInvoker(frame.mailList);
             ((mainTable)frame.mailList).createPopup(((mainTable)frame.mailList).popup);
 
             dispose();
           }
         }
 
         else if(arg.equals(res.getString("button.cancel"))) {
           dispose();
         }
       }
     };
   }
 }
