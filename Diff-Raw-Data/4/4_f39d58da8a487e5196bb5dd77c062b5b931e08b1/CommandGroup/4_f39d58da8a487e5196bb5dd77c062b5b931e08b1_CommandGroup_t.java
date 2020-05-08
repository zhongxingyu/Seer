 package interiores.core.presentation.terminal;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author hector
  */
 abstract public class CommandGroup
 {
     private IOStream iostream;
     
     public void setIOStream(IOStream iostream)
     {
         this.iostream = iostream;
     }
     
     public String readString(String question)
     {
         return iostream.readString(question);
     }
     
     public Collection<String> readStrings(String question) {
         return iostream.readStrings(question);
     }
     
     public String readChoice(String question, String ... choices) {
         List<String> list = Arrays.asList(choices);
         
         String available = "Available choices are: ";
         
         for(int i = 0; i < list.size(); ++i) {
             if(i != 0) available += ", ";
             available += list.get(i);
         }
         
         String choice = readString(question + " (" + available + ")");
         
        if(! list.contains(choice))
            choice = list.get(0);
         
         return choice;
     }
     
     public int readInt(String question)
     {       
         return iostream.readInt(question);
     }
     
     public float readFloat(String question) {
         return iostream.readFloat(question);
     }
     
     public void println(String line) {
         iostream.println(line);
     }
     
     public void print(Collection<?> collection) {
         for(Object o : collection)
             println(o.toString());
     }
 }
