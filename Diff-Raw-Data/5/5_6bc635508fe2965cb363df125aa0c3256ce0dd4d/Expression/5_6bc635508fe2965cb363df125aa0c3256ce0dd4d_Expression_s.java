 package compilateur;
 
 import java.util.Stack;
 
 /**
  * Compute the expressions.
  */
 public class Expression {
 	private Stack<Type> stackType;
 	private Stack<Operator> stackOp;
 	private boolean invert;
 	private Ident affectTo;
 	
 	/**
 	 * Constructor
 	 */
 	public Expression() {
 		this.stackType = new Stack<Type>();
 		this.stackOp = new Stack<Operator>();
 		this.invert = false;
 	}
 	
 	/**
 	 * Tell Expression that the next value need to be inverted.
 	 */
 	public void invert() {
 		this.invert = true;
 	}
 	
 	/**
 	 * Push an operator to the stack of operators.
 	 * @param op The operator.
 	 */
 	public void pushOperator(Operator op) {
 		this.stackOp.push(op);
 	}
 	
 	/**
 	 * Push an integer to the stack of values.
 	 * @param ent The integer.
 	 */
 	public void pushInteger(int ent) {
 		this.stackType.push(Type.INT);
 		Yaka.yvm.iconst(ent);
 		if(this.invert) {
 			Yaka.yvm.ineg();
 		}
 	}
 	
 	/**
 	 * Push a boolean to the stack of values.
 	 * @param bool The boolean.
 	 */
 	public void pushBoolean(int bool) {
 		this.stackType.push(Type.BOOL);
 		Yaka.yvm.iconst(bool);
 		if(this.invert) {
 			Yaka.yvm.inot();
 		}
 	}
 	
 	/**
 	 * Push an ident to the stack of values.
 	 * @param str The key for the ident.
 	 */
 	public void pushIdent(String str) {
 		Ident ident = Yaka.tabIdent.getIdent(str);
 		if(ident==null) {
 			System.err.println("Ident not found: "+str);
 			return;
 		}
 		this.stackType.push(ident.getType());
 		if(ident.isVar()) {
 			Yaka.yvm.iload(ident.getValue());
 		} else {
 			Yaka.yvm.iconst(ident.getValue());
 		}
 	}
 	
 	/**
 	 * Add the operation part of the expression according to the two last values.
 	 * Display an error if the type of the values doesn't match with the operator.
 	 */
 	public void compute() {
 		Type b = this.stackType.pop();
 		Type a = this.stackType.pop();
 		Operator op = this.stackOp.pop();
 		if((a==Type.BOOL || a==Type.ERROR) && (b==Type.BOOL || b==Type.ERROR)) {
 			switch(op) {
 				case OR:
 					Yaka.yvm.ior();
 					this.stackType.push(Type.BOOL);
 					break;
 				case AND:
 					Yaka.yvm.iand();
 					this.stackType.push(Type.BOOL);
 					break;
 				case DIFF:
 					Yaka.yvm.idiff();
 					this.stackType.push(Type.BOOL);
 					break;
 				case EQUAL:
 					Yaka.yvm.iegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				default:
 					System.err.println("Expression: Invalid operation.");
 					this.stackType.push(Type.ERROR);
 			}
 		} else if((a==Type.INT || a==Type.ERROR) && (b==Type.INT || b==Type.ERROR)) {
 			switch(op) {
 				case ADD:
 					Yaka.yvm.iadd();
 					this.stackType.push(Type.INT);
 					break;
 				case SUB:
 					Yaka.yvm.isub();
 					this.stackType.push(Type.INT);
 					break;
 				case MUL:
 					Yaka.yvm.imul();
 					this.stackType.push(Type.INT);
 					break;
 				case DIV:
 					Yaka.yvm.idiv();
 					this.stackType.push(Type.INT);
 					break;
 				case LT:
 					Yaka.yvm.iinf();
 					this.stackType.push(Type.BOOL);
 					break;
 				case LTE:
 					Yaka.yvm.iinfegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				case GT:
 					Yaka.yvm.isup();
 					this.stackType.push(Type.BOOL);
 					break;
 				case GTE:
 					Yaka.yvm.isupegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				case DIFF:
 					Yaka.yvm.idiff();
 					this.stackType.push(Type.BOOL);
 					break;
 				case EQUAL:
 					Yaka.yvm.iegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				default:
 					System.err.println("Expression: Invalid operation.");
 					this.stackType.push(Type.ERROR);
 			}
 		} else {
 			System.err.println("Expression: The two operands doesn't match.");
 			this.stackType.push(Type.ERROR);
 		}
 	}
 	
 	/**
 	 * Record the ident for the affectation.
 	 * @param name The name of the ident.
 	 */
 	public void setAffectation(String name) {
 		if(Yaka.tabIdent.containsIdent(name)) {
 			this.affectTo = Yaka.tabIdent.getIdent(name);
 		} else {
 			System.err.println("Expression: Ident does not exist in the table of idents.");
 		}
 	}
 	
 	/**
 	 * Add the affectation part of an expression.
 	 */
 	public void affectation() {
 		if(this.affectTo.getType()==this.stackType.pop()) {
 			Yaka.yvm.istore(this.affectTo.getValue());
 		} else {
 			System.err.println("Expression: Types don't match at the affectation.");
 		}
 	}
 }
