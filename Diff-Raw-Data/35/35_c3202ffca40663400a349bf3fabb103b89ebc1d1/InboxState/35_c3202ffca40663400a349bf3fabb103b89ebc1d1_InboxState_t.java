 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cu.cs.cpsc215.crazy_mail.ui.messages;
 
 import cu.cs.cpsc215.crazy_mail.data.DataStore;
 import cu.cs.cpsc215.crazy_mail.ui.FrameState;
 import cu.cs.cpsc215.crazy_mail.ui.MainFrame;
 import cu.cs.cpsc215.crazy_mail.util.MailAccount;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author Emmanuel John
  * @author Kevin Jett
  * 
  */
 public class InboxState implements FrameState {
     private JPanel main_panel;
     private JPanel c_panel;
     private static InboxState inst;
     private JPanel message_panel;
     private ArrayList<Component> states = new ArrayList<Component>();
     private HashMap<MailAccount, DefaultListModel> account_map = new HashMap<MailAccount, DefaultListModel>();
     private JComboBox accountsBox;
     private JTextPane textpane;
     private JList messagelist;
     SwingWorker loadMessageWorker;
     private InboxState(){
         main_panel = new JPanel();
         c_panel = new JPanel();
         message_panel = new JPanel();
         main_panel.setLayout(new BorderLayout());
         c_panel.setLayout(new BorderLayout());
         message_panel.setLayout(new BorderLayout());
         
         message_panel.setBackground(Color.white);
         
         final DataStore dstore = DataStore.get();
         for(MailAccount account: dstore.getAccounts()){
             account_map.put(account, new DefaultListModel());
         }
         
         main_panel.add(createNorthPanel(account_map),"North");
         
         c_panel.add(messageListView(),BorderLayout.WEST);
         try {
             c_panel.add(createMessageViewer(null), BorderLayout.CENTER);
         } catch (IOException ex) {
             //c_panel.add(message_panel, BorderLayout.CENTER);
             Logger.getLogger(InboxState.class.getName()).log(Level.SEVERE, null, ex);
         } catch (MessagingException ex) {
             //c_panel.add(message_panel, BorderLayout.CENTER);
             Logger.getLogger(InboxState.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         main_panel.add(c_panel, BorderLayout.CENTER);
         
         
         loadMessageWorker = new SwingWorker(){
 
             @Override
             protected Object doInBackground() throws Exception {
                 for(final MailAccount account: account_map.keySet()){
                     Thread t = new Thread(new Runnable(){
                         @Override
                         public void run(){
                             
                                 loadMessages(account, account_map.get(account));
                            
                         }
                     });
                     
                     t.start();
                 }
                 return null;
             }
         
         };
         
         
         loadMessageWorker.execute();
         
     }
     
     public static InboxState get()
     {
 		if(inst == null)
 		{
 			inst = new InboxState();
 		}
 		return inst;
     }
     
     
     public JPanel messageListView(){
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.setPreferredSize(new Dimension(300,700));
         MailAccount account = (MailAccount) accountsBox.getSelectedItem();
         DefaultListModel model = account_map.get(account);
         
         if(model != null)
         {
         
 	        messagelist = new JList();
 	        messagelist.setModel(model);
 	    
	        messagelist.setFixedCellHeight(70);
 	        messagelist.addListSelectionListener(new ListSelectionListener() {
 	
 	            @Override
 	            public void valueChanged(ListSelectionEvent e) {

 	                if(!e.getValueIsAdjusting()){
                             try {
                                 Message msg = (Message)messagelist.getSelectedValue();
                                 textpane.setText(getMessageContent(msg));
                             } catch (IOException ex) {
                                 Logger.getLogger(InboxState.class.getName()).log(Level.SEVERE, null, ex);
                             } catch (MessagingException ex) {
                                 Logger.getLogger(InboxState.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         }

 	            }
 	        });
 	        
 	        messagelist.setCellRenderer(new MessageListCellRenderer());
         }
         panel.add(new JScrollPane(messagelist));
         
         
         return panel;
         
     }
     
     public JPanel createMessageViewer(Message message) throws IOException, MessagingException{
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.setBackground(Color.white);
         textpane = new JTextPane();
         textpane.setEditable(false);
         textpane.setContentType("text/html");
         if(message == null){
             textpane.setText("");
             
         }else{
             textpane.setText(getMessageContent(message));
         }
         
         textpane.setMargin(new Insets(7,7,7,7));
         
         panel.add(new JScrollPane(textpane));
         
         return panel;
     }
     public synchronized void loadMessages(MailAccount account, DefaultListModel model) {
         MainFrame.getInst().setStatus("Downloading messages for "+ account +"...");
         Properties props = System.getProperties();
         
         //create session object from properties file
         Session session = Session.getDefaultInstance(props,null);
         try {
             //get store object
             Store store = session.getStore(account.getIncomingMail().value());
             store.connect(account.getInHost(), account.getAccountEmail(), account.getAccountPassword());
             //create folder
             Folder inbox = store.getFolder("INBOX");
 
             inbox.open(Folder.READ_ONLY);
 
             int min;
             min = inbox.getMessageCount()-50;
             if(min <=0)
             {
             	min = 1;
             }
             Message [] messages = inbox.getMessages(min,inbox.getMessageCount());
             
             for(int i = messages.length-1; i>=0; i--){
                 model.addElement(messages[i]);
             }
             
             
         } catch (MessagingException ex) {
             if(!account.getInHost().equals(""))
                 JOptionPane.showMessageDialog(MainFrame.getInst(),"Invalid incoming mail Configuration for mail account: "+ account);
         }
         MainFrame.getInst().setStatus("Ready");
     }
     public static String getMessageContent(Message message) throws IOException, MessagingException
     {
     	//If message is invalid, return nothing
     	if(message == null)
     		return "";
     	
         Object content = message.getContent();
 
         if (content instanceof Multipart) {
             StringBuffer messageContent = new StringBuffer();
             Multipart multipart = (Multipart) content;
             for (int i = 0; i < multipart.getCount(); i++) {
                 Part part = (Part) multipart.getBodyPart(i);
                 if (part.isMimeType("text/html")||part.isMimeType("text/plain")) {

                     if(!messageContent.toString().contains(part.getContent().toString()))
                         messageContent.append(part.getContent().toString());
                 }
                else
                {
                	messageContent.append("<html><span style='color:red;'>Could not display Mime Type: "+part.getContentType()+"<br/></span></html>");
                }
                 
             }
             return messageContent.toString();
         } else {
             return content.toString();
         }
     }
     public JPanel createNorthPanel(HashMap<MailAccount, DefaultListModel> accounts){
         JPanel panel = new JPanel();
         panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
         accountsBox = new JComboBox(DataStore.get().getAccounts().toArray());
         accountsBox.addItemListener(new ItemListener(){
 
             @Override
             public void itemStateChanged(ItemEvent e) {
                 final MailAccount account = (MailAccount) accountsBox.getSelectedItem();
                 SwingUtilities.invokeLater(new Runnable(){
                     @Override
                     public void run(){
                     	DefaultListModel model = account_map.get(account);
                         if(model!=null)
                         	messagelist.setModel(model);
                     }
                 });
                 
             }
             
         });
         
         
         panel.add(accountsBox);
         
         return panel;
     }
     
     //This updates the account listing and properties in the inbox. Requires a list of the old accounts for comparison
     public void updateAccountList(final ArrayList<MailAccount>oldAccounts, final ArrayList<MailAccount> accounts){
       Set<MailAccount> stored = account_map.keySet();
        
       //Remove anything from the map that was in old accounts but not new
       for(int i = 0; i<oldAccounts.size(); i++)
        {
            MailAccount old = oldAccounts.get(i);
            if(!accounts.contains(old))
            {
                account_map.remove(old);
            }
        }
        accountsBox.removeAllItems(); //Remove all from the dropdown since we will rebuild it
        
        //For every account
        for(int i = 0; i<accounts.size(); i++)
        {
     	   final MailAccount account = accounts.get(i);
     	   
            if(!stored.contains(accounts.get(i))) //If we don't have a listing for it, making a new thread to retrieve its mail
            {
               account_map.put(account, new DefaultListModel());
                new Thread(new Runnable(){
 	                @Override
 	                public void run(){
 	                        loadMessages(account, account_map.get(account));                     
 	                }
 	            }).start();
            }
            
            accountsBox.addItem(account); //Also add to the dropdown
        }
        accountsBox.validate();
        MainFrame.getInst().repaint();
     }
     
     public void onHide() {}
 
     public void onShow() {}
 
     public String getName() {
         return "Inbox";
     }
 
     public JPanel getPanel() {
         return main_panel;
     }
     
     
 }
