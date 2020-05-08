 package org.greatage.domain.objectify;
 
 import com.googlecode.objectify.Query;
 import org.greatage.domain.AbstractCriteriaVisitor;
 import org.greatage.domain.Criteria;
 import org.greatage.domain.Entity;
 import org.greatage.domain.JunctionCriteria;
 import org.greatage.domain.PropertyCriteria;
 import org.greatage.domain.SortCriteria;
 
 import java.io.Serializable;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class ObjectifyCriteriaVisitor<PK extends Serializable, E extends Entity<PK>> extends AbstractCriteriaVisitor<PK, E> {
	private final Query<E> query;
 
	public ObjectifyCriteriaVisitor(final Query<E> query) {
 		this.query = query;
 	}
 
 	@Override
 	protected void visitJunction(final JunctionCriteria<PK, E> criteria) {
 		if (criteria.getOperator() == JunctionCriteria.Operator.OR) {
 			throw new UnsupportedOperationException("OR operation is not supported by appengine queries");
 		}
 
 		for (Criteria<PK, E> child : criteria.getChildren()) {
 			visit(child);
 		}
 	}
 
 	@Override
 	protected void visitProperty(final PropertyCriteria<PK, E> criteria) {
 		final StringBuilder condition = new StringBuilder();
 		if (criteria.getPath() != null) {
 			condition.append(criteria.getPath()).append('.');
 		}
 		condition.append(criteria.getProperty());
 		switch (criteria.getOperator()) {
 			case EQUAL:
 				condition.append(" =");
 				break;
 			case NOT_EQUAL:
 				condition.append(" !=");
 				break;
 			case GREATER_THAN:
 				condition.append(" >");
 				break;
 			case GREATER_OR_EQUAL:
 				condition.append(" >=");
 				break;
 			case LESS_THAN:
 				condition.append(" <");
 				break;
 			case LESS_OR_EQUAL:
 				condition.append(" <=");
 				break;
 			case LIKE:
 				break;
 			case IN:
 				condition.append(" in");
 				break;
 		}
 		query.filter(condition.toString(), criteria.getValue());
 	}
 
 	@Override
 	protected void visitSort(final SortCriteria<PK, E> criteria) {
 		final StringBuilder condition = new StringBuilder();
 		if (!criteria.isAscending()) {
 			condition.append("-");
 		}
 		if (criteria.getPath() != null) {
 			condition.append(criteria.getPath()).append('.');
 		}
 		condition.append(criteria.getProperty());
 		query.order(condition.toString());
 	}
 }
