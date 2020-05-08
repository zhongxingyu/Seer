 package de.skuzzle.polly.parsing;
 
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 
 
 import de.skuzzle.polly.parsing.PrecedenceTable.PrecedenceLevel;
 import de.skuzzle.polly.parsing.declarations.Declaration;
 import de.skuzzle.polly.parsing.declarations.FunctionDeclaration;
 import de.skuzzle.polly.parsing.declarations.VarDeclaration;
 import de.skuzzle.polly.parsing.tree.AssignmentExpression;
 import de.skuzzle.polly.parsing.tree.BinaryExpression;
 import de.skuzzle.polly.parsing.tree.CastExpression;
 import de.skuzzle.polly.parsing.tree.Expression;
 import de.skuzzle.polly.parsing.tree.IfExpression;
 import de.skuzzle.polly.parsing.tree.NamespaceAccessExpression;
 import de.skuzzle.polly.parsing.tree.Root;
 import de.skuzzle.polly.parsing.tree.TernaryExpression;
 import de.skuzzle.polly.parsing.tree.TypeParameterExpression;
 import de.skuzzle.polly.parsing.tree.UnaryExpression;
 import de.skuzzle.polly.parsing.tree.VarOrCallExpression;
 import de.skuzzle.polly.parsing.tree.literals.BooleanLiteral;
 import de.skuzzle.polly.parsing.tree.literals.ChannelLiteral;
 import de.skuzzle.polly.parsing.tree.literals.CommandLiteral;
 import de.skuzzle.polly.parsing.tree.literals.DateLiteral;
 import de.skuzzle.polly.parsing.tree.literals.HelpLiteral;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 import de.skuzzle.polly.parsing.tree.literals.ListLiteral;
 import de.skuzzle.polly.parsing.tree.literals.Literal;
 import de.skuzzle.polly.parsing.tree.literals.NumberLiteral;
 import de.skuzzle.polly.parsing.tree.literals.StringLiteral;
 import de.skuzzle.polly.parsing.tree.literals.TimespanLiteral;
 import de.skuzzle.polly.parsing.tree.literals.UserLiteral;
 
 
 
 /**
  * 
  * <pre>
  * input           -> command (\t signature)? EOS
  * signature       -> assignment (\t assignment)*
  * 
  * assignment      -> relation ('->' modifier definition)?
  * modifier        -> 'public'? 'temp'? 
  * definition      -> identifier ( '(' func_definition ')' )
  * func_def        -> ( type_def \t identifier (',' type_def \t identifier)* ) | e
  * type_def        -> identifier ('<' identifier '>')?
  * 
  * relation        -> conjunction (relational_op conjunction)?
  * conjunction     -> disjunction (conjunction_op disjunction)*
  * disjunction     -> expression (disjunction_op expression)*
  * expression      -> term (expression_op term)*
  * term            -> factor (term_op factor)*
  * factor          -> postfix (factor_op factor)?
  * postfix         -> autlist (postfix_op)*
  * autolist        -> dotdot (';' dotdot)*
  * dotdot          -> unary ('..' unary ('?' unary)?)?
  * unary           -> unary_op unary | access
  * access          -> literal ['.' literal]
  * literal         -> identifier ( '(' parameters ')' )?
  *                  | boolean_literal
  *                  | number_literal
  *                  | channel_literal
  *                  | string_literal
  *                  | date_literal
  *                  | '{' list_literal '}'
  *                  | '(' relation ')'
  *                  | '-' literal
  *                  | '!' literal
  *                  | 'if' '(' relation ')' '(' relation ')' '?' '(' relation ')'
  * list_literal    -> e         // for empty lists!
  *                  | expression (',' expression)
  * 
  * command         -> ':' identifier 
  * identifier      -> 
  * boolean_literal -> 'true' | 'false' | 'ja' | 'nein'
  * number_literal  -> \d+(\.\d+)?([+-]?[eE]\d+)?
  * channel_literal -> '#' identifier
  * user_literal    -> identifier ':'
  * string_literal  -> '"' any_char '"'
  * date_literal    -> 
  * unary_op        -> '-' | '!'
  * conjunction_op  -> '||' | '|'
  * disjunction_op  -> '&&' | '&'
  * relational_op   -> '>' | '<' | '<=' | '>=' | '==' | '!='
  * expression_op   -> '+' | '-'
  * term_op         -> '*' | '/' | '\' | '%' 
  * factor_op       -> '^'
  * </pre>
  * 
  * This leads to the following list of operator precedence (with descending precedence
  * level):
  * 
  * <pre>
  * !,-          Unary operators
  * ..           Dotdot list generator operator
  * []           Index access for lists and strings
  * ^            Exponential operator
  * /,*,\,%      Arithmetic operators
  * +,-          Arithmetic operators
  * &,&&         Boolean/Integer 'And'
  * |,||         Boolean/Integer 'Or'
  * <,>,==,!=    Relational operators
  * <=,>=
  * ->           Assignment operator
  * </pre>
  * 
  */
 public class InputParser extends AbstractParser<InputScanner> {
 
     private PrecedenceTable operators;
     private int openExpressions;
 
     
     
     public Root parse(String input) throws ParseException {
         try {
             return this.parse(input, "ISO-8859-1");
         } catch (UnsupportedEncodingException ignore) {
             ignore.printStackTrace();
         }
         // better not happen
         return null;
     }
     
     
     
     public Root parse(String input, String encoding) 
             throws ParseException, UnsupportedEncodingException {
         InputScanner inp = new InputScanner(input, encoding);
         this.operators = new PrecedenceTable();
         
         return (Root) this.parse(inp);
     }
     
     
     
     public Root tryParse(String input) {
         try {
             return this.tryParse(input, "ISO-8859-1");
         } catch (UnsupportedEncodingException ignore) {
             return null;
         }
     }
     
     
     
     public Root tryParse(String input, String encoding) 
             throws UnsupportedEncodingException {
         return (Root) this.tryParse(new InputScanner(input, encoding));
     }
     
     
     
     protected void allowWhiteSpace() throws ParseException {
         this.scanner.match(TokenType.SEPERATOR);
     }
     
     
     
     protected void enterExpression() {
         ++this.openExpressions;
         this.scanner.setSkipWhiteSpaces(true);
     }
     
     
     
     protected void leaveExpression() {
         --this.openExpressions;
         if (this.openExpressions == 0) {
             this.scanner.setSkipWhiteSpaces(false);
         }
     }
 
     
     
     @Override
     protected Root parse_input() throws ParseException {
         /*
          * Ignore any ParseException when reading the first character to ignore
          * inputs that are not meant to be parsed.
          */
         Root root = null;
         try {
         	Token la = this.scanner.lookAhead();
             if (!this.scanner.match(TokenType.COMMAND)) {
                 return null;
             }
             root = new Root(new CommandLiteral(la));
             
            // HACK: smiley fix
            if (root.getName().getCommandName().length() < 3) {
                return null;
            }
         } catch (ParseException e) {
             return null;
         }
         if (this.scanner.lookAhead().matches(TokenType.SEPERATOR)) {
 	        this.expect(TokenType.SEPERATOR);
 	        this.parse_signature(root.getParameters());
         }
         // fixing ISSUE 0000015 with expecting an EOS here. This prevents unexpected chars
         // to be ignored.
         this.expect(TokenType.EOS);
         return root;
     }
     
     
     
     protected void parse_signature(List<Expression> expressions) throws ParseException {
         expressions.add(this.parse_assignment());
         
         while (this.scanner.lookAhead().matches(TokenType.SEPERATOR)) {
             this.scanner.consume();
             expressions.add(this.parse_assignment());
         }
     }
     
     
     
     protected Expression parse_assignment() throws ParseException {
         Expression expression = this.parse_relational();
         
         Token la = this.scanner.lookAhead();
         if (la.matches(TokenType.ASSIGNMENT)) {
             this.scanner.consume();
             
             expression = new AssignmentExpression(expression, la.getPosition(), 
                     this.parse_definition());
         }
         
         return expression;
     }
     
     
     
     protected Declaration parse_definition() throws ParseException {
         boolean isPublic = false;
         if (this.scanner.match(TokenType.PUBLIC)) {
             this.expect(TokenType.SEPERATOR);
             isPublic = true;
         }
         
         boolean isTemp = false;
         if (this.scanner.match(TokenType.TEMP)) {
             this.expect(TokenType.SEPERATOR);
             isTemp = true;
         }
         
         Token id = this.expect(TokenType.IDENTIFIER);
         
         if (this.scanner.match(TokenType.OPENBR)) {
             FunctionDeclaration decl = new FunctionDeclaration(new IdentifierLiteral(id), 
                     isPublic, isTemp);
             this.parse_func_definition(decl.getFormalParameters());
             
             this.expect(TokenType.CLOSEDBR);
             return decl;
         }
         return new VarDeclaration(new IdentifierLiteral(id), isPublic, isTemp);
     }
 
     
     
     protected void parse_func_definition(List<VarDeclaration> parameters) 
             throws ParseException {
         /*
          * If the next token is a closing brace, return. So we have functions with no
          * parameters
          */
         if (this.scanner.lookAhead().getType() == TokenType.CLOSEDBR) {
             return;
         }
         int i = 0;
         do {
             if (i++ != 0) {
                 // HACK: in second iteration, a whitespace is allowed here
                 this.allowWhiteSpace();
             }
             Expression type = this.parse_type_definition();
             this.expect(TokenType.SEPERATOR);
             Token paramName = this.expect(TokenType.IDENTIFIER);
             
             VarDeclaration decl = new VarDeclaration(
                 new IdentifierLiteral(paramName.getStringValue()), false, false);
             decl.setExpression(type);
             parameters.add(decl);
         } while (this.scanner.match(TokenType.COMMA));
         
     }
     
     
     
     protected Expression parse_type_definition() throws ParseException {
         Token typeId = this.expect(TokenType.IDENTIFIER);
         
         Token la = this.scanner.lookAhead();
         if (la.matches(TokenType.LT)) {
             this.scanner.consume();
             Token subId = this.expect(TokenType.IDENTIFIER);
             this.expect(TokenType.GT);
             
             return new TypeParameterExpression(new IdentifierLiteral(typeId), 
                     new IdentifierLiteral(subId), 
                     this.scanner.spanFrom(typeId));
         }
         
         return new TypeParameterExpression(new IdentifierLiteral(typeId), 
                 this.scanner.spanFrom(typeId));
     }
     
     
     
     protected Expression parse_relational() throws ParseException {
         Expression expression = this.parse_conjunction();
         
         Token la = this.scanner.lookAhead();
         if (this.operators.match(la, PrecedenceLevel.RELATION)) {
             this.scanner.consume();
             
             expression = new BinaryExpression(expression, la, this.parse_conjunction());
         }
         return expression;
     }
     
     
     
     protected Expression parse_conjunction() throws ParseException {
         Expression expression = parse_disjunction();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.CONJUNCTION)) {
             this.scanner.consume();
             
             expression = new BinaryExpression(expression, la, this.parse_disjunction());
             la = this.scanner.lookAhead();
         }
         
         return expression;
     }
     
     
     
     protected Expression parse_disjunction() throws ParseException {
         Expression expression = parse_expression();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.DISJUNCTION)) {
             this.scanner.consume();
             
             expression = new BinaryExpression(expression, la, this.parse_expression());
             la = this.scanner.lookAhead();
         }
         
         return expression;
     }
     
     
     
     protected Expression parse_expression() throws ParseException {
         Expression expression = this.parse_term();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.EXPRESSION)) {
             this.scanner.consume();
             
             expression = new BinaryExpression(expression, la, this.parse_term());
             la = this.scanner.lookAhead();
         }
         
         return expression;
     }
     
     
     
     protected Expression parse_term() throws ParseException {
         Expression expression = this.parse_factor();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.TERM)) {            
             // ISSUE 0000099: If Identifier or open brace, do not consume the token but
             //                pretend it was a multiplication
             if (la.matches(TokenType.IDENTIFIER) || la.matches(TokenType.OPENBR)) {
                 la = new Token(TokenType.MUL, la.getPosition());
             } else {
                 this.scanner.consume();
             }
             
             expression = new BinaryExpression(expression, la, this.parse_factor());
             la = this.scanner.lookAhead();
         }
         
         return expression;
     }
     
     
     
     protected Expression parse_factor() throws ParseException {
         Expression expression = this.parse_postfix();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.FACTOR)) {
             this.scanner.consume();
             
             expression = new BinaryExpression(expression, la, this.parse_factor());
             la = this.scanner.lookAhead();
         }
         return expression;
     }
     
     
     
     protected Expression parse_postfix() throws ParseException {
 //        Expression expression = this.parse_dotdot();
         Expression expression = this.parse_autolist();
         
         Token la = this.scanner.lookAhead();
         while (this.operators.match(la, PrecedenceLevel.POSTFIX)) {
             this.scanner.consume();
         
             if (la.matches(TokenType.OPENSQBR)) {
                 Token t = new Token(TokenType.INDEX, this.scanner.spanFrom(la));
                 expression = new BinaryExpression(expression, t, this.parse_expression());
                 
                 this.expect(TokenType.CLOSEDSQBR);
                 
                 /*
                  * Correct position so that it spans the whole operator including
                  * the closing braces.
                  */
                 expression.setPosition(this.scanner.spanFrom(la));
             } else if (la.matches(TokenType.QUESTION) || 
                     la.matches(TokenType.QUEST_EXCALAMTION)) {
                 return new UnaryExpression(la, expression);
             }
             la = this.scanner.lookAhead();
         }
         return expression;
     }
     
     
     
     protected Expression parse_autolist() throws ParseException {
         Expression e = this.parse_dotdot();
         Token la = this.scanner.lookAhead();
         
         if (la.matches(TokenType.SEMICOLON)) {
             ListLiteral result = new ListLiteral(la);
             result.getElements().add(e);
             
             while (this.scanner.match(TokenType.SEMICOLON)) {
                 result.getElements().add(this.parse_dotdot());
             }
             return result;
         } else {
             return e;
         }
     }
     
     
     
     protected Expression parse_dotdot() throws ParseException {
         Expression expression = this.parse_unary();
         
         Token la = this.scanner.lookAhead();
         if (this.operators.match(la, PrecedenceLevel.DOTDOT)) {
             this.scanner.consume();
                         
              // Default step value
             Literal tmp = new NumberLiteral(1.0, this.scanner.spanFrom(la));
             TernaryExpression tmpExpression = new TernaryExpression(expression, 
                 this.parse_unary(), tmp, la);
             la = scanner.lookAhead();
             if (la.matches(TokenType.DOLLAR)) {
                 this.scanner.consume();
                 tmpExpression.setThirdOperand(this.parse_unary());
             }
             expression = tmpExpression;
         }
         return expression;
     }
     
     
     
     protected Expression parse_unary() throws ParseException {
         Token la = this.scanner.lookAhead();
         if (this.operators.match(la, PrecedenceLevel.UNARY)) {
             this.scanner.consume();
             return new UnaryExpression(la, this.parse_unary());
         }
         return this.parse_access();
     }
     
     
     
     protected Expression parse_access() throws ParseException {
         Expression e = this.parse_literal();
         Token la = this.scanner.lookAhead();
         
         if (la.matches(TokenType.DOT)) {
             this.scanner.consume();
             e = new NamespaceAccessExpression(e, this.parse_literal(), 
                 new Position(e.getPosition(), this.scanner.spanFrom(la)));
         }
         return e;
     }
     
     
     
     protected Expression parse_literal() throws ParseException {
         Token la = this.scanner.lookAhead();
         Expression expression = null;
         
         switch (la.getType()) {
             case IDENTIFIER:
                 this.scanner.consume();
                 
                 IdentifierLiteral id = new IdentifierLiteral(la);
 
                 la = this.scanner.lookAhead();
                 if (la.getType() == TokenType.OPENBR) {
                     this.scanner.consume();
                     
                     VarOrCallExpression call = new VarOrCallExpression(id);
                     this.parse_expression_list(call.getActualParameters(), 
                             TokenType.CLOSEDBR);
                     
                     this.expect(TokenType.CLOSEDBR);
                     
                     /*
                      * CONSIDER: make the function calls position span the whole
                      *           statement including parameter and braces?
                      * 
                      * call.setPosition(this.scanner.spanFrom(id.getToken()));
                      */
                     
                     return call;
                 } else {
                     /* fixed ISSUE: 0000003 with VarAccessExpression */
                     return new VarOrCallExpression(id);
                 }
             case RADIX:
                 this.scanner.consume();
                 return new BinaryExpression(
                     new NumberLiteral(la.getLongValue(), la.getPosition()), 
                     la, 
                     this.parse_literal());
 
             case CHANNEL:
                 this.scanner.consume();
                 return new ChannelLiteral(la);
 
             case USER:
                 this.scanner.consume();
                 return new UserLiteral(la);
                 
             case STRING:
                 this.scanner.consume();
                 return new StringLiteral(la);
 
             case NUMBER:
                 this.scanner.consume();
                 return new NumberLiteral(la);
 
             case TRUE:
             case FALSE:
                 this.scanner.consume();
                 return new BooleanLiteral(la);
 
             case OPENBR:
                 this.scanner.consume();
                 
                 /*
                  * Now we can ignore whitespaces until the matching closing brace is 
                  * read.
                  */
                 this.enterExpression();
                 
                 /* 
                  * Check if this is a type cast. If it is not, but a normal 
                  * identifier in braces, context check will resolve this issue.
                  * 
                  * gettin' little messy now
                  */
                 Token tmp = this.scanner.lookAhead();
                 if (tmp.matches(TokenType.IDENTIFIER)) {
                     this.scanner.consume();
                     
                     // check for subtype
                     Token subType = null;
                     if (this.scanner.match(TokenType.LT)) {
                         subType = this.scanner.lookAhead();
                         this.expect(TokenType.IDENTIFIER);
                         this.expect(TokenType.GT);
                     }
                     
                     if (this.scanner.lookAhead().getType() == TokenType.CLOSEDBR) {
                         this.scanner.consume();
                         this.leaveExpression();
                         
                         Expression castOp;
                         if (this.scanner.lookAhead().getType() == TokenType.EOS) {
                             // just an identifier in braces?
                             return new VarOrCallExpression(
                                 new IdentifierLiteral(tmp));
                         } else if (subType == null) {
                             castOp = new TypeParameterExpression(
                                 new IdentifierLiteral(tmp.getStringValue()), 
                                 tmp.getPosition());
                         } else {
                             castOp = new TypeParameterExpression(
                                 new IdentifierLiteral(tmp.getStringValue()),
                                 new IdentifierLiteral(subType.getStringValue()), 
                                 this.scanner.spanFrom(tmp.getPosition().getStart()));
                         }
                         
                         return new CastExpression(
                             castOp,
                             this.parse_literal(), 
                             this.scanner.spanFrom(la));
                     } else if (subType != null) {
                         throw new ParseException("Invalid sub type definition", 
                             subType.getPosition());
                     } else {
                         /*
                          * this was no typecast, so pushback the identifier and go on
                          * the normal way.
                          */
                         this.scanner.pushback(tmp);
                     }
                 }
 
                 expression = this.parse_relational();
                 this.expect(TokenType.CLOSEDBR);
                 
                 this.leaveExpression();
                 
                 /*
                  * To include braces within the position
                  */               
                 expression.setPosition(this.scanner.spanFrom(la));
                 return expression;
                 
             case OPENCURLBR:
                 this.scanner.consume();
                 ListLiteral result = new ListLiteral(la);
                 
                 this.parse_expression_list(result.getElements(), TokenType.CLOSEDCURLBR);
                 
                 this.expect(TokenType.CLOSEDCURLBR);
                 
                 /*
                  * Correct position so it contains the closing brace 
                  */
                 result.setPosition(this.scanner.spanFrom(la));
                 return result;
             case DATETIME:
                 this.scanner.consume();
                 return new DateLiteral(la);
                 
             case TIMESPAN: 
                 this.scanner.consume();
                 return new TimespanLiteral(la);
                 
             case SUB:
             case EXCLAMATION:
                 this.scanner.consume();
                 return new UnaryExpression(la, this.parse_literal());
                 
             case COMMAND:
             	this.scanner.consume();
             	return new CommandLiteral(la);
             case QUESTION:
                 this.scanner.consume();
                 return new HelpLiteral(la);
             	
             case IF:
                 this.scanner.consume();
                 this.scanner.match(TokenType.SEPERATOR);
                 this.expect(TokenType.OPENBR);
                 Expression condition = this.parse_relational();
                 this.expect(TokenType.CLOSEDBR);
                 this.scanner.match(TokenType.SEPERATOR);
                 
                 this.expect(TokenType.OPENBR);
                 Expression ifExpression = this.parse_relational();
                 this.expect(TokenType.CLOSEDBR);
                 this.scanner.match(TokenType.SEPERATOR);
                 this.expect(TokenType.QUESTION);
                 this.scanner.match(TokenType.SEPERATOR);
                 this.expect(TokenType.OPENBR);
                 Expression elseExpression = this.parse_relational();
                 this.expect(TokenType.CLOSEDBR);
                 
                 return new IfExpression(condition, ifExpression, elseExpression);
             default:
                 /*
                  * This will cause a ParseException to be thrown, indicating a missing
                  * literal.
                  */
                 this.expect(TokenType.LITERAL);
         }
         
         return null;
     }
     
     
     
     protected void parse_expression_list(List<Expression> result, 
                 TokenType listEnd) throws ParseException {
         Token la = this.scanner.lookAhead();
         
         /*
          * Empty list
          */
         if (la.matches(listEnd)) {
             return;
         }
         
         result.add(this.parse_relational());
         
         /*
          * Whitespaces are allowed after comma
          */
         la = this.scanner.lookAhead();
         while (la.matches(TokenType.COMMA)) {
             this.scanner.consume();
             this.allowWhiteSpace();
             
             result.add(this.parse_relational());
             la = this.scanner.lookAhead();
         }
     }
 }
