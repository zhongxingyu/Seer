 package org.deepfs.fsml;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 
 import org.deepfs.fsml.xdcr.MP3Transducer;
 import org.deepfs.util.Conf;
 import org.deepfs.util.FileCommand;
 
 /**
  * Markup files in FSML during file hierarchy traversal.
  * 
  * @author Copyright (c) 2011, Alexander Holupirek, ISC license
  */
 public class FSMLVisitor implements FileVisitor<Path> {
     /** Depth of traversal. */
     private int depth;
     /** Flag if opening directory tag still needs to be closed. */
     private boolean dirNeedsClosing;
     
     /** file(1) process using default file type output. */
     FileCommand fileDesc = new FileCommand();
     /** file(1) process using --mime-type flag. */
     FileCommand fileMime = new FileCommand(true);
 
     /**
      * Constructs indentation string.
      * 
      * @param i level of indentation
      * @return indentation string
      */
     private String indent(final int i) {
         StringBuffer sb = new StringBuffer(32);
         for (int j = 0; j < i; j++)
             sb.append(Conf.INDENT);
         return sb.toString();
     }
     
     /** 
      * Prepends indentation string to newline separated XML fragment. 
      * 
      * @param xml String containing newline separated text lines
      * @return xml with each line prepended with correct indentation
      */
     private String indent(final String xml) {
         if (xml == null) return null;
         final StringBuffer sb = new StringBuffer(256);
         final String i = indent(depth + 1);
         for (String s : xml.split("\n"))
             sb.append(i + s + "\n");
         return sb.toString();
     }
 
     /**
      * Gets MIME type string using file(1).
      * 
      * @param pathname absolute path to file
      * @return detected mime type or empty string
      */
     private String getFileMIME(final String pathname) {
         if (!fileMime.init) {
             fileMime.reportProblem();
             return null;
         }
         return fileMime.get(pathname);
     }
 
     /**
      * Gets default file type description string using file(1).
      * 
      * @param pathname absolute path to file
      * @return detected description or empty string
      */
     private String getFileDesc(final String pathname) {
         if (!fileDesc.init) {
             fileDesc.reportProblem();
             return null;
         }
         return fileDesc.get(pathname);
     }
     
     /**
      * Gets meta data from file using a transducer.
      * 
      * Decision which transducer to take is based on the MIME type detected
      * by file(1).
      * 
      * <ul>
      *   <li>Find transducer responsible for detected mime type.
      *   <li>Wrap meta data in XML (with correct indentation)</li>
      * </ul> 
      * @param mime as reported by file(1)
      * @param path (absolute) to file
      * @return XML string with extracted meta data or null
      */
     private String getMetaData(final String mime, final String path) {
         // There is only one transducer right now. Later we need a dispatcher.
         MP3Transducer mp3 = new MP3Transducer();
         if (mp3.accept(mime))
             return indent(mp3.read(path));
         return null;
     }
 
     @Override
     public FileVisitResult preVisitDirectory(final Path file,
             final BasicFileAttributes attrs) throws IOException {
         if (depth == 0) { // skip first directory
             depth++;
             return FileVisitResult.CONTINUE;
         }
         if (dirNeedsClosing)
             System.out.printf(">\n");
        final Path p = file.getName();
         System.out.printf("%s<dir name=\"%s\"", indent(depth),
                 FSML.xmlify(p.toString()));
         dirNeedsClosing = true;
         depth++;
         return FileVisitResult.CONTINUE;
     }
 
     @Override
     public FileVisitResult visitFile(final Path file,
             final BasicFileAttributes attrs) throws IOException {
         if (depth == 0)
             depth = 1;
         if (dirNeedsClosing) {
             System.out.printf(">\n");
             dirNeedsClosing = false;
         }
         final String path = file.toAbsolutePath().toString();
         final String indent = indent(depth);
        final String name = FSML.xmlify(file.getName().toString());
         final String mime = getFileMIME(path); // no xmlify for MIME types 
         final String desc = FSML.xmlify(getFileDesc(path)); 
         final String meta = getMetaData(mime, path);
         
         System.out.printf("%s<file name=\"%s\"", indent, name);
         if (mime != null) 
             System.out.printf(" mime=\"%s\"", mime);
         if (desc != null) 
             System.out.printf(" desc=\"%s\"", desc);
         if (meta != null)
             System.out.printf(">\n%s%s</file>\n", meta, indent);
         else
             System.out.printf("/>\n");
         return FileVisitResult.CONTINUE;
     }
 
     @Override
     public FileVisitResult postVisitDirectory(final Path dir,
             final IOException exc) {
         if (exc != null)
             exc.printStackTrace();
         if (--depth == 0)
             return FileVisitResult.CONTINUE;
         if (dirNeedsClosing) {
             System.out.printf("/>\n");
             dirNeedsClosing = false;
         } else
             System.out.printf("%s</dir>\n", indent(depth));
         return FileVisitResult.CONTINUE;
     }
 
     @Override
     public FileVisitResult visitFileFailed(final Path file, final IOException x)
             throws IOException {
         System.err.println("Excluded from mapping: " + x.toString());
         return FileVisitResult.CONTINUE;
     }
 }
