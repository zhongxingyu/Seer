 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.vocabulary.*;
 
 public class Ctalkerithm {
 	
 	
 	public static void main(String[] args) throws Exception {
 		
		String[] celebs = Utils.readFile("../ontology/celebrities.txt").split("\n");
 				
 		String[] celebXMLList = new String[celebs.length];
 		
 		int i=0;
 		for(String celeb: celebs) {
			celebXMLList[i++] = Utils.openURL("http://search.twitter.com/search.atom?q=@" + celeb);
 			System.out.println("\n\n\n\n\n\n" + celebXMLList[i-1]);
 		}
 		
 //		System.out.println(celebs);
 		
 		
 		
 		
 		
 	}
 	
 
 	
 	
 }
