 package models;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import models.account.AccountModel;
 import models.general.CompositeModel;
 import models.tm.ProjectModel;
 import org.apache.commons.lang.ArrayUtils;
 import org.hibernate.SessionFactory;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.persister.entity.Joinable;
 import play.Play;
 import play.classloading.ApplicationClasses;
 import play.db.DB;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class SchemaCreation {
 
     private static final String CREATE_ACCOUNTMODEL_TRIGGER_SYNTAX =
             "CREATE TRIGGER naturalIdTrigger_%1$s\n" +
                     "BEFORE INSERT ON %1$s FOR EACH ROW\n" +
                     "BEGIN\n" +
                     "  DECLARE row_count BIGINT;\n" +
                    "  SELECT CASE WHEN MAX(naturalId) IS NULL THEN 0 ELSE MAX(naturalId) END\n" +
                     "  INTO row_count\n" +
                     "  FROM %1$s\n" +
                     "  WHERE %2$s=NEW.%2$s;\n" +
                     "  SET NEW.naturalId=row_count + 1;\n" +
                     "END\n";
 
     private static final String CREATE_PROJECTMODEL_TRIGGER_SYNTAX =
             "CREATE TRIGGER naturalIdTrigger_%1$s\n" +
                     "BEFORE INSERT ON %1$s FOR EACH ROW\n" +
                     "BEGIN\n" +
                     "  DECLARE row_count BIGINT;\n" +
                    "  SELECT CASE WHEN MAX(naturalId) IS NULL THEN 0 ELSE MAX(naturalId) END\n" +
                     "  INTO row_count\n" +
                     "  FROM %1$s\n" +
                     "  WHERE %2$s=NEW.%2$s AND %3$s=NEW.%3$s;\n" +
                     "  SET NEW.naturalId=row_count + 1;\n" +
                     "END\n";
 
     private final SessionFactory sessionFactory;
 
     public SchemaCreation(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     public void createTriggers() {
         Connection c = DB.getConnection();
         for (String stmt : getTriggerCreationStatements()) {
             try {
                 c.createStatement().executeUpdate(stmt);
                 System.out.println(stmt);
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         try {
             c.commit();
             c.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     private List<String> getTriggerCreationStatements() {
         List<String> triggerStatements = new ArrayList<String>();
 
         List<Class<CompositeModel>> models = getCompositeModels();
 
         for (Class<CompositeModel> c : models) {
             if (isSubclass(ProjectModel.class, c)) {
                triggerStatements.add(String.format(CREATE_PROJECTMODEL_TRIGGER_SYNTAX, getTableName(c), "project_id", "account_id"));
             } else if (isSubclass(AccountModel.class, c)) {
                 triggerStatements.add(String.format(CREATE_ACCOUNTMODEL_TRIGGER_SYNTAX, getTableName(c), "account_id"));
             } else if (!c.equals(ProjectModel.class) && !c.equals(AccountModel.class)) {
                 throw new RuntimeException("Quantum leak in the hyperdrive: " + c);
             }
         }
         return triggerStatements;
     }
 
     /**
      * Custom validation method to make sure we did not forget to annotate one class
      *
      * TODO this should actually be a unit test !!!
      */
     public void validateCompositeModels() {
         List<Class<CompositeModel>> compositeModels = getCompositeModels();
         for (Class<CompositeModel> compositeModel : compositeModels) {
             Table table = compositeModel.getAnnotation(Table.class);
             if(table == null) {
                 throw new RuntimeException("No @Table annotation with unique constraints found for entity " + compositeModel.getName());
             }
             for (UniqueConstraint uniqueConstraint : table.uniqueConstraints()) {
                 if(isSubclass(ProjectModel.class, compositeModel)) {
                     boolean valid = ArrayUtils.contains(uniqueConstraint.columnNames(), "project_id");
                     valid = valid && ArrayUtils.contains(uniqueConstraint.columnNames(), "naturalId");
                     if(!valid) {
                         throw new RuntimeException("WRONGLY MAPPED PROJECT MODEL: MISSING UNIQUE CONSTRAINT ON (naturalId, project_id)");
                     }
                 } else if(isSubclass(AccountModel.class, compositeModel)) {
                     boolean valid = ArrayUtils.contains(uniqueConstraint.columnNames(), "account_id");
                     valid = valid && ArrayUtils.contains(uniqueConstraint.columnNames(), "naturalId");
                     if(!valid) {
                         throw new RuntimeException("WRONGLY MAPPED ACCOUNT MODEL: MISSING UNIQUE CONSTRAINT ON (naturalId, project_id)");
                     }
                 } else {
                     throw new RuntimeException("Subspace anomaly");
                 }
             }
 
         }
     }
 
     private List<Class<CompositeModel>> getCompositeModels() {
         List<Class<CompositeModel>> models = new ArrayList<Class<CompositeModel>>();
 
         List<ApplicationClasses.ApplicationClass> compositeModels = Play.classes.getAssignableClasses(CompositeModel.class);
         for (ApplicationClasses.ApplicationClass c : compositeModels) {
             Class<CompositeModel> javaClass = (Class<CompositeModel>) c.javaClass;
             if (!models.contains(javaClass) && !javaClass.equals(ProjectModel.class) && !javaClass.equals(AccountModel.class)) {
                 models.add(javaClass);
             }
         }
         return models;
     }
 
     private boolean isSubclass(Class parent, Class kid) {
         return parent.isAssignableFrom(kid) && !kid.equals(parent);
     }
 
     private String getTableName(Class<?> mappedClass) {
         ClassMetadata cmd = sessionFactory.getClassMetadata(mappedClass);
 
         //check that the class is mapped to something with a table name
         if (cmd == null || !Joinable.class.isInstance(cmd))
             throw new RuntimeException("Can't get table name for class " + mappedClass);
 
         return Joinable.class.cast(cmd).getTableName();
     }
 
 
 }
