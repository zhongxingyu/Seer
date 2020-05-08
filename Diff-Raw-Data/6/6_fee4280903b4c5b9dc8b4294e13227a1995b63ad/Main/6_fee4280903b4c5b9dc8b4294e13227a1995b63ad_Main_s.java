 package org.thiesen.cogitter;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 
 import org.thiesen.cogitter.RectanglePacker.Rectangle;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ConcurrentHashMultiset;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset.Entry;
 import com.google.common.io.ByteStreams;
 
 public class Main {
 
     private final static int WIDTH = 1000;
     private final static int HEIGHT = 1000;
     
     private final static ImmutableSet<String> SOURCE_FILE_ENDINGS = ImmutableSet.of("java", "js", "c", "cpp", "sh", "h", "fcgi", "pl", "py","tcl"  );
 
     private static class LineCounter implements Runnable {
 
         private final static Pattern EMAIL_PATTERN = Pattern.compile( "^[0-9a-f]+\\s*\\(<(\\S+@\\S+)>" );
 
         private final ConcurrentHashMultiset<String> _counter;
         private final String _filename;
         private final File _repo;
 
         public LineCounter( final File repo, final ConcurrentHashMultiset<String> counter, final String filename ) {
             _repo =  repo;
             _counter = counter;
             _filename = filename;
         }
 
         @Override
         public void run() {
             final ProcessBuilder builder = new ProcessBuilder( "git", "annotate",  "-e", "--",  _filename );
             builder.directory( _repo );
 
             try {
                 final Process process = builder.start();
 
                 final BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
 
                 String line;
                 while ( ( line = reader.readLine() ) != null ) {
                     final Matcher matcher = EMAIL_PATTERN.matcher( line );
                     if ( matcher.find() ) {
                         _counter.add( matcher.group( 1 ).toLowerCase() );
                     }
                 }
                 process.waitFor();
             } catch ( final IOException e ) {
                 e.printStackTrace();
             } catch ( final InterruptedException e ) {
                 e.printStackTrace();
             }
         }
 
     }
 
    private final static ExecutorService FILE_BLAME_READER_EXECUTOR = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() + 1 );
 
     public static void main( final String[] args ) throws IOException, InterruptedException {
         if ( args.length != 3 ) {
             System.out.println("Usage: Main git-directory output-filename image-cache-directory");
             System.exit( 1 );
         }
         final File repo = new File( args[0] );
         final String outputBase = args[1];
         final String imageCache = args[2];
         
         final ProcessBuilder builder = new ProcessBuilder( "git", "ls-files" );
         builder.directory( repo );
         final Process process = builder.start();
 
         final BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
 
         final ConcurrentHashMultiset<String> counter = ConcurrentHashMultiset.create();
         String line;
         final List<Future<?>> futures = Lists.newLinkedList();
         while ( ( line = reader.readLine() ) != null ) {
             final String trimmed = line.trim();
             final String suffix = line.substring( line.lastIndexOf( '.' ) + 1 );
             if ( !SOURCE_FILE_ENDINGS.contains( suffix ) ) {
                 continue;
             }
             final Future<?> submitted = FILE_BLAME_READER_EXECUTOR.submit( new LineCounter( repo, counter, trimmed ) );
             futures.add( submitted );
             
         }
         process.waitFor();
 
         final int total = futures.size();
         
         FILE_BLAME_READER_EXECUTOR.shutdown();
 
         while ( !FILE_BLAME_READER_EXECUTOR.awaitTermination( 1, TimeUnit.SECONDS ) ) {
             int doneCount = 0;
             for ( final Future<?> f : futures ) {
                 if ( f.isDone() ) {
                     doneCount++;
                 }
             }
             
             final double percentComplete = ((double)doneCount / (double)total) * 100D;
           
             System.out.printf( "\rReading %.2f%% (%s of %s files)", Double.valueOf( percentComplete ), String.valueOf( doneCount ), String.valueOf( total ) );
             System.out.flush();
         }
         System.out.println();
         futures.clear();
         
         
         printStat( counter );
    
         renderImage( counter, imageCache, outputBase );
     }
 
     private static void alias(String oldName, String newName,
 			ConcurrentHashMultiset<String> counter) {
     	   counter.add( newName, counter.count( oldName ) );
            counter.remove( oldName, counter.count( oldName ) );
         
 	}
 
 	private static void printStat( final ConcurrentHashMultiset<String> counter ) {
         final Set<Entry<String>> entrySet = counter.entrySet();
 
         int sum = 0;
         for ( final Entry<String> entry : entrySet ) {
             sum += entry.getCount();
         }
 
         final List<Entry<String>> sortedEntries = Lists.newArrayList( entrySet );
         Collections.sort( sortedEntries, new Comparator<Entry<String>>() {
 
 			@Override
 			public int compare(Entry<String> o1, Entry<String> o2) {
				return Integer.valueOf( o2.getCount() ).compareTo( Integer.valueOf( o2.getCount() ) );
 						
 						
 			}
 		});
         
         for ( final Entry<String> entry : sortedEntries ) {
             System.out.printf( "%s\t%s\t%.2f%%%n", Strings.padEnd( entry.getElement() + ":", 40, ' ' ), String.valueOf( entry.getCount() ), Double.valueOf( ( entry.getCount() / (double)sum ) * 100.0D )  );
         }
     }
 
     private static void renderImage( final ConcurrentHashMultiset<String> counter, final String imageCache, final String outputBase ) throws IOException {
         int sum = 0;
         for ( final Entry<String> entry : counter.entrySet() ) {
             sum += entry.getCount();
         }
 
         final List<BufferedImage> images = loadImages( counter, sum, imageCache, outputBase );
 
         renderSortedComplete(  images, outputBase );
         renderComplete(  images, outputBase );
     }
 
     private static void renderSortedComplete( final List<BufferedImage> inImages, final String outputBase ) throws IOException {
         final List<BufferedImage> images = Lists.newArrayList( inImages );
         final BufferedImage newImage = new BufferedImage( WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB );
         final RectanglePacker<BufferedImage> packer = new RectanglePacker<BufferedImage>( WIDTH, HEIGHT, 0 );
         final Graphics graphics = newImage.createGraphics();
         
         while ( images.size() != 0 ) {
 
             System.out.println("Packing from " + images.size() );
             
             int width = 0;
             int index = 0;
 
             for ( int i = 0; i < images.size(); i++ ) {
                 final BufferedImage img = images.get( i );
                 if ( img.getWidth() > width) {
                     width = img.getWidth();
                     index = i;
                 }
             }
 
             final BufferedImage current = images.remove( index );
 
             final Rectangle insert = packer.insert( current.getWidth(), current.getHeight(), current );
 
             if ( insert == null ) {
                 System.err.println("Could not pack image with size " + current.getWidth() + " in sorted");
                 continue;
             }
             
             graphics.drawImage( current, insert.x, insert.y, insert.height, insert.width, null );
         }
 
         
         ImageIO.write( newImage, "jpg", new File( outputBase + "-sorted.jpg" ) );
     }
 
     private static void renderComplete( final List<BufferedImage> inImages, final String outputBase ) throws IOException {
         final BufferedImage newImage = new BufferedImage( WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB );
         final RectanglePacker<BufferedImage> packer = new RectanglePacker<BufferedImage>( WIDTH, HEIGHT, 0 );
         final Graphics graphics = newImage.createGraphics();
         
         for ( final BufferedImage current : inImages ) {
             final Rectangle insert = packer.insert( current.getWidth(), current.getHeight(), current );
 
             if ( insert == null ) {
                 System.err.println("Could not pack image with size " + current.getWidth() + " in unsorted" );
                 continue;
             }
             
             graphics.drawImage( current, insert.x, insert.y, insert.height, insert.width, null );
         }
 
         
         ImageIO.write( newImage, "jpg", new File( outputBase + "-unsorted.jpg" ) );
     }
     
     private static List<BufferedImage> loadImages( final ConcurrentHashMultiset<String> counter, final int sum, final String imageCache, final String outputBase )
     throws MalformedURLException, IOException {
         final double availableSize = WIDTH * HEIGHT;
         final ImmutableList.Builder<BufferedImage> images = ImmutableList.builder();
         final ImagePacker imagePacker = new ImagePacker( WIDTH, HEIGHT, 0, false );
         
         for ( final Entry<String> entry : counter.entrySet() ) {
             final double percent = ( (double)entry.getCount() / (double)sum );
             System.out.println( percent );
             
             final int smallesBorder = Math.min( WIDTH, HEIGHT );
             final int occupyableSpace = Math.min( (int)Math.floor( Math.sqrt( availableSize * percent * 0.7D ) ) , smallesBorder );
 
             if ( occupyableSpace == 0 ) {
                 continue;
             }
             
             final BufferedImage image = loadImage( imageCache, entry.getElement() );
 
             if ( image == null ) {
                 continue;
             }
             
             final BufferedImage current;
             if ( image.getWidth() != occupyableSpace ) {
                 final BufferedImage scaledImage = new BufferedImage(
                         occupyableSpace, occupyableSpace, BufferedImage.TYPE_INT_RGB);
                 final Graphics2D graphics2D = scaledImage.createGraphics();
                 graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                 graphics2D.drawImage(image, 0, 0, occupyableSpace, occupyableSpace, null);
                 
                 graphics2D.dispose();
                 
                 current = scaledImage;
             } else {
                 current = image;
             }
 
             final String text = String.format( "%.2f%%, %s, %s Lines", Double.valueOf( percent * 100.0D ), entry.getElement().replaceAll( "@.+", "" ), String.valueOf( entry.getCount() )  );
             final Graphics2D graphics = current.createGraphics();
             graphics.setColor(Color.RED);
             
             final int fontSize = Math.max( 20, (int)( (current.getHeight() * 0.05 )) );
             
             graphics.setFont(new Font( "SansSerif", Font.BOLD, fontSize ) );
             graphics.drawString( text , 1  , current.getHeight() - fontSize );
         
             graphics.dispose();
             
             images.add( current );
             
             try {
                 imagePacker.insertImage( entry.getElement(), current );
             } catch ( final RuntimeException e ) {
                 e.printStackTrace();
             }
             
             
         }
         
         ImageIO.write( imagePacker.getImage(), "jpg", new File( outputBase + "2.jpg" ) );
         
         return images.build();
     }
 
     private static BufferedImage loadImage( final String imageCache, final String email ) throws IOException {
         final File imageCacheFile = new File( imageCache, email );
         
         if ( imageCacheFile.exists() ) {
             try {
                 return ImageIO.read( imageCacheFile );
             } catch ( final IOException e  ) {
                 System.err.println("Could not read cached image for " + email + ":" + e.getMessage() );
                 imageCacheFile.delete();
             }
         }
         
         final BufferedImage loaded = loadGravatarImage( email );
         
         try {
             ImageIO.write( loaded, "jpg", imageCacheFile );
         } catch ( final IOException e ) {
             System.err.println("Could not store cache image for " + email + ": " + e.getMessage() );
             imageCacheFile.delete();
             return loaded;
         }
         
         return loaded;
     }
 
     private static BufferedImage loadGravatarImage( final String email ) throws MalformedURLException, IOException {
         final String imageUrl = "http://www.gravatar.com/avatar/" + MD5Util.md5Hex( email ) + "?s=512&d=identicon&r=x";
 
         final URL url = new URL( imageUrl );
         final URLConnection openConnection = url.openConnection();
 
         final InputStream inputStream = openConnection.getInputStream();
 
         final byte[] imageBytes = ByteStreams.toByteArray( inputStream );
 
         inputStream.close();
 
         final BufferedImage image = ImageIO.read( new ByteArrayInputStream( imageBytes ) );
         return image;
     }
 
     public static class MD5Util {
         public static String hex(final byte[] array) {
             final StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
                 sb.append(Integer.toHexString((array[i]
                                                      & 0xFF) | 0x100).substring(1,3));       
             }
             return sb.toString();
         }
         public static String md5Hex (final String message) {
             try {
                 final MessageDigest md =
                     MessageDigest.getInstance("MD5");
                 return hex (md.digest(message.getBytes("CP1252")));
             } catch (final NoSuchAlgorithmException e) {
                 throw new RuntimeException( e );
             } catch (final UnsupportedEncodingException e) {
                 throw new RuntimeException( e );
             }
         }
     }
 
 
 }
