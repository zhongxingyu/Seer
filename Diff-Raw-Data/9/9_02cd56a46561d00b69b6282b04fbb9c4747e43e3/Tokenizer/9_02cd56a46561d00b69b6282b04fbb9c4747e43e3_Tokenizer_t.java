 
 package com.delcyon.capo.parsers;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Arrays;
 
 /**
  * @author jeremiah
  *
  */
 public class Tokenizer
 {
     
     /**
      * These values are used to tokenize the token stream as it is read. 
      */
     public enum CharacterType
     {
       //There is a maximum of 8 possibilities, or the bitmask math won't work
         
         /** used to indicate a char that will force delimination, but will still be included as the next token **/ 
         TOKEN,
         /** set's as a white space char **/
         WHITESPACE,
         /** used to indicate a plain text char, ie. part of a token, not a complete token unto itself. **/
         ALPHA,
         /** used to indicate a quote char **/
         QUOTE,
         /** used to indicate a comment char **/
         COMMENT,        
         /** used to indicate the start of an escape sequence for an non ALPHA chars.**/
         ESCAPE
         ;
         
         /**
          * bit mask of the ControlToken
          */
         public byte mask = 0;
         
         private CharacterType()
         {
             mask = (byte) Math.floor(Math.pow(2, ordinal()-1));            
         }
         
     }
     
     public enum TokenType
     {
         NOTHING(-4),
         TOKEN(-3),
         OTHER(-2),
         EOL('\n'),
         EOF(-1);
         
         public int value;
 
         private TokenType(int intValue)
         {
             this.value = intValue;
         }
     }
     
     private class InternalTokenType
     {
         public TokenType tokenType = null;
         
         public InternalTokenType(TokenType tokenType)
         {
             this.tokenType = tokenType;
         }
 
         public TokenType setTokenType(TokenType tokenType, int tokenValue)
         {
             this.tokenType = tokenType;
             return tokenType;
         }
         
         public TokenType setTokenValue(int tokenValue)
         {
             for (TokenType tokenType : TokenType.values())
             {
                 if(tokenType.value == tokenValue)
                 {
                     this.tokenType = tokenType;
                     return tokenType;
                 }
             }
             tokenType = TokenType.OTHER;
             return tokenType;
         }
     }
     
   
   
     private byte characterTypes[] = new byte[256];
     private Reader reader = null;
     private String value = null;
     int currentChar = -4;
     int currentQuoteChar = -1;
     private boolean pushedBack = false;
 
     private InternalTokenType internalTokenTypeHolder = new InternalTokenType(TokenType.NOTHING);
     
     
   
     private boolean isEOLSignificant = false;
   
     /**
      * default constructor. This will set a number of basic char types. resetSyntax() should be called if you want a blank slate. 
      */
     public Tokenizer() 
     {
         setCharRangeType('a', 'z', CharacterType.ALPHA);
         setCharRangeType('A', 'Z', CharacterType.ALPHA);
         setCharRangeType(128 + 32, 255, CharacterType.ALPHA);
         setCharRangeType(0, ' ', CharacterType.WHITESPACE);
         setCharType('/', CharacterType.COMMENT);        
         setCharType('"', CharacterType.QUOTE);        
         setCharType('\'', CharacterType.QUOTE);
     }
     
     public Tokenizer(InputStream inputStream)
     {
         this(new BufferedReader(new InputStreamReader(inputStream)));
     }
     
     public Tokenizer(Reader reader) 
     {
         this();
         if (reader == null) {
             throw new NullPointerException();
         }
         this.reader = reader;
     }
 
     /**
      * will set the input stream to be read. 
      * @param inputStream
      */
     public void setInputStream(InputStream inputStream)
     {
         this.reader = new BufferedReader(new InputStreamReader(inputStream));
         //reset everything        
         value = null;
         pushedBack = false;        
         internalTokenTypeHolder = new InternalTokenType(TokenType.NOTHING);
     }
     
     /**
      * Set's every char to the TOKEN control type
      */
     public void resetSyntax() 
     {
         Arrays.fill(characterTypes, CharacterType.TOKEN.mask);
     }
 
     /**
      * Set a particular character to a characterType
      * @param character
      * @param characterType
      */
     public void setCharType(int character, CharacterType characterType)
     {
         if(characterType.mask == 0)
         {
             characterTypes[character] = characterType.mask;
         }
         else
         {
             characterTypes[character] |= characterType.mask;
         }
     }
    
     /**
      * Sets a range of characters to a characterType 'A','Z',ALPHA for example
      * @param lowChar
      * @param highChar
      * @param characterType
      */
     public void setCharRangeType(int lowChar, int highChar, CharacterType characterType)
     {
         if (lowChar < 0)
         {
             lowChar = 0;
         }
         if (highChar >= characterTypes.length)
         {
             highChar = characterTypes.length - 1;
         }
         while (lowChar <= highChar)
         {
        	if(characterType.mask == 0)
            {
                characterTypes[lowChar++] = characterType.mask;
            }
            else
            {
                characterTypes[lowChar++] |= characterType.mask;
            }           
         }
     }
     
     /**
      * determines of EOL will be returned as a separate token
      * @param isEOLSignificant
      */
     public void setEOLSignificant(boolean isEOLSignificant)
     {
         this.isEOLSignificant = isEOLSignificant;
     }
 
     /**   
      * @return the type of token that nextToken just read
      */
     public TokenType getTokenType()
     {
         return internalTokenTypeHolder.tokenType;
     }
     
     /**
      * @return the value that nextToken just read
      */
     public String getValue()
     {
         return value;
     }
     
     
     
     
     
     /**
      * This is the main method of this class. Each call to it will return the next token type from the stream, and make available getValue() and getTokenType() for use.
      * @return
      * @throws Exception
      */
     public TokenType nextToken() throws Exception
     {
         
         value = null;
         
         if (pushedBack) //in theory, push back will only occur when we read a significant control char, or token 
         {
             pushedBack = false;
             //if we're not a control char, then turn us into a token with a value.
             if(internalTokenTypeHolder.setTokenValue(currentChar) == TokenType.OTHER)
             {
                 value = ((char)currentChar)+"";
                 return internalTokenTypeHolder.setTokenType(TokenType.TOKEN, currentChar);
             }
             else
             {
                 return internalTokenTypeHolder.tokenType;
             }
         }
         
         StringBuffer stringBuffer = new StringBuffer();
         
         while(true)
         {
             currentChar = reader.read();
             
             //check for EOF
             if(currentChar < 0)
             {
                 if(stringBuffer.length() == 0) //if we don't have data, just return
                 {                    
                     return internalTokenTypeHolder.setTokenValue(currentChar);
                 }
                 else //if we do, return it, and push back for the next call
                 {
                     pushedBack = true;
                     break;    
                 }
             }
             
             //check for EOL
             if(currentChar == TokenType.EOL.value)
             {
                 if(isEOLSignificant) //check to see if we care about EOL chars
                 {
                     //if we do, and we have no data, just return it.
                     if(stringBuffer.length() == 0)
                     {
                        return internalTokenTypeHolder.setTokenValue(currentChar);
                     }
                     else //otherwise return our data, and then let the system know, that on the next read, we have to fake it.
                     {
                        pushedBack = true;
                        break; 
                     }
                 }                
                 else //we just need to eat EOL's like any other whitespace
                 {
                     if(stringBuffer.length() == 0) //if we have no data, just keep reading
                     {
                        continue;
                     }
                     else //if we do have data, return it.
                     {
                        break; 
                     }
                 }
                 
             }
             
              
             int charType = currentChar < 256 ? characterTypes[currentChar] : CharacterType.ALPHA.mask;
             
             //check for an escape symbol
             //There are two kinds of escapes, one that turns a TOKEN into an ALPHA
             //and one that actually un-escapes some sort of escape code like \n  
             if((charType & CharacterType.ESCAPE.mask) != 0)
             {
                 int nextChar = reader.read();
                 if(nextChar != currentChar) //if there are the same, then we're just escaping our escape char
                 {
                     switch (nextChar)
                     {
                         case 'r':  
                             currentChar = '\r';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */
                         case 'n':  
                             currentChar = '\n';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */
                         case 'f':  
                             currentChar = '\f';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */                            
                         case 'b':  
                             currentChar = '\b';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */
                         case 't':  
                             currentChar = '\t';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */
                         case 'a':  
                             currentChar = '\007';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */
                         case 'e':  
                             currentChar = '\033';
                             charType = CharacterType.ALPHA.mask;
                             break; /* switch */
                         case 'c': //handle control chars
                             nextChar = reader.read();
                             if(nextChar > 0x7f)
                             {
                                 throw new Exception("Expected ASCII after \\c");
                             }
                             stringBuffer.append(Character.toChars(nextChar ^ 64));
                             continue; //while loop
                         case '8'://start octal code
                         case '9': throw new Exception("Illegal octal digit");
                         case '1':
                         case '2':
                         case '3':
                         case '4':
                         case '5':
                         case '6':
                         case '7':
                         case '0': 
                             stringBuffer.append(getOctalCodeFromReader(nextChar));
                             continue; //while loop
                         case 'x': //start hex code
                             stringBuffer.append(getHexCodeFromReader());
                             continue; //while loop
                         case 'u': //start unicode small                                           
                             stringBuffer.append(getUnicodeFromReader(4));
                             continue; //while loop                       
                         case 'U'://start unicode big                           
                             stringBuffer.append(getUnicodeFromReader(8));
                             continue; //while loop
                         default://we're just escaping a token here so set it as an alpha
                             charType = CharacterType.ALPHA.mask;
                             currentChar = nextChar;
                     }                    
                 }
                 else
                 {
                     //got the escape char twice, so assume we're escaping it and set it as an alpha
                     charType = CharacterType.ALPHA.mask;
                     currentChar = nextChar;
                 }
                 
             }
             
            
             //if were a comment, then read until EOL or EOF
             if ((charType & CharacterType.COMMENT.mask) != 0)
             {
                 while ((currentChar = reader.read()) != TokenType.EOL.value && currentChar >= 0);
                 //only push back if we've got some data, and EOL is Significant. Otherwise we just need to act like just a new token. 
                 if(internalTokenTypeHolder.tokenType == TokenType.EOL && stringBuffer.length() == 0)
                 {
                     //do nothing, since the last token was already an EOL, we just want to absorb this one as this line was nothing but a comment.
                     continue;
                 }
                 else if(isEOLSignificant == true && stringBuffer.length() != 0)
                 {
                     pushedBack = true;
                 }
                 else if(isEOLSignificant == true && stringBuffer.length() == 0)
                 {
                     internalTokenTypeHolder.setTokenValue(currentChar);
                 }
                 break;
             }
 
             
             //check for a quote symbol
             if((charType & CharacterType.QUOTE.mask) != 0)
             {
                 
                 //start quoting
                 if(stringBuffer.length() == 0 && currentQuoteChar < 0)
                 {
                     currentQuoteChar = currentChar; //keep track of our current quote type
                     continue;
                 }
                 else if(currentChar == currentQuoteChar && stringBuffer.length() > 0)//we're done quoting
                 {
                     currentQuoteChar = TokenType.NOTHING.value; //reset our current quote type
                     break; 
                 }
                 else if(currentChar == currentQuoteChar && stringBuffer.length() == 0)//we're done quoting
                 {
                     currentQuoteChar = TokenType.NOTHING.value; //reset our current quote type
                     value = ""; //special case, where we've referred to the empty string, NOT null.
                     break; 
                 }
             }
 
             if(currentQuoteChar > 0) //if we're quoting, then ignore whitespace
             {
                 charType = CharacterType.ALPHA.mask;
             }
             
             if((charType & CharacterType.WHITESPACE.mask) != 0)
             {
                 if(stringBuffer.length() == 0)
                 {
                    continue;
                 }
                 else
                 {
                    break; 
                 }
             }
             
             if(charType == 0)
             {
                 if(stringBuffer.length() == 0) //if we don't have anything on the buffer, then just go ahead and dump this
                 {
                     stringBuffer.append((char)currentChar);
                     internalTokenTypeHolder.setTokenType(TokenType.TOKEN, currentChar);
                     break;
                 }
                 else //already got something on the buffer, going to need to get creative, and push stuff back a bit.
                 {
                     pushedBack = true;
                     break; 
                 }
             }
             
             if((charType & CharacterType.ALPHA.mask) != 0)
             {
                 stringBuffer.append((char)currentChar);
                 internalTokenTypeHolder.setTokenType(TokenType.TOKEN, currentChar);             
             }
         }
         
         if(stringBuffer.length() != 0)
         {
             value = stringBuffer.toString();            
         }
         return internalTokenTypeHolder.tokenType;
     }
     
     /**
      * this will read up to 8 chars in brackets, or 2 without brackets, and make a char array from the resulting hexcode. 
      * @return
      * @throws Exception
      */
     private char[] getHexCodeFromReader() throws Exception
     {
         
         reader.mark(10);
         char[] buffer = null; 
         int firstChar = reader.read();
         int count = 1;
         boolean braced = false;
         if(firstChar == '{')
         {
             braced = true;
             buffer = new char[8];
             count = 0;
         }
         else if((firstChar >= '0' && firstChar <= '9') || (firstChar >= 'A' && firstChar <= 'F') || (firstChar >= 'a' && firstChar <= 'f'))
         {
             buffer = new char[2];
             buffer[0] = (char) firstChar;
         }
         for(;count < buffer.length;count++)
         {
             int ch = reader.read();
             if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) 
             {
                 buffer[count] = (char) ch;
                 reader.mark(10);
             }
             else if (ch == '}' && braced == true)
             {
                 break;
             }
             else //not a valid hex, so reset
             {                
                 reader.reset();
                 break;
             }
         }
         
         
         int value = Integer.parseInt(new String(buffer,0,count), 16);
         return Character.toChars(value);
     }
     
     
     //read until we get a non octal char or we read 2 additional values.
     private char[] getOctalCodeFromReader(int firstChar) throws Exception
     {
         reader.mark(10);
         char[] buffer = new char[3];
         buffer[0] = (char) firstChar;
         int count = 1;
         for(;count < buffer.length;count++)
         {
             int ch = reader.read();
             if (ch >= '0' && ch <= '7') 
             {
                 buffer[count] = (char) ch;
                 reader.mark(10);
             }
             else //not a valid octal, so reset
             {                
                 reader.reset();
                 break;
             }
         }
         int value = Integer.parseInt(new String(buffer,0,count), 8);
         return Character.toChars(value);
         
     }
     
     /**
      * 
      * @param the maximum length that the unicode declaration can be.
      * @return char[] representing the unicode value.
      * @throws Exception
      */
     private char[] getUnicodeFromReader(int length) throws Exception
     {
         char[] buffer = new char[length];
         int readLength = reader.read(buffer);
         if(readLength < length) //check length
         {
             throw new Exception("value to short of unicode escape");
         }
         //check values
         for (int index = 0; index < length; index++) 
         {
             /* this also handles the surrogate issue */
             if (buffer[index] > 127)
             {
                 throw new Exception("Illegal non-ASCII hex digit in \\u escape");
             }
         }                            
         int value = 0;
         try
         {
             value = Integer.parseInt(new String(buffer), 16);
             return Character.toChars(value);            
         }
         catch (NumberFormatException numberFormatException)
         {
             throw new Exception("Invalid hex value for \\u escape");
         }
         
     }
     
 }
