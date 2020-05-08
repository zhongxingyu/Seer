 package chameleon.support.expression;
 
 import java.util.List;
 
 import org.rejuse.association.OrderedReferenceSet;
 
 import chameleon.core.expression.Expression;
 
 /**
  * @author Marko van Dooren
  * @author Tim Laeremans
  */
 public class FilledArrayIndex extends ArrayIndex<FilledArrayIndex> {
 
 	public FilledArrayIndex(){
 
 	}
 
 	public FilledArrayIndex(Expression expr){
 		addIndex(expr);
 	}
 
 
 	private OrderedReferenceSet<FilledArrayIndex,Expression> _expressions = new OrderedReferenceSet<FilledArrayIndex,Expression>(this);
 
 
 	public void addIndex(Expression expr){
 		if(expr != null) {
 		  _expressions.add(expr.parentLink());
 		}
 	}
 
 	public void removeIndex(Expression expr){
 		if(expr != null) {
 		  _expressions.remove(expr.parentLink());
 		}
 	}
 	
 	public List<Expression> getIndices() {
         return _expressions.getOtherEnds();
 	}
 	
 	public List<Expression> children() {
 		return getIndices();
 	}
 
 	@Override public FilledArrayIndex clone() {
		    List<Expression> els = descendants(Expression.class);
         FilledArrayIndex result = new FilledArrayIndex();
        for (Expression e : els) {
            Expression expr = e.clone();
            result.addIndex(expr);
         }
         return result;
 
 	}
 
 	@Override public String toString(){
 		int i = descendants(Expression.class).size();
 		StringBuffer result = new StringBuffer("[");
 
 		while(i > 1){
 			result.append(",");
 			i--;
 		}
 
 		result.append("]");
 		return result.toString();
 	}
 
 }
