 package cinnamon.index;
 
 import cinnamon.index.Indexable;
 import org.apache.lucene.document.Document;
 import cinnamon.interfaces.XmlConvertable;
 
 /**
  * A ResultValidator checks if the results are correct and access may be granted to
  * the user. For Cinnamon, it needs to check the permissions of a given
  * object.<br/>
  * @author ingo
  *
  */
 public interface ResultValidator {
 
 	/**
      * Validate access permissions for a Lucene search result.
 	 * This method checks vs. BROWSE_OBJECT or BROWSE_FOLDER-permission on the
 	 * item's ACL and returns true/false, depending on the result.
      * If the object has to bee excluded from the result set for any reason,
 	 * validateSearchResult returns false (and not an exception, though those
 	 * should be logged).
 	 * <br/>
 	 * Note that this is a rather expensive operation, as it requires loading
 	 * a lot of objects (Permissions, ACLs, OSDs, Folders, ... the whole six yards)
 	 * from the database. Much more so for large result sets.
      *
 	 * @param doc the Document to check
      * @param filterClass a class which implements the Indexable interface and will be used to filter all
     * results whose class is not of filterClass. For example, if you set this parameter to cinnamon.Folder,
     * all cinnamon.data.OSD-results will be omitted from the final result. This parameter may be null, in that
      * case class based filtering will not happen).
 	 * @return the XmlConvertable instance referenced by the document - or null if this result was filtered.
 	 */
 	XmlConvertable validateAccessPermissions(Document doc, Class<? extends Indexable> filterClass);
 
 }
