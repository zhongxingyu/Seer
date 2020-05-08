 package org.you.core.dao;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.you.core.config.ConfigUtil;
 
 public class DataAccessManagerTest {
 
 	@Before
 	public void init(){
 		ConfigUtil.init();
 	}
 	
 	@Test
 	public void testUpdate() throws SQLException{
 		String sql = " update test set name= ? where id= ? ";
 		OpUpdate op = new OpUpdate(sql, "self_test"){
 			@Override
 			public void setParam(PreparedStatement ps) throws SQLException {
 				ps.setString(1, "ggggggff");
 				ps.setLong(2, 2);				
 			}			
 		};
 		
 		DataAccessManager.getInstance().update(op);
 		
 	}
 	
 	@Test
 	public void testModel() throws SQLException{
		String sql = " select * from test ";
 		
		OpList op = new OpList(sql, "self_test"){
 
 			@Override
 			public Object parse(ResultSet rs) throws SQLException {
 				return Demo.parse(rs);
 			}
 			
 		};
 		
 		@SuppressWarnings("unchecked")
 		List<Demo> demos = DataAccessManager.getInstance().queryList(op);
 		System.out.println("demos:"+demos);
 	}
 	
 	@Test
 	public void testUniq(){
 		String sqlStr = "select count(*)  from test where id < ? ";
 		OpUniq op = new OpUniq(sqlStr, "self_test") {
 			public void setParam(PreparedStatement ps) throws SQLException {
 				ps.setInt(1,3);
 			}
 
 			public Object parse(ResultSet rs) throws SQLException {
 				return new Integer(rs.getInt(1));
 			}
 		};
 		try {
 			int count = ((Integer) DataAccessManager.getInstance().queryUnique(op)).intValue();
 			System.out.print(count);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}	
 }
 class Demo{
 	private long uid;
 	private String name;
 	
 	@Override
 	public String toString() {
 		return "Demo [uid=" + uid + ", name=" + name + "]";
 	}
 
 	public static Demo parse(ResultSet rs){
 		Demo demo = new Demo();
 		try {
 			demo.setName(rs.getString("name"));
 			demo.setUid(rs.getLong("id"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return demo;
 	}
 	
 	public long getUid() {
 		return uid;
 	}
 	public void setUid(long uid) {
 		this.uid = uid;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 }
