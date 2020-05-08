 package org.araqne.logdb.query.expr;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 
 public class Split extends FunctionExpression {
 	private Expression target;
 	private final String delimiters;
 	private final int next;
 
 	public Split(QueryContext ctx, List<Expression> exprs) {
 		super("split", exprs);
		
 		if (exprs.size() < 2)
 			throw new QueryParseException("missing-split-args", -1);
 
 		this.target = exprs.get(0);
 		try {
 			this.delimiters = exprs.get(1).eval(null).toString();
 			this.next = delimiters.length();
 		} catch (NullPointerException e) {
 			throw new QueryParseException("invalid-delimiters", -1);
 		}
 	}
 
 	@Override
 	public Object eval(Row row) {
 		Object o = target.eval(row);
 		if (o == null)
 			return null;
 
 		String line = o.toString();
 		if (line.isEmpty())
 			return new ArrayList<String>(1);
 
 		int last = 0;
 		List<String> tokens = new ArrayList<String>();
 		while (true) {
 			int p = line.indexOf(delimiters, last);
 
 			if (p < 0) {
 				tokens.add(line.substring(last));
 				break;
 			} else {
 				tokens.add(line.substring(last, p));
 			}
 			last = p + next;
 		}
 
 		return tokens;
 	}
 }
