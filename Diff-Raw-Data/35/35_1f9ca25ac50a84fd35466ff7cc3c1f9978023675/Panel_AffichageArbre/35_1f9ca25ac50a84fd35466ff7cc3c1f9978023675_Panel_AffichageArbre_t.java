 package fr.arbre.ihm;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.filechooser.FileFilter;
 
 import fr.arbre.dao.csv.CsvPersonDao;
 
 @SuppressWarnings("serial")
 public class Panel_AffichageArbre extends JPanel {
 	private TreeDrawPanel tree;
 
 	private enum Typesave {
 		DESSIN, CSV;
 	}
 
 	// Actions
 	private newMember_Action newMember_Action = new newMember_Action();
 	private displayMemberTable_Action displayMemberTable_Action = new displayMemberTable_Action();
 	private saveImg_Action saveImg_Action = new saveImg_Action();
 	private saveTab_Action saveTab_Action = new saveTab_Action();
 	private veriffErrors_Action veriffErrors_Action = new veriffErrors_Action();
 
 	public Panel_AffichageArbre(String filename) {
 		// Dfinition du dlai avant apparition de l'info-bulle sur les boutons
 		int delai = 500;
 		ToolTipManager.sharedInstance().setInitialDelay(delai);
 
 		tree = new TreeDrawPanel();
 
 		JPanel menu = new JPanel();
 
 		JPanel affichageArbrePanel = new JPanel(new GridLayout(5, 1));
 
 		menu.add(affichageArbrePanel);
 
 		// Dfinition des boutons
 		JButton newMember = new JButton(new ImageIcon(
 				"../ArbreGen/resources/Icons/ajouter_personne-32.png"));
 		newMember.setToolTipText("Ajouter un membre");
 
 		JButton displayMemberTable = new JButton(new ImageIcon(
 				"../ArbreGen/resources/Icons/table-32.png"));
 		displayMemberTable.setToolTipText("Afficher la table des membres");
 
 		JButton saveImg = new JButton(new ImageIcon(
 				"../ArbreGen/resources/Icons/enregistrer_image-32.png"));
 		saveImg.setToolTipText("Sauvegarder l'image");
 
 		JButton saveTab = new JButton(new ImageIcon("../ArbreGen/resources/Icons/table_save.png"));
 		saveTab.setToolTipText("Sauvegarder le tableau");
 
 		JButton veriffErrors = new JButton(new ImageIcon(
 				"../ArbreGen/resources/Icons/valider-32.png"));
 		veriffErrors.setToolTipText("Vrifier les erreurs");
 
 		// Ajout des boutons au pannel
 
 		affichageArbrePanel.add(newMember, BorderLayout.WEST);
 		affichageArbrePanel.add(displayMemberTable, BorderLayout.WEST);
 		affichageArbrePanel.add(saveImg, BorderLayout.WEST);
 		affichageArbrePanel.add(saveTab, BorderLayout.WEST);
 		affichageArbrePanel.add(veriffErrors, BorderLayout.WEST);
 
		if (filename == null || filename.isEmpty()) {
			tree.setPreferredSize(new Dimension(1024, 760));
			this.add(tree, BorderLayout.CENTER);
		} else {
 			JScrollPane treeScroller = new JScrollPane(tree);
 			treeScroller.setPreferredSize(new Dimension(1024, 760));
 			this.add(treeScroller, BorderLayout.CENTER);
 			CsvPersonDao.getInstance().load(filename);
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					tree.drawTree();
 				}
 			});
 		}
 		this.add(menu, BorderLayout.WEST);
 
 		// Lien des action listener aux boutons
 		newMember.addActionListener(newMember_Action);
 		displayMemberTable.addActionListener(displayMemberTable_Action);
 		saveImg.addActionListener(saveImg_Action);
 		saveTab.addActionListener(saveTab_Action);
 		veriffErrors.addActionListener(veriffErrors_Action);
 
 	}
 
 	public class FiltreSimple extends FileFilter {
 		// Description et extension accepte par le filtre
 		private String description;
 		private String extension;
 
 		// Constructeur  partir de la description et de l'extension accepte
 		public FiltreSimple(String description, String extension) {
 			if (description == null || extension == null) {
 				throw new NullPointerException("La description (ou extension) ne peut tre null.");
 			}
 			this.description = description;
 			this.extension = extension;
 		}
 
 		// Implmentation de FileFilter
 		public boolean accept(File file) {
 			if (file.isDirectory()) {
 				return true;
 			}
 			String nomFichier = file.getName().toLowerCase();
 
 			return nomFichier.endsWith(extension);
 		}
 
 		public String getDescription() {
 			return description;
 		}
 	}
 
 	public void save(Typesave ts) {
 
 		JFileChooser filechoose = new JFileChooser();
 		filechoose.setCurrentDirectory(new File("."));
 		String approve = new String("Enregistrer");
 		int resultatEnregistrer = filechoose.showDialog(filechoose, approve);
 		if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
 			String monFichier = new String(filechoose.getSelectedFile().toString());
 			if (ts == Typesave.DESSIN) {
 				if (monFichier.endsWith(".png") != true && monFichier.endsWith(".PNG") != true) {
 
 					monFichier = monFichier + ".png";
 				}
 			} else {
 
 				if (monFichier.endsWith(".csv") != true && monFichier.endsWith(".CSV") != true) {
 
 					monFichier = monFichier + ".csv";
 
 				}
 			}
 
 			{
 
 				try {
 					FileWriter lu = new FileWriter(monFichier);
 
 					BufferedWriter out = new BufferedWriter(lu);
 
 					switch (ts) {
 					case CSV:
 						// out.write(history.getTextArea());
 						break;
 
 					case DESSIN:
 						ImageIO.write(tree.toBufferedImage(), "PNG", new File(monFichier));
 						break;
 
 					}
 					out.close();
 
 				} catch (IOException er) {
 					;
 				}
 			}
 		}
 	}
 
 	// //////////////////
 	// ActionListener //
 	// //////////////////
 
 	class newMember_Action implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 
 			// Ouverture fenetre nouveau membre
 			System.out.println("NewMember.");
 
 		}
 	}
 
 	class displayMemberTable_Action implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 
 			// Ouverture fenetre tableau
 			System.out.println("displayMemberTable.");
 			new Tableau("Afficher une table", false, null);
 
 		}
 	}
 
 	class saveImg_Action implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 
 			// Sauvegarde de l'image
 			System.out.println("saveImg.");
 			save(Typesave.DESSIN);
 
 		}
 	}
 
 	class saveTab_Action implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 
 			// Sauvegarde de l'image
 			System.out.println("saveTab.");
 			save(Typesave.CSV);
 
 		}
 	}
 
 	class veriffErrors_Action implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 
 			// Sauvegarde de l'image
 			System.out.println("veriffErrors.");
 
 		}
 	}
 
 }
