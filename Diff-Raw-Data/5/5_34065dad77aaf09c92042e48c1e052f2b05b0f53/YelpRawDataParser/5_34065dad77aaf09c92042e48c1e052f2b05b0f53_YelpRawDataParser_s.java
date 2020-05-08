 package com.where.atlas.feed;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.queryParser.QueryParser.Operator;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Filter;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.QueryWrapperFilter;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.spell.JaroWinklerDistance;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.apache.lucene.util.Version;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import com.where.place.Address;
 import com.where.place.Place;
 import com.where.util.lucene.NullSafeGeoFilter;
 import com.where.util.lucene.NullSafeGeoFilter.GeoHashCache;
 
 
 public class YelpRawDataParser implements FeedParser { 
     
     protected YelpParserUtils parser;
     protected final float withPhoneThreshold_ = .65f;
     protected final float withoutPhoneThreshold_ = .9f;
     protected BufferedWriter bufferedWriter;
     protected IndexSearcher searcher;
     protected Set<String> cityMap,zipMap;
     protected Map<Place,String> searchedPlaces; 
     protected GeoHashCache geoCache;
     protected JaroWinklerDistance jw;
     protected Analyzer analyzer;
     protected HashMap<String,Float> boosts;
     protected MultiFieldQueryParser qp;
     protected int counter;
 //    protected static String currentState;
     private static final String del = "\t";
     
     public YelpRawDataParser(YelpParserUtils Yparser)
     {
         parser=Yparser;
         BooleanQuery.setMaxClauseCount(10000);
         try{
             searchedPlaces = new ConcurrentHashMap<Place,String>();
             
             bufferedWriter = new BufferedWriter(new FileWriter(parser.getTargetPath()));
             searcher = new IndexSearcher(new NIOFSDirectory(new File(parser.getOldIndexPath())));
             
             zipMap = populateMapFromTxt(new Scanner(new File("/home/fliuzzi/data/bostonMarketZipCodes.txt")));
             System.out.println("Loaded zipcode map: " + zipMap.size() + " entries.");
             cityMap = populateMapFromTxt(new Scanner(new File("/home/fliuzzi/data/bostoncityCSIDS.txt")));
             System.out.println("Loaded city-map: " + cityMap.size() + " entries.");
             counter=0;
             
             geoCache = new GeoHashCache(CSListingDocumentFactory.LATITUDE_RANGE, CSListingDocumentFactory.LONGITUDE_RANGE,
                     CSListingDocumentFactory.LISTING_ID, searcher.getIndexReader());
             jw = new JaroWinklerDistance();
             analyzer = CSListingDocumentFactory.getAnalyzerWrapper();
             boosts = new HashMap<String, Float>();
             boosts.put(CSListingDocumentFactory.NAME,1.0f);
         }
         catch(Throwable t){
             System.err.print("Error Loading!");
         }
     }
     
     private synchronized void writeEntry(StringBuilder strBuilder)
     {
         counter++;
        if(counter % 50 == 0)
            System.out.print("+");
         if(counter % 500 == 0)
             System.out.println();
         
         try{
             bufferedWriter.write(strBuilder.toString());
             bufferedWriter.newLine();
         }
         catch(Throwable t){
             System.err.println("Error writing entry: "+strBuilder);
         }
     }
     
     public Set<String> populateMapFromTxt(Scanner in)
     {
         Set<String> txtSet = new HashSet<String>();
         
         
         while(in.hasNextLine())
         {
             txtSet.add(in.nextLine().trim());
         }
         in.close();
         return txtSet;
     }
     
     private String cleanReview(String review)
     {
         review.replace("\\n","");
         review.replace("\\t", "");
         return review;
     }
     
     public void closeWriter()
     {
         try{
             bufferedWriter.close();
         }
         catch(Exception e)
         {
             System.err.println("err closing output file");
         }
     }
     
     //Makes sure the first character of line is not a * or ?
     //                                     (query safe)
     protected String cleanForQuery(String line)
     {
         if(line == null || line.trim().length() == 0){return line;}
         
         char firstLetter = line.charAt(0);
         
         if(firstLetter =='*' || firstLetter == '?')
             return cleanForQuery(line.substring(1));
         else
         {
             //get rid of unsafe characters
             line.replace("&#39;", "'");
             line.replace("&amp;", "&");
             
             return QueryParser.escape(line);
         }
     }
     
         
     public static String stripPhone(String phone) {
         if(phone == null || phone.trim().length() == 0){return phone;}
         StringBuffer buffer = new StringBuffer();
         char[] chrs = phone.toCharArray();
         for(int i = 0, n = chrs.length; i < n; i++) {
             if(Character.isDigit(chrs[i])) {
                 buffer.append(chrs[i]);
             }
         }
         String tel = buffer.toString();
         if(tel.length() > 10) tel = tel.substring(0, 9);
         
         return tel;
     }
     
     public BooleanQuery getLatLongQueryFromCriteria(double lat, double lng)
     {
         BooleanQuery rawQuery = new BooleanQuery();
         Query latQ = NumericRangeQuery.newDoubleRange(
                 CSListingDocumentFactory.LATITUDE_RANGE,
                 lat-.05, lat+.05,
                 true, true);
 
         Query longQ = NumericRangeQuery.newDoubleRange(
                 CSListingDocumentFactory.LONGITUDE_RANGE,
                 lng-.05, lng+.05,
                 true, true);
 
         rawQuery.add(latQ, Occur.MUST);
         rawQuery.add(longQ, Occur.MUST);
         
         return rawQuery;
     }
     
     public void parse(PlaceCollector collector, InputStream ins) throws IOException {
         try{
                 BooleanQuery bq = new BooleanQuery();
                 BooleanQuery mainQuery = new BooleanQuery();
                 
                 String[] fields = { CSListingDocumentFactory.NAME };
                 MultiFieldQueryParser qp = new MultiFieldQueryParser(
                         Version.LUCENE_30, fields, analyzer, boosts);
                 qp.setDefaultOperator(Operator.OR);
                 
                 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                 Document doc = docBuilder.parse(ins);
                 
                 doc.getDocumentElement().normalize();
                 
                 NodeList listOfListings = doc.getElementsByTagName("listing");
                 Node listingNode = null;
                 Place poi = null;
                 Address location = null;
                 
                 for(int i = 0; i < listOfListings.getLength();i++)
                 {
 
                     
                     listingNode = listOfListings.item(i);
                     if(listingNode.getNodeType() == Node.ELEMENT_NODE){
                         Element listingElement = (Element)listingNode;
                         
                         
                         
                         
                         
                         poi = new Place();
                         location = new Address();
                         
                         poi.setName(listingElement.getAttribute("name"));//NAME
                         location.setZip(listingElement.getAttribute("postal_code"));//addy.ZIP
                         poi.setPhone(stripPhone(listingElement.getAttribute("phone")));//PHONE
                         location.setCity(listingElement.getAttribute("locality"));//addy.CITY
                         
                         
                         
                         if(listingElement.getAttribute("lat").length() > 1 && listingElement.getAttribute("lon").length() > 1){
                             location.setLat(Double.parseDouble(listingElement.getAttribute("lat")));//LATITUDE
                             location.setLng(Double.parseDouble(listingElement.getAttribute("lon")));//LONGITUDE
                         }
                         
                         poi.setAddress(location);
                         
                         //Only query if poi location is in zipMap  (speeeeed)
                         if(poi.getAddress().getZip() != null && zipMap.contains(poi.getAddress().getZip()))
                         {
                                 NodeList reviewNodes = listingElement.getElementsByTagName("reviews");
                                 StringBuilder strBuilder = new StringBuilder();
                                     for(int x = 0;x < reviewNodes.getLength();x++)
                                     {
                                         if (strBuilder != null && strBuilder.length() > 0)
                                             strBuilder = strBuilder.delete(0, strBuilder.length());
     
                                         Node reviewNode = reviewNodes.item(x);
                                         if(reviewNode.getNodeType() == Node.ELEMENT_NODE){
                                             Element reviewElement = (Element) reviewNode;
                                             
                                             
                                             
                                             strBuilder.append(reviewElement.getAttribute("date") + del);
                                             strBuilder.append(reviewElement.getAttribute("user_id") + del);
                                             strBuilder.append(reviewElement.getAttribute("rating") + del);
                                             strBuilder.append(cleanReview(reviewElement.getAttribute("text")) + del);
                                             
                                             
                                             if(!searchedPlaces.containsKey(poi))
                                             {
                                             
                                                 bq = getLatLongQueryFromCriteria(poi.getAddress().getLat(),poi.getAddress().getLng());
                                                 
                                                 mainQuery.add(new TermQuery(new Term(
                                                        CSListingDocumentFactory.PHONE,
                                                        poi.getPhone())),
                                                        Occur.SHOULD);
                                             
                                                 try
                                                 {
                                                     mainQuery.add(qp.parse(cleanForQuery(poi.getName())), Occur.SHOULD);
                                                     bq.add(mainQuery, Occur.MUST);
                                                     Filter wrapper = new QueryWrapperFilter(bq);
                                                     
                                                     Filter distanceFilter = 
                                                         new NullSafeGeoFilter(wrapper, poi.getAddress().getLat(),poi.getAddress().getLng(), 1, geoCache, CSListingDocumentFactory.GEOHASH);                     
                                                     TopDocs td = searcher.search(bq, distanceFilter, 10);  //TODO: tweak with 10
                                                     ScoreDoc [] sds = td.scoreDocs;
                                                     for(ScoreDoc sd : sds)
                                                     {
                                                         org.apache.lucene.document.Document d = searcher.getIndexReader().document(sd.doc);
                                                         String csId = d.get(CSListingDocumentFactory.LISTING_ID).trim();
                                                         boolean isInBostonMarket = cityMap.contains(csId);
                                                         if(!isInBostonMarket) continue;
                                                         
                                                         String left = d.get(CSListingDocumentFactory.NAME).toLowerCase().trim();
                                                         String right = poi.getName().toLowerCase().trim();
                                                         float dist = jw.getDistance(left, right);
                                                         
                                                         
                                                         searchedPlaces.put(poi, csId);
                                                         
                                                         
                                                         if(dist > withoutPhoneThreshold_)
                                                         {
                                                             writeEntry(strBuilder.insert(0, csId+del));
                                                             break;
                                                         }
                                                         else
                                                         {
                                                             String phone = d.get(CSListingDocumentFactory.PHONE);
                                                             if(poi.getPhone() != null && phone != null && phone.equals(poi.getPhone()))
                                                             {
                                                                 writeEntry(strBuilder.insert(0, csId+del));
                                                                 break;
                                                             }
                                                         }
                                                     }
                                                 }
                                                 catch (Exception e)
                                                 {
                                                     System.err.println(e.getMessage());
                                                     e.printStackTrace();
                                                 }
                                                 
                                                 
                                             }
                                             else  //POI is recognized, dont need to query...
                                             {
                                                 String csId = searchedPlaces.get(poi);
                                                 writeEntry(strBuilder.insert(0, csId+del));
                                             }
                                         }
                                     }
                             
                             
                         }
                             
                             
                             
                     }
                 }
         }
         catch(Exception e){
             System.err.println(e.getMessage());
             }
     }
 }
 
