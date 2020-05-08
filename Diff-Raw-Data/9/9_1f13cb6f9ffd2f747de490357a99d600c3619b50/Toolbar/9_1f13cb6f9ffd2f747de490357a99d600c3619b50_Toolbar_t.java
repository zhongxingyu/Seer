 /**
  * 
  */
 package no.whg.mini;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JToolBar;
 /**
  * Class that handles the toolbar of the program, save buttons etc
  * @author Fredrik, Peer
  *
  */
 public class Toolbar extends JToolBar implements ActionListener, ItemListener
 {
 	private CustomTableModel tableModel;
 	File currentFile;
 	
 	public Toolbar(CustomTableModel tableModel)
 	{
 		this.tableModel = tableModel;
 		JToolBar toolBar = new JToolBar();
 		toolBar.setMinimumSize(new Dimension(400, 50));
 		toolBar.setMaximumSize(new Dimension(400,80));
 		setFloatable(true);
 		setRollover(true);
 		
 		JButton newButton = new JButton();
 		
 		ImageIcon newImage = new ImageIcon("gbleditor_icons/NEW.GIF");
 		newButton = new JButton(newImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.newToolTip"));
 		newButton.setActionCommand("new");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		ImageIcon newOpenImage = new ImageIcon("gbleditor_icons/OPENDOC.GIF");
 		newButton = new JButton(newOpenImage);	
 		newButton.setToolTipText(Messages.getString("Toolbar.OpenDocument"));
 		newButton.setActionCommand("open");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		ImageIcon newSaveImage = new ImageIcon("gbleditor_icons/SAVE.GIF");
 		newButton = new JButton(newSaveImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.Save"));
 		newButton.setActionCommand("save");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		addSeparator();
 		
 		ImageIcon newSaveJavaImage = new ImageIcon("gbleditor_icons/SAVEJAVA.GIF");
 		newButton = new JButton(newSaveJavaImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.Generate"));
 		newButton.setActionCommand("generate");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		ImageIcon newExecuteImage = new ImageIcon("gbleditor_icons/ExecuteProject.GIF");
 		newButton = new JButton(newExecuteImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.Run"));
 		newButton.setActionCommand("run");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		addSeparator();
 		
 		ImageIcon newRowImage = new ImageIcon("gbleditor_icons/NEWROW.GIF");
 		newButton = new JButton(newRowImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.addRow"));
 		newButton.setActionCommand("newRow");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		ImageIcon newRowDownImage = new ImageIcon("gbleditor_icons/MoveRowDown.GIF");
 		newButton = new JButton(newRowDownImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.rowDown"));
 		newButton.setActionCommand("rowDown");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		ImageIcon newRowUpImage = new ImageIcon("gbleditor_icons/MoveRowUp.GIF");
 		newButton = new JButton(newRowUpImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.rowUp"));
 		newButton.setActionCommand("rowUp");
 		newButton.addActionListener(this);
 		add(newButton);
 		
 		addSeparator();
 		
 		ImageIcon newHelpImage = new ImageIcon("gbleditor_icons/HELP.GIF");
 		newButton = new JButton(newHelpImage);
 		newButton.setToolTipText(Messages.getString("Toolbar.help"));
 		newButton.setActionCommand("help");
 		newButton.addActionListener(this);
 		add(newButton);
 	}
 
 	@Override
 	public void itemStateChanged(ItemEvent e)
 	{
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) 
 	{
 		if(e.getActionCommand() == "new")
 		{
 			JFileChooser newWindow = new JFileChooser();
 			int rVal = newWindow.showSaveDialog(Toolbar.this);
 			
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
 		else if(e.getActionCommand() == "open")
 		{
 			JFileChooser openWindow = new JFileChooser();	//create a FileChooser
 			int rVal = openWindow.showOpenDialog(Toolbar.this);	//creates a window for opening files
 			
 			if(rVal == JFileChooser.APPROVE_OPTION)
 			{
 				currentFile = openWindow.getSelectedFile();	//get the file the user selected
 				
 				if(currentFile != null)	//if the file exists
 				{
 					try
 					{
 						
 						final ObjectInputStream in = new ObjectInputStream(
 						        new BufferedInputStream(new FileInputStream(currentFile)));
 
 						    final Vector<TableData> data = (Vector<TableData>) in.readObject();
 						    tableModel.load(data);
 					}
 					catch(IOException | ClassNotFoundException e1)
 					{
 						e1.printStackTrace();
 					}
 				}
 			}
 		}
 		else if(e.getActionCommand() == "save")
 		{
 			if(currentFile != null) //as long as there is a file to save
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
 			else
 			{
 				JFileChooser saveAsWindow = new JFileChooser();	//create a new FileChooser
 				int rVal = saveAsWindow.showSaveDialog(Toolbar.this);	//creates a window for saving files and specify name
 				
 				if(rVal == JFileChooser.APPROVE_OPTION)	//if the user chose the save option
 				{
 					currentFile = saveAsWindow.getSelectedFile();	//get the file selected by the user
 					
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
 		else if(e.getActionCommand() == "generate")
 		{
			tableModel.startGeneration();
 		}
 		else if(e.getActionCommand() == "run")
 		{
 			
 		}
 		else if(e.getActionCommand() == "newRow")
 		{
 			tableModel.addRow();
 			
 			
 		}
 		else if(e.getActionCommand() == "rowDown")
 		{
 			tableModel.moveRowDown();
 		}
 		else if(e.getActionCommand() == "rowUp")
 		{
 			tableModel.moveRowUp();
 		}
 		else if(e.getActionCommand() == "help")
 		{
 			
 		}
 	}
 }
