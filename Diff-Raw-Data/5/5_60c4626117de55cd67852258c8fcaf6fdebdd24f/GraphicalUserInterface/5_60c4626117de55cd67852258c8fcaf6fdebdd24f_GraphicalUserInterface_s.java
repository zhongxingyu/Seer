 package userinterface;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IContainerFormat;
 
 import userinterface.OngletLecteur;
 
 public class GraphicalUserInterface extends JFrame implements ActionListener
 {
 	private static final long serialVersionUID = 5373991180139317820L;
 
 	private JTabbedPane onglets;
 
 	private JMenuItem aideAPropos;
 	private JMenuItem fichierFermer;
 	private JMenuItem fichierOuvrir;
 
 	private ModeleTableau modeleTableau;
 
 	private JScrollPane scrollPane;
 
 	public GraphicalUserInterface()
 	{
 		/*
 		 * Conteneur
 		 */
 		onglets = new JTabbedPane();
 
 		/*
 		 * Menu
 		 */
 		fichierFermer = new JMenuItem("Fermer");
 		fichierFermer.addActionListener(this);
 
 		fichierOuvrir = new JMenuItem("Ouvrir");
 		fichierOuvrir.addActionListener(this);
 
 		JMenu menuFichier = new JMenu("Fichier");
 		menuFichier.add(fichierOuvrir);
 		menuFichier.addSeparator();
 		menuFichier.add(fichierFermer);
 
 		aideAPropos = new JMenuItem("À propos");
 		aideAPropos.addActionListener(this);
 
 		JMenu menuOutils = new JMenu("Outils");
 
 		JMenu menuAide = new JMenu("Aide");
 		menuAide.add(aideAPropos);
 
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.add(menuFichier);
 		menuBar.add(menuOutils);
 		menuBar.add(menuAide);
 
 
 		modeleTableau = new ModeleTableau();
 		modeleTableau.addColumn("Nom de l'enregistrement");
 		modeleTableau.addColumn("Durée");
 
 		JTable table = new JTable(modeleTableau);
 
 		scrollPane = new JScrollPane(table);
 		scrollPane.setPreferredSize(new Dimension(270, 800));
 		scrollPane.setAutoscrolls(true);
 
 		Box boxLabel = Box.createVerticalBox();
 		boxLabel.add(Box.createVerticalStrut(7));
 		boxLabel.add(new JLabel("Liste des enregistrements"));
 		boxLabel.add(Box.createVerticalStrut(2));
 		boxLabel.add(new JSeparator(SwingConstants.HORIZONTAL));
 		boxLabel.add(Box.createVerticalStrut(5));
 
 		JPanel panelEnregistrements = new JPanel(new BorderLayout());
 		panelEnregistrements.add(boxLabel,BorderLayout.NORTH);
 		panelEnregistrements.add(scrollPane,BorderLayout.CENTER);
 
 		/*
 		 * Conteneur
 		 */
 		JPanel conteneur = new JPanel(new BorderLayout());
 		conteneur.add(onglets,BorderLayout.CENTER);
 		conteneur.add(panelEnregistrements,BorderLayout.EAST);
 		/*
 		 * Fenêtre
 		 */
 		this.setBackground(Color.WHITE);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(true);
 		this.setTitle("LieLab");
 		this.setLocationRelativeTo(null);
 		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		this.setContentPane(conteneur);
 		this.setJMenuBar(menuBar);
 		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		this.setVisible(true);
 	}
 
 	public void ajouterOnglet(OngletLecteur onglet)
 	{
 		JButton boutonFermeture = new JButton(new ImageIcon("images/CloseTab.png"));
 		boutonFermeture.setToolTipText("Fermer cet onglet");
 		boutonFermeture.setContentAreaFilled(false);
 		boutonFermeture.setFocusable(false);
 		boutonFermeture.setBorder(BorderFactory.createEmptyBorder());
 		boutonFermeture.setBorderPainted(false);
 		boutonFermeture.addActionListener(new FermetureOngletListener(this.onglets, onglet));
 
 		JPanel panelFermeture = new JPanel();
 		panelFermeture.setBackground(new Color(0, 0, 0, 0));
 		panelFermeture.add(new JLabel(onglet.getNom()));
 		panelFermeture.add(boutonFermeture);
 
 		this.onglets.add(onglet);
 		this.onglets.setTabComponentAt(this.onglets.getTabCount() - 1, panelFermeture);
 	}
 
 	public void quitter()
 	{
 		System.exit(0);
 	}
 
 	protected void processWindowEvent(WindowEvent event)
 	{
 		if (event.getID() == WindowEvent.WINDOW_CLOSING)
 		{
 			this.quitter();
 		}
 		else
 			super.processWindowEvent(event);
 	}
 
 	public static void main(String args[])
 	{
 		System.setProperty("awt.useSystemAAFontSettings", "on");
 		System.setProperty("swing.aatext", "true");
 		javax.swing.SwingUtilities.invokeLater(new Runnable()
 				{
 					public void run()
 		{
 			new GraphicalUserInterface();
 		}
 		});
 	}
 	/**
 	 * Affiche une popup qui signale une erreur
 	 *
 	 * @param message
 	 *            Le message d'erreur à afficher
 	 */
 	public static void popupErreur(String message)
 	{
 		JOptionPane.showMessageDialog(null, message, "Erreur", JOptionPane.ERROR_MESSAGE);
 	}
 
 	/**
 	 * Affiche une popup qui signale une erreur
 	 *
 	 * @param message
 	 *            Le message d'erreur à afficher
 	 * @param title
 	 *            Le titre de la popup
 	 */
 	public static void popupErreur(String message, String title)
 	{
 		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
 	}
 	@Override
 		public void actionPerformed(ActionEvent event)
 		{
 			if (event.getSource() == fichierFermer)
 			{
 				this.quitter();
 			}
 			else if (event.getSource() == aideAPropos)
 			{
 				JOptionPane.showMessageDialog(null, "Projet de détection de mensonge", "À propos",
 						JOptionPane.PLAIN_MESSAGE);
 			}
 			else if (event.getSource() == fichierOuvrir)
 			{
 				JFileChooser fileChooser = new JFileChooser();
 				IContainer containerInput = IContainer.make();
 
 				fileChooser.showOpenDialog(this);
 				if (fileChooser.getSelectedFile() != null)
 				{
 					try
 					{
 						if (containerInput.open(fileChooser.getSelectedFile().getCanonicalPath(), IContainer.Type.READ, null) < 0)
 							throw new Exception("Impossible d'ouvrir ce fichier, format non géré.");
 						else
 						{
 							try
 							{
 								this.ajouterOnglet(new OngletLecteur(new File(fileChooser.getSelectedFile().getCanonicalPath())));
 							}
 							catch (IOException e)
 							{
 								popupErreur(e.getMessage());
 							}
 
 						}
 					}
 					catch (Exception e1)
 					{
 						popupErreur(e1.getMessage());
 					}
 				}
 			}
 		}
 }
 
 class FermetureOngletListener implements ActionListener
 {
 	private JTabbedPane onglets;
 	private OngletLecteur onglet;
 
 	public FermetureOngletListener(JTabbedPane onglets, OngletLecteur onglet)
 	{
 		this.onglet = onglet;
 		this.onglets = onglets;
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		onglet.fermerOnglet();
 		onglets.remove(onglet);
 
 	}
 }
