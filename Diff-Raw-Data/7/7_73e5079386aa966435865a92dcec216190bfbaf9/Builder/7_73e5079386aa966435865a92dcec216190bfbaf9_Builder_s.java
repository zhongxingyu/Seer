 package toolbus_ide;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.builder.BuilderBase;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.runtime.PluginBase;
 
 public class Builder extends BuilderBase {
     public static final String BUILDER_ID = Activator.kPluginID
             + ".toolbus_ide.builder";
 
     public static final String PROBLEM_MARKER_ID = Activator.kPluginID
             + ".toolbus_ide.builder.problem";
 
     public static final String LANGUAGE_NAME = "tscript";
 
     public static final Language LANGUAGE = LanguageRegistry
             .findLanguage(LANGUAGE_NAME);
 
     protected String getErrorMarkerID() {
         return PROBLEM_MARKER_ID;
     }
 
     protected String getWarningMarkerID() {
         return PROBLEM_MARKER_ID;
     }
 
     protected String getInfoMarkerID() {
         return PROBLEM_MARKER_ID;
     }
 
     protected boolean isSourceFile(IFile file) {
         IPath path = file.getRawLocation();
         if (path == null)
             return false;
 
         String pathString = path.toString();
         if (pathString.indexOf("/bin/") != -1)
             return false;
 
         return LANGUAGE.hasExtension(path.getFileExtension());
     }
 
     protected boolean isNonRootSourceFile(IFile resource) {
         return false;
     }
 
     protected void collectDependencies(IFile file) {
     }
 
     protected boolean isOutputFolder(IResource resource) {
         return resource.getFullPath().lastSegment().equals("bin");
     }
 
     protected void compile(final IFile file, IProgressMonitor monitor) {
         try {
         	System.err.println("Implement something that builds the file here");
         } catch (Exception e) {
             getPlugin().logException(e.getMessage(), e);
         }
     }
 
 	@Override
 	protected PluginBase getPlugin() {
 		return Activator.getInstance();
 	}
 }
