 package mensonge.userinterface;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.sql.ResultSet;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import mensonge.core.BaseDeDonnees.BaseDeDonnees;
 
 
 public class DialogueNouveauSujet extends JDialog
 {
 	private static final long serialVersionUID = 1L;
 
	private JComboBox combo = new JComboBox();
 	private JLabel label = new JLabel("Liste des catégories");
 
 	private JButton envoyer = new JButton("Valider");
 
 	private Object[] retour = new Object[1];
 
 	public DialogueNouveauSujet(JFrame parent, String title, boolean modal, BaseDeDonnees bdd)
 	{
 		super(parent, title, modal);
 		JPanel pan = new JPanel(), j1 = new JPanel(), bouton = new JPanel();
 		try
 		{
 			combo.addItem("Ne rien changer");
 			retour[0] = new String("Ne rien changer");
 			ResultSet rs = bdd.getListeSujet();
 			while (rs.next())
 			{
 				combo.addItem(rs.getString("nomsuj"));
 			}
 			rs.close();
 		}
 		catch (Exception e)
 		{
 
 		}
 		envoyer.addMouseListener(new Envoyer());
 		combo.addItemListener(new comboListner());
 
 		j1.add(label);
 		j1.add(combo);
 
 		bouton.add(envoyer);
 
 		pan.add(j1);
 		pan.add(envoyer);
 
 		this.setContentPane(pan);
 		this.setLocationRelativeTo(null);
 		this.setTitle("Changer de sujet");
 		this.setSize(350, 120);
 	}
 
 	public Object[] activer()
 	{
 		this.setVisible(true);
 		return retour;
 	}
 
 	class comboListner implements ItemListener
 	{
 		@Override
 		public void itemStateChanged(ItemEvent e)
 		{
 			retour[0] = e.getItem().toString();
 		}
 	}
 
 	class Envoyer implements MouseListener
 	{
 		@Override
 		public void mouseClicked(MouseEvent e)
 		{
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent e)
 		{
 		}
 
 		@Override
 		public void mouseExited(MouseEvent e)
 		{
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			setVisible(false);
 		}
 
 	}
 }
