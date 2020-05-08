 /**
  * 
  */
 package jLanSend;
 
 import java.awt.AWTException;
 import java.awt.CheckboxMenuItem;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 
 /**
  * @author Moritz Bellach
  *
  */
 public class JLanSend extends Observable implements Observer {
 	
 	private static JLanSend jls;
 	private TrayIcon trayicon = null;
 	private PopupMenu popup;
 	private MenuItem sendStat, recvStat;
 	private MainWindow mw;
 	private Receiver receiver = null;
 	private Vector<ReceiveOp> recvOps;
 	private Vector<SendOp> sendOps;
 	private final int lprotov = 1;
 	private Detector [] detector;
 	
 	//settings:
 	private String downloaddir;
 	private String nick;
 	private boolean startReceiver;
 	private boolean startTray;
 	private boolean startAutodetection;
 	private int port;
 	
 	/**
 	 * 
 	 * @return the application object
 	 */
 	public static JLanSend getJLanSend(){
 		return jls;
 	}
 
 	/**
 	 * 
 	 */
 	public JLanSend() {
 		recvOps = new Vector<ReceiveOp>();
 		sendOps = new Vector<SendOp>();
 		new Vector<String>();
 		port = 55555;
 		nick = "no nick yet";
 		setDownloaddir("");
 		setStartReceiver(true);
 		setStartAutodetection(true);
 		setStartTray(true);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		jls = new JLanSend();
 		jls.readSettings();
 		if(jls.isStartTray()){
 			if(!jls.initTray()){
 				jls.setStartTray(false);
 				jls.writeSettings();
 			}
 		}
 		jls.initMW();
 		if(jls.isStartReceiver()){
 			jls.startReceiver(jls.getPort());
 		}
 		if(jls.isStartAutodetection()){
 			jls.initDetector();
 		}
 		
 		
 	}
 	
 	/**
 	 * starts receiver is it isn't started already
 	 * @param port
 	 */
 	public void startReceiver(int port) {
 		if(receiver == null) {
 			try {
 				receiver = new Receiver(port);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		// TODO what if thread still running?
 	}
 	
 	/**
 	 * 
 	 * @param op
 	 */
 	public void addReceiveOp(ReceiveOp op) {
 		recvOps.add(op);
 		op.addObserver(this);
 		op.startThread();
 	}
 	
 	
 	/**
 	 * 
 	 * @param op
 	 */
 	public void addSendOp(SendOp op) {
 		sendOps.add(op);
 		op.addObserver(this);
 		op.startThread();
 	}
 	
 	
 	/**
 	 * stops receiver if it isn't stopped already
 	 */
 	public void stopReceiver() {
 		if(receiver != null) {
 			receiver.stop();
 			receiver = null;
 		}
 	}
 	
 	public int getLProtoV() {
 		return lprotov;
 	}
 	
 	public int getPort() {
 		return port;
 	}
 	
 	public void setPort(int newport){
 		port = newport;
 	}
 
 	/**
 	 * initializes the systray icon
 	 * could be used again later if turning on and off the icon is allowed via settings
 	 */
 	private boolean initTray(){
 		if(SystemTray.isSupported()){
 			popup = new PopupMenu("JLanSend");
 			MenuItem send = new MenuItem("Send File");
 			sendStat = new MenuItem("Sending: 0");
 			recvStat = new MenuItem("Receiving: 0");
 			
 			MenuItem exit = new MenuItem("Exit");
 			exit.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					// TODO do this better
 					System.exit(0);
 					
 				}
 			});
 			
 			CheckboxMenuItem recv = new CheckboxMenuItem("Receive Files", true);
 			
 			popup.add(send);
 			popup.addSeparator();
 			popup.add(sendStat);
 			popup.add(recvStat);
 			popup.addSeparator();
 			popup.add(recv);
 			popup.addSeparator();
 			popup.add(exit);
 			
 			URL imageURL = JLanSend.class.getResource("icon.png");
 			trayicon = new TrayIcon(new ImageIcon(imageURL).getImage(), "JLanSend", popup);
 			trayicon.setImageAutoSize(true);
 			trayicon.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					mw.toggleVisibility();
 				}
 			});
 			SystemTray systray = SystemTray.getSystemTray();
 			try {
 				systray.add(trayicon);
 			} catch (AWTException e){
 				// free up some minimal resources
 				sendStat = null;
 				recvStat = null;
 				popup = null;
 				trayicon = null;
 			}
 			
 			
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	private void initMW(){
 		mw = new MainWindow();
 	}
 	
 	private void initDetector() {
 		detector = new Detector[8];
 		for(int i = 0; i < 8; i++){
 			detector[i] = new Detector(i);
 		}
 		
 	}
 	
 	private void readSettings(){
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("config.ini"));
 			String line;
 			while((line = in.readLine()) != null){
 				if(! (line.startsWith(";") || line.startsWith("#") || line.isEmpty())){
 					String [] lineparts = line.split("\\=");
 					if(lineparts[0].equalsIgnoreCase("port")){
 						port = Integer.parseInt(lineparts[1]);
 					}
 					else if(lineparts[0].equalsIgnoreCase("downloaddir")){
						if(lineparts.length > 1){
							setDownloaddir(lineparts[1]);
						}
						else{
							setDownloaddir("");
						}
						
 					}
 					else if(lineparts[0].equalsIgnoreCase("nick")){
 						nick = lineparts[1];
 					}
 					else if(lineparts[0].equalsIgnoreCase("startReceiver")){
 						if(lineparts[1].equalsIgnoreCase("yes")){
 							setStartReceiver(true);
 						}
 						else{
 							setStartReceiver(false);
 						}
 					}
 					else if(lineparts[0].equalsIgnoreCase("startTray")){
 						if((lineparts[1]).equalsIgnoreCase("yes")){
 							setStartTray(true);
 						}
 						else{
 							setStartTray(false);
 						}
 					}
 					else if(lineparts[0].equalsIgnoreCase("startAutodetection")){
 						if(lineparts[1].equalsIgnoreCase("yes")){
 							setStartAutodetection(true);
 						}
 						else{
 							setStartAutodetection(false);
 						}
 					}
 					else{
 						//wtf? ignore for now...
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			writeSettings();
 		} catch (IOException e) {
 			//ahm... ???
 		}
 		
 	}
 
 	/**
 	 * @param nick the nick to set
 	 */
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	/**
 	 * @return the nick
 	 */
 	public String getNick() {
 		return nick;
 	}
 	
 	
 	public String niceBytes(Long bytes) {
 		String si;
 		double b = 0;
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMinimumFractionDigits(1);
 		nf.setMaximumFractionDigits(1);
 		
 		if(bytes > (1024*1024*1024)) {
 			si = "GB";
 			b = ((double) bytes) / (1024.0*1024.0*1024.0);
 		}
 		else if(bytes > (1024*1024)) {
 			si = "MB";
 			b = ((double) bytes) / (1024.0*1024.0);
 		}
 		else if(bytes > 1024) {
 			si = "KB";
 			b = ((double) bytes) / 1024.0;
 		}
 		else {
 			si = "B";
 		}
 		if(si == "B") {
 			return Long.toString(bytes) + si;
 		}
 		else {
 			return nf.format(b) + si;
 		}
 	}
 	
 	public void addRHost(String rHost) {
 		if(mw != null){
 			mw.changeRHost(true, rHost);
 			// TODO add cli here later?
 		}
 	}
 	
 	public void delRHost(String rHost) {
 		if(mw != null){
 			mw.changeRHost(false, rHost);
 			//TODO add cli here later?
 		}
 	}
 	
 	
 	public void notifyObservers(Object o) {
 		setChanged();
 		super.notifyObservers(o);
 	}
 
 	@Override
 	public void update(Observable src, Object msg) {
 		switch ((ObsMsg) msg) {
 		case RECVSTART:
 		case SENDSTART:
 			notifyObservers(src);
 			break;
 		case REMOVEME:
 			if(src instanceof ReceiveOp) {
 				recvOps.remove(src);
 			}
 			else if(src instanceof SendOp) {
 				sendOps.remove(src);
 			}
 			else {
 				System.out.println("oO");
 			}
 			break;
 
 		default:
 			break;
 		}
 		
 	}
 	
 	public void writeSettings(){
 		File f = new File("config.ini");
 		if(f.exists()){
 			f.delete();
 		}
 		try {
 			f.createNewFile();
 			PrintWriter out = new PrintWriter(f);
 			out.println("; empty lines, line starting with ; or # are ignored");
 			out.println("; besides that, you should not put any extra spaces anywhere, since leading, trailing or in-the-middle spaces are read in. example:");
 			out.println(";");
 			out.println("; downloaddir=C:\\Users\\Adam Smith\\Downloads");
 			out.println(";");
 			out.println("; would work out of the box, but");
 			out.println(";");
 			out.println("; downloaddir = C:\\Users\\Adam Smith\\Downloads");
 			out.println(";");
 			out.println("; would not even be recognized as the download directory");
 			out.println();
 			out.println("; nick, well ... probably too many crazy charachters will mess something up");
 			out.println("nick=" + nick);
 			out.println();
 			out.println("; downloaddir may contain spaces");
 			out.println("downloaddir=" + downloaddir);
 			out.println();
 			out.println("; if you change the port then you must do so on all computers you want to exchange files between");
 			out.println("port=" + String.valueOf(port));
 			out.println();
 			out.println("; start receiving files? (recommended)");
 			out.println("startReceiver=" + (startReceiver ? "yes" : "no"));
 			out.println();
 			out.println("; start detecting other JLanSends? (recommended)");
 			out.println("startAutodetection=" + (startAutodetection ? "yes" : "no"));
 			out.println();
 			out.println("; do you want that quiet little tray icon, so do not always have to have yet another window open? Or is your System Tray so crowded you rather not?");
 			out.println("startTray=" + (startTray ? "yes" : "no"));
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * @param downloaddir the downloaddir to set
 	 */
 	public void setDownloaddir(String downloaddir) {
 		this.downloaddir = downloaddir;
 	}
 
 	/**
 	 * @return the downloaddir
 	 */
 	public String getDownloaddir() {
 		return downloaddir;
 	}
 
 	/**
 	 * @param startReceiver the startReceiver to set
 	 */
 	public void setStartReceiver(boolean startReceiver) {
 		this.startReceiver = startReceiver;
 	}
 
 	/**
 	 * @return the startReceiver
 	 */
 	public boolean isStartReceiver() {
 		return startReceiver;
 	}
 
 	/**
 	 * @param startTray the startTray to set
 	 */
 	public void setStartTray(boolean startTray) {
 		this.startTray = startTray;
 	}
 
 	/**
 	 * @return the startTray
 	 */
 	public boolean isStartTray() {
 		return startTray;
 	}
 
 	/**
 	 * @param startAutodetection the startAutodetection to set
 	 */
 	public void setStartAutodetection(boolean startAutodetection) {
 		this.startAutodetection = startAutodetection;
 	}
 
 	/**
 	 * @return the startAutodetection
 	 */
 	public boolean isStartAutodetection() {
 		return startAutodetection;
 	}
 }
