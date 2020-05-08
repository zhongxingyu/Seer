 package service_threads;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.SwingUtilities;
 
 import service.Errorist;
 import service.IOOperations;
 
 import filelist.FileList;
 
 public class DropIniter extends BaseThread {
 	ArrayList<DropCell> drops_collection = new ArrayList<DropCell>();
 	
     public DropIniter() {
 		setDaemon(true);
		setPriority(Thread.MAX_PRIORITY);
 		start();
     }
 
     public synchronized void run() { routing(); }
     
     public void ProceedDrop(FileList curr_list, File [] curr_elems) {
     	drops_collection.add(new DropCell(curr_list, curr_elems));
     }
       
     void routing() {
     	while(!closeRequest()) {
             while(drops_collection.size() > 0) {
             	if (closeRequest()) return;
             	final DropCell temp = drops_collection.get(0);
             	
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run(){
 //                    	temp.list.SetStatus("Drop proceeding");
                     	temp.list.AddElemsF(IOOperations.ScanDirectoriesF(temp.elems));
 //                    	temp.list.SetStatus("");
                     }
                 });
                 
             	drops_collection.remove(0);
             }
             
 	        try { wait(sleep_time); }
 	        catch (InterruptedException e) { Errorist.printLog(e); }
     	}    	
     }
 	
 	class DropCell {
 		public FileList list;
 		public File [] elems;
 		
 		public DropCell(FileList curr_list, File [] curr_elems) {
 			list = curr_list;
 			elems = curr_elems;
 		}
 	}
 }
