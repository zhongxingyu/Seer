 /*
  * $Id: ContextPathResolver.java 794 2008-04-23 22:23:10Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.server;
 
 import org.mule.galaxy.Activity;
 import org.mule.galaxy.ActivityManager;
 import org.mule.galaxy.ActivityManager.EventType;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactPolicyException;
 import org.mule.galaxy.ArtifactType;
 import org.mule.galaxy.ArtifactTypeDao;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.Comment;
 import org.mule.galaxy.CommentManager;
 import org.mule.galaxy.Dependency;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyDescriptor;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.impl.jcr.UserDetailsWrapper;
 import org.mule.galaxy.index.Index;
 import org.mule.galaxy.index.IndexManager;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.lifecycle.TransitionException;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.policy.ArtifactCollectionPolicyException;
 import org.mule.galaxy.policy.ArtifactPolicy;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.render.ArtifactRenderer;
 import org.mule.galaxy.render.RendererManager;
 import org.mule.galaxy.security.AccessControlManager;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.web.client.RPCException;
 import org.mule.galaxy.web.rpc.ApplyPolicyException;
 import org.mule.galaxy.web.rpc.ArtifactGroup;
 import org.mule.galaxy.web.rpc.ArtifactVersionInfo;
 import org.mule.galaxy.web.rpc.BasicArtifactInfo;
 import org.mule.galaxy.web.rpc.DependencyInfo;
 import org.mule.galaxy.web.rpc.ExtendedArtifactInfo;
 import org.mule.galaxy.web.rpc.ItemExistsException;
 import org.mule.galaxy.web.rpc.ItemNotFoundException;
 import org.mule.galaxy.web.rpc.RegistryService;
 import org.mule.galaxy.web.rpc.SearchPredicate;
 import org.mule.galaxy.web.rpc.TransitionResponse;
 import org.mule.galaxy.web.rpc.WActivity;
 import org.mule.galaxy.web.rpc.WApprovalMessage;
 import org.mule.galaxy.web.rpc.WArtifactPolicy;
 import org.mule.galaxy.web.rpc.WArtifactType;
 import org.mule.galaxy.web.rpc.WComment;
 import org.mule.galaxy.web.rpc.WGovernanceInfo;
 import org.mule.galaxy.web.rpc.WIndex;
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WPhase;
 import org.mule.galaxy.web.rpc.WProperty;
 import org.mule.galaxy.web.rpc.WPropertyDescriptor;
 import org.mule.galaxy.web.rpc.WSearchResults;
 import org.mule.galaxy.web.rpc.WUser;
 import org.mule.galaxy.web.rpc.WWorkspace;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.acegisecurity.context.SecurityContextHolder;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class RegistryServiceImpl implements RegistryService {
 
     protected static final String DEFAULT_DATETIME_FORMAT = "h:mm a, MMMM d, yyyy";
 
     private final Log log = LogFactory.getLog(getClass());
 
     private Registry registry;
     private ArtifactTypeDao artifactTypeDao;
     private RendererManager rendererManager;
     private PolicyManager policyManager;
     private LifecycleManager lifecycleManager;
     private IndexManager indexManager;
     private ActivityManager activityManager;
     private CommentManager commentManager;
     private AccessControlManager accessControlManager;
 
     private ContextPathResolver contextPathResolver;
 
     @SuppressWarnings("unchecked")
     public Collection getWorkspaces() throws RPCException {
         try {
             Collection<Workspace> workspaces = registry.getWorkspaces();
             List wis = new ArrayList();
 
             for (Workspace w : workspaces) {
                 WWorkspace ww = toWeb(w);
                 wis.add(ww);
             }
             return wis;
         } catch (RegistryException e) {
             log.error( e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private WWorkspace toWeb(Workspace w) {
         WWorkspace ww = new WWorkspace(w.getId(), w.getName(), w.getPath());
         ww.setDefaultLifecycleId(w.getDefaultLifecycle().getId());
 
         Collection<Workspace> children = w.getWorkspaces();
         if (children != null && children.size() > 0) {
             ww.setWorkspaces(new ArrayList());
             addWorkspaces(ww, children);
         }
         return ww;
     }
 
     @SuppressWarnings("unchecked")
     private void addWorkspaces(WWorkspace parent, Collection<Workspace> workspaces) {
         for (Workspace w : workspaces) {
             WWorkspace ww = new WWorkspace(w.getId(), w.getName(), w.getPath());
             parent.getWorkspaces().add(ww);
 
             Collection<Workspace> children = w.getWorkspaces();
             if (children != null && children.size() > 0) {
                 ww.setWorkspaces(new ArrayList());
                 addWorkspaces(ww, children);
             }
         }
     }
 
     public void addWorkspace(String parentWorkspaceId, String workspaceName, String lifecycleId) throws RPCException, ItemNotFoundException, ItemExistsException {
         try {
             Workspace w;
             if (parentWorkspaceId == null || "[No parent]".equals(parentWorkspaceId)) {
                 w = registry.createWorkspace(workspaceName);
             } else {
                 Workspace parent = registry.getWorkspace(parentWorkspaceId);
                 w = registry.createWorkspace(parent, workspaceName);
             }
             if (lifecycleId != null) {
                 w.setDefaultLifecycle(lifecycleManager.getLifecycleById(lifecycleId));
                 registry.save(w);
             }
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         }  catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             log.error(e.getMessage(), e);
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void updateWorkspace(String workspaceId, String parentWorkspaceId, String workspaceName, String lifecycleId)
         throws RPCException, ItemNotFoundException {
         try {
             if (parentWorkspaceId == null || "[No parent]".equals(parentWorkspaceId)) {
                 parentWorkspaceId = null;
             }
             Workspace w = registry.getWorkspace(workspaceId);
             if (lifecycleId != null) {
                 w.setDefaultLifecycle(lifecycleManager.getLifecycleById(lifecycleId));
             }
             w.setName(workspaceName);
             registry.save(w, parentWorkspaceId);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void deleteWorkspace(String workspaceId) throws RPCException, ItemNotFoundException {
         try {
             registry.deleteWorkspace(workspaceId);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     @SuppressWarnings("unchecked")
     public Collection getArtifactTypes() {
         Collection<ArtifactType> artifactTypes = artifactTypeDao.listAll();
         List atis = new ArrayList();
 
         for (ArtifactType a : artifactTypes) {
             WArtifactType at = toWeb(a);
             atis.add(at);
         }
         return atis;
     }
 
     public WArtifactType getArtifactType(String id) throws RPCException {
         try {
             return toWeb(artifactTypeDao.get(id));
         } catch (Exception e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private WArtifactType toWeb(ArtifactType a) {
         Set<QName> docTypes = a.getDocumentTypes();
         List<String> docTypesAsStr = new ArrayList<String>();
         if (docTypes != null) {
             for (QName q : docTypes) {
                 docTypesAsStr.add(q.toString());
             }
         }
         return new WArtifactType(a.getId(), a.getContentType(), a.getDescription(), docTypesAsStr);
     }
 
     public void deleteArtifactType(String id) throws RPCException {
         try {
             artifactTypeDao.delete(id);
         } catch (RuntimeException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         }
     }
 
     public void saveArtifactType(WArtifactType artifactType) throws RPCException, ItemExistsException {
         try {
             ArtifactType at = fromWeb(artifactType);
             artifactTypeDao.save(at);
         } catch (RuntimeException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private ArtifactType fromWeb(WArtifactType wat) {
         ArtifactType at = new ArtifactType();
         at.setId(wat.getId());
         at.setDescription(wat.getDescription());
         at.setContentType(wat.getMediaType());
         at.setDocumentTypes(fromWeb(wat.getDocumentTypes()));
         return at;
     }
 
     private Set<QName> fromWeb(Collection documentTypes) {
         if (documentTypes == null) return null;
 
         Set<QName> s = new HashSet<QName>();
         for (Object o : documentTypes) {
             String qn = o.toString();
             if (qn.startsWith("{}")) {
                 qn = qn.substring(2);
             }
 
             s.add(QName.valueOf(qn));
         }
         return s;
     }
 
     private OpRestriction getRestrictionForPredicate(SearchPredicate pred) {
         String property = pred.getProperty();
         String value = pred.getValue();
         switch (pred.getMatchType()) {
         case SearchPredicate.HAS_VALUE:
             return OpRestriction.eq(property, value);
         case SearchPredicate.LIKE:
             return OpRestriction.like(property, value);
         case SearchPredicate.DOES_NOT_HAVE_VALUE:
             return OpRestriction.not(OpRestriction.eq(property, value));
         default:
             return null;
         }
     }
 
     @SuppressWarnings("unchecked")
     public WSearchResults getArtifacts(String workspaceId, Set artifactTypes, Set searchPredicates,
                                        String freeformQuery, int start, int maxResults) throws RPCException {
        Query q = new Query(Artifact.class).workspaceId(workspaceId).orderBy("artifactType");
         q.setMaxResults(maxResults);
         q.setStart(start);
         // Filter based on our search terms
         for (Object predObj : searchPredicates) {
             SearchPredicate pred = (SearchPredicate)predObj;
             q.add(getRestrictionForPredicate(pred));
         }
 
         try {
             SearchResults results;
             if (freeformQuery != null && !freeformQuery.equals(""))
                 results = registry.search(freeformQuery, start, maxResults);
             else
                 results = registry.search(q);
 
             Map<String, ArtifactGroup> name2group = new HashMap<String, ArtifactGroup>();
             Map<String, ArtifactRenderer> name2view = new HashMap<String, ArtifactRenderer>();
 
             for (Object o : results.getResults()) {
                 Artifact a = (Artifact)o;
                 ArtifactType type = artifactTypeDao.getArtifactType(a.getContentType().toString(), a
                     .getDocumentType());
 
                 // If we want to filter based on the artifact type, filter!
                 if (artifactTypes != null && artifactTypes.size() != 0
                     && !artifactTypes.contains(type.getId())) {
                     continue;
                 }
 
                 ArtifactGroup g = name2group.get(type.getDescription());
                 ArtifactRenderer view = name2view.get(type.getDescription());
 
                 if (g == null) {
                     g = new ArtifactGroup();
                     g.setName(type.getDescription());
                     name2group.put(type.getDescription(), g);
 
                     view = rendererManager.getArtifactRenderer(a.getDocumentType());
                     if (view == null) {
                         view = rendererManager.getArtifactRenderer(a.getContentType().toString());
                     }
                     name2view.put(type.getDescription(), view);
 
                     int i = 0;
                     for (String col : view.getColumnNames()) {
                         if (view.isSummary(i)) {
                             g.getColumns().add(col);
                         }
                         i++;
                     }
                 }
 
                 BasicArtifactInfo info = createBasicArtifactInfo(a, view);
 
                 g.getRows().add(info);
             }
 
             List values = new ArrayList();
             List<String> keys = new ArrayList<String>();
             keys.addAll(name2group.keySet());
             Collections.sort(keys);
 
             for (String key : keys) {
                 values.add(name2group.get(key));
             }
 
             WSearchResults wsr = new WSearchResults();
             wsr.setResults(values);
             wsr.setTotal(results.getTotal());
             return wsr;
         } catch (QueryException e) {
             throw new RPCException(e.getMessage());
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         }
     }
 
     private BasicArtifactInfo createBasicArtifactInfo(Artifact a, ArtifactRenderer view) {
         BasicArtifactInfo info = new BasicArtifactInfo();
         return createBasicArtifactInfo(a, view, info, false);
     }
 
     private BasicArtifactInfo createBasicArtifactInfo(Artifact a, ArtifactRenderer view,
                                                       BasicArtifactInfo info, boolean extended) {
         info.setId(a.getId());
         info.setWorkspaceId(a.getParent().getId());
         info.setName(a.getName());
         info.setPath(a.getParent().getPath());
         int column = 0;
         for (int i = 0; i < view.getColumnNames().length; i++) {
             if (!extended && view.isSummary(i)) {
                 info.setColumn(column, view.getColumnValue(a, i));
                 column++;
             } else if (extended && view.isDetail(i)) {
                 info.setColumn(column, view.getColumnValue(a, i));
                 column++;
             }
         }
         return info;
     }
 
     @SuppressWarnings("unchecked")
     public Collection getIndexes() {
         ArrayList<WIndex> windexes = new ArrayList<WIndex>();
 
         Collection<Index> indices = indexManager.getIndexes();
         for (Index idx : indices) {
             windexes.add(toWeb(idx));
         }
 
         return windexes;
     }
 
     private WIndex toWeb(Index idx) {
 
         ArrayList<String> docTypes = new ArrayList<String>();
         if (idx.getDocumentTypes() != null) {
             for (QName q : idx.getDocumentTypes()) {
                 docTypes.add(q.toString());
             }
         }
         String qt;
         if (String.class.equals(idx.getQueryType())) {
             qt = "String";
         } else {
             qt = "QName";
         }
 
         return new WIndex(idx.getId(), 
                           idx.getDescription(), 
                           idx.getMediaType(),
                           idx.getConfiguration().get("property"),
                           idx.getConfiguration().get("expression"), 
                           idx.getIndexer(), 
                           qt,
                           docTypes);
     }
 
     public WIndex getIndex(String id) throws RPCException {
         try {
             Index idx = indexManager.getIndex(id);
             if (idx == null) {
                 return null;
             }
             return toWeb(idx);
         } catch (Exception e) {
             throw new RPCException("Could not find index " + id);
         }
     }
 
     public void saveIndex(WIndex wi) throws RPCException {
         try {
             Index idx = fromWeb(wi);
 
             indexManager.save(idx);
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new RPCException("Couldn't save index.");
         }
     }
 
     private Index fromWeb(WIndex wi) throws RPCException {
         Index idx = new Index();
         idx.setId(wi.getId());
         idx.setConfiguration(new HashMap<String,String>());
         idx.setIndexer(wi.getIndexer());
         
         if (idx.getIndexer().contains("Groovy")) {
             idx.getConfiguration().put("script", wi.getExpression());
         } else {
             idx.getConfiguration().put("property", wi.getProperty());
             idx.getConfiguration().put("expression", wi.getExpression());
         }
         idx.setIndexer(wi.getIndexer());
         idx.setDescription(wi.getDescription());
         idx.setMediaType(wi.getMediaType());
         
         if (wi.getResultType().equals("String")) {
             idx.setQueryType(String.class);
         } else {
             idx.setQueryType(QName.class);
         }
 
         Set<QName> docTypes = new HashSet<QName>();
         for (Object o : wi.getDocumentTypes()) {
             try {
                 docTypes.add(QName.valueOf(o.toString()));
             } catch (IllegalArgumentException e) {
                 throw new RPCException("QName was formatted incorrectly: " + o.toString());
             }
         }
         idx.setDocumentTypes(docTypes);
         return idx;
     }
 
     @SuppressWarnings("unchecked")
     public Collection getDependencyInfo(String artifactId) throws RPCException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
             List deps = new ArrayList();
             ArtifactVersion latest = artifact.getDefaultVersion();
             for (Dependency d : latest.getDependencies()) {
                 Artifact depArt = d.getArtifact();
                 deps.add(new DependencyInfo(d.isUserSpecified(), true, depArt.getName(), depArt.getId()));
             }
 
             for (Dependency d : registry.getDependedOnBy(artifact)) {
                 Artifact depArt = d.getArtifact();
                 deps.add(new DependencyInfo(d.isUserSpecified(), false, depArt.getName(), depArt.getId()));
             }
 
             return deps;
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new RPCException("Could not find artifact " + artifactId);
         }
 
     }
 
     @SuppressWarnings("unchecked")
     public ArtifactGroup getArtifact(String artifactId) throws RPCException, ItemNotFoundException {
         try {
             Artifact a = registry.getArtifact(artifactId);
             ArtifactType type = artifactTypeDao.getArtifactType(a.getContentType().toString(), a
                 .getDocumentType());
 
             ArtifactGroup g = new ArtifactGroup();
             g.setName(type.getDescription());
             ArtifactRenderer view = rendererManager.getArtifactRenderer(a.getDocumentType());
             if (view == null) {
                 view = rendererManager.getArtifactRenderer(a.getContentType().toString());
             }
 
             for (int i = 0; i < view.getColumnNames().length; i++) {
                 if (view.isDetail(i)) {
                     g.getColumns().add(view.getColumnNames()[i]);
                 }
             }
 
             ExtendedArtifactInfo info = new ExtendedArtifactInfo();
             createBasicArtifactInfo(a, view, info, true);
 
             info.setDescription(a.getDescription());
 
             List wcs = info.getComments();
 
             List<Comment> comments = commentManager.getComments(a.getId());
             for (Comment c : comments) {
                 final SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
                 WComment wc = new WComment(c.getId(), c.getUser().getUsername(), dateFormat.format(c
                     .getDate().getTime()), c.getText());
                 wcs.add(wc);
 
                 Set<Comment> children = c.getComments();
                 if (children != null && children.size() > 0) {
                     addComments(wc, children);
                 }
             }
 
             g.getRows().add(info);
 
             final String context = contextPathResolver.getContextPath();
 
             info.setArtifactLink(getLink(context + "/api/registry", a));
             info.setArtifactFeedLink(getLink(context + "/api/registry", a) + ";history");
             info.setCommentsFeedLink(context + "/api/comments");
 
             List versions = new ArrayList();
             for (ArtifactVersion av : a.getVersions()) {
                 versions.add(toWeb(av, false));
             }
             info.setVersions(versions);
 
             return g;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public ArtifactVersionInfo getArtifactVersionInfo(String artifactVersionId, boolean showHidden) throws RPCException,
         ItemNotFoundException {
         try {
             ArtifactVersion av = registry.getArtifactVersion(artifactVersionId);
             
             return toWeb(av, showHidden);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     @SuppressWarnings("unchecked")
     private ArtifactVersionInfo toWeb(ArtifactVersion av, boolean showHidden) {
         ArtifactVersionInfo vi = new ArtifactVersionInfo(av.getId(),
                                                          av.getVersionLabel(),
                                                          getVersionLink(av),
                                                          av.getCreated().getTime(),
                                                          av.isDefault(),
                                                          av.isEnabled(),
                                                          av.getAuthor().getName(),
                                                          av.getAuthor().getUsername(),
                                                          av.getPhase().getName(),
                                                          av.isIndexedPropertiesStale());
         for (Iterator<PropertyInfo> props = av.getProperties(); props.hasNext();) {
             PropertyInfo p = props.next();
 
             if (!showHidden && !p.isVisible()) {
                 continue;
             }
             
             Object val = p.getValue();
             if (val instanceof Collection) {
                 String s = val.toString();
                 val = s.substring(1, s.length() - 1);
             } else if (val != null) {
                 val = val.toString();
             } else {
                 val = "";
             }
 
             String desc = p.getDescription();
             if (desc == null) {
                 desc = p.getName();
             }
             vi.getProperties().add(new WProperty(p.getName(), desc, val.toString(), p.isLocked()));
         }
 
         Collections.sort(vi.getProperties(), new Comparator() {
 
             public int compare(Object o1, Object o2) {
                 return ((WProperty)o1).getDescription().compareTo(((WProperty)o2).getDescription());
             }
 
         });
         
         return vi;
     }
 
     private String getLink(String base, Artifact a) {
         StringBuilder sb = new StringBuilder();
         Workspace w = a.getParent();
 
         sb.append(base).append(w.getPath()).append(a.getName());
         return sb.toString();
     }
 
     public WComment addComment(String artifactId, String parentComment, String text) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
 
             Comment comment = new Comment();
             comment.setText(text);
 
             Calendar cal = Calendar.getInstance();
             cal.setTime(new Date());
             comment.setDate(cal);
 
             comment.setUser(getCurrentUser());
 
             if (parentComment != null) {
                 Comment c = commentManager.getComment(parentComment);
                 if (c == null) {
                     throw new RPCException("Invalid parent comment");
                 }
                 comment.setParent(c);
             } else {
                 comment.setArtifact(artifact);
             }
             commentManager.addComment(comment);
 
             SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
             return new WComment(comment.getId(), comment.getUser().getUsername(), dateFormat.format(comment
                 .getDate().getTime()), comment.getText());
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private User getCurrentUser() throws RPCException {
         UserDetailsWrapper wrapper = (UserDetailsWrapper)SecurityContextHolder.getContext()
             .getAuthentication().getPrincipal();
         if (wrapper == null) {
             throw new RPCException("No user is logged in!");
         }
         return wrapper.getUser();
     }
 
     @SuppressWarnings("unchecked")
     private void addComments(WComment parent, Set<Comment> comments) {
         for (Comment c : comments) {
             SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
             WComment child = new WComment(c.getId(), c.getUser().getUsername(), dateFormat.format(c.getDate()
                 .getTime()), c.getText());
             parent.getComments().add(child);
 
             Set<Comment> children = c.getComments();
             if (children != null && children.size() > 0) {
                 addComments(child, children);
             }
         }
     }
 
     public void newPropertyDescriptor(String name, String description, boolean multivalued)
         throws RPCException, ItemExistsException {
         if (name.contains(" ")) {
             throw new RPCException("The property name cannot contain a space.");
         }
 
         PropertyDescriptor pd = new PropertyDescriptor(name, description, multivalued);
 
         try {
             registry.savePropertyDescriptor(pd);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void setProperty(String artifactId, String propertyName, String propertyValue) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
 
             artifact.setProperty(propertyName, propertyValue);
             registry.save(artifact);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (PropertyException e) {
             // occurs if property name is formatted wrong
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void deleteProperty(String artifactId, String propertyName) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
             artifact.setProperty(propertyName, null);
             registry.save(artifact);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (PropertyException e) {
             // occurs if property name is formatted wrong
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public Map getPropertyList() throws RPCException {
         try {
             Collection<PropertyDescriptor> pds = registry.getPropertyDescriptors();
 
             HashMap<Object, Object> props = new HashMap<Object, Object>();
             for (PropertyDescriptor pd : pds) {
                 props.put(pd.getDescription(), pd.getProperty());
             }
 
             return props;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         }
     }
 
     public Map getProperties() throws RPCException {
         try {
             Collection<PropertyDescriptor> pds = registry.getPropertyDescriptors();
 
             HashMap<Object, Object> props = new HashMap<Object, Object>();
             for (PropertyDescriptor pd : pds) {
                 props.put(pd.getProperty(), pd.getDescription());
             }
 
             return props;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         }
     }
 
     public void deletePropertyDescriptor(String id) throws RPCException {
         try {
             registry.deletePropertyDescriptor(id);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         }
     }
 
     public Collection getPropertyDescriptors() throws RPCException {
         try {
             List<WPropertyDescriptor> pds = new ArrayList<WPropertyDescriptor>();
             for (PropertyDescriptor pd : registry.getPropertyDescriptors()) {
                 pds.add(toWeb(pd));
             }
             return pds;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         }
     }
 
     private WPropertyDescriptor toWeb(PropertyDescriptor pd) {
         return new WPropertyDescriptor(pd.getId(), pd.getProperty(), pd.getDescription(), pd.isMultivalued());
     }
 
     public void savePropertyDescriptor(WPropertyDescriptor wpd) throws RPCException, ItemNotFoundException, ItemExistsException {
         try {
             PropertyDescriptor pd;
             
             if (wpd.getId() == null) {
                 pd = new PropertyDescriptor();
             } else {
                 pd = registry.getPropertyDescriptor(wpd.getId());
             }
             
             pd.setProperty(wpd.getName());
             pd.setDescription(wpd.getDescription());
             pd.setMultivalued(wpd.isMultiValued());
             
             registry.savePropertyDescriptor(pd);
             
             wpd.setId(pd.getId());
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void setDescription(String artifactId, String description) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
 
             artifact.setDescription(description);
 
             registry.save(artifact);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void move(String artifactId, String workspaceId, String name) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
 
             artifact.setName(name);
             registry.save(artifact);
 
             registry.move(artifact, workspaceId);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void delete(String artifactId) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(artifactId);
 
             registry.delete(artifact);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public WGovernanceInfo getGovernanceInfo(String artifactVersionId) throws RPCException, ItemNotFoundException {
         try {
             ArtifactVersion artifact = registry.getArtifactVersion(artifactVersionId);
 
             WGovernanceInfo gov = new WGovernanceInfo();
             Phase phase = artifact.getPhase();
             gov.setCurrentPhase(phase.getName());
             gov.setLifecycle(phase.getLifecycle().getName());
 
             Set<Phase> nextPhases = phase.getNextPhases();
             List<WPhase> wNextPhases = new ArrayList<WPhase>();
             for (Phase p : nextPhases) {
                 wNextPhases.add(toWeb(p));
             }
             gov.setNextPhases(wNextPhases);
 
             // Collection<PolicyInfo> policies =
             // policyManager.getActivePolicies(artifact, false);
 
             return gov;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public TransitionResponse transition(String artifactVersionId, String nextPhaseId) throws RPCException, ItemNotFoundException {
         try {
             ArtifactVersion artifact = registry.getArtifactVersion(artifactVersionId);
 
             Phase nextPhase = lifecycleManager.getPhaseById(nextPhaseId);
 
             TransitionResponse tr = new TransitionResponse();
 
             try {
                 lifecycleManager.transition(artifact, nextPhase, getCurrentUser());
 
                 tr.setSuccess(true);
             } catch (TransitionException e) {
                 tr.setSuccess(false);
                 tr.addMessage("Phase " + nextPhase.getName() + " isn't a valid next phase!", false);
             } catch (ArtifactPolicyException e) {
                 tr.setSuccess(false);
                 for (ApprovalMessage app : e.getApprovals()) {
                     tr.addMessage(app.getMessage(), app.isWarning());
                 }
             }
 
             return tr;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     @SuppressWarnings("unchecked")
     public Collection getLifecycles() throws RPCException {
         Collection<Lifecycle> lifecycles = lifecycleManager.getLifecycles();
         Lifecycle defaultLifecycle = lifecycleManager.getDefaultLifecycle();
         
         ArrayList<WLifecycle> wls = new ArrayList<WLifecycle>();
         for (Lifecycle l : lifecycles) {
             WLifecycle lifecycle = toWeb(l, defaultLifecycle.equals(l));
             wls.add(lifecycle);
         }
 
         return wls;
     }
     
     public WLifecycle getLifecycle(String id) throws RPCException {
         try {
             Lifecycle defaultLifecycle = lifecycleManager.getDefaultLifecycle();
             
             Lifecycle l = lifecycleManager.getLifecycleById(id);
             
             return toWeb(l, defaultLifecycle.equals(l));
         } catch (Exception e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private WLifecycle toWeb(Lifecycle l, boolean defaultLifecycle) {
         WLifecycle lifecycle = new WLifecycle(l.getId(), l.getName(), defaultLifecycle);
 
         List<WPhase> wphases = new ArrayList<WPhase>();
         lifecycle.setPhases(wphases);
 
         for (Phase p : l.getPhases().values()) {
             WPhase wp = toWeb(p);
             wphases.add(wp);
 
             if (p.equals(l.getInitialPhase())) {
                 lifecycle.setInitialPhase(wp);
             }
         }
 
         for (Phase p : l.getPhases().values()) {
             WPhase wp = lifecycle.getPhase(p.getName());
             List<WPhase> nextPhases = new ArrayList<WPhase>();
 
             for (Phase next : p.getNextPhases()) {
                 WPhase wnext = lifecycle.getPhase(next.getName());
 
                 nextPhases.add(wnext);
             }
             wp.setNextPhases(nextPhases);
         }
 
         Collections.sort(wphases, new Comparator<WPhase>() {
 
             public int compare(WPhase o1, WPhase o2) {
                 return o1.getName().compareTo(o2.getName());
             }
 
         });
         return lifecycle;
     }
 
     private WPhase toWeb(Phase p) {
         WPhase wp = new WPhase(p.getId(), p.getName());
         return wp;
     }
 
     public Collection getActivePoliciesForLifecycle(String lifecycleName, String workspaceId)
         throws RPCException {
         Collection<ArtifactPolicy> pols = null;
         Lifecycle lifecycle = lifecycleManager.getLifecycle(lifecycleName);
         try {
             if (workspaceId != null) {
                 Workspace w = registry.getWorkspace(workspaceId);
                 pols = policyManager.getActivePolicies(w, lifecycle);
             } else {
                 pols = policyManager.getActivePolicies(lifecycle);
             }
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
         return getArtifactPolicyIds(pols);
     }
 
     public Collection getActivePoliciesForPhase(String lifecycle, String phaseName, String workspaceId)
         throws RPCException {
         Collection<ArtifactPolicy> pols = null;
         Phase phase = lifecycleManager.getLifecycle(lifecycle).getPhase(phaseName);
         try {
             if (workspaceId != null) {
                 Workspace w = registry.getWorkspace(workspaceId);
                 pols = policyManager.getActivePolicies(w, phase);
             } else {
                 pols = policyManager.getActivePolicies(phase);
             }
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
 
         return getArtifactPolicyIds(pols);
     }
 
     public void setActivePolicies(String workspace, String lifecycle, String phase, Collection ids)
         throws RPCException, ApplyPolicyException, ItemNotFoundException {
         Lifecycle l = lifecycleManager.getLifecycle(lifecycle);
         List<ArtifactPolicy> policies = getArtifactPolicies(ids);
 
         try {
             if (phase != null) {
                 Phase p = l.getPhase(phase);
 
                 if (p == null) {
                     throw new RPCException("Invalid phase: " + phase);
                 }
 
                 List<Phase> phases = Arrays.asList(p);
 
                 if (workspace == null || "".equals(workspace)) {
                     policyManager.setActivePolicies(phases, policies.toArray(new ArtifactPolicy[policies
                         .size()]));
                 } else {
                     Workspace w = registry.getWorkspace(workspace);
                     policyManager.setActivePolicies(w, phases, policies.toArray(new ArtifactPolicy[policies
                         .size()]));
                 }
             } else {
                 if (workspace == null || "".equals(workspace)) {
                     policyManager.setActivePolicies(l, policies.toArray(new ArtifactPolicy[policies.size()]));
                 } else {
                     Workspace w = registry.getWorkspace(workspace);
                     policyManager.setActivePolicies(w, l, policies
                         .toArray(new ArtifactPolicy[policies.size()]));
                 }
             }
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (ArtifactCollectionPolicyException e) {
             Map<BasicArtifactInfo, Collection<WApprovalMessage>> failures = new HashMap<BasicArtifactInfo, Collection<WApprovalMessage>>();
             for (Map.Entry<ArtifactVersion, List<ApprovalMessage>> entry : e.getPolicyFailures().entrySet()) {
                 ArtifactVersion av = entry.getKey();
                 List<ApprovalMessage> approvals = entry.getValue();
 
                 Artifact a = av.getParent();
                 ArtifactRenderer view = rendererManager.getArtifactRenderer(a.getDocumentType());
                 if (view == null) {
                     view = rendererManager.getArtifactRenderer(a.getContentType().toString());
                 }
 
                 BasicArtifactInfo info = createBasicArtifactInfo(a, view);
 
                 ArrayList<WApprovalMessage> wapprovals = new ArrayList<WApprovalMessage>();
                 for (ApprovalMessage app : approvals) {
                     wapprovals.add(new WApprovalMessage(app.getMessage(), app.isWarning()));
                 }
 
                 failures.put(info, wapprovals);
             }
             throw new ApplyPolicyException(failures);
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private List<ArtifactPolicy> getArtifactPolicies(Collection ids) {
         List<ArtifactPolicy> policies = new ArrayList<ArtifactPolicy>();
         for (Iterator itr = ids.iterator(); itr.hasNext();) {
             String id = (String)itr.next();
 
             ArtifactPolicy policy = policyManager.getPolicy(id);
             policies.add(policy);
         }
         return policies;
     }
 
     private Collection getArtifactPolicyIds(Collection<ArtifactPolicy> pols) {
         ArrayList<String> polNames = new ArrayList<String>();
         for (ArtifactPolicy ap : pols) {
             polNames.add(ap.getId());
         }
         return polNames;
     }
 
     public TransitionResponse setDefault(String artifactVersionId) throws RPCException, ItemNotFoundException {
         try {
             ArtifactVersion artifact = registry.getArtifactVersion(artifactVersionId);
 
             TransitionResponse tr = new TransitionResponse();
 
             try {
                 registry.setDefaultVersion(artifact, getCurrentUser());
 
                 tr.setSuccess(true);
             } catch (ArtifactPolicyException e) {
                 tr.setSuccess(false);
                 for (ApprovalMessage app : e.getApprovals()) {
                     tr.addMessage(app.getMessage(), app.isWarning());
                 }
             }
 
             return tr;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public TransitionResponse setEnabled(String artifactVersionId, boolean enabled) throws RPCException,
         ItemNotFoundException {
         try {
             ArtifactVersion artifact = registry.getArtifactVersion(artifactVersionId);
 
             TransitionResponse tr = new TransitionResponse();
 
             try {
                 registry.setEnabled(artifact, enabled, getCurrentUser());
 
                 if (!enabled) {
                     return null;
                 }
 
                 tr.setSuccess(true);
             } catch (ArtifactPolicyException e) {
                 tr.setSuccess(false);
                 for (ApprovalMessage app : e.getApprovals()) {
                     tr.addMessage(app.getMessage(), app.isWarning());
                 }
             }
 
             return tr;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public Collection getPolicies() throws RPCException {
         Collection<ArtifactPolicy> policies = policyManager.getPolicies();
         List<WArtifactPolicy> gwtPolicies = new ArrayList<WArtifactPolicy>();
         for (ArtifactPolicy p : policies) {
             gwtPolicies.add(toWeb(p));
         }
         Collections.sort(gwtPolicies, new Comparator<WArtifactPolicy>() {
 
             public int compare(WArtifactPolicy o1, WArtifactPolicy o2) {
                 return o1.getName().compareTo(o2.getName());
             }
 
         });
 
         return gwtPolicies;
     }
 
     public void saveLifecycle(WLifecycle wl) throws RPCException, ItemExistsException {
         Lifecycle l = fromWeb(wl);
 
         try {
             lifecycleManager.save(l);
             
             if (wl.isDefaultLifecycle()) {
                 Lifecycle defaultLifecycle = lifecycleManager.getDefaultLifecycle();
                 
                 if (!defaultLifecycle.equals(l)) {
                     lifecycleManager.setDefaultLifecycle(l);
                 }
             }
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void deleteLifecycle(String id) throws RPCException {
         try {
             String fallback = lifecycleManager.getDefaultLifecycle().getId();
             
             if (id.equals(fallback)) {
                 throw new RPCException("The default lifecycle cannot be deleted. Please assign " +
                                        "another lifecycle to be the default before deleting this one.");
             }
             
             lifecycleManager.delete(id, fallback);
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private Lifecycle fromWeb(WLifecycle wl) throws RPCException {
         Lifecycle l = new Lifecycle();
         l.setPhases(new HashMap<String, Phase>());
         l.setName(wl.getName());
         l.setId(wl.getId());
 
         for (Object o : wl.getPhases()) {
             WPhase wp = (WPhase) o;
 
             Phase p = new Phase(l);
             p.setId(wp.getId());
             p.setName(wp.getName());
             l.getPhases().put(p.getName(), p);
         }
 
         for (Object o : wl.getPhases()) {
             WPhase wp = (WPhase) o;
             Phase p = l.getPhase(wp.getName());
             p.setNextPhases(new HashSet<Phase>());
 
             if (wp.getNextPhases() != null) {
                 for (Object oNext : wp.getNextPhases()) {
                     WPhase wNext = (WPhase) oNext;
                     Phase next = l.getPhase(wNext.getName());
 
                     p.getNextPhases().add(next);
                 }
             }
         }
 
         if (wl.getInitialPhase() == null) {
             throw new RPCException("You must set a phase as the initial phase.");
         }
 
         l.setInitialPhase(l.getPhase(wl.getInitialPhase().getName()));
         return l;
     }
 
     private WArtifactPolicy toWeb(ArtifactPolicy p) {
         WArtifactPolicy wap = new WArtifactPolicy();
         wap.setId(p.getId());
         wap.setDescription(p.getDescription());
         wap.setName(p.getName());
 
         return wap;
     }
 
     private String getVersionLink(ArtifactVersion av) {
         Artifact a = av.getParent();
         StringBuilder sb = new StringBuilder();
         Workspace w = a.getParent();
 
         final String context = contextPathResolver.getContextPath();
         sb.append(context).append("/api/registry").append(w.getPath()).append(a.getName()).append("?version=")
             .append(av.getVersionLabel());
         return sb.toString();
     }
 
     public Collection getActivities(Date from, Date to, String user, String eventTypeStr, int start,
                                     int results, boolean ascending) throws RPCException {
 
         if ("All".equals(user)) {
             user = null;
         }
 
         if (from != null) {
             Calendar c = Calendar.getInstance();
             c.setTime(from);
             c.set(Calendar.HOUR, 0);
             c.set(Calendar.MINUTE, 0);
             c.set(Calendar.SECOND, 0);
             from = c.getTime();
         }
 
         if (to != null) {
             Calendar c = Calendar.getInstance();
             c.setTime(to);
             c.set(Calendar.HOUR, 23);
             c.set(Calendar.MINUTE, 59);
             c.set(Calendar.SECOND, 59);
             to = c.getTime();
         }
 
         EventType eventType = null;
         if ("Info".equals(eventTypeStr)) {
             eventType = EventType.INFO;
         } else if ("Warning".equals(eventTypeStr)) {
             eventType = EventType.WARNING;
         } else if ("Error".equals(eventTypeStr)) {
             eventType = EventType.ERROR;
         }
 
         try {
             Collection<Activity> activities = activityManager.getActivities(from, to, user, eventType, start,
                                                                             results, ascending);
 
             ArrayList<WActivity> wactivities = new ArrayList<WActivity>();
 
             for (Activity a : activities) {
                 wactivities.add(createWActivity(a));
             }
             return wactivities;
         } catch (AccessException e){
             throw new RPCException(e.getMessage());
         }
     }
 
     protected WActivity createWActivity(Activity a) {
         WActivity wa = new WActivity();
         wa.setId(a.getId());
         wa.setEventType(a.getEventType().getText());
         if (a.getUser() != null) {
             wa.setUsername(a.getUser().getUsername());
             wa.setName(a.getUser().getName());
         }
         wa.setMessage(a.getMessage());
         SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATETIME_FORMAT);
         wa.setDate(dateFormat.format(a.getDate().getTime()));
         return wa;
     }
 
     public WUser getUserInfo() throws RPCException {
         User user = getCurrentUser();
         WUser w = SecurityServiceImpl.createWUser(user);
 
         List<String> perms = new ArrayList<String>();
 
         for (Permission p : accessControlManager.getGrantedPermissions(user)) {
             perms.add(p.toString());
         }
         w.setPermissions(perms);
 
         return w;
     }
 
     public void setAccessControlManager(AccessControlManager accessControlManager) {
         this.accessControlManager = accessControlManager;
     }
 
     public void setCommentManager(CommentManager commentManager) {
         this.commentManager = commentManager;
     }
 
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     public void setLifecycleManager(LifecycleManager lifecycleManager) {
         this.lifecycleManager = lifecycleManager;
     }
 
     public void setArtifactTypeDao(ArtifactTypeDao artifactTypeDao) {
         this.artifactTypeDao = artifactTypeDao;
     }
 
     public void setPolicyManager(PolicyManager policyManager) {
         this.policyManager = policyManager;
     }
 
     public void setRendererManager(RendererManager viewManager) {
         this.rendererManager = viewManager;
     }
 
     public void setActivityManager(ActivityManager activityManager) {
         this.activityManager = activityManager;
     }
 
     public void setIndexManager(IndexManager indexManager) {
         this.indexManager = indexManager;
     }
 
     public ContextPathResolver getContextPathResolver()
     {
         return contextPathResolver;
     }
 
     public void setContextPathResolver(final ContextPathResolver contextPathResolver)
     {
         this.contextPathResolver = contextPathResolver;
     }
 }
