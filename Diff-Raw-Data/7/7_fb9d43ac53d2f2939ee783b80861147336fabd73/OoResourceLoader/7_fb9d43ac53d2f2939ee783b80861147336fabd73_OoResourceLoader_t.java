 package org.otherobjects.cms.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Matcher;
 
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.springframework.beans.SimpleTypeConverter;
 import org.springframework.beans.TypeMismatchException;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.context.ResourceLoaderAware;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.ResourceLoader;
 import org.springframework.util.Assert;
 
 /**
  * OoResource factory that knows about resolving virtual resource paths into real paths. 
  * Also knows about how to populate a resource's metadata.
  * 
  * @author joerg
  *
  */
 public class OoResourceLoader implements ResourceLoaderAware, InitializingBean
 {
     @javax.annotation.Resource
     private OtherObjectsConfigurator otherObjectsConfigurator;
     private ResourceLoader resourceLoader;
     private OoResourceMetaDataHelper metaDataHelper = new OoResourceMetaDataHelper();
     private SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();
 
     public void afterPropertiesSet() throws Exception
     {
 
     }
 
     public OoResource getResource(String path) throws IOException
     {
         ResourceInfo resourceInfo = preprocessPath(path);
         Resource resoure = resourceLoader.getResource(resourceInfo.getPath());
 
         //wrap in ooResource
         DefaultOoResource ooResource = new DefaultOoResource(resoure, path, resourceInfo.isWritable());
         if (resoure.exists())
         {
             postprocessResource(ooResource);
         }
         else
         {
             preprocessNewResource(ooResource);
         }
 
         return ooResource;
     }
 
     protected void preprocessNewResource(DefaultOoResource ooResource) throws IOException
     {
         if (ooResource.isWritable())
         {
             // make all dirs in this path not yet existing
            File parentDir = ooResource.getFile().getParentFile();
             if (parentDir != null)
             {
                 parentDir.mkdirs();
             }
         }
     }
 
     protected void postprocessResource(DefaultOoResource ooResource)
     {
         String metaData = metaDataHelper.readMetaDataString(ooResource);
         if (metaData != null)
         {
             try
             {
                 OoResourceMetaData ooResourceMetaData = (OoResourceMetaData) simpleTypeConverter.convertIfNecessary(metaData, OoResourceMetaData.class);
                 ooResource.setMetaData(ooResourceMetaData);
             }
             catch (TypeMismatchException e)
             {
                 // TODO Explain why we ignore exception
             }
         }
 
     }
 
     /**
      * Transform the path into a path understandable by a Spring resource loader by stripping of the path prefix and modifying 
      * the path according to the stripped off prefix
      * 
      * @param path
      * @return
      */
     protected ResourceInfo preprocessPath(String path)
     {
         for (OoResourcePathPrefix prefix : OoResourcePathPrefix.values())
         {
             Matcher matcher = prefix.pattern().matcher(path);
             if (matcher.lookingAt())
                 return rewritePathAccordingToPrefix(matcher.replaceFirst(""), prefix);
         }
         // return unchanged if no prefix matched, defaults to non writable because with non-prefixed paths you shouldn't use OoResource specific stuff anyway
         return new ResourceInfo(path, false);
     }
 
     private ResourceInfo rewritePathAccordingToPrefix(String path, OoResourcePathPrefix prefix)
     {
         StringBuffer buf = new StringBuffer();
         boolean writable = false;
         switch (prefix)
         {
             case CORE :
                 buf.append("otherobjects.resources");
                 buf.append(path);
                 break;
             case STATIC :
                 buf.append("site.resources/static"); //FIXME this is probably wrong
                 buf.append(path);
                 break;
             case SITE :
                 buf.append("site.resources");
                 buf.append(path);
                 break;
             case DATA :
                 buf.append("file:");
                 String dataDirPath = otherObjectsConfigurator.getProperty("site.data.dir.path");
                 Assert.notNull(dataDirPath, "Property site.data.dir.path has not been set. Add it to site.properties");
                 buf.append(dataDirPath);
                 buf.append(path);
                 writable = true; //only resources in the data path should be writable
                 break;

            default :
                 // TODO Should we have a default case?
 
         }
         return new ResourceInfo(buf.toString(), writable);
     }
 
     public void setResourceLoader(ResourceLoader resourceLoader)
     {
         this.resourceLoader = resourceLoader;
     }
 
     protected class ResourceInfo
     {
         private String path;
         private boolean writable;
 
         public ResourceInfo(String path, boolean writable)
         {
             this.path = path;
             this.writable = writable;
         }
 
         public String getPath()
         {
             return path;
         }
 
         public boolean isWritable()
         {
             return writable;
         }
 
     }
 
     public void setOtherObjectsConfigurator(OtherObjectsConfigurator otherObjectsConfigurator)
     {
         this.otherObjectsConfigurator = otherObjectsConfigurator;
     }
 
 }
