 package de.skuzzle.polly.core.parser.ast.lang.operators;
 
 
 import de.skuzzle.polly.core.parser.Position;
 import de.skuzzle.polly.core.parser.ast.declarations.Namespace;
 import de.skuzzle.polly.core.parser.ast.declarations.types.Type;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.Literal;
 import de.skuzzle.polly.core.parser.ast.expressions.literals.NumberLiteral;
 import de.skuzzle.polly.core.parser.ast.lang.BinaryOperator;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.parser.ast.visitor.ExecutionVisitor;
 import de.skuzzle.polly.core.parser.problems.ProblemReporter;
 import de.skuzzle.polly.tools.collections.Stack;
 
 /**
  * Contains arithmetic operators that operate on {@link NumberLiteral}s and produce a
  * new NumberLiteral.
  *  
  * @author Simon Taddiken
  */
 public class BinaryArithmetic extends BinaryOperator<NumberLiteral, NumberLiteral> {
 
     public BinaryArithmetic(OpType id) {
         super(id);
         this.initTypes(Type.NUM, Type.NUM, Type.NUM);
     }
     
     
 
     @Override
     protected void exec(Stack<Literal> stack, Namespace ns,
             NumberLiteral left, NumberLiteral right, 
             Position resultPos, ExecutionVisitor execVisitor) throws ASTTraversalException {
         
         final ProblemReporter reporter = execVisitor.getReporter();
         switch (this.getOp()) {
         case ADD: 
             stack.push(new NumberLiteral(resultPos, left.getValue() + right.getValue()));
             break;
         case SUB:
             stack.push(new NumberLiteral(resultPos, left.getValue() - right.getValue()));
             break;
         case MUL:
             stack.push(new NumberLiteral(resultPos, left.getValue() * right.getValue()));
             break;
         case DIV:
             right.nonZero(reporter);
             stack.push(new NumberLiteral(resultPos, left.getValue() / right.getValue()));
             break;
         case INTDIV:
             right.nonZero(reporter);
             // XXX: implicit conversion
             stack.push(new NumberLiteral(resultPos, 
                 Math.ceil(left.getValue()) / Math.ceil(right.getValue())));
             break;
         case MOD:
             int r = right.nonZeroInteger(reporter);
            int l = (left.isInteger(reporter)  + r) % r;
             stack.push(new NumberLiteral(resultPos, l));
         case INT_AND:
             stack.push(new NumberLiteral(resultPos, 
                 left.isInteger(reporter) & right.isInteger(reporter)));
             break;
         case INT_OR:
             stack.push(new NumberLiteral(resultPos, 
                 left.isInteger(reporter) | right.isInteger(reporter)));
             break;
         case LEFT_SHIFT:
             stack.push(new NumberLiteral(resultPos,
                     left.isInteger(reporter) << right.isInteger(reporter)));
             break;
         case RIGHT_SHIFT:
             stack.push(new NumberLiteral(resultPos,
                 left.isInteger(reporter) >> right.isInteger(reporter)));
             break;
         case URIGHT_SHIFT:
             stack.push(new NumberLiteral(resultPos,
                 left.isInteger(reporter) >>> right.isInteger(reporter)));
             break;
         case RADIX:
             right.setRadix(left.isInteger(reporter));
             stack.push(right);
             break;
         case POWER:
             stack.push(new NumberLiteral(resultPos, 
                 Math.pow(left.getValue(), right.getValue())));
             break;
         case MIN:
             stack.push(new NumberLiteral(resultPos, 
                 Math.min(left.getValue(), right.getValue())));
             break;
         case MAX:
             stack.push(new NumberLiteral(resultPos, 
                 Math.max(left.getValue(), right.getValue())));
             break;
         case XOR:
             stack.push(new NumberLiteral(
                 resultPos, left.isInteger(reporter) ^ right.isInteger(reporter)));
             break;
         case ATAN2:
             stack.push(new NumberLiteral(resultPos, 
                 Math.atan2(left.getValue(), right.getValue())));
             break;
         case HYPOT:
             stack.push(new NumberLiteral(resultPos, 
                     Math.hypot(left.getValue(), right.getValue())));
             break;
         default:
             this.invalidOperatorType(this.getOp());
         }
     }
 }
