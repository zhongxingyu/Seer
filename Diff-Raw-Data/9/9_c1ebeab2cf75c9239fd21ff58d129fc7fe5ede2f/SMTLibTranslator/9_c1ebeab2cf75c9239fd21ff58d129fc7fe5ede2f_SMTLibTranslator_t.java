 package verifier;
 
 import ast.*;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 /**
  *
  */
 public class SMTLibTranslator implements ASTVisitor {
     private ArrayList<LinkedList<S_Expression>> programs;
     private S_Expression tempExpr;
 
     public WPProgram getWPTree(ASTRoot ast) {
         //TODO: Fill stub
         programs = new ArrayList<LinkedList<S_Expression>>();
         ast.accept(this);
         int size = programs.get(0).size();
         return new WPProgram(programs.get(0).toArray(new S_Expression[size]));
     }
 
     //TODO: fill in stubs
     @Override
     public void visit(Conditional conditional) {
     }
 
     @Override
     public void visit(Loop loop) {
     }
 
     @Override
     public void visit(ArrayAssignment arrayAssignment) {
     }
 
     @Override
     public void visit(ArithmeticExpression arithmeticExpression) {
         arithmeticExpression.getSubexpression1().accept(this);
         ArithmeticOperator operator =
                 arithmeticExpression.getArithmeticOperator();
        int length = operator instanceof BinaryOperator ? 2 : 1;
        S_Expression[] expressions = new S_Expression[length];
         expressions[0] = tempExpr;
         String op = operator.toString();
         if (operator instanceof Division) {
             op = "div";
         } else if (operator instanceof Modulo) {
             op = "mod";
         }
         if (operator instanceof BinaryOperator) {
             arithmeticExpression.getSubexpression2().accept(this);
             expressions[1] = tempExpr;
         }
         tempExpr = new S_Expression(op, expressions);
     }
 
     @Override
     public void visit(NumericLiteral number) {
         tempExpr = new Constant(number.toString());
     }
 
     @Override
     public void visit(LogicalExpression logicalExpression) {
         logicalExpression.getSubexpression1().accept(this);
         LogicalOperator operator = logicalExpression.getLogicalOperator();
        int length = operator instanceof BinaryOperator ? 2 : 1;
        S_Expression[] expressions = new S_Expression[length];
         expressions[0] = tempExpr;
         String op = operator.toString();
         if (operator instanceof Negation) {
             op = "not";
         } else if (operator instanceof Conjunction) {
             op = "and";
         } else if (operator instanceof Disjunction) {
             op = "or";
         } else if (operator instanceof Equal) {
             op = "=";
         } else if (operator instanceof NotEqual) {
             op = "!=";
         }
         if (operator instanceof BinaryOperator) {
             logicalExpression.getSubexpression2().accept(this);
             expressions[1] = tempExpr;
         }
         tempExpr = new S_Expression(op, expressions);
     }
 
     @Override
     public void visit(BooleanLiteral bool) {
         tempExpr = new Constant(bool.toString());
     }
 
     @Override
     public void visit(FunctionCall functionCall) {
     }
 
     @Override
     public void visit(VariableRead variableRead) {
         tempExpr = new Variable(variableRead.toString());
     }
 
     @Override
     public void visit(ArrayRead arrayRead) {
     }
 
     @Override
     public void visit(Function function) {
         programs.add(new LinkedList<S_Expression>());
         LinkedList<S_Expression> program = programs.get(0);
         program.add(new Constant("true"));
         Ensure[] ensures = function.getEnsures();
         for (Ensure ensure : ensures) {
             ensure.accept(this);
             program.set(program.size() - 1, new S_Expression("and",
                             new S_Expression[]{tempExpr, program.getLast()}));
         }
         function.getFunctionBlock().accept(this);
         Assumption[] assumptions = function.getAssumptions();
         for (Assumption assumption : assumptions) {
             assumption.accept(this);
             program.set(program.size() - 1, new S_Expression("=>",
                     new S_Expression[]{tempExpr, program.getLast()}));
         }
         program.set(program.size() - 1, new S_Expression("assert",
                 new S_Expression[]{ new S_Expression("not",
                             new S_Expression[]{program.getLast()})}));
         FunctionParameter[] parameters = function.getParameters();
         for (FunctionParameter parameter : parameters) {
             //TODO: check for arrays
             String type = parameter.getType() instanceof BooleanType
                     ? "Bool" : "Int";
             program.addFirst(new S_Expression("declare-fun",
                     new S_Expression[]{new Constant(parameter.getName()),
                         new Constant("()"), new Constant(type)}));
         }
         program.addFirst(new S_Expression("set-logic",
                 new S_Expression[]{new Constant("AUFNIRA")}));
         program.addLast(new S_Expression("check-sat", new S_Expression[0]));
     }
 
     @Override
     public void visit(Program program) {
         program.getMainFunction().accept(this);
     }
 
     @Override
     public void visit(Assignment assignment) {
         assignment.getValue().accept(this);
         programs.get(0).getLast().replace(assignment.getIdentifier().toString(),
                 tempExpr);
     }
 
     @Override
     public void visit(Assertion assertion) {
     }
 
     @Override
     public void visit(Assumption assumption) {
     }
 
     @Override
     public void visit(Axiom axiom) {
     }
 
     @Override
     public void visit(Ensure ensure) {
         ensure.getExpression().accept(this);
     }
 
     @Override
     public void visit(Invariant invariant) {
     }
 
     @Override
     public void visit(ReturnStatement returnStatement) {
     }
 
     @Override
     public void visit(VariableDeclaration varDec) {
         varDec.getValue().accept(this);
         programs.get(0).getLast().replace(varDec.getName(), tempExpr);
     }
 
     @Override
     public void visit(ArrayDeclaration arrDec) {
     }
 
     @Override
     public void visit(ExistsQuantifier existsQuantifier) {
     }
 
     @Override
     public void visit(ForAllQuantifier forAllQuantifier) {
     }
 
     @Override
     public void visit(StatementBlock statementBlock) {
         Statement[] statements = statementBlock.getStatements();
         for (int i = statements.length - 1; i >= 0; i--) {
             statements[i].accept(this);
         }
     }
 }
