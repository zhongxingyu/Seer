 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project.configattr;
 
 import org.apache.log4j.Logger;
 
 import org.openide.actions.DeleteAction;
 import org.openide.actions.EditAction;
 import org.openide.actions.OpenAction;
 import org.openide.cookies.CloseCookie;
 import org.openide.cookies.EditCookie;
 import org.openide.cookies.OpenCookie;
 import org.openide.cookies.SaveCookie;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
 import org.openide.nodes.PropertySupport;
 import org.openide.nodes.Sheet;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.util.actions.CallableSystemAction;
 import org.openide.windows.TopComponent;
 
 import java.awt.EventQueue;
 import java.awt.Image;
 
 import java.io.IOException;
 
 import java.lang.reflect.InvocationTargetException;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.FutureTask;
 
 import javax.swing.Action;
 import javax.swing.SwingUtilities;
 
 import de.cismet.cids.abf.domainserver.project.DomainserverProject;
 import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.utilities.Refreshable;
 import de.cismet.cids.abf.utilities.nodes.ModifyCookie;
 
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
 import de.cismet.cids.jpa.entity.user.User;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class ConfigAttrEntryNode extends ProjectNode {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ConfigAttrEntryNode.class);
 
     private static final String SEPARATOR = " -> "; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Image userIcon;
     private final transient Image adminIcon;
     private final transient Image ugIcon;
     private final transient Image domainIcon;
 
     private final transient SaveCookie saveCookie;
 
     private transient ConfigAttrEntry entry;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ConfigAttrEntryNode object.
      *
      * @param  entry    DOCUMENT ME!
      * @param  project  DOCUMENT ME!
      */
     public ConfigAttrEntryNode(final ConfigAttrEntry entry, final DomainserverProject project) {
         super(Children.LEAF, project);
         this.entry = entry;
 
         final StringBuilder sb = new StringBuilder();
         sb.append(entry.getDomain().getName());
         if (entry.getUsergroup() != null) {
             sb.append(SEPARATOR).append(entry.getUsergroup().getName());
         }
         if (entry.getUser() != null) {
             sb.append(SEPARATOR).append(entry.getUser().getLoginname());
         }
 
         setName(sb.toString());
 
         userIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "user.png");           // NOI18N
         adminIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "admin.png");         // NOI18N
         ugIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "group.png");            // NOI18N
         domainIcon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER + "domainserver.png"); // NOI18N
 
         saveCookie = new SaveCookieImpl();
 
         getCookieSet().add(new ConfigAttrEntryOpenEditCloseCookie());
         getCookieSet().add(new ConfigAttrEntryCookieImpl());
         getCookieSet().add(new ModifyCookieImpl());
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Image getIcon(final int type) {
         if (entry.getUser() != null) {
             if (entry.getUser().isAdmin()) {
                 return adminIcon;
             } else {
                 return userIcon;
             }
         } else if (entry.getUsergroup() != null) {
             return ugIcon;
         } else if (entry.getDomain() != null) {
             return domainIcon;
         } else {
             return null;
         }
     }
 
     @Override
     public Image getOpenedIcon(final int type) {
         return super.getIcon(type);
     }
 
     @Override
     public Action[] getActions(final boolean context) {
         return new Action[] {
                 CallableSystemAction.get(OpenAction.class),
                 CallableSystemAction.get(EditAction.class),
                 null,
                 CallableSystemAction.get(DeleteAction.class)
             };
     }
 
     @Override
     public Action getPreferredAction() {
         return CallableSystemAction.get(OpenAction.class);
     }
 
     @Override
     public boolean canCopy() {
         return false;
     }
 
     @Override
     public boolean canCut() {
         return false;
     }
 
     @Override
     public boolean canDestroy() {
         return true;
     }
 
     @Override
     public boolean canRename() {
         return false;
     }
 
     @Override
     public void destroy() throws IOException {
         try {
             if (getCookieSet().getCookie(CloseCookie.class).close()) {
                 project.getCidsDataObjectBackend().delete(entry);

                final Node parent = getParentNode();
                if (parent == null) {
                    LOG.warn("cannot access parent node, refresh cannot be performed");                            // NOI18N
                } else {
                    final Refreshable refreshable = parent.getCookie(Refreshable.class);
                    if (refreshable == null) {
                        LOG.warn("cannot get Refreshable from parent, refresh cannot be performed: " + getName()); // NOI18N
                    } else {
                        refreshable.refresh();
                    }
                }

                 fireNodeDestroyed();
             }
         } catch (final Exception e) {
             final String message = "cannot destroy entry: " + entry; // NOI18N
             LOG.error(message, e);
             throw new IOException(message, e);
         }
     }
 
     @Override
     protected Sheet createSheet() {
         final Sheet sheet = super.createSheet();
         try {
             //J-
             // <editor-fold defaultstate="collapsed" desc=" Create Property: EntryID ">
             final Property idProp = new PropertySupport.Reflection(entry, Integer.class, "getId", null);             // NOI18N
             idProp.setName(
                     NbBundle.getMessage(ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().idProp.name")); // NOI18N
             // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Username ">
             final Property usernameProp = new PropertySupport.ReadOnly<String>(
                     "propUsername",                                       // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().usernameProp.user"),   // NOI18N
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().usernameProp.userOfEntry")) { // NOI18N
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         final User user = entry.getUser();
                         if(user == null){
                             return NbBundle.getMessage(
                                     ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().usernameProp.noUser");  // NOI18N;
                         }else {
                             return user.getLoginname();
                         }
                     }
                 };                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Usergroup ">
             final Property usergroupProp = new PropertySupport.ReadOnly<String>(
                     "propUsergroup",                                       // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().usergroupProp.usergroup"),   // NOI18N
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().usergroupProp.usergroupOfEntry")) { // NOI18N
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         final UserGroup usergroup = entry.getUsergroup();
                         if(usergroup == null){
                             return NbBundle.getMessage(
                                     ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().usergroupProp.noGroup");  // NOI18N;
                         }else {
                             return usergroup.getName();
                         }
                     }
                 };                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Domain ">
             final Property domainProp = new PropertySupport.ReadOnly<String>(
                     "propDomain",                                       // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().domainProp.domain"),   // NOI18N
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().domainProp.domainOfEntry")) { // NOI18N
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         return entry.getDomain().getName();
                     }
                 };                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Key ">
             final Property keyProp = new PropertySupport.ReadOnly<String>(
                     "propKey",                                       // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().keyProp.key"),   // NOI18N
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().keyProp.keyOfEntry")) { // NOI18N
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         return entry.getKey().getKey();
                     }
                 };                                                      // </editor-fold>
             // <editor-fold defaultstate="collapsed" desc=" Create Property: Value ">
             final Property valueProp = new PropertySupport.ReadOnly<String>(
                     "propValue",                                       // NOI18N
                     String.class,
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().valueProp.value"),   // NOI18N
                     NbBundle.getMessage(
                         ConfigAttrEntryNode.class, "ConfigAttrEntryNode.createSheet().valueProp.valueOfEntry")) { // NOI18N
 
                     @Override
                     public String getValue() throws IllegalAccessException, InvocationTargetException {
                         return entry.getValue().getValue();
                     }
                 };                                                      // </editor-fold>
             //J+
 
             final Sheet.Set main = Sheet.createPropertiesSet();
             main.setName("entryProperties");                                // NOI18N
             main.setDisplayName(NbBundle.getMessage(
                     ConfigAttrEntryNode.class,
                     "ConfigAttrEntryNode.createSheet().main.displayName")); // NOI18N
             main.put(idProp);
             main.put(keyProp);
             main.put(valueProp);
             main.put(domainProp);
             main.put(usergroupProp);
             main.put(usernameProp);
             sheet.put(main);
         } catch (final Exception e) {
             LOG.error("cannot create property sheet", e);                   // NOI18N
         }
 
         return sheet;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ModifyCookieImpl implements ModifyCookie {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void setModified(final boolean modified) {
             if (modified) {
                 getCookieSet().add(saveCookie);
             } else {
                 getCookieSet().remove(saveCookie);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class ConfigAttrEntryOpenEditCloseCookie implements OpenCookie, EditCookie, CloseCookie {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void edit() {
             final TopComponent editor = ConfigAttrEntryEditorFactory.createEditor(ConfigAttrEntryNode.this);
 
             if (editor == null) {
                 LOG.error("cannot create editor for node: " + ConfigAttrEntryNode.this); // NOI18N
             } else {
                 editor.setActivatedNodes(new Node[] { ConfigAttrEntryNode.this });
                 editor.open();
                 editor.requestActive();
             }
         }
 
         @Override
         public void open() {
             edit();
         }
 
         @Override
         public boolean close() {
             final TopComponent editor = ConfigAttrEntryEditorFactory.getEditor(ConfigAttrEntryNode.this);
 
             if (editor == null) {
                 return true;
             } else {
                 if (SwingUtilities.isEventDispatchThread()) {
                     return editor.close();
                 } else {
                     try {
                         final FutureTask<Boolean> closeFuture = new FutureTask<Boolean>(new Callable<Boolean>() {
 
                                     @Override
                                     public Boolean call() throws Exception {
                                         return editor.close();
                                     }
                                 });
                         EventQueue.invokeAndWait(closeFuture);
 
                         return closeFuture.get();
                     } catch (final Exception ex) {
                         LOG.warn("could not close editor", ex); // NOI18N
 
                         return false;
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class SaveCookieImpl implements SaveCookie {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void save() throws IOException {
             final ConfigAttrEditor editor = ConfigAttrEntryEditorFactory.getEditor(ConfigAttrEntryNode.this);
             if (editor == null) {
                 if (LOG.isInfoEnabled()) {
                     LOG.info("nothing to save");           // NOI18N
                 }
             } else {
                 final ConfigAttrEntry newEntry = editor.getEditorValue();
                 if (LOG.isInfoEnabled()) {
                     LOG.info("saving entry: " + newEntry); // NOI18N
                 }
 
                 try {
                     entry = project.getCidsDataObjectBackend().storeEntry(newEntry);
                 } catch (final Exception e) {
                     final String message = "could not store entry: " + newEntry; // NOI18N
                     LOG.error(message, e);
                     throw new IOException(message, e);
                 }
 
                 // if the user did choose NO_OPTION the old entry values are still present
                 // otherwise the new value will be propagated via ConfigAttrEntryCookie
                 firePropertyChange(PROP_COOKIE, null, null);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     final class ConfigAttrEntryCookieImpl implements ConfigAttrEntryCookie {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public ConfigAttrEntry getEntry() {
             return entry;
         }
     }
 }
