 public class NegNumberException extends Exception
 {
   public NegNumberException()
   {
    super("You have entered a negative number. Thank you for using the program.");
   }
   
   public NegNumberException(String message)
   {
     super(message)
   }
 }
   
