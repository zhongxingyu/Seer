 package net.grinder.console.swingui;
 
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.net.URL;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 /**
  * TestImage.java
  *
 *
 * Created: Mon Jul 30 06:58:37 2001
 *
 * @author <a href="mailto: "Philip Aston</a>
 * @version
  */

 public class TestImage{
     
     public static void main(String[] args)
     {
 	new TestImage();
     }
 
     public TestImage()
     {
 	final JFrame frame = new JFrame("Test images");
 	final Container topLevelPane = frame.getContentPane();
 	topLevelPane.setLayout(new GridLayout());
 
 	final String images[] = {
 	    "grinder.gif",
 	    "start.gif",
 	    "stop.gif",
 	    "start-processes.gif",
 	    "stop-processes.gif",
 	    "summary.gif",
 	    "save.gif",
 	    "pause.gif",
 	    "reset.gif",
 	};
 
 	for (int i=0; i<images.length; i++) {
 	    final ImageIcon logoIcon = getImageIcon(images[i]);
 
 	    if (logoIcon != null) {
 		final JLabel logo = new JLabel(logoIcon);
 		topLevelPane.add(logo);
 	    }
 	}
 
 	frame.addWindowListener(new WindowCloseAdapter());
 	frame.pack();
 	frame.show();
     }
 
     private URL getResource(String name, boolean warnIfMissing)
     {
 	final URL url = this.getClass().getResource("resources/" + name);
 
 	if (warnIfMissing && url == null) {
 	    System.err.println("Warning - could not load resource " + name);
 	}
 
 	return url;
     }
     
     private ImageIcon getImageIcon(String resourceName)
     {
 	final URL resource = getResource(resourceName, false);
 
 	return resource != null ? new ImageIcon(resource) : null;
     }
 
     private static final class WindowCloseAdapter extends WindowAdapter
     {
 	public void windowClosing(WindowEvent e)
 	{
 	    System.exit(0);
 	}
     }
 } // TestImage
