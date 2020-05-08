 package org.lightmare.jpa.jta;
 
 import java.lang.reflect.Method;
 
 import javax.ejb.TransactionAttribute;
 
 /**
  * Class to manage {@link javax.transaction.UserTransaction} for
  * {@link javax.ejb.Stateless} bean {@link java.lang.reflect.Proxy} calls
  * 
  * @author levan
  * 
  */
 public class BeanTransactions {
 
     public static boolean isReqNew(TransactionAttribute attribute, Method method) {
 
 	boolean isReqNew;
 	if (attribute == null) {
 	    isReqNew = false;
	} else {
 	    isReqNew = true;
 	}

	return isReqNew;
     }
 }
