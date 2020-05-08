 package ar.proyecto.controller;
 
 import ar.proyecto.gui.MainWindow;
 import ar.proyecto.model.DatabaseConnection;
 
 public class Controller {
 	//Para utilizar el modelo
 	private DatabaseConnection dcModel;
 	//Para modificar la GUI
 	private MainWindow gui;
 	
 	
 	//Creador
 	public Controller() {
 		gui = new MainWindow();
 		dcModel = new DatabaseConnection();
 		gui.setController(this);
 	}
 	
 	//Executar una consulta y modificar la gui
 	public void sendRequestToModelAndUpdateGui(String request, RequestType type) {
 		System.out.println("In Controller : sendRequestToModelAndUpdateGui received " + request);
 
 		String result;
 		//si el request type esta select
 		if (type == RequestType.SELECT){	
 			// resulta toma el resultat de la execucion de la request en el modelo
 			result = dcModel.executeSelect(request);
 			// update la gui
 			System.out.println("In Controller : sendRequestToModelAndUpdateGui executed an SELECT ");
 			gui.setResult(result);
 		}
 		//si el request type esta insert
 		else if (type == RequestType.INSERT){
			dcModel.executeInsert(request);
			result = dcModel.executeSelect(request);
 			System.out.println("In Controller : sendRequestToModelAndUpdateGui executed an INSERT ");
 			gui.setResult(result);
 		}
 		//si el request type esta delete
 		else if (type == RequestType.DELETE){
			dcModel.executeDelete(request);
			result = dcModel.executeSelect(request);
 			System.out.println("In Controller : sendRequestToModelAndUpdateGui executed an DELETE ");
 			gui.setResult(result);
 		}
 
 		System.out.println("In Controller : sendRequestToModelAndUpdateGui updated gui");
 	}
 
 }
