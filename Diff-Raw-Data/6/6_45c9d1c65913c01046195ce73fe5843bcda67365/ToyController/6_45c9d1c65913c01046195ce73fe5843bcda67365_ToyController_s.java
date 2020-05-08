 package chalmers.dax021308.ecosystem.controller;
 
 
 import chalmers.dax021308.ecosystem.view.GraphPopulationAmountView;
 import chalmers.dax021308.ecosystem.view.IGraphView;
 import chalmers.dax021308.ecosystem.view.AWTSimulationView;
 import chalmers.dax021308.ecosystem.view.OpenGLSimulationView;
 
 import java.awt.Dimension;
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 
 /**
  * Controller class
  * 
  * @author Henrik Ernstsson
  */
 public class ToyController implements IController {
 
 	private EcoWorld model;
 	private IGraphView graphView;
 
 	public ToyController() {
 		init();
 	}
 
 	@Override
 	public void init() {
 		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		d.height = d.height - 40;
 		
 		int tickDelay = 16;
 		int numIterations = Integer.MAX_VALUE;
 		boolean recordSimulation = false;
 		Dimension f = new Dimension(d.width-16, d.height-39);
 		this.model = new EcoWorld(f, tickDelay, numIterations, recordSimulation);
 		//Uncommend below to run without delay.
 //		this.model = new EcoWorld(d);
 		
 		//Uncomment to start model.
 		model.start();
 		
 		//OpenGL 
 		OpenGLSimulationView simView = new OpenGLSimulationView(model, d, true);
 		//Java AWT
 //		AWTSimulationView simView = new AWTSimulationView(model, d, true);
 		simView.init();
 		
 		this.graphView = new GraphPopulationAmountView(model);
 		graphView.init();
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 	}
 
 }
