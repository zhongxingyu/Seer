 package org.jboss.pressgangccms.restserver.entities;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.AuditReader;
 import org.hibernate.envers.AuditReaderFactory;
 import org.hibernate.envers.Audited;
 import org.hibernate.envers.query.AuditEntity;
 import org.hibernate.envers.query.AuditQuery;
 import org.jboss.pressgangccms.restserver.constants.Constants;
 import org.jboss.pressgangccms.restserver.entities.base.AuditedEntity;
 import org.jboss.pressgangccms.restserver.utils.SkynetExceptionUtilities;
 import org.jboss.pressgangccms.utils.common.CollectionUtilities;
 
 
 /**
  * A TranslatedTopic represents a particular revision of a topic. This revision
  * then holds the translated version of the document for various locales in a
  * collection of TranslatedTopicData entities.
  */
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "TranslatedTopic", uniqueConstraints = @UniqueConstraint(columnNames = { "TopicRevision", "TopicID" }))
 public class TranslatedTopic extends AuditedEntity<TranslatedTopic> implements java.io.Serializable
 {
 	@PersistenceContext(unitName="TranslatedTopic") EntityManager entityManager;
 	
 	private static final long serialVersionUID = 4190214754023153898L;
 	public static final String SELECT_ALL_QUERY = "select translatedTopic from TranslatedTopic translatedTopic";
 	private Integer translatedTopicId;
 	private Integer topicId;
 	private Integer topicRevision;
 	private Set<TranslatedTopicData> translatedTopicDataEntities = new HashSet<TranslatedTopicData>(0);
 	private Topic enversTopic;
 
 	public TranslatedTopic()
 	{
 		super(TranslatedTopic.class);
 	}
 
 	@Transient
 	public Integer getId()
 	{
 		return this.translatedTopicId;
 	}
 
 	@Id
 	@GeneratedValue(strategy = IDENTITY)
 	@Column(name = "TranslatedTopicID", unique = true, nullable = false)
 	public Integer getTranslatedTopicId()
 	{
 		return translatedTopicId;
 	}
 
 	public void setTranslatedTopicId(final Integer translatedTopicId)
 	{
 		this.translatedTopicId = translatedTopicId;
 	}
 
 	@Column(name = "TopicID", nullable = false)
 	public Integer getTopicId()
 	{
 		return topicId;
 	}
 
 	public void setTopicId(final Integer topicId)
 	{
 		this.topicId = topicId;
 	}
 
 	@Column(name = "TopicRevision", nullable = false)
 	public Integer getTopicRevision()
 	{
 		return topicRevision;
 	}
 
 	public void setTopicRevision(final Integer topicRevision)
 	{
 		this.topicRevision = topicRevision;
 	}
 
 	/**
 	 * @return The File ID used to identify this topic and revision in Zanata
 	 */
 	@Transient
 	public String getZanataId()
 	{
 		return this.topicId + "-" + this.topicRevision;
 	}
 
 	@OneToMany(fetch = FetchType.LAZY, mappedBy = "translatedTopic", cascade = CascadeType.ALL, orphanRemoval = true)
 	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 	@BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
 	public Set<TranslatedTopicData> getTranslatedTopicDataEntities()
 	{
 		return translatedTopicDataEntities;
 	}
 
 	public void setTranslatedTopicDataEntities(final Set<TranslatedTopicData> translatedTopicDataEntities)
 	{
 		this.translatedTopicDataEntities = translatedTopicDataEntities;
 	}
 
 	@Transient
 	public List<TranslatedTopicData> getTranslatedTopicDataEntitiesArray()
 	{
 		return CollectionUtilities.toArrayList(this.translatedTopicDataEntities);
 	}
 	
 	@Transient
 	public List<String> getTranslatedTopicDataLocales() {
 		List<String> locales = new ArrayList<String>();
 		for (TranslatedTopicData translatedTopicData: translatedTopicDataEntities) {
 			locales.add(translatedTopicData.getTranslationLocale());
 		}
 		
 		/* Sort the locales into alphabetical order */
 		Collections.sort(locales);
 		
 		return locales;
 	}
 	
 	@Transient
 	public Topic getEnversTopic()
 	{
 		return getEnversTopic(entityManager);
 	}
 	
 	@Transient
 	public Topic getEnversTopic(final EntityManager entityManager)
 	{
 		if (enversTopic == null) 
 		{
 			/* Find the envers topic */
 			final AuditReader reader = AuditReaderFactory.get(entityManager);
 			final AuditQuery query = reader.createQuery().forEntitiesAtRevision(Topic.class, this.topicRevision).add(AuditEntity.id().eq(this.topicId));
 			enversTopic = (Topic) query.getSingleResult();
 		}
 		return enversTopic;
 	}
 	
 	@Transient
 	public String getTopicTitle()
 	{
 		if (getEnversTopic() != null)
 			return getEnversTopic().getTopicTitle();
 		return null;
 	}
 
 	@Transient
 	public String getTopicTags()
 	{
 		if (getEnversTopic() != null)
 		{
 			return getEnversTopic().getTagsList(true);
 		}
 
 		return null;
 	}
 	
 	@Transient
 	public void pullFromZanata()
 	{
 		try
 		{
 			final ZanataPullTopicThread zanataPullTopicThread = new ZanataPullTopicThread(CollectionUtilities.toArrayList(this.translatedTopicId));
 			final Thread thread = new Thread(zanataPullTopicThread);
 			thread.start();
 		}
 		catch (final Exception ex)
 		{
 			SkynetExceptionUtilities.handleException(ex, false, "Catch all exception handler");
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Transient
 	public void pushToZanata()
 	{
 		try
 		{
 			final ZanataPushTopicThread zanataPushTopicThread = new ZanataPushTopicThread(CollectionUtilities.toArrayList(new Pair<Integer, Integer>(this.topicId, this.topicRevision)), false);
 			final Thread thread = new Thread(zanataPushTopicThread);
 			thread.start();
 		}
 		catch (final Exception ex)
 		{
 			SkynetExceptionUtilities.handleException(ex, false, "Catch all exception handler");
 		}
 	}
 }
