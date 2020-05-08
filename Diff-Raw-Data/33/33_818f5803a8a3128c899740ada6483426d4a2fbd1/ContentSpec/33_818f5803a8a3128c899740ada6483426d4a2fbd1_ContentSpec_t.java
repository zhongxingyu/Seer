 package org.jboss.pressgang.ccms.model.contentspec;
 
 import static ch.lambdaj.Lambda.filter;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static javax.persistence.GenerationType.IDENTITY;
 import static org.hamcrest.Matchers.equalTo;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.TagToCategory;
 import org.jboss.pressgang.ccms.model.base.ParentToPropertyTag;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.exceptions.CustomConstraintViolationException;
 import org.jboss.pressgang.ccms.model.sort.TagIDComparator;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "ContentSpec")
 public class ContentSpec extends ParentToPropertyTag<ContentSpec, ContentSpecToPropertyTag> implements Serializable {
     private static final long serialVersionUID = 5229054857631287690L;
     public static final String SELECT_ALL_QUERY = "select contentSpec from ContentSpec as contentSpec";
 
     private Integer contentSpecId = null;
     private Integer contentSpecType = CommonConstants.CS_BOOK;
     private String locale = CommonConstants.DEFAULT_LOCALE;
     private String condition = null;
     private Date lastPublished = null;
     private Date lastModified = null;
     private String errors = null;
     private String failedContentSpec = null;
     private Set<ContentSpecToPropertyTag> contentSpecToPropertyTags = new HashSet<ContentSpecToPropertyTag>(0);
     private Set<CSNode> csNodes = new HashSet<CSNode>(0);
     private Set<ContentSpecToTag> contentSpecToTags = new HashSet<ContentSpecToTag>(0);
 
     @Override
     @Transient
     public Integer getId() {
         return contentSpecId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "ContentSpecID", unique = true, nullable = false)
     public Integer getContentSpecId() {
         return contentSpecId;
     }
 
     public void setContentSpecId(Integer contentSpecId) {
         this.contentSpecId = contentSpecId;
     }
 
     @Column(name = "Locale", nullable = false, length = 255)
     @NotNull(message = "{contentspec.locale.notBlank}")
     @NotBlank(message = "{contentspec.locale.notBlank}")
     public String getLocale() {
         return locale == null ? CommonConstants.DEFAULT_LOCALE : locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 
     @Column(name = "GlobalCondition", nullable = true, length = 255)
     public String getCondition() {
         return condition;
     }
 
     public void setCondition(String condition) {
         this.condition = condition;
     }
 
     @Column(name = "ContentSpecType", nullable = false, columnDefinition = "TINYINT")
    @NotNull(message = "contentspec.type.notNull")
     public Integer getContentSpecType() {
         return contentSpecType;
     }
 
     public void setContentSpecType(Integer contentSpecType) {
         this.contentSpecType = contentSpecType;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "contentSpec", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<ContentSpecToPropertyTag> getContentSpecToPropertyTags() {
         return contentSpecToPropertyTags;
     }
 
     public void setContentSpecToPropertyTags(Set<ContentSpecToPropertyTag> contentSpecToPropertyTags) {
         this.contentSpecToPropertyTags = contentSpecToPropertyTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "contentSpec", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<CSNode> getCSNodes() {
         return csNodes;
     }
 
     public void setCSNodes(Set<CSNode> csNodes) {
         this.csNodes = csNodes;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "contentSpec", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<ContentSpecToTag> getContentSpecToTags() {
         return contentSpecToTags;
     }
 
     public void setContentSpecToTags(Set<ContentSpecToTag> contentSpecToTags) {
         this.contentSpecToTags = contentSpecToTags;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "LastPublished")
     public Date getLastPublished() {
         return lastPublished;
     }
 
     public void setLastPublished(Date lastPublished) {
         this.lastPublished = lastPublished;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "LastModified")
     public Date getLastModified() {
         return lastModified;
     }
 
     public void setLastModified(Date lastModified) {
         this.lastModified = lastModified;
     }
 
     @Column(name = "Errors", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getErrors() {
         return errors;
     }
 
     public void setErrors(String errors) {
         this.errors = errors;
     }
 
     @Column(name = "FailedSpec", columnDefinition = "MEDIUMTEXT")
     @Size(max = 16777215)
     public String getFailedContentSpec() {
         return failedContentSpec;
     }
 
     public void setFailedContentSpec(String failedContentSpec) {
         this.failedContentSpec = failedContentSpec;
     }
 
     @PrePersist
     @PreUpdate
     protected void setLastModified() {
         lastModified = new Date();
     }
 
     @Transient
     public List<CSNode> getTopCSNodes() {
         final List<CSNode> nodes = new ArrayList<CSNode>();
 
         for (final CSNode node : csNodes) {
             if (node.getParent() == null) {
                 nodes.add(node);
             }
         }
 
         return nodes;
     }
 
     @Transient
     public void removeChild(final CSNode child) {
         final List<CSNode> removeNodes = new ArrayList<CSNode>();
 
         for (final CSNode childNode : csNodes) {
             if (childNode.getId() != null && childNode.getId().equals(child.getId())) {
                 removeNodes.add(childNode);
             }
         }
 
         for (final CSNode removeNode : removeNodes) {
             csNodes.remove(removeNode);
             removeNode.setContentSpec(null);
             if (removeNode.getParent() != null) {
                 removeNode.getParent().removeChild(child);
             }
         }
     }
 
     @Transient
     public void removeChildAndAllChildren(final CSNode child) {
         final List<CSNode> removeNodes = new ArrayList<CSNode>();
 
         // Find the node (or possibly nodes) to remove
         for (final CSNode childNode : csNodes) {
             if (childNode.getId() != null && childNode.getId().equals(child.getId())) {
                 removeNodes.add(childNode);
             }
         }
 
         for (final CSNode removeNode : removeNodes) {
             csNodes.remove(removeNode);
             removeNode.setContentSpec(null);
 
             // Remove all the children for the node
             for (final CSNode childChildNode : removeNode.getChildren()) {
                 removeChildAndAllChildren(childChildNode);
             }
         }
     }
 
     @Transient
     public void addChild(final CSNode child) {
         csNodes.add(child);
         child.setContentSpec(this);
     }
 
     @Transient
     public List<ContentSpecToPropertyTag> getContentSpecToPropertyTagsList() {
         return new ArrayList<ContentSpecToPropertyTag>(contentSpecToPropertyTags);
     }
 
     @Override
     @Transient
     protected Set<ContentSpecToPropertyTag> getPropertyTags() {
         return contentSpecToPropertyTags;
     }
 
     public void addPropertyTag(final PropertyTag propertyTag, final String value) {
         final ContentSpecToPropertyTag mapping = new ContentSpecToPropertyTag();
         mapping.setContentSpec(this);
         mapping.setPropertyTag(propertyTag);
         mapping.setValue(value);
 
         contentSpecToPropertyTags.add(mapping);
         propertyTag.getContentSpecToPropertyTags().add(mapping);
     }
 
     public void removePropertyTag(final PropertyTag propertyTag, final String value) {
         final List<ContentSpecToPropertyTag> removeList = new ArrayList<ContentSpecToPropertyTag>();
 
         for (final ContentSpecToPropertyTag mapping : contentSpecToPropertyTags) {
             final PropertyTag myPropertyTag = mapping.getPropertyTag();
             if (myPropertyTag.equals(propertyTag) && mapping.getValue().equals(value)) {
                 removeList.add(mapping);
             }
         }
 
         for (final ContentSpecToPropertyTag mapping : removeList) {
             contentSpecToPropertyTags.remove(mapping);
             mapping.getPropertyTag().getContentSpecToPropertyTags().remove(mapping);
         }
     }
 
     @Transient
     public List<Tag> getTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final ContentSpecToTag contentSpecToTag : contentSpecToTags) {
             final Tag tag = contentSpecToTag.getTag();
             retValue.add(tag);
         }
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     public void addTag(final Tag tag) throws CustomConstraintViolationException {
         if (filter(having(on(ContentSpecToTag.class).getTag(), equalTo(tag)), getContentSpecToTags()).size() == 0) {
 
             // remove any excluded tags
             for (final Tag excludeTag : tag.getExcludedTags()) {
                 if (excludeTag.equals(tag)) continue;
 
                 removeTag(excludeTag);
             }
 
             // Remove other tags if the category is mutually exclusive
             for (final TagToCategory category : tag.getTagToCategories()) {
                 if (category.getCategory().isMutuallyExclusive()) {
                     for (final Tag categoryTag : category.getCategory().getTags()) {
                         if (categoryTag.equals(tag)) continue;
 
                         // Check if the Category Tag exists in this topic
                         if (filter(having(on(ContentSpecToTag.class).getTag(), equalTo(categoryTag)), getContentSpecToTags()).size() != 0) {
                             throw new CustomConstraintViolationException(
                                     "Adding Tag " + tag.getTagName() + " (" + tag.getId() + ") failed due to a mutually exclusive " +
                                             "constraint violation.");
                         }
                     }
                 }
             }
 
             final ContentSpecToTag mapping = new ContentSpecToTag(this, tag);
             contentSpecToTags.add(mapping);
             tag.getContentSpecToTags().add(mapping);
         }
     }
 
     public void removeTag(final Tag tag) {
         final List<ContentSpecToTag> mappingEntities = filter(having(on(ContentSpecToTag.class).getTag(), equalTo(tag)),
                 getContentSpecToTags());
         if (mappingEntities.size() != 0) {
             for (final ContentSpecToTag mapping : mappingEntities) {
                 contentSpecToTags.remove(mapping);
                 mapping.getTag().getContentSpecToTags().remove(mapping);
             }
         }
     }
 
     public void addTag(final ContentSpecToTag contentSpecToTag) {
         contentSpecToTags.add(contentSpecToTag);
         contentSpecToTag.getTag().getContentSpecToTags().add(contentSpecToTag);
     }
 
     public void removeTag(final ContentSpecToTag contentSpecToTag) {
         contentSpecToTags.remove(contentSpecToTag);
         contentSpecToTag.getTag().getContentSpecToTags().remove(contentSpecToTag);
     }
 
     public void addPropertyTag(final ContentSpecToPropertyTag contentSpecToPropertyTag) {
         contentSpecToPropertyTags.add(contentSpecToPropertyTag);
         contentSpecToPropertyTag.getPropertyTag().getContentSpecToPropertyTags().add(contentSpecToPropertyTag);
     }
 
     public void removePropertyTag(final ContentSpecToPropertyTag contentSpecToPropertyTag) {
         contentSpecToPropertyTags.remove(contentSpecToPropertyTag);
         contentSpecToPropertyTag.getPropertyTag().getContentSpecToPropertyTags().remove(contentSpecToPropertyTag);
     }
 
     @Transient
     public CSNode getContentSpecTitle() {
         for (final CSNode node : getTopCSNodes()) {
             if (node.getCSNodeTitle().equals("Title")) {
                 return node;
             }
         }
 
         return null;
     }
 
     @Transient
     public void setContentSpecTitle(final CSNode contentSpecTitle) {
         if (!getTopCSNodes().contains(contentSpecTitle)) {
             contentSpecTitle.setParent(null);
             contentSpecTitle.setContentSpec(this);
             getCSNodes().add(contentSpecTitle);
         }
     }
 
     @Transient
     public CSNode getContentSpecProduct() {
         for (final CSNode node : getTopCSNodes()) {
             if (node.getCSNodeTitle().equals("Product")) {
                 return node;
             }
         }
 
         return null;
     }
 
     @Transient
     public void setContentSpecProduct(final CSNode contentSpecProduct) {
         if (!getTopCSNodes().contains(contentSpecProduct)) {
             contentSpecProduct.setParent(null);
             contentSpecProduct.setContentSpec(this);
             getCSNodes().add(contentSpecProduct);
         }
     }
 
     @Transient
     public CSNode getContentSpecVersion() {
         for (final CSNode node : getTopCSNodes()) {
             if (node.getCSNodeTitle().equals("Version")) {
                 return node;
             }
         }
 
         return null;
     }
 
     @Transient
     public void setContentSpecVersion(final CSNode contentSpecVersion) {
         if (!getTopCSNodes().contains(contentSpecVersion)) {
             contentSpecVersion.setParent(null);
             contentSpecVersion.setContentSpec(this);
             getCSNodes().add(contentSpecVersion);
         }
     }
 
     @Transient
     public CSNode getMetaData(final String metaDataTitle) {
         if (getTopCSNodes() != null) {
             for (final CSNode item : getTopCSNodes()) {
                 if (item.getCSNodeTitle().equalsIgnoreCase(metaDataTitle)) {
                     return item;
                 }
             }
         }
 
         return null;
     }
 
     @SuppressWarnings("unchecked")
     @Transient
     public List<TranslatedContentSpec> getTranslatedContentSpecs(final EntityManager entityManager, final Number revision) {
         /*
          * We have to do a query here as a @OneToMany won't work with hibernate envers since the TranslatedContentSpec entity is
          * audited and we need the latest results. This is because the translated node will never exist for its matching
          * audited node.
          */
         final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         final CriteriaQuery<TranslatedContentSpec> query = criteriaBuilder.createQuery(TranslatedContentSpec.class);
         final Root<TranslatedContentSpec> root = query.from(TranslatedContentSpec.class);
         query.select(root);
 
         final Predicate contentSpecIdMatches = criteriaBuilder.equal(root.get("contentSpecId"), contentSpecId);
         final Predicate contentSpecRevisionMatches = criteriaBuilder.lessThanOrEqualTo(root.get("contentSpecRevision").as(Integer.class),
                 (Integer) revision);
 
         if (revision == null) {
             query.where(contentSpecIdMatches);
         } else {
             query.where(criteriaBuilder.and(contentSpecIdMatches, contentSpecRevisionMatches));
         }
 
         return entityManager.createQuery(query).getResultList();
     }
 }
