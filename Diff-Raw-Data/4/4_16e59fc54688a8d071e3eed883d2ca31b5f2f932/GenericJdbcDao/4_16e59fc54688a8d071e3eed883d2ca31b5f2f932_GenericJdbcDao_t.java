 package org.app.repo.jdbc.dao;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.sql.DataSource;
 
 import org.app.framework.paging.PagingParam;
 import org.app.framework.paging.PagingResult;
 import org.app.repo.service.SimpleSqlBuilder;
 import org.app.repo.service.SimpleSqlBuilder.UpdateStatement;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.ColumnMapRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.SqlParameterValue;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class GenericJdbcDao {
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 	@Autowired
 	SimpleSqlBuilder sqlBuilder;
 
 	public PagingResult<Map<String, Object>> findPaing(DataSource dataSource, String tableName, PagingParam pagingParam) {
 		String countSql = sqlBuilder.count(dataSource, tableName, pagingParam);
 		String findAllSql = sqlBuilder.findAll(dataSource, tableName, pagingParam);
 
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		RowMapper<Map<String, Object>> rowMapper = new ColumnMapRowMapper();
 		List<Map<String, Object>> resultList = jdbcTemplate.query(findAllSql, rowMapper);
		int total = jdbcTemplate.queryForInt(countSql);
		PagingResult<Map<String, Object>> result = new PagingResult<Map<String, Object>>(resultList,  total);
 		return result;
 	}
 
 	public void updateTable(DataSource dataSource, String tableName, String pKeyCol, HashMap<String, SqlParameterValue> sqlParams) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		UpdateStatement statement = sqlBuilder.update(dataSource, tableName, pKeyCol, sqlParams);
 		int update = jdbcTemplate.update(statement.sql, statement.args);
 		if (update != 1) {
 			logger.warn("update statement expect one row to be affected, but the reuslt is {}.\n"
 					+ "table:{}, content: {}", new Object[] { update, tableName, sqlParams });
 		}
 	}
 	public Object insertRecord(DataSource dataSource, String tableName, String pKeyCol, HashMap<String, SqlParameterValue> sqlParams) {
 		if(sqlParams == null){
 			return null;
 		}
 	    HashMap<String, Object> args = new HashMap<String, Object>();
 	    for (Entry<String, SqlParameterValue> entry : sqlParams.entrySet()) {
 			args.put(entry.getKey(), entry.getValue().getValue());
 		}
 	    
 	    SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(dataSource);
 	    jdbcInsert.withTableName(tableName).usingGeneratedKeyColumns(pKeyCol);
 	    if(sqlParams!= null && sqlParams.containsKey(pKeyCol)){
 	    	//primary key specified.
 	    	int rows = jdbcInsert.execute(args);
 	    	if(rows == 1){
 	    		return sqlParams.get(pKeyCol).getValue();
 	    	}else{
 	    		return null;
 	    	}
 	    }else{
 	    	Number key = jdbcInsert.executeAndReturnKey(args);
 	    	return key;
 	    }
 	}
 
     public void deleteRecord(DataSource dataSource, String tableName, String pKeyCol, Integer id) {
         JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
         UpdateStatement statement = sqlBuilder.delete(dataSource, tableName, pKeyCol, id);
         int update = jdbcTemplate.update(statement.sql, statement.args);
         if (update != 1) {
             logger.warn("update statement expect one row to be affected, but the reuslt is {}.\n"
                     + "table:{}, content: {}", new Object[] { update, tableName, id });
         }
     }
 
 }
