 package repository;
 
 import com.github.ldriscoll.ektorplucene.CustomLuceneResult;
 import com.github.ldriscoll.ektorplucene.LuceneAwareCouchDbConnector;
 import com.github.ldriscoll.ektorplucene.LuceneQuery;
 import com.github.ldriscoll.ektorplucene.util.IndexUploader;
 import domain.Patient;
 import org.codehaus.jackson.type.TypeReference;
 import org.ektorp.CouchDbConnector;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class PatientRepository extends org.ektorp.support.CouchDbRepositorySupport<Patient>{
 
     private static final String VIEW_NAME = "Patient";
     private static final String SEARCH_FUNCTION = "findByCriteria";
     private static final String INDEX_FUNCTION = "function(doc) { " +
                 "var index=new Document(); " +
                 "index.add(doc.name, {field: 'name'}); " +
                 "index.add(doc.age, {field: 'age'});" +
                 "index.add(doc.dob, {field: 'dob', type : 'date'});" +
                 "index.add(doc.district, {field: 'district'});" +
                 "index.add(doc.patientId, {field: 'patientId'}); " +
 
                 "if(doc.addresses != undefined) { "+
                "for (var address in doc.addresses) { " +
                    "index.add(address.state, {field: 'state'}); "+
                    "index.add(address.city, {field: 'city'}); " +
                 "}} "+
                 "return index;" +
             "}";
 
     protected PatientRepository(CouchDbConnector db) {
         super(Patient.class, db, true);
         IndexUploader uploader = new IndexUploader();
         uploader.updateSearchFunctionIfNecessary(db, VIEW_NAME, SEARCH_FUNCTION, INDEX_FUNCTION);
     }
 
     public int count(Map<String, String> queryParams) {
        CustomLuceneResult luceneResult = getLuceneResult(queryParams, null, null);
        return luceneResult.getTotalRows();
     }
 
     public List<Patient> find(Map<String, String> queryParams, Integer limit, Integer skip) {
         CustomLuceneResult luceneResult = getLuceneResult(queryParams, limit, skip);
         List<CustomLuceneResult.Row<Patient>> resultRows = luceneResult.getRows();
 
         List<Patient> patients = new ArrayList();
         for(CustomLuceneResult.Row<Patient> row : resultRows){
             patients.add(row.getDoc());
         }
 
         return patients;
     }
 
     private CustomLuceneResult getLuceneResult(Map<String, String> queryParams, Integer limit, Integer skip) {
         LuceneQuery query = new LuceneQuery(VIEW_NAME, SEARCH_FUNCTION);
 
         String queryString = buildQueryString(queryParams);
         query.setQuery(queryString.toString());
         query.setIncludeDocs(true);
         query.setLimit(limit);
         query.setSkip(skip);
         TypeReference resultDocType = new TypeReference<CustomLuceneResult<Patient>>() {};
         return ((LuceneAwareCouchDbConnector) db).queryLucene(query, resultDocType);
     }
 
     private String buildQueryString(Map<String, String> queryParams) {
         StringBuilder queryString = new StringBuilder();
         for(String queryParam : queryParams.keySet()){
             queryString.append(queryParam);
             queryString.append(":");
             queryString.append(queryParams.get(queryParam));
             queryString.append(" AND ");
         }
 
         return queryString.substring(0, queryString.length() - 5);
     }
 }
