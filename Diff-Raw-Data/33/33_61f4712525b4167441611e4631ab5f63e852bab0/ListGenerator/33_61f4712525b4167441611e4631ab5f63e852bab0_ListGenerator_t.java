 package exporter;
 
 import java.util.ArrayList;
 
 import adapters.db.sqlite.inventory.InventoryDAO;
 import adapters.db.sqlite.inventory.InventoryEntry;
 
 public class ListGenerator {
 	long start;
 	long end;
 	
 	public ListGenerator(long start, long end) {
 		this.start = start;
 		this.end = end;
 	}
 	
	public ArrayList<String> getList() throws Exception{
 		ArrayList<String> outList = new ArrayList<String>();
 		
 		InventoryDAO inventory = new InventoryDAO();
 		
 		ArrayList<InventoryEntry> list = inventory.lookUp(start, end);
 		
 		for(InventoryEntry e : list) {
 			outList.add(e.toString());
 		}
 		
 		return outList;
 	}
 }
