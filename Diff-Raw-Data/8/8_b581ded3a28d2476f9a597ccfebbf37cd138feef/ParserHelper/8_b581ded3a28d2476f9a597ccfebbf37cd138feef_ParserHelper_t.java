 /*
  * ParserHelper.java
  *
  */
 
 package ua.gradsoft.parsers.java5;
 
 import ua.gradsoft.termware.TermWareException;
 import ua.gradsoft.termware.exceptions.AssertException;
 
 /**
  *Utility class for helper functions.
  * @author rssh
  */
 public class ParserHelper {
 
     
     public static String decodeStringLiteral(String s) throws TermWareException
     {
       return decodeStringOrCharLiteral(s,'\"',"string");  
     }
     
     public static char decodeCharLiteral(String s) throws TermWareException
     {
       if (s.length()==0)  {
           throw new AssertException("ParserHelper.decodeCharLiteral(\'\') called");
       }
       String r = decodeStringOrCharLiteral(s,'\'',"character");
       return r.charAt(0);
     }
 
     
     
     static String decodeStringOrCharLiteral(String s, char quote, String kind) throws TermWareException
     {
       char[] sarray = s.toCharArray();
       
       if (sarray[0]!=quote) {
           throw new AssertException("Invalid "+kind+" literal:"+s);
       }
       StringBuilder sb = new StringBuilder();
       for(int i=1; i<sarray.length-1; ++i) {
           switch(sarray[i]) {              
               case '\\': 
               {
                 if (i+1==sarray.length-1) {
                     throw new AssertException("Invalid "+kind+" literal:"+s);
                 }  
                char ch=sarray[i+1];
                 switch(ch) {
                     case 'n':
                         sb.append('\n');
                         ++i;
                         break;
                     case 't':
                         sb.append('\t');
                         ++i;
                         break;
                     case 'b':
                         sb.append('\b');
                         ++i;
                         break;
                     case 'r':
                         sb.append('\r');
                         ++i;
                         break;
                     case 'f':
                         sb.append('\f');
                         ++i;
                         break;
                     case '\'':
                         sb.append('\'');
                         ++i;
                         break;
                     case '\"':
                         sb.append('\"');
                         ++i;
                         break;
                     case '\\':
                         sb.append('\\');
                         ++i;
                         break;
                     case '0':
                     case '1':
                     case '2':
                     case '3':
                     case '4':
                     case '5':
                     case '6':
                     case '7':
                     case '8':
                     case '9':
                     {
                         // read octal constant
                         ++i;                        
                         int sum=0;
                         int j=0;
                         while(Character.isDigit(ch) && j<3) {
                             sum=sum*8;
                             int q = Character.digit(ch,8);
                             sum=sum+q;
                             ++i;
                             if (i==sarray.length) {
                                 throw new AssertException("Invalid "+kind+" literal:"+s);
                             }
                             ++j;
                             ch=sarray[i];
                         }
                         ch = (char)sum;
                         --i;
                         sb.append(ch);
                     }
                     break;
                     case 'u':
                     {
                         // read unicode decimal constant
                         ++i;
                         if (i==sarray.length-1) {
                             throw new AssertException("Invalid "+kind+" literal:"+s);
                         }
                         ++i;
                         if (i==sarray.length-1) {
                             throw new AssertException("Invalid "+kind+" literal:"+s);
                         }                        
                         ch=sarray[i];
                         int sum=0;
                         int j=0;
                         while(Character.isDigit(ch) && j<4) {
                             sum=sum*16;
                             int q = Character.digit(ch,16);
                             sum+=q;
                             ++i;
                             if (i==sarray.length) {
                                 throw new AssertException("Invalid "+kind+" literal:"+s);
                             }
                             ++j;
                             ch=sarray[i];
                         }
                         ch=(char)sum;
                         --i;
                         sb.append(ch);
                     }
                     break;
                     default:
                         throw new AssertException("Invalid literal - unexpected symbol after \\:"+s);                        
                 }
               }
               break;
               default:
                   sb.append(sarray[i]);
                   break;
           }
       }
       return sb.toString();
     }
     
     static String codeStringOrCharLiteral(String s,char quote)
     {
       StringBuilder sb = new StringBuilder();
       sb.append(quote);
       char[] sarray = s.toCharArray();
       for(int i=0; i<sarray.length; ++i) {
           char ch = sarray[i];         
           switch(sarray[i]) {
               case '\n':
                   sb.append("\\");
                   sb.append("n");
                   break;
               case '\t':
                    sb.append("\\");
                    sb.append("t");
                    break;
               case '\b':
                   sb.append("\\");
                   sb.append("b");
                   break;
               case '\r':
                   sb.append('\\');
                   sb.append('r');
                   break;     
               case '\f':
                   sb.append('\\');
                   sb.append('f');
                   break;
               case '\'':
                   if (quote=='\'') {
                       sb.append('\\');
                       sb.append('\'');
                   }else{
                       sb.append(ch);
                   }
                   break;
               case '"':
                   if (quote=='"') {
                       sb.append('\\');
                       sb.append('"');
                   }else{
                       sb.append('"');
                   }
                   break;
               case '\\':
                   sb.append('\\');
                   sb.append('\\');
                   break;
               case ' ':
                   sb.append(' ');
                   break;
               case '.':
               case ',':
               case '<':
               case '>':
               case ':':
               case ';':
               case '~':
               case '`':
               case '@':
               case '#':
               case '$':
               case '%':
               case '^':
               case '&':
               case '*':
               case '(':
               case ')':
               case '_':
               case '-':
               case '+':
               case '=':
               case '{':
               case '}':
               case '[':
               case ']':
               case '|':
               case '/':
               case '?':
                   sb.append(ch);
                   break;              
               default:
                  if (ch < 20 && ch >0) {
                      String oct = ("\\0"+(ch/8))+(ch%8);
                      sb.append(oct);
                  } else if (Character.isLetterOrDigit(ch)) {
                       sb.append(ch);
                   }else{
                       String hex = byteToHex( (byte)(ch >>> 8))+byteToHex((byte)(ch & 0xff));
                       sb.append("\\u");
                       sb.append(ZEROS.substring(0,4-hex.length()));
                       sb.append(hex);
                   }                                    
           }
       }
       sb.append(quote);
       return sb.toString();
     }
     
     public static String codeStringLiteral(String s)
     { return codeStringOrCharLiteral(s,'"'); }
     
     public static String codeCharLiteral(char ch)
     { return codeStringOrCharLiteral(Character.toString(ch),'\''); }
 
     public static String byteToHex(byte b)
     {
         char[] array = { HEX_DIGITS[(b>>4)&0x0f], HEX_DIGITS[b & 0x0f] };
         return new String(array);
     }
     
     private static final String ZEROS = "0000";
     
     private static final char[] HEX_DIGITS={ '0','1','2','3','4','5','6','7','8','9','a',
                                            'b','c','d','e','f'};
     
     
     
 }
