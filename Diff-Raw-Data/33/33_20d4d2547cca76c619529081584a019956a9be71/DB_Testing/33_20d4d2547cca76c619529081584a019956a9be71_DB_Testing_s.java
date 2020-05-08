 package EMS_Database;
 
 /**
  *
  * @author Mike Meding
  *
  */
 public class DB_Testing extends UserData {
 
     public DB_Testing() {
         super("db");
     }
 
     public void run() {
 
         User user = new User(4, 2, "Rickey", "rickey's password", "retarded@email.com");
 
 
 // can only be done once.
 //        try {
 //            createUser(user);                        
 //        } catch (DuplicateInsertionException e) {
 //            System.out.println("Insertion Failed");
 //        }
         
        System.out.println(queryEntireTable()); //debug method
 
         //sample output
//        try {
//            //Testing get methods
//            //System.out.println(getUIDByName("Rickey"));          
//            
//            int uid = 3;
//            System.out.println("Level = " + getLevel(uid));
//            System.out.println("Name = " + getName(uid));
//            System.out.println("Password =  " + getPwd(uid));
//            System.out.println("Email = " + getEmail(uid));            
//
//        } catch (DoesNotExistException e) {
//            System.out.println("That user does not exist.");
//        }
//
//
 //        try {
 //            setName(3, "CurrentTime=wayTooLate...");            
 //        } catch (DoesNotExistException dnee) {
 //            System.out.println("updating name failed.");
 //        }
 //        
 //        try {
 //            System.out.println(getName(3));
 //        } catch(DoesNotExistException f) {
 //            System.out.println("getting name failed.");            
 //        }
 
 
     }
 
     public static void main(String[] args) {
         new DB_Testing().run();
     }
 }
