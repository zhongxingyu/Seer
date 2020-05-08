 package overwatch.controllers;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.ResultSet;
 import javax.swing.JFrame;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import overwatch.db.Database;
 import overwatch.db.EnhancedResultSet;
 import overwatch.gui.tabs.RankTab;
 
 
 
 
 
 /**
  * RankTab logic
  * @author john
  * Version 3
  */
 
 
 
 
 
 public class RankLogic 
 {
 		
 	
 	public RankLogic(RankTab rt)
 	{
		attatchButtonEvents(rt);		
 	}
 	
 	
 	
	public void attatchButtonEvents(RankTab rt)
 	{
 		addNewRank(rt);
 		deleteRank(rt);
 		saveRank(rt);
 		populateTabList(rt);
 		rankListChange(rt);
 	}
 	
 	
 	
 	
 	private static void populateTabList(RankTab rt)
 	{
 		rt.setSearchableItems(
 			Database.queryKeyNamePairs( "Ranks", "rankNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	public void rankListChange(final RankTab rt)
 	{
 		//TODO populate the fields here
 		rt.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				System.out.println(rt.getSelectedItem());	
 			}
 		});
 		
 	}
 	
 	
 	
 	public void addNewRank(RankTab rt)
 	{
 		rt.addNewListener(new ActionListener() {			
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked addnew");				
 			}
 		});	
 	}
 	
 	
 	
 	public void deleteRank(RankTab rt) 
 	{
 		rt.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked delete");
 			}
 		});		
 	}
 	
 	
 	
 	public void saveRank(RankTab rt)
 	{
 		rt.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked save");		
 			}
 		});
 	}
 }
