 import org.apache.lucene.index.*;
 import org.apache.lucene.search.*;
 import java.io.*;
 
 public class Leech
 {
     protected IndexReader reader;
     protected IndexSearcher searcher;
 
     private String field;
     private TermEnum tenum;
     private Normaliser normaliser;
 
 
     private String getEnvironment (String var)
     {
 	return (System.getenv (var) != null) ? 
 	    System.getenv (var) : System.getProperty (var.toLowerCase ());
     }
 
 
     public Leech (String indexPath,
                   String field) throws Exception
     {
         reader = IndexReader.open (indexPath);
         searcher = new IndexSearcher (reader);
         this.field = field;
         tenum = reader.terms (new Term (field, ""));
 
         if (getEnvironment ("NORMALISER") != null) {
             String normaliserClass = getEnvironment ("NORMALISER");
 
             normaliser = (Normaliser) (Class.forName (normaliserClass)
                         .getConstructor ()
                         .newInstance ());
         } else {
             normaliser = new Normaliser ();
         }
     }
 
 
     public String buildSortKey (String heading)
     {
         return normaliser.normalise (heading);
     }
 
 
     public void dropOff () throws IOException
     {
         searcher.close ();
         reader.close ();
     }
 
 
     private boolean termExists (Term t)
     {
         try {
             return (this.searcher.search (new TermQuery (t)).length () > 0);
         } catch (IOException e) {
             return false;
         }
     }
 
 
     public String[] next () throws Exception
     {
        if (tenum.term () != null &&
             tenum.term ().field ().equals (this.field)) {
             if (termExists (tenum.term ())) {
                 String term = tenum.term ().text ();
                tenum.next ();
                 return new String[] {buildSortKey (term), term};
             } else {
                tenum.next ();
                 return this.next ();
             }
         } else {
             return null;
         }
     }
 }
