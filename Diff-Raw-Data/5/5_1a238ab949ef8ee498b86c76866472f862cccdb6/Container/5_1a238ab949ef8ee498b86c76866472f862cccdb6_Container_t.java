 package com.ringlord.odf;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 import java.io.BufferedOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 
 import java.util.Iterator;
 
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 // ======================================================================
 // This file is part of the Ringlord Technologies Java ODF Library,
 // which provides access to the contents of OASIS ODF containers,
 // including encrypted contents.
 //
 // Copyright (C) 2012 K. Udo Schuermann
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>.
 // ======================================================================
 
 /**
  * <p>Represents an OASIS Open Document Format (ODF) Container, which
  * is, in essence, a Zip File containing a "META-INF/manifest.xml"
  * file with a particular structure that references the rest of the
  * file's contents, and also describes cryptographical information
  * required to decode the container's encrypted components.</p>
  *
  * <p>You should always {@link #close()} the Container when you are
  * finished with it.</p>
  *
  * <p>Two constructors are available, on accepting a {@link File}
  * object, the other takes an {@link InputStream}. Please note that as
  * of Java 1.7 the Java {@link java.util.zip.ZipInputStream} is
  * <em>unable to parse Zip files with entries that are not DEFLATED,
  * but have EXT blocks</em>. This makes the direct use of
  * ZipInputStream unreliable at best, wherefore <em>this class
  * persists an InputStream to a temporary file, and then processes
  * this as a local File.</em> The temporary file is cleaned up when
  * the Container is closed or the JVM exits properly.</p>
  *
  * <pre>
  * // import com.ringlord.odf.Container;
  * // import com.ringlord.odf.Entry;
  * // &hellip;
  *
  * try( Container odf = new Container(new File("test.odt")) )
  *   {
  *     for( Entry item : odf )
  *       {
  *         System.err.println( "\t" + item );
  *       }
  *     final Entry body = odf.get( "content.xml" );
  *     if( body != null )
  *       {
  *         System.err.println( "Found the 'content.xml' entry" );
  *         final byte[] data = (e.isEncrypted()
  *                              ? e.data("test") // "test" is the password
  *                              : e.data());
  *         if( data != null ) // it's a file, not a directory?
  *           {
  *             System.err.println( "Here is the XML:\n" + new String(data) );
  *           }
  *       }
  *   }
  * </pre>
  *
  * @author K. Udo Schuermann
  **/
 public class Container
   implements Iterable<Entry>,
              Closeable
 {
   /**
    * <p>Processes the File as an ODF container.</p>
    *
    * <p>Resources are not released until the {@linkplain #close()
    * container is closed}.
    *
    * @param file The OASIS Open Document Format (ODF) container (this
    * is actually a Zip file).
    *
    * @throws IOException An error occurred accessing or reading the
    * given file.
    *
    * @throws parserConfigurationException The XML Parser could not be
    * configured in order to parse the ODF "META-INF/manifest.xml"
    * (manifest) file.
    *
    * @throws SAXException The SAX Parser failed to process the ODF
    * manifest.
    *
    * @throws SAXParseException The SAX Parser failed to parse the ODF
    * manifest.
    **/
   public Container( final File file )
     throws IOException,
            ParserConfigurationException,
            SAXException
   {
     super();
     this.file = file;
     if( file.exists() )
       {
         this.container = new ZipFile( file );
         init();
       }
     this.isTemporaryContainer = false;
   }
 
   /**
    * <p>Processes the InputStream as an ODF container.</p>
    *
    * <p>Please note that due to a bug in the Java library's {@link
    * java.util.zip.ZipInputStream} (it cannot handle EXT blocks in a
    * non-DEFLATED entry) the given InputStream is first persisted to a
    * temporary file, which is then processed as if the ODF container
    * had been given as a {@linkplain #Container(File) local file}. The
    * temporary file is deleted when the {@linkplain #close() container
    * is closed} or the JVM exits in a proper manner.</p>
    *
    * @param f The InputStream to be processed
    *
    * @see #Container(File)
    *
    * @throws IOException An error occurred accessing or reading the
    * given file.
    *
    * @throws parserConfigurationException The XML Parser could not be
    * configured in order to parse the ODF "META-INF/manifest.xml"
    * (manifest) file.
    *
    * @throws SAXException The SAX Parser failed to process the ODF
    * manifest.
    *
    * @throws SAXParseException The SAX Parser failed to parse the ODF
    * manifest.
    **/
   public Container( final InputStream f )
     throws IOException,
            ParserConfigurationException,
            SAXException
   {
     super();
     this.file = File.createTempFile( "odf", null);
     this.file.deleteOnExit();
 
     // Copy the given InputStream into the temporary file
     final OutputStream out = new BufferedOutputStream( new FileOutputStream(this.file) );
     final byte[] buffer = new byte[ 1024 ];
     int inBuffer;
     while( (inBuffer = f.read(buffer)) > -1 )
       {
         out.write( buffer, 0, inBuffer );
       }
     out.flush();
     out.close();
 
     // Now process the temporary file
     this.container = new ZipFile( this.file );
     this.isTemporaryContainer = true;
 
     init();
   }
 
   /**
    * <p>Obtain the Container's associated {@link File}.</p>
    *
    * @return The associated File; When the Container was {@linkplain
    * #Container(InputStream) constructed from an InputStream}, the
    * associated temporary File is returned. Please note that this
    * temporary file will be removed when the Container is closed or
    * the JVM exits properly.
    **/
   public File file()
   {
     return file;
   }
 
   /*
     // UNIMPLEMENTED / UNTESTED FUNCTIONALITY
     //
     // DISABLED TO REDUCED THE CHANCE THAT YOU BUY A GUN AND KILL
     // YOURSELF AFTER SCREWING THINGS UP BEYOND ALL RECOVERY ;-)
 
   public void save()
   {
     save( this.file );
   }
   public void save( final File file )
   {
     if( file == null )
       {
         throw new IllegalStateException( "Cannot save to a null file" );
       }
   }
   */
 
   /**
    * <p>Implements the functionality that allows you to iterate over
    * the Container's {@linkplain Entry entries}. The Iterator is
    * <em>not backed by the Container</em>, meaning that it is actually
    * safe to loop over the Iterator and call {@link
    * #remove(Entry)}.</p>
    *
    * <pre>
    * Container odf = new Container( new File("test.odt") );
    * for( Entry e : odf )
    *   {
    *     if( e.name().equals("deleteme.txt") )
    *       {
    *         odf.remove( e ); // this is safe! :)
    *       }
    *   }
    * </pre>
    **/
   public Iterator<Entry> iterator()
   {
     return manifest.iterator();
   }
 
   /**
    * <p>Adds a new {@link Entry} to the Container.</p>
    *
    * @param entry The entry to be added. The entry must not be null.
    * Entries are keyed by their name (upper/lower case is significant)
    * so that an entry will replace one with the exact same name.
    **/
   public void add( final Entry entry )
   {
     manifest.add( entry );
   }
   /**
    * <p>Removes an {@link Entry} from the Container.</p>
    *
    * @param entry The entry to be removed. The entry must not be null,
    * but it is not an error if the entry is not actually a member of
    * the container.
    *
    * @return 'true' if the entry was actually removed, 'false' if it
    * was not a member (and as not removed).
    **/
   public boolean remove( Entry entry )
   {
     return manifest.remove( entry );
   }
   /**
    * <p>Obtain the {@link Entry} with the exact name given.</p>
    *
    * @return 'null' if the named Entry could not be found.
    **/
   public Entry get( final String name )
   {
     return manifest.get( name );
   }
 
   /**
    * <p>Close the Container and release all associated resources.</p>
    *
    * <p>Note that if the Container was constructed from an {@link
    * InputStream} it is important to call this method to ensure that
    * the temporary file (which can consume significant disk resources)
    * is properly closed and removed.</p>
    **/
   public void close()
     throws IOException
   {
     if( file != null )
       {
         try
           {
            if( container != null )
              {
                container.close();
              }
             if( isTemporaryContainer )
               {
                 file.delete();
               }
           }
         finally
           {
             file = null;
           }
       }
   }
 
   /**
    * <p>Begins parsing the OASIS Open Document Format (ODF)
    * container's manifest ("META-INF/manifest.xml") file, building the
    * internal representation and reference associations for the various
    * components of the container.</p>
    *
    * <p>This method is called by each constructor to prepare the
    * Container for general operations. All XML parsing happens within
    * the context of this method.</p>
    **/
   private void init()
     throws IOException,
            ParserConfigurationException,
            SAXException
   {
     final InputStream manifestStream = getInputStream( "META-INF/manifest.xml" );
     if( manifestStream == null )
       {
         throw new IllegalArgumentException( "No META-INF/manifest.xml: Not an ODF container" );
       }
 
     try
       {
         this.manifest = new Manifest( manifestStream, container );
       }
     finally
       {
         manifestStream.close();
       }
   }
 
   private Entry getEntry( final String name )
   {
     return manifest.get( name );
   }
 
   /**
    * <p>Obtain the InputStream representing the data of the indicated
    * file component. The caller is responsible for closing the
    * InputStream.</p>
    **/
   private InputStream getInputStream( final String name )
     throws IOException
   {
     final ZipEntry e = container.getEntry( name );
     if( e == null )
       {
         return null;
       }
     return container.getInputStream( e );
   }
 
   private Manifest manifest;
   private ZipFile container;
   //
   private File file;
   private final boolean isTemporaryContainer;
 }
