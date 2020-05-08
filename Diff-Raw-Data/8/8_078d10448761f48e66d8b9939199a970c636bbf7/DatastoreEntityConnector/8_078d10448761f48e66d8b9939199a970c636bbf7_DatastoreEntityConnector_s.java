 package com.socialcomputing.wps.server.plandictionary.connectors.datastore;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 import com.socialcomputing.wps.server.plandictionary.connectors.AttributeEnumeratorItem;
 import com.socialcomputing.wps.server.plandictionary.connectors.WPSConnectorException;
 import com.socialcomputing.wps.server.plandictionary.connectors.iAffinityGroupReader;
 import com.socialcomputing.wps.server.plandictionary.connectors.iClassifierConnector;
 import com.socialcomputing.wps.server.plandictionary.connectors.iEntityConnector;
 import com.socialcomputing.wps.server.plandictionary.connectors.iEnumerator;
 import com.socialcomputing.wps.server.plandictionary.connectors.iProfileConnector;
 import com.socialcomputing.wps.server.plandictionary.connectors.iSelectionConnector;
 
 public abstract class DatastoreEntityConnector implements iEntityConnector {
 
     private final String m_Name;
 
     public String m_Description = null;
     protected Hashtable<String, Object> m_WPSParams = null;
     protected int m_planType;
 
     protected Hashtable<String, Entity> m_Entities = new Hashtable<String, Entity>();
     protected Hashtable<String, Attribute> m_Attributes = new Hashtable<String, Attribute>();
 
     protected Set<String> entityProperties = new HashSet<String>();
     protected Set<AttributePropertyDefinition> attributeProperties = new HashSet<AttributePropertyDefinition>();
 
     protected List<DatastoreAffinityGroupReader> affinityGroupReaders = new ArrayList<DatastoreAffinityGroupReader>();
     protected List<DatastoreProfileConnector> profileConnectors = new ArrayList<DatastoreProfileConnector>();
 
     public DatastoreEntityConnector(String name) {
         this.m_Name = name;
     }
 
     public void _readObject(org.jdom.Element element) {
         m_Description = element.getChildText("comment");
         affinityGroupReaders.add(new DatastoreAffinityGroupReader());
         profileConnectors.add(new DatastoreProfileConnector());
     }
 
     @Override
     public String getName() {
         return m_Name;
     }
 
     @Override
     public String getDescription() {
         return m_Description;
     }
 
     @Override
     public void openConnections(int planType, Hashtable<String, Object> wpsparams) throws WPSConnectorException {
         m_planType = planType;
         m_WPSParams = wpsparams;
 
         for (DatastoreAffinityGroupReader affinityGroupReader : affinityGroupReaders) {
             affinityGroupReader.openConnections(this);
         }
         for (DatastoreProfileConnector profileConnector : profileConnectors) {
             profileConnector.openConnections(this);
         }
     }
 
     @Override
     public void closeConnections() throws WPSConnectorException {
         for (DatastoreAffinityGroupReader affinityGroupReader : affinityGroupReaders) {
             affinityGroupReader.closeConnections();
         }
         for (DatastoreProfileConnector profileConnector : profileConnectors) {
             profileConnector.closeConnections();
         }
     }
 
     @Override
     public Hashtable<String, Object> getProperties(String entityId) throws WPSConnectorException {
         return getEntity(entityId).getProperties();
     }
 
     @Override
     public iEnumerator<String> getEnumerator() throws WPSConnectorException {
         return new DataEnumerator<String>(m_Entities.keySet());
     }
 
     @Override
     public Collection getAffinityGroupReaders() {
         return affinityGroupReaders;
     }
 
     @Override
     public iAffinityGroupReader getAffinityGroupReader(String affGrpReader) {
         return affinityGroupReaders.get(0);
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     @Override
     public Collection getProfiles() throws WPSConnectorException {
         return profileConnectors;
     }
 
     @Override
     public iProfileConnector getProfile(String profile) throws WPSConnectorException {
         return profileConnectors.get(0);
     }
 
     @Override
     public Collection<iClassifierConnector> getClassifiers() throws WPSConnectorException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public iClassifierConnector getClassifier(String classifierId) throws WPSConnectorException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Collection<iSelectionConnector> getSelections() throws WPSConnectorException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public iSelectionConnector getSelection(String selectionId) throws WPSConnectorException {
         // TODO Auto-generated method stub
         return null;
     }
 
     protected Entity getEntity(String id) {
         return m_Entities.get(id);
     }
 
     protected Entity addEntity(String id) {
         Entity entity = getEntity(id);
         if (entity == null) {
             entity = new Entity(id);
             m_Entities.put(id, entity);
         }
         return entity;
     }
 
     protected void removeEntity(String id) {
        Entity entity = getEntity( id);
         if( id != null) {
             AttributeEnumeratorItem item = new AttributeEnumeratorItem( id, 0);
            if( entity.m_Attributes.contains( item)) {
                entity.m_Attributes.remove( item);
             }
             m_Entities.remove( id);
         }
     }
     
     protected Attribute getAttribute(String id) {
         return m_Attributes.get(id);
     }
 
     protected Attribute addAttribute(String id) {
         Attribute attribute = getAttribute(id);
         if (attribute == null) {
             attribute = new Attribute(id);
             m_Attributes.put(id, attribute);
         }
         return attribute;
     }
 
     public void addEntityProperties(Attribute attribute) {
         for (AttributePropertyDefinition propDefinition : attributeProperties) {
             if (!propDefinition.isSimple()) {
                 ArrayList<String> property = new ArrayList<String>();
                 for (String entityId : attribute.m_Entities) {
                     Entity entity = m_Entities.get(entityId);
                     if( entity != null) {
                         String value = (String) entity.getProperties().get(propDefinition.getEntity());
                         if (value != null)
                             property.add(value);
                     }
                 }
                 attribute.addProperty(propDefinition, property.toArray(new String[property.size()]));
             }
         }
     }
 }
