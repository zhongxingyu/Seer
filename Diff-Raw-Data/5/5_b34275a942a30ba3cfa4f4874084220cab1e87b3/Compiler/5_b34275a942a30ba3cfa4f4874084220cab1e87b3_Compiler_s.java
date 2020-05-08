 package project;
 
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 4/27/13
  * Time: 8:40 AM
  */
 public class Compiler {
     public static final String CLASS = "Class";
     public static final String METHOD = "Method";
     public static final String VARIABLE = "Variable";
     public static final String LITERAL = "Literal";
     public static final int ELEM_SIZE = 1;
 
     private static final String ILLEGAL_EXPRESSION = "Illegal expression.";
     private static final String ILLEGAL_NEW_DECLARATION = "Illegal new_declaration";
     private static final String ILLEGAL_NEW_OPERATION = "Illegal new operation.";
 
     private static final String ILLEGAL_ATOI_OPERATION = "Illegal atoi operation.";
     private static final String ILLEGAL_ITOA_OPERATION = "Illegal itoa operation.";
     private static final String INVALID_ARGUMENT_LIST = "Invalid argument list.";
     private static final String INVALID_FUNCTION = "Invalid function.";
     private static final String INVALID_TYPE = "Invalid type.";
 
     private static final String INVALID_STATEMENT = "Invalid statement.";
     private static final String MISSING_ARRAY_CLOSE = "Array element missing array close.";
     private static final String MISSING_CLOSING_PARENTHESIS = "Missing closing parenthesis.";
     private static final String MISSING_OPENING_PARENTHESIS = "Missing opening parenthesis.";
 
     private static final String LINE = " Line: ";
     private static final String OPERATION = " operation.";
     private static final String ARGUMENT_LIST = " argument_list.";
     private static final String EXPRESSION = " expression.";
 
     private String scope = "g.";
     private LinkedHashMap<String, Symbol> symbolTable = new LinkedHashMap<String, Symbol>();
     private int variableId = 100;
     private int methodId = 1000;
 
     private LexicalAnalyzer lexicalAnalyzer;
     private String errorList = "";
 
     public Compiler(LexicalAnalyzer lexicalAnalyzer) {
         this.lexicalAnalyzer = lexicalAnalyzer;
     }
 
     public LexicalAnalyzer getLexicalAnalyzer() {
         return lexicalAnalyzer;
     }
 
     public String getErrorList() {
         return errorList;
     }
 
     public void evaluate(boolean isTest) {
         // pass one
         if (!compilation_unit()) {
             System.out.print(errorList);
             System.exit(0);
         }
 
         if (isTest) {
             System.out.println("Syntax Analysis Successful!");
         }
 
         lexicalAnalyzer.resetList();
 
         // pass two
         PassTwo passTwo = new PassTwo(symbolTable, lexicalAnalyzer, variableId);
         passTwo.evaluate(isTest);
     }
 
     private boolean new_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" [argument_list] ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 lexicalAnalyzer.nextToken();
                 return true;
             }
 
             String errorCheck = errorList;
 
             if (!argument_list()) {
                 if (!errorCheck.equals(errorList)) {
                     errorList += ILLEGAL_NEW_DECLARATION + ARGUMENT_LIST + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             // check format: "[" expression "]"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_NEW_DECLARATION + EXPRESSION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + ", " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         return false;
     }
 
     public boolean assignment_expression() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(KeyConst.NEW.getKey())) {
 
             // check format: "new" type new_declaration
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += ILLEGAL_NEW_OPERATION + " " + INVALID_TYPE + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !new_declaration()) {
                 errorList += ILLEGAL_NEW_OPERATION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ATOI.getKey())) {
 
             // check format: "atoi" "(" expression ")"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ATOI_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += ILLEGAL_ATOI_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ATOI_OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_ATOI_OPERATION + " Invalid Expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ATOI_OPERATION + " atoi can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_ATOI_OPERATION + " atoi can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ITOA.getKey())) {
 
             // check format: "itoa" "(" expression ")"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_ITOA_OPERATION + " Invalid Expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_ITOA_OPERATION + " itoa can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_ITOA_OPERATION + " itoa can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.THIS.getKey())) {
             lexicalAnalyzer.nextToken();
             return true;
         } else {
             if (!expression()) {
                 errorList += "Invalid assignment expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
             return true;
         }
     }
 
     public boolean expressionz() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         Tuple token = lexicalAnalyzer.getToken();
         if (!(token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(token.getType()) || isBooleanExpression(token.getType()) || isMathematicalExpression(token.getType()))) {
             errorList += "Invalid expressionz." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             errorList += "expressionz missing right hand expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             lexicalAnalyzer.previousToken();
             return false;
         }
 
         if (token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
             if (!assignment_expression()) {
                 errorList += "Invalid assignment expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         } else {
             if (!expression()) {
                 errorList += "Invalid expressionz expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         return true;
     }
 
     public boolean expression() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" expression ")" [expressionz]
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += ILLEGAL_EXPRESSION + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_EXPRESSION + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                 return expressionz();
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.NULL.getKey()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
 
             // check format: "value" [expressionz]
             if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey())) {
                 addToSymbolTable("L", lexicalAnalyzer.getToken().getName(), LITERAL, new VariableData(KeyConst.BOOL.getKey(), KeyConst.PUBLIC.getKey()));
             } else {
                 addToSymbolTable("L", lexicalAnalyzer.getToken().getName(), LITERAL, new VariableData(lexicalAnalyzer.getToken().getType(), KeyConst.PUBLIC.getKey()));
             }
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                 return expressionz();
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
 
             // check format: identifier [ fn_arr_member ] [ member_refz ] [ expressionz ]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             String errCheck = errorList;
 
             fn_arr_member();
             if (!errCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             member_refz();
             if (!errCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(lexicalAnalyzer.getToken().getType()) || isBooleanExpression(lexicalAnalyzer.getToken().getType()) || isMathematicalExpression(lexicalAnalyzer.getToken().getType())) {
                 return expressionz();
             }
 
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.UNKNOWN.name())) {
             return !isUnknownSymbol(lexicalAnalyzer.getToken().getType());
         }
         return false;
     }
 
     private boolean argument_list() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: expression { "," expression}
         if (!expression()) {
             errorList += INVALID_ARGUMENT_LIST + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             return false;
         }
 
         while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getName().equals(",")) {
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!expression()) {
                 errorList += INVALID_ARGUMENT_LIST + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
         }
         return true;
     }
 
     private boolean fn_arr_member() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "(" [ argument_list ] ")" | "[" expression "]"
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             //check format: "(" [ argument_list ] ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += INVALID_FUNCTION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 lexicalAnalyzer.nextToken();
                 return true;
             }
 
             if (!argument_list()) {
                 errorList += INVALID_ARGUMENT_LIST + " in function parameter." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_FUNCTION + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             //check format: "[" expression "]"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "Invalid array expression." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += MISSING_ARRAY_CLOSE + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
         return false;
     }
 
     private boolean member_refz() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "." identifier [ fn_arr_member ] [ member_refz ]
         if (!lexicalAnalyzer.getToken().getName().equals(".")) {
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Invalid member ref." + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             return true;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         String errCheck = errorList;
 
         fn_arr_member();
         if (!errCheck.equals(errorList)) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             return true;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         member_refz();
         return errCheck.equals(errorList);
     }
 
     public boolean statement() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
 
             // check format: "{" {statement} "}"
             String errorCheck = errorList;
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             while (statement()) {
                 lexicalAnalyzer.nextToken();
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (!errorCheck.equals(errorList)) {
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
                 errorList += INVALID_STATEMENT + " Missing a closing block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.IF.getKey())) {
 
             // check format: "if" "(" expression ")" statement [ "else" statement ]
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'if' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!statement()) {
                 errorList += INVALID_STATEMENT + " 'if' statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!(lexicalAnalyzer.peek() instanceof NullTuple) && lexicalAnalyzer.peek().getName().equals(KeyConst.ELSE.getKey())) {
 
                 lexicalAnalyzer.nextToken();
                 lexicalAnalyzer.nextToken();
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !statement()) {
                     errorList += INVALID_STATEMENT + " a valid statement is required after an else statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.WHILE.getKey())) {
 
             // check format: "while" "(" expression ")" statement
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'while' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!statement()) {
                 errorList += INVALID_STATEMENT + " 'while' statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.RETURN.getKey())) {
 
             // check format: "return" [ expression ] ";"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += INVALID_STATEMENT + " 'return' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 return true;
             }
 
             if (!expression()) {
                 errorList += "Invalid 'return' statement expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'return' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.COUT.getKey())) {
 
             // check format: "cout" "<<" expression ";"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getName().equals("<<")) {
                 errorList += INVALID_STATEMENT + "'cout' statement missing extraction operator." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'cout' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'cout' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getName().equals(KeyConst.CIN.getKey())) {
 
             // check format: "cin" ">>" expression ";"
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getName().equals(">>")) {
                 errorList += INVALID_STATEMENT + "'cin' statement missing extraction operator." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'cin' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'cin' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else {
 
             // check format: expression ";"
             if (!expression()) {
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'expression' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         }
     }
 
     public boolean parameter(List<Parameter> parameterNames) {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: type identifier ["[" "]"]
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
             errorList += "Parameter declarations must start with a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String type = lexicalAnalyzer.getToken().getName();
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Parameter declarations require a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String name = lexicalAnalyzer.getToken().getName();
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
             parameterNames.add(new Parameter(type, "P" + variableId));
             return addToSymbolTable("P", name, "param", new VariableData(type, "private"));
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
             errorList += "Invalid parameter declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         parameterNames.add(new Parameter(type, "P" + variableId));
         if (!addToSymbolTable("P", name, "param", new VariableData(type, "private"))) {
             return false;
         }
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean parameter_list(List<Parameter> parameters) {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: parameter { "," parameter}
         if (!parameter(parameters)) {
             errorList += "Invalid parameter_list" + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (!(lexicalAnalyzer.getToken().getName().equals(",") || lexicalAnalyzer.getToken().getType().equalsIgnoreCase(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()))) {
            errorList += "Invalid parameter_list: invalid argument separator. expected ',' but found '" + lexicalAnalyzer.getToken().getName() + "'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getName().equals(",")) {
             lexicalAnalyzer.nextToken();
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!parameter(parameters)) {
                 errorList += "Invalid parameter_list" + LINE + lexicalAnalyzer.peekPreviousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!(lexicalAnalyzer.getToken().getName().equals(",") || lexicalAnalyzer.getToken().getType().equalsIgnoreCase(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name()))) {
                errorList += "Invalid parameter_list: invalid argument separator. expected ',' but found '" + lexicalAnalyzer.getToken().getName() + "'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
         return true;
     }
 
     public boolean variable_declaration() {
         boolean symbolAdded = false;
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: type identifier ["[" "]"] ["=" assignment_expression ] ";"
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
             errorList += "Variable declarations must start with a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String type = lexicalAnalyzer.getToken().getName();
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Variable declarations require a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String name = lexicalAnalyzer.getToken().getName();
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += "Invalid parameter declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!addToSymbolTable("@", name, "lvar", new VariableData("@:" + type, "private"))) {
                 return false;
             }
             symbolAdded = true;
         }
 
         if (!symbolAdded) {
             if (!addToSymbolTable("V", name, "lvar", new VariableData(type, "private"))) {
                 return false;
             }
         }
         /**
          * to this point
          */
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !assignment_expression()) {
                 errorList += "Invalid variable declaration. Invalid assignment expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
             errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean method_body() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "{" {variable_declaration} {statement} "}"
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
             errorList += "Method body must begin with an open block '{'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         if (lexicalAnalyzer.getToken().getType().equalsIgnoreCase(KeyConst.MODIFIER.name())) {
             errorList += "Method Body Error: cannot use modifier tags inside methods. " + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String errorCheck = errorList;
 
         if (type(lexicalAnalyzer.getToken().getType())) {
             while (variable_declaration()) {
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken().getType().equalsIgnoreCase(KeyConst.MODIFIER.name())) {
                     errorList += "Method Body Error: cannot use modifier tags inside methods. " + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (!type(lexicalAnalyzer.getToken().getType())) break;
             }
 
             if (!errorCheck.equals(errorList)) {
                 errorList += "Invalid variable_declaration in method body." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             lexicalAnalyzer.nextToken();
             return true;
         }
 
         while (statement()) {
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
         }
 
         if (!errorCheck.equals(errorList)) {
             errorList += "Invalid statement in method body." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean constructor_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: class_name "(" [parameter_list] ")" method_body
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             errorList += "Invalid class name in constructor declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String constructorName = lexicalAnalyzer.getToken().getName();
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             errorList += MISSING_OPENING_PARENTHESIS + " for constructor declaration. 'class " + constructorName + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += MISSING_CLOSING_PARENTHESIS + " for constructor declaration. 'class " + constructorName + "'\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             lexicalAnalyzer.nextToken();
             String key = "M" + methodId;
             if (!addToSymbolTable("M", constructorName, METHOD, new MethodData("public", new ArrayList<Parameter>(), constructorName))) {
                 return false;
             }
             incrementScope(constructorName, false);
 
             if (!method_body()) {
                 return false;
             }
 
             updateSymbolSize(key);
 
             decrementScope();
             return true;
         }
 
         String key = "M" + methodId;
         if (!addToSymbolTable("M", constructorName, METHOD, new MethodData("public", new ArrayList<Parameter>(), constructorName))) {
             return false;
         }
         incrementScope(constructorName, false);
         List<Parameter> parameterNames = new ArrayList<Parameter>();
 
         if (!parameter_list(parameterNames)) {
             errorList += "Invalid parameter list for constructor declaration. 'class " + constructorName + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         ((MethodData)symbolTable.get(key).getData()).setParameters(parameterNames);
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             errorList += MISSING_CLOSING_PARENTHESIS + " for constructor declaration. 'class " + constructorName + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (!method_body()) {
             return false;
         }
 
         updateSymbolSize(key);
 
         decrementScope();
         return true;
     }
 
     public boolean field_declaration(String accessMod, String type, String value) {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             // check format: "(" [parameter_list] ")" method_body
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid field declaration. NullTuple exception. " + MISSING_CLOSING_PARENTHESIS + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 String key = "M" + methodId;
                 if (!addToSymbolTable("M", value, "method", new MethodData(accessMod, new ArrayList<Parameter>(), type))) {
                     return false;
                 }
                 incrementScope(value, false);
                 lexicalAnalyzer.nextToken();
                 if (!method_body()) {
                     return false;
                 }
 
                 updateSymbolSize(key);
                 decrementScope();
                 return true;
             }
 
             String methodKey = "M" + methodId;
             if (!addToSymbolTable("M", value, "method", new MethodData(accessMod, new ArrayList<Parameter>(), type))) {
                 return false;
             }
             incrementScope(value, false);
             List<Parameter> parameters = new ArrayList<Parameter>();
 
             if (!parameter_list(parameters)) {
                 errorList += "Invalid parameter list in field declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             ((MethodData)symbolTable.get(methodKey).getData()).setParameters(parameters);
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += "Invalid field declaration." + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (!method_body()) {
                 return false;
             }
 
             updateSymbolSize(methodKey);
 
             decrementScope();
             return true;
 
         } else {
             boolean symbolAdded = false;
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             // check format: ["[" "]"] ["=" assignment_expression ] ";"
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 if (!addToSymbolTable("V", value, "ivar", new VariableData(type, accessMod))) {
                     return false;
                 }
                 lexicalAnalyzer.nextToken();
                 return true;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
                 lexicalAnalyzer.nextToken();
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                     errorList += "Invalid field declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (!addToSymbolTable("@", value, "ivar", new VariableData("@:" + type, accessMod))) {
                     return false;
                 }
                 symbolAdded = true;
                 if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                     lexicalAnalyzer.nextToken();
                     return true;
                 }
             }
 
             if (!symbolAdded) {
                 if (!addToSymbolTable("V", value, "ivar", new VariableData(type, accessMod))) {
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
 
                 lexicalAnalyzer.nextToken();
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !assignment_expression()) {
                     errorList += "Invalid field declaration. Invalid assignment expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
     }
 
     public boolean class_member_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(KeyConst.MODIFIER.getKey())) {
             // check format: modifier type identifier field_declaration
 
             String modifier = lexicalAnalyzer.getToken().getName();
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += "Invalid class member declaration. Missing a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
             String type = lexicalAnalyzer.getToken().getName();
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                 errorList += "Invalid class member declaration. Missing a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
             String name = lexicalAnalyzer.getToken().getName();
 
             lexicalAnalyzer.nextToken();
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !field_declaration(modifier, type, name)) {
                 errorList += "Invalid class member declaration. Invalid field declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             // check format: constructor_declaration
             if (!constructor_declaration()) {
                 errorList += "Invalid class member declaration. Invalid constructor declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         } else if (!lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Class methods and instance variable declarations must start with a valid modifier. " + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
         }
 
         return false;
     }
 
     public boolean class_declaration() {
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         // check format: "class" class_name "{" {class_member_declaration} "}"
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS.getKey())) {
             errorList += "Invalid class declaration. Missing 'class' tag." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             errorList += "Invalid class declaration. Missing a valid class name." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
 
         if (!addToSymbolTable("C", lexicalAnalyzer.getToken().getName(), CLASS, null)) {
             return false;
         }
         incrementScope(lexicalAnalyzer.getToken().getName(), true);
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
             errorList += "Invalid class declaration. Missing opening block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         while (class_member_declaration()) {
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Invalid class declaration. Missing closing block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         decrementScope();
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean compilation_unit() {
         // check format: {class_declaration} "void" "main" "(" ")" method_body
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Invalid compilation unit. Missing 'main' method.\n";
             return false;
         }
 
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (!lexicalAnalyzer.getToken().getName().equals(KeyConst.VOID.getKey())) {
             if (!class_declaration()) {
                 errorList += "Invalid compilation unit. Invalid class declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid compilation unit. Missing 'main' method.\n";
                 return false;
             }
 
             if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                 return false;
             }
 
             while (lexicalAnalyzer.getToken().getName().equals(KeyConst.CLASS.getKey())) {
                 if (!class_declaration()) {
                     errorList += "Invalid compilation unit. Invalid class declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid compilation unit. Missing 'main' method.\n";
                 return false;
             }
 
             if (!lexicalAnalyzer.getToken().getName().equals(KeyConst.VOID.getKey())) {
                 errorList += "Invalid compilation unit. expected 'void' but found '" + lexicalAnalyzer.getToken().getName() + "'."  + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getName().equals(KeyConst.MAIN.getKey())) {
             errorList += "Invalid compilation unit. Invalid name for method 'main'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             errorList += "Invalid compilation unit. Invalid 'main' method. " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             errorList += "Invalid compilation unit. Invalid 'main' method. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (isUnknownSymbol(lexicalAnalyzer.getToken().getType())) {
             return false;
         }
 
         String mainKey = "M" + methodId;
 
         if (!addToSymbolTable("M", "main", "method", new MethodData("public", new ArrayList<Parameter>(), "void"))) {
             return false;
         }
         incrementScope("main", true);
 
         // at this point we have declared classes and "void main()"
         if (!method_body()) {
             return false;
         }
 
         updateSymbolSize(mainKey);
 
         decrementScope();
         return true;
     }
 
     private boolean type(String itemType) {
         return (itemType.equals(KeyConst.INT.getKey()) || itemType.equals(KeyConst.CHAR.getKey()) || itemType.equals(KeyConst.BOOL.getKey()) || itemType.equals(KeyConst.VOID.getKey()) || itemType.equals(KeyConst.CLASS_NAME.getKey()));
     }
 
     private boolean isLogicalConnectiveExpression(String itemType) {
         return itemType.equals(LexicalAnalyzer.tokenTypesEnum.LOGICAL_OPR.name());
     }
 
     private boolean isBooleanExpression(String itemType) {
         return itemType.equals(LexicalAnalyzer.tokenTypesEnum.BOOLEAN_OPR.name());
     }
 
     private boolean isMathematicalExpression(String itemType) {
         return itemType.equals(LexicalAnalyzer.tokenTypesEnum.MATH_OPR.name());
     }
 
     private boolean isUnknownSymbol(String type) {
         if (type.equals(LexicalAnalyzer.tokenTypesEnum.UNKNOWN.name())) {
             errorList += "Unknown Symbol on" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return true;
         }
 
         return false;
     }
 
     private void decrementScope() {
         int scopeDepth = 0;
         for (char c : scope.toCharArray()) {
             if (c == '.') scopeDepth++;
         }
 
         if (scopeDepth > 1) {
             scope = scope.substring(0, scope.lastIndexOf("."));
         } else {
             scope = scope.substring(0, scope.lastIndexOf(".") + 1);
         }
     }
 
     private void incrementScope(String name, boolean isClass) {
         if (isClass) {
             scope += name;
         } else {
             scope += "." + name;
         }
     }
 
     private boolean addToSymbolTable(String key, String name, String kind, IData data) {
         for (Symbol temp : symbolTable.values()) {
             if (temp.getValue().equals(name) && temp.getScope().equals(scope)) {
 
                 if (kind.equals(LITERAL)) return true;
 
                 errorList += "duplicate symbol: symbol: '" + name + "' already exists in symbol table." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (kind.equals(LITERAL)) {
             symbolTable.put(key + variableId, new Symbol(scope, key + variableId++, name, kind, data, ELEM_SIZE, 1));
         } else if (key.equals("M")) {
             symbolTable.put(key + methodId, new Symbol(scope, key + methodId, name, kind, data, 0, 1));
             symbolTable.get(key + methodId).setObjectSize(3);
             methodId++;
         } else if (key.equals("C")) {
             symbolTable.put(key + variableId, new Symbol(scope, key + variableId++, name, kind, data, 1, 0));
         } else {
             Symbol method = getMethodSymbol();
             if (key.equals("P")) {
                 symbolTable.put(key + variableId, new Symbol(scope, key + variableId++, name, kind, data, method.getObjectSize(), 1));
                 method.updateObjectSize(1);
             } else if (kind.equals("ivar")) {
                 Symbol classObj = getClassSymbol();
                 symbolTable.put(key + variableId, new Symbol(scope, key + variableId++, name, kind, data, classObj.getSize(), 1));
                 classObj.updateSize(1);
             } else {
                 symbolTable.put(key + variableId, new Symbol(scope, key + variableId++, name, kind, data, method.getSize(), 1));
                 method.updateSize(1);
             }
         }
         return true;
     }
 
     private Symbol getMethodSymbol() {
         String name = scope.substring(scope.lastIndexOf(".") + 1, scope.length());
         String localScope = scope.substring(0, scope.lastIndexOf("."));
         if (localScope.equals("g")) {
             localScope += ".";
         }
 
         for (Symbol symbol : symbolTable.values()) {
             if (symbol.getData() instanceof MethodData) {
                 if (symbol.getScope().equals(localScope) && symbol.getValue().equals(name)) {
                     return symbol;
                 }
             }
         }
 
         return null;
     }
 
     private Symbol getClassSymbol() {
         String name = scope.substring(scope.lastIndexOf(".") + 1, scope.length());
         String localScope = scope.substring(0, scope.lastIndexOf("."));
         if (localScope.equals("g")) {
             localScope += ".";
         }
 
         for (Symbol symbol : symbolTable.values()) {
             if (symbol.getKind().equals(Compiler.CLASS)) {
                 if (symbol.getScope().equals(localScope) && symbol.getValue().equals(name)) {
                     return symbol;
                 }
             }
         }
 
         return null;
     }
 
     private void updateSymbolSize(String key) {
         int newSize = 0;
 
         for(Symbol temp : symbolTable.values()) {
             if (temp.getScope().equals(scope) && !temp.getSymId().startsWith("L") && !temp.getSymId().startsWith("P")) {
                 newSize++;
             }
         }
 
         int objectSize = 3;
         for(Symbol temp : symbolTable.values()) {
             if (temp.getScope().equals(scope) && temp.getSymId().startsWith("P")) {
                 objectSize++;
             }
         }
 
         symbolTable.get(key).setSize(newSize);
         symbolTable.get(key).setObjectSize(objectSize);
     }
 }
