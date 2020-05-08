 package edu.wustl.cab2b.client.ui.mainframe;
 
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.MAIN_FRAME_TITLE;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.CAB2B_LOGO_IMAGE;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.border.Border;
 import javax.swing.text.Keymap;
 
 import org.jdesktop.swingx.JXErrorDialog;
 import org.jdesktop.swingx.JXFrame;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bComboBox;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTextField;
 import edu.wustl.cab2b.client.ui.controls.RiverLayout;
 import edu.wustl.cab2b.client.ui.util.ClientPropertyLoader;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * @author Chandrakant Talele
  * @author Hrishikesh Rajpathak
  * @author Deepak
  */
 public class LoginFrame extends JXFrame {
 
     private static final long serialVersionUID = 1L;
 
     private static final Border border = BorderFactory.createLineBorder(new Color(100, 200, 220));
 
     private static Font font = getTextFont();
 
     private static String providedUserName;
 
     private static String providedPassword;
 
     private Cab2bComboBox idProvider;
 
     private Cab2bTextField usrNameText;
 
     private JPasswordField passText;
 
     private LoginFrame selfReference = this;
 
     private JLabel credentialError;
 
     /**
      * Start of the application
      * @param args command line arguments
      */
     public static void main(String[] args) {
         if (args.length == 2) {
             providedUserName = args[0];
             providedPassword = args[1];
         }
 
         try {
             CommonUtils.setHome();
             CommonUtils.initializeResources(); // Initialize all Resources
 
             LoginFrame loginFrame = new LoginFrame();
             loginFrame.setVisible(true);
         } catch (Throwable t) {
             String msg = "Fatal error orccured while launching caB2B client.\nPlease contact administrator";
             JXErrorDialog.showDialog(null, "caB2B Fatal Error", msg, t);
             System.exit(1);
         }
     }
 
     private static Font getTextFont() {
         Cab2bLabel label = new Cab2bLabel(":");
         return new Font("calibri", Font.BOLD, label.getFont().getSize() + 2);
     }
 
     private LoginFrame() {
         super("Login - ca Bench To Bedside (B2B)");
         initUI();
     }
 
     private void initUI() {
         ImageIcon bannerImage = getImageIcon("top_img.gif");
         Cab2bLabel topImage = new Cab2bLabel(bannerImage);
         ImageIcon bgImage = getImageIcon("body_bg.gif");
 
         int totalWidth = bannerImage.getIconWidth();
         int totalHeight = bgImage.getIconHeight() + bannerImage.getIconHeight();
         Point start = getStartPosition(totalWidth, totalHeight);
 
         BackgroundImagePanel centralPanel = new BackgroundImagePanel(bgImage.getImage());
         centralPanel.setLayout(new BorderLayout());
         centralPanel.add(getLeftPanel(), BorderLayout.LINE_START);
         centralPanel.add(getRightPanel(), BorderLayout.CENTER);
 
         credentialError = new JLabel("    ");
         credentialError.setForeground(Color.RED);
         JPanel errorPanel = new JPanel(new BorderLayout());
         errorPanel.add(credentialError, BorderLayout.CENTER);
         errorPanel.add(new JLabel("   "), BorderLayout.SOUTH);
         errorPanel.setOpaque(false);
 
         centralPanel.add(errorPanel, BorderLayout.SOUTH);
 
         JPanel mainpanel = new JPanel();
         mainpanel.setLayout(new BorderLayout());
         mainpanel.add(topImage, BorderLayout.NORTH);
         mainpanel.add(centralPanel, BorderLayout.CENTER);
 
         mainpanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(145, 145, 145)));
         getContentPane().add(mainpanel);
         setIconImage(getImageIcon(CAB2B_LOGO_IMAGE).getImage());
         setLocation(start);
         setUndecorated(true);
         setSize(new Dimension(totalWidth, totalHeight));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setResizable(false);
         Toolkit.getDefaultToolkit().setDynamicLayout(true);
     }
 
     private Cab2bTextField getTextField(String text) {
         Cab2bTextField textField = new Cab2bTextField();
         textField.setBorder(border);
         textField.setText(text);
         if (providedUserName != null) {
             textField.setText(providedUserName);
         }
         return textField;
     }
 
     private JPasswordField getPasswordField() {
         JPasswordField field = new JPasswordField();
         field.setEchoChar('*');
         field.setBorder(border);
         field.setPreferredSize(new Dimension(160, 20));
         if (providedPassword != null) {
             field.setText(providedPassword);
         }
         return field;
 
     }
 
     private Point getStartPosition(int width, int height) {
         DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
         Float w = (displayMode.getWidth() - width) / 2F;
         Float h = (displayMode.getHeight() - height) / 2F;
         return new Point(w.intValue(), h.intValue());
     }
 
     private JPanel getLeftPanel() {
         ImageIcon userImage = getImageIcon("user.gif");
         Cab2bLabel userImageLabel = new Cab2bLabel(userImage);
 
         JPanel leftPanel = getTransparentPanel();
         leftPanel.setLayout(new BorderLayout(25, 0));
         leftPanel.add(new Cab2bLabel(), BorderLayout.NORTH);
         leftPanel.add(new Cab2bLabel(), BorderLayout.LINE_START);
         leftPanel.add(userImageLabel, BorderLayout.CENTER);
         leftPanel.add(new Cab2bLabel(), BorderLayout.SOUTH);
         return leftPanel;
     }
 
     private JPanel getTransparentPanel() {
         JPanel panel = new JPanel();
         panel.setOpaque(false);
         return panel;
     }
 
     private Cab2bLabel getLabel(String text) {
         Cab2bLabel userNameLabel = new Cab2bLabel(text, font);
         userNameLabel.setOpaque(false);
         return userNameLabel;
     }
 
     private void setKeyMap(JTextField txtField) {
 
         Keymap keyMap = JTextField.addKeymap("enter", txtField.getKeymap());
         KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
         keyMap.addActionForKeyStroke(key, new AbstractAction() {
             private static final long serialVersionUID = 1L;
 
             /**
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed(ActionEvent arg0) {
                 swingWorkerLogic();
             }
         });
         txtField.setKeymap(keyMap);
     }
 
     private JPanel getRightPanel() {
         Cab2bLabel userNameLabel = getLabel("User Name :");
         Cab2bLabel passWordLabel = getLabel("Password :");
         Cab2bLabel idProviderLabel = getLabel("ID Provider :");
 
         usrNameText = getTextField("");
         usrNameText.setPreferredSize(new Dimension(160, 20));
 
         passText = getPasswordField();
 
         idProvider = new Cab2bComboBox();
         idProvider.setPreferredSize(new Dimension(160, 23));
         Font idProviderFont = new Font(idProvider.getFont().getName(), Font.PLAIN,
                 idProvider.getFont().getSize() + 1);
         idProvider.setFont(idProviderFont);
         idProvider.setOpaque(false);
         idProvider.setBorder(border);
         String[] idPNames = ClientPropertyLoader.getIdPNames();
         int len = idPNames.length;
         for (int i = 0; i < len; i++) {
             idProvider.addItem(idPNames[i]);
         }
 
         ImageIcon loginImage = getImageIcon("login_button.gif");
         JButton loginButton = new JButton(loginImage);
         loginButton.setBorder(null);
         loginButton.addActionListener(new LoginButtonListener());
         loginButton.setPreferredSize(new Dimension(loginImage.getIconWidth(), loginImage.getIconHeight()));
 
         setKeyMap(usrNameText);
         setKeyMap(passText);
         idProvider.addKeyListener(new KeyListener() {
             /**
              * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
              */
             public void keyPressed(KeyEvent e) {
             }
 
             /**
              * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
              */
             public void keyReleased(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     swingWorkerLogic();
                 }
             }
 
             /**
              * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
              */
             public void keyTyped(KeyEvent e) {
             }
         });
 
         ImageIcon cancelImage = getImageIcon("cancel_button.gif");
         JButton cancleButton = new JButton(cancelImage);
         cancleButton.setBorder(null);
         cancleButton.setPreferredSize(new Dimension(cancelImage.getIconWidth(), cancelImage.getIconHeight()));
         cancleButton.addActionListener(new ActionListener() {
             /**
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed(ActionEvent e) {
                 System.exit(-1);
             }
         });
 
         JPanel containerPanel = getTransparentPanel();
         containerPanel.setLayout(new RiverLayout(10, 5));
         containerPanel.add("br", userNameLabel);
         containerPanel.add("tab", usrNameText);
         containerPanel.add("br", passWordLabel);
         containerPanel.add("tab", passText);
         containerPanel.add("br", idProviderLabel);
         containerPanel.add("tab", idProvider);
         containerPanel.add("br", new Cab2bLabel());
         containerPanel.add("br tab", loginButton);
         containerPanel.add("tab", new Cab2bLabel("|"));
         containerPanel.add("tab", cancleButton);
         Cab2bHyperlink<String> anonymousUserLink = new Cab2bHyperlink<String>(true);
         anonymousUserLink.addActionListener(new AnonymousUserButtonListener());
         anonymousUserLink.setText("Login as anonymous user");
         containerPanel.add("br tab", anonymousUserLink);
 
         JPanel panel = getTransparentPanel();
         panel.setLayout(new BorderLayout(2, 60));
         panel.add(new Cab2bLabel(), BorderLayout.NORTH);
         panel.add(containerPanel, BorderLayout.CENTER);
         return panel;
     }
 
     private ImageIcon getImageIcon(String imageName) {
         return new ImageIcon(this.getClass().getClassLoader().getResource(imageName));
     }
 
     /**
      * @author Hrishikesh Rajpathak
      */
     private class LoginButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             swingWorkerLogic();
         }
     }
 
     /**
      *  for anonymous link
      *
      */
     private class AnonymousUserButtonListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             CustomSwingWorker swingWorker = new CustomSwingWorker(LoginFrame.this) {
                 @Override
                 protected void doNonUILogic() {
                     nonUILogicAnonymusUser();
                 }
 
                 @Override
                 protected void doUIUpdateLogic() throws Exception {
                 }
             };
             swingWorker.start();
 
         }
     }
 
     private void swingWorkerLogic() {
         CustomSwingWorker swingWorker = new CustomSwingWorker(LoginFrame.this) {
             @Override
             protected void doNonUILogic() {
                 nonUILogic();
             }
 
             @Override
             protected void doUIUpdateLogic() throws Exception {
             }
         };
         swingWorker.start();
     }
 
     private void nonUILogic() {
         credentialError.setText(" ");
         final String userName = usrNameText.getText();
         char[] passwordArray = passText.getPassword();
         if (userName == "" || passwordArray.length == 0) {
             credentialError.setText("  * Please enter username and password");
             credentialError.setForeground(Color.RED);
             return;
         }
         String password = new String(passwordArray);
         final String selectedIdentityProvider = idProvider.getSelectedItem().toString();
 
         try {
             new UserValidator(userName, selectedIdentityProvider).validateUser(password);
             Thread mainThread = new Thread() {
                 public void run() {
                     launchMainFrame();
                 }
             };
             mainThread.setPriority(Thread.NORM_PRIORITY);
             selfReference.dispose();
             mainThread.start();
         } catch (RuntimeException e) {
             String message = CommonUtils.getErrorMessage(e);
            credentialError.setText("  * " + message);
             credentialError.setForeground(Color.RED);
         }
     }
 
     private void nonUILogicAnonymusUser() {
         try {
             Thread mainThread = new Thread() {
                 public void run() {
                     launchMainFrame();
                 }
             };
             mainThread.setPriority(Thread.NORM_PRIORITY);
             selfReference.dispose();
             mainThread.start();
         } catch (RuntimeException e) {
             String message = CommonUtils.getErrorMessage(e);
            credentialError.setText("  * " + message);
             credentialError.setForeground(Color.RED);
         }
 
     }
 
     /**
      * Method to launch caB2B client application.
      * 
      * @param args
      *            Command line arguments. They will not be used.
      */
     private void launchMainFrame() {
         try {
             ClientLauncher clientLauncher = ClientLauncher.getInstance();
             clientLauncher.launchClient();
 
             MainFrame mainFrame = new MainFrame(ApplicationProperties.getValue(MAIN_FRAME_TITLE), true);
             mainFrame.pack();
             mainFrame.setVisible(true);
             mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
         } catch (Throwable t) {
             String msg = "Fatal error orccured while launching caB2B client.\nPlease contact administrator";
             JXErrorDialog.showDialog(null, "caB2B Fatal Error", msg, t);
             System.exit(1);
         }
     }
 
 }
