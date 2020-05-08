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
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactType;
 import org.mule.galaxy.ArtifactTypeDao;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Entry;
 import org.mule.galaxy.EntryResult;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.Link;
 import org.mule.galaxy.Links;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.activity.Activity;
 import org.mule.galaxy.activity.ActivityManager;
 import org.mule.galaxy.activity.ActivityManager.EventType;
 import org.mule.galaxy.collab.Comment;
 import org.mule.galaxy.collab.CommentManager;
 import org.mule.galaxy.event.EventManager;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.impl.jcr.UserDetailsWrapper;
 import org.mule.galaxy.index.Index;
 import org.mule.galaxy.index.IndexManager;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.lifecycle.Phase;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.policy.Policy;
 import org.mule.galaxy.policy.PolicyException;
 import org.mule.galaxy.policy.PolicyManager;
 import org.mule.galaxy.query.OpRestriction;
 import org.mule.galaxy.query.Query;
 import org.mule.galaxy.query.QueryException;
 import org.mule.galaxy.query.Restriction;
 import org.mule.galaxy.query.SearchResults;
 import org.mule.galaxy.query.OpRestriction.Operator;
 import org.mule.galaxy.render.ItemRenderer;
 import org.mule.galaxy.render.RendererManager;
 import org.mule.galaxy.security.AccessControlManager;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.type.TypeManager;
 import org.mule.galaxy.view.View;
 import org.mule.galaxy.view.ArtifactViewManager;
 import org.mule.galaxy.web.client.RPCException;
 import org.mule.galaxy.web.rpc.EntryGroup;
 import org.mule.galaxy.web.rpc.EntryVersionInfo;
 import org.mule.galaxy.web.rpc.EntryInfo;
 import org.mule.galaxy.web.rpc.ExtendedEntryInfo;
 import org.mule.galaxy.web.rpc.ItemExistsException;
 import org.mule.galaxy.web.rpc.ItemNotFoundException;
 import org.mule.galaxy.web.rpc.LinkInfo;
 import org.mule.galaxy.web.rpc.RegistryService;
 import org.mule.galaxy.web.rpc.SearchPredicate;
 import org.mule.galaxy.web.rpc.WActivity;
 import org.mule.galaxy.web.rpc.WApprovalMessage;
 import org.mule.galaxy.web.rpc.WPolicy;
 import org.mule.galaxy.web.rpc.WArtifactType;
 import org.mule.galaxy.web.rpc.WArtifactView;
 import org.mule.galaxy.web.rpc.WComment;
 import org.mule.galaxy.web.rpc.WExtensionInfo;
 import org.mule.galaxy.web.rpc.WGovernanceInfo;
 import org.mule.galaxy.web.rpc.WIndex;
 import org.mule.galaxy.web.rpc.WLifecycle;
 import org.mule.galaxy.web.rpc.WPhase;
 import org.mule.galaxy.web.rpc.WPolicyException;
 import org.mule.galaxy.web.rpc.WProperty;
 import org.mule.galaxy.web.rpc.WPropertyDescriptor;
 import org.mule.galaxy.web.rpc.WSearchResults;
 import org.mule.galaxy.web.rpc.WUser;
 import org.mule.galaxy.web.rpc.WWorkspace;
 
 public class RegistryServiceImpl implements RegistryService {
 
     protected static final String DEFAULT_DATETIME_FORMAT = "h:mm a, MMMM d, yyyy";
 
     private static final String RECENT_VIEWS = "recent.artifactViews";
 
     private final Log log = LogFactory.getLog(getClass());
 
     private Registry registry;
     private ArtifactTypeDao artifactTypeDao;
     private RendererManager rendererManager;
     private PolicyManager policyManager;
     private IndexManager indexManager;
     private ActivityManager activityManager;
     private AccessControlManager accessControlManager;
     private ArtifactViewManager artifactViewManager;
     private TypeManager typeManager;
     private UserManager userManager;
 
     private ContextPathResolver contextPathResolver;
 
     private LifecycleManager localLifecycleManager;
 
     private EventManager eventManager;
 
     public List<WExtensionInfo> getExtensions() throws RPCException {
         ArrayList<WExtensionInfo> exts = new ArrayList<WExtensionInfo>();
         for (Extension e : registry.getExtensions()) {
             exts.add(new WExtensionInfo(e.getId(), e.getName(), e.getPropertyDescriptorConfigurationKeys(), e.isMultivalueSupported()));
         }
         return exts;
     }
 
     public Collection<WWorkspace> getWorkspaces() throws RPCException {
         try {
             Collection<Workspace> workspaces = registry.getWorkspaces();
             List<WWorkspace> wis = new ArrayList<WWorkspace>();
 
             for (Workspace w : workspaces) {
                 WWorkspace ww = toWeb(w);
                 wis.add(ww);
             }
             return wis;
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
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
             ww.setWorkspaces(new ArrayList<WWorkspace>());
             addWorkspaces(ww, children);
         }
         return ww;
     }
 
     private void addWorkspaces(WWorkspace parent, Collection<Workspace> workspaces) {
         for (Workspace w : workspaces) {
             WWorkspace ww = new WWorkspace(w.getId(), w.getName(), w.getPath());
             parent.getWorkspaces().add(ww);
 
             Collection<Workspace> children = w.getWorkspaces();
             if (children != null && children.size() > 0) {
                 ww.setWorkspaces(new ArrayList<WWorkspace>());
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
                 Workspace parent = (Workspace) registry.getItemById(parentWorkspaceId);
                 w = registry.createWorkspace(parent, workspaceName);
             }
             if (lifecycleId != null) {
                 w.setDefaultLifecycle(w.getLifecycleManager().getLifecycleById(lifecycleId));
                 registry.save(w);
             }
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (RegistryException e) {
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
             Workspace w = (Workspace) registry.getItemById(workspaceId);
             if (lifecycleId != null) {
                 w.setDefaultLifecycle(w.getLifecycleManager().getLifecycleById(lifecycleId));
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
             registry.getItemById(workspaceId).delete();
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public String newEntry(String workspaceId, String name, String version) 
         throws RPCException, ItemExistsException, WPolicyException, ItemNotFoundException {
         try {
             Workspace w = (Workspace) registry.getItemById(workspaceId);
             
             EntryResult result = w.newEntry(name, version);
             
             return result.getEntry().getId();
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (PolicyException e) {
             throw toWeb(e);
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         }
     }
 
     public String newEntryVersion(String entryId, String version) 
         throws RPCException, ItemExistsException, WPolicyException, ItemNotFoundException {
         try {
             Entry e = (Entry) registry.getItemById(entryId);
             
             EntryResult result = e.newVersion(version);
             
             return result.getEntryVersion().getId();
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (PolicyException e) {
             throw toWeb(e);
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         }
     }
     
     public Collection<WArtifactType> getArtifactTypes() {
         Collection<ArtifactType> artifactTypes = artifactTypeDao.listAll();
         List<WArtifactType> atis = new ArrayList<WArtifactType>();
 
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
         return new WArtifactType(a.getId(), a.getContentType(),
                                  a.getDescription(), docTypesAsStr,
                                  a.getFileExtensions());
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
 
         HashSet<String> exts = new HashSet<String>();
         exts.addAll((Collection<String>) wat.getFileExtensions());
         at.setFileExtensions(exts);
 
         return at;
     }
 
     private Set<QName> fromWeb(Collection<String> documentTypes) {
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
 
     public WSearchResults getArtifacts(String workspaceId, String workspacePath, boolean includeChildWkspcs,
                                        Set<String> artifactTypes, Set<SearchPredicate> searchPredicates, String freeformQuery,
                                        int start, int maxResults) throws RPCException {
         Query q = getQuery(searchPredicates, start, maxResults);
 
         if (workspaceId != null) {
             q.workspaceId(workspaceId, includeChildWkspcs);
         } else if (workspacePath != null && !"".equals(workspacePath) && !"/".equals(workspacePath)) {
             q.workspacePath(workspacePath, includeChildWkspcs);
         }
         try {
             SearchResults results;
             if (freeformQuery != null && !freeformQuery.equals(""))
                 results = registry.search(freeformQuery, start, maxResults);
             else
                 results = registry.search(q);
 
             return getSearchResults(artifactTypes, results);
         } catch (QueryException e) {
             throw new RPCException(e.getMessage());
         } catch (RegistryException e) {
             log.error("Could not query the registry.", e);
             throw new RPCException(e.getMessage());
         }
     }
 
     private Query getQuery(Set<SearchPredicate> searchPredicates, int start, int maxResults) {
         Query q = new Query().orderBy("artifactType");
 
         q.setMaxResults(maxResults);
         q.setStart(start);
         // Filter based on our search terms
 
         if (searchPredicates != null) {
             for (Object predObj : searchPredicates) {
                 SearchPredicate pred = (SearchPredicate) predObj;
                 q.add(getRestrictionForPredicate(pred));
             }
         }
         return q;
     }
 
     private WSearchResults getSearchResults(Set<String> artifactTypes, SearchResults results) {
         Map<String, EntryGroup> name2group = new HashMap<String, EntryGroup>();
         Map<String, ItemRenderer> name2view = new HashMap<String, ItemRenderer>();
 
         int total = 0;
 
         for (Object obj : results.getResults()) {
             Entry e = (Entry) obj;
             String groupName;
             ArtifactType type = null;
             if (e instanceof Artifact) {
                 type = artifactTypeDao.getArtifactType(
                     ((Artifact) e).getContentType().toString(), 
                     ((Artifact) e).getDocumentType());
 
                 groupName = type.getDescription();
             } else {
                 groupName = "Entries";
             }
             // If we want to filter based on the artifact type, filter!
             if (artifactTypes != null && artifactTypes.size() != 0
                    && (type == null
                    || !artifactTypes.contains(type.getId()))) {
                 continue;
             }
 
             total++;
 
             EntryGroup g = name2group.get(groupName);
             ItemRenderer view = name2view.get(groupName);
 
             if (g == null) {
                 g = new EntryGroup();
                 g.setName(groupName);
                 name2group.put(groupName, g);
 
                 if (e instanceof Artifact) {
                     Artifact a = (Artifact) e;
                     view = rendererManager.getArtifactRenderer(a.getDocumentType());
                     if (view == null) {
                         view = rendererManager.getArtifactRenderer(a.getContentType().toString());
                     }
                 } else {
                     // Hack
                     view = rendererManager.getArtifactRenderer("application/octet-stream");
                 }
                 name2view.put(groupName, view);
 
                 int i = 0;
                 for (String col : view.getColumnNames()) {
                     if (view.isSummary(i)) {
                         g.getColumns().add(col);
                     }
                     i++;
                 }
             }
 
             EntryInfo info = createBasicEntryInfo(e, view);
 
             g.getRows().add(info);
         }
 
         List<EntryGroup> values = new ArrayList<EntryGroup>();
         List<String> keys = new ArrayList<String>();
         keys.addAll(name2group.keySet());
         Collections.sort(keys);
 
         for (String key : keys) {
             values.add(name2group.get(key));
         }
 
         WSearchResults wsr = new WSearchResults();
         wsr.setResults(values);
         wsr.setTotal(total);
         return wsr;
     }
 
     private EntryInfo createBasicEntryInfo(Entry a, ItemRenderer view) {
         EntryInfo info = new EntryInfo();
         return createBasicEntryInfo(a, view, info, false);
     }
 
     private EntryInfo createBasicEntryInfo(Entry a, 
                                               ItemRenderer view,
                                               EntryInfo info, 
                                               boolean extended) {
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
 
     public WSearchResults getArtifactsForView(String viewId,
                                               int resultStart,
                                               int maxResults)
             throws RPCException {
         View view = artifactViewManager.getArtifactView(viewId);
 
         try {
             return getSearchResults(null, registry.search(view.getQuery(), resultStart, maxResults));
         } catch (QueryException e) {
             throw new RPCException(e.getMessage());
         } catch (RegistryException e) {
             log.error("Could not query the registry.", e);
             throw new RPCException(e.getMessage());
         }
     }
 
     @SuppressWarnings("unchecked")
     public void deleteArtifactView(String id) throws RPCException {
         artifactViewManager.delete(id);
         // clean up recent views too
         User user = getCurrentUser();
         List<String> views = (List<String>) user.getProperties().get(RECENT_VIEWS);
         boolean wasFound = views.remove(id);
 
         assert wasFound : "View deleted, but no corresponding Recent Views entry found for " + id;
     }
 
     public WArtifactView getArtifactView(String id) throws RPCException, ItemExistsException, ItemNotFoundException {
         User user = getCurrentUser();
         WArtifactView view = toWeb(artifactViewManager.getArtifactView(id));
         try {
             updateRecentArtifactViews(user, id);
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (NotFoundException e) {
             log.error(e.getMessage(), e);
             throw new ItemNotFoundException();
         }
 
         return view;
     }
 
     private void updateRecentArtifactViews(User user, String id) throws DuplicateItemException, NotFoundException {
         List<String> recent = getRecentArtifactViewIds(user);
 
         // remove this id if it alread exists
         recent.remove(id);
 
         // add the view to the top of the list
         recent.add(0, id);
 
         while (recent.size() > 5) {
             recent.remove(recent.size() - 1);
         }
 
         userManager.save(user);
     }
 
     @SuppressWarnings("unchecked")
     private List<String> getRecentArtifactViewIds(User user) {
         if (user.getProperties() == null) {
             user.setProperties(new HashMap<String, Object>());
         }
         List<String> recent = (List<String>) user.getProperties().get(RECENT_VIEWS);
 
         if (recent == null) {
             recent = new ArrayList<String>();
             user.getProperties().put(RECENT_VIEWS, recent);
         }
         return recent;
     }
 
     public Collection<WArtifactView> getArtifactViews() throws RPCException {
         List<WArtifactView> views = new ArrayList<WArtifactView>();
         User currentUser = getCurrentUser();
         for (View v : artifactViewManager.getArtifactViews(currentUser)) {
             views.add(toWeb(v));
         }
 
         Collections.sort(views, new Comparator<WArtifactView>() {
             public int compare(WArtifactView v1, WArtifactView v2) {
                 return v1.getName().compareTo(v2.getName());
             }
         });
         return views;
     }
 
     public Collection<WArtifactView> getRecentArtifactViews() throws RPCException {
         List<WArtifactView> views = new ArrayList<WArtifactView>();
         User currentUser = getCurrentUser();
         List<String> ids = getRecentArtifactViewIds(currentUser);
         if (ids != null) {
             for (String id : ids) {
                 views.add(toWeb(artifactViewManager.getArtifactView(id)));
             }
         }
         return views;
     }
 
     private WArtifactView toWeb(View v) throws RPCException {
         WArtifactView wv = new WArtifactView();
         if (v == null) {
             return wv;
         }
         wv.setName(v.getName());
         wv.setId(v.getId());
 
         try {
             Query q = Query.fromString(v.getQuery());
 
 
             wv.setPredicates(getPredicates(q));
             wv.setShared(v.getUser() == null);
             wv.setWorkspace(q.getWorkspacePath());
             wv.setWorkspaceSearchRecursive(q.isWorkspaceSearchRecursive());
         } catch (QueryException e) {
             log.error("Could not parse query. " + e.getMessage(), e);
             throw new RPCException(e.getMessage());
         }
         return wv;
     }
 
     /**
      * Convert a string query to a set of search predicates for the SearchForm.
      * You'll see that we do not have full fidelity yet between text queries and
      * the actual form. However, that is not a problem as we'll only ever encounter
      * queries which were created with the form. So there are some cases here
      * that we don't have to worry about.
      *
      * @param q
      * @return
      * @throws RPCException
      */
     public Set<SearchPredicate> getPredicates(Query q) throws RPCException {
         Set<SearchPredicate> predicates = new HashSet<SearchPredicate>();
 
         for (Restriction r : q.getRestrictions()) {
             if (r instanceof OpRestriction) {
                 OpRestriction op = (OpRestriction) r;
 
                 Object left = op.getLeft();
                 Object right = op.getRight();
                 Operator operator = op.getOperator();
 
                 if (operator.equals(Operator.NOT)) {
                     if (right instanceof OpRestriction) {
                         OpRestriction op2 = (OpRestriction) right;
 
                         predicates.add(new SearchPredicate(op2.getLeft().toString(),
                                                            SearchPredicate.DOES_NOT_HAVE_VALUE,
                                                            op2.getRight().toString()));
                     } else {
                         throw new RPCException("Query could not be converted.");
                     }
                 } else if (operator.equals(Operator.EQUALS)) {
                     predicates.add(new SearchPredicate(left.toString(), SearchPredicate.HAS_VALUE, right.toString()));
                 } else if (operator.equals(Operator.LIKE)) {
                     predicates.add(new SearchPredicate(left.toString(), SearchPredicate.LIKE, right.toString()));
                 }
             }
         }
         return predicates;
     }
 
     public String saveArtifactView(WArtifactView wv) throws RPCException {
         View v = fromWeb(wv);
 
         try {
             artifactViewManager.save(v);
             return v.getId();
         } catch (DuplicateItemException e) {
             log.error(e.getMessage(), e);
             throw new RPCException("Couldn't save view.");
         } catch (NotFoundException e) {
             log.error(e.getMessage(), e);
             throw new RPCException("The view being saved has been deleted.");
         }
     }
 
     private View fromWeb(WArtifactView wv) throws RPCException {
         View v = new View();
         v.setId(wv.getId());
         v.setName(wv.getName());
         if (!wv.isShared()) {
             v.setUser(getCurrentUser());
         }
         Query query = getQuery(wv.getPredicates(), 0, 0);
         query.workspacePath(wv.getWorkspace(), wv.isWorkspaceSearchRecursive());
 
         v.setQuery(query.toString());
 
         return v;
     }
 
     public Collection<WIndex> getIndexes() {
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
 
     public void deleteIndex(String id, boolean removeArtifactMetadata) throws RPCException {
         try {
             indexManager.delete(id, removeArtifactMetadata);
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new RPCException("Couldn't save index.");
         }
     }
 
     private Index fromWeb(WIndex wi) throws RPCException {
         Index idx = new Index();
         idx.setId(wi.getId());
         idx.setConfiguration(new HashMap<String, String>());
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
 
     public Collection<LinkInfo> getLinks(String itemId, String property) throws RPCException {
         try {
             Item item = registry.getItemById(itemId);
             Links links = (Links) item.getProperty(property);
             
             List<LinkInfo> deps = new ArrayList<LinkInfo>();
             
             for (Link l : links.getLinks()) {
                 deps.add(toWeb(l, false));
             }
 
             for (Link l : links.getReciprocalLinks()) {
                 deps.add(toWeb(l, true));
             }
 
             return deps;
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new RPCException("Could not find artifact " + itemId);
         }
 
     }
 
     private LinkInfo toWeb(Link l, boolean recip) {
         Item i = recip ? l.getItem() : l.getLinkedTo();
         String name;
         int itemType;
         String id = null;
 
         if (i instanceof Entry) {
             itemType = LinkInfo.TYPE_ENTRY;
             name = ((Entry) i).getName();
             id = i.getId();
         } else if (i instanceof EntryVersion) {
             itemType = LinkInfo.TYPE_ENTRY_VERSION;
             EntryVersion av = ((EntryVersion) i);
             name = av.getParent().getName() + " (" + av.getVersionLabel() + ")";
             id = i.getId();
         } else if (i == null) {
             itemType = LinkInfo.TYPE_NOT_FOUND;
             name = l.getLinkedToPath();
         } else {
             throw new UnsupportedOperationException();
         }
         return new LinkInfo(l.getId(),
                             l.isAutoDetected(),
                             id,
                             name,
                             itemType,
                             recip);
     }
 
     public ExtendedEntryInfo getEntry(String entryId) throws RPCException, ItemNotFoundException {
         try {
             Entry a = (Entry) registry.getItemById(entryId);
             
             return getEntryGroup(a);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public ExtendedEntryInfo getArtifactByVersionId(String artifactVersionId) throws RPCException, ItemNotFoundException {
         try {
             ArtifactVersion av = registry.getArtifactVersion(artifactVersionId);
             return getEntryGroup(av.getParent());
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private ExtendedEntryInfo getEntryGroup(Entry e) {
         ExtendedEntryInfo info = new ExtendedEntryInfo();
         
         ItemRenderer view;
         if (e instanceof Artifact) {
             Artifact a = (Artifact) e;
             ArtifactType type = artifactTypeDao.getArtifactType(a.getContentType().toString(), 
                                                                 a.getDocumentType());
     
             info.setType(type.getDescription());
             info.setMediaType(type.getContentType().toString());
             
             view = rendererManager.getArtifactRenderer(a.getDocumentType());
             if (view == null) {
                 view = rendererManager.getArtifactRenderer(a.getContentType().toString());
             }
 
             final String context = contextPathResolver.getContextPath();
 
             info.setArtifactLink(getLink(context + "/api/registry", a));
             info.setArtifactFeedLink(getLink(context + "/api/registry", a) + ";history");
             info.setCommentsFeedLink(context + "/api/comments");
         } else {
             view = rendererManager.getArtifactRenderer("application/octet-stream");
             info.setType("Entry");
         }
 
         createBasicEntryInfo(e, view, info, true);
 
         info.setDescription(e.getDescription());
 
         List<WComment> wcs = info.getComments();
 
         Workspace workspace = (Workspace) e.getParent();
         List<Comment> comments = workspace.getCommentManager().getComments(e.getId());
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
 
         List<EntryVersionInfo> versions = new ArrayList<EntryVersionInfo>();
         for (EntryVersion av : e.getVersions()) {
             versions.add(toWeb((EntryVersion)av, false));
         }
         info.setVersions(versions);
 
         return info;
     }
 
     public EntryVersionInfo getEntryVersionInfo(String entryVersionId, boolean showHidden) throws RPCException,
                                                                                                            ItemNotFoundException {
         try {
             EntryVersion ev = (EntryVersion) registry.getItemById(entryVersionId);
 
             return toWeb(ev, showHidden);
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
     private EntryVersionInfo toWeb(EntryVersion av, boolean showHidden) {
         EntryVersionInfo vi = new EntryVersionInfo(av.getId(),
                                                    av.getVersionLabel(),
                                                    av.getCreated().getTime(),
                                                    av.isDefault(),
                                                    av.isEnabled(),
                                                    av.getAuthor().getName(),
                                                    av.getAuthor().getUsername(),
                                                    av.isIndexedPropertiesStale());
         
         if (av instanceof ArtifactVersion) {
             vi.setLink(getVersionLink((ArtifactVersion)av));
         }
         
         for (Iterator<PropertyInfo> props = av.getProperties(); props.hasNext();) {
             PropertyInfo p = props.next();
 
             if (!showHidden && !p.isVisible()) {
                 continue;
             }
 
             Object val = p.getInternalValue();
 
             val = convertQNames(val);
             
             String desc = p.getDescription();
             if (desc == null) {
                 desc = p.getName();
             }
             PropertyDescriptor pd = p.getPropertyDescriptor();
             String ext = pd != null && pd.getExtension() != null ? pd.getExtension().getId() : null;
             vi.getProperties().add(new WProperty(p.getName(), desc, val, ext, p.isLocked()));
         }
 
         Collections.sort(vi.getProperties(), new Comparator() {
 
             public int compare(Object o1, Object o2) {
                 return ((WProperty) o1).getDescription().compareTo(((WProperty) o2).getDescription());
             }
 
         });
 
         return vi;
     }
 
     /**
      * This method is here temporarily until we can serialize qnames remotely
      * @param val
      */
     private Object convertQNames(Object val) {
         if (val instanceof Collection) {
             List<String> objs = new ArrayList<String>();
             for (Object o : (Collection) val) {
                 objs.add(o.toString());
             }
             return objs;
         }
         return val;
     }
 
     private String getLink(String base, Artifact a) {
         StringBuilder sb = new StringBuilder();
         Workspace w = (Workspace) a.getParent();
 
         sb.append(base).append(w.getPath()).append(a.getName());
         return sb.toString();
     }
 
     public WComment addComment(String entryId, String parentComment, String text) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(entryId);
 
             Comment comment = new Comment();
             comment.setText(text);
 
             Calendar cal = Calendar.getInstance();
             cal.setTime(new Date());
             comment.setDate(cal);
 
             comment.setUser(getCurrentUser());
 
             Workspace w = (Workspace)artifact.getParent();
             CommentManager commentManager = w.getCommentManager();
             if (parentComment != null) {
                 Comment c = commentManager.getComment(parentComment);
                 if (c == null) {
                     throw new RPCException("Invalid parent comment");
                 }
                 comment.setParent(c);
             } else {
                 comment.setItem(artifact);
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
         UserDetailsWrapper wrapper = (UserDetailsWrapper) SecurityContextHolder.getContext()
                 .getAuthentication().getPrincipal();
         if (wrapper == null) {
             throw new RPCException("No user is logged in!");
         }
         return wrapper.getUser();
     }
 
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
 
 
     public void setProperty(String entryId, String propertyName, Collection<String> propertyValue)
         throws RPCException, ItemNotFoundException, WPolicyException {
         setProperty(entryId, propertyName, (Object)propertyValue);
     }
 
     public void setProperty(String entryId, String propertyName, String propertyValue)
         throws RPCException, ItemNotFoundException, WPolicyException {
         setProperty(entryId, propertyName, (Object)propertyValue);
     }
 
     protected void setProperty(String itemId, String propertyName, Object propertyValue) throws RPCException, ItemNotFoundException, WPolicyException {
         try {
             Item item = registry.getItemById(itemId);
 
             item.setInternalProperty(propertyName, propertyValue);
 
             registry.save(item);
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
         } catch (PolicyException e) {
             throw toWeb(e);
         }
     }
 
     public void setProperty(Collection<String> entryIds, String propertyName, Collection<String> propertyValue)
         throws RPCException, ItemNotFoundException {
     }
 
     public void setProperty(Collection<String> entryIds, String propertyName, String propertyValue)
         throws RPCException, ItemNotFoundException {
     }
     public void setProperty(Collection<String> entryIds, String propertyName, Object propertyValue) throws RPCException, ItemNotFoundException {
 
     }
 
     public void deleteProperty(String itemId, String propertyName) throws RPCException, ItemNotFoundException {
         try {
             Item item = registry.getItemById(itemId);
             item.setProperty(propertyName, null);
             registry.save(item);
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
         } catch (PolicyException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void deleteProperty(Collection<String> entryIds, String propertyName) throws RPCException, ItemNotFoundException {
 
     }
 
     public void deletePropertyDescriptor(String id) throws RPCException {
         typeManager.deletePropertyDescriptor(id);
     }
 
     public WPropertyDescriptor getPropertyDescriptor(String id) throws RPCException, ItemNotFoundException {
         try {
             return toWeb(typeManager.getPropertyDescriptor(id));
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         }
     }
 
     public List<WPropertyDescriptor> getPropertyDescriptors(boolean includeIndex) throws RPCException {
         List<WPropertyDescriptor> pds = new ArrayList<WPropertyDescriptor>();
         for (PropertyDescriptor pd : typeManager.getPropertyDescriptors(includeIndex)) {
             pds.add(toWeb(pd));
         }
         return pds;
     }
 
     private WPropertyDescriptor toWeb(PropertyDescriptor pd) {
         String ext = pd.getExtension() != null ? pd.getExtension().getId() : null;
         
         return new WPropertyDescriptor(pd.getId(), pd.getProperty(), pd.getDescription(), ext, pd.isMultivalued(), pd.getConfiguration());
     }
 
     public void savePropertyDescriptor(WPropertyDescriptor wpd) throws RPCException, ItemNotFoundException, ItemExistsException {
         try {
             PropertyDescriptor pd;
 
             if (wpd.getId() == null) {
                 pd = new PropertyDescriptor();
             } else {
                 pd = typeManager.getPropertyDescriptor(wpd.getId());
             }
 
             pd.setProperty(wpd.getName());
             pd.setDescription(wpd.getDescription());
             pd.setMultivalued(wpd.isMultiValued());
             pd.setConfiguration(wpd.getConfiguration());
             pd.setExtension(registry.getExtension(wpd.getExtension()));
             
             typeManager.savePropertyDescriptor(pd);
 
             wpd.setId(pd.getId());
         } catch (DuplicateItemException e) {
             throw new ItemExistsException();
         } catch (NotFoundException e) {
             throw new RPCException(e.getMessage());
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void setDescription(String entryId, String description) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(entryId);
 
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
 
     public void move(String entryId, String workspaceId, String name) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(entryId);
 
             registry.move(artifact, workspaceId, name);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public void delete(String entryId) throws RPCException, ItemNotFoundException {
         try {
             Artifact artifact = registry.getArtifact(entryId);
 
             artifact.delete();
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     public boolean deleteArtifactVersion(String artifactVersionId) throws RPCException, ItemNotFoundException {
         try {
             ArtifactVersion av = registry.getArtifactVersion(artifactVersionId);
             Artifact a = (Artifact) av.getParent();
             boolean last = a.getVersions().size() == 1;
 
             av.delete();
 
             return last;
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
             
             Phase phase = (Phase) artifact.getProperty(Registry.PRIMARY_LIFECYCLE);
             gov.setCurrentPhase(phase.getName());
             gov.setLifecycle(phase.getLifecycle().getName());
 
             gov.setNextPhases(toWeb(phase.getNextPhases()));
             gov.setPreviousPhases(toWeb(phase.getPreviousPhases()));
 
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
 
     private List<WPhase> toWeb(Set<Phase> nextPhases) {
         List<WPhase> wNextPhases = new ArrayList<WPhase>();
         for (Phase p : nextPhases) {
             wNextPhases.add(toWeb(p));
         }
         return wNextPhases;
     }
 
     // TODO:
     public void transition(Collection<String> entryIds, String lifecycle, String phase) throws RPCException, ItemNotFoundException {
         
     }
 
     public Collection<WLifecycle> getLifecycles() throws RPCException {
         Collection<Lifecycle> lifecycles = localLifecycleManager.getLifecycles();
         Lifecycle defaultLifecycle = localLifecycleManager.getDefaultLifecycle();
 
         ArrayList<WLifecycle> wls = new ArrayList<WLifecycle>();
         for (Lifecycle l : lifecycles) {
             WLifecycle lifecycle = toWeb(l, defaultLifecycle.equals(l));
             wls.add(lifecycle);
         }
 
         return wls;
     }
 
     public WLifecycle getLifecycle(String id) throws RPCException {
         try {
             Lifecycle defaultLifecycle = localLifecycleManager.getDefaultLifecycle();
 
             Lifecycle l = localLifecycleManager.getLifecycleById(id);
 
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
 
     public Collection<String> getActivePoliciesForLifecycle(String lifecycleName, String workspaceId)
             throws RPCException {
         Collection<Policy> pols = null;
         Lifecycle lifecycle = localLifecycleManager.getLifecycle(lifecycleName);
         try {
             if (workspaceId != null) {
                 Workspace w = (Workspace) registry.getItemById(workspaceId);
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
 
     public Collection<String> getActivePoliciesForPhase(String lifecycle, String phaseName, String workspaceId)
             throws RPCException {
         Collection<Policy> pols = null;
         Phase phase = localLifecycleManager.getLifecycle(lifecycle).getPhase(phaseName);
         try {
             if (workspaceId != null) {
                 Workspace w = (Workspace) registry.getItemById(workspaceId);
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
 
     public void setActivePolicies(String workspace, String lifecycle, String phase, Collection<String> ids)
             throws RPCException, WPolicyException, ItemNotFoundException {
         Lifecycle l = localLifecycleManager.getLifecycle(lifecycle);
         List<Policy> policies = getArtifactPolicies(ids);
 
         try {
             if (phase != null) {
                 Phase p = l.getPhase(phase);
 
                 if (p == null) {
                     throw new RPCException("Invalid phase: " + phase);
                 }
 
                 List<Phase> phases = Arrays.asList(p);
 
                 if (workspace == null || "".equals(workspace)) {
                     policyManager.setActivePolicies(phases, policies.toArray(new Policy[policies
                             .size()]));
                 } else {
                     Workspace w = (Workspace) registry.getItemById(workspace);
                     policyManager.setActivePolicies(w, phases, policies.toArray(new Policy[policies
                             .size()]));
                 }
             } else {
                 if (workspace == null || "".equals(workspace)) {
                     policyManager.setActivePolicies(l, policies.toArray(new Policy[policies.size()]));
                 } else {
                     Workspace w = (Workspace) registry.getItemById(workspace);
                     policyManager.setActivePolicies(w, l, policies
                             .toArray(new Policy[policies.size()]));
                 }
             }
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (PolicyException e) {
             throw toWeb(e);
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         }
     }
 
     private WPolicyException toWeb(PolicyException e) {
         Map<EntryInfo, Collection<WApprovalMessage>> failures = new HashMap<EntryInfo, Collection<WApprovalMessage>>();
         for (Map.Entry<Item, List<ApprovalMessage>> entry : e.getPolicyFailures().entrySet()) {
             Item i = entry.getKey();
             List<ApprovalMessage> approvals = entry.getValue();
 
             Artifact a = (Artifact) i.getParent();
             ItemRenderer view = rendererManager.getArtifactRenderer(a.getDocumentType());
             if (view == null) {
                 view = rendererManager.getArtifactRenderer(a.getContentType().toString());
             }
 
             EntryInfo info = createBasicEntryInfo(a, view);
 
             ArrayList<WApprovalMessage> wapprovals = new ArrayList<WApprovalMessage>();
             for (ApprovalMessage app : approvals) {
                 wapprovals.add(new WApprovalMessage(app.getMessage(), app.isWarning()));
             }
 
             failures.put(info, wapprovals);
         }
         WPolicyException e2 = new WPolicyException(failures);
         return e2;
     }
 
     private List<Policy> getArtifactPolicies(Collection ids) {
         List<Policy> policies = new ArrayList<Policy>();
         for (Iterator itr = ids.iterator(); itr.hasNext();) {
             String id = (String) itr.next();
 
             Policy policy = policyManager.getPolicy(id);
             policies.add(policy);
         }
         return policies;
     }
 
     private Collection<String> getArtifactPolicyIds(Collection<Policy> pols) {
         ArrayList<String> polNames = new ArrayList<String>();
         for (Policy ap : pols) {
             polNames.add(ap.getId());
         }
         return polNames;
     }
 
     public void setDefault(String artifactVersionId) throws RPCException, ItemNotFoundException, WPolicyException {
         try {
             ArtifactVersion artifact = registry.getArtifactVersion(artifactVersionId);
 
             artifact.setAsDefaultVersion();
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         } catch (PolicyException e) {
             throw toWeb(e);
         }
     }
 
     public void setEnabled(String artifactVersionId, boolean enabled) throws RPCException, ItemNotFoundException, WPolicyException {
         try {
             ArtifactVersion artifact = registry.getArtifactVersion(artifactVersionId);
 
             artifact.setEnabled(enabled);
         } catch (RegistryException e) {
             log.error(e.getMessage(), e);
             throw new RPCException(e.getMessage());
         } catch (NotFoundException e) {
             throw new ItemNotFoundException();
         } catch (AccessException e) {
             throw new RPCException(e.getMessage());
         } catch (PolicyException e) {
             throw toWeb(e);
         }
     }
 
     public Collection<WPolicy> getPolicies() throws RPCException {
         Collection<Policy> policies = policyManager.getPolicies();
         List<WPolicy> gwtPolicies = new ArrayList<WPolicy>();
         for (Policy p : policies) {
             gwtPolicies.add(toWeb(p));
         }
         Collections.sort(gwtPolicies, new Comparator<WPolicy>() {
 
             public int compare(WPolicy o1, WPolicy o2) {
                 return o1.getName().compareTo(o2.getName());
             }
 
         });
 
         return gwtPolicies;
     }
 
     public void saveLifecycle(WLifecycle wl) throws RPCException, ItemExistsException {
         Lifecycle l = fromWeb(wl);
 
         try {
             localLifecycleManager.save(l);
 
             if (wl.isDefaultLifecycle()) {
                 Lifecycle defaultLifecycle = localLifecycleManager.getDefaultLifecycle();
 
                 if (!defaultLifecycle.equals(l)) {
                     localLifecycleManager.setDefaultLifecycle(l);
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
             String fallback = localLifecycleManager.getDefaultLifecycle().getId();
 
             if (id.equals(fallback)) {
                 throw new RPCException("The default lifecycle cannot be deleted. Please assign " +
                         "another lifecycle to be the default before deleting this one.");
             }
 
             localLifecycleManager.delete(id, fallback);
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
 
     private WPolicy toWeb(Policy p) {
         WPolicy wap = new WPolicy();
         wap.setId(p.getId());
         wap.setDescription(p.getDescription());
         wap.setName(p.getName());
 
         return wap;
     }
 
     private String getVersionLink(ArtifactVersion av) {
         Artifact a = (Artifact) av.getParent();
         StringBuilder sb = new StringBuilder();
         Workspace w = (Workspace) a.getParent();
 
         final String context = contextPathResolver.getContextPath();
         sb.append(context).append("/api/registry").append(w.getPath()).append(a.getName()).append("?version=")
                 .append(av.getVersionLabel());
         return sb.toString();
     }
 
     public Collection<WActivity> getActivities(Date from, Date to, String user, String eventTypeStr, int start,
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
         } catch (AccessException e) {
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
 
     public void setRegistry(Registry registry) {
         this.registry = registry;
     }
 
     public void setTypeManager(TypeManager typeManager) {
         this.typeManager = typeManager;
     }
 
     public void setLifecycleManager(LifecycleManager lifecycleManager) {
         this.localLifecycleManager = lifecycleManager;
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
 
     public void setArtifactViewManager(ArtifactViewManager artifactViewManager) {
         this.artifactViewManager = artifactViewManager;
     }
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
 
     public ContextPathResolver getContextPathResolver() {
         return contextPathResolver;
     }
 
     public void setContextPathResolver(final ContextPathResolver contextPathResolver) {
         this.contextPathResolver = contextPathResolver;
     }
 
     public EventManager getEventManager() {
         return eventManager;
     }
 
     public void setEventManager(final EventManager eventManager) {
         this.eventManager = eventManager;
     }
 }
