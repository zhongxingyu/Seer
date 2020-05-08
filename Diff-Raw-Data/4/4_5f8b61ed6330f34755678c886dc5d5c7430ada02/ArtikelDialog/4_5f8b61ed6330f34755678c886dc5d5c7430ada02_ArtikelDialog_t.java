 package verkauf;
 
 import hauptFenster.Reha;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JComboBox;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EtchedBorder;
 
 import org.jdesktop.swingx.JXButton;
 import org.jdesktop.swingx.JXLabel;
 import org.jdesktop.swingx.JXPanel;
 
 import systemTools.JRtaTextField;
 import utils.INIFile;
 import verkauf.model.Artikel;
 import verkauf.model.Lieferant;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 import dialoge.PinPanel;
 import dialoge.RehaSmartDialog;
 import events.RehaTPEvent;
 import events.RehaTPEventClass;
 import events.RehaTPEventListener;
 
 public class ArtikelDialog extends RehaSmartDialog {
 
 	private static final long serialVersionUID = 1L;
 	
 	private JXButton speichern = new JXButton("speichern");
 
 	private JRtaTextField textArtikelID, textBeschreibung, textPreis, textEinkaufspreis, textLagerstand;
 	
 	private JComboBox comboLieferant, comboEinheit, comboMwst;
 	
 	private ActionListener al;
 	
 	private KeyListener kl;
 	
 	RehaTPEventClass rtp = null;
 	
 	PinPanel pinPanel = null;
 	
 	private Artikel artikel;
 	
 	private String[] mwstSaetze = {"0", "7", "19"};
 	
 	INIFile inif;
 	
 	public ArtikelDialog(int id, Point position) {
 		super(null, "ArtikelDlg");
 		this.activateListener();
 		this.setSize(300, 300);
 		this.setUndecorated(true);
 		
 		pinPanel = new PinPanel();
 		pinPanel.getGruen().setVisible(false);
 		//pinPanel.getRot().setActionCommand("close");
 		pinPanel.setName("ArtikelDlg");
 		setPinPanel(pinPanel);
 		inif = new INIFile(Reha.proghome +"ini/"+ Reha.aktIK +"/verkauf.ini");
 		getSmartTitledPanel().setContentContainer(getContent());
 		getSmartTitledPanel().getContentContainer().setName("ArtikelDlg");
 		getSmartTitledPanel().setTitle("Artikel anlegen / bearbeiten");
 		this.setName("ArtikelDlg");
 		this.setLocation(position);
 		
 		if(id != -1) {
 			artikel = new Artikel(id);
 			this.lade();
 		}
 	}
 	
 	public void setzeFocus(){
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run(){
 				textArtikelID.requestFocus();
 			}
 		});
 	}
 	
 	private JXPanel getContent() {
 		JXPanel pane = new JXPanel();
 		this.addKeyListener(kl);
 		pane.setBorder(new EtchedBorder(Color.white, Color.gray));
 		
 		String xwerte = "5dlu, 50dlu, 5dlu, 50dlu:g, 5dlu";
 		
 		String ywerte = "5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu:g, p";
 		
 		FormLayout lay = new FormLayout(xwerte, ywerte);
 		CellConstraints cc = new CellConstraints();
 		pane.setLayout(lay);
 		
 		JXLabel lab = new JXLabel("Artikel-ID");
 		pane.add(lab, cc.xy(2, 2));
 		
 		textArtikelID = new JRtaTextField("nix", false);
 		pane.add(textArtikelID, cc.xy(4, 2));
 		
 		lab = new JXLabel("Beschreibung");
 		pane.add(lab, cc.xy(2, 4));
 		
 		textBeschreibung = new JRtaTextField("nix", false);
 		pane.add(textBeschreibung, cc.xy(4, 4));
 
 		lab = new JXLabel("VK-Preis");
 		pane.add(lab, cc.xy(2, 6));
 		
 		textPreis = new JRtaTextField("FL",true,"6.2","RECHTS");
 		pane.add(textPreis, cc.xy(4, 6));
 
 		lab = new JXLabel("EK-Preis");
 		pane.add(lab, cc.xy(2, 8));
 		
 		textEinkaufspreis = new JRtaTextField("FL",true,"6.2","RECHTS");
 		pane.add(textEinkaufspreis, cc.xy(4, 8));
 			
 		lab = new JXLabel("Lagerstand");
 		pane.add(lab, cc.xy(2, 10));
 		
 		textLagerstand = new JRtaTextField("FL",true,"6.2","RECHTS");
 		pane.add(textLagerstand, cc.xy(4, 10));
 		
 		lab = new JXLabel("Lieferant");
 		pane.add(lab, cc.xy(2, 12));
 		
 		comboLieferant = new JComboBox(Lieferant.liefereLieferantenCombo());
 		pane.add(comboLieferant, cc.xy(4, 12));
 		
 		lab = new JXLabel("Einheit");
 		pane.add(lab, cc.xy(2, 14));
 		
 		comboEinheit = new JComboBox(this.getEinheiten());
 		pane.add(comboEinheit, cc.xy(4, 14));
 		
 		lab = new JXLabel("MwSt.");
 		pane.add(lab, cc.xy(2, 16));
 		
 		comboMwst = new JComboBox(mwstSaetze);
 		pane.add(comboMwst, cc.xy(4, 16));
 		
 		speichern.setActionCommand("speicher");
 		speichern.addActionListener(al);
 		pane.add(speichern, cc.xyw(2, 18, 3));
 		
 		return pane;
 	}
 	
 	private void activateListener() {
 		al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				speicher();
 			}
 			
 		};
 		
 		kl = new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					schliessen();
 				} else if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					speicher();
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				
 			}
 			
 		};
 		rtp = new RehaTPEventClass();
 		rtp.addRehaTPEventListener((RehaTPEventListener) this);
 	}
 	
 	private void schliessen() {
 		if(rtp != null){
 			rtp.removeRehaTPEventListener((RehaTPEventListener) this);
 			rtp = null;
 			pinPanel = null;			
 		}
 		this.setVisible(false);
 		this.dispose();
 	}
 	
 	private void speicher() {
 		if(!(this.textArtikelID.getText().equals("") && this.textBeschreibung.getText().equals("") && this.textPreis.getText().equals("") && this.textLagerstand.getText().equals(""))) {
 			if(this.textEinkaufspreis.getText().equals("")) {
 				this.textEinkaufspreis.setText("0,00");
 			}
 			String dummyparse = "";
 			if(this.artikel != null) {
 				this.artikel.setEan(this.textArtikelID.getText());
 				this.artikel.setBeschreibung(this.textBeschreibung.getText());
 				dummyparse = (this.textPreis.getText().trim().equals("") ? "0.00" : this.textPreis.getText().replace(',', '.')); 
 				this.artikel.setPreis(Double.parseDouble(dummyparse));
 				dummyparse = (this.textEinkaufspreis.getText().trim().equals("") ? "0.00" : this.textEinkaufspreis.getText().replace(',', '.'));				
 				this.artikel.setEinkaufspreis(Double.parseDouble(dummyparse));
 				dummyparse = (this.textLagerstand.getText().trim().equals("") ? "0.00" : this.textLagerstand.getText().replace(',', '.'));				
 				this.artikel.setLagerstand(Double.parseDouble(dummyparse));
 				this.artikel.setLieferant(((Lieferant) this.comboLieferant.getSelectedItem()).getID());
 				this.artikel.setEinheit((String) this.comboEinheit.getSelectedItem());
 				this.artikel.setMwst(Double.parseDouble(((String) this.comboMwst.getSelectedItem()).replace(',', '.')));
 			} else {
 				this.artikel = new Artikel(
 					this.textArtikelID.getText(),
 					this.textBeschreibung.getText(),
 					(String) this.comboEinheit.getSelectedItem(), 
 					Double.parseDouble( (this.textPreis.getText().trim().equals("") ? "0.00" : this.textPreis.getText().replace(',', '.')) ),
 					Double.parseDouble(((String) this.comboMwst.getSelectedItem()).replace(',', '.')),
 					Double.parseDouble( (this.textLagerstand.getText().trim().equals("") ? "0.00" : this.textLagerstand.getText().replace(',', '.')) ), 
 					Double.parseDouble( (this.textEinkaufspreis.getText().trim().equals("") ? "0.00" : this.textEinkaufspreis.getText().replace(',', '.')) ),
 					( this.comboLieferant.getItemCount() > 0 ? ((Lieferant) this.comboLieferant.getSelectedItem()).getID() : -1)
 					);
 			}
 			this.schliessen();
 		}
 	}
 	
 	private void lade() {
 		this.textArtikelID.setText(this.artikel.getEan());
 		this.textBeschreibung.setText(this.artikel.getBeschreibung());
 		this.textPreis.setText(String.valueOf(this.artikel.getPreis()).replace('.', ','));
 		this.textEinkaufspreis.setText(String.valueOf(this.artikel.getEinkaufspreis()).replace('.', ','));
 		this.textLagerstand.setText(String.valueOf(this.artikel.getLagerstand()).replace('.', ','));
		if(this.artikel.getLieferant() != -1) {
			this.comboLieferant.setSelectedItem(new Lieferant(this.artikel.getLieferant()));
		}
 		this.comboEinheit.setSelectedItem(this.artikel.getEinheit());
 		this.comboMwst.setSelectedItem(String.valueOf(this.artikel.getMwst()));
 	}
 	
 	private String[] getEinheiten() {
 		String[] einheiten = new String[inif.getIntegerProperty("Einheiten", "AnzahlEinheiten")];
 		for(int i = 0; i < einheiten.length; i++) {
 			einheiten[i] = inif.getStringProperty("Einheiten", "Einheit"+(i+1));
 		}
 		return einheiten;
 	}
 	
 	public void rehaTPEventOccurred(RehaTPEvent evt) {
 		try{
 			if(evt.getDetails()[0].equals("ArtikelDlg")){
 				this.setVisible(false);
 				rtp.removeRehaTPEventListener((RehaTPEventListener) this);
 				rtp = null;
 				pinPanel = null;
 				schliessen();
 			}
 		}catch(NullPointerException ne){
 		}
 	}		
 	
 	public JRtaTextField getTextField() {
 		return this.textArtikelID;
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
