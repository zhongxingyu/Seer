 package mx.itesm.model2roo.handlers;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import mx.itesm.model2roo.Ecore2RooAnnotatedEcore;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.emf.ecore.EPackage;
 
 /**
  * Handler to transform from UML to Ecore.
  * 
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class Ecore2RooAnnotatedEcoreHandler extends AbstractHandler {
 
     /**
     * Names of the roo annotations files.
      */
    private static List<String> rooAnnotationsNames;

    static {
        rooAnnotationsNames = new ArrayList<String>();
        rooAnnotationsNames.add("/profiles/rooCommand.ecore");
        rooAnnotationsNames.add("profiles/rooStructure.ecore");
    }
 
     /**
      * 
      */
     @SuppressWarnings("unchecked")
     @Override
     public Object execute(final ExecutionEvent event) throws ExecutionException {
         File ecoreFile;
        InputStream[] rooAnnotations;
         List<EPackage> ecorePackages;
         List<String> incorrectPackages;
 
         rooAnnotations = this.getRooAnnotations();
         incorrectPackages = new ArrayList<String>();
         ecorePackages = (List<EPackage>) Util.getSelectedItems(event);
         for (EPackage ecorePackage : ecorePackages) {
             try {
                ecoreFile = Util.getEcoreFile(ecorePackage);
                Ecore2RooAnnotatedEcore.annotateEcore(ecoreFile, rooAnnotations);
             } catch (Exception e) {
                 incorrectPackages.add(ecorePackage.getName());
                 e.printStackTrace();
             }
         }
 
         // Output results
         Util.outputMessage(event, "The Ecore models were correctly annotated",
                         "An error occurred while annotating packages: ", incorrectPackages);
 
         return null;
     }
 
     /**
      * 
      * @return
      */
    protected InputStream[] getRooAnnotations() {
        InputStream[] returnValue;
 
        returnValue = new InputStream[Ecore2RooAnnotatedEcoreHandler.rooAnnotationsNames.size()];
        for (int i = 0; i < Ecore2RooAnnotatedEcoreHandler.rooAnnotationsNames.size(); i++) {
            returnValue[i] = Util.getResourceStream(this, Ecore2RooAnnotatedEcoreHandler.rooAnnotationsNames.get(i));
         }
 
         return returnValue;
     }
 
 }
