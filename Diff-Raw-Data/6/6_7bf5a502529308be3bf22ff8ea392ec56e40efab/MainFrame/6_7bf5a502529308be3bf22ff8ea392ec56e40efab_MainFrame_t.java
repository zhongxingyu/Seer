 package view;
 
 import controller.*;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 /*
 class SoundPlayer {
     private AudioInputStream audioInputStream;
     private Clip clip;
 
     SoundPlayer(String url) {
         try {
             audioInputStream = AudioSystem.getAudioInputStream(new FileInputStream(url));
             clip = AudioSystem.getClip();
         } catch (LineUnavailableException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (UnsupportedAudioFileException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     //public static synchronized void playSound(final String url) {
     public synchronized void playSound() {
         new Thread(new Runnable() {
             public void run() {
                 try {
                     Clip clip = AudioSystem.getClip();
                     //AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(MainFrame.class.getResourceAsStream("/path/to/sounds/" + url));
                     clip.open(audioInputStream);
                     clip.start();
                 } catch (Exception e) {
                     System.err.println(e.getMessage());
                 }
             }
         }).start();
     }
 
     public void stopSound() {
         clip.stop();
     }
 
     public void resumeSound() {
         clip.start();
     }
 }
 */
 
 public class MainFrame {
     private static final boolean OPAQUE = false;
     private int currentHeight = 0;
     private int currentWidth = 0;
     private String textOfQuestion = "\n\n\nType your question or query here and click \"send\" to receive a consultation";
     //private int lectureNumber = 1;
     //private int slideNumber = 1;
 
     private String news = "<h2>Latest News</h2>\t\t<h3>February 1, 2012 &nbsp;&nbsp;&nbsp;&nbsp; </h3>\n" +
             "\t\t<a href=\"http://cuda-course.eventbrite.com/\">5-8 february On-site training and Consultancy and a 4 day advanced CUDA course at  Irish Supercomputing Center ICHEC, Dublin</a>\n" +
             "\n" +
             "\t\t<br/><br/>\n" +
             "\n" +
             "\t\t<h3>January 16, 2012 &nbsp;&nbsp;&nbsp;&nbsp; </h3>\n" +
             "\t\t<a href=\"http://parallel-compute.com/education/plan\">January 16-18, 2012. A three day CUDA course at the Technical University of Munich. Germany.</a>\n" +
             "\t\t\n" +
             "\t\t<br/><br/>\n" +
             "\t\n" +
             "\t\t\n" +
             "\t\t<h3>December 12, 2011 &nbsp;&nbsp;&nbsp;&nbsp;</h3>\n" +
             "\t\tGPU Computing and CUDA.&nbsp;Supercomputing winter school at MSU for \"T-Platforms'\n" +
             "\t\t\n" +
             "\t";
 
     private controller.Controller controller;
 
     public MainFrame(Controller controller) {
         this.controller = controller;
     }
 
     private void paintGradient(Graphics g, JPanel panel) {
         //UIDefaults uid = UIManager.getDefaults();
         Graphics2D g2d = (Graphics2D) g;
         Dimension d = panel.getSize();
 
         //g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, d.height, Color.BLACK, true));
         java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(d.width / 2, d.height / 2);
         float radius = 2 * Math.min(d.width, d.height);
         float[] dist = {0.0f, 1.0f};
         Color[] colors = {new Color(0.0f, 0.0f, 0.0f, 0.0f), Color.BLACK};
         RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
         g2d.setPaint(p);
         g2d.fillRect(0, 0, d.width, d.height);
     }
 
     /*
     private void postMail(String text) {
         //private void postMail(String recipients[], String subject, String message, String from) {
         String[] sendTo = {"edu.cuda@parallel-compute.com"};
         //String emailMsgTxt = "Test Message Contents";
         String emailMsgTxt = text;
         String emailSubjectTxt = "A test from gmail";
         String emailFromAddress = "alexander.myltsev@gmail.com";
 
         Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
 
         try {
             new GoogleMailer().sendSSLMessage(sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
             System.out.println("Sucessfully Sent mail to All Users");
         } catch (MessagingException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
     */
 
     private JScrollPane createTextAreaScroll(int rows, int cols, boolean hasVerScroll) {
         final String text = "\n\n\nType your question or query here and click \"send\" to receive a consultation";
         final JTextArea ta = new JTextArea(textOfQuestion, rows, cols);
         ta.setFont(UIManager.getFont("TextField.font"));
         ta.setWrapStyleWord(true);
         ta.setLineWrap(true);
 
         ta.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 textOfQuestion = ta.getText();
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 textOfQuestion = ta.getText();
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 textOfQuestion = ta.getText();
             }
         });
 
         ta.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseEntered(MouseEvent e) {
                 if (ta.getText().equals(text)) {
                     ta.setText("");
                 }
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                 if (ta.getText().equals("")) {
                     ta.setText(text);
                     ta.setCaretPosition(0);
                 }
             }
         });
 
         JScrollPane scroll = new JScrollPane(
                 ta,
                 hasVerScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
         return scroll;
     }
 
     private ArrayList<JButton> getSocialButtons() {
         String[][] pathsToIcons = {
                 {"resource/rss.png", "rss-clicked"},
                 {"resource/youtube.png", "youtube-clicked"},
                 {"resource/facebook.png", "facebook-clicked"},
                 {"resource/linkedin.png", "linkedin-clicked"},
                 {"resource/twitter.png", "twitter-clicked"}
         };
         ArrayList<JButton> buttons = new ArrayList<JButton>();
         ActionListener socialButtonsEventListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //System.out.println(e.toString());
                 try {
                     URI u;
                     if ("rss-clicked".equals(e.getActionCommand()))
                         u = new URI("http://parallel-compute.com/news/");
                     else if ("facebook-clicked".equals(e.getActionCommand())) u = new URI("http://facebook.com");
                     else if ("linkedin-clicked".equals(e.getActionCommand())) u = new URI("http://linkedin.com");
                     else if ("twitter-clicked".equals(e.getActionCommand())) u = new URI("http://twitter.com");
                     else if ("youtube-clicked".equals(e.getActionCommand())) u = new URI("http://youtube.com");
                     else u = new URI("http://parallel-compute.com");
                     Desktop.getDesktop().browse(u);
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace(); // TODO: Process the error correctly.
                 } catch (IOException e1) {
                     e1.printStackTrace(); // TODO: Process the error correctly.
                 }
             }
         };
         for (int i = 0; i < pathsToIcons.length; i++) {
             JButton button = new JButton("", new ImageIcon(pathsToIcons[i][0]));
             button.addActionListener(socialButtonsEventListener);
             button.setActionCommand(pathsToIcons[i][1]);
             buttons.add(button);
         }
         return buttons;
     }
 
     class LectureButtonListener implements ActionListener {
         private JImagePanel lectureContentViewer;
 
         LectureButtonListener(JImagePanel lectureContentViewer) {
             this.lectureContentViewer = lectureContentViewer;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             //lectureNumber = Integer.parseInt(e.getActionCommand());
             //String path = "resource/Lectures/Lecture" + lectureNumber + "/Slide1.PNG";
             //lectureContentViewer.updateImage(path, true);
 
             int lectNum = Integer.parseInt(e.getActionCommand());
             SelectLectureCmd command = new SelectLectureCmd(lectNum);
             controller.executeCommand(command);
             LectureDescription lectureDescription = command.lectureDescription();
             lectureContentViewer.updateImage(lectureDescription.content(), true);
         }
     }
 
     private JPanel createLectureSelectorPanel() {
         final JPanel panel = new JPanel(new MigLayout(
                 "insets 10",
                 "[grow,fill][]",
                 "[grow,fill][]"), true
         ) {
             @Override
             protected void paintComponent(Graphics g) {
                 paintGradient(g, this);
             }
         };
 
         GetCurrentLectureCmd getCurrentLectureCmd = new GetCurrentLectureCmd();
         controller.executeCommand(getCurrentLectureCmd);
         JImagePanel lectureContentViewer = new JImagePanel(getCurrentLectureCmd.content().content(), true);
         lectureContentViewer.setCursor(new Cursor(Cursor.HAND_CURSOR));
         lectureContentViewer.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 System.out.println("Mouse clicked");
                 JPanel slidesSelectorPanel = createSlidesSelectorPanel();
                 frame.setVisible(false);
                 frame.setContentPane(slidesSelectorPanel);
                 frame.pack();
                 frame.setSize(currentWidth, currentHeight);
                 frame.setVisible(true);
             }
         });
         panel.add(lectureContentViewer, "cell 0 0");
 
         panel.add(createBanner(), "w 200!, h 100!, flowy, cell 1 0");
         panel.add(createNewsPanel(news, true), "w 200!, h 200!,flowy, cell 1 0");
         panel.add(new JLabel("Ask a question"), "gapx push, cell 1 0");
         panel.add(createTextAreaScroll(4, 30, true), "w 200!, flowy, grow, cell 1 0");
         panel.add(createSendButton(), "gapx push, gapy 0px, cell 1 0"); // pos visual.x2-pref visual.y2-pref-35
 
         LectureButtonListener lectureButtonActionListener = new LectureButtonListener(lectureContentViewer);
         for (int i = 1; i <= 4; i++) {
             JButton lectureButton = new JButton("Lecture " + i);
             lectureButton.setActionCommand("" + i);
             lectureButton.addActionListener(lectureButtonActionListener);
             panel.add(lectureButton, "cell 0 1,h 35!,flowx");
         }
 
         ArrayList<JButton> socialButtons = getSocialButtons();
         for (int i = 0; i < socialButtons.size(); i++) {
             panel.add(socialButtons.get(i), "gapx push,gapy push,w 35!,h 35!,cell 1 1");
         }
 
         return panel;
     }
 
     private JButton createSendButton() {
         JButton sendButton = new JButton("Send");
         sendButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //postMail(textOfQuestion);
                 System.out.println("!!! NOT IMPLEMENTED !!!");
             }
         });
         return sendButton;
     }
 
     private JScrollPane createNewsPanel(String news, boolean hasVerScroll) {
         JEditorPane ep = new JEditorPane();
         ep.setContentType("text/html");
         ep.setText(news);
         ep.setEditable(false);
         ep.addHyperlinkListener(new HyperlinkListener() {
             @Override
             public void hyperlinkUpdate(HyperlinkEvent e) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 //System.out.println(e);
 
                 try {
                     if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                         Desktop.getDesktop().browse(e.getURL().toURI());
                 } catch (IOException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         ep.setCaretPosition(0);
 
         JScrollPane scroll = new JScrollPane(
                 ep,
                 hasVerScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         return scroll;
     }
 
     private JPanel createSlidesSelectorPanel() {
         JPanel panel = new JPanel(new MigLayout(
                 "insets 10",
                 "[][grow,fill][]",
                 "[grow,fill][]"), true) {
             @Override
             protected void paintComponent(Graphics g) {
                 paintGradient(g, this);
             }
         };
 
         GetCurrentSlideCmd getCurrentSlideCmd = new GetCurrentSlideCmd();
         controller.executeCommand(getCurrentSlideCmd);
         final JImagePanel slideSelectorPanel = new JImagePanel(getCurrentSlideCmd.content().content(), false);
 
         for (int i = 1; i < 10; i++) {
             JButton slideButton = new JButton("Slide " + i);
             final int slideNum = i;
             slideButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     // TODO: FIX IT
                     //slideNumber = slideNum;
                     //String path = "resource/Lectures/Lecture" + 1 + "/Slide" + 1 + ".PNG";
                    SelectSlideCmd selectSlideCmd = new SelectSlideCmd(slideNum);
                    controller.executeCommand(selectSlideCmd);
                    slideSelectorPanel.updateImage(selectSlideCmd.slideInfo().content(), false);
                 }
             });
             panel.add(slideButton, "cell 0 0,flowy,sg g1,w 80!");
         }
 
         panel.add(slideSelectorPanel, "grow");
 
         panel.add(createBanner(), "w 200!, h 100!, cell 2 0,flowy");
         panel.add(createNewsPanel(news, true), "w 200!, h 200!, grow, cell 2 0,flowy");
         panel.add(new JLabel("Ask a question"), "gapx push, cell 2 0");
         panel.add(createTextAreaScroll(4, 30, true), "w 200!, grow, cell 2 0,flowy");
         panel.add(createSendButton(), "cell 2 0, gapx push, gapy 0px");
 
         final JButton playPauseButton = new JButton("||");
         playPauseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (playPauseButton.getText().equals("||")) {
                     playPauseButton.setText("|>");
                 } else {
                     playPauseButton.setText("||");
                 }
             }
         });
         panel.add(playPauseButton, "cell 1 1,w 50!,h 35!");
         panel.add(new JSlider(0, 100, 0), "cell 1 1,growx 90,h 35!");
         //panel.add(new JButton("Mute"), "cell 1 1,w 65!");
         panel.add(new JLabel(new ImageIcon("resource/volume+icon.jpg")), "cell 1 1,w 30!,h 35!");
         JSlider jSlider = new JSlider(0, 100, 100);
         panel.add(jSlider, "cell 1 1,w 100!,h 35!");
 
         JButton backToLectureSelectionButton = new JButton("<html><p style=\"text-align: center;\">Lecture<br/>selection</p></html>");
         backToLectureSelectionButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Back to lecture selection is clicked");
                 JPanel lectureSelectorPanel = createLectureSelectorPanel();
                 frame.setVisible(false);
                 frame.setContentPane(lectureSelectorPanel);
                 frame.pack();
                 frame.setSize(currentWidth, currentHeight);
                 frame.setVisible(true);
             }
         });
         panel.add(backToLectureSelectionButton, "w 80!,h 35!,cell 0 1");
 
         ArrayList<JButton> socialButtons = getSocialButtons();
         for (int i = 0; i < socialButtons.size(); i++) {
             panel.add(socialButtons.get(i), "gapx push,gapy push,w 35!,h 35!,cell 2 1");
         }
 
         return panel;
     }
 
     private JLabel createBanner() {
         JLabel banner = new JLabel(new ImageIcon("resource/TESLA_200x100.jpg"));
         banner.setCursor(new Cursor(Cursor.HAND_CURSOR));
         banner.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 try {
                     Desktop.getDesktop().browse(new URI("http://www.nvidia.com/object/workstation-solutions-tesla.html"));
                 } catch (IOException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         return banner;
     }
 
     private static void setUIFont(javax.swing.plaf.FontUIResource f) {
         java.util.Enumeration keys = UIManager.getDefaults().keys();
         while (keys.hasMoreElements()) {
             Object key = keys.nextElement();
             Object value = UIManager.get(key);
             if (value instanceof javax.swing.plaf.FontUIResource) {
                 UIManager.put(key, f);
             }
         }
     }
 
     JFrame frame = new JFrame("CourseGUI");
 
     public static void launch(final Controller controller) {
         Font globalFont = new Font("Tahoma", Font.PLAIN, 12);
         setUIFont(new javax.swing.plaf.FontUIResource(globalFont));
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
 
                 Toolkit toolkit = Toolkit.getDefaultToolkit();
                 Dimension dimension = toolkit.getScreenSize();
                 System.out.println("Width: " + dimension.width);
                 System.out.println("Height: " + dimension.height);
 
                 final MainFrame mainFrame = new MainFrame(controller);
 
                 mainFrame.currentHeight = (int) (dimension.height * 0.8);
                 mainFrame.currentWidth = (int) (dimension.width * 0.8);
 
                 int startX = (dimension.width - mainFrame.currentWidth) / 2;
                 int startY = (dimension.height - mainFrame.currentHeight) / 2;
 
                 //JFrame frame = new JFrame("CourseGUI");
                 mainFrame.frame.getContentPane().add(mainFrame.createLectureSelectorPanel());
                 //mainFrame.frame.getContentPane().add(mainFrame.createStartPanel());
                 mainFrame.frame.pack();
                 mainFrame.frame.setSize(mainFrame.currentWidth, mainFrame.currentHeight);
                 mainFrame.frame.setLocation(startX, startY);
                 mainFrame.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                 mainFrame.frame.setVisible(true);
 
                 mainFrame.frame.addComponentListener(new java.awt.event.ComponentAdapter() {
                     public void componentResized(ComponentEvent event) {
                         mainFrame.currentHeight = mainFrame.frame.getHeight();
                         mainFrame.currentWidth = mainFrame.frame.getWidth();
                         //mainFrame.currentHeight = Math.max(600, mainFrame.frame.getHeight());
                         //mainFrame.currentWidth = Math.max(1200, mainFrame.frame.getWidth());
                         //mainFrame.frame.setSize(mainFrame.currentWidth, mainFrame.currentHeight);
                     }
                 });
             }
         });
     }
 
     private JPanel createStartPanel() {
         JPanel mainPanel = new JPanel(new MigLayout()) {
             @Override
             protected void paintComponent(Graphics g) {
                 paintGradient(g, this);
             }
         };
 
         //mainPanel.add(new JTextField("Login"), "wrap, w 200!");
         //mainPanel.add(new JTextField("String to send"), "wrap, w 200!");
         //mainPanel.add(new JTextField("Your string"), "wrap, w 200!");
         JButton registrationButton = new JButton("Pass registration");
         registrationButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 System.out.println("Pass registration is clicked");
                 JPanel lectureSelectorPanel = createLectureSelectorPanel();
                 frame.setVisible(false);
                 frame.setContentPane(lectureSelectorPanel);
                 frame.pack();
                 frame.setSize(currentWidth, currentHeight);
                 frame.setVisible(true);
             }
         });
         mainPanel.add(registrationButton);
 
         return mainPanel;
     }
 }
