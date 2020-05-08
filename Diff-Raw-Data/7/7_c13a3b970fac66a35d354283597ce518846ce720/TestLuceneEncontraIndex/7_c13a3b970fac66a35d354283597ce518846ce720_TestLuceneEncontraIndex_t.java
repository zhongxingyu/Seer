 package pt.inevo.encontra.image.lucene.test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import junit.framework.TestCase;
 import pt.inevo.encontra.image.IndexedImage;
 import pt.inevo.encontra.image.lucene.ImageSearcherFactory;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.index.ResultSetDefaultImp;
 import pt.inevo.encontra.index.search.Searcher;
 import pt.inevo.encontra.query.CriteriaQuery;
 import pt.inevo.encontra.query.criteria.CriteriaBuilderImpl;
 
 /**
  * Test the creation of an ImageObject (with the underlying Document from Lucene)
  * @author ricardo
  */
 public class TestLuceneEncontraIndex extends TestCase {
 
     public TestLuceneEncontraIndex(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     /**
      * this method returns all images from a directory in an arrayList
      * @param directory the directory where all images should be found
      * @param descendIntoSubDirectories - decides if subdirectories should be used also
      * @return the filenames as ArrayList<String>
      * @throws IOException
      */
     public static ArrayList<String> getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
         ArrayList<String> resultList = new ArrayList<String>(256);
         File[] f = directory.listFiles();
         for (File file : f) {
             if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".JPG")) && !file.getName().startsWith("tn_")) {
                 resultList.add(file.getCanonicalPath());
             }
             if (descendIntoSubDirectories && file.isDirectory()) {
                 ArrayList<String> tmp = getAllImages(file, true);
                 if (tmp != null) {
                     resultList.addAll(tmp);
                 }
             }
         }
         if (resultList.size() > 0) {
             return resultList;
         } else {
             return null;
         }
     }
 
 
 
     public void testMain() throws FileNotFoundException, IOException {
         String testFilesPath = getClass().getResource("/images").getPath();
 
         //Searcher<IndexedImage> searcher= ImageSearcherFactory.createCEDDImageSearcher(IndexedImage.class);
         Searcher<IndexedImage> searcher= ImageSearcherFactory.createWeightedSearcher(1,1,1);
         //Searcher<IndexedImage> searcher= ImageSearcherFactory.createFCTHImageSearcher(IndexedImage.class);
 
         //Engine e = new SimpleEngine();
         //e.setSearcher();
 
         for (String path : getAllImages(new File(testFilesPath), true)) {
             System.out.println("Indexing file " + path);
             IndexedImage img= new IndexedImage(path);
             img.setId(path);
 
             searcher.insert(img);
         }
 
         String img=getClass().getResource("/images/img04.jpg").getPath();
         IndexedImage query = new IndexedImage(img);
         query.setId(img);
 
         CriteriaBuilderImpl cb = new CriteriaBuilderImpl();
         CriteriaQuery criteriaQuery = cb.createQuery().where(cb.similar(query, query));
 
         // TODO perform here the similarity query
//        ResultSetDefaultImp<IndexedImage> results = searcher.search(criteriaQuery);
 //        System.out.println("Printing the results...");
 //        for(Result<IndexedImage> r : results){
 //            System.out.println("Similarity: "+r.getSimilarity()+" Result id: " + r.getResult().getId());
 //        }
     }
 }
