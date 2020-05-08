 package models.norpneu;
 
 import com.avaje.ebean.validation.Length;
 import com.avaje.ebean.validation.NotNull;
 import models.User;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import javax.persistence.*;
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
 /**
  * Cleverly mastered by: CMM
  * Date: 8/29/12
  * Time: 7:50 PM
  */
 @Entity
 public class OrderTire extends Model {
     @Id
     public Long id;
     @Constraints.Required
     @NotNull
     public Long timestamp;
     @NotNull
     public Long creationTimestamp;
     @OneToOne(fetch = FetchType.EAGER)
     public User user;
     @Constraints.Required
     @NotNull
     public ORDERSTATUS status = ORDERSTATUS.INITIATED;
     @Constraints.Required
     @NotNull
     public Long totalLines = Long.valueOf(0l);
     @Constraints.Required
     @NotNull
     public Long totalQuantity = Long.valueOf(0l);
     @Constraints.Required
     @NotNull
     public Long totalAmount = Long.valueOf(0l);
     @Constraints.Required
     @NotNull
     public Long totalVat = Long.valueOf(0l);
     @Constraints.Required
     @NotNull
     public Long totalDiscount = Long.valueOf(0l);
     @Constraints.Required
     @NotNull
     public Long totalEcoValue = Long.valueOf(0l);
     @Transient
     public Long totalGlobal = totalAmount + totalVat + totalEcoValue - totalDiscount;
 
     /**
      * FINDERS
      */
     public static Model.Finder<Long, OrderTire> finder = new Model.Finder<Long, OrderTire>(Long.class, OrderTire.class);
     public static Model.Finder<User, OrderTire> finderUser = new Model.Finder<User, OrderTire>(User.class, OrderTire.class);
 
     public OrderTire() {
     }
 
     public static List<OrderTire> findByUser(String username) {
         return finder.where().eq("user", User.findByEmail(username)).orderBy("status").orderBy("timestamp").findList();
     }
 
     public static List<OrderTire> findByUserAndStatuses(String username,String[] statuses) {
        return finder.where().eq("user", User.findByEmail(username)).in("status", Arrays.asList(statuses)).orderBy("status").orderBy("timestamp").findList();
     }
 
     public Long getTotalGlobal() {
         return this.totalAmount + this.totalVat + this.totalEcoValue - this.totalDiscount;
     }
 
 
 }
