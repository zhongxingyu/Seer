 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.awt.Color;
 
 public class Accueil extends JPanel implements MouseListener
 {
     int x = Client.fenetre_x;
     int y = Client.fenetre_y;
     
     // conteneurs
     JPanel conteneur_est = new JPanel();
     JPanel conteneur_centre = new JPanel();
     JPanel conteneur_ouest = new JPanel();
 
     // conteneur_forme
     JPanel content_deco = new JPanel();
     JPanel content_recherche = new JPanel();
     JPanel content_tout = new JPanel();
     JPanel content_titre_bienvenue = new JPanel();
     JPanel content_titre_reco = new JPanel();
     JPanel content_titre_top = new JPanel();
     JPanel content_info = new JPanel();
     JPanel content_nb = new JPanel();
     JPanel content_periph = new JPanel();
     JPanel content_modifier = new JPanel();
     JPanel content_reco_app1 = new JPanel();
     JPanel content_reco_app2 = new JPanel();
     JPanel content_reco_app3 = new JPanel();
     JPanel content_top_app1 = new JPanel();
     JPanel content_top_app2 = new JPanel();
     JPanel content_top_app3 = new JPanel();
     JPanel content_top_view = new JPanel();
     JPanel content_reco_view = new JPanel();
 
 
     //boutons
     JButton deconnexion = new JButton("Deconnexion");
     JButton view_all = new JButton("Toutes les applications");
     JButton modifier_profil = new JButton("modifier");
     JButton view_more_reco = new JButton("Voir plus");
     JButton view_more_top = new JButton("Voir plus");
     JButton view_periph = new JButton("Voir périphériques");
     JButton reco_app1 = new JButton("<html>Appli 1<br/>OS: type:<html>");
     JButton reco_app2 = new JButton("<html>Appli 2<br/>OS: type:<html>");
     JButton reco_app3 = new JButton("<html>Appli 3<br/>OS: type:<html>");
     JButton top_app1 = new JButton("<html>App1 etoiles<br/>nbcommentaires:</html>");
     JButton top_app2 = new JButton("<html>App2 etoiles<br/>nbcommentaires:</html>");
     JButton top_app3 = new JButton("<html>App3 etoiles<br/>nbcommentaires:</html>");
     
     //text
     JTextField recherche = new JTextField(30);
     
     Accueil()
     {
 	setSize(x, y);
 
 	// on met une grille de 6 lignes dans chacune des trois colonnes
 	setLayout(new BorderLayout());
 	conteneur_est.setLayout(new GridLayout(6,1));
 	conteneur_centre.setLayout(new GridLayout(6,1));
 	conteneur_ouest.setLayout(new GridLayout(6,1));
 	
 	content_deco.add(deconnexion) ;
 	content_recherche.add(recherche); 
 	content_tout.add(view_all);
 	content_titre_bienvenue.add(new JLabel("Bienvenue")); 
 	content_titre_reco.add(new JLabel("Applications recommandées"));
 	content_titre_top.add(new JLabel("Top Applications"));
 	//need req
 	nom_prenom();
 	content_nb.add(new JLabel("<html>Mela: <br/>Nombre App: </html>"));
 	content_periph.add(view_periph);
 	content_modifier.add(modifier_profil);  
 	content_reco_app1.add(reco_app1);
 	content_reco_app2.add(reco_app2); 
 	content_reco_app3.add(reco_app3);
 	content_top_app1.add(top_app1);
 	content_top_app2.add(top_app2);
 	content_top_app3.add(top_app3);
 	content_top_view.add(view_more_top)   ;
 	content_reco_view.add(view_more_reco);
 	// OUEST
 	conteneur_ouest.add(content_deco);
 	conteneur_ouest.add(content_titre_bienvenue);
 	conteneur_ouest.add(content_info);
 	conteneur_ouest.add(content_nb);
 	conteneur_ouest.add(content_periph);
 	conteneur_ouest.add(content_modifier);
 	// CENTRE
 	conteneur_centre.add(content_recherche);
 	conteneur_centre.add(content_titre_reco);
 	conteneur_centre.add(content_reco_app1);
 	conteneur_centre.add(content_reco_app2);
 	conteneur_centre.add(content_reco_app3);
 	conteneur_centre.add(content_reco_view);
 	// EST
 	conteneur_est.add(content_tout);
 	conteneur_est.add(content_titre_top);
 	conteneur_est.add(content_top_app1);
 	conteneur_est.add(content_top_app2);
 	conteneur_est.add(content_top_app3);
 	conteneur_est.add(content_top_view);
 	// CONTROLE
 	deconnexion.addMouseListener(this);
 	view_all.addMouseListener(this);
 	modifier_profil.addMouseListener(this);
 	view_more_reco.addMouseListener(this);
 	view_more_top.addMouseListener(this);
 	view_periph.addMouseListener(this);
 	reco_app1.addMouseListener(this);
 	reco_app2.addMouseListener(this);
 	reco_app3.addMouseListener(this);
 	top_app1.addMouseListener(this);
 	top_app2.addMouseListener(this);
 	top_app3.addMouseListener(this);
 
 	// ADD
 	add("East", conteneur_est);
 	add("Center", conteneur_centre);	
 	add("West", conteneur_ouest);
 	setVisible(true);
     }
 
     /*---Requetes---*/
     private void nom_prenom()
     {
	ConnexionClient c = new ConnexionClient();
	c.request("get_infoClientID");
 	content_info.add(new JLabel("<html>Nom: <br/>Prenom:</html>"));
     }
     
     /*---CLICKS---*/
     public void mouseClicked(MouseEvent e)
     {
 	if(e.getSource() == deconnexion)
 	    {
 		System.out.println("deco");
 	    }
 	if(e.getSource() == view_all)
 	    {
 		System.out.println("view all");
 	    }
 	if(e.getSource() == modifier_profil)
 	    {
 		System.out.println("modifier");
 	    }
 	if(e.getSource() == view_more_reco)
 	    {
 		System.out.println("view more reco");
 	    }
 	if(e.getSource() == view_more_top)
 	    {
 		System.out.println("view more top");
 	    }
 	if(e.getSource() == view_periph)
 	    {
 		System.out.println("view periph");
 	    }
 	if(e.getSource() == reco_app1)
 	    {
 		System.out.println("reco 1");
 	    }
 	if(e.getSource() == reco_app2)
 	    {	
 		System.out.println("reco 2");
 	    } 
 	if(e.getSource() == reco_app3)
 	    {
 		System.out.println("reco 3");
 	    }
 	if(e.getSource() == top_app1)
 	    {
 		System.out.println("top 1");
 	    }
 	if(e.getSource() == top_app2)
 	    {
 		System.out.println("top 2");
 	    }
 	if(e.getSource() == top_app3)
 	    {
 		System.out.println("top 3");
 	    }
 
     }
 
     public void mouseEntered(MouseEvent e){}
     public void mouseExited(MouseEvent e){}
     public void mousePressed(MouseEvent e){}
     public void mouseReleased(MouseEvent e){}
 }
