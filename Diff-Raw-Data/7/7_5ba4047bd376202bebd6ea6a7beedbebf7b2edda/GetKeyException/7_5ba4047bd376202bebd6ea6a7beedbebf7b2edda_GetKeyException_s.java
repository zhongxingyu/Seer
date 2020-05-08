 package FileUnique;
 
 /**
  * User: Mikita_Abramenka
  * Date: 3/21/13
  * Time: 2:34 PM
  */
 class GetKeyException extends Throwable {
     public GetKeyException(Exception e) {
         super(e);
     }
 }
