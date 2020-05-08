 package ar.proyecto.controller;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFormattedTextField;
 import javax.swing.JTextField;
 
 import ar.proyecto.gui.MainWindow;
 import ar.proyecto.gui.MiddlePanelDelete;
 
 public class ActionMiddlePanelDeleteOk extends AbstractAction {
 
 
 	MainWindow gui;
 	JFormattedTextField dni;
 	JTextField nombreYApellido;	
 	JFormattedTextField fechaDeNacimiento;
 	JTextField direccion;	
 	JTextField telefono;
 	JFormattedTextField puntosCarnet;
 
 
 	public ActionMiddlePanelDeleteOk(MiddlePanelDelete panel, String name) {
 		super(name);
 		this.gui = panel.getGui();
 		this.dni = panel.getTxtDni();
 		this.nombreYApellido = panel.getTxtNombreYApellido();
 		this.fechaDeNacimiento = panel.getTxtFechaDeNacimiento();
 		this.direccion = panel.getTxtDireccion();
 		this.telefono = panel.getTxtTelefono();
 		this.puntosCarnet = panel.getTxtPuntosCarnet();
 	}
 
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("In ActionDeleteOk : received action " + e.getActionCommand());
 		if (e.getActionCommand().equals("OK")) {
 			System.out.println(" In ActionDeleteOk : Check if action is OK");	
 			if (everythigEnteredWell()) {
 				System.out.println(" In ActionDeleteOk : everything entered well");
 				String request = buildRequest();
 				System.out.println(" In ActionDeleteOk : request = " + request);
 				gui.sendRequestToController(request, RequestType.DELETE);
 				System.out.println("In ActionDeleteOk : sending request to controller : " + request);
 			}
 		}
 	}
 
 	private boolean everythigEnteredWell() {
 		// TODO Auto-generated method stub
 		boolean result = true;
		if (dni.getText().isEmpty() && nombreYApellido.getText().isEmpty() && fechaDeNacimiento.getText().equals("  -  -    ") && direccion.getText().isEmpty() && telefono.getText().isEmpty() && puntosCarnet.getText().isEmpty()) {
 			dni.setBackground(Color.RED);
 			nombreYApellido.setBackground(Color.RED);
 			fechaDeNacimiento.setBackground(Color.RED);
 			direccion.setBackground(Color.RED);
 			telefono.setBackground(Color.RED);
 			puntosCarnet.setBackground(Color.RED);	
 			result = false;
 		}
 		else if (telefono.getText().length() > 11) {
 			telefono.setBackground(Color.RED);
 			result = false;
 		}
 		else if (!puntosCarnet.getText().isEmpty()){
 			if (Integer.valueOf(puntosCarnet.getText())> 10 || Integer.valueOf(puntosCarnet.getText())< 0 )
 			{
 				telefono.setBackground(Color.RED);
 				result = false;
 			}
 		}
 		else{
 			dni.setBackground(Color.WHITE);
 			nombreYApellido.setBackground(Color.WHITE);
 			fechaDeNacimiento.setBackground(Color.WHITE);
 			direccion.setBackground(Color.WHITE);
 			telefono.setBackground(Color.WHITE);
 			puntosCarnet.setBackground(Color.WHITE);
 			result = true;
 		}
 		return result;
 	}
 
 	private String buildRequest() {
 		String request = new String();
 		boolean firstNotEmpty = true;
 		request += "DELETE FROM Persona WHERE ";
 		
 		System.out.print("In ActionMiddlePanelDeleteOk : Found not Empty :");
 		
 		if (!dni.getText().isEmpty()) {
 			firstNotEmpty = false;
 			request += " dni = " + dni.getText();		
 			System.out.print("DNI ");
 		}
 		if (!nombreYApellido.getText().isEmpty()) {
 			if (!firstNotEmpty){
 				request += " AND ";
 			}
 			firstNotEmpty = false;
 			request += "nombreYApellido = \'" + nombreYApellido.getText() + "\'";
 			System.out.print("nombreYApellido ");
 		}
		if (!fechaDeNacimiento.getText().equals("  -  -    ")) {
 			if (!firstNotEmpty){
 				request += " AND ";
 			}
 			firstNotEmpty = false;
			request += "fechaDeNacimiento = \'" + fechaDeNacimiento.getText() + "\'";
 			System.out.print("fechaDeNacimiento ");
 		}
 		if (!direccion.getText().isEmpty()) {
 			if (!firstNotEmpty){
 				request += " AND ";
 			}
 			firstNotEmpty = false;
 			request += "direccion =  \'" + direccion.getText() + "\'";
 			System.out.print("direccion ");
 		}
 		if (!telefono.getText().isEmpty()) {
 			if (!firstNotEmpty){
 				request += " AND ";
 			}
 			firstNotEmpty = false;
 			request += "telefono =  \'" + telefono.getText() + "\'";
 			System.out.print("telefono ");
 		}
 
 		if (!puntosCarnet.getText().isEmpty()){
 			if (!firstNotEmpty){
 				request += " AND ";
 			}
 			firstNotEmpty = false;
 			request += "puntosCarnet = " + puntosCarnet.getText();
 			System.out.print("puntosCarnet ");
 		}
 		System.out.println();
 		return request;
 	}
 }
