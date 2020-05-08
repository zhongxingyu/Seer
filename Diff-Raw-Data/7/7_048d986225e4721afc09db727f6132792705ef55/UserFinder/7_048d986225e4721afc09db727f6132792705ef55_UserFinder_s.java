 package org.northstar.bricks.dao;
 
 import com.google.inject.name.Named;
 import com.google.inject.persist.finder.Finder;
 import com.google.inject.persist.finder.FirstResult;
 import com.google.inject.persist.finder.MaxResults;
 import org.northstar.bricks.domain.User;
 
 import java.util.ArrayList;
 
import java.util.Collection;

 public interface UserFinder {
 
 //	@Finder(query="select u from User u", returnAs=ArrayList.class)
     @Finder(namedQuery = "getAllUsers", returnAs = ArrayList.class)
 	ArrayList<User> listAll();
 	
 //	@Finder(query="select u from User u where u.name=:name")
 	@Finder(namedQuery = "getUserByName")
     User getUser(@Named("name") String name);
 
 //    @Finder(query = "select u from User u", returnAs = ArrayList.class)
     @Finder(namedQuery = "getPagedUsers", returnAs = ArrayList.class)
     ArrayList<User> listUsers(@FirstResult int first, @MaxResults int max);
 
    @Finder(namedQuery = "getUserByNamePwd")
     ArrayList<User> authenticated(@Named("name") String name, @Named("password") String password);

 }
