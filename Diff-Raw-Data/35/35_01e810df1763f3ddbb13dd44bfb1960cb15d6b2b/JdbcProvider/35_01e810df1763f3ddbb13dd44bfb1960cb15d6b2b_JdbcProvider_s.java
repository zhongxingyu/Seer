 /**
  * 
  */
 package edu.thu.keg.mdap_impl.provider;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import edu.thu.keg.mdap.datamodel.AggregatedDataField;
 import edu.thu.keg.mdap.datamodel.DataContent;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.GeneralDataField;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.DataField.FieldType;
 import edu.thu.keg.mdap.datamodel.Query.OrderClause;
 import edu.thu.keg.mdap.datamodel.Query.WhereClause;
 import edu.thu.keg.mdap.provider.AbstractDataProvider;
 import edu.thu.keg.mdap.provider.DataProviderException;
 import edu.thu.keg.mdap.provider.IllegalQueryException;
 
 /**
  * @author Yuanchao Ma, Bozhi Yuan
  * 
  */
 public class JdbcProvider extends AbstractDataProvider {
 
 	protected HashMap<Query, ResultSet> results;
 
 	public JdbcProvider(String connString) {
 		super(connString);
 		if (results == null)
 			results = new HashMap<Query, ResultSet>();
 	}
 
 	public JdbcProvider(String connString, String username, String password) {
 		super(connString, username, password);
 		if (results == null)
 			results = new HashMap<Query, ResultSet>();
 	}
 
 	private synchronized Connection getConnection() throws SQLException {
 		// if (this.conn == null || this.conn.isClosed())
 		// this.conn = DriverManager.getConnection(connString);
 		// return this.conn ;
 		return DriverManager.getConnection(connString);
 	}
 
 	@Override
 	public String getQueryString(Query q) {
 		return this.getQueryString(q, 0);
 	}
 
 	private String getQueryString(Query q, int level) {
 		HashMap<Query, String> aliasMap = new HashMap<Query, String>();
 		if (q.getInnerQuery() != null) {
 			String alias = "t_" + level;
 			aliasMap.put(q.getInnerQuery(), alias);
 		}
 
 		if (q.getJoinOnClause() != null) {
 			String alias = "tj_" + level;
 			aliasMap.put(q.getJoinOnClause().getQuery(), alias);
 		}
 
 		DataField[] fields = q.getFields();
 		StringBuffer sb = new StringBuffer("SELECT ");
 
 		for (int i = 0; i < fields.length - 1; i++) {
 			DataField df = fields[i];
 			if (q.getInnerQuery() == null) {
 				sb.append(df.getQueryName()).append(" AS ")
 						.append(df.getName()).append(",");
 			} else {
 				sb.append(getFieldAliasName(df, aliasMap)).append(" AS ")
 						.append(df.getName()).append(",");
 			}
 		}
 		DataField df = fields[fields.length - 1];
 		if (q.getInnerQuery() == null) {
 			sb.append(df.getQueryName()).append(" AS ").append(df.getName());
 		} else {
 			sb.append(getFieldAliasName(df, aliasMap)).append(" AS ")
 					.append(df.getName());
 		}
 
 		if (q.getInnerQuery() == null)
 			sb.append(" FROM ").append(fields[0].getDataSet().getName());
 		else {
 
 			sb.append(" FROM ( ")
 					.append(getQueryString(q.getInnerQuery(), level + 1))
					.append(" ) as ").append(aliasMap.get(q.getInnerQuery()));
 		}
 		if (q.getJoinOnClause() != null) {
 			sb.append(" INNER JOIN (")
 					.append(getQueryString(q.getJoinOnClause().getQuery(),
 							level + 1)).append(") AS ")
 					.append(aliasMap.get(q.getJoinOnClause().getQuery()))
 					.append(" ON ");
 			for (Entry<DataField, DataField> fs : q.getJoinOnClause().getOns()
 					.entrySet()) {
 				sb.append(getFieldAliasName(fs.getKey(), aliasMap)).append("=")
 						.append(getFieldAliasName(fs.getValue(), aliasMap))
 						.append(" AND ");
 			}
 			sb.delete(sb.length() - 4, sb.length());
 		}
 
 		List<WhereClause> wheres = q.getWhereClauses();
 		if (wheres != null && wheres.size() > 0) {
 			sb.append(" WHERE ");
 			sb.append(whereToSB(wheres, aliasMap));
 		}
 
 		List<DataField> gb = q.getGroupByFields();
 		if (gb != null && gb.size() > 0) {
 			sb.append(" GROUP BY ");
 			for (int i = 0; i < gb.size() - 1; i++) {
 				sb.append(gb.get(i).getName()).append(", ");
 			}
 			sb.append(gb.get(gb.size() - 1).getName());
 		}
 
 		if (level == 0) {
 			List<OrderClause> orders = q.getOrderClauses();
 			if (orders != null && orders.size() > 0) {
 				sb.append(" ORDER BY ");
 				for (int i = 0; i < orders.size() - 1; i++) {
 					OrderClause order = orders.get(i);
 					sb.append(getFieldAliasName(order.getField(), aliasMap))
 							.append(" ").append(order.getOrder().toString());
 					sb.append(", ");
 				}
 				sb.append(
 						getFieldAliasName(orders.get(orders.size() - 1)
 								.getField(), aliasMap))
 						.append(" ")
 						.append(orders.get(orders.size() - 1).getOrder()
 								.toString());
 			}
 		}
 
 		return sb.toString();
 	}
 
 	private String getFieldAliasName(DataField f, Map<Query, String> aliasMap) {
 		if (f.getQuery() == null) {
 			return f.getQueryName();
 		} else {
 			if (f instanceof GeneralDataField)
 				return aliasMap.get(f.getQuery()) + "." + f.getQueryName();
 			else if (f instanceof AggregatedDataField)
 				return ((AggregatedDataField) f).getQueryName(aliasMap.get(f
 						.getQuery()));
 		}
 		return null;
 	}
 
 	private StringBuilder whereToSB(WhereClause where,
 			Map<Query, String> aliasMap) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getFieldAliasName(where.getField(), aliasMap)).append(
 				where.getOperator().toString());
 		if (where.getField().getFieldType().isNumber())
 			sb.append(where.getValue().toString());
 		else
 			sb.append("'").append(where.getValue().toString()).append("'");
 		return sb;
 	}
 
 	private String whereToSB(List<WhereClause> wheres,
 			Map<Query, String> aliasMap) {
 		String sb = "";
 
 		for (int i = 0; i < wheres.size(); i++) {
 			WhereClause where = wheres.get(i);
 			String sb_temp = "";
 			sb_temp = sb_temp + (getFieldAliasName(where.getField(), aliasMap))
 					+ (where.getOperator().toString());
 			if (where.getField().getFieldType().isNumber())
 				sb_temp = sb_temp + (where.getValue().toString());
 			else
 				sb_temp = sb_temp + "'" + where.getValue().toString() + "'";
 			if (where.getInnerWhereClauses() != null)
 				sb_temp = "(" + sb_temp + " and ( "
 						+ whereToSB(where.getInnerWhereClauses(), aliasMap)
 						+ " ) )";
 			sb = sb + sb_temp + " or ";
 		}
 		return sb.substring(0, sb.lastIndexOf("or"));
 
 	}
 
 	protected ResultSet executeQuery(String queryStr)
 			throws IllegalQueryException {
 		Connection conn = null;
 		try {
 			System.out.println(connString + " : " + queryStr);
 			conn = getConnection();
 			Statement stmt = conn.createStatement();
 			ResultSet rs = stmt.executeQuery(queryStr);
 			super.rsMapConn.put(rs, conn);
 			return rs;
 		} catch (Exception ex) {
 			throw new IllegalQueryException(ex.getMessage());
 		}
 	}
 
 	@Override
 	public void removeContent(DataSet ds) throws DataProviderException {
 		execute(getDrop(ds));
 	}
 
 	private String getDrop(DataSet ds) {
 		return "DROP TABLE " + ds.getName();
 	}
 
 	public void execute(String text) throws DataProviderException {
 		Statement stmt = null;
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			stmt = conn.createStatement();
 			stmt.executeUpdate(text);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				conn.close();
 			} catch (SQLException e) {
 				throw new DataProviderException(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void writeDataSetContent(DataSet ds, DataContent data)
 			throws DataProviderException {
 
 		removeContent(ds);
 
 		String ddl = getDDL(ds);
 		execute(ddl);
 		// 将data里面的字段复制到ds中，复制ds中拥有的所有字段
 		if (data instanceof Query) {
 			Query q = (Query) data;
 			if (q.getProvider() == ds.getProvider()) {
 				StringBuilder sb = new StringBuilder();
 				for (DataField df : ds.getDataFields()) {
 					sb.append(df.getName()).append(",");
 				}
 
 				String insertQueryStr = "INSERT INTO " + ds.getName() + " ( "
 						+ sb.toString() + " ) SELECT "
 						+ sb.substring(0, sb.length() - 1) + " FROM ( "
 						+ q.toString() + " ) as in0";
 				execute(insertQueryStr);
 			}
 		}
 	}
 
 	private String getDDL(DataSet ds) {
 		StringBuilder sb = new StringBuilder("CREATE TABLE ");
 		sb.append(ds.getName());
 		sb.append(" ( ");
 		List<DataField> fields = ds.getDataFields();
 		for (int i = 0; i < fields.size() - 1; i++) {
 			sb.append(getDDL(fields.get(i))).append(",");
 		}
 		sb.append(getDDL(fields.get(fields.size() - 1))).append(" ) ");
 		return sb.toString();
 	}
 
 	private String getDDL(DataField field) {
 		FieldType type = field.getFieldType();
 		String typeStr = "";
 		switch (type) {
 		case ShortString:
 			typeStr = " NVARCHAR(50) ";
 			break;
 		case LongString:
 			typeStr = " NVARCHAR(200) ";
 			break;
 		case Text:
 			typeStr = " NVARCHAR(999) ";
 			break;
 		case Double:
 			typeStr = " FLOAT ";
 			break;
 		case Int:
 			typeStr = " INTEGER ";
 			break;
 		case DateTime:
 			typeStr = " DATETIME ";
 			break;
 		}
 		String nullStr = "";
 		if (field.allowNull())
 			nullStr = " NULL ";
 		else
 			nullStr = " NOT NULL ";
 		return field.getName() + typeStr + nullStr;
 	}
 
 	@Override
 	public void openQuery(Query query) throws DataProviderException {
 		if (!results.containsKey(query)) {
 			String sql = getQueryString(query, 0);

 			ResultSet rs = executeQuery(sql);
 			results.put(query, rs);
 		}
 	}
 
 	@Override
 	public boolean next(Query q) throws DataProviderException {
 		try {
 			return results.get(q).next();
 		} catch (SQLException e) {
 			throw new DataProviderException(e.getMessage());
 		}
 	}
 
 	@Override
 	public Object getValue(Query q, DataField field)
 			throws DataProviderException {
 		try {
 			ResultSet rs = results.get(q);
 			FieldType type = field.getFieldType();
 			switch (type) {
 			case ShortString:
 			case LongString:
 			case Text:
 				return rs.getString(field.getName());
 			case Double:
 				return rs.getDouble(field.getName());
 			case Int:
 				return rs.getInt(field.getName());
 			case DateTime:
 				return rs.getDate(field.getName());
 			}
 		} catch (SQLException e) {
 			throw new DataProviderException(e.getMessage());
 		}
 		throw new IllegalArgumentException(
 				"Type of this field is not supported");
 	}
 
 	@Override
 	public void closeQuery(Query q) throws DataProviderException {
 		try {
 			ResultSet rs = results.get(q);
 			rs.close();
 			results.remove(q);
 			super.rsMapConn.get(rs).close();
 		} catch (SQLException e) {
 			throw new DataProviderException(e.getMessage());
 		}
 	}
 
 }
