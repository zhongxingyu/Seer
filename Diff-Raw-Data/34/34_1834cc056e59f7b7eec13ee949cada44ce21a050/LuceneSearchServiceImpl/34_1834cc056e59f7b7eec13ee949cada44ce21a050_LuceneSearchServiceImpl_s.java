 package com.thompson234.bg95.service.impl;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.Sets;
 import com.thompson234.bg95.dao.AircraftDao;
 import com.thompson234.bg95.dao.AirmanDao;
 import com.thompson234.bg95.dao.MissionDao;
 import com.thompson234.bg95.model.Aircraft;
 import com.thompson234.bg95.model.Airman;
 import com.thompson234.bg95.model.CrewAssignment;
 import com.thompson234.bg95.model.Mission;
 import com.thompson234.bg95.model.Name;
 import com.thompson234.bg95.model.SearchResult;
 import com.thompson234.bg95.model.Sortie;
 import com.thompson234.bg95.model.Type;
 import com.thompson234.bg95.service.SearchService;
 import com.yammer.dropwizard.logging.Log;
 import org.apache.commons.io.IOUtils;
 import org.apache.lucene.analysis.SimpleAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 public class LuceneSearchServiceImpl implements SearchService {
     private static final Log _sLog = Log.forClass(LuceneSearchServiceImpl.class);
 
     private final AircraftDao _aircraftDao;
     private final AirmanDao _airmanDao;
     private final MissionDao _missionDao;
 
     private final Directory _directory;
     private final QueryParser _parser =
             new QueryParser(Version.LUCENE_36, "name", new SimpleAnalyzer(Version.LUCENE_36));
     private IndexSearcher _searcher;
 
     @Inject
     public LuceneSearchServiceImpl(AircraftDao aircraftDao,
                                    AirmanDao airmanDao,
                                    MissionDao missionDao,
                                    Directory directory) {
 
         _aircraftDao = aircraftDao;
         _airmanDao = airmanDao;
         _missionDao = missionDao;
 
         try {
             _directory = directory;
            _searcher = new IndexSearcher(IndexReader.open(_directory));
         } catch (Exception ex) {
             _sLog.error("Error creating search service.", ex);
             throw Throwables.propagate(ex);
         }
     }
 
     private synchronized IndexSearcher getIndexSearcher() {

         IndexReader reader = null;
 
         try {
            reader = IndexReader.openIfChanged(_searcher.getIndexReader());
         } catch (Exception ex) {
             _sLog.error("Error obtaining reader for search.", ex);
             throw Throwables.propagate(ex);
         }
 
        if (reader != null) {
             _searcher = new IndexSearcher(reader);
         }
 
         return _searcher;
     }
 
     @Override
     public SearchResult search(String query) {
         try {
             final IndexSearcher searcher = getIndexSearcher();
             final IndexReader reader = searcher.getIndexReader();
 
             final Query parsed = _parser.parse(query);
             final TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
 
             searcher.search(parsed, collector);
 
             _sLog.debug("Total hits for {} : {}", query, collector.getTotalHits());
 
             final TopDocs topDocs = collector.topDocs();
             final SearchResult result = new SearchResult(query);
             final Set ids = Sets.newHashSet();
 
             for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
                 final Document doc = reader.document(scoreDoc.doc);
                 final String id = doc.get("id");
                 final String type = doc.get("type");
 
                 if (Airman.class.getSimpleName().equals(type) && ids.add(id)) {
                     result.airmanId(id);
                     for (String missionId: Sets.difference(getCollectionIds(doc, "missionId"), ids)) {
                         result.missionId(missionId);
                     }
 
                     for (String acId: Sets.difference(getCollectionIds(doc, "aircraftId"), ids)) {
                         result.aircraftId(acId);
                     }
                 } else if (Aircraft.class.getSimpleName().equals(type) && ids.add(id)) {
                     result.aircraftId(id);
 
                     for (String missionId: Sets.difference(getCollectionIds(doc, "missionId"), ids)) {
                         result.missionId(missionId);
                     }
                 } else if (Mission.class.getSimpleName().equals(type) && ids.add(id)) {
                     result.missionId(id);
 
                     for (String missionId: Sets.difference(getCollectionIds(doc, "aircraftId"), ids)) {
                         result.missionId(missionId);
                     }
 
                     for (String missionId: Sets.difference(getCollectionIds(doc, "airmanId"), ids)) {
                         result.missionId(missionId);
                     }
                 }
             }
 
             return result;
         } catch (Exception ex) {
             _sLog.error("Error occurred during search.", ex);
             throw Throwables.propagate(ex);
         }
     }
 
     private Set<String> getCollectionIds(Document doc, String collectionName) {
         final Set<String> result = Sets.newHashSet();
         String[] ids = doc.getValues(collectionName);
 
         if (ids != null) {
             for (String id: ids) {
                 result.add(id);
             }
         }
 
         return result;
     }
 
     @Override
     public void buildIndex() {
 
         final IndexWriterConfig writerConfig =
                 new IndexWriterConfig(Version.LUCENE_36, new SimpleAnalyzer(Version.LUCENE_36));
         //we are overwriting
         writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
 
         IndexWriter writer = null;
 
         try {
             writer = new IndexWriter(_directory, writerConfig);
 
             indexAirmen(writer);
             indexAircraft(writer);
             indexMissions(writer);
         } catch (Exception ex) {
             _sLog.error("Error creating index.", ex);
             throw Throwables.propagate(ex);
         } finally {
             IOUtils.closeQuietly(writer);
         }
     }
 
     private void indexAirmen(IndexWriter writer) throws IOException {
 
         for (Airman airman: _airmanDao.findAll()) {
             final Document airmanDoc = new Document();
             airmanDoc.add(new Field("id", airman.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             airmanDoc.add(new Field("type", Type.AIRMAN, Field.Store.YES, Field.Index.NOT_ANALYZED));
 
             final Name name = airman.getName();
             //last name is required, no null check needed
             airmanDoc.add(new Field("lastName", name.getLastName(), Field.Store.NO, Field.Index.ANALYZED));
 
             final String firstName = name.getFirstName();
             if (firstName != null) {
                 airmanDoc.add(new Field("firstName", firstName, Field.Store.NO, Field.Index.ANALYZED));
             }
 
             airmanDoc.add(new Field("fullName", name.getFullName(), Field.Store.NO, Field.Index.ANALYZED));
             airmanDoc.add(new Field("name", name.getFullName(), Field.Store.NO, Field.Index.ANALYZED));
 
             final Set<String> associatedAircraft = Sets.newHashSet();
             for (Mission mission: _missionDao.findAllByAirman(airman)) {
 
                 airmanDoc.add(new Field("missionId", mission.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 associatedAircraft.add(mission.getSortieByAirmanId(airman.getId()).getAircraft().getId());
             }
 
             for (String acId: associatedAircraft) {
                 airmanDoc.add(new Field("aircraftId", acId, Field.Store.YES, Field.Index.NOT_ANALYZED));
             }
 
             writer.addDocument(airmanDoc);
         }
     }
 
     private void indexAircraft(IndexWriter writer) throws IOException {
 
         for (Aircraft ac: _aircraftDao.findAll()) {
             final Document acDoc = new Document();
             acDoc.add(new Field("id", ac.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             acDoc.add(new Field("type", Type.AIRCRAFT, Field.Store.YES, Field.Index.NOT_ANALYZED));
             acDoc.add(new Field("number", ac.getNumber(), Field.Store.NO, Field.Index.ANALYZED));
 
             for (String name: ac.getNames()) {
                 acDoc.add(new Field("name", name, Field.Store.YES, Field.Index.ANALYZED));
             }
 
             for (Mission mission: _missionDao.findAllByAircraft(ac)) {
                 acDoc.add(new Field("missionId", mission.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             }
 
             writer.addDocument(acDoc);
         }
     }
 
     private void indexMissions(IndexWriter writer) throws IOException {
 
         for (Mission mission: _missionDao.findAll()) {
             final Document missionDoc = new Document();
             missionDoc.add(new Field("id", mission.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             missionDoc.add(new Field("type", Type.MISSION, Field.Store.YES, Field.Index.NOT_ANALYZED));
             missionDoc.add(new Field("destination", mission.getDestination(), Field.Store.YES, Field.Index.ANALYZED));
             missionDoc.add(new NumericField("date", Field.Store.NO, true).setLongValue(mission.getDate().getTime()));
 
             for (Sortie sortie: mission.getSorties()) {
                 missionDoc.add(new Field("aircraftId", sortie.getAircraft().getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 
                 for (CrewAssignment ca: sortie.getCrewAssignments()) {
                     missionDoc.add(new Field("airmanId", ca.getAirman().getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                 }
             }
 
             writer.addDocument(missionDoc);
         }
     }
 
 }
