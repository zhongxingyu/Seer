 package ar.proyecto.controller;
 
import java.sql.ResultSet;
import java.sql.SQLException;

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
 
 		//si el request type esta select
 		if (type == RequestType.SELECT){	

			try {
				// resulta toma el resultat de la execucion de la request en el modelo
				ResultSet result = dcModel.executeSelectBasic(request);
				// update la gui
				System.out.println("In Controller : sendRequestToModelAndUpdateGui executed a SELECT ");
				gui.setResult(result);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("In Controller : sendRequestToModelAndUpdateGui executed a SELECT ");
				gui.setResult(e.getMessage());
			}
 		}
 		//si el request type esta insert
 		else if (type == RequestType.INSERT){
			String result;
 			result = dcModel.executeInsert(request);;
 			System.out.println("In Controller : sendRequestToModelAndUpdateGui executed an INSERT ");
 			gui.setResult(result);
 		}
 		//si el request type esta delete
 		else if (type == RequestType.DELETE){
			String result;
 			result = dcModel.executeDelete(request);
 			System.out.println("In Controller : sendRequestToModelAndUpdateGui executed an DELETE ");
 			gui.setResult(result);
 		}
 
 		System.out.println("In Controller : sendRequestToModelAndUpdateGui updated gui");
 	}
 
 }
