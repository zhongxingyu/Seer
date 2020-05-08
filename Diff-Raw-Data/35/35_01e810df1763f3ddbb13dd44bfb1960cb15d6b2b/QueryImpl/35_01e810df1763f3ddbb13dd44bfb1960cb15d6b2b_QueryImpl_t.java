 package edu.thu.keg.mdap_impl.datamodel;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import edu.thu.keg.mdap.datamodel.AggregatedDataField;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.GeneralDataField;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.provider.DataProvider;
 import edu.thu.keg.mdap.provider.DataProviderException;
 
 /**
  * Simple implementation of the Query interface. Only a wrapper around plain SQL
  * query.
  * 
  * @author Yuanchao Ma
  * 
  */
 public class QueryImpl implements Query {
 
 	QueryImpl(DataSet ds) {
 		this.fields = ds.getDataFields().toArray(new DataField[0]);
 		this.wheres = new ArrayList<WhereClause>();
 		this.orders = new ArrayList<OrderClause>();
 		this.join = null;
 		this.innerQuery = null;
 		this.setProvider(ds.getProvider());
 
 		this.state = DataContentState.Ready;
 	}
 
 	public QueryImpl(Query q) {
 		this.fields = q.getFields();
 		this.wheres = q.getWhereClauses();
 		this.orders = q.getOrderClauses();
 		this.join = q.getJoinOnClause();
 		this.provider = q.getProvider();
 		this.innerQuery = q.getInnerQuery();
 		this.state = DataContentState.Ready;
 	}
 
 	QueryImpl(DataField[] fields, List<WhereClause> wheres,
 			List<OrderClause> orders, JoinOnClause join, DataProvider provider,
 			Query innerQuery) {
 		if (innerQuery == null) {
 			this.fields = fields.clone();
 			this.wheres = wheres;
 			this.orders = orders;
 			this.join = join;
 			this.provider = provider;
 			this.innerQuery = innerQuery;
 		} else {
 			this.fields = fields.clone();
 			this.wheres = wheres;
 			this.orders = orders;
 			this.join = join;
 			this.provider = provider;
 			this.innerQuery = innerQuery;
 		}
 		this.state = DataContentState.Ready;
 	}
 
 	private Query innerQuery;
 	private DataContentState state;
 
 	private DataField[] fields;
 	private List<WhereClause> wheres;
 	private List<OrderClause> orders;
 	private JoinOnClause join;
 
 	public void setFields(DataField[] fields) {
 		this.fields = fields;
 	}
 
 	@Override
 	public DataField[] getFields() {
 		return fields;
 	}
 
 	private DataProvider provider = null;
 
 	@Override
 	public DataProvider getProvider() {
 		return this.provider;
 	}
 
 	@Override
 	public void setProvider(DataProvider provider) {
 		this.provider = provider;
 	}
 
 	@Override
 	public void close() throws DataProviderException {
 		this.getProvider().closeQuery(this);
 		this.state = DataContentState.Closed;
 	}
 
 	@Override
 	public boolean next() throws DataProviderException {
 		return this.getProvider().next(this);
 	}
 
 	@Override
 	public Object getValue(DataField field) throws DataProviderException {
 		return this.getProvider().getValue(this, field);
 	}
 
 	@Override
 	public void open() throws DataProviderException {
 		this.getProvider().openQuery(this);
 		this.state = DataContentState.Opened;
 	}
 
 	@Override
 	public Query select(DataField... fields) {
 		Query q = null;
 
 		q = new QueryImpl(fields.clone(), new ArrayList<WhereClause>(),
 				new ArrayList<OrderClause>(), null, this.provider, this);
 		transformFieldsSelect(q);
 		return q;
 	}
 
 	@Override
 	public Query whereOr(String fieldName, Operator op, Object value) {
 		DataField field = null;
 		for (DataField tdf : this.fields) {
 			if (tdf.getName().equals(fieldName)) {
 				field = tdf;
 			}
 		}
 		if (field == null) {
 			throw new IllegalArgumentException("The field " + fieldName
 					+ " does not exist in the field list of this query");
 		}
 		List<WhereClause> wheres = new ArrayList<WhereClause>();
 		wheres.addAll(this.wheres);
 		wheres.add(new WhereClause(field, op, value));
 		Query q = new QueryImpl(this.fields, wheres, this.orders, this.join,
 				this.provider, this.getInnerQuery());
 		// transformFields(q);
 		return q;
 
 	}
 
 	@Override
 	public Query whereAnd(String fieldName, Operator op, Object value) {
 		DataField field = null;
 		for (DataField tdf : this.fields) {
 			if (tdf.getName().equals(fieldName)) {
 				field = tdf;
 			}
 		}
 		if (field == null) {
 			throw new IllegalArgumentException("The field " + fieldName
 					+ " does not exist in the field list of this query");
 		}
 		List<WhereClause> wheres = new ArrayList<WhereClause>();
 		wheres.add(new WhereClause(field, op, value));
 		wheres.get(0).setInnerWhereClauses(this.wheres);
 		Query q = new QueryImpl(this.fields, wheres, this.orders, this.join,
 				this.provider, this.getInnerQuery());
 		// transformFields(q);
 		return q;
 
 	}
 
 	@Override
 	public Query join(Query q2, Map<DataField, DataField> fieldsMap) {
 		JoinOnClause newJoin = new JoinOnClause(q2, fieldsMap);
 		Query q = new QueryImpl(this.fields, new ArrayList<WhereClause>(),
 				new ArrayList<OrderClause>(), newJoin, this.provider, this);
 		transformJoinFields(q);
 		return q;
 	}
 
 	@Override
 	public List<WhereClause> getWhereClauses() {
 		return this.wheres;
 	}
 
 	@Override
 	public Query orderBy(String fieldName, Order order) {
 
 		DataField field = null;
 		for (DataField tdf : this.fields) {
 			if (tdf.getName().equalsIgnoreCase(fieldName)) {
 				field = tdf;
 			}
 		}
 		if (field == null) {
 			throw new IllegalArgumentException("The field " + fieldName
 					+ " does not exist in the field list of this query");
 		}
 		List<OrderClause> orders = new ArrayList<OrderClause>();
 		orders.addAll(this.orders);
 		orders.add(new OrderClause(field, order));
 		return new QueryImpl(this.fields, this.wheres, orders, this.join,
 				this.provider, this.getInnerQuery());
 	}
 
 	@Override
 	public List<OrderClause> getOrderClauses() {
 		return this.orders;
 	}
 
 	@Override
 	public Query getInnerQuery() {
 		return this.innerQuery;
 	}
 
 	@Override
 	public List<DataField> getGroupByFields() {
 		List<DataField> gf = new ArrayList<DataField>();
 		int count = 0;
 		for (DataField f : this.fields) {
 			if (!(f instanceof AggregatedDataField)) {
 				gf.add(f);
 				count++;
 			}
 		}
 		if (count == this.fields.length) {
 			return null;
 		}
 		return gf;
 	}
 
 	private void transformFieldsSelect(Query q) {
 		DataField[] df_all = new DataField[q.getFields().length];
 		for (int i = 0; i < q.getFields().length; i++) {
 			DataField df = q.getFields()[i];
 			if (df instanceof AggregatedDataField) {
 				// df = (AggregatedDataField) df;
 				df_all[i] = new AggregatedDataField(
 						((AggregatedDataField) df).getField(),
 						((AggregatedDataField) df).getFunc(), df.getName(),
 						q.getInnerQuery());
 
 			} else {
 				df_all[i] = new GeneralDataField(df.getName(),
 						df.getFieldType(), df.getDescription(), df.isKey(),
 						df.allowNull(), df.isDim(), df.getFunction(),
 						q.getInnerQuery());
 				// df_all[i].setDataSet(df.getDataSet());
 			}
 			if (q.getInnerQuery() == null) {
 				df.setDataSet(df.getDataSet());
 			}
 		}
 		q.setFields(df_all);
 	}
 
 	private void transformJoinFields(Query q) {
 		int joinClauseLen = q.getJoinOnClause().getQuery().getFields().length;
 		DataField[] df_all = new DataField[q.getFields().length + joinClauseLen];
 		// 复制所有q1,q2的fields到select中
 		for (int i = 0; i < q.getFields().length; i++) {
 			DataField df = q.getFields()[i];
 			df_all[i] = new GeneralDataField(df.getName(), df.getFieldType(),
 					df.getDescription(), df.isKey(), df.allowNull(),
 					df.isDim(), df.getFunction(), q.getInnerQuery());
//			if (q.getInnerQuery() == null) {
//				df_all[i].setDataSet(df.getDataSet());
//			}
 		}
 		for (int i = 0; i < joinClauseLen; i++) {
 			Query q2 = q.getJoinOnClause().getQuery();
 			DataField df = q2.getFields()[i];
 			df_all[i + q.getFields().length] = new GeneralDataField(
 					df.getName(), df.getFieldType(), df.getDescription(),
 					df.isKey(), df.allowNull(), df.isDim(), df.getFunction(),
 					q2);
 			df_all[i + q.getFields().length].setDataSet(df.getDataSet());
//			if (q2 == null) {
//				df_all[i].setDataSet(df.getDataSet());
//			}
 		}
 		q.setFields(df_all);
 
 		// 将所有要join里实现on的DataField的query设置成自己来自表的query
 		HashMap<DataField, DataField> nfs = new HashMap<DataField, DataField>();
 		for (DataField fs : q.getJoinOnClause().getOns().keySet()) {
 			DataField nk = new GeneralDataField(fs.getName(),
 					fs.getFieldType(), fs.getDescription(), fs.isKey(),
 					fs.allowNull(), fs.isDim(), fs.getFunction(),
 					q.getInnerQuery());
 			DataField df = q.getJoinOnClause().getOns().get(fs);
 			DataField nv = new GeneralDataField(df.getName(),
 					df.getFieldType(), df.getDescription(), df.isKey(),
 					df.allowNull(), df.isDim(), df.getFunction(), q
 							.getJoinOnClause().getQuery());
 
 			nfs.put(nk, nv);
 		}
 		// 重新设置join的on的属性
 		q.getJoinOnClause().setOns(nfs);
 	}
 
 	@Override
 	public DataContentState getState() {
 		return this.state;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return this.provider.getQueryString(this);
 	}
 
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see java.lang.Object#hashCode()
 	// */
 	// @Override
 	// public int hashCode() {
 	// final int prime = 31;
 	// int result = 1;
 	// result = prime * result + Arrays.hashCode(fields);
 	// result = prime * result
 	// + ((innerQuery == null) ? 0 : innerQuery.hashCode());
 	// result = prime * result + ((orders == null) ? 0 : orders.hashCode());
 	// result = prime * result
 	// + ((provider == null) ? 0 : provider.hashCode());
 	// result = prime * result + ((state == null) ? 0 : state.hashCode());
 	// result = prime * result + ((wheres == null) ? 0 : wheres.hashCode());
 	// return result;
 	// }
 	//
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see java.lang.Object#equals(java.lang.Object)
 	// */
 	// @Override
 	// public boolean equals(Object obj) {
 	// if (this == obj)
 	// return true;
 	// if (obj == null)
 	// return false;
 	// if (getClass() != obj.getClass())
 	// return false;
 	// QueryImpl other = (QueryImpl) obj;
 	// if (!Arrays.equals(fields, other.fields))
 	// return false;
 	// if (innerQuery == null) {
 	// if (other.innerQuery != null)
 	// return false;
 	// } else if (!innerQuery.equals(other.innerQuery))
 	// return false;
 	// if (orders == null) {
 	// if (other.orders != null)
 	// return false;
 	// } else {
 	// if (orders.size() != other.orders.size())
 	// return false;
 	// for (int i = 0; i < orders.size(); i++) {
 	// if (!orders.get(i).getField()
 	// .equals(other.orders.get(i).getField()))
 	// return false;
 	// if (orders.get(i).getOrder()
 	// .compareTo(other.orders.get(i).getOrder()) != 0)
 	// return false;
 	// }
 	// }
 	// if (provider == null) {
 	// if (other.provider != null)
 	// return false;
 	// } else if (!provider.getConnectionString().equals(
 	// other.provider.getConnectionString()))
 	// return false;
 	// if (state != other.state)
 	// return false;
 	// if (wheres == null) {
 	// if (other.wheres != null)
 	// return false;
 	// } else {
 	// if (wheres.size() != other.wheres.size())
 	// return false;
 	// for (int i = 0; i < wheres.size(); i++) {
 	// if (!wheres.get(i).getField()
 	// .equals(other.wheres.get(i).getField()))
 	// return false;
 	// if (wheres.get(i).getOperator()
 	// .compareTo(other.wheres.get(i).getOperator()) != 0)
 	// return false;
 	// if (wheres.get(i).getValue() != other.wheres.get(i).getValue())
 	// return false;
 	// }
 	// }
 	// return true;
 	// }
 
 	@Override
 	public JoinOnClause getJoinOnClause() {
 		return this.join;
 	}
 }
