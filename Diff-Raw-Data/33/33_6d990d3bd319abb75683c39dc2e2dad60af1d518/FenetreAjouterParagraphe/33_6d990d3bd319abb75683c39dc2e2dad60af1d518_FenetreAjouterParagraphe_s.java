 package IHM.Frame;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.text.html.HTMLEditorKit;
 
 import IHM.Panel.PanelArbre;
 import Main.*;
 import Utilitaire.*;
 
 public class FenetreAjouterParagraphe extends JFrame implements ActionListener
 {
 	private JEditorPane editorPane;
 	private JButton button;
 	
 	private Page page;
 	private int statue;
 	private int indice;
 	private int indiceParagraphe;
 	
	private static int cpt=1;
	
 	public FenetreAjouterParagraphe(Page page, int statue, String paragraphe, int indice, int indiceParagraphe)
 	{
 		this.page = page;
 		this.statue = statue;
 		this.indice = indice;
 		this.indiceParagraphe = indiceParagraphe;
 		
 		setSize(500,500);
 		setLayout(new BorderLayout());
 		setTitle("Entrer un paragraphe");
 		
 		editorPane = new JEditorPane();
 		editorPane.setEditable(true);
 		editorPane.setContentType("text/plain");
 		
 		editorPane.setText(paragraphe);
 
 		add(editorPane,BorderLayout.CENTER);
 		
 		button = new JButton("Valider");
 		button.addActionListener(this);
 		add(button,BorderLayout.SOUTH);	
 		
 		setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent e) 
 	{
 		if (statue == 0) 
 		{
 			Generateur.alProjet.get(0).getPage(page).ajouterParagraphe(editorPane.getText());
 			Generateur.fenetre.getArborescence().ajoutFils("element", "Paragraphe " + cpt, editorPane.getText());
			cpt++;
 		}
 		else
 		{
 			Generateur.alProjet.get(0).getPage(page).modParagraphe(editorPane.getText(), indiceParagraphe);
 			Generateur.fenetre.getArborescence().setAlS(indice, editorPane.getText());
 		}
 		this.dispose();
 	}
 	
 	
 }
