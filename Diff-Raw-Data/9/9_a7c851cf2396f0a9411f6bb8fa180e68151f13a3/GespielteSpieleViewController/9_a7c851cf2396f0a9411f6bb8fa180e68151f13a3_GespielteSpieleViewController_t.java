 package controller;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.Label;
 import javafx.scene.layout.Pane;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Circle;
 import javafx.stage.Stage;
 import spieldaten.Satz;
 import spieldaten.GespielteSpieleModel;
 import spieldaten.Spieler;
 import spieldaten.Zug;
 
 
 
 public class GespielteSpieleViewController {
 		
 		private Scene scene;
 		private GespielteSpieleModel model;
 		private final Color eigeneFarbe = Color.web("0x33CC66");
 		private final Color gegnerFarbe = Color.web("0xFF3333");
 		@FXML
 		private Label simulationSpielnummer, simulationEndergebnis, simulationZug;
 		@FXML
 		private Label simulationSatzstatus, simulationRunde, simulationSpiel, simulationSatz;
 		@FXML
 		private Label simulationSpielstandHeim, simulationSpielstandGast;
 		@FXML
 		private Button simulationBeendenButton;
 		
 		@FXML
 		private Circle a1;
 		@FXML
 		private Circle a2;
 		@FXML
 		private Circle a3;
 		@FXML
 		private Circle a4;
 		@FXML
 		private Circle a5;
 		@FXML
 		private Circle a6;
 		@FXML
 		private Circle b1;
 		@FXML
 		private Circle b2;
 		@FXML
 		private Circle b3;
 		@FXML
 		private Circle b4;
 		@FXML
 		private Circle b5;
 		@FXML
 		private Circle b6;
 		@FXML
 		private Circle c1;
 		@FXML
 		private Circle c2;
 		@FXML
 		private Circle c3;
 		@FXML
 		private Circle c4;
 		@FXML
 		private Circle c5;
 		@FXML
 		private Circle c6;
 		@FXML
 		private Circle d1;
 		@FXML
 		private Circle d2;
 		@FXML
 		private Circle d3;
 		@FXML
 		private Circle d4;
 		@FXML
 		private Circle d5;
 		@FXML
 		private Circle d6;
 		@FXML
 		private Circle e1;
 		@FXML
 		private Circle e2;
 		@FXML
 		private Circle e3;
 		@FXML
 		private Circle e4;
 		@FXML
 		private Circle e5;
 		@FXML
 		private Circle e6;
 		@FXML
 		private Circle f1;
 		@FXML
 		private Circle f2;
 		@FXML
 		private Circle f3;
 		@FXML
 		private Circle f4;
 		@FXML
 		private Circle f5;
 		@FXML
 		private Circle f6;
 		@FXML
 		private Circle g1;
 		@FXML
 		private Circle g2;
 		@FXML
 		private Circle g3;
 		@FXML
 		private Circle g4;
 		@FXML
 		private Circle g5;
 		@FXML
 		private Circle g6;
 		
 		private Circle feld[][] = new Circle[7][6];
 		
 		@FXML
 		private ChoiceBox<Satz> satzauswahlBoxSimulation;
 		
 		public GespielteSpieleViewController(GespielteSpieleModel sModel){
 			this.model = sModel;
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/GespielteSpieleView.fxml"));
 			fxmlLoader.setController(this);
 						
 			try{
 				Pane pane = (Pane)fxmlLoader.load();
 				scene = new Scene(pane);
 						
 			}catch(Exception ex){
 				ex.printStackTrace();
 			}
 			
 			feld[0][0] = a1;
 			feld[1][0] = b1;
 			feld[2][0] = c1;
 			feld[3][0] = d1;
 			feld[4][0] = e1;
 			feld[5][0] = f1;
 			feld[6][0] = g1;
 			feld[0][1] = a2;
 			feld[1][1] = b2;
 			feld[2][1] = c2;
 			feld[3][1] = d2;
 			feld[4][1] = e2;
 			feld[5][1] = f2;
 			feld[6][1] = g2;
 			feld[0][2] = a3;
 			feld[1][2] = b3;
 			feld[2][2] = c3;
 			feld[3][2] = d3;
 			feld[4][2] = e3;
 			feld[5][2] = f3;
 			feld[6][2] = g3;
 			feld[0][3] = a4;
 			feld[1][3] = b4;
 			feld[2][3] = c4;
 			feld[3][3] = d4;
 			feld[4][3] = e4;
 			feld[5][3] = f4;
 			feld[6][3] = g4;
 			feld[0][4] = a5;
 			feld[1][4] = b5;
 			feld[2][4] = c5;
 			feld[3][4] = d5;
 			feld[4][4] = e5;
 			feld[5][4] = f5;
 			feld[6][4] = g5;
 			feld[0][5] = a6;
 			feld[1][5] = b6;
 			feld[2][5] = c6;
 			feld[3][5] = d6;
 			feld[4][5] = e6;
 			feld[5][5] = f6;
 			feld[6][5] = g6;
 			
 			for(Satz satz : model.getSpiel().getSaetze())
 				satzauswahlBoxSimulation.getItems().add(satz);
 			
 			
 			if(!satzauswahlBoxSimulation.getItems().isEmpty())
 				satzauswahlBoxSimulation.getSelectionModel().select(satzauswahlBoxSimulation.getItems().get(0));
 			
 			simulationStarten();
 			
 			satzauswahlBoxSimulation.getSelectionModel().selectedIndexProperty().addListener(
 					new ChangeListener<Number>() {
 						public void changed(ObservableValue ov,Number alterSatz,Number neuerSatz){
 							simulationStarten();
 						}
 					}
 			);
 	}
 	
 	public void show(){
 		Stage stage = model.getStage();
 		stage.setScene(scene);
 		stage.show();
 		simulationStarten();
 	}
 	
 	private Color getAktuelleFarbe(Spieler spieler){
 		return spieler == model.getSpiel().getSelbst() ? eigeneFarbe : gegnerFarbe;
 	}
 	
 	public void simulationStarten(){
 		Satz ausgewaehlterSatz = satzauswahlBoxSimulation.getSelectionModel().getSelectedItem();
 		for(int i=0;i<7;i++)
 			for(int j=0;j<6;j++)
 				feld[i][j].setFill(Color.WHITE);
 		for(Zug zug : ausgewaehlterSatz.getZuege())
 			feld[zug.getSpalte()][zug.getZeile()].setFill(this.getAktuelleFarbe(zug.getSpieler()));
 		
 	}
 	
 	//Simulation Beenden und Fenster schliessen
 	@FXML
 	public void simulationBeenden(){
 		model.getStage().close();
 	}
 }
