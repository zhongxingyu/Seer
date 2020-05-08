 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import dataparser.Dataparser;
 
 import Model.CoAuthor;
 
 public class CollaborationController {
 
 	public HashMap<String, ArrayList<CoAuthor>> getCollaborationList(){
 		Dataparser dp = Dataparser.getInstance();
 		List<String> authors = dp.getListOfAuthors();
 		HashMap<String, ArrayList<CoAuthor>> retval = new HashMap<String, ArrayList<CoAuthor>>();
 		ArrayList<CoAuthor> coauthors;
 		for (String aut : authors){
 			String key = aut + " ("+dp.getNumberOfCoAuthorsForAuthor(aut)+")";
 			coauthors = new ArrayList<CoAuthor>();
 			for(String coaut : dp.getCoAuthorsForAuthor(aut)){
 				coauthors.add(new CoAuthor(coaut, dp.getNumberOfCoAuthorsForAuthor(coaut)));
 			}
 			retval.put(key, coauthors);
 		}

 		return retval;
 	}
 	
 }
