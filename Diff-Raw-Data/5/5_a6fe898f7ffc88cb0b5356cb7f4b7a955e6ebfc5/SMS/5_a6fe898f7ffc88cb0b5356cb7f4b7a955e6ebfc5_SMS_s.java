 package fi.jasoft.qrcode.data;
 
 /**
  * Embed a SMS message in a QR code
  * 
  * @author John Ahlroos
  * 
  */
@SuppressWarnings("serial")
 public class SMS implements QRCodeType {
 
     private String number;
 
     private String message;
 
     /**
      * Constructor
      * 
      * @param number
      *            The number to send an SMS to
      * 
      * @param message
      *            The SMS message
      */
     public SMS(String number, String message) {
        // TODO Auto-generated constructor stub
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see fi.jasoft.qrcode.types.QRCodeType#toQRString()
      */
     public String toQRString() {
         return "sms:" + number + ":" + message;
     }
 
     /**
      * Get the phone number
      * 
      * @return
      */
     public String getNumber() {
         return number;
     }
 
     /**
      * Set the phone number
      * 
      * @param number
      */
     public void setNumber(String number) {
         this.number = number;
     }
 
     /**
      * Get the message
      * 
      * @return
      */
     public String getMessage() {
         return message;
     }
 
     /**
      * Set the message
      * 
      * @param message
      */
     public void setMessage(String message) {
         this.message = message;
     }
 
 }
