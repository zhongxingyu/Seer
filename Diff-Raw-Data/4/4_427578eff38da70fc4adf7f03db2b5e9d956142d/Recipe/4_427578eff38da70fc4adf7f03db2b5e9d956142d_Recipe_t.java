 package beans;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import play.i18n.Messages;
 import server.exceptions.ServerException;
 import utils.CollectionUtils;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: guym
  * Date: 1/28/13
  * Time: 3:06 PM
  */
 public class Recipe {
 
     private File recipeRootDirectory;
 
     public Recipe(File recipeFile) {
         this.recipeRootDirectory = recipeFile;
     }
 
     public static enum Type {
         APPLICATION("install-application", "application.groovy"), SERVICE("install-service", "service.groovy");
 
 
         static Type getRecipeTypeByFileName(String fileName) {
             for (Type type : values()) {
                 if (fileName.endsWith(type.fileIdentifier)){
                     return type;
                 }
             }
             return null;
         }
 
 
         String commandParam;
         String fileIdentifier;
 
         Type(String commandParam, String fileIdentifier) {
             this.commandParam = commandParam;
             this.fileIdentifier = fileIdentifier;
         }
     }
 
     private static WildcardFileFilter fileFilter = null;
     static {
         List<String> wildCards = new LinkedList<String>();
         for (Type type : Type.values()) {
             wildCards.add("*" + type.fileIdentifier);
         }
         fileFilter = new WildcardFileFilter( wildCards );
     }
     private static List<String> wildCards = new LinkedList<String>();
 
     /**
      * @return recipe type Application or Service by recipe directory.
      * @throws server.exceptions.ServerException
      *          if found a not valid recipe file.
      */
     public Type getRecipeType() {
 
         Collection<File> files = FileUtils.listFiles(recipeRootDirectory, fileFilter, null);
 
         if (CollectionUtils.isEmpty( files ) ) {
             throw new ServerException(Messages.get("recipe.not.valid.1",
                     Type.APPLICATION.fileIdentifier, Type.SERVICE.fileIdentifier));
         }
 
         if ( CollectionUtils.size( files ) > 1) {
             throw new ServerException(Messages.get("recipe.not.valid.2",
                     Type.APPLICATION.fileIdentifier, Type.SERVICE.fileIdentifier));
         }
 
        File filename = CollectionUtils.first(files);
        return Type.getRecipeTypeByFileName(filename.getName());
     }
 }
