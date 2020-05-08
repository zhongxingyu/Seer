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
     * 
      */
    private static final String[] rooAnnotationsNames = { "/profiles/rooCommand.ecore", "profiles/rooStructure.ecore" };
 
     /**
      * 
      */
     @SuppressWarnings("unchecked")
     @Override
     public Object execute(final ExecutionEvent event) throws ExecutionException {
         File ecoreFile;
         List<EPackage> ecorePackages;
         List<String> incorrectPackages;
        List<InputStream> rooAnnotations;
 
         rooAnnotations = this.getRooAnnotations();
         incorrectPackages = new ArrayList<String>();
         ecorePackages = (List<EPackage>) Util.getSelectedItems(event);
         for (EPackage ecorePackage : ecorePackages) {
             try {
                for (InputStream rooAnnotation : rooAnnotations) {
                    ecoreFile = Util.getEcoreFile(ecorePackage);
                    Ecore2RooAnnotatedEcore.annotateEcore(ecoreFile, rooAnnotation);
                }

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
    protected List<InputStream> getRooAnnotations() {
        List<InputStream> returnValue;
 
        returnValue = new ArrayList<InputStream>(Ecore2RooAnnotatedEcoreHandler.rooAnnotationsNames.length);
        for (String rooAnnotationName : Ecore2RooAnnotatedEcoreHandler.rooAnnotationsNames) {
            returnValue.add(Util.getResourceStream(this, rooAnnotationName));
         }
 
         return returnValue;
     }
 
 }
