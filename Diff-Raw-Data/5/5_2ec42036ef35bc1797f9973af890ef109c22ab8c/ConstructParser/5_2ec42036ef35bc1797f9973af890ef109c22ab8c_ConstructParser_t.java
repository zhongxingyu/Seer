 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package featureExtractor;
 
 /**
  *
  * @author Ilan
  */ 
 public class ConstructParser {
     
         /*
          *  Spaces around an assignment or equality operand( + - == >=....)
          *  Also counts the amount of opporutnities to later normalize
          */
          int assignment = 0;
          int assignmentOpp = 0;
          int equality = 0;
          int equalityOpp = 0;
     
         /*
          *  Spaces around regular parenthesis
          *      Includes construct parens(if,while,for)
          */
          int open_paren_left = 0;
          int open_paren_right = 0;
          int close_paren_left = 0;
          int close_paren_right = 0;
     
         /*  
          *  Spaces within IF statement
          */
          int if_ifParen = 0;
          int if_parenCondition = 0;
          int if_conditionParen = 0;
          int if_parenBracket = 0;
          int if_bracketInline = 0;
          int if_bracketNewline = 0;
          
          
          /*  
          *  Spaces within WHILE statement
          */
          int while_whileParen = 0;
          int while_parenCondition = 0;
          int while_conditionParen = 0;
          int while_parenBracket = 0;
          int while_bracketInline = 0;
          int while_bracketNewline = 0;
     
     
         /*  
          *  Spaces within FOR statement
          */
         int for_forParen = 0;
         int for_parenVariable = 0;
         int for_variableSemi = 0;
         int for_semiCondition = 0;
         int for_conditionSemi = 0;
         int for_semiIncrement = 0;
         int for_incrementParen = 0;
         int for_parenBracket = 0;
         int for_bracketInline = 0;
         int for_bracketNewline = 0;
         
         public ConstructParser(){
             
         }
         
         public int[] generic_paren_spacing(String s, int i){
                 
                 int constructParen = 0;
                 int parenMiddle = 0;
                 int middleParen = 0;
                 int parenAfter = 0;
             
 		if(s == null)
 			return null;
                 
                 /* 
                  *  Get spacing between if and paren and between
                  *      paren and condition
                  */
                 while( s.charAt(i) != '(' )
                     i++;
                 assert(s.charAt(i) == '(');
                 if( s.charAt( i - 1 ) == ' ') {
                     constructParen++;
                     open_paren_left++;
                 }
                 if( s.charAt( i + 1 ) == ' ') {
                     parenMiddle++;
                     open_paren_right++;
                 }   
                 
                /*
                 *  Get spacing between condition and paren and between
                 *      parent and bracket
                 */            
                 //Take care of special cases
                 i++; // Push past open paren
                 
                 System.out.println("GenericPS: " + s + " " + s.charAt(i) + " " + i);
                 
                 i = find_close_paren(s,i);
 
                 assert(s.charAt(i) == ')');
                 if( s.charAt(i - 1) == ' ' ) {
                     middleParen++;
                     close_paren_left++;
                 }
                 if( s.length() <= i+1 || s.charAt(i + 1) == ' ') { 
                     parenAfter++;
                     close_paren_right++;
                 }
 
                 int array [] = {i, constructParen, parenMiddle, middleParen, parenAfter};
                 return array;
             
         }
         
         
         public int check_if(String s){
 	
 		int count = 0;
                 int i = 0;
                 
                 int array [] = generic_paren_spacing(s,i);
                 if( array == null){
                     System.out.println("PROBLEM in CHECK_IF with generic_paren_spacing");
                     System.exit(1);
                 }
                 
                 i = array[0];
                 if_ifParen = array[1];
                 if_parenCondition = array[2];
                 if_conditionParen = array[3];
                 if_parenBracket = array[4];
                 
                 /*
                  *  Check if bracket is inline or on a new line
                  */
                 if( s.substring(i).contains("{") )
                     if_bracketInline++;
                 else
                     if_bracketNewline++;
                 
                 int check_until = i;
                 if( s.indexOf("{") != -1)
                     check_until = s.indexOf('{');
                     
 		for(int j = 2; j < check_until; j++){
 			if(s.charAt(j) == ' '){
 				count++;
 			}
 		}
 		return count;
 	}
         
         public int check_while(String s){
 		
 		int count = 0;
 		int i = 0;
                 
                 int array [] = generic_paren_spacing(s,i);
                 if( array == null){
                     System.out.println("PROBLEM in CHECK_WHILE with generic_paren_spacing");
                     System.exit(1);
                 }
                 
                 i = array[0];
                 while_whileParen = array[1];
                 while_parenCondition = array[2];
                 while_conditionParen = array[3];
                 while_parenBracket = array[4];
                 
                 /*
                  *  Check if bracket is inline or on a new line
                  */
                 if( s.substring(i).contains("{") )
                     while_bracketInline++;
                 else
                     while_bracketNewline++;
 
                 int check_until = i;
                 if( s.indexOf("{") != -1)
                     check_until = s.indexOf('{');
                 
 		for(int j = 5; j < check_until; j++){
 			//System.out.println("char: " + s.charAt(i));
 			if(s.charAt(j) == ' '){
 				count++;
 			}
 		}
 		return count;
 	}
 
 
 	public int check_for(String s){
 		//System.out.println("check for");
 		
                 int count = 0;
 		int i = 0;
                 
                 if(s == null)
 			return count;
 
                 
                 /* 
                  *  Get spacing between for and paren and between
                  *      parent and variable
                  */
                 while( s.charAt(i) != '(' )
                     i++;
                 assert(s.charAt(i) == '(');
                 if( s.charAt( i - 1 ) == ' ')
                     for_forParen++;
                 if( s.charAt( i + 1 ) == ' ')
                     for_parenVariable++;
                 
                 
                 
                 //Skip this next part if this is a foreach loop
                 if( !isForEachLoop(s) )
                 {
                 
                     /*
                      * Get space between variable and comma and between
                      *      comma and condition
                      */
                     
                     //Skip variable declaration part
                     while( s.charAt(i) != ';')
                         i++;
                        
                     if( s.charAt(i-1) == ' ')
                         for_variableSemi++;
                     if( s.charAt(i+1) == ' ')
                         for_semiCondition++;
                     
                     
                     /*
                      * Get space between condition and comma and between
                      *  comma and increment
                      */
                     i++; //Advance past ';'
                     while( s.charAt(i) != ';')
                         i++;
                        
                     if( s.charAt(i-1) == ' ')
                         for_conditionSemi++;
                     if( s.charAt(i+1) == ' ')
                         for_semiIncrement++;
                     
                 }
                 
                 /* 
                  *  Get spacing between increment and paren and between
                  *      paren and bracket
                  */             
                 i = find_close_paren(s,i);
                 
                 assert(s.charAt(i) == ')');
                 if( s.charAt(i - 1) == ' ' )
                     for_incrementParen++;
                 System.out.println("String: " + s + " length: " + s.length() + " i: " + i);
                 if( s.length() < i+1 && s.charAt(i + 1) == ' ')
                     for_parenBracket++;
                 
                 
                 /*
                  *  Check if bracket is inline or on a new line
                  */
                 if( s.substring(i).contains("{") )
                     for_bracketInline++;
                 else
                     for_bracketNewline++;
 		
                 int check_until = i;
                 if( s.indexOf("{") != -1)
                     check_until = s.indexOf('{');
                 
                 for(int j = 3; j < check_until; j++){
 			//System.out.println("char: " + s.charAt(i));
 			if(s.charAt(j) == ' '){
 				count++;
 			}
                         
                         
 		}
 		return count;
 	}
         
         public int [] checkAssignmentOrEquality(String s){
             
             int array [] = containsAssignmentOrEquality(s);
             if( array[0] == -1)
                 return array;
             
             int index = array[0];
             int stretch_right = array[1];
             
             
             int spacing = 0;
             if( index - 1 < 0 || s.charAt(index - 1) == ' ')
                 spacing++;
             if( s.length() <= index + 1 || s.charAt(index + stretch_right + 1) == ' ')
                 spacing++;
             
             /* Keep assignment counting separate */
             if( s.charAt(index) == '=' && stretch_right == 0){
                 assignment += spacing;
                 assignmentOpp += 2;
             }
             else {
                 equality += spacing;
                 equalityOpp += 2;
             }
             
             return array;
         }
         
         
         
         /* Helper methods */
         
         public int find_close_paren(String s, int i){
             int open_paren_counter = 0;
             
             System.out.println("Find Close Paren:  " + s + " " + i );
             
            while(s.charAt(i) != ')' /*|| open_paren_counter > 0*/) {
                 if( s.charAt(i) == '\"')
                     i = find_close_string(s,i+1);
                 
                 if( s.charAt(i) == '\'')
                     i = find_close_char(s,i+1);
                
                     
                 if( s.charAt(i) == '(') {
                     open_paren_counter++;
                     if( s.charAt(i - 1) == ' ' )
                         open_paren_left++;
                     if( s.charAt(i + 1) == ' ')
                         open_paren_right++;
                 }
                 
                 if( s.charAt(i) == ')') {
                     open_paren_counter--;
                     if( s.charAt(i - 1) == ' ' )
                         close_paren_left++;
                     if( s.charAt(i + 1) == ' ')
                         close_paren_right++;
                     
                 }
                System.out.println("Done looking at char: " + s.charAt(i));
                 i++;
             }
             
             return i;
         }
         
         public int find_close_string(String s, int i){
             
             System.out.println("Find Close String:  " + s + " " + i );
             
             while(s.charAt(i) != '\"') {
                 i++;
                 if( s.length() <= i){
                     System.out.println("MULTIPLE LINE STRING: " + s + " " + i);
                     System.exit(1);
                 }
             }
             
             return i;
         }
         
          public int find_close_char(String s, int i){
             
             System.out.println("Find Close Char:  " + s + " " + i + " " +s.charAt(i) );
             
             while(s.charAt(i) != '\'') {
                 
                 if( s.length() <= i){
                     System.out.println("MULTIPLE LINE CHAR: " + s + " " + i);
                     System.exit(1);
                 }
                 
                 if( s.charAt(i) == '\\'){
                     i = i + 2; // Skip \'
                     continue;
                 }
                 i++;
             }
             
             return i;
         }
         
         
         
         public int [] containsAssignmentOrEquality(String s){
             
             int min_index = Integer.MAX_VALUE;
             int index;
             int stretch_right = 0;
             
             /* First putting two character operands or equality */
             
             if( (index = s.indexOf("==")) != -1 && index < min_index) {
                 min_index = index;
                 stretch_right = 1;
             }
             
             if( (index = s.indexOf("!=")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 1;
             }
             
             if( (index = s.indexOf("<=")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 1;
             }
             
             if( (index = s.indexOf(">=")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 1;
             }
             
             if( (index = s.indexOf(">=")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 1;
             }
             
             if( (index = s.indexOf("++")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 1;
             }
             
             if( (index = s.indexOf("--")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 1;
             }
             
             /* Now come the single operand or equality */
                        
             if( (index = s.indexOf("=")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf("<")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf(">")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf("?")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf(":")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf("%")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf("+")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             /*
              * FALSE POSITIVE ON NEGATIVE NUMBERS -- Possible ignore/fix?
              */
             if( (index = s.indexOf("-")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf("*")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             if( (index = s.indexOf("/")) != -1 && index < min_index) {
                 min_index = (index < min_index) ? index : min_index;
                 stretch_right = 0;
             }
             
             int returnIndex = (min_index != Integer.MAX_VALUE) ? min_index : -1;
             
             int array [] = {returnIndex, stretch_right};
             return array;
         }
         
         public boolean isForEachLoop(String s){
             return s.contains(":");
         }
         
     
 }
