 package com.cise.ufl.rpal;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Queue;
 import java.util.Stack;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class Lexer<E> {
         private StringBuilder contents = new StringBuilder();
         private ArrayList<E> tokenList = new ArrayList<E> ();
         private HashMap lexTable = new HashMap ();
        
         Logger logger = Logger.getLogger("rpalLogger");
         
 
         public void readFile (String fileName) {
         		logger.info ("---------------------------\n");
                 logger.info ("Input file: " + fileName);
                 logger.info ("---------------------------\n");
                 try {
                         BufferedReader br = new BufferedReader (new FileReader(fileName));
                         String line = null;
                         while (( line = br.readLine()) != null){
                              line = line.replaceAll("\t", "");	
                                  contents.append(line);
                                  contents.append(" <eol> "); // the space in front of <eol> is crucial for the lexer
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                
                 System.out.println ("BEFORE " + contents);
                     
                 int temp = contents.indexOf(",");        
                  while (temp > 0) {
                      contents.insert(temp, " ");   // inserting a space in case of Commas
                      contents.insert(temp+2, " "); 
                    	 temp = contents.indexOf(",", temp+2);
                  }
                
                 
                 int temp2 = contents.indexOf(")");        
                while (temp2 > 0) {             
                     contents.insert(temp2, " ");   // inserting a space in case of Commas
                     contents.insert(temp2+2, " ");
                     temp2 = contents.indexOf(")", temp2+4);  
                 }
               
                 temp2 = contents.indexOf("(");  
                System.out.println ("temp2 "+ temp2);
                 while (temp2 > 0) {
                      contents.insert(temp2, " ");   // inserting a space in case of Commas
                      contents.insert(temp2+2, " ");
                      temp2 = contents.indexOf("(", temp2+4);
                 }
                 // temp fix for the single quote problem
                 temp2 = contents.indexOf("=");  
              //   System.out.println ("temp2 "+ temp2);
                 while (temp2 > 0) {
                      contents.insert(temp2, " ");   // inserting a space in case of Commas
                      contents.insert(temp2+2, " ");
                      temp2 = contents.indexOf("=", temp2+4);
                 }
                 System.out.println (contents);
                 //logger.info ("File Contents: " + contents);
         }
         
         public void constructTokens () {
         	logger.info("Initiating token construction...");
         	if (contents == null || contents.equals("")) {
         		logger.severe("content is empty");
         		return;
         	}
         	
         	StringTokenizer st = new StringTokenizer (contents.toString(), " ");
         	boolean status = false;
         	E token = (E) "";
         	//String tempToken =  "";
         	//ArrayList queue = new ArrayList();
         	
         	while (st.hasMoreTokens()) {
         		
         		if (! status) {
         			token =  (E) st.nextToken().trim();
         						// We only get the next token if the status is true. because we might have additional sequences of ((((( etc...
         			if (((String) token).startsWith ("'")) {
         				
         				if (token.equals("''")) {
         					System.out.println ("Came here " + token);
         					tokenList.add((E) token.toString());
         					status = false;
         					continue;
         				}
    
         				else {
         					String temp = ((String) token);
         					
         					if (temp.charAt(0) == '\'' && temp.charAt(temp.length()-1) == '\'' && temp.length() > 1) {    // add the string as it is...> 1 check is reqd
         						System.out.println ("Came here2 " + token);
             					tokenList.add((E) temp);
             					status = false;
             					continue;
         					}
         				}
         				StringBuffer buffer = new StringBuffer ();
         				System.out.println ("TOKEN did COME HERE " + token);
        				
        				while (((String) token).endsWith("'") != true)  {
        					System.out.println ("TOKEN SHD COME HERE " + token);
         						buffer.append(token+" ");
        					 token =  (E) st.nextToken();
         				}
         				 buffer.append(token); 
         				 if (buffer.toString().indexOf ("<eol>") > 0) {
         					 buffer = new StringBuffer (buffer.toString().replaceAll(" <eol> ", ""));
         				 }
         				 tokenList.add((E) buffer.toString());
         				 status=false;
         				 continue;
         			}
         			 System.out.println ("About to classify " + token );
         		}
        /* 		if (((String) token).startsWith ("'")) {
         			System.out.println("Token started with " +token );
         			StringBuffer buffer = new StringBuffer ();
         			while (((String) token).endsWith("'") != true)  {
         				//System.out.println ("Inside string " + token);
          			    buffer.append(token);
          			    token =  (E) st.nextToken();
         			}
         	
         			buffer.append(token);  			// We add the remaining token before pushing it into the arraylist
         			tokenList.add((E) buffer.toString());
         				
         		
         		    status=false;
         		    System.out.println ("ADDED STRING TOKEN "+ buffer.toString());
         		    continue;
         		}*/
         		if (token.equals("<eol>") || token.equals ("\t") || token.equals("\n")) {
         			status = false;
         			continue;
         		}
         		else if (((String) token).startsWith("//")) {
         				status = false;
                			while (st.nextToken().equals("<eol>") != true)
         				continue;
         		}
         		/* insert code to add it to the identifer list 
         		 * Trial code by making use of lexemes
         		 * 
         		 * */
         		else {
         			StringBuffer sb = new StringBuffer ();
         			String temp = (String) token;
         			int i = 0;
         			char operator = 0 ;
         			boolean operatorFound = true;
         			
         			while ( i < temp.length() &&  (((String)token).charAt(i)) != 0 && !(isOperatorSymbol(new String(""+((String)token).charAt(i))))   && !isLeftBracketSymbol(new String(""+((String)token).charAt(i))) 
         					    && !isRightBracketSymbol (new String(""+((String)token).charAt(i)))  &&  !isCommaOperatorSymbol(new String(""+((String)token).charAt(i))) && temp.length()>1
         		        						)
         			{
         					operatorFound = true;
         					//System.out.println (temp.charAt(i));
         					sb.append(((String)token).charAt(i));		// keep pushing the tokens in to the string buffer until we encounter any of the above...
         					i++;
         					if (i == temp.length()) {
         						operatorFound = false;
         						break;
         					}
         			}
         			
         			
         			if (temp.length() == 1 && temp.charAt(0) != 0) {
         				//System.out.println ("Adding just one character" + temp.charAt(0));
         				tokenList.add((E) temp);	
         				status = false;
         				continue;
         			}
         			if (operatorFound ) {
         			
         			    operator = ((String)token).charAt(i);
         			   
         			    if ( isOperatorSymbol(new String(""+operator) )|| 
         			    	 isLeftBracketSymbol(new String(""+operator) ) || isRightBracketSymbol(new String(""+operator) ) || isCommaOperatorSymbol(new String(""+operator))) {
         			        //System.out.println ("Operator Found " +  operator);
         			        if (sb.length()>0) {
         			        	//System.out.println ("Adding a complete token " + sb);
         			        	tokenList.add((E) sb.toString());
         			        } 
         			        if (operator == '-' && ((String)token).charAt(i+1) !=0 &&  ((String)token).charAt(i+1) == '>') {
         			        	tokenList.add((E) "->");	
         			        	//System.out.println ("Token added ->");
         			        	//System.out.println ("String token"+ ((String)token).length() + "and I+2 value " + (i+2) );
         			        	if ( (i+2) >= ((String)token).length()) {
         			        		status = false;
         			        		continue;
         			        	}
         			        	else {
         			        		
         			        		token = (E) ((String)token).substring(i+2);
         			        		temp = (String) token;
         			        		//System.out.println ("There is a token further so, making the token as " +token );
         			        	}
         			        }
         			        else {
         			        	//System.out.println ("Adding Operator " + operator);
         			        	tokenList.add( (E) new String(""+operator));
         			        }
         			       
         			        if (  ((String)token).length() > 1 ) {
         			            
         			        	if (sb.length() > 0) {
         			        	    token = (E) temp.substring(sb.length()+1);
         			        	    //System.out.println ("After reduction " + token);
         			        	}
         			        	else {
         			        		token = (E) temp.substring(1);
         			        		//System.out.println ("After reduction " + token);
         			        	}
         			        	if (((String)token).length() > 0) { 
         			            status = true;
         			            continue;
         			        	}
         			        	else {
         			        		status=false;
         			        		continue;
         			        	}
         			        }
         			    else {
         			    	status=false;
         			    	continue;
         			    }
         			    }
         			    else { 
         			    	//System.out.println ("I think this is a complete  token " + sb);
     			        	if (sb.length() > 0)
         			    	    tokenList.add((E) sb.toString());
         			    	status=false;
         			    }
         			}
         			else {
         				
         				//System.out.println ("Operator not found, so jus adding " + sb);
         				if (sb.length() > 0)
         				    tokenList.add((E) sb.toString());
         				status = false;
         				continue;
         			}
         		}
         		/*
         		 * end trial code
         		 */
  /*       		else if ( ((String)token).charAt(0)   == '(') {
         			
         			String temp = (String) token;
         			tokenList.add( (E) (    new String (""+ ((String)temp).charAt(0) )));
         			if (temp.length() > 1) {
                			token = (E) temp.substring(1);			// we add the first occurance of '(' to the list and then let the while loop detect subsequent tokens by setting the status to false.
         				status = true;
         			}
         			else {
         				status = false;
         				continue;
         			}
         			//System.out.println ("Token is reduced to " + token);
         			continue;
         		}
         		
         		else if (((String) token).contains(",")) {		// for now, i am going to assume that ',' if it occurs would only occur at the end of the string.
         			String temp = (String) token;
         			tokenList.add( (E) (    new String (",")));
         			if (temp.length() > 1) {
         			token = (E) temp.substring(0, temp.length()-1 );
         			status = true;
         			}
         			else {
         				status = false;
         				continue;
         			}
         				
         			
         		}
         		else if ( ((String)token).charAt(((String)token).length()-1)   == ')') {
         			
         			String temp = (String) token;
         			tokenList.add( (E) (    new String (""+ ((String)temp).charAt(((String)token).length()-1) )));
         			if (temp.length() > 1) {
         				status = true;
             			token = (E) temp.substring(0, ((String)token).length()-1);			// we add the last occurance of ')' to the list and then let the while loop detect subsequent preceding tokens by setting the status to false.
         			}
         			else {
         			////System.out.println ("Token is reduced to " + token);
         				status = false;
         				continue;
         			}
         			continue;
         		}
         		
         		if (! ((String) token).startsWith("//")) {
         			tokenList.add((E) ((String) token).trim());
         			status = false;
         		}*/
         	}
        	//System.out.println ("Token List :" + tokenList );
         }
         
         public ArrayList getTokens () {
         	return this.tokenList;
         }
         
         public HashMap getLexTable () {
         	return this.lexTable;
         }
         private boolean isArrow (String token ) {  	
         	Pattern p = Pattern.compile("^->$");  // pattern to detect only the integers
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
         	System.out.println ("Matched arrow" + result);
         	return result;  	
         }
         private boolean isInteger (String token) {
         	Pattern p = Pattern.compile("^[0-9]*$");  // pattern to detect only the integers
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
         	return result;  	
         }
         private boolean isIdentifier (String token) {
         	Pattern p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");  // pattern to detect identifiers valid are the ones that start with _, letters and digits followed only by those. 
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
         	return result;  	
         }
        
         private boolean isString (String token) {
         	Pattern p = Pattern.compile("^'[\t\n\\\\\\();,a-zA-Z0-9-+*/<>&.@/:=~|$!#%^_\\[\\]{}\"'?\\s]*'$");  // Strings.. Note the \s space at the end...
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
         	return result;  	
         }
         public boolean isOperatorSymbol(String token) {
        	Pattern p = Pattern.compile("^[-+*/<>&.@/:=~|$!#%^_\\[\\]{}\"'?]$");
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
 			return result;
 		}
         public boolean isLeftBracketSymbol (String token ) {
         	Pattern p = Pattern.compile("^[(]$");
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
 			return result;
         }
 
         public boolean isRightBracketSymbol (String token ) {
         	Pattern p = Pattern.compile("^[)]$");
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
 			return result;
         }
         
         public boolean isCommaOperatorSymbol (String token) {
         	Pattern p = Pattern.compile("^[,]$");
         	Matcher match = p.matcher(token);
         	boolean result = match.find();
 			return result;
         }
         
         public String getTypeOfToken (String token) {				// Note that the order of checking for various types is important...
         	if ( isInteger (token)) {
         		//System.out.println( token + "\t" + "Integer");
         		return "Integer";
         	}
         	
         	else if (isOperatorSymbol (token)) {
         		//System.out.println( token + "\t" + "Operator");
         		return "Operator_symbol";
         	}
         	
          	else if (isLeftBracketSymbol (token)) {
         		//System.out.println( token + "\t" + "(");
         		return "(";
         	}
         	
          	else if (isRightBracketSymbol (token)) {
         		//System.out.println( token + "\t" + ")");
         		return ")";
         	}
         	
          	else if (isCommaOperatorSymbol (token)) {
         		//System.out.println( token + "\t" + ",");
         		return ",";
         	}
         	
         	else if (isIdentifier(token)) {
         		//System.out.println( token + "\t" + "Identifier");
         		return "Identifier";
            	}
         	else if (isArrow(token)) {
         		//System.out.println( token + "\t" + "Arrow");
         		return "Arrow";
         	}
         	else if (isString (token)) {
         		//System.out.println( token + "\t" + "String");
         		return "String";
         	}
         	else {
         		//System.out.println( token + "\t" + "Unknown");
         		return "Unknown";
         	}
         }
       
 		public void printLexTable () {
         	for (E token: tokenList ) {
         		try {
         		lexTable.put((String) token, this.getTypeOfToken((String)token));
         		}
         		catch (Exception e) {
         			e.printStackTrace();
         		}
         		//this.getTypeOfToken((String) token);
         	}
         		System.out.print(lexTable);
 		    }
         
         public static void main(String args[]) {
                 Lexer lexer = new Lexer ();
                 lexer.readFile (args[0]);
                lexer.constructTokens();
                System.out.println("The result  " + lexer.isIdentifier("Vec_sum"));
                 lexer.printLexTable();
         }
 }
