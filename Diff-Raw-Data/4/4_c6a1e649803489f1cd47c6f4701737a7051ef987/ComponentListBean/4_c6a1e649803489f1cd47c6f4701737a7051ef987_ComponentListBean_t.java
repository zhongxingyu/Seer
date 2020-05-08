 package beans;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class ComponentListBean {
 	
 	private HashMap<Integer, ComponentBean> componentList; 
 	private String url = null;
 	private Connection conn = null;
 	private Statement stmt = null;
 	private ResultSet rs = null;
 	
 	
 	public ComponentListBean() {
 	}
 	
 	public ComponentListBean(String _url) throws Exception {
 		this.componentList = new HashMap<Integer, ComponentBean>();
 		this.url = _url;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
 			String sql = "SELECT * FROM AUTHORS";
 			this.rs = stmt.executeQuery(sql);
 			
 			while(rs.next()) {
 				ComponentBean cb = new ComponentBean();
 				cb.setId(rs.getInt("AUTHOR_ID"));
 				cb.setManufacturer(rs.getString("NAME"));
 				cb.setType(rs.getString("SURNAME"));
 				cb.setQuantity(rs.getInt("QTY"));
 				cb.setPrice(rs.getInt("C_PRICE"));
 				this.componentList.put(cb.getId(), cb);
 			}
 			
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		finally {
 			try {
 				rs.close();
 			} catch (Exception e) {}
 			try {
 				stmt.close();
 			} catch (Exception e) {}
 			try {
 				conn.close();
 			} catch (Exception e) {}
 		}
 	}
 	
 	
 	public Collection<ComponentBean> getComponentList() {
 		return this.componentList.values();
 	}
 	
 	
 	public ComponentBean getById(int id) {
 		return this.componentList.get(id);
 	}
 	
 	
 	
 	public String getXml() {
 		StringBuffer xmlOut = new StringBuffer();
 		Iterator<ComponentBean> iter = getComponentList().iterator();
 		xmlOut.append("<componentlist>");
 		while(iter.hasNext()) {
 			xmlOut.append(iter.next().getXml());
 		}
 		xmlOut.append("</componentlist>");
 		return xmlOut.toString();
 	}
 	
 	
 	
 	public void updateComponent(int id, String manufacturer, String type,
 			int price, int quantity) throws Exception {
 		ComponentBean cb = this.componentList.get(id);
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
 			
 			String sql = "UPDATE AUTHORS SET NAME = '" + manufacturer + "', ";
 			sql += "SURNAME = '" + type + "', ";
 			sql += "QTY = " + quantity + ", ";
 			sql += "C_PRICE = " + price + " ";
 			sql += "WHERE AUTHOR_ID = " + id;
 			this.stmt.executeUpdate(sql);
 			
			sql = "UPDATE COMPOSITION SET COM_ID = COM_ID WHERE ";
			sql += "COM_ID = " + id;
			this.stmt.executeUpdate(sql);
			
 			cb.setManufacturer(manufacturer);
 			cb.setType(type);
 			cb.setPrice(price);
 			cb.setQuantity(quantity);
 			
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		
 	}
 	
 	
 }
