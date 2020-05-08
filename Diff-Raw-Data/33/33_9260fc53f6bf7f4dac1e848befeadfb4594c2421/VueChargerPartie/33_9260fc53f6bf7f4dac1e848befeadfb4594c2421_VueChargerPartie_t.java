 package fr.ujm.tse.info4.pgammon.vues;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.RadialGradientPaint;
 import java.awt.RenderingHints;
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.jdom2.JDOMException;
 
 import fr.ujm.tse.info4.pgammon.gui.MonochromeButton;
 import fr.ujm.tse.info4.pgammon.gui.MonochromeListe;
 import fr.ujm.tse.info4.pgammon.gui.SessionCellRenderer;
 import fr.ujm.tse.info4.pgammon.models.GestionDeSession;
 import fr.ujm.tse.info4.pgammon.models.Session;
 
 public class VueChargerPartie extends JPanel{
 	
 	/**
 	 * Cette classe permet de gérer la visualisation de la vue de charger une partie
 	 * elle contient une liste de session et un panel de parametre des sessions
 	 */
 
 	private static final long serialVersionUID = 2698819973936287585L;
 	
 	private MonochromeButton boutonCommencer;
 	
 
 
 
 	private Collection<Session> listSession;
 	
 	private MonochromeListe<Session>  mListeSessions;
 	
 	private Session session;
 
 	private PanelParametresVueCharger panelParametresVueCharger;
 
 
 	/**
 	 * Constructeur de la classe
 	 * @param s c'est une liste de session
 	 */
 	public VueChargerPartie(ArrayList<Session> s){
 		
 		try {
 			GestionDeSession gestion = GestionDeSession.getGestionDeSession();
 			listSession = gestion.getListSession();
 			System.out.println(listSession);
 		} catch (IOException | JDOMException e1) {
 			
 			e1.printStackTrace();
			
		}finally{
 		
 		//listSession = s;
 		build();
 		
 		mListeSessions.getList().addListSelectionListener(new ListSelectionListener() {
 			
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (mListeSessions.getList().getSelectedValue() !=null){
 					session=mListeSessions.getList().getSelectedValue();
 					updateData();
 						
 				}
 			}
 		});
		}
 	}
 	
 	/**
 	 * Méthode permettant de faire la mise à jour de la classe
 	 * comme la mise à jour de la liste session ou celle de paramètre
 	 */
 	public void updateData(){
 		panelParametresVueCharger.setVisible(true);
 		panelParametresVueCharger.setSession(session);
 		//listSession.setListDatas(new Vector<Session>(profil.getList()));
 	}
 
 	private void build() {
 		setLayout(null);	
 		setOpaque(false);
 
 		//if (listSession == 0)
 		panelParametresVueCharger = new PanelParametresVueCharger(null);
 		panelParametresVueCharger.setBounds(450, 20, 300, 400);
 		add(panelParametresVueCharger);
 		panelParametresVueCharger.setVisible(false);
 		
 		
 		//il faut que j'ajoute les sessions
 		
 		mListeSessions = new MonochromeListe<>("Sessions enregistrées",listSession,new SessionCellRenderer());
 		mListeSessions.setBounds(40, 20, 330, 400);
 		add(mListeSessions);
 		
 		
 		boutonCommencer = new MonochromeButton("Commencer");
 		boutonCommencer.setBounds(300, 430, 200, 50);
 		add(boutonCommencer);
 		
 	}
 	
 	
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g.create(); 
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
 		
 		Paint p;
 		int h = getHeight(); 
 		int w = getWidth(); 
 		
 		// Arriere plan
 		p = new RadialGradientPaint(new Point2D.Double(getWidth() / 2.0,
                 getHeight() / 2.0), 
                 getHeight(),
                 new float[] { 0.0f, 0.8f },
                 new Color[] { new Color(0x333333), new Color(0x000000) },
                 RadialGradientPaint.CycleMethod.NO_CYCLE);
 		
 		g2.setPaint(p); 
 		g2.fillRect(0, 0, w, h); 
 		
 		// Bordure
 		p = new Color(0x808080);
 		g2.setStroke(new BasicStroke(5.0f) );
 		g2.setPaint(p); 
 		g2.drawRect(2, 0, w - 5 , h - 5 );
 		
 		
 		g2.dispose(); 
 		
 	}
 	
 	
 	/**
 	 * Getter du bouton commencer
 	 * @return retourne la classe du bouton commencer
 	 */
 	public MonochromeButton getBoutonCommencer() {
 		return boutonCommencer;
 	}
 	
 	/**
 	 * Getter de session
 	 * @return retourne une classe Session
 	 */
 	public Session getSession() {
 		return session;
 	}
 	
 	/**
 	 * Getter de la vue de paramétrage de la session selectionner
 	 * @return retourne la classe du panel de paramétre de session
 	 */
 	public PanelParametresVueCharger getPanelParametresVueCharger() {
 		return panelParametresVueCharger;
 	}
 
 }
