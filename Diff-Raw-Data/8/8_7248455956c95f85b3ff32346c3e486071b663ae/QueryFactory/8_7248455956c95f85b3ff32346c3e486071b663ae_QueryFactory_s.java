 package niagara.client;
 
 public class QueryFactory {
 
     public static Query makeQuery(String text) throws ClientException {
 	   if (text.startsWith("<?xml")) 
 	       return new QPQuery(text);
	   else if (text.toUpperCase().indexOf("WHERE") != -1) 
	       return new XMLQLQuery(text);
	   else {
	       throw new ClientException("Invalid query: " + text);
		 }
     }
 }
