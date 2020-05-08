 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements;
 
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.controller.UpdateRequirementController;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.Requirement;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.Iteration;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementPriority;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.RequirementStatus;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.ViewEventController;
 /**
  * 
  * @author Pi 
  * @author Chris
  * @author Brian
  *
  */
 public class EditRequirementPanel extends RequirementPanel 
 {	
 	private Requirement requirementBeingEdited;
 	private JTextField iteration;
 	
 	/**
 	 * Constructor for a new requirement panel
 	 * @param reqModel Local requirement model for containing data
 	 */
 	public EditRequirementPanel(Requirement req) {
 		setLayout(new GridLayout(1, 2));
 
 		requirementBeingEdited = req;
 		this.add(buildLeftPanel()); //add left panel
 		this.add(buildRightPanel()); //add right panel
 		
 		fillFieldsForRequirement();
 	}
 	
 	/**
 	 * Fills the fields of the edit requirement panel based on the current settings of the
 	 * edited requirement.
 	 */
 	private void fillFieldsForRequirement()
 	{
 		boxName.setText(requirementBeingEdited.getName());
 		boxDescription.setText(requirementBeingEdited.getDescription());
 		boxEstimate.setText(String.valueOf(requirementBeingEdited.getEstimate()));
 		boxReleaseNum.setText(requirementBeingEdited.getRelease());
 		dropdownStatus.setSelectedItem(requirementBeingEdited.getStatus());
 		boxIteration.setText(requirementBeingEdited.getIteration().toString());
 		
 		switch(requirementBeingEdited.getPriority())
 		{
 		case BLANK:
 			group.clearSelection();
 			break;
 		case LOW:
 			priorityLow.setSelected(true);
 			break;
 		case MEDIUM:
 			priorityMedium.setSelected(true);
 		case HIGH:
 			priorityHigh.setSelected(true);
 		}
 		
 		if(requirementBeingEdited.getStatus() == RequirementStatus.INPROGRESS || requirementBeingEdited.getStatus() == RequirementStatus.COMPLETE)
 		{
 			boxEstimate.setEnabled(false);
 		}
 		else
 		{
 			boxEstimate.setEnabled(true);
 		}
 	}
 	
 	/**
 	 * Builds the right panel
 	 */
 	@Override
 	protected JPanel buildRightPanel()
 	{
 		super.buildRightPanel();
 
 		//setup the buttons
 		JPanel buttonPanel = new JPanel();
 		JButton buttonUpdate = new JButton("Update");
 		JButton buttonCancel = new JButton("Cancel");
 		JButton buttonClear = new JButton("Undo Changes");
 		
 		// Construct the add requirement controller and add it to the update button
 		buttonUpdate.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{
 				boolean isNameValid;
 				boolean isDescriptionValid;
 				boolean isEstimateValid;
 				
 				if (boxName.getText().length() >= 100)
 				{
 					isNameValid = false;
 					errorName.setText("No more than 100 chars");
 					errorName.setForeground(Color.RED);
 				}
 				else if(boxName.getText().trim().length() <= 0)
 				{
 					isNameValid = false;
 					errorName.setText("Name is REQUIRED");
 					errorName.setForeground(Color.RED);
 				}
 				else
 				{
 					errorName.setText("");
 					isNameValid = true;
 					
 				}
 				if (boxDescription.getText().trim().length() <= 0)
 				{
 					isDescriptionValid = false;
 					errorDescription.setText("Description is REQUIRED");
 					errorDescription.setForeground(Color.RED);
 				}
 				else
 				{	
 					errorDescription.setText("");
 					isDescriptionValid = true;
 				}
 				
 				if (boxEstimate.getText().trim().length() <= 0)
 				{
 					boxEstimate.setText("");
 					errorEstimate.setText("");
 					isEstimateValid = true;
 				}
 				else if(!(isInteger(boxEstimate.getText())))
 				{
 					errorEstimate.setText("** Please enter a non-negative integer");
 					isEstimateValid = false;
 					errorEstimate.setForeground(Color.RED);
 				}
 				else if(Integer.parseInt(boxEstimate.getText())<0)
 				{
 					errorEstimate.setText("** Please enter a non-negative integer");
 					isEstimateValid = false;
 					errorEstimate.setForeground(Color.RED);
 				}
 				else
 				{
 					errorEstimate.setText("");
 					isEstimateValid = true;
 				}
 			
 				if(isNameValid && isDescriptionValid && isEstimateValid )
 				{
 					update();
 				}
 			}
 		});
 		
 		buttonClear.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fillFieldsForRequirement();
 			}
 		
 		});
 		
 		buttonCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancel();
 			}
 		});
 
 		buttonPanel.add(buttonUpdate);
 		buttonPanel.add(buttonClear);
 		buttonPanel.add(buttonCancel);
 		SpringLayout rightLayout = (SpringLayout)rightPanel.getLayout();
 		
 		rightLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 15,
 				SpringLayout.SOUTH, errorEstimate);
 		rightLayout.putConstraint(SpringLayout.WEST, buttonPanel, 15,
 				SpringLayout.WEST, rightPanel);
 		
 		rightPanel.add(buttonPanel);
 		
 		return rightPanel;
 	}
 	
 	/**
 	 * Updates the requirement.
 	 */
 	private void update()
 	{
 		// Extract the name, release number, and description from the GUI fields
 		String stringName = this.boxName.getText();
 		String stringReleaseNum = this.boxReleaseNum.getText();
 		String stringDescription = this.boxDescription.getText();
 		String stringEstimate = this.boxEstimate.getText();
 		String stringIteration = this.boxIteration.getText();
 
 
 		RequirementPriority priority;
 		RequirementStatus status;
 		
 		int estimate = stringEstimate.trim().length() == 0 ? null : Integer.parseInt(stringEstimate);
 		// Extract the status from the GUI
 		status = (RequirementStatus)this.dropdownStatus.getSelectedItem();
 		Iteration iteration = new Iteration(stringIteration);
 		// Extract which radio is selected for the priority
 		boolean stateHigh = priorityHigh.isSelected();
 		boolean stateMedium = priorityMedium.isSelected();
 		boolean stateLow = priorityLow.isSelected();
 
 		// Convert the priority string to its corresponding enum
 		if (stateHigh)
 			priority = RequirementPriority.HIGH;
 		else if (stateMedium)
 			priority = RequirementPriority.MEDIUM;
 		else if (stateLow)
 			priority = RequirementPriority.LOW;
 		else
 			priority = RequirementPriority.BLANK;
 
 		// Create a new requirement object based on the extracted info
 		requirementBeingEdited.setName(stringName);
 		requirementBeingEdited.setRelease(stringReleaseNum);
 		requirementBeingEdited.setDescription(stringDescription);
 		requirementBeingEdited.setStatus(status);
 		requirementBeingEdited.setPriority(priority);
 		requirementBeingEdited.setEstimate(estimate);
 		requirementBeingEdited.setIteration(iteration);
 		UpdateRequirementController.getInstance().updateRequirement(requirementBeingEdited);
 		ViewEventController.getInstance().refreshTable();
 		ViewEventController.getInstance().removeTab(this);
 	}
 	
 	/**
 	 * Cancels the editing of the requirement.
 	 */
 	private void cancel()
 	{
 		ViewEventController.getInstance().removeTab(this);
 	}
  
 	
 	public boolean isInteger( String input ) {
 	    try {
 	        Integer.parseInt( input );
 	        return true;
 	    }
 	    catch( Exception e ) {
 	        return false;
 	    }
 	}
 }
