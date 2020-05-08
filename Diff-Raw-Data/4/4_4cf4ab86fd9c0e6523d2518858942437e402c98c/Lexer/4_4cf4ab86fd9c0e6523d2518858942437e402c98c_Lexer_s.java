 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Lexer;
 import java.io.*;
 import java.util.HashMap;
 
 /**
  *
  * @author aB
  */
 public class Lexer {
     private BufferedReader BufferedReader;
     private long FileSize = 0, BytesRead = 0;
     private int BufferSize = 128;
     private int pos = 0;
     private int row = 0, column = 0;
     private char [] buffer;
     private char CurrentSymbol;
     
     private HashMap Keywords;
     
     public Lexer(String FileName){
         try{
             FileInputStream fileInputStream = new FileInputStream(FileName);
             DataInputStream dataInputStream = new DataInputStream( fileInputStream );
             InputStreamReader inputStreamReader = new InputStreamReader ( dataInputStream );
             BufferedReader = new BufferedReader( inputStreamReader );
             
             File file = new File(FileName);
             FileSize = file.length();
             
             buffer = new char[BufferSize];
             pos = BufferSize;
            getNextSymbol();
             setKeyWords();
         }catch(Exception e){
             System.err.println("Lexer Error: " + e.getMessage());
         }
     }
     
     private void setKeyWords(){
        
        Keywords = new HashMap<String, Token.TokenType>();
        
        Keywords.put("import", Token.TokenType.Import);
        Keywords.put("parser", Token.TokenType.Parser);
        Keywords.put("code", Token.TokenType.Code);
        Keywords.put("terminal", Token.TokenType.Terminal);
        Keywords.put("non", Token.TokenType.Non);
     }
     
     private void CleanBuffer(){
         for(int i = 0; i < BufferSize; i++) {
             buffer[i] = '\0';
         }
     }
     
     private char getNextSymbol() throws Exception{
         if( pos == BufferSize){
             if( BytesRead == FileSize ){
                 return '\0';
             }
             CleanBuffer();
             BytesRead += BufferedReader.read( buffer, 0, BufferSize );
             pos = 0;
             column++;
             CurrentSymbol = buffer[pos];
             return buffer[pos++];
         }else{
             CurrentSymbol = buffer[pos];
             return buffer[pos++];
         }
     }
     
     public Token getNextToken() throws Exception{
         return pnextToken();
     }
     
     private Token pnextToken() throws Exception{        
         
         while (true) {
 
             CurrentSymbol = getNextSymbol();
 
             if (CurrentSymbol == '\0') {
                 
                 return new Token("EOF", Token.TokenType.EOF, row, column);
                 
             } else if (CurrentSymbol == ' ' || CurrentSymbol == '\n'
                     || CurrentSymbol ==  '\t' ) {
                 continue;
             }
 
             switch (CurrentSymbol) {
 
                 case '{':
                     CurrentSymbol = getNextSymbol();
                     if (CurrentSymbol == ':') {
                         return new Token("{:", Token.TokenType.JavaCodeStart, row, column);
                     }
                     break;
                 case ':':
                     CurrentSymbol = getNextSymbol();
                     
                     if (CurrentSymbol == '}') {
                         return new Token(":}", Token.TokenType.JavaCodeEnd, row, column);
                     } else if (CurrentSymbol == ':') {
                         
                         CurrentSymbol = getNextSymbol();
 
                         if (CurrentSymbol == '=') {
 
                             return new Token("::=", Token.TokenType.Assign, row, column);
                         }
                         
                         return new Token(":", Token.TokenType.Colon, row, column);
                     }
                 case ';':
                     return new Token(";", Token.TokenType.Semicolon, row, column);
                 case ',':
                     return new Token(",", Token.TokenType.Comma, row, column);
                 case '|':
                     return new Token("|", Token.TokenType.Or, row, column);
                 default:
                                        
                     if (Character.isLetter(CurrentSymbol) || CurrentSymbol == '_') {
                         String lexema = "";
                         
                         do {
                             lexema += CurrentSymbol;
                             CurrentSymbol = getNextSymbol();
 
                         } while (Character.isLetter(CurrentSymbol) || CurrentSymbol == '_' || Character.isDigit(CurrentSymbol));
                         pos--;
                       
                         if(Keywords.containsKey(lexema.toLowerCase())){
                             return new Token(lexema, (Token.TokenType)Keywords.get(lexema),row,column); 
                         }
                         
                         return new Token(lexema, Token.TokenType.Identifier, row, column);
                     }                  
             }
         }
     }          
 }
