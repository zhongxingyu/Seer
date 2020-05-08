 //package start;
 
 import java.io.*;
 import java.net.*;
 import java.security.KeyStore;
 import javax.net.ssl.*;
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 import java.awt.event.*;
 
 class ExtraThread extends Thread{
 		BufferedReader incoming;
          ExtraThread(BufferedReader in) {
              this.incoming = in;
          }
  
          public void run() {
 			 try {
 				 String inputLine;
 	 			while ((inputLine = this.incoming.readLine()) != null && !inputLine.equals("Bye.")) {
 			
 	 				System.out.println(inputLine);
 	 			}
 				 } catch(IOException ioe) {
 				 }
 			 
          }
      }
 
 
 
 public class SecureAdditionClient implements ActionListener {
 
 	public SecureAdditionClient () {
 
 		}
 
 	
 	private InetAddress host;
 	// This is not a reserved port number 
 	static final int port = 8189;
 	static final String KEYSTORE = "ClientKeystore.ks";
 	static final String TRUSTSTORE = "ClientTruststore.ks";
 	static final String STOREPASSWD = "111111";
 	static final String ALIASPASSWD = "333333";
 	private BufferedReader in = null;
 	private PrintWriter out = null;
 	private Vector<JRadioButton> buttonList = null;
 	private JPanel alternatives;
 	private JRadioButton alt1;
 	private JButton voteButton;
 	private JLabel results;
 	private ButtonGroup buttonGroup;
   
 	
 	public SecureAdditionClient( InetAddress host ) {
 		this.host = host;
 	}
 
 //GUI 
 	private void createAndShowGUI()
 	{
 		String inStr = "";
 		int nrOfArguments = 0;
 		buttonList = new Vector<JRadioButton>();
 
 		try {
 		//Window
 		JFrame frame = new JFrame("VotingMachine");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		//Layout
 		JPanel contentPane = new JPanel(new BorderLayout());
 
 		//Label
 		this.out.println("fetch_question"); //ask for a question from server
 		inStr = this.in.readLine();
 		CommandParser question = new CommandParser(inStr);
 		JLabel label = new JLabel(question.next_argument());
 		contentPane.add(label, BorderLayout.PAGE_START);
 
 		//Buttons
 		this.out.println("fetch_alternatives");
 		inStr= this.in.readLine(); 
 		CommandParser command = new CommandParser(inStr);
 	
 		//buttongroup
 		buttonGroup = new ButtonGroup();
 		alternatives = new JPanel();
 		alternatives.setLayout(new GridLayout(command.count_arguments(),1));
 		
 		System.out.println(command.count_arguments());
 		for (int i = 0; i<command.count_arguments(); i++)
 		{
 		String arg = command.next_argument();
 		System.out.println(arg);
 		JRadioButton temp = new JRadioButton(arg);
 		buttonList.add(temp);
 		buttonGroup.add(temp);
 		alternatives.add(temp);
 		}
 		
 		contentPane.add(alternatives , BorderLayout.CENTER);
 			
 				
 		//VoteButton
 		voteButton = new JButton("Vote!");
 		voteButton.addActionListener(this);
 		contentPane.add(voteButton, BorderLayout.EAST);
 		
 		//VotingResults
 		results = new JLabel("Resultat");
 		contentPane.add(results, BorderLayout.SOUTH);
 
 		
 		
 		frame.setContentPane(contentPane);		
 		
 
 		//Container
 		
 		//frame.getContentPane().setLayout(new BorderLayout());
 		//frame.getContentPane().add(pnlEast, BorderLayout.CENTER);
 
 		//Display the window
 		frame.pack();
 		frame.setVisible(true);
 		}
 		
 		catch(Exception e)
 		{
 
 		}
 	}
 
 	//End of GUI
 	
 //Lyssnarmetod
 
 	public void actionPerformed(ActionEvent e) {
 	
 
 			 if(e.getSource() == voteButton)
 			{	
 				System.out.println("Du har röstat!");
 			}
 			
 		}
 
 
   // The method used to start a client object
 	public void run() {
 		try {
 			KeyStore ks = KeyStore.getInstance( "JCEKS" );
 			ks.load( new FileInputStream( KEYSTORE ), STOREPASSWD.toCharArray() );
 			
 			KeyStore ts = KeyStore.getInstance( "JCEKS" );
 			ts.load( new FileInputStream( TRUSTSTORE ), ALIASPASSWD.toCharArray() );
 			
 			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
 			kmf.init( ks, STOREPASSWD.toCharArray() );
 			
 			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
 			tmf.init( ts );
 			
 			SSLContext sslContext = SSLContext.getInstance( "TLS" );
 			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
 			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
 			SSLSocket clientSocket =  (SSLSocket)sslFact.createSocket(host, port);
 			//clientSocket.setEnabledCipherSuites( clientSocket.getSupportedCipherSuites() );
 			String[] ciphers = clientSocket.getSupportedCipherSuites();
 			String[] selectedCiphers = { ciphers[9] };
 			System.out.println(ciphers[9]);
 			clientSocket.setEnabledCipherSuites( selectedCiphers );
 			
 			
 			this.in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
 			this.out = new PrintWriter( clientSocket.getOutputStream(), true );
 			
 			//ExtraThread incThread = new ExtraThread(in);
 			//incThread.start();
 			
 
 			
 
 		}
 		catch( Exception x ) {
 			System.out.println( x );
 			x.printStackTrace();
 		}
 	}
 	
 	
 	public static void main( String[] args ) {
 
 
 
 		SecureAdditionClient client = new SecureAdditionClient(); 
<<<<<<< HEAD
		//client.createAndShowGUI();
		client.run();
=======
 
 		client.run();
 	
 		client.createAndShowGUI();
		
 
>>>>>>> Fix
 		/*try {
 			InetAddress host = InetAddress.getLocalHost();
 			if ( args.length > 0 ) {
 				host = InetAddress.getByName( args[0] );
 			}
 			SecureAdditionClient addClient = new SecureAdditionClient( host );
 			addClient.run();
 		}
 		catch ( UnknownHostException uhx ) {
 			System.out.println( uhx );
 			uhx.printStackTrace();
 		}*/
 	}
 }
