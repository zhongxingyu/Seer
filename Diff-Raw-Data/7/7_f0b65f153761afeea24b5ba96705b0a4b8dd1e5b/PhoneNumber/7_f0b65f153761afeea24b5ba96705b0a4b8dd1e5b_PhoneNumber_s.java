 package BackEnd.UserSystem;
 
 import java.util.ArrayList;
 
 /**
  * This class represents a phone number.
  * @author Anderson Santana
  */
 public class PhoneNumber{
   
   private final int AREA_CODE_LENGTH = 3;
   private final int EXCHANGE_NUMBER_LENGTH = 3;
   private final int LOCAL_NUMBER_LENGTH = 4;
   private final char[] VALID_SYMBOLS = {'-', '.', '(', ')'};
   private char[] areaCode = new char[AREA_CODE_LENGTH];
   private char[] exchangeNumber = new char[EXCHANGE_NUMBER_LENGTH];
   private char[] localNumber = new char[LOCAL_NUMBER_LENGTH];
   private ArrayList<Character> extensionNumber;
 
public class PhoneNumber{
   
 public PhoneNumber(String digits){
     
     /*for(int i = 0; i < areaCode.length; i++){
         if(digits.charAt(0) == VALID_SYMBOLS[2]){}
         else if(Character.isDigit(digits.charAt(i)))
           areaCode[digitAmount] = digits.charAt(digitAmount);
          
             
             throw new PhoneNumberNonNumericException(
                     "Digits entered are not numeric");
             
             else {
             throw new PhoneNumberNonNumericException(
                     "Digits entered are not numeric");
         }*/
             
             
         
     }
     
     //Strip symbols inside or outside loop?
     //for(int i = digitAmount; i < EXCHANGE_NUMBER_LENGTH; digitAmount++)
       //exchangeNumber = digits.charAt(digitAmount);
     
     //catch()
   //}
   
   
   public PhoneNumber(char[] areaCode, char[] exchangeNumber,
              char[] localNumber){
       this.areaCode = areaCode; 
       this.exchangeNumber = exchangeNumber;
       this.localNumber = localNumber;
   }
   
   private String stripSymbols(String digits){
       return digits;
   }
   
   public void setDigits(char[] areaCode, char[] exchangeNumber,
              char[] localNumber){
       this.areaCode = areaCode; 
       this.exchangeNumber = exchangeNumber;
       this.localNumber = localNumber;
   }
   
   private boolean verifyNumeric(char[] digits){
       return true;
   }
   
   public void setAreaCode(char[] areaCode){
       this.areaCode = areaCode; 
   }
   
   private boolean verifyAreaCodeLength(char[] areaCode){
       return true;
   }
   
   public char[] getAreaCode(){
       return areaCode;
   }
   
   public void setExchangeNumber(char[] exchangeNumber){
   }
   
   
 }
