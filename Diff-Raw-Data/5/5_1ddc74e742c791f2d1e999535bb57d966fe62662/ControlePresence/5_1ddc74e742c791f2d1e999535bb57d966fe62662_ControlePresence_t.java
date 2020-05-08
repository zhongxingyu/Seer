 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 public class ControlePresence extends JFrame {
 
 	private JPanel contentPane, panelDroite, panelGauche;
 
 	private String cheminImage = "./img/smiley.jpg";
 	private ArrayList<Etudiant> listeEtudiants;
 	private JTextField textFieldInput;
 	private JSplitPane splitPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					ControlePresence frame = new ControlePresence();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public ControlePresence() {
 
 		listeEtudiants = new ArrayList<Etudiant>();
 		listeEtudiants.add(new Etudiant("Guillaume Bugnet", "001"));
 		listeEtudiants.add(new Etudiant("Bertrand Magnien", "002"));
 		majFenetre();
 	}
 
 	public void majFenetre() {
 		
 		System.out.println("MISE A JOUR DE L\'affichage de la fentre");
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 550, 300);
 		setResizable(true);
 		contentPane = new JPanel();
 		// contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JLabel titreFenetre = new JLabel("Contrle en cours");
 
 		// Gestion de la droite, ou se trouve l'image et le champ de texte
 		panelDroite = new JPanel();
 		panelDroite.setLayout(new BorderLayout());
 		
 		Box boiteVerticale = Box.createVerticalBox();
 		boiteVerticale.add(Box.createGlue());
 			Box boiteHorizontale1= Box.createHorizontalBox();
 			boiteHorizontale1.add(Box.createGlue());
 
 			ImageIcon icone = createImageIcon(cheminImage);
 			
 			JLabel jlbLabel1 = new JLabel(icone, JLabel.CENTER);
 			jlbLabel1.setPreferredSize(new Dimension(100, 100));
 			boiteHorizontale1.add(jlbLabel1);
 			boiteHorizontale1.add(Box.createGlue());
 			
 		boiteVerticale.add(boiteHorizontale1);
 		
 			Box boiteHorizontale2= Box.createHorizontalBox();
 			boiteHorizontale2.add(Box.createGlue());
 				textFieldInput = new JTextField();
 				textFieldInput.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						rechercheETUpdate();
 					}
 				});
 				textFieldInput.setPreferredSize(new Dimension(170, 30));
 			boiteHorizontale2.add(textFieldInput);
 			boiteHorizontale2.add(Box.createGlue());
 		boiteVerticale.add(boiteHorizontale2);
 		boiteVerticale.add(Box.createGlue());
 		panelDroite.add(boiteVerticale,BorderLayout.CENTER);
 
 		
 		
 		// Gestion de la gauche, liste des lves
 		panelGauche = new JPanel();
 		panelGauche.setLayout(new BorderLayout());
 
 		JTable tabledesEleves = majTableEleves();
 		tabledesEleves.setEnabled(false);
 
 		panelGauche.add(tabledesEleves, BorderLayout.CENTER);
 
 		JSplitPane splitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				panelGauche, panelDroite);
 		splitPane.setDividerLocation(300);
 
 		contentPane.add(splitPane, BorderLayout.CENTER);
 		contentPane.add(titreFenetre, BorderLayout.NORTH);
 		// contentPane.add(panelDroite, BorderLayout.EAST);
 		//
 	}
 
 	public JTable majTableEleves() {
 		int nombreEtudiants = listeEtudiants.size();
 		String[][] lignesEleves = new String[nombreEtudiants][2];
 		int position = 0;
 		for (Etudiant etu : listeEtudiants) {
 			System.out.println("On ecrit " + etu.getNom()
 					+ " il est present : " + etu.getPresent());
 			lignesEleves[position][0] = etu.getNom();
 			if (etu.getPresent()) {
 				System.out.println("On met \"Present\" dans la case");
 				lignesEleves[position][1] = "Present";
 			}
 
 			position++;
 		}
 		String[] nomColonnes = { "Nom", "Prsence" };
 
 		JTable tableEleves = new JTable(lignesEleves, nomColonnes);
 
 		return tableEleves;
 	}
 
 	public void rechercheETUpdate() {
 		String myfareTrouve = textFieldInput.getText();
 		for (Etudiant etu : listeEtudiants) {
 			if (etu.getNumeroMifare().equals(myfareTrouve)) {
 				etu.setPresent(true);
 				System.out.println(myfareTrouve
 						+ " a ete considere comme present : c\'est "
 						+ etu.getNom());
 			}
 		}
 		textFieldInput.setText("");
 		majTableEleves();
 		this.repaint();
 
 	}
 
 	public ImageIcon createImageIcon(String path) {
			ImageIcon retour = new ImageIcon() ;
 	
 			
             try {
 				Image img = ImageIO.read(new File(path));
             	Image mieux = img.getScaledInstance(80, 100, 1);
            	retour.setImage(mieux);
 
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 //			
 //			
 //			try {
 //				retour= new ImageIcon(path,"icone");
 //			} catch (Exception e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 			return retour;
 	}
 }
