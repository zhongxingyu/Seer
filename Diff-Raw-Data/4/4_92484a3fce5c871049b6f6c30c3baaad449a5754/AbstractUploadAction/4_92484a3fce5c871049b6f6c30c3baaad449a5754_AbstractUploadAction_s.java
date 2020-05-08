 package org.seventyeight.web.actions;
 
 import com.google.gson.JsonObject;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.math.IEEE754rUtils;
 import org.apache.log4j.Logger;
 import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
 import org.seventyeight.database.mongodb.MongoDocument;
 import org.seventyeight.utils.PostMethod;
 import org.seventyeight.web.Core;
 import org.seventyeight.web.model.Action;
 import org.seventyeight.web.model.Authorizer;
 import org.seventyeight.web.model.Node;
 import org.seventyeight.web.servlet.Request;
 import org.seventyeight.web.servlet.Response;
 import org.seventyeight.web.utilities.JsonException;
 import org.seventyeight.web.utilities.JsonUtils;
 import org.seventyeight.web.utilities.ServletUtils;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * @author cwolfgang
  */
 public abstract class AbstractUploadAction<T extends AbstractUploadAction<T>> extends Action<T> {
 
     private static Logger logger = Logger.getLogger( AbstractUploadAction.class );
 
     protected AbstractUploadAction( Node parent, MongoDocument document ) {
         super( parent, document );
     }
 
     public abstract File getPath();
     public abstract String getRelativePath();
 
     /**
      * Get the filename for the upload. The filename must not contain the extension.
      * @param thisFilename The uploaded filename without extension
      * @return An extensionless filename
      */
     public abstract String getFilename( String thisFilename );
 
     public abstract Authorizer.Authorization getUploadAuthorization();
 
     @PostMethod
     public void doUpload( Request request, Response response ) throws Exception {
         request.checkAuthorization( getParent(), getUploadAuthorization() );
         logger.debug( "Uploading file" );
 
         //String relativePath = request.getUser().getIdentifier();
         File path = new File( getPath(), getRelativePath() );
 
         if( !path.exists() && !path.mkdirs() ) {
             throw new IllegalStateException( "Unable to create path " + path.toString() );
         }
 
         List<String> uploadedFilenames = ServletUtils.upload( request, path, true, 1 );
 
         logger.debug( "Filenames: " + uploadedFilenames );
 
         if( uploadedFilenames.size() > 0 ) {
             String ext = "." + FilenameUtils.getExtension( uploadedFilenames.get( 0 ) );
 
             /* Rename */
            String fname = FilenameUtils.getBaseName( uploadedFilenames.get( 0 ) );
            File f = new File( path, getFilename( fname ) + ext );
             f.delete();
             FileUtils.moveFile( new File( path, uploadedFilenames.get( 0 ) ), f );
             setExtension( ext );
 
             setFile( new File( getRelativePath(), fname ).toString() );
             try {
                 JsonObject json = JsonUtils.getJsonFromRequest( request );
                 save( request, json );
                 Core.superSave( this );
             } catch( JsonException e ) {
                 logger.warn( "Json is null: " + e.getMessage() );
                 save( request, null );
                 Core.superSave( this );
             }
 
         } else {
             throw new IllegalStateException( "No file uploaded" );
         }
 
         response.sendRedirect( "" );
     }
 
     public void setFile( String file ) {
         logger.debug( "Setting file to " + file );
         document.set( "file", file );
     }
 
     public void setExtension( String extension ) {
         document.set( "ext", extension );
     }
 
     public String getExtension() {
         return document.get( "ext", "" );
     }
 
     public void onUpload() {
         /* Default implementation is a no op, for now. */
     }
 
     //public abstract boolean allowMultiple();
 }
