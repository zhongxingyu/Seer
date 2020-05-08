 import javax.swing.JTabbedPane;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JFrame;
 import javax.swing.JComponent;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 
 
 public class DataBasePanel extends JPanel {
   /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 public DataBasePanel() {
     super(new GridLayout(1, 1));
     
 
     JTabbedPane tabbedPane = new JTabbedPane();
     OverviewFilter tableFilter = new OverviewFilter();
     
     
    ImageIcon icon1 = createImageIcon("/images/magnifier.jpg");
    ImageIcon icon2 = createImageIcon("/images/books.png");
    ImageIcon icon3 = createImageIcon("/images/author.jpg");
    ImageIcon icon4 = createImageIcon("/images/publisher.png");
    ImageIcon icon5 = createImageIcon("/images/borrower.gif");
    ImageIcon icon6 = createImageIcon("/images/shelf.jpg");
     
 
 
     tabbedPane.addTab("Overview", icon1, tableFilter, "Does nothing");
     tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
 
     JComponent panel2 = makeTextPanel("Books");
     tabbedPane.addTab("Books", icon2, panel2, "Does twice as much nothing");
     tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
 
     JComponent panel3 = makeTextPanel("Authors");
     tabbedPane.addTab("Authors", icon3, panel3, "Still does nothing");
     
     tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
 
     JComponent panel4 = makeTextPanel("Publisher");
     panel4.setPreferredSize(new Dimension(410, 50));
     tabbedPane.addTab("Publisher", icon4, panel4, "Does nothing at all");
     tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
     
     JComponent panel5 = makeTextPanel("Borrower");
     panel4.setPreferredSize(new Dimension(410, 50));
     tabbedPane.addTab("Borrower", icon5, panel5, "Does nothing at all");
     tabbedPane.setMnemonicAt(4, KeyEvent.VK_5);
     
     JComponent panel6 = makeTextPanel("Shelfs");
     panel4.setPreferredSize(new Dimension(410, 50));
     tabbedPane.addTab("Shelfs", icon6, panel6, "Does nothing at all");
     tabbedPane.setMnemonicAt(5, KeyEvent.VK_6);
 
     //Add the tabbed pane to this panel.
     add(tabbedPane);
 
     //Uncomment the following line to use scrolling tabs.
     //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
   }
 
   protected JComponent makeTextPanel(String text) {
     JPanel panel = new JPanel(false);
     JLabel filler = new JLabel(text);
     filler.setHorizontalAlignment(JLabel.CENTER);
     panel.setLayout(new GridLayout(1, 1));
     panel.add(filler);
     return panel;
   }
 
   /** Returns an ImageIcon, or null if the path was invalid. */
   protected static ImageIcon createImageIcon(String path) {
     java.net.URL imgURL = DataBasePanel.class.getResource(path);
     if (imgURL != null) {
       return new ImageIcon(imgURL);
     } else {
       System.err.println("Couldn't find file: " + path);
       return null;
     }
   }
 
   /**
    * Create the GUI and show it. For thread safety, this method should be
    * invoked from the event-dispatching thread.
    */
   private static void createAndShowGUI() {
     //Make sure we have nice window decorations.
 	  JFrame.setDefaultLookAndFeelDecorated(true);
 	  
 	 // Added by Jufi
 	 int w = Toolkit.getDefaultToolkit().getScreenSize().width;
 	 int h = Toolkit.getDefaultToolkit().getScreenSize().height;
 	  
 	
 	  
 	  
   
 
     //Create and set up the window.
     JFrame frame = new JFrame("TabbedPaneDemo");
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     
   
     //Set Screensize
     frame.setExtendedState(JFrame.NORMAL);
     frame.setSize( w, h );
     frame.validate();
  
     //Create and set up the content pane.
     JComponent newContentPane = new DataBasePanel();
     newContentPane.setOpaque(true); //content panes must be opaque
     frame.getContentPane().add(new DataBasePanel(), BorderLayout.CENTER);
 
     //Display the window.
     //frame.pack();
     frame.setVisible(true);
   }
 
   public static void main(String[] args) {
     //Schedule a job for the event-dispatching thread:
     //creating and showing this application's GUI.
     javax.swing.SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         createAndShowGUI();
       }
     });
   }
 }
