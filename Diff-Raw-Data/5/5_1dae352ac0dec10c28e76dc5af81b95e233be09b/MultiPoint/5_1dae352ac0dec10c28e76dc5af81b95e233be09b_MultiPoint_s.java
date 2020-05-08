 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import java.util.ListIterator;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jms.JMSException;
 import javax.naming.NamingException;
 import javax.swing.*;
 
 /**
  * Class used to start or join MultiPoint presentation sessions. 
  * 
  */
 public class MultiPoint extends JApplet {
 
     /** Associated Drawing canvas */
     protected DrawingCanvas canvas;
     /** Tool list to be used */
     protected ToolList toolList;
     /** Tool bar to hold tools */
     protected ToolBarView toolBar;
     /** Menu bar to hold tools */
     protected MenuBarView menuBar;
 
     /** XML handler */
     public XMLHandler xmlHandler;
     /** Slide handler */
     public SlideHandler slideHandler;
     /** Network handler **/
     public NetworkHandler networkHandler;
     
     private static AppCloser closer; 
 
     private javax.swing.JPanel iconPanel;
     private javax.swing.JPanel canvasPanel;
     public javax.swing.JButton clearButton; 
     public javax.swing.JComboBox fillColorComboBox;
     public javax.swing.JComboBox lineColorComboBox;
     private boolean isApplet = false;
     private javax.swing.JLabel fillColorLabel;
     private javax.swing.JLabel iconLabel;
     private javax.swing.JLabel lineColorLabel;
     /** Startup window for clients to input info when program is run*/
     public StartupWindow sw;
     public boolean closeApplication = false;
     private javax.swing.JButton requestButton;
     public java.awt.ScrollPane slidePanel;
     public javax.swing.JPanel slideListPanel;
     private javax.swing.JLabel userLabel;
     private javax.swing.JPanel userList;
     private javax.swing.JButton addSlideButton;
     private javax.swing.JButton deleteSlideButton;
     private javax.swing.JComboBox fontComboBox;
     private javax.swing.JLabel fontLabel;
     private javax.swing.JLabel slideLabel;
     
     public final MultiPoint mp;
     
     // for user
     public String clientUserName; 
     public String IP;
     boolean online = false;
     // for client list
     public String owner;
     public ArrayList<String> clientList;
     public boolean isOwner;
     public String selectedUser;
     
 
     /** 
      *<Constructor>
      * Constructor for class MultiPoint (when invoked as application)
      * @param isApplet boolean variable to determine if the program is invokes as an application or applet
      */
     public MultiPoint(boolean isApplet) {
         this.isApplet = isApplet;
         
         sw = new StartupWindow(this);
         
         sw.setLocationRelativeTo(this);
         sw.setVisible(true);
         
         if (!isApplet) {
             initGUI();
         }
         mp = this;
         
         if(closeApplication == true){
             networkHandler.closeThreads();
             System.exit(0);
         }
         /////////////////////////
         // For testing ClientList
 //        clientUserName = "Matt";
 //        owner = "Matt";
 //        clientList.add(clientUserName);
         System.out.println("ClientUserName: "+clientUserName);
 //        clientList.add("Josh");
 //        clientList.add("Bhavna");
 //        clientList.add("Jean");
 //        System.out.println(clientList.toString());
         setClientList(clientList, clientUserName);
        transferOwnership();
         //////////////////////////
     }
     
     /** Constructor for class ObjectDraw (when invoked as applet)*/
     public MultiPoint(){
         this(true);
     }
     /**
      * Method to initialize GUI components 
      */
     private void initGUI() {
     
         canvas = createDrawingCanvas();
         slideHandler = createSlideHandler();
   
         xmlHandler = new XMLHandler(slideHandler);
         xmlHandler.readXMLSlideTemplates();
         toolList = createToolList();
         toolBar = createToolBarView(toolList);
         menuBar = createMenuBarView(toolList, canvas);
         canvasPanel = new javax.swing.JPanel();
         fillColorComboBox = new javax.swing.JComboBox();
         fillColorLabel = new javax.swing.JLabel();
         clearButton = new javax.swing.JButton();
         iconPanel = new javax.swing.JPanel();
         iconLabel = new javax.swing.JLabel();
         lineColorComboBox = new javax.swing.JComboBox();
         lineColorLabel = new javax.swing.JLabel();
         closer = new AppCloser();
         slideLabel = new javax.swing.JLabel();
         fontComboBox = new javax.swing.JComboBox();
         fontLabel = new javax.swing.JLabel();
         userLabel = new javax.swing.JLabel();
         userList = new javax.swing.JPanel();
         requestButton = new javax.swing.JButton();
         slidePanel = new java.awt.ScrollPane(1);  
         slidePanel.setEnabled(true);
         slideListPanel = new javax.swing.JPanel();
         addSlideButton = new javax.swing.JButton();
         deleteSlideButton = new javax.swing.JButton();
         // for client list
         clientList = new ArrayList<String>();
         isOwner = false;
 
         this.setBackground(Color.white);
 
         canvasPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         canvasPanel.setLayout(new BorderLayout());
         canvasPanel.add(canvas, BorderLayout.CENTER);
         
         toolBar.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Tools", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
         toolBar.setFloatable(false);
         toolBar.setRollover(true);
         
         fillColorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"No Fill", "Black", "Red", "Blue", "Yellow", "Green", "Orange", "Purple"})); // ITER 4
         fillColorComboBox.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 // ITER 4
                 if (canvas.getSelectedShape() == null) {
                     if ("Black".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.black);
                     } else if ("Red".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.red);
                     } else if ("Blue".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.blue);
                     } else if ("Yellow".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.yellow);
                     } else if ("Green".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.green);
                     } else if ("Orange".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.orange);
                     } else if ("Purple".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.setFillColor(Color.magenta);
                     } else {
                         canvas.setFillColor(null);
                     }
                 }
                 else{
                     if ("Black".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.black);
                         canvas.setFillColor(Color.black);
                     } else if ("Red".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.red);
                         canvas.setFillColor(Color.red);
                     } else if ("Blue".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.blue);
                         canvas.setFillColor(Color.blue);
                     } else if ("Yellow".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.yellow);
                         canvas.setFillColor(Color.yellow);
                     } else if ("Green".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.green);
                         canvas.setFillColor(Color.green);
                     } else if ("Orange".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.orange);
                         canvas.setFillColor(Color.orange);
                     } else if ("Purple".equals(fillColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setFillColor(Color.magenta);
                         canvas.setFillColor(Color.magenta);
                     } else {
                         canvas.getSelectedShape().setFillColor(null);
                         canvas.setFillColor(null);
                     }
                     canvas.clearImageBuffer();
                 }
             }
         });
         lineColorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Black", "Red", "Blue", "Yellow", "Green", "Orange", "Purple"}));
         lineColorComboBox.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 if (canvas.getSelectedShape() == null) {
                     if ("Black".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.setPenColor(Color.black);
                     } else if ("Red".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.setPenColor(Color.red);
                     } else if ("Blue".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.setPenColor(Color.blue);
                     } else if ("Yellow".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.setPenColor(Color.yellow);
                     } else if ("Green".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.setPenColor(Color.green);
                     } else if ("Orange".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.setPenColor(Color.orange);
                     } else {
                         canvas.setPenColor(Color.magenta);
                     }
                 }
                 else{
                     if ("Black".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setLineColor(Color.black);
                        
                         canvas.setPenColor(Color.black);
                     } else if ("Red".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setLineColor(Color.red);
                       
                         canvas.setPenColor(Color.red);
                     } else if ("Blue".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setLineColor(Color.blue);
                        
                         canvas.setPenColor(Color.blue);
                     } else if ("Yellow".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setLineColor(Color.yellow);
                         
                         canvas.setPenColor(Color.yellow);
                     } else if ("Green".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setLineColor(Color.green);
                         
                         canvas.setPenColor(Color.green);
                     } else if ("Orange".equals(lineColorComboBox.getSelectedItem())) {
                         canvas.getSelectedShape().setLineColor(Color.orange);
                         
                         canvas.setPenColor(Color.orange);
                     } else {
                         canvas.getSelectedShape().setLineColor(Color.magenta);
                         
                         canvas.setPenColor(Color.magenta);
                     }
                     canvas.clearImageBuffer();
                 }
             }
         });
 
         lineColorLabel.setText("Line Color:");
 
         fillColorLabel.setText("Fill Color:");
 
         clearButton.setText("Clear Slide");
         clearButton.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 // ITER 4
                 if (clearButton.getText().equals("Clear Slide")){
                     canvas.clearCanvas();
                   
                 }
                 else{
                     canvas.objectList.remove(canvas.getSelectedShape());
                     canvas.setSelectedShape(null);
                 }
                 canvas.sendXMLUpdate();
                 // ITER 4
             }
         });
         // Font sizes - Tiny = 16, Small = 20, Medium = 24, Large = 28, Extra Large = 32
         fontComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tiny", "Small", "Medium", "Large", "Extra Large" }));
         fontComboBox.setSelectedIndex(2);
         fontComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                  if (canvas.getSelectedShape() == null) {
                      
                      if ("Tiny".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(16);
                          canvas.repaint();
                          canvas.textTool.iBGraphics.drawString(canvas.textTool.text.toString(), canvas.textTool.startingPosition.x, //Removed/Changed ITER 4
                          canvas.textTool.startingPosition.y + canvas.textTool.fm.getHeight() - 11); // Changed/Removed ITER 4
                      } else if ("Small".equals(fontComboBox.getSelectedItem())) {               
                          canvas.textTool.setFontSize(20);
                          canvas.repaint();
                          canvas.textTool.iBGraphics.drawString(canvas.textTool.text.toString(), canvas.textTool.startingPosition.x, //Removed/Changed ITER 4
                          canvas.textTool.startingPosition.y + canvas.textTool.fm.getHeight() - 11); // Changed/Removed ITER 4
                      } else if ("Medium".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(24);
                          canvas.repaint();
                          canvas.textTool.iBGraphics.drawString(canvas.textTool.text.toString(), canvas.textTool.startingPosition.x, //Removed/Changed ITER 4
                          canvas.textTool.startingPosition.y + canvas.textTool.fm.getHeight() - 11); // Changed/Removed ITER 4
                      } else if ("Large".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(28);
                          canvas.repaint();
                          canvas.textTool.iBGraphics.drawString(canvas.textTool.text.toString(), canvas.textTool.startingPosition.x, //Removed/Changed ITER 4
                          canvas.textTool.startingPosition.y + canvas.textTool.fm.getHeight() - 11); // Changed/Removed ITER 4
                      } else if ("Extra Large".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(32);
                          canvas.repaint();
                          canvas.textTool.iBGraphics.drawString(canvas.textTool.text.toString(), canvas.textTool.startingPosition.x, //Removed/Changed ITER 4
                          canvas.textTool.startingPosition.y + canvas.textTool.fm.getHeight() - 11); // Changed/Removed ITER 4
                      }
                  }
                  else if (canvas.getSelectedShape().hasText){
                      if ("Tiny".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(16);
                          FontMetrics fm = canvas.getImageBufferGraphics().getFontMetrics(new Font("Serif", Font.BOLD, 16));
                          canvas.getSelectedShape().setWidth((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getWidth());
                          canvas.getSelectedShape().setHeight((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getHeight());
                          canvas.getSelectedShape().fontSize = 16;
                      } else if ("Small".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(20);
                          FontMetrics fm = canvas.getImageBufferGraphics().getFontMetrics(new Font("Serif", Font.BOLD, 20));
                          canvas.getSelectedShape().setWidth((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getWidth());
                          canvas.getSelectedShape().setHeight((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getHeight());
                          canvas.getSelectedShape().fontSize = 20;
                      } else if ("Medium".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(24);
                          FontMetrics fm = canvas.getImageBufferGraphics().getFontMetrics(new Font("Serif", Font.BOLD, 24));
                          canvas.getSelectedShape().setWidth((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getWidth());
                          canvas.getSelectedShape().setHeight((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getHeight());
                          canvas.getSelectedShape().fontSize = 24;
                      } else if ("Large".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(28);
                          FontMetrics fm = canvas.getImageBufferGraphics().getFontMetrics(new Font("Serif", Font.BOLD, 28));
                          canvas.getSelectedShape().setWidth((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getWidth());
                          canvas.getSelectedShape().setHeight((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getHeight());
                          canvas.getSelectedShape().fontSize = 28;
                      } else if ("Extra Large".equals(fontComboBox.getSelectedItem())) {
                          canvas.textTool.setFontSize(32);
                          FontMetrics fm = canvas.getImageBufferGraphics().getFontMetrics(new Font("Serif", Font.BOLD, 32));
                          canvas.getSelectedShape().setWidth((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getWidth());
                          canvas.getSelectedShape().setHeight((int)fm.getStringBounds(canvas.getSelectedShape().getText(), canvas.getImageBufferGraphics()).getHeight());
                          canvas.getSelectedShape().fontSize = 32;
                      }
                      canvas.paint(canvas.getImageBufferGraphics());
                  }
             }
         });
 
         fontLabel.setText("Font Size:");
 
         slideLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         slideLabel.setText("Slide List:");
         
         
         userLabel.setFont(new java.awt.Font("Tahoma", 1, 12));
         userLabel.setText("Users Connected:");
 
         userList.setBackground(new java.awt.Color(255, 255, 255));
         userList.setLayout(new java.awt.GridLayout(5, 1, 3, 3));
 
         requestButton.setText("Request Ownership");
         requestButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 if(!isOwner)
                     networkHandler.setOwnership("");
                 else
                     networkHandler.setOwnership(selectedUser);
             }
         });
         
         // SlideHanlder stuff
         slidePanel.setBackground(new java.awt.Color(255, 255, 255));
         slideListPanel.setLayout(new java.awt.FlowLayout());
 //        slideListPanel.add(createSlideLabel(slideHandler.addBlankSlide()), slideListPanel.getComponentCount());
         slidePanel.add(slideListPanel);
         final MultiPoint mp = this;
         addSlideButton.setText("Add Slide");
         addSlideButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 // For now add just blank slide
                 final TemplateWindow dialog = new TemplateWindow(mp);
                 dialog.addWindowListener(new java.awt.event.WindowAdapter() {
 
                     public void windowClosing(java.awt.event.WindowEvent e) {
                         
                         dialog.dispose();
                     }
                 });
                 dialog.setVisible(true);
                 
             }
         });
 
         deleteSlideButton.setText("Delete Slide");
         deleteSlideButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
 //                slideListPanel.remove(slidePanel.getComponentCount());
 //                slidePanel.add(slideListPanel);
 //                slideHandler.setSelectedSlide(slidePanel.getComponentCount());
                 int slideId = slideHandler.getSelectedSlideId();
                 if(slideId < 0){
                     slideId = slideHandler.getSlideList().size() - 1; // delete last slide if none is selected
                 }
                 
                 /* update slideStack */        
                 ArrayList<SlideClass> newSlideStack = new ArrayList<SlideClass>();
                 int slideCounter = 0;
                 for(int i = 0; i < slideHandler.getSlideList().size(); i++){
                     if(i != slideId){
                         newSlideStack.add(slideHandler.getSlide(i));
                         newSlideStack.get(slideCounter).setSlideId(slideCounter);
                         slideCounter++;
                     }
                 }
                 slideHandler.setSlideStack(newSlideStack);
                 
                 /* update slidePanel */
                 slideListPanel.removeAll();
                 for(int i = 0; i < slideHandler.getSlideList().size(); i++){
                     slideListPanel.add(updateSlideLabel(i,3));
                 }
                 slidePanel.add(slideListPanel);
                 
                 /*set selected slide to slideId - 1 */
                 if(slideId == slideHandler.getSlideList().size()){// last slide
                     slideHandler.setSelectedSlide(slideHandler.getSlideList().size() - 1);
                 }
                 else if(slideId == 0){
                     slideHandler.setSelectedSlide(0);
                 }
                 else{
                     slideHandler.setSelectedSlide(slideId-1);
                 }
                                
             }
         });
         
 
         try {
             iconLabel = new javax.swing.JLabel(getImageIcon("logo.png"));
         } catch (Exception ex) {
             Logger.getLogger(MultiPoint.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         iconPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         iconPanel.setPreferredSize(new java.awt.Dimension(119, 75));
         setJMenuBar(menuBar);
         layoutGUI();
         
         // if set to online mode then start networkHandler
         if(online)
         {
             try {
                 networkHandler = new NetworkHandler(this,clientUserName,"tcp://"+IP+":3035");
             } catch (JMSException ex) {
                 Logger.getLogger(MultiPoint.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(MultiPoint.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
     
     /**
      * Method to layout GUI components
      */
     private void layoutGUI(){
         
         
           
         javax.swing.GroupLayout iconPanelLayout = new javax.swing.GroupLayout(iconPanel);
         iconPanel.setLayout(iconPanelLayout);
         iconPanelLayout.setHorizontalGroup(
             iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(iconLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
         );
         iconPanelLayout.setVerticalGroup(
             iconPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(iconLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
         );
 
         // start new
         
       javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                 .addComponent(requestButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(userList, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                                 .addComponent(userLabel))
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(deleteSlideButton, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                                 .addComponent(addSlideButton, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                                 .addComponent(slidePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)))
                         .addGap(19, 19, 19))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(slideLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                 .addComponent(canvasPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 752, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(toolBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                     .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                     .addComponent(iconPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(fillColorComboBox, 0, 119, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(lineColorLabel)
                         .addGap(84, 84, 84))
                     .addComponent(lineColorComboBox, 0, 119, Short.MAX_VALUE)
                     .addComponent(fillColorLabel, javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(fontComboBox, 0, 119, Short.MAX_VALUE)
                     .addComponent(fontLabel, javax.swing.GroupLayout.Alignment.LEADING))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(canvasPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(userLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(userList, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(requestButton)
                         .addGap(66, 66, 66)
                         .addComponent(slideLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(slidePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                         .addComponent(addSlideButton)
                         .addGap(18, 18, 18)
                         .addComponent(deleteSlideButton)
                         .addGap(26, 26, 26))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(iconPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(30, 30, 30)
                         .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(lineColorLabel)
                         .addGap(1, 1, 1)
                         .addComponent(lineColorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(fillColorLabel)
                         .addGap(1, 1, 1)
                         .addComponent(fillColorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(fontLabel)
                         .addGap(1, 1, 1)
                         .addComponent(fontComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                         .addComponent(clearButton)
                         .addGap(15, 15, 15))))
         );
 
         
         // end new
       
     }
     
     /**
      * Method to transfer ownership and disable GUI components accordingly
      */
     public void transferOwnership(){
         isOwner = false;
         clearButton.setEnabled(false);
         fillColorComboBox.setEnabled(false);
         lineColorComboBox.setEnabled(false);
         fontComboBox.setEnabled(false);
         addSlideButton.setEnabled(false);
         deleteSlideButton.setEnabled(false);
 //        toolBar.removeAll();
 //        menuBar.removeAll();
         toolBar.setEnabled(false);
         menuBar.setEnabled(false);
         requestButton.setText("Request Ownership");
     }
     
     /**
      * Method to obtain ownership and enable GUI components accordingly
      */
     public void getOwnership(){
         isOwner = true;
         clearButton.setEnabled(true);
         fillColorComboBox.setEnabled(true);
         lineColorComboBox.setEnabled(true);
         fontComboBox.setEnabled(true);
         addSlideButton.setEnabled(true);
         deleteSlideButton.setEnabled(true);
 //        toolBar = createToolBarView(toolList);
 //        toolBar.repaint();
 //        menuBar = createMenuBarView(toolList, canvas);
 //        menuBar.repaint();
         toolBar.setEnabled(true);
         menuBar.setEnabled(true);
         requestButton.setText("Transfer Ownership");
     }
 
     public void setRequestingLabels(ArrayList<String> reqUsers)
     {
         Iterator iter = reqUsers.iterator();
         while(iter.hasNext())
             setRequestingUserLabel((String)iter.next());
        
     }
     
     public void resetRequestingLabels()
     {
         Iterator iter = clientList.iterator();
         while(iter.hasNext())
             resetRequestingUserLabel((String)iter.next());
        
     }
     
      public void setRequestingUserLabel(String userName) {
         for (int i = 0; i < clientList.size(); i++) {
             JPanel temp = ((JPanel) (userList.getComponent(i)));
             if ((clientList.get(i)).equals(userName)) {
                 JLabel tempIcon = (JLabel) (temp.getComponent(0));
                 JLabel tempLabel = (JLabel) (temp.getComponent(1));
                 tempLabel.setForeground(Color.yellow);
                 temp.removeAll();
                 temp.add(tempIcon, BorderLayout.WEST);
                 temp.add(tempLabel, BorderLayout.CENTER);
                 userList.remove(i);
                 userList.add(temp, i);
             }
         }
     }
 
     public void resetRequestingUserLabel(String userName) {
         for (int i = 0; i < clientList.size(); i++) {
             JPanel temp = ((JPanel) (userList.getComponent(i)));
             if ((clientList.get(i)).equals(userName)) {
                 JLabel tempIcon = (JLabel) (temp.getComponent(0));
                 JLabel tempLabel = (JLabel) (temp.getComponent(1));
                 tempLabel.setForeground(Color.black);
                 temp.removeAll();
                 temp.add(tempIcon, BorderLayout.WEST);
                 temp.add(tempLabel, BorderLayout.CENTER);
                 userList.remove(i);
                 userList.add(temp, i);
             }
         }
 
     }
     
     /**
      * Method to create a drawing canvas
      * @return The new DrawingCanvas object
      */
     protected DrawingCanvas createDrawingCanvas() {
         return new DrawingCanvas(this); // ITER 4
     }
 
     /**
      * Method to create a tool bar view
      * @param toolList The list of tools to include
      * @return The new ToolBarView object
      */
     protected ToolBarView createToolBarView(ToolList toolList) {
         return new ToolBarView(toolList);
     }
 
     /**
      * Method to create a menu bar view
      * @param toolList The list of tools to include
      * @param c The associated drawing canvas
      * @return The new MenuBarView object
      */
     protected MenuBarView createMenuBarView(ToolList toolList, DrawingCanvas c) {
         return new MenuBarView(toolList, c, this);
     }
 
     /**
      * Method to create an image icon from a specified file location
      * @param fileName the file location of the icon to use
      * @return The new ImageIcon
      */
     protected ImageIcon getImageIcon(String fileName) {
         URL url;
         if (isApplet) {
             try {
                 url = new URL(getCodeBase(), fileName);
             } catch (MalformedURLException e) {
                 return null;
             }
             return new ImageIcon(url);
         }// end executed as applet
         else {
             url = ClassLoader.getSystemResource(fileName);
             if(url == null)
                 return null;
             return new ImageIcon(url);
         }
     }
     
     // add clientLabel for user with slide ownership
     private JLabel createGoldIcon(){
         JLabel label = new JLabel(new ImageIcon("UserGold.png"));
         return label;
     }
 
     
     // add clientLabel for uer without ownership
     private JLabel createGreyIcon(){
         JLabel label = new JLabel(new ImageIcon("UserGrey.png"));
         return label;
     }
     
     /**
      * Updates the "Users Connected" portion of the GUI by creating custom
      * JPanels for each client in the specified clientList.
      * @param clientList the specified list of client's connected to the server
      * @param owner String containing the user name of the client currently in ownership of the presentation
      */
     public void setClientList(ArrayList<String> clientlist, String owner){
         clientList = clientlist;
         ArrayList<JPanel> clientJPanels = new ArrayList<JPanel>();
         for(int i = 0; i < clientList.size(); i++){
             JPanel panel;
             if(clientList.get(i).contentEquals(owner)){
                 panel = makeClientPanel(clientList.get(i),true);
             }else{
                 panel = makeClientPanel(clientList.get(i),false);
             }
             clientJPanels.add(panel);
         }
         
         if(userList.getComponentCount()>0)
             userList.removeAll();
 
         for(int i = 0; i < clientJPanels.size(); i++){
             userList.add(clientJPanels.get(i));
         }
         SwingUtilities.updateComponentTreeUI(userList);
 
     }
     
     /**
      * Creates a custom panel from a particular client's user name (for the "Users Connected" box)
      * @param userName the user name of the client for which this panel represents
      * @return the custom panel
      */
     private JPanel makeClientPanel(String userName, boolean owner){
         final JPanel panel = new JPanel();
         JLabel clientIcon;
         JLabel clientLabel = new JLabel();
         clientLabel.setText(userName);
         if(owner){
             clientIcon = createGoldIcon();
             clientIcon.setBorder(BorderFactory.createLineBorder(Color.black));
             clientIcon.setSize(50, 50);
             clientIcon.setPreferredSize(new Dimension(27,20));
             clientIcon.setMinimumSize(new Dimension(40,20));
         }else{
             clientIcon = createGreyIcon();
             clientIcon.setBorder(BorderFactory.createLineBorder(Color.black));
             clientIcon.setSize(50, 50);
             clientIcon.setPreferredSize(new Dimension(27,20));
             clientIcon.setMinimumSize(new Dimension(40,20));
         }
         panel.setLayout(new BorderLayout());
         panel.add(clientIcon, BorderLayout.WEST);
         panel.add(clientLabel,BorderLayout.CENTER);
         panel.setSize(100, 20);
         panel.setMinimumSize(new Dimension(100,30));
         panel.setBorder(BorderFactory.createLineBorder(Color.black));
         panel.setBackground(Color.white);
         panel.addMouseListener(new java.awt.event.MouseListener() {
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 if(isOwner){
                     JLabel temp  = (JLabel)panel.getComponent(1);
                     if(!temp.getText().contentEquals(clientUserName)){
                     selectedUser = temp.getText();
                     updateSelectedUser();
                     }
                 }
                 
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
                
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                
             }
 
             @Override
             public void mouseEntered(MouseEvent e) {
                
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                
             }
 
             
         });
 
         return panel;
     }
 
     /**
      * Method used to create a new slide button to add to the slide panel
      * This adds an action listener to allow the use to switch between slides
      * Should only take in type for icon (if we have the time)
      * @param type The template being used for the new slide
      * @return The new slide button
      */
     public JButton createSlideLabel(int type){
         JButton slideButton; 
         final int slideid;
         
         // Create blank slide
         slideid = slideHandler.addTemplateSlide(type-1);
 
 
         // Store currentslide and Load this new slide to canvas
         storeAndLoadSlide(slideid);
 
         // Set Button Name to ID so you can parse and get it back
         slideButton = new JButton(""+(slideid) );     
         // Set for an Icon if avaliable
         /*switch(type)
         {
             case 1:
                 slideButton.setIcon(new ImageIcon("TitleSlide.png"));
                 break;
 
             case 2:
                 slideButton.setIcon(new ImageIcon("BulletedSlide.png"));
                 break;
 
             case 3:
                 slideButton.setIcon(new ImageIcon("DrawingSlide.png"));
                 break;
         }*/
 
         slideButton.addActionListener(new java.awt.event.ActionListener() 
         {
             public void actionPerformed(java.awt.event.ActionEvent evt) 
             {
                 // Get slide ID from button
                 int id = Integer.parseInt(evt.getActionCommand());
 
                 // Store currentslide and Load this new slide to canvas
                 storeAndLoadSlide(id);
             }
         });
         return slideButton;
     }
     
     
     /**
      * Method to assign slide buttons to slides read from an XML file
      * @param slideId slide ID
      * @param type Slide type to assign an icon
      * @return 
      */
     public JButton updateSlideLabel(int slideId, int type){
         
         JButton slideButton = new JButton(""+(slideId) );
         
         /*switch(type)
         {
             case 1:
                 slideButton.setIcon(new ImageIcon("TitleSlide.png"));
                 break;
 
             case 2:
                 slideButton.setIcon(new ImageIcon("BulletedSlide.png"));
                 break;
 
             case 3:
                 slideButton.setIcon(new ImageIcon("DrawingSlide.png"));
                 break;
         }*/
 
         slideButton.addActionListener(new java.awt.event.ActionListener() 
         {
             public void actionPerformed(java.awt.event.ActionEvent evt) 
             {
                 // Get slide ID from button
                 int id = Integer.parseInt(evt.getActionCommand());
 
                 // Store currentslide and Load this new slide to canvas
                 storeAndLoadSlide(id);
             }
         });
         return slideButton;
     }
 
     public void updateSelectedUser(){
         for(int i = 0; i < clientList.size(); i++){
             JPanel temp = ((JPanel)(userList.getComponent(i)));
                     if((clientList.get(i)).contentEquals(selectedUser)){
                         temp.setBackground(Color.gray);
                         userList.remove(i);
                         userList.add(temp, i);
                     }
                     else{
                         temp.setBackground(Color.white);
                         userList.remove(i);
                         userList.add(temp, i);
                         
                     }
                     SwingUtilities.updateComponentTreeUI(userList);
                 }
                 System.out.println("Currently Selected User is: " + selectedUser);
     }
 
     
    /* public void mapSlidesToButtons()
     {
         ArrayList<SlideClass> slides = slideHandler.getSlideList();
         ListIterator iter = slides.listIterator();
         // Go through the slide list and check for a button mapping each slide
         while(iter.hasNext())
         {
             
             Component[] buttonList = slideListPanel.getComponents();
             SlideClass bob = (SlideClass)iter.next();
             int id = bob.getSlideId();
             boolean found = false;
             for(int i = 0; i <= buttonList.length && !found;i++)
             {
                 if(Integer.getInteger(buttonList[i].getName()) == id )
                     found = true;
             }
             if(!found)
             {
                 JButton slideButton = new JButton(""+(id) );     
     
 
                 slideButton.addActionListener(new java.awt.event.ActionListener() 
                 {   
                     public void actionPerformed(java.awt.event.ActionEvent evt) 
                     {
                         // Get slide ID from button
                         int id = Integer.parseInt(evt.getActionCommand());
 
                         // Store currentslide and Load this new slide to canvas
                         storeAndLoadSlide(id);
                     }
                 });
             }
         }
     }*/
     
     /**
      * Method that stores the current slide on drawing canvas and then 
      * uses loadSlideId to load the new slide
      * @param loadSlideId - the slide ID to load to the drawing canvas
      * 
      */
     public void storeAndLoadSlide(int loadSlideId)
     {
         slideHandler.updateSlide(canvas.getCurrentSlideId(),canvas.getObjectList());       
         // set this as new current slide in slide handler
         slideHandler.setSelectedSlide(loadSlideId);
         // Load to current slide
         canvas.setObjectList(slideHandler.getSelectedSlide().getSlideId(), 
                             slideHandler.getSelectedSlide().getObjectList());
 
     }
     
     /**
      * 
      * 
      * @return 
      */
     protected SlideHandler createSlideHandler()
     {
         SlideHandler slideHandler = new SlideHandler(canvas);
         return slideHandler;
     }
     
     /**
      * Method to create a tool list with the desired MultiPoint tools
      * @return The ToolList to be used
      */
     protected ToolList createToolList() {
         ToolList actions = new ToolList();
 
         actions.add(
                 new ToolController("Freehand", 
                 getImageIcon("FreeHandTool.png"),
                 "Freehand drawing tool",
                 canvas,
                 new FreehandTool(canvas)));
 
         //ADD NEW TOOLS HERE
 
         actions.add(
                 new ToolController("Selection", 
                 getImageIcon("SelectionTool.png"),
                 "Object Selection tool",
                 canvas,
                 new SelectionTool(canvas)
                 )
                 );
         
         actions.add(
   		new ToolController("Line",
   		getImageIcon("LineTool.png"),
   		"Line drawing tool",
   		canvas,
   		new TwoEndShapeTool(canvas, new LineShape())));
         
         actions.add(
   		new ToolController("Rectangle",
   		getImageIcon("RectangleTool.png"),
   		"Rectangle drawing tool",
   		canvas,
   		new TwoEndShapeTool(canvas, new RectangleShape())));
         
         actions.add(
   	        new ToolController("Oval",
   	        getImageIcon("OvalTool.png"),
   		"Oval drawing tool",
   		canvas,
   		new TwoEndShapeTool(canvas, new OvalShape())));
         
         actions.add(
   	        new ToolController("Circle",
   	        getImageIcon("CircleTool.png"),
   		"Circle drawing tool",
   		canvas,
   		new TwoEndShapeTool(canvas, new CircleShape())));
         
         actions.add(
   	        new ToolController("Triangle",
   	        getImageIcon("TriangleTool.png"),
   		"Triangle drawing tool",
   		canvas,
   		new TwoEndShapeTool(canvas, new TriangleShape())));
         
         actions.add(new ToolController("Text",
   		getImageIcon("TextTool.png"),
   		"Text drawing tool",
                 canvas,
   		canvas.textTool)
   		);
 
         
         return actions;
     }
 
     /**
      * Main method for MultiPoint application
      */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 JFrame frame = new JFrame();
                 MultiPoint mpnt = new MultiPoint(false);
                 frame.setTitle("MultiPoint"); // ITER 4
                 frame.getContentPane().setLayout(new BorderLayout());
                 frame.getContentPane().add(mpnt, BorderLayout.CENTER);
                 frame.addWindowListener(closer);
                 frame.pack();
                 mpnt.setVisible(true);
                 frame.setVisible(true);
                 frame.setResizable(false);
 
                 //mpnt.slideListPanel.add(mpnt.createSlideLabel(0));
 
                 mpnt.canvas.clearCanvas();
                 
             }
         });
     }
     
     static class AppCloser extends WindowAdapter  {
     public void windowClosing(WindowEvent e) {
        
        System.exit(0);
     }
   }
 }
