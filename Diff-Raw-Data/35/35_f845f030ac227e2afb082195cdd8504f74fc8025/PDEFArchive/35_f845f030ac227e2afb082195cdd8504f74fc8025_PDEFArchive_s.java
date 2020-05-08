 /* This file is part of calliope.
  *
  *  calliope is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  calliope is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
  */
 package calliope.export;
 import calliope.constants.Database;
 import calliope.Connector;
 import calliope.constants.JSONKeys;
 import calliope.json.JSONDocument;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.FileOutputStream;
 import calliope.exception.AeseException;
 import static calliope.export.Format.MVD;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.ArrayList;
 import edu.luc.nmerge.mvd.MVD;
 import edu.luc.nmerge.mvd.MVDFile;
 
 /**
  * Represent a hierarchical set of files as a set of nested folders
  * @author desmond
  */
 public class PDEFArchive 
 {
     public static String CORTEX_NAME = "cortex.mvd";
     public static String NAME = "archive";
     static String[] IMG_KEYS = { "img","src" };
     static String[] IMG_SUFFIXES = {".bmp",".eps",".gif",".jpg",".png",
         ".tif","jpeg","tiff"};
     /** unique wrapper for archive */
     File root;
     /** actual archive directory inside root */
     File archive;
     /** export formats to generate */
     Format[] formats;
     /**
      * Delete a directory and its contents recursively
      * @param dir the dir to delete
      */
     private void deleteDir( File dir )
     {
         File[] contents = dir.listFiles();
         for ( int i=0;i<contents.length;i++ )
         {
             if ( contents[i].isFile() )
                 contents[i].delete();
             else if ( !contents[i].getName().equals("..")
                 && !contents[i].getName().equals(".") )
                 deleteDir( contents[i] );
         }
         dir.delete();
     }
     /**
      * Make a temporary directory
      * @return the directory in tmpdir
      * @throws IOException 
      */
     private File createTempDirectory() throws IOException
     {
         final File temp;
         temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
         if ( !temp.delete() )
             throw new IOException("Could not delete temp file: " 
                 +temp.getAbsolutePath());
         if ( !temp.mkdir() )
             throw new IOException("Could not create temp directory: " 
                 +temp.getAbsolutePath());
         return temp;
     }
     /**
      * Create a PDEF archive
      * @param name the name for the archive
      * @param formats an array of formats of type
      * @param host
      */
     public PDEFArchive( String name, Format[] formats, String host ) 
         throws AeseException
     {
         this.formats = formats;
         try
         {
             root = createTempDirectory();
             archive = new File( root, name );
            root.mkdir();
             File archiveConf = new File( archive, "archive.conf" );
             FileWriter fw = new FileWriter( archiveConf );
             fw.write( "{\n\t\"" );
             fw.write( JSONKeys.BASE_URL );
             fw.write( "\": " );
             fw.write( host );
             fw.write( "\"\n}\n" );
             fw.close();
         }
         catch ( IOException ioe )
         {
             throw new AeseException( ioe );
         }
     }
     /**
      * Add a resource from the server to the root directory
      * @param db the database to fetch it from
      * @param path the path within that database
      */
     private void addToAbsolutePath( String db, String path ) throws Exception
     {
         File parent = new File( archive, "@"+db );
         if ( !parent.exists() )
             parent.mkdir();
         String fullDocID = db+"/"+path;
         String json = Connector.getConnection().getFromDb( fullDocID );
         String[] parts = path.split("/");
         File current = parent;
         for ( int i=0;i<parts.length;i++ )
         {
             if ( i == parts.length-1 )
             {
                 File child = new File( current, parts[i] );
                 if ( !child.exists() )
                 {
                     child.createNewFile();
                     FileWriter fw = new FileWriter( child );
                     fw.write( json, 0, json.length() );
                     fw.close();
                 }
             }
             else
             {
                 current = new File( current, parts[i] );
                 if ( !current.exists() )
                     current.mkdir();
             }
         }
     }
     /**
      * Write the corform specified in a cortex or corcode
      * @param jdoc the parsed json document
      * @throws Exception 
      */
     private void writeCorForm( JSONDocument jdoc ) throws Exception
     {
         String corformID = (String)jdoc.get( JSONKeys.STYLE );
         if ( corformID != null )
             addToAbsolutePath( Database.CORFORM, corformID );
     }
     private void saveSplitVersions( File dir, String[] texts, 
         ArrayList<String> versionIDs ) throws IOException
     {
         for ( int j=0;j<texts.length;j++ )
         {
             File current = dir;
             String versionID = versionIDs.get(j);
             String[] parts = versionID.split("/");
             // create group directory structure
             for ( int k=0;k<parts.length-1;k++ )
             {
                 current = new File( current, parts[k] );
                 if ( !current.exists() )
                     current.mkdir();
             }
             current = new File( current, parts[parts.length-1] );
             current.createNewFile();
             FileWriter fw = new FileWriter( current );
             fw.write( texts[j] );
             fw.close();
         }
     }
     /**
      * Write a cortex to disk
      * @param dir the directory to create it in
      * @param docID the file's docID
      */
     private void writeCortexToDir( File dir, String docID ) 
         throws Exception
     {
         FileWriter fw;
         String fullDocID = Database.CORTEX+"/"+docID;
         String json = Connector.getConnection().getFromDb( fullDocID );
         JSONDocument jdoc = JSONDocument.internalise( json );
         for ( int i=0;i<formats.length;i++ )
         {
             switch ( formats[i] )
             {
                 case MVD:
                     File mvdDir = new File( dir, Format.MVD.toString() );
                     mvdDir.mkdir();
                     writeCorForm( jdoc );
                     File cor = new File( mvdDir, CORTEX_NAME );
                     cor.createNewFile();
                     fw = new FileWriter( cor );
                     fw.write( json, 0, json.length() );
                     fw.close();
                     break;
                 case TEXT:
                     File textDir = new File( dir, Format.TEXT.toString() );
                     if ( !textDir.exists() )
                         textDir.mkdir();
                     // make a big dir for all the corcodes
                     File ctDir = new File( textDir, Database.CORTEX );
                     if ( !ctDir.exists() )
                         ctDir.mkdir();
                     ArrayList<String> versionIDs = new ArrayList<String>();
                     String[] texts = splitVersions( 
                         (String)jdoc.get(JSONKeys.BODY), versionIDs );
                     saveSplitVersions( ctDir, texts, versionIDs );
                     break;
                 case XML:
                     // unimplemented
                     break;
             }
         }
     }
     /**
      * Split a corcode into its constituent versions
      * @param corcode the merged mvd
      * @return an array of split corcodes
      * @throws AeseException if the stil couldn't be encoded as a string
      */
     private String[] splitVersions( String corcode, 
         ArrayList<String> versionIDs ) throws AeseException
     {
         try
         {
             MVD mvd = MVDFile.internalise( corcode );
             int nVersions = mvd.numVersions();
             String[] array = new String[nVersions];
             for ( int i=1;i<=nVersions;i++ )
             {
                 byte[] data = mvd.getVersion( i );
                 versionIDs.add( mvd.getGroupPath((short)i)+"/"
                     +mvd.getVersionShortName(i));
                 array[i-1] = new String( data, mvd.getEncoding() );
             }
             return array;
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
     /**
      * Do a binary lookup on a sorted array of strings
      * @param suffix the suffix to test
      * @return true if it was an image suffix else false
      */
     private boolean isInList( String suffix, String[] list )
     {
         int top,bot;
         top = 0;
         bot = list.length-1;
         while ( top <= bot )
         {
             int mid = (top+bot)/2;
             int res = list[mid].compareTo(suffix);
             if ( res == 0 )
                 return true;
             else if ( res > 0 )
                 top = mid+1;
             else
                 bot = mid-1;
         }
         return false;
     }
     /**
      * Strip image tags from STIL corcodes
      * @param images a set in which to store the image docIDs
      * @param jdoc a json document containing the corcode
      */
     private void stripFromStil( HashSet<String> images, JSONDocument jdoc )
     {
         Set<String> keys = jdoc.keySet();
         Iterator<String> iter = keys.iterator();
         while ( iter.hasNext() )
         {
             String key = iter.next();
             if ( isInList(key,IMG_KEYS) )
             {
                 String value = (String)jdoc.get(key);
                 String suffix = "";
                 if ( value.length()>4 )
                     suffix = value.substring(value.length()-4);
                 if ( isInList(suffix,IMG_SUFFIXES) && !images.contains(value) )
                     images.add( value );
             }
             else
             {
                 Object value = jdoc.get( key );
                 if ( value instanceof JSONDocument )
                     stripFromStil( images, (JSONDocument)value );
             }
         }
     }
     /**
      * Add an image to the archive
      * @param imgID the image to add
      */
     private void addImage( String imgID ) throws AeseException, IOException
     {
         byte[] imageData = Connector.getConnection().getImageFromDb( imgID );
         File corpixDir = new File( archive, Database.CORPIX );
         if ( !corpixDir.exists() )
             corpixDir.mkdir();
         String[] parts = imgID.split("/");
         File current = corpixDir;
         for ( int i=0;i<parts.length;i++ )
         {
             if ( i == parts.length-1 )
             {
                 File child = new File( current, parts[i] );
                 child.createNewFile();
                 FileOutputStream fos = new FileOutputStream( child );
                 fos.write( imageData );
                 fos.close();
             }
             else
             {
                 current = new File( current, parts[i] );
                 current.mkdir();
             }
         }
     }
     /**
      * Write a corcode to the given directory
      * @param dir the directory to write to
      * @param docID its docid minus the file name
      * @param name its file name
      * @throws Exception 
      */
     private void writeCorcodeToDir( File dir, String docID ) 
         throws Exception
     {
         FileWriter fw;
         String tail;
         String regex = docID+"/*";
         String[] corcodes = Connector.getConnection().listDocuments( 
             Database.CORCODE, regex );
         for ( int i=0;i<corcodes.length;i++ )
         {
             String fullDocID = Database.CORCODE+"/"+corcodes[i];
             String json = Connector.getConnection().getFromDb( fullDocID );
             // save the corform first
             JSONDocument jdoc = JSONDocument.internalise( json );
             writeCorForm( jdoc );
             // now split each corcode MVD into separate STIL files 
             String mvd = (String)jdoc.get(JSONKeys.BODY);
             ArrayList<String> versionIDs = new ArrayList<String>();
             String[] versions = splitVersions( mvd, versionIDs );
             // extract the image IDs for each version of the corcode
             HashSet<String> images = new HashSet<String>();
             for ( int j=0;j<versions.length;j++ )
                 stripFromStil( images, JSONDocument.internalise(versions[j]) );
             // now turn them into our desired formats
             for ( int j=0;j<formats.length;j++ )
             {
                 switch ( formats[j] )
                 {
                     case MVD:
                         File mvdDir = new File( dir, Format.MVD.toString() );
                         if ( !mvdDir.exists() )
                             mvdDir.mkdir();
                         tail = corcodes[i].substring(docID.length()+1);
                         File mvdFile = new File( mvdDir, tail );
                         mvdFile.createNewFile();
                         fw = new FileWriter( mvdFile );
                         fw.write( mvd );
                         fw.close();
                         break;
                     case TEXT:
                         // make a TEXT dir to hold all the corcodes
                         File textDir = new File( dir, Format.TEXT.toString() );
                         if ( !textDir.exists() )
                             textDir.mkdir();
                         // make a big dir for all the corcodes
                         File ccDir = new File( textDir, Database.CORCODE );
                         if ( !ccDir.exists() )
                             ccDir.mkdir();
                         // now create ONE dir for each corcode
                         tail = corcodes[i].substring(docID.length()+1);
                         File oneDir = new File( ccDir, tail );
                         if ( !oneDir.exists() )
                             oneDir.mkdir();
                         // and put each version of the corcode in it. phew!
                         saveSplitVersions( oneDir, versions, versionIDs );
                         break;
                     case XML:
                         // unimplemented
                         break;
                 }
             }
             // finally, write out the collected images
             String[] imgs = new String[images.size()];
             images.toArray( imgs );
             for ( int j=0;j<imgs.length;j++ )
                 addImage( imgs[j] );
         }
     }
     /**
      * Build an MVD record within a folder
      * @param dir the directory to store everything in
      * @param docID the docID of the cortex and corcodes
      */
     private void buildMVDDoc( File dir, String docID ) throws AeseException
     {
         try
         {
             writeCortexToDir( dir, docID );
             writeCorcodeToDir( dir, docID );
             
             // 3. get configs
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }   
     /**
      * Split an MVD into its components versions
      * @param dir the directory to store everythign in
      * @param docID the docID of the cortex and corcodes
      */
     private void buildTextDoc( File dir, String docID ) throws AeseException
     {
         try
         {
             // 1. get cortex
             String fullDocID = Database.CORTEX+"/"+docID;
             String json = Connector.getConnection().getFromDb( fullDocID );
             // 2. get corcode
             // 3. get configs
             // 4. get images
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     } 
     /**
      * Put a document into the given directory using all the PDEF's formats
      * @param dir the directory to contain the doc
      * @param docID the ID of the document in cortex and corcode
      */
     private void buildDoc( File dir, String docID ) throws AeseException
     {
         for ( int i=0;i<formats.length;i++ )
         {
             switch ( formats[i] )
             {
                 case TEXT:
                     buildTextDoc( dir, docID );
                     break;
                 case MVD:
                     buildMVDDoc( dir, docID );
                     break;
                 case XML:
                     // unimplemented
                     break;
             }
         }
     }
     /**
      * Add a CorTex to the archive by creating appropriate directories
      * @param docID the docID of the cortex
      */
     public void addCorTex( String docID ) throws AeseException
     {
         String[] parts = docID.split("/");
         if ( parts.length > 0 )
         {
             File first,last,current;
             current = archive;
             for ( int i=0;i<parts.length;i++ )
             {
                 if ( i == parts.length-1 )
                 {
                     last = new File( current, "%"+parts[i] );
                     if ( !last.exists() )
                         last.mkdir();
                     buildDoc( last, docID );
                 }
                 else if ( i == 0 )
                 {
                     current = first = new File( current, "+"+parts[0] );
                     if ( !first.exists() )
                         first.mkdir();
                 }
                 else
                 {
                     current = new File( current, parts[i] );
                     if ( !current.exists() )
                         current.mkdir();
                 }
             }
         }
     }
     /**
      * Convert this PDEF archive into a ZIP file
      * @param zipType the type of zipping to use
      * @return a file being a complete zip archive
      */
     public File zip( ZipType zipType ) throws AeseException
     {
         try
         {
             File compressed = null;
             Compressor comp = null;
             switch ( zipType )
             {
                 case TAR_GZ:
                     comp = new ZipArchive( archive );
                     break;
                 case ZIP:
                     comp = new TarArchive( archive );
                     break;
             }
             compressed = comp.compress();
             deleteDir( root );
             return compressed;
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
 }
