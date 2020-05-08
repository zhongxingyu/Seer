 /**
  * @author Hadrien Milano <Sciper : 224340>
  * @author Christophe Tafani-Dereeper <Sciper : 223529>
  */
 
 package ch.epfl.flamemaker.gui;
 
 import java.awt.event.FocusAdapter;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DecimalFormat;
import java.text.ParseException;
 import java.util.ArrayList;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.InputVerifier;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
 import javax.swing.JLabel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingUtilities;
 
 import ch.epfl.flamemaker.flame.ObservableFlameBuilder;
 import ch.epfl.flamemaker.flame.Variation;
 
 /**
  *	Classe représentant le composant de modification
  *	des poids des variations
  */
 @SuppressWarnings("serial")
 public class WeightsModificationComponent extends JComponent {
 
 	/**
 	 * Le bâtisseur de fractale
 	 */
 	final private ObservableFlameBuilder flameBuilder;
 	
 	/**
 	 *	La transformation actuellement sélectionnée 
 	 */
 	private int selectedTransformationIndex;
 	
 	
 	/**
 	 *	La liste des champs permettant de modifier les poids 
 	 */
 	final private ArrayList<JFormattedTextField> fields = new ArrayList<JFormattedTextField>();
 
 
 	public WeightsModificationComponent(
 			final ObservableFlameBuilder flameBuilder) {
 
 		this.flameBuilder = flameBuilder;
 
 		GroupLayout weightsGroup = new GroupLayout(this);
 		this.setLayout(weightsGroup);
 
 		SequentialGroup H = weightsGroup.createSequentialGroup();
 		weightsGroup.setHorizontalGroup(H);
 		SequentialGroup V = weightsGroup.createSequentialGroup();
 		weightsGroup.setVerticalGroup(V);
 
 		// Autant de groupes verticaux que de variations, et 2 lignes
 		ArrayList<ParallelGroup> verticalGroups = new ArrayList<ParallelGroup>();
 		ParallelGroup currentGroup;
 		int nbVariations = Variation.values().length;
 		for (int i = 0; i < nbVariations ; i++) {
 			currentGroup = weightsGroup.createParallelGroup(GroupLayout.Alignment.TRAILING);
 			verticalGroups.add(currentGroup);
 			H.addGroup(currentGroup);
 		}
 		ArrayList<ParallelGroup> horizontalGroups = new ArrayList<ParallelGroup>();
 		for (int i = 0; i < 2; i++) {
 			currentGroup = weightsGroup.createParallelGroup(GroupLayout.Alignment.BASELINE);
 			horizontalGroups.add(currentGroup);
 			V.addGroup(currentGroup);
 		}
 		
 		int h = 0, v = 0;
 		// On parcourt la liste des variations
 		for (Variation variation : Variation.values()) {
 			// On crée le champ de texte et l'étiquette associées
 			final JLabel label = new JLabel(variation.printableName());
 			final JFormattedTextField formattedTextField = buildFormattedTextField();
 			
 			fields.add(formattedTextField);
 			
 			// Crée une copie finale pour l'utilisation dans le listener
 			final Variation fVariation = variation;
 		
 			formattedTextField.addPropertyChangeListener("value",
 					new PropertyChangeListener() {
 
 				@Override
 				public void propertyChange(PropertyChangeEvent evt) {
 					// Lorsque le champ de texte contenant le poids est modifié, on met à jour le bâtisseur
 					double newWeight = ((Number) formattedTextField
 							.getValue()).doubleValue();
 
 					/*
 					*  Le bâtisseur est mis à jour seulement si le poids entré diffère de l'actuel
 					* Cela évite notamment le redessin inutile de la fractale lorsque le composant
 					* de modification des poids est mis à jour quand une nouvelle transformation est sélectionnée
 					*/
 					if (flameBuilder.getTransformation(selectedTransformationIndex).weight(fVariation) != newWeight ) {
 						flameBuilder.setVariationWeight(
 								selectedTransformationIndex,
 								fVariation, newWeight);
 					}
 				}
 			});
 
 			horizontalGroups.get(h).addComponent(label);
 			verticalGroups.get(v).addComponent(label);
 
 			horizontalGroups.get(h).addComponent(formattedTextField);
 			verticalGroups.get(v + 1).addComponent(formattedTextField);
 
 			// On incrémente la colonne de 2 (label + champ de texte ajoutés)
 			v += 2;
 			
 			// Lorsque la moitié des variations a été ajoutée au composant, on change de ligne
 			if (v % nbVariations == 0) {
 				h++;
 				v = 0;
 			}
 
 			H.addPreferredGap(ComponentPlacement.UNRELATED);
 		}
 
 	}
 
 	/**
 	 * Met à jour le composant lorsqu'une nouvelle transformation
 	 * a été sélectionnée
 	 * 
 	 * @param id	La nouvelle transformation sélectionnée
 	 */
 	public void setSelectedTransformationIndex(int id) {
 		if (id != -1) {
 			this.selectedTransformationIndex = id;
 
 			/*
 			 * On récupère le tableau des poids de la nouvelle transformation,
 			 * et on remplit nos 6 champs avec
 			 */
 			double[] newWeights = flameBuilder.getTransformation(id).weights();
 			int i = 0;
 			for (double weight : newWeights) {
 				fields.get(i).setValue(weight);
 				i++;
 			}
 		}
 	}
 
 	/**
 	 * Construit un champ de texte formatté (évite la duplication de code)
 	 * @return	Le champ de texte
 	 */
 	private JFormattedTextField buildFormattedTextField() {
 		final JFormattedTextField field = new JFormattedTextField(
				new DecimalFormat("###"));
 		field.setValue(0);
 		field.setColumns(3);
 
 		/*
 		 * On fait en sorte que les valeurs des champs se sélectionnent au focus
 		 * Note : On doit emballer le selectAll() dans un invokeLater, sinon le
 		 * formattage du champ enlève la sélection
 		 */
 		field.addFocusListener(new FocusAdapter() {
 			public void focusGained(java.awt.event.FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						field.selectAll();
 					}
 				});
 			}
 		});
 
 		return field;
 	}
 }
