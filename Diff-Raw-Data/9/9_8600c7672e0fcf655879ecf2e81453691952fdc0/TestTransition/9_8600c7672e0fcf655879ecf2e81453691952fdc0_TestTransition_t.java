 package fr.ujm.tse.info4.pgammon.test.gui;
 
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.List;
 import java.io.ObjectInputStream.GetField;
 import java.util.HashSet;
 import java.util.SortedSet;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import javax.swing.JFrame;
 
 import fr.ujm.tse.info4.pgammon.controleur.ControleurPartie;
 import fr.ujm.tse.info4.pgammon.gui.ChangementTourAnimation;
 import fr.ujm.tse.info4.pgammon.models.Joueur;
 import fr.ujm.tse.info4.pgammon.models.NiveauAssistant;
 import fr.ujm.tse.info4.pgammon.models.ParametreJeu;
 import fr.ujm.tse.info4.pgammon.models.Partie;
 import fr.ujm.tse.info4.pgammon.vues.VuePartie;
 
 public class TestTransition {
 	
 	public static void main(String[] args) {
 		JFrame frame = new JFrame("TestTransition");
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(812,645);
 		
 		Container panel = frame.getContentPane();
 		panel.setLayout(null);
 
 		// creation des parametre et joueur
 		Joueur jBlanc = new Joueur(1, "ben", "beauGoss",
 				NiveauAssistant.NON_UTILISE);
 		Joueur jNoir = new Joueur(2, "JM", "null", NiveauAssistant.COMPLET);
 
 		ParametreJeu param = new ParametreJeu(0, 3, true, jBlanc, jNoir);
 		Partie p = new Partie(param);
 		p.LancerPartie();
 		ControleurPartie controleurPartie = new ControleurPartie(p);
 		VuePartie vue = controleurPartie.getVuePartie();
 		vue.setBounds(0,0,830,630);
 
 		panel.add(vue);
 		SortedSet<String> hs = new ConcurrentSkipListSet<>();
 		hs.add("Non");
 		hs.add("Oui");
 
 		hs.add("Peut-etre");
 
		vue.afficherFenetreDemande("Ca va?", hs);
 		
 		frame.setVisible(true);
 	}
 }
