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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TemporalType;
 import javax.persistence.TypedQuery;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.grabber.DatafileMetadata;
 import au.edu.uq.cmm.paul.grabber.DatasetMetadata;
 import au.edu.uq.cmm.paul.status.Facility;
 
 /**
  * This class is responsible for low-level management of the ingestion queue.
  * 
  * @author scrawley
  */
 public class QueueManager {
     public static enum Slice {
        HELD, INGESTIBLE, ALL
     }
     
     private static final Logger LOG = LoggerFactory.getLogger(QueueManager.class);
     private Paul services;
     private QueueFeedAdapter queueFeedAdapter;
 
     public QueueManager(Paul services) {
         this.services = services;
     }
 
     public List<DatasetMetadata> getSnapshot(Slice slice, String facilityName) {
         EntityManager em = createEntityManager();
         try {
             String whereClause = "";
             switch (slice) {
             case HELD:
                 whereClause = "where m.userName is null ";
                 break;
             case INGESTIBLE:
                 whereClause = "where m.userName is not null ";
                 break;
             }
             TypedQuery<DatasetMetadata> query;
             if (facilityName == null) {
                 query = em.createQuery("from DatasetMetadata m " +
                     whereClause + "order by m.id", DatasetMetadata.class);
             } else {
                 if (whereClause.isEmpty()) {
                     whereClause = "where ";
                 } else {
                     whereClause += "and ";
                 }
                 query = em.createQuery("from DatasetMetadata m " +
                     whereClause + "facilityName = :name " +
                         "order by m.id", DatasetMetadata.class);
                 query.setParameter("name", facilityName);
             }
             List<DatasetMetadata> res = query.getResultList();
             return res;
         } finally {
             em.close();
         }
     }
 
     public List<DatasetMetadata> getSnapshot(Slice slice) {
         return getSnapshot(slice, null);
     }
 
     public Date getCatchupTimestamp(Facility facility) {
         EntityManager em = createEntityManager();
         Date res;
         try {
             TypedQuery<Date> query = em.createQuery(
                     "SELECT MAX(d.captureTimestamp) FROM DatasetMetadata d " +
                             "GROUP BY d.facilityId HAVING d.facilityId = :id", 
                             Date.class);
             query.setParameter("id", facility.getId());
             res = query.getSingleResult();
         } catch (NoResultException ex) {
             res = null;
         } finally {
             em.close();
         }
         LOG.info("determineCatchupTime(" + facility.getFacilityName() + ") -> " + res);
         return res;
     }
 
     public void addEntry(DatasetMetadata metadata, File metadataFile) 
             throws JsonGenerationException, IOException {
         saveToFileSystem(metadataFile, metadata);
         saveToDatabase(metadata);
     }
 
     private void saveToDatabase(DatasetMetadata metadata) {
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             em.persist(metadata);
             em.getTransaction().commit();
         } finally {
             em.close();
         }
     }
 
     private void saveToFileSystem(File metadataFile, DatasetMetadata metadata)
             throws IOException, JsonGenerationException {
         try (BufferedWriter bw = new BufferedWriter(new FileWriter(metadataFile))) {
             metadata.serialize(bw);
             LOG.info("Saved admin metadata to " + metadataFile);
         }
     }
 
     public int expireAll(boolean discard, String facilityName, Slice slice, Date olderThan) {
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             String andPart = "";
             switch (slice) {
             case HELD:
                 andPart = " and m.userName is null";
                 break;
             case INGESTIBLE:
                 andPart = " and m.userName is not null";
                 break;
             }
             if (facilityName != null && !facilityName.isEmpty()) {
                 andPart += " and m.facilityName = :facility";
             }
             String queryString = "from DatasetMetadata m " +
                     "where m.updateTimestamp < :cutoff" + andPart;
             TypedQuery<DatasetMetadata> query = 
                     em.createQuery(queryString, DatasetMetadata.class);
             query.setParameter("cutoff", olderThan, TemporalType.TIMESTAMP);
            query.setParameter("facility", facilityName);
             List<DatasetMetadata> datasets = query.getResultList();
             for (DatasetMetadata dataset : datasets) {
                 doDelete(discard, em, dataset);
             }
             em.getTransaction().commit();
             return datasets.size();
         } finally {
             em.close();
         }
     }
 
     public int deleteAll(boolean discard, String facilityName, Slice slice) {
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             String whereClause = "";
             switch (slice) {
             case HELD:
                 whereClause = " where m.userName is null";
                 break;
             case INGESTIBLE:
                 whereClause = " where m.userName is not null";
                 break;
             }
             if (facilityName != null && !facilityName.isEmpty()) {
                 if (whereClause.isEmpty()) {
                     whereClause = " where ";
                 } else {
                     whereClause += " and ";
                 }
                 whereClause += "m.facilityName = :facility";
             }
             TypedQuery<DatasetMetadata> query = em.createQuery(
                     "from DatasetMetadata m" + whereClause, 
                     DatasetMetadata.class);
             query.setParameter("facility", facilityName);
             List<DatasetMetadata> datasets = query.getResultList();
             for (DatasetMetadata dataset : datasets) {
                 doDelete(discard, em, dataset);
             }
             em.getTransaction().commit();
             return datasets.size();
         } finally {
             em.close();
         }
     }
     
     public int delete(String[] ids, boolean discard) {
         int count = 0;
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             for (String idStr : ids) {
                 long id = Long.parseLong(idStr);
                 TypedQuery<DatasetMetadata> query =
                         em.createQuery("from DatasetMetadata d where d.id = :id", 
                                 DatasetMetadata.class);
                 query.setParameter("id", id);
                 List<DatasetMetadata> datasets = query.getResultList();
                 for (DatasetMetadata dataset : datasets) {
                     doDelete(discard, em, dataset);
                     count++;
                 }
             }
             em.getTransaction().commit();
         } finally {
             em.close();
         }
         return count;
     }
 
     private void doDelete(boolean discard, EntityManager entityManager,
             DatasetMetadata dataset) {
         // FIXME - should we do the file removal after committing the
         // database update?
         for (DatafileMetadata datafile : dataset.getDatafiles()) {
             disposeOfFile(datafile.getCapturedFilePathname(), discard);
         }
         disposeOfFile(dataset.getMetadataFilePathname(), discard);
         entityManager.remove(dataset);
     }
 
     private void disposeOfFile(String pathname, boolean discard) {
         File file = new File(pathname);
         if (!file.exists()) {
             LOG.info("File " + pathname + " no longer exists");
             return;
         }
         if (!discard) {
             File dest = new File("/tmp/archive", file.getName());
             if (dest.exists()) {
                 LOG.info("Archived file " + dest + " already exists");
             } else {
                 if (file.renameTo(dest)) {
                     LOG.info("File " + file + " archived as " + dest);
                 } else {
                     LOG.info("File " + file + " count not be archived - " +
                     		"it remains in the queue area");
                 }
                 return;
             }
         }
         if (file.delete()) {
             LOG.info("File " + pathname + " deleted from queue area");
         } else {
             LOG.info("File " + pathname + " not deleted from queue area");
         }
     }
 
     public int changeUser(String[] ids, String userName, boolean reassign) 
             throws JsonGenerationException, IOException {
         EntityManager em = createEntityManager();
         int nosChanged = 0;
         try {
             em.getTransaction().begin();
             TypedQuery<DatasetMetadata> query =
                     em.createQuery("from DatasetMetadata d where d.id = :id", 
                     DatasetMetadata.class);
             for (String idstr : ids) {
                 query.setParameter("id", new Long(idstr));
                 DatasetMetadata dataset = query.getSingleResult();
                 if (reassign || dataset.getUserName() == null) {
                     dataset.setUserName(userName.isEmpty() ? null : userName);
                     dataset.setUpdateTimestamp(new Date());
                     saveToFileSystem(new File(dataset.getMetadataFilePathname()), dataset);
                     nosChanged++;
                 }
             }
             em.getTransaction().commit();
         } catch (NoResultException ex) {
             LOG.info("Records not found", ex);
         } finally {
             em.close();
         }
         return nosChanged;
     }
     
     private EntityManager createEntityManager() {
         return services.getEntityManagerFactory().createEntityManager();
     }
 
     public void setQueueFeedAdapter(QueueFeedAdapter qfa) {
         this.queueFeedAdapter = qfa;
     }
 }
