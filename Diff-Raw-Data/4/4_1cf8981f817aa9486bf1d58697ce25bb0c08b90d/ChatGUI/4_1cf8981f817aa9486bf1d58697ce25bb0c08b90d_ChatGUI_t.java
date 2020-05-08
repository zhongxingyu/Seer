 package client;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Map;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.util.List;
 
 import protocol.Message;
 import protocol.SimpleMessage;
 
 /**
  * Testing Strategy:
  *
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Leave room
  * ---
  * This test should show the message displayed next to the specified username, the new chat room should be in the list
  * of available chat rooms. The user should then be able to leave the room.
  *
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Leaves room
  * Other user connects to chat server
  * Other user creates a username
  * Other user creates a new room
  * Other user types message into new room
  * ---
  * This test should show the message displayed next to the first specified username, the new chat room should be in the list
  * of available chat rooms, the other user should be able to create a new username and login to the chat server and not see the 
  * other user's chat room in the list, the new chat room that the second user creates should join the list of available chat
  * rooms, the message the second user types should be only displayed in the second user's chat room next to the second user's
  * name.
  *
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Other user connects to chat server
  * Other user creates a username
  * Other user joins existing room
  * Other user types message into new room
  * First user types message into room
  * First user leaves room
  * Second user leaves room
  * ---
  * This test should show the message displayed next to the first specified username, the new chat room should be in the list
  * of available chat rooms, the other user should be able to create a new username and login to the chat server and see the 
  * other user's chat room in the list. The second user should be able to join the first user's room. The first user should be
  * notified that the second user joined. The second user should not be able to see the first user's previous message. The 
  * second user should be able to type in a message, which will be displayed next to their username. The first user should
  * see this new message. The first user should be able to type in a message, which will be displayed next to their username.
  * The second user should see this new message. The second user should be notified that the first user left. The second user
  * should be able to leave.
  * 
  * Connect to Chat Server
  * Create a username
  * Create a new room
  * Type message into room
  * Other user connects to chat server
  * Other user creates a username
  * Other user joins existing room
  * Other user types delayed message into new room
  * First user types message into room
  * First user leaves room
  * Second user leaves room
  * ---
  * This test should show the message displayed next to the first specified username, the new chat room should be in the list
  * of available chat rooms, the other user should be able to create a new username and login to the chat server and see the 
  * other user's chat room in the list. The second user should be able to join the first user's room. The first user should be 
  * notified that the second user joined. The second user should not be able to see the first user's previous message. The 
  * second user should be able to type in a delayed message, which will be displayed next to their username immediately. 
  * The first user should be able to type in a message before the second user's message shows up for them, which will be 
  * displayed next to the first username. The second user's delayed message should show up before the first user's message. 
  * Both users should be able to see both messages eventually.The second user should be notified that the first user left. 
  * The second user should be able to leave.
  * 
  * Other tests:
  * One user, multiple chat rooms
  * Multiple users, one chat room
  * Multiple users, multiple chat rooms
  * 
  */
 
 public class ChatGUI extends JFrame {
 
     /**
      *  Variables
      */
     private static final long serialVersionUID = 9049377501245959344L;
     //GUI variable declarations
     private JScrollPane jScrollPane1, jScrollPane2, jScrollPane3;
     private JTextArea inputTextArea, chatTextArea;
     private JLabel avaiableChatRoomsLabel, messageLabel;
     private JButton sendButton;
     private JMenuBar menuBar;
     private JMenu fileMenu, editMenu, historyMenu, helpMenu;
     private JMenuItem jMenuItem1, jMenuItem2, jMenuItem3, jMenuItem4, jMenuItem5, jMenuItem6, jMenuItem7, jMenuItem8, jMenuItem9;
     private final String startupMessage = "System Message: To connect to the chat server navigate to Connect to Server in the menu or press ctrl-d (or command-d on mac). " +
             "Type \"--help\" in the box below to get a list of avaible commands.\n";
     private final String helpCommands = "System Message: The available commands are as follows:\n" +
             "1) \"--list_users_in_room\"\n" +
             "2) \"--exit_room\"\n" +
             "3) \"--exit_chat_client\"\n";
     public JTable chatWindowsTable;
     public DefaultTableModel chatWindowsTableModel;
     //Server/client connection variables
     private boolean isConnected = false;
     private final String SERVER_NAME = "localhost";
     private final int SERVER_PORT = 4444;
     private Socket socket;
     private ChatSession chatSession = null;
     public ChatWindow currentChatWindow = null;
     public String username;
     private Object tableModelLock = new Object();
 
 
     /**
      * ChatGUI constructor
      * 
      * calls initComponents() to create gui
      */
     public ChatGUI() {
         initComponents();    
         writeToWindow(startupMessage);
     }
 
     private void initComponents() {
         //init items
         jScrollPane1 = new JScrollPane();
         inputTextArea = new JTextArea();
         jScrollPane2 = new JScrollPane();
         chatTextArea = new JTextArea();
         sendButton = new JButton();
         jScrollPane3 = new JScrollPane();
         avaiableChatRoomsLabel = new JLabel();
         messageLabel = new JLabel();
         menuBar = new JMenuBar();
         fileMenu = new JMenu();
         editMenu = new JMenu();
         historyMenu = new JMenu();
         helpMenu = new JMenu();
         jMenuItem1 = new JMenuItem();
         jMenuItem2 = new JMenuItem();
         jMenuItem3 = new JMenuItem();
         jMenuItem4 = new JMenuItem();
         jMenuItem5 = new JMenuItem();
         jMenuItem6 = new JMenuItem();
         jMenuItem7 = new JMenuItem();
         jMenuItem8 = new JMenuItem();
         jMenuItem9 = new JMenuItem();
 
         //set close and title
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("Chat Client");
 
         //box to write message in
         inputTextArea.setColumns(20);
         inputTextArea.setLineWrap(true);
         inputTextArea.setWrapStyleWord(true);
         inputTextArea.setRows(5);
         inputTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "submitMessage");
         //NOTE: do the following for textarea events. DON'T USE KEYLISTENERS. bad practice.
         inputTextArea.getActionMap().put("submitMessage", new AbstractAction() {
             /**
              * 
              */
             private static final long serialVersionUID = -4542333036170122454L;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 sendButtonHandler(e);
             }
         });
         jScrollPane1.setViewportView(inputTextArea);
 
 
         //area to display messages
         chatTextArea.setColumns(20);
         chatTextArea.setEditable(false);
         chatTextArea.setFont(new Font("Times New Roman", 0, 16));
         chatTextArea.setLineWrap(true);
         chatTextArea.setWrapStyleWord(true);
         chatTextArea.setRows(5);
         jScrollPane2.setViewportView(chatTextArea);
 
         //display messages label
         messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
         messageLabel.setText("Chat Window");
 
         //send button
         sendButton.setText("Send");
         sendButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sendButtonHandler(e);
             }
         });
 
         //list of open rooms. NOTE: rooms that you are connected
         //to will be starred or put in bold.
         //They will also have a number next to the name if there
         //are unread messages
         chatWindowsTableModel = (DefaultTableModel) new DefaultTableModel(new Object[] {"Active","Name","# Users","# Unread"},0);
         chatWindowsTable = new JTable(chatWindowsTableModel) {
             /**
              * jholliman
              */
             private static final long serialVersionUID = 674689143091386263L;
 
             @Override
             public boolean isCellEditable (int row, int column) {
                 return false;
             }
         };
         jScrollPane3.setViewportView(chatWindowsTable);
         jScrollPane3.setSize(30, 10);
         chatWindowsTable.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 changeCurrentChatWindow(e);
             }
         });
 
         //Open chat rooms label
         avaiableChatRoomsLabel.setHorizontalAlignment(SwingConstants.CENTER);
         avaiableChatRoomsLabel.setText("Chat Rooms");
 
         //Menu bar
         fileMenu.setText("File");
         editMenu.setText("Edit");
         historyMenu.setText("History");
         helpMenu.setText("Help");
         jMenuItem1.setText("Settings");
         jMenuItem1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 settings(e);
             }
         });
         jMenuItem2.setText("Connect to Chat Server");
         jMenuItem2.setMnemonic(KeyEvent.VK_T); //example of mnemonic
         jMenuItem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); //example of accelerator
         jMenuItem2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 connectToServer(e);
             }
         });
         jMenuItem3.setText("Disconnect from Chat Server");
         jMenuItem3.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 disconnectFromServer(e);
             }
         });
         jMenuItem4.setText("Connect to Room");
         jMenuItem4.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 connectToRoom(e);
             }
         });
         jMenuItem5.setText("Create Room");
         jMenuItem5.setMnemonic(KeyEvent.VK_N); //example of mnemonic
         jMenuItem5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); //example of accelerator
         jMenuItem5.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 createRoom(e);
             }
         });
         jMenuItem6.setText("Documentation");
         jMenuItem6.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 //open up a documentation in browser
                 // open the default web browser for the HTML page
                 try {
                     Desktop.getDesktop().browse(new URI("https://github.com/jholliman/guichat/blob/master/README.md"));
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();
                 }
             }
         });
         jMenuItem7.setText("Leave Current Room");
         jMenuItem7.setMnemonic(KeyEvent.VK_E); //example of mnemonic
         jMenuItem7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); //example of accelerator
         jMenuItem7.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 disconnectFromRoom(e);
             }
         });
         jMenuItem8.setText("Save Current Conversation");
         jMenuItem8.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 saveConversation(e);
             }
         });
         jMenuItem9.setText("Select Room");
         jMenuItem9.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 selectHistory(e);
             }
         });
         fileMenu.add(jMenuItem5);
         fileMenu.addSeparator();
         fileMenu.add(jMenuItem4);
         fileMenu.add(jMenuItem7);
         fileMenu.addSeparator();
         fileMenu.add(jMenuItem2);
         fileMenu.add(jMenuItem3);
         fileMenu.addSeparator();
         fileMenu.add(jMenuItem1);
         helpMenu.add(jMenuItem6);
         editMenu.add(jMenuItem8);
         historyMenu.add(jMenuItem9);
         menuBar.add(fileMenu);
         menuBar.add(editMenu);
         menuBar.add(historyMenu);
         menuBar.add(helpMenu);
         setJMenuBar(menuBar);
 
         //set window layout
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                 .addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                         .addGap(4, 4, 4)
                                         .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 336, GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(sendButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                         .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 419, GroupLayout.PREFERRED_SIZE)
                                         .addGroup(layout.createSequentialGroup()
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                 .addGap(18, 18, 18)))
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                         .addComponent(avaiableChatRoomsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                         .addComponent(jScrollPane3))
                                                         .addContainerGap())
                 );
         layout.setVerticalGroup(
                 layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                 .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                 .addComponent(messageLabel)
                                 .addComponent(avaiableChatRoomsLabel))
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                         .addGroup(layout.createSequentialGroup()
                                                 .addComponent(jScrollPane2)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                         .addComponent(sendButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                         .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)))
                                                         .addComponent(jScrollPane3))
                                                         .addContainerGap())
                 );
 
         //size window properly
         pack();
     }
 
     //Helpers
 
     public synchronized void writeToWindow(String text) {
         chatTextArea.append(text);
         scrollToBottomOfChatWindow();
     }
     
     public synchronized void writeToHistoryWindow(List<Message> messages) {
        JTextArea textArea = new JTextArea(100, 250);
        for (int i = 0; i < messages.size(); i++){
            textArea.append(messages.get(i).getUsername() + ":" + messages.get(i).getMessage() + "\n");
        }
        textArea.setEditable(false);
        textArea.setColumns(30);
        textArea.setLineWrap( true );
        textArea.setWrapStyleWord( true );
        textArea.setSize(textArea.getPreferredSize().width, 1);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(100, 250));
        JOptionPane.showMessageDialog(
         null, scrollPane, "Chat History", JOptionPane.PLAIN_MESSAGE);
 
     }
 
     public void clearWindow() {
         chatTextArea.setText("");
     }
 
     private void scrollToBottomOfChatWindow() {
         //scroll to the bottom of the scroll pane
         JScrollBar vertical = jScrollPane2.getVerticalScrollBar();
         vertical.setValue(vertical.getMaximum());
     }
 
     public void setIsConnected(boolean b) {
         isConnected = b;
     }
 
     public synchronized ChatWindow getCurrentChatWindow() {
         return currentChatWindow;
     }
 
     public Object getTableModelLock() {
         return tableModelLock;
     }
 
     //Listener Methods
 
     private void changeCurrentChatWindow(MouseEvent e) {
         if (isConnected) {
             synchronized(tableModelLock) {
                 int selectedRow = chatWindowsTable.getSelectedRow();
                 String roomName = (String) chatWindowsTableModel.getValueAt(selectedRow, 1);
                 //located room
                 Map<String, ChatWindow>  chatWindows = chatSession.getActiveChatWindows();
                 Map<String, ChatWindow>  prevChatWindows = chatSession.getPrevChatWindows();
                 if (chatWindows.containsKey(roomName)) {
                     currentChatWindow = chatWindows.get(roomName);
                     //set number of unread messages in currentChatWindow to 0
                     System.out.println("currentChatWindow :" + currentChatWindow);
                     currentChatWindow.setMessageCountToZero();
                     chatWindowsTableModel.setValueAt(0,selectedRow,3);
                     //refresh chat history window with messages from the selected chat window
                     clearWindow();
                     for (Message m: currentChatWindow.getMessages()) {
                         writeToWindow(m.getUsername() + ":" + m.getMessage()+"\n");
                     }
                     //set label to the title of the current room
                     messageLabel.setText(currentChatWindow.getName());
                 } else if (prevChatWindows.containsKey(roomName)) {
                     chatSession.joinChatWindow(roomName);
                     clearWindow();
                 }
                 
             } 
         }
     }
 
     public void reload(String name) {
         synchronized(tableModelLock) {
             //locate the correct row
             for (int row = 0; row < chatWindowsTableModel.getRowCount(); row++) {
                 if (((String) chatWindowsTableModel.getValueAt(row, 1)).equals(name)) {
                     currentChatWindow = chatSession.getActiveChatWindow(name);
                     //set number of unread messages in currentChatWindow to 0
                     System.out.println("currentChatWindow :" + currentChatWindow);
                     currentChatWindow.setMessageCountToZero();
                     chatWindowsTableModel.setValueAt(0,row,3);
                     //refresh chat history window with messages from the selected chat window
                     clearWindow();
                     for (Message m: currentChatWindow.getMessages()) {
                         writeToWindow(m.getUsername() + ":" + m.getMessage()+"\n");
                     }
                     //set label to the title of the current room
                     messageLabel.setText(currentChatWindow.getName());
                     //alert user of how to exit
                     writeToWindow("System Message: You are connected to " + currentChatWindow.getName() + ". Press ctrl-e (or command-e on mac) to exit.\n");
                     break;
                 }
             }
         }
     }
 
     private void connectToRoom(ActionEvent e) {
         if (isConnected) {
             Object[] possibilities = chatSession.getAvailableChatRooms(); //get the list of rooms
             if (possibilities.length == 0){
                 JOptionPane.showMessageDialog(
                         null, "There are no rooms to join, create room first!", "Please create a room", JOptionPane.WARNING_MESSAGE);
             }
             else{
                 String roomName = (String)JOptionPane.showInputDialog(
                         this,
                         "What room would you like to join?",
                         "Connect to Room",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         possibilities,
                         possibilities[0]);
                if (roomName != null) {
                    chatSession.joinChatWindow(roomName);
                } 
             }
         }
     }
 
     private void disconnectFromRoom(ActionEvent e) {
         if (isConnected && currentChatWindow != null) {
             int n = JOptionPane.showConfirmDialog(
                     this,
                     "Are you sure that you want to leave this room?",
                     "Disconnect from Room",
                     JOptionPane.YES_NO_OPTION);
             if (n == 0) { //yes
                 chatSession.closeChatWindow(currentChatWindow);
                 currentChatWindow = null;
                 clearWindow();
                 messageLabel.setText("Chat Window");
                 writeToWindow("You have successfully exited the room. " +
                               "Please select or join another conversation to continue chatting.\n");
             }
         }
     }
 
     private void connectToServer(ActionEvent e) {
         if (isConnected) {
             writeToWindow("System Message: You are already connected. Logout and log back in if you are looking to change username.");
         } else {
             //ask user for username
             String username = (String)JOptionPane.showInputDialog(
                     this,
                     "What would you like your nickname to be? (This must be unique)",
                     "Connect to Server",
                     JOptionPane.PLAIN_MESSAGE,
                     null,
                     null,
                     "Ex. jholliman");
             if (username != null) {
                 this.username = username;
                 try {
                     //Attempt to connect to the chat server
                     this.socket = new Socket(SERVER_NAME, SERVER_PORT);
                     //Alert of success for testing purposes
                     System.out.println("Connected to chat server at " + SERVER_NAME + ":" + SERVER_PORT + ".");
                     //create the chat session
                     chatSession = new ChatSession(socket, this);
                     //send login request
                     chatSession.sendLoginRequest();
                 } catch (IOException e1) {
                     //Failure to connect
                     e1.printStackTrace();
                     System.out.println("Failed to connect to chat server at " + SERVER_NAME + ":" + SERVER_PORT + " or broken socket.");
                     System.exit(-1);
                 }
             }
             
         }  
     }
 
     private void disconnectFromServer(ActionEvent e) {
         if (isConnected) {
             int n = JOptionPane.showConfirmDialog(
                     this,
                     "Are you sure that you would like to disconnect?\nAll chats will be closed and the associated histories will be deleted.",
                     "Disconnect from Room",
                     JOptionPane.YES_NO_OPTION);
             if (n == 0) { //yes
                 chatSession.logout();
                 isConnected = false;
                 try {
                     socket.close();
                     socket = null;
                     username = null;
                     clearWindow();
                     writeToWindow("You have been successfully disconnected from the server.\n");
                 } catch (IOException e1) {
                     //System.out.println("User was disconnected");
                 }
             }
         }
     }
 
     private void createRoom(ActionEvent e) {
         if (isConnected) {
             String name = (String)JOptionPane.showInputDialog(
                     this,
                     "What is the name of the room you would like to create? (This must be unique)",
                     "Create Room",
                     JOptionPane.PLAIN_MESSAGE,
                     null,
                     null,
                     "Ex. John's room");
             if (name != null) {
                 //attempt to create the chatWindow
                 chatSession.createChatWindow(name);
             } 
         }
     }
 
     private void settings(ActionEvent e) {
         String[] possibilities = new String[] {"10","12","14","16","18","20"};
         String fontSize = (String)JOptionPane.showInputDialog(
                         this,
                         "Select prefered font size?",
                         "Settings",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         possibilities,
                         chatTextArea.getFont().getSize()+"");
         if (fontSize != null) {
             chatTextArea.setFont(new Font("Times New Roman", 0, Integer.parseInt(fontSize)));
         }
         //TODO make more in depth settings if there's time
     }
 
     private void saveConversation(ActionEvent e) {
         if (isConnected && currentChatWindow != null) {
             chatSession.saveConversation(currentChatWindow);
         }
     }
     
     private void selectHistory(ActionEvent e) {
         if (isConnected && currentChatWindow != null) {
             Object[] possibilities = chatSession.getAvailableChatRooms(); //get the list of rooms
             String roomName = (String)JOptionPane.showInputDialog(
                     this,
                     "What room history would you like to view?",
                     "Select Room",
                     JOptionPane.PLAIN_MESSAGE,
                     null,
                     possibilities,
                     possibilities[0]);
             chatSession.viewHistory(roomName);
         }
     }
 
     private void sendButtonHandler(ActionEvent e) {
         String textEntered = inputTextArea.getText();
         if (textEntered.equals("--help")) {
             writeToWindow(helpCommands);
         } else if (textEntered.equals("--list_users_in_room") && isConnected && currentChatWindow != null) {
             chatSession.getUsersInChatWindow(currentChatWindow);
         } else if (textEntered.equals("--exit_room") && isConnected && currentChatWindow != null) {
             chatSession.closeChatWindow(currentChatWindow);
             //TODO the current chat window needs to be set to something else
         } else if (textEntered.equals("--exit_chat_client") && isConnected) {
             disconnectFromServer(e);
             System.exit(0);
         } else if (textEntered.equals("--talk_dirty_to_me")) {
             writeToWindow(" /  \\\n" +
                           "(.)(.)\n" +
                           " )  (\n" +
                           "(  . )\n" + 
                           "  \\/\n");
         } else {
             if (isConnected) {
                 if (currentChatWindow == null) {
                     writeToWindow("Please create or join a room to chat sex meow meow. ;)\n");
                 } else {
                     String nothing = "";
                     Message m = new SimpleMessage(textEntered, new Timestamp((new Date()).getTime()), username, currentChatWindow.getName());
                     if (!textEntered.equals(nothing)) { //check for text
                         chatSession.sendMessage(currentChatWindow, m);//chat session will write to the window
                         //writeToWindow(username + ": " + textEntered + "\n");
                     }
                 }  
             } else {
                 writeToWindow(startupMessage);
             }
         } 
         //clear chat window and refocus
         inputTextArea.setText("");
         inputTextArea.requestFocus();
     }
 
 }
