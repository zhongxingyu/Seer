 package experimental;
 
 import experimental.chess.Board;
 import experimental.info.InfoPanel;
 import experimental.sweeper.Sweeper;
 import pl.eurekin.editor.LineDefinitionEditorView;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.io.IOException;
 
 import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
 
 /**
  * @author gmatoga
  */
 public class ShowOffApplication {
 
     private JFrame frame;
     private JPanel mainPanel;
     private JTabbedPane tabbedPane;
 
     public static void main(String... args) throws Exception {
 
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(-2, 1, 1, 1));
 //        UIManager.getDefaults().put("TabbedPane.selectedTabPadInsets", new Insets(-2,1,1,1));
 //        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(10,10,10,10));
         UIManager.getDefaults().remove("TabbedPane.contentAreaColor");
 //        UIManager.getDefaults().put("TabbedPane.contentAreaColor", Color.YELLOW);
 
 
         final ShowOffApplication showOffApplication = new ShowOffApplication();
         showOffApplication.safelyShowGUIfromAnyThread();
 
         if (System.getProperty("os.version").equalsIgnoreCase("6.1")) try {
            JNATest.tryReallyHardToSetWindowsTaskbarProperties(showOffApplication.frame);
         } catch (Exception e) {
             System.err.println("Exception prevented IPropertyStorage configuration. Continuing despite error.");
             e.printStackTrace();
         }
 
     }
 
     private void safelyShowGUIfromAnyThread() {
         final Runnable showGUIRunnable = new Runnable() {
             @Override
             public void run() {
                 initiateAndShowOnEDT();
             }
         };
         if (SwingUtilities.isEventDispatchThread()) showGUIRunnable.run();
         else try {
             EventQueue.invokeAndWait(showGUIRunnable);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void initiateAndShowOnEDT() {
 
         constructMainFrame();
         addMainPanel();
         showMainFrame();
     }
 
     private void addMainPanel() {
         constructMainPanel();
         frame.add(mainPanel);
         frame.setResizable(false);
         frame.pack();
         frame.setLocationRelativeTo(null);
     }
 
     private void constructMainPanel() {
         mainPanel = new JPanel();
         constructTabbedFrame();
         mainPanel.add(tabbedPane);
     }
 
     private void constructTabbedFrame() {
         tabbedPane = new JTabbedPane();
         tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
 
         addAllRegisteredShowOffViewsToJTabbedPane();
     }
 
     private void addAllRegisteredShowOffViewsToJTabbedPane() {
         final LineDefinitionEditorView lineDefinitionEditorView = new LineDefinitionEditorView();
         lineDefinitionEditorView.initialize();
         Icon icon1 = null;
         try {
             icon1 = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/table.png")));
         } catch (IOException e) {
 
         }
         tabbedPane.addTab("Table", icon1, lineDefinitionEditorView.panel1);
 
         Sweeper sweeper = new Sweeper(20, 40);
         final JPanel sweeperFrame = sweeper.getMainPanel();
         Icon sweeperIcon = sweeper.getIcon();
         tabbedPane.addTab("MineSweeper", sweeperIcon, sweeperFrame);
 
 
         final JPanel construct = new Board().construct();
         final JPanel outerPanel = new JPanel(new GridBagLayout());
         outerPanel.add(construct);
         Icon icon = null;
         try {
             icon = new ImageIcon(ImageIO.read(this.getClass().getResourceAsStream("/chess.png")));
         } catch (IOException e) {
 
         }
         tabbedPane.addTab("Chess", icon, outerPanel);
 
         final JPanel panel = new InfoPanel().getPanel();
         tabbedPane.addTab("About", panel);
 
 
     }
 
     private void showMainFrame() {
         frame.setVisible(true);
     }
 
     private JFrame constructMainFrame() {
         frame = new JFrame("Experimental show-off");
         try {
             frame.setIconImage(ImageIO.read(this.getClass().getResource("/ico_lab.png")));
         } catch (IOException e) {
             e.printStackTrace();
         }
         frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         frame.pack();
         return frame;
     }
 }
