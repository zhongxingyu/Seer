 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToPropertyTag;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedContentSpec;
 import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.provider.DBProviderFactory;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBCSNodeCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.DBContentSpecToPropertyTagCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.base.UpdateableCollectionEventListener;
 
 public class DBContentSpecWrapper extends DBBaseWrapper<ContentSpecWrapper, ContentSpec> implements ContentSpecWrapper {
     private final CSNodeCollectionEventListener csNodeCollectionEventListener = new CSNodeCollectionEventListener();
     private final PropertyCollectionEventListener propertyCollectionEventListener = new PropertyCollectionEventListener();
 
     private final ContentSpec contentSpec;
 
     public DBContentSpecWrapper(final DBProviderFactory providerFactory, final ContentSpec contentSpec, boolean isRevision) {
         super(providerFactory, isRevision, ContentSpec.class);
         this.contentSpec = contentSpec;
     }
 
     @Override
     protected ContentSpec getEntity() {
         return contentSpec;
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTags() {
         return getWrapperFactory().createCollection(getEntity().getTags(), Tag.class, isRevisionEntity());
     }
 
     @Override
     public void setTags(CollectionWrapper<TagWrapper> tags) {
         if (tags == null) return;
 
         final List<Tag> unwrappedTags = (List<Tag>) tags.unwrap();
         getEntity().getContentSpecToTags().clear();
         for (final Tag tag : unwrappedTags) {
             getEntity().addTag(tag);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getChildren() {
         final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(getEntity().getTopCSNodes(), CSNode.class,
                 isRevisionEntity());
         final DBCSNodeCollectionWrapper dbCollection = (DBCSNodeCollectionWrapper) collection;
         dbCollection.registerEventListener(csNodeCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setChildren(UpdateableCollectionWrapper<CSNodeWrapper> nodes) {
         if (nodes == null) return;
         final DBCSNodeCollectionWrapper dbNodes = (DBCSNodeCollectionWrapper) nodes;
         dbNodes.registerEventListener(csNodeCollectionEventListener);
 
         // Remove the current children
         final List<CSNode> children = getEntity().getTopCSNodes();
         for (final CSNode child : children) {
             getEntity().removeChild(child);
         }
 
         // Set the new children
         final Collection<CSNode> newChildren = dbNodes.unwrap();
         for (final CSNode child : newChildren) {
             getEntity().addChild(child);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> getProperties() {
         final CollectionWrapper<PropertyTagInContentSpecWrapper> collection = getWrapperFactory().createCollection(
                 getEntity().getContentSpecToPropertyTags(), ContentSpecToPropertyTag.class, isRevisionEntity());
         final DBContentSpecToPropertyTagCollectionWrapper dbCollection = (DBContentSpecToPropertyTagCollectionWrapper) collection;
         dbCollection.registerEventListener(propertyCollectionEventListener);
         return dbCollection;
     }
 
     @Override
     public void setProperties(UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> properties) {
         if (properties == null) return;
         final DBContentSpecToPropertyTagCollectionWrapper dbProperties = (DBContentSpecToPropertyTagCollectionWrapper) properties;
         dbProperties.registerEventListener(propertyCollectionEventListener);
 
         // Remove the current properties
         final Set<ContentSpecToPropertyTag> propertyTags = new HashSet<ContentSpecToPropertyTag>(getEntity().getContentSpecToPropertyTags
                 ());
         for (final ContentSpecToPropertyTag propertyTag : propertyTags) {
             getEntity().removePropertyTag(propertyTag);
         }
 
         // Set the new properties
         final Collection<ContentSpecToPropertyTag> newPropertyTags = dbProperties.unwrap();
         for (final ContentSpecToPropertyTag propertyTag : newPropertyTags) {
            propertyTag.setContentSpec(getEntity());
             getEntity().addPropertyTag(propertyTag);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecs() {
         return getWrapperFactory().createCollection(getEntity().getTranslatedContentSpecs(getEntityManager(), getRevision()),
                 TranslatedContentSpec.class, isRevisionEntity());
     }
 
     @Override
     public String getTitle() {
         final CSNode titleNode = getEntity().getContentSpecTitle();
         return titleNode == null ? null : titleNode.getAdditionalText();
     }
 
     @Override
     public String getProduct() {
         final CSNode productNode = getEntity().getContentSpecProduct();
         return productNode == null ? null : productNode.getAdditionalText();
     }
 
     @Override
     public String getVersion() {
         final CSNode versionNode = getEntity().getContentSpecVersion();
         return versionNode == null ? null : versionNode.getAdditionalText();
     }
 
     @Override
     public String getLocale() {
         return getEntity().getLocale();
     }
 
     @Override
     public void setLocale(String locale) {
         getEntity().setLocale(locale);
     }
 
     @Override
     public Integer getType() {
         return getEntity().getContentSpecType();
     }
 
     @Override
     public void setType(Integer typeId) {
         getEntity().setContentSpecType(typeId);
     }
 
     @Override
     public String getCondition() {
         return getEntity().getCondition();
     }
 
     @Override
     public void setCondition(String condition) {
         getEntity().setCondition(condition);
     }
 
     @Override
     public Date getLastModified() {
         return EnversUtilities.getFixedLastModifiedDate(getEntityManager(), getEntity());
     }
 
     @Override
     public PropertyTagInContentSpecWrapper getProperty(int propertyId) {
         return getWrapperFactory().create(getEntity().getProperty(propertyId), isRevisionEntity());
     }
 
     @Override
     public CSNodeWrapper getMetaData(String metaDataTitle) {
         return getWrapperFactory().create(getEntity().getMetaData(metaDataTitle), isRevisionEntity());
     }
 
     @Override
     public void setId(Integer id) {
         getEntity().setContentSpecId(id);
     }
 
     @Override
     public ContentSpec unwrap() {
         return contentSpec;
     }
 
     /**
      *
      */
     private class CSNodeCollectionEventListener implements UpdateableCollectionEventListener<CSNode> {
         @Override
         public void onAddItem(final CSNode entity) {
             getEntity().addChild(entity);
         }
 
         @Override
         public void onRemoveItem(final CSNode entity) {
             getEntity().removeChild(entity);
         }
 
         @Override
         public void onUpdateItem(final CSNode entity) {
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof CSNodeCollectionEventListener;
         }
     }
 
     /**
      *
      */
     private class PropertyCollectionEventListener implements UpdateableCollectionEventListener<ContentSpecToPropertyTag> {
         @Override
         public void onAddItem(final ContentSpecToPropertyTag entity) {
             getEntity().addPropertyTag(entity);
         }
 
         @Override
         public void onRemoveItem(final ContentSpecToPropertyTag entity) {
             getEntity().removePropertyTag(entity);
         }
 
         @Override
         public void onUpdateItem(final ContentSpecToPropertyTag entity) {
         }
 
         @Override
         public boolean equals(Object o) {
             return o instanceof PropertyCollectionEventListener;
         }
     }
 }
