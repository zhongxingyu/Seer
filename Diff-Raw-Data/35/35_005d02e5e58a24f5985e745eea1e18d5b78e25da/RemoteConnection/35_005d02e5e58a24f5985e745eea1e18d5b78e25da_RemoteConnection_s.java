 package solarshado.jNetChess.net;
 
 import java.net.*;
 import java.util.Arrays;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import javax.swing.*;
 import solarshado.jNetChess.Util;
 
 /**
  * represents an established connection
  * objects will be created by a Lobby and passed to a LobbybListener
  * constructors will handle establishing said connection
  * 
  * in other words, this class implements both the client and server
  * 
  * @author Adrian Todd
  */
 public final class RemoteConnection {
 
     public final String myPlayerName;
     public final String remotePlayerName;
     public final InetAddress remoteComputer;
 
     private final Socket sock;
     private final StatusWindow myStatus;
 
     private StatusState state = StatusState.SETTING_UP;
 
     /**
      * Creates a new listening connection. Does not return until successfully
      * connected.
      * 
      * @param myName playerName for this end of the connection
      * @throws IOException
      */
     public RemoteConnection(String myName) throws ConnectionCancledExcpetion,
             IOException {
         myStatus = new StatusWindow();
         myPlayerName = myName;
         System.err.println("creating New Listening Connction...");
 
         ServerSocket srv = null;
         try {
             srv = new ServerSocket(NetConstants.TCP_PORT);
         }
         catch (IOException e) {
             // TODO handle error setting up ServerSock
             e.printStackTrace();
         }
 
         try {
             srv.setSoTimeout(500);
         }
         catch (SocketException e) {
             // TODO handle error setting SO_TIMEOUT
             e.printStackTrace();
         }
 
         status(StatusState.WAITING);
 
         Socket sockTmp = null;
         String remoteNameTmp = null;
 
         do {
             if (state == StatusState.CANCELED) {
                 try {
                     if (sockTmp != null) sockTmp.close();
                     srv.close();
                 }
                 catch (IOException e) {
                     // TODO handle errors closing connections
                     e.printStackTrace();
                 }
                 status(StatusState.CLOSED);
                 throw new ConnectionCancledExcpetion();
             }
             try {
                 sockTmp = srv.accept();
                 status(StatusState.CONNECTING);
             }
             catch (SocketTimeoutException e) {
                 // expected, so just:
                 continue;
             }
             catch (IOException e) {
                 // TODO handle IOE on accept()
                 e.printStackTrace();
             }
 
             remoteNameTmp = swapNames(myName, sockTmp);
 
             if (!myStatus.prompt(remoteNameTmp)) { // rejected
                 try {
                     sockTmp.getOutputStream().write(NetConstants.REJECT_MSG);
                     sockTmp.close();
                     status(StatusState.WAITING);
                 }
                 catch (IOException e) {
                     // TODO handle IOE on rejecting connection
                     e.printStackTrace();
                 }
                 continue;
             }
             break;
         } while (true);
 
         sock = sockTmp;
         remotePlayerName = remoteNameTmp;
         remoteComputer = sock.getInetAddress();
 
         status(StatusState.HANDSHAKING);
         // connected, proceed w/ rest of handshake
         try {
             sockTmp.getOutputStream().write(NetConstants.ACCEPT_MSG);
         }
         catch (IOException e) {
             // TODO handle IOE on sending accept
             e.printStackTrace();
         }
 
         try {
             if (sock.getInputStream().read() != 255)
                 throw new IOException("Handshake failure");
         }
         catch (IOException e) {
             if (e.getMessage().equals("Handshake failure"))
                 throw e;
             else {
                 // TODO handle IOE on finishing handshake
                 e.printStackTrace();
             }
         }
         status(StatusState.CONNECTED);
     }
 
     /**
      * Creates and attempts to establish a connection using the specified remote
      * address.
      * Will either return a successful connection or throw an Exception.
      * 
      * @param myName playerName for this end of the connection
      * @param remote Address to (attempt) to connect to.
      * @throws IOException
      */
     public RemoteConnection(String myName, InetAddress remote)
             throws ConnectionCancledExcpetion, IOException {
         myStatus = new StatusWindow();
         myPlayerName = myName;
         remoteComputer = remote;
 
         Socket sockTmp = null;
 
         System.err.println("New Connction to " + remote + " created");
 
         status(StatusState.SETTING_UP);
         status(StatusState.CONNECTING);
         try {
             sockTmp = new Socket(remote, NetConstants.TCP_PORT);
         }
         catch (IOException e) {
             // TODO handle IOE on connection
             e.printStackTrace();
         }
         finally {
             sock = sockTmp;
         }
 
         status(StatusState.HANDSHAKING);
 
         remotePlayerName = swapNames(myName, sock);
 
         byte[] response = new byte[6];
         boolean hs_success = false;
         boolean accepted = false;
 
         try {
             if (sock.getInputStream().read(response) == -1) {
                 hs_success = false;
             }
             else if (Arrays.equals(response, NetConstants.ACCEPT_MSG)) {
                 hs_success = true;
                 accepted = true;
             }
             else if (Arrays.equals(response, NetConstants.REJECT_MSG)) {
                 hs_success = true;
                 accepted = false;
             }
             else {
                 hs_success = false;
             }
         }
         catch (IOException e) {
             // TODO handle IOE on handshake response
             e.printStackTrace();
         }
 
         if (!hs_success) { throw new IOException("Handshake failure"); }
         if (!accepted) {
             status(StatusState.REFUSED);
             status(StatusState.CLOSED);
             throw new ConnectionCancledExcpetion();
         }
 
         // confirm acceptance
         try {
             sock.getOutputStream().write(255);
         }
         catch (IOException e) {
             // TODO handle IOE on end of handshake
             e.printStackTrace();
         }
         status(StatusState.CONNECTED);
     }
 
     /**
      * notifies the RemoteConnection that is should close it's status dialog
      */
     public void closeStatus() {
         myStatus.f.dispose();
     }
    
     private void status(StatusState s) {
         state = s;
         myStatus.update();
     }
 
     private String swapNames(String myName, Socket sock) {
         try {
             sock.getOutputStream()
                     .write((myName + '\n').getBytes("ISO-8859-1"));
             return (new BufferedReader(new InputStreamReader(
                     sock.getInputStream()))
                     .readLine());
         }
         catch (IOException e) {
             // TODO handle IOE while swapping names
             e.printStackTrace();
             return null;
         }
     }
 
     private class StatusWindow implements ActionListener {
 
         private static final String CANCEL = "cancelPanel",
                 PROMPT = "promptPanel", CONFIRM = "confirmPanel";
 
         private final JFrame f = new JFrame("Connection Status...");
         private final JLabel infoLlb = new JLabel();
 
         private final java.awt.CardLayout lowerLayout = new java.awt.CardLayout();
         private final JPanel lowerPanel = new JPanel(lowerLayout);
         private final JButton cancelBtn = new JButton("Cancel");
 
         private final JPanel promptPnl = new JPanel();
         private final JButton acceptBtn = new JButton("Accept");
         private final JButton rejectBtn = new JButton("Reject");
 
         private final JButton confirmBtn = new JButton("OK");
 
         private volatile boolean haveResponse;
         private volatile boolean response;
 
         public StatusWindow() {
             java.awt.Container p = f.getContentPane();
             p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(infoLlb);
 
            promptPnl.setLayout(new BoxLayout(promptPnl, BoxLayout.X_AXIS));
             promptPnl.add(acceptBtn);
             promptPnl.add(rejectBtn);
 
             lowerPanel.add(cancelBtn, CANCEL);
             lowerPanel.add(promptPnl, PROMPT);
             lowerPanel.add(confirmBtn, CONFIRM);
 
             p.add(lowerPanel);
 
             cancelBtn.addActionListener(this);
             acceptBtn.addActionListener(this);
             rejectBtn.addActionListener(this);
             confirmBtn.addActionListener(this);
 
             f.setVisible(true);
             f.pack();
             Util.centerWindow(f);
         }
 
         public void update() {
             infoLlb.setText(state.getMessage());
             if (state == StatusState.CLOSED)
                 f.dispose();
             else if (state == StatusState.REFUSED) {
                 lowerLayout.show(lowerPanel, CONFIRM);
                 haveResponse = false;
                 while (!haveResponse) {
                     try {
                         Thread.sleep(250);
                     }
                     catch (InterruptedException e) {
                         // don't care
                     }
                 }
                //rejection ok'd by user
             }
             else if (state == StatusState.CONNECTED) {
                 f.remove(lowerPanel);
             }
         }
 
         public boolean prompt(String remoteName) {
             haveResponse = false;
             lowerLayout.show(lowerPanel, PROMPT);
             infoLlb.setText("'" + remoteName + "' would like to play.");
             while (!haveResponse) {
                 try {
                     Thread.sleep(250);
                 }
                 catch (InterruptedException e) {
                     // don't care
                 }
             }
             lowerLayout.show(lowerPanel, CANCEL);
             return response;
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.Component s = (Component) e.getSource();
             if (s == acceptBtn) {
                 response = true;
                 haveResponse = true;
             }
             else if (s == rejectBtn) {
                 response = false;
                 haveResponse = true;
             }
            else if (s==confirmBtn){
                 haveResponse = true;
             }
             else if (s == cancelBtn) {
                 status(StatusState.CANCELED);
             }
         }
     }
 
     public enum StatusState {
         SETTING_UP("Setting up server..."),
         WAITING("Waiting for connections..."),
         CANCELED("Canceled, stopping server..."),
         CLOSED(">status window closing...<"),
         // above 4 for server
         CONNECTING("Connecting..."),
         REFUSED("Connection refused..."),
         // above 2 for client, last 2 for both
         HANDSHAKING("Connected: handshaking..."),
         CONNECTED("Successfully Connected!");
 
         String message;
 
         StatusState(String msg) {
             this.message = msg;
         }
 
         String getMessage() {
             return message;
         }
     }
}
