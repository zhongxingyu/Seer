 package org.mule.galaxy.impl.jcr;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.Session;
 import javax.jcr.Value;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 
 import org.mule.galaxy.Identifiable;
 import org.mule.galaxy.impl.jcr.onm.ClassPersister;
 import org.mule.galaxy.impl.jcr.onm.FieldDescriptor;
 import org.mule.galaxy.impl.jcr.onm.FieldPersister;
 import org.mule.galaxy.impl.jcr.onm.PersisterManager;
 import org.mule.galaxy.mapping.OneToMany;
 
 public class CollectionPersister implements FieldPersister {
 
     private Class implementation;
     private PersisterManager persisterManager;
     
     public CollectionPersister(Class implementation, PersisterManager persisterManager) {
         super();
         this.implementation = implementation;
         this.persisterManager = persisterManager;
     }
 
     @SuppressWarnings("unchecked")
     public Object build(Node n, FieldDescriptor fd, Session session) throws Exception {
         OneToMany otm = fd.getOneToMany();
         boolean hasAnnotation = otm != null;
         
         if (!hasAnnotation || otm.treatAsField()) {
             return JcrUtil.getProperty(fd.getName(), n);
         }
 
         String parentField = otm.mappedBy();
         if (Set.class.isAssignableFrom(implementation)) {
             return new LazySet(this, n, fd, session, parentField);
         } else {
             return new LazyList(this, n, fd, session, parentField);
         }
     }
 
     public Object build(String id, FieldDescriptor fd, Session session) throws Exception {
         throw new UnsupportedOperationException();
     }
 
     public void persist(Object o, Node n, FieldDescriptor fd, Session session) throws Exception {
         OneToMany otm = fd.getOneToMany();
         boolean hasAnnotation = otm != null;
         
         if (!hasAnnotation || otm.treatAsField()) {
             JcrUtil.setProperty(fd.getName(), o, n);
         } else if (fd.getOneToMany().mappedBy().equals("")) {
             List<String> values = new ArrayList<String>();
             
             Collection c = (Collection) o;
             
             if (c != null) {
                 for (Object cObj : c) {
                     values.add(((Identifiable) cObj).getId());
                 }
             }
             
             n.setProperty(fd.getName(), (String[]) values.toArray(new String[values.size()]));
         }
     }
     
     protected void initializeCollection(Collection c, Node n, FieldDescriptor fd, Session session, String parentField) {
         if (!parentField.equals("")) {
             try {
                 String parentId = n.getUUID();
                 String rootNode = fd.getClassPersister().getPath();
                 
                 QueryManager qm = session.getWorkspace().getQueryManager();
                 Query q = qm.createQuery("/jcr:root/" + rootNode 
                                          + "/*[@" + parentField + "='" + parentId + "']", Query.XPATH);
                 
                 QueryResult result = q.execute();
                 
                 ClassPersister cp = persisterManager.getClassPersister(fd.getClassPersister().getType().getName());
                 for (NodeIterator nodes = result.getNodes(); nodes.hasNext();) {
                     Node next = nodes.nextNode();
                     Identifiable obj = (Identifiable) cp.build(next, session);
                     
                     // Hack to get the ID set because we normally do it in AbstractDao
                     obj.setId(next.getUUID());
                     
                     c.add(obj);
                 }
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         } else {
            try {
                FieldPersister fp = persisterManager.getPersister(fd.getOneToMany().componentType());
                Property property = n.getProperty(fd.getName());
                
                for (Value v : property.getValues()) {
                    c.add(fp.build(v.getString(), fd, session));
                }
            } catch (PathNotFoundException e) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
         }
     }
     
     public static class LazySet extends HashSet {
 
        private transient  Node n;
         private transient FieldDescriptor fd;
         private transient Session session;
         private transient String parentField;
         private transient CollectionPersister persister;
        private transient boolean initialized;
 
         public LazySet(CollectionPersister persister, 
                        Node n, 
                        FieldDescriptor fd, 
                        Session session,
                        String parentField) {
             this.persister = persister;
             this.n = n;
             this.fd = fd;
             this.session = session;
             this.parentField = parentField;
         }
 
         public void initialize() {
             if (initialized) return;
 
             initialized = true;
             persister.initializeCollection(this, n, fd, session, parentField);
         }
         
         @Override
         public boolean add(Object arg0) {
             initialize();
             return super.add(arg0);
         }
 
         @Override
         public void clear() {
             initialize();
             super.clear();
         }
 
         @Override
         public Object clone() {
             initialize();
             return super.clone();
         }
 
         @Override
         public boolean contains(Object arg0) {
             initialize();
             return super.contains(arg0);
         }
 
         @Override
         public boolean isEmpty() {
             initialize();
             return super.isEmpty();
         }
 
         @Override
         public Iterator iterator() {
             initialize();
             return super.iterator();
         }
 
         @Override
         public boolean remove(Object arg0) {
             initialize();
             return super.remove(arg0);
         }
 
         @Override
         public int size() {
             initialize();
             return super.size();
         }
 
         @Override
         public boolean equals(Object arg0) {
             initialize();
             return super.equals(arg0);
         }
 
         @Override
         public int hashCode() {
             initialize();
             return super.hashCode();
         }
 
         @Override
         public boolean removeAll(Collection arg0) {
             initialize();
             return super.removeAll(arg0);
         }
 
         @Override
         public boolean addAll(Collection c) {
             initialize();
             return super.addAll(c);
         }
 
         @Override
         public boolean containsAll(Collection c) {
             initialize();
             return super.containsAll(c);
         }
 
         @Override
         public boolean retainAll(Collection c) {
             initialize();
             return super.retainAll(c);
         }
 
         @Override
         public Object[] toArray() {
             initialize();
             return super.toArray();
         }
 
         @Override
         public Object[] toArray(Object[] a) {
             initialize();
             return super.toArray(a);
         }
 
         @Override
         public String toString() {
             initialize();
             return super.toString();
         }
         
     }
     
     public static class LazyList extends ArrayList {
         
         private transient Node n;
         private transient FieldDescriptor fd;
         private transient Session session;
         private transient String parentField;
         private transient CollectionPersister persister;
        private transient boolean initialized;
 
         public LazyList(CollectionPersister persister,
                         Node n, 
                         FieldDescriptor fd, 
                         Session session, 
                         String parentField) {
             this.persister = persister;
             this.n = n;            
             this.fd = fd;
             this.session = session;
             this.parentField = parentField;
         }
 
         public void initialize() {
             if (initialized) return;
             initialized = true;
             
             persister.initializeCollection(this, n, fd, session, parentField);
         }
         
         @Override
         public boolean add(Object arg0) {
             initialize();
             return super.add(arg0);
         }
 
         @Override
         public void add(int arg0, Object arg1) {
             initialize();
             super.add(arg0, arg1);
         }
 
         @Override
         public boolean addAll(Collection arg0) {
             initialize();
             return super.addAll(arg0);
         }
 
         @Override
         public boolean addAll(int arg0, Collection arg1) {
             initialize();
             return super.addAll(arg0, arg1);
         }
 
         @Override
         public void clear() {
             initialize();
             super.clear();
         }
 
         @Override
         public Object clone() {
             initialize();
             return super.clone();
         }
 
         @Override
         public boolean contains(Object arg0) {
             initialize();
             return super.contains(arg0);
         }
 
         @Override
         public void ensureCapacity(int arg0) {
             initialize();
             super.ensureCapacity(arg0);
         }
 
         @Override
         public Object get(int arg0) {
             initialize();
             return super.get(arg0);
         }
 
         @Override
         public int indexOf(Object arg0) {
             initialize();
             return super.indexOf(arg0);
         }
 
         @Override
         public boolean isEmpty() {
             initialize();
             return super.isEmpty();
         }
 
         @Override
         public int lastIndexOf(Object arg0) {
             initialize();
             return super.lastIndexOf(arg0);
         }
 
         @Override
         public Object remove(int arg0) {
             initialize();
             return super.remove(arg0);
         }
 
         @Override
         public boolean remove(Object arg0) {
             initialize();
             return super.remove(arg0);
         }
 
         @Override
         protected void removeRange(int arg0, int arg1) {
             initialize();
             super.removeRange(arg0, arg1);
         }
 
         @Override
         public Object set(int arg0, Object arg1) {
             initialize();
             return super.set(arg0, arg1);
         }
 
         @Override
         public int size() {
             initialize();
             return super.size();
         }
 
         @Override
         public Object[] toArray() {
             initialize();
             return super.toArray();
         }
 
         @Override
         public Object[] toArray(Object[] arg0) {
             initialize();
             return super.toArray(arg0);
         }
 
         @Override
         public void trimToSize() {
             initialize();
             super.trimToSize();
         }
 
         @Override
         public boolean equals(Object arg0) {
             initialize();
             return super.equals(arg0);
         }
 
         @Override
         public int hashCode() {
             initialize();
             return super.hashCode();
         }
 
         @Override
         public Iterator iterator() {
             initialize();
             return super.iterator();
         }
 
         @Override
         public ListIterator listIterator() {
             initialize();
             return super.listIterator();
         }
 
         @Override
         public ListIterator listIterator(int arg0) {
             initialize();
             return super.listIterator(arg0);
         }
 
         @Override
         public List subList(int arg0, int arg1) {
             initialize();
             return super.subList(arg0, arg1);
         }
 
         @Override
         public boolean containsAll(Collection arg0) {
             initialize();
             return super.containsAll(arg0);
         }
 
         @Override
         public boolean removeAll(Collection arg0) {
             initialize();
             return super.removeAll(arg0);
         }
 
         @Override
         public boolean retainAll(Collection arg0) {
             initialize();
             return super.retainAll(arg0);
         }
 
         @Override
         public String toString() {
             initialize();
             return super.toString();
         }
         
     }
 }
