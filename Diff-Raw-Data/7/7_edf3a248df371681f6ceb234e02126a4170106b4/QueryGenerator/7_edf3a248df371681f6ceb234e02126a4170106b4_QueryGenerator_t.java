 package joins.queries;
 
 import joins.indexer.WordLists;
 
 import java.io.BufferedWriter;
 import java.io.Closeable;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class QueryGenerator {
   private final String OUTPUT_FILE = "queries.txt";
   private static final int ACL_RANGE = 10000;
 
   protected QGen queryConsumer;
   
   interface QGen {
 	  void onQuery(String terms, String aclRange);
   }
   
   static class QueryFile implements QGen, Closeable{
 	  
 	  private final BufferedWriter writer;
 	private String format;
 	  
 	  QueryFile(String fileName, String format) throws IOException{
 		  writer = new BufferedWriter(new FileWriter(fileName));
 		  this.format = format;
 	  }
 	  
 	@Override
 	public void close() throws IOException {
 		writer.close();
 	}
 
 	@Override
 	public void onQuery(String terms, String aclRange) {
 	    try {
 			writer.write(String.format(format,
 			        terms, aclRange ));
 			writer.newLine();
 		} catch (IOException e) {
 			throw new RuntimeException("wrap", e);
 		}
 	}
 	  
   }
 
 	public static void main(String[] args) throws IOException {
 		final List<QueryFile> consumers = Arrays
 				.asList(new QueryFile("queries-join.txt", "q=text_all:(%s)" +
 				// "&fl=id,score&sort=score desc&fq={!join from=few_join_id to=few_id}acl:%s",
 						"&fl=id,score&sort=score desc&fq={!join from=join_id to=id}acl:%s"),
 						new QueryFile(
 								"queries-bjq-fq.txt",
 								"q=text_all:(%s)"
										+ "&fl=id,score&sort=score desc&fq={!parent which=kind:body}acl:%s"),
 
						new QueryFile("queries-bjq.txt", // AND _query_:\"{!parent which=kind:body}acl:%s\""
								"q=text_all:(%s) AND _query_:\"{!parent which=kind:body}acl:%s\""
 										+ "&fl=id,score&sort=score desc"),
 						new QueryFile("queries-no-acls.txt",
 								"q=text_all:(%s)&fl=id,score&sort=score desc"));
 		
 		QueryGenerator qg = new QueryGenerator();
 		qg.queryConsumer = new QGen() {
 			@Override
 			public void onQuery(String terms, String aclRange) {
 				for (QGen consumer : consumers) {
 					consumer.onQuery(terms, aclRange);
 				}
 			}
 		};
 		
 		main(qg);
 		
 		for (QueryFile consumer : consumers) {
 			consumer.close();
 		}
 	}
 
 protected static void main(QueryGenerator qg) {
 	try {
       WordLists.init();
       qg.generateQueries();
     } catch (Exception e) {
       e.printStackTrace();
     }
 }
 
   void generateQueries() throws Exception {
     for (int idx = 0; idx < 10000; ++idx) {
       // Solrmeter doesn't need the above in the text file, you enter that in a screen.
       String terms = getTerms(idx);
 	String aclRange = getAclRange(idx);
 	String place = getPlace(idx);
 //      String raw = String.format("q=kind:instance OR text_all:(%s)" +
 //          "&fl=id,score&sort=score desc&fq={!bbox}&sfield=place&pt=%s&d=100",
 //          getTerms(idx), getPlace(idx));
 
 //      String uri = URLEncoder.encode(raw, "UTF-8");
 	queryConsumer.onQuery(terms, aclRange);
     }
   }
 
   String getTerms(int idx) {
     // Idea here is that if idx mod 10 is between 1-5 (inclusive), use one term. 6-7 two terms, 8-9 three terms. ORed.
     int modulus = idx % 10;
     if (modulus < 6) return String.format("%s", WordLists.getCommonWord());
     if (modulus < 8) return String.format("%s OR %s", WordLists.getCommonWord(), WordLists.getCommonWord());
     return String.format("%s OR %s OR %s", WordLists.getCommonWord(), WordLists.getCommonWord(), WordLists.getCommonWord());
   }
 
   String getAclRange(int idx) {
     // Similarly to above. Two random numbers here, one is the base ACL number (range 1-10000) and the second the number
     // of entries (up to 100). All this is arbitrary.
 
     int base = WordLists.getInt(ACL_RANGE);
     int count = WordLists.getInt(10);
     if (base + count >= ACL_RANGE) base = (ACL_RANGE - 1) - count;
     return String.format("[%s TO %s]", base, base + count);
   }
 
   String getPlace(int idx) {
     Boolean doNeg = false;
     if ((idx % 2) == 0) {
       doNeg = true;
     }
     int lat = WordLists.getInt(90) * ((doNeg) ? 1 : -1);
     int lon = WordLists.getInt(90) * ((doNeg) ? 1 : -1);
     return String.format("%d,%d", lat, lon);
   }
 }
