 package com.javaop.SwingGui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FocusTraversalPolicy;
 import java.beans.PropertyVetoException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Hashtable;
 
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.UIManager;
 
 import com.javaop.util.gui.Gui;
 import com.javaop.callback_interfaces.PublicExposedFunctions;
 import com.javaop.callback_interfaces.StaticExposedFunctions;
 
 
 /**
  * Frame - Contains a list of bots' panels - Allows some means to select the
  * panels (JDesktop) - Can add bots - Create the Panel - Can remove bots -
  * Destroy the Panel - Can be told to select a specific bot - Can provide a list
  * of bots - Can provide the name of the currently selected bot
  */
 public class JavaOpFrame extends JFrame {
     private static final long    serialVersionUID  = 1L;
     
     private final Hashtable<String, JavaOpPanel>      bots
         = new Hashtable<String, JavaOpPanel>();
     
     private final JDesktopPane   desktop;
     
     private final JavaOpMainMenu menu;
     
     private int                  x                 = 1000000000;
     private int                  y                 = 1000000000;
     
     int                          minx              = 5;
     int                          miny              = 5;
     
     private final int            width             = 600;
     private final int            height            = 400;
     
     private final int            locationIncrement = 20;
     
     public JavaOpFrame(StaticExposedFunctions staticFuncs) {
         
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch(Exception e) {
             // no native look and feel? no big deal
         }
         
         // Set the program's title
        this.setTitle("JavaOp2 " + staticFuncs.getVersion());
         
         // Create the desktop pane where we'll be storing everything, and make
         // that our content pane
         this.setContentPane(desktop = new JDesktopPane());
         
         // Set my menu bar
         this.setJMenuBar(menu = new JavaOpMainMenu(staticFuncs, desktop, this));
         
         // Set the default size
         this.setSize(800, 500);
         
         // Set the colors
         desktop.setBackground(Color.BLACK);
         desktop.setForeground(Color.WHITE);
         
         // Center the frame
         Gui.center(this);
         
         // Make it visible
         this.setVisible(true);
         
         // Make it end when we close
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
     }
     
     public JavaOpPanel addBot(PublicExposedFunctions out)
     {
         // If the bot already exists, select it
         JavaOpPanel bot = (JavaOpPanel) bots.get(out.getName());
         if (bot != null) {
             // Bring it to the front
             select(out.getName());
         } else {
             MyOwnFocusTraversalPolicy policy = new MyOwnFocusTraversalPolicy();
             // Create the new panel
             bot = new JavaOpPanel(out, policy);
             // Add it to the list of bots
             bots.put(out.getName(), bot);
             // Add it to the desktop
             desktop.add(bot);
             // Give it a good size and location
             placeNext(bot);
 
             // Listen for keypresses
             // Set forwardKeys =
             // getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
             // Set newForwardKeys = new HashSet(forwardKeys);
             // newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
             // desktop.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
             // newForwardKeys);
             this.setFocusTraversalPolicy(policy);
             // .setFocusTraversalPolicyProvider(true);
             // desktop.setFocusTraversalKeysEnabled(true);
 
             // Make it visible
             bot.setVisible(true);
         }
 
         menu.updateWindowsMenu();
 
         return bot;
     }
 
     public void removeBot(String name) {
         JavaOpPanel bot = (JavaOpPanel) bots.get(name);
 
         if (bot != null) {
             bot.dispose();
             bots.remove(name);
         }
 
         menu.updateWindowsMenu();
         this.invalidate();
         this.validate();
     }
 
     public void select(String name) {
         JavaOpPanel bot = (JavaOpPanel) bots.get(name);
 
         if (bot != null)
             bot.select();
     }
 
     public String getCurrentbot() {
         return ((JavaOpPanel) desktop.getSelectedFrame()).getBotName();
     }
 
     public JavaOpPanel getBotByName(String name) {
         return (JavaOpPanel) bots.get(name);
     }
 
     public void minimizeAll() {
         JInternalFrame[] frames = desktop.getAllFrames();
         for (int i = 0; i < frames.length; i++)
             try {
                 frames[i].setIcon(true);
             } catch (PropertyVetoException e) {
             }
         ;
     }
 
     public void maximizeAll() {
         JInternalFrame[] frames = desktop.getAllFrames();
         for (int i = 0; i < frames.length; i++) {
             try {
                 frames[i].setIcon(false);
             } catch (PropertyVetoException e) {
             }
             
             try {
                 frames[i].setMaximum(true);
             } catch (PropertyVetoException e) {
             }
 
         }
     }
 
     public void cascade() {
         JInternalFrame[] frames = desktop.getAllFrames();
 
         Arrays.sort(frames, new Comparator() {
             public int compare(Object arg0, Object arg1) {
                 return ((JavaOpPanel) arg0).getTitle().compareTo(((JavaOpPanel) arg1).getTitle());
             }
         });
 
         x = 100000000;
         y = 100000000;
 
         for (int i = 0; i < frames.length; i++)
             if (frames[i].isIcon() == false)
                 placeNext((JavaOpPanel) frames[i]);
     }
 
     public void tile() {
         JInternalFrame[] frames = desktop.getAllFrames();
 
         Arrays.sort(frames, new Comparator() {
             public int compare(Object arg0, Object arg1) {
                 return ((JavaOpPanel) arg0).getTitle().compareTo(((JavaOpPanel) arg1).getTitle());
             }
         });
 
         int cols = (int) Math.sqrt(frames.length);
         int rows = (int) Math.ceil((double) ((frames.length)) / (double) cols);
 
         int width = this.getWidth() / cols;
         int height = (this.getHeight() - 55) / rows;
 
         int x = 0;
         int y = 0;
 
         for (int i = 0; i < frames.length; i++) {
             frames[i].setSize(width, height);
             frames[i].setLocation(x * width, y * height);
             x++;
             if (x >= cols)
             {
                 x = 0;
                 y++;
             }
         }
     }
 
     private void placeNext(JavaOpPanel bot) {
         // Make it a good starting size
         bot.setSize(width, height);
 
         // Find a location for it
         x += locationIncrement;
         y += locationIncrement;
 
         // Wrap x around
         if (x + width > desktop.getWidth())
             x = minx;
 
         // Wrap y around
         if (y + height > desktop.getHeight())
             y = miny;
 
         bot.setLocation(x, y);
         bot.toFront();
         bot.select();
     }
 
     private class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy {
         private JavaOpPanel findPanel(Component parent) {
             while (parent != null && parent instanceof JavaOpPanel == false)
                 parent = parent.getParent();
 
             return (JavaOpPanel) parent;
         }
 
         private JInternalFrame[] getFrames() {
             JInternalFrame[] frames = desktop.getAllFrames();
 
             Arrays.sort(frames, new Comparator() {
                 public int compare(Object arg0, Object arg1) {
                     return ((JavaOpPanel) arg0).getTitle().compareTo(
                             ((JavaOpPanel) arg1).getTitle());
                 }
             });
 
             return frames;
         }
 
         private JavaOpPanel selectFrame(JavaOpPanel panel) {
             panel.select();
             return panel;
         }
 
         public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
             JInternalFrame[] frames = getFrames();
             JavaOpPanel panel = findPanel(aComponent);
 
             if (panel == null)
                 return aComponent;
 
             for (int i = 0; i < frames.length; i++)
                 if (frames[i] == panel)
                     return selectFrame((JavaOpPanel) frames[(i + 1) % frames.length]);
 
             return getDefaultComponent(focusCycleRoot);
         }
 
         public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
             JInternalFrame[] frames = getFrames();
             JavaOpPanel panel = findPanel(aComponent);
 
             if (panel == null)
                 return aComponent;
 
             for (int i = 0; i < frames.length; i++)
                 if (frames[i] == panel)
                     return selectFrame((JavaOpPanel) frames[(i - 1 + frames.length) % frames.length]);
 
             return getDefaultComponent(focusCycleRoot);
         }
 
         public Component getDefaultComponent(Container focusCycleRoot) {
             JInternalFrame[] frames = getFrames();
 
             return frames.length > 0 ? frames[0] : focusCycleRoot;
         }
 
         public Component getLastComponent(Container focusCycleRoot) {
             JInternalFrame[] frames = getFrames();
 
             return frames.length > 0 ? frames[frames.length - 1] : focusCycleRoot;
         }
 
         public Component getFirstComponent(Container focusCycleRoot) {
             JInternalFrame[] frames = getFrames();
 
             return frames.length > 0 ? frames[0] : focusCycleRoot;
         }
     }
}
