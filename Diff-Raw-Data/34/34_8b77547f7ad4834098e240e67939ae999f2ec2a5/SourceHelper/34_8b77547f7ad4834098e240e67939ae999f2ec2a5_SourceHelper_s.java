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
     * Creates a source file from the file specified by an {@link URI} obect.
      *
     * @param fixtureFile
      * @return
      * @throws FileNotFoundException
      */
    public static Source createFrom(URI fixtureFile) throws FileNotFoundException {
        return createFrom(new File(fixtureFile));
     }
 
     /**
      * Creates source from given file.
      *
     * @param f
      * @return
      * @throws FileNotFoundException
      */
    public static Source createFrom(File f) throws FileNotFoundException {
        return createFrom(new FileReader(f));
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
