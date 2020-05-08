 package exporter;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.BufferedOutputStream;
 
 import adapters.db.sqlite.inventory.InventoryDAO;
 import adapters.db.sqlite.inventory.InventoryEntry;
 import adapters.db.sqlite.upcMap.UPCEntry;
 
 
 public class FileExporter implements Exporter {
 	private long start;
 	private long end;
 	
 	public FileExporter(long start, long end) {
 		this.start = start;
 		this.end = end;
 	}
 	
	public void export() throws IOException,  Exception{
 		OutputStream out = new BufferedOutputStream(new FileOutputStream("export/inventorylist.csv", false));
 		InventoryDAO dao = new InventoryDAO();
 		
 		for(InventoryEntry entry : dao.lookUp(start, end)){
 			UPCEntry upc = entry.getUPC();
 			String temp = upc.getUPC() + "," + upc.getItemName() + "," + upc.getAmount() + "," + entry.getCreated() + "\n";
 			out.write(temp.getBytes());
 		}
 		
 		out.close();
 	}
 
 }
