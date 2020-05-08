 package org.araqne.logdb.query.parser;
 
 import java.util.Stack;
 
 import org.araqne.logdb.query.expr.BooleanConstant;
 import org.araqne.logdb.query.expr.EvalField;
 import org.araqne.logdb.query.expr.Expression;
 import org.araqne.logdb.query.expr.NumberConstant;
 import org.araqne.logdb.query.expr.StringConstant;
 import org.araqne.logdb.query.parser.ExpressionParser.TokenTerm;
 
 public class EvalTermEmitterFactory implements TermEmitterFactory {
 	@Override
 	public void emit(Stack<Expression> exprStack, TokenTerm t) {
 		if (!t.getText().equals("(") && !t.getText().equals(")")) {
 			String token = ((TokenTerm) t).getText().trim();
 			Expression expr = parseTokenExpr(exprStack, token);
 			exprStack.add(expr);
 		}
 	}
 
 	private Expression parseTokenExpr(Stack<Expression> exprStack, String token) {
 		// is quoted?
 		if (token.startsWith("\"") && token.endsWith("\""))
 			return new StringConstant(token.substring(1, token.length() - 1));
 
 		try {
 			long v = Long.parseLong(token);
 			if (Integer.MIN_VALUE <= v && v <= Integer.MAX_VALUE)
 				return new NumberConstant((int) v);
 			return new NumberConstant(v);
 		} catch (NumberFormatException e1) {
 			try {
 				double v = Double.parseDouble(token);
 				return new NumberConstant(v);
 			} catch (NumberFormatException e2) {
				if (token.equals("true") || token.equals("false"))
					return new BooleanConstant(Boolean.parseBoolean(token));
 
 				return new EvalField(token);
 			}
 		}
 	}
 }
