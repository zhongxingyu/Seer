 package org.mule.galaxy.impl.jcr.onm;
 
 import javax.jcr.Node;
 import javax.jcr.Session;
 
 import org.mule.galaxy.impl.jcr.JcrUtil;
 
 public class EnumPersister implements FieldPersister {
 
     public Object build(Node node, FieldDescriptor fd, Session session) throws Exception {
         String value = JcrUtil.getProperty(fd.getName(), node);
         
         if (value == null) {
             return null;
         }
         return build(value, fd, session);
     }
 
     @SuppressWarnings("unchecked")
     public Object build(String id, FieldDescriptor fd, Session session) throws Exception {
        return Enum.valueOf((Class<? extends Enum>)fd.getType(), id);
     }
 
     public void persist(Object o, Node n, FieldDescriptor fd, Session session) throws Exception {
        JcrUtil.setProperty(fd.getName(), o.toString(), n);
     }
     
 }
