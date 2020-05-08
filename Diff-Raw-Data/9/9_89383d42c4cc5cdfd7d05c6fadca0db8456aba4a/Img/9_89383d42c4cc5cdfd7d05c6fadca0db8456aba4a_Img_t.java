 package construct;
 
 public class Img implements Construct
 {
     private String filePath;
     
     public Img(String filePath){
         this.filePath = filePath;
     }
     
     @Override
     public String toHTML()
     {
         if(filePath != null){
            return "<img src=\""+filePath+"\"/>\n</img>";
         }else{
             return "";
         }
     }
 
 }
