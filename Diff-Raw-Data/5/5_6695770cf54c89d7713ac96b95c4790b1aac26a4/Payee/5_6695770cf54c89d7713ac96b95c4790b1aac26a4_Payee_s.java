 package models;
 
 import java.util.*;
 import javax.persistence.*;
 
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 
 
 import com.avaje.ebean.*;
 
 
 /**
  * Payee entity managed by Ebean
  */
 @Entity 
 @Table(name="payees")
 public class Payee extends Model {
 
     @Id
     public Long id;
     
     @Constraints.Required
     public String name;
     
     /**
      * Generic query helper for entity Payee with id Long
      */
     public static Model.Finder<Long,Payee> find = new Model.Finder<Long,Payee>(Long.class, Payee.class);
 
     public static Map<String,String> options() {
         LinkedHashMap<String,String> options = new LinkedHashMap<String,String>();
         for(Payee c: Payee.find.orderBy("name").findList()) {
             options.put(c.id.toString(), c.name);
         }
         return options;
     }
     
 
     /**
      * Return a page of payee
      *
      * @param page Page to display
      * @param pageSize Number of payees per page
      * @param sortBy Payee property used for sorting
      * @param order Sort order (either or asc or desc)
      * @param filter Filter applied on the name column
      */
     public static Page<Payee> page(int page, int pageSize, String sortBy, String order, String filter) {
         return 
             find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 
 }
 
