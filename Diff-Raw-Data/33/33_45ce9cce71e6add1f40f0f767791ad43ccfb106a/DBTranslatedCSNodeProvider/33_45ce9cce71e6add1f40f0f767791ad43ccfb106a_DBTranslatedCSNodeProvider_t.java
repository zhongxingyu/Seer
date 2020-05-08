 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedCSNode;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedCSNodeString;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.wrapper.DBTranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeStringWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class DBTranslatedCSNodeProvider extends DBDataProvider implements TranslatedCSNodeProvider {
     protected DBTranslatedCSNodeProvider(EntityManager entityManager, DBWrapperFactory wrapperFactory, List<ProviderListener> listeners) {
         super(entityManager, wrapperFactory, listeners);
     }
 
     @Override
     public TranslatedCSNodeWrapper getTranslatedCSNode(int id) {
         return getWrapperFactory().create(getEntity(TranslatedCSNode.class, id), false);
     }
 
     @Override
     public TranslatedCSNodeWrapper getTranslatedCSNode(int id, Integer revision) {
         if (revision == null) {
             return getTranslatedCSNode(id);
         } else {
             return getWrapperFactory().create(getRevisionEntity(TranslatedCSNode.class, id, revision), true);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedCSNodeStringWrapper> getTranslatedCSNodeStrings(int id, Integer revision) {
         final DBTranslatedCSNodeWrapper translatedNode = (DBTranslatedCSNodeWrapper) getTranslatedCSNode(id, revision);
         if (translatedNode == null) {
             return null;
         } else {
             final CollectionWrapper<TranslatedCSNodeStringWrapper> collection = getWrapperFactory().createCollection(
                     translatedNode.unwrap().getTranslatedCSNodeStrings(), TranslatedCSNodeString.class, revision != null);
             return (UpdateableCollectionWrapper<TranslatedCSNodeStringWrapper>) collection;
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedCSNodeWrapper> getTranslatedCSNodeRevisions(int id, Integer revision) {
         final List<TranslatedCSNode> revisions = getRevisionList(TranslatedCSNode.class, id);
         return getWrapperFactory().createCollection(revisions, TranslatedCSNode.class, revision != null);
     }
 
     @Override
     public CollectionWrapper<TranslatedCSNodeWrapper> createTranslatedCSNodes(
             CollectionWrapper<TranslatedCSNodeWrapper> nodes) {
         // Send the notification events
         notifyCreateEntityCollection(nodes);
 
         // Persist the new entities
         for (final TranslatedCSNodeWrapper topic : nodes.getItems()) {
            getEntityManager().persist(topic.unwrap());
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return nodes;
     }
 
     @Override
     public TranslatedCSNodeWrapper newTranslatedCSNode() {
         return getWrapperFactory().create(new TranslatedCSNode(), false, TranslatedCSNodeWrapper.class);
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedCSNodeWrapper> newTranslatedCSNodeCollection() {
         final CollectionWrapper<TranslatedCSNodeWrapper> collection = getWrapperFactory().createCollection(
                 new ArrayList<TranslatedCSNode>(), TranslatedCSNode.class, false, TranslatedCSNodeWrapper.class);
         return (UpdateableCollectionWrapper<TranslatedCSNodeWrapper>) collection;
     }
 }
