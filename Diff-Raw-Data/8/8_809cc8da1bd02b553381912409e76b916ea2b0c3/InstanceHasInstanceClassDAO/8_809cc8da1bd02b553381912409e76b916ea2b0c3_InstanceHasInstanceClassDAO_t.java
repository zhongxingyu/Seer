 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edacc.model;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  *
  * @author dgall
  */
 public class InstanceHasInstanceClassDAO {
 
     protected static final String table = "Instances_has_instanceClass";
     protected static final String insertQuery = "INSERT INTO " + table + " (Instances_idInstance, instanceClass_idinstanceClass) VALUES (?, ?)";
     protected static final String deleteQuery = "DELETE FROM " + table + " WHERE Instances_idInstance=? AND instanceClass_idinstanceClass=?";
     private static final Hashtable<InstanceHasInstanceClass, InstanceHasInstanceClass> cache = new Hashtable<InstanceHasInstanceClass, InstanceHasInstanceClass>();
 
     /**
      * InstanceHasInstanceClass factory method, ensures that the created experiment is persisted.
      * @return new InstanceHasInstanceClass object
      */
     public static InstanceHasInstanceClass createExperimentHasInstance(Instance instance, InstanceClass instanceClass) throws SQLException {
         InstanceHasInstanceClass i = new InstanceHasInstanceClass(instance, instanceClass);
         i.setNew();
         save(i);
         cacheInstanceHasInstanceClass(i);
         return i;
     }
 
     /**
      * Returns a new InstanceHasInstanceClass object from a complete result set.
      * @param rs a complete result set containing all fields of the Instances_has_instanceClass table!
      * @return a new InstanceHasInstanceClass object. It won't check the cache!
      * @throws SQLException
      */
     private static InstanceHasInstanceClass getInstanceHasInstanceClassFromResultset(ResultSet rs) throws SQLException, InstanceClassMustBeSourceException {
         int idInstance = rs.getInt("Instances_idInstance");
         int idInstanceClass = rs.getInt("instanceClass_idinstanceClass");
         // get the Instance and InstanceClass objects from the DB
         Instance instance = InstanceDAO.getById(idInstance);
         InstanceClass instanceClass = InstanceClassDAO.getById(idInstanceClass);
         InstanceHasInstanceClass i = new InstanceHasInstanceClass(instance, instanceClass);
         return i;
     }
 
     /**
      * Persists an InstanceHasInstanceClass object in the DB.
      * This means: If it's new, it will be inserted into the db and if it's deleted, it will be removed from the db.
      * @param i
      * @throws SQLException
      */
     private static void save(InstanceHasInstanceClass i) throws SQLException {
         if (i.isDeleted()) {
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(deleteQuery);
             st.setInt(1, i.getInstance().getId());
             st.setInt(2, i.getInstanceClass().getInstanceClassID());
             st.executeUpdate();
             cache.remove(i);
         } else if (i.isNew()) {
             PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
             st.setInt(1, i.getInstance().getId());
             st.setInt(2, i.getInstanceClass().getInstanceClassID());
             st.executeUpdate();
             i.setSaved();
         }
 
     }
 
     private static InstanceHasInstanceClass getCached(InstanceHasInstanceClass i) {
         if (cache.containsKey(i)) {
             return cache.get(i);
         } else {
             return null;
         }
     }
 
     private static void cacheInstanceHasInstanceClass(InstanceHasInstanceClass i) {
         if (cache.containsKey(i)) {
             return;
         } else {
             cache.put(i, i);
         }
     }
 
     /**
      * Removes an InstanceHasInstanceClass object from the DB and the cache.
      * @param i
      * @throws SQLException
      */
     public static void removeInstanceHasInstanceClass(InstanceHasInstanceClass i) throws SQLException {
         i.setDeleted();
         save(i);
     }
 
     private static Vector<InstanceHasInstanceClass> getInstanceHasInstanceClassByInstanceClassId(int id) throws SQLException, InstanceClassMustBeSourceException {
         Vector<InstanceHasInstanceClass> res = new Vector<InstanceHasInstanceClass>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT * FROM " + table + " WHERE instanceClass_idinstanceClass=?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             InstanceHasInstanceClass i = getInstanceHasInstanceClassFromResultset(rs);
 
             InstanceHasInstanceClass c = getCached(i);
             if (c != null) {
                 res.add(c);
             } else {
                 i.setSaved();
                 cacheInstanceHasInstanceClass(i);
                 res.add(i);
             }
         }
         return res;
     }
 
      private static Vector<InstanceHasInstanceClass> getInstanceHasInstanceClassByInstanceId(int id) throws SQLException, InstanceClassMustBeSourceException {
         Vector<InstanceHasInstanceClass> res = new Vector<InstanceHasInstanceClass>();
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT * FROM " + table + " WHERE instanceClass_idinstanceClass=?");
         st.setInt(1, id);
         ResultSet rs = st.executeQuery();
         while (rs.next()) {
             InstanceHasInstanceClass i = getInstanceHasInstanceClassFromResultset(rs);
 
             InstanceHasInstanceClass c = getCached(i);
             if (c != null) {
                 res.add(c);
             } else {
                 i.setSaved();
                 cacheInstanceHasInstanceClass(i);
                 res.add(i);
             }
         }
         return res;
     }
 
     /**
      * Returns all persisted instances of an already persisted user defined instance class.
      * @param instanceClass
      * @return
      */
    public static Vector<Instance> getInstanceClassElements(InstanceClass instanceClass) throws SQLException {
         Vector<Instance> elements = new Vector<Instance>();
        Vector<InstanceHasInstanceClass> relation = getInstanceHasInstanceClassByInstanceClassId(instanceClass.getInstanceClassID());
         for (InstanceHasInstanceClass el : relation) {
             elements.add(el.getInstance());
         }
         return elements;
     }
 
     /**
      * Returns all persisted instance classes of an already persisted instance.
      * @param instanceClass
      * @return
      */
     public static Vector<InstanceClass> getInstanceClassElements(Instance instance) {
         Vector<InstanceClass> elements = new Vector<InstanceClass>();
        Vector<InstanceHasInstanceClass> relation = getInstanceHasInstanceClassByInstanceId(instance.getId());
         for (InstanceHasInstanceClass el : relation) {
             elements.add(el.getInstanceClass());
         }
         return elements;
     }
 }
