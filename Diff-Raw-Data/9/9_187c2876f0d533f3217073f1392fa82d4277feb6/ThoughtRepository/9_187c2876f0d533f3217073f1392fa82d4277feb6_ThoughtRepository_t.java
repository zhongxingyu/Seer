 package com.aneeshpu.gae.domain.repository;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.aneesh.gae.domain.QuickThought;
 import com.aneesh.gae.domain.Tag;
 
 @Repository
 public class ThoughtRepository {
 
 	private final PersistenceManagerFactory persistenceManagerFactory;
 
 	@Autowired
 	public ThoughtRepository(final PersistenceManagerFactory persistenceManagerFactory) {
 		this.persistenceManagerFactory = persistenceManagerFactory;
 
 	}
 
 	public void persist(final QuickThought quickThought) {
 
 		final PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		try {
 			persistenceManager.makePersistent(quickThought);
 		} catch (Exception e) {
 			throw new QuickThoughtPersistenceException(MessageFormat.format("failed to persist {0}", quickThought), e);
 		} finally {
 			persistenceManager.close();
 		}
 	}
 
 	public Collection<QuickThought> allMyThoughts() {
 		final List<QuickThought> allMyThoughts = new ArrayList<QuickThought>();
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		final Query allMyThoughtsQuery = persistenceManager.newQuery(QuickThought.class);
 
 		return (Collection<QuickThought>) allMyThoughtsQuery.execute();
 	}
 
 	public Collection<QuickThought> allThoughtsTaggedWith(Tag tag) {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		Query query = persistenceManager.newQuery("javax.jdo.query.JDOQL", "SELECT FROM com.aneesh.gae.domain.Tag WHERE tag == \"" + tag + "\"");
 
 		Collection<Tag> retrievedTag = (Collection<Tag>) query.execute();
 		ArrayList<QuickThought> thoughts = new ArrayList<QuickThought>();
 		System.out.println("found " + retrievedTag.size() + " tags");
 		for (Tag tag2 : retrievedTag) {
 			System.out.println(MessageFormat.format("Found thought {0} tagged with {1}", tag2.thought(), tag2));
 			System.out.println("found tag " + tag2);
 
 			thoughts.addAll(tag2.thought());
 		}
 
 		return thoughts;
 	}
 
 	public Tag find(Tag tag) {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		Query query = persistenceManager.newQuery("SELECT FROM com.aneesh.gae.domain.Tag WHERE tag == \"" + tag + "\"");
 		Collection<Tag> tags = (Collection<Tag>) query.execute();
 		if (tags == null || tags.isEmpty())
 			return null;
 
 		return tags.toArray(new Tag[tags.size()])[0];
 	}
 
 	public Tag persist(Tag tag) {
 		PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
 		try {
 			Tag persistentTag = persistenceManager.makePersistent(tag);
 			return persistentTag;
 		} catch (Exception e) {
 			throw new QuickThoughtPersistenceException(MessageFormat.format("Failed to persist tag {0}", tag), e);
 		} finally {
//			persistenceManager.close();
 		}
 
 	}
 }
