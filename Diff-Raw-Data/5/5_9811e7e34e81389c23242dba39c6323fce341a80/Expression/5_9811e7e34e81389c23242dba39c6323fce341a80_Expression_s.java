 package compilateur;
 
 import java.util.Stack;
 
 /**
  * Compute the expressions.
  */
 public class Expression {
 	private Stack<Type> stackType;
 	private Stack<Operator> stackOp;
 	private String expr;
 	private boolean invert;
 	private Ident affectTo;
 	
 	/**
 	 * Constructor
 	 */
 	public Expression() {
 		this.stackType = new Stack<Type>();
 		this.stackOp = new Stack<Operator>();
 		this.invert = false;
 		this.expr = "";
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
 		if(this.invert) {
 			ent = -ent;
 			this.invert = false;
 		}
 		this.expr += Yaka.yvm.iconst(ent);
 	}
 	
 	/**
 	 * Push a boolean to the stack of values.
 	 * @param bool The boolean.
 	 */
 	public void pushBoolean(int bool) {
 		this.stackType.push(Type.BOOL);
 		if(this.invert) {
 			if (bool == Constante.TRUE) {
 				bool = Constante.FALSE;
 			} else if (bool == Constante.FALSE) {
 				bool = Constante.TRUE;
 			} else {
 				System.err.println("Expression: Booleen mal defini");
 			}
 			this.invert = false;
 		}
 		this.expr += Yaka.yvm.iconst(bool);
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
 			this.expr += Yaka.yvm.iload(ident.getValue());
 		} else {
 			this.expr += Yaka.yvm.iconst(ident.getValue());
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
 					this.expr += Yaka.yvm.ior();
 					this.stackType.push(Type.BOOL);
 					break;
 				case AND:
 					this.expr += Yaka.yvm.iand();
 					this.stackType.push(Type.BOOL);
 					break;
 				case DIFF:
 					this.expr += Yaka.yvm.idiff();
 					this.stackType.push(Type.BOOL);
 					break;
 				case EQUAL:
 					this.expr += Yaka.yvm.iegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				default:
 					this.stackType.push(Type.ERROR);
 			}
 		} else if((a==Type.INT || a==Type.ERROR) && (b==Type.INT || b==Type.ERROR)) {
 			switch(op) {
 				case ADD:
 					this.expr += Yaka.yvm.iadd();
 					this.stackType.push(Type.INT);
 					break;
 				case SUB:
 					this.expr += Yaka.yvm.isub();
 					this.stackType.push(Type.INT);
 					break;
 				case MUL:
 					this.expr += Yaka.yvm.imul();
 					this.stackType.push(Type.INT);
 					break;
 				case DIV:
 					this.expr += Yaka.yvm.idiv();
 					this.stackType.push(Type.INT);
 					break;
 				case LT:
 					this.expr += Yaka.yvm.iinf();
 					this.stackType.push(Type.BOOL);
 					break;
 				case LTE:
 					this.expr += Yaka.yvm.iinfegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				case GT:
 					this.expr += Yaka.yvm.isup();
 					this.stackType.push(Type.BOOL);
 					break;
 				case GTE:
 					this.expr += Yaka.yvm.isupegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				case DIFF:
 					this.expr += Yaka.yvm.idiff();
 					this.stackType.push(Type.BOOL);
 					break;
 				case EQUAL:
 					this.expr += Yaka.yvm.iegal();
 					this.stackType.push(Type.BOOL);
 					break;
 				default:
 					this.stackType.push(Type.ERROR);
 			}
 		} else {
 			System.err.println("Expression: The two operands doesn't match.");
 		}
 	}
 	
 	/**
 	 * @return The final result: the generated code.
 	 */
 	public String getResult() {
 		return this.expr;
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
 			this.expr += Yaka.yvm.istore(this.affectTo.getValue());
 		} else {
 			System.err.println("Expression: Types don't match at the affectation.");
 		}
 	}
 }
