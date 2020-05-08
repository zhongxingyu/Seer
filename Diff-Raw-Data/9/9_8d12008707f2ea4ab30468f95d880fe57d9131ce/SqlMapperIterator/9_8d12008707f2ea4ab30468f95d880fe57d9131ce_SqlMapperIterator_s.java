 package com.gushuley.utils.orm.sql;
 
 import java.sql.*;
 import java.util.*;
 
 
 import org.apache.log4j.Logger;
 
 import com.gushuley.utils.orm.*;
 
 public class SqlMapperIterator<T extends ORMObject<K>, K> {
 	private final static Logger log = Logger.getLogger(SqlMapperIterator.class);
 	private ResultSet rs;
 	private PreparedStatement stm;
 	private AbstractSqlMapper<T, K> mapper;
 
 	public SqlMapperIterator(AbstractSqlMapper<T, K> mapper)
 			throws ORMException {
 		this.mapper = mapper;
 		Connection cnn = mapper.getContext().
 			getConnection(mapper.getConnectionKey(), false);
 		try {
 			stm = cnn.prepareStatement(getSelectSqlText());
 			try {
 				rs = stm.executeQuery();
 			} catch (SQLException e) {
 				stm.close();
 				throw new ORMException(e);
 			}
 		} catch (SQLException e) {
 			throw new ORMException(e);
 		}
 	}
 	
 	protected String getSelectSqlText() {
 		return mapper.getSelectAllSql();
 	}
	protected void setParams(PreparedStatement stm) throws ORMException {
 		mapper.setSelectAllStatementParams(stm);
 	}
 	
 	public void close() {
 		try {
 			stm.close();
 		} catch (SQLException e) {
 			log.error(e);
 		}
 	}
 
 	public List<T> get(int len) throws ORMException {
 		try {
 			List<T> list = new ArrayList<T>();
 			while (list.size() < len && rs.next()) {
 				list.add(mapper.loadObject(rs));
 			}
 			return list;
 		} catch (SQLException e) {
 			throw new ORMException(e);
 		}				
 	}		
 }
