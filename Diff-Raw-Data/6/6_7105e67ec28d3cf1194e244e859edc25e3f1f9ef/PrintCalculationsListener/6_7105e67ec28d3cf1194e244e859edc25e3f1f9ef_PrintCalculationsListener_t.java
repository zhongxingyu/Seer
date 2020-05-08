 package Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.Print;
 
 public class PrintCalculationsListener implements ActionListener, AirportObserver{
 
 	Airport airport;
 	StringBuilder body;
 	Print print = new Print();
 	
 	public PrintCalculationsListener(Airport airport){
 		this.airport = airport;
 	}
 
 	@Override
 	public void updateAirport(Airport airport) {
 		this.airport = airport;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(airport.getCurrentPhysicalRunway() == null){
 			JOptionPane.showMessageDialog(null, "No physical Runway selected", "", JOptionPane.ERROR_MESSAGE);
 		}
 		else if (airport.getCurrentPhysicalRunway().getObstacle() == null){
 			JOptionPane.showMessageDialog(null, "Runway does not contain an Obstacle", "", JOptionPane.ERROR_MESSAGE);
 		}
 		else if (airport.getCurrentRunway() == null){
 			JOptionPane.showMessageDialog(null, "No Runway Selected", "", JOptionPane.ERROR_MESSAGE);
 		}
 		else {
 			body = new StringBuilder();
			body.append(airport.getName() + "\n\n");
			body.append(airport.getCurrentPhysicalRunway().toDetails(airport.getCurrentRunway().getName()) + "\n");
 			body.append(airport.getCurrentPhysicalRunway().getRunway(0).getName());
			body.append(airport.getCurrentPhysicalRunway().toCalculation(airport.getCurrentPhysicalRunway().getRunway(0).getName())+ "\n");
 			body.append(airport.getCurrentPhysicalRunway().getRunway(1).getName());
 			body.append(airport.getCurrentPhysicalRunway().toCalculation(airport.getCurrentPhysicalRunway().getRunway(1).getName()));
 			print.print(body.toString());
 		}
 	}
 }
