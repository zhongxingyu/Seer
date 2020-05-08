 package com.sunnyd;
 
 import com.sunnyd.annotations.*;
 import com.sunnyd.database.Manager;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 import java.util.HashMap;
 /*****
  * 
  * 
  * @author Mike
  * All Children isa relation/children of a model primary key is the parent's primary key
  */
 
 public class Base {
 
     @ActiveRecordField
     private Integer id;
     
     @ActiveRecordField
     private Date creationDate;
     
     @ActiveRecordField
     private Date lastModifiedDate;
 
     private Boolean updateFlag = false;
 
     public Base() {
     }
 
     public Base(HashMap<String, Object> HM) {
         id = (Integer) HM.get("id");
         creationDate = (Date) HM.get("creationDate");
         lastModifiedDate = (Date) HM.get("lastModifiedDate");
         
         // Get Caller ClassName
         Class<?> classObject = this.getClass();
       
         //Set Attribute
         BaseHelper.setAttributes(classObject, this, HM);
         
         //Set updateDateFlag to false after setter methods
         this.setUpdateFlag(false);
     }
 
     @SuppressWarnings("unchecked")
     public static <T> T find(int id) {
         // Since this is a static method, to get caller of method we must look in stack
         // At this point stack should look like this:
         // [java.lang.Thread.getStackTrace(Unknown Source), com.sunnyd.Base.find(Base.java:79), com.sunnyd.models.Person.main(Person.java:20), .....so on]
         // TODO:Need a better solution than stack to get caller class
         StackTraceElement[] ste = Thread.currentThread().getStackTrace();
         String className = ste[2].getClassName();
         try {
             //Get class attribute from database
             String tableName = BaseHelper.getClassTableName(className);
             HashMap<String, Object> HM = Manager.find(id, tableName);
             
             //Get inherited values from parent table
             HashMap<String, Object> parentDatas = BaseHelper.getSuperDatas((Integer)HM.get("id"), Class.forName(className));
             
             if(parentDatas != null){
                 //Merge parent's table data's into map
                 HM.putAll(parentDatas);
             }
             return (T) Class.forName(className).getConstructor(HashMap.class).newInstance(HM);
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     
     @SuppressWarnings("unchecked")
     // test find for hasOne
     public static <T> T find_hasOne(int id) {
         // Since this is a static method, to get caller of method we must look in stack
         // At this point stack should look like this:
         // [java.lang.Thread.getStackTrace(Unknown Source), com.sunnyd.Base.find(Base.java:79), com.sunnyd.models.Person.main(Person.java:20), .....so on]
         // TODO:Need a better solution than stack to get caller class
         StackTraceElement[] ste = Thread.currentThread().getStackTrace();
         String className = ste[2].getClassName();
         try {
             //Get class attribute from database
             String tableName = BaseHelper.getClassTableName(className);
             HashMap<String, Object> HM = Manager.find(id, tableName);
             
             //Get inherited values from parent table
             HashMap<String, Object> parentDatas = BaseHelper.getSuperDatas((Integer)HM.get("id"), Class.forName(className));
             
             if(parentDatas != null){
                 //Merge parent's table data's into map
                 HM.putAll(parentDatas);
             }
             return (T) Class.forName(className).getConstructor(HashMap.class).newInstance(HM);
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     
     public static <T> T findAll() {
         return null;
     }
 
     public boolean update() {
         if(this.getUpdateFlag()) {
             boolean allUpdated = update(this.getClass(), this);
             if(allUpdated){
                 this.setUpdateFlag(false);
                 return allUpdated;
             }
         }
         return false;
     }
 
     private static boolean update(Class<?> classObject, Object instanceObject){
         HashMap<String, Object> updateAttributes = BaseHelper.getTableFieldNameAndValue(classObject, instanceObject);
         if(classObject.getAnnotation(ActiveRecordInheritFrom.class) !=null){
             boolean updated = Base.update(classObject.getSuperclass(), instanceObject);
             if(!updated){
                 System.out.println("Could not update "+((Base)instanceObject).getId()+" "+classObject.getName());
                 return false;
             }
         }
         return Manager.update(((Base)instanceObject).getId(), BaseHelper.getClassTableName(classObject), updateAttributes);
     }
 
     
     public Boolean Destroy() {
        boolean success = destroyHierarchy(this.getClass(), this.getId());
        if(success){
            id = null;
        }
        return success;
     }
     
     //Delete Parent Data after child has been deleted
     private static boolean destroyHierarchy(Class<?> classObject, Integer id){
         String tableName = BaseHelper.getClassTableName(classObject);
         boolean success = Manager.destroy(id, tableName);
         if(classObject.getAnnotation(ActiveRecordInheritFrom.class) !=null){
             success = Base.destroyHierarchy(classObject.getSuperclass(), id);
         }
         return success;
     }
 
 
     public boolean save() {
         int newId = 0;
         if (this.getId() == null) {
             newId = save(this.getClass(), this);
             if (newId != 0) {
                 id = newId;
                 return true;
             }
         }
         return false;
     }
     
     
     private static Integer save(Class<?> classObject, Object objectInstance){
         HashMap<String, Object> attrToPersist = BaseHelper.getTableFieldNameAndValue(classObject, objectInstance);
 //        System.out.println(classObject.getName());
         int id = 0;
         if(classObject.getAnnotation(ActiveRecordInheritFrom.class) != null){
            id = Base.save(classObject.getSuperclass(), objectInstance);
 //           System.out.println(id);
         }
         if(id != 0){
             attrToPersist.put("id", id);
 //            System.out.println(Arrays.asList(attrToPersist).toString());
         }
         return Manager.save(BaseHelper.getClassTableName(classObject), attrToPersist); 
     }
 
     
     /****** MUTATOR ****************************************************/
     // Common Mutator in all child class.
 
     public Integer getId() {
         return this.id;
     }
 
     private void setId(Integer id) {
         this.id = id;
     }
 
     public Boolean getUpdateFlag() {
         return this.updateFlag;
     }
 
     public void setUpdateFlag(Boolean flag) {
         this.updateFlag = flag;
     }
     
     public Date getCreationDate() {
         return creationDate;
     }
     
     public void setCreationDate(Date creationDate){
         this.creationDate = creationDate;
     }
     
     public Date getLastModifiedDate() {
         return lastModifiedDate;
     }
     
     public void setLastModifiedDate(Date lastModifiedDate){
         this.lastModifiedDate = lastModifiedDate;
     }
     
     /******** Private ***********************************************/
     private String getClassName() {
         return this.getClass().getName();
     }
     
     private String getTableName() {
         try {
             return this.getClass().getDeclaredField("tableName").get(null).toString();
         } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
 
 }
