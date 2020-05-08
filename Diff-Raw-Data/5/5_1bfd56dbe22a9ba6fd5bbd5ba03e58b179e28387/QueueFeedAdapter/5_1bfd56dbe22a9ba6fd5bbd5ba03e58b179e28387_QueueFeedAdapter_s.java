 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.queue;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.TypedQuery;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.i18n.iri.IRI;
 import org.apache.abdera.model.Content;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.model.Person;
 import org.apache.abdera.protocol.server.RequestContext;
 import org.apache.abdera.protocol.server.context.ResponseContextException;
 import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.eccles.FacilitySession;
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.grabber.DatafileMetadata;
 import au.edu.uq.cmm.paul.grabber.DatasetMetadata;
 
 /**
  * This class is an Abdera feed adapter that maps the data grabber's output queue as
  * an atom feed.  Note that we override some of the superclasses protected methods 
  * in order to implement paging and to add categories to the feed entries.
  * 
  * @author scrawley
  */
 public class QueueFeedAdapter extends AbstractEntityCollectionAdapter<DatasetMetadata> {
     private static final Logger LOG = LoggerFactory.getLogger(QueueFeedAdapter.class);
     private static final String ID_PREFIX = "urn:uuid:";
 
     private EntityManagerFactory entityManagerFactory;
     private PaulConfiguration config;
     private boolean holdDatasetsWithNoUser;
     
     public QueueFeedAdapter(EntityManagerFactory entityManagerFactory) {
         this.entityManagerFactory = entityManagerFactory;
         config = PaulConfiguration.load(entityManagerFactory);
         holdDatasetsWithNoUser = config.isHoldDatasetsWithNoUser();
     }
     
     @Override
     public String getTitle(RequestContext request) {
         return config.getFeedTitle();
     }
 
     @Override
     public void deleteEntry(String resourceName, RequestContext request)
             throws ResponseContextException {
         throw new ResponseContextException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     public Object getContent(DatasetMetadata record, RequestContext request)
             throws ResponseContextException {
         return "dataset for " + record.getSourceFilePathnameBase() + 
                 ", capture timestamp = " + record.getCaptureTimestamp() + 
                 ", dataset uuid = " + record.getRecordUuid() + 
                 ", session uuid = " + record.getSessionUuid();
     }
 
     @Override
     public Iterable<DatasetMetadata> getEntries(RequestContext request)
             throws ResponseContextException {
         EntityManager entityManager = entityManagerFactory.createEntityManager();
         try {
             TypedQuery<DatasetMetadata> query;
             Long id;
             try {
                 String from = request.getParameter("from");
                 id = from == null ? null : Long.valueOf(from);
             } catch (NumberFormatException ex) {
                 throw new ResponseContextException(HttpServletResponse.SC_BAD_REQUEST);
             }
             if (id != null) {
                 LOG.debug("Fetching from id " + id);
                 query = entityManager.createQuery(
                         "from DatasetMetadata m where m.id <= :id " +
                         (holdDatasetsWithNoUser ? "and m.userName is not null " : "") +
                        "order by m.updateTimestamp desc", 
                         DatasetMetadata.class).setParameter("id", id);
             } else {
                 LOG.debug("Fetching from start of queue");
                 query = entityManager.createQuery(
                         "from DatasetMetadata m " +
                         (holdDatasetsWithNoUser ? "where m.userName is not null " : "") +
                        "order by m.updateTimestamp desc", 
                         DatasetMetadata.class);
             }
             query.setMaxResults(config.getFeedPageSize() + 1);
             List<DatasetMetadata> res = new ArrayList<DatasetMetadata>(query.getResultList());
             LOG.debug("Max page size " + config.getFeedPageSize() +
                       ", fetched " + res.size());
             return res;
         } finally {
             entityManager.close();
         }
     }
 
     @Override
     public DatasetMetadata getEntry(String resourceName, RequestContext request)
             throws ResponseContextException {
         String[] parts = resourceName.split("-");
         EntityManager entityManager = entityManagerFactory.createEntityManager();
         try {
             DatasetMetadata record = 
                     entityManager.createQuery("from DatasetMetadata a where a.id = :id", 
                     DatasetMetadata.class).setParameter("id", parts[0]).getSingleResult();
             if (record == null) {
                 throw new ResponseContextException(HttpServletResponse.SC_NOT_FOUND);
             } else {
                 return record;
             }
         } finally {
             entityManager.close();
         }
     }
 
     @Override
     public String getId(DatasetMetadata record) throws ResponseContextException {
         return ID_PREFIX + record.getRecordUuid();
     }
 
     @Override
     public String getName(DatasetMetadata record) throws ResponseContextException {
         return record.getId() + "-" + record.getRecordUuid();
     }
 
     @Override
     public String getTitle(DatasetMetadata record) throws ResponseContextException {
         return record.getFacilityFilePathnameBase();
     }
 
     @Override
     public Date getUpdated(DatasetMetadata record) throws ResponseContextException {
         return record.getCaptureTimestamp();
     }
 
     @Override
     public DatasetMetadata postEntry(String title, IRI id, String summary, Date updated,
             List<Person> authors, Content content, RequestContext rc)
             throws ResponseContextException {
         throw new ResponseContextException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     public void putEntry(DatasetMetadata record, String title, Date updated, 
             List<Person> authors, String summary, Content content, RequestContext rc)
             throws ResponseContextException {
         throw new ResponseContextException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
     }
 
     @Override
     public String getAuthor(RequestContext rc)
             throws ResponseContextException {
         return config.getFeedAuthor();
     }
     
     @Override
     public List<Person> getAuthors(DatasetMetadata record, RequestContext request)
             throws ResponseContextException {
         Person author = request.getAbdera().getFactory().newAuthor();
         author.setName(record.getUserName());
         if (record.getEmailAddress() != null) {
             author.setEmail(record.getEmailAddress());
         }
         return Arrays.asList(author);
     }
     
     @Override
     protected String addEntryDetails(RequestContext request, Entry entry,
             IRI feedIri, DatasetMetadata record)
             throws ResponseContextException {
         String res = super.addEntryDetails(request, entry, feedIri, record);
         for (DatafileMetadata datafile : record.getDatafiles()) {
             entry.addLink(config.getBaseFileUrl() + 
                     new File(datafile.getCapturedFilePathname()).getName(),
                     "enclosure", datafile.getMimeType(), 
                     new File(datafile.getSourceFilePathname()).getName(),
                     "en", datafile.getFileSize());
         }
         entry.addLink(config.getBaseFileUrl() + 
                 new File(record.getMetadataFilePathname()).getName(),
                 "enclosure");
         return res;
     }
 
     @Override
     public String getId(RequestContext rc) {
         return config.getFeedId();
     }
     
     /**
      * Create the base feed for the requested collection.  This override allows
      * us to add the author email and so forth.
      */
     @Override
     protected Feed createFeedBase(RequestContext request) 
             throws ResponseContextException {
         Factory factory = request.getAbdera().getFactory();
         Feed feed = factory.newFeed();
         feed.setId(getId(request));
         feed.setTitle(getTitle(request));
         feed.addLink(config.getFeedUrl(), "self");
         Person author = factory.newAuthor();
         author.setName(getAuthor(request));
         String email = config.getFeedAuthorEmail();
         if (email != null && !email.isEmpty()) {
             author.setEmail(email);
         }
         feed.addAuthor(author);
         feed.setUpdated(new Date());
         return feed;
     }
     
     /**
      * Adds the selected entries to the Feed document.  It also sets 
      * the feed's atom:updated element to the current date and time,
      * and adds a link to the next "page" of the feed.
      */
     @Override
     protected void addFeedDetails(Feed feed, RequestContext request) 
             throws ResponseContextException {
         feed.setUpdated(new Date());
         Iterable<DatasetMetadata> entries = getEntries(request);
         if (entries != null) {
             int count = 0;
             for (DatasetMetadata record : entries) {
                 LOG.debug("count = " + count + ", entry id = " + record.getId());
                 if (++count > config.getFeedPageSize()) {
                     String nextPageUrl = config.getFeedUrl() +
                             "?from=" + record.getId();
                     LOG.debug("Adding 'next' link - " + nextPageUrl);
                     feed.addLink(nextPageUrl, "next");
                     break;
                 }
                 Entry entry = feed.addEntry();
 
                 IRI feedIri = new IRI(getFeedIriForEntry(record, request));
                 addEntryDetails(request, entry, feedIri, record);
 
                 if (isMediaEntry(record)) {
                     addMediaContent(feedIri, entry, record, request);
                 } else {
                     addContent(entry, record, request);
                 }
 
                 if (!record.getUserName().equals(FacilitySession.UNKNOWN)) {
                     String sessionTitle = "Session of " + record.getUserName() + "/" +
                             record.getAccountName() + " started on " +
                             record.getSessionStartTimestamp();
                     entry.addCategory(
                             "http://mytardis.org/schemas/atom-import#experiment-ExperimentID",
                             record.getSessionUuid(), "experiment");
                     entry.addCategory(
                             "http://mytardis.org/schemas/atom-import#experiment-ExperimentTitle",
                             sessionTitle, "experiment title");
                 }
             }
         }
     }
 }
