 import java.io.*;
 import java.net.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Client extends JFrame{
    
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;
    
    //constructor
    public Client(String host){
       super("WilTecIM Client");
       serverIP = host;
       userText = new JTextField();
       userText.setEditable(false);
       userText.addActionListener(
          new ActionListener(){
             public void actionPerformed(ActionEvent event){
                sendMessage(event.getActionCommand());
                userText.setText("");
             }
          }
       );
       add(userText, BorderLayout.NORTH);
       chatWindow = new JTextArea();
       add(new JScrollPane(chatWindow), BorderLayout.CENTER);
       setSize(300,150);
       setVisible(true);
    }
    
    //connect to server
    public void startRunning(){
       try{
          connectToServer();
          setupStreams();
          whileChatting();
       }catch(EOFException eofException){
          showMessage("\n Client ended connection!");
       }catch(IOException ioException){
          ioException.printStackTrace();
       }finally{
          closeRubbish();
       }
    }
    
    //connect to server
    private void connectToServer() throws IOException{
       showMessage("Attempting connection... \n");
       connection = new Socket(InetAddress.getByName(serverIP), 6789);
       showMessage("Connected to: " + connection.getInetAddress().getHostName() );
    }
    
    //set up streams to send and receive messages
    private void setupStreams() throws IOException{
       output = new ObjectOutputStream(connection.getOutputStream());
       output.flush();
       input = new ObjectInputStream(connection.getInputStream());
       showMessage("\n Streams Setup! \n");
    }
    //442
    //while chatting with server
    private void whileChatting() throws IOException{
       ableToType(true);
       do{
          try{
             message = (String) input.readObject();
             showMessage("\n" + message);
          }catch(ClassNotFoundException classNotfoundException){
             showMessage("\n Error: 442");
          }
       }while(!message.equals("SERVER - END"));
    }
    
    //close the streams and sockets
    private void closeRubbish(){
       showMessage("\n Closing Down...");
       ableToType(false);
       try{
          output.close();
          input.close();
          connection.close();
       }catch(IOException ioException){
          ioException.printStackTrace();
       }
    }
    
   //413
    //send messages to server
    private void sendMessage(String message){
       try{
          output.writeObject("CLIENT - " + message);
          output.flush();
          showMessage("\nCLIENT - " + message);
       }catch(IOException ioException){
         chatWindow.append("\n Error: 413. Somthing Went Wrong :<");
       }
    }
    
    //change/update chatWindow
    private void showMessage(final String m){
       SwingUtilities.invokeLater(
          new Runnable(){
             public void run(){
                chatWindow.append(m);
             }
          }
       );
    }
    
    //gives user permission to type Rubbish into the text box
    private void ableToType(final boolean tof){
       SwingUtilities.invokeLater(
          new Runnable(){
             public void run(){
                userText.setEditable(tof);
             }
          }
       );      
    }
 }
