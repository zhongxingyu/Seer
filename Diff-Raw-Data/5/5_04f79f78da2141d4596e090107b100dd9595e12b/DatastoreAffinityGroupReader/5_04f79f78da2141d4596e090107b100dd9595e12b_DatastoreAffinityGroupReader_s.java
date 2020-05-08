 package com.socialcomputing.wps.server.plandictionary.connectors.datastore;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.jdom.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.socialcomputing.wps.server.planDictionnary.connectors.AttributeEnumeratorItem;
 import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
 import com.socialcomputing.wps.server.plandictionary.AnalysisProfile;
 import com.socialcomputing.wps.server.plandictionary.connectors.iAffinityGroupReader;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.file.FileEntityConnector;
 import com.socialcomputing.wps.server.utils.StringAndFloat;
 
 public class DatastoreAffinityGroupReader implements iAffinityGroupReader {
 	
     private final static Logger LOG = LoggerFactory.getLogger(DatastoreAffinityGroupReader.class);
     
     protected DatastoreEntityConnector m_entityConnector = null;
 	
 	static DatastoreAffinityGroupReader readObject( Element element)
 	{
 		DatastoreAffinityGroupReader grp = new DatastoreAffinityGroupReader();
 		return grp;
 	}
 	
 	public void openConnections( DatastoreEntityConnector entityConnector)  {
 		m_entityConnector = entityConnector;
 	}
 
 	public void closeConnections() {
 	}
 	
 	@Override
 	public StringAndFloat[] retrieveAffinityGroup(String id, int affinityThreshold, int max) throws JMIException {
 		StringAndFloat[] result = null;
 		float maxPond = Float.MIN_VALUE;
 		int i = 0;
 		switch( m_entityConnector.m_planType)
 		{
 			case AnalysisProfile.GLOBAL_PLAN:
 			    LOG.debug("global plan");
 			    if( m_entityConnector.isInverted()) {
                     result = new StringAndFloat[  m_entityConnector.getAttributes().size()];
                     for( String id2 : m_entityConnector.getAttributes().keySet()) {
                         result[i++] = new StringAndFloat( id2, 1);
                     }
 			    }
 			    else {
     				result = new StringAndFloat[  m_entityConnector.getEntities().size()];
     				for( String id2 : m_entityConnector.getEntities().keySet()) {
     					result[i++] = new StringAndFloat( id2, 1);
     				}
 			    }
 				break;
 				
 			case AnalysisProfile.PERSONAL_PLAN:
 			    LOG.debug("Personal plan with id = {}", id);
 				Map<String, Integer> set = new HashMap<String, Integer> ();
                 if( m_entityConnector.isInverted()) {
                     if( m_entityConnector.getAttribute(id) == null) 
                         throw new JMIException( "Unknonwn entity id " + id);
                     for(String entityId2 : m_entityConnector.getAttribute(id).getEntities()) {
                         for( AttributeEnumeratorItem attributeItem : m_entityConnector.getEntity(entityId2).getAttributes()) {
                             if( set.containsKey( attributeItem.m_Id)) {
                                 int pond = set.get( attributeItem.m_Id) + 1;
                                 set.put( attributeItem.m_Id, pond);
                                 if( pond > maxPond)
                                     maxPond = pond;
                             }
                             else {
                                 set.put( attributeItem.m_Id, 1);
                                 if( maxPond < 1)
                                     maxPond = 1;
                             }
                         }
                     }
                 }
                 else {
     				if( m_entityConnector.getEntity(id) == null) 
     				    throw new JMIException( "Unknonwn entity id " + id);
     				for(AttributeEnumeratorItem attributeItem : m_entityConnector.getEntity(id).getAttributes()) {
     					for( String entityId2 : m_entityConnector.getAttribute(attributeItem.m_Id).getEntities()) {
     						if( set.containsKey( entityId2)) {
     							int pond = set.get( entityId2) + 1;
     							set.put( entityId2, pond);
     							if( pond > maxPond)
     								maxPond = pond;
     						}
     						else {
     							set.put( entityId2, 1);
     							if( maxPond < 1)
     								maxPond = 1;
     						}
     					}
     				}
                 }
 				result = new StringAndFloat[ set.size()];
 				for( Entry<String, Integer> entry : set.entrySet()) {
					result[i++] = new StringAndFloat( entry.getKey(), (maxPond - entry.getValue()) / maxPond);
 				}
 				break;
 				
 			case AnalysisProfile.DISCOVERY_PLAN:
 			    LOG.debug("discovery plan with id = {}", id);
 				Map<String,Integer> set2 = new HashMap<String,Integer>();
                 if( m_entityConnector.isInverted()) {
                     if( m_entityConnector.getEntity(id) == null) 
                         throw new JMIException( "Unknonwn attribute id " + id);
                     for(AttributeEnumeratorItem attributeItem : m_entityConnector.getEntity(id).getAttributes()) {
                         if( set2.containsKey( attributeItem.m_Id)) {
                             int pond = set2.get( attributeItem.m_Id) + 1;
                             set2.put( attributeItem.m_Id, pond);
                             if( pond > maxPond)
                                 maxPond = pond;
                         }
                         else {
                             set2.put( attributeItem.m_Id, 1);
                             if( maxPond < 1)
                                 maxPond = 1;
                         }
                     }
                 }
                 else {
     				if( m_entityConnector.getAttribute(id) == null) 
     				    throw new JMIException( "Unknonwn attribute id " + id);
     				for( String entityId : m_entityConnector.getAttribute(id).getEntities()) {
     					if( set2.containsKey( entityId)) {
     						int pond = set2.get( entityId) + 1;
     						set2.put( entityId, pond);
     						if( pond > maxPond)
     							maxPond = pond;
     					}
     					else {
     						set2.put( entityId, 1);
     						if( maxPond < 1)
     							maxPond = 1;
     					}
     				}
                 }
 				result = new StringAndFloat[ set2.size()];
 				for( Entry<String, Integer> entry : set2.entrySet()) {
					result[i++] = new StringAndFloat( entry.getKey(), (maxPond - entry.getValue()) / maxPond);
 				}
 				break;
 		}
 		return result;
 	}
 }
