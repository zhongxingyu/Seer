 package com.celements.blog.article;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xwiki.bridge.DocumentAccessBridge;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.parents.IDocParentProviderRole;
import com.celements.parents.XDocParents;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 
@Component(XDocParents.DOC_PROVIDER_NAME)
 public class ArticleParent implements IDocParentProviderRole {
   
  private static Logger _LOGGER = LoggerFactory.getLogger(XDocParents.class);
 
   public static final String DOC_PROVIDER_NAME = "xwiki";
 
   @Requirement
   private DocumentAccessBridge docAccessBridge;
 
   @Override
   public List<DocumentReference> getDocumentParentsList(DocumentReference docRef) {
     ArrayList<DocumentReference> docParents = new ArrayList<DocumentReference>();
     try {
       DocumentReference nextParent = getParentRef(docRef);
       while ((nextParent != null)
           && docAccessBridge.exists(nextParent) && !docParents.contains(nextParent)) {
         docParents.add(nextParent);
         nextParent = getParentRef(nextParent);
       }
     } catch (XWikiException e) {
       _LOGGER.error("Failed to get parent reference. ", e);
     }
     return docParents;
   }
 
   private DocumentReference getParentRef(DocumentReference docRef) throws XWikiException {
     try {
       return ((XWikiDocument)docAccessBridge.getDocument(docRef)).getParentReference();
     } catch (Exception exp) {
       _LOGGER.error("Failed to get document [" + docRef + "].", exp);
       throw new XWikiException(XWikiException.MODULE_XWIKI_DOC,
           XWikiException.ERROR_XWIKI_UNKNOWN, "Failed to get document [" + docRef + "].",
           exp);
     }
 
   }
 
 }
