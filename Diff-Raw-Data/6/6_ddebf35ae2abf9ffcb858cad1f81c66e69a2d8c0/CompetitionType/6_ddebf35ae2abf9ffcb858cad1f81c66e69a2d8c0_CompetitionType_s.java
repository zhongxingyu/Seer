 package models.competition;
 
import com.avaje.ebean.Ebean;
 import com.avaje.ebean.annotation.EnumValue;
 import models.EMessages;
import models.dbentities.ClassGroup;
import models.user.AuthenticationManager;
 
 import java.util.LinkedHashMap;
import java.util.List;
 
 /**
  * An enumeration of contests types.
  *
  * @author Kevin Stobbelaar
  */
 public enum CompetitionType {
 
     @EnumValue("UNRESTRICTED")
     UNRESTRICTED,
 
     @EnumValue("RESTRICTED")
     RESTRICTED,
 
     @EnumValue("ANONYMOUS")
     ANONYMOUS;
 
     @Override
     public String toString(){
         return EMessages.get("competition.type." + this.name().toLowerCase());
     }
 
     /**
      * Returns the options for a select in template.
      * @return options map
      */
     public static LinkedHashMap<String, String> options() {
         LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
         options.put(RESTRICTED.name(), RESTRICTED.toString());
         options.put(UNRESTRICTED.name(), UNRESTRICTED.toString());
         options.put(ANONYMOUS.name(), ANONYMOUS.toString());
         return options;
     }
 
 }
