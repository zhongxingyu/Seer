 package jmathlib.core.interpreter;
 
 import jmathlib.core.tokens.*;
 import jmathlib.core.tokens.numbertokens.*;
 import jmathlib.core.constants.*;
 
 
 /**class for turning a string expression into a vector of tokens.
 All public methods are static so there is no need
 to instantiate it. it creates an instance of itself when the 
 analyseExpression function is called.*/
 public class LexicalAnalyser implements TokenConstants, ErrorCodes
 {
     /**The expression being worked on*/
     private String exp="";
 
     /**bracketLevel is used to implement bracketing within expressions*/
     private int bracketLevel;
     
     /**the last token parsed*/
     private Token lastToken;
 
     /** previous valid character */
     private char previousChar;
 
     private int   charNo;
 
     /**If all characters are processed the value of EOCharsB is set to TRUE*/
     private boolean EOCharsB;
 
     //set up strings used to determine token type
     /**List of characters recognised as being digits*/ 
     private String  numberChars;
     //private String  operatorChars      = "+-/*^<>~=:"; 
     //private String  unaryOperatorChars = "!";
     
     /**List of alphanumeric characters*/
     private String  textChars;
     
     /**List of delimiters*/
     private String  delimiterChars; 
 
     /**The list of reserved words*/ 
     private String  reservedWords;
 
     /**special reserved words which act as delimiters*/
     private String delimiterWords;
 
     /** currently scanned line of code */
     private String codeLine = "";
     
     /**store the next scanned token*/
     private Token nextToken;
 
     /**store whether the next value should be negative*/
     private boolean negative;
 
     /**hide evaluation of invisible code (comments, signs of numbers) */
     private boolean invisibleCode;
     
     /**switch to enable parsing of 'spaces' and 'return' characters */
     private boolean parseWhitespaceSwitch = false;
     
     /**default constructor - creates the lexical analyser object with an empty string*/
     public LexicalAnalyser()
     {
         reservedWords      = " break do exit try catch continue ";
         reservedWords     += " for help history hold if load more return ";
         reservedWords     += " load dir ls save set show who whos ";
         reservedWords     += " cd chdir clear diary echo format ";
         reservedWords     += " type global isglobal ";
 	    reservedWords     += " save switch while ";     // trailing " " is very important !!
         delimiterWords	   = " end endif else elseif endfunction endwhile endfor ";
         delimiterWords    += " case default otherwise endswitch ";
         delimiterChars     = ",()[];{}\n"; 
         textChars          = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";
         numberChars        = "0123456789";
     }
 
     /**Interface function used to analyse an expression
        @param expression - expression to be analysed   */
     public void analyseExpression(String expression)
     {
         //ErrorLogger.debugLine(expression);
         exp = expression.trim();
 		
         EOCharsB = false;
     }
 
     /**@return the next token in the input string*/ 
     public Token getNextToken(int type)
     {
         // switch on/off parsing of whitespaces
         if (type==MATRIX)
         	parseWhitespaceSwitch = true;
         else
         	parseWhitespaceSwitch = false;
 
     	scanNextToken();
    		lastToken = nextToken;
         
         //if (nextToken !=null)
         //    ErrorLogger.debugLine("Lex "+nextToken.toString());
                 
         return nextToken;
     }
     
     /**return string of currently scanned line of code for error message*/
     public String getScannedLineOfCode()
     {
     	return codeLine;
     }
 
 /******************************************************************************
 *                     internal methods                                        *      
 *      e.g: sin(3+4)
 *             |
 *           pointer
 *******************************************************************************
 *
 *  inspectNextChar() returns '(' and leaves the pointer at the current position
 *      e.g: sin(3+4)
 *             |
 *           pointer
 *******************************************************************************
 *
 *  getNextChar()  returns '(' and increased the pointer
 *      e.g: sin(3+4)
 *              |
 *            pointer
 *******************************************************************************
 *  advance() only increases the pointer
 *      e.g: sin(3+4)
 *              |
 *            pointer
 ******************************************************************************/
 
     /** return true if no more characters are available for parsing */
     public boolean EOChars() 
     {
         // True if End Of chars
         if (exp.length()==0) return true;
 		
         return EOCharsB;
     } 
 
     /** return the next character in exp-array, but do not increment pointer 
         to next character. Only inspect next token, but don't process it    */
     private char inspectNextChar() 
     {
         if (charNo >= exp.length()) return ' ';
         return exp.charAt(charNo);  
     } 
 
     /** return next character of exp-array, also increase the pointer in the
         exp-array.                                                           */
     private char getNextChar() 
     {
         if (charNo>0) 
     	    previousChar = exp.charAt(charNo-1);
     	
     	if (charNo < exp.length() )
     	{
     	    charNo ++;
     	    EOCharsB = false;
     
     	    //build string of currently scanned line of code for error message
                 codeLine += exp.charAt(charNo-1);
     	    if ((exp.charAt(charNo-1) == '\n') ||
                     (exp.charAt(charNo-1) == '\r')    )
                 	codeLine = "";    
                        
     	    return exp.charAt(charNo-1);  
     	}
     	else
     	{
     	    EOCharsB = true; // end of chars reached
     	    return ' ';
     	}
     } 
 
     /** increase the pointer of the character array */
     private void advance()
     {
     	if (charNo < (exp.length()-1))
     	{
     	    charNo ++;
     		EOCharsB = false;
                 
     		//build string of currently scanned line of code for error message
     		codeLine += exp.charAt(charNo-1);
          	if ((exp.charAt(charNo-1) == '\n') ||
     		    (exp.charAt(charNo-1) == '\r')    )
     		    codeLine = "";                
     	}
     	else
     	    EOCharsB = true; // end of chars reached
     }
 
     /* return the previous character */
     private char getPreviousChar()
     {
         return previousChar;
     }
 
     /**move back to the previouse character*/
     private void backTrack()
     {
         if(charNo > 0)
             charNo--;
     }
 
     //private char inspectNextNonWhitespaceChar() 
     //{
 	//if (charNo >= exp.length()) return ' ';
 	//return exp.charAt(charNo);  
     //} 
 
 /*****************************************************************************/
 
     /** parse the array of characters (exp[]) to find next token     
 	For each character it calls a series of functions until it
 	finds one that can handle the character
 	@return the next token contained within the expression string */
     private boolean scanNextToken() throws MathLibException
     {
         negative = false;  // reset sign indicator
 
         //Exit the loop as soon as a token has been found
         //  or no more characters are available
         while( !EOChars() )	
         {
             // next character to analyse
             char nextChar = getNextChar();
 
             // comments and '+' '-' signs of numbers ... are invisible
             invisibleCode = false;
             
             //call each function in turn on the next character
             //stop as soon as a function returns true to indicate it has handled the character
             boolean foundToken = handleEmptyLine(nextChar) ||
                                  handleSpace(nextChar)     ||
 		                 		 handleComment(nextChar)   ||
 								 handleNumber(nextChar)    ||
                                  handleOperator(nextChar)  ||
                                  handleText(nextChar)      ||
                                  handleString(nextChar)    ||
                                  handleDelimiter(nextChar) ||
                                  handleDotOperator(nextChar);
 
             if (!invisibleCode)
             {
             	//code is visible
                 
                 if(!foundToken)
             	{
                 	//ignore any characters that it doesn't recognize
                 	// could be something like cd /programs/word
                 	ErrorLogger.debugLine("LexAna: don't know what to do with: >"+nextChar+"<");
             	}
             	else
             	{
             		// return a valid token
             		return true;
             	}
             }
         } // end while
 
         // no more tokens available
         nextToken = null;
         
         return false;
 
     } // end scanNextToken
     
 //*************************************************************************************************
 //Utility functions
 //*************************************************************************************************
     /**
      * Sometimes a file starts with some empty line or comments.
      * This methods removes all returns,tabs,comments at the beginning of a file or after a new line
      */
     private boolean handleEmptyLine(char nextChar)
     {
         boolean foundToken = false;
         
         // check if previous char is start of file OR a line feed
         if ((getPreviousChar() ==   0  ) ||
             (getPreviousChar() == '\n' )   )
         {
             
             //nextChar = inspectNextChar();
             
             // loop until all whitespaces, comments are removed
             while (true)
             {
                 
                 // check for all types of invisible chars
                 if  ((nextChar == ' ') ||
                      (nextChar == '\t')  ) 
                 {
                     //remove multiple spaces and tabs
                     while ((inspectNextChar() == ' ') ||
                            (inspectNextChar() == '\t')  )
                     {
                         advance();
                     }
                     invisibleCode = true;
                     foundToken    = true;
                 }
                 else if (nextChar == '\n')
                 {
                     // remove return
                     //nextChar = getNextChar();
                     invisibleCode = true;
                     foundToken    = true;
                 }
                 else if ((nextChar == '#') ||
                          (nextChar == '%')  )
                 {
                     // comment for the rest of this line
                     // e.g.                   # some comment
                     // e.g. #some comment
                     while((inspectNextChar() != '\n') && (!EOChars()))
                     {
                         advance();
                     }
                     invisibleCode = true;
                     foundToken    = true;
                 }
                 else
                     break;
                 
                 // check if next char would be a whitespace or comment again
                 nextChar = inspectNextChar();
                 if ((nextChar == ' ')  ||
                     (nextChar == '\t') ||
                     (nextChar == '\n') ||
                     (nextChar == '#')  ||
                     (nextChar == '%')     )
                 {
                     advance();
                 }
                 
             } // end loop invisible coee
 
         }
         
         
         return foundToken;   // nothing found
     }
 
     /**Check if the next token is a whitespace
     @return true if it is
     */
     private boolean handleSpace(char nextChar)
     {
         boolean foundToken = false;
 
 		if( (nextChar == ' ' ) ||
         	//(nextChar == '\n') || 
 			(nextChar == '\r') || 
 			(nextChar == '\t')    ) 
 		{
             // whitespaces ' ', '\n' and '\r' are also treated as tokens if they are used
             // inside of matrices  (e.g.: a=[1 2 \n 3 4] --> a=[1,2;3,4])
             if ( parseWhitespaceSwitch)
             {
             	char p = getPreviousChar();   // save previous char
             	char t = nextChar;	          // might be ' ', '\r' or '\n'
                 
                 // remove multiple spaces
                 while (inspectNextChar() == ' ')
                 {
                 	char n = getNextChar();
                 }  
                 
                 // check if '\n' or '\r' are superseeding ' '
                 if ((inspectNextChar() == '\n') || 
                     (inspectNextChar() == '\r')   )
                 {
                 		t = getNextChar();
                         t = '\n';
                         //ErrorLogger.debugLine("LexAna: whitespace return");
                 }
                 
                 // If there is a previous delimiter or trailing delimiter a 
                 // whitespace is just some empty code (e.g. a=[1, 2] or a=[1 ,2] or
                 // a=[1,2 ] )
                 // Otherwise the whitespace is a delimiter (e.g. a=[1 2])
                 char n = inspectNextChar();
                 if ((p !=  0 )  &&
                     (p != ',')  && (p != ';')  && 
                     (p != '\n') && (p != '\r') &&
                     (n != ',')  && (n != ';')  && (n != ']')    )
                 { 
                 	nextToken = new DelimiterToken(t);
             		//ErrorLogger.debugLine("LexAna: whitespace delimiter");
             	}
                 else
                    invisibleCode = true;
             }
             else
             {
                 invisibleCode = true;
             } 
             
             foundToken    = true;
         }
         
         return foundToken;
     }
 
     /**Check if the next token is a comment
     @return true if it is
     */
     private boolean handleComment(char nextChar)
     {
         boolean foundToken   = false;
         char    nextNextChar = inspectNextChar();
         if((nextChar == '#' ) || 
            (nextChar == '%' ) || 
            (nextChar == '/' && nextNextChar == '/'))
         {
             //comment for the rest of the line
             // # this is a comment ...
             // % this is a comment ...
             // // this is a comment
             while((inspectNextChar() != '\n') && (!EOChars()))
             {
                 advance();
             }
             
             // remove trailing \n
             // e.g.  # some comment \n
             if (inspectNextChar() == '\n')
                 advance();
                 
             invisibleCode = true;
             foundToken    = true;
         }
         else if(nextChar == '/' && nextNextChar == '*')
         {
             // /* */ style comment
             boolean endComment = false;
             while(!endComment)
             {
                 if(getNextChar() == '*')
                 {
                     if(getNextChar() == '/')
                     {
                         endComment = true;
                         advance();
                     }
                 }
                 else if(EOChars())
                     endComment = true;
             }
             invisibleCode = true;
             foundToken    = true;
         }
         return foundToken;
     }
     
     /**Check if the next token is a number
     @return true if it is
     */
     private boolean handleNumber(char nextChar)
     {
         boolean foundToken = false;
         // e.g. 50000
         // e.g. .6789
         if((numberChars.indexOf(nextChar) != -1) || 
            (nextChar == '.' && (numberChars.indexOf(inspectNextChar()) != -1)))
         {
             StringBuffer sBuffer = new StringBuffer();
             //token is a number (e.g. 123 or 123.456 or 1234.345e+5)
             sBuffer.append(nextChar);
 
             // analyse number left of the point (e.g. >123<.456e+5)
             while(numberChars.indexOf(inspectNextChar()) != -1) 
             {
                 sBuffer.append(getNextChar());
             }
 
             // look for a "." (e.g. 3.4)
             if (inspectNextChar() == '.')
             {
                 getNextChar();
                 if(numberChars.indexOf(inspectNextChar()) > -1)
                 {
                     sBuffer.append('.');  // append '.'
     
                     // look for numbers on the right side of '.' (e.g. 123.>456<e+5)
                     while(numberChars.indexOf(inspectNextChar()) != -1) 
                     {
                         sBuffer.append(getNextChar());
                     }
                 }
                 else
                 {
                     backTrack();
                 }
             }
 
             // check for "e" or "E" (e.g. 22.33e5)
             if (   (inspectNextChar() == 'e')
                 || (inspectNextChar() == 'E') )
             {
                 sBuffer.append(getNextChar());  // append 'e' or 'E'
 
                 // check for sign (e.g. 22.33e+5 or 22.33e-5)
                 if (   (inspectNextChar() == '+')
                     || (inspectNextChar() == '-') )
                 {
                     sBuffer.append(getNextChar());  // append '+' or '-'
                 }
 
                 // check for exponent
                 while(numberChars.indexOf(inspectNextChar()) != -1)
                 {
                     sBuffer.append(getNextChar());
                 }
             }
 
             // convert buffer to string and check for minus sign
             String number = sBuffer.toString();
 
             if (negative)
                 number = "-" + number;
 
             // real or imaginary number
             // check for "i" or "j" to indicate imaginary number
             if (   (inspectNextChar() == 'i')
                 || (inspectNextChar() == 'j') )
             {
                 // imaginary number
                 advance();
                 nextToken = new DoubleNumberToken("0", number);
             }
             else
             {
                 // real number
                 nextToken = new DoubleNumberToken(number, null);
             }
 
             //ErrorLogger.debugLine("LexAna: number = "+nextToken.toString());
             foundToken = true;
         }
     
         return foundToken;
     }
     
     /**Check if the next token is a string
     @return true if it is
     */
     private boolean handleString(char nextChar)
     {
         boolean foundToken = false;
         if(nextChar == '\"' || nextChar == '\'')
         {
             if(nextChar == '\'')
             {
                 char prevChar = getPreviousChar();
                 // e.g.  [1,2]' or variable' 
                 if (   (prevChar == ']')
                     || (prevChar == ')')
                     || (textChars.indexOf(prevChar) != -1) )
                 {
                     return false;
                 }
             }
 
             //token is a string
             char endChar = nextChar;
             StringBuffer sBuffer = new StringBuffer();
             nextChar = getNextChar();
             while((nextChar != endChar) && (!EOChars()))
             {
                 sBuffer.append( nextChar );           			
                 nextChar = getNextChar();					
             }
                                 
             //ErrorLogger.debugLine("LexAna: String = "+sBuffer.toString());
             nextToken = new CharToken(sBuffer.toString());
             foundToken = true;
         }
     
         return foundToken;
     }
     
     /**Check if the next token is a text item
     @return true if it is	                   */
     private boolean handleText(char nextChar)
     {
         boolean foundToken = false;
         if(textChars.indexOf(nextChar) != -1)
         {
             foundToken = true;
             //token is either a command, function or variable
             StringBuffer sBuffer = new StringBuffer();
 
             sBuffer.append(nextChar);
             while((textChars.indexOf(inspectNextChar()) != -1) )
             {
                 sBuffer.append( getNextChar() );
             }
             String name = sBuffer.toString();
     
             if( name.equals("i") ||
                 name.equals("j") )
             {
                 //ErrorLogger.debugLine("LexAna: ImaginaryNumber = "+name);
 
                 int value = 1;
                 if(negative)
                     value = -1;
 
                 nextToken = new DoubleNumberToken(0,value);
             }
             else if(name.equals("Inf") || name.equals("inf"))
             {
             	// positive and negative infinity
                 if(negative)
                 	nextToken = new DoubleNumberToken(Double.NEGATIVE_INFINITY);
                 else
                 	nextToken = new DoubleNumberToken(Double.POSITIVE_INFINITY);
             }
             else if(name.equals("NaN"))
             {
             	// not a number (e.g. 0/0 or Inf/Inf)
                 nextToken = new DoubleNumberToken(Double.NaN);
             }
             else if(delimiterWords.indexOf(" " + name + " ") != -1)
             {
                 nextToken = new DelimiterToken(name);
                 //ErrorLogger.debugLine("LexAna: found reserved delimiter. word "+name);
             }
             else if(reservedWords.indexOf(" "+name+" ") != -1)
             {
                 nextToken = new FunctionToken(name);
                 //ErrorLogger.debugLine("LexAna: found reserved func. word "+name);
             }
             else if(inspectNextChar() == '(')
             {
                 //text is a function
                 //ErrorLogger.debugLine("LexAna: Function = "+name);
                 nextToken =  new FunctionToken(name);
             }
             else
             {
                 nextToken = new VariableToken(name);
                 //ErrorLogger.debugLine("LexAna: Variable ="+name);
             }
         }    
         return foundToken;        
     }
     
     /**Check if the next token is an operator
     @return true if it is					 */
     private boolean handleOperator(char nextChar)
     {
         boolean foundToken = false;
         if ((nextChar == '+') || 
             (nextChar == '-')    )
         {
             foundToken = true;
             // after ,;([{ a minus sign indicates a negative number
             boolean lastDelim = false;
             if (lastToken instanceof DelimiterToken)
             {
                 DelimiterToken delimiter = (DelimiterToken)lastToken;
                 if (delimiter.value==',' ||
                     delimiter.value==';' ||
                     delimiter.value==':' ||
                     delimiter.value=='[' ||
                     delimiter.value=='{' ||
                     delimiter.value=='('    )
                     lastDelim = true;
             }
 
             // After an operator token a minus/plus sign indicates
             //   a negative/positive number (e.g. 3* -4 or 3* +4 or 3+ +4 or 3+ -4)
             if (lastToken instanceof BinaryOperatorToken)
                  lastDelim = true;
 
             // maybe '-' or '+' is first character of character-array
             if (lastToken == null)
                  lastDelim = true;
 
             if (lastDelim)
             {
                 // minus sign indicates a negative number	
                 if(nextChar == '-')
                     negative = true;   // e.g. -4444
                 else
                     negative = false;  // e.g. +4444
             
                 
                 // +++++-+-+-8 
                 while (inspectNextChar()=='-' || inspectNextChar()=='+')
                 {
                     // e.g. +- then change sign
                     if (inspectNextChar()=='-')
                         negative = !negative;
 
                     advance();
                    
                    if (EOChars())
                        Errors.throwMathLibException("end of chars");
                 }
                 
                
                 // e.g. if other than number return sign (plus/minus)
                 // e.g. -*  -hello +foo +(3+4) 
             	if (numberChars.indexOf(inspectNextChar())==-1    )
                 {
                 	nextToken=new AddSubOperatorToken(nextChar);
                 	return true;
                 }
                     
                 invisibleCode = true;
             	return true;
             }
             else
             {
                 // check for increment/decrement ++ or --
                 if (inspectNextChar() == nextChar)
                 {
                     //++ or --
                     advance();
                     //ErrorLogger.debugLine("LexAna: Increment/Decrement "+nextChar);
                     nextToken = new UnaryOperatorToken(nextChar); //, bracketLevel);
                 }
 	            else
                 {
 	                //ErrorLogger.debugLine("LexAna: Add/Sub = "+nextChar);
     	            nextToken = new AddSubOperatorToken(nextChar); //, bracketLevel);
                 }
             }
         }
         else if(nextChar == '/' )
         {
             foundToken = true;
             //token is division "/", comment /* */ or // 
             if(inspectNextChar() == '*' || inspectNextChar() == '/')
             {
                 return false;
             }
             else
             {
                 //ErrorLogger.debugLine("LexAna: MulDiv = "+nextChar);
                 nextToken = new MulDivOperatorToken('/'); // bracketLevel);
             }
         }
         else if(nextChar == '\'')
         {
             char prevChar = getPreviousChar();
             if(prevChar == ']' ||
                prevChar == ')' ||
                textChars.indexOf(prevChar) > -1)
              {
                 foundToken = true;
                 //ErrorLogger.debugLine("LexAna: Transpose");
                 nextToken = new UnaryOperatorToken(nextChar); 
              }
         }
         else if(nextChar == '*')
         {
             foundToken = true;
             //ErrorLogger.debugLine("LexAna: MulDiv = "+nextChar);
             nextToken = new MulDivOperatorToken('*');
         }
         else if(nextChar == '\\')
         {
             foundToken = true;
             //ErrorLogger.debugLine("LexAna: left division = "+nextChar);
             nextToken = new MulDivOperatorToken('L');
         }
         else if(nextChar == '^')
         {
             foundToken = true;
             //ErrorLogger.debugLine("LexAna: Power = "+nextChar);
             nextToken = new PowerOperatorToken('m'); 
         }
         else if (nextChar=='~' && (inspectNextChar() != '='))
         {
             // e.g. !3 or !(a<3) or ~3 or ~(a<3)
         	foundToken = true;
             //ErrorLogger.debugLine("LexAna: Not = "+nextChar);
             nextToken = new UnaryOperatorToken(nextChar); 
         }
         else if((nextChar == '<' ) || 
                 (nextChar == '>' ) || 
                 (nextChar == '~' ) || 
                 (nextChar == '&' ) || 
                 (nextChar == '|' ) || 
                 (nextChar == '=' && inspectNextChar() == '=') )
         {
             foundToken = true;
             // <  less than				 '<'
             // >  greater than           '>'
             // <= less    than or equal  'l'
             // >= greater than or equal  'g'
             // == equal                  'e'  (see Assign)
             // ~= not equal              'n'
             if (inspectNextChar() == '=') 
             {
                 advance();
                 // <=, >=, ~=
                 if(nextChar == '<')
                 {
                     nextChar = 'l';
                 }
                 else if(nextChar == '>')
                 {
                     nextChar = 'g';
                 }
                 else if(nextChar == '~')
                 {
                     nextChar = 'n';
                 }
                 else if(nextChar == '=')
                 {
                     nextChar = 'e';
                 }
             }
             else if (nextChar=='&' && (inspectNextChar() == '&'))
 			{
 				// &&   -> and
 				advance();
 				nextChar = 'a'; 
 			}
             else if (nextChar=='|' && (inspectNextChar() == '|'))
 			{
 				// ||   -> or
 				advance();
 				nextChar = 'o'; 
 			}
             			
             //ErrorLogger.debugLine("LexAna: Relation = "+nextChar);
             nextToken = new RelationOperatorToken(nextChar); //, bracketLevel);
         }
         else if(nextChar == '=')
         {
             foundToken = true;
             //token is an assignment operator
             //ErrorLogger.debugLine("LexAna: Assign = --");
             nextToken = new AssignmentOperatorToken(); //bracketLevel);
         }
         else if(nextChar == ':')
         {
             foundToken = true;
             //ErrorLogger.debugLine("LexAna: Colon = "+nextChar);
             nextToken = new ColonOperatorToken(); 
         }
         else if(nextChar =='!')
         {
             foundToken = true;
             // matlab-style "~=" and C-style "!=" implement NOT-EQUAL
             if (inspectNextChar() == '=') 
             {
                 advance();
                 //ErrorLogger.debugLine("LexAna: !=");
                 nextToken = new RelationOperatorToken('n'); //, bracketLevel);
             }
             else
             {
                 // factorial (e.g. 3!)       or 
             	// logical not (e.g. !(2<3) )
                 //ErrorLogger.debugLine("LexAna: !");
                 nextToken = new UnaryOperatorToken('!'); //, bracketLevel);
             }
         }
         else if(nextChar == '@')
         {
             foundToken = true;
             //token is a function handle
             //ErrorLogger.debugLine("LexAna: function handle");
 
             //token is either a command, function or variable
             StringBuffer sBuffer = new StringBuffer();
 
             while((textChars.indexOf(inspectNextChar()) != -1) )
             {
                 sBuffer.append( getNextChar() );
             }
             String name = sBuffer.toString();
             nextToken = new FunctionHandleToken(name);
         }
 
         return foundToken;
     }
     
     /**Check if the next token is a delimiter
     @return true if it is
     */
     private boolean handleDelimiter(char nextChar)
     {
         boolean foundToken = false;
         if(delimiterChars.indexOf(nextChar) != -1)
         {
             //token is a delimiter
             if(nextChar == '(')
                 bracketLevel += BRACKET_PRIORITY;
             else if(nextChar == ')')
                 bracketLevel -= BRACKET_PRIORITY;
 
             //check the bracket ordering is valid
             if(bracketLevel < 0)
             {
                 Errors.throwMathLibException(ERR_BRACKET_ORDER);
             }
             
             //ErrorLogger.debugLine("LexAna: Delimiter = "+nextChar);
             foundToken = true;
             nextToken = new DelimiterToken(nextChar);
             
             // remove trailing ' ' \t and \n
             //remove multiple spaces and tabs
             if ((nextChar == ';') || (nextChar == ','))
             {
                 while (((inspectNextChar() == ' ') ||
                         (inspectNextChar() == '\t')  ) && !EOChars() )
                 {
                     advance();
                 }
                 // remove return after other delimiter
                 // e.g. disp(x);   \n   (\n will be removed)
                 // e.g. disp(x)    \n   (\n will not be removed)
                 if ((inspectNextChar()=='\n'))
                     advance();
             }
         }
         
         return foundToken;
     }
 
     /**Check if the next token is a dot operator
     @return true if it is
     */
     private boolean handleDotOperator(char nextChar)
     {
         boolean foundToken = false;
         if(nextChar == '.' )
         {
             foundToken = true;
             char nextNextChar = inspectNextChar();
             // something like ".*", "./", ".^", "..."  (scalar operations on matrices)
             //                ".\"
             switch(nextNextChar)
             {
                 case '*':
                 {
                     nextChar  = getNextChar();
                     //ErrorLogger.debugLine("LexAna: scalar muliplication .*");
                     nextToken = new MulDivOperatorToken('m');
                     break;
                 }
                 case '/':
                 {
                     nextChar  = getNextChar();
                     //ErrorLogger.debugLine("LexAna: scalar division ./");
                     nextToken = new MulDivOperatorToken('d');
                     break;                    
                 }
                 case '\\':
                 {
                     nextChar  = getNextChar();
                     //ErrorLogger.debugLine("LexAna: scalar left division .\\");
                     nextToken = new MulDivOperatorToken('l');
                     break;
                 }
                 case '^':
                 {
                     nextChar  = getNextChar();
                     //ErrorLogger.debugLine("LexAna: scalar power .^");
                     nextToken = new PowerOperatorToken('p');
                     break;
                 }
                 case '\'':
                 {
                     nextChar  = getNextChar();
                     //ErrorLogger.debugLine("LexAna: nonconjugate transpose .'");
                     nextToken = new UnaryOperatorToken('t');
                     break;
                 }
                 case '.':
                 {
                     // checking for ... \n  opr
                 	//              .... \r
                 	nextChar  = getNextChar();
                 	if( inspectNextChar()=='.')
             		{
                 		//found ...
                 		invisibleCode = true;  // the scanned code needs to be ignored
                     	nextChar  = getNextChar();
 
                     	// scan the next chars until a \r or \n is found
                     	while ((inspectNextChar() != '\r') &&
                     		   (inspectNextChar() != '\n') &&
 							    !EOCharsB                      )
                     	{
                     		getNextChar();
                     	}
                     	getNextChar();  // remove \n or \r
                     	return false;
             		}
                 	else 
                 	{
                 		// found .. this is not allowed
                 		Errors.throwMathLibException("LexAnal: found .. instead of ...");
                 	}
                     break;
                 }
                 default:
                 {                   
                     if (numberChars.indexOf(inspectNextChar()) != -1)
                     {
                         return false;
                     }
                     else
                     {
                         // dot operator
                         //ErrorLogger.debugLine("LexAna: dot operator");
                         nextToken = new DotOperatorToken();
                     }
                     break;
                 }
             }
         }
     
         return foundToken;
     }
 } // end LexicalAnalyser
