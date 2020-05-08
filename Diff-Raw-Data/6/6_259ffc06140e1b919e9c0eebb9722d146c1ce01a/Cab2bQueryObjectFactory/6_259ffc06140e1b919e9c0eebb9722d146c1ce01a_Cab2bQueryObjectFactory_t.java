 package edu.wustl.cab2b.common.queryengine;
 
import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
import java.util.HashSet;
 
 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 
 import java.util.Date;
 
 import edu.wustl.common.querysuite.factory.QueryObjectFactory;
 
 /**
  * @author chetan_patil
  *
  */
 public class Cab2bQueryObjectFactory extends QueryObjectFactory {
     /**
      * This method creates and return a Cab2bQuery object along with current system date set as created date.
      * @return Cab2bQuery
      */
     public static ICab2bQuery createCab2bQuery() {
         ICab2bQuery query = new Cab2bQuery();
         query.setCreatedDate(new Date());              
        query.setServiceGroups(new HashSet<ServiceGroup>());
         
         return query;
     }
 }
