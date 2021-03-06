 /*
  * Copyright (C) 2010 SonarSource SA
  * All rights reserved
  * mailto:contact AT sonarsource DOT com
  */
 package com.sonar.sslr.test.lexer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.sonar.channel.CodeReader;
 
 import com.sonar.sslr.api.GenericTokenType;
 import com.sonar.sslr.api.Token;
 
 public class TokenUtils {
 
   public static String merge(List<Token> tokens) {
     removeLastTokenIfEof(tokens);
     StringBuilder result = new StringBuilder();
     for (int i = 0; i < tokens.size(); i++) {
       Token token = tokens.get(i);
       result.append(token.getValue());
       if (i < tokens.size() - 1) {
         result.append(" ");
       }
     }
     return result.toString();
   }
 
   private static void removeLastTokenIfEof(List<Token> tokens) {
    if (tokens.size() > 0) {
      Token lastToken = tokens.get(tokens.size() - 1);
      if (lastToken.getValue().equals("EOF")) {
        tokens.remove(tokens.size() - 1);
      }
     }
   }
 
   public static List<Token> split(String sourceCode) {
     List<Token> tokens = new ArrayList<Token>();
     CodeReader reader = new CodeReader(sourceCode);
     Matcher matcher = Pattern.compile("[a-zA-Z_0-9]+").matcher("");
 
     while (reader.peek() != -1) {
       StringBuilder nextStringToken = new StringBuilder();
       Token token;
       if (reader.popTo(matcher, nextStringToken) != -1) {
         token = new Token(GenericTokenType.IDENTIFIER, nextStringToken.toString(), reader.getLinePosition(), reader.getColumnPosition());
       } else if (' ' == (char) reader.peek()) {
         reader.pop();
         continue;
       } else {
         token = new Token(GenericTokenType.IDENTIFIER, "" + (char) reader.pop(), reader.getLinePosition(), reader.getColumnPosition());
       }
       tokens.add(token);
     }
     return tokens;
   }
 }
