 // DO NOT EDIT.  Make changes to SyncInfo.java instead.
 package com.wowodc12.blogdemo.model;
 
 import com.webobjects.eoaccess.*;
 import com.webobjects.eocontrol.*;
 import com.webobjects.foundation.*;
 import java.math.*;
 import java.util.*;
 import org.apache.log4j.Logger;
 
 import er.extensions.eof.*;
 import er.extensions.foundation.*;
 
 @SuppressWarnings("all")
 public abstract class _SyncInfo extends  ERXGenericRecord {
   public static final String ENTITY_NAME = "SyncInfo";
 
   // Attribute Keys
   public static final ERXKey<String> DELEGATED_PRIMARY_KEY_VALUE = new ERXKey<String>("delegatedPrimaryKeyValue");
   public static final ERXKey<String> ETAG = new ERXKey<String>("etag");
   public static final ERXKey<NSTimestamp> LAST_MODIFIED = new ERXKey<NSTimestamp>("lastModified");
   public static final ERXKey<com.wowodc12.blogdemo.enums.SyncInfoStatus> STATE = new ERXKey<com.wowodc12.blogdemo.enums.SyncInfoStatus>("state");
   // Relationship Keys
   public static final ERXKey<com.wowodc12.blogdemo.model.DelegatePKHistory> DELEGATE_PK_HISTORIES = new ERXKey<com.wowodc12.blogdemo.model.DelegatePKHistory>("delegatePKHistories");
 
   // Attributes
   public static final String DELEGATED_PRIMARY_KEY_VALUE_KEY = DELEGATED_PRIMARY_KEY_VALUE.key();
   public static final String ETAG_KEY = ETAG.key();
   public static final String LAST_MODIFIED_KEY = LAST_MODIFIED.key();
   public static final String STATE_KEY = STATE.key();
   // Relationships
   public static final String DELEGATE_PK_HISTORIES_KEY = DELEGATE_PK_HISTORIES.key();
 
   private static Logger LOG = Logger.getLogger(_SyncInfo.class);
 
   public SyncInfo localInstanceIn(EOEditingContext editingContext) {
     SyncInfo localInstance = (SyncInfo)EOUtilities.localInstanceOfObject(editingContext, this);
     if (localInstance == null) {
       throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
     }
     return localInstance;
   }
 
   public String delegatedPrimaryKeyValue() {
     return (String) storedValueForKey(_SyncInfo.DELEGATED_PRIMARY_KEY_VALUE_KEY);
   }
 
   public void setDelegatedPrimaryKeyValue(String value) {
     if (_SyncInfo.LOG.isDebugEnabled()) {
     	_SyncInfo.LOG.debug( "updating delegatedPrimaryKeyValue from " + delegatedPrimaryKeyValue() + " to " + value);
     }
     takeStoredValueForKey(value, _SyncInfo.DELEGATED_PRIMARY_KEY_VALUE_KEY);
   }
 
   public String etag() {
     return (String) storedValueForKey(_SyncInfo.ETAG_KEY);
   }
 
   public void setEtag(String value) {
     if (_SyncInfo.LOG.isDebugEnabled()) {
     	_SyncInfo.LOG.debug( "updating etag from " + etag() + " to " + value);
     }
     takeStoredValueForKey(value, _SyncInfo.ETAG_KEY);
   }
 
   public NSTimestamp lastModified() {
     return (NSTimestamp) storedValueForKey(_SyncInfo.LAST_MODIFIED_KEY);
   }
 
   public void setLastModified(NSTimestamp value) {
     if (_SyncInfo.LOG.isDebugEnabled()) {
     	_SyncInfo.LOG.debug( "updating lastModified from " + lastModified() + " to " + value);
     }
     takeStoredValueForKey(value, _SyncInfo.LAST_MODIFIED_KEY);
   }
 
   public com.wowodc12.blogdemo.enums.SyncInfoStatus state() {
     return (com.wowodc12.blogdemo.enums.SyncInfoStatus) storedValueForKey(_SyncInfo.STATE_KEY);
   }
 
   public void setState(com.wowodc12.blogdemo.enums.SyncInfoStatus value) {
     if (_SyncInfo.LOG.isDebugEnabled()) {
     	_SyncInfo.LOG.debug( "updating state from " + state() + " to " + value);
     }
     takeStoredValueForKey(value, _SyncInfo.STATE_KEY);
   }
 
   public NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory> delegatePKHistories() {
     return (NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory>)storedValueForKey(_SyncInfo.DELEGATE_PK_HISTORIES_KEY);
   }
 
   public NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory> delegatePKHistories(EOQualifier qualifier) {
     return delegatePKHistories(qualifier, null, false);
   }
 
   public NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory> delegatePKHistories(EOQualifier qualifier, boolean fetch) {
     return delegatePKHistories(qualifier, null, fetch);
   }
 
   public NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory> delegatePKHistories(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
     NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory> results;
     if (fetch) {
       EOQualifier fullQualifier;
       EOQualifier inverseQualifier = new EOKeyValueQualifier(com.wowodc12.blogdemo.model.DelegatePKHistory.SYNC_INFO_KEY, EOQualifier.QualifierOperatorEqual, this);
     	
       if (qualifier == null) {
         fullQualifier = inverseQualifier;
       }
       else {
         NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
         qualifiers.addObject(qualifier);
         qualifiers.addObject(inverseQualifier);
         fullQualifier = new EOAndQualifier(qualifiers);
       }
 
       results = com.wowodc12.blogdemo.model.DelegatePKHistory.fetchDelegatePKHistories(editingContext(), fullQualifier, sortOrderings);
     }
     else {
       results = delegatePKHistories();
       if (qualifier != null) {
         results = (NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
       }
       if (sortOrderings != null) {
         results = (NSArray<com.wowodc12.blogdemo.model.DelegatePKHistory>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
       }
     }
     return results;
   }
   
   public void addToDelegatePKHistories(com.wowodc12.blogdemo.model.DelegatePKHistory object) {
     includeObjectIntoPropertyWithKey(object, _SyncInfo.DELEGATE_PK_HISTORIES_KEY);
   }
 
   public void removeFromDelegatePKHistories(com.wowodc12.blogdemo.model.DelegatePKHistory object) {
     excludeObjectFromPropertyWithKey(object, _SyncInfo.DELEGATE_PK_HISTORIES_KEY);
   }
 
   public void addToDelegatePKHistoriesRelationship(com.wowodc12.blogdemo.model.DelegatePKHistory object) {
     if (_SyncInfo.LOG.isDebugEnabled()) {
       _SyncInfo.LOG.debug("adding " + object + " to delegatePKHistories relationship");
     }
     if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
     	addToDelegatePKHistories(object);
     }
     else {
     	addObjectToBothSidesOfRelationshipWithKey(object, _SyncInfo.DELEGATE_PK_HISTORIES_KEY);
     }
   }
 
   public void removeFromDelegatePKHistoriesRelationship(com.wowodc12.blogdemo.model.DelegatePKHistory object) {
     if (_SyncInfo.LOG.isDebugEnabled()) {
       _SyncInfo.LOG.debug("removing " + object + " from delegatePKHistories relationship");
     }
     if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
     	removeFromDelegatePKHistories(object);
     }
     else {
     	removeObjectFromBothSidesOfRelationshipWithKey(object, _SyncInfo.DELEGATE_PK_HISTORIES_KEY);
     }
   }
 
   public com.wowodc12.blogdemo.model.DelegatePKHistory createDelegatePKHistoriesRelationship() {
     EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( com.wowodc12.blogdemo.model.DelegatePKHistory.ENTITY_NAME );
     EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
     editingContext().insertObject(eo);
     addObjectToBothSidesOfRelationshipWithKey(eo, _SyncInfo.DELEGATE_PK_HISTORIES_KEY);
     return (com.wowodc12.blogdemo.model.DelegatePKHistory) eo;
   }
 
   public void deleteDelegatePKHistoriesRelationship(com.wowodc12.blogdemo.model.DelegatePKHistory object) {
     removeObjectFromBothSidesOfRelationshipWithKey(object, _SyncInfo.DELEGATE_PK_HISTORIES_KEY);
     editingContext().deleteObject(object);
   }
 
   public void deleteAllDelegatePKHistoriesRelationships() {
     Enumeration<com.wowodc12.blogdemo.model.DelegatePKHistory> objects = delegatePKHistories().immutableClone().objectEnumerator();
     while (objects.hasMoreElements()) {
       deleteDelegatePKHistoriesRelationship(objects.nextElement());
     }
   }
 
 
  public static SyncInfo createSyncInfo(EOEditingContext editingContext, String delegatedPrimaryKeyValue
, String etag
 , NSTimestamp lastModified
 , com.wowodc12.blogdemo.enums.SyncInfoStatus state
 ) {
     SyncInfo eo = (SyncInfo) EOUtilities.createAndInsertInstance(editingContext, _SyncInfo.ENTITY_NAME);    
		eo.setDelegatedPrimaryKeyValue(delegatedPrimaryKeyValue);
 		eo.setEtag(etag);
 		eo.setLastModified(lastModified);
 		eo.setState(state);
     return eo;
   }
 
   public static ERXFetchSpecification<SyncInfo> fetchSpec() {
     return new ERXFetchSpecification<SyncInfo>(_SyncInfo.ENTITY_NAME, null, null, false, true, null);
   }
 
   public static NSArray<SyncInfo> fetchAllSyncInfos(EOEditingContext editingContext) {
     return _SyncInfo.fetchAllSyncInfos(editingContext, null);
   }
 
   public static NSArray<SyncInfo> fetchAllSyncInfos(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
     return _SyncInfo.fetchSyncInfos(editingContext, null, sortOrderings);
   }
 
   public static NSArray<SyncInfo> fetchSyncInfos(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
     ERXFetchSpecification<SyncInfo> fetchSpec = new ERXFetchSpecification<SyncInfo>(_SyncInfo.ENTITY_NAME, qualifier, sortOrderings);
     fetchSpec.setIsDeep(true);
     NSArray<SyncInfo> eoObjects = fetchSpec.fetchObjects(editingContext);
     return eoObjects;
   }
 
   public static SyncInfo fetchSyncInfo(EOEditingContext editingContext, String keyName, Object value) {
     return _SyncInfo.fetchSyncInfo(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
   }
 
   public static SyncInfo fetchSyncInfo(EOEditingContext editingContext, EOQualifier qualifier) {
     NSArray<SyncInfo> eoObjects = _SyncInfo.fetchSyncInfos(editingContext, qualifier, null);
     SyncInfo eoObject;
     int count = eoObjects.count();
     if (count == 0) {
       eoObject = null;
     }
     else if (count == 1) {
       eoObject = eoObjects.objectAtIndex(0);
     }
     else {
       throw new IllegalStateException("There was more than one SyncInfo that matched the qualifier '" + qualifier + "'.");
     }
     return eoObject;
   }
 
   public static SyncInfo fetchRequiredSyncInfo(EOEditingContext editingContext, String keyName, Object value) {
     return _SyncInfo.fetchRequiredSyncInfo(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
   }
 
   public static SyncInfo fetchRequiredSyncInfo(EOEditingContext editingContext, EOQualifier qualifier) {
     SyncInfo eoObject = _SyncInfo.fetchSyncInfo(editingContext, qualifier);
     if (eoObject == null) {
       throw new NoSuchElementException("There was no SyncInfo that matched the qualifier '" + qualifier + "'.");
     }
     return eoObject;
   }
 
   public static SyncInfo localInstanceIn(EOEditingContext editingContext, SyncInfo eo) {
     SyncInfo localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
     if (localInstance == null && eo != null) {
       throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
     }
     return localInstance;
   }
 }
