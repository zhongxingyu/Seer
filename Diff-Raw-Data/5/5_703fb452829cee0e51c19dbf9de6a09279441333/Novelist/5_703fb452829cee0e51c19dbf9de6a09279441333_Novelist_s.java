 /*
  * Copyright (C) 2010 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package novelang.novelist;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import novelang.system.DefaultCharset;
 import novelang.system.Log;
 import novelang.system.LogFactory;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Generates multiple {@link novelang.part.Part}s incrementally. 
  *
  * @author Laurent Caillette
  */
 public class Novelist {
 
   private static final Log LOG = LogFactory.getLog( Novelist.class );
 
   private final File directory ;
   private final String filenamePrototype ;
   private final GeneratorSupplier< Level > generatorSupplier ;
   private final List<Ghostwriter> ghostwriters = Lists.newArrayList() ;
 
 
   public Novelist(
       final File directory,
       final String fileNamePrototype,
       final GeneratorSupplier< Level > generatorSupplier,
       final int ghostwriterCount
   ) throws IOException {
    checkArgument( directory.isDirectory() ) ;
     this.directory = directory ;
     checkArgument( ! StringUtils.isBlank( fileNamePrototype ) ) ;
     this.filenamePrototype = fileNamePrototype ;
     this.generatorSupplier = checkNotNull( generatorSupplier ) ;
     createFreshDirectory( directory ) ;
     for( int i = 1 ; i <= ghostwriterCount ; i ++ ) {
       addGhostwriter() ;
     }
     createComposium( directory ) ;
   }
 
   public static final String BOOK_NAME_RADIX = "book" ;
   
   private static final String BOOK_FILE_NAME = BOOK_NAME_RADIX + ".nlb" ;
   private static final String BOOK_CONTENT = "insert file:." ;
 
   private static void createFreshDirectory( final File directory ) throws IOException {
     if( directory.exists() ) {
       FileUtils.deleteDirectory( directory ) ;
       LOG.info( "Deleted directory '" + directory.getAbsolutePath() + "' and all its contents." ) ;
     }
     if( directory.mkdirs() ) {
       LOG.info( "Created directory '" + directory.getAbsolutePath() + "'." ) ;
     }
     
   }
   
   private static File createFreshNovellaFile( 
       final File directory, 
       final String prototype, 
       final int counter 
   )
       throws IOException
   {
     final File targetFile = new File( 
         directory, prototype + "-" + String.format( "%04d", counter ) + ".nlp" ) ;
     if( targetFile.exists() ) {
       if( ! targetFile.delete() ) {
         throw new IOException( "Could not delete file: '" + targetFile.getAbsolutePath() + "'" ) ;
       }
       if( ! targetFile.createNewFile() ) {
         throw new IOException( "Could not create file: '" + targetFile.getAbsolutePath() + "'" ) ;
       }
     }
     return targetFile ;
   }
 
 
   private static void createComposium( final File directory ) throws IOException {
     final File bookFile = new File( directory, BOOK_FILE_NAME ) ;
     FileUtils.writeStringToFile( bookFile, BOOK_CONTENT ) ;
     LOG.info( "Created Composium: '" + bookFile.getAbsolutePath() + "'" ) ;
   }
   
   public void addGhostwriter() throws IOException {
     synchronized( ghostwriters ) {
       final int ghostwriterIndex = ghostwriters.size() + 1 ;
       final File file = createFreshNovellaFile( directory, filenamePrototype, ghostwriterIndex ) ;
       ghostwriters.add( new Ghostwriter( file, generatorSupplier.get( ghostwriterIndex ) ) ) ;
     }
   }
 
   public long addGhostwriter( final int iterationCountForNewGhostwriter ) throws IOException {
     final Ghostwriter newGhostwriter ;
     synchronized( ghostwriters ) {
       final int ghostwriterIndex = ghostwriters.size() + 1 ;
       final File file = createFreshNovellaFile( directory, filenamePrototype, ghostwriterIndex ) ;
       newGhostwriter = new Ghostwriter( file, generatorSupplier.get( ghostwriterIndex ) );
       ghostwriters.add( newGhostwriter ) ;
       return newGhostwriter.write( iterationCountForNewGhostwriter ) ;
     }
   }
 
   /**
    * Calls {@link novelang.novelist.Novelist.Ghostwriter#write(int)} multiple times.
    *
    * @param iterationCount number of iterations.
    * @return number of bytes written.
    */
   public long write( final int iterationCount ) throws IOException {
     long bytesWritten = 0 ;
     synchronized( ghostwriters ) {
       for( final Ghostwriter ghostwriter : ghostwriters ) {
         bytesWritten += ghostwriter.write( iterationCount ) ;
       }
     }
     LOG.debug( "Writing done, wrote " + bytesWritten + " bytes." ) ;
     return bytesWritten ;
   }
 
   private static class Ghostwriter {
     private final File file ;
     private final Generator< ? extends TextElement > generator ;
 //    private static final Log WRITER_LOG = LogFactory.getLog( Ghostwriter.class ) ;
 
     private Ghostwriter(
         final File file,
         final Generator< ? extends TextElement > generator
     ) {
       this.file = file ;
       this.generator = generator ;
     }
 
     public long write( final int iterationCount ) throws IOException {
       Preconditions.checkArgument( iterationCount >= 0 ) ;
       long bytesWritten = 0L ;
       final OutputStream outputStream = new FileOutputStream( file, true ) ;
       try {
         for( int i = 0 ; i < iterationCount ; i ++ ) {
           final String text = generator.generate().getLiteral() ;
           bytesWritten += ( long ) text.length() ;
           IOUtils.write( text, outputStream, DefaultCharset.SOURCE.name() ) ;
 //          WRITER_LOG.debug( "{" + name + "} wrote " + text.length() + " bytes." ) ;
         }
       } finally {
         outputStream.close() ;
       }
       return bytesWritten ;
     }
 
   }
 
   private static final int DEFAULT_GHOSTWRITER_COUNT = 5 ;
   private static final int DEFAULT_ITERATION_COUNT = 3 ;
 
   public static void main( final String[] args ) throws IOException {
     if( args.length < 1 ) {
       System.out.println( Novelist.class.getName() +
           "<target-file-noprefix> [ ghostwriter-count [iteration-count] ]" ) ;
       System.exit( 1 ) ;
     }
 
     final int ghostwriterCount ;
     if( args.length < 2 ) {
       ghostwriterCount = DEFAULT_GHOSTWRITER_COUNT ;
     } else {
       ghostwriterCount = Integer.parseInt( args[ 1 ] ) ;
     }
 
     final int iterationCount ;
     if( args.length < 3 ) {
       iterationCount = DEFAULT_ITERATION_COUNT ;
     } else {
       iterationCount = Integer.parseInt( args[ 2 ] ) ;
     }
 
     new Novelist(
        new File( "." ),
         args[ 0 ],
         new LevelGeneratorSupplierWithDefaults(), 
         ghostwriterCount
     ).write( iterationCount ) ;
   }
 
   public interface GeneratorSupplier< T extends TextElement > {
     Generator< ? extends T > get( final int number ) ;
   }
 
   public static class LevelGeneratorSupplierWithDefaults implements GeneratorSupplier< Level > {
     public Generator< ? extends Level > get( final int number ) {
       final LevelGenerator.Configuration configuration = GenerationDefaults.FOR_LEVELS
           .withLevelCounterStart( number )
           .withLockLevelCounterAtDepthOne( true )
       ;
       return new LevelGenerator( configuration ) ;
     }
   }
 }
