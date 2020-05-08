 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2004-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 /*
  * Written by Jeff Dyer
  * Copyright (c) 1998-2003 Mountain View Compiler Company
  * All rights reserved.
  */
 
 package macromedia.asc.parser;
 import java.util.concurrent.*;
 import macromedia.asc.util.*;
 import macromedia.asc.embedding.ErrorConstants;
 
 import java.io.InputStream;
 import static macromedia.asc.parser.Tokens.*;
 import static macromedia.asc.parser.States.*;
 import static macromedia.asc.parser.CharacterClasses.*;
 import static macromedia.asc.embedding.avmplus.Features.*;
 
 /**
  * Partitions input character stream into tokens.
  *
  * @author Jeff Dyer
  */
 public final class Scanner implements ErrorConstants
 {
     private static final boolean debug = false;
 
     private static final int slashdiv_context = 0x1;
     private static final int slashregexp_context = 0x2;
 
     private ObjectList<Token> tokens;   // vector of token instances.
     private IntList slash_context = new IntList();  // slashdiv_context or slashregexp_context
     private boolean isFirstTokenOnLine;
     private boolean save_comments;
     private Context ctx;
 
     public InputBuffer input;
 
     private static final String[][] rsvd = {
         {"as"},
         {"break"},
         {"case","catch","class","const","continue"},
         {"default","delete","do"},
         {"else","extends"},
         {"false","finally","for","function"},
         {"get"},
         {""},//h
         {"if","implements","import","in","include","instanceof","interface","is"},
         {""},//j
         {""},//k
         {""},//l
         {""},//m
         {"namespace","new","null"},
         {""},//o
         {"package","private","protected","public"},
         {""},//q
         {"return"},
         {"set","super","switch"},
         {"this","throw","true","try","typeof"},
         {"use"},
         {"var","void"},
         {"while","with"},
         {""},//x
         {""},//y
         {""}//z
     };
     
     // ??? too fragile...come up with a better way
     
     private static final int[][] rsvd_token = {
         {AS_TOKEN},
         {BREAK_TOKEN},
         {CASE_TOKEN,CATCH_TOKEN,CLASS_TOKEN,CONST_TOKEN,CONTINUE_TOKEN},
         {DEFAULT_TOKEN,DELETE_TOKEN,DO_TOKEN},
         {ELSE_TOKEN,EXTENDS_TOKEN},
         {FALSE_TOKEN,FINALLY_TOKEN,FOR_TOKEN,FUNCTION_TOKEN},
         {GET_TOKEN},
         {},//h
         {IF_TOKEN,IMPLEMENTS_TOKEN,IMPORT_TOKEN,IN_TOKEN,INCLUDE_TOKEN,INSTANCEOF_TOKEN,INTERFACE_TOKEN,IS_TOKEN},
         {},//j
         {},//k
         {},//l
         {},//m
         {NAMESPACE_TOKEN,NEW_TOKEN,NULL_TOKEN},
         {},//o
         {PACKAGE_TOKEN,PRIVATE_TOKEN,PROTECTED_TOKEN,PUBLIC_TOKEN},
         {},//q
         {RETURN_TOKEN},
         {SET_TOKEN,SUPER_TOKEN,SWITCH_TOKEN},
         {THIS_TOKEN,THROW_TOKEN,TRUE_TOKEN,TRY_TOKEN,TYPEOF_TOKEN},
         {USE_TOKEN},
         {VAR_TOKEN,VOID_TOKEN},
         {WHILE_TOKEN,WITH_TOKEN},
         {},//x
         {},//y
         {}//z
     };
     
     private final int screen_rsvd()
     {
         char c;
         int row;
         int row_length;
         int text_length = input.markLength();
         int i,j;
         
         if ( text_length < 2 )
             return 0;
         
         c = input.markCharAt(0);
         assert(c >= 'a' && c <= 'z');
         row = (int) c - 'a';
         assert(row >= 0 && row < rsvd.length);
         row_length = rsvd[row].length;
         
         for (i = 0; i < row_length; i++ )
         {
             if ( rsvd[row][i].length() != text_length )
                 continue;
             
             for ( j = 1; j < text_length; j++ )
             {
                 c = input.markCharAt(j);
                 if ( c != rsvd[row][i].charAt(j) )
                     break;
             }
             if ( j == text_length )
                 return rsvd_token[row][i];
         }
         return 0;
     }
     
     private static final ConcurrentHashMap<String,Integer> reservedWord;
     
     static {
     	reservedWord = new ConcurrentHashMap<String,Integer>(64);
     	reservedWord.put("as",AS_TOKEN); // ??? predicated on HAS_ASOPERATOR
     	reservedWord.put("break",BREAK_TOKEN);
     	reservedWord.put("case",CASE_TOKEN);
     	reservedWord.put("catch",CATCH_TOKEN);
     	reservedWord.put("class",CLASS_TOKEN);
     	reservedWord.put("const",CONST_TOKEN);
     	reservedWord.put("continue",CONTINUE_TOKEN);
     	reservedWord.put("default",DEFAULT_TOKEN);
     	reservedWord.put("delete",DELETE_TOKEN);
     	reservedWord.put("do",DO_TOKEN);
     	reservedWord.put("else",ELSE_TOKEN);
     	reservedWord.put("extends",EXTENDS_TOKEN);
     	reservedWord.put("false",FALSE_TOKEN);
     	reservedWord.put("finally",FINALLY_TOKEN);
     	reservedWord.put("for",FOR_TOKEN);
     	reservedWord.put("function",FUNCTION_TOKEN);
     	reservedWord.put("get",GET_TOKEN);
     	reservedWord.put("if",IF_TOKEN);
     	reservedWord.put("implements",IMPLEMENTS_TOKEN);
     	reservedWord.put("import",IMPORT_TOKEN);
     	reservedWord.put("in",IN_TOKEN);
     	reservedWord.put("include",INCLUDE_TOKEN);  
     	reservedWord.put("instanceof",INSTANCEOF_TOKEN);
     	reservedWord.put("interface",INTERFACE_TOKEN);
     	reservedWord.put("is",IS_TOKEN); //??? predicated on HAS_ISOPERATOR
     	reservedWord.put("namespace",NAMESPACE_TOKEN);
     	reservedWord.put("new",NEW_TOKEN);
     	reservedWord.put("null",NULL_TOKEN);
     	reservedWord.put("package",PACKAGE_TOKEN);
     	reservedWord.put("private",PRIVATE_TOKEN);
     	reservedWord.put("protected",PROTECTED_TOKEN);
     	reservedWord.put("public",PUBLIC_TOKEN);
     	reservedWord.put("return",RETURN_TOKEN);
     	reservedWord.put("set",SET_TOKEN);
     	reservedWord.put("super",SUPER_TOKEN);
     	reservedWord.put("switch",SWITCH_TOKEN);
     	reservedWord.put("this",THIS_TOKEN);
     	reservedWord.put("throw",THROW_TOKEN);
     	reservedWord.put("true",TRUE_TOKEN);
     	reservedWord.put("try",TRY_TOKEN);
     	reservedWord.put("typeof",TYPEOF_TOKEN);
     	reservedWord.put("use",USE_TOKEN);
     	reservedWord.put("var",VAR_TOKEN);
     	reservedWord.put("void",VOID_TOKEN);
     	reservedWord.put("while",WHILE_TOKEN);
     	reservedWord.put("with",WITH_TOKEN);
     }
     
     /*
      * Scanner constructors.
      */
 
     private void init(Context cx, boolean save_comments)
     {
         ctx = cx;
         tokens = new ObjectList<Token>(2048);
         state = start_state;
         level = 0;
         slash_context.add(slashregexp_context);
         states = new IntList();
         levels = new IntList();
         slashcontexts = new ObjectList<IntList>();
         this.save_comments = save_comments;
     }
 
     
     public Scanner(Context cx, InputStream in, String encoding, String origin){this(cx,in,encoding,origin,true);}
     public Scanner(Context cx, InputStream in, String encoding, String origin, boolean save_comments)
     {
         init(cx,save_comments);
         this.input = new InputBuffer(in, encoding, origin);
         cx.input = this.input;
     }
     
     public Scanner(Context cx, String in, String origin){this(cx,in,origin,true);}
     public Scanner(Context cx, String in, String origin, boolean save_comments)
     {
         init(cx,save_comments);
         this.input = new InputBuffer(in, origin);
         cx.input = this.input;
     }
     
     /**
      * This contructor is used by Flex direct AST generation.  It
      * allows Flex to pass in a specialized InputBuffer.
      */
     
     public Scanner(Context cx, InputBuffer input)
     {
         init(cx,true);
         this.input = input;
         cx.input = input;
     }
 
     /**
      * nextchar() --just fetch the next char 
      */
 
     private char nextchar()
     {
         return (char) input.nextchar();
     }
   
     /*
      * retract() --
      * Causes one character of input to be 'put back' onto the
      * que. [Test whether this works for comments and white space.]
      */
 
     public void retract()
     {
         input.retract();
     }
 
     /**
      * @return +1 from current char pos in InputBuffer
      */
     
     private int pos()
     {
         return input.textPos();
     }
     
     /**
      * set mark position
      */
     private void mark()
     {
         input.textMark();
     }
     
     /*
      * Various helper methods for managing and testing the
      * scanning context for slashes.
      */
 
     public void enterSlashDivContext()
     {
         slash_context.add(slashdiv_context);
     }
 
     public void exitSlashDivContext()
     {
         slash_context.removeLast();
     }
 
     public void enterSlashRegExpContext()
     {
         slash_context.add(slashregexp_context);
     }
 
     public void exitSlashRegExpContext()
     {
         slash_context.removeLast();
     }
 
     public boolean isSlashDivContext()
     {
         return slash_context.last() == slashdiv_context;
     }
 
     public boolean isSlashRegexpContext()   // ???  this method is not used.
     {
         return slash_context.last() == slashregexp_context;
     }
 
     /*
      * makeTokenInstance() --
      * Make an instance of the specified token class using the lexeme string.
      * Return the index of the token which is its identifier.
      */
 
     private int makeTokenInstance(int token_class, String lexeme)
     {
         tokens.add(new Token(token_class, lexeme));
         return tokens.size() - 1; /* return the tokenid */
     }
     
     /*
      * getTokenClass() --
      * Get the class of a token instance.
      */
 
     public int getTokenClass(int token_id)
     {
         // if the token id is negative, it is a token_class.
         if (token_id < 0)
         {
             return token_id;
         }
 
         // otherwise, get instance data from the instance vector.
         return tokens.get(token_id).getTokenClass();
     }
 
     /*
      * getTokenText() --
      * Get the text of a token instance.
      */
 
     public String getTokenText(int token_id)
     {
         // if the token id is negative, it is a token_class.
         if (token_id < 0)
         {
             return Token.getTokenClassName(token_id);
         }
 
         // otherwise, get instance data from the instance vector.
         return tokens.get(token_id).getTokenText();
     }
 
     /*
      * getStringTokenText
      * Get text of literal string token as well as info about whether it was single quoted or not
      *
      */
     
     public String getStringTokenText( int token_id, boolean[] is_single_quoted )
     {
         // if the token id is negative, it is a token_class.
         if( token_id < 0 )
         {
             is_single_quoted[0] = false;
             return Token.getTokenClassName(token_id);
         }
 
         // otherwise, get tokenSourceText (which includes string delimiters)
         String fulltext = tokens.get( token_id ).getTokenSource();
         is_single_quoted[0] = (fulltext.charAt(0) == '\'' ? true : false);
         String enclosedText = fulltext.substring(1, fulltext.length() - 1);
         
         return enclosedText;
     }
 
     /*
      * Record an error.
      */
 
     private void error(int kind, String arg, int tokenid)
     {
         StringBuilder out = new StringBuilder();
 
         String origin = this.input.origin;
         
         int errPos = input.positionOfMark();    // note use of source adjusted position
         int ln  = input.getLnNum(errPos);
         int col = input.getColPos(errPos);
 
         String msg = (ContextStatics.useVerboseErrors ? "[Compiler] Error #" + kind + ": " : "") + ctx.errorString(kind);
         
         if(debug) 
         {
             msg = "[Scanner] " + msg;
         }
         
         int nextLoc = Context.replaceStringArg(out, msg, 0, arg);
         if (nextLoc != -1) // append msg remainder after replacement point, if any
             out.append(msg.substring(nextLoc, msg.length()));
 
         ctx.localizedError(origin,ln,col,out.toString(),input.getLineText(errPos), kind);
         skiperror(kind);
     }
 
     private void error(String msg)
     {
         ctx.internalError(msg);
         error(kError_Lexical_General, msg, ERROR_TOKEN);
     }
 
     private void error(int kind)
     {
         error(kind, "", ERROR_TOKEN);
     }
 
     /*
      * skip ahead after an error is detected. this simply goes until the next
      * whitespace or end of input.
      */
 
     private void skiperror()
     {
         skiperror(kError_Lexical_General);
     }
 
     private void skiperror(int kind)
     {
         //Debugger::trace("skipping error\n");
         switch (kind)
         {
             case kError_Lexical_General:
                 //while ( true )
                 //{
                 //    char nc = nextchar();
                 //    //Debugger::trace("nc " + nc);
                 //    if( nc == ' ' ||
                 //        nc == '\n' ||
                 //        nc == 0 )
                 //    {
                 //        return;
                 //    }
                 //}
                 return;
             case kError_Lexical_LineTerminatorInSingleQuotedStringLiteral:
             case kError_Lexical_LineTerminatorInDoubleQuotedStringLiteral:
                 while (true)
                 {
                     char nc = nextchar();
                     if (nc == '\'' || nc == 0)
                     {
                         return;
                     }
                 }
             case kError_Lexical_SyntaxError:
             default:
                 while (true)
                 {
                     char nc = nextchar();
                     if (nc == ';' || nc == '\n' || nc == '\r' || nc == 0)
                     {
                         return;
                     }
                 }
         }
     }
 
     /*
      *
      *
      */
 
     public boolean followsLineTerminator()
     {
         if (debug)
         {
             System.out.println("isFirstTokenOnLine = " + isFirstTokenOnLine);
         }
         return isFirstTokenOnLine;
     }
 
     /*
      *
      *
      */
 
     public int state;
     public int level;
 
     public IntList states;
     public IntList levels;
     public ObjectList<IntList> slashcontexts;
 
     public void pushState()
     {
         states.add(state);
         levels.add(level);
         IntList temp = new IntList(slash_context);
         slashcontexts.add(temp);
         state = start_state;
         level = 0;
         slash_context.clear();
         enterSlashRegExpContext();
     }
 
     public void popState()
     {
         exitSlashRegExpContext();  // only necessary to do the following assert
         if (slash_context.size() != 0)
         {
             assert(false); // throw "internal error";
         }
         state = states.removeLast();
         level = levels.removeLast();
         slash_context = slashcontexts.removeLast();
     }
 
     private StringBuilder getDocTextBuffer(String doctagname)
     {
         StringBuilder doctextbuf = new StringBuilder();
         doctextbuf.append("<").append(doctagname).append("><![CDATA[");
         return doctextbuf;
     }
 
     private String getXMLText(int begin, int end)
     {
         int len = (end-begin)+1; 
 
         String xmltext = null;
         if( len > 0 )
         {
 	        xmltext = input.copy(begin,end);
         }
         return xmltext;
     }
 
     public void clearUnusedBuffers() {
      //   input.clearUnusedBuffers();
         input = null;
     } 
     
     /*
      * 
      * 
      */
 
     public int nexttoken(boolean resetState)
     {
         String xmltagname = null, doctagname = "description";
         StringBuilder doctextbuf = null;
         int startofxml = pos();
         StringBuilder blockcommentbuf = null;
         char regexp_flags =0; // used to track option flags encountered in a regexp expression.  Initialized in regexp_state
         boolean maybe_reserved = false;
 
         if (resetState)
         {
             isFirstTokenOnLine = false;
         }
         
         while (true)
         {
             if (debug)
             {
                 System.out.println("state = " + state + ", next = " + pos());
             }
 
             switch (state)
             {
 
                 case start_state:
                     {
                         int c = nextchar();
                         mark();
                         
                         switch (c)
                         {
                         case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g': case 'h': case 'i': case 'j':
                         case 'k': case 'l': case 'm': case 'n': case 'o': case 'p': case 'q': case 'r': case 's': case 't':
                         case 'u': case 'v': case 'w': case 'x': case 'y': case 'z': 
                             maybe_reserved = true;
                         case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I': case 'J':
                         case 'K': case 'L': case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T':
                         case 'U': case 'V': case 'W': case 'X': case 'Y': case 'Z': 
                         case '_': case '$':
                             state = A_state;
                             continue;
                             
                             case 0xffffffef:
                                 state = utf8sig_state;
                                 continue;
                                 
                             case '@':
                                 return AMPERSAND_TOKEN;
                               
                             case '\'':
                             case '\"':
                             {
                                 char startquote = (char) c;
                                 boolean needs_escape = false;
 
                                 while ( (c=nextchar()) != startquote )
                                 {         
                                     if ( c == '\\' )
                                     {
                                         needs_escape = true;
                                         c = nextchar();
 
                                         // special case: escaped eol strips crlf or lf
                                          
                                         if ( c  == '\r' )
                                             c = nextchar();
                                         if ( c == '\n' )
                                             continue;
                                     }
                                     else if ( c == '\r' || c == '\n' )
                                     {
                                         if ( startquote == '\'' )
                                             error(kError_Lexical_LineTerminatorInSingleQuotedStringLiteral);
                                         else
                                             error(kError_Lexical_LineTerminatorInDoubleQuotedStringLiteral);
                                         break;
                                     }
                                     else if ( c == 0 )
                                     {
                                         error(kError_Lexical_EndOfStreamInStringLiteral);
                                         return EOS_TOKEN;
                                     }
                                 }
                                 return makeTokenInstance(STRINGLITERAL_TOKEN, input.copy(needs_escape));
                             }
 
                             case '-':   // tokens: -- -= -
                                 switch (nextchar())
                                 {
                                     case '-':
                                         return MINUSMINUS_TOKEN;
                                     case '=':
                                         return MINUSASSIGN_TOKEN;
                                     default:
                                         retract();
                                         return MINUS_TOKEN;
                                 }
                                 
                             case '!':   // tokens: ! != !===
                                 switch (nextchar())
                                 {
                                     case '=':
                                         switch (nextchar())
                                         {
                                             case '=':
                                                 return STRICTNOTEQUALS_TOKEN;
                                             default:
                                                 retract();
                                                 return NOTEQUALS_TOKEN;
                                         }
                                     default:
                                         retract();
                                         return NOT_TOKEN;
                                 }
                                 
                             case '%':   // tokens: % %=
                                 switch (nextchar())
                                 {
                                     case '=':
                                         return MODULUSASSIGN_TOKEN;
                                     default:
                                         retract();
                                         return MODULUS_TOKEN;
                                 }
                                 
                             case '&':   // tokens: & &= && &&=
                                 switch (nextchar())
                                 {
                                     case '&':
                                         switch (nextchar())
                                         {
                                             case '=':
                                                 return LOGICALANDASSIGN_TOKEN;
                                             default:
                                                 retract();
                                                 return LOGICALAND_TOKEN;
                                         }
                                     case '=':
                                         return BITWISEANDASSIGN_TOKEN;
                                     default:
                                         retract();
                                         return BITWISEAND_TOKEN;
                                 }
                                 
                             case '#':
                                 if (HAS_HASHPRAGMAS)
                                 {
                                     return USE_TOKEN;
                                 }
                                 else
                                 {
                                     state = error_state;
                                     continue;
                                 }  // # is short for use
                                 
                             case '(':
                                 return LEFTPAREN_TOKEN;
                                 
                             case ')':
                                 return RIGHTPAREN_TOKEN;
                                 
                             case '*':   // tokens: *=  *
                                 switch (nextchar())
                                 {
                                     case '=':
                                         return MULTASSIGN_TOKEN;
                                     default:
                                         retract();
                                         return MULT_TOKEN;
                                 }
 
                             case ',':
                                 return COMMA_TOKEN;
                                 
                             case '.':
                                 state = dot_state;
                                 continue;
                                 
                             case '/':
                                 state = slash_state;
                                 continue;
 
                             case ':':   // tokens: : ::
                                 switch (nextchar())
                                 {
                                     case ':':
                                         return DOUBLECOLON_TOKEN;
                                     default:
                                         retract();
                                         return COLON_TOKEN;
                                 }
                              
                             case ';':
                                 return SEMICOLON_TOKEN;
                                 
                             case '?':
                                 return QUESTIONMARK_TOKEN;
                                 
                             case '[':
                                 return LEFTBRACKET_TOKEN;
                                 
                             case ']':
                                 return RIGHTBRACKET_TOKEN;
                                 
                             case '^':
                                 state = bitwisexor_state;
                                 continue;
                                 
                             case '{':
                                 return LEFTBRACE_TOKEN;
                                 
                             case '|':   // tokens: | |= || ||=
                                 switch (nextchar())
                                 {
                                     case '|':
                                         switch (nextchar())
                                         {
                                             case '=':
                                                 return LOGICALORASSIGN_TOKEN;
                                             default:
                                                 retract();
                                                 return LOGICALOR_TOKEN;
                                         }
                                     case '=':
                                         return BITWISEORASSIGN_TOKEN;
                                     default:
                                         retract();
                                         return BITWISEOR_TOKEN;
                                 }
                                 
                             case '}':
                                 return RIGHTBRACE_TOKEN;
                                 
                             case '~':
                                 return BITWISENOT_TOKEN;
                                 
                             case '+':   // tokens: ++ += +
                                 switch (nextchar())
                                 {
                                     case '+':
                                         return PLUSPLUS_TOKEN;
                                     case '=':
                                         return PLUSASSIGN_TOKEN;
                                     default:
                                         retract();
                                         return PLUS_TOKEN;
                                 }
                                 
                             case '<':
                                 state = lessthan_state;
                                 continue;
                                 
                             case '=':   // tokens: === == =
                                 switch (nextchar())
                                 {
                                     case '=':                            
                                         switch (nextchar())
                                         {
                                             case '=':
                                                 return STRICTEQUALS_TOKEN;
                                             default:
                                                 retract();
                                                 return EQUALS_TOKEN;
                                         }
                                     default:
                                         retract();
                                         return ASSIGN_TOKEN;
                                 }
                                 
                             case '>':
                                 state = greaterthan_state;
                                 continue;            
                                 
                             case '0':
                                 state = zero_state;
                                 continue;
                                 
                             case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                                 state = decimalinteger_state;
                                 continue;
                                 
                             case ' ': // White space
                            case '\t':
                             case 0x000b:
                             case 0x000c:
                             case 0x00a0:
                                 continue;
                                 
                             case '\n': // Line terminators.
                             case '\r':
                             case 0x2028:
                             case 0x2029:
                                 isFirstTokenOnLine = true;
                                 continue;
                                 
                             case 0:
                                 return EOS_TOKEN;
                                 
                             default:
                                 switch (input.classOfNext())
                                 {
                                     case Lu:
                                     case Ll:
                                     case Lt:
                                     case Lm:
                                     case Lo:
                                     case Nl:
                                         maybe_reserved = false;
                                         state = A_state;
                                         continue;
                                     default:
                                         state = error_state;
                                         continue;
                                 }
                         }
                     }
 
                 /*
                  * prefix: <letter>
                  */
 
                 case A_state:
                
                     while ( true ){
                         int c = nextchar();
                         if ( c >= 'a' && c <= 'z' )
                         {
                             continue;
                         }
                         if ( (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '$' || c == '_' ){
                             maybe_reserved = false;
                             continue;
                         }
                         if ( c == 0 )
                         {
                             break;
                         }
 
                         switch (input.classOfNext())
                         {
                         case Lu: case Ll: case Lt: case Lm: case Lo: case Nl: case Mn: case Mc: case Nd: case Pc:
                             maybe_reserved = false;
                             continue;
                         }
                         break;
                     }
                     retract();
                     state = start_state;   
                     String s = input.copy(); 
                     if ( maybe_reserved )
                     {
                         Integer i = reservedWord.get(s); 
                         if ( i != null )
                             return (int) i;
 
                         //int r = screen_rsvd();
                         //if ( r != 0 )
                         //    return r;
                     }
                     //String s = input.copy(); 
                     return makeTokenInstance(IDENTIFIER_TOKEN,s);
                
                 /*
                  * prefix: 0
                  * accepts: 0x... | 0X... | 01... | 0... | 0
                  */
 
                 case zero_state:
                     switch (nextchar())
                     {
                         case 'x':
                         case 'X':
                             switch(nextchar())
                             {
                             case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                             case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'A': case 'B': case 'C': case 'D': 
                             case 'E': case 'F':
                                 state = hexinteger_state;
                                 break;
                             default:
                                 state = start_state;
                                 error(kError_Lexical_General); 
                             }
                             continue;
   
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                         case '.':
                             state = decimalinteger_state;
                             continue;
                         case 'E':
                         case 'e':
                             state = exponentstart_state;
                             continue;
                         case 'd':
                         case 'm':
                         case 'i':
                         case 'u':
                         	if (!ctx.statics.es4_numerics)
                         		retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                         default:
                             retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                     }
 
                     /*
                      * prefix: 0x<hex digits>
                      * accepts: 0x123f
                      */
 
                 case hexinteger_state:
                     switch (nextchar())
                     {
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                         case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'A': case 'B': case 'C': case 'D': 
                         case 'E': case 'F':
                             state = hexinteger_state;
                             continue;
                         case 'u':
                         case 'i':
                         	if (!ctx.statics.es4_numerics)
                         		retract();
                             state = start_state; 
                             return makeTokenInstance( NUMBERLITERAL_TOKEN, input.copy() );
                         default:  
                             retract();
                             state = start_state; 
                             return makeTokenInstance( NUMBERLITERAL_TOKEN, input.copy() );
                     }
 
                     /*
                      * prefix: .
                      * accepts: .123 | .
                      */
 
                 case dot_state:
                     switch (nextchar())
                     {
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                             state = decimal_state;
                             continue;
                         case '.':
                             state = doubledot_state;
                             continue;
                         case '<':
                             state = start_state;
                             return DOTLESSTHAN_TOKEN;
                         default:
                             retract();
                             state = start_state;
                             return DOT_TOKEN;
                     }
 
                     /*
                      * accepts: ..
                      */
 
                 case doubledot_state:
                     state = start_state;
                     if ( nextchar() == '.' )
                             return TRIPLEDOT_TOKEN;
 
                     retract();
                     return DOUBLEDOT_TOKEN;
 
                     /*
                      * prefix: N
                      * accepts: 0.123 | 1.23 | 123 | 1e23 | 1e-23
                      */
 
                 case decimalinteger_state:
                     switch (nextchar())
                     {
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                             state = decimalinteger_state;
                             continue;
                         case '.':
                             state = decimal_state;
                             continue;
                         case 'd':
                         case 'm':
                         case 'u':
                         case 'i':
                         	if (!ctx.statics.es4_numerics)
                         		retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                         case 'E':
                         case 'e':
                             state = exponentstart_state;
                             continue;
                         default:
                             retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                     }
 
                     /*
                      * prefix: N.
                      * accepts: 0.1 | 1e23 | 1e-23
                      */
 
                 case decimal_state:
                     switch (nextchar())
                     {
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                             state = decimal_state;
                             continue;
                         case 'd':
                         case 'm':
                         	if (!ctx.statics.es4_numerics)
                         		retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                         case 'E':
                         case 'e':
                             state = exponentstart_state;
                             continue;
                         default:
                             retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                     }
 
                     /*
                      * prefix: ..e
                      * accepts: ..eN | ..e+N | ..e-N
                      */
 
                 case exponentstart_state:
                     switch (nextchar())
                     {
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                         case '+':
                         case '-':
                             state = exponent_state;
                             continue;
                         default:
                             error(kError_Lexical_General);
                             state = start_state;
                             continue;
                             // Issue: needs specific error here.
                     }
 
                     /*
                      * prefix: ..e
                      * accepts: ..eN | ..e+N | ..e-N
                      */
 
                 case exponent_state:
                     switch (nextchar())
                     {
                         case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': 
                             state = exponent_state;
                             continue;
                    		case 'd':
                    		case 'm':
                         	if (!ctx.statics.es4_numerics)
                         		retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                         default:
                             retract();
                             state = start_state;
                             return makeTokenInstance(NUMBERLITERAL_TOKEN, input.copy());
                     }
 
                     /*
                      * prefix: /
                      */
 
                 case slash_state:
                     switch (nextchar())
                     {
                         case '/':
                             if (blockcommentbuf == null) blockcommentbuf = new StringBuilder();
                             state = linecomment_state;
                             continue;
                         case '*':
                             if (blockcommentbuf == null) blockcommentbuf = new StringBuilder();
                             blockcommentbuf.append("/*");
                             state = blockcommentstart_state;
                             continue;
                         default:
                         {
                             retract(); // since we didn't use the current character for this decision.
                             if (isSlashDivContext())
                             {
                                 state = slashdiv_state;
                             }
                             else
                             {
                                 state = slashregexp_state;
                             }
                             continue;
                         }
                     }
 
                     /*
                      * tokens: / /=
                      */
 
                 case slashdiv_state:
                     state = start_state; 
                     switch (nextchar())
                     {
                         case '>':
                             return XMLTAGENDEND_TOKEN;
                         case '=':
                             return DIVASSIGN_TOKEN;
                         default:
                             retract();
                             return DIV_TOKEN;
                     }
 
                     /*
                      * tokens: /<regexpbody>/<regexpflags>
                      */
 
                 case slashregexp_state:
                     switch (nextchar())
                     {
                         case '\\': 
                             nextchar(); 
                             continue;
                         case '/':
                             regexp_flags = 0;
                             state = regexp_state;
                             continue;
                         case 0:
                         case '\n':
                         case '\r':
                             error(kError_Lexical_General);
                             state = start_state;
                             continue;
                         default:
                             state = slashregexp_state;
                             continue;
                     }
 
                 /*
                 * tokens: g | i | m | s | x  .  Note that s and x are custom extentions to match perl's functionality
                 *   Also note we handle this via an array of boolean flags intead of state change logic.
                 *   (5,1) + (5,2) + (5,3) + (5,4) + (5,5) is just too many states to handle this via state logic
                 */
 
                 case regexp_state:
                 switch ( nextchar() )
                 {
                     case 'g': 
                         if ((regexp_flags & 0x01) == 0)
                         {
                             regexp_flags |= 0x01;
                             continue;
                         }
                         error(kError_Lexical_General); 
                         state = start_state; 
                         continue;
 
                     case 'i': 
                         if ((regexp_flags & 0x02) == 0)
                         {
                             regexp_flags |= 0x02;
                             continue;
                         }
                         error(kError_Lexical_General); 
                         state = start_state; 
                         continue;
 
                     case 'm': 
                         if ((regexp_flags & 0x04) == 0)
                         {
                             regexp_flags |= 0x04;
                             continue;
                         }
                         error(kError_Lexical_General); 
                         state = start_state; 
                         continue;
 
                     case 's':
                         if ((regexp_flags & 0x08) == 0)
                         {
                             regexp_flags |= 0x08;
                             continue;
                         }
                         error(kError_Lexical_General); 
                         state = start_state; 
                         continue;
 
                     case 'x':
                         if ((regexp_flags & 0x10) == 0)
                         {
                             regexp_flags |= 0x10;
                             continue;
                         }
                         error(kError_Lexical_General); 
                         state = start_state; 
                         continue;
 
                     case 'A': 
                     case 'a': 
                     case 'B': 
                     case 'b': 
                     case 'C': 
                     case 'c': 
                     case 'D': 
                     case 'd': 
                     case 'E': 
                     case 'e': 
                     case 'F': 
                     case 'f':
                     case 'G': 
                     case 'H': 
                     case 'h': 
                     case 'I':
                     case 'J': 
                     case 'j': 
                     case 'K': 
                     case 'k': 
                     case 'L': 
                     case 'l': 
                     case 'M': 
                     case 'N': 
                     case 'n': 
                     case 'O': 
                     case 'o':
                     case 'P': 
                     case 'p': 
                     case 'Q': 
                     case 'q': 
                     case 'R': 
                     case 'r': 
                     case 'S': 
                     case 'T': 
                     case 't': 
                     case 'U': 
                     case 'u': 
                     case 'V': 
                     case 'v': 
                     case 'W': 
                     case 'w': 
                     case 'X': 
                     case 'Y': 
                     case 'y': 
                     case 'Z': 
                     case 'z': 
                     case '$': 
                     case '_':
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
                         error(kError_Lexical_General); 
                         state = start_state; 
                         continue;
                         
                     default: 
                         retract(); 
                         state = start_state; 
                         return makeTokenInstance( REGEXPLITERAL_TOKEN, input.copy(false) );
                 }
                     /*
                      * tokens: ^^ ^^= ^=  ^
                      */
 
                 case bitwisexor_state:
                     switch (nextchar())
                     {
                         case '=':
                             state = start_state;
                             return BITWISEXORASSIGN_TOKEN;
 /* not yet supported
                         case '^':
                             state = logicalxor_state;
                             continue;
 */
                         default:
                             retract();
                             state = start_state;
                             return BITWISEXOR_TOKEN;
                     }
 
                     /*
                      * tokens: ^^ ^=  ^
                      */
 
                 case logicalxor_state:
                     switch (nextchar())
                     {
                         case '=':
                             state = start_state;
                             return LOGICALXORASSIGN_TOKEN;
                         default:
                             retract();
                             state = start_state;
                             return LOGICALXOR_TOKEN;
                     }
 
                     /*
                      * prefix: <
                      */
 
                 case lessthan_state:
                     if( isSlashDivContext() )
                     {
                         switch (nextchar())
                         {
                             case '<':
                                 state = leftshift_state;
                                 continue;
                             case '=':
                                 state = start_state;
                                 return LESSTHANOREQUALS_TOKEN;
                             case '/': 
                                 state = start_state; 
                                 return XMLTAGSTARTEND_TOKEN;
                             case '!': 
                                 state = xmlcommentorcdatastart_state; 
                                 continue;
                             case '?': 
                                 state = xmlpi_state; 
                                 continue;                            
                             default:
                                 retract();
                                 state = start_state;
                                 return LESSTHAN_TOKEN;
                         }
                     }
                     else
                     {
                         switch ( nextchar() )             
                         {
                             case '/': 
                                 state = start_state; 
                                 return XMLTAGSTARTEND_TOKEN;
                             case '!': 
                                 state = xmlcommentorcdatastart_state; 
                                 continue;
                             case '?': 
                                 state = xmlpi_state; 
                                 continue;
                             default:  
                                 retract(); 
                                 state = start_state; 
                                 return LESSTHAN_TOKEN;
                         }                          
                     }
 
                 /*
                  * prefix: <!
                  */
                     
                 case xmlcommentorcdatastart_state:
                     switch ( nextchar() )        
                     {
                         case '[':  state = xmlcdatastart_state; continue;
                         case '-':  state = xmlcommentstart_state; continue;
                         default:    error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlcdatastart_state:
                     switch ( nextchar() )        
                     {
                         case 'C':  state = xmlcdatac_state; continue;
                         default:    error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlcdatac_state:
                     switch ( nextchar() )          
                     {
                         case 'D':  state = xmlcdatacd_state; continue;
                         default:    error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlcdatacd_state:
                     switch ( nextchar() )        
                     {
                         case 'A':  state = xmlcdatacda_state; continue;
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlcdatacda_state:
                     switch ( nextchar() )       
                     {
                         case 'T':  state = xmlcdatacdat_state; continue;
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlcdatacdat_state:
                     switch ( nextchar() )          
                     {
                         case 'A':  state = xmlcdatacdata_state; continue;
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
                 case xmlcdatacdata_state:
                     switch ( nextchar() )          
                     {
                         case '[':  state = xmlcdata_state; continue;
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
                 case xmlcdata_state:
                     switch ( nextchar() )         
                     {
                         case ']':  state = xmlcdataendstart_state; continue;
                         case 0:   error(kError_Lexical_General); state = start_state; continue;
                         default:   state = xmlcdata_state; continue;
                     }
                 case xmlcdataendstart_state:
                     switch ( nextchar() )          
                     {
                         case ']':  state = xmlcdataend_state; continue;
                         default:   state = xmlcdata_state; continue;
                     }
 
                 case xmlcdataend_state:
                     switch ( nextchar() )         
                     {
                         case '>':  
                         {
                             state = start_state;
                             return makeTokenInstance(XMLMARKUP_TOKEN,getXMLText(startofxml,pos()-1));
                         }
                         default:   state = xmlcdata_state; continue;
                     }
                 case xmlcommentstart_state:
                     switch ( nextchar() )        
                     {
                         case '-':  state = xmlcomment_state; continue;
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlcomment_state:
                     switch ( nextchar() )
                     {
                         case '-':  state = xmlcommentendstart_state; continue;
                         case 0:   error(kError_Lexical_General); state = start_state; continue;
                         default:   state = xmlcomment_state; continue;
                     }
 
                 case xmlcommentendstart_state:
                     switch ( nextchar() )
                     {
                         case '-':  state = xmlcommentend_state; continue;
                         default:   state = xmlcomment_state; continue;
                     }
 
                 case xmlcommentend_state:
                     switch ( nextchar() )  
                     {
                         case '>':  
                         {
                             state = start_state;
                             return makeTokenInstance(XMLMARKUP_TOKEN,getXMLText(startofxml,pos()-1));
                         }
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
 
                 case xmlpi_state:
                     switch ( nextchar() )
                     {
                         case '?':  state = xmlpiend_state; continue;
                         case 0:   error(kError_Lexical_General); state = start_state; continue;
                         default:   state = xmlpi_state; continue;
                     }
 
                 case xmlpiend_state:
                     switch ( nextchar() )
                     {
                         case '>':  
                         {
                             state = start_state;
                             return makeTokenInstance(XMLMARKUP_TOKEN,getXMLText(startofxml,pos()-1));
                         }
                         default:   error(kError_Lexical_General); state = start_state; continue;
                     }
                 case xmltext_state:
                 { 
                     switch(nextchar())
                     {
                         case '<': case '{':  
                         {
                             retract();
                             String xmltext = getXMLText(startofxml,pos()-1);
                             if( xmltext != null )
                             {
                                 state = start_state;
                                 return makeTokenInstance(XMLTEXT_TOKEN,xmltext);
                             }
                             else  // if there is no leading text, then just return puncutation token to avoid empty text tokens
                             {
                                 switch(nextchar()) 
                                 {
                                     case '<': 
                                     switch( nextchar() )
                                     {
                                         case '/': state = start_state; return XMLTAGSTARTEND_TOKEN;
                                         case '!': state = xmlcommentorcdatastart_state; continue;
                                         case '?': state = xmlpi_state; continue;
                                         default: retract(); state = start_state; return LESSTHAN_TOKEN;
                                     }
                                     case '{': 
                                         state = start_state; 
                                         return LEFTBRACE_TOKEN;
                                 }
                             }
                         }
                         case 0:   
                             state = start_state; 
                             return EOS_TOKEN;
                             
                         default:  
                             state = xmltext_state; 
                         continue;
                     }
                 }
 
                 case xmlliteral_state:
                     switch (nextchar())
                     {
                         case '{':  // return XMLPART_TOKEN
                             {
 	                            String xmltext = input.copy(startofxml, pos()-2);
                                 return makeTokenInstance(XMLPART_TOKEN, xmltext);
                             }
                         case '<':
                             switch (nextchar())
                             {
                                 case '/':
                                     --level;
                                     nextchar();
                                     mark();
                                     retract();
                                     state = endxmlname_state;
                                     continue;
                                 default:
                                     ++level;
                                     state = xmlliteral_state;
                                     continue;
                             }
                         case '/':
                             {
                                 switch (nextchar())
                                 {
                                     case '>':
                                         {
                                             --level;
                                             if (level == 0)
                                             {
 	                                            String xmltext = input.copy(startofxml, pos());
                                                 state = start_state;
                                                 return makeTokenInstance(XMLLITERAL_TOKEN, xmltext);
                                             }
                                             // otherwise continue
                                             state = xmlliteral_state;
                                             continue;
                                         }
                                     default: /*error(kError_Lexical_General);*/
                                         state = xmlliteral_state;
                                         continue; // keep going anyway
                                 }
                             }
                         case 0:
                             retract();
                             error(kError_Lexical_NoMatchingTag);
                             state = start_state;
                             continue;
                             
                         default:
                             continue;
                     }
 
                 case endxmlname_state:  // scan name and compare it to start name
                     switch (nextchar())
                     {
                         case 'A': case 'a':
                         case 'B': case 'b':
                         case 'C': case 'c':
                         case 'D': case 'd':
                         case 'E': case 'e':
                         case 'F': case 'f':
                         case 'G':
                         case 'g':
                         case 'H':
                         case 'h':
                         case 'I':
                         case 'i':
                         case 'J':
                         case 'j':
                         case 'K':
                         case 'k':
                         case 'L':
                         case 'l':
                         case 'M':
                         case 'm':
                         case 'N':
                         case 'n':
                         case 'O':
                         case 'o':
                         case 'P':
                         case 'p':
                         case 'Q':
                         case 'q':
                         case 'R':
                         case 'r':
                         case 'S':
                         case 's':
                         case 'T':
                         case 't':
                         case 'U':
                         case 'u':
                         case 'V':
                         case 'v':
                         case 'W':
                         case 'w':
                         case 'X':
                         case 'x':
                         case 'Y':
                         case 'y':
                         case 'Z':
                         case 'z':
                         case '$':
                         case '_':
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
                         case ':':
                             {
                                 // stop looking for matching tag if the names diverge
                                 String temp = input.copy();
                                 if (xmltagname != null && xmltagname.indexOf(temp) == -1)
                                 {
                                     state = xmlliteral_state;
                                     //level -= 2;
                                 }
                                 else
                                 {
                                     state = endxmlname_state;
                                 }
                                 continue;
                             }
                         case '{':  // return XMLPART_TOKEN
                             {
                                 if (xmltagname != null)  // clear xmltagname since there is an expression in it.
                                 {
                                     xmltagname = null;
                                 }
 	                            String xmltext = input.copy(startofxml, pos()-2);
                                 return makeTokenInstance(XMLPART_TOKEN, xmltext);
                             }
                         case '>':
                             {
                                 retract();
                                 String temp = input.copy();
                                 nextchar();
                                 if (level == 0)
                                 {
                                     if (xmltagname != null)
                                     {
                                         if (temp.equals(xmltagname))
                                         {
 	                                        String xmltext = input.copy(startofxml, pos());
                                             state = start_state;
                                             return makeTokenInstance(XMLLITERAL_TOKEN, xmltext);
                                         }
                                     }
                                     else
                                     {
 	                                    String xmltext = input.copy(startofxml, pos());
                                         state = start_state;
                                         return makeTokenInstance(XMLLITERAL_TOKEN, xmltext);
                                     }
                                 }
                                 state = xmlliteral_state;
                                 continue;
                             }
                         default:
                             state = xmlliteral_state;
                             continue;
                     }
 
                     /*
                      * tokens: <<=  <<
                      */
 
                 case leftshift_state:
                     switch (nextchar())
                     {
                         case '=':
                             state = start_state;
                             return LEFTSHIFTASSIGN_TOKEN;
                         default:
                             retract();
                             state = start_state;
                             return LEFTSHIFT_TOKEN;
                     }
 
                     /*
                      * prefix: >
                      */
 
                 case greaterthan_state:
                     if( isSlashDivContext() )       
                     {
                         switch ( nextchar() )          
                         {
                             case '>': state = rightshift_state; break;
                             case '=': state = start_state; return GREATERTHANOREQUALS_TOKEN;
                             //default:  retract(); state = start_state; return greaterthan_token;
                             default:  retract(); state = start_state; return GREATERTHAN_TOKEN;
                         }
                     }
                     else      
                     {
                         state = start_state; 
                         return GREATERTHAN_TOKEN;
                     }
 
                     /*
                      * prefix: >> >>> >>>=
                      */
 
                 case rightshift_state:
                     state = start_state;
                     switch (nextchar())
                     {
                         case '>':
                             switch (nextchar())
                             {
                                 case '=':
                                     return UNSIGNEDRIGHTSHIFTASSIGN_TOKEN;
                                 default:
                                     retract();
                                     return UNSIGNEDRIGHTSHIFT_TOKEN;
                             }
                         case '=':
                             return RIGHTSHIFTASSIGN_TOKEN;
                         default:
                             retract();
                             return RIGHTSHIFT_TOKEN;
                     }
                     
                /*
                 * prefix: /*
                 */
 
                 case blockcommentstart_state:
                 {
                     int c = nextchar();
                     blockcommentbuf.append(c);
                     switch ( c )
                     {
                     case '*':
                         if ( nextchar() == '/' ){
                             state = start_state;
                             return makeTokenInstance( BLOCKCOMMENT_TOKEN, new String());
                         }
                         retract(); 
                         state = doccomment_state; 
                         continue;
                         
                     case 0:    
                         error(kError_BlockCommentNotTerminated); 
                         state = start_state; 
                         continue;
                         
                     case '\n': 
                     case '\r':
                         isFirstTokenOnLine = true; 
                     default:
                         state = blockcomment_state;
                         continue;
                     }
                 }
 
                 /*
                  * prefix: /**
                  */
 
                 case doccomment_state:
                 {
                     int c = nextchar();
                     blockcommentbuf.append(c);
                     switch ( c )
                     {
                     case '*':  state = doccommentstar_state; continue;
                     case '@':
                         if (doctextbuf == null) doctextbuf = getDocTextBuffer(doctagname);
                         if( doctagname.length() > 0 ) { doctextbuf.append("]]></").append(doctagname).append(">"); };
                         doctagname = "";
                         state = doccommenttag_state; continue;
                     case '\r': case '\n': isFirstTokenOnLine = true;
                     if (doctextbuf == null) doctextbuf = getDocTextBuffer(doctagname);
                     doctextbuf.append('\n');
                     state = doccomment_state; continue;
                     case 0:    error(kError_BlockCommentNotTerminated); state = start_state; continue;
                     default:
                         if (doctextbuf == null) doctextbuf = getDocTextBuffer(doctagname);
                     doctextbuf.append((char)(c)); state = doccomment_state; continue;
                     }
                 }
 
                 case doccommentstar_state:
                 {
                     int c = nextchar();
                     blockcommentbuf.append(c);
                     switch ( c )                    
                     {
                         case '/':
                             {
                             if (doctextbuf == null) doctextbuf = getDocTextBuffer(doctagname);
                             if( doctagname.length() > 0 ) { doctextbuf.append("]]></").append(doctagname).append(">"); };
                             String doctext = doctextbuf.toString();
                             state = start_state; return makeTokenInstance(DOCCOMMENT_TOKEN,doctext);
                         }
                         case '*':  state = doccommentstar_state; continue;
                         case 0:    error(kError_BlockCommentNotTerminated); state = start_state; continue;
                         default:   state = doccomment_state; continue;
                             // if not a slash, then keep looking for an end comment.
                     }
                 }
 
                 /*
                 * prefix: @
                 */
 
                 case doccommenttag_state:
                     {
                     int c = nextchar();
                     switch ( c )
                     {
                         case '*':  state = doccommentstar_state; continue;
                        case ' ': case '\t': case '\r': case '\n': 
                             {
                             if (doctextbuf == null) doctextbuf = getDocTextBuffer(doctagname);
                             if( doctagname.length() > 0 ) { doctextbuf.append("\n<").append(doctagname).append("><![CDATA["); };
                             state = doccomment_state; continue;
                         }
                         case 0:    error(kError_BlockCommentNotTerminated); state = start_state; continue;
                         default:   doctagname += (char)(c); state = doccommenttag_state; continue;
                     }
                 }
 
                 /*
                 * prefix: /**
                 */
 
                 case doccommentvalue_state:
                 switch ( nextchar() )
                 {
                     case '*':  state = doccommentstar_state; continue;
                     case '@':  state = doccommenttag_state; continue;
                     case '\r': case '\n': state = doccomment_state; continue;
                     case 0:    error(kError_BlockCommentNotTerminated); state = start_state; continue;
                     default:   state = doccomment_state; continue;
                 }
 
                 /*
                 * prefix: /*
                 */
 
                 case blockcomment_state:
                 {
                     int c = nextchar();
                     blockcommentbuf.append(c);
                     switch ( c )                    
                     {
                         case '*':  state = blockcommentstar_state; continue;
                         case '\r': case '\n': isFirstTokenOnLine = true; 
                             state = blockcomment_state; continue;
                         case 0:    error(kError_BlockCommentNotTerminated); state = start_state; continue;
                         default:   state = blockcomment_state; continue;
                     }
                 }
 
                 case blockcommentstar_state:
                 {
                     int c = nextchar();
                     blockcommentbuf.append(c);
                     switch ( c )
                     {
                         case '/':  
                         {
                             state = start_state;
                             String blocktext = blockcommentbuf.toString();
 
                             return makeTokenInstance( BLOCKCOMMENT_TOKEN, blocktext );
                         }
                         case '*':  state = blockcommentstar_state; continue;
                         case 0:    error(kError_BlockCommentNotTerminated); state = start_state; continue;
                         default:   state = blockcomment_state; continue;
                             // if not a slash, then keep looking for an end comment.
                     }
                 }
 
                 /*
                 * prefix: // <comment chars>
                 */
 
                 case linecomment_state:
                 {
                     state = start_state;
                     end_of_comment: while (true)
                     {
                         int c = nextchar();
                         switch ( c )
                         {
                         case '\r': case '\n': // don't include newline in line comment. (Sec 7.3)
                             retract(); 
                             if ( save_comments == false )
                                 break end_of_comment;
                             return makeTokenInstance( SLASHSLASHCOMMENT_TOKEN, input.copy() );
 
                         case 0:    
                             return EOS_TOKEN;
                         }
                     }
                     continue;
                 }
               
                 /*
                 * utf8sigstart_state
                 */
 
                 case utf8sig_state:
                     switch (nextchar())
                     {
                         case (char) 0xffffffbb:
                             {
                                 switch (nextchar())
                                 {
                                     case (char) 0xffffffbf:
                                         // ISSUE: set encoding scheme to utf-8, and implement support for utf8
                                         state = start_state;
                                         continue; // and contine
                                 }
                             }
                     }
                     state = error_state;
                     continue;
 
                 /*
                  * skip error
                  */
 
                 case error_state:
                     error(kError_Lexical_General);
                     skiperror();
                     state = start_state;
                     continue;
 
                 default:
                     error("invalid scanner state");
                     state = start_state;
                     return EOS_TOKEN;
 
             }
         }
     }
 }
