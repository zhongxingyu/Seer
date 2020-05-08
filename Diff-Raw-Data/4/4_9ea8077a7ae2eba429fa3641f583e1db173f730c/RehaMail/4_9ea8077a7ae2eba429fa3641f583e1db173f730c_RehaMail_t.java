 package rehaMail;
 
 
 
 
 
 
 
 
 
 
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.jdesktop.swingworker.SwingWorker;
 
 import RehaIO.RehaIOMessages;
 import RehaIO.RehaReverseServer;
 import RehaIO.SocketClient;
 import Tools.INIFile;
 import Tools.SqlInfo;
 import Tools.Verschluesseln;
 import ag.ion.bion.officelayer.application.IOfficeApplication;
 import ag.ion.bion.officelayer.application.OfficeApplicationException;
 import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
 import ag.ion.bion.officelayer.document.DocumentException;
 import ag.ion.bion.officelayer.document.IDocument;
 import ag.ion.bion.officelayer.event.ITerminateEvent;
 import ag.ion.bion.officelayer.event.VetoTerminateListener;
 
 public class RehaMail implements WindowListener {
 
 	/**
 	 * @param args
 	 */
 	/**
 	 * @param args
 	 */
 	public static boolean DbOk;
 	JFrame jFrame;
 	public static JFrame thisFrame = null;
 	public Connection conn;
 	public static RehaMail thisClass;
 	
 	public static IOfficeApplication officeapplication;
 	
 	public String dieseMaschine = null;
 	/*
 	public static String dbIpAndName = null;
 	public static String dbUser = null;
 	public static String dbPassword = null;
 	
 	
 */
 	public final Cursor wartenCursor = new Cursor(Cursor.WAIT_CURSOR);
 	public final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
 
 	public static String dbIpAndName = "jdbc:mysql://192.168.2.2:3306/rtadaten";
 	public static String dbUser = "rtauser";
 	public static String dbPassword = "rtacurie";
 	public static String officeProgrammPfad = "C:/Program Files (x86)/LibreOffice 3";
 	//public static String officeProgrammPfad = "C:/Program Files (x86)/OpenOffice.org 3";
 	public static String officeNativePfad = "C:/RehaVerwaltung/Libraries/lib/openofficeorg/";
 	public static String progHome = "C:/RehaVerwaltung/";
 	public static String aktIK = "510841109";
 	public static String hmRechnungPrivat = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	public static String hmRechnungKasse = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	public static String rhRechnungPrivat = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	public static String rhRechnungKasse = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	/*
 	public static String dbIpAndName = "jdbc:mysql://192.168.2.2:3306/rtadaten";
 	public static String dbUser = "rtauser";
 	public static String dbPassword = "rtacurie";
 	public static String officeProgrammPfad = "C:/Programme/OpenOffice.org 3";
 	public static String officeNativePfad = "C:/RehaVerwaltung/Libraries/lib/openofficeorg/";
 	public static String progHome = "C:/RehaVerwaltung/";
 	public static String aktIK = "510841109";
 	public static String hmRechnungPrivat = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	public static String hmRechnungKasse = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	public static String rhRechnungPrivat = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	public static String rhRechnungKasse = "C:/RehaVerwaltung/vorlagen/HMRechnungPrivatKopie.ott";
 	*/
 		
 	public static int xport = 7000;
 	public static boolean xportOk = false;
 	public RehaReverseServer rehaReverseServer = null;
 	public static int rehaReversePort = 6000;
 	
 	public static Vector<Vector<String>> einzelMail = null;
 	public static Vector<Vector<String>> gruppenMail = null;
 	
 	public static String mailUser = "Jürgen Steinhilber";
 	
 	public static Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
 	public static Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
 	public final Cursor cmove = new Cursor(Cursor.MOVE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cnsize = new Cursor(Cursor.N_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cnwsize = new Cursor(Cursor.NW_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cnesize = new Cursor(Cursor.NE_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cswsize = new Cursor(Cursor.SW_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cwsize = new Cursor(Cursor.W_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor csesize = new Cursor(Cursor.SE_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cssize = new Cursor(Cursor.S_RESIZE_CURSOR);  //  @jve:decl-index=0:
 	public final Cursor cesize = new Cursor(Cursor.E_RESIZE_CURSOR);  //  @jve:decl-index=0:	
 	public final Cursor cdefault = new Cursor(Cursor.DEFAULT_CURSOR);  //  @jve:decl-index=0:	
 	
 	public static int BenutzerSuper_user 	= 2;
 	public static int Sonstiges_NachrichtenLoeschen		= 102;
 	public static String progRechte = "";
 	
 	public static ImageIcon[] attachmentIco = {null,null,null,null,null,null,null};
 	public static int toolsDlgRueckgabe;
 	public MailTab mtab = null;
 	//public MailPanel mpanel = null;
 	public static ImageIcon[] icoPinPanel = {null,null,null};
 
 	public static String pdfReader = null;
 	
 	public static String sTitle;
 	public static int testint = 1;
 
 	public static Timer nachrichtenTimer = null;
 	public static boolean nachrichtenLaeuft = false;
 	public static boolean nachrichtenInBearbeitung = false;
 	public static long timerdelay = 600000;
 	public static boolean timerpopup = true;
 	public static boolean timerprogressbar = true;
 	
 	public static boolean testcase = false;
 	
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
 			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
 	        UIManager.put("TabbedPane.tabsOpaque", Boolean.FALSE);
 		} catch (ClassNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (InstantiationException e1) {
 			e1.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			e1.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e1) {
 			e1.printStackTrace();
 		}
 
 		
 		if(args.length > 0 || testcase){
 			if(!testcase){
 				System.out.println("hole daten aus INI-Datei "+args[0]);
 				INIFile inif = new INIFile(args[0]+"ini/"+args[1]+"/rehajava.ini");
 				dbIpAndName = inif.getStringProperty("DatenBank","DBKontakt1");
 				dbUser = inif.getStringProperty("DatenBank","DBBenutzer1");
 				String pw = inif.getStringProperty("DatenBank","DBPasswort1");
 				String decrypted = null;
 				if(pw != null){
 					Verschluesseln man = Verschluesseln.getInstance();
 					man.init(Verschluesseln.getPassword().toCharArray(), man.getSalt(), man.getIterations());
 					decrypted = man.decrypt (pw);
 				}else{
 					decrypted = new String("");
 				}
 				dbPassword = decrypted.toString();
 				inif = new INIFile(args[0]+"ini/"+args[1]+"/rehajava.ini");
 				officeProgrammPfad = inif.getStringProperty("OpenOffice.org","OfficePfad");
 				officeNativePfad = inif.getStringProperty("OpenOffice.org","OfficeNativePfad");
 				progHome = args[0];
 				aktIK = args[1];
 				if(args.length >= 3){
 					rehaReversePort = Integer.parseInt(args[2]);
 				}
 				if(args.length >= 4){
 					mailUser = args[3].replace("#", " ");
 				}
 
 				inif = new INIFile(args[0]+"ini/"+args[1]+"/nachrichten.ini");
 				timerdelay = inif.getLongProperty("RehaNachrichten", "NachrichtenTimer");
 				timerpopup = (inif.getIntegerProperty("RehaNachrichten", "NachrichtenPopUp") <= 0 ? false : true);
 				timerprogressbar = (inif.getIntegerProperty("RehaNachrichten", "NachrichtenProgressbar") <= 0 ? false : true);
 				
 				inif = new INIFile(RehaMail.progHome+"ini/"+RehaMail.aktIK+"/fremdprog.ini");
 				pdfReader = inif.getStringProperty("FestProg", "FestProgPfad1");
 				
 			}
 			
 				
 			RehaMail application = new RehaMail();
 			application.getInstance();
 
 			final RehaMail xapplication = application;
 			new SwingWorker<Void,Void>(){
 				@Override
 				protected Void doInBackground() throws java.lang.Exception {
 					try{
 						
 					}catch(Exception ex){
 						ex.printStackTrace();
 					}
 					xapplication.starteDB();
 					long zeit = System.currentTimeMillis();
 					while(! DbOk){
 						try {
 							Thread.sleep(20);
 							if(System.currentTimeMillis()-zeit > 5000){
 								System.exit(0);
 							}
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					/*********************************/
 							
 							Verschluesseln man = Verschluesseln.getInstance();
 							man.init(Verschluesseln.getPassword().toCharArray(), man.getSalt(), man.getIterations());
 							einzelMail = SqlInfo.holeFelder("select user,rights,id from rehalogin");
 							for(int i = 0; i < einzelMail.size();i++){
 								einzelMail.get(i).set(0,man.decrypt(einzelMail.get(i).get(0)));
 								einzelMail.get(i).set(1,man.decrypt(einzelMail.get(i).get(1)));
 							}
 							Vector<String> vdummy = new Vector<String>();
 							vdummy.add("");
 							vdummy.add("kein Passwort");
 							einzelMail.insertElementAt((Vector<String>)vdummy.clone(), 0);
 							Comparator<Vector<String>> comparator = new Comparator<Vector<String>>() {
 								@Override
 								public int compare(Vector<String> o1, Vector<String> o2) {
 									String s1 = (String)o1.get(0);
 									String s2 = (String)o2.get(0);
 									return s1.compareTo(s2);
 								}
 							};
 							Collections.sort(einzelMail,comparator);
 							gruppenMail = SqlInfo.holeFelder("select groupname,groupmembers,id from pimailgroup");
 							Collections.sort(gruppenMail,comparator);
 							setRechte();
 							//System.out.println(einzelMail);
 							//System.out.println(gruppenMail);
 					/*********************************/
 					if(!DbOk){
 						JOptionPane.showMessageDialog(null, "Datenbank konnte nicht geöffnet werden!\\nReha-Sql kann nicht gestartet werden");				
 					}
 					xapplication.getJFrame();
 					if(timerdelay > 0){
 						xapplication.starteTimer();	
 					}
 					
 					return null;
 				}
 				
 			}.execute();
 			new Thread(){
 				public void run(){
 					try {
 						RehaMail.starteOfficeApplication();
 					} catch (OfficeApplicationException e) {
 						e.printStackTrace();
 					}		
 				}
 			}.start();
 
 			
 
 			
 		}else{
 		
 			
 			JOptionPane.showMessageDialog(null, "Keine Datenbankparameter übergeben!\\nReha-Sql kann nicht gestartet werden");
 			System.exit(0);
 			
 		}
 		
 	}
 	public static void setRechte(){
 		for(int i = 0; i < einzelMail.size();i++){
 			if(einzelMail.get(i).get(0).trim().equals(RehaMail.mailUser)){
 				RehaMail.progRechte = String.valueOf(einzelMail.get(i).get(1));
 				break;
 			}
 		}
 	}
 	public MailTab getMTab(){
 		return mtab;
 	}
 	public void starteTimer(){
 		RehaMail.nachrichtenTimer = new Timer();
 		TimerTask task = new TimerTask() {
 			public void run() {
 				if(!nachrichtenInBearbeitung){
 					try{
 					nachrichtenInBearbeitung = true;
 					/**************/
 					if( (!RehaMail.mailUser.equals("")) && 
 							(!SqlInfo.holeEinzelFeld("select gelesen from pimail where empfaenger_person ='"+
 									RehaMail.mailUser+"' and gelesen='F' LIMIT 1").trim().equals("")) ){
 							getMTab().mailPanel.checkForNewMail(true);
 							//getMTab().mailPanel.allesAufNull();							
 						SwingUtilities.invokeAndWait(new Runnable(){
 							public void run(){
 								RehaMail.thisFrame.setVisible(true);		
 							}
 						});
 					}
 					}catch(Exception ex){
 						ex.printStackTrace();
 					}
 					nachrichtenInBearbeitung = false;
 				}
 			}
 		};
 		//start des Timers:
 		RehaMail.nachrichtenTimer.scheduleAtFixedRate(task, RehaMail.timerdelay, RehaMail.timerdelay);
 	}
 	
 	/********************/
 	
 	public JFrame getJFrame(){
 		try {
 			UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		thisClass = this;
 		createIcons();
 		jFrame = new JFrame(){
 			
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void setVisible(final boolean visible) {
 				
 				if(getState()!=JFrame.NORMAL) { setState(JFrame.NORMAL); }
 
 				  if (visible) {
 				      //setDisposed(false);
 				  }
 				  if (!visible || !isVisible()) { 
 				      super.setVisible(visible);
 				  }
 
 				  if (visible) {
 				      int state = super.getExtendedState();
 				      state &= ~JFrame.ICONIFIED;
 				      super.setExtendedState(state);
 				      super.setAlwaysOnTop(true);
 				      super.toFront();
 				      super.requestFocus();
 				      super.setAlwaysOnTop(false);
 				  }
 			}
 
 			@Override
 			public void toFront() {
 				  super.setVisible(true);
 				  int state = super.getExtendedState();
 				  state &= ~JFrame.ICONIFIED;
 				  super.setExtendedState(state);
 				  super.setAlwaysOnTop(true);
 				  super.toFront();
 				  super.requestFocus();
 				  super.setAlwaysOnTop(false);
 			}	
 		};	
 		try{
 			rehaReverseServer = new RehaReverseServer(7000);
 		}catch(Exception ex){
 			rehaReverseServer = null;
 		}
 		thisFrame = jFrame;
 		jFrame.addWindowListener(this);
 		jFrame.setSize(800,650);
 		jFrame.setPreferredSize(new Dimension(800,650));
 		sTitle = "Thera-Pi Nachrichten --> [IK: "+aktIK+"] --> [Benutzer: ";
 		jFrame.setTitle(sTitle+RehaMail.mailUser+"]");
 
 		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		jFrame.setLocationRelativeTo(null);
 		/****************/
 		//jFrame.getContentPane().add (mpanel=new MailPanel());
 		
 		jFrame.getContentPane().add (mtab=new MailTab(this));
 		jFrame.pack();
 		
 		jFrame.setIconImage( Toolkit.getDefaultToolkit().getImage( RehaMail.progHome+"icons/emblem-mail.png" ) );
 		jFrame.setVisible(true);
 
 		
 		try{
 			new SocketClient().setzeRehaNachricht(RehaMail.rehaReversePort,"AppName#RehaMail#"+Integer.toString(RehaMail.xport));
 			new SocketClient().setzeRehaNachricht(RehaMail.rehaReversePort,"RehaMail#"+RehaIOMessages.IS_STARTET);
 		}catch(Exception ex){
 			JOptionPane.showMessageDialog(null, "Fehler in der Socketkommunikation");
 		}
 
 		return jFrame;
 	}
 	private void createIcons(){
 		Image ico = new ImageIcon(RehaMail.progHome+"icons/pdf.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[0] = new ImageIcon(ico);
 		ico = new ImageIcon(RehaMail.progHome+"icons/ooo-writer.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[1] = new ImageIcon(ico);
 		ico = new ImageIcon(RehaMail.progHome+"icons/ooo-calc.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[2] = new ImageIcon(ico);
 		ico = new ImageIcon(RehaMail.progHome+"icons/document-save-as.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[3] = new ImageIcon(ico);
 		ico = new ImageIcon(RehaMail.progHome+"icons/application-exit.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[4] = new ImageIcon(ico);
 		ico = new ImageIcon(RehaMail.progHome+"icons/stock_inbox.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[5] = new ImageIcon(ico);
 		ico = new ImageIcon(RehaMail.progHome+"icons/stock_outbox.png").getImage().getScaledInstance(26,26, Image.SCALE_SMOOTH);
 		attachmentIco[6] = new ImageIcon(ico);
 		
 		icoPinPanel[0] = new ImageIcon(RehaMail.progHome+"icons/red.png");
 		icoPinPanel[1] = new ImageIcon(RehaMail.progHome+"icons/buttongreen.png");
 		icoPinPanel[2] = new ImageIcon(RehaMail.progHome+"icons/inaktiv.png");
 
 	}
 
 	
 	public static void updateTitle(String user){
 		RehaMail.mailUser = String.valueOf(user);
 		RehaMail.thisFrame.setTitle(RehaMail.sTitle+RehaMail.mailUser+"]");
 	}
 	/********************/
 	
 	public RehaMail getInstance(){
 		thisClass = this;
 		return this;
 	}
 	
 	/*******************/
 	
 	public void starteDB(){
 		
 		//piHelpDatenbankStarten dbstart = new piHelpDatenbankStarten();
 		//dbstart.run(); 			
 		
 		DatenbankStarten dbstart = new DatenbankStarten();
 		dbstart.run();
 		 			
 	}
 	
 	/*******************/
 	
 	public static void stoppeDB(){
 		try {
 			RehaMail.thisClass.conn.close();
 			RehaMail.thisClass.conn = null;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} 			
 	}
 	
 	/**********************************************************
 	 * 
 	 */
 	final class DatenbankStarten implements Runnable{
 		private void StarteDB(){
 			final RehaMail obj = RehaMail.thisClass;
 
 			final String sDB = "SQL";
 			if (obj.conn != null){
 				try{
 				obj.conn.close();}
 				catch(final SQLException e){}
 			}
 			try{
 				Class.forName("com.mysql.jdbc.Driver").newInstance();
 	        } catch (InstantiationException e) {
 				e.printStackTrace();
         		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
         		RehaMail.DbOk = false;
 	    		return ;
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
         		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
         		RehaMail.DbOk = false;
 	    		return ;
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
         		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
         		RehaMail.DbOk = false;
 	    		return ;
 			}	
         	try {
         		
    				obj.conn = (Connection) DriverManager.getConnection(dbIpAndName,dbUser,dbPassword);
 				RehaMail.DbOk = true;
     			System.out.println("Datenbankkontakt hergestellt");
         	} 
         	catch (final SQLException ex) {
         		System.out.println("SQLException: " + ex.getMessage());
         		System.out.println("SQLState: " + ex.getSQLState());
         		System.out.println("VendorError: " + ex.getErrorCode());
         		RehaMail.DbOk = false;
         
         	}
 	        return;
 		}
 		public void run() {
 			StarteDB();
 		}
 	
 	
 	}
 	/*****************************************************************
 	 * 
 	 */
 	/**********************************************************
 	 * 
 	 */
 	final class piHelpDatenbankStarten implements Runnable{
 		private void StarteDB(){
 			final RehaMail obj = RehaMail.thisClass;
 
 			final String sDB = "SQL";
 			if (obj.conn != null){
 				try{
 				obj.conn.close();}
 				catch(final SQLException e){}
 			}
 			try{
 				Class.forName("de.root1.jpmdbc.Driver");
 				//Class.forName("com.mysql.jdbc.Driver").newInstance();
 	         
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(null, "Kein Kontakt zu MySql bei 1 & 1");
         		System.out.println(sDB+"Treiberfehler: " + e.getMessage());
         		RehaMail.DbOk = false;
 	    		return ;
 			}	
         	try {
     			System.out.println("Starte de.root1.jpmdbc.Drive");
     			//System.out.println("Starte Serveradresse:192.168.2.2");
     			//obj.connMySql = (Connection) DriverManager.getConnection("jdbc:mysql://192.168.2.2:3306/dbf","entwickler","entwickler");
     			Properties connProperties = new Properties();
     			connProperties.setProperty("user", "dbo336243054");
     			connProperties.setProperty("password", "allepreise");
     			//connProperties.setProperty("host", "localhost");
     			connProperties.setProperty("host", "db2614.1und1.de");	        			
     			//connProperties.setProperty("host", "db2614.1und1.de");
     			connProperties.setProperty("port", "3306");
     			connProperties.setProperty("compression","false");
     			connProperties.setProperty("NO_DRIVER_INFO", "1");
 
     			obj.conn = (Connection) DriverManager.getConnection("jdbc:jpmdbc:http://www.thera-pi.org/jpmdbc.php?db336243054",connProperties);
         		
    				//obj.conn = (Connection) DriverManager.getConnection(dbIpAndName,dbUser,dbPassword);
 				RehaMail.DbOk = true;
     			System.out.println("Datenbankkontakt hergestellt");
         	} 
         	catch (final SQLException ex) {
 				JOptionPane.showMessageDialog(null, "Kein Kontakt zu MySql bei 1 & 1");
         		System.out.println("SQLException: " + ex.getMessage());
         		System.out.println("SQLState: " + ex.getSQLState());
         		System.out.println("VendorError: " + ex.getErrorCode());
         		RehaMail.DbOk = false;
         
         	}
 	        return;
 		}
 		public void run() {
 			StarteDB();
 		}
 	
 	
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 	}
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 		if(RehaMail.thisClass.conn != null){
 			try {
 				RehaMail.thisClass.conn.close();
 				System.out.println("Datenbankverbindung wurde geschlossen");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		System.exit(0);
 	}
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		if(RehaMail.thisClass.conn != null){
 			try {
 				RehaMail.thisClass.conn.close();
 				System.out.println("Datenbankverbindung wurde geschlossen");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		if(RehaMail.thisClass.mtab.getMailPanel() != null){
 		}
 
 		if(RehaMail.thisClass.mtab.getSendPanel() != null){
 		}
 		
 		if(RehaMail.thisClass.rehaReverseServer != null){
 			try{
 				new SocketClient().setzeRehaNachricht(RehaMail.rehaReversePort,"RehaMail#"+RehaIOMessages.IS_FINISHED);
 			}catch(Exception ex){
 				ex.printStackTrace();
 			}
 			try{
 				rehaReverseServer.serv.close();
 				System.out.println("SocketServer wurde geschlossen");
 			}catch(Exception ex){
 				ex.printStackTrace();
 			}
 			
 		}
 		if(RehaMail.nachrichtenTimer != null){
 			RehaMail.nachrichtenTimer.cancel();
 			RehaMail.nachrichtenLaeuft = false;
 			RehaMail.nachrichtenTimer = null;
 			System.out.println("Nachrichten-Timer beendet");
 		}						
 
 		
 		System.exit(0);
 	}
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 	}
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {
 	}
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 	}
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 	}
 	
 	/**
 	 * @throws Throwable *************************/
 	
     public static void starteOfficeApplication() throws OfficeApplicationException{ 
 
  
 
         	final String OPEN_OFFICE_ORG_PATH = RehaMail.officeProgrammPfad;
 
             try
             {
             	//System.out.println("**********Open-Office wird gestartet***************");
                 String path = OPEN_OFFICE_ORG_PATH;
                 Map <String, String>config = new HashMap<String, String>();
                 config.put(IOfficeApplication.APPLICATION_HOME_KEY, path);
                 config.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
                 System.setProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH,RehaMail.officeNativePfad);
                 officeapplication = OfficeApplicationRuntime.getApplication(config);
                 officeapplication.activate();
                 officeapplication.getDesktopService().addTerminateListener(new VetoTerminateListener() {
                 	  public void queryTermination(ITerminateEvent terminateEvent) {
                 	    super.queryTermination(terminateEvent);
                 	    try {
                 	      IDocument[] docs = officeapplication.getDocumentService().getCurrentDocuments();
                 	      if (docs.length == 1) { 
                 	        docs[0].close();
                 	        //System.out.println("Letztes Dokument wurde geschlossen");
                 	      }
                 	    }
                 	    catch (DocumentException e) {
                 	    	e.printStackTrace();
                 	    } catch (OfficeApplicationException e) {
     						e.printStackTrace();
     					}
                 	  }
                 	});
             }
             catch (OfficeApplicationException e) {
                 e.printStackTrace();
             }
             System.out.println("OpenOffice ist gestartet und Active ="+officeapplication.isActive());
         }
     
     public final static String notread =
     "{\\rtf1\\ansi\\deff0\\adeflang1025"
     		+"{\\fonttbl{\\f0\\froman\\fprq2\\fcharset0 Times New Roman;}{\\f1\\froman\\fprq2\\fcharset2 Symbol;}{\\f2\\fswiss\\fprq2\\fcharset0 Arial;}{\\f3\\fswiss\\fprq2\\fcharset128 Arial;}{\\f4\\fnil\\fprq2\\fcharset0 Microsoft YaHei;}{\\f5\\fnil\\fprq2\\fcharset0 Mangal;}{\\f6\\fnil\\fprq0\\fcharset0 Mangal;}}"
     		+"{\\colortbl;\\red0\\green0\\blue0;\\red0\\green0\\blue255;\\red128\\green128\\blue0;\\red128\\green128\\blue128;}"
     		+"{\\stylesheet{\\s0\\snext0\\nowidctlpar{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\cf0\\kerning1\\hich\\af7\\langfe2052\\dbch\\af5\\afs24\\lang1081\\loch\\f0\\fs24\\lang1031 Standard;}"
    		+"{\\s15\\sbasedon0\\snext16\\sb240\\sa120\\keepn\\hich\\af4\\dbch\\af5\\afs28\\loch\\f2\\fs28 Überschrift;}"
    		+"{\\s16\\sbasedon0\\snext16\\sb0\\sa120 Textkörper;}"
     		+"{\\s17\\sbasedon16\\snext17\\sb0\\sa120\\dbch\\af6 Liste;}"
     		+"{\\s18\\sbasedon0\\snext18\\sb120\\sa120\\noline\\i\\dbch\\af6\\afs24\\ai\\fs24 Beschriftung;}"
     		+"{\\s19\\sbasedon0\\snext19\\noline\\dbch\\af6 Verzeichnis;}"
     		+"}{\\info{\\author J\\'fcrgen Steinhilber}{\\creatim\\yr2011\\mo6\\dy7\\hr11\\min49}{\\revtim\\yr0\\mo0\\dy0\\hr0\\min0}{\\printim\\yr0\\mo0\\dy0\\hr0\\min0}{\\comment LibreOffice}{\\vern3300}}\\deftab709"
     		+"{\\*\\pgdsctbl"
     		+"{\\pgdsc0\\pgdscuse195\\pgwsxn11906\\pghsxn16838\\marglsxn1134\\margrsxn1134\\pgdscnxt0 Standard;}}"
     		+"\\formshade\\paperh16838\\paperw11906\\margl1134\\margr1134\\margt1134\\margb1134\\sectd\\sbknone\\sectunlocked1\\pgndec\\pgwsxn11906\\pghsxn16838\\marglsxn1134\\margrsxn1134\\ftnbj\\ftnstart1\\ftnrstcont\\ftnnar\\aenddoc\\aftnrstcont\\aftnstart1\\aftnnrlc"
     		+"\\pgndec\\pard\\plain \\s0\\nowidctlpar{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\cf0\\kerning1\\hich\\af7\\langfe2052\\dbch\\af5\\afs24\\lang1081\\loch\\f0\\fs24\\lang1031{\\cf2\\afs28\\rtlch \\ltrch\\loch\\fs28\\loch\\f3"
     		+"Damit Sie die Nachricht lesen k\\u246\\'f6nnen:}"
     		+"\\par \\pard\\plain \\s0\\nowidctlpar{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\cf0\\kerning1\\hich\\af7\\langfe2052\\dbch\\af5\\afs24\\lang1081\\loch\\f0\\fs24\\lang1031{\\afs28\\rtlch \\ltrch\\loch\\fs28\\loch\\f3"
     		+"}"
     		+"\\par \\pard\\plain \\s0\\nowidctlpar{\\*\\hyphen2\\hyphlead2\\hyphtrail2\\hyphmax0}\\cf0\\kerning1\\hich\\af7\\langfe2052\\dbch\\af5\\afs24\\lang1081\\loch\\f0\\fs24\\lang1031{\\cf3\\afs28\\rtlch \\ltrch\\fs28\\loch\\f3"
     		+"\\u8594\\'92 }{\\cf3\\afs28\\rtlch \\ltrch\\loch\\fs28\\loch\\f3"
     		+"Doppelklick auf die Tabellenzeile}"
     		+"\\par }";
 	
 
 }
