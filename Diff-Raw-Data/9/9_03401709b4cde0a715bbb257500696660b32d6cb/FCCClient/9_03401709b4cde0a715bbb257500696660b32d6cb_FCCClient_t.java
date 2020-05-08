 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fccscraper;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.google.code.morphia.query.Query;
 import com.mongodb.Mongo;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.util.PDFTextStripper;
 
 /**
  * Command-line client to use the scraper and search tool.
  *
  * @author armin
  */
 public class FCCClient {
 
     /**
      * Small function to print the menu for the user.
      */
     private static void printMenu() {
         System.out.println("Choose one of the following options:");
         System.out.println("(a) Run a scrape");
         System.out.println("(b) Run a search");
         System.out.println("(c) View System Report");
         System.out.println("(d) Run a search on a sample doc (deprecated)");
         System.out.println("(e) Run a numerical search on a sample doc (deprecated)");
         System.out.println("(q) Quit");
     }
 
     /**
      * Function that takes an input and generates string patterns derived from
      * the input.
      *
      * @param query the input string
      * @return a List<String> of the generated strings.
      */
     private static List<String> generatePatterns(String query) {
         ArrayList<String> patterns = new ArrayList<String>();
         //patterns.add(query);
         patterns.add(query + " %");
         patterns.add(query + "-percent");
         patterns.add(query + " MHz");
         patterns.add(query + "-" + query + "MHz");
         patterns.add(query + " percent");
         patterns.add(query + " million");
         patterns.add(query + " GHz");
         patterns.add(query + " dBm");
         patterns.add(query + " dB");
         patterns.add(query + " dBi");
         patterns.add(query + " meters");
         patterns.add(query + " kilometers");
         patterns.add(query + "-km");
         patterns.add(query + " foot");
         patterns.add(query + " % increase");
         patterns.add(query + " % decrease");
         patterns.add(query + "/" + query + " MHz");
         patterns.add(query + "/" + query + " GHz");
         patterns.add(query + " times");
         patterns.add(query + " devices");
         patterns.add(query + " dB increase");
         patterns.add(query + " dB decrease");
         patterns.add(query + " milliseconds");
         patterns.add(query + " kilobytes");
         patterns.add(query + " times");
         patterns.add(query + " mbps");
         patterns.add(query + " Mbps");
         patterns.add(query + " kbps");
         patterns.add(query + " Kbps");
         patterns.add(query + " dBm/MHz");
         patterns.add(query + " watt");
         patterns.add(query + " watts");
         patterns.add(query + " W");
         patterns.add(query + " kW");
         patterns.add(query + " EIRP");
         patterns.add(query + " degrees");
         patterns.add(query + " billion");
         patterns.add(query + " bits");
         patterns.add(query + " K");
         patterns.add(query + " miles");
         patterns.add(query + "-meter");
         patterns.add(query + " devices");
         patterns.add(query + " handsets");
         patterns.add(query + " towers");
         patterns.add(query + " receivers");
         patterns.add("part " + query);
         patterns.add("Part " + query);
         patterns.add(query + " CFR " + query);
         patterns.add(query + " C.F.R. " + query);
         return patterns;
     }
 
     /**
      * main() function that powers the command-line client.
      *
      * @param args
      */
     public static void main(String[] args) {
         // Get the morphia db connection.
         Datastore ds = ECFSScraperLib.getDataStore("fccTesting", "localhost", 27017);
         String folderPath = "/home/armin/data/scraper";
         String startingURL = "http://apps.fcc.gov/ecfs/proceeding_search/";
         Scanner in = new Scanner(System.in);
         System.out.println("Welcome to the FCC Scraper/Analysis text client!");
         // Get the user input to process.
         String choice = "";
         while (!choice.equals("q")) {
             printMenu();
             choice = in.next();
             // If user chooses to scrape
             if (choice.equals("a")) {
                 java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
                 // Create a new scraper object and set the bureau and
                 // proceeding name parameters.
                 ECFSScraper scraper = new ECFSScraper(ds, folderPath, startingURL);
                 scraper.setBureau("Office of Communications and Business Op");
                 // Run the scraper
                 List<FCCProceeding> proceedings;
                 try {
                     proceedings = scraper.scrapeECFS("htmlunit");
                     // Persist the proceedings.  If they exist already delete them and save
                     // the new proceeding.
                     for (int i = 0; i < proceedings.size(); i++) {
                         FCCProceeding proceeding = ds.createQuery(FCCProceeding.class).field("proceedingURL").equal(proceedings.get(i).getProceedingURL()).get();
                         if (proceeding == null) {
                             proceedings.get(i).save(ds);
                         }
                         else {
                             proceeding.updateWith(ds, proceedings.get(i));
                         }
                     }
                 }
                 catch (MalformedURLException ex) {
                     Logger.getLogger(FCCClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 catch (IOException ex) {
                     Logger.getLogger(FCCClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             // If user chooses to run a search
             else if (choice.equals("b")) {
                 System.out.println("Would you like to search a specific query or run a number series search?:");
                 System.out.println("Press (a) to run a specific query and (b) to run a number series search:");
                 String searchChoice = in.next();
                 // Run a specific query search
                 if (searchChoice.equals("a")) {
                     System.out.println("Please type in the term you would like to search.");
                     // Get the query to search
                     String query = in.next();
                     // Go through every proceeding and filing and search for the query.
                     List<FCCProceeding> list = ds.createQuery(FCCProceeding.class).asList();
                     for (FCCProceeding proceeding : list) {
                         List<Filing> filings = proceeding.getFilings(ds);
                         for (Filing filing : filings) {
                             for (String doc : filing.getDocuments()) {
                                 String location = "/home/armin/data/scraper/" + proceeding.getProceeding() + "/" + doc;
                                 try {
                                     PDDocument pdf = PDDocument.load(location);
                                     PDFTextStripper stripper = new PDFTextStripper();
                                     String text = stripper.getText(pdf);
                                     System.out.println("Printing text for " + filing.getDocuments().get(0));
                                     List<SearchHit> newHits = new ArrayList<SearchHit>();
                                     System.out.println("Searching for " + query);
                                     filing.removeHits(ds, query, query);
                                     Pattern p = Pattern.compile(query);
                                     Matcher matcher = p.matcher(text);
                                     int count = 0;
                                     // Search the documents.  If a hit is found, create a SearchHit object and count the hit.
                                     while (matcher.find()) {
                                         count++;
                                         SearchHit hit = new SearchHit();
                                         hit.setFiling(filing);
                                         hit.setGenericPattern(query);
                                         hit.setActualPattern(query);
                                         newHits.add(hit);
                                     }
                                     System.out.println("Found " + count + " hits for " + query);
                                     filing.recordHits(ds, newHits);
                                     pdf.close();
                                 }
                                 catch (IOException ex) {
                                     Logger.getLogger(FCCClient.class.getName()).log(Level.SEVERE, null, ex);
                                 }
                             }
                         }
                     }
                 }
                 // If the user chooses to search a number range instead, with generated search values
                 else if (searchChoice.equals("b")) {
                     System.out.println("Please type in the beginning index:");
                     int begin = in.nextInt();
                     System.out.println("Please type in the end index:");
                     int end = in.nextInt();
                     List<FCCProceeding> list = ds.createQuery(FCCProceeding.class).asList();
                     for (FCCProceeding proceeding : list) {
                         List<Filing> filings = proceeding.getFilings(ds);
                         for (Filing filing : filings) {
                             for (String doc : filing.getDocuments()) {
                                 String location = "/home/armin/data/scraper/" + proceeding.getProceeding() + "/" + doc;
                                 try {
                                     PDDocument pdf = PDDocument.load(location);
                                     PDFTextStripper stripper = new PDFTextStripper();
                                     String text = stripper.getText(pdf);
                                     System.out.println("Printing text for " + filing.getDocuments().get(0));
                                     List<SearchHit> newHits = new ArrayList<SearchHit>();
                                     for (int i = begin; i <= end; i++) {
                                         List<String> patterns = generatePatterns(Integer.toString(i));
                                         for (String pattern : patterns) {
                                             System.out.println("Searching for " + pattern);
                                            filing.removeHits(ds, Integer.toString(i), pattern);
                                             Pattern p = Pattern.compile(pattern);
                                             Matcher matcher = p.matcher(text);
                                             int count = 0;
                                             while (matcher.find()) {
                                                 count++;
                                                 SearchHit hit = new SearchHit();
                                                 hit.setFiling(filing);
                                                 hit.setGenericPattern(Integer.toString(i));
                                                 hit.setActualPattern(pattern);
                                                 newHits.add(hit);
                                             }
                                             System.out.println("Found " + count + " hits for " + pattern);
                                             ds.save(newHits);
                                         }
                                     }
                                     pdf.close();
                                 }
                                 catch (IOException ex) {
                                     Logger.getLogger(FCCClient.class.getName()).log(Level.SEVERE, null, ex);
                                 }
                             }
                         }
                     }
                 }
                 else {
                     System.out.println("Could not understand the input.  Please try again.");
                 }
 
             }
             // Run a quick report of the system.
             else if (choice.equals("c")) {
                 List<FCCProceeding> procList = ds.createQuery(FCCProceeding.class).asList();
                 int count = 0;
                 for (FCCProceeding proceeding : procList) {
                     System.out.println("Proceeding: " + proceeding.getProceeding());
                     count++;
                     int filingCount = 0;
                     List<Filing> filingList = ds.createQuery(Filing.class).filter("proceeding", proceeding).asList();
                     for (Filing filing : filingList) {
                         filingCount++;
                         List<SearchHit> hitList = ds.createQuery(SearchHit.class).filter("filing", filing).asList();
                         int hitNum = hitList.size();
                         System.out.println("Filing " + filing.getFilingId() + " has " + hitNum + " hits.");
                     }
                     System.out.println(proceeding.getProceeding() + " has " + filingCount + " filings in total.");
                 }
                 System.out.println("There are currently " + count + " proceedings.c");
             }
             // d and e were used in testing and should not be used.
             else if (choice.equals("d")) {
                 System.out.println("Please type in the query you would like to search:");
                 String query = in.next();
                 List<String> patterns = generatePatterns(query);
                 Filing filing = ds.createQuery(Filing.class).field("filingId").equal("6016842789").get();
 
                 String location = "/home/armin/data/scraper/" + filing.getProceeding().getProceeding() + "/" + filing.getDocuments().get(0);
                 try {
                     PDDocument pdf = PDDocument.load(location);
                     PDFTextStripper stripper = new PDFTextStripper();
                     String text = stripper.getText(pdf);
                     System.out.println("Printing text for " + filing.getDocuments().get(0));
                     //System.out.println(text);
                     List<SearchHit> newHits = new ArrayList<SearchHit>();
                     for (String pattern : patterns) {
                         System.out.println("Searching for " + pattern);
                         filing.removeHits(ds, query, pattern);
                         //ds.delete(ds.createQuery(Filing.class).field("filingId").equal("?id=101197").get());
                         Pattern p = Pattern.compile(pattern);
                         Matcher matcher = p.matcher(text);
                         int count = 0;
                         while (matcher.find()) {
                             count++;
                             SearchHit hit = new SearchHit();
                             hit.setFiling(filing);
                             hit.setGenericPattern(query);
                             hit.setActualPattern(pattern);
                             newHits.add(hit);
                         }
                         System.out.println("Found " + count + " hits for " + pattern);
                         filing.recordHits(ds, newHits);
                     }
                     pdf.close();
                 }
                 catch (IOException ex) {
                     Logger.getLogger(FCCClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             else if (choice.equals("e")) {
                 System.out.println("Please type in the beginning index:");
                 int begin = in.nextInt();
                 System.out.println("Please type in the end index:");
                 int end = in.nextInt();
                 //List<String> patterns = generatePatterns(query);
                 //Datastore ds = ECFSScraperLib.getDataStore("fccTesting", "localhost", 27017);
                 Filing filing = ds.createQuery(Filing.class).field("filingId").equal("6016842789").get();
 
                 String location = "/home/armin/data/scraper/" + filing.getProceeding().getProceeding() + "/" + filing.getDocuments().get(0);
                 try {
                     PDDocument pdf = PDDocument.load(location);
                     PDFTextStripper stripper = new PDFTextStripper();
                     String text = stripper.getText(pdf);
                     System.out.println("Printing text for " + filing.getDocuments().get(0));
                     //System.out.println(text);
                     List<SearchHit> newHits = new ArrayList<SearchHit>();
                     for (int i = begin; i <= end; i++) {
                         List<String> patterns = generatePatterns(Integer.toString(i));
                         for (String pattern : patterns) {
                             System.out.println("Searching for " + pattern);
                             filing.removeHits(ds, Integer.toString(i), pattern);
                             //ds.delete(ds.createQuery(Filing.class).field("filingId").equal("?id=101197").get());
                             Pattern p = Pattern.compile(pattern);
                             Matcher matcher = p.matcher(text);
                             int count = 0;
                             while (matcher.find()) {
                                 count++;
                                 SearchHit hit = new SearchHit();
                                 hit.setFiling(filing);
                                 hit.setGenericPattern(Integer.toString(i));
                                 hit.setActualPattern(pattern);
                                 newHits.add(hit);
                             }
                             System.out.println("Found " + count + " hits for " + pattern);
                             ds.save(newHits);
                             //filing.recordHits(newHits);
                         }
                     }
                     pdf.close();
                 }
                 catch (IOException ex) {
                     Logger.getLogger(FCCClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             else if (choice.equals("q")) {
                 System.exit(0);
             }
             else {
                 System.out.println("Could not process the command.");
             }
         }
         System.out.println(choice);
     }
 }
