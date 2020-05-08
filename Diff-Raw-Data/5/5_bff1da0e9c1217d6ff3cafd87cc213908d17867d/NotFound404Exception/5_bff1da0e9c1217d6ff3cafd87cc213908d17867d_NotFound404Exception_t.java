 package Domain;
 
 import java.io.Serializable;
 
 /**.
  * Chris Card Steven Rupert
  * Date: 10/29/13
  */                     
 public class NotFound404Exception extends Exception implements Serializable {
     
    public NotFound404Exception(String Msg)
    {
        super(Msg);
    }
 }
