 package fr.ujm.tse.info4.pgammon.controleur;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 import org.jdom2.JDOMException;
 
 import fr.ujm.tse.info4.pgammon.models.CouleurCase;
 import fr.ujm.tse.info4.pgammon.models.GestionDeSession;
 import fr.ujm.tse.info4.pgammon.models.Joueur;
 import fr.ujm.tse.info4.pgammon.models.ParametreJeu;
 import fr.ujm.tse.info4.pgammon.vues.VueIntermediairePartie;
 
 public class ControleurIntermediairePartie implements Controleur{
 
 	private ControleurPrincipal controleurPrincipal;
 	private ControleurIntermediairePartie controleurIntermediairePartie;
 	private boolean isNouvellePartie;
 	private VueIntermediairePartie vueCreationPartie;
 	private JFrame frame;
 	private CouleurCase joueurEnCourDeModification;
 	protected ControleurListeJoueur controleurListeJoueur;
 	
 	public ControleurIntermediairePartie(boolean isNouvellePartie,ControleurPrincipal controleurPrincipal)
 	{
 		this.controleurPrincipal = controleurPrincipal;
 		this.isNouvellePartie = isNouvellePartie;
 		controleurIntermediairePartie = this;
 		vueCreationPartie = new VueIntermediairePartie(this.isNouvellePartie);
 		controleurPrincipal.getFrame().setContentPane(vueCreationPartie);
 		frame = controleurPrincipal.getFrame();
 		build();
 	}
 	
 	public void build()
 	{
 		listenerRetour();
 		listenerCommencer();
 		listenerCommencerCharger();
 		listenerSuppresionSession();
 		listenerChargerJoueur1();
 		listenerChargerJoueur2();
 	}
 	
 	public void listenerSuppresionSession()
 	{
 		vueCreationPartie.getVueChargerPartie().getPanelParametresVueCharger().getBoutonSupprimer().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 
 			@Override
 			public void mouseExited(MouseEvent e) {}
 
 			@Override
 			public void mousePressed(MouseEvent e) {}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 
 				try {
 					GestionDeSession gestion = GestionDeSession.getGestionDeSession();
 					gestion.supprimerSession(vueCreationPartie.getVueChargerPartie().getSession().getIdSession());
 					gestion.sauvegarder();
 					vueCreationPartie.getVueChargerPartie().updateData();
 					vueCreationPartie.getVueChargerPartie().updateUI();

 				} catch (IOException | JDOMException e1) {
 					e1.printStackTrace();
					vueCreationPartie.afficherFenetreDemande("P'tit soucis", "Les sessions n'ont pas été chargées");

 				}
 
 			}
 
 		});
 		
 		
 	}
 	
 	public void listenerRetour()
 	{
 		vueCreationPartie.getBoutonRetour().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				vueCreationPartie.setVisible(false);
 				controleurPrincipal.retour();
 				}
 		});
 	}
 	
 	public void listenerCommencer()
 	{
 		vueCreationPartie.getVueNouvelleSession().getBoutonCommencer().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 			}
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}
 			@Override
 			public void mouseReleased(MouseEvent e) {	
 				
 				int temp = vueCreationPartie.getVueNouvelleSession().getPanelParamètre().getNbTemps()*1000;
 				int nbPartie = vueCreationPartie.getVueNouvelleSession().getPanelParamètre().getNbParties();
 				boolean videau = vueCreationPartie.getVueNouvelleSession().getPanelParamètre().getVideau().isSelected();
 				
 				
 				Joueur jBlanc =  vueCreationPartie.getVueNouvelleSession().getPanelJoueur1().getJoueur();
 				Joueur jNoir = vueCreationPartie.getVueNouvelleSession().getPanelJoueur2().getJoueur();
 				
 				if(jBlanc == null || jNoir == null){
 					vueCreationPartie.afficherFenetreDemande("Oups!","Choisissez des joueurs!");
 					return;
 				}
 				
 				if(jBlanc == jNoir){
 					vueCreationPartie.afficherFenetreDemande("Oups!", "Les joueurs sont identiques!");
 					return;
 				}
 				//Creation des paramétres de jeu.
 				ParametreJeu param = new ParametreJeu(temp,nbPartie,videau,jBlanc,jNoir);
 				vueCreationPartie.setVisible(false);
 				controleurPrincipal.nouvelleSession(param);
 			}
 		});
 			
 	}
 	
 	public void listenerCommencerCharger()
 	{
 		vueCreationPartie.getVueChargerPartie().getBoutonCommencer().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {}
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				vueCreationPartie.setVisible(false);
 				controleurPrincipal.chargerSession(vueCreationPartie.getVueChargerPartie().getSession());
 			}
 		});
 			
 	}
 	
 
 
 	public void  listenerChargerJoueur1(){
 		vueCreationPartie.getVueNouvelleSession().getBoutonChangerJoueurBlanc().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				vueCreationPartie.setVisible(false);
 				controleurListeJoueur = new ControleurListeJoueur(true,controleurIntermediairePartie);
 				joueurEnCourDeModification = CouleurCase.BLANC;}
 		
 		});
 	}
 	
 	public void  listenerChargerJoueur2(){
 		vueCreationPartie.getVueNouvelleSession().getBoutonChangerJoueurNoir().addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {}
 			@Override
 			public void mouseExited(MouseEvent e) {}
 			@Override
 			public void mousePressed(MouseEvent e) {}
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				vueCreationPartie.setVisible(false);
 				controleurListeJoueur = new ControleurListeJoueur(true,controleurIntermediairePartie);
 				joueurEnCourDeModification = CouleurCase.NOIR;
 				}
 		
 		});
 	}
 	
 	@Override
 	public Controleur getControleur() {
 		return this;
 	}
 	
 	@Override
 	public JFrame getFrame() {
 		
 		return frame;
 	}
 
 	@Override
 	public void retour() {
 		getFrame().setContentPane(vueCreationPartie);
 		vueCreationPartie.setVisible(true);
 
 		//build();
 	}
 	
 	public void retour(Joueur j) {
 		getFrame().setContentPane(vueCreationPartie);
 		vueCreationPartie.setVisible(true);
 		if (joueurEnCourDeModification == CouleurCase.BLANC)
 			vueCreationPartie.getVueNouvelleSession().setJoueur1(j);
 		else
 			vueCreationPartie.getVueNouvelleSession().setJoueur2(j);
 		//TODO  mettre a jour la fenétre
 		//build();
 	}
 	
 	
 }
