 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 @SuppressWarnings("serial")
 public class BarreMenu extends JMenuBar{
 	
 	private Controleur controleur;
 	//JMenu "Fichier"
 	JMenu menuFichier = new JMenu("Fichier");
 	JMenuItem nouveau = new JMenuItem("Nouveau");
 	JMenuItem ouvrirImage = new JMenuItem("Ouvrir une image");
 	JMenuItem ouvrirHistorique = new JMenuItem("Ouvrir un historique");
 	JMenuItem enregistrerImage = new JMenuItem("Enregistrer l'image");
     JMenuItem enregistrerHistorique = new JMenuItem("Enregistrer l'historique");
     JMenuItem quitter = new JMenuItem("Quitter");
     
     //JMenu "Outils"
 	JMenu menuOutils = new JMenu("Outils");
 	JMenuItem options = new JMenuItem("Options");
 	JMenuItem aPropos = new JMenuItem("A propos");
 	
     /**
      *  Constructeur
      */
 	BarreMenu(){
 		super();
 		
 		//Menu "Fichier"
 		this.add(menuFichier);
 		menuFichier.add(nouveau);
 		menuFichier.add(ouvrirImage);
 		menuFichier.add(ouvrirHistorique);
         menuFichier.add(enregistrerImage);
         menuFichier.add(enregistrerHistorique);
 		menuFichier.add(quitter);
 		
 		//Menu "Outils"
 		this.add(menuOutils);
 		menuOutils.add(options);
 		menuOutils.add(aPropos);
 		
 		
 		//Ajout des Action Listener
 		nouveau.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				nouveau();
 			}
 		});
 		ouvrirImage.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				ouvrirImage();
 			}
 		});
 		ouvrirHistorique.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				ouvrirHistorique();
 			}
 		});
 		enregistrerImage.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				enregistrerImage();
 			}
 		});
 		enregistrerHistorique.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				enregistrerHistorique();
 			}
 		});
 		quitter.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				quitter();
 			}
 		});
 		options.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				MenuOption menuOption = new MenuOption(null, "Option", true);
 				
 			}
 		});
 		aPropos.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				aPropos();
 			}
 		});
 		
 	}
 	
 	public void nouveau(){
 		controleur.commande("new", true);
 	}
 	public void ouvrirImage(){
		controleur.commande("open", true);
 	}
 	public void ouvrirHistorique(){
		controleur.commande("exec", true);
 	}	
 	public void enregistrerImage(){
 		controleur.commande("save", true);
 	}
 	
 	public void enregistrerHistorique(){
		controleur.commande("savehistory", true);
 	}
 	
 	public void quitter(){
 
         /* TODO : N'afficher la boîte de dialogue que si le dessin a été modifié après sauvegarde
          * ou n'a pas été sauvegardé
          */
 		JOptionPane quitter = new JOptionPane();
 		int option = quitter.showConfirmDialog(null, "Voulez vous sauvegarder votre travail avant de fermer le programme ?", "Sauvegarder", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 		if (option == JOptionPane.OK_OPTION){
 			enregistrerHistorique();
 			System.exit(0);
 		}
 		if (option == JOptionPane.NO_OPTION){
 			System.exit(0);
 		}
 		
 	}
 	
 	public void aPropos(){
 		JOptionPane aPropos = new JOptionPane();
 		aPropos.showMessageDialog(null,
 								"Carapuce est un projet Universitaire développé par 4 étudiants :\n\n" +
 								"Mehdi Khelifi\n" +
 								"Loïc Runarvot\n" +
 								"Gauthier Lo\n" +
 								"Frederic Mamath\n",
 								"A propos",
 								JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public void options(){}
 	
 	
     /**
      *  Modifieur du controleur
      *  @param c nouveau controleur
      */
     public void setControleur(Controleur c)
     {
         this.controleur = c;
     } 
 }
