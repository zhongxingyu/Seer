 import java.io.InputStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Lexer {
 	String SPERATORSET = "[:;()+-/=><~\n\f\r\t /*]";
     Pattern SPERATOR = Pattern.compile(SPERATORSET);
 	public static int token;
 	public static String yylval;;
 
 	private InputStream in;
 	private static int c;
     public static int line = 1;
 	
 
 	@SuppressWarnings("unused")
 	private Lexer() {};
 
 	public Lexer(InputStream in) {
 		this.in = in;
 	};
 	
 	
 	/* 
 	 * is the current char among punctuation characters 
 	 */
 	boolean isPunctuation(){
 		String s = "";
 		if (c < 0)
 			return false;
 		s += (char)c;
		Matcher matcher = SPERATOR.matcher(s);
 		return matcher.matches();
 	}
 	/*
 	 * called for saved the char into the yyval
 	 * it will append the current char to  the yylval, and read next char 
 	 */
     private void getChar(){
 		if (c >= 0) {
 			try {
 				yylval += (char)c;
 				c = in.read();	
 			} catch (Exception e) {
 				c = (-1);
 			}
 		}
     }
 	
     /*
      * read next char
      */
 	public void nextChar() {
 		if (c >= 0) {
 			try {
 				c = in.read();
 			} catch (Exception e) {
 				c = (-1);
 			}
 		}
 	}
 	
 	private void yyerror(String s){
 		System.err.println("at line "+ line + ": " + s);
 		System.exit(-1);
 	}
 
 	
 	private void ContinueGetInteger(){
 		do {
 			String s = "";
 			if (c < 0)
 				break;
 			char tmp = (char)c;
 			s += tmp;
			Matcher matcher = SPERATOR.matcher(s);
 			if (matcher.matches() == true)
 				break;
 			if (!(tmp>='0' && tmp<='9'))
				yyerror("get an integer, here should be a digit, but here is " + tmp);
 			getChar();
 		}while(true);
 	}
 	
 	/*
 	 * get a ID started with the key words
 	 */
 	private void ContinueGetID(){
 		do {
 			String s = "";
 			if (c < 0)
 				break;
 			char tmp = (char)c;
 			s += tmp;
			Matcher matcher = SPERATOR.matcher(s);
 			if (matcher.matches() == true)
 				break;
 			if (!((tmp >= 'a' && tmp <= 'z') ||(tmp>='0' && tmp<='9')))
				yyerror("get an ID, here should be a valid char, but here is " + tmp);
 			getChar();
 		}while(true);
 		char first = yylval.charAt(0);
 		if (!(first >= 'a' && first <= 'z')){
			yyerror("get an ID, the fisrt char should be a valid char, but here is " + first);
 		}
 	}
 	
 	public int yylex() {
 		 for (;;) {
 		      // Skip whitespace
 		      while (c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f') {
 		    	  if (c == '\n')
 		    		  line++;
 		          nextChar();
 		      }
 		      if (c<0) {
 		        return (token=Tokens.ENDINPUT);
 		      }
 		      switch (c) {
 		        case '$' : 
 		        	      if (Interpreter.shellMode == true){
 		        	    	  return (token=Tokens.ENDINPUT);
 		        	      } else {
 		        	    	  yyerror("Illegal character "+"$" + "in file mode");
 		        	      }
 		        case '+' : // '+'
 		        	       nextChar();
 		        	       return token=Tokens.PLUS;
 		        	       /* positive number
 		        	        if (Character.isDigit((char)c)){
 		        	    	   yylval = "";
 				        	   ContinueGetID();
 				        	   return token=Tokens.INTEGER;		
 		        	       } else {
 		        	    	   return token='+';
 		        	       }
 		        	       */
 		        case '-' : // '-' and  ->
 		        	       nextChar();
 		        	       if (c == '>'){
 		        	    	   nextChar();
 			        	       return token=Tokens.LAMDA;
 		        	       /*} else if (Character.isDigit((char)c)){
 		        	    	   yylval = "-";
 				        	   ContinueGetID();
 				        	   return token=Tokens.INTEGER; */
 		        	       } else {
 		        	    	   return token=Tokens.MINUS;
 		        	       } 
 		        case '*' : nextChar();
 		                   return token=Tokens.TIMES;
 		        case '/' :  // '/' and /*  */
 		        	       nextChar();
 		             	   if (c != '*'){
 		        			   return token=Tokens.DEVIDE; 
 		        		   }
 		        		   while(true){
 		        				nextChar();
 		        				if (c != '*'){
 		        					continue;
 		        				}else{
 		        					nextChar();
 		        					if (c == '/'){
 		        						break;
 		        					}
 		        				}
 		        			}
 		        			nextChar();
 		        			break;
 		        case '=' : nextChar();
 		        		   return token=Tokens.EQ;
 		        case '>' : nextChar();
 	        		       return token=Tokens.GT;
 		        case '<' : nextChar();
 	        		       return token=Tokens.LT;
 		        case '(' : nextChar();
 		                   return token=Tokens.LPAREN;
 		        case ')' : nextChar();
 		                   return token=Tokens.RPAREN;
 		        case ';' : nextChar();
 		                   return token=Tokens.SEMICOLON;
 		        case ',' : nextChar();
     				       return token=Tokens.COMMA;
 		        case '~' :  // negative Integer
 		        	       nextChar();
 		        	       return token=Tokens.UMINUS;
 		        		   /*
 		        		   if (Character.isDigit((char)c)){
 		        			   yylval = "-";
 		        			   ContinueGetInteger();
 		        			   return token=Tokens.INTEGER;		
 		        		   } else {
 		        			   yyerror("the char following ~ should be digit value");
 		        		   }
 		        		   */
 			               
 		        case ':' :  // := assignment  :: cons
 		        	       nextChar();
 		        	       if (c == '=')
 		        	       {
 		        	    	   nextChar();
 		        	    	   return token = Tokens.ASSIGN;
 		        	       }else if (c == ':'){
 		        	    	   nextChar();
 		        	    	   return token = Tokens.CONS;
 		        	       } else {
 		        	    	   yyerror("Illegal character "+c);
 		        	       }
 //	-----------------------------------------------------------------------------
 		        case 'f' :  // fun and fst and false
 		        		   yylval="";
 		        	       getChar();
 		        	       if (c == 'u'){
 		        	    	   getChar();
 		        	    	   if (c == 'n')
 		        	    	   {
 		        	    		   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 		        	    			   return token=Tokens.FUN;
 		        	    	   }
 		        	       } else if (c == 's'){
 		        	    	   getChar();
 		        	    	   if (c == 't')
 		        	    	   {
 		        	    		   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 		        	    			   return token=Tokens.FST;
 		        	    	   }
 		        	       } else if (c == 'a'){
 		        	    	   getChar();
 		        	    	   if (c == 'l')
 		        	    	   {
 		        	    		   getChar();
 		        	    		   if (c == 's')
 			        	    	   {
 		        	    			   getChar();
 			        	    		   if (c == 'e')
 				        	    	   {
 				        	    		   getChar();
 				        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 				        	    		   if (isPunctuation())
 				        	    			   return token=Tokens.BOOLEAN;
 				        	    	   }
 			        	    	   }
 		        	    	   }
 		        	       }
 		        	       ContinueGetID();
 		        	       return token=Tokens.ID;
 		        case 'l' :  // let
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'e'){
 	        	    	   getChar();
 	        	    	   if (c == 't')
 	        	    	   {
 	        	    		   getChar();
 	        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 	        	    		   if (isPunctuation())
 	        	    			   return token=Tokens.LET;
 	        	    	   }
 	        	       } 
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'i' : // in and if
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'n'){
 	        	    	   getChar();
         	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
         	    		   if (isPunctuation())
 	        	    		   return token=Tokens.IN;
 
 	        	       } else if (c == 'f'){
 	          	    	   getChar();
         	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
         	    		   if (isPunctuation())
         	    			   return token=Tokens.IF;
 
 	        	       }
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'e' : // end and else
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'n'){
 	        	    	   getChar();
 	        	    	   if (c == 'd'){
 	        	    		   getChar();
 	        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 	        	    		   if (isPunctuation())
 	        	    			   return token=Tokens.END;
 	        	    	   }
 
 	        	       } else if (c == 'l'){
 	          	    	   getChar();
 	          		       if (c == 's'){
 	          		    	   getChar();
 	          		    	   if (c == 'e'){
 	          		    		   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 	          		    			   return token=Tokens.ELSE;
 	          		    	   }
 	          		       }
 
 	        	       }
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 't' : // then and tail and true
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'h'){
 	        	    	   getChar();
 	        	    	   if (c == 'e'){
 	        	    		   getChar();
 	        	    		   if (c == 'n'){
 	        	    			   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 	        	    				   return token=Tokens.THEN;
 	        	    		   }
 	        	    	   }
 	        	       } else if (c == 'a'){
 	          	    	   getChar();
 	          		       if (c == 'i'){
 	          		    	   getChar();
 	          		    	   if (c == 'l'){
 	          		    		   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 	          		    			   return token=Tokens.TAIL;
 	          		    	   }
 	          		       }
 
 	        	       } else if (c == 'r'){
 	          	    	   getChar();
 	          		       if (c == 'u'){
 	          		    	   getChar();
 	          		    	   if (c == 'e'){
 	          		    		   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 	          		    			   return token=Tokens.BOOLEAN;
 	          		    	   }
 	          		       }
 
 	        	       }
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'w' : // while
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'h'){
 	        	    	   getChar();
 	        	    	   if (c == 'i'){
 	        	    		   getChar();
 	        	    		   if (c == 'l'){
 	        	    			   getChar();
 	        	    			   if (c == 'e'){
 	        	    				   getChar();
 			        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 			        	    		   if (isPunctuation())
 	        	    					   return token=Tokens.WHILE;
 	        	    			   }
 	        	    			   
 	        	    		   }
 	        	    	   }
 	        	       }
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'd' :  // do
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'o'){
 	        	    	   getChar();
         	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
         	    		   if (isPunctuation())
 	        	    		   return token=Tokens.DO;
 	        	       } 
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'n' :  // nil and not
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'i'){
 	        	    	   getChar();
 	        	    	   if (c == 'l')
 	        	    	   {
 	        	    		   getChar();
 	        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 	        	    		   if (isPunctuation())
 	        	    			   return token=Tokens.NIL;
 	        	    	   }
 	        	       } else if (c == 'o'){
 	        	    	   getChar();
 	        	    	   if (c == 't')
 	        	    	   {
 	        	    		   getChar();
 	        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 	        	    		   if (isPunctuation())
 	        	    			   return token=Tokens.NOT;
 	        	    	   }
 	        	       }
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 's' :  // snd
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'n'){
 	        	    	   getChar();
 	        	    	   if (c == 'd')
 	        	    	   {
 	        	    		   getChar();
 	        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 	        	    		   if (isPunctuation())
 	        	    			   return token=Tokens.SND;
 	        	    	   }
 	        	       } 
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 
 		        case 'h' :  // head
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'e'){
 	        	    	   getChar();
 	        	    	   if (c == 'a')
 	        	    	   {
 	        	    		   getChar();
 		        	    	   if (c == 'd')
 		        	    	   {
 		        	    		   getChar();
 		        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 		        	    		   if (isPunctuation())
 		        	    			   return token=Tokens.HEAD;
 		        	    	   }
 	        	    	   }
 	        	       } 
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'a' :  // and
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'n'){
 	        	    	   getChar();
 	        	    	   if (c == 'd')
 	        	    	   {
 	        	    		   getChar();
 	        	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
 	        	    		   if (isPunctuation())
 	        	    			   return token=Tokens.AND;
 	        	    	   }
 	        	       } 
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case 'o' :  // or
 	        		   yylval="";
 	        	       getChar();
 	        	       if (c == 'r'){
 	        	    	   getChar();
         	    		   //if ( c==' ' || c=='\n' || c=='\t' || c=='\r' || c == '\f')
         	    		   if (isPunctuation())
 	        	    		   return token=Tokens.OR;
 	        	       } 
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		        case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':  // positive integer
 		        	   yylval = "";
 		        	   ContinueGetInteger();
 		        	   return token=Tokens.INTEGER;		        	   
 		        default  : 
 		        	   yylval="";
 	        	       ContinueGetID();
 	        	       return token=Tokens.ID;
 		      }
 		    }
 	}
 
 }
