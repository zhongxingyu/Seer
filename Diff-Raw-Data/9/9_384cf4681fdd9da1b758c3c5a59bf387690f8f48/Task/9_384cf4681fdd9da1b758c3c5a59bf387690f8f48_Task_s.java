 package crowdtrust;
 
 public abstract class Task
 {
 
   public enum Type
   {
     BinaryImageClassification;
   };
 
   protected int id;
   protected String name;
   protected String question;
 
   public Task()
   {
 	  //for the benefit of the object mapper
   }
 
   public int getId()
   {
     return this.id;
   }
 
   public String getName()
   {
     return this.name;
   }
 
   protected void addToDatabase()
   {
     // need to be able to find out which user is logged in
     // get a connection, call serializeAdditionalData, insert into database
   }
 
   /*TODO
   protected abstract BLOB serializeAdditionalData();
 */
   public abstract void assignCrowd();
 
   // Define implementations of general crowd assignment methods, to be used by
   // assignCrowd.
 
};
