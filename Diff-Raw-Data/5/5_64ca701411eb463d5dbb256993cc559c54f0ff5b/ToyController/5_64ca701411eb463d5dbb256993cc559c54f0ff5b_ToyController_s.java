 package chalmers.dax021308.ecosystem.controller;
 
 
 import chalmers.dax021308.ecosystem.view.GraphView;
 import chalmers.dax021308.ecosystem.view.SimulationView;
 import chalmers.dax021308.ecosystem.view.SimulationView2;
 
 import java.awt.Dimension;
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 
 /**
  * Controller class
  * 
  * @author Henrik Ernstsson
  */
 public class ToyController implements IController {
 
 	private EcoWorld model;
 	private GraphView graphView;
 
 	public ToyController() {
 		init();
 	}
 
 	@Override
 	public void init() {
 		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		d.height = d.height - 40;
 		
 		this.model = new EcoWorld(d, 15, Integer.MAX_VALUE);
		//Uncommend below to run without delay.
//		this.model = new EcoWorld(d);
 		
 		//Uncomment to start model.
 		model.start();
 		
 		//OpenGL 
 		SimulationView2 simView = new SimulationView2(model, d, true);
 		//Java AWT
 		//SimulationView simView = new SimulationView(model, d, true);
 		simView.init();
 		
 		//this.graphView = new GraphView(model);
 		//graphView.init();
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 	}
 
 }
