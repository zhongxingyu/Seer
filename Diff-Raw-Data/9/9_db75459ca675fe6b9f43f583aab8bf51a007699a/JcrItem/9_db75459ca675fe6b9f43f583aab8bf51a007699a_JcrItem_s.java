 package org.mule.galaxy.impl.jcr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javax.jcr.Node;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.Value;
 
 import org.apache.jackrabbit.util.ISO9075;
 import org.apache.jackrabbit.value.StringValue;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.collab.CommentManager;
 import org.mule.galaxy.event.GalaxyEvent;
 import org.mule.galaxy.event.PropertyChangedEvent;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.impl.workspace.AbstractItem;
 import org.mule.galaxy.lifecycle.Lifecycle;
 import org.mule.galaxy.lifecycle.LifecycleManager;
 import org.mule.galaxy.policy.PolicyException;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.type.Type;
 import org.mule.galaxy.util.BundleUtils;
 import org.mule.galaxy.util.GalaxyUtils;
 import org.mule.galaxy.util.Message;
 import org.mule.galaxy.util.SecurityUtils;
 import org.springmodules.jcr.JcrCallback;
 
 
 public class JcrItem extends AbstractItem {
 
     public static final String PROPERTIES = "properties";
     public static final String LOCKED = ".locked";
     public static final String VISIBLE = ".visible";
     public static final String UPDATED = "updated";
     public static final String NAME = "name";
     public static final String CREATED = "created";
     public static final String TYPE = "type";
     public static final String LIFECYCLE = "lifecycle";
     public static final String AUTHOR = "author";
     
     private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JcrItem.class);
     
     private Lifecycle lifecycle;
     protected Node node;
     protected final JcrWorkspaceManager manager;
     private JcrItem parent;
     private Type type;
     private User author;
     private List<GalaxyEvent> saveEvents;
     private String name;
     
     public JcrItem(Node node, JcrWorkspaceManager manager) throws RepositoryException {
         super(manager);
         this.node = node;
         this.manager = manager;
         
         this.name = ISO9075.decode(node.getName());
     }
 
     public JcrWorkspaceManager getManager() {
         return manager;
     }
 
     public String getId() {
         try {
             return JcrWorkspaceManagerImpl.ID + Registry.WORKSPACE_MANAGER_SEPARATOR + node.getUUID();
        } catch (RepositoryException e) {
             return null;
         }
     }
 
     public User getAuthor() {
         if (author == null) {
             String authId = getStringOrNull(AUTHOR);
             
             if (authId != null) {
                 try {
                     author = getManager().getUserManager().get(authId);
                 } catch (NotFoundException e) {
                     // TODO
                 }
             } else {
                 author = SecurityUtils.SYSTEM_USER;
             }
         }
         return author;
     }
     
     public Calendar getCreated() {
         return getCalendarOrNull(CREATED);
     }
 
     public String getName() {
         return name;
     }
     
     public void setName(final String name) {
         try {
             
             String nodeName = ISO9075.decode(node.getName());
             if (!nodeName.equals(name)) {
                 manager.getTemplate().execute(new JcrCallback() {
     
                     public Object doInJcr(Session session) throws IOException, RepositoryException {
                         String dest = node.getParent().getPath() + "/" + ISO9075.encode(name);
                         session.move(node.getPath(), dest);
                         return null;
                     }
                     
                 });
             }
             node.setProperty(NAME, name);
             this.name = name;
             update();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     public boolean isLocal() {
         return true;
     }
 
     public void delete() throws RegistryException, AccessException {
         manager.delete(this);
     }
     
     public Node getNode() {
         return node;
     }
 
     public void setNode(Node node) {
         this.node = node;
     }
 
     public Calendar getUpdated() {
         return getCalendarOrNull(UPDATED);
     }
 
     public Item getParent() {
         try {
             if (parent == null) {
                 Node parentNode = node.getParent();
                 if ("galaxy:item".equals(parentNode.getPrimaryNodeType().getName())) {
                     parent = new JcrItem(parentNode, manager);
                 }
             }
             return parent;
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void setParent(JcrItem parent) {
         this.parent = parent;
     }
 
     public Type getType() {
         if (type == null) {
             String id = getStringOrNull(TYPE);
 
             if (id == null) {
                 throw new IllegalStateException("Items in the registry require a type!");
             }
 
             try {
                 type = manager.getTypeManager().getType(id);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return type;
     }
     
     public void setType(Type t) throws PropertyException {
         try {
             verifyConformance(t);
             node.setProperty(TYPE, t.getId()/*, PropertyType.REFERENCE */);
             update();
             this.type = t;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public void verifyConformance() throws PropertyException {
         verifyConformance(getType());
     }
 
     private void verifyConformance(Type t) throws PropertyException {
         Set<Type> verified = new HashSet<Type>();
         verifyConformance(t, verified);
     }
 
     private void verifyConformance(Type t, Set<Type> verified) throws PropertyException {
         if (verified.contains(t)) {
             return;
         }
         
         verified.add(t);
         if (t.getProperties() != null) {
             for (PropertyDescriptor p : t.getProperties()) {
                 Object val = getProperty(p.getProperty());
                 if (val == null) {
                     throw new PropertyException(new Message("MISSING_PROPERTY", BUNDLE, t.getName(), p.getDescription()));
                 }
             }
         }
         
         if (t.getMixins() != null) {
             for (Type mix : t.getMixins()) {
                 verifyConformance(mix);
             }
         }
     }
     
     protected String getStringOrNull(String propName) {
         return JcrUtil.getStringOrNull(node, propName);
     }
     
     protected Date getDateOrNull(String propName) {
         return JcrUtil.getDateOrNull(node, propName);
     }
     
     protected Calendar getCalendarOrNull(String propName) {
         return JcrUtil.getCalendarOrNull(node, propName);
     }
     
     public Lifecycle getDefaultLifecycle() {
         if (lifecycle == null) {
             String id = (String) getStringOrNull(LIFECYCLE);
             
             if (id == null) {
                 Item parent = getParent();
                 if (parent == null) {
                     return getLifecycleManager().getDefaultLifecycle();
                 } else {
                     return parent.getDefaultLifecycle();
                 }
             } else {
                 lifecycle = getLifecycleManager().getLifecycleById(id);
             }
         }
         return lifecycle;
     }
 
     public void setDefaultLifecycle(Lifecycle lifecycle) {
         this.lifecycle = lifecycle;
         update();
         
         try {
             if (lifecycle == null) {
                 node.setProperty(LIFECYCLE, (String) null);
             } else {
                 node.setProperty(LIFECYCLE, lifecycle.getId());
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public LifecycleManager getLifecycleManager() {
       return manager.getLifecycleManager();
     }
 
     public CommentManager getCommentManager() {
         return manager.getCommentManager();
     }
 
     protected Value getValueOrNull(String propName) throws PathNotFoundException, RepositoryException {
         return JcrUtil.getValueOrNull(node, propName);
     }
 
     public void setNodeProperty(String name, String value) {
         try {
             node.setProperty(name, value);
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
     
     public void setProperty(String name, Object value) throws PropertyException, PolicyException, AccessException {
         if (name.contains(" ")) {
                 throw new PropertyException(new Message("SPACE_NOT_ALLOWED", getBundle()));
         }
         
         PropertyDescriptor pd = getManager().getTypeManager().getPropertyDescriptorByName(name, null);
         if (pd != null && pd.getExtension() != null) {
             pd.getExtension().store(this, pd, value);
     } else {
         setInternalProperty(name, value, false);
 
             getSaveEvents().add(new PropertyChangedEvent(SecurityUtils.getCurrentUser(), this, name, value));
     }
     }
 
     public void setInternalProperty(String name, Object value) throws PropertyException, PolicyException, AccessException {
         setInternalProperty(name, value, true);
     }
     
     private void setInternalProperty(String name, Object value, boolean log) throws PropertyException, PolicyException, AccessException {
         try {
             if (name.contains(" ")) {
                 throw new PropertyException(new Message("SPACE_NOT_ALLOWED", getBundle()));
             }
             manager.getAccessControlManager().assertAccess(Permission.MODIFY_ITEM, this);
             
             JcrUtil.setProperty(name, value, node);
             
             if (value == null) {
                 deleteProperty(name);
             } else {
                 ensureProperty(name);
             }
 
             if (log) {
                 // We need to re-extract the property value because we need to log the external value
                 getSaveEvents().add(
                     new PropertyChangedEvent(SecurityUtils.getCurrentUser(), this, name, getProperty(name)));
             }
             update();
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
 
     public List<GalaxyEvent> getSaveEvents() {
         if (saveEvents == null) {
             saveEvents = new ArrayList<GalaxyEvent>();
         }
         return saveEvents;
     }
 
     public boolean hasProperty(String name) {
         try {
             return node.hasProperty(name);
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     private void deleteProperty(String name) throws RepositoryException {
         Property p = null;
         try {
             p = node.getProperty(PROPERTIES);
             
             List<Value> values = new ArrayList<Value>();
             for (Value v : p.getValues()) {
                 if (!v.getString().equals(name)) {
                     values.add(v);
                 }
             }
             
             p.setValue(values.toArray(new Value[values.size()]));
             update();
         } catch (PathNotFoundException e) {
             return;
         }
     }
 
     private ResourceBundle getBundle() {
         return BundleUtils.getBundle(JcrItem.class);
     }
 
     private void ensureProperty(String name) throws RepositoryException {
         ensureProperty(node, name);
     }
     
     public static void ensureProperty(Node node, String name) throws RepositoryException {
         Property p = null;
         try {
             p = node.getProperty(PROPERTIES);
             
             for (Value v : p.getValues()) {
                 if (v.getString().equals(name)) {
                     return;
                 }
             }
             
             List<Value> values = new ArrayList<Value>();
             for (Value v : p.getValues()) {
                 values.add(v);
             }
             values.add(new StringValue(name));
             p.setValue(values.toArray(new Value[values.size()]));
             
         } catch (PathNotFoundException e) {
             Value[] values = new Value[1];
             values[0] = new StringValue(name);
             node.setProperty(PROPERTIES, values);
             
             return;
         }
     }
 
     public Object getProperty(String name) {
     PropertyDescriptor pd = manager.getTypeManager().getPropertyDescriptorByName(name, getType());
 
     if (pd != null && pd.getExtension() != null) {
             return pd.getExtension().get(this, pd, true);
         } else {
         return JcrUtil.getProperty(name, node);
     }
     }
 
     public Object getInternalProperty(String name) {
         return JcrUtil.getProperty(name, node);
     }
 
     public Collection<PropertyInfo> getProperties() {
         try {
             Property p = null;
             final Map<String, PropertyInfo> properties = new HashMap<String, PropertyInfo>();
             try {
                 p = node.getProperty(PROPERTIES);
                 final Value[] values = p.getValues();
                 for (Value v : values) {
                     String name = v.getString();
                     properties.put(name, new PropertyInfoImpl(this, name, node, manager.getTypeManager()));
                 }
             } catch (PathNotFoundException e) {
             }
             
             Collection<PropertyDescriptor> pds = manager.getTypeManager().getGlobalPropertyDescriptors(false);
             
             for (PropertyDescriptor pd : pds) {
                 Extension ext = pd.getExtension();
                 if (ext != null) {
                     Object value = ext.get(this, pd, false);
                     
                     if (value != null) {
                         properties.put(pd.getProperty(), new PropertyInfoImpl(this, pd.getProperty(), node, pd, value));
                     }
                 }
             }
             return properties.values();
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
 
     public PropertyInfo getPropertyInfo(String name) {
         try {
             Property propList = node.getProperty("properties");
             
             boolean found = false;
             for (Value v : propList.getValues()) {
                 if (v.getString().equals(name)) {
                     found = true;
                     break;
                 }
             }
             
             if (!found) return null;
         } catch (PathNotFoundException e) {
             return null;
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
         
         return new PropertyInfoImpl(this, name, node, manager.getTypeManager());
     }
 
     public void setLocked(String name, boolean locked) {
         try {
             node.setProperty(name + LOCKED, locked);
             update();
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void setVisible(String name, boolean visible) {
         try {
             node.setProperty(name + VISIBLE, visible);
             update();
         } catch (RepositoryException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void update() {
         try {
             node.setProperty(UPDATED, GalaxyUtils.getCalendarForNow());
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public int hashCode() {
         String id = getId();
         final int prime = 31;
         int result = 1;
         result = prime * result + ((id == null) ? 0 : id.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         JcrItem other = (JcrItem)obj;
         String id = getId();
         String otherId = other.getId();
         if (id == null) {
             if (otherId != null)
                 return false;
         } else if (!id.equals(otherId))
             return false;
         return true;
     }
     
     public String toString() {
         return "Item [" + getPath() + "]";
     }
     
 }
