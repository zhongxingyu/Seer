 package org.eclipse.jst.server.generic.core.internal;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleArtifact;
 
 /**
  * The abstract publisher. This is intended to be subclassed by
  * clients implementing the genericpublisher extension point.
  *
  * @author Gorkem Ercan
  */
 public abstract class GenericPublisher 
 {
     
     private IModule[] fModule;
     private GenericServerRuntime fServerRuntime;
     
    protected void initialize(IModule[] module, GenericServerRuntime runtime)
     {
         fModule = module;
        fServerRuntime = runtime;
     }
    /**
     * Called by the generic server implementation when a module is 
     * removed form the server instance. 
     * Subclasses may extend this method to perform their own module removal
     * 
     * @param module
     * @param monitor
     * @return
     */ 
    public abstract IStatus[] unpublish(IProgressMonitor monitor);
     
     /**
      * Called by the generic server implementation when a publish module 
      * event occurs. 
      * Subclasses may extend this method to perform their own publishing
      * 
      * @param resource
      * @param monitor
      * @return
      */
     public abstract IStatus[] publish(IModuleArtifact[] resource,
             IProgressMonitor monitor);
    
     public IModule[] getModule() {
         return fModule;
     }
 
     public GenericServerRuntime getServerRuntime() {
         return fServerRuntime;
     }
 }
