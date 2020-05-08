 package dao;
 
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import beans.Comment;
 import interfaces.Model;
 
 public class Comments extends DAO {
 
 
 	public Comments() {
 		this.tableName = "comments";
 		this.idField = "idcomments";
 	}
 
 	@Override
 	public Boolean create(Model item) {
 		Comment c = (Comment) item;
 		int rs = 0;
 		String query = "";
 		query += "INSERT INTO " + this.tableName+ "( txt, rating, idusers, idreceipes) \n";
 		query += "VALUES ('" + c.getTxt() + "', '" + c.getRating() + "', '"+ c.getIdUser() + "', '" + c.getIdReceipe() + "')";
 		System.out.println(query);
 		try {
 			rs = DB.executeUpdate(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Rs : " + rs);
 		return (rs == 1);
 	}
 
 	@Override
 	public Boolean update(Model item) {
 		Comment c = (Comment) item;
 		int rs = 0;
 		System.out.println(c);
 		String query = "";
 		query += "UPDATE " + this.tableName + "\n";
 		query += "SET txt       = '" + c.getTxt()       + "',\n";
 		query += "rating        = '" + c.getRating()    + "',\n";
 		query += "idusers       = '" + c.getIdUser()    + "',\n";
		query += "idreceipes    = '" + c.getIdReceipe() + "',\n";
 		query += "WHERE " + this.idField + " = '" + c.getId() + "'";
 		System.out.println(query);
 		try {
 			rs = DB.executeUpdate(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Rs : " + rs);
 		return (rs == 1);
 	}
 
 	public Comment getItem(int CommentId) {
 		return this.fromRow(this.read(CommentId));
 	}
 
 	public ArrayList<Model> getItems() {
 		ArrayList<Model> items = new ArrayList<Model>();
 		ArrayList<HashMap<String, String>> ds = this.read();
 		int  nbRow = ds.size();
 		for(int i = 0; i < nbRow; i++) {
 			items.add(this.fromRow(ds.get(i)));
 		}
 		
 		return items;
 	}
 
 	public Boolean delete(Model m) {
 		Comment c = (Comment) m;
 		return this.delete(c.getId());
 	}
 	
 	public ArrayList<Comment> getCommentsByReceipe(Integer rId) {
 		java.sql.ResultSet rs;
 		ResultSetMetaData resultMeta;
 		HashMap<String, String> row = new HashMap<String, String>();
 		ArrayList<Comment> result = new ArrayList<Comment>();
 		int nbCol = 0;
 		String query = "SELECT * FROM " + this.tableName + " WHERE idreceipes = '" + rId + "' ";
 		System.out.println(query);
 		try { 
 			rs = DB.executeQuery(query);
 			resultMeta= rs.getMetaData();
 			nbCol = resultMeta.getColumnCount();
 			
 			while (rs.next()) {
 				for (int i = 1; i <= nbCol; i++) {
 					String val = "";
 					if(rs.getObject(i) != null)
 						val = rs.getObject(i).toString();
 					row.put(resultMeta.getColumnName(i), val);
 				}
 				result.add(this.fromRow(row));
 			}
 		} catch (Exception e) {
 			System.out.println("Oooopps");
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public Comment fromRow(HashMap<String, String> row) {
 		Comment c;
 		try {
 			Integer id = Integer.parseInt(row.get(this.idField));
 			Integer rating = Integer.parseInt(row.get("rating"));
 			Integer idU = Integer.parseInt(row.get("idusers"));
 			Integer idR = Integer.parseInt(row.get("idreceipes"));
 			c = new Comment(id, row.get("txt"), rating, idU, idR);
 		} catch (Exception E) {
 			c = null;
 		}
 		
 		return c;
 	}
 	
 }
