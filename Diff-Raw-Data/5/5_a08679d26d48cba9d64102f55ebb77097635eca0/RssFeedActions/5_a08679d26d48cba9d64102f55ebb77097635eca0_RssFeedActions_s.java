 package org.nuxeo.rss.reader.manager.seam;
 
 import static org.jboss.seam.annotations.Install.FRAMEWORK;
 
 import java.io.Serializable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Install;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
 import org.nuxeo.ecm.core.api.security.ACE;
 import org.nuxeo.ecm.core.api.security.ACL;
 import org.nuxeo.ecm.core.api.security.ACP;
 import org.nuxeo.ecm.core.api.security.SecurityConstants;
 import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
 import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
 import org.nuxeo.ecm.platform.usermanager.UserManager;
 import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;
 import org.nuxeo.rss.reader.manager.api.Constants;
 
 @Name("rssFeedActions")
 @Scope(ScopeType.CONVERSATION)
 @Install(precedence = FRAMEWORK)
 public class RssFeedActions implements Serializable {
 
     private static final long serialVersionUID = 8882417548656036277L;
 
     private static final String RSS_FEEDS_FOLDER = "rssFeeds";
 
     private static final String MANAGEMENT_ROOT_PATH = "/management";
 
     public static final String RSS_FEED_CONTAINER_PATH = MANAGEMENT_ROOT_PATH
             + "/" + RSS_FEEDS_FOLDER;
 
     protected static final Log log = LogFactory.getLog(RssFeedActions.class);
 
     @In(create = true)
     protected transient CoreSession documentManager;
 
     @In(create = true)
     protected transient DocumentActions documentActions;
 
     @In(create = true)
     protected transient UserManager userManager;
 
     protected String rssFeedsContainerPath = null;
 
     protected DocumentModel newRssFeedModel = null;
 
     protected boolean showForm = false;
 
    public DocumentModel getBareReportModel() throws ClientException {
         return documentManager.createDocumentModel(Constants.RSS_FEED_TYPE);
     }
 
     public DocumentModel getNewReportModel() throws ClientException {
         if (newRssFeedModel == null) {
            newRssFeedModel = getBareReportModel();
         }
         return newRssFeedModel;
     }
 
     public void saveDocument() throws ClientException {
         createRssFeedModelContainerIfNeeded();
         documentActions.saveDocument(newRssFeedModel);
         resetDocument();
         toggleForm();
     }
 
     protected void resetDocument() {
         newRssFeedModel = null;
     }
 
     public boolean isShowForm() {
         return showForm;
     }
 
     public void toggleForm() {
         showForm = !showForm;
     }
 
     public void toggleAndReset() {
         toggleForm();
         resetDocument();
     }
 
     protected void createRssFeedModelContainerIfNeeded() throws ClientException {
         if (!documentManager.exists(new PathRef(RSS_FEED_CONTAINER_PATH))) {
             createRssFeedContainer(RSS_FEED_CONTAINER_PATH);
         }
     }
 
     protected void createRssFeedContainer(String path) throws ClientException {
         new UnrestrictedRssFeedContainerCreator(path).runUnrestricted();
     }
 
     public String getRssFeedsContainerPath() throws ClientException {
 
         return RSS_FEED_CONTAINER_PATH;
     }
 
     public class UnrestrictedRssFeedContainerCreator extends
             UnrestrictedSessionRunner {
 
         protected String rssFeedModelContainerPath;
 
         protected UnrestrictedRssFeedContainerCreator(
                 String reportModelsContainerPath) {
             super(documentManager);
             this.rssFeedModelContainerPath = reportModelsContainerPath;
         }
 
         @Override
         public void run() throws ClientException {
             if (!session.exists(new PathRef(rssFeedModelContainerPath))) {
                 DocumentModel doc = session.createDocumentModel(
                         MANAGEMENT_ROOT_PATH, RSS_FEEDS_FOLDER,
                         Constants.RSS_FEED_ROOT_TYPE);
                 doc.setPropertyValue("dc:title", "Rss Feed Models");
                 doc = session.createDocument(doc);
 
                 ACP acp = new ACPImpl();
                 ACL acl = new ACLImpl();
                 for (String administratorGroup : userManager.getAdministratorsGroups()) {
                     ACE ace = new ACE(administratorGroup,
                             SecurityConstants.EVERYTHING, true);
                     acl.add(ace);
                 }
                 acp.addACL(acl);
                 doc.setACP(acp, true);
                 session.save();
             }
         }
     }
 
 }
