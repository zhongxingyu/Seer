 package org.bh.gui.swing;
 
 import java.awt.Component;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.IPeriodController;
 import org.bh.controller.InputController;
 import org.bh.data.DTO;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.gui.ValidationMethods;
 import org.bh.gui.View;
 import org.bh.gui.ViewException;
 import org.bh.platform.PlatformController;
 import org.bh.platform.ProjectController;
 import org.bh.platform.ScenarioController;
 import org.bh.platform.Services;
 
 //TODO Schmalzhaf.Alexander Javadoc schreiben
 /**
  * 
  * <short_description>
  * 
  * <p>
  * <detailed_description>
  * 
  * @author 001
  * @version 1.0, 04.01.2010
  * 
  */
 public class BHTreeSelectionListener implements TreeSelectionListener {
 	
 	private static final Logger log = Logger.getLogger(BHTreeSelectionListener.class);
 	
 	private BHMainFrame bhmf;
 	private PlatformController pc;
 	private InputController controller = null;
 
 	public BHTreeSelectionListener(PlatformController pc, BHMainFrame bhmf) {
 		this.pc = pc;
 		this.bhmf = bhmf;
 	}
 
 	@Override
 	public void valueChanged(final TreeSelectionEvent tse) {
 		if (tse.getNewLeadSelectionPath() == null) {
 			bhmf.setContentForm(new BHContent());
			bhmf.removeResultForm();
 		} else {
 			// save divider location of split pane
 			BHTreeNode oldActiveNode;
 			if (tse.getOldLeadSelectionPath() != null) {
 				TreePath tp = tse.getOldLeadSelectionPath();
 				oldActiveNode = (BHTreeNode) tp.getLastPathComponent();
 				oldActiveNode.setDividerLocation(bhmf.getVDividerLocation());
 			}
			//remove resultpanel
			bhmf.removeResultForm();
 
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					if (tse.getPath().getLastPathComponent() instanceof BHTreeNode) {
 						BHTreeNode selectedNode = (BHTreeNode) tse.getPath()
 								.getLastPathComponent();
 						DTO<?> selectedDto = (DTO<?>) selectedNode
 								.getUserObject();
 						if (selectedDto instanceof DTOProject) {
 							try {
 								View view = new BHProjectView(
 										new BHProjectForm());
 
 								IDTO<?> model = selectedDto;
 								controller = new ProjectController(view, model);
 								
 								selectedNode.setController(controller);
 
 								// set content and result (=dashboard) if
 								// available
 								bhmf.setContentForm(view.getViewPanel());
 								JScrollPane resultPane;
 								if ((resultPane = selectedNode.getResultPane()) != null) {
 									bhmf.setResultForm(resultPane);
 									bhmf.setVDividerLocation(selectedNode
 											.getDividerLocation());
 								} else {
 									bhmf.removeResultForm();
 								}
 
 								// TODO START VIEW
 
 								controller.loadAllToView();
 
 							} catch (ViewException e) {
 								e.printStackTrace();
 							}
 
 						} else if (selectedDto instanceof DTOScenario) {
 							try {
 
 								DTOScenario model = (DTOScenario) selectedDto;
 
 								// check if controller is already there...
 								if ((controller = selectedNode.getController()) == null) {
 
 									// if not, create view at first
 
 									View view;
 									// find out if stochastic process was
 									// chosen
 									// at init of strategy
 									if (!model.isDeterministic()) {
 										view = new BHScenarioView(
 												new BHScenarioForm(
 														BHScenarioForm.Type.STOCHASTIC,
 														model
 																.isIntervalArithmetic()),
 												new ValidationMethods());
 									} else {
 										view = new BHScenarioView(
 												new BHScenarioForm(
 														BHScenarioForm.Type.DETERMINISTIC,
 														model
 																.isIntervalArithmetic()),
 												new ValidationMethods());
 									}
 
 									// create controller
 									controller = new ScenarioController(view,
 											model, bhmf);
 									selectedNode.setController(controller);
 								}
 
 								if (model.isDeterministic()) {
 									// if scenario is deterministic, an
 									// overview
 									// table is provided
 									((BHDeterministicProcessForm) ((BHScenarioForm) controller
 											.getViewPanel()).getProcessForm())
 											.setPeriodTable(pc
 													.prepareScenarioTableData(model));
 								}
 
 								// set content and result panels to bhmf
 								JScrollPane resultPane;
 
 								bhmf.setContentForm(controller.getViewPanel());
 
 								if ((resultPane = selectedNode.getResultPane()) != null) {
 									bhmf.setResultForm(resultPane);
 									bhmf.setVDividerLocation(selectedNode
 											.getDividerLocation());
 								} else {
 									bhmf.removeResultForm();
 								}
 
 							} catch (ViewException e) {
 								e.printStackTrace();
 							}
 
 						} else if (selectedDto instanceof DTOPeriod) {
 							bhmf.removeResultForm();
 							
 							DTOPeriod period = (DTOPeriod)selectedDto;
 							
 							IPeriodController periodController = Services
 									.getPeriodController(period.getScenario()
 											.get(DTOScenario.Key.PERIOD_TYPE)
 											.toString());
 							Component viewComponent = periodController
 									.editDTO(period);
 							BHPeriodForm container = new BHPeriodForm();
 							try {
 								View periodView = new View(container
 										.getPperiod(), new ValidationMethods());
 								InputController controller = new InputController(
 										periodView, period);
 								controller.loadAllToView();
 								//set Controller to tree
 								selectedNode.setController(controller);
 							} catch (ViewException e) {
 								// should not happen
 								log.error("Cannot create period view", e);
 							}
 							
 							
 							
 							container.setPvalues((JPanel) viewComponent);
 							bhmf.setContentForm(container);
 						}
 					}
 
 				}
 			});
 		}
 	}
 }
