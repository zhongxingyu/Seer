 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.msu.nscl.olog;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import java.util.*;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 
 /**
  *
  * @author berryman
  */
 public class LogManager {
 
     private static EntityManager em = null;
 
     private LogManager() {
     }
 
     /**
      * Returns the list of logs in the database.
      *
      * @return Logs
      * @throws CFException wrapping an SQLException
      */
     public static Logs findAll() throws CFException {
         em = JPAUtil.getEntityManagerFactory().createEntityManager();
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<Log> cq = cb.createQuery(Log.class);
         Root<Log> from = cq.from(Log.class);
         CriteriaQuery<Log> select = cq.select(from);
         Predicate statusPredicate = cb.equal(from.get(Log_.state), State.Active);
         select.where(statusPredicate);
         select.orderBy(cb.desc(from.get(Log_.modifiedDate)));
         TypedQuery<Log> typedQuery = em.createQuery(select);
         JPAUtil.startTransaction(em);
         try {
             Logs result = new Logs();
             List<Log> rs = typedQuery.getResultList();
 
             if (rs != null) {
                 Iterator<Log> iterator = rs.iterator();
                 while (iterator.hasNext()) {
                     result.addLog(iterator.next());
                 }
             }
 
             return result;
         } catch (Exception e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "JPA exception: " + e);
         } finally {
             JPAUtil.finishTransacton(em);
         }
     }
 
     public static Logs findLog(MultivaluedMap<String, String> matches) throws CFException {
 
 
         List<String> log_patterns = new ArrayList();
         List<String> logbook_matches = new ArrayList();
         List<String> logbook_patterns = new ArrayList();
         List<String> tag_matches = new ArrayList();
         List<String> tag_patterns = new ArrayList();
         List<String> property_matches = new ArrayList();
         List<String> property_patterns = new ArrayList();
         Multimap<String, String> date_matches = ArrayListMultimap.create();
         Multimap<String, String> paginate_matches = ArrayListMultimap.create();
         Multimap<String, String> value_patterns = ArrayListMultimap.create();
         Boolean empty = false;
 
         em = JPAUtil.getEntityManagerFactory().createEntityManager();
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<Log> cq = cb.createQuery(Log.class);
         Root<Log> from = cq.from(Log.class);
         Join<Log, Entry> entry = from.join(Log_.entry, JoinType.LEFT);
         SetJoin<Log, Tag> tags = from.join(Log_.tags, JoinType.LEFT);
         SetJoin<Log, Logbook> logbooks = from.join(Log_.logbooks, JoinType.LEFT);
         Join<Attribute, Property> property = from.join(Log_.attributes, JoinType.LEFT).join(LogAttribute_.attribute, JoinType.LEFT).join(Attribute_.property, JoinType.LEFT);
         Join<LogAttribute, Attribute> attribute = from.join(Log_.attributes, JoinType.LEFT).join(LogAttribute_.attribute, JoinType.LEFT);
         Join<Log, LogAttribute> logAttribute = from.join(Log_.attributes, JoinType.LEFT);
 
         for (Map.Entry<String, List<String>> match : matches.entrySet()) {
             String key = match.getKey().toLowerCase();
             Collection<String> matchesValues = match.getValue();
             if (key.equals("search")) {
                 for (String m : matchesValues) {
                     if (m.contains("?") || m.contains("*")) {
                         if (m.contains("\\?") || m.contains("\\*")) {
                             m = m.replace("\\", "");
                             log_patterns.add(m);
                         } else {
                             m = m.replace("*", "%");
                             m = m.replace("?", "_");
                             log_patterns.add(m);
                         }
                     } else {
                         log_patterns.add(m);
                     }
                 }
             } else if (key.equals("tag")) {
                 for (String m : matchesValues) {
                     if (m.contains("?") || m.contains("*")) {
                         if (m.contains("\\?") || m.contains("\\*")) {
                             m = m.replace("\\", "");
                             tag_matches.add(m);
                         } else {
                             m = m.replace("*", "%");
                             m = m.replace("?", "_");
                             tag_patterns.add(m);
                         }
                     } else {
                         tag_matches.add(m);
                     }
                 }
                 if (tag_matches.size() == 1) {
                     String match1 = tag_matches.get(0);
                     tag_matches.clear();
                     tag_matches.addAll(Arrays.asList(match1.split(",")));
                 }
             } else if (key.equals("logbook")) {
                 for (String m : matchesValues) {
                     if (m.contains("?") || m.contains("*")) {
                         if (m.contains("\\?") || m.contains("\\*")) {
                             m = m.replace("\\", "");
                             logbook_matches.add(m);
                         } else {
                             m = m.replace("*", "%");
                             m = m.replace("?", "_");
                             logbook_patterns.add(m);
                         }
                     } else {
                         logbook_matches.add(m);
                     }
                 }
                 if (logbook_matches.size() == 1) {
                     String match1 = logbook_matches.get(0);
                     logbook_matches.clear();
                     logbook_matches.addAll(Arrays.asList(match1.split(",")));
                 }
             } else if (key.equals("property")) {
                 for (String m : matchesValues) {
                     if (m.contains("?") || m.contains("*")) {
                         if (m.contains("\\?") || m.contains("\\*")) {
                             m = m.replace("\\", "");
                             property_matches.add(m);
                         } else {
                             m = m.replace("*", "%");
                             m = m.replace("?", "_");
                             property_patterns.add(m);
                         }
                     } else {
                         property_matches.add(m);
                     }
                 }
             } else if (key.equals("page")) {
                 paginate_matches.putAll(key, match.getValue());
             } else if (key.equals("limit")) {
                 paginate_matches.putAll(key, match.getValue());
             } else if (key.equals("start")) {
                 date_matches.putAll(key, match.getValue());
             } else if (key.equals("end")) {
                 date_matches.putAll(key, match.getValue());
             } else if (key.equals("empty")) {
                 empty = true;
             } else {
                 Collection<String> cleanedMatchesValues = new HashSet<String>();
                 for (String m : matchesValues) {
                     if (m.contains("?") || m.contains("*")) {
                         if (m.contains("\\?") || m.contains("\\*")) {
                             m = m.replace("\\", "");
                             cleanedMatchesValues.add(m);
                         } else {
                             m = m.replace("*", "%");
                             m = m.replace("?", "_");
                             cleanedMatchesValues.add(m);
                         }
                     } else {
                         cleanedMatchesValues.add(m);
                     }
                 }
                 value_patterns.putAll(key, cleanedMatchesValues);
             }
         }
         //cb.or() causes an error in eclipselink with p1 as first argument
         Predicate tagPredicate = cb.disjunction();
         if (!tag_matches.isEmpty()) {
             tagPredicate = cb.or(tags.get(Tag_.name).in(tag_matches), tagPredicate);
         }
         for (String s : tag_patterns) {
             tagPredicate = cb.or(cb.like(tags.get(Tag_.name), s), tagPredicate);
         }
 
         Predicate logbookPredicate = cb.disjunction();
         if (!logbook_matches.isEmpty()) {
             logbookPredicate = cb.and(logbookPredicate, logbooks.get(Logbook_.name).in(logbook_matches));
         }
         for (String s : logbook_patterns) {
             logbookPredicate = cb.and(logbookPredicate, cb.like(logbooks.get(Logbook_.name), s));
         }
 
         Predicate propertyPredicate = cb.disjunction();
         if (!property_matches.isEmpty()) {
             propertyPredicate = cb.and(propertyPredicate, property.get(Property_.name).in(property_matches));
         }
         for (String s : property_patterns) {
             propertyPredicate = cb.and(propertyPredicate, cb.like(property.get(Property_.name), s));
         }
 
         Predicate propertyAttributePredicate = cb.disjunction();
         for (Map.Entry<String, String> match : value_patterns.entries()) {
             // Key is coming in as property.attribute
             List<String> group = Arrays.asList(match.getKey().split("\\."));
             if (group.size() == 2) {
                 propertyAttributePredicate = cb.and(propertyAttributePredicate,
                         cb.like(logAttribute.get(LogAttribute_.value),
                         match.getValue()), property.get(Property_.name).in(group.get(0),
                         attribute.get(Attribute_.name).in(group.get(1))));
             }
         }
 
         Predicate searchPredicate = cb.disjunction();
         for (String s : log_patterns) {
             searchPredicate = cb.or(cb.like(from.get(Log_.description), s), searchPredicate);
         }
 
         Predicate datePredicate = cb.disjunction();
         if (!date_matches.isEmpty()) {
             String start = null, end = null;
             for (Map.Entry<String, Collection<String>> match : date_matches.asMap().entrySet()) {
                 if (match.getKey().toLowerCase().equals("start")) {
                     start = match.getValue().iterator().next();
                 }
                 if (match.getKey().toLowerCase().equals("end")) {
                     end = match.getValue().iterator().next();
                 }
             }
             if (start != null && end == null) {
                 Date jStart = new java.util.Date(Long.valueOf(start) * 1000);
                 Date jEndNow = new java.util.Date(Calendar.getInstance().getTime().getTime());
                 datePredicate = cb.between(entry.get(Entry_.createdDate),
                         jStart,
                         jEndNow);
             } else if (start == null && end != null) {
                 Date jStart1970 = new java.util.Date(0);
                 Date jEnd = new java.util.Date(Long.valueOf(end) * 1000);
                 datePredicate = cb.between(entry.get(Entry_.createdDate),
                         jStart1970,
                         jEnd);
             } else {
                 Date jStart = new java.util.Date(Long.valueOf(start) * 1000);
                 Date jEnd = new java.util.Date(Long.valueOf(end) * 1000);
                 datePredicate = cb.between(entry.get(Entry_.createdDate),
                         jStart,
                         jEnd);
             }
         }
 
         cq.distinct(true);
 
         Predicate statusPredicate = cb.equal(from.get(Log_.state), State.Active);
         Predicate finalPredicate = cb.and(statusPredicate, logbookPredicate, tagPredicate, propertyPredicate, propertyAttributePredicate, datePredicate, searchPredicate);
         cq.where(finalPredicate);
         cq.orderBy(cb.desc(entry.get(Entry_.createdDate)));
         TypedQuery<Log> typedQuery = em.createQuery(cq);
 
         if (!paginate_matches.isEmpty()) {
             String page = null, limit = null;
             for (Map.Entry<String, Collection<String>> match : paginate_matches.asMap().entrySet()) {
                 if (match.getKey().toLowerCase().equals("limit")) {
                     limit = match.getValue().iterator().next();
                 }
                 if (match.getKey().toLowerCase().equals("page")) {
                     page = match.getValue().iterator().next();
                 }
             }
             if (limit != null && page != null) {
                 Integer offset = Integer.valueOf(page) * Integer.valueOf(limit) - Integer.valueOf(limit);
                 typedQuery.setFirstResult(offset);
                 typedQuery.setMaxResults(Integer.valueOf(limit));
             }
         }
 
         JPAUtil.startTransaction(em);
 
         try {
             Logs result = new Logs();
 
             result.setCount(JPAUtil.count(em, cq));
             if (empty) {
                 return result;
             }
 
             List<Log> rs = typedQuery.getResultList();
 
             if (rs != null) {
                 Iterator<Log> iterator = rs.iterator();
                 while (iterator.hasNext()) {
                     Log log = iterator.next();
                     Entry e = log.getEntry();
                     Collection<Log> logs = e.getLogs();
                     log.setVersion(String.valueOf(logs.size()));
                     Iterator<LogAttribute> iter = log.getAttributes().iterator();
                     while (iter.hasNext()) {
                         LogAttribute logattr = iter.next();
                         Attribute attr = logattr.getAttribute();
                         XmlProperty xmlProperty = attr.getProperty().toXmlProperty();
                         Map<String, String> map = xmlProperty.getAttributes();
                         map.put(attr.getName(), logattr.getValue());
                         xmlProperty.setAttributes(map);
                         log.addXmlProperty(xmlProperty);
                     }
                     result.addLog(log);
                 }
             }
 
             return result;
         } catch (Exception e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "JPA exception: " + e);
         } finally {
             JPAUtil.finishTransacton(em);
         }
     }
 
     /**
      * Finds a log and edits in the database by id.
      *
      * @return Log
      * @throws CFException wrapping an SQLException
      */
     public static Log findLog(Long id) throws CFException {
         try {
             Entry entry = (Entry) JPAUtil.findByID(Entry.class, id);
             Collection<Log> logs = entry.getLogs();
             Log result = Collections.max(logs);
             result.setVersion(String.valueOf(logs.size()));
             Iterator<LogAttribute> iter = result.getAttributes().iterator();
             Set<XmlProperty> xmlProperties = new HashSet<XmlProperty>();
             while (iter.hasNext()) {
                 XmlProperty xmlProperty = new XmlProperty();
                 HashMap<String, String> map = new HashMap<String, String>();
                 LogAttribute logattr = iter.next();
                 Attribute attr = logattr.getAttribute();
                 xmlProperty.setName(attr.getProperty().getName());
                 xmlProperty.setId(attr.getProperty().getId());
                 map.put(attr.getName(), logattr.getValue());
                 xmlProperty.setAttributes(map);
                 xmlProperties.add(xmlProperty);
             }
             result.setXmlProperties(xmlProperties);
             return result;
         } catch (Exception e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "JPA exception: " + e);
         }
     }
 
     /**
      * Creates a Log in the database.
      *
      * @param name name of tag
      * @param owner owner of tag
      * @throws CFException wrapping an SQLException
      */
     public static Log create(Log log) throws CFException {
         em = JPAUtil.getEntityManagerFactory().createEntityManager();
         JPAUtil.startTransaction(em);
         Log newLog = new Log();
         newLog.setState(State.Active);
         newLog.setLevel(log.getLevel());
         newLog.setOwner(log.getOwner());
         newLog.setDescription(log.getDescription());
         newLog.setSource(log.getSource());
         em.persist(newLog);
         if (!log.getLogbooks().isEmpty()) {
             Iterator<Logbook> iterator = log.getLogbooks().iterator();
             Set<Logbook> logbooks = new HashSet<Logbook>();
             while (iterator.hasNext()) {
                 String logbookName = iterator.next().getName();
                 Logbook logbook = LogbookManager.findLogbook(logbookName);
                 if (logbook != null) {
                     logbook = em.merge(logbook);
                     logbook.addLog(newLog);
                     logbooks.add(logbook);
                 } else {
                     throw new CFException(Response.Status.NOT_FOUND,
                             "Log entry " + log.getId() + " logbook:" + logbookName + " does not exists.");
                 }
             }
             newLog.setLogbooks(logbooks);
         } else {
             throw new CFException(Response.Status.NOT_FOUND,
                     "Log entry " + log.getId() + " must be in at least one logbook.");
         }
         if (log.getTags() != null) {
             Iterator<Tag> iterator2 = log.getTags().iterator();
             Set<Tag> tags = new HashSet<Tag>();
             while (iterator2.hasNext()) {
                 String tagName = iterator2.next().getName();
                 Tag tag = TagManager.findTag(tagName);
                 if (tag != null) {
                     tag = em.merge(tag);
                     tag.addLog(newLog);
                     tags.add(tag);
                 } else {
                     throw new CFException(Response.Status.NOT_FOUND,
                             "Log entry " + log.getId() + " tag:" + tagName + " does not exists.");
                 }
             }
             newLog.setTags(tags);
         }
         try {
             if (log.getEntryId() != null) {
                 Entry entry = (Entry) JPAUtil.findByID(Entry.class, log.getEntryId());
                 if (entry.getLogs() != null) {
                     List<Log> logs = entry.getLogs();
                     ListIterator<Log> iterator = logs.listIterator();
                     while (iterator.hasNext()) {
                         Log sibling = iterator.next();
                         sibling = em.merge(sibling);
                         sibling.setState(State.Inactive);
                         iterator.set(sibling);
                     }
                     entry.addLog(newLog);
                 }
                 newLog.setState(State.Active);
                 newLog.setEntry(entry);
                 em.merge(entry);
             } else {
                 Entry entry = new Entry();
                 newLog.setState(State.Active);
                 entry.addLog(newLog);
                 newLog.setEntry(entry);
                 em.persist(entry);
             }
             em.flush();
             if (log.getXmlProperties() != null) {
                 Set<LogAttribute> logattrs = new HashSet<LogAttribute>();
                 for (XmlProperty p : log.getXmlProperties()) {
                     Property prop = PropertyManager.findProperty(p.getName());
                    
                     for (Map.Entry<String, String> att : p.getAttributes().entrySet()) {
                         Attribute newAtt = AttributeManager.findAttribute(prop, att.getKey());
                        LogAttribute logattr = new LogAttribute();
                         logattr.setAttribute(newAtt);
                         logattr.setLog(newLog);
                         logattr.setAttributeId(newAtt.getId());
                         logattr.setLogId(newLog.getId());
                         logattr.setValue(att.getValue());
                         logattr.setGroupingNum(0L);
                         em.persist(logattr);
                         logattrs.add(logattr);
                     }
                     newLog.setAttributes(logattrs);
                 }
             }
             newLog.setXmlProperties(log.getXmlProperties());
             JPAUtil.finishTransacton(em);
             return newLog;
         } catch (Exception e) {
             JPAUtil.transactionFailed(em);
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "JPA exception: " + e);
         }
 
     }
 
     /**
      * Remove a tag (mark as Inactive).
      *
      * @param name tag name
      */
     public static void remove(Long id) throws CFException {
         try {
             Entry entry = (Entry) JPAUtil.findByID(Entry.class, id);
             if (entry != null) {
                 if (entry.getLogs() != null) {
                     List<Log> logs = entry.getLogs();
                     ListIterator<Log> iterator = logs.listIterator();
                     while (iterator.hasNext()) {
                         Log sibling = iterator.next();
                         sibling.setState(State.Inactive);
                         iterator.set(sibling);
                         JPAUtil.update(sibling);
                     }
                 }
             }
         } catch (Exception e) {
             throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                     "JPA exception: " + e);
 
         }
     }
 }
