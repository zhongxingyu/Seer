 package joos1syntax;
 
 import joos1lexer.Lexer;
 import joos1lexer.Token;
 import joos1lexer.TokenConstants;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 public final class SyntaxChecker {
   protected TokenConstants constants;
   protected Token currentToken;
   protected TokenStream tokenStream;
 
   public SyntaxChecker() {
     constants = new TokenConstants();
   }
 
   public String init(Reader in) throws IOException {
     Lexer lexer = new Lexer(in);
     tokenStream = new TokenStream();
     Token err = tokenStream.init(lexer);
     if (err != null)
       return "Lexical error on line " + err.line() + ", col " + err.column() +
         ":\n" + err.text();
     else
       return null;
   }
 
   public void check() {
     currentToken = tokenStream.get();
     checkProgram();
   }
 
   protected void checkProgram() {
     if (currentToken.kind() == constants.PACKAGE)
       checkPackageDeclaration();
     while (currentToken.kind() == constants.IMPORT)
       checkImportDeclaration();
     checkClassDeclaration();
     accept(constants.EOT);
   }
 
   protected void checkPackageDeclaration() {
     accept(constants.PACKAGE);
     checkName();
     accept(constants.SEMICOLON);
   }
 
   protected void checkImportDeclaration() {
     accept(constants.IMPORT);
     accept(constants.IDENTIFIER);
     accept(constants.DOT);
     if (currentToken.kind() == constants.STAR) {
       acceptIt();
       accept(constants.SEMICOLON);
       return;
     } else {
       accept(constants.IDENTIFIER);
     }
     while (currentToken.kind() == constants.DOT) {
       acceptIt();
       if (currentToken.kind() == constants.STAR) {
         acceptIt();
         accept(constants.SEMICOLON);
         return;
       } else {
         accept(constants.IDENTIFIER);
       }
     }
     accept(constants.SEMICOLON);
   }
 
   protected void checkClassDeclaration() {
     accept(constants.PUBLIC);
     if (currentToken.kind() == constants.FINAL)
       acceptIt();
     if (currentToken.kind() == constants.ABSTRACT)
       acceptIt();
     accept(constants.CLASS);
     accept(constants.IDENTIFIER);
     if (currentToken.kind() == constants.EXTENDS) {
       acceptIt();
       checkName();
     }
     if (currentToken.kind() == constants.IMPLEMENTS) {
       acceptIt();
       checkName();
       while (currentToken.kind() == constants.COMMA) {
         acceptIt();
         checkName();
       }
     }
     checkClassBody();
   }
 
   protected void checkClassBody() {
     accept(constants.L_BRACE);
 
     while (isAccess(currentToken)) {
       Token next = tokenStream.lookAhead(0);
       if (next.kind() == constants.ABSTRACT ||
           next.kind() == constants.FINAL ||
           next.kind() == constants.STATIC) {
         checkMethodDeclaration();
       } else {
         acceptIt(); // Accept access modifier
         if (currentToken.kind() == constants.VOID) {
           // Void method
           acceptIt();
           accept(constants.IDENTIFIER);
           checkFormalParameterList();
           if (currentToken.kind() == constants.THROWS)
             checkThrowsClause();
           checkBlock();
         } else {
           boolean possiblyConstructor = true;
           if (isPrimitive(currentToken)) {
             acceptIt();
             possiblyConstructor = false;
           } else {
             accept(constants.IDENTIFIER);
             while (currentToken.kind() == constants.DOT) {
               acceptIt();
               accept(constants.IDENTIFIER);
               possiblyConstructor = false;
             }
           }
           if (possiblyConstructor && currentToken.kind() == constants.L_PAREN) {
             // Constructor
             checkFormalParameterList();
             if (currentToken.kind() == constants.THROWS)
               checkThrowsClause();
             checkBlock();
           } else {
             if (currentToken.kind() == constants.L_BRACKET) {
               acceptIt();
               accept(constants.R_BRACKET);
             }
             accept(constants.IDENTIFIER);
             if (currentToken.kind() == constants.L_PAREN) {
               // Method
               checkFormalParameterList();
               if (currentToken.kind() == constants.THROWS)
                 checkThrowsClause();
               checkBlock();
             } else {
               // Field
               if (currentToken.kind() == constants.ASSIGN) {
                 acceptIt();
                 checkExpression();
               }
               accept(constants.SEMICOLON);
             }
           }
         }
       }
     }
 
     accept(constants.R_BRACE);
   }
 
   protected void checkExpression() {
     checkUnaryExpression();
     if (currentToken.kind() == constants.ASSIGN) {
       // TODO: Use the above unary expression to check
       //       when this assignment is allowed.
       acceptIt();
       checkExpression();
     } else {
       while (isBinaryOperator(currentToken)) {
         acceptIt();
         checkUnaryExpression();
       }
       if (currentToken.kind() == constants.INSTANCEOF) {
         acceptIt();
         if (isPrimitive(currentToken)) {
           acceptIt();
           accept(constants.L_BRACKET);
           accept(constants.R_BRACKET);
         } else {
           checkTypeExp();
         }
       }
     }
   }
 
   protected void checkStatementExpression() {
     checkUnaryExpression();
     if (currentToken.kind() == constants.ASSIGN) {
       // TODO: Use the above unary expression to check
       //       when this assignment is allowed.
       acceptIt();
       checkExpression();
     }
   }
 
   protected void checkUnaryExpression() {
     if (currentToken.kind() == constants.MINUS ||
         currentToken.kind() == constants.COMPLEMENT) {
       // TODO: Can't be an assignment
       acceptIt();
       checkUnaryExpression();
     } else if (currentToken.kind() == constants.IDENTIFIER ||
                isLiteral(currentToken) ||
                currentToken.kind() == constants.THIS ||
                currentToken.kind() == constants.NEW) {
       // TODO: When can this be an assignment?
       checkPostfixExpression();
     } else if (currentToken.kind() == constants.L_PAREN) {
       // TODO: Can't be an assignment according to reference compiler
      Token leftParenSuccessor = tokenStream.lookAhead(0);
       int off = 0;
       while (tokenStream.lookAhead(off).kind() != constants.R_PAREN &&
              tokenStream.lookAhead(off).kind() != constants.EOT) {
        // TODO: This while loop doesn't work as intended: It stops at
        //       the first ')' encountered rather than at the *matching* ')'.
         off = off + 1;
       }
       if (tokenStream.lookAhead(off).kind() == constants.R_PAREN) {
         Token next = tokenStream.lookAhead(off + 1);
        if (isPrimitive(leftParenSuccessor))
          checkCastExpression();
        else if (predictsUnary(next) && next.kind() != constants.MINUS)
           checkCastExpression();
         else
           checkPostfixExpression();
       } else {
         syntaxError("Unmatched parenthesis",
                     currentToken.line(), currentToken.column());
       }
     } else {
       syntaxError("Missing expression or invalid start of expression",
                   currentToken.line(), currentToken.column());
     }
   }
 
   protected void checkPostfixExpression() {
     checkPrimaryExpression();
     while (currentToken.kind() == constants.L_BRACKET ||
            currentToken.kind() == constants.L_PAREN ||
            currentToken.kind() == constants.DOT) {
       if (currentToken.kind() == constants.L_BRACKET) {
         acceptIt();
         checkExpression();
         accept(constants.R_BRACKET);
       } else if (currentToken.kind() == constants.L_PAREN) {
         checkArgumentList();
       } else { // "." IDENTIFIER
         accept(constants.DOT);
         accept(constants.IDENTIFIER);
       }
     }
   }
 
   protected void checkPrimaryExpression() {
     if (currentToken.kind() == constants.IDENTIFIER ||
         isLiteral(currentToken) ||
         currentToken.kind() == constants.THIS) {
       acceptIt();
     } else if (currentToken.kind() == constants.L_PAREN) {
       checkParExpression();
     } else if (currentToken.kind() == constants.NEW) {
       acceptIt();
       if (isPrimitive(currentToken)) {
         acceptIt();
         accept(constants.L_BRACKET);
         checkExpression();
         accept(constants.R_BRACKET);
       } else {
         checkName();
         if (currentToken.kind() == constants.L_BRACKET) {
           acceptIt();
           checkExpression();
           accept(constants.R_BRACKET);
         } else {
           checkArgumentList();
         }
       }
     } else {
       syntaxError("Invalid primary expression",
                   currentToken.line(), currentToken.column());
     }
   }
 
   protected void checkCastExpression() {
     accept(constants.L_PAREN);
     checkTypeExp();
     accept(constants.R_PAREN);
     checkUnaryExpression();
   }
 
   protected void checkParExpression() {
     accept(constants.L_PAREN);
     checkExpression();
     accept(constants.R_PAREN);
   }
 
   protected void checkArgumentList() {
     accept(constants.L_PAREN);
     if (currentToken.kind() != constants.R_PAREN) {
       checkExpression();
       while (currentToken.kind() == constants.COMMA) {
         acceptIt();
         checkExpression();
       }
     }
     accept(constants.R_PAREN);
   }
 
   protected void checkMethodDeclaration() {
     if (isAccess(currentToken))
       acceptIt();
     else
       syntaxError("Member declaration must begin with 'public' or 'protected'" +
                   "; found '" + constants.spell(currentToken.kind()) + "'",
                   currentToken.line(), currentToken.column());
     Token mod = null;
     if (isModifier(currentToken)) {
       mod = currentToken;
       acceptIt();
     }
     checkTypeExp();
     accept(constants.IDENTIFIER);
     if (mod != null) {
       if (currentToken.kind() == constants.SEMICOLON ||
           currentToken.kind() == constants.ASSIGN)
         syntaxError("Only methods can have modifiers",
                     mod.line(), mod.column());
     }
     checkFormalParameterList();
     if (currentToken.kind() == constants.THROWS)
       checkThrowsClause();
     if (mod != null && mod.kind() == constants.ABSTRACT)
       accept(constants.SEMICOLON);
     else
       checkBlock();
   }
 
   protected void checkBlock() {
     accept(constants.L_BRACE);
 
     boolean done = false;
     while (!done) {
       if (currentToken.kind() == constants.IF ||
           currentToken.kind() == constants.WHILE ||
           currentToken.kind() == constants.FOR ||
           currentToken.kind() == constants.L_BRACE ||
           currentToken.kind() == constants.L_PAREN ||
           currentToken.kind() == constants.SEMICOLON ||
           currentToken.kind() == constants.RETURN ||
           currentToken.kind() == constants.NEW ||
           currentToken.kind() == constants.INTEGER_LITERAL ||
           currentToken.kind() == constants.CHAR_LITERAL ||
           currentToken.kind() == constants.STRING_LITERAL ||
           currentToken.kind() == constants.THIS) {
         checkStatement();
       } else if (currentToken.kind() == constants.VOID ||
                  isPrimitive(currentToken)) {
         checkLocalDeclarationStatement();
       } else if (currentToken.kind() == constants.IDENTIFIER) {
         int off = 0;
         while (tokenStream.lookAhead(off).kind() == constants.DOT) {
           off = off + 1;
           if (tokenStream.lookAhead(off).kind() == constants.IDENTIFIER)
             off = off + 1;
         }
         if (tokenStream.lookAhead(off).kind() == constants.L_PAREN) {
           checkStatement();
         } else if (tokenStream.lookAhead(off).kind() == constants.L_BRACKET) {
           if (tokenStream.lookAhead(off+1).kind() == constants.R_BRACKET)
             checkLocalDeclarationStatement();
           else
             checkStatement();
         } else if (tokenStream.lookAhead(off).kind() == constants.IDENTIFIER) {
           checkLocalDeclarationStatement();
         } else if (tokenStream.lookAhead(off).kind() == constants.ASSIGN) {
           checkStatement();
         } else {
           syntaxError("Expected statement or local declaration",
                       currentToken.line(), currentToken.column());
         }
       } else {
         done = true;
       }
     }
 
     accept(constants.R_BRACE);
   }
 
   protected void checkStatement() {
     if (currentToken.kind() == constants.IF)
       checkIfStatement();
     else if (currentToken.kind() == constants.WHILE)
       checkWhileStatement();
     else if (currentToken.kind() == constants.FOR)
       checkForStatement();
     else if (currentToken.kind() == constants.L_BRACE)
       checkBlock();
     else if (currentToken.kind() == constants.SEMICOLON)
       acceptIt();
     else if (currentToken.kind() == constants.RETURN)
       checkReturnStatement();
     else
       checkExpressionStatement();
   }
 
   protected void checkIfStatement() {
     accept(constants.IF);
     accept(constants.L_PAREN);
     checkExpression();
     accept(constants.R_PAREN);
     checkStatement();
     if (currentToken.kind() == constants.ELSE) {
       acceptIt();
       checkStatement();
     }
   }
 
   protected void checkWhileStatement() {
     accept(constants.WHILE);
     accept(constants.L_PAREN);
     checkExpression();
     accept(constants.R_PAREN);
     checkStatement();
   }
 
   protected void checkForStatement() {
     accept(constants.FOR);
     accept(constants.L_PAREN);
     if (currentToken.kind() != constants.SEMICOLON)
       checkForInitializer();
     accept(constants.SEMICOLON);
     if (currentToken.kind() != constants.SEMICOLON)
       checkExpression();
     accept(constants.SEMICOLON);
     if (currentToken.kind() != constants.R_PAREN)
       checkStatementExpression();
     accept(constants.R_PAREN);
     checkStatement();
   }
 
   protected void checkForInitializer() {
     if (predictsUnary(currentToken))
       checkStatementExpression();
     else
       checkLocalDeclaration();
   }
 
   protected void checkReturnStatement() {
     accept(constants.RETURN);
     if (currentToken.kind() != constants.SEMICOLON)
       checkExpression();
     accept(constants.SEMICOLON);
   }
 
   protected void checkExpressionStatement() {
     checkStatementExpression();
     accept(constants.SEMICOLON);
   }
 
   protected void checkLocalDeclarationStatement() {
     checkLocalDeclaration();
     accept(constants.SEMICOLON);
   }
 
   protected void checkLocalDeclaration() {
     checkTypeExp();
     accept(constants.IDENTIFIER);
     if (currentToken.kind() == constants.ASSIGN) {
       acceptIt();
       checkExpression();
     }
   }
 
   protected void checkThrowsClause() {
     accept(constants.THROWS);
     checkName();
     while (currentToken.kind() == constants.COMMA) {
       acceptIt();
       checkName();
     }
   }
 
   protected void checkFormalParameterList() {
     accept(constants.L_PAREN);
     if (currentToken.kind() != constants.R_PAREN) {
       checkFormalParameter();
       while (currentToken.kind() == constants.COMMA) {
         acceptIt();
         checkFormalParameter();
       }
     }
     accept(constants.R_PAREN);
   }
 
   protected void checkFormalParameter() {
     checkTypeExp();
     accept(constants.IDENTIFIER);
   }
 
   protected void checkTypeExp() {
     if (currentToken.kind() == constants.VOID) {
       acceptIt();
     } else {
       checkType();
       if (currentToken.kind() == constants.L_BRACKET) {
         acceptIt();
         accept(constants.R_BRACKET);
       }
     }
   }
 
   protected void checkType() {
     if (currentToken.kind() == constants.BOOLEAN ||
         currentToken.kind() == constants.BYTE ||
         currentToken.kind() == constants.CHAR ||
         currentToken.kind() == constants.INT ||
         currentToken.kind() == constants.SHORT) {
       acceptIt();
     } else {
       checkName();
     }
   }
 
   protected void checkName() {
     accept(constants.IDENTIFIER);
     while (currentToken.kind() == constants.DOT) {
       acceptIt();
       accept(constants.IDENTIFIER);
     }
   }
 
   protected boolean predictsUnary(Token t) {
     return t.kind() == constants.MINUS ||
       t.kind() == constants.COMPLEMENT ||
       t.kind() == constants.L_PAREN ||
       t.kind() == constants.IDENTIFIER ||
       isLiteral(t) ||
       t.kind() == constants.THIS ||
       t.kind() == constants.NEW;
   }
 
   protected boolean isAccess(Token t) {
     return t.kind() == constants.PUBLIC || t.kind() == constants.PROTECTED;
   }
 
   protected boolean isBinaryOperator(Token t) {
     return t.kind() == constants.OR_OR ||
       t.kind() == constants.AND_AND ||
       t.kind() == constants.OR ||
       t.kind() == constants.XOR ||
       t.kind() == constants.AND ||
       t.kind() == constants.EQ ||
       t.kind() == constants.NEQ ||
       t.kind() == constants.LT ||
       t.kind() == constants.GT ||
       t.kind() == constants.LTEQ ||
       t.kind() == constants.GTEQ ||
       t.kind() == constants.PLUS ||
       t.kind() == constants.MINUS ||
       t.kind() == constants.STAR ||
       t.kind() == constants.DIV ||
       t.kind() == constants.MOD;
   }
 
   protected boolean isLiteral(Token t) {
     return t.kind() == constants.INTEGER_LITERAL ||
       t.kind() == constants.CHAR_LITERAL ||
       t.kind() == constants.STRING_LITERAL ||
       t.kind() == constants.NULL ||
       t.kind() == constants.TRUE ||
       t.kind() == constants.FALSE;
   }
 
   protected boolean isModifier(Token t) {
     return t.kind() == constants.ABSTRACT ||
       t.kind() == constants.FINAL ||
       t.kind() == constants.STATIC;
   }
 
   protected boolean isPrimitive(Token t) {
     return t.kind() == constants.BOOLEAN ||
       t.kind() == constants.BYTE ||
       t.kind() == constants.CHAR ||
       t.kind() == constants.INT ||
       t.kind() == constants.SHORT;
   }
 
   protected void accept(byte expected) {
     accept(expected, (String) null);
   }
 
   protected void accept(byte expected, String source) {
     if (source == null)
       source = "";
     else
       source = source + ": ";
 
     if (currentToken.kind() == expected)
       acceptIt();
     else
       syntaxError(source + "expected '" + constants.spell(expected) +
                   "' but saw " + debug(currentToken),
                   currentToken.line(), currentToken.column());
   }
 
   protected void acceptIt() {
     currentToken = tokenStream.get();
   }
 
   protected void syntaxError(String msg, int line, int col) {
     System.err.println("Syntax error on line " + line + ", col " + col + ":\n" +
                        msg);
     System.exit(1);
   }
 
   protected String debug(Token t) {
     if (t.kind() == constants.IDENTIFIER)
       return "IDENTIFIER[" + t.text() + "]";
     if (t.kind() == constants.INTEGER_LITERAL)
       return "INTEGER_LITERAL[" + t.text() + "]";
     if (t.kind() == constants.STRING_LITERAL)
       return "STRING_LITERAL[" + t.text() + "]";
     if (t.kind() == constants.CHAR_LITERAL)
       return "CHAR_LITERAL[" + t.text() + "]";
     if (t.kind() == constants.TRUE || t.kind() == constants.FALSE)
       return "BOOLEAN_LITERAL[" + t.text() + "]";
     return "'" + t.text() + "'";
   }
 }
