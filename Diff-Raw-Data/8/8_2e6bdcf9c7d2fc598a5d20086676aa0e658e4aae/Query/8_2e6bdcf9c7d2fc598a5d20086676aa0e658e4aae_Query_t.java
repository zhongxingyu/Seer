 package internal.containers.query;
 
 import internal.containers.condition.ICondition;
 import internal.containers.pattern.IPattern;
 
 public class Query implements IQuery {
 	private IQuery baseQuery;
 	private IQuery subQuery;
 	
 	public Query(IPattern pattern, ICondition condition, Query subQuery) {
 		this.baseQuery	= new BaseQuery(pattern, condition);
 		this.subQuery	= subQuery;
 	}
 	
 	public Query(IQuery baseQuery, IQuery subQuery) {
 		this.baseQuery	= baseQuery;
 		this.subQuery	= subQuery;
 	}
 	
 	
 	@Override
 	public String toString() {
 		StringBuffer result = new StringBuffer(baseQuery.toString());
 		
 		if(subQuery != null)
 			result.append(" UNION " + subQuery.toString());
 		
 		return result.toString();
 		
 	}
 	
 	@Override
 	public String debugString() {
		StringBuffer result = new StringBuffer("QUERY(" + baseQuery.debugString());
		
		if(subQuery != null)
			result.append(" UNION " + subQuery.debugString());
		
		result.append(")");
		return result.toString();
 	}
 
 
 	@Override
 	public IPattern pattern() {
 		return baseQuery.pattern();
 	}
 
 
 	@Override
 	public ICondition condition() {
 		return baseQuery.condition();
 	}
 
 
 	@Override
 	public IQuery subQuery() {
 		return subQuery;
 	}
 }
