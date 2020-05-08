 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import com.sun.jna.*;
 	
 public class ViewPanel extends JPanel
 {
 	private static final long serialVersionUID = 1L;
 	private JButton logout, add;
 	private JLabel instructions;
 	private JList photos;
 	private JScrollPane scroll;
 	private String[] data;
 	private BufferedImage preview;
 	private JLabel pictureframe;
 	private MainPanel panel;
 	private boolean prevSelect;
 	private File file;
 	private boolean login;
 	 
 	public ViewPanel(MainPanel panel)
 	{	
 		this.panel = panel;
 		prevSelect=false;
 		instructions = new JLabel("Click on the name of the photo you would like to view\nand it will show up on the right.");
 		
 		if(login)
 		{
			logout = new JButton("Logout");
 		}//end if statement
 		else
 		{
			
 		}//end else statement
 		add = new JButton("Add Photos");
 		
 		setPreferredSize(new Dimension(400,400));
 		
 		pictureframe = new JLabel();
 		
 		ArrayList<String> temp = panel.getControl().getFiles(panel.getCurUser());
 		data = new String[temp.size()];
 		
 		for(int i = 0; i<temp.size();i++)
 		{
 			data[i]=temp.get(i);
 		}//end for loop
 		
 		photos = new JList(data);
 		photos.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		photos.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		photos.setVisibleRowCount(10);
 		
 		ListListener listlisten = new ListListener();
 		photos.addListSelectionListener(listlisten);
 		
 		scroll = new JScrollPane(photos);
 		scroll.setPreferredSize(new Dimension(250, 80));
 		
 		LogoutListener log = new LogoutListener();
 		logout.addActionListener(log);
 		
 		LogoutListener ad = new LogoutListener();
 		add.addActionListener(ad);
 		
 		add(instructions);
 		add(scroll);
 		add(pictureframe);
 		add(add);
 		add(logout);
 	}//end ViewPanel constructor
 	
 	public void setPanel(MainPanel panel)
 	{
 		this.panel = panel;
 	}//end setPanel method
 	
 	public void setLogin(boolean state)
 	{
 		login = state;
 	}//end login setter
 	
 	public interface CStdLib extends Library 
 	{
         int syscall(int number, Object... args);
     }//end CStdLib interface
 	
 	private class ListListener implements ListSelectionListener
 	{
 		public void valueChanged(ListSelectionEvent e) 
 		{
 			CStdLib c = (CStdLib)Native.loadLibrary("c", CStdLib.class);
 			if (e.getValueIsAdjusting() == false) 
 		    {
 				c.syscall(289, true);
 				if(prevSelect)
 		    	{
 		    		try
 		    		{
 		    			ImageIO.write(preview,"jpg",file);
 		    		}//end try block
 		    		catch(IOException ex)
 		    		{
 		    			ex.printStackTrace();
 		    		}//end catch block
 		    	}
 		    	int sel = photos.getSelectedIndex();
 		    	try 
 				{
 					file = new File(data[sel]);
 		    		preview = ImageIO.read(file);
 				}//end try block
 				catch (IOException ex) 
 				{
 					ex.printStackTrace();
 				}//end catch block
 		    	c.syscall(289,false);
 				pictureframe = new JLabel(new ImageIcon( preview ));
 				prevSelect = true;
 		    }
 		}
 	}//end ButtonListener class
 	
 	private class LogoutListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			if(e.getSource()== logout)
 			{
 				panel.logout();
 			}//end if statement
 			else
 			{
 				panel.switchAdd();
 			}//end else statement
 		}//end ActionPerformed method
 	}//end ButtonListener class
 }//end ViewPanel class
