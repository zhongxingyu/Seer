 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class Statement implements Comparable<Statement> {
 	
 	private final Expression expression;
 	private ArrayList<Statement> logicDependencies = new ArrayList<Statement>();
	@SuppressWarnings("unused")
 	private Drawables geometryDependencies = new Drawables();
 	private boolean hidden = false;
 	private boolean valid = false;
 	
 	public Statement(final Expression expression) {
 		this.expression = expression;
 	}
 	public Statement(final String expression) {
 		this.expression = Expression.parse(expression);
 	}
 	
 	public Statement(final Expression expression, final ArrayList<Statement> logicDependencies, final Drawables geometryDependencies) {
 		this.expression = expression;
 		this.logicDependencies = logicDependencies;
 		this.geometryDependencies = geometryDependencies;
 	}
 	public Statement(final String expression, final ArrayList<Statement> logicDependencies, final Drawables geometryDependencies) {
 		this.expression = Expression.parse(expression);
 		this.logicDependencies = logicDependencies;
 		this.geometryDependencies = geometryDependencies;
 	}
 	
 	public Expression getExpression() {
 		return expression;
 	}
 	@Override
 	public String toString() {
 		return expression.toString();
 	}
 	public String toLatex() {
 		return expression.toLatex();
 	}
 	
 	public boolean equals(Statement s) {
 		return expression.equals(s.getExpression());
 	}
 	@Override
 	public int compareTo(Statement s) {
 		return expression.compareTo(s.getExpression());
 	}
 	
 	public Statement substitute(Expression quid, Expression quo) {
 		return new Statement(expression.substitute(quid, quo));
 	}
 	public Statement substitute(HashMap<String, String> map) {
 		return new Statement(expression.substitute(map));
 	}
 	
 	public boolean isHidden() {
 		return hidden;
 	}
 	public void setHidden(boolean hidden) {
 		this.hidden = hidden;
 	}
 	
 	public boolean isValid() {
 		// The instance variable represents whether this statement is valid on its own merits.
 		if (! valid)
 			return false;
 		// Also check all the dependencies: an invalid dependency invalidates this statement.
 		else {
 			// Logic dependencies
 			for (Statement s : logicDependencies)
 				if (! s.isValid())
 					return false;
 			// TODO Uncomment when this file and the rays-and-intersections branch are brought together.
 			/*
 			for (Drawable d : geometryDependencies)
 				if (! d.exists())
 					return false;
 			 */
 			// If this.valid and all of the dependencies are also valid, return true.
 			return true;
 		}
 	}
 	public void setValid(boolean valid) {
 		this.valid = valid;
 	}
 	
 }
