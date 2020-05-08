 /**
  * Created on 13.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.util.List;
 
 import edu.mit.csail.sdg.alloy4.Pair;
 
 /**
  * A formula constructed from a binary operation
  * 
  * @author Ulrich Geilmann
  *
  */
 public class TermBinOp extends Term {
 	
	// TODO: make AND and OR variadic operators to conform to SMT mechanics
 	public enum Op {
 		IFF, IMPLIES, AND, OR, EQUALS,
 		LT, LTE, GT, GTE, PLUS, MINUS, MUL, DIV, REM, SHL, SHR, SHA
 	};
 	
 	// left/right operand
 	private Term left, right;
 	
 	// the operator
 	private Op operator;
 	
 	public TermBinOp (Term left, Op op, Term right) {
 		this.left = left;
 		this.operator = op;
 		this.right = right;
 	}
 	
 	@Override
 	public String toString() {
 		return buildTerm (left.toString(), right.toString());
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toStringTaclet() {
 		return buildTerm (left.toStringTaclet(), right.toStringTaclet());
 	}
 	
 	/** builds SMT style terms. some expressions may be z3-only 
 	 * @param l left hand side
 	 * @param r right hand side
 	 * @return a string in SMT syntax
 	 */
 	private String buildTerm (String l, String r) {
 		switch (operator) {	
 		case IFF:
 			return "(iff "+ l + " " + r + ")";	// z3-syntax, no smt2 standard (yet)
 		case IMPLIES:
 			return "(=> "+ l + " " + r + ")";	// smt-syntax
 		case AND:
 			return "(and "+ l + " " + r + ")";	// smt-syntax
 		case OR:
 			return "(or "+ l + " " + r + ")";	// smt-syntax
 		case EQUALS:
 			return "(= "+ l + " " + r + ")";	// smt-syntax
 		case LT:
 			return "(< "+ l + " " + r + ")";	// smt-syntax
 		case LTE:
 			return "(<= "+ l + " " + r + ")";	// smt-syntax
 		case GT:
 			return "(> "+ l + " " + r + ")";	// smt-syntax
 		case GTE:
 			return "(>= "+ l + " " + r + ")";	// smt-syntax
 		case PLUS:
 			return "(+ "+ l + " " + r + ")";	// smt-syntax
 		case MINUS:
 			return "(- "+ l + " " + r + ")";	// smt-syntax
 		case MUL:
 			return "(* "+ l+" " + r + ")";	    // smt-syntax
 		case DIV:
 			return "(div "+ l + " " + r + ")";	// smt-syntax
 		case REM:
 			return "(mod "+ l + " " + r + ")";	// smt-syntax
 		default:
 			throw new RuntimeException(new ModelException("This binary term does not know how to deal with this operator: "+ operator.name()) );
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public List<Pair<String,String>> getQuantVars() {
 		List<Pair<String,String>> decls = left.getQuantVars();
 		decls.addAll(right.getQuantVars());
 		return decls;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean occurs (String id) {
 		return left.occurs(id) || right.occurs(id);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Term substitute (String a, String b) {
 		return new TermBinOp(left.substitute(a,b), operator, right.substitute(a,b));
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public Term fill(Term t) {
 		return new TermBinOp(left.fill(t), operator, right.fill(t));
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public boolean isInt() {
 		switch (operator) {
 		case PLUS:
 		case MINUS:
 		case MUL:
 		case DIV:
 		case REM:
 		case SHL:
 		case SHR:
 		case SHA:
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 }
