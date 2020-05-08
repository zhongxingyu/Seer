 package chalmers.dax021308.ecosystem.controller;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.view.NewSimulationView;
 
 public class NewSimulationController implements IController {
 	private EcoWorld model;
 	private NewSimulationView view;
 	private ActionListener onStartButtonListener = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			startSimulation();
 		}
 	};
 	
 	public NewSimulationController() {
 		
 	}
 
 	@Override
 	public void init() {
 		if(model == null) throw new NullPointerException("Model is NULL.");
 		if(view == null) {
 			view = new NewSimulationView(model);
 			view.btnRunSim.addActionListener(onStartButtonListener);
 		}
 		view.show();
 	}
 
 	@Override
 	public void release() {
 		
 	}
 
 	@Override
 	public void setModel(IModel m) {
 		if(m instanceof EcoWorld) {
 			this.model = (EcoWorld) m;
 		}
 	}
 	
 	private void startSimulation() {
 		try {
			try {
				model.stop();
			} catch (IllegalStateException e) {
				
			}
 			model.setNumIterations(Integer.MAX_VALUE);
 			if(view.chckbxCustomSize.isSelected()) {
 				int width = Integer.parseInt(view.tfCustomWidth.getText());
 				int height = Integer.parseInt(view.tfCustomHeight.getText());
 				model.setSimulationDimension(new Dimension(width, height));
 			} else {
 				model.setSimulationDimension((String) view.listSimulationDim.getSelectedValue());
 			}
 			int tickDelay = Integer
 					.parseInt(view.textfield_Iterationdelay.getText());
 
 			if (view.rdbtn2Threads.isSelected()) {
 				model.setNumThreads(2);
 			} else if (view.rdbtn4Threads.isSelected()) {
 				model.setNumThreads(4);
 			} else {
 				model.setNumThreads(8);
 			}
 
 			if (tickDelay < 1) {
 				model.setRunWithoutTimer(true);
 			} else {
 				model.setRunWithoutTimer(false);
 				model.setDelayLength(tickDelay);
 			}
 			if (view.checkBoxLimitIterations.isSelected()) {
 				int iterations = Integer.parseInt(view.tvNumIterations.getText());
 				model.setNumIterations(iterations);
 			} else {
 				model.setNumIterations(Integer.MAX_VALUE);
 			}
 			model.setRecordSimulation(view.chckbxRecordSimulation.isSelected());
 			// Should the shape really be set here?
 			// TODO fix an input value for shape and not just a squareshape
 			String shape = null;
 			if(view.rdbtnCircle.isSelected()) {
 				shape = EcoWorld.SHAPE_CIRCLE;
 			} else if (view.rdbtnSquare.isSelected()){
 				shape = EcoWorld.SHAPE_SQUARE;
 			} else {
 				shape = EcoWorld.SHAPE_TRIANGLE;
 			}
 			model.createInitialPopulations(
 					(String) view.predList.getSelectedValue(),
 					Integer.parseInt(view.tvPredPopSize.getText()),
 					(String) view.preyList.getSelectedValue(),
 					Integer.parseInt(view.tvPreyPopSize.getText()),
 					(String) view.grassList.getSelectedValue(),
 					Integer.parseInt(view.tvGrassPopSize.getText()), shape, 
 					(String) view.obstacleList.getSelectedValue());
 			try {
 				model.start();
 			} catch (IllegalStateException e) {
 				Log.v(e.toString());
 			}
 			view.hide();
 		} catch (Exception e) {
 			view.showErrorMessage();
 		}
 	}
 
 
 }
