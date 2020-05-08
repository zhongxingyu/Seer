 package service;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import filelist.ListItem;
 import filelist.ListItem.STATUS;
 
 public class NamesLibrary {
 	Map<String, LibraryCatalog> library = new HashMap<String, LibraryCatalog>();
 	
 	public NamesLibrary() {}
 	
 	char ProceedLetter(char l) {
         if (l >= (int)'a' && l <= (int)'z') return l;
         if (l == (int)'' || l == (int)'' || l == (int)'' || l == (int)'') return '_';
         if (l >= (int)'' && l <= (int)'') return l;
         return '_';
 	}
 	
 	LibraryCatalog GetCatalog(char letter) {
 		String name = "" + ProceedLetter(letter);
 		LibraryCatalog res = (LibraryCatalog)library.get(name);
 		if (res == null)
 			res = Load(name);
 		return res;
 	}
 	
 	public LibraryCatalog Load(String letter) {
 		LibraryCatalog res = null;
 		try {
 			res = new LibraryCatalog(new HashMap<String, Integer>());
 			String strLine;
 			BufferedReader reader = IOOperations.GetReader(service.Settings.librarypath + letter);
 	  		while ((strLine = reader.readLine()) != null) {
 	  			if (strLine.length() == 0) continue;
 	  			res.catalog.put(strLine.substring(1), Integer.parseInt(strLine.charAt(0) + ""));
 	  		}
 	  		reader.close();			
 		}
 		catch (FileNotFoundException e) {
 			res = new LibraryCatalog(new HashMap<String, Integer>());
 		}
 		catch (Exception e) {
 			res = null;
 			Errorist.printLog(e); 
 		}
 		
 		if (res != null) 
 			library.put(letter, res);
 		
 		return res;
 	}
 	
 	public void Save() {
 	    PrintWriter pw = null;
 	    int counter;
 	    
 	    File f = new File(service.Settings.libraryroot);
 	    if (!f.exists())
 	    	f.mkdirs();
 	    
 	    for (Map.Entry<String, LibraryCatalog> catalog : library.entrySet()) {
 	    	if (catalog.getValue().updated) {
 			    try {
 			    	counter = 0;
 			        pw = IOOperations.GetWriter(service.Settings.librarypath + catalog.getKey(), false);
 			        	        
 					for (Map.Entry<String, Integer> entry : catalog.getValue().catalog.entrySet()) {
 						pw.println(("" + entry.getValue()) + entry.getKey());
 						if(counter++ >= 100) {
 							counter=0;
 							pw.flush();
 						}
 			        }
 			        
 			        pw.flush();
 			    }
 			    catch (Exception e) { Errorist.printLog(e); }
 			    finally { if (pw != null) pw.close(); }
 	    	}
 	    }
 	}
 	
 	void Put(String title, Integer down) {
 		GetCatalog(title.charAt(0)).put(title, down);
 	}
 	
 	public void Set(String title, Boolean down) {
 		if (title.length() == 0) return;
 		Put(title, down ? 1 : 0);
 	}
 	
 	public Boolean Contains(String title) {
 		if (title.length() == 0) return false;
 		return GetCatalog(title.charAt(0)).containsKey(title);
 	}	
 	
 	public Boolean Get(String title) {
 		return GetCatalog(title.charAt(0)).get(title);
 	}
 	
 //	public int Count () { return library.size();}
 	
 	public boolean ProceedFile(File file) {
 		MediaInfo info = new MediaInfo(file);
 		
 		for(String title : info.Titles)
 			if (Contains(title))
 				return true;
 			else Set(title, false);
 		return false;
 	}
 	
 	public void ProceedItem(ListItem item) {
 		item.media_info = new MediaInfo(item.file);
 		System.out.println("*******Library proceed item*****" + item.title);
 //		if (item.state == STATUS.NONE)
 			for(String title : item.media_info.Titles)
 				if (Contains(title)) {
 					item.state = Get(title) ? STATUS.LIKED : STATUS.LISTENED;
 					break;
 				} 
 	//			else Set(title, false);		
 	}
 	
 //	public void SetListItemState(ListItem item) {
 //    	if (Common.library.Contains(item.title)) {
 //    		if (Common.library.Get(item.title))
 //    			item.state = ListItem.STATUS.LIKED;
 //    		else item.state = ListItem.STATUS.LISTENED;
 //    	}
 //	}
 	
 	class LibraryCatalog {
 		public boolean updated = false;
 		
 		Map<String, Integer> catalog;
 		
 		public LibraryCatalog(Map<String, Integer> cat) {
 			catalog = cat;
 		}
 		
 		public void put(String key, Integer val) {
 			catalog.put(key, val);
 			updated = true;
 		}
 		
 		public Boolean containsKey(String title) {
 			if (title.length() == 0) return false;
 			return catalog.containsKey(title);
 		}
 		
 		public Boolean get(String title) {
 			return catalog.get(title) == 1 ? true : false;
 		}		
 	}
 }
