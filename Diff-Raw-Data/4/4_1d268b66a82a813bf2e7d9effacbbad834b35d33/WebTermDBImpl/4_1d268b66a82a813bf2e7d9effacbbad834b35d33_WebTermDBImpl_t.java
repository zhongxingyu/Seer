 package ru.chuprikov.search.web.terms;
 
 import ru.chuprikov.search.database.CloseableIterator;
 import ru.chuprikov.search.database.SearchDatabase;
 import ru.chuprikov.search.database.SearchDatabases;
 import ru.chuprikov.search.database.TermDB;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.jws.WebService;
 import java.io.File;
 import java.util.ArrayList;
 
 @WebService(endpointInterface = "ru.chuprikov.search.web.terms.WebTermDB")
 public class WebTermDBImpl implements WebTermDB {
 
     private SearchDatabase searchDB;
     private TermDB termDB;
 
     @PostConstruct
     void openDatabase() {
         try {
             searchDB = SearchDatabases.openBerkeley(new File("/home/pasha/repos/SearchCourseWork/mydb/"));
             termDB = searchDB.openTermDB();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @PreDestroy
     void closeDatabase() {
         try {
             termDB.close();
             searchDB.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public TermInfo getTermInfo(String term) throws Exception {
         return new TermInfo(termDB.get(term), term);
     }
 
     @Override
     public TermInfo getFirstTermInfo() throws Exception {
         try (CloseableIterator<String> it = termDB.iterator()) {
             return it.hasNext() ? getTermInfo(it.next()) : null;
         }
     }
 
     @Override
     public TermInfo[] getNextTermInfos(String term, int length) {
         ArrayList<TermInfo> result = new ArrayList<>();
         try {
            for (CloseableIterator<String> it = termDB.upperBound(term); it.hasNext() && result.size() < length;)
                 result.add(getTermInfo(it.next()));
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result.toArray(new TermInfo[result.size()]);
     }
 }
