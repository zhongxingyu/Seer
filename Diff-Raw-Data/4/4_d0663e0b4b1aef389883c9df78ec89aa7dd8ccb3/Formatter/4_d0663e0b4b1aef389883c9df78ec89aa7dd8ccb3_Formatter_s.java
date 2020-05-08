 package org.suite.doer;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.parser.Operator;
 import org.parser.Operator.Assoc;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Str;
 import org.suite.node.Tree;
 
 public class Formatter {
 
 	private Operator operators[];
 	private boolean isDump;
 	private Set<Integer> set = new HashSet<Integer>();
 	private StringBuilder sb = new StringBuilder();
 
 	public Formatter(Operator operators[], boolean isDump) {
 		this.operators = operators;
 		this.isDump = isDump;
 	}
 
 	public static String display(Node node) {
 		return new Formatter(TermOp.values(), false).format(node);
 	}
 
 	public static String dump(Node node) {
 		return new Formatter(TermOp.values(), true).format(node);
 	}
 
 	private String format(Node node) {
 		format(node, 0);
 		return sb.toString();
 	}
 
 	/**
 	 * Converts a node to its string representation.
 	 * 
 	 * @param node
 	 *            Node to be converted.
 	 * @param parentPrec
 	 *            Minimum operator precedence without adding parentheses.
 	 * @param isDump
 	 *            If specified as true, the output would be machine-parsable.
 	 * @param sb
 	 *            Buffer to hold output.
 	 */
 	private void format(Node node, int parentPrec) {
 		node = node.finalNode();
 		Integer objectId = System.identityHashCode(node);
 
 		// Avoids infinite recursion if object is recursive
 		if (set.add(objectId)) {
 			format0(node, parentPrec);
 			set.remove(objectId);
 		} else
 			sb.append("<<recurse>>");
 	}
 
 	private void format0(Node node, int parentPrec) {
 		if (node instanceof Int)
 			sb.append(((Int) node).getNumber());
 		else if (node instanceof Atom) {
 			String s = ((Atom) node).getName();
 			s = isDump ? quoteAtomIfRequired(s) : s;
 			sb.append(s);
 		} else if (node instanceof Str) {
 			String s = ((Str) node).getValue();
 			s = isDump ? quote(s, '"') : s;
 			sb.append(s);
 		} else if (node instanceof Tree) {
 			Tree tree = (Tree) node;
 			Operator operator = tree.getOperator();
 			int ourPrec = operator.getPrecedence();
 			boolean needParentheses = (ourPrec <= parentPrec);
 
 			int leftPrec = ourPrec, rightPrec = ourPrec;
 			if (operator.getAssoc() == Assoc.LEFT)
 				leftPrec--;
 			else if (operator.getAssoc() == Assoc.RIGHT)
 				rightPrec--;
 
 			if (needParentheses)
 				sb.append('(');
 
 			format(tree.getLeft(), leftPrec);
 
 			if (operator != TermOp.BRACES) {
 				String name = operator.getName();
 				sb.append(name);
 
 				if (operator != TermOp.AND___ || tree.getRight() != Atom.nil) {
 					if (operator == TermOp.AND___ || operator == TermOp.OR____)
 						sb.append(' ');
 
 					format(tree.getRight(), rightPrec);
 				} // a, () suppressed as a,
 			} else {
 				sb.append(" {");
 				format(tree.getRight(), 0);
 				sb.append("}");
 			}
 
 			if (needParentheses)
 				sb.append(')');
 		} else if (node instanceof Reference)
 			sb.append(Generalizer.DEFAULTPREFIX + ((Reference) node).getId());
 		else
 			sb.append(node.getClass().getSimpleName() + '@'
 					+ Integer.toHexString(node.hashCode()));
 	}
 
 	public String quoteAtomIfRequired(String s) {
 		if (!s.isEmpty()) {
 			boolean quote = false;
 
 			for (char c : s.toCharArray())
 				if (c <= 32 //
 						|| c == '(' || c == ')' //
 						|| c == '[' || c == ']' //
 						|| c == '{' || c == '}' //
 						|| c == '\'' || c == '"' || c == '`')
 					quote = true;
 
 			for (Operator operator : operators) {
				String operatorName = operator.getName();
				if (!operatorName.trim().isEmpty() && s.contains(operatorName))
 					quote = true;
 			}
 
 			if (quote)
 				s = quote(s, '\'');
 		} else
 			s = "()";
 		return s;
 	}
 
 	public String quote(String s, char quote) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(quote);
 
 		for (char ch : s.toCharArray()) {
 			if (Character.isWhitespace(ch) && ch != ' ') {
 				if (ch >= 256)
 					sb.append(encodeHex(ch >> 8));
 				sb.append(encodeHex(ch & 0xff));
 			} else if (ch == quote || ch == '%')
 				sb.append(ch + "" + ch);
 			else
 				sb.append(ch);
 		}
 
 		sb.append(quote);
 		return sb.toString();
 	}
 
 	private String encodeHex(int i) {
 		return "%" + String.format("%02x", i).toUpperCase();
 	}
 
 }
