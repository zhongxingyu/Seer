 package com.enonic.tools.ert.selector;
 
 import java.util.logging.Logger;
 
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSelectInfo;
 import org.apache.commons.vfs2.FileSelector;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileType;
 
 import com.enonic.tools.ert.client.ResourceLocation;
 import com.enonic.tools.ert.exception.DeleteResourceException;
 import com.enonic.tools.ert.exception.ResourceNotFoundException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: rmh
  * Date: Dec 10, 2010
  * Time: 10:11:56 PM
  */
 public class ResourceFileTools
 {
 
     private final static Logger LOG = Logger.getLogger( ResourceFileTools.class.getName() );
 
     public static void copyFile( ResourceLocation source, String fileRelativePath, ResourceLocation target, ResourceSelector selector )
         throws Exception
     {
         String sourceBaseName = getBasePath( source.getRoot() );
         String targetBaseName = getBasePath( target.getRoot() );
 
        final String fileName = sourceBaseName + "/" + fileRelativePath;

        FileObject sourceFile = source.getRoot().resolveFile( fileName );
 
         if ( !sourceFile.exists() )
         {
            System.out.println( "Warning: File not found: " + fileName + ", skipping" );
            return;
         }
 
         System.out.println( "Copy sourcefile: " + sourceFile.getName() + " (" + Boolean.valueOf( sourceFile.exists() ).toString() + ")" );
 
         FileObject targetFile = target.getRoot().resolveFile( targetBaseName + "/" + fileRelativePath );
 
         targetFile.copyFrom( sourceFile, selector );
     }
 
     public static boolean deleteFile( FileObject targetRoot, String fileRelativePath, boolean allowDeleteRecursive )
         throws Exception
     {
         String targetBaseName = getBasePath( targetRoot );
 
         final String fileName = targetBaseName + "/" + fileRelativePath;
 
         FileObject targetFile = targetRoot.resolveFile( fileName );
 
         if ( targetFile.exists() )
         {
             if ( targetFile.getType().equals( FileType.FOLDER ) )
             {
                 deleteFolder( allowDeleteRecursive, targetFile );
                 return true;
             }
             else
             {
                 return targetFile.delete();
             }
         }
         else
         {
             LOG.warning( "File do delete not found: " + fileName );
             return false;
         }
 
     }
 
     private static void deleteFolder( boolean allowDeleteRecursive, FileObject targetFile )
         throws FileSystemException
     {
         if ( !allowDeleteRecursive )
         {
             throw new DeleteResourceException( "Not allowed to delete folder" );
         }
 
         deleteFolderRecursivly( targetFile );
     }
 
     private static void deleteFolderRecursivly( FileObject targetFile )
         throws FileSystemException
     {
         targetFile.delete( new FileSelector()
         {
             public boolean includeFile( FileSelectInfo fileSelectInfo )
                 throws Exception
             {
                 return true;
             }
 
             public boolean traverseDescendents( FileSelectInfo fileSelectInfo )
                 throws Exception
             {
                 return true;
             }
         } );
 
     }
 
     private static String getBasePath( FileObject root )
     {
         return root.getName().getPath();
     }
 
 
 }
