 package de.graind.server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import de.graind.client.model.PicasaImageBase;
 import de.graind.client.service.GraindService;
 
 public class GraindServiceImpl extends RemoteServiceServlet implements GraindService {
 
   private static final long serialVersionUID = 1L;
   private Connection connection;
 
   public GraindServiceImpl() {
 
     try {
       Class.forName("org.postgresql.Driver");
 
       String url = "jdbc:postgresql:graind";
       Properties props = new Properties();
       props.setProperty("user", "graind");
       props.setProperty("password", "graind");
       // props.setProperty("ssl", "true");
       this.connection = DriverManager.getConnection(url, props);
 
     } catch (ClassNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   @Override
   public void saveMonthlyPictureSelection(String username, PicasaImageBase[] images) {
     try {
       String insertString = "INSERT INTO images VALUES (?, ?, ?, ?, ?);";
 
       // delete old values first
       deleteMonthlyPictureSelection(username);
 
       PreparedStatement stmt = this.connection.prepareStatement(insertString);
       for (short i = 0; i < images.length; i++) {
         PicasaImageBase img = images[i];
 
         stmt.setString(1, username);
         stmt.setShort(2, i);
         stmt.setString(3, img.getUrl());
         stmt.setInt(4, img.getHeight().intValue());
         stmt.setInt(5, img.getWidth().intValue());
 
         stmt.execute();
       }
 
     } catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   @Override
   public void deleteMonthlyPictureSelection(String username) {
     try {
       String deleteString = "DELETE FROM images WHERE user_id = ?;";
 
       PreparedStatement stmt = this.connection.prepareStatement(deleteString);
       stmt.setString(1, username);
       stmt.execute();
 
     } catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   @Override
   public PicasaImageBase getImageForMonth(String username, int month) {
     PicasaImageBase ret = null;
    month--;
 
     try {
       String queryString = "SELECT image_url, image_height, image_width FROM images WHERE user_id = ? AND month = ?;";
 
       PreparedStatement stmt = this.connection.prepareStatement(queryString);
       stmt.setString(1, username);
       stmt.setShort(2, (short) month);
       ResultSet rs = stmt.executeQuery();
 
       // should there really be more then one result, just take the first one!
       // But this should never happen.
       if (rs.next()) {
         ret = new PicasaImageBase();
         ret.setUrl(rs.getString(1));
         ret.setHeight((long) rs.getInt(2));
         ret.setWidth((long) rs.getInt(3));
       }
 
     } catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return ret;
   }
 
   @Override
   public PicasaImageBase[] getAllImages(String username) {
     PicasaImageBase[] ret = new PicasaImageBase[12];
 
     try {
       String queryString = "SELECT month, image_url, image_height, image_width FROM images WHERE user_id = ?;";
 
       PreparedStatement stmt = this.connection.prepareStatement(queryString);
       stmt.setString(1, username);
       ResultSet rs = stmt.executeQuery();
 
       // TODO: check if we really got 12 results.
       while (rs.next()) {
         PicasaImageBase tmp = new PicasaImageBase();
         tmp.setUrl(rs.getString(2));
         tmp.setHeight((long) rs.getInt(3));
         tmp.setWidth((long) rs.getInt(4));
 
         ret[rs.getShort(1)] = tmp;
       }
 
     } catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     return ret;
   }
 }
