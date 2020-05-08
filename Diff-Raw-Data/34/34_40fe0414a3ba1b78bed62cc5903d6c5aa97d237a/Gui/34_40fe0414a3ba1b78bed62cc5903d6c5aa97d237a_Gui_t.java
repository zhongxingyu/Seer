 package gui;
 
 import java.awt.Component;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.EventObject;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.text.DefaultCaret;
 
 import config.Configurations;
 import logging.Logger;
 import logging.Message;
 import events.EventListener;
 import gui.inputs.DefaultUsernameInput;
 import network.Client;
 
 @SuppressWarnings("serial")
 public class Gui extends JFrame  {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8652766380073376172L;
 	private Logger _logger;
 	private boolean shouldLog;
 	
 	public Gui(Logger logger) {
 		_logger = logger;
 		shouldLog=Boolean.parseBoolean( Configurations.getAttribute("Should Log"));
		_logger.log(new Message("Logging "+(shouldLog ? "Enabled" : "Disabled"),Message.Type.Report));
         initComponents();
         
         addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				handleClose(arg0);
 			}
 		});
         
     }
                        
     private void initComponents() {
 
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jMenuBar1 = new JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
         jMenu3 = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         setDefaultUsername = new JMenuItem();
         shouldLogMenuItem = new JCheckBoxMenuItem();
        sendFile = new JMenuItem();
         
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 formComponentResized(evt);
             }
         });
 
         jTabbedPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                 .addGap(0, 56, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                 .addGap(0, 34, Short.MAX_VALUE))
         );
 
         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);
         
        shouldLogMenuItem.setText("Logging");
         shouldLogMenuItem.setSelected(shouldLog);
         shouldLogMenuItem.addActionListener(new java.awt.event.ActionListener(){
         	public void actionPerformed(java.awt.event.ActionEvent evt){
         		shouldLogMenuItemActionPerformed(evt);
         	}
         });
         jMenu1.add(shouldLogMenuItem);
 
         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);
 
         jMenu3.setText("Chat");
         setDefaultUsername.setText("Set Default UserName");
         setDefaultUsername.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
             	setDefaultUsernameActionPerformed(evt);
             }
         });
         
         jMenuItem1.setText("Add Chat");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
        
         jMenu3.add(jMenuItem1);
         jMenu3.add(setDefaultUsername);
        
        sendFile.setName("Send File");
        sendFile.addActionListener(new java.awt.event.ActionListener(){
        	public void actionPerformed(java.awt.event.ActionEvent evt){
        		sendFileActionPerformed(evt);
        	}
        });
 
         jMenuBar1.add(jMenu3);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout1 = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout1);
         layout1.setHorizontalGroup(
             layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout1.createSequentialGroup()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                 .addGap(0, 56, Short.MAX_VALUE))
         );
         layout1.setVerticalGroup(
             layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout1.createSequentialGroup()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                 .addGap(0, 24, Short.MAX_VALUE))
         );
     
         pack();
         _logger.log(new Message("Opened window",Message.Type.Report));
     }// </editor-fold>                        
 
     private void formComponentResized(java.awt.event.ComponentEvent evt) {                                      
         // TODO add your handling code here:
     }                                     
 
     public void addChat(Client c){
     	ChatPanel chatPanel = new ChatPanel(c,this);
     	jTabbedPane1.add(chatPanel, jTabbedPane1.getTabCount());
     }
     
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {                                           
         new ChatInfo(this,_logger).setVisible(true);
     }
     
     private void setDefaultUsernameActionPerformed(java.awt.event.ActionEvent evt){
     	new DefaultUsernameInput().setVisible(true);
     }
     
     public void shouldLogMenuItemActionPerformed(java.awt.event.ActionEvent evt){
     	shouldLog = !shouldLog;
     	Configurations.setAttribute("Should Log", shouldLog ? "True" : "False");
    	_logger.log(new Message((shouldLog ? "Enabled" : "Disabled")+" Logging",Message.Type.Report));
     }
 
    public void sendFileActionPerformed(java.awt.event.ActionEvent evt){
    	
    }
   
     // Variables declaration - do not modify        
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem setDefaultUsername;
     private javax.swing.JCheckBoxMenuItem shouldLogMenuItem;
     private javax.swing.JMenuItem sendFile;
     // End of variables declaration                   
 	
 
     
     	
     	public void handleClose(WindowEvent e) { 
     		_logger.log(new Message("Closeing window",Message.Type.Report));
     			for(Component c :jTabbedPane1.getComponents()){
     				if(c instanceof ChatPanel){
     					ChatPanel chat = (ChatPanel)c;
     					chat.close();
     					
     				}
     			}
     			if(shouldLog)
     				_logger.writeMessages();
     			System.exit(0);
     		}
     	
     	public void removeChat(ChatPanel c){
     	
     	}
     
 }
