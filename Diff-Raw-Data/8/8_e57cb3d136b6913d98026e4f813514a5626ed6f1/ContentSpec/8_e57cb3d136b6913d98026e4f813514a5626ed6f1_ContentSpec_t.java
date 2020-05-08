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
 import org.jboss.pressgang.ccms.model.interfaces.HasCSNodes;
 import org.jboss.pressgang.ccms.model.interfaces.HasTags;
 import org.jboss.pressgang.ccms.model.sort.TagIDComparator;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "ContentSpec")
 public class ContentSpec extends ParentToPropertyTag<ContentSpec, ContentSpecToPropertyTag> implements HasCSNodes, HasTags, Serializable {
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
     @Override
     public List<CSNode> getChildrenList() {
         final List<CSNode> nodes = new ArrayList<CSNode>();
 
         for (final CSNode node : csNodes) {
             if (node.getParent() == null) {
                 nodes.add(node);
             }
         }
 
         return nodes;
     }
 
     @Transient
     @Override
     public Set<CSNode> getChildren() {
         final Set<CSNode> nodes = new HashSet<CSNode>();
 
         for (final CSNode node : csNodes) {
             if (node.getParent() == null) {
                 nodes.add(node);
             }
         }
 
         return nodes;
     }
 
     @Transient
     @Override
     public void removeChild(final CSNode child) {
         final Set<CSNode> removeNodes = new HashSet<CSNode>();
 
         // Find the node (or possibly nodes) to remove
         for (final CSNode childNode : csNodes) {
             if (childNode.getId() != null && childNode.getId().equals(child.getId())) {
                 removeNodes.add(childNode);
             }
         }
 
         for (final CSNode removeNode : removeNodes) {
             removeChildAndAllChildrenForChild(removeNode);
 
             if (child.getParent() != null) {
                 child.getParent().removeChild(child);
             }
         }
     }
 
     @Transient
     protected void removeChildAndAllChildrenForChild(final CSNode child) {
         // Add all the children of the node to be removed.
         final Set<CSNode> children = new HashSet<CSNode>(child.getChildren());
         for (final CSNode childChildNode : children) {
             removeChildAndAllChildrenForChild(childChildNode);
         }
 
         csNodes.remove(child);
         child.setContentSpec(null);
     }
 
     @Transient
     @Override
     public void addChild(final CSNode child) {
         if (child.getContentSpec() != null && !child.getContentSpec().equals(this)) {
             child.getContentSpec().removeChild(child);
         }
 
         csNodes.add(child);
         child.setContentSpec(this);
 
         // Make sure all the children are transferred as well
         for (final CSNode childChild : child.getChildren()) {
             if (csNodes.contains(childChild) && childChild.getContentSpec().equals(this)) {
                 continue;
             } else {
                 addChild(childChild);
             }
         }
     }
 
     @Override
     @Transient
     public Set<ContentSpecToPropertyTag> getPropertyTags() {
         return contentSpecToPropertyTags;
     }
 
     @Override
     public void addPropertyTag(final PropertyTag propertyTag, final String value) {
         final ContentSpecToPropertyTag mapping = new ContentSpecToPropertyTag();
         mapping.setContentSpec(this);
         mapping.setPropertyTag(propertyTag);
         mapping.setValue(value);
 
         contentSpecToPropertyTags.add(mapping);
         propertyTag.getContentSpecToPropertyTags().add(mapping);
     }
 
     @Override
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
 
     @Override
     public void addPropertyTag(final ContentSpecToPropertyTag contentSpecToPropertyTag) {
         contentSpecToPropertyTag.setContentSpec(this);
         contentSpecToPropertyTags.add(contentSpecToPropertyTag);
         contentSpecToPropertyTag.getPropertyTag().getContentSpecToPropertyTags().add(contentSpecToPropertyTag);
     }
 
     @Override
     public void removePropertyTag(final ContentSpecToPropertyTag contentSpecToPropertyTag) {
         contentSpecToPropertyTags.remove(contentSpecToPropertyTag);
         contentSpecToPropertyTag.getPropertyTag().getContentSpecToPropertyTags().remove(contentSpecToPropertyTag);
     }
 
     @Transient
     @Override
     public List<Tag> getTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final ContentSpecToTag contentSpecToTag : contentSpecToTags) {
             if (!contentSpecToTag.getBookTag()) {
                 retValue.add(contentSpecToTag.getTag());
             }
         }
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     @Transient
     public List<Tag> getBookTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final ContentSpecToTag contentSpecToTag : contentSpecToTags) {
             if (contentSpecToTag.getBookTag()) {
                 retValue.add(contentSpecToTag.getTag());
             }
         }
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     public void addTag(final Tag tag) throws CustomConstraintViolationException {
         addTag(tag, false);
     }
 
     protected void addTag(final Tag tag, boolean isBookTag) throws CustomConstraintViolationException {
         if (filter(having(on(ContentSpecToTag.class).getTag(), equalTo(tag)), getContentSpecToTags()).size() == 0) {
 
             // remove any excluded tags
             for (final Tag excludeTag : tag.getExcludedTags()) {
                 if (excludeTag.equals(tag)) continue;
 
                 removeTag(excludeTag);
             }
 
             // Throw an error if other tags if exist for the category and it's mutually exclusive
             for (final TagToCategory category : tag.getTagToCategories()) {
                 if (category.getCategory().isMutuallyExclusive()) {
                     for (final Tag categoryTag : category.getCategory().getTags()) {
                         if (categoryTag.equals(tag)) continue;
 
                         // Check if the Category Tag exists in this content spec
                         final List<ContentSpecToTag> mappingEntities = filter(
                                 having(on(ContentSpecToTag.class).getTag(), equalTo(categoryTag)), getContentSpecToTags());
                         if (mappingEntities.size() != 0 && filter(having(on(ContentSpecToTag.class).getBookTag(), equalTo(isBookTag)),
                                 mappingEntities).size() != 0) {
                             throw new CustomConstraintViolationException(
                                     "Adding Tag " + tag.getTagName() + " (" + tag.getId() + ") failed due to a mutually exclusive " +
                                             "constraint violation.");
                         }
                     }
                 }
             }
 
             final ContentSpecToTag mapping = new ContentSpecToTag(this, tag, isBookTag);
             contentSpecToTags.add(mapping);
             tag.getContentSpecToTags().add(mapping);
         }
     }
 
     public void addBookTag(final Tag tag) throws CustomConstraintViolationException {
         addTag(tag, true);
     }
 
     @Override
     public void removeTag(final Tag tag) {
         removeTag(tag, false);
     }
 
     public void removeBookTag(final Tag tag) {
         removeTag(tag, true);
     }
 
     protected void removeTag(final Tag tag, final boolean isBookTag) {
         // Filter down to the matching tags
         final List<ContentSpecToTag> mappingEntities = filter(having(on(ContentSpecToTag.class).getTag(), equalTo(tag)),
                 getContentSpecToTags());
         if (mappingEntities.size() != 0) {
             // filter to book or content spec tags
             final List<ContentSpecToTag> matchingEntities = filter(having(on(ContentSpecToTag.class).getBookTag(), equalTo(isBookTag)),
                     mappingEntities);
             if (matchingEntities.size() != 0) {
                 for (final ContentSpecToTag mapping : matchingEntities) {
                     contentSpecToTags.remove(mapping);
                     mapping.getTag().getContentSpecToTags().remove(mapping);
                 }
             }
         }
     }
 
     @Transient
     public CSNode getContentSpecTitle() {
         for (final CSNode node : getChildrenList()) {
            if (node.getCSNodeTitle().equals(CommonConstants.CS_TITLE_TITLE)) {
                 return node;
             }
         }
 
         return null;
     }
 
     @Transient
     public void setContentSpecTitle(final CSNode contentSpecTitle) {
         if (!getChildrenList().contains(contentSpecTitle)) {
             contentSpecTitle.setParent(null);
             contentSpecTitle.setContentSpec(this);
             getCSNodes().add(contentSpecTitle);
         }
     }
 
     @Transient
     public CSNode getContentSpecProduct() {
         for (final CSNode node : getChildrenList()) {
            if (node.getCSNodeTitle().equals(CommonConstants.CS_PRODUCT_TITLE)) {
                 return node;
             }
         }
 
         return null;
     }
 
     @Transient
     public void setContentSpecProduct(final CSNode contentSpecProduct) {
         if (!getChildrenList().contains(contentSpecProduct)) {
             contentSpecProduct.setParent(null);
             contentSpecProduct.setContentSpec(this);
             getCSNodes().add(contentSpecProduct);
         }
     }
 
     @Transient
     public CSNode getContentSpecVersion() {
         for (final CSNode node : getChildrenList()) {
            if (node.getCSNodeTitle().equals(CommonConstants.CS_VERSION_TITLE)) {
                 return node;
             }
         }
 
         return null;
     }
 
     @Transient
     public void setContentSpecVersion(final CSNode contentSpecVersion) {
         if (!getChildrenList().contains(contentSpecVersion)) {
             contentSpecVersion.setParent(null);
             contentSpecVersion.setContentSpec(this);
             getCSNodes().add(contentSpecVersion);
         }
     }
 
     @Transient
     public CSNode getMetaData(final String metaDataTitle) {
         if (getChildrenList() != null) {
             for (final CSNode item : getChildrenList()) {
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
