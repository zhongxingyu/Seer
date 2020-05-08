 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  * 
  */
 
 /**
  * @author Rochelle
  *
  */
 public class RegExpFunc {
     private String SPACE = " ", BSLASH = "/", MULTI = "*",
     PLUS = "+", OR = "|", LBRAC = "[", RBRAC = "]", LPAREN = "(",
     RPAREN = ")", PERIOD = ".", APOS = "'", QUOT = "\"", UNION = "UNION",
     RECHAR = "RECHAR", CLSCHAR = "CLSCHAR", IN = "IN", NOT = "^", DASH = "-";
     private ArrayList<Terminals> classes = Parser.getClasses();
    
    private String input;
    private InputStream is;
 
    /* Constructor */
    public RegExpFunc(String input) {
    	this.input = new String(input);
    	this.is = new InputStream(this.input);
    }
    
    private boolean matchToken(String token) {
        return is.matchToken(token.charAt(0));
     }
 
     private String peekToken() {
        return String.valueOf(is.peekToken());
     }
     
     private void union() {
         //TODO Auto-generated method stub
     }
     
     private void reChar() {
         // TODO Auto-generated method stub
     }
     
     private void clsChar() {
         // TODO Auto-generated method stub
     }
     
     private boolean definedClass() {
         for(int i=0; i<classes.size(); i++){
             HashSet<Character> chars = classes.get(i).getChars();
             if(chars.contains(peekToken())){
                 matchToken(peekToken());
                 return true;
             }
         }
         return false;
     }
     
     public void origRegExp() {
         regExp();
     }
     
     public void regExp() {
         regExOne();
         regExPrime();
     }
     
     public void regExPrime() {
         if(peekToken() == OR) {
             matchToken(OR);
             union();
             regExOne();
             regExPrime();
         }
         else {
             return;
         }
     }
 
     public void regExOne() {
         regExTwo();
         regExOnePrime();
     }
     
     public void regExOnePrime() {
         if(peekToken() == LPAREN) {
             regExTwo();
             regExOnePrime();
         }
         else if(peekToken() == RECHAR) {
             regExTwo();
             regExOnePrime();
         }
         else if(peekToken() == PERIOD) {
             regExTwo();
             regExOnePrime();
         }
         else if(peekToken() == LBRAC) {
             regExTwo();
             regExOnePrime();
         }
         else if(definedClass()) {
                 regExTwo();
                 regExOnePrime();            
         }
         else {
             return;
         }
     }
     
     public void regExTwo() {
         if(peekToken() == LPAREN) {
             matchToken(LPAREN);
             regExp();
             if(peekToken() == RPAREN) {
                 matchToken(RPAREN);
                 regExTwoTail();
             }
         }
         else if(peekToken() == RECHAR) {
             matchToken(RECHAR);
             regExTwoTail();
         }
         else {
             if(peekToken() == PERIOD) {
                 regExThree();
             }
             else if(peekToken() == LBRAC) {
                 regExThree();
             }
             else if(definedClass()) {
                 regExThree();
             }
         }
     }
     
     public void regExTwoTail() {
         if(peekToken() == MULTI) {
             matchToken(MULTI);
         }
         else if(peekToken() == PLUS) {
             matchToken(PLUS);
         }
         else {
             return;
         }
     }
     
     public void regExThree() {
         if(peekToken() == PERIOD) {
             charClass();
         }
         else if(peekToken() == LBRAC) {
             charClass();
         }
         else if(definedClass()) {
             charClass();
         }
         else {
             return;
         }
     }
     
     public void charClass() {
         if(peekToken() == PERIOD) {
             matchToken(PERIOD);
         }
         else if(peekToken() == LBRAC) {
             matchToken(LBRAC);
             charClassOne();
         }
         else {
             definedClass();
         }
     }
 
     public void charClassOne() {
         if(peekToken() == CLSCHAR) {
             charSetList();
         }
         else if(peekToken() == RBRAC) {
             charSetList();
         }
         else {
             excludeSet();
         }
     }
     
     public void charSetList() {
         if(peekToken() == CLSCHAR) {
             charSet();
             charSetList();
         }
         else if(peekToken() == RBRAC){
             matchToken(RBRAC);
         }
     }
     
     public void charSet() {
         if(peekToken() == CLSCHAR){
             matchToken(CLSCHAR);
             charSetTail();
         }
     }
     
     public void charSetTail() {
         if(peekToken() == DASH){
             matchToken(DASH);
             clsChar();
         }
         else{
             return;
         }
     }
     
     public void excludeSet() {
         if(peekToken() == NOT) {
             charSet();
             if(peekToken() == RBRAC) {
                 matchToken(RBRAC);
                 if(peekToken() == IN)
                     excludeSetTail();
             }
         }
     }
     
     public void excludeSetTail() {
         if(peekToken() == LBRAC){
             matchToken(LBRAC);
             charSet();
             if(peekToken() == RBRAC){
                 matchToken(RBRAC);
             }
         }
         else{
             definedClass();
         }
     }
 }
