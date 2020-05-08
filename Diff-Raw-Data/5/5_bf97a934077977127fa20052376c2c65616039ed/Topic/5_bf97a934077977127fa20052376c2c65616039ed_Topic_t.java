 package org.jboss.pressgang.ccms.model;
 
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
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.PrePersist;
 import javax.persistence.PreRemove;
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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.htmlparser.jericho.Source;
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.envers.NotAudited;
 import org.hibernate.search.annotations.Analyze;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Index;
 import org.hibernate.search.annotations.Indexed;
 import org.hibernate.search.annotations.Store;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.ParentToPropertyTag;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.exceptions.CustomConstraintViolationException;
 import org.jboss.pressgang.ccms.model.sort.TagIDComparator;
 import org.jboss.pressgang.ccms.model.sort.TopicIDComparator;
 import org.jboss.pressgang.ccms.model.sort.TopicToTopicMainTopicIDSort;
 import org.jboss.pressgang.ccms.model.sort.TopicToTopicRelatedTopicIDSort;
 import org.jboss.pressgang.ccms.model.utils.TopicUtilities;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 @Entity
 @Audited
 @Indexed
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "Topic")
 public class Topic extends ParentToPropertyTag<Topic, TopicToPropertyTag> implements java.io.Serializable {
     public static final String SELECT_ALL_QUERY = "SELECT topic FROM Topic as Topic";
     private static final long serialVersionUID = 5580473587657911655L;
 
     private Integer topicId;
     private String topicText;
     private Date topicTimeStamp;
     private String topicTitle;
     private Integer xmlDoctype = CommonConstants.DOCBOOK_45;
     private Set<TopicToTag> topicToTags = new HashSet<TopicToTag>(0);
     private Set<TopicToTopic> parentTopicToTopics = new HashSet<TopicToTopic>(0);
     private Set<TopicToTopic> childTopicToTopics = new HashSet<TopicToTopic>(0);
     private Set<TopicToTopicSourceUrl> topicToTopicSourceUrls = new HashSet<TopicToTopicSourceUrl>(0);
     private Set<TopicToPropertyTag> topicToPropertyTags = new HashSet<TopicToPropertyTag>(0);
     private Set<TopicToBugzillaBug> topicToBugzillaBugs = new HashSet<TopicToBugzillaBug>(0);
     private String topicXML;
     private TopicSecondOrderData topicSecondOrderData;
     private String topicLocale = CommonConstants.DEFAULT_LOCALE;
 
     @Override
     @Transient
     public Integer getId() {
         return topicId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "TopicID", unique = true, nullable = false)
     public Integer getTopicId() {
         return topicId;
     }
 
     public void setTopicId(final Integer topicId) {
         this.topicId = topicId;
     }
 
     @Column(name = "TopicLocale", length = 45)
     @NotNull(message = "{topic.locale.notBlank}")
     @NotBlank(message = "{topic.locale.notBlank}")
     @Size(max = 45)
     public String getTopicLocale() {
         return topicLocale == null ? CommonConstants.DEFAULT_LOCALE : topicLocale;
     }
 
     public void setTopicLocale(final String topicLocale) {
         this.topicLocale = topicLocale;
     }
 
     @Column(name = "TopicXMLDoctype", nullable = false)
     @NotNull
     public Integer getXmlDoctype() {
         return xmlDoctype;
     }
 
     public void setXmlDoctype(Integer xmlDoctype) {
         this.xmlDoctype = xmlDoctype;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "relatedTopic")
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToTopic> getChildTopicToTopics() {
         return childTopicToTopics;
     }
 
     public void setChildTopicToTopics(final Set<TopicToTopic> childTopicToTopics) {
         this.childTopicToTopics = childTopicToTopics;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "mainTopic", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToTopic> getParentTopicToTopics() {
         return parentTopicToTopics;
     }
 
     public void setParentTopicToTopics(final Set<TopicToTopic> parentTopicToTopics) {
         this.parentTopicToTopics = parentTopicToTopics;
     }
 
     @OneToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
     @JoinTable(name = "TopicToTopicSecondOrderData", joinColumns = { @JoinColumn(name = "TopicID", unique = true) }, inverseJoinColumns = { @JoinColumn(name = "TopicSecondOrderDataID") })
     @NotAudited
     public TopicSecondOrderData getTopicSecondOrderData() {
         return topicSecondOrderData;
     }
 
     public void setTopicSecondOrderData(TopicSecondOrderData topicSecondOrderData) {
         this.topicSecondOrderData = topicSecondOrderData;
     }
 
     @Column(name = "TopicText", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getTopicText() {
         return this.topicText;
     }
 
     public void setTopicText(final String topicText) {
         this.topicText = topicText;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "TopicTimeStamp", nullable = false, length = 0)
     @NotNull
     public Date getTopicTimeStamp() {
         return this.topicTimeStamp;
     }
 
     public void setTopicTimeStamp(final Date topicTimeStamp) {
         this.topicTimeStamp = topicTimeStamp;
     }
 
    @Column(name = "TopicTitle", nullable = false, length = 1024)
     @NotNull(message = "{topic.title.notBlank}")
     @NotBlank(message = "{topic.title.notBlank}")
    @Size(max = 1024)
     public String getTopicTitle() {
         return this.topicTitle;
     }
 
     public void setTopicTitle(final String topicTitle) {
         this.topicTitle = topicTitle;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToTag> getTopicToTags() {
         return topicToTags;
     }
 
     public void setTopicToTags(final Set<TopicToTag> topicToTags) {
         this.topicToTags = topicToTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToTopicSourceUrl> getTopicToTopicSourceUrls() {
         return topicToTopicSourceUrls;
     }
 
     public void setTopicToTopicSourceUrls(final Set<TopicToTopicSourceUrl> topicToTopicSourceUrls) {
         this.topicToTopicSourceUrls = topicToTopicSourceUrls;
     }
 
     @Column(name = "TopicXML", columnDefinition = "MEDIUMTEXT")
     @Size(max = 16777215)
     public String getTopicXML() {
         return topicXML;
     }
 
     public void setTopicXML(final String topicXML) {
         this.topicXML = topicXML;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToPropertyTag> getTopicToPropertyTags() {
         return topicToPropertyTags;
     }
 
     public void setTopicToPropertyTags(Set<TopicToPropertyTag> topicToPropertyTags) {
         this.topicToPropertyTags = topicToPropertyTags;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TopicToBugzillaBug> getTopicToBugzillaBugs() {
         return topicToBugzillaBugs;
     }
 
     public void setTopicToBugzillaBugs(final Set<TopicToBugzillaBug> topicToBugzillaBugs) {
         this.topicToBugzillaBugs = topicToBugzillaBugs;
     }
 
     /**
      * This function will take the XML in the topicXML String and use it to generate a text only view that will be used by
      * Hibernate Search. The text extraction uses Jericho - http://jericho.htmlparser.net/
      */
     @Transient
     @Field(name = "TopicSearchText", index = Index.YES, analyze = Analyze.YES, store = Store.YES)
     public String getTopicSearchText() {
         if (topicXML == null) return "";
 
         final Source source = new Source(topicXML);
         source.fullSequentialParse();
         return source.getTextExtractor().toString();
     }
 
     @Transient
     public String getTopicRendered() {
         if (topicSecondOrderData == null) return null;
 
         return topicSecondOrderData.getTopicHTMLView();
     }
 
     public void setTopicRendered(final String value) {
         if (topicSecondOrderData == null) topicSecondOrderData = new TopicSecondOrderData();
 
         topicSecondOrderData.setTopicHTMLView(value);
     }
 
     @Transient
     public String getTopicXMLErrors() {
         if (topicSecondOrderData == null) return null;
 
         return topicSecondOrderData.getTopicXMLErrors();
     }
 
     public void setTopicXMLErrors(final String value) {
         if (topicSecondOrderData == null) {
             topicSecondOrderData = new TopicSecondOrderData();
         }
 
         topicSecondOrderData.setTopicXMLErrors(value);
     }
 
     @PrePersist
     private void onPrePresist() {
         topicTimeStamp = new Date();
         TopicUtilities.validateAndFixTags(this);
         TopicUtilities.validateAndFixRelationships(this);
     }
 
     @PreUpdate
     private void onPreUpdate() {
         TopicUtilities.validateAndFixTags(this);
         TopicUtilities.validateAndFixRelationships(this);
     }
 
     @Transient
     public boolean isRelatedTo(final Integer relatedTopicId) {
         for (final TopicToTopic topicToTopic : getParentTopicToTopics())
             if (topicToTopic.getRelatedTopic().topicId.equals(relatedTopicId)) return true;
 
         return false;
     }
 
     @Transient
     public boolean isRelatedTo(final Topic relatedTopic, final RelationshipTag relationshipTag) {
         for (final TopicToTopic topicToTopic : getParentTopicToTopics())
             if (topicToTopic.getRelatedTopic().equals(relatedTopic) && topicToTopic.getRelationshipTag().equals(relationshipTag))
                 return true;
 
         return false;
     }
 
     @Transient
     public boolean isRelatedTo(final Topic relatedTopic) {
         for (final TopicToTopic topicToTopic : getParentTopicToTopics())
             if (topicToTopic.getRelatedTopic().equals(relatedTopic)) return true;
 
         return false;
     }
 
     @Transient
     public boolean isTaggedWith(final Integer tagId) {
         for (final TopicToTag topicToTag : getTopicToTags())
             if (topicToTag.getTag().getTagId().equals(tagId)) return true;
 
         return false;
     }
 
     @Transient
     public boolean isTaggedWith(final Tag tag) {
         for (final TopicToTag topicToTag : getTopicToTags())
             if (topicToTag.getTag().equals(tag)) return true;
 
         return false;
     }
 
     public boolean addRelationshipFrom(final EntityManager entityManager, final Integer topicId, final Integer relationshipTagId) {
         final Topic topic = entityManager.getReference(Topic.class, topicId);
         final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
         return addRelationshipFrom(topic, relationshipTag);
     }
 
     public boolean addRelationshipFrom(final EntityManager entityManager, final Topic topic, final Integer relationshipTagId) {
         final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
         return addRelationshipFrom(topic, relationshipTag);
     }
 
     public boolean addRelationshipFrom(final Topic relatedTopic, final RelationshipTag relationshipTag) {
         if (!isRelatedTo(relatedTopic, relationshipTag)) {
             final TopicToTopic topicToTopic = new TopicToTopic(relatedTopic, this, relationshipTag);
             getChildTopicToTopics().add(topicToTopic);
             relatedTopic.getParentTopicToTopics().add(topicToTopic);
             return true;
         }
 
         return false;
     }
 
     public boolean addRelationshipTo(final EntityManager entityManager, final Integer topicId, final Integer relationshipTagId) {
         final Topic topic = entityManager.getReference(Topic.class, topicId);
         final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
         return addRelationshipTo(topic, relationshipTag);
     }
 
     public boolean addRelationshipTo(final EntityManager entityManager, final Topic topic, final Integer relationshipTagId) {
         final RelationshipTag relationshipTag = entityManager.getReference(RelationshipTag.class, relationshipTagId);
         return addRelationshipTo(topic, relationshipTag);
     }
 
     public boolean addRelationshipTo(final Topic relatedTopic, final RelationshipTag relationshipTag) {
         if (!isRelatedTo(relatedTopic, relationshipTag)) {
             final TopicToTopic topicToTopic = new TopicToTopic(this, relatedTopic, relationshipTag);
             getParentTopicToTopics().add(topicToTopic);
             relatedTopic.getChildTopicToTopics().add(topicToTopic);
             return true;
         }
 
         return false;
     }
 
     public void addTag(final EntityManager entityManager, final int tagID) {
         final Tag tag = entityManager.getReference(Tag.class, tagID);
         addTag(tag);
     }
 
     public void addTag(final Tag tag) {
         if (filter(having(on(TopicToTag.class).getTag(), equalTo(tag)), getTopicToTags()).size() == 0) {
 
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
                         if (filter(having(on(TopicToTag.class).getTag(), equalTo(categoryTag)), getTopicToTags()).size() != 0) {
                             throw new CustomConstraintViolationException(
                                     "Adding Tag " + tag.getTagName() + " (" + tag.getId() + ") failed due to a mutually exclusive " +
                                             "constraint violation.");
                         }
                     }
                 }
             }
 
             final TopicToTag mapping = new TopicToTag(this, tag);
             topicToTags.add(mapping);
             tag.getTopicToTags().add(mapping);
         }
     }
 
     public void addTopicSourceUrl(final TopicSourceUrl topicSourceUrl) {
         if (filter(having(on(TopicToTopicSourceUrl.class).getTopicSourceUrl(), equalTo(topicSourceUrl)),
                 getTopicToTopicSourceUrls()).size() == 0) {
             topicToTopicSourceUrls.add(new TopicToTopicSourceUrl(topicSourceUrl, this));
         }
     }
 
     public void addBugzillaBug(final BugzillaBug entity) {
         if (filter(having(on(TopicToBugzillaBug.class).getBugzillaBug(), equalTo(entity)), topicToBugzillaBugs).size() == 0) {
             final TopicToBugzillaBug mapping = new TopicToBugzillaBug(entity, this);
             topicToBugzillaBugs.add(mapping);
             entity.getTopicToBugzillaBugs().add(mapping);
         }
     }
 
     @Transient
     public List<Integer> getIncomingRelatedTopicIDs() {
         final List<Integer> retValue = new ArrayList<Integer>();
         for (final TopicToTopic topicToTopic : getChildTopicToTopics())
             retValue.add(topicToTopic.getMainTopic().getTopicId());
         return retValue;
     }
 
     @Transient
     public List<Topic> getOutgoingRelatedTopicsArray() {
         final ArrayList<Topic> retValue = new ArrayList<Topic>();
         for (final TopicToTopic topicToTopic : getParentTopicToTopics()) {
             retValue.add(topicToTopic.getRelatedTopic());
         }
         return retValue;
     }
 
     @Transient
     public List<Topic> getIncomingRelatedTopicsArray() {
         final ArrayList<Topic> retValue = new ArrayList<Topic>();
         for (final TopicToTopic topicToTopic : getChildTopicToTopics())
             retValue.add(topicToTopic.getMainTopic());
 
         Collections.sort(retValue, new TopicIDComparator());
 
         return retValue;
     }
 
     @Transient
     public Topic getRelatedTopicByID(final Integer id) {
         for (final Topic topic : getOutgoingRelatedTopicsArray())
             if (topic.getTopicId().equals(id)) return topic;
         return null;
     }
 
     @Transient
     public List<Integer> getRelatedTopicIDs() {
         final List<Integer> retValue = new ArrayList<Integer>();
         for (final TopicToTopic topicToTopic : getParentTopicToTopics())
             retValue.add(topicToTopic.getRelatedTopic().getTopicId());
         return retValue;
     }
 
     @Transient
     public List<Integer> getTagIDs() {
         final List<Integer> retValue = new ArrayList<Integer>();
         for (final TopicToTag topicToTag : topicToTags) {
             final Integer tagId = topicToTag.getTag().getTagId();
             retValue.add(tagId);
         }
 
         return retValue;
     }
 
     @Transient
     public List<Tag> getTags() {
         final List<Tag> retValue = new ArrayList<Tag>();
         for (final TopicToTag topicToTag : topicToTags) {
             final Tag tag = topicToTag.getTag();
             retValue.add(tag);
         }
 
         Collections.sort(retValue, new TagIDComparator());
 
         return retValue;
     }
 
     /**
      * This is necessary because a4j:repeat does not work with a Set
      */
     @Transient
     public ArrayList<Tag> getTagsArray() {
         final ArrayList<Tag> retValue = new ArrayList<Tag>();
         for (final TopicToTag topicToTag : topicToTags)
             retValue.add(topicToTag.getTag());
         return retValue;
     }
 
     @Transient
     public List<Tag> getTagsInCategories(final List<Category> categories) {
         final List<Integer> catgeoriesByID = new ArrayList<Integer>();
         for (final Category category : categories)
             catgeoriesByID.add(category.getCategoryId());
         return getTagsInCategoriesByID(catgeoriesByID);
     }
 
     @Transient
     public List<Tag> getTagsInCategoriesByID(final List<Integer> categories) {
         final List<Tag> retValue = new ArrayList<Tag>();
 
         for (final Integer categoryId : categories) {
             for (final TopicToTag topicToTag : topicToTags) {
                 final Tag tag = topicToTag.getTag();
 
                 if (topicToTag.getTag().isInCategory(categoryId)) {
                     if (!retValue.contains(tag)) retValue.add(tag);
                 }
             }
         }
 
         return retValue;
     }
 
     public boolean removeRelationshipTo(final Topic topic, final RelationshipTag relationshipTag) {
         return removeRelationshipTo(topic.getTopicId(), relationshipTag.getRelationshipTagId());
     }
 
     public boolean removeRelationshipTo(final Integer relatedTopicId, final Integer relationshipTagId) {
         for (final TopicToTopic topicToTopic : getParentTopicToTopics()) {
             final Topic relatedTopic = topicToTopic.getRelatedTopic();
             final RelationshipTag relationshipTag = topicToTopic.getRelationshipTag();
 
             if (relatedTopic.getTopicId().equals(relatedTopicId) && relationshipTag.getRelationshipTagId().equals(relationshipTagId)) {
                 /* remove the relationship from this topic */
                 getParentTopicToTopics().remove(topicToTopic);
 
                 /* now remove the relationship from the other topic */
                 for (final TopicToTopic childTopicToTopic : relatedTopic.getChildTopicToTopics()) {
                     if (childTopicToTopic.getMainTopic().equals(this)) {
                         relatedTopic.getChildTopicToTopics().remove(childTopicToTopic);
                         break;
                     }
                 }
 
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean removeRelationshipFrom(final Topic topic, final RelationshipTag relationshipTag) {
         return removeRelationshipFrom(topic.getTopicId(), relationshipTag.getRelationshipTagId());
     }
 
     public boolean removeRelationshipFrom(final Integer relatedTopicId, final Integer relationshipTagId) {
         for (final TopicToTopic topicToTopic : getChildTopicToTopics()) {
             final Topic relatedTopic = topicToTopic.getRelatedTopic();
             final RelationshipTag relationshipTag = topicToTopic.getRelationshipTag();
 
             if (relatedTopic.getTopicId().equals(relatedTopicId) && relationshipTag.getRelationshipTagId().equals(relationshipTagId)) {
                 /* remove the relationship from this topic */
                 getChildTopicToTopics().remove(topicToTopic);
 
                 /* now remove the relationship from the other topic */
                 for (final TopicToTopic parentTopicToTopic : relatedTopic.getParentTopicToTopics()) {
                     if (parentTopicToTopic.getMainTopic().equals(this)) {
                         relatedTopic.getParentTopicToTopics().remove(parentTopicToTopic);
                         break;
                     }
                 }
 
                 return true;
             }
         }
 
         return false;
     }
 
     public void removeTag(final int tagID) {
         final List<TopicToTag> mappingEntities = filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(tagID)), getTopicToTags());
         if (mappingEntities.size() != 0) {
             for (final TopicToTag mapping : mappingEntities) {
                 topicToTags.remove(mapping);
                 mapping.getTag().getTopicToTags().remove(mapping);
             }
         }
     }
 
     public void removeTag(final Tag tag) {
         removeTag(tag.getTagId());
     }
 
     public void removeTopicSourceUrl(final int id) {
         final List<TopicToTopicSourceUrl> mappingEntities = filter(
                 having(on(TopicToTopicSourceUrl.class).getTopicSourceUrl().getTopicSourceUrlId(), equalTo(id)),
                 getTopicToTopicSourceUrls());
         if (mappingEntities.size() != 0) {
             for (final TopicToTopicSourceUrl mapping : mappingEntities) {
                 topicToTopicSourceUrls.remove(mapping);
             }
         }
     }
 
     public void removeBugzillaBug(final int id) {
         final List<TopicToBugzillaBug> mappingEntities = filter(
                 having(on(TopicToBugzillaBug.class).getBugzillaBug().getBugzillaBugId(), equalTo(id)), topicToBugzillaBugs);
         if (mappingEntities.size() != 0) {
             for (final TopicToBugzillaBug mapping : mappingEntities) {
                 topicToBugzillaBugs.remove(mapping);
                 mapping.getBugzillaBug().getTopicToBugzillaBugs().remove(mapping);
             }
         }
     }
 
     @Transient
     public List<TopicToTopic> getParentTopicToTopicsArray() {
         final List<TopicToTopic> retValue = CollectionUtilities.toArrayList(parentTopicToTopics);
         Collections.sort(retValue, new TopicToTopicRelatedTopicIDSort());
         return retValue;
     }
 
     @Transient
     public List<TopicToTopic> getChildTopicToTopicsArray() {
         final List<TopicToTopic> retValue = CollectionUtilities.toArrayList(childTopicToTopics);
         Collections.sort(retValue, new TopicToTopicMainTopicIDSort());
         return retValue;
     }
 
     public void changeTopicToTopicRelationshipTag(final RelationshipTag relationshipTag, final Topic existingTopic,
             final RelationshipTag existingRelationshipTag) {
         for (final TopicToTopic topicToTopic : parentTopicToTopics) {
             if (topicToTopic.getRelatedTopic().equals(existingTopic) && topicToTopic.getRelationshipTag().equals(existingRelationshipTag)) {
                 topicToTopic.setRelationshipTag(relationshipTag);
                 break;
             }
         }
     }
 
     @Transient
     public List<TopicToPropertyTag> getTopicToPropertyTagsArray() {
         final List<TopicToPropertyTag> topicToPropertyTags = CollectionUtilities.toArrayList(this.topicToPropertyTags);
         return topicToPropertyTags;
     }
 
     @Transient
     public List<PropertyTag> getPropertyTagsArray() {
         final List<PropertyTag> retValue = new ArrayList<PropertyTag>();
         for (final TopicToPropertyTag mapping : topicToPropertyTags) {
             final PropertyTag entity = mapping.getPropertyTag();
             retValue.add(entity);
         }
 
         return retValue;
     }
 
     @Transient
     public List<TopicSourceUrl> getTopicSourceUrls() {
         final List<TopicSourceUrl> retValue = new ArrayList<TopicSourceUrl>();
         for (final TopicToTopicSourceUrl mapping : topicToTopicSourceUrls) {
             final TopicSourceUrl entity = mapping.getTopicSourceUrl();
             retValue.add(entity);
         }
 
         return retValue;
     }
 
     @Transient
     public List<BugzillaBug> getBugzillaBugs() {
         final List<BugzillaBug> retValue = new ArrayList<BugzillaBug>();
         for (final TopicToBugzillaBug mapping : topicToBugzillaBugs) {
             final BugzillaBug entity = mapping.getBugzillaBug();
             retValue.add(entity);
         }
 
         return retValue;
     }
 
     @Override
     @Transient
     protected Set<TopicToPropertyTag> getPropertyTags() {
         return topicToPropertyTags;
     }
 
     public void removePropertyTag(final TopicToPropertyTag topicToPropertyTag) {
         topicToPropertyTags.remove(topicToPropertyTag);
         topicToPropertyTag.getPropertyTag().getTopicToPropertyTags().remove(topicToPropertyTag);
     }
 
     public void addPropertyTag(final TopicToPropertyTag topicToPropertyTag) {
         topicToPropertyTags.add(topicToPropertyTag);
         topicToPropertyTag.getPropertyTag().getTopicToPropertyTags().add(topicToPropertyTag);
     }
 
     @PreRemove
     private void preRemove() {
         for (final TopicToTag mapping : topicToTags)
             mapping.getTag().getTopicToTags().remove(mapping);
 
         for (final TopicToTopic mapping : childTopicToTopics)
             mapping.getMainTopic().getParentTopicToTopics().remove(mapping);
 
         for (final TopicToTopic mapping : parentTopicToTopics)
             mapping.getRelatedTopic().getChildTopicToTopics().remove(mapping);
 
         for (final TopicToPropertyTag mapping : topicToPropertyTags)
             mapping.getPropertyTag().getTopicToPropertyTags().remove(mapping);
 
         for (final TopicToTopicSourceUrl mapping : topicToTopicSourceUrls)
             mapping.getTopicSourceUrl().getTopicToTopicSourceUrls().remove(mapping);
 
         for (final TopicToBugzillaBug mapping : topicToBugzillaBugs)
             mapping.getBugzillaBug().getTopicToBugzillaBugs().remove(mapping);
 
         topicToTags.clear();
         childTopicToTopics.clear();
         parentTopicToTopics.clear();
         topicToPropertyTags.clear();
         topicToTopicSourceUrls.clear();
         topicToBugzillaBugs.clear();
     }
 
     public void addPropertyTag(final PropertyTag propertyTag, final String value) {
         final TopicToPropertyTag mapping = new TopicToPropertyTag();
         mapping.setTopic(this);
         mapping.setPropertyTag(propertyTag);
         mapping.setValue(value);
 
         topicToPropertyTags.add(mapping);
         propertyTag.getTopicToPropertyTags().add(mapping);
     }
 
     public void removePropertyTag(final PropertyTag propertyTag, final String value) {
         final List<TopicToPropertyTag> removeList = new ArrayList<TopicToPropertyTag>();
 
         for (final TopicToPropertyTag mapping : topicToPropertyTags) {
             final PropertyTag myPropertyTag = mapping.getPropertyTag();
             if (myPropertyTag.equals(propertyTag) && mapping.getValue().equals(value)) {
                 removeList.add(mapping);
             }
         }
 
         for (final TopicToPropertyTag mapping : removeList) {
             topicToPropertyTags.remove(mapping);
             mapping.getPropertyTag().getTopicToPropertyTags().remove(mapping);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Transient
     public List<TranslatedTopicData> getTranslatedTopics(final EntityManager entityManager, final Number revision) {
         final List<TranslatedTopicData> translatedTopicDatas = new ArrayList<TranslatedTopicData>();
 
         /*
          * We have to do a query here as a @OneToMany won't work with hibernate envers since the TranslatedTopic entity is
          * audited and we need the latest results. This is because the translated topic will never exist for its matching
          * audited topic.
          */
         final String translatedTopicQuery = TranslatedTopic.SELECT_ALL_QUERY + " WHERE translatedTopic.topicId = " + topicId +
                 (revision == null ? "" : (" AND translatedTopic.topicRevision <= " + revision));
         final List<TranslatedTopic> translatedTopics = entityManager.createQuery(translatedTopicQuery).getResultList();
 
         for (final TranslatedTopic translatedTopic : translatedTopics) {
             translatedTopicDatas.addAll(translatedTopic.getTranslatedTopicDatas());
         }
 
         return translatedTopicDatas;
     }
 
     @Transient
     public List<ContentSpec> getContentSpecs(final EntityManager entityManager) {
         final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         final CriteriaQuery<ContentSpec> query = criteriaBuilder.createQuery(ContentSpec.class);
         final Root<CSNode> root = query.from(CSNode.class);
         query.select(root.get("contentSpec").as(ContentSpec.class));
 
         final Predicate topicIdMatches = criteriaBuilder.equal(root.get("entityId"), getTopicId());
         final Predicate topicTypeMatches = criteriaBuilder.equal(root.get("CSNodeType"), CommonConstants.CS_NODE_TOPIC);
         query.where(criteriaBuilder.and(topicIdMatches, topicTypeMatches));
 
         final List<ContentSpec> results = entityManager.createQuery(query).getResultList();
         return results;
     }
 }
