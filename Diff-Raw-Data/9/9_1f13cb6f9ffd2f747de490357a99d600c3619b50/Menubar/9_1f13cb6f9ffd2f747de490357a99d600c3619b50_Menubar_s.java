 /**
  * 
  */
 package no.whg.mini;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.io.*;
 
 import javax.swing.*;
 /**
  * @author PeerAndreas
  *
  */
 public class Menubar extends JMenuBar implements ActionListener, ItemListener
 {
 	String currentDirectory = null;
 	File currentFile = null;
 	CustomTableModel tableModel;
 	
 	public Menubar(CustomTableModel tableModel)
 	{
 		
 		this.tableModel = tableModel;
 		JMenuBar menuBar = new JMenuBar();	//the menubar that holds the menu
 		JMenu menu;	//an object of JMenu
 		JMenuItem menuItem;	//an object of JMenuItem
 		
 		menu = new JMenu(Messages.getString("Menubar.File"));	//create a menu named file
 		menu.setMnemonic(KeyEvent.VK_F);
 		add(menu); //add the menu too the menubar
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.New"), new ImageIcon("gbleditor_icons/NEW.GIF"));	//create an item to the menu with image
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("new");
 		menu.add(menuItem);	//add the item to the menu
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.Load"), new ImageIcon("gbleditor_icons/OPENDOC.GIF"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("load");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.Save"), new ImageIcon("gbleditor_icons/SAVE.GIF"));
 		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("save");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.SaveAs"), new ImageIcon("gbleditor_icons/SAVE.GIF"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("saveAs");
 		menu.add(menuItem);
 		
 		menu.addSeparator();	//adds a line separator to the menu
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.Preview"));	//create an item without the image
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("preview");
 		menu.add(menuItem);	//add the item to the menu
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.Generate"), new ImageIcon("gbleditor_icons/SAVEJAVA.GIF"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("generate");
 		menu.add(menuItem);
 		
 		menu.addSeparator();
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.Exit"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("exit");
 		menu.add(menuItem);
 		
 		menu = new JMenu(Messages.getString("Menubar.Edit"));	//creates a new menu next to the first
 		add(menu);	//add it to the JMenuBar
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.addRow"), new ImageIcon("gbleditor_icons/NEWROW.GIF"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("newRow");
 		menu.add(menuItem);
 		
 		menu.addSeparator();
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.preferences"));
 		menu.add(menuItem);
 		
 		menu = new JMenu(Messages.getString("Menubar.help"));	//creates a new menu next to the two others
 		add(menu);	//add the menu to JMenuBar
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.help"), new ImageIcon("gbleditor_icons/HELP.GIF"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("help");
 		menu.add(menuItem);
 		
 		menuItem = new JMenuItem(Messages.getString("Menubar.about"));
 		menuItem.addActionListener(this);
 		menuItem.setActionCommand("about");
 		menu.add(menuItem);
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
 		
 		if(e.getActionCommand() == "new")
 		{
 			JFileChooser newWindow = new JFileChooser();
 			int rVal = newWindow.showSaveDialog(Menubar.this);
 			
 			if(rVal == JFileChooser.APPROVE_OPTION)
 			{
 				currentFile = newWindow.getSelectedFile();
 				 
 				if(currentFile != null)
 				{
 					tableModel.delete();
 					try 
 					{
 						final ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(currentFile)));
 						out.writeObject(null);
 						out.close();
 					}
 					catch (IOException e1)
 					{
 						e1.printStackTrace();
 					}					
 				}
 				
 			}
 		}
 		else if(e.getActionCommand() == "load")
 		{
 			JFileChooser openWindow = new JFileChooser();	//create a new FileChooser
 			int rVal = openWindow.showOpenDialog(Menubar.this);	//creates a window for opening files
 			
 			if(rVal == JFileChooser.APPROVE_OPTION)	//if the user hit the open button in the dialog
 			{
 				currentFile = openWindow.getSelectedFile();	//get the file selected
 				
 				if(currentFile != null)	//if there is a file
 				{
 					try
 					{
 						FileInputStream fileInput = new FileInputStream(currentFile);	//create a file input from the current file
 						ObjectInputStream inputObject = new ObjectInputStream(fileInput);	//create an object input stream from the file input
 						inputObject.readObject();	//read the object stream
 						inputObject.close();	//close the object stream
 						fileInput.close();	//close the file input
 					}
 					catch(IOException | ClassNotFoundException e1)	//catch any exceptions thrown
 					{
 						e1.printStackTrace();
 					}
 				}
 			}			
 		}
 		else if(e.getActionCommand() == "save")
 		{
 			if(currentFile != null)	//if we have saved the file to a location
 			{
 				try 
 				{
 					final ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(currentFile)));
 					out.writeObject(tableModel.save());
 					out.close();
 				}
 				catch (IOException e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 			else	//if you have not saved the file to a location before
 			{
 				JFileChooser saveAsWindow = new JFileChooser();	//create a new FileChooser
 				int rVal = saveAsWindow.showSaveDialog(Menubar.this);	//creates a window for saving files and specify name
 				
 				if(rVal == JFileChooser.APPROVE_OPTION)	//if the user choose the save option
 				{
 					currentFile = saveAsWindow.getSelectedFile();	//get current file
 					
 					try 
 					{
 						final ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(currentFile)));
 						out.writeObject(tableModel.save());
 						out.close();
 					}
 					catch (IOException e1)
 					{
 						e1.printStackTrace();
 					}
 				}
 			}
 		}
 		else if(e.getActionCommand() == "saveAs")
 		{
 			JFileChooser saveAsWindow = new JFileChooser();	//create a new FileChooser
 			int rVal = saveAsWindow.showSaveDialog(Menubar.this);	//creates a window for saving files and specify name
 			
 			if(rVal == JFileChooser.APPROVE_OPTION)
 			{
 				currentFile = saveAsWindow.getSelectedFile();	//get the selected file the user saved to
 				
 				try 
 				{
 					final ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(currentFile)));
 					out.writeObject(tableModel.save());
 					out.close();
 				}
 				catch (IOException e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		}
 		else if(e.getActionCommand() == "preview")
 		{
 			
 		}
 		else if(e.getActionCommand() == "generate")
 		{
			
 		}
 		else if(e.getActionCommand() == "exit")
 		{
 			System.exit(0);	//exit program
 		}
 		else if(e.getActionCommand() == "newRow") {
 			tableModel.addRow();
 		}
 		
 	}
 
 
 }
