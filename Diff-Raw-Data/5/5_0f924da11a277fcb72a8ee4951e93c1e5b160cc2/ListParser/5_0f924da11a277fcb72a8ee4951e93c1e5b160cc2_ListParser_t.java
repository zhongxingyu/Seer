 package com.github.droxer.parser;
 
 public class ListParser  {
 
     private Lexer input;
     private Token lookahead;
     private int p = 0;
 
 
     public ListParser(Lexer lexer) {
         lookahead = lexer.nextToken();
     }
 
     public void list(){
         match(ListLexer.LBRACK);
         elements();
         match(ListLexer.RBRACK);
     }
 
     private void elements() {
         element();
         while (lookahead.getType() == ListLexer.COMMA){
             match(ListLexer.COMMA);
             element();
         }
     }
 
     private void element() {
         if(lookahead.getType() == ListLexer.NAME) {
             match(ListLexer.NAME);
         }else if( lookahead.getType() == ListLexer.LBRACK){
             list();
         }else{
            throw new Error("expecting name or list;\n found " + lookahead);
         }
     }
 
     private void match(int x) {
         if(lookahead.getType() == x){
             consume();
         }else{
            throw new Error("expecting " + input.getTokenName(x) + ";\n found " + lookahead);
         }
     }
 
     public void consume(){
         lookahead = input.nextToken();
     }
 }
