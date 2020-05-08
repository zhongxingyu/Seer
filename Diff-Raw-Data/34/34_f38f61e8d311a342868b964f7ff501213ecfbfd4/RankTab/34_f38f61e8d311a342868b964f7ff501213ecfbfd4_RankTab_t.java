 package overwatch.gui;
 
 import javax.swing.JFrame;
 




/**
 * Rank tab
 * @author john
 *
 */





 public class RankTab extends GenericPanelButtoned<Integer>{
 	
 	LabelFieldPair name;
 	LabelFieldPair privileges;

 	
 	
 	
 	public RankTab()
 	{
 		super( "Rank", "Details" );		
 		setupComponents();
 	}

	

	
	
	
	
	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////

	
 	
 	private void setupComponents()
 	{
		name    	= addLabelledField( "Name:");
		privileges  = addLabelledField("Privileges:");
 	}
 		
 		
 		//Test
 		public static void main(String[] args)
 		{
 			JFrame frame = new JFrame();
 			frame.add(new RankTab());
 			frame.pack();
 			frame.setVisible(true);		
 		}
 
 }
