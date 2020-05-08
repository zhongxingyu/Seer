 package core.impl;
 
 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Vector;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.border.Border;
 import javax.swing.border.LineBorder;
 import javax.swing.text.BadLocationException;
 //import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.PropertyConfigurator;
 import core.ChatEventListener;
 import chatsession.ChatClientService;
 import chatsession.ex.ChatServiceException;
 import chatsession.impl.ChatClientServiceFactoryImpl;
 import chatsession.pdu.ChatAction;
 
 public class Client implements ChatEventListener {
 	
 	//Attribute//
 	private ClientCommunicator clientCommunicator;
 	private ChatClientService chatClientService;
 	private String userName, userIP, userPort;	
 	private static Log log = LogFactory.getLog(Client.class);
 	//Chat
     private JFrame chatFrame;
     private JTextPane chatArea;
     private JTextField chatField;
     private JButton submitButton, logoutButton, iconButton;
 	private JList jList;
 	private DefaultListModel defaultListModel;
 	private JScrollPane jScrollPaneList, chatAreaScrollable;
 	private JLabel header2;
	//private javax.swing.text.html.HTMLEditorKit eKit; //HTML-Code in JTextPane mglich
 	private ImageIcon icon = null;
 	private java.net.URL sourire = null;
 	private boolean smileys = false;
 	private StyledDocument doc;
     //private SimpleAttributeSet attributeSet;
 	//Login
 	private JFrame clientLoginFrame;
 	private JTextField name, ip, port;
 	private JButton loginButton, downButton;
 	private JLabel header;
 	private JLabel labelName, labelIP, labelPORT, labelError;
 
 
 	//Erzeugen des Loginframes.
 	public void showLoginFrame(){
 		clientLoginFrame = new JFrame("Client-Login");
 		clientLoginFrame.setResizable(false);
 		
		downButton = new JButton("Chat schlieen");
 		name = new JTextField();
 		ip = new JTextField();
 		port = new JTextField();
 		
 		loginButton = new JButton("Chat starten");
 		header = new JLabel("Chat-Login");
 		header.setFont(new Font("Impact", Font.BOLD,40));
 		labelName = new JLabel("Bitte Nicknamen eingeben:");
 		labelIP = new JLabel("Bitte IP eingeben:");
 		labelPORT = new JLabel("Bitte Server-Port eingeben:");
 		labelError = new JLabel("");
 		JPanel panel = new JPanel();
 		panel.add(header);panel.add(name);panel.add(ip);panel.add(port);panel.add(labelName);
 		panel.add(labelIP);panel.add(labelPORT);panel.add(labelError);
 		panel.add(loginButton);panel.add(downButton);
 		
 		downButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent event){
             	clientLoginFrame.dispose();
                 System.exit(0);
             }
         });
 		
 		header.setBounds(30,20,280,80); //(int x, int y, int width, int height) 
 		
 		labelName.setBounds(120,80,250,60);
 		name.setBounds(280,95,100,30);
 		name.setText("");
 		
 		labelIP.setBounds(120,120,250,60);
 		ip.setBounds(280,135,100,30);
 		ip.setText("127.0.0.1");
 		
 		labelPORT.setBounds(120,160,250,60);
 		port.setBounds(280,175,100,30);
 		port.setText("50000");
 		
 		loginButton.setBounds(170,220,180,30);
 		downButton.setBounds(350,220,180,30);
 		
         loginButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent event){
 				logMEin();
             }
         });
 		enter(name, true);
 		enter(ip, true);
 		enter(port, true);
 
         clientLoginFrame.pack();
 		clientLoginFrame.add(panel);
 		panel.setLayout(null);
 		clientLoginFrame.setSize(590, 325);
 	    clientLoginFrame.setVisible(true);
 		clientLoginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 	
 
 	//Erzeugen des Chatframes
 	private void showChatFrame() throws MalformedURLException {
 		chatFrame = new JFrame("Client-Chatframe");
 		chatFrame.setResizable(false);
 		
 		header2 = new JLabel("Chat");
 		header2.setFont(new Font("Impact", Font.BOLD,40));
 		
 		submitButton = new JButton("Submit");
 		logoutButton = new JButton("Logout");
 		
 		chatArea 	= new JTextPane();
 		chatField 	= new JTextField(300);
 		enter(chatField, false);
 
 		chatAreaScrollable = new JScrollPane(chatArea,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 	    //SmileyButton
 		sourire = new URL("http://www.friegel-design.de/dl/dakosmileys/sourire.gif");
 	    icon = new ImageIcon(sourire);
 	    iconButton = new JButton(icon);
 	    iconButton.addActionListener(new ActionListener() {
 	      public void actionPerformed(final ActionEvent event) {
 	    	  if (smileys) { //Button laggt, daher verkehrte Welt :D
 	    		  iconButton.setText("aus");
 	    		  smileys = false;
 	    		  chatField.requestFocus();
 	    	  }
 	    	  else { 
 		    	  iconButton.setText("an");
 	    		  smileys = true;
 	    		  chatField.requestFocus();
 	    	  }
 	      }
 	    });
 	    
 		defaultListModel = new DefaultListModel();
 	    jList = new JList();
 	    jList.setModel(defaultListModel);
 	    
 	    jScrollPaneList = new JScrollPane(jList);
 	    jScrollPaneList.setViewportView(jList);
 		
 		JPanel chatpanel = new JPanel();
 		chatpanel.add(jScrollPaneList);chatpanel.add(chatAreaScrollable);chatpanel.add(chatField);
 		chatpanel.add(submitButton);chatpanel.add(logoutButton);chatpanel.add(header2);chatpanel.add(iconButton);
 		
 		//Netz-Elemente, von oben Links beginnend(int x, int y, int width, int height) 
 		header2.setBounds(20,20,100,50); //NEW @Raphi
 		jScrollPaneList.setBounds(150,20,220,80);
 		chatAreaScrollable.setBounds(0,120,396,180); //NEW @Raphi
 		chatField.setBounds(0,300,396,30);
 		submitButton.setBounds(100,330,100,30);
 		logoutButton.setBounds(200,330,100,30);
 		iconButton.setBounds(0,330,75,30);
 		
 		chatArea.setEditable(false);
 		submitButton.addMouseListener(new ChatListener());
 		logoutButton.addMouseListener(new ChatListener());
 		
 		doc = chatArea.getStyledDocument();
         chatFrame.pack();
 		chatFrame.add(chatpanel);
 		chatpanel.setLayout(null);
 		chatFrame.setSize(400, 390);
 		chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         chatFrame.setVisible(true);
         chatField.requestFocus(); //Setzt den Cursor in das ChatField
 	}
 
 	
 	//eigene Hilfsmethode Enter-Taste
     private void enter(JTextField label, final boolean login) {
 	    label.addKeyListener(new KeyAdapter() {
             public void keyPressed(KeyEvent e)
             {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER)
                 {
                 	if(login){
                 		logMEin();
                 	}
                 	else {
                 		if (!chatField.getText().isEmpty()) {
                         clientCommunicator.tell(userName, chatField.getText());
                         chatField.setText("");
                 		}
                 	}
                 }
             }
         });
 	}
     
     
     //eigene Hilfsmethode um Nicknames + Datum fett zu machen 
     /*private void formatSelectedText(String username, int startPos) {
         attributeSet = new SimpleAttributeSet();
         StyleConstants.setBold(attributeSet, true);
         
         int nameLenght = username.length();
         int beBoldLength = 8 + nameLenght + 1;
         
         if(startPos != 0) 
         	startPos = startPos + 2;
 
         doc = (StyledDocument) chatArea.getDocument();
        doc.setCharacterAttributes(startPos, beBoldLength, attributeSet, true); //StartPosi, Lnge der Fettzuwerdenden, Fettattribut, true^^
     }*/
     
     
     //Hilfsmethode zum Check der IP-Adresse - IP-Pattern aus http://forums.sun.com/thread.jspa?threadID=584205&start=15&tstart=0
     public final static boolean ValidateIPAddress(String  str)
     {
     	final String IP_PATTERN = "^((\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";
 		
     	if (str.matches(IP_PATTERN))
     		return true;
     	else
     		return false;
     }
 
     
     //eigene Hilfsmethode zum Check des Ports
     public boolean ValidatePort(String userPort) {
 		try {
 			int port = Integer.parseInt(userPort); //String -> int
 			if (userPort.length() > 1 && userPort.length() < 6 && port >= 00000 && port < 65536)
 				return true;
 			else return false;
 			} catch (Exception e) {
 				return false;
 			}
     }
     
     
     //eigene Hilfsmethode Smiley
     private void smiley(){
 		try {
 			sourire = new URL("http://www.friegel-design.de/dl/dakosmileys/sourire.gif");
 	    	icon = new ImageIcon(sourire);
 			Style style = doc.addStyle(null, null);
 			StyleConstants.setIcon(style, icon);
 			doc.insertString(doc.getLength(),"",chatArea.getCharacterAttributes());
 			doc.insertString(doc.getLength()," TEXT SHOULD BE IGNORED IN FAVOUR OF ICON ",style);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		catch (MalformedURLException e1) {
 			e1.printStackTrace();
 		}
     }
     
     
     //eigene Hilfsmethode "Wie oft im String?" 		
    //TODO: Auskommentierter Code by Chris in separater Klasse abspeichern, da sehr nice und aufwndig war <3
 /*	public int ContainsCount(String SearchPhrase, String SearchText) {
 		String Remains = SearchText;
 		int NewIndex = 0;
 		int Count = 0;
 		while (Remains.length() >= SearchPhrase.length())
 		{
 		NewIndex = Remains.indexOf(SearchPhrase);
 
 		    if (NewIndex >= 0)
 		    {
 		    Count++;
 		    Remains = Remains.substring(NewIndex + SearchPhrase.length());
 		    }
 		    else
 		    {
 		    return Count;
 		    }
 
 		}
 		return Count;
 		}*/
 	
 	
 	//eigene Hilfsmethode: Smileys drin?
 	private boolean containsSmileys(String text) {
 		return (text.contains("=)") ||  //Evtl noch durch String-Array ersetzen.
 					text.contains(";)") || 
 						text.contains(":-D") || 
 							text.contains(";-)") || 
 								text.contains(":-)") ||
 									text.contains(":D") || 
 										text.contains(":)")		);
 	}
     
     
     //eigene Hilfsmethode Smiley-Check
 	private String smileyCheck(String text) {
 
 		if ((this.smileys) && containsSmileys(text)) {// wenn Smileys aktiviert AND iein Smiley vor kommt
 
 			String smiley = "";
 			
 			// Falls ja, alle Smileys nacheinander durchsuchen und durch
 			// Bild ersetzen
 				for (int i = 1; i < 7; i++) {
 					
 					switch (i) { //Evtl noch durch String-Array ersetzen.
 					case 1:
						smiley = "=\\)"; // ":)" nicht mglich sonst regex.PatternSyntaxException, daher DoppelSlash
 						break;
 					case 2:
 						smiley = ";\\)";
 						break;
 					case 3:
 						smiley = ":-D";
 						break;
 					case 4:
 						smiley = ";-\\)";
 						break;
 					case 5:
 						smiley = ":-\\)";
 						break;
 					case 6:
 						smiley = ":D";
 						break;
 					}
 					text = text.replaceAll(smiley,  ":\\)"); //alle Smileys die zu ersetzen sind gleich "machen"
 				}
 				return text;
 			} 
 		else { //falls keine Smileys drin oder nicht aktiviert sind
 			try {
 				doc.insertString(doc.getLength(), text, chatArea.getCharacterAttributes());
 			} catch (Exception e) {
 				System.err.println("Exception in inserting text and icons: " + e);
 			}
 			return null;
 		}
 	}
 	
 	
 	//eigene Hilfsmethode: rekrusives abschneiden und schreiben der Nachricht mit Smileys
 	private void messageCutAndPrint(String textPart) {
 		
 		if (containsSmileys(textPart)) {
 		String smiley = ":)";
 		int pause = 2; //Whitespace, welcher anstelle Smiley entfernt werden muss
 		
 			String eins = "";
 			String zwei = "";
 
 			int start = 0;
 			int position = textPart.indexOf(smiley);
 			int ende = textPart.length();
 
			//Nachricht in zwei Hlften schneiden, wobei das Smiley abgeschnitten wird
 			eins = textPart.substring(start, position);
 			zwei = textPart.substring(position + pause, ende);
 
 			// text = eins + "bild" + zwei;
 			try {
 				doc.insertString(doc.getLength(), eins, chatArea.getCharacterAttributes()); //von links nach rechts printen bis Smiley und wieder recursiv
 				smiley(); //Smiley printen
 			} catch (Exception e) {
 				System.err.println("Exception in inserting text and icons: " + e);
 			}
 			
 			// Texte werden solange auseinander geschnitten bis alle Smileys ersetzt wurden
 			messageCutAndPrint(zwei);
 		}
 		else {
 			try {
 				doc.insertString(doc.getLength(), textPart, chatArea.getCharacterAttributes());
 			} catch (Exception e) {
 				System.err.println("Exception in inserting text and icons: " + e);
 			}
 		}
 	}
     
 
     //Nachricht und Name werden empfangen und zusammen mit der Serverzeit im Chat eingetragen
     public void onMessage(String username, String message, String time) {
    	//int startCaret = chatArea.getText().length(); //erstes Zeichen der neuen Zeile, damit man wei ab wann fett gemacht werden muss
  
 		Style style = doc.addStyle(null, null);
 		StyleConstants.setIcon(style, icon);
 		try {
 		    doc.insertString(doc.getLength(),"(",chatArea.getCharacterAttributes());
 		    doc.insertString(doc.getLength(),time,chatArea.getCharacterAttributes());
 		    doc.insertString(doc.getLength(),")",chatArea.getCharacterAttributes());
 		    doc.insertString(doc.getLength()," ",chatArea.getCharacterAttributes());
 		    doc.insertString(doc.getLength(),username,chatArea.getCharacterAttributes());
 		    doc.insertString(doc.getLength(),": ",chatArea.getCharacterAttributes());
 		    //doc.insertString(doc.getLength(),message,chatArea.getCharacterAttributes()); //Nachricht direkt unbehandelt posten
 		} catch (Exception e) {
 		    System.err.println("Exception in inserting text and icons: " + e);
 		}
     	
		//Checken ob Nachricht ein Smiley enthlt
 		String SmileyChecked = smileyCheck(message);
 		
 		//Wenn Smileys drin sind, dann ab zur CutterMethode
 		if(SmileyChecked != null)
 			messageCutAndPrint(SmileyChecked); 
 	
     	try {	
     		doc.insertString(doc.getLength(), "\n",chatArea.getCharacterAttributes());
 	} catch (Exception e) {
 	    System.err.println("Exception in inserting text and icons: " + e);
 	}
 	
 		//TODO: Datum + Name fett machen
 		//formatSelectedText(username, startCaret);
 		
 	
		//TODO: Auskommentierter Code by Chris in separater Klasse abspeichern, da sehr nice und aufwndig war <3
 	
		//EditorKit setzen und Smiley einfgen///////
 		//chatArea.setEditorKit(eKit);
 		//chatArea.setText("<IMG SRC='http://www.zuh.net/java/img/sourire.gif' />");
     	
     	/*neuer Versuch///////
 	    try {
 		    where = new URL("http://www.zuh.net/java/img/sourire.gif");
 		    icon = new ImageIcon(where);
 	    	//chatArea.setCaretPosition(chatArea.getText().length()); // muss raus, try muss in schleife-algo rein
 	        //chatArea.insertIcon(icon);
 	        //chatField();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		StyledDocument doc = chatArea.getStyledDocument();
 		Style style = doc.addStyle(null, null);
 		StyleConstants.setIcon(style, icon);
 
 		try {
 		    //chatArea.insertIcon(icon);
 		    doc.insertString(doc.getLength()," the icon should have just been inserted ",chatArea.getCharacterAttributes());
 		    doc.insertString(doc.getLength()," TEXT SHOULD BE IGNORED IN FAVOUR OF ICON ",style);
 		    //doc.insertString(doc.getLength(), "\n---\n",chatArea.getCharacterAttributes());
 		} catch (Exception e) {
 		    System.err.println("Exception in inserting text and icons: " + e);
 		}
 		*///Ende neuer Versuch///////
     	
     	
 		///////effizienter bei viel text:///////
 		/*try {
 			chatArea.getDocument().insertString(chatArea.getDocument().getLength(), "(" + time + ")" + " " + username + ": " + message + "\n", null);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}*/
         
 
     	///////Standard///////
     	//chatArea.setText(chatArea.getText()+ "(" + time + ")" + " " + username + ": " + message + "\n"); //NEW @Raphi: chatArea.getText()+ 
 		
 		autoscroll();
         chatField.requestFocus();
         chatFrame.setVisible(true);
 
         //jScrollPane.getVerticalScrollBar().getModel().setValue(0);  
     }
     
     
     //eigene Hilfsmethode um automatisch nach unten zu scrollen
 	public void autoscroll() {
 		Point point = new Point( 0, (int)(chatArea.getSize().getHeight()) );
 		chatAreaScrollable.getViewport().setViewPosition( point );
 	}
     
 
     //Userliste wird an fillUserList weitergegeben
     public void onUserListUpdate(String[] userList) {
         fillUserList(new Vector<String>(Arrays.asList(userList)));
         chatFrame.setVisible(true);
     }
 
     
     //aktualisiert die Userliste
     private void fillUserList(Vector<String> names) {
    	jList.removeAll();
     	for (int i = 0; i < names.size(); i++) {
             if (names.get(i).equals(userName)) {
             	defaultListModel.addElement(new String("<" + names.get(i) + ">"));
     			jList.ensureIndexIsVisible(defaultListModel.size() - 1); }
             else { defaultListModel.addElement(new String(names.get(i) + " ")); }  	
     	}
     }   
     
     
     //Wenn der Name schon vorhanden ist -> Errormeldung im Chat
     public void onAction(int actionId, String reserved) {
         if (actionId == ChatAction.CHATACTION_USERNAME_SCHON_VERGEBEN) {
             chatArea.setText("Der Username ist schon vergeben. Bitte loggen Sie sich aus und starten Sie die Anwendung neu!");
             chatField.requestFocus();
         }
     }
 
 
     //Ctor, Logdatei einstellen + Loginframe anzeigen
     public Client() {
         PropertyConfigurator.configureAndWatch("log4j.properties", 60 * 1000);
         showLoginFrame();
 		log.debug("Chat gestartet");
     }
 
     
     //MAIN
     public static void main(String[] args) throws AWTException {
         new Client();
     }
 
     
     //Login-Methode (vorher LoginListener)
     public void logMEin() {
 	    Border thickBorder = new LineBorder(Color.WHITE, 2);
 	    name.setBorder(thickBorder);
 	    ip.setBorder(thickBorder);
 	    port.setBorder(thickBorder);
 	    thickBorder = new LineBorder(Color.RED, 2);
 	    
             boolean ok = true;
         	String error = "";
         	userName = name.getText();
             userIP = ip.getText();
             userPort = port.getText();
         	
         	if (name.getText().isEmpty()) {
         		error += " Name ";
         		ok = false;
         		name.setBorder(thickBorder);
             }
         	if (userIP.isEmpty() | !ValidateIPAddress(userIP)) {
             	error += "IP ";
             	ok = false;
         	    ip.setBorder(thickBorder);
 	            }
     		if (!ValidatePort(userPort)) {
                 	error += "Port ";
                 	ok = false;
             	    port.setBorder(thickBorder);
     			}
             
             if (ok) {
             int portNumber = Integer.parseInt(userPort);
             clientLoginFrame.dispose();
             try {
 				showChatFrame();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
             login(userName,userIP,portNumber);
             }
         }
     
     
     //Methode Login und Logout zust�ndig
     class ChatListener extends MouseAdapter {
         public void mouseClicked(MouseEvent e) {
             if (e.getSource() == submitButton) {
             	if (!chatField.getText().isEmpty()) {
                 clientCommunicator.tell(userName, chatField.getText());
                 chatField.setText("");
             	}
                 chatField.requestFocus();
             } else if (e.getSource() == logoutButton) {
                 clientCommunicator.logout();
 				chatFrame.dispose();
                 System.exit(0);
             }
         }
     }
     
     
     //LoginTO
 	public void login(String userName, String remoteAdress, int remotePort) {
 		log.debug(userName + " meldet sich im Chat an mit Remote-Addy: " +  remoteAdress + " und Port: "+ remotePort);
 		System.out.println();
 		ChatClientServiceFactoryImpl clServFac = new ChatClientServiceFactoryImpl();	
 		try {
 			chatClientService = clServFac.register(50000); //Registriet mal fest 50000 als Listener
 			chatClientService.create(remoteAdress, remotePort, userName); //Baut eine Session mit dem angegeben Partner auf
 			clientCommunicator = new ClientCommunicator(this, chatClientService, userName);
 			} catch (ChatServiceException e) {
 				new ChatServiceException("Verbindungsfehler.");
 			}
 	}
 } // Client
