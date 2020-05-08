 package view;
 
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListModel;
 
 import controlleur.SerieController;
 
 import model.Serie;
 
 public class PanelSerie extends JPanel{
 	private static final long serialVersionUID = 1L;
 
 	public PanelSerie(){
		JPanel PanelSerie = new JPanel();
 		
 		//Recuperation des noms de series
 		ArrayList<String> mesSeries = new ArrayList<String>();
 		/*SerieController ctrl = new SerieController();
 		mesSeries = ctrl.getAllSerieName();*/
 		
 		//Arraylist temporaire
 		mesSeries.add("Malcom");
 		mesSeries.add("TBBT");
 		mesSeries.add("HIMYM");
 		mesSeries.add("The Simpsons");
 		
 			
 		//Passage par un defaultListModel
 		DefaultListModel<String> listModel = new DefaultListModel<>();
 		for(int i = 0; i <mesSeries.size(); i++){
 			listModel.addElement(mesSeries.get(i));
 		}
 			
 		
 		//Creation et remplissage de la JList
 		JList listSerie = new JList(listModel);
 		
 		//Ajout des components
		PanelSerie.add(listSerie);
 		
 		
 	}
 }
