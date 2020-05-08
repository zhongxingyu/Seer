 package com.springapp.mvc.data;
 
 import com.springapp.mvc.HelperClasses.ByteObjectConversion;
 import com.springapp.mvc.ImageSettings;
 import com.springapp.mvc.model.User;
 import org.apache.log4j.Logger;
 import org.aspectj.lang.annotation.Aspect;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.cache.annotation.EnableCaching;
 import org.springframework.context.annotation.AdviceMode;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.PreparedStatementCallback;
 import org.springframework.jdbc.core.ResultSetExtractor;
 import org.springframework.stereotype.Repository;
 import redis.clients.jedis.Jedis;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository
 public class ImageRepository{
 
     static Logger log = Logger.getLogger(ImageRepository.class);
     private final JdbcTemplate jdbcTemplate;
     private ByteObjectConversion byteObjectConverter;
     private final Jedis jedis;
 
     @Autowired
     public ImageRepository(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
         jedis = new Jedis("localhost");
         new Jedis("localhost", 6379, 10000);
         jedis.connect();
         jedis.flushDB();
         jedis.flushAll();
         byteObjectConverter = new ByteObjectConversion();
     }
 
     public void setImage(final String username,String path) {
         final File file = new File(path);
         try {
             String key = "getImage:"+username;
             byte[] keyInBytes = byteObjectConverter.toBytes(key);
             try{
                 jedis.del(keyInBytes);
             }
             catch (redis.clients.jedis.exceptions.JedisConnectionException e){
                 log.info("redis.clients.jedis.exceptions.JedisConnectionException - no need to call jedis.del.");
             }
            catch (ArrayIndexOutOfBoundsException e){
                log.info("ArrayIndexOutOfBoundsException - no need to call jedis.del.");
            }
             final FileInputStream fis = new FileInputStream(file);
             String query = "insert into images values (?,?)";
             jdbcTemplate.execute(query, new PreparedStatementCallback<Boolean>() {
                 @Override
                 public Boolean doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                     preparedStatement.setString(1,username);
                     preparedStatement.setBinaryStream(2, fis, (int) file.length());
                     Boolean returnVal =  preparedStatement.execute();
                     preparedStatement.close();
                     return returnVal;
                 }
             });
             fis.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public byte[] getImage(String username) {
         String query = "select image from images where username='"+username+"'";
         log.info("Getting image of user "+username);
         try{
             String key = "getImage:"+username;
             byte[] keyInBytes = byteObjectConverter.toBytes(key);
             System.out.println("Trying for user:"+username);
             System.out.println(jedis.isConnected());
             try{System.out.println(jedis.exists(keyInBytes));}catch(Exception e){System.out.println(e);}
             if (!jedis.exists(keyInBytes)){
                 System.out.println("Cache for image is empty. Filling Cache.");
 
                 byte[] fileByteArray;
                 fileByteArray = jdbcTemplate.query(query, new ResultSetExtractor<byte[]>() {
                     @Override
                     public byte[] extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                         while(resultSet.next()) {
                             byte[] imgBytes = resultSet.getBytes(1);
                             return imgBytes;
                         }
                         return null;
                     }
                 });
 
                 jedis.set(keyInBytes, fileByteArray);
                 System.out.println("Cache for image filled.");
                 log.info("Cached image is "+fileByteArray);
                 return fileByteArray;
             }
             System.out.println("Replying from the cache for image..");
             byte[] outputBytes = jedis.get(keyInBytes);
             return outputBytes;
         }
         catch(redis.clients.jedis.exceptions.JedisConnectionException e){
             log.info("redis.clients.jedis.exceptions.JedisConnectionException - using direct DB call to get image."+username);
             return jdbcTemplate.query(query, new ResultSetExtractor<byte[]>() {
                 @Override
                 public byte[] extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                     while(resultSet.next()) {
                         byte[] imgBytes = resultSet.getBytes(1);
                         return imgBytes;
                     }
                     return null;
                 }
             });
         }
         catch(ArrayIndexOutOfBoundsException e){
             log.info("ArrayIndexOutOfBoundsException - using direct DB call to get image."+username);
             return jdbcTemplate.query(query, new ResultSetExtractor<byte[]>() {
                 @Override
                 public byte[] extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                     while(resultSet.next()) {
                         byte[] imgBytes = resultSet.getBytes(1);
                         return imgBytes;
                     }
                     return null;
                 }
             });
         }
     }
 
     public List<byte[]> getTweetImage(long id) {
         String query = "select image from tweetimages where id='"+id+"'";
         log.info("Getting image of user "+id);
 
         try{
             String key = "getTweetImage:"+id;
             byte[] keyInBytes = byteObjectConverter.toBytes(key);
             if (!jedis.exists(keyInBytes)){
                 System.out.println("Cache for tweetImage is empty. Filling Cache.");
 
                 List<byte[]> listByteArray;
                 listByteArray = jdbcTemplate.query(query, new ResultSetExtractor<List<byte[]>>() {
                     @Override
                     public List<byte[]> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                         List<byte[]> images = new ArrayList<byte[]>();
                         while(resultSet.next()) {
                             byte[] imgBytes = resultSet.getBytes(1);
                             images.add(imgBytes);
                         }
                         return images;
                     }
                 });
                 byte[] inputBytes = byteObjectConverter.toBytes(listByteArray);
                 jedis.set(keyInBytes, inputBytes);
                 System.out.println("Cache for TweetImage filled.");
                 return listByteArray;
             }
 
             System.out.println("Replying from the cache for tweetImage..");
             byte[] outputBytes = jedis.get(keyInBytes);
             Object outputObjectByteList = byteObjectConverter.fromBytes(outputBytes);
             List<byte[]> outputByteList = (List<byte[]>) outputObjectByteList;
             return outputByteList;
         }
         catch(redis.clients.jedis.exceptions.JedisConnectionException e){
             log.info("redis.clients.jedis.exceptions.JedisConnectionException - using direct DB call to get tweetImage.");
             return jdbcTemplate.query(query, new ResultSetExtractor<List<byte[]>>() {
                 @Override
                 public List<byte[]> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                     List<byte[]> images = new ArrayList<byte[]>();
                     while(resultSet.next()) {
                         byte[] imgBytes = resultSet.getBytes(1);
                         images.add(imgBytes);
                     }
                     return images;
                 }
             });
         }
         catch (ArrayIndexOutOfBoundsException e){
             log.info("ArrayIndexOutOfBoundsException - using direct DB call to get tweetImage.");
             return jdbcTemplate.query(query, new ResultSetExtractor<List<byte[]>>() {
                 @Override
                 public List<byte[]> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                     List<byte[]> images = new ArrayList<byte[]>();
                     while(resultSet.next()) {
                         byte[] imgBytes = resultSet.getBytes(1);
                         images.add(imgBytes);
                     }
                     return images;
                 }
             });
         }
     }
 
     public void updateImage(final String username, String path) {
         final File file = new File(path);
         log.info("Updating image of user "+username);
         try {
             String key = "getImage:"+username;
             byte[] keyInBytes = byteObjectConverter.toBytes(key);
             try{
                 jedis.del(keyInBytes);
             }
             catch (redis.clients.jedis.exceptions.JedisConnectionException e){
                 log.info("redis.clients.jedis.exceptions.JedisConnectionException - no need to call jedis.del.");
             }
            catch (ArrayIndexOutOfBoundsException e){
                log.info("ArrayIndexOutOfBoundsException - no need to call jedis.del.");
            }
             final FileInputStream fis = new FileInputStream(file);
             String query = "update images set image=? where username=?";
             jdbcTemplate.execute(query, new PreparedStatementCallback<Boolean>() {
                 @Override
                 public Boolean doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                     preparedStatement.setString(2,username);
                     preparedStatement.setBinaryStream(1, fis, (int) file.length());
                     Boolean returnVal =  preparedStatement.execute();
                     preparedStatement.close();
                     return returnVal;
                 }
             });
             fis.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public void setTweetImage(final long id, String path) {
         final File file = new File(path);
         try {
             log.info("Uploading image file");
             final FileInputStream fis = new FileInputStream(file);
             log.info(file.length());
             String query = "insert into tweetimages values (?,?)";
             jdbcTemplate.execute(query, new PreparedStatementCallback<Boolean>() {
                 @Override
                 public Boolean doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                     preparedStatement.setLong(1,id);
                     preparedStatement.setBinaryStream(2, fis, (int) file.length());
                     Boolean returnVal =  preparedStatement.execute();
                     preparedStatement.close();
                     return returnVal;
                 }
             });
             fis.close();
             file.delete();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public boolean checkIfProfileImageExists(String userName) {
         return this.getImage(userName) == null;
     }
 
     public void setUserImage(String userName, String filePath) {
         if(checkIfProfileImageExists(userName)) setImage(userName,filePath);
         else updateImage(userName,filePath);
     }
 }
