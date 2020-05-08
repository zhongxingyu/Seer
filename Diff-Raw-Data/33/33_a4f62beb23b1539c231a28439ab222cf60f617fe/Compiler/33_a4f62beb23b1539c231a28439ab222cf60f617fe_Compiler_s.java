 package project;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 4/27/13
  * Time: 8:40 AM
  */
 public class Compiler {
     private static final String ILLEGAL_EXPRESSION = "Illegal expression.";
     private static final String ILLEGAL_NEW_DECLARATION = "Illegal new_declaration";
     private static final String ILLEGAL_NEW_OPERATION = "Illegal new operation.";
     private static final String ILLEGAL_ATOI_OPERATION = "Illegal atoi operation.";
     private static final String ILLEGAL_ITOA_OPERATION = "Illegal itoa operation";
 
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
 
     private LexicalAnalyzer lexicalAnalyzer;
     private String errorList = "";
 
     public Compiler(LexicalAnalyzer lexicalAnalyzer) {
         this.lexicalAnalyzer = lexicalAnalyzer;
     }
 
     public void evaluate() {
         // pass one
         if (!compiliation_unit()) {
             System.out.print(errorList);
             System.exit(0);
         }
     }
 
     private boolean new_declaration() {
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" [argument_list] ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 lexicalAnalyzer.nextToken();
                 return true;
             }
 
             String errorCheck = errorList;
 
             if (!argument_list()) {
                 if (!errorCheck.equals(errorList)) {
                     errorList += ILLEGAL_NEW_DECLARATION + ARGUMENT_LIST + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
             return true;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             // check format: "[" expression "]"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!expression()) {
                 errorList += ILLEGAL_NEW_DECLARATION + EXPRESSION + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += ILLEGAL_NEW_DECLARATION + OPERATION + ", " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
             return true;
         }
 
         return false;
     }
 
     private boolean assignment_expression() {
         if (lexicalAnalyzer.getToken().getType().equals(KeyConst.NEW.getKey())) {
 
             // check format: "new" type new_declaration
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += ILLEGAL_NEW_OPERATION + " " + INVALID_TYPE + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !new_declaration()) {
                 errorList += ILLEGAL_NEW_OPERATION + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
            lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ATOI.getKey())) {
 
             // check format: "atoi" "(" expression ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += ILLEGAL_ATOI_OPERATION + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += ILLEGAL_ATOI_OPERATION + " Invalid Expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_ATOI_OPERATION + " atoi can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.ITOA.getKey())) {
 
             // check format: "itoa" "(" expression ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += ILLEGAL_ITOA_OPERATION + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += ILLEGAL_ITOA_OPERATION + " Invalid Expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += ILLEGAL_ITOA_OPERATION + " itoa can only contain one parameter. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.THIS.getKey())) {
             lexicalAnalyzer.nextToken();
             return true;
         } else {
             if (!expression()) {
                 errorList += "Invalid assignment expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
             return true;
         }
     }
 
     public boolean expressionz() {
         Tuple token = lexicalAnalyzer.getToken();
         if (token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name()) || isLogicalConnectiveExpression(token.getType()) || isBooleanExpression(token.getType()) || isMathematicalExpression(token.getType())) {
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "expressionz missing right hand expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (token.getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
                 if (!assignment_expression()) {
                     errorList += "Invalid assignment expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                     return false;
                 }
             } else {
                 if (!expression()) {
                     errorList += "Invalid expressionz expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             return true;
         }
         return false;
     }
 
     public boolean expression() {
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" expression ")" [expressionz]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += ILLEGAL_EXPRESSION + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
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
 
             String errCheck = errorList;
             expressionz();
 
             return errCheck.equals(errorList);
 
         } else if (lexicalAnalyzer.getToken().getType().equals(KeyConst.TRUE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.FALSE.getKey()) || lexicalAnalyzer.getToken().getType().equals(KeyConst.NULL.getKey()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.NUMBER.name()) || lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.CHARACTER.name())) {
 
             // check format: "value" [expressionz]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             String errCheck = errorList;
             expressionz();
 
             return errCheck.equals(errorList);
 
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
 
             // check format: identifier [ fn_arr_member ] [ member_refz ] [ expressionz ]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
             String errCheck = errorList;
 
             fn_arr_member();
             if (!errCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             member_refz();
             if (!errCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 return true;
             }
 
             expressionz();
 
             return errCheck.equals(errorList);
         }
         return false;
     }
 
     private boolean argument_list() {
 
         // check format: expression { "," expression}
         if (!expression()) {
             errorList += INVALID_ARGUMENT_LIST + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
             return false;
         }
 
         while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getLexi().equals(",")) {
             lexicalAnalyzer.nextToken();
             if (!expression()) {
                 errorList += INVALID_ARGUMENT_LIST + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
         }
         return true;
     }
 
     private boolean fn_arr_member() {
 
         // check format: "(" [ argument_list ] ")" | "[" expression "]"
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             //check format: "(" [ argument_list ] ")"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += INVALID_FUNCTION + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 return true;
             }
 
             if (!argument_list()) {
                 errorList += INVALID_ARGUMENT_LIST + " in function." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_FUNCTION + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
             //check format: "[" expression "]"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "Invalid array expression." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                 errorList += MISSING_ARRAY_CLOSE + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
         }
         return false;
     }
 
     private boolean member_refz() {
 
         // check format: "." identifier [ fn_arr_member ] [ member_refz ]
         if (!lexicalAnalyzer.getToken().getLexi().equals(".")) {
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Invalid member ref." + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             return true;
         }
 
         String errCheck = errorList;
 
         fn_arr_member();
         if (!errCheck.equals(errorList)) {
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             return true;
         }
 
         member_refz();
         return errCheck.equals(errorList);
     }
 
     public boolean statement() {
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
 
             // check format: "(" {statement} ")"
             String errorCheck = errorList;
 
             lexicalAnalyzer.nextToken();
             while (statement()) {
                 lexicalAnalyzer.nextToken();
             }
 
             if (!errorCheck.equals(errorList)) {
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.IF.getKey())) {
 
             // check format: "if" "(" expression ")" statement [ "else" statement ]
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'if' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!statement()) {
                 errorList += INVALID_STATEMENT + " 'if' statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.ELSE.getKey())) {
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !statement()) {
                     errorList += INVALID_STATEMENT + " a valid statement is required after an else statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.WHILE.getKey())) {
 
             // check format: "while" "(" expression ")" statement
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'while' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += INVALID_STATEMENT + " " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (!statement()) {
                 errorList += INVALID_STATEMENT + " 'while' statement." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.RETURN.getKey())) {
 
             // check format: "return" [ expression ] ";"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += INVALID_STATEMENT + " 'return' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 return true;
             }
 
             if (!expression()) {
                 errorList += "Invalid 'return' statement expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'return' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.COUT.getKey())) {
 
             // check format: "cout" "<<" expression ";"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals("<<")) {
                 errorList += INVALID_STATEMENT + "'cout' statement missing extraction operator." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'cout' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'cout' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else if (lexicalAnalyzer.getToken().getLexi().equals(KeyConst.CIN.getKey())) {
 
             // check format: "cin" ">>" expression ";"
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals(">>")) {
                 errorList += INVALID_STATEMENT + "'cin' statement missing extraction operator." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !expression()) {
                 errorList += "'cin' statement requires a valid expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'cin' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return true;
 
         } else {
 
             // check format: expression ";"
             if (!expression()) {
                errorList += "Invalid statement expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.EOT.name())) {
                 errorList += INVALID_STATEMENT + " 'expression' statement must end with a ';'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
             return true;
         }
     }
 
     public boolean parameter() {
         // check format: type identifier ["[" "]"]
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
             errorList += "Parameter declarations must start with a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
             errorList += "Parameter declarations require a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
             return true;
         }
 
         lexicalAnalyzer.getToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
             errorList += "Invalid parameter declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         return true;
     }
 
     public boolean parameter_list() {
         // check format: parameter { "," parameter}
         if (!parameter()) {
             errorList += "Invalid parameter_list" + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
             return false;
         }
 
         while (!(lexicalAnalyzer.getToken() instanceof NullTuple) && lexicalAnalyzer.getToken().getLexi().equals(",")) {
             lexicalAnalyzer.nextToken();
             if (!parameter()) {
                 errorList += "Invalid parameter_list" + LINE + lexicalAnalyzer.previousToken().getLineNum() + "\n";
                 return false;
             }
         }
         return true;
     }
 
     public boolean variable_declaration() {
         // check format: type identifier ["[" "]"] ["=" assignment_expression ] ";"
        if (!type(lexicalAnalyzer.getToken().getType())) {
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += "Variable declarations must start with a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                 errorList += "Variable declarations require a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
                 lexicalAnalyzer.getToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                     errorList += "Invalid parameter declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid variable declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !assignment_expression()) {
                     errorList += "Invalid variable declaration. Invalid assignment expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
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
         return false;
     }
 
     public boolean method_body() {
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
 
         String errorCheck = errorList;
 
         while (variable_declaration()) {
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (!errorCheck.equals(errorList)) {
             errorList += "Invalid variable_declaration in method body." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         while (statement()) {
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (!errorCheck.equals(errorList)) {
             errorList += "Invalid statement in method body." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
        lexicalAnalyzer.getToken();
         return true;
     }
 
     public boolean constructor_declaration() {
         // check format: class_name "(" [parameter_list] ")" method_body
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey())) {
             errorList += "Invalid class name in constructor declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         String className = lexicalAnalyzer.getToken().getLexi();
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             errorList += MISSING_OPENING_PARENTHESIS + " for constructor declaration. 'class " + className + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += MISSING_CLOSING_PARENTHESIS + " for constructor declaration. 'class " + className + "'\n";
             return false;
         }
 
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             return true;
         }
 
         if (!parameter_list()) {
             errorList += "Invalid parameter list for constructor declaration. 'class " + className + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             errorList += MISSING_CLOSING_PARENTHESIS + " for constructor declaration. 'class " + className + "'" + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return method_body();
     }
 
     public boolean field_declaration() {
         if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             // check format: "(" [parameter_list] ")" method_body
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid field declaration. NullTuple exception. " + MISSING_CLOSING_PARENTHESIS + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 lexicalAnalyzer.nextToken();
                 return true;
             }
 
             if (!parameter_list()) {
                 errorList += "Invalid parameter list in field declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
                 errorList += "Invalid field declaration." + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             return method_body();
 
         } else {
             // check format: ["[" "]"] ["=" assignment_expression ] ";"
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_BEGIN.name())) {
 
                 lexicalAnalyzer.getToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ARRAY_END.name())) {
                     errorList += "Invalid field declaration. Missing closing array bracket." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.ASSIGNMENT_OPR.name())) {
 
                 lexicalAnalyzer.nextToken();
                 if (lexicalAnalyzer.getToken() instanceof NullTuple || !assignment_expression()) {
                     errorList += "Invalid field declaration. Invalid assignment expression." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
 
                 if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                     errorList += "Invalid field declaration. Missing semi-colon at end." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
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
         if (lexicalAnalyzer.getToken().getType().equals(KeyConst.MODIFIER.getKey())) {
             // check format: modifier type identifier field_declaration
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !type(lexicalAnalyzer.getToken().getType())) {
                 errorList += "Invalid class member declaration. Missing a valid type." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.IDENTIFIER.name())) {
                 errorList += "Invalid class member declaration. Missing a valid identifier." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             lexicalAnalyzer.nextToken();
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !field_declaration()) {
                 errorList += "Invalid class member declaration. Invalid field declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
 
        } else {
             // check format: constructor_declaration
             if (!constructor_declaration()) {
                 errorList += "Invalid class member declaration. Invalid constructor declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             return true;
         }
     }
 
     public boolean class_declaration() {
         // check format: "class" class_name "{" {class_member_declaration} "}"
        if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS.getKey()))  {
             errorList += "Invalid class declaration. Missing 'class' tag." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
        if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(KeyConst.CLASS_NAME.getKey()))  {
             errorList += "Invalid class declaration. Missing a valid class name." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_BEGIN.name())) {
             errorList += "Invalid class declaration. Missing opening block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         while (class_member_declaration()) {
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Method body must end with a closing block '}'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
         }
 
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.BLOCK_END.name())) {
             errorList += "Invalid class declaration. Missing closing block." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return true;
     }
 
     public boolean compiliation_unit() {
         // check format: {class_declaration} "void" "main" "(" ")" method_body
         if (lexicalAnalyzer.getToken() instanceof NullTuple) {
             errorList += "Invalid compilation unit. Missing 'main' method.\n";
             return false;
         }
 
         if (!lexicalAnalyzer.getToken().getLexi().equals(KeyConst.VOID.getKey())) {
             if (!class_declaration()) {
                 errorList += "Invalid compilation unit. Invalid class declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                 return false;
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple) {
                 errorList += "Invalid compilation unit. Missing 'main' method.\n";
                 return false;
             }
 
            while(lexicalAnalyzer.getToken().getLexi().equals(KeyConst.CLASS.getKey())) {
                 if (!class_declaration()) {
                     errorList += "Invalid compilation unit. Invalid class declaration." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
                     return false;
                 }
             }
 
             if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals(KeyConst.VOID.getKey())) {
                 errorList += "Invalid compilation unit. Missing 'main' method.\n";
                 return false;
             }
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getLexi().equals(KeyConst.MAIN.getKey())) {
             errorList += "Invalid compilation unit. Invalid name for method 'main'." + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_OPEN.name())) {
             errorList += "Invalid compilation unit. Invalid 'main' method. " + MISSING_OPENING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         if (lexicalAnalyzer.getToken() instanceof NullTuple || !lexicalAnalyzer.getToken().getType().equals(LexicalAnalyzer.tokenTypesEnum.PAREN_CLOSE.name())) {
             errorList += "Invalid compilation unit. Invalid 'main' method. " + MISSING_CLOSING_PARENTHESIS + LINE + lexicalAnalyzer.getToken().getLineNum() + "\n";
             return false;
         }
 
         lexicalAnalyzer.nextToken();
         return method_body();
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
 }
