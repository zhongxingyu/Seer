 package net.cyklotron.cms.skins;
 
 import java.io.IOException;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.filesystem.FileSystem;
 
 import net.cyklotron.cms.security.SecurityService;
 import net.cyklotron.cms.site.BaseSiteListener;
 import net.cyklotron.cms.site.SiteCreationListener;
 import net.cyklotron.cms.site.SiteService;
 
 public class SkinsSiteCreationListener
     extends BaseSiteListener
     implements SiteCreationListener
 {
     protected FileSystem fileSystem;
 
    protected Logger log;

     public SkinsSiteCreationListener(Logger logger, CoralSessionFactory sessionFactory,
         SecurityService cmsSecurityService, FileSystem fileSystem)
     {
         super(logger, sessionFactory, cmsSecurityService);
         this.fileSystem = fileSystem;
     }
 
 
     /**
      * Called when a new site is created.
      *
      * <p>The method will be called after the site Resources are successfully
      * copied from the template.</p>
      *
      * @param template the site template name.
      * @param name the site name.
      */
     public void createSite(SiteService siteService, String template, String name)
     {
         // copy templates
         try
         {
             String src = "/templates/sites/"+template;
             String dst = "/templates/sites/"+name;
             copyDir(src, dst);
         }
         catch(Exception e)
         {
             log.error("failed to copy templates", e);
         }
 
         // copy content
         try
         {
             String src = "/content/sites/"+template;
             String dst = "/content/sites/"+name;
             copyDir(src, dst);
         }
         catch(Exception e)
         {
             log.error("failed to copy content", e);
         }
     }
 
     // implementation ////////////////////////////////////////////////////////
 
     protected void copyDir(String src, String dst)
         throws Exception
     {
         if(!fileSystem.exists(src))
         {
             throw new IOException("source directory "+src+" does not exist");
         }
 		if(!fileSystem.canRead(src))
 		{
 			throw new IOException("source directory "+src+" is not readable");
 		}
 		if(!fileSystem.isDirectory(src))
 		{
 			throw new IOException(src+" is not a directory");
 		}
         fileSystem.mkdirs(dst);
         String[] srcFiles = fileSystem.list(src);
         for(int i=0; i<srcFiles.length; i++)
         {
             String name = srcFiles[i];
             if(name.startsWith(".") || name.equals("CVS"))
             {
                 continue;
             }
             if(fileSystem.isDirectory(src+"/"+name))
             {
                 copyDir(src+"/"+name, dst+"/"+name);
             }
             else
             {
                 fileSystem.copyFile(src+"/"+name, dst+"/"+name);
             }
         }
     }
 }
