 package org.nohope.node;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.nohope.typetools.node.*;
 
 import javax.annotation.Nonnull;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 11/16/12 3:36 AM
  */
 public class MongoInterceptor implements Interceptor<Node, DBObject> {
     private static final long serialVersionUID = 1L;
 
     @Override
     public DBObject intercept(@Nonnull final Node node) {
         final DBObject obj = new BasicDBObject();
         if (node instanceof OrNode) {
             obj.put("$or", ((OrNode) node).getChildValues(this));
         } else if (node instanceof AndNode) {
             obj.put("$and", ((AndNode) node).getChildValues(this));
         } else if (node instanceof NotNode) {
            obj.put("$not", ((NotNode) node).getValue(this));
         } else if (node instanceof EmptyNode) {
             return obj;
         } else {
             return null;
         }
         return obj;
     }
 }
