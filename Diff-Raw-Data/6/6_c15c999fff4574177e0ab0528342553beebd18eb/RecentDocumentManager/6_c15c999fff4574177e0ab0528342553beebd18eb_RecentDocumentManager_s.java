 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.util.files;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Stack;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import wombat.util.OS;
 import wombat.util.errors.ErrorManager;
 
 /**
  * Remember recently opened documents.
  */
 public class RecentDocumentManager {
 	private RecentDocumentManager() {}
 
 	// How many document we're going to deal with.
 	static int FileCount = 10;
 	
 	// The actual files that were opened.
 	static Stack<File> fileList = new Stack<File>();
 	
 	// The menu containing a list of recently opened files.
 	static JMenu menu;
 	
 	/**
 	 * Add a new file.
 	 * @param f The file to add/remove.
 	 */
 	public static void addFile(File f) {
 		// If it already existed, use remove to bump it to the front of the list.
 		fileList.remove(f);
 		fileList.push(f);
 
 		// Old documents fall off.
 		while (fileList.size() > FileCount)
 			fileList.remove(0);
 		
 		// Rebuild the menu.
 		rebuildMenu();
 	}
 	
 	/**
 	 * Rebuild the menu.
 	 */
 	static void rebuildMenu() {
 		if (menu != null) {
 			// Clear the old items out.
 			menu.removeAll();
 			
 			// Go through the item list, remove any that no longer exist, add the rest to the menu.
 			JMenuItem item;
 			int j = 1;
 			for (int i = fileList.size() - 1; i >= 0; i--) {
 				final File each = fileList.get(i);
 				
 				if (!each.exists()) {
 					fileList.remove(i);
 					continue;
 				}
 				
 				// Add to the menu with a shortcut for each.
 				item = new JMenuItem(j + " - " + each.getPath());
 				item.setMnemonic(("" + j).charAt(0));
 				item.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						DocumentManager.Open(each);
 					}
 				});
 				menu.add(item);
 				j += 1;
 			}
 			
 			// Add a final option to clear this list.
 			menu.addSeparator();
 			item = new JMenuItem("Clear recent documents");
 			item.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					fileList.clear();
 					rebuildMenu();
 				}
 			});
 			menu.add(item);
 			
 			// Figure out if it should be enabled or disabled.
 			menu.setVisible(!fileList.isEmpty());
 		}
 	}
 
 	/**
 	 * Build a list that keeps track of recent documents.
 	 * @return
 	 */
 	public static JMenuItem buildRecentDocumentsMenu() {
 		// Build the menu if it hasn't already been.
 		if (menu == null) {
 			menu = new JMenu("Open recent");
 			if (!OS.IsOSX) menu.setMnemonic('r');
 			rebuildMenu();
 		}
 		
 		return menu;
 	}
 
 	/**
 	 * Set the files to a string.
 	 */
 	public static void setFiles(String files) {
 		if (files != null) {
 			for (String doc : files.split(";")) {
 				if (!doc.trim().isEmpty()) {
 					try {
 						fileList.add(new File(doc).getCanonicalFile());
 					} catch(IOException ex) {
 						ErrorManager.logError("Unable to find canonical path for '" + doc.toString() + "' when loading recent documents");
 					}
 				}
 			}
 		}
 		
 		rebuildMenu();
 	}
 	
 	/**
 	 * Get the files as a string.
 	 * @return Duh
 	 */
 	public static String getFiles() {
 		StringBuilder sb = new StringBuilder();
 		for (File f : fileList) {
 			try {
 				sb.append(f.getCanonicalPath());
 				sb.append(";");
 			} catch(IOException ex) {
 				ErrorManager.logError("Unable to find canonical path for '" + f.toString() + "' when saving recent documents");
 			}
 		}
 		if (sb.length() > 0)
 			sb.delete(sb.length() - 1, sb.length());
 		return sb.toString();
 	}
 }
 
