 package ecologylab.bigsemantics.tools;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.io.Files;
 
 public class AssistApp extends WindowAdapter
 {
 
   static Logger logger = LoggerFactory.getLogger(AssistApp.class);
 
   private File  bsWrappersDir;
 
   private File  bsServiceDir;
 
   private File  bsJsDir;
 
   JButton       btnUpdate;
 
   JTextArea     textArea;
 
   JFrame        frame;
 
   AntRunner     antRunner;
 
   BSService     service;
 
   public AssistApp(Configuration configs)
   {
     createAndDisplayGUI();
     info("Initializing...");
 
     while (bsWrappersDir == null)
     {
       bsWrappersDir = PathUtil.checkAndChooseDir(frame,
                                                  configs.getString("bigsemantics_wrappers_dir"),
                                                  "BigSemanticsWrappers directory");
     }
 
     while (bsServiceDir == null)
     {
       bsServiceDir = PathUtil.checkAndChooseDir(frame,
                                                 configs.getString("bigsemantics_service_dir"),
                                                 "BigSemanticsService project directory");
     }
 
     while (bsJsDir == null)
     {
       bsJsDir = PathUtil.checkAndChooseDir(frame,
                                            configs.getString("bigsemantics_javascript_dir"),
                                            "BigSemanticsJavaScript project directory");
     }
 
     antRunner = new AntRunner();
     service = new BSService(configs);
     btnUpdate.setEnabled(true);
     info("Ready.");
   }
 
   public void createAndDisplayGUI()
   {
     JPanel contentPanel = new JPanel(new GridBagLayout());
     contentPanel.setOpaque(true);
 
     btnUpdate = new JButton("Update Backend with New Wrappers");
     btnUpdate.setEnabled(false);
     btnUpdate.setVerticalTextPosition(AbstractButton.CENTER);
     btnUpdate.setHorizontalTextPosition(AbstractButton.CENTER);
     btnUpdate.addActionListener(new ActionListener()
     {
       @Override
       public void actionPerformed(ActionEvent event)
       {
         btnUpdate.setEnabled(false);
         ExecutorService executor = Executors.newSingleThreadExecutor();
         executor.execute(new Runnable()
         {
           @Override
           public void run()
           {
             updateBackend();
             SwingUtilities.invokeLater(new Runnable()
             {
               @Override
               public void run()
               {
                 btnUpdate.setEnabled(true);
               }
             });
           }
         });
         executor.shutdown();
       }
     });
 
     GridBagConstraints c =
         new GridBagConstraints(0, // gridx
                                0, // gridy
                                1, // gridwidth
                                1, // gridheight
                                0, // weightx
                                0, // weighty
                                GridBagConstraints.CENTER, // anchor
                                GridBagConstraints.NONE, // fill
                                new Insets(8, 8, 8, 8), // insets
                                8, // ipadx
                                8); // ipady
     contentPanel.add(btnUpdate, c);
 
     textArea = new JTextArea(40, 100);
     textArea.setEditable(false);
     textArea.setBorder(BorderFactory.createLoweredBevelBorder());
     textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
     JScrollPane scroll = new JScrollPane(textArea);
     scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
     c.gridx = 0;
     c.gridy = 1;
     c.weightx = 1;
     c.weighty = 1;
     c.fill = GridBagConstraints.BOTH;
     contentPanel.add(scroll, c);
 
     frame = new JFrame("BS Wrapper Dev Assist");
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.addWindowListener(new WindowAdapter()
     {
       @Override
       public void windowClosing(WindowEvent event)
       {
         info("Window is closing, cleaning up ...");
         try
         {
           stopService();
         }
         catch (Exception e)
         {
           error("Error stopping BS service.", null, e);
         }
       }
 
     });
     frame.setContentPane(contentPanel);
 
     javax.swing.SwingUtilities.invokeLater(new Runnable()
     {
       public void run()
       {
         frame.pack();
         frame.setVisible(true);
       }
     });
   }
 
   private void updateBackend()
   {
     try
     {
       stopService();
 
       info("Recompiling wrappers...");
       antRunner.runAntTarget(PathUtil.subPath(bsWrappersDir, "build.xml"), "compile-wrappers-java");
 
       info("Updating dependencies in the service project...");
       File wrappersJar =
           PathUtil.subPath(bsWrappersDir, "build", "jar", "BigSemanticsWrappers.jar");
       File destWrappersJar =
           PathUtil.subPath(bsServiceDir, "lib", "BigSemanticsWrappers.jar");
       Files.copy(wrappersJar, destWrappersJar);
 
       File metadataJar =
           PathUtil.subPath(bsWrappersDir.getParentFile(), "BigSemanticsGeneratedClassesJava",
                            "build", "jar", "BigSemanticsGeneratedClassesJava.jar");
       File destMetadataJar =
           PathUtil.subPath(bsServiceDir, "lib", "BigSemanticsGeneratedClassesJava.jar");
       Files.copy(metadataJar, destMetadataJar);
 
       info("Rebuilding service war...");
       File serviceBuildFile =
           PathUtil.subPath(bsServiceDir, "BigSemanticsService", "build", "build.xml");
       antRunner.runAntTarget(serviceBuildFile, "buildwar");
 
       startService();
     }
     catch (Exception e)
     {
       error("Error relaunching BS service.", null, e);
       return;
     }
 
     info("Service started, running.");
   }
 
   private void stopService() throws Exception
   {
     info("Stopping service...");
     service.stop();
   }
 
   private void startService() throws Exception
   {
     info("Starting service...");
     service.start();
   }
 
   private void infoHelper(String msg)
   {
     logger.info(msg);
     textArea.append(msg + "\n");
     textArea.setCaretPosition(textArea.getDocument().getLength());
   }
 
   private void info(String msg)
   {
     infoHelper(msg);
     SwingUtilities.invokeLater(new Runnable()
     {
       @Override
       public void run()
       {
         frame.invalidate();
       }
     });
   }
 
   private void error(String msg, String info, Throwable t)
   {
     if (t != null)
     {
       logger.error(msg, t);
     }
     if (info != null)
     {
       String[] lines = info.split("\n");
       for (String line : lines)
       {
         infoHelper("    >> " + line);
       }
     }
     info("ERROR: " + msg);
     info("Check for the log file for more details.");
   }
 
   public static void main(String[] args) throws ConfigurationException
   {
     Configuration configs = new PropertiesConfiguration("wrapper-dev-assist.conf");
     new AssistApp(configs);
   }
 
 }
