 package Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.LoadXMLFile;
 import Model.PhysicalRunway;
 import View.MainFrame;
 
 public class OpenAirportListener implements ActionListener, AirportObserver{
 
 	Airport airport;
 	List<AirportObserver> airportObservers;
 
 	public OpenAirportListener(Airport airport, List<AirportObserver> aiportObserver){
 		this.airportObservers = aiportObserver;
 		this.airport = airport;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		LoadXMLFile lf = new LoadXMLFile();
 		Airport ap;
 
 		try {
 			ap = lf.loadAirport();
 
 			System.out.println("This is the airport opened:: " + ap.getName());
 			//iterate over the runways in the loaded airport and print all values
 			for (PhysicalRunway r : ap.getPhysicalRunways()) { // this will show all the physical runways
 				System.out.println(((PhysicalRunway) r).getId() 
 						+" "+ r.getRunway(0).getName() 
 						+" "+ r.getRunway(0).getTORA(1)
 						+" "+ r.getRunway(0).getASDA(1)
 						+" "+ r.getRunway(0).getTODA(1)
 						+" "+ r.getRunway(0).getLDA(1)
 
 						+" "+ r.getRunway(1).getName()
 						+" "+ r.getRunway(1).getTORA(1)
 						+" "+ r.getRunway(1).getASDA(1)
 						+" "+ r.getRunway(1).getTODA(1)
 						+" "+ r.getRunway(1).getLDA(1)
 
 						);
 			}
 
 			if(airport.getPhysicalRunways().size() != 0) { 
 				airport.setCurrentPhysicalRunway(airport.getPhysicalRunways().get(0));
 				airport.setCurrentRunway(airport.getCurrentPhysicalRunway().getRunway(0));
 			}
 			
 			notifyAirportObservers();
 			try {
 				MainFrame.saveRecentFile(ap, lf.getFilename());
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
		} catch (Exception ex) {}
 
 
 	}
 
 	@Override
 	public void updateAirport(Airport airport) {
 		this.airport = airport;
 
 	}
 	
 	void notifyAirportObservers() {
 		for(AirportObserver ao: airportObservers){
 			ao.updateAirport(this.airport);
 		}
 	}
 
 }
 
 
 
 /*
 public void actionPerformed(ActionEvent arg0) {
 /*
  *  Open airport code goes here - need to reset airport if the user decides to cancel
 
 LoadXMLFile lf = new LoadXMLFile();
 Airport ap = airport;
 try {
 	airport = lf.loadFile();
 	System.out.println("This is the airport opened: " + airport.getName());
 	//iterate over the runways in the loaded airport and print all values
 	for (PhysicalRunway r : airport.runways()) { // this will show all the physical runways
 		System.out.println(((PhysicalRunway) r).getId() 
 				+" "+ r.getRunway(0).getName() 
 				+" "+ r.getRunway(0).getTORA(1)
 				+" "+ r.getRunway(0).getASDA(1)
 				+" "+ r.getRunway(0).getTODA(1)
 				+" "+ r.getRunway(0).getLDA(1)
 
 				+" "+ r.getRunway(1).getName()
 				+" "+ r.getRunway(1).getTORA(1)
 				+" "+ r.getRunway(1).getASDA(1)
 				+" "+ r.getRunway(1).getTODA(1)
 				+" "+ r.getRunway(1).getLDA(1)
 
 				);
 	}
 	lblCurrentAirport.setText("Current Airport: " + airport.getName());
 } catch (Exception e) {}
 if (airport == null) {
 	airport = ap;
 }
 }
 }
  */
