 package construct;
 
 public class Ahref implements Construct
 {
     private String URL;
     public Ahref(String URL){
         this.URL = URL;
     }
 
     @Override
     public String toHTML()
     {
         
        return "<a href=\""+URL+"\"></a>\n";
     }
     
 }
