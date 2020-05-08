package domain;
 
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
 import org.springframework.roo.addon.serializable.RooSerializable;
 import org.springframework.roo.addon.tostring.RooToString;
 
 @RooJavaBean
 @RooToString
 @RooJpaActiveRecord
 @RooSerializable
 public class MoneyValue {
 
    private long amount;
 }
