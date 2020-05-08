 package blogger;
 
 import com.google.gdata.client.blogger.BloggerService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.IEntry;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 
 // TODO - Separate the Row class, and parseText into it's own class, enabling other uses of parsing the data file
 
 public class FromText {
 
     
     public class Thumbnail {
         public Thumbnail() {
             this.url    = null;
             this.width  = -1;
             this.height = -1;
         }
         
         public Thumbnail(String val) {
             String splits[] = val.split(" ");
             for (int i = 0; i < splits.length; i++) {
                 if (splits[i].length() > 0 && splits[i].startsWith("url=")) {
                     this.url = splits[i].substring(4);
                 }
                 if (splits[i].length() > 0 && splits[i].startsWith("width=")) {
                     this.width = Integer.parseInt(splits[i].substring(6));
                 }
                 if (splits[i].length() > 0 && splits[i].startsWith("height=")) {
                     this.height = Integer.parseInt(splits[i].substring(7));
                 }
             }
         }
         
         String url;
         int    width;
         int    height;
 
         @Override
         public String toString() {
             return " url="+ url +" width="+ Integer.toString(width) +" height="+ Integer.toString(height) +" ";
         }
     }
         
     public class Row {
         
         public Row() {
             this.categories       = new LinkedList<String>();
             this.title            = null;
             this.date             = null;
             this.urls             = new LinkedList<String>();
             this.links            = new LinkedList<String>();
             this.uri              = null;
             this.id               = null;
             this.descriptions     = new LinkedList<String>();
             this.images           = new LinkedList<String>();
             this.youtubeUrl       = null;
             this.vimeoUrl         = null;
             this.bliptvUrl        = null;
             this.enclosureUrl     = null;
             this.enclusureMIME    = null;
             this.mediaCredit      = null;
             this.mediaDescription = null;
             this.mediaTitle       = null;
             this.thumbs           = new LinkedList<Thumbnail>();
         }
         LinkedList<String> categories;
         String title;
         LinkedList<String> urls;
         LinkedList<String> links;
         String uri;
         String id;
         String date;
         LinkedList<String> descriptions;
         LinkedList<String> images;
         String youtubeUrl;
         String vimeoUrl;
         String bliptvUrl;
         String enclosureUrl;
         String enclusureMIME;
         String mediaCredit;
         String mediaDescription;
         String mediaTitle;
         LinkedList<Thumbnail> thumbs;
         
         public boolean isEmpty() {
             if (title == null || title.length() <= 0
              /*|| url   == null || url.length() <= 0 */
              /*|| description == null || description.length() <= 0*/) {
                 return true;
             } else {
                 return false;
             }
         }
         
         public void addUniqueThumbnail(Thumbnail thumb) {
             boolean inThumbs = false;
             for (Thumbnail t : thumbs) {
                 if (t != null && t.url.equals(thumb.url))
                     inThumbs = true;
             }
             if (! inThumbs) thumbs.add(thumb);
         }
         
         public void addUniqueImage(String imgurl) {
             if (! images.contains(imgurl))
                 images.add(imgurl);
         }
         
         public void addUniqueLink(String href) {
             if (! links.contains(href))
                 links.add(href);
         }
         
         public void addUniqueCategory(String name) {
             if (! categories.contains(name))
                 categories.add(name);
         }
         
         public void addDescription(String desc) {
             descriptions.add(desc);
         }
         
         public void addUrl(String url) {
             urls.add(url);
         }
         
         private static final String tmpl =
             "title: @title@\n"
            +"uri: @uri@\n"
            +"id: @id@\n"
            +"date: @date@\n"
            +"youtubeUrl: @youtubeUrl@\n"
            +"vimeoUrl: @vimeoUrl@\n"
            +"bliptvUrl: @bliptvUrl@\n"
            +"enclosureUrl: @enclosureUrl@\n"
            +"enclusureMIME: @enclusureMIME@\n"
            +"mediaCredit: @mediaCredit@\n"
            +"mediaDescription: @mediaDescription@\n"
            +"mediaTitle: @mediaTitle@\n"
            +"@urls@@links@@descriptions@@images@@thumbs@@tags@";
         private static final String tagTmpl    = "tag: @tag@\n";
         private static final String urlTmpl    = "url: @url@\n";
         private static final String linkTmpl   = "link: @link@\n";
         private static final String imgTmpl    = "image: @imageurl@\n";
         private static final String descTmpl   = "description: @description@\n";
         private static final String thumbsTmpl = "mediaThumbnail: @mediaThumbnail@\n";
         
         @Override
         public String toString() {
             String tags = "";
             for (String tag : categories) {
                 tags += tagTmpl.replaceAll("@tag@", Matcher.quoteReplacement(tag));
             }
             String Slinks = "";
             for (String link : links) {
                 Slinks += linkTmpl.replaceAll("@link@", Matcher.quoteReplacement(link));
             }
             String Simages = "";
             for (String image : images) {
                 Simages += imgTmpl.replaceAll("@imageurl@", Matcher.quoteReplacement(image));
             }
             String thumbnails = "";
             for (Thumbnail thumb : thumbs) {
                 thumbnails += thumbsTmpl.replaceAll("@mediaThumbnail@", Matcher.quoteReplacement(thumb.toString()));
             }
             String Surls = "";
             for (String Surl : urls) {
                 Surls += urlTmpl.replaceAll("@url@", Surl);
             }
             String Sdescs = "";
             for (String Sdesc : descriptions) {
                 Sdescs += descTmpl.replaceAll("@description@", Sdesc);
             }
             return tmpl
                     .replaceAll("@title@",         (title != null) ? Matcher.quoteReplacement(title) : "")
                     .replaceAll("@urls@",          Matcher.quoteReplacement(Surls))
                     .replaceAll("@links@",         Matcher.quoteReplacement(Slinks))
                     .replaceAll("@uri@",           (uri != null) ? Matcher.quoteReplacement(uri) : "")
                     .replaceAll("@id@",            (id != null) ? Matcher.quoteReplacement(id) : "")
                     .replaceAll("@date@",          (date != null) ? Matcher.quoteReplacement(date) : "")
                     .replaceAll("@images@",        Matcher.quoteReplacement(Simages))
                     .replaceAll("@descriptions@",  Matcher.quoteReplacement(Sdescs))
                     .replaceAll("@youtubeUrl@",    (youtubeUrl != null) ? Matcher.quoteReplacement(youtubeUrl) : "")
                     .replaceAll("@vimeoUrl@",      (vimeoUrl != null) ? Matcher.quoteReplacement(vimeoUrl) : "")
                     .replaceAll("@bliptvUrl@",     (bliptvUrl != null) ? Matcher.quoteReplacement(bliptvUrl) : "")
                     .replaceAll("@enclosureUrl@",  (enclosureUrl != null) ? Matcher.quoteReplacement(enclosureUrl) : "")
                     .replaceAll("@enclusureMIME@", (enclusureMIME != null) ? Matcher.quoteReplacement(enclusureMIME) : "")
                     .replaceAll("@mediaCredit@",   (mediaCredit != null) ? Matcher.quoteReplacement(mediaCredit) : "")
                     .replaceAll("@mediaDescription@", (mediaDescription != null) ? Matcher.quoteReplacement(mediaDescription) : "")
                     .replaceAll("@mediaTitle@",    (mediaTitle != null) ? Matcher.quoteReplacement(mediaTitle) : "")
                     .replaceAll("@thumbs@", Matcher.quoteReplacement(thumbnails))
                     .replaceAll("@tags@",   Matcher.quoteReplacement(tags));
         }
     }
     
     LinkedList<Row> rows   = null;
     LinkedList<Row> posted = null;
     
     public FromText() {
         rows   = new LinkedList<Row>();
         posted = new LinkedList<Row>();
     }
     
     private void parseText(File inputFile)
         throws java.io.FileNotFoundException, java.io.IOException
     {
         FileReader fr = new FileReader(inputFile);
         BufferedReader br = new BufferedReader(fr);
         String line;
         Row thisRow;
         for (thisRow = new Row(); (line = br.readLine()) != null; /*thisRow = new Row()*/) {
             line = line.trim();
             if (line.length() <= 0) {
                 // - process previously gathered data
                 if (! thisRow.isEmpty()) {
                     rows.addLast(thisRow);
 //                    System.out.println("ADDED " + thisRow.toString());
                     thisRow = new Row();
                 }
                 continue;
             }
 //            System.out.println(line);
             int colon = line.indexOf(":");
             if (colon > 0) {
                 String name = line.substring(0, colon);
                 String val  = line.substring(colon+1).trim();
 //                if (name.equals("authorName"))     { authorName             = val; }
 //                if (name.equals("userName"))       { userName               = val; }
 //                if (name.equals("userPasswd"))     { userPasswd             = val; }
 //                if (name.equals("blogId"))         { blogId                 = val; }
                 if (name.equals("tag"))              { thisRow.addUniqueCategory(val); }
                 if (name.equals("title"))            { thisRow.title            = val; }
                 if (name.equals("url"))              { thisRow.addUrl(val);            }
                 if (name.equals("link"))             { thisRow.addUniqueLink(val);     }
                 if (name.equals("uri"))              { thisRow.uri              = val; }
                 if (name.equals("rssguid"))          { thisRow.id               = val; }
                 if (name.equals("atomid"))           { thisRow.id               = val; }
                 if (name.equals("date"))             { thisRow.date             = val; }
                 if (name.equals("description"))      { thisRow.addDescription(val);    }
                 if (name.equals("image"))            { thisRow.addUniqueImage(val);    }
                 if (name.equals("youtubeUrl"))       { thisRow.youtubeUrl       = val; }
                 if (name.equals("enclosureUrl"))     { thisRow.enclosureUrl     = val; }
                 if (name.equals("enclusureMIME"))    { thisRow.enclusureMIME    = val; }
                 if (name.equals("mediaCredit"))      { thisRow.mediaCredit      = val; }
                 if (name.equals("mediaDescription")) { thisRow.mediaDescription = val; }
                 if (name.equals("mediaTitle"))       { thisRow.mediaTitle       = val; }
                 if (name.equals("mediaThumbnail")) {
                     Thumbnail t = new Thumbnail(val);
                     thisRow.addUniqueThumbnail(t);
                 }
             }
         }
         fr.close();
     }
     
 //    String removeCR(String s) {
 //        while (s.endsWith("\r")) {
 //            String n = s.substring(0, s.indexOf("\r")); // + s.substring(s.indexOf("\r") + 1);
 //            s = n;
 //        }
 //        return s;
 //    }
     
     private void postIndividually(Blog blog)
         throws com.google.gdata.util.ServiceException, java.io.IOException,
             java.net.MalformedURLException, ParseException
     {
         long fakeTime = new Date().getTime() - (1000 * 60 * 120); // start fake time 120 minutes ago
         for (Row row = rows.peekLast(); row != null; row = rows.peekLast()) {
             DateTime publ;
             
 //            System.out.println(row.toString());
             if (row.date != null && row.date.length() > 0) {
                 String[] dates = row.date.split(" ");
                 Date date = new Date(new Long(dates[0]));
                 publ = new DateTime(date);
                 publ.setTzShift(dates.length >= 2 ? new Integer(dates[1]) : 0);
             } else {
                 publ = new DateTime(fakeTime);
                 publ.setTzShift(0);
                 fakeTime += 120 * 1000;  // Fake an advance of time because blogger lops off seconds
             }
             
             String urls = "";
             for (String url : row.urls) {
                 urls += linkTemplate.replaceAll("@url@", Matcher.quoteReplacement(url));
             }
             
             String Sdesc = "";
             for (String desc : row.descriptions) {
                 Sdesc += descriptTemplate.replaceAll("@description@", Matcher.quoteReplacement(desc));
             }
             if (Sdesc == null)
                 Sdesc = (row.mediaDescription != null && row.mediaDescription.length() > 0)
                     ? row.mediaDescription : null;
             
             String MC = 
                     (row.mediaCredit != null && row.mediaCredit.length() > 0)
                     ? mediaCreditTemplate.replaceAll("@mediaCredit@", Matcher.quoteReplacement(row.mediaCredit))
                     : null;
             
             String images = "";
             for (String imgsrc : row.images) {
                 images += imageTemplate.replaceAll("@imageurl@", Matcher.quoteReplacement(imgsrc));
             }
             
             String thumbs = "";
             for (Thumbnail thumb : row.thumbs) {
                 thumbs += thumbTemplate.replaceAll("@width@", Integer.toString(thumb.width))
                         .replaceAll("@height@", Integer.toString(thumb.height))
                         .replaceAll("@imageurl@", Matcher.quoteReplacement(thumb.url));
             }
             
             String post = postBodyTemplate
                     .replaceAll("@descriptions@", Matcher.quoteReplacement(Sdesc))
                     .replaceAll("@images@",  Matcher.quoteReplacement(images))
                     .replaceAll("@youtube@", 
                         (row.youtubeUrl != null && row.youtubeUrl.length() > 0)
                         ? generateYoutubeIframe(row.youtubeUrl)
                         : ""
                     )
                     // TODO vimeoUrl
                     // TODO bliptvUrl
                     .replaceAll("@urls@", Matcher.quoteReplacement(urls))
                     .replaceAll("@thumbnails@", Matcher.quoteReplacement(thumbs))
                     .replaceAll("@mediaCredit@", MC != null ? Matcher.quoteReplacement(MC) : "")
                     .replaceAll("@enclosures@", 
                         (row.enclosureUrl != null && row.enclosureUrl.length() > 0)
                         ? generateEnclosurePlayer(row.enclosureUrl, row.enclusureMIME)
                         : ""
                     );
             IEntry Ipost = blog.createPost(row.title,
                     post,
                     row.categories,
                     false, publ, publ);
             
             rows.remove(row);
             posted.addLast(row);
         }
     }
     
     // Row data fields not appearing in post templates:
     // 
     //    links, mediaTitle
     
     static final String postBodyTemplate =
             "@descriptions@\n"
            +"@images@\n"
            +"@youtube@\n"
            +"@enclosures@\n"
            +"@urls@\n"
            +"@thumbnails@\n"
            +"@mediaCredit@\n";
     
     static final String postTemplate =
            "<h3>@title@</b></h3>\n"
            +"@descriptions@\n"
            +"@images@\n"
            +"@youtube@\n"
            +"@enclosures@\n"
            +"@urls@\n"
            +"@mediaCredit@\n"
            +"<br/><br/>\n";
     
 //    static final String postTemplate2 =
 //            "<p><b>@title@</b></p>\n"
 //           +"<p>@description@</p>\n"
 //           +"@images@\n"
 //           +"@youtube@\n"
 //           +"@enclosures@\n"
 //           +"<p><a href=\"@url@\">@url@</a></p>\n"
 //           +"@mediaCredit@\n"
 //           +"<br/><br/>\n";
     
     static final String descriptTemplate = "<p>@description@</p>\n";
     static final String linkTemplate  = "<p><a href=\"@url@\">@url@</a></p>\n";
     static final String smLinkTemplate  = "<p><span style='font-size: x-small;'><a href=\"@url@\">@url@</a></span></p>\n";
     static final String thumbTemplate = "<p><img width='@width' height='@height' src=\"@imageurl@\"/></p>";
     static final String mediaCreditTemplate = "<p>Credit: @mediaCredit@</p>\n";
     
     static final String enclosureAudioTemplate = ""; // TODO audio enclosure template
     static final String enclosureVideoTemplate = ""; // TODO video enclosure template
     
     static final String imageTemplate = "<p><img style='max-width: 100%' src='@imageurl@'/></p>";
     
     static final String youtubeTemplate = "<iframe width='450' height='253' src='http://www.youtube.com/embed/@code@' frameborder='0' allowfullscreen></iframe>";
     
     private void postSummary(String postedFile)
         throws java.net.MalformedURLException, FileNotFoundException
     {
         PrintStream out = new PrintStream(postedFile);
         // for (Row row : posted) {
         for (Row row = posted.peekLast(); row != null; row = posted.peekLast()) {
             posted.remove(row);
             String images = "";
             for (String imgsrc : row.images) {
                 images += imageTemplate.replaceAll("@imageurl@", Matcher.quoteReplacement(imgsrc));
             }
             
             String description = (row.descriptions != null && row.descriptions.size() > 0)
                     ? row.descriptions.get(0) : null;
             if (description == null)
                 description = (row.mediaDescription != null && row.mediaDescription.length() > 0)
                     ? row.mediaDescription : null;
             
             String MC = 
                     (row.mediaCredit != null && row.mediaCredit.length() > 0)
                     ? mediaCreditTemplate.replaceAll("@mediaCredit@", Matcher.quoteReplacement(row.mediaCredit))
                     : null;
             
             String urls = "";
             for (String url : row.urls) {
                 urls += smLinkTemplate.replaceAll("@url@", Matcher.quoteReplacement(url));
             }
             
             out.println("");
             out.println(
                     postTemplate.replaceAll("@title@", Matcher.quoteReplacement(row.title))
                         .replaceAll("@descriptions@",  
                             (description != null && description.length() > 0)
                             ? Matcher.quoteReplacement(description) : "")
                         .replaceAll("@urls@", Matcher.quoteReplacement(urls))
                         .replaceAll("@mediaCredit@", MC != null ? Matcher.quoteReplacement(MC) : "")
                         .replaceAll("@images@", Matcher.quoteReplacement(images))
                         .replaceAll("@youtube@", 
                             (row.youtubeUrl != null && row.youtubeUrl.length() > 0)
                             ? Matcher.quoteReplacement(generateYoutubeIframe(row.youtubeUrl))
                             : "")
                         .replaceAll("@enclosures@", 
                             (row.enclosureUrl != null && row.enclosureUrl.length() > 0)
                             ? Matcher.quoteReplacement(generateEnclosurePlayer(row.enclosureUrl, row.enclusureMIME))
                             : ""
                         )
                     );
             out.println("");
         }
         out.close();
     }
     
     private void notPostedSummary(String notPostedFile) throws FileNotFoundException {
         PrintStream out = new PrintStream(notPostedFile);
         for (Row row : rows) {
         // for (Row row = rows.peekLast(); row != null; row = rows.peekLast()) {
             rows.remove(row);
             out.println("");
             out.println(row.toString());
             out.println("");
         }
         out.close();
     }
     
     // http://www.youtube.com/watch?v=c1wCszOpeYk&feature=g-vrec
     // <iframe width="450" height="253" src="http://www.youtube.com/embed/c1wCszOpeYk" frameborder="0" allowfullscreen></iframe>
     String generateYoutubeIframe(String youtubeUrl)
         throws java.net.MalformedURLException
     {
 //        System.out.println("generateYoutubeIframe " + youtubeUrl);
         URL url = new URL(youtubeUrl);
         String qry = url.getQuery();
         if (qry == null || qry.length() <= 0) {
             return "";
         }
         String[] splits = qry.split("&");
         for (int i = 0; i < splits.length; i++) {
             if (splits[i].startsWith("v=")) {
                 String code = splits[i].substring(2);
 //                System.out.println("\t youtube code=" + code);
                 return Matcher.quoteReplacement(
                         youtubeTemplate.replaceAll("@code@", Matcher.quoteReplacement(code))
                         );
             }
         }
         return "";
     }
     
     String generateEnclosurePlayer(String enclUrl, String enclType) {
         
         // TODO implement generateEnclosurePlayer
         return Matcher.quoteReplacement("");
     }
     
     String authorName = null; // "David Herron";
     String userName   = null; // "reikiman@gmail.com";
     String userPasswd = null; // "ellhnpezyfoxbqcn";
     
     String blogId = null; // "4358407941295111084"; // "3621272226038322596";
     String inputFile = null; // "input.txt";
     
     String postedFile = null;
     String notPostedFile = null;
     
     void run(String[] args) 
             throws ParserConfigurationException, SAXException, IOException,
             AuthenticationException, ServiceException, java.io.FileNotFoundException,
             java.net.MalformedURLException, ParseException, Exception {
         
         authorName = args[1];
         userName   = args[2];
         userPasswd = args[3];
         blogId     = args[4];
         inputFile  = args[5];
         
         if (args.length >= 7) postedFile    = args[6];
         if (args.length >= 8) notPostedFile = args[7];
         
         System.out.println("author name: " + authorName);
         System.out.println("user name: " + userName);
         System.out.println("user password: " + userPasswd);
         System.out.println("blog ID: " + blogId);
         System.out.println("input file: " + inputFile);
         System.out.println("posted file: " + postedFile);
         System.out.println("not posted file: " + notPostedFile);
         
         BloggerService service = new BloggerService("exampleCo-exampleApp-1");
         boolean notAuthenticated = true;
         for (int i = 0; i < 5 && notAuthenticated; i++) {
             try {
                 service.setUserCredentials(userName, userPasswd);
                 notAuthenticated = false; // we only get here if there's no exception
             } catch (com.google.gdata.util.AuthenticationException ae) {
                 System.out.println("CAUGHT EXCEPTION AuthenticationException "+ ae.getMessage());
                 System.err.println("Response body " + ae.getResponseBody());
                 System.err.println("Code name " + ae.getCodeName());
                 System.err.println("Debug info " + ae.getDebugInfo());
                 System.err.println("Domain name " + ae.getDomainName());
                 System.err.println("Extended help " + ae.getExtendedHelp());
                 System.err.println("Internal reason " + ae.getInternalReason());
                 System.err.println("Location " + ae.getLocation());
                 System.err.println("string: " + ae.toString());
                 try {
                     // The exception may be due to blogger being overloaded
                     // sleep to give time for blogger to catch up .. maybe ..?
                     Thread.sleep(10 * 1000);
                 } catch (InterruptedException ex) {
                     // ignore
                 }
             }
         }
         if (notAuthenticated) {
             throw new Exception("Blog service did not authenticate.");
         }
         Blog blog = new Blog(service, blogId, authorName, userName);
         parseText(new File(inputFile)); 
         try { 
             postIndividually(blog); 
         } catch (Exception e) {
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
         
         if (postedFile != null) postSummary(postedFile);
         if (notPostedFile != null) notPostedSummary(notPostedFile);
     }
     
     public void countItems(String fn) throws FileNotFoundException, IOException {
         parseText(new File(fn));
         System.out.println("input file: " + fn); 
         System.out.println("number of items: " + Integer.toString(rows.size()));
     }
     
     public void generateSummary(String fnIn, String fnOut) 
         throws FileNotFoundException, IOException 
     {
         parseText(new File(fnIn));
         posted = rows;
         postSummary(fnOut);
     }
     
     public static void main(String[] args) 
             throws ParserConfigurationException, SAXException, IOException,
             AuthenticationException, ServiceException, 
             java.io.FileNotFoundException, MalformedURLException, ParseException, Exception {
         new FromText().run(args);
     }
 }
 
