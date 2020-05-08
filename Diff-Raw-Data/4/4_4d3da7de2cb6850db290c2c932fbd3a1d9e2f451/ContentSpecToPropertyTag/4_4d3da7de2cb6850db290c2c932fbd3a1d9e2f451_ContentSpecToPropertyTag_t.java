 package org.jboss.pressgang.ccms.model.contentspec;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.Cacheable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Query;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.AuditReader;
 import org.hibernate.envers.AuditReaderFactory;
 import org.hibernate.envers.Audited;
 import org.hibernate.envers.query.AuditEntity;
 import org.hibernate.envers.query.AuditQuery;
 import org.hibernate.envers.query.AuditQueryCreator;
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.TopicToPropertyTag;
 import org.jboss.pressgang.ccms.model.base.ToPropertyTag;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "ContentSpecToPropertyTag")
 public class ContentSpecToPropertyTag extends ToPropertyTag<ContentSpecToPropertyTag> implements Serializable {
     private static final long serialVersionUID = 7567494908179498295L;
     public static String SELECT_ALL_QUERY = "SELECT contentSpecToPropertyTag FROM ContentSpecToPropertyTag AS contentSpecToPropertyTag";
     public static String SELECT_SIZE_QUERY = "SELECT COUNT(contentSpecToPropertyTag) FROM ContentSpecToPropertyTag AS " +
             "contentSpecToPropertyTag";
 
     private Integer contentSpecToPropertyTagId = null;
     private ContentSpec contentSpec = null;
 
     @Override
     @Transient
     public Integer getId() {
         return contentSpecToPropertyTagId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "ContentSpecToPropertyTagID", unique = true, nullable = false)
     public Integer getContentSpecToPropertyTagId() {
         return contentSpecToPropertyTagId;
     }
 
     public void setContentSpecToPropertyTagId(Integer contentSpecToPropertyTagId) {
         this.contentSpecToPropertyTagId = contentSpecToPropertyTagId;
     }
 
     @ManyToOne
     @JoinColumn(name = "ContentSpecID", nullable = false)
     @NotNull
     public ContentSpec getContentSpec() {
         return contentSpec;
     }
 
     public void setContentSpec(ContentSpec contentSpec) {
         this.contentSpec = contentSpec;
     }
 
     @Override
     @ManyToOne
     @JoinColumn(name = "PropertyTagID", nullable = false)
     @NotNull
     public PropertyTag getPropertyTag() {
         return propertyTag;
     }
 
     @Override
     public void setPropertyTag(final PropertyTag propertyTag) {
         this.propertyTag = propertyTag;
     }
 
     @Override
     @Column(name = "Value", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getValue() {
         return value;
     }
 
     @Override
     public void setValue(final String value) {
         this.value = value;
     }
 
     @Override
     protected boolean testUnique(final EntityManager entityManager, final Number revision) {
         if (propertyTag.getPropertyTagIsUnique()) {
             /*
              * Since having to iterate over thousands of entities is slow, use a HQL query to find the count for us.
              */
             final Long count;
             if (revision == null) {
                final String query = ContentSpecToPropertyTag.SELECT_SIZE_QUERY + " WHERE contentSpecToPropertyTag.propertyTag" +
                        ".propertyTagId = :propertyTagId AND contentSpecToPropertyTag.value = :value";
                 final Query entityQuery = entityManager.createQuery(query);
                 entityQuery.setParameter("value", getValue());
                 entityQuery.setParameter("propertyTagId", getPropertyTag().getId());
                 count = (Long) entityQuery.getSingleResult();
             } else {
                 final AuditReader reader = AuditReaderFactory.get(entityManager);
                 final AuditQueryCreator queryCreator = reader.createQuery();
                 final AuditQuery query = queryCreator.forEntitiesAtRevision(TopicToPropertyTag.class, revision).addProjection(
                         AuditEntity.id().count("contentSpecToPropertyTagId")).add(
                         AuditEntity.relatedId("propertyTag").eq(getPropertyTag().getId())).add(
                         AuditEntity.property("value").eq(getValue()));
                 query.setCacheable(true);
                 count = (Long) query.getSingleResult();
             }
 
             if (count > 1) return false;
         }
 
         return true;
     }
 
 }
