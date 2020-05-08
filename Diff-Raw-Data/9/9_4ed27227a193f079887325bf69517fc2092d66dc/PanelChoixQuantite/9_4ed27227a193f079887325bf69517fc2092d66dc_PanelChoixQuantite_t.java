 package ihm.barresOutils;
 
 import ihm.actions.ValiderQuantiteAction;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.text.NumberFormat;
 
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 
 @SuppressWarnings("serial")
 public class PanelChoixQuantite extends JPanel implements ItemListener {
 	private boolean subventionne, paye, donne;
 	private int quantite;
 	private JCheckBox chckSubventionne, chckPaye, chckDonne;
 	private JFormattedTextField textField;
 	
 	public PanelChoixQuantite (ValiderQuantiteAction keyValiderAction) {
 		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		
 		this.subventionne = false;
 		this.paye = false;
 		this.donne = false;
 		this.quantite = 0;
 		
 		JTextPane txtpn = new JTextPane();
 		txtpn.setText("Choisissez la quantité de billets à commander");
 		
 		// choix de la quantite de billets a commander
 		textField = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		textField.setValue(1);
 		textField.setColumns(20);
 		textField.addKeyListener(keyValiderAction);
 		
 		chckSubventionne = new JCheckBox("Subventionne");
 		chckSubventionne.setSelected(false);
 		chckSubventionne.addItemListener(this);
 		chckPaye = new JCheckBox("Paye");
 		chckPaye.setSelected(false);
 		chckPaye.addItemListener(this);
 		chckDonne = new JCheckBox("Donne");
 		chckDonne.setSelected(false);
 		chckDonne.addItemListener(this);
 		
 		this.add(txtpn);
 		this.add(textField);
 		this.add(chckSubventionne);
 		this.add(chckPaye);
 		this.add(chckDonne);
 	}
 
 	public boolean getSubventionne() {
 		return subventionne;
 	}
 	
 	public boolean getPaye() {
 		return paye;
 	}
 	
 	public boolean getDonne() {
 		return donne;
 	}
 	
 	public int getQuantite() {
		quantite = Integer.parseInt(textField.getText());
 		return quantite;
 	}
 
 	public void itemStateChanged(ItemEvent e) {
         Object source = e.getItemSelectable();
         boolean selectionne = (e.getStateChange() == ItemEvent.SELECTED);
         
         if (source == chckSubventionne) {
         	subventionne = selectionne;
         } else if (source == chckPaye) {
         	paye = selectionne;
         } else if (source == chckDonne) {
             donne = selectionne;
         }
     }
 }
