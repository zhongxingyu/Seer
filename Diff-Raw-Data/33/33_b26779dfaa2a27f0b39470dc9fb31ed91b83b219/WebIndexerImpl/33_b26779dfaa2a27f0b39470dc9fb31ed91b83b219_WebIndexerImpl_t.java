 package ru.chuprikov.search.web.index;
 
 import ru.chuprikov.search.database.*;
 import ru.chuprikov.search.database.datatypes.Datatypes;
 import ru.chuprikov.search.database.datatypes.Document;
 import ru.chuprikov.search.database.datatypes.ParsedProblem;
 import ru.chuprikov.search.database.datatypes.ProblemID;
 import ru.chuprikov.search.index.indexer.Indexer;
 import ru.chuprikov.search.index.indexer.spimi.SPIMIIndexer;
 import ru.chuprikov.search.web.fetch.ProcessStatistics;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.jws.WebService;
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 @WebService(endpointInterface = "ru.chuprikov.search.web.index.WebIndexer")
 public class WebIndexerImpl implements WebIndexer {
     private SearchDatabase searchDB;
     private DocumentDB documentDB;
     private TermDB termDB;
     private ParsedDB parsedDB;
 
     private static void splitWithType(Map<String, Datatypes.Posting.Builder> dic, String text, long docID, Datatypes.Posting.PositionType type) {
         int positionIdx = 0;
         for (String s : text.split("[\\W]")) {
             if (!s.isEmpty()) {
                 if (!dic.containsKey(s)) {
                     dic.put(s, Datatypes.Posting.newBuilder());
                     dic.get(s).setDocumentID(docID);
                 }
                 dic.get(s).addPositions(Datatypes.Posting.Position.newBuilder().setIndex(positionIdx++).setType(type).build());
             }
         }
     }
 
     @PostConstruct
     private void openDatabaseConnection() throws Exception {
         searchDB = SearchDatabases.openBerkeley(new File(System.getProperty("user.dir") + "/mydb"));
         documentDB = searchDB.openDocumentDB();
         termDB = searchDB.openTermDB();
         parsedDB = searchDB.openParseDB();
     }
 
     @PreDestroy
     private void closeDatabaseConnection() throws Exception {
         termDB.close();
         documentDB.close();
         parsedDB.close();
         searchDB.close();
     }
 
 
 
     @Override
     public ProcessStatistics index(ProblemID from, ProblemID to, long maxMemoryUsage, int maxPostingsChunkSize) throws Exception {
         int total = 0;
         int successful = 0;
         try (IndexDB indexDB = searchDB.openIndexDB(maxPostingsChunkSize);
              Indexer indexer = new SPIMIIndexer(new File(System.getProperty("user.dir") + "/spimi"), indexDB, termDB, maxMemoryUsage);
              CloseableIterator<ParsedProblem> parsedIter = parsedDB.upperBound(from)) {
 
             while (parsedIter.hasNext()) {
                 final ParsedProblem parsedProblem = parsedIter.next();
                 if (parsedProblem.getProblemID().compareTo(to) > 0)
                     break;
 
                 total++;
                 Map<String, Datatypes.Posting.Builder> documentPostings = new HashMap<>();
                 try { //TODO: What the hell? Why is this try block here?
                     long documentID = documentDB.addDocument(new Document(parsedProblem.getProblemID(), parsedProblem.getUrl()));
                     splitWithType(documentPostings, parsedProblem.getTitle(), documentID, Datatypes.Posting.PositionType.TITLE);
                     splitWithType(documentPostings, parsedProblem.getInputSpecification(), documentID, Datatypes.Posting.PositionType.INPUT_SPEC);
                     splitWithType(documentPostings, parsedProblem.getOutputSpecification(), documentID, Datatypes.Posting.PositionType.OUTPUT_SPEC);
                     splitWithType(documentPostings, parsedProblem.getCondition(), documentID, Datatypes.Posting.PositionType.PLAIN_TEXT);
                 } catch (Exception e) {
                     continue;
                 }
 
                 for (Map.Entry<String, Datatypes.Posting.Builder> e : documentPostings.entrySet()) {
                     indexer.addToIndex(e.getKey(), e.getValue().build());
                 }
                 successful++;
             }
         }
         return new ProcessStatistics(total, successful, 0);
     }
 
     @Override //TODO: remove code duplication
     public ProcessStatistics indexAll(long maxMemoryUsage, int maxPostingsChunkSize) throws Exception {
         int total = 0;
         int successful = 0;
         try (IndexDB indexDB = searchDB.openIndexDB(maxPostingsChunkSize);
              Indexer indexer = new SPIMIIndexer(new File(System.getProperty("user.dir") + "/spimi"), indexDB, termDB, maxMemoryUsage);
              CloseableIterator<ParsedProblem> parsedIter = parsedDB.iterator()) {
 
             while (parsedIter.hasNext()) {
                 final ParsedProblem parsedProblem = parsedIter.next();
 
                 total++;
                 Map<String, Datatypes.Posting.Builder> documentPostings = new HashMap<>();
                 try { //TODO: What the hell? Why is this try block here?
                     long documentID = documentDB.addDocument(new Document(parsedProblem.getProblemID(), parsedProblem.getUrl()));
                     splitWithType(documentPostings, parsedProblem.getTitle(), documentID, Datatypes.Posting.PositionType.TITLE);
                     splitWithType(documentPostings, parsedProblem.getInputSpecification(), documentID, Datatypes.Posting.PositionType.INPUT_SPEC);
                     splitWithType(documentPostings, parsedProblem.getOutputSpecification(), documentID, Datatypes.Posting.PositionType.OUTPUT_SPEC);
                     splitWithType(documentPostings, parsedProblem.getCondition(), documentID, Datatypes.Posting.PositionType.PLAIN_TEXT);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 for (Map.Entry<String, Datatypes.Posting.Builder> e : documentPostings.entrySet()) {
                     indexer.addToIndex(e.getKey(), e.getValue().build());
                 }
                 successful++;
             }
         }
         return new ProcessStatistics(total, successful, 0);
     }
 }
