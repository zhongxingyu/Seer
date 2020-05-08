 package polly.rx.http;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import polly.rx.MyPlugin;
 import polly.rx.core.ScoreBoardManager;
 import polly.rx.entities.ScoreBoardEntry;
 import polly.rx.parsing.ScoreBoardParser;
 import de.skuzzle.polly.sdk.CSVExporter;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.http.HttpAction;
 import de.skuzzle.polly.sdk.http.HttpEvent;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 import de.skuzzle.polly.sdk.http.HttpTemplateException;
 import de.skuzzle.polly.sdk.http.HttpTemplateSortHelper;
 import de.skuzzle.polly.sdk.time.Time;
 
 
 public class ScoreBoardHttpAction extends HttpAction {
 
     private ScoreBoardManager sbeManager;
     
     public final static String CSV_FILE_NAME = "scoreboard.txt";
     
 
     private final static DateFormat DATE_FORMAT = new SimpleDateFormat(
             "EE MMM dd, yyyy hh:mm a");
     
     
     public ScoreBoardHttpAction(MyPolly myPolly, ScoreBoardManager sbeManager) {
         super("/Scoreboard", myPolly);
         this.requirePermission(MyPlugin.SBE_PERMISSION);
         this.sbeManager = sbeManager;
     }
 
     
     
     @Override
     public HttpTemplateContext execute(HttpEvent e) throws HttpTemplateException,
             InsufficientRightsException {
         HttpTemplateContext c = new HttpTemplateContext("pages/sbe.html");
         String action = e.getProperty("action");
         if (action != null && action.equals("postSB")) {
             synchronized (this) {
                 String paste = e.getProperty("paste");
                 String d = e.getProperty("date");
                 try {
                     Date date = Time.currentTime();
                     if (d != null && !d.equals("")) {
                         date = DATE_FORMAT.parse(d);
                     }
                     Collection<ScoreBoardEntry> ents = ScoreBoardParser.parse(paste, date);
                     
                    for (ScoreBoardEntry ent : ents) {
                        this.sbeManager.addEntry(ent);
                    }
 
                     Collection<ScoreBoardEntry> allEntries = this.sbeManager.getEntries();
                     CSVExporter.exportToCSV(allEntries, 
                             new File(e.getSource().getTemplateRoot(), 
                                     CSV_FILE_NAME).toString(), ",");
                 } catch (Exception e1) {
                     e.throwTemplateException("Invalid paste", "");
                 }
             }
         } else if (action != null && action.equals("delete")) {
             String idS = e.getProperty("id");
             if (idS != null) {
                 int id = Integer.parseInt(idS);
                 try {
                     this.sbeManager.deleteEntry(id);
                 } catch (DatabaseException e1) {
                     e.throwTemplateException(e1);
                 }
             }
         }
         
         HttpTemplateSortHelper.makeListSortable(
             c, e, "sortKey", "dir", "getRank");
         
         List<ScoreBoardEntry> entries = this.sbeManager.getEntries();
         entries = ScoreBoardEntry.postFilter(entries);
         
         c.put("maxCompare", this.sbeManager.maxColors());
         c.put("csvFileName", CSV_FILE_NAME);
         c.put("nformat", ScoreBoardManager.NUMBER_FORMAT);
         c.put("entries", entries);
         
         return c;
     }
 
 }
