 package pl.psnc.dl.wf4ever.eventbus.listeners;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.ApplicationProperties;
 import pl.psnc.dl.wf4ever.db.dao.AtomFeedEntryDAO;
 import pl.psnc.dl.wf4ever.db.hibernate.HibernateUtil;
 import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROAfterDeleteEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterCreateEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterDeleteEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterUpdateEvent;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.RO.Resource;
 import pl.psnc.dl.wf4ever.notifications.Notification;
 import pl.psnc.dl.wf4ever.notifications.Notification.Summary;
 import pl.psnc.dl.wf4ever.notifications.Notification.Title;
 import pl.psnc.dl.wf4ever.notifications.notifiedmodels.Comment;
 import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * Listener for ResearchObject and ResearchObjectComponent, performs operation
  * on solr indexs.
  * 
  * @author pejot
  * 
  */
 public class NotificationsListener {
 
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(NotificationsListener.class);
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param eventBus
 	 *            EventBus instance
 	 */
 	public NotificationsListener(EventBus eventBus) {
 		eventBus.register(this);
 	}
 
 	/**
 	 * Subscription method.
 	 * 
 	 * @param event
 	 *            processed event
 	 */
 	@Subscribe
 	public void onAfterROCreate(ROAfterCreateEvent event) {
 		AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 		Notification entry = new Notification.Builder(event.getResearchObject().getUri())
 				.title(Title.created(event.getResearchObject()))
 				.summary(Summary.created(event.getResearchObject()))
 				.source(ApplicationProperties.getContextPath() != null ? ApplicationProperties
 						.getContextPath() : "/").sourceName("RODL").build();
 		dao.save(entry);
 	}
 
 	/**
 	 * Subscription method.
 	 * 
 	 * @param event
 	 *            processed event
 	 */
 	@Subscribe
 	public void onAfterRODelete(ROAfterDeleteEvent event) {
 		AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 		Notification entry = new Notification.Builder(event.getResearchObject())
 				.title(Title.deleted(event.getResearchObject()))
 				.summary(Summary.deleted(event.getResearchObject()))
 				.source(ApplicationProperties.getContextPath() != null ? ApplicationProperties
 						.getContextPath() : "/").sourceName("RODL").build();
 		dao.save(entry);
 	}
 
 	/**
 	 * Subscription method.
 	 * 
 	 * @param event
 	 *            processed event
 	 */
 	@Subscribe
 	public void onAfterResourceCreate(ROComponentAfterCreateEvent event) {			
 		String source = ApplicationProperties.getContextPath() != null ? ApplicationProperties
 			.getContextPath() : "/";
 		if (event.getResearchObjectComponent() instanceof Annotation) {
 			AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 			Annotation res = (Annotation) event.getResearchObjectComponent();
 			deleteBodyOfCreatedAnnotation(dao, res);
 			if (isComment(dao, res)) {
 				Comment comment = new Comment(res);
 				Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 				.title(Title.created(comment)).summary(Summary.created(comment)).source(source)
 				.sourceName("RODL").build();
 				dao.save(entry);
 				return;
 			}
 
 			Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 					.title(Title.created(res)).summary(Summary.created(res)).source(source)
 					.sourceName("RODL").build();
 			dao.save(entry);
 			return;
 		}
 		if (event.getResearchObjectComponent() instanceof Resource) {
 			AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 			ResearchObjectComponentSerializable res = event.getResearchObjectComponent();
 			Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 					.title(Title.created(res)).summary(Summary.created(res)).source(source)
 					.sourceName("RODL").build();
 			dao.save(entry);
 		}
 	}
 
 	/**
 	 * Subscription method.
 	 * 
 	 * @param event
 	 *            processed event
 	 */
 	@Subscribe
 	public void onAfterResourceDelete(ROComponentAfterDeleteEvent event) {
 		String source = ApplicationProperties.getContextPath() != null ? ApplicationProperties
 				.getContextPath() : "/";
 		if (event.getResearchObjectComponent() instanceof Annotation) {
 			AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 			Annotation res = (Annotation) event.getResearchObjectComponent();
 			deleteBodyOfCreatedAnnotation(dao, res);
 			if (isComment(dao, res)) {
 				Comment comment = new Comment(res);
 				Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 				.title(Title.deleted(comment)).summary(Summary.deleted(comment)).source(source)
 				.sourceName("RODL").build();
 				dao.save(entry);
 				return;
 			}
 			Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 					.title(Title.deleted(res)).summary(Summary.deleted(res)).source(source)
 					.sourceName("RODL").build();
 			dao.save(entry);
 			return;
 		}
 		if (event.getResearchObjectComponent() instanceof Resource
 				|| event.getResearchObjectComponent() instanceof AggregatedResource) {
 			AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 			ResearchObjectComponentSerializable res = event.getResearchObjectComponent();
 			Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 					.title(Title.deleted(res)).summary(Summary.deleted(res)).source(source)
 					.sourceName("RODL").build();
 			dao.save(entry);
 		}
 	}
 
 	/**
 	 * Subscription method.
 	 * 
 	 * @param event
 	 *            processed event
 	 */
 	@Subscribe
 	public void onAfterResourceUpdate(ROComponentAfterUpdateEvent event) {
 		String source = ApplicationProperties.getContextPath() != null ? ApplicationProperties
 				.getContextPath() : "/";
 		if (event.getResearchObjectComponent() instanceof Annotation) {
 			AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 			Annotation res = (Annotation) event.getResearchObjectComponent();
 			deleteBodyOfCreatedAnnotation(dao, res);
 			if (isComment(dao, res)) {
 				Comment comment = new Comment(res);
 				Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 				.title(Title.updated(comment)).summary(Summary.updated(comment)).source(source)
 				.sourceName("RODL").build();
 				dao.save(entry);
 				return;
 			}
 			Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 					.title(Title.updated(res)).summary(Summary.updated(res)).source(source)
 					.sourceName("RODL").build();
 			dao.save(entry);
 			return;
 		}
 		if (event.getResearchObjectComponent() instanceof Resource) {
 			AtomFeedEntryDAO dao = new AtomFeedEntryDAO();
 			ResearchObjectComponentSerializable res = event.getResearchObjectComponent();
 			Notification entry = new Notification.Builder(res.getResearchObject().getUri())
 					.title(Title.updated(res)).summary(Summary.updated(res)).source(source)
 					.sourceName("RODL").build();
 			dao.save(entry);
 		}
 	}
 	
 	
 	//if annotation is created and resource becomes body it removes notification about this resource.
 	//not the best solution but only one easy which I see...
 	//anyway it isn't good but it works...
 	//super dirty stuff
 	private void deleteBodyOfCreatedAnnotation(AtomFeedEntryDAO dao, Annotation ann) {
 		//I don't know why but first it needs to be commit. Otherwise annotations aren't accessible.
		HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
		HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().begin();
 
 		List<Notification> notifications = dao.find(ann.getResearchObject().getUri(), null, null, null, 3);
 		for (Notification n : notifications) {
 			if(n.getSummary().contains("resource has been")) {
 				if(n.getSummary().contains(ann.getBody().getUri().toString())) {
 					dao.delete(n);
 				}
 			}
 		}
 	}
 	
 	private boolean isComment(AtomFeedEntryDAO dao, Annotation ann) {
 		OntModel model = ModelFactory.createOntologyModel();
 		if(ann.getBody().getGraphAsInputStream(RDFFormat.RDFXML)==null) {
 			return false;
 		}
 		model.read(ann.getBody().getGraphAsInputStream(RDFFormat.RDFXML), "");
 		com.hp.hpl.jena.rdf.model.Resource resource = model.getResource(ann.getResearchObject().getUri().toString());
 		if( resource != null ) {
 			if ( resource.getProperty(RDFS.comment) != null ){
 				return true;
 			}
 		} 
 		return false;
 	}
 }
