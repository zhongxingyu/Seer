 package logic;
 
 /**
  * @author john
  * version 1
  */
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.math.BigDecimal;
 
 import state.MainState;
 import utill.GeneralUtill;
 import gui.GuiUtill;
 import gui.View;
 
 public class Controller {
 	
 	Model mod;
 	View view;
 	MainState state;
 	
 	public Controller(Model mod, View view, MainState state)
 	{
 		this.mod 	= mod;
 		this.view 	= view;
 		this.state	= state;
 		
 		//Load all the details from the state
 		loadFromState();
 		
 		
 		//Connect action listeners
 		view.applyActionListener(new applyAction() );
 	}
 	
 	/**
 	 * Loads the table headings
 	 */
 	private void loadFromState()
 	{
 		view.setSaveTableData(DataForTable.getSaveTableHeadings(), state.getTableContents());
 	}
 	
 	/**
 	 * saves changes to the state of the program
 	 */
 	private void saveToState()
 	{
 		
 	}
 	
 	
 	
 	
 	
 	////Action listeners//////
 	/////////////////////////
 	
 	/**
 	 * Action event for the apply button. Get all the data entered
 	 * @author john
 	 *
 	 */
 	class applyAction implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) 
 		{
 			BigDecimal beginAmount;
 			BigDecimal goal;
 			
 			//Check the inputs are big decimal
 			if(! GeneralUtill.stringToBigDecimalCheck(view.getStartingAmount()))
 			{
 				GuiUtill.showError("Incorrect input", "please put in the correct in put in your starting amount");
 			}
			else if(! GeneralUtill.stringToBigDecimalCheck(view.getGoalAmount() ))
 			{
 				GuiUtill.showError("Incorrect input", "please put in the correct in put in your goal amount");
 			}
			else
			{
				beginAmount = GeneralUtill.convertStringToBigDecimal(view.getStartingAmount()); 
				goal 		= GeneralUtill.convertStringToBigDecimal(view.getGoalAmount());
			}
 			
 			//mod.averageAmountWeekly(GeneralUtill.stringToBigDecimalCheck(check)   getStartingAmount(), view.getGoalAmount());			
 		}
 		
 	}
 	
 
 }
