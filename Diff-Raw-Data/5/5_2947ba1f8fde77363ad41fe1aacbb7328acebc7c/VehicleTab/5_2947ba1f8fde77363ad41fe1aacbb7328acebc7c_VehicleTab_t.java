 package overwatch.gui;
 
 import javax.swing.*;
 
 
 /**
  * Creates the vehicle tab
  * @author john
  *
  */
 
 public class VehicleTab extends GenericPanelButtoned<Integer>
 {
 	private LabelFieldPair            number;
 	private LabelFieldPair            type;
 	private LabelFieldEllipsisTriplet pilot;
 	
 	
 	public VehicleTab()
 	{
 		super( "Vehicles", "Details" );		
 		setupComponents();
 	}
 	
 				
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private void setupComponents()
 	{
 		number  = addLabelledField( "Number:" );
 		type	= addLabelledField("Type:");
		pilot   = addLabelledFieldWithEllipsis( "Pilot:" );		
 	}
 	
 	
 	//Test
 	public static void main(String[] args)
 	{
 		JFrame frame = new JFrame();
 		frame.add(new VehicleTab());
 		frame.pack();
 		frame.setVisible(true);		
 	}
 	
 	
 	
 	
 
 }
