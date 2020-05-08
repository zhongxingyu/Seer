 package com.prealpha.dcputil.compiler.lexer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * User: Ty
  * Date: 7/12/12
  * Time: 3:45 AM
  */
 public class Lexer {
     private String inputString = "";
 
     public Lexer(){
 
     }
 
     public List<Expression> lex(String input){
         this.inputString = input;
         List<Expression> tokenList = new ArrayList<Expression>();
 
         String[] lines = input.split("\n");
         int ln = 1;
         for(String line:lines){
             tokenList.addAll(lexLine(line, ln));
             ln++;
         }
 
         return tokenList;
     }
 
     private static final Pattern section = Pattern.compile("(:?\\[? *\\w+\\+?\\w* *\\]?)([;.*]?)");
     public List<Expression> lexLine(String line, int linNum){
         line = line.trim();
         line = line.replace(","," ");
        line = line.split(";")[0];
 
         String[] segments = line.split("\\|");
         List<Expression> expressions = new ArrayList<Expression>();
         for(String s:segments){
             List<Token> tokens = new ArrayList<Token>(3);
 
             Matcher matcher = section.matcher(s);
             int start = 0;
             while(matcher.find(start)){
                 String token = matcher.group(1).trim();
                 start = matcher.end(1);
                 tokens.add(new Token(token,linNum));
             }
 
             if(tokens.size()>0){
                 expressions.add(new Expression(tokens.toArray(new Token[tokens.size()])));
             }
         }
         return expressions;
     }
 }
 
