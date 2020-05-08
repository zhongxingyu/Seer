 /*
  * Created On: 10-Oct-06 9:24:13 AM
  */
 package com.thinkparity.ophelia.model.document;
 
 import java.text.MessageFormat;
 
 import com.thinkparity.codebase.FileUtil;
 
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 
 /**
  * <b>Title:</b>thinkParity Document Name<br>
  * <b>Description:</b>A thinkParity document name is a utility class for naming
  * documents and document versions. This includes display names; file names
  * directory names etc.
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class DocumentNameGenerator {
     
     /**
      * Create DocumentName.
      *
      */
     DocumentNameGenerator() {
         super();
     }
 
     /**
      * Generate a directory name for a document.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A directory name <code>String</code>.
      */
     public String localDirectoryName(final Document document) {
         return MessageFormat.format("{0}", document.getId());
     }
 
     /**
      * Generate an export file name for a document version.
      * 
      * @param version
      *            A <code>DocumentVerison</code>.
      * @return A file name <code>String</code>..
      */
     public String exportFileName(final DocumentVersion version) {
        return MessageFormat.format("{0}.{1}",
                 FileUtil.getName(version.getName()),
                 FileUtil.getExtension(version.getName()));
     }
 
     /**
      * Generate an local file name for a document version.
      * 
      * @param version
      *            A <code>DocumentVerison</code>.
      * @return A file name <code>String</code>..
      */
     public String localFileName(final DocumentVersion version) {
         return MessageFormat.format("{0}.{1,date,MMM dd, yyyy h mm ss a}{2}",
                 FileUtil.getName(version.getName()),
                 version.getUpdatedOn().getTime(),
                 FileUtil.getExtension(version.getName()));
     }
 
     /**
      * Generate a file name for a document.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A file name <code>String</code>.
      */
     public String fileName(final Document document) {
         return MessageFormat.format("{0}{1}",
                 FileUtil.getName(document.getName()),
                 FileUtil.getExtension(document.getName()));
     }
 }
