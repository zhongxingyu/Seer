 package client.controller;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import client.JeuPanel;
 import client.view.jeu.EcranJeu;
 
 import common.Constante;
 import common.Joueur;
 import common.partie.batiment.Batiment;
 import common.partie.batiment.TypeBatiment;
 import common.partie.plateau.Case;
 
 /**
  * @author omar
  */
 public class EcranJeuListener implements MouseListener,MouseMotionListener{
 	private JeuPanel jeu;
 	private Joueur joueur;
 	private EcranJeu ecranJeu;
 	
 	public EcranJeuListener(JeuPanel jeu,Joueur joueur,EcranJeu ecranJeu){
 		super();
 		this.jeu = jeu;
 		this.joueur = joueur;
 		this.ecranJeu = ecranJeu;
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e){
 		ecranJeu.setPositionLabelConstructionBatiment(e.getX(),e.getY());
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e){
 		ecranJeu.setPositionLabelConstructionBatiment(e.getX(),e.getY());
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e){
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e){
 		int x = e.getX();
 		int y = e.getY();
 		
 		if (e.getButton() == MouseEvent.BUTTON1){
 			// clic gauche
 			if (ecranJeu.isModeCreationBatiment()){
 				// on verifie que l'utilisateur a bien cliquer sur le plateau
				if (x > Constante.DECALAGE_PLATEAU_X && x < (Constante.LARGEUR_PLATEAU + Constante.DECALAGE_PLATEAU_X)
						&& y > Constante.DECALAGE_PLATEAU_Y && y < (Constante.HAUTEUR_PLATEAU + Constante.DECALAGE_PLATEAU_Y)){
 					// il y a un decalage entre la fenetre et le plateau d'où le Constante.DECALAGE_PLATEAU_X et le
 					// Constante.DECALAGE_PLATEAU_Y
 					// concernant le Constante.LARGEUR_CASE et le Constante.HAUTEUR_CASE, c'est parce que l'on centre le JLabel (du
 					// batiment)
 					// sur la souris
 					x -= Constante.DECALAGE_PLATEAU_X + Constante.LARGEUR_CASE;
 					y -= Constante.DECALAGE_PLATEAU_Y + Constante.HAUTEUR_CASE;
 					// on recupere la case la plus proche du clic et on y ajoute le nouveau batiment
 					TypeBatiment type = ecranJeu.getTypeBatimentEnConstruction();
 					int montant = TypeBatiment.getPrix(type,joueur.getNiveauBatiment(type));
 					Case position = jeu.getClient().getPartie().getPlateau().getCasePlusProche(x,y);
 					Batiment batiment = new Batiment(type,position);
 					joueur.ajouterBatiment(batiment);
 					joueur.decrementArgent(montant);
 					ecranJeu.cacherModeCreationBatiment();
 					ecranJeu.update();
 				}else{
 					ecranJeu.cacherModeCreationBatiment();
 				}
 			}else{
 				if (y <= 46){
 					// test des trois liens du haut
 					if (x >= 90 && x <= 275){
 						// lien joueur
 						ecranJeu.afficherOngletJoueur();
 					}else if (x >= 420 && x <= 605){
 						// lien ville
 						ecranJeu.afficherOngletVille();
 					}else if (x >= 750 && x <= 930){
 						// lien menu
 						ecranJeu.afficherOngletMenu();
 					}
 				}else if (x <= 60){
 					// test des deux liens à gauche
 					if (y >= 85 && y <= 275){
 						// lien batiement
 						ecranJeu.afficherOngletBatiment();
 					}else if (y >= 320 && y <= 510){
 						// lien unités
 						ecranJeu.afficherOngletUnite();
 					}
 				}
 			}
 		}else{
 			// clic droit
 			ecranJeu.cacherModeCreationBatiment();
 		}
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e){
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent e){
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e){
 	}
 	
 }
