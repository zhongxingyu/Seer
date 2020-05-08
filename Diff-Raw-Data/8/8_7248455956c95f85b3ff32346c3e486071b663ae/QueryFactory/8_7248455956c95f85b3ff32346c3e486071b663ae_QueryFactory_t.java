 package niagara.client;
 
 public class QueryFactory {
 
     public static Query makeQuery(String text) throws ClientException {
 	   if (text.startsWith("<?xml")) 
 	       return new QPQuery(text);
		 else
				 throw new ClientException("Invalid Query: " + text);
	    //   return new XMLQLQuery(text);
     }
 }
