 //////////////////////////////////////////////////////////////////
 //Creat a sidepanel in Pathvisio for the metabolite information.//
 //////////////////////////////////////////////////////////////////
 
 package metaboliteplugin;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
import java.awt.Graphics2D;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.openscience.cdk.nonotify.NNMolecule;
 import org.pathvisio.desktop.PvDesktop;
 import org.pathvisio.desktop.plugin.Plugin;
 
 public class MetaboliteInfoGUI implements Plugin
 {
 	private PvDesktop desktop;
 	
 	
 	private final String HMDB = "HMDB00122";
 	
 	//String for Cactus service
 	private static final String SERVICE = "http://cactus.nci.nih.gov/chemical/structure/";
 	
 	//Get the name of the metabolite.
 	public static String GetName(){
 		String name = "Glucose";
 		return name; 
 	}
 	
 	//Request InChI string from Cactus
 	public static String Inchi(){
 		String inchiInfo = null;
 		try {
 		//Set up connection and put InChI key into a string
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpGet getInchi = new HttpGet(SERVICE + GetName() + "/stdinchi");
 			
 			HttpResponse response = null;
 			response = httpclient.execute(getInchi);
 			
 			HttpEntity entity = response.getEntity();
 			inchiInfo = EntityUtils.toString(entity);
 		
 		} catch (ClientProtocolException ClientException) {
 			System.out.println(ClientException.getMessage());
 			ClientException.printStackTrace();
 		} catch (IOException IoException) {
 			System.out.println(IoException.getMessage());
 			IoException.printStackTrace();
 		} catch (Throwable throwable) {
 			  System.out.println(throwable.getMessage());
 			  throwable.printStackTrace();
 		}
 		return inchiInfo;
 		}
 	
 	//Request InChI string from Cactus
 	public static String InchiKey(){
 		String inchiKeyInfo = null;
 		try {
 		//Set up connection and put InChI key into a string
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpGet getInchiKey = new HttpGet(SERVICE + GetName() + "/stdinchikey");
 			
 			HttpResponse response = null;
 			response = httpclient.execute(getInchiKey);
 			
 			HttpEntity entity = response.getEntity();
 			inchiKeyInfo = EntityUtils.toString(entity);
 		
 		} catch (ClientProtocolException ClientException) {
 			System.out.println(ClientException.getMessage());
 			ClientException.printStackTrace();
 		} catch (IOException IoException) {
 			System.out.println(IoException.getMessage());
 			IoException.printStackTrace();
 		} catch (Throwable throwable) {
 			  System.out.println(throwable.getMessage());
 			  throwable.printStackTrace();
 		}
 		return inchiKeyInfo;
 	}
 	
 //	public static String Pubchem(){
 //		String cid = "5793";
 //		String pubChemInchi = null;
 //		try {
 //		
 //		HttpClient httpclient = new DefaultHttpClient();
 //		HttpGet pubChemRequest = new HttpGet("http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid="
 //				+ cid + "&disopt=SaveXML");
 //		pubChemRequest.getAllHeaders();
 //		System.out.println(pubChemRequest);
 //
 //		HttpResponse response = null;
 //		response = httpclient.execute(pubChemRequest);
 //		HttpEntity entity = response.getEntity();
 //		pubChemInchi = EntityUtils.toString(entity);
 //		System.out.println(pubChemInchi);
 //		
 //	} catch (ClientProtocolException ClientException) {
 //		System.out.println(ClientException.getMessage());
 //		ClientException.printStackTrace();
 //	} catch (IOException IoException) {
 //		System.out.println(IoException.getMessage());
 //		IoException.printStackTrace();
 //	} catch (Throwable throwable) {
 //		  System.out.println(throwable.getMessage());
 //		  throwable.printStackTrace();
 //	}
 //		return pubChemInchi;
 //		
 //	}
 	
 	public Image MSImageLow(){
 		URL imageUrl = null;
 		try {
 			imageUrl = new URL("http://www.hmdb.ca/labm/metabolites/"
 			+ HMDB + "/ms/spectra/" + HMDB + "L.png");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
	
 		return image;
 	}
 	
 	public Image MSImageMed(){
 		URL imageUrl = null;
 		try {
 			imageUrl = new URL("http://www.hmdb.ca/labm/metabolites/" 
 			+ HMDB + "/ms/spectraM/" + HMDB + "M.png");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
 		return image;
 	}
 	
 	public Image MSImageHigh(){
 		URL imageUrl = null;
 		try {
 			imageUrl = new URL("http://www.hmdb.ca/labm/metabolites/" 
 			+ HMDB + "/ms/spectraH/" + HMDB + "H.png");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
 		return image;
 	}
 	
 	public String h1NMR(){
 		String H1NMR = null;
 
 		try {
 		//Set up connection and put InChI key into a string
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpGet getH1NMR = new HttpGet("http://www.hmdb.ca/labm/metabolites/"
 			+ HMDB + "/chemical/pred_hnmr_peaklist/" + HMDB + "_peaks.txt");
 			
 			HttpResponse response = null;
 			response = httpclient.execute(getH1NMR);
 			
 			HttpEntity entity = response.getEntity();
 			H1NMR = EntityUtils.toString(entity);
 		
 		} catch (ClientProtocolException ClientException) {
 			System.out.println(ClientException.getMessage());
 			ClientException.printStackTrace();
 		} catch (IOException IoException) {
 			System.out.println(IoException.getMessage());
 			IoException.printStackTrace();
 		} catch (Throwable throwable) {
 			  System.out.println(throwable.getMessage());
 			  throwable.printStackTrace();
 		}
 		return H1NMR;
 		
 	}
 	
 	public String c13NMR(){
 		String C13NMR = null;
 		try {
 		//Set up connection and put InChI key into a string
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpGet getC13NMR = new HttpGet("http://www.hmdb.ca/labm/metabolites/"
 			+ HMDB + "/chemical/pred_cnmr_peaklist/" + HMDB + "_peaks.txt");
 			
 			HttpResponse response = null;
 			response = httpclient.execute(getC13NMR);
 			
 			HttpEntity entity = response.getEntity();
 			C13NMR = EntityUtils.toString(entity);
 		
 		} catch (ClientProtocolException ClientException) {
 			System.out.println(ClientException.getMessage());
 			ClientException.printStackTrace();
 		} catch (IOException IoException) {
 			System.out.println(IoException.getMessage());
 			IoException.printStackTrace();
 		} catch (Throwable throwable) {
 			  System.out.println(throwable.getMessage());
 			  throwable.printStackTrace();
 		}
 		return C13NMR;
 	}
 	//Request structure image from Cactus
 	public Image moleculeImage(){
 		URL imageUrl = null;
 		try {
 			imageUrl = new URL(SERVICE + GetName() + "/image");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 		Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
 
 		return image;
 	}
 
 	public Component GeneralPanel(){
 
 		String name = "Metabolite name: " + GetName() + "\n \n";
 		String inchiKey = InchiKey();
 		String inchi = Inchi() + "\n \n";
 		//String pubchem = Pubchem();
 		String info = name + inchi + inchiKey /* + pubchem*/;
 
 		JLabel image = new JLabel(new ImageIcon(moleculeImage()));
 		
 		JTextArea area = new JTextArea(info);
 		area.setEditable(false); 
 		area.setLineWrap(true); 
 		area.setWrapStyleWord(true); 
 		
 		//Create panel for general information about the metabolite.
 		JPanel GeneralPanel = new JPanel();
 		GeneralPanel.setBackground(Color.WHITE);
 		GeneralPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		GeneralPanel.setLayout(new BoxLayout(GeneralPanel, BoxLayout.Y_AXIS));
 		
 		GeneralPanel.add(area);
 		GeneralPanel.add(image);
 		
 		return GeneralPanel;
 	}
 	
 	public Component MSPanel(){
 		
 		//Create panel for MS data/information
 		JPanel MSPanel = new JPanel();
 		MSPanel.add(new JLabel("MS info")); //TODO instead of labels, implement MS data
 		MSPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
 		MSPanel.setLayout(new BoxLayout(MSPanel, BoxLayout.Y_AXIS));
 		JPanel MSPanel2 = new JPanel();
 		MSPanel.add(MSPanel2);
 		MSPanel2.setLayout(new BoxLayout(MSPanel2, BoxLayout.X_AXIS));
 		MSPanel2.add(new JLabel(new ImageIcon(MSImageLow())));
 		MSPanel2.add(new JLabel(new ImageIcon(MSImageMed())));
 		MSPanel2.add(new JLabel(new ImageIcon(MSImageHigh())));
 		
 		return MSPanel;
 	}
 	
 	public Component NMRPanel(){
 		
 		//Create panel for NMR data/information
 		JPanel NMRPanel = new JPanel();
 		NMRPanel.add(new JLabel("NMR info")); //TODO instead of labels, implement NMR data
 		NMRPanel.setLayout(new BoxLayout(NMRPanel, BoxLayout.Y_AXIS));
 		NMRPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
 		
 		JTextArea C13NMR = new JTextArea(c13NMR());
 			C13NMR.setEditable(false);
 			C13NMR.setLineWrap(true); 
 			C13NMR.setWrapStyleWord(true);
 		
 		JTextArea H1NMR = new JTextArea(h1NMR());
 			H1NMR.setEditable(false);
 			H1NMR.setLineWrap(true); 
 			H1NMR.setWrapStyleWord(true);
 			
 		NMRPanel.add(C13NMR);
 		NMRPanel.add(H1NMR);
 		return NMRPanel;
 	}
 	
 	public void init(PvDesktop desktop)
 	{
 		// TODO if datanode type equals metabolite{
 				
 		// create side bar
 		JPanel panel = new JPanel();
 		
 		panel.setLayout (new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		//panel.setBackground(Color.white);
 		panel.add(GeneralPanel());
 		panel.add(MSPanel());
 		panel.add(NMRPanel());
 		
 		// get a reference to the sidebar
 		JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();
 		JScrollPane jsp = new JScrollPane(panel);
 		// add side bar title
 		sidebarTabbedPane.add("Metabolite Information", jsp);
 		
 	}
 
 	@Override
 	public void done() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
