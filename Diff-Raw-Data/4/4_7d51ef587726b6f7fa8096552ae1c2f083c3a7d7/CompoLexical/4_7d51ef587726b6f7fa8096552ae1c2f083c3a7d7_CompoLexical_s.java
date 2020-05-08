 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package componentprogramming;
 
 public class CompoLexical {
 
     
     public static enum typeToken {
             Num,openTok_brace,closeTok_brace ,openBrace,closeBrace ,ParallelSign,dot,comma,string ;
     }     
     public static class Token {
         
         private  typeToken type;
         private  String s;
         
         public Token(){
             
         }
         
         public Token(typeToken t) {
             this.type = t;
             this.s = t.toString();
         }
         
         public Token(String str){
             s = str ;
             if (s.length() == 1 ){
                 switch(s.charAt(0))
                 {
                     case '(':
                         type = typeToken.openBrace ;
                         break;
                     case ')':
                         type = typeToken.closeBrace ;
                         break;
                     case '<':
                         type = typeToken.openTok_brace ;
                         break;
                     case '>':
                         type = typeToken.closeTok_brace ;
                         break;
                     case '.':
                         type = typeToken.dot;
                         break;
                     case '&':
                         type = typeToken.ParallelSign ;
                         break;
                     case ',':
                         type = typeToken.comma ;
                         break;
                 }
             }
             else
                 type = typeToken.string;
         }
         
         public void load(String str) { 
             s = str; 
         }
         
         public String toString() { 
             return s; 
         }
         
         public int getInt() {
             if (type == typeToken.Num)
                 return Integer.parseInt(s);
             return -1 ;
         }
         
         public typeToken getType() {
             return type;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof Token){
                 Token t = (Token)obj;
                 if (t.type == type){
                     return true;
                 }
                 return false;
             }
             return false;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
             return hash;
         }
     }
 
     NFA nfa;
     String Input;
     Token mCurrentToken;
     nodeNFA CurrentState ;
     int indexInput;
 
     CompoLexical(String str)
     {
        Input = str; 
        nfa = new NFA();
        indexInput = 0;
        CurrentState = nfa.getStartState() ;
        mCurrentToken = null;
     }
     
     Token nextToken(){
         //TODO add next token logic
         Token res = null ;
         String temp = new String() ;
         for (int i =indexInput; i < Input.length(); i++) {
             indexInput++ ; 
             res = new Token();    
                 nodeNFA nextNode  = CurrentState.getNextNode(Input.charAt(i));
                 if (nextNode == null){
                     //TODO Exception
                     throw new RuntimeException("Error in Alphabetic of language");
                 }else if (nextNode == nfa.loopState){
                     temp += Input.charAt(i);
                     res.type = typeToken.Num ;
                     int index = i+1 ;
                    while(nextNode.getNextNode(Input.charAt(index)) ==nfa.loopState )
                     {
                         nextNode = nextNode.getNextNode(Input.charAt(index));
                         temp += Input.charAt(index);
                         index++;
                         i++;
                     }
                     indexInput = index ;
                     res.s = temp;
                     CurrentState = nextNode ;
                     break;
                 }else if (nextNode.isFiniteState()){
                     temp += Input.charAt(i);
                     switch(Input.charAt(i))
                     {
                         case '(':
                             res.type = typeToken.openBrace ;
                             break;
                         case ')':
                             res.type = typeToken.closeBrace ;
                             break;
                         case '<':
                             res.type = typeToken.openTok_brace ;
                             break;
                         case '>':
                             res.type = typeToken.closeTok_brace ;
                             break;
                         case '.':
                             res.type = typeToken.dot;
                             break;
                         case '&':
                             res.type = typeToken.ParallelSign ;
                             break;
                         case ',':
                             res.type = typeToken.comma ;
                             break;
                     }
                     res.s = temp;
                     CurrentState = nextNode ;
                     break;
                 }else{
                     CurrentState = nextNode ;
                 }
         }
         
         if (res !=null)
             mCurrentToken = res ;
         return res ;
     }
     Token currentToken(){
         //TOOD add currnt Token Logic
         return mCurrentToken;
     }
     
     String imageToken(){
         //TODO return token string
         if (currentToken() !=null)
             return currentToken().toString();
         return "";
     }
     
     boolean end() {
         return (indexInput > Input.length());
     }
     
     public void reset() {
         indexInput = 0;
         nfa = new NFA();
         indexInput = 0;
         CurrentState = nfa.getStartState() ;
         mCurrentToken = null;
     }
 }
 
 /*
     Token nextToken(){
         //TODO add next token logic
         Token res = new Token();
         String temp = new String() ;
         for (int i = indexInput; i < Input.length(); i++) {
             if ((Input.charAt(i) == '&')&&(CurrentState.getNextNode(Input.charAt(i))!=null)){
                 res.type = typeToken.ParallelSign ;
                 CurrentState = CurrentState.getNextNode(Input.charAt(i));
                 break;
              }else if ((Input.charAt(i) == '(')&&(CurrentState.getNextNode(Input.charAt(i))!=null)){
                 res.type = typeToken.Open ;
                 break;
             }else if ((Input.charAt(i) == ')')&&(CurrentState.getNextNode(Input.charAt(i))!=null)){
                 res.type = typeToken.Closer ;
                 break;
             }else {
                 temp += Input.charAt(i);
                 nodeNFA nextNode  = CurrentState.getNextNode(Input.charAt(i));
                 if (nextNode == null){
                     //TODO Exception
                     int ii = 0 ;
                 }else if (nextNode.isFiniteState()){
                     res.type = typeToken.component ;
                     res.Load(temp);
                     CurrentState = nextNode ;
                     break;
                 }else{
                     CurrentState = nextNode ;
                 }       
             }
         }
         mCurrentToken = res ;
         return res ;
     }
 */    
