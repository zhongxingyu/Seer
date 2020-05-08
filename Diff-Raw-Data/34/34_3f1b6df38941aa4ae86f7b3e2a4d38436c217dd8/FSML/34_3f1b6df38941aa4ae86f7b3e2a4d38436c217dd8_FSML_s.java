 package org.deepfs.fsml;
 
 import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.Path;
 import java.nio.file.attribute.Attributes;
 
 import org.deepfs.util.Conf;
 
 public final class FSML {
 
     private FSML() { }
 
     /** Printer putting FSML on output stream. */
     public static PrintWriter out = new PrintWriter(System.out, true);
 
     /**
      * Replaces characters not allowed in XML element name/attribute value.
      *
      * @param s String to be escaped
      * @return escaped string
      */
     public static String xmlify(final String s) {
         return xmlify(s, true);
     }
 
     /**
      * Replace characters not allowed in text content.
      *
      * @param s String to be analysed
      * @return xmlified text content string
      */
     public static String xmlifyTextContent(final String s) {
         return xmlify(s, false);
     }
 
     /**
      * Replaces characters not allowed in XML element content/attribute value.
      *
      * @param s String to be escaped
      * @param dquote whether to escape double quote '"'
      * @return escaped string
      */
     private static String xmlify(final String s, final boolean dquote) {
         if(s == null) return "";
         final StringBuilder sb = new StringBuilder(s.length());
         for (final char c : s.toCharArray()) {
             switch (c) {
             case '&':  sb.append("&amp;");  break;
             case '>':  sb.append("&gt;");   break;
             case '<':  sb.append("&lt;");   break;
             case '\"':
                 if (dquote) {
                     sb.append("&quot;");
                     break;
                 }
                /* [MS] had one email containing Ux01, thus this fix */
             case '\u0001': sb.append(""); break;
                /* FALLTHROUGH */
             default:   sb.append(c);
             }
         }
         return sb.toString();
     }
 
     /** FSML root element tag name. */
     private static final String S_FSML = "fsml";
     /** FSML directory element tag name. */
     private static final String S_DIR = "dir";
     /** FSML file/directory name attribute. */
     private static final String S_NAME = "name";
 
     /**
      * Converts a pathname to a FSML XPath expression.
      *
      * Expected are 'absolute, normalized' pathnames, i.e., starting
      * with a slash, redundant and trailing slashes removed.
      * @param p path name
      * @param dir toggle flag
      * @return query
      */
     public static String pn2xp(final String p, final boolean dir) {
       final StringBuilder qb = new StringBuilder();
       final StringBuilder eb = new StringBuilder();
       // cut <fsml root="/a/b/c" /> prefix from path
       final String path = p.substring(Conf.root.length());
       System.err.println("[pn2xp] " + p + " " + path);
 
       qb.append("/" + S_FSML);
       if(path.equals("/")) return qb.toString();
 
       for(int i = 0; i < path.length(); ++i) {
         final char c = path.charAt(i);
         if(c == '/') {
           if(eb.length() != 0) {
             qb.append(S_DIR + "[@" + S_NAME + " = \"" + eb + "\"]");
             eb.setLength(0);
           }
           qb.append(c);
         } else {
           eb.append(c);
         }
       }
       if(eb.length() != 0)
         if(dir) qb.append(S_DIR + "[@" + S_NAME + " = \"" + eb + "\"]");
         else qb.append("*[@" + S_NAME + " = \"" + eb + "\"]");
 
       String qu = qb.toString();
       qu = qu.endsWith("/") ? qu.substring(0, qu.length() - 1) : qu;
 
       System.err.println("[pn2xp] " + qu);
 
       return qu;
     }
 
     /**
      * Determine if path is a directory.
      * @param path to be checked
      * @return whether path is a directory
      */
     public static boolean isDirectory(final Path path) {
         try {
             return Attributes.readBasicFileAttributes(path,
                     NOFOLLOW_LINKS).isDirectory();
         } catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
         }
         return false;
     }
 }
