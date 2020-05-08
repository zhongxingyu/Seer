 package srt.tool;
 
 import srt.ast.BinaryExpr;
 import srt.ast.DeclRef;
 import srt.ast.Expr;
 import srt.ast.IntLiteral;
 import srt.ast.TernaryExpr;
 import srt.ast.UnaryExpr;
 import srt.ast.visitor.impl.DefaultVisitor;
 import srt.util.Names;
 
 public class ExprToSmtlibVisitor extends DefaultVisitor {
 	
 	public ExprToSmtlibVisitor() {
 		super(false);
 	}
 
 	@Override
 	public String visit(BinaryExpr expr) {
 		String operator = null;
 		switch(expr.getOperator())
 		{
 			case BinaryExpr.ADD:
 				operator = "(bvadd %s %s)";
 				break;
 			case BinaryExpr.BAND:
 				operator = "(bvand %s %s)";
 				break;
 			case BinaryExpr.BOR:
 				operator = "(bvor %s %s)";
 				break;
 			case BinaryExpr.BXOR:
 				operator = "(bvxor %s %s)";
 				break;
 			case BinaryExpr.DIVIDE:
				// Only works for positive numbers.
				// TODO: Find out if this is the right behaviour
				operator = "(bvudiv %s %s)";
 				break;
 			case BinaryExpr.LSHIFT:
 				operator = "(bvshl %s %s)";
 				break;
 			case BinaryExpr.MOD:
 				operator = "(bvurem %s %s)";
 				break;
 			case BinaryExpr.MULTIPLY:
 				operator = "(bvmul %s %s)";
 				break;
 			case BinaryExpr.RSHIFT:
				// Only works for positive numbers.
				// TODO: Fix it
				operator = "(bvlshr %s %s)";
 				break;
 			case BinaryExpr.SUBTRACT:
 				operator = "(bvsub %s %s)";
 				break;
 				
 			case BinaryExpr.LAND:
 				operator = String.format("(%s (and (%s %%s) (%s %%s)))",
 						Names.toBVectorFunction,
 						Names.toBoolFunction,
 						Names.toBoolFunction);
 				break;
 			case BinaryExpr.LOR:
 				operator = String.format("(%s (or (%s %%s) (%s %%s)))",
 						Names.toBVectorFunction,
 						Names.toBoolFunction,
 						Names.toBoolFunction);
 				break;
 				
 			case BinaryExpr.GEQ:
 				operator = String.format("(%s (bvsge %%s %%s))", Names.toBVectorFunction);
 				break;
 			case BinaryExpr.GT:
 				operator = String.format("(%s (bvsgt %%s %%s))", Names.toBVectorFunction);
 				break;
 			case BinaryExpr.LEQ:
 				operator = String.format("( %s (bvsle %%s %%s))", Names.toBVectorFunction);
 				break;
 			case BinaryExpr.LT:
 				operator = String.format("( %s (bvslt %%s %%s))", Names.toBVectorFunction);
 				break;
 			case BinaryExpr.NEQUAL:
 				operator = String.format("( %s  (not (= %%s %%s)))", Names.toBVectorFunction);
 				break;
 			case BinaryExpr.EQUAL:
 				operator = String.format("( %s (= %%s %%s))", Names.toBVectorFunction);
 				break;
 			default:
 				throw new IllegalArgumentException("Invalid binary operator");
 		}
 		
 		return String.format(operator, visit(expr.getLhs()), visit(expr.getRhs()));
 		
 	}
 
 	@Override
 	public String visit(DeclRef declRef) {
 		return declRef.getName();
 	}
 
 	@Override
 	public String visit(IntLiteral intLiteral) {
 		/* We need to convert given number to a String Hex representation.
 		 * For example 10 = #x0000000a
 		 */
 		String hex = Integer.toHexString(intLiteral.getValue());
 		StringBuilder hexString = new StringBuilder("#x00000000");
 		hexString.replace(hexString.length()-hex.length(), hexString.length(), hex);
 		return hexString.toString();
 	}
 
 	@Override
 	public String visit(TernaryExpr ternaryExpr) {
 		return null;
 	}
 
 	@Override
 	public String visit(UnaryExpr unaryExpr) {
 		String operator = null;
 		switch(unaryExpr.getOperator())
 		{
 		case UnaryExpr.UMINUS:
 			operator = "(bvneg %s)";
 			break;
 		case UnaryExpr.UPLUS:
 			operator = "%s";
 			break;
 		case UnaryExpr.LNOT:
 			operator = String.format("(%s (not (%s %%s)))",
 										Names.toBVectorFunction,
 										Names.toBoolFunction);
 			break;
 		case UnaryExpr.BNOT:
 			operator = "(bvnot %s)";
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid binary operator");
 		}
 		
 		return String.format(operator, visit(unaryExpr.getOperand()));
 	}
 	
 	
 	/* Overridden just to make return type String. 
 	 * @see srt.ast.visitor.DefaultVisitor#visit(srt.ast.Expr)
 	 */
 	@Override
 	public String visit(Expr expr) {
 		return (String) super.visit(expr);
 	}
 	
 	
 
 }
