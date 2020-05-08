 package es.eucm.eadventure.editor.gui.otherpanels;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 import es.eucm.eadventure.common.data.chapter.Trajectory;
 import es.eucm.eadventure.editor.control.controllers.TrajectoryScenePreviewEditionController;
 import es.eucm.eadventure.editor.control.controllers.scene.NodeDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.TrajectoryDataControl;
 import es.eucm.eadventure.common.gui.TextConstants;
 
 /**
  * A Panel for the edition of the trajectory and barriers, including the necessary
  * buttons.
  * 
  * @author Eugenio Marchiori
  */
 public class TrajectoryEditionPanel extends JPanel {
 
 	/**
 	 * Default generated serialVersionUID
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * The preview and edition panel
 	 */
 	private ScenePreviewEditionPanel spep;
 	
 	/**
 	 * The mouse controller for the panel
 	 */
 	protected TrajectoryScenePreviewEditionController tspec;
 	
 	/**
 	 * Default constructor, with the path to the background and the trajectoryDataControl
 	 * 
 	 * @param scenePath path to the background image
 	 * @param trajectoryDataControl the trajectoryDataControl
 	 */
 	public TrajectoryEditionPanel(String scenePath, TrajectoryDataControl trajectoryDataControl) {
 		setLayout(new BorderLayout());
 		spep = new ScenePreviewEditionPanel(scenePath);
 		tspec = new TrajectoryScenePreviewEditionController(spep, trajectoryDataControl);
 		spep.changeController(tspec);
 		for (NodeDataControl nodeDataControl: trajectoryDataControl.getNodes())
 			spep.addNode(nodeDataControl);
 		spep.setMovableCategory(ScenePreviewEditionPanel.CATEGORY_NODE, true);
 		spep.setTrajectory((Trajectory) trajectoryDataControl.getContent());
 		
 		add(createButtonPanel(), BorderLayout.NORTH);
 		
 		add(spep, BorderLayout.CENTER);		
 	}
 	
 	/**
 	 * Create the button panel
 	 * 
 	 * @return the new button panel
 	 */
 	private JPanel createButtonPanel() {
 		JPanel buttonPanel = new JPanel();
 		ButtonGroup group = new ButtonGroup();
 	    buttonPanel.add(createToolButton("BarriersList.EditNodes", TrajectoryScenePreviewEditionController.NODE_EDIT, "img/icons/nodeEdit.png", group));
 		buttonPanel.add(createToolButton("BarriersList.EditSides", TrajectoryScenePreviewEditionController.SIDE_EDIT, "img/icons/sideEdit.png", group));
 		buttonPanel.add(createToolButton("BarriersList.SelectInitialNode", TrajectoryScenePreviewEditionController.SELECT_INITIAL, "img/icons/selectStartNode.png", group));
 		buttonPanel.add(createToolButton("BarriersList.DeleteTool", TrajectoryScenePreviewEditionController.DELETE_TOOL, "img/icons/deleteTool.png", group));
 		buttonPanel.add(createToolButton("BarriersList.EditBarriers", TrajectoryScenePreviewEditionController.EDIT_BARRIERS, "img/icons/barrierEdit.png", group));
 		return buttonPanel;
 	}
 	
 	/**
 	 * Create a tool button
 	 * 
 	 * @param text The text of the button
 	 * @param tool The tool asosiated with the button
 	 * @param group 
 	 * @return The new button
 	 */
 	private AbstractButton createToolButton(String text, final int tool, String iconPath, ButtonGroup group) {
 		
 		AbstractButton button;
 		ImageIcon icon = new ImageIcon(iconPath);
 		button = new JToggleButton(icon);
 		group.add(button);
 		button.setToolTipText(TextConstants.getText(text));
 
 		if (tool == TrajectoryScenePreviewEditionController.NODE_EDIT) {
 			button.setSelected(true);
 		}
 		
 		button.addActionListener(new ActionListener() {
			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				tspec.setSelectedTool(tool);
 			}
 		});
 		return button;
 		
 	}
 	
 	/**
 	 * Returns the preview panel
 	 * 
 	 * @return the preview panel
 	 */
 	public ScenePreviewEditionPanel getScenePreviewEditionPanel() {
 		return spep;
 	}
 }
