 package ro.btanase.chordlearning.services;
 
 import java.sql.Connection;
 
 public interface JdbcService {
 
   public abstract Connection getCon();
 
}
