 package swp_compiler_ss13.fuc.parser;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArithmeticUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.ast.nodes.unary.LogicUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode.UnaryOperator;
 import swp_compiler_ss13.common.lexer.Lexer;
 import swp_compiler_ss13.common.lexer.Token;
 import swp_compiler_ss13.common.lexer.TokenType;
 import swp_compiler_ss13.common.parser.Parser;
 import swp_compiler_ss13.common.parser.ReportLog;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.DoubleType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.common.types.primitive.StringType;
 import swp_compiler_ss13.fuc.ast.ASTImpl;
 import swp_compiler_ss13.fuc.ast.ArithmeticBinaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.ArithmeticUnaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.AssignmentNodeImpl;
 import swp_compiler_ss13.fuc.ast.BasicIdentifierNodeImpl;
 import swp_compiler_ss13.fuc.ast.BlockNodeImpl;
 import swp_compiler_ss13.fuc.ast.BreakNodeImpl;
 import swp_compiler_ss13.fuc.ast.DeclarationNodeImpl;
 import swp_compiler_ss13.fuc.ast.LiteralNodeImpl;
 import swp_compiler_ss13.fuc.ast.LogicUnaryExpressionNodeImpl;
 import swp_compiler_ss13.fuc.ast.ReturnNodeImpl;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.ParseTableEntry;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.ParseTableEntry.ParseTableEntryType;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.ParseTableGeneratorImpl;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.Production;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.Reduce;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.interfaces.ParseTable;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.interfaces.ParseTable.StateOutOfBoundsException;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.interfaces.ParseTable.TokenNotFoundException;
 import swp_compiler_ss13.fuc.parser.parseTableGenerator.interfaces.ParseTableGenerator;
 
 
 public class ParserImpl implements Parser {
    
    private static final Object NO_VALUE = new Object();
    
    private final Logger log = Logger.getLogger(getClass());
    
    protected Lexer lexer;
    private Stack<Integer> parserStack = new Stack<Integer>();
    protected ParseTable table;
    private ReportLog reportLog;
    private AST ast = new ASTImpl();
    // private Stack<Token> tokenStack;
    // private List<DeclarationNode> decls = new LinkedList<DeclarationNode>();
    // private List<StatementNode> stmts = new LinkedList<StatementNode>();
    // private Stack<StatementNode> stmtStack = new Stack<StatementNode>();
    private Stack<Object> valueStack = new Stack<>();
    
    @Override
    public void setLexer(Lexer lexer) {
       this.lexer = lexer;
    }
    
    @Override
    public void setReportLog(ReportLog reportLog) {
       this.reportLog = reportLog;
    }
    
    @Override
    public AST getParsedAST() {
       // TODO
       ParseTableGenerator generator = new ParseTableGeneratorImpl();
       table = generator.getTable();
       return parse();
    }
    
    
    protected AST parse() {
       
       // add initial state
       parserStack.add(0);
       
       int s = 0;
       TokenType tokenType = null;
       ParseTableEntryType entryType;
       Token token = lexer.getNextToken();
       ParseTableEntry entry = null;
       
       while (true) {
          s = parserStack.peek();
          
          tokenType = token.getTokenType();
          
          if (tokenType == TokenType.NOT_A_TOKEN) {
             reportLog.reportError(token.getValue(), token.getLine(), token.getColumn(), "Undefined Symbol found!");
             return null;
          }
          
          try {
             entry = table.getEntry(s, token);
          } catch (StateOutOfBoundsException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
          } catch (TokenNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
          
          entryType = entry.getType();
          
          
          switch (entryType) {
             case SHIFT: {
                parserStack.push(entry.getNewState());
                // Token shiftedToken = token;
               valueStack.push(token);
                token = lexer.getNextToken();
                
                // Push value corresponding to the token here.
                // Need relation Token -> Value here! If !Token.hasValue(), put NoValue on stack.
                // what's the matter whit getSymbolShiftValue??
                // Object value = getSymbolShiftValue(shiftedToken);
                // Is there a Token without a Value??
                // valueStack.push(value);
                // Isn't it better to push a token, otherwise there are only null and NOVALUE onto stack?
               
                
                break;
             }
             
             case REDUCE: {
                
                // pop states from stack
                Reduce reduce = (Reduce) entry;
                for (int i = 1; i <= reduce.getCount(); i++) {
                   parserStack.pop();
                }
                // push next state on stack
                parserStack.push(reduce.getNewState());
                
                // +++++++++++++++++++++++++++++++++++
                // get action for reduced production
                Production prod = reduce.getProduction();
                ReduceAction reduceAction = getReduceAction(prod);
                
                // If there is anything to do on the value stack
                // (There might be no reduce-action for Productions like unary -> factor, e.g.)
                if (reduceAction != null) {
                   // Pop all values reduced by this production
                   int nrOfValuesReduced = prod.getNrOFSymbolsWOEpsilon();
                   LinkedList<Object> valueHandle = new LinkedList<>();
                   for (int i = 0; i < nrOfValuesReduced; i++) {
                      valueHandle.addFirst(valueStack.pop());
                   }
                   
                   // Execute reduceAction and push onto the stack
                   Object newValue = reduceAction.create(arr(valueHandle));
                   if (newValue == null) {
                      log.error("Error occurred! newValue is null");
                      return null;
                   }
                   valueStack.push(newValue);
                   // TODO Anything to do here for shortcuts?
                }
             }
             
             case ACCEPT: {
                if (tokenType != TokenType.EOF) {
                   // TODO Errorhandling
                } else {
                   BlockNode programBlock = (BlockNode) valueStack.pop();
                   ast.setRootNode(programBlock);
                   programBlock.setParentNode(ast.getRootNode());
                   return ast;
                }
             }
             
             case ERROR: {
                // TODO Errorhandling
             }
          }
       }
       
    }
    
    private static Object[] arr(List<Object> objs) {
       return objs.toArray(new Object[objs.size()]);
    }
    
    
    // private Object getSymbolShiftValue(Token t) {
    // switch (t.getTokenType()) {
    // case TRUE:
    // return null; // TODO WTF, which node to use for TRUE???
    //
    // case FALSE:
    // return null; // TODO WTF, which node to use for TRUE???
    //
    // default:
    // return NO_VALUE;
    // }
    // }
    
    private ReduceAction getReduceAction(Production prod) {
       switch (prod.getString()) {
       
          case "program -> decls stmts":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   Object left = objs[0]; // Should be NO_VALUE or BlockNode
                   Object right = objs[1]; // Should be NO_VALUE or BlockNode
                   
                   return joinBlocks(left, right);
                }
             };
             
          case "block -> { decls stmts }":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   Object left = objs[1]; // Should be NO_VALUE or BlockNode
                   Object right = objs[2]; // Should be NO_VALUE or BlockNode
                   
                   return joinBlocks(left, right);
                }
             };
             
          case "decls -> decls decl":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   Object left = objs[0]; // Should be NO_VALUE or DeclarationNode
                   Object right = objs[1]; // Should be DeclarationNode or BlockNode
                   
                   LinkedList<DeclarationNode> declList = new LinkedList<>();
                   // Handle left
                   if (!left.equals(NO_VALUE)) {
                      // We have exactlly ONE decl
                      if (!(left instanceof DeclarationNode)) {
                         log.error("Error in decls -> decls decl: Left must be a DeclarationNode!");
                      } else {
                         declList.add((DeclarationNode) left);
                      }
                   }
                   
                   // Handle right
                   if (right instanceof BlockNode) {
                      BlockNode oldBlock = (BlockNode) right;
                      declList.addAll(oldBlock.getDeclarationList());
                   } else {
                      if (!(right instanceof DeclarationNode)) {
                         log.error("Error in decls -> decls decl: Right must be a DeclarationNode!");
                      } else {
                         declList.add((DeclarationNode) right);
                      }
                   }
                   
                   // Create new BlockNode
                   BlockNode block = new BlockNodeImpl();
                   for (DeclarationNode decl : declList) {
                      block.addDeclaration(decl);
                      decl.setParentNode(block);
                   }
                   return block;
                }
             };
             
          case "decls -> ":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return NO_VALUE; // Symbolizes epsilon
                }
             };
             
          case "decl -> type id ;":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   Token typeToken = (Token) objs[0];
                   Token idToken = (Token) objs[1];
 
                   DeclarationNode decl = new DeclarationNodeImpl();
                   // Set type
                   switch (typeToken.getTokenType()) {
                      case REAL:
                         decl.setType(new DoubleType());
                         break;
                      case NUM:
                         decl.setType(new LongType());
                         break;
                      case STRING:
                         decl.setType(new StringType((long) typeToken.getValue().length()));
                         break;
                   }
                   
                   // Set ID
                   decl.setIdentifier(idToken.getValue());
                   return decl;
                }
             };
             
          case "stmts -> stmts stmt":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   Object left = objs[0]; // Should be NO_VALUE or StatementNode
                   Object right = objs[1]; // Should be StatementNode or BlockNode
                   
                   LinkedList<StatementNode> stmtList = new LinkedList<>();
                   // Handle left
                   if (!left.equals(NO_VALUE)) {
                      // there are just 0ne statement
                      if (!(left instanceof StatementNode)) {
                         log.error("Error in decls -> decls decl: Left must be a DeclarationNode!");
                      } else {
                         stmtList.add((StatementNode) left);
                      }
                   }
                   
                   // Handle right
                   if (right instanceof BlockNode) {
                      BlockNode oldBlock = (BlockNode) right;
                      stmtList.addAll(oldBlock.getStatementList());
                   } else {
                      if (!(right instanceof StatementNode)) {
                         log.error("Error in decls -> decls decl: Right must be a DeclarationNode!");
                      } else {
                         stmtList.add((StatementNode) right);
                      }
                   }
                   
                   // Create new BlockNode
                   BlockNode block = new BlockNodeImpl();
                   for (StatementNode stmt : stmtList) {
                      block.addStatement(stmt);
                      stmt.setParentNode(block);
                   }
                   return block;
                }
             };
             
          case "stmts -> ":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return NO_VALUE; // Symbolizes epsilon
                }
             };
             
          case "stmt -> assign":
             break; // Nothing to reduce here
          case "stmt -> block":
             break; // Nothing to reduce here. Block is a Stmt
             
          case "stmt -> if ( assign ) stmt":
          case "stmt -> if ( assign ) stmt else stmt":
          case "stmt -> while ( assign ) stmt":
          case "stmt -> do stmt while ( assign )":
             // TODO M2
             break;
             
          case "stmt -> break ;":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return new BreakNodeImpl();
                }
                
             };
          case "stmt -> return ;":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return new ReturnNodeImpl();
                }
             };
          case "stmt -> return loc ;":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
             	   ReturnNode returnNode = new ReturnNodeImpl();
             	   IdentifierNode identifier = (IdentifierNode) objs[1];
             	   returnNode.setRightValue(identifier);
             	   identifier.setParentNode(returnNode);
             	   return returnNode;
                }
             };
          case "stmt -> print loc ;":
          case "loc -> loc [ assign ]":
             // TODO m2
             break;
             
          case "loc -> id":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   BasicIdentifierNode identifierNode = new BasicIdentifierNodeImpl();
                   Token token = (Token) objs[0];
                   if (token.getTokenType() == TokenType.ID) {
                      identifierNode.setIdentifier(token.getValue());
                   } else {
                      log.error("Wrong TokenType in ReduceAction \"loc -> id\"");
                      return null;
                   }
                   return identifierNode;
                }
             };
          case "loc -> loc.id":
          case "assign -> loc = assign":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   AssignmentNodeImpl assignNode = new AssignmentNodeImpl();
                   assignNode.setLeftValue((IdentifierNode) objs[0]);
                   assignNode.getLeftValue().setParentNode(assignNode);
                   assignNode.setRightValue((StatementNode) objs[2]); // [1] is the "=" token
                   assignNode.getRightValue().setParentNode(assignNode);
                   return assignNode;
                }
             };
          case "assign -> bool":
          case "bool -> bool || join":
          case "bool -> join":
          case "join -> join && equality":
             // TODO M2
             break;
             
          case "join -> equality":
             break;   // Nothing to do here
             
          case "equality -> equality == rel":
          case "equality -> equality != rel":
             // TODO M2
             break;
          case "equality -> rel":
             return null;   // Nothing to do here
          case "rel -> expr < expr":
          case "rel -> expr > expr":
          case "rel -> expr >= expr":
          case "rel -> expr <= expr":
             // TODO M2
             break;
          case "rel -> expr":
             break;   // Nothing to do here
          case "expr -> expr + term":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return binop(objs[0], objs[2], BinaryOperator.ADDITION);
                }
             };
          case "expr -> expr - term":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return binop(objs[0], objs[2], BinaryOperator.SUBSTRACTION);
                }
             };
          case "expr -> term":
             break;   // Nothing to do here
          case "term -> term * unary":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return binop(objs[0], objs[2], BinaryOperator.MULTIPLICATION);
                }
             };
          case "term -> term / unary":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   return binop(objs[0], objs[2], BinaryOperator.DIVISION);
                }
             };
          case "term -> unary":
             break;   // Nothing to do here
          case "unary -> ! unary":
              return new ReduceAction() {
                 @Override
                 public Object create(Object... objs) {
 //                   Token token = (Token) objs[0]; // not
                    UnaryExpressionNode unary = (UnaryExpressionNode) objs[1];
                   
                    LogicUnaryExpressionNode logicalUnary = new LogicUnaryExpressionNodeImpl();
                    logicalUnary.setOperator(UnaryOperator.LOGICAL_NEGATE);
                    logicalUnary.setRightValue(unary);
                    unary.setParentNode(logicalUnary);
                    
                    return logicalUnary;
                 }
              };
          case "unary -> - unary":
             return new ReduceAction() {
                @Override
                public Object create(Object... objs) {
                   // Token token = (Token) objs[0]; // minus
                   UnaryExpressionNode unary = (UnaryExpressionNode) objs[1];
                   
                   ArithmeticUnaryExpressionNode arithUnary = new ArithmeticUnaryExpressionNodeImpl();
                   arithUnary.setOperator(UnaryOperator.MINUS);
                   arithUnary.setRightValue(unary);
                   unary.setParentNode(arithUnary);
                   return arithUnary;
                }
             };
          case "unary -> factor":
          case "factor -> ( assign )":
          case "factor -> loc":
             break;
          case "factor -> num":
             return new ReduceAction() {
                
                @Override
                public Object create(Object... objs) {
                   LiteralNode literal = new LiteralNodeImpl();
                   Token token = (Token) objs[0];
                   literal.setLiteral(token.getValue());
                   literal.setLiteralType(new LongType());
                   return literal;
                }
                
             };
          case "factor -> real":
             return new ReduceAction() {
                
                @Override
                public Object create(Object... objs) {
                   LiteralNode literal = new LiteralNodeImpl();
                   Token token = (Token) objs[0];
                   literal.setLiteral(token.getValue());
                   literal.setLiteralType(new DoubleType());
                   return literal;
                }
                
             };
          case "factor -> true":
             return new ReduceAction() {
                
                @Override
                public Object create(Object... objs) {
                   LiteralNode literal = new LiteralNodeImpl();
                   Token token = (Token) objs[0];
                   literal.setLiteral(token.getValue());
                   literal.setLiteralType(new BooleanType());
                   return literal;
                }
                
             };
          case "factor -> false":
             return new ReduceAction() {
                
                @Override
                public Object create(Object... objs) {
                   LiteralNode literal = new LiteralNodeImpl();
                   Token token = (Token) objs[0];
                   literal.setLiteral(token.getValue());
                   literal.setLiteralType(new BooleanType());
                   return literal;
                }
                
             };
          case "factor -> string":
             return new ReduceAction() {
                
                @Override
                public Object create(Object... objs) {
                   LiteralNode literal = new LiteralNodeImpl();
                   Token token = (Token) objs[0];
                   literal.setLiteral(token.getValue());
                   literal.setLiteralType(new StringType((long) token.getValue().length()));
                   return literal;
                }
                
             };
          case "type -> type [ num ]":
          case "type -> bool":
          case "type -> string":
          case "type -> num":
          case "type -> real":
          case "type -> record { decls }":
          default:
             return null; // Means "no ReduceAction to perform"
       }
       return null;
    }
    
 
    private static Object joinBlocks(Object left, Object right) {
       BlockNode newBlock = new BlockNodeImpl();
       // Handle left
       if (!left.equals(NO_VALUE)) {
          BlockNode declsBlock = (BlockNode) left;
          for (DeclarationNode decl : declsBlock.getDeclarationList()) {
             newBlock.addDeclaration(decl);
             decl.setParentNode(newBlock);
          }
       }
       
       // Handle right
       if (!right.equals(NO_VALUE)) {
          BlockNode stmtsBlock = (BlockNode) right;
          for (StatementNode decl : stmtsBlock.getStatementList()) {
             newBlock.addStatement(decl);
             decl.setParentNode(newBlock);
          }
       }
       return newBlock;
    }
    
    
    private static ArithmeticBinaryExpressionNode binop(Object leftExpr, Object rightExpr, final BinaryOperator op) {
       ExpressionNode left = (ExpressionNode) leftExpr;
       ExpressionNode right = (ExpressionNode) rightExpr;
       
       ArithmeticBinaryExpressionNode binop = new ArithmeticBinaryExpressionNodeImpl();
       binop.setLeftValue(left);
       binop.setRightValue(right);
       binop.setOperator(op);
       left.setParentNode(binop);
       right.setParentNode(binop);
       
       return binop;
    }
    
    
    private interface ReduceAction {
       Object create(Object... objs);
    }
 }
