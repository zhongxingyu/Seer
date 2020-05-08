 package de.softwarekollektiv.dbs.app;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.List;
 
import org.apache.log4j.Logger;

 public abstract class SelectionMenu implements MenuItem {
	private static Logger log = Logger.getLogger(SelectionMenu.class);
	
 	
 	protected PrintStream out;
 	protected BufferedReader in;
 	
 	/**
 	 * Get all selectable menu items. Must be implemented by subclasses.
 	 * 
 	 * @return a list of all menu items
 	 */
 	protected abstract List<MenuItem> getItems();
 	
 	public SelectionMenu(PrintStream out, InputStream in) {
 		this.out = out;
 		this.in = new BufferedReader(new InputStreamReader(in));
 	}
 	
 	public boolean run() throws Exception {
 		List<MenuItem> items = getItems();
 		boolean loop = true;
 		while(loop) {
 		
 			printItems();
 			
 			int choice = 0;
 			while((choice < 1) || (choice > items.size())) {
 				out.println("Please choose a number or enter '?' to read the descriptions: ");
 				
 				String line = null;
 				try {
 					line = in.readLine();
 				} catch (IOException e) {
					log.error(e);
 					return false;
 				}
 				
 				if(line.equals("?")) {
 					printDescriptions();
 				} else {
 					try {
 						choice = Integer.parseInt(line);
 					} catch (NumberFormatException e) {
 						// Do nothing but let the user try again.
 					}
 				}
 			}
 		
 			// Do NOT catch the exception.
 			loop = items.get(choice - 1).run();
 		}
 		
 		return true;
 	}
 	
 	private void printItems() {
 		List<MenuItem> items = getItems();
 		for(int i = 0; i < items.size(); ++i) {
 			out.print(i + 1);
 			out.print(") ");
 			out.println(items.get(i).getTitle());
 		}
 		out.println();
 	}
 	
 	private void printDescriptions() {
 		List<MenuItem> items = getItems();
 		for(int i = 0; i < items.size(); ++i) {
 			out.print(i + 1);
 			out.print(") ");
 			out.println(items.get(i).getTitle());
 			out.println(items.get(i).getDescription());
 			out.println();
 		}
 	}
 
 }
