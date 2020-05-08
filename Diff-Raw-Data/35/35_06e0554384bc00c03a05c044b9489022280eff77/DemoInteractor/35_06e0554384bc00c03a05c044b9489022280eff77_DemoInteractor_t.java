 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 import java.lang.management.*;
 import java.util.*;
 
 import org.apache.commons.fileupload.*;
 import org.apache.log4j.*;
 
 /**
  * This Interactor displays a collection of pages that illustrate the
  * facilities of Fiz.
  */
 
 public class DemoInteractor extends Interactor {
     // The following variable is used for log4j-based logging.
     protected Logger logger = Logger.getLogger("org.fiz.DemoInteractor");
 
     // The following beans allow us to collect execution time information
     // from all of the various garbage collectors.
     List<GarbageCollectorMXBean> beans =
         ManagementFactory.getGarbageCollectorMXBeans();
 
     // Total time spent in garbage collection since the last time
     // performance statistics were reset.
     long baseGcMs = 0;
 
     // Total number of garbage collector indications since the last time
     // performance statistics were reset.
     int baseGcInvocations = 0;
 
     // Lazily instantiated: Data manager for connecting to SQL database
     // to authenticate users
     SqlDataManager sqlDataManager = null;
 
     // Use this data manager for reading datasets from demo.yaml in the
     // demo directory.
     FileDataManager fileDataManager = new FileDataManager(
             Config.get("main", "home") + "/WEB-INF/demo");
 
     // Datasets that supply results for raw data requests.
     Dataset formData = new Dataset("name", "Bill", "age", "41",
             "height", "73", "weight", "195",
            "saying", "Line 1\nLine 2\n<Line 3>\n", "fruit",
            new Dataset("value", "grape"), "notify", "all", "state", "California",
            "mascot", "Spartans");
 
     Dataset fruitInfo = new Dataset(
             "fruit", new Dataset("name", "Apple", "value", "Apple"),
             "fruit", new Dataset("name", "Banana", "value", "banana"),
             "fruit", new Dataset("name", "Grape", "value", "grape"),
             "fruit", new Dataset("name", "Kiwi", "value", "kiwi"),
             "fruit", new Dataset("name", "Peach", "value", "peach"),
             "fruit", new Dataset("name", "Pear", "value", "pear"),
             "fruit", new Dataset("name", "Raspberry", "value", "raspberry"),
             "fruit", new Dataset("name", "Strawberry", "value", "strawberry"),
             "fruit", new Dataset("name", "Watermelon", "value", "watermelon"));
 
     private static Dataset autocompleteData = new Dataset(
             "name", new Dataset("name", "George Washington", "value", "1"),
             "name", new Dataset("name", "John Adams", "value", "2"),
             "name", new Dataset("name", "Thoms Jefferson", "value", "3"),
             "name", new Dataset("name", "James Madison", "value", "4"),
             "name", new Dataset("name", "James Monroe", "value", "5"),
             "name", new Dataset("name", "John Quincy Adams", "value", "6"),
             "name", new Dataset("name", "Andrew Jackson", "value", "7"),
             "name", new Dataset("name", "Martin Van Buren", "value", "8"),
             "name", new Dataset("name", "William Henry Harrison", "value", "9"),
             "name", new Dataset("name", "John Tyler", "value", "10"),
             "name", new Dataset("name", "James Knox Polk", "value", "11"),
             "name", new Dataset("name", "Zachary Taylor", "value", "12"),
             "name", new Dataset("name", "Millard Fillmore", "value", "13"),
             "name", new Dataset("name", "Franklin Pierce", "value", "14"),
             "name", new Dataset("name", "James Buchanan", "value", "15"),
             "name", new Dataset("name", "Abraham Lincoln", "value", "16"),
             "name", new Dataset("name", "Andrew Johnson", "value", "17"),
             "name", new Dataset("name", "Ulysses S. Grant", "value", "18"),
             "name", new Dataset("name", "Rutherford B. Hayes", "value", "19"),
             "name", new Dataset("name", "James Garfield", "value", "20"),
             "name", new Dataset("name", "Chester Arthur", "value", "21"),
             "name", new Dataset("name", "Grover Cleveland", "value", "22"),
             "name", new Dataset("name", "Benjamin Harrison", "value", "23"),
             "name", new Dataset("name", "Grover Cleveland", "value", "24"),
             "name", new Dataset("name", "William McKinley", "value", "25"),
             "name", new Dataset("name", "Theodore Roosevelt", "value", "26"),
             "name", new Dataset("name", "William Howard Taft", "value", "27"),
             "name", new Dataset("name", "Woodrow Wilson", "value", "28"),
             "name", new Dataset("name", "Warren Harding", "value", "29"),
             "name", new Dataset("name", "Calvin Coolidge", "value", "30"),
             "name", new Dataset("name", "Herbert Hoover", "value", "31"),
             "name", new Dataset("name", "Franklin D. Roosevelt", "value", "32"),
             "name", new Dataset("name", "Harry S. Truman", "value", "33"),
             "name", new Dataset("name", "Dwight D. Eisenhower", "value", "34"),
             "name", new Dataset("name", "John F. Kennedy", "value", "35"),
             "name", new Dataset("name", "Lyndon Johnson", "value", "36"),
             "name", new Dataset("name", "Richard Nixon", "value", "37"),
             "name", new Dataset("name", "Gerald Ford", "value", "38"),
             "name", new Dataset("name", "James Carter", "value", "39"),
             "name", new Dataset("name", "Ronald Reagan", "value", "40"),
             "name", new Dataset("name", "George H.W. Bush", "value", "41"),
             "name", new Dataset("name", "William J. Clinton", "value", "42"),
             "name", new Dataset("name", "George W. Bush", "value", "43"),
             "name", new Dataset("name", "Barack H. Obama", "value", "44"));
 
     FormSection sideBySideForm = new FormSection(
             new Dataset("id", "form1", "request", "getFormData",
                     "postUrl", "postSideBySide"),
             new EntryFormElement(new Dataset("id", "name",
 
                                              "label", "Name:", "help", "Enter customer name here")),
             new EntryFormElement("age", "Age:"),
             new EntryFormElement("state", "Home state:"),
             new DateFormElement(new Dataset("id", "expiration",
                     "label", "Expiration date:",
                     "attachPosition", "bottom",
                     "dateFormat", "m-d-Y",
                     "exclude", "Saturday, 1/12/2005, 7/23," +
                     "8/13/2010:9/11/2010, " +
                     ":6/24/2009, 9/3/2011:")),
             new PasswordFormElement("password", "Password:"),
             new PasswordFormElement(new Dataset("id", "password2",
                     "label", "Retype password:", "duplicate", "password")),
             new CheckboxFormElement(new Dataset("id", "citizen",
                     "label", "U.S. citizen:", "extra",
                     "(you can click here too)")),
             new RadioFormElement(new Dataset("id", "notify",
                     "label", "Email opt-out:", "value", "all", "extra",
                     "Wants to receive all communications")),
             new RadioFormElement(new Dataset("id", "notify",
                     "value", "orders", "extra",
                     "Send only order status messages")),
             new RadioFormElement(new Dataset("id", "notify",
                     "value", "none", "extra",
                     "Send no messages of any form")),
             new TemplateFormElement(new Dataset("id", "heading",
                     "span", "true", "template",
                     "<div class=\"heading\">Optional Information</div>")),
             new SelectFormElement(new Dataset("id", "fruit",
                     "label", "Favorite fruits:",
                     "choiceRequest", "getFruits",
                     "multiple", "multiple",
                     "height", "5",
                     "choiceName", "fruit")),
                     new TextAreaFormElement("saying", "Favorite saying:"),
                     new UploadFormElement("upload", "File to upload:"),
                     new HiddenFormElement("mascot")
     );
 
     public static DataRequest autocompleteRequest(String query) {
         query = query.toLowerCase();
 
         Dataset matches = new Dataset();
         for(Dataset data : autocompleteData.getDatasetList("name")) {
             String dataName = data.getString("name").toLowerCase();
             if(dataName.substring(0, Math.min(query.length(), dataName.length())).equals(query)) {
                 matches.add("data", data);
             }
         }
 
         return RawDataManager.newRequest(matches);
     }
 
     FormSection verticalForm = new FormSection(
             new Dataset("id", "form2", "request", "getFormData",
                     "layout", "vertical", "postUrl", "postVertical"),
                     new EntryFormElement(new Dataset("id", "name",
                             "label", "Name:", "help", "Enter employee name here")),
                     new EntryFormElement(new Dataset("id", "age",
                             "label", "Age:")),
                     new EntryFormElement(new Dataset("id", "state",
                             "label", "Home state:")),
                     new CheckboxFormElement(new Dataset("id", "citizen",
                             "extra", "U.S. citizen")),
                     new SelectFormElement(new Dataset("id", "fruit",
                             "label", "Favorite fruit:",
                             "choiceRequest", "getFruits",
                             "choiceName", "fruit")),
                     new TextAreaFormElement(new Dataset("id", "saying",
                             "label", "Favorite saying:", "rows", "3"))
     );
 
     /**
      * Displays a page illustrating usage of the CompoundSection class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void compound(ClientRequest cr) {
         Html html = cr.getHtml();
         html.setTitle("CompoundSection Demo");
         CompoundSection section = new CompoundSection (
                 new Dataset("borderFamily", "/static/fiz/images/borderBlueFilled.gif",
                         "background", "#f1f5fb"),
                         new TemplateSection ("<p>Here is some sample text " +
                         "to fill the body of this compound section.</p>\n"),
                         new TemplateSection ("<p>This is a second paragraph, " +
                         "which follows the first.</p>\n")
         );
         CompoundSection section2 = new CompoundSection (
                 new Dataset("borderFamily", "/static/fiz/images/borderGrayFilled.gif",
                         "background", "#f5f5f5"),
                         new TemplateSection ("<p>This text provides the body " +
                                 "of the second compound section.  I will type " +
                                 "and type and type some more, in order to " +
                                 "provide lots of text for this section, " +
                         "so that it overflows onto several lines.</p>\n")
         );
         cr.showSections(
                 new TemplateSection("<h1>Compound Section</h1>\n"),
                 section,
                 new TemplateSection("<br>"),
                 section2);
     }
 
     /**
      * Displays a page illustrating the FormSection class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void formSection(ClientRequest cr) {
         Html html = cr.getHtml();
         html.setTitle("FormSection Demo");
         html.includeCssFile("demo/form.css");
         cr.setPageProperty("formInfo", "Zip code: 94301");
         cr.addDataRequest("getFormData", RawDataManager.newRequest(formData));
         cr.addDataRequest("getFruits", RawDataManager.newRequest(fruitInfo));
 
         cr.showSections(
                 new TemplateSection("<h1>Side-By-Side form</h1>\n"),
                 sideBySideForm,
                 new TemplateSection("<h1>Vertical Form</h1>\n"),
                 verticalForm);
     }
 
     /**
      * Returns an empty page; used as a control case for benchmarking.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void noop(ClientRequest cr) {
         // Don't do anything.
     }
 
     /**
      * Displays a page illustrating the TabSection class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void tabSection(ClientRequest cr) {
         Html html = cr.getHtml();
         html.setTitle("TabSection Demo");
         cr.showSections(
                 new TemplateSection("<h1>Sample TabSection</h1>\n"),
                 new TabSection(new Dataset(),
                         new Dataset("id", "t1", "text", "Inventory",
                                 "url", "tabSection?currentTabId=t1"),
                         new Dataset("id", "t2", "text", "Orders",
                                 "url", "tabSection?currentTabId=t2"),
                         new Dataset("id", "t3", "text", "Accounts",
                                 "ajaxUrl", "ajaxTab?tab=t3"),
                         new Dataset("id", "t4", "text", "Shippers",
                                 "javascript",
                                 "document.getElementById(\"text\").innerHTML " +
                                 "= 'You clicked on tab \"t4\", which caused " +
                                 "this text to be updated by Javascript.'"),
                         new Dataset("id", "t5", "text", "Suppliers",
                                 "url", "tabSection?currentTabId=t5")),
                 new TemplateSection("<p id=\"text\">The currently selected " +
                     "tab has id \"@currentTabId?{??}\"</p>\n"));
     }
 
     /**
      * Invoked by some of the tabs in {@code tabSection}.  Modifies
      * the text displayed underneath the tabs.
      * @param cr
      */
     public void ajaxTab(ClientRequest cr) {
         cr.updateElement("text", "You clicked on tab \"" +
                 cr.getMainDataset().getString("tab") +
                 "\", which caused this text to be updated via AJAX.");
     }
 
     /**
      * Displays a page with various demonstrations of the ChartSection
      * class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void chartSection(ClientRequest cr) {
         Html html = cr.getHtml();
         html.setTitle("ChartSection Demos");
         cr.addDataRequest("someData", fileDataManager.newReadRequest("demo.yaml",
                 "someData"));
         cr.addDataRequest("someData2", fileDataManager.newReadRequest("demo.yaml",
                 "someData2"));
         cr.addDataRequest("fizUsers", fileDataManager.newReadRequest("demo.yaml",
                 "fizUsers"));
         cr.addDataRequest("widgetSales", fileDataManager.newReadRequest("demo.yaml",
                 "widgetSales"));
         cr.addDataRequest("gadgetSales", fileDataManager.newReadRequest("demo.yaml",
                 "gadgetSales"));
 
         Dataset chart1 =
             new Dataset("request", "fizUsers", "xId", "month", "yId", "users",
                         "xLocation", "right", "yLocation", "bottom",
                         "legend", new Dataset("display", "false"),
                         "rightAxis", new Dataset("title", "Month", "majorGridWidth", "1"),
                         "bottomAxis", new Dataset("title", "Number of Fiz Users",
                                              "scale", "log", "logBase", "10"),
                         "title", "Fiz Growth" );
 
         Dataset chart2 =
             new Dataset("titleColor", "blue", "borderWidth", "0",
                         "type", "Line", "title", "Products sold at ACME",
                         "width", "700",
                         "xAxis", new Dataset("title", "Year",
                                              "majorGridWidth", "0"),
                         "yAxis", new Dataset("majorGridWidth", "1",
                                              "minorTicks", "5"),
                         "series", new Dataset("request", "widgetSales", "xId", "year", "color", "green",
                                               "yId", "sales", "name", "Widgets"),
                         "series", new Dataset("request", "gadgetSales", "xId", "year", "color", "purple",
                                               "yId", "sales", "name", "Gadgets"));
 
         Dataset chart3 =
             new Dataset("xAxis", new Dataset("title", "Doodads",
                                              "majorGridWidth", "1"),
                         "yAxis", new Dataset("title", "Gizmos",
                                              "majorGridWidth", "1"),
                         "plot", new Dataset("request", "someData", "name", "Substance X",
                                             "xId", "xVal", "yId", "yVal", "type", "Scatter",
                                             "opacity", "1"),
                         "plot", new Dataset("request", "someData2", "name", "Substance Y",
                                             "xId", "xVal", "yId", "yVal", "type", "Scatter",
                                             "opacity", "1", "shape", "square"));
 
         cr.showSections(new ChartSection(chart1),
                         new ChartSection(chart2),
                         new ChartSection(chart3));
     }
 
     /**
      * Displays a page with various demonstrations of the TableSection
      * class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void tableSection(ClientRequest cr) {
         Html html = cr.getHtml();
         html.setTitle("TableSection Demos");
         html.includeCssFile("demo/shoppingCart.css");
         cr.addDataRequest("people", fileDataManager.newReadRequest("demo.yaml",
                 "people"));
         cr.addDataRequest("noData", fileDataManager.newReadRequest("demo.yaml",
                 "empty"));
         cr.addDataRequest("error", fileDataManager.newReadRequest("demo.yaml",
                 "bogus.nonexistent"));
         cr.addDataRequest("cart", fileDataManager.newReadRequest("demo.yaml",
                 "shoppingCart"));
         cr.showSections(
                 new TemplateSection("<h1>TableSection Demos</h1>\n"),
 
                 new TemplateSection("<h2>Basic table:</h2>\n"),
                 new TableSection(
                         new Dataset("request", "people"),
                         new Column("Name", "@name"),
                         new Column("Age", "{{@age}}"),
                         new Column("Height", "{{@height}}"),
                         new Column("Weight", "{{@weight}}"),
                         new Column("Social Security Number", "{{@ssn}}")),
 
                 new TemplateSection("<h2>Table with no data:</h2>\n"),
                 new TableSection(
                         new Dataset("request", "noData"),
                         new Column("Name", "@name"),
                         new Column("Age", "{{@age}}"),
                         new Column("Height", "{{@height}}"),
                         new Column("Weight", "{{@weight}}"),
                         new Column("Social Security Number", "{{@ssn}}")),
 
                 new TemplateSection("<h2>Error in data request:</h2>\n"),
                 new TableSection(
                         new Dataset("request", "error"),
                         new Column("Name", "@name"),
                         new Column("Age", "{{@age}}"),
                         new Column("Height", "{{@height}}"),
                         new Column("Weight", "{{@weight}}"),
                         new Column("Social Security Number", "{{@ssn}}")),
 
                 new TemplateSection("<h2>Simple shopping cart:</h2>\n"),
                 new TableSection(
                         new Dataset("request", "cart", "noHeader", "true",
                                 "class", "shoppingCart"),
                                 new Column("Item", "@item"),
                                 new Column("Price", "@price"))
         );
     }
 
     /**
      * Displays a page containing statistics from all the performance timers.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void stats(ClientRequest cr) {
         Html html = cr.getHtml();
         html.setTitle("Timer Statistics");
         ArrayList<Dataset> stats = Timer.getStatistics(1.0e06, "%.3f");
         Collections.sort(stats, new DatasetComparator("average",
                 DatasetComparator.Type.FLOAT,
                 DatasetComparator.Order.DECREASING));
         Dataset statsDataset = new Dataset();
         for (Dataset d : stats) {
             statsDataset.add("record", d);
         }
         cr.addDataRequest("stats", RawDataManager.newRequest(statsDataset));
         cr.showSections(
                 new TemplateSection("<h1>Timer Statistics</h1>\n"),
                 new TableSection(
                         new Dataset("request", "stats"),
                         new Column("Timer", "@name"),
                         new Column("Count", "@intervals"),
                         new Column("Avg (ms)", "@average"),
                         new Column("Min (ms)", "@minimum"),
                         new Column("Max (ms)", "@maximum"),
                         new Column("Std Dev. (ms)", "@standardDeviation"))
         );
         long gcMs = -baseGcMs;
         int gcInvocations = -baseGcInvocations;
         for (GarbageCollectorMXBean bean: beans) {
             gcMs += bean.getCollectionTime();
             gcInvocations += bean.getCollectionCount();
         }
         StringBuilder body = html.getBody();
         body.append(String.format("<p>\nGarbage collector invocations: %d" +
                 "<br>Garbage collector execution time: %dms</p>",
                 gcInvocations, gcMs));
         Link link = new Link(new Dataset("text", "Clear statistics",
                 "ajaxUrl", "/fiz/demo/ajaxClearStats"));
         body.append("<p>");
         link.render(cr, cr.getMainDataset());
     }
 
     /**
      * Displays a page demonstrating the TreeSection class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void tree(ClientRequest cr) {
         Html html = cr.getHtml();
         html.clear();
         html.setTitle("TreeSection Demo");
         Dataset main = cr.getMainDataset();
         String edgeFamily = main.checkString("edgeFamily");
         if (edgeFamily == null) {
             edgeFamily = "treeSolid.gif";
         }
         cr.showSections(
                 new TemplateSection("<h1>TreeSection Demo</h1>\n" +
                         "<div id=\"p1\"><p>Current edge style: " +
                         "@edgeStyle?{treeSolid}</p></div>\n" +
                         "<p><a href=\"tree?edgeFamily=treeSolid.gif\">Change " +
                         "edge style to treeSolid</a><br />\n" +
                         "<a href=\"tree?edgeFamily=treeDotted.gif\">Change " +
                         "edge style to treeDotted</a><br />\n" +
                         "<a href=\"tree?edgeFamily=treeNoLines.gif\">Change " +
                 "edge style to treeNoLines</a></p>\n"),
                 new TreeSection(
                         new Dataset("id", "tree1", "requestFactory",
                                 "DemoInteractor.treeRequest", "edgeFamily",
                                 edgeFamily))
         );
     }
 
     /**
      * Creates a DataRequest that returns the contents of a node in the
      * TreeSection demonstration.
      * @param name                 Name of the parent node whose children
      *                             are needed.
      * @return                     DataRequest whose response contains
      *                             information about all of the children of
      *                              by gone for that I the I{@code name}.
      */
     public static DataRequest treeRequest(String name) {
         Dataset data = YamlDataset.newStringInstance(
                 "top:\n" +
                 "    record:\n" +
                 "        - text:       Alice\n" +
                 "        - text:       Bill\n" +
                 "          name:       bill\n" +
                 "          expandable: 1\n" +
                 "        - text:       Carol\n" +
                 "          name:       carol\n" +
                 "          expandable: 1\n" +
                 "        - text:       David\n" +
                 "bill:\n" +
                 "    record:\n" +
                 "        - text:       Ellen\n" +
                 "          name:       ellen\n" +
                 "          expandable: 1\n" +
 
                 "        - text:       Frank\n" +
                 "        - text:       Grace\n" +
                 "carol:\n" +
                 "    record:\n" +
                 "        - text:       Harry\n" +
                 "        - text:       Ian\n" +
                 "        - text:       Juliet\n" +
                 "ellen:\n" +
                 "    record:\n" +
                 "        - text:       Kurt\n" +
                 "        - text:       Leslie\n" +
                 "        - text:       Michael\n" +
                 "          name:       top\n" +
                 "          expandable: 1"
 
         );
         if (name.length() == 0) {
             return RawDataManager.newRequest(data.getDataset("top"));
         }
         return RawDataManager.newRequest(data.getDataset(name));
     }
 
     public void ajaxClearStats(ClientRequest cr) {
         Timer.resetAll();
         Timer.measureNoopTime();
         long gcMs = 0;
         int gcInvocations = 0;
         for (GarbageCollectorMXBean bean: beans) {
             gcMs += bean.getCollectionTime();
             gcInvocations += bean.getCollectionCount();
         }
         baseGcMs = gcMs;
         baseGcInvocations = gcInvocations;
         cr.redirect("stats");
     }
 
     public void postSideBySide(ClientRequest cr) {
         Dataset main = cr.getMainDataset();
         logger.info("Posted dataset:\n" + main.toString());
         FileItem upload = cr.getUploadedFile("upload");
         if (upload != null) {
             if (upload.getSize() > 200) {
                 logger.info("Contents of uploaded file:\n" +
                         upload.getString().substring(0, 200) + " ...");
             } else {
                 logger.info("Contents of uploaded file:\n" +
                         upload.getString());
             }
         } else {
             logger.info("No file upload with this submission");
         }
         logger.info("Page property value: " + cr.getPageProperty("formInfo"));
         Dataset formData = sideBySideForm.collectFormData(cr);
         if (formData.getString("state").length() == 0) {
             sideBySideForm.displayErrors(cr, new Dataset("culprit", "name",
                     "message",
                     "User doesn't exist; this is a very long message " +
                     "just to see what happens.  Does the line wrap, or " +
                     "doesn't it?  In order to find out, there needs to be " +
                     "lots and lots of text in the error message.  Also, " +
             "here are some special characters: <&>!\"\""));
         } else {
             sideBySideForm.displayErrors(cr, new Dataset("culprit", "name",
                     "message", "User doesn't exist"),
                     new Dataset("message",
                             "An error occurred that isn't associated " +
                             "with a particular form element, so this " +
                     "message should appear in the bulletin."));
         }
     }
 
     public void postVertical(ClientRequest cr) {
         Dataset main = cr.getMainDataset();
         logger.info("Posted dataset:\n" + main.toString());
         verticalForm.displayErrors(cr, new Dataset("culprit", "fruit",
                 "message", "You have chosen an exceptionally bad " +
         "tasting fruit; please pick another."));
     }
 
     RatingSection ratingStars1 = new RatingSection(
             new Dataset("numImages", "5",
                     "granularity", "0.25",
                     "imageFamily", "/static/fiz/images/goldstar48.png",
                     "unratedMessage", "Select a Rating",
                     "ajaxUrl", "/demo/ajaxReceiveData?section=ratingStars1")
     );
     RatingSection ratingStars2 = new RatingSection(
             new Dataset("numImages", "7",
                     "imageFamily", "/static/fiz/images/goldstar32.png",
                     "ajaxUrl", "/demo/ajaxReceiveData?section=ratingStars2")
     );
     RatingSection ratingStars3 = new RatingSection(
             new Dataset("numImages", "4",
                     "ajaxUrl", "/demo/ajaxReceiveData?section=ratingStars3",
                     "readOnly", "true",
                     "request", "initRatingRequest",
                     "initRatingKey", "initRating")
     );
     RatingSection ratingStars4 = new RatingSection(
             new Dataset("numImages", "5",
                     "granularity", "0.5",
                     "imageFamily", "/static/fiz/images/goldstar24.png",
                     "ajaxUrl", "/demo/ajaxReceiveData?section=ratingStars4")
     );
 
     /**
      * Displays a page demonstrating the RatingSection class.
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void ratingSection(ClientRequest cr) {
         cr.addDataRequest("initRatingRequest", RawDataManager.newRequest(
                 new Dataset("initRating", "3.4")));
         cr.getHtml().setTitle("Rating Sections");
         cr.showSections(
                 new TemplateSection("<h1>Rating Sections</h1>\n<ul>"),
                 new TemplateSection("<li><h3>Basic Stars</h3>"),
                 ratingStars1,
                 new TemplateSection("<p><b>Current Rating:</b> " +
                         "<span id=\"ratingStars1_rating\">none</span><br/>\n" +
                         "Uses stars to reflect a user rating.  This section " +
                         "use a rating fineness of 1/4 star, has a " +
                         "customized \"unrated\" message (\"Select a Rating\")," +
                         " and uses larger star images than the default ." +
                         "</p>\n</li>\n"),
                 new TemplateSection("<li><h3>Single-rating Stars</h3>"),
                 ratingStars2,
                 new TemplateSection("<p><b>Current Rating:</b> " +
                         "<span id=\"ratingStars2_rating\">none</span><br/>\n" +
                         "Only allows users to select a rating once.  After " +
                         "the first rating is submitted via Ajax, the section " +
                         "is set as read-only.</p>\n</li>\n"),
                 new TemplateSection("<li><h3>Fixed-rating Stars</h3>"),
                 ratingStars3,
                 new TemplateSection("initRatingRequest", "<p><b>Current " +
                         "Rating:</b> <span id=\"ratingStars3_rating\">" +
                         "@initRating</span><br/>\nAssigned an initial rating," +
                         " and set as read-only.</p>\n</li>\n"),
                 new TemplateSection("<li><h3>Resettable Stars</h3>"),
                 ratingStars4,
                 new TemplateSection("<p><b>Current Rating:</b> " +
                         "<span id=\"ratingStars4_rating\">none</span><br/>\n" +
                         "The rating on these stars can be reset by the " +
                         "button below.</p>\n</li>\n"),
                 new Button(new Dataset("text", "Reset",
                         "ajaxUrl", "/demo/ajaxResetSection")),
                 new TemplateSection("</ul>")
         );
     }
 
     /**
      * Updates the RatingSection demo page via Ajax.
      *
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void ajaxReceiveData(ClientRequest cr) {
         String section = cr.getMainDataset().getString("section");
         cr.updateElement(section + "_rating", Template.expandHtml(
                 "@rating (updated via Ajax)", cr.getMainDataset()));
 
         if (section.equals("ratingStars2")) {
             ratingStars2.setReadOnly(cr, true);
         }
     }
 
     /**
      * Resets one of the RatingSections on the RatingSection demo page.
      *
      * @param cr                   Overall information about the client
      *                             request being serviced.
      */
     public void ajaxResetSection(ClientRequest cr) {
         cr.updateElement("ratingStars4_rating", "none (reset by button)");
         ratingStars4.setRating(cr, -1);
     }
 }
