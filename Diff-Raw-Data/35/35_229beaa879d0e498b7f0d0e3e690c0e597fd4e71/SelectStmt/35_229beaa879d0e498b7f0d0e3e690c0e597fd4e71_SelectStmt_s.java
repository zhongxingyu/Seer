 package pl.edu.mimuw.cloudatlas.query;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import pl.edu.mimuw.cloudatlas.attributes.BooleanValue;
 import pl.edu.mimuw.cloudatlas.attributes.SimpleType;
 
 public class SelectStmt extends Stmt {
 	
 	private List<NamedExpr> selection;
 	private Expr where;
 	private List<OrderExpr> ordering;
 
 	public SelectStmt(List<NamedExpr> selection, Expr where, List<OrderExpr> ordering) {
 		assert selection != null;
 		
 		this.selection = selection;
 		this.where = where;
 		this.ordering = ordering;
 	}
 	
 	private boolean evaluateWhereForRow(Env rowEnv) throws EvaluationException {
 		if (this.where == null) {
 			return true;
 		}
 		
 		return this.where.evaluate(rowEnv).accept(new ResultVisitor<Boolean, EvaluationException>() {
 
 			public Boolean visit(OneResult result)
 					throws EvaluationException {
 				if (result.getType().equals(SimpleType.BOOLEAN)) {
 					BooleanValue value = (BooleanValue) result.getValue();
 					return value != null && value.getBoolean();
 				} else {
 					throw new EvaluationException("WHERE clause should evaluate to " + SimpleType.BOOLEAN +
 							", not " + result.getType());
 				}
 			}
 
 			public Boolean visit(ListResult result)
 					throws EvaluationException {
 				// Impossible, but better safe than sorry
 				throw new EvaluationException("WHERE clause should return single value");
 			}
 
 			public Boolean visit(ColumnResult result)
 					throws EvaluationException {
 				// Impossible, but better safe than sorry
 				throw new EvaluationException("WHERE clause should return single value");
 			}
 		});
 	}
 	
 	@SuppressWarnings({ "rawtypes" })
 	private List<Comparable> evaluateOrderingForRow(Env rowEnv) throws EvaluationException {
 		if (ordering == null) {
 			return null;
 		}
 		
 		final List<Comparable> rowOrdering = new ArrayList<Comparable>();
 		
 		for (OrderExpr orderExpr : ordering) {
 			orderExpr.getExpr().evaluate(rowEnv).accept(new ResultVisitor<Void, EvaluationException>() {
 
 				public Void visit(OneResult result)
 						throws EvaluationException {
 					if (result.getType().isComparable()) {
 						rowOrdering.add((Comparable) result.getValue());
 					} else {
 						throw new EvaluationException("ORDER BY clause should evaluate to comparable type, not " +
 								result.getType());
 					}
 					return null;
 				}
 
 				public Void visit(ListResult result)
 						throws EvaluationException {
 					// Impossible, but better safe than sorry
 					throw new EvaluationException("ORDER BY clause should return single value");
 				}
 
 				public Void visit(ColumnResult result)
 						throws EvaluationException {
 					// Impossible, but better safe than sorry
 					throw new EvaluationException("ORDER BY clause should return single value");
 				}
 			});
 		}
 		
 		return rowOrdering;
 	}
 	
 	private List<Env.Row> evaluateWhereAndOrderBy(Env env) throws EvaluationException {
 		List<RowEntry> rowEntries = new ArrayList<RowEntry>();
 		for (Env.Row row : env.getInitialRows()) {
 			Env rowEnv = env.subEnv(row);
 			if (evaluateWhereForRow(rowEnv)) {
 				rowEntries.add(new RowEntry(row, evaluateOrderingForRow(rowEnv)));
 			}
 		}
 		if (this.ordering != null) {
 			Collections.sort(rowEntries, new RowEntryComparator());
 		}
 		
 		List<Env.Row> rows = new ArrayList<Env.Row>();
 		for (RowEntry rowEntry : rowEntries) {
 			rows.add(rowEntry.row);
 		}
 		return rows;
 	}
 	
 	public List<SelectionResult> evaluate(Env env) throws EvaluationException {
 		List<Env.Row> rows = evaluateWhereAndOrderBy(env);
 		Env rowsEnv = env.subEnv(rows);
 		
 		List<SelectionResult> retVal = new ArrayList<SelectionResult>();
 		for (NamedExpr namedExpr : this.selection) {
 			retVal.add(namedExpr.evaluate(rowsEnv));
 		}
 		return retVal;
 	}
 	
 	public Result evaluateAsExpr(Env env) throws EvaluationException {
 		Result result = env.getStoredResult(this);
 		if (result != null) {
 			return result;
 		}
 		
 		List<SelectionResult> stmtResult = evaluate(env);
 		if (stmtResult.size() != 1) {
 			throw new EvaluationException("Nested SELECT should return only one value");
 		}
 		result = new OneResult(stmtResult.get(0).getType(), stmtResult.get(0).getValue());
 		env.storeResult(this, result);
 		return result;
 	}
 	
 	
 	public List<NamedExpr> getSelection() {
 		return selection;
 	}
 
 	public Expr getWhere() {
 		return where;
 	}
 
 	@Override
 	public String toString() {
 		return "SelectStmt [selection=" + selection + ", where=" + where
 				+ ", ordering=" + ordering + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((ordering == null) ? 0 : ordering.hashCode());
 		result = prime * result
 				+ ((selection == null) ? 0 : selection.hashCode());
 		result = prime * result + ((where == null) ? 0 : where.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SelectStmt other = (SelectStmt) obj;
 		if (ordering == null) {
 			if (other.ordering != null)
 				return false;
 		} else if (!ordering.equals(other.ordering))
 			return false;
 		if (selection == null) {
 			if (other.selection != null)
 				return false;
 		} else if (!selection.equals(other.selection))
 			return false;
 		if (where == null) {
 			if (other.where != null)
 				return false;
 		} else if (!where.equals(other.where))
 			return false;
 		return true;
 	}
 
 	public List<OrderExpr> getOrdering() {
 		return ordering;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private static class RowEntry {
 		private final Env.Row row;
 		private final List<Comparable> orderValues;
 		
 		private RowEntry(Env.Row row, List<Comparable> orderValues) {
 			this.row = row;
 			this.orderValues = orderValues;
 		}
 	}
 	
 	private class RowEntryComparator implements Comparator<RowEntry> {
 		
 		@SuppressWarnings({ "unchecked", "rawtypes" })
 		public int compare(RowEntry arg0, RowEntry arg1) {
			Iterator<Comparable> it0 = arg0.orderValues.iterator();
			Iterator<Comparable> it1 = arg1.orderValues.iterator();
			for (OrderExpr orderExpr : ordering) {
				Comparable obj0 = it0.next();
				Comparable obj1 = it1.next();
 				if (obj0 == null) {
 					if (obj1 != null) {
 						return orderExpr.getNullOrd().areNullsFirst(orderExpr.getOrd()) ? -1 : 1;
 					}
 				} else if (obj1 == null) {
 					return orderExpr.getNullOrd().areNullsFirst(orderExpr.getOrd()) ? 1 : -1;
 				} else {
 					int comparison = obj0.compareTo(obj1);
 					if (comparison != 0) {
 						return orderExpr.getOrd() == Ord.ASC ? comparison : -comparison;
 					}
 				}
 			}
 			return 0;
 		}
 		
 	}
 }
