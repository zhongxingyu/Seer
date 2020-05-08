 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.jackrabbit.util.ChildrenCollectorFilter;
 import org.apache.jackrabbit.value.BinaryImpl;
 import org.jahia.api.Constants;
 import org.jahia.services.content.nodetypes.*;
 
 import javax.jcr.*;
 import javax.jcr.lock.Lock;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.*;
 import javax.jcr.version.Version;
 import javax.jcr.version.VersionException;
 import javax.jcr.version.VersionHistory;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.*;
 
 /**
  * Implementation of the {@link javax.jcr.Node} for the {@link org.jahia.modules.external.ExternalData}.
  *
  * @author Thomas Draier
  */
 public class ExternalNodeImpl extends ExternalItemImpl implements Node {
 
     private ExternalData data;
     private Map<String, ExternalPropertyImpl> properties = null;
 
     private String uuid;
 
     public ExternalNodeImpl(ExternalData data, ExternalSessionImpl session) throws RepositoryException {
         super(session);
         this.data = data;
         this.properties = new HashMap<String, ExternalPropertyImpl>();
 
         for (Map.Entry<String, String[]> entry : data.getProperties().entrySet()) {
             ExtendedPropertyDefinition definition = getPropertyDefinition(entry.getKey());
             if (definition != null) {
                 int requiredType = definition.getRequiredType();
                 if (requiredType == PropertyType.UNDEFINED) {
                     requiredType = PropertyType.STRING;
                 }
                 if (definition.isMultiple()) {
                     Value[] values = new Value[entry.getValue().length];
                     for (int i = 0; i < entry.getValue().length; i++) {
                         values[i] = session.getValueFactory().createValue(entry.getValue()[i], requiredType);
                     }
                     properties.put(entry.getKey(),new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
                 } else {
                     properties.put(entry.getKey(),
                             new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                                     session.getValueFactory().createValue(entry.getValue().length > 0 ? entry.getValue()[0] : null, requiredType)));
                 }
             }
         }
         if (data.getBinaryProperties() != null) {
             for (Map.Entry<String, Binary[]> entry : data.getBinaryProperties().entrySet()) {
                 ExtendedPropertyDefinition definition =  getPropertyDefinition(entry.getKey());
                 if (definition != null && definition.getRequiredType() == PropertyType.BINARY) {
                     if (definition.isMultiple()) {
                         Value[] values = new Value[entry.getValue().length];
                         for (int i = 0; i < entry.getValue().length; i++) {
                             values[i] = session.getValueFactory().createValue(entry.getValue()[i]);
                         }
                         properties.put(entry.getKey(),new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
                     } else {
                         properties.put(entry.getKey(),
                                 new ExternalPropertyImpl(new Name(entry.getKey(), NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                                         session.getValueFactory().createValue(entry.getValue()[0])));
                     }
                 }
             }
         }
         properties.put("jcr:uuid",
                 new ExternalPropertyImpl(new Name("jcr:uuid", NodeTypeRegistry.getInstance().getNamespaces()), this, session,
                         session.getValueFactory().createValue(getIdentifier())));
     }
 
     private ExtendedNodeDefinition getChildNodeDefinition(String name, String childType) throws RepositoryException {
         Map<String, ExtendedNodeDefinition> nodeDefinitionsAsMap = getExtendedPrimaryNodeType().getChildNodeDefinitionsAsMap();
         if (nodeDefinitionsAsMap.containsKey(name)) {
             return nodeDefinitionsAsMap.get(name);
         }
         for (NodeType nodeType : getMixinNodeTypes()) {
             nodeDefinitionsAsMap = ((ExtendedNodeType)nodeType).getChildNodeDefinitionsAsMap();
             if (nodeDefinitionsAsMap.containsKey(name)) {
                 return nodeDefinitionsAsMap.get(name);
             }
         }
        ExtendedNodeType childTypeNT = NodeTypeRegistry.getInstance().getNodeType(childType);
         for (Map.Entry<String, ExtendedNodeDefinition> entry : getExtendedPrimaryNodeType().getUnstructuredChildNodeDefinitions().entrySet()) {
             if (childTypeNT.isNodeType(entry.getKey())) {
                 return entry.getValue();
             }
         }
         return null;
     }
 
     private ExtendedPropertyDefinition getPropertyDefinition(String name) throws RepositoryException {
         Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = getExtendedPrimaryNodeType().getPropertyDefinitionsAsMap();
         if (propertyDefinitionsAsMap.containsKey(name)) {
             return propertyDefinitionsAsMap.get(name);
         }
         for (NodeType nodeType : getMixinNodeTypes(false)) {
             propertyDefinitionsAsMap = ((ExtendedNodeType)nodeType).getPropertyDefinitionsAsMap();
             if (propertyDefinitionsAsMap.containsKey(name)) {
                 return propertyDefinitionsAsMap.get(name);
             }
         }
         if (getExtensionNode(false) != null) {
             for (NodeType nodeType : getExtensionNode(false).getMixinNodeTypes()) {
                 nodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType.getName());
                 propertyDefinitionsAsMap = ((ExtendedNodeType)nodeType).getPropertyDefinitionsAsMap();
                 if (propertyDefinitionsAsMap.containsKey(name)) {
                     return propertyDefinitionsAsMap.get(name);
                 }
             }
         }
 
         if (!getExtendedPrimaryNodeType().getUnstructuredPropertyDefinitions().isEmpty()) {
             return getExtendedPrimaryNodeType().getUnstructuredPropertyDefinitions().values().iterator().next();
         }
         return null;
     }
 
 
     public String getPath() throws RepositoryException {
         return data.getPath();
     }
 
     public String getName() throws RepositoryException {
         return StringUtils.substringAfterLast(data.getPath(), "/");
     }
 
     public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
         if (data.getPath().equals("/")) {
             throw new ItemNotFoundException();
         }
         String path = StringUtils.substringBeforeLast(data.getPath(), "/");
         return session.getNode(path.isEmpty() ? "/" : path);
     }
 
     public boolean isNode() {
         return true;
     }
 
     public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         session.getDeletedData().put(getPath(),data);
     }
 
     protected void removeProperty(String name) throws RepositoryException {
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         boolean hasProperty = false;
         if (data.getBinaryProperties() != null && data.getBinaryProperties().containsKey(name)) {
             hasProperty = true;
             data.getBinaryProperties().remove(name);
         }
         if (data.getProperties() != null && data.getProperties().containsKey(name)) {
             hasProperty = true;
             data.getProperties().remove(name);
             properties.remove(name);
         }
         if (data.getLazyBinaryProperties() != null && data.getLazyBinaryProperties().contains(name)) {
             hasProperty = true;
             data.getLazyBinaryProperties().remove(name);
         }
         if (data.getLazyProperties() != null && data.getLazyProperties().contains(name)) {
             hasProperty = true;
             data.getLazyProperties().remove(name);
         }
         if (hasProperty) {
             session.getChangedData().put(getPath(), data);
         }
     }
 
     public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         return addNode(relPath,null);
     }
 
     public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
         Node extendedNode = getExtensionNode(true);
         if (extendedNode != null && canItemBeExtended(getChildNodeDefinition(relPath, primaryNodeTypeName))) {
             Node n = extendedNode.addNode(relPath, primaryNodeTypeName);
             n.addMixin("jmix:externalProviderExtension");
             n.setProperty("j:extendedType",primaryNodeTypeName);
             n.setProperty("j:isExternalProviderRoot", false);
             return new ExtensionNode(n,getPath() + "/" + relPath,getSession());
         }
 
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         String separator = StringUtils.equals(this.data.getId(),"/")?"":"/";
         ExternalData data = new ExternalData(this.data.getId() + separator + relPath ,getPath() + ( getPath().equals("/")? "" : "/" ) + relPath,primaryNodeTypeName,new HashMap<String, String[]>());
         session.getChangedData().put(data.getPath(),data);
         return  new ExternalNodeImpl(data,session);
     }
 
     public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
         if (srcChildRelPath.equals(destChildRelPath)) {
             return;
         }
         List<String> children = session.getOrderedData().containsKey(getPath()) ? session.getOrderedData().get(getPath()) : session.getRepository().getDataSource().getChildren(getPath());
 
         children.remove(srcChildRelPath);
         if (destChildRelPath == null || !children.contains(destChildRelPath)) {
             // put scrChildNode at the end of the list
             children.add(srcChildRelPath);
         } else {
             children.add(children.indexOf(destChildRelPath), srcChildRelPath);
         }
         session.getOrderedData().put(getPath(), children);
     }
 
     public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (canItemBeExtended(getPropertyDefinition(name)) && getExtensionNode(true) != null) {
             return new ExtensionProperty(getExtensionNode(true).setProperty(name, value), getPath()+"/"+name, session, this);
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (value == null) {
             Property p = getProperty(name);
             if (hasProperty(name)) {
                 removeProperty(name);
                 return p;
             } else  {
                 String s = null;
                 return new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, new ExternalValueImpl(s));
             }
         }
         ExtendedPropertyDefinition epd = getPropertyDefinition(name);
 
         if (!hasProperty(name) || (hasProperty(name) && !getProperty(name).equals(value))) {
             if (name.equals(Constants.JCR_DATA)) {
                 if (data.getBinaryProperties() == null) {
                     data.setBinaryProperties(new HashMap<String, Binary[]>());
                 }
                 data.getBinaryProperties().put(name, new Binary[]{value.getBinary()});
             } else if (epd.isInternationalized()) {
                 Map<String,String[]> valMap = new HashMap<String, String[]>();
                 if (StringUtils.substringAfterLast(getPath(), "/").startsWith("j:translation_")) {
                     String lang = StringUtils.substringAfterLast(getPath(), "_");
                     valMap.put(lang,new String[]{value.getString()});
                     data.getI18nProperties().put(name,valMap);
                 }
 
             } else {
                 data.getProperties().put(name, new String[]{value.getString()});
             }
             properties.put(name, new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, value));
             session.getChangedData().put(getPath(),data);
         }
         return getProperty(name);
     }
 
     public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,value);
     }
 
     public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (canItemBeExtended(getPropertyDefinition(name)) && getExtensionNode(true) != null) {
             return new ExtensionProperty(getExtensionNode(true).setProperty(name, values), getPath()+ "/" + name, session, this);
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (values == null) {
             Property p = getProperty(name);
             if (hasProperty(name)) {
                 removeProperty(name);
                 return p;
             } else  {
                 String s = null;
                 return new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, new ExternalValueImpl(s));
             }
         }
 
         if (!hasProperty(name) || (hasProperty(name) && !getProperty(name).equals(values))) {
             String[] s = new String[values.length];
             for (int i = 0; i < values.length; i ++) {
                 if (values[i] != null)  {
                     s[i] = values[i].getString();
                 }   else {
                     s[i] = null;
                 }
             }
             data.getProperties().put(name,s);
             properties.put(name, new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, values));
             session.getChangedData().put(getPath(),data);
         }
         return getProperty(name);
     }
 
     public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,values);
     }
 
     public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value[] v = null;
         if (values != null) {
             v = new Value[values.length];
             for (int i =0; i < values.length; i ++ ) {
                 if (values[i] != null) {
                     v[i] = getSession().getValueFactory().createValue(values[i]);
                 }  else {
                     v[i] = null;
                 }
             }
         }
         return setProperty(name,v);
     }
 
     public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name, values);
     }
 
     public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         return setProperty(name,value);
     }
 
     @SuppressWarnings("deprecation")
     public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         if (canItemBeExtended(getPropertyDefinition(name)) && getExtensionNode(true) != null) {
             return new ExtensionProperty(getExtensionNode(true).setProperty(name, value), getPath()+ "/" + name, session, this);
         }
         if (!(session.getRepository().getDataSource() instanceof ExternalDataSource.Writable)) {
             throw new UnsupportedRepositoryOperationException();
         }
         if (value == null) {
             if (hasProperty(name)) {
                 Property p = getProperty(name);
                 removeProperty(name);
                 return p;
             } else {
                 Binary b = null;
                 return new ExternalPropertyImpl(new Name(name, NodeTypeRegistry.getInstance().getNamespaces()), this, session, new ExternalValueImpl(b));
             }
         }
         Value v = null;
         Binary binary = null;
         try{
             binary = new BinaryImpl(value);
             Binary[] b = {binary};
             if (data.getBinaryProperties() == null) {
                 data.setBinaryProperties(new HashMap<String, Binary[]>());
             }
             data.getBinaryProperties().put(name, b);
             v = getSession().getValueFactory().createValue(binary);
         } catch (IOException e) {
             throw new RepositoryException(e);
         }
 
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = getSession().getValueFactory().createValue(value);
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     public Node getNode(String s) throws RepositoryException {
         Node n = session.getNode(getPath().endsWith("/") ? getPath() + s : getPath() + "/" + s);
         if (n != null) {
             return  n;
         }
         n = getExtensionNode(false);
         if (n != null) {
             return new ExtensionNode(n.getNode(s),getPath() + "/" + s,getSession());
         }
         throw new PathNotFoundException();
     }
 
     public NodeIterator getNodes() throws RepositoryException {
         List<String> l = new ArrayList<String>(session.getRepository().getDataSource().getChildren(getPath()));
         Set<String> languages = new HashSet<String>();
         if (data.getI18nProperties() != null) {
             languages.addAll(data.getI18nProperties().keySet());
         }
         if (data.getLazyI18nProperties() != null) {
             languages.addAll(data.getLazyI18nProperties().keySet());
         }
         for (String lang : languages) {
             l.add("j:translation_" + lang);
         }
 
         Node n = getExtensionNode(false);
         if (n != null && n.hasNodes()) {
             return  new ExternalNodeIterator(l,n.getNodes());
         }
         return new ExternalNodeIterator(l);
     }
 
     public NodeIterator getNodes(String namePattern) throws RepositoryException {
         final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
         final List<String> filteredList = new ArrayList<String>();
         for (String path : l) {
             if (ChildrenCollectorFilter.matches(path,namePattern)) {
                 filteredList.add(path);
             }
         }
         Set<String> languages = new HashSet<String>();
         if (data.getI18nProperties() != null) {
             languages.addAll(data.getI18nProperties().keySet());
         }
         if (data.getLazyI18nProperties() != null) {
             languages.addAll(data.getLazyI18nProperties().keySet());
         }
         for (String lang : languages) {
             if (ChildrenCollectorFilter.matches("j:translation_" + lang, namePattern)) {
                 filteredList.add("j:translation_" + lang);
             }
         }
 
         Node n = getExtensionNode(false);
         if (n != null) {
             return  new ExternalNodeIterator(filteredList,n.getNodes(namePattern));
         }
         return new ExternalNodeIterator(filteredList);
     }
 
     public Property getProperty(String s) throws PathNotFoundException, RepositoryException {
         Node n = getExtensionNode(false);
         if (n != null && n.hasProperty(s)  && canItemBeExtended(getPropertyDefinition(s))) {
             return new ExtensionProperty(n.getProperty(s), getPath() + "/" + s, session, this);
         }
         Property property = properties.get(s);
         if (property == null) {
             if (data.getLazyProperties() != null && data.getLazyProperties().contains(s)) {
                 String[] values;
                 if (properties.containsKey("jcr:language")) {
                     values = session.getI18nPropertyValues(data, properties.get("jcr:language").getString(), s);
                 } else {
                     values = session.getPropertyValues(data, s);
                 }
                 data.getProperties().put(s, values);
                 data.getLazyProperties().remove(s);
                 ExternalPropertyImpl p = new ExternalPropertyImpl(new Name(s, NodeTypeRegistry.getInstance().getNamespaces()), this, session);
                 if (getPropertyDefinition(s).isMultiple()) {
                     p.setValue(values);
                 } else if (values != null && values.length > 0) {
                     p.setValue(values[0]);
                 }
                 properties.put(s, p);
                 return p;
             } else if (data.getLazyBinaryProperties() != null && data.getLazyBinaryProperties().contains(s)) {
                 Binary[] values = session.getBinaryPropertyValues(data, s);
                 data.getBinaryProperties().put(s, values);
                 data.getLazyBinaryProperties().remove(s);
                 ExternalPropertyImpl p = new ExternalPropertyImpl(new Name(s, NodeTypeRegistry.getInstance().getNamespaces()), this, session);
                 if (getPropertyDefinition(s).isMultiple()) {
                     p.setValue(values);
                 } else if (values != null && values.length > 0) {
                     p.setValue(values[0]);
                 }
                 properties.put(s, p);
                 return p;
             } else {
                 throw new PathNotFoundException(s);
             }
         }
         return property;
     }
 
     public PropertyIterator getProperties() throws RepositoryException {
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(properties, n.getProperties(), data.getLazyProperties(), data.getLazyBinaryProperties(), this);
         }
         return new ExternalPropertyIterator(properties, data.getLazyProperties(), data.getLazyBinaryProperties(), this);
     }
 
     public PropertyIterator getProperties(String namePattern) throws RepositoryException {
         final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
         for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
             if (ChildrenCollectorFilter.matches(entry.getKey(),namePattern)) {
                 filteredList.put(entry.getKey(), entry.getValue());
             }
         }
         Set<String> lazyProperties = null;
         if (data.getLazyProperties() != null) {
             lazyProperties = new HashSet<String>();
             for (String propertyName : data.getLazyProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, namePattern)) {
                     lazyProperties.add(propertyName);
                 }
             }
         }
         Set<String> lazyBinaryProperties = null;
         if (data.getLazyBinaryProperties() != null) {
             lazyBinaryProperties = new HashSet<String>();
             for (String propertyName : data.getLazyBinaryProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, namePattern)) {
                     lazyBinaryProperties.add(propertyName);
                 }
             }
         }
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(filteredList, n.getProperties(namePattern), lazyProperties, lazyBinaryProperties, this);
         }
         return new ExternalPropertyIterator(filteredList, lazyProperties, lazyBinaryProperties, this);
     }
 
     public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {
         throw new ItemNotFoundException("External node does not support getPrimaryItem");
     }
 
     public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
         return getIdentifier();
     }
 
     public int getIndex() throws RepositoryException {
         return 0;
     }
 
     public PropertyIterator getReferences() throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>(), this);
     }
 
     public boolean hasNode(String s) throws RepositoryException {
         return session.itemExists(getPath().endsWith("/") ? getPath() + s : getPath() + "/" + s);
     }
 
     public boolean hasProperty(String relPath) throws RepositoryException {
         return properties.containsKey(relPath) ||
                 (data.getLazyProperties() != null && data.getLazyProperties().contains(relPath)) ||
                 (data.getLazyBinaryProperties() != null && data.getLazyBinaryProperties().contains(relPath)) ||
                 (getExtensionNode(false) != null && getPropertyDefinition(relPath) != null && canItemBeExtended(getPropertyDefinition(relPath)) && getExtensionNode(false).hasProperty(relPath));
     }
 
     public boolean hasNodes() throws RepositoryException {
         return getNodes().hasNext();
     }
 
     public boolean hasProperties() throws RepositoryException {
         return !properties.isEmpty() ||
                 (data.getLazyProperties() != null && !data.getLazyProperties().isEmpty()) ||
                 (data.getLazyBinaryProperties() != null && !data.getLazyBinaryProperties().isEmpty()) ||
                 getProperties().hasNext();
     }
 
     public NodeType getPrimaryNodeType() throws RepositoryException {
         return getExtendedPrimaryNodeType();
     }
 
     public ExtendedNodeType getExtendedPrimaryNodeType() throws RepositoryException {
         return NodeTypeRegistry.getInstance().getNodeType(data.getType());
     }
 
     public NodeType[] getMixinNodeTypes() throws RepositoryException {
         return getMixinNodeTypes(true);
     }
 
     private NodeType[] getMixinNodeTypes(boolean withExtension) throws RepositoryException {
         List<NodeType> nt = new ArrayList<NodeType>();
         if (data.getMixin() != null) {
             for (String s : data.getMixin()) {
                 nt.add(NodeTypeRegistry.getInstance().getNodeType(s));
             }
         }
         if (withExtension) {
             Node extensionNode = getExtensionNode(false);
             if (extensionNode != null) {
                 for (NodeType type : extensionNode.getMixinNodeTypes()) {
                     if (!type.isNodeType("jmix:externalProviderExtension")) {
                         nt.add(NodeTypeRegistry.getInstance().getNodeType(type.getName()));
                     }
                 }
             }
         }
         return nt.toArray(new NodeType[nt.size()]);
     }
 
     public boolean isNodeType(String nodeTypeName) throws RepositoryException {
         if (getPrimaryNodeType().isNodeType(nodeTypeName)) {
             return true;
         }
         for (NodeType nodeType : getMixinNodeTypes()) {
             if (nodeType.isNodeType(nodeTypeName)) {
                 return true;
             }
         }
         return false;
     }
 
     public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         if (isNodeType(mixinName)) {
             return;
         }
 
         Node extensionNode = getExtensionNode(true);
         if (extensionNode != null) {
             extensionNode.addMixin(mixinName);
             return;
         }
 
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         Node extensionNode = getExtensionNode(false);
         if (extensionNode != null) {
             extensionNode.removeMixin(mixinName);
 
 
             // remove child node and properties brought by the mixin
             PropertyIterator pi = getProperties();
             while (pi.hasNext()) {
                 Property extensionProperty = pi.nextProperty();
                 List<NodeType> nodeTypes = new ArrayList<NodeType>();
                 nodeTypes.addAll(Arrays.asList(getMixinNodeTypes(true)));
                 nodeTypes.add(NodeTypeRegistry.getInstance().getNodeType("jmix:externalProviderExtension"));
                 boolean canSetProperty = extensionProperty.isMultiple()?getPrimaryNodeType().canSetProperty(extensionProperty.getName(), extensionProperty.getValues()):getPrimaryNodeType().canSetProperty(extensionProperty.getName(), extensionProperty.getValue());
                 for (PropertyDefinition propertyDefinition : getPrimaryNodeType().getPropertyDefinitions()) {
                     if (propertyDefinition.getName().equals(extensionProperty.getName()) && propertyDefinition.getRequiredType() == extensionProperty.getType()) {
                         canSetProperty = true;
                         break;
                     }
                 }
                 if (!canSetProperty) {
                     for (NodeType mixinType : nodeTypes) {
                         if (!StringUtils.equals(mixinType.getName(),mixinName)) {
                             if (extensionProperty.isMultiple()) {
                                 if (mixinType.canSetProperty(extensionProperty.getName(),extensionProperty.getValues())) {
                                     canSetProperty = true;
                                     break;
                                 }
                             } else {
                                 if (mixinType.canSetProperty(extensionProperty.getName(),extensionProperty.getValue())) {
                                     canSetProperty = true;
                                     break;
                                 }
                             }
                         }
                     }
                     if (!canSetProperty) {
                         extensionProperty.remove();
                     }
                 }
             }
             NodeIterator ni = extensionNode.getNodes();
             while (ni.hasNext()) {
                 Node extensionChildNode = ni.nextNode();
                 boolean canAddChildNode = getPrimaryNodeType().canAddChildNode(extensionChildNode.getName(), getPrimaryNodeType().getName());
                 if (!canAddChildNode) {
                     for (NodeType mixinType : getMixinNodeTypes(true)) {
                         if (!StringUtils.equals(mixinType.getName(),mixinName)) {
                             if (mixinType.canAddChildNode(extensionChildNode.getName(), getPrimaryNodeType().getName())) {
                                 canAddChildNode = true;
                                 break;
                             }
                         }
                     }
                     if (!canAddChildNode) {
                         extensionChildNode.remove();
                     }
                 }
             }
             return;
         }
         if (!isNodeType(mixinName)) {
             throw new NoSuchNodeTypeException("Mixin "+mixinName + " not included in node "+getPath());
         }
 
         throw new UnsupportedRepositoryOperationException();
     }
 
     public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException {
         Node extensionNode = getExtensionNode(true);
         if (extensionNode != null) {
             return extensionNode.canAddMixin(mixinName);
         }
         return false;
     }
 
     public NodeDefinition getDefinition() throws RepositoryException {
         ExternalNodeImpl parentNode = (ExternalNodeImpl) getParent();
         ExtendedNodeType parentNodeType = parentNode.getExtendedPrimaryNodeType();
         ExtendedNodeDefinition nodeDefinition = parentNodeType.getChildNodeDefinitionsAsMap().get(getName());
         if (nodeDefinition != null) {
             return nodeDefinition;
         }
         for (Map.Entry<String, ExtendedNodeDefinition> entry : parentNodeType.getUnstructuredChildNodeDefinitions().entrySet()) {
             if (isNodeType(entry.getKey())) {
                 return entry.getValue();
             }
         }
         return null;
     }
 
     public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
     }
 
     public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void update(String s) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
         return getPath();
     }
 
     public boolean isCheckedOut() throws RepositoryException {
         return true;
     }
 
     public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public Lock lock(boolean b, boolean b1) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
         return session.getWorkspace().getLockManager().lock(getPath(),b,b1, Long.MAX_VALUE, null);
     }
 
     public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException {
         return session.getWorkspace().getLockManager()!= null ?session.getWorkspace().getLockManager().getLock(getPath()):null;
     }
 
     public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
         if (session.getWorkspace().getLockManager()!=null) {
             session.getWorkspace().getLockManager().unlock(getPath());
         }
     }
 
     public boolean holdsLock() throws RepositoryException {
         return session.getWorkspace().getLockManager()!=null && session.getWorkspace().getLockManager().getLock(getPath()) != null;
     }
 
     public boolean isLocked() throws RepositoryException {
         return  session.getWorkspace().getLockManager()!=null && session.getWorkspace().getLockManager().isLocked(getPath());
     }
 
     public Property setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             InputStream stream = value.getStream();
             try {
                 return setProperty(name, stream);
             } finally {
                 IOUtils.closeQuietly(stream);
             }
         }
         return setProperty(name, v);
     }
 
     public Property setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
         Value v = null;
         if (value != null) {
             v = getSession().getValueFactory().createValue(value);
         }
         return setProperty(name, v);
     }
 
     public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
         final List<String> l = session.getRepository().getDataSource().getChildren(getPath());
         final List<String> filteredList = new ArrayList<String>();
         for (String path : l) {
             if (ChildrenCollectorFilter.matches(path,nameGlobs)) {
                 filteredList.add(path);
             }
         }
         Set<String> languages = new HashSet<String>();
         if (data.getI18nProperties() != null) {
             languages.addAll(data.getI18nProperties().keySet());
         }
         if (data.getLazyI18nProperties() != null) {
             languages.addAll(data.getLazyI18nProperties().keySet());
         }
         for (String lang : languages) {
             if (ChildrenCollectorFilter.matches("j:translation_" + lang, nameGlobs)) {
                 filteredList.add("j:translation_" + lang);
             }
         }
 
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalNodeIterator(filteredList,n.getNodes(nameGlobs));
         }
         return new ExternalNodeIterator(filteredList);
     }
 
     public PropertyIterator getProperties(String[] nameGlobs) throws RepositoryException {
         final Map<String, ExternalPropertyImpl> filteredList = new HashMap<String, ExternalPropertyImpl>();
         for (Map.Entry<String, ExternalPropertyImpl> entry : properties.entrySet()) {
             if (ChildrenCollectorFilter.matches(entry.getKey(),nameGlobs)) {
                 filteredList.put(entry.getKey(), entry.getValue());
             }
         }
         Set<String> lazyProperties = null;
         if (data.getLazyProperties() != null) {
             lazyProperties = new HashSet<String>();
             for (String propertyName : data.getLazyProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, nameGlobs)) {
                     lazyProperties.add(propertyName);
                 }
             }
         }
         Set<String> lazyBinaryProperties = null;
         if (data.getLazyBinaryProperties() != null) {
             lazyBinaryProperties = new HashSet<String>();
             for (String propertyName : data.getLazyBinaryProperties()) {
                 if (ChildrenCollectorFilter.matches(propertyName, nameGlobs)) {
                     lazyBinaryProperties.add(propertyName);
                 }
             }
         }
         Node n = getExtensionNode(false);
         if (n != null) {
             return new ExternalPropertyIterator(filteredList, n.getProperties(nameGlobs), lazyProperties, lazyBinaryProperties, this);
         }
         return new ExternalPropertyIterator(filteredList, lazyProperties, lazyBinaryProperties, this);
     }
 
     public String getIdentifier() throws RepositoryException {
         if (uuid == null) {
             if (!session.getRepository().getDataSource().isSupportsUuid() || data.getId().startsWith(ExternalSessionImpl.TRANSLATION_PREFIX)) {
                 uuid = getStoreProvider().getInternalIdentifier(data.getId());
                 if (uuid == null) {
                     // not mapped yet -> store mapping
                     uuid = getStoreProvider().mapInternalIdentifier(data.getId());
                 }
             } else {
                 uuid = data.getId();
             }
         }
 
         return uuid;
     }
 
     public PropertyIterator getReferences(String name) throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>(), this);
     }
 
     public PropertyIterator getWeakReferences() throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>(), this);
     }
 
     public PropertyIterator getWeakReferences(String name) throws RepositoryException {
         return new ExternalPropertyIterator(new HashMap<String, ExternalPropertyImpl>(), this);
     }
 
     public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public NodeIterator getSharedSet() throws RepositoryException {
         return new ExternalNodeIterator(new ArrayList<String>());
     }
 
     public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
         throw new UnsupportedRepositoryOperationException();
     }
 
     public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
         return new String[0];
     }
 
     public Node getExtensionNode(boolean create) throws RepositoryException {
         Session extensionSession = getSession().getExtensionSession();
         if (extensionSession == null) {
             return null;
         }
         List<String> extensionAllowedTypes = getSession().getExtensionAllowedTypes();
         if (extensionAllowedTypes != null) {
             for (String type : extensionAllowedTypes) {
                 if (!isNodeType(type)) {
                     return null;
                 }
             }
         }
         String path = getPath();
         boolean isRoot = path.equals("/");
 
         String mountPoint = getStoreProvider().getMountPoint();
         String globalPath = mountPoint + (isRoot ? "" : path);
 
         if (!extensionSession.itemExists(globalPath)) {
             if (!create) {
                 return null;
             } else if (isRoot) {
                 String parent = StringUtils.substringBeforeLast(mountPoint, "/");
                 if (parent.equals("")) {
                     parent = "/";
                 }
                 final Node extParent = extensionSession.getNode(parent);
                 takeLockToken(extParent);
                 extParent.addMixin("jmix:hasExternalProviderExtension");
                 Node n = extParent.addNode(StringUtils.substringAfterLast(mountPoint, "/"), "jnt:externalProviderExtension");
                 n.setProperty("j:extendedType", getPrimaryNodeType().getName());
                 n.addMixin("jmix:externalProviderExtension");
                 n.setProperty("j:isExternalProviderRoot", true);
                 n.setProperty("j:externalNodeIdentifier", getIdentifier());
             } else {
                 final Node extParent = ((ExternalNodeImpl) getParent()).getExtensionNode(true);
                 takeLockToken(extParent);
                 Node n = extParent.addNode(getName(), "jnt:externalProviderExtension");
                 n.setProperty("j:extendedType", getPrimaryNodeType().getName());
                 n.addMixin("jmix:externalProviderExtension");
                 n.setProperty("j:isExternalProviderRoot", false);
                 n.setProperty("j:externalNodeIdentifier", getIdentifier());
             }
         }
 
         return extensionSession.getNode(globalPath);
     }
 
     public void takeLockToken(Node parentNode) throws RepositoryException {
         if (parentNode.isLocked() && parentNode.hasProperty("j:locktoken")) {
             parentNode.getSession().addLockToken(parentNode.getProperty("j:locktoken").getString());
         }
     }
 
     private boolean canItemBeExtended(ItemDefinition definition) throws RepositoryException {
         if (definition == null) {
             throw new ConstraintViolationException();
         }
 
         NodeType type = definition.getDeclaringNodeType();
 
         Map<String,List<String>> overridableProperties = getSession().getOverridableProperties();
         for (Map.Entry<String, List<String>> entry : overridableProperties.entrySet()) {
             if ((entry.getKey().equals("*") || type.getName().equals(entry.getKey())) &&
                     (entry.getValue().contains("*") || entry.getValue().contains(definition.getName()))) {
                 return true;
             }
         }
 
         if (type.isMixin()) {
             Node ext = getExtensionNode(false);
             if (ext != null) {
                 for (NodeType assignedMixin : ext.getMixinNodeTypes()) {
                     if (assignedMixin.isNodeType(type.getName())) {
                         return true;
                     }
                 }
             }
         }
 
         return false;
     }
 
     private class ExternalPropertyIterator implements PropertyIterator {
         private int pos = 0;
         private Iterator<ExternalPropertyImpl> it;
         private PropertyIterator extensionPropertiesIterator;
         private Property nextProperty = null;
         private Map<String, ExternalPropertyImpl> externalProperties;
         private Set<String> lazyProperties;
         private Iterator<String> lazyPropertiesIterator;
         private ExternalNodeImpl node;
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap, Set<String> lazyProperties,
                                  Set<String> lazyBinaryProperties, ExternalNodeImpl node) {
             this(externalPropertyMap, null, lazyProperties, lazyBinaryProperties, node);
         }
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap, ExternalNodeImpl node) {
             this(externalPropertyMap, null, null, null, node);
         }
 
         ExternalPropertyIterator(Map<String, ExternalPropertyImpl> externalPropertyMap,
                                  PropertyIterator extensionPropertiesIterator, Set<String> lazyProperties,
                                  Set<String> lazyBinaryProperties, ExternalNodeImpl node) {
             this.extensionPropertiesIterator = extensionPropertiesIterator;
             this.externalProperties = new HashMap<String, ExternalPropertyImpl>(externalPropertyMap);
             this.lazyProperties = new HashSet<String>();
             if (lazyProperties != null) {
                 this.lazyProperties.addAll(lazyProperties);
             }
             if (lazyBinaryProperties != null) {
                 this.lazyProperties.addAll(lazyBinaryProperties);
             }
             this.node = node;
             fetchNext();
         }
 
         private void fetchNext() {
             nextProperty = null;
             if (extensionPropertiesIterator != null) {
                 while (extensionPropertiesIterator.hasNext()) {
                     Property next = extensionPropertiesIterator.nextProperty();
                     try {
                         final ExtendedPropertyDefinition propertyDefinition = getPropertyDefinition(next.getName());
                         if (propertyDefinition != null && canItemBeExtended(propertyDefinition)) {
                             nextProperty = new ExtensionProperty(next, getPath() + "/" + next.getName(), node.getSession(), ExternalNodeImpl.this);
                             externalProperties.remove(next.getName());
                             lazyProperties.remove(next.getName());
                             return;
                         }
                     } catch (RepositoryException e) {
                         e.printStackTrace();
                     }
                 }
             }
             if (it == null) {
                 it = externalProperties.values().iterator();
             }
             if (it.hasNext()) {
                 nextProperty = it.next();
                 return;
             }
             if (lazyPropertiesIterator == null && lazyProperties != null) {
                 lazyPropertiesIterator = lazyProperties.iterator();
             }
             if (lazyPropertiesIterator != null && lazyPropertiesIterator.hasNext()) {
                 String propertyName = lazyPropertiesIterator.next();
                 try {
                     nextProperty = getProperty(propertyName);
                 } catch (RepositoryException e) {
                     e.printStackTrace();
                 }
                 return;
             }
         }
 
         @Override
         public Property nextProperty() {
             if (nextProperty == null) {
                 throw new NoSuchElementException();
             }
             Property next = nextProperty;
             fetchNext();
             pos ++;
             return next;
         }
 
         public void skip(long skipNum) {
             for (int i=0; i<skipNum; i++) {
                 nextProperty();
             }
         }
 
         @Override
         public long getSize() {
             return externalProperties.size() + (extensionPropertiesIterator != null ? extensionPropertiesIterator.getSize() : 0);
         }
 
         @Override
         public long getPosition() {
             return pos;
         }
 
         @Override
         public boolean hasNext() {
             return nextProperty != null;
         }
 
         @Override
         public Object next() {
             return nextProperty();
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException("remove");
         }
     }
 
     private class ExternalNodeIterator implements NodeIterator {
         private int pos = 0;
         private Iterator<String> it;
         private final List<String> list;
         private NodeIterator extensionNodeIterator;
         private Node nextNode;
 
         public ExternalNodeIterator(List<String> list) {
             this(list,null);
         }
 
         public ExternalNodeIterator(List<String> list, NodeIterator extensionNodeIterator) {
             this.extensionNodeIterator = extensionNodeIterator;
             this.list = list;
             this.it = list.iterator();
             fetchNext();
         }
 
         private Node fetchNext() {
             nextNode = null;
             if (it.hasNext()) {
                 Node next = null;
                 try {
                     next = getNode(it.next());
                 } catch (RepositoryException e) {
                     e.printStackTrace();
                 }
                 nextNode = next;
                 return nextNode;
             }
             if (extensionNodeIterator != null) {
                 while (extensionNodeIterator.hasNext()) {
                     Node n = extensionNodeIterator.nextNode();
                     try {
                         ExtendedNodeDefinition childNodeDefinition = getChildNodeDefinition(n.getName(), n.getPrimaryNodeType().getName());
                         if (childNodeDefinition == null) {
                             for (NodeType t : n.getMixinNodeTypes()) {
                                 childNodeDefinition = getChildNodeDefinition(n.getName(), t.getName());
                                 if (childNodeDefinition != null) {
                                     break;
                                 }
                             }
                         }
                         if (childNodeDefinition != null && canItemBeExtended(childNodeDefinition) && !list.contains(n.getName())) {
                             nextNode = new ExtensionNode(n,getPath() + "/" + n.getName(),getSession());
                             return  nextNode;
                         }
                     } catch (RepositoryException e) {
                         e.printStackTrace();
                     }
                 }
             }
             return null;
         }
 
         public Node nextNode() {
             if (nextNode == null) {
                 throw new NoSuchElementException();
             }
             Node next = nextNode;
             fetchNext();
             pos++;
             return next;
         }
 
         public void skip(long skipNum) {
             for (int i = 0; i<skipNum ; i++) {
                 nextNode();
             }
         }
 
         public long getSize() {
             return list.size() + (extensionNodeIterator!= null ? extensionNodeIterator.getSize():0);
         }
 
         public long getPosition() {
             return pos;
         }
 
         public boolean hasNext() {
             return nextNode != null;
         }
 
         public Object next() {
             return nextNode();
         }
 
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
