 package models.statistics.populations;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.avaje.ebean.Ebean;
 
 import play.db.ebean.Model.Finder;
 
 import models.EMessages;
 import models.dbentities.ClassGroup;
 import models.dbentities.UserModel;
 import models.dbentities.ClassPupil;
 
 /**
  * A Population of one class.
  * @author Felix Van der Jeugt
  */
 public class ClassPopulation extends Population {
 
     private ClassGroup classGroup;
 
     public ClassPopulation() {}
 
     public ClassPopulation(ClassGroup classGroup) {
         this.classGroup = classGroup;
     }
 
     @Override public PopulationType populationType() {
         return PopulationType.CLASS;
     }
 
     @Override public String id() {
         return "" + classGroup.id;
     }
 
     @Override public String describe() {
         return EMessages.get("statistics.populations.of_the", classGroup.name, classGroup.getSchool().name);
     }
 
     @Override public List<UserModel> getUsers() { //TODO Felix, check if this does what it should
    	if(classGroup.isActive()){
    		return classGroup.getPupils(ClassGroup.PupilSet.ACTIVE);
    	}else{
    		return classGroup.getPupils(ClassGroup.PupilSet.ALL);
    	}
     }
 
     /**
      * Factory for ClassPopulations.
      * @see models.statistics.ClassPopulation
      * @see models.statistics.PopulationFactory
      */
     public static class Factory implements Population.Factory {
         @Override public Population create(String identifier)
                 throws PopulationFactoryException {
             try {
                 int id = Integer.parseInt(identifier);
                 ClassGroup cg = Ebean.find(ClassGroup.class, identifier);
                 return new ClassPopulation(cg);
             } catch(Exception e) {
                 throw new PopulationFactoryException(e);
             }
         }
     }
 
 }
