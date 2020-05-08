 
 package models.user;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import models.dbentities.ClassGroup;
 import models.dbentities.ClassPupil;
 import models.dbentities.UserModel;
 
 import com.avaje.ebean.Ebean;
 
 
 /**
  * @author Sander Demeester
  * @author Jens N. Rammant
  */
 
 public class Independent extends Authenticated{
 
     private List<String> previousClassList;
 
     protected Independent(UserModel data, UserType type){
         super(data, type); //abstract class constructor could init some values
         previousClassList = new ArrayList<String>();
     }
 
     public Independent(UserModel data) {
         this(data, UserType.PUPIL_OR_INDEP);
     }
 
     /**
      * Add an old class
      * @param oldClass
      */
     public void addPreviousClass(String oldClass){
         previousClassList.add(oldClass);
     }
 
     /**
      * Add a class to Independent user.
      * @param classGroup
      */
     public void addCurrentClass(ClassGroup classGroup){
 
     }
 
     public ClassGroup getCurrentClass() throws PersistenceException{
         ClassGroup res = Ebean.find(ClassGroup.class).where().eq("id", this.data.classgroup).findUnique();
        return res != null && res.isActive() ? res : null;
     }
 	
 	/**
 	 * Queries the database for all previous classes the user is associated with
 	 * @return list of previous classes 
 	 * @throws PersistenceException
 	 */
 	public Collection<ClassGroup> getPreviousClasses() throws PersistenceException{
 		ArrayList<ClassGroup> res = new ArrayList<ClassGroup>();
 		
 		List<ClassPupil> cp = Ebean.find(ClassPupil.class).where().eq("indid", this.data.id).findList();
 		for(ClassPupil c : cp){
 			ClassGroup cg = Ebean.find(ClassGroup.class).where().eq("id", c.classid).findUnique();
 			if(cg != null)res.add(cg);
 		}
 		ClassGroup posCurrent = Ebean.find(ClassGroup.class).where().eq("id", this.data.classgroup).findUnique();
		if(posCurrent != null && !posCurrent.isActive())res.add(posCurrent);
 		
 		return res;
 	}
 	
 	/**
 	 * Returns if the user is active in the class, assuming the class is active. This does not check whether 
 	 * the class is active.
 	 * @param classID the class to be checked
 	 * @return whether or not the user is active in that class, assuming the class is active
 	 */
 	public boolean isActiveInClass(int classID){
 		if(data.classgroup == null) return false;
 		else return data.classgroup.equals(classID);
 	}
 
 
 }
