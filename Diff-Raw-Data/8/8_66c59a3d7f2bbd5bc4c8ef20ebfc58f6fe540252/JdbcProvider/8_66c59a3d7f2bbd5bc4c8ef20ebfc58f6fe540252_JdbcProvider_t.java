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
 
 import edu.thu.keg.mdap.datamodel.DataContent;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.DataField.FieldType;
 import edu.thu.keg.mdap.datamodel.Query.OrderClause;
 import edu.thu.keg.mdap.datamodel.Query.WhereClause;
 import edu.thu.keg.mdap.provider.AbstractDataProvider;
 import edu.thu.keg.mdap.provider.DataProviderException;
 import edu.thu.keg.mdap.provider.IllegalQueryException;
 
 /**
  * @author Yuanchao Ma
  *
  */
 public class JdbcProvider extends AbstractDataProvider {
 	
 	private HashMap<Query, ResultSet> results;
 
 	public JdbcProvider(String connString) {
 		super(connString);
 		results = new HashMap<Query, ResultSet>();
 	}
 	
 	private synchronized Connection getConnection() throws SQLException {
 //		if (this.conn == null || this.conn.isClosed())
 //			this.conn = DriverManager.getConnection(connString);
 //		return this.conn;
 		return DriverManager.getConnection(connString);
 	}
 	
 	@Override 
 	public String getQueryString(Query q) {
 		return this.getQueryString(q, 0);
 	}
 
 	private String getQueryString(Query q, int level) {
 		DataField[] fields = q.getFields();
 		StringBuffer sb = new StringBuffer("SELECT ");
 
 		for (int i = 0; i < fields.length - 1; i++) {
 			DataField df = fields[i];
 			sb.append(df.getQueryName()).append(" AS ")
 			.append(df.getName()).append(",");
 		}
 		sb.append(fields[fields.length - 1].getQueryName()).append(" AS ")
 			.append(fields[fields.length - 1].getName());
 		
 		
 		if (q.getInnerQuery() == null)
 			sb.append(" FROM ").append(fields[0].getDataSet().getName());
 		else
 			sb.append(" FROM ( ")
 			.append(getQueryString(q.getInnerQuery(), level+1))
 			.append(" ) as t_").append(level);
 		List<WhereClause> wheres = q.getWhereClauses();
 		if (wheres.size() > 0) {
 			sb.append(" WHERE ");
 			for (int i = 0; i < wheres.size() - 1; i++) {
 				WhereClause where = wheres.get(i);
 				sb.append(whereToSB(where));
 				sb.append(" AND ");
 			}
 			sb.append(whereToSB(wheres.get(wheres.size() - 1)));
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
 			if (orders.size() > 0) {
 				sb.append(" ORDER BY ");
 				for (int i = 0; i < orders.size() - 1; i++) {
 					OrderClause order = orders.get(i);
 					sb.append(order.getField().getName())
 						.append(" ").append(order.getOrder().toString());
 					sb.append(", ");
 				}
 				sb.append(orders.get(orders.size() - 1).getField().getName())
 				.append(" ").append(orders.get(orders.size() - 1).getOrder().toString());
 			}
 		}
 		
 		
 		return sb.toString();
 	}
 	private StringBuilder whereToSB(WhereClause where) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(where.getField().getName())
 			.append(where.getOperator().toString());
 		if (where.getField().getFieldType().isNumber())
 			sb.append(where.getValue().toString());
 		else
 			sb.append("'") 
 				.append(where.getValue().toString())
 				.append("'");
 		return sb;
 	}
 	private ResultSet executeQuery(String query) throws IllegalQueryException {
 		try {
 			Statement stmt = getConnection().createStatement();
 			ResultSet rs = stmt.executeQuery(query);
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
 		try {
 			stmt = this.getConnection().createStatement();
 			stmt.executeUpdate(text);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.getConnection().close();
 			} catch (SQLException e) {
 				throw new DataProviderException(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void writeDataSetContent(DataSet ds, DataContent data) throws DataProviderException {
 		
 		removeContent(ds);
 		
 		String ddl = getDDL(ds);
 		execute(ddl);
 		
 		if (data instanceof Query) {
 			Query q = (Query)data;
 			if (q.getProvider() == ds.getProvider()) {
 				StringBuilder sb = new StringBuilder();
 				for (DataField df : ds.getDataFields()) {
 					sb.append(df.getName()).append(",");
 				}
 				
 				String insertQueryStr = "INSERT INTO " + ds.getName() + " ( "
 						+ sb.toString() + " ) SELECT " + sb.substring(0, sb.length() - 1)
 						+ " FROM ( " + q.toString() + " ) as in0";
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
 		sb.append(getDDL(fields.get(fields.size() - 1)) ).append(" ) ");
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
 			ResultSet rs = executeQuery(getQueryString(query, 0));
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
 		throw new IllegalArgumentException("Type of this field is not supported");
 	}
 
 	@Override
 	public void closeQuery(Query q) throws DataProviderException {
		Connection conn = null;
 		try {
			conn = results.get(q).getStatement().getConnection();
 			results.get(q).close();
 			results.remove(q);
 //			if (results.size() == 0)
 //				getConnection().close();
 		} catch (SQLException e) {
 			throw new DataProviderException(e.getMessage());
 		} finally {
 			try {
				conn.close();
 			} catch (SQLException e) {
 				throw new DataProviderException(e.getMessage());
 			}
 		}
 	}
 
 }
