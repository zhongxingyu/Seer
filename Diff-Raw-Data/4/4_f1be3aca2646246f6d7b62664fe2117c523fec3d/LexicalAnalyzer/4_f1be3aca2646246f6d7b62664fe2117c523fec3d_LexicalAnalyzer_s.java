 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nmalloch
  * Date: 1/8/13
  * Time: 12:05 PM
  */
 public class LexicalAnalyzer {
     private List<String> keyWords;
     private List<String> tokenList;
     private List<String> symbolCheck;
 
     private List<Tuple<String, String, Integer>> lexicalList;
     private int lexPtr;
 
     public LexicalAnalyzer() {
         lexicalList = new ArrayList<Tuple<String, String, Integer>>();
         lexPtr = 0;
     }
 
     public static enum tokenTypesEnum {
         NUMBER,
         CHARACTER,
         IDENTIFIER,
         PUNCTUATION,
         KEYWORD,
         MATH_OPR,
         RELATIONAL_OPR,
         BOOLEAN_OPR,
         ASSIGNMENT_OPR,
         ARRAY_BEGIN,
         ARRAY_END,
         BLOCK_BEGIN,
         BLOCK_END,
         PAREN_OPEN,
         PAREN_CLOSE,
         UNKNOWN,
         EOT,
         IO_OPR
     }
 
     protected void setKeyWords() {
         keyWords = new ArrayList<String>();
         keyWords.add("atoi");
         keyWords.add("bool");
         keyWords.add("class");
         keyWords.add("char");
         keyWords.add("cin");
         keyWords.add("cout");
         keyWords.add("else");
         keyWords.add("false");
         keyWords.add("if");
         keyWords.add("int");
         keyWords.add("itoa");
         keyWords.add("main");
         keyWords.add("new");
         keyWords.add("null");
         keyWords.add("object");
         keyWords.add("public");
         keyWords.add("private");
         keyWords.add("return");
         keyWords.add("string");
         keyWords.add("this");
         keyWords.add("true");
         keyWords.add("void");
         keyWords.add("while");
     }
 
     protected void setSymbolCheck() {
         symbolCheck = new ArrayList<String>();
         // boolean operators
         symbolCheck.add("&&");
         symbolCheck.add("||");
 
         // IO operators
         symbolCheck.add("<<");
         symbolCheck.add(">>");
 
         symbolCheck.add("\'");
 
         // relational operators
         symbolCheck.add(":=");
         symbolCheck.add("<=");
         symbolCheck.add(">=");
         symbolCheck.add("==");
         symbolCheck.add("<");
         symbolCheck.add(">");
         symbolCheck.add("!=");
         symbolCheck.add("=");
 
         // Parentheses
         symbolCheck.add(")");
         symbolCheck.add("(");
 
         // Arrays
         symbolCheck.add("[");
         symbolCheck.add("]");
 
         // Blocks
         symbolCheck.add("{");
         symbolCheck.add("}");
 
         // punctuation
         symbolCheck.add("\"");
         symbolCheck.add(",");
         symbolCheck.add(".");
         symbolCheck.add(":");
 
         // math operators
         symbolCheck.add("+");
         symbolCheck.add("-");
         symbolCheck.add("/");
         symbolCheck.add("*");
 
         // end of line
         symbolCheck.add(";");
     }
 
     public void processFile(String fileName) {
         setKeyWords();
         setSymbolCheck();
         BufferedReader file = null;
         int lineCount = 1;
 
         try {
             file = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
         } catch (FileNotFoundException e) {
             System.out.println("Cannot find File");
             System.exit(0);
         }
 
         String line;
 
         try {
             while ((line = file.readLine()) != null) {
                 // check for empty or pure comment lines
                 if (line.trim().isEmpty() || line.trim().startsWith("//")) {
                     lineCount++;
                     continue;
                 }
 
                 // check for and remove comments
                 if (line.trim().contains("//")) {
                     String[] tempLine = line.split("//");
                     line = tempLine[0].trim();
                 }
 
                 tokenList = new ArrayList<String>();
                 // break up string by spaces
                 for (int i = 0; i < line.length(); i++){
                     if (i > 0 && i < line.length()) {
                         if (line.toCharArray()[i] == ' ' && line.toCharArray()[i -1] == '\'' && line.toCharArray()[i +1] == '\'') {
                             StringBuilder sb = new StringBuilder(line);
                             sb.setCharAt(i, 'ƒ');
                             line = sb.toString();
                         }
                     }
                 }
 
                 String[] tokenizer = line.split("[\\s\\t]");
 
                 for (int j = 0; j < tokenizer.length; j++) {
                     if (tokenizer[j].contains("ƒ")) {
                         for (int i = 0; i < tokenizer[j].length(); i++){
                             if (i > 0 && i < tokenizer[j].length()) {
                                 if (tokenizer[j].toCharArray()[i] == 'ƒ' && tokenizer[j].toCharArray()[i -1] == '\'' && tokenizer[j].toCharArray()[i +1] == '\'') {
                                     StringBuilder sb = new StringBuilder(tokenizer[j]);
                                     sb.setCharAt(i, ' ');
                                     tokenizer[j] = sb.toString();
                                 }
                             }
                         }
                     }
                 }
 
                 for (String item : tokenizer) {
                     if (tokenizer.length < 1 && item.trim().isEmpty()) {
                         lineCount++;
                         continue;
                     }
 
                     int originalListSize = tokenList.size();
                     for (String s : symbolCheck) {
                         while (item.contains(s) && item.length() > 0) {
                             if (s.equals("'")) {
                                 s = checkForChars(item, s);
                             }
 
                             item = breakDownToken(item, s);
                         }
                     }
 
                     if (originalListSize == tokenList.size()) {
                         tokenList.add(item);
                     }
                 }
 
                 for (String token : tokenList) {
 
                     if (token.trim().isEmpty()) {
                         continue;
                     }
 
                     String tokenType = tokenTypesEnum.UNKNOWN.toString();
 
                     if (keyWords.contains(token.trim())) {
                         tokenType = tokenTypesEnum.KEYWORD.toString();
                     } else if (token.matches("^[-|\\+]?[0-9]+$")) {
                         tokenType = tokenTypesEnum.NUMBER.toString();
                     } else if (token.equals("&&") || token.equals("||")) {
                         tokenType = tokenTypesEnum.BOOLEAN_OPR.toString();
                     } else if (token.equals(">>") || token.equals("<<")) {
                         tokenType = tokenTypesEnum.IO_OPR.toString();
                     } else if (token.equals(":=") || token.equals("<=") || token.equals(">=") || token.equals("==") || token.equals("<") || token.equals(">") || token.equals("!=")) {
                         tokenType = tokenTypesEnum.RELATIONAL_OPR.toString();
                     } else if (token.equals("=")) {
                         tokenType = tokenTypesEnum.ASSIGNMENT_OPR.toString();
                     } else if (token.equals("(")) {
                         tokenType = tokenTypesEnum.PAREN_OPEN.toString();
                     } else if (token.equals(")")) {
                         tokenType = tokenTypesEnum.PAREN_CLOSE.toString();
                     } else if (token.equals("{")) {
                         tokenType = tokenTypesEnum.BLOCK_BEGIN.toString();
                     } else if (token.equals("}")) {
                         tokenType = tokenTypesEnum.BLOCK_END.toString();
                     } else if (token.equals("[")) {
                         tokenType = tokenTypesEnum.ARRAY_BEGIN.toString();
                     } else if (token.equals("]")) {
                         tokenType = tokenTypesEnum.ARRAY_END.toString();
                     } else if (token.matches("[,.:]")) {
                         tokenType = tokenTypesEnum.PUNCTUATION.toString();
                     } else if (token.equals(";")) {
                         tokenType = tokenTypesEnum.EOT.toString();
                    } else if (token.matches("[\\*\\+-/]")) {
                         tokenType = tokenTypesEnum.MATH_OPR.toString();
                     } else if (token.matches("'" + "[\\w><_,: ]" + "'") || token.matches("'\\" + "\\[\\w]" + "'")) {
                         tokenType = tokenTypesEnum.CHARACTER.toString();
                     } else if (token.matches("^[a-zA-Z]+[a-zA-Z0-9_]*$") && token.length() < 80) {
                         tokenType = tokenTypesEnum.IDENTIFIER.toString();
                     }
 
                     lexicalList.add(new Tuple<String, String, Integer>(token, tokenType, lineCount));
                 }
 
                 lineCount++;
             }
 
         } catch (IOException e) {
             System.out.println("There was an error reading in the file. Failed on line " + lineCount);
             System.exit(0);
         }
     }
 
     private String checkForChars(String item, String breakDownItem) {
         int size = item.length();
         int index = item.indexOf(breakDownItem);
 
         if (index < size - 3) {
             if (item.charAt(index + 2) == '\'') {
                 if (item.substring(index + 1, index + 2).matches("[\\w><_,: ]")) {
                     breakDownItem = item.substring(index, index + 3);
                 }
             } else if (item.charAt(index + 1) == '\\' && item.charAt(index + 3) == '\'') {
                 if (item.substring(index + 2, index + 3).matches("[\\w><_,: ]")) {
                     breakDownItem = item.substring(index, index + 4);
                 }
             }
             return breakDownItem;
         }
 
         if (index < size - 2) {
             if (item.charAt(index + 2) == '\'') {
                 if (item.substring(index + 1, index + 2).matches("[\\w><_,: ]")) {  // || item.substring(index + 1, index + 2).matches("[>]")
                     breakDownItem = item.substring(index, index + 3);
                 }
             }
         }
 
         return breakDownItem;
     }
 
     private String breakDownToken(String item, String breakDownItem) {
         if (item.contains(breakDownItem) && item.trim().length() > 0) {
             int size = tokenList.size();
             int breakLength = 1;
             if (breakDownItem.equals("'")) {
                 breakDownItem = checkForChars(item, breakDownItem);
             }
             if (breakDownItem.length() > 1) {
                 breakLength = breakDownItem.length();
             }
 
             String temp = item.trim().substring(0, item.indexOf(breakDownItem));
             if (!temp.trim().isEmpty()) {
                 for (String s : symbolCheck) {
                     while (temp.contains(s) && temp.length() > 0) {
                         temp = breakDownToken(temp, s);
                     }
                 }
                 if (size == tokenList.size()) {
                     tokenList.add(temp);
                 }
             }
             tokenList.add(breakDownItem);
 
             size = tokenList.size();
             item = item.trim().substring(item.indexOf(breakDownItem) + breakLength, item.length());
             if (!item.trim().isEmpty()) {
                 for (String s : symbolCheck) {
                     while (item.contains(s) && item.length() > 0) {
                         item = breakDownToken(item, s);
                     }
                 }
 
                 if (size == tokenList.size()) {
                     tokenList.add(item.substring(item.indexOf(breakDownItem) + 1, item.length()));
                 }
             }
             return item;
         }
         return item;
     }
 
     public boolean hasNext() {
         return lexPtr < lexicalList.size();
     }
 
     public Tuple<String, String, Integer> getNext() {
         return lexicalList.get(lexPtr++);
     }
 
     public Tuple<String, String, Integer> get(int ptr) {
         try {
             lexicalList.get(ptr);
         } catch (IndexOutOfBoundsException e) {
             return null;
         }
         return lexicalList.get(ptr);
     }
 
     public void resetList() {
         lexPtr = 0;
     }
 }
