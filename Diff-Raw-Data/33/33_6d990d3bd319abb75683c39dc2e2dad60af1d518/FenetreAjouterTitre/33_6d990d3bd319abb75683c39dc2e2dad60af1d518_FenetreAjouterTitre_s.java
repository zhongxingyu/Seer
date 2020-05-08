 package IHM.Frame;
 
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import IHM.Panel.PanelArbre;
 import Main.Generateur;
 import Utilitaire.*;
 
 public class FenetreAjouterTitre extends JFrame implements ActionListener
 {
 	private JTextField	tf;
 	private Page 		page;
 	private int			statue;
 	private int			indice;
 	private int			indiceTitre;
	
	private static int	cpt	= 1;
 
 	public FenetreAjouterTitre(Page page, int statue, String titre, int indice, int indiceTitre)
 	{
 		this.page = page;
 		this.statue = statue;
 		this.indice = indice;
 		this.indiceTitre = indiceTitre;
 
 		// 0 = ajout, 1 = modif
 		if (statue == 0)
 		{
 			setTitle("Entrer un titre");
 			tf = new JTextField("", 20);
 		}
 		else
 		{
 			setTitle("Modifier un titre");
 			tf = new JTextField(titre, 20);
 		}
 		tf.addActionListener(this);
 		add(tf);
 
 		setVisible(true);
 		pack();
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		if (statue == 0)
 		{
 			Generateur.alProjet.get(0).getPage(page).ajouterTitre(tf.getText());
 			Generateur.fenetre.getArborescence().ajoutFils("element", "Titre " + cpt, tf.getText());
			cpt++;
 		}
 		else
 		{
 			Generateur.alProjet.get(0).getPage(page).modTitre(tf.getText(), indiceTitre);
 			Generateur.fenetre.getArborescence().setAlS(indice, tf.getText());
 		}
 		this.dispose();
 	}
 
 }
