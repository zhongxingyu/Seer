 package de.weltraumschaf.caythe.util;
 
 import de.weltraumschaf.caythe.frontend.Source;
 import java.io.*;
 import java.net.URI;
 
 /**
  * Helper to create source objects.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class SourceHelper {
 
     /**
     * Creates a source file from the file specified by an {@link URI} object.
      *
     * @param uri The resource URI.
      * @return
      * @throws FileNotFoundException
      */
    public static Source createFrom(URI uri) throws FileNotFoundException {
        return createFrom(new File(uri));
     }
 
     /**
      * Creates source from given file.
      *
     * @param file
      * @return
      * @throws FileNotFoundException
      */
    public static Source createFrom(File file) throws FileNotFoundException {
        return createFrom(new FileReader(file));
     }
 
     /**
      * Creates a source file from given string.
      *
      * @param src
      * @return
      */
     public static Source createFrom(String src) {
         return createFrom(new StringReader(src));
     }
 
     /**
      * Creates source from a given reader.
      *
      * @param r
      * @return
      */
     public static Source createFrom(Reader r) {
         return new Source(new BufferedReader(r));
     }
 
 }
