 package ihm.fenetres;
 
 import java.awt.BorderLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableRowSorter;
 
 import modele.ListeAchats;
 import modele.Personne;
 
 @SuppressWarnings("serial")
 public class FenetreDetails extends Fenetre {
 	private JPanel contentPane;
 	private JTable tableau;
 	
 	public FenetreDetails(Personne personne) {
 		//Fenetre
 		this.setTitle("Informations sur les achat de " + personne);
 		contentPane = new JPanel();
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
 		this.add(contentPane);
 		
 		//Restant a payer
 		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.add(new JLabel("Restant a payer : " + personne.restantAPayer() + "euros"), "North");
 		
 		// Tableau
 		tableau = new JTable(personne.getAchats());
 		personne.getAchats().setTableau(tableau);
 		tableau.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		tableau.setAutoCreateRowSorter(true);// Gestion des tableaux triables
 		TableRowSorter<ListeAchats> sorter = new TableRowSorter<ListeAchats>((ListeAchats) tableau.getModel());   
 		tableau.setRowSorter(sorter);
 		sorter.setSortsOnUpdates(true);
 		contentPane.add(new JScrollPane(tableau), "Center");
 		
 		this.afficher();
 	}
 }
