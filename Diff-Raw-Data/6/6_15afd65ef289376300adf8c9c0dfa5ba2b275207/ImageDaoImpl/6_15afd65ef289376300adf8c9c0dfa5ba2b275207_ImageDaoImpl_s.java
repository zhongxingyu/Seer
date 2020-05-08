 package edu.uib.info323.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.uib.info323.model.Image;
 
 public class ImageDaoImpl {
 	private static final Logger LOGGER = LoggerFactory.getLogger(ImageDaoImpl.class);
 	private DataSource dataSource;
 	 
 	public void setDataSource(DataSource dataSource) {
 		this.dataSource = dataSource;
 	}
 	
 	public void insert(Image image) {
		String sql = "INSERT INTO IMAGE (image_uri,page_uri) VALUES (?,?);";
 		Connection con = null;
 		try {
 			con = dataSource.getConnection();
 			PreparedStatement ps = con.prepareStatement(sql);
 			ps.setString(1, image.getImageUri());
 			ps.setString(2, image.getPageUri());
 			ps.executeUpdate();
 			ps.close();
		}catch(Exception e){	
 		}finally {
 			if (con != null) {
 				try {
 					con.close();
 				} catch (SQLException e) {}
 			}
 		}
 
 	}
 	
 	public Image getImageByImageUri(String imageUri) {
 		String sql = "SELECT * FROM IMAGE WHERE image_uri = ?";
 		 
 		Connection con = null;
  
 		Image image = null;
 		try {
 			con = dataSource.getConnection();
 			PreparedStatement ps = con.prepareStatement(sql);
 			ps.setString(1, imageUri);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				image = new Image();
 				image.setImageUri(rs.getString("image_uri"));
 				image.setPageUri(rs.getString("page_uri"));
 			}
 			rs.close();
 			ps.close();
 			return image;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (con != null) {
 				try {
 				con.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	public List<Image> getAllImages(){
 		List<Image> images = new ArrayList<Image>();
 		String sql = "SELECT * FROM IMAGE";
 		Connection con = null;
 		try {
 			con = dataSource.getConnection();
 			PreparedStatement ps = con.prepareStatement(sql);
 			ResultSet rs = ps.executeQuery();
 			while(rs.next()) {
 				Image image = new Image();
 				image.setImageUri(rs.getString("image_uri"));
 				image.setPageUri(rs.getString("page_uri"));
 				images.add(image);
 			}
 			
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (con != null) {
 				try {
 				con.close();
 				} catch (SQLException e) {}
 			}
 		}
 		
 		return images;
 	}
 }
