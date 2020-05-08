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
     private int row = 1, column = 0;
     private char [] buffer;
     private char CurrentSymbol;
     
     private String modo = "Cup";
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
             column++;
             return buffer[pos++];
         }
     }
     
     public Token getNextToken() throws Exception{
         Token t = pnextToken();
         if( t.getTipo() == Token.TokenType.Identifier ){
             if( Keywords.containsKey(t.getLexema())){
                 t.setTipo( (Token.TokenType)Keywords.get(t.getLexema()) );
             }                
         }
         return t;
     }
     
     private Token pnextToken() throws Exception{
         if( modo.equals("Cup") ){
             while (true) {
                 if (CurrentSymbol == '\0') {
                     return new Token("EOF", Token.TokenType.EOF, row, column);                
                 } else if (CurrentSymbol == ' ' || CurrentSymbol ==  '\t' ) {
                     getNextSymbol();
                     continue;
                 }else if( CurrentSymbol == '\n'){
                     getNextSymbol();
                     row++;
                     column = 1;
                     continue;
                 }else if( CurrentSymbol == '\r'){
                     getNextSymbol();
                     if(CurrentSymbol == '\n'){
                         getNextSymbol();
                     }                
                     row++;
                     column = 1;
                     continue;
                 }
                 switch (CurrentSymbol) {
                     case '{':
                         getNextSymbol();
                         if (CurrentSymbol == ':') {
                             getNextSymbol();
                             modo = "JavaCode";
                             return new Token("{:", Token.TokenType.JavaCodeStart, row, column-2);
                         }
                         break;
                     case ':':
                         getNextSymbol();
                         if (CurrentSymbol == '}') {
                             getNextSymbol();
                             return new Token(":}", Token.TokenType.JavaCodeEnd, row, column-2);
                         } else if (CurrentSymbol == ':') {
                             getNextSymbol();
                             if (CurrentSymbol == '=') {
                                 getNextSymbol();
                                 return new Token("::=", Token.TokenType.Assign, row, column-2);
                             }                        
                             return new Token(":", Token.TokenType.Colon, row, column-1);
                         }
                     case ';':
                         getNextSymbol();
                         return new Token(";", Token.TokenType.Semicolon, row, column-1);
                     case ',':
                         getNextSymbol();
                         return new Token(",", Token.TokenType.Comma, row, column-1);
                     case '|':
                         getNextSymbol();
                         return new Token("|", Token.TokenType.Or, row, column-1);
                     case '.':
                         getNextSymbol();
                         return new Token(".", Token.TokenType.Dot, row, column-1);
                     case '*':
                         getNextSymbol();
                         return new Token("*", Token.TokenType.Asterisk, row, column-1);
                     default:                    
                         if (Character.isLetter(CurrentSymbol) || CurrentSymbol == '_') {
                             String lexema = "";
 
                             do {
                                 lexema += CurrentSymbol;
                                 CurrentSymbol = getNextSymbol();
                             } while (Character.isLetter(CurrentSymbol) || CurrentSymbol == '_' || Character.isDigit(CurrentSymbol));
                             return new Token(lexema, Token.TokenType.Identifier, row, column - lexema.length());
                         }else{
                             String error = String.format("Lexer Error: Unrecognized symbol \'%c\' at [%d,%d]", CurrentSymbol, row, column);
                             throw new Exception(error);
                         }
                 }
             }
         }else if( modo.equals("ReturnEndOfJavaCode")){
             modo = "Cup";
             return new Token(":}", Token.TokenType.JavaCodeEnd, row, column - 2);
         }else if( modo.equals("JavaCode") ){
             String javaCode = "";
             int tempRow = row, tempColumn = column;
             while (true) {
                 if (CurrentSymbol == '\0') {
                    javaCode += CurrentSymbol;
                    return new Token("EOF", Token.TokenType.EOF, row, column);                
                 } else if (CurrentSymbol == ' ' || CurrentSymbol ==  '\t' ) {
                     javaCode += CurrentSymbol;
                     getNextSymbol();
                     continue;
                 }else if( CurrentSymbol == '\n'){
                     javaCode += CurrentSymbol;
                     getNextSymbol();
                     row++;
                     column = 1;
                     continue;
                 }else if( CurrentSymbol == '\r'){
                     javaCode += CurrentSymbol;
                     getNextSymbol();
                     if(CurrentSymbol == '\n'){
                         javaCode += CurrentSymbol;
                         getNextSymbol();
                     }                
                     row++;
                     column = 1;
                     continue;
                 }else if( CurrentSymbol == ':'){
                     getNextSymbol();
                     if( CurrentSymbol == '}' ){
                         modo = "ReturnEndOfJavaCode";
                         getNextSymbol();
                         return new Token(javaCode, Token.TokenType.JavaCode, tempRow, tempColumn);
                     }else{
                         javaCode += CurrentSymbol;
                     }
                 }else{
                     javaCode += CurrentSymbol;
                     getNextSymbol();
                 }
             }
         }else{
             throw new Exception("Lexer Error: Unrecognized mode!");
         }
     }    
 }
