 package uk.ac.ic.doc.protein_factory;
 
 import java.security.PublicKey;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Michael
  * Date: 09/03/12
  * Time: 13:19
  * To change this template use File | Settings | File Templates.
  */
 public class StartNucleotide extends Nucleotide {
 
     boolean start;
 
     public StartNucleotide(Game g,boolean start)
     {
         super(g,'q');
         this.start=start;
        setColour(getTerminal());
     }
 
     public void wobbleLeft()
     {
         x--;
     }
 
     protected String partial_bitmap_filename()
     {
             return "backbone";
     }
     
     private String getTerminal()
     {
         if (start)
             return "start";
         else
             return "end";
     }
 }
