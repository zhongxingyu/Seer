 package rxr.util;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import rxr.*;
 
 /**
  * @author Josh Vinson
  */
 public class WindowUtil
 {
 	public static void warning(String warning)
 	{
 		JOptionPane.showMessageDialog(null, warning, "Warning", JOptionPane.WARNING_MESSAGE);
 	}
 
 	public static void error(Exception exception, String error, boolean fatal)
 	{
 		JOptionPane.showMessageDialog(null, error + "\n\nException text: " + exception, "Error", JOptionPane.ERROR_MESSAGE);
		if(exception != null)
		{
			exception.printStackTrace(RXR.log);
		}
 		if(fatal)
 		{
 			java.lang.System.exit(0);
 		}
 	}
 
 	public static boolean confirm(String message)
 	{
 		return confirm(null, message);
 	}
 
 	public static boolean confirm(Component parentComponent, String message)
 	{
 		return JOptionPane.showConfirmDialog(parentComponent, message, "Confirm Action", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
 	}
 
 	public static void center(Component c)
 	{
 		center(c, null);
 	}
 
 	public static void center(Component c, Component rel)
 	{
 		if(rel != null)
 		{
 			c.setLocation(rel.getLocation().x + rel.getSize().width / 2 - c.getSize().width / 2, rel.getLocation().y + rel.getSize().height / 2 - c.getSize().height / 2);
 		}
 		else
 		{
 			c.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - c.getSize().width / 2, +Toolkit.getDefaultToolkit().getScreenSize().height / 2 - c.getSize().height / 2);
 		}
 	}
 }
