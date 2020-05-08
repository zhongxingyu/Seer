 /**
  * 
  */
 package org.idch.bible.importers.hcsb;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.idch.bible.importers.hcsb.BookHandler.BookName;
 import org.idch.bible.importers.hcsb.BookHandler.LastUpdate;
 import org.idch.bible.ref.BookOrder;
 import org.idch.texts.importer.StructureHandler;
 
 /**
  * @author Neal Audenaert
  */
 public class BCV {
     public static Pattern OSIS_ID = Pattern.compile("bible\\.(\\d+)\\.(\\d+)\\.(\\d+)");
     
     final BookHandler bookHandler;
     final SectionHandler sectionHandler;
     final BookName    nameHandler;
     final LastUpdate  lastUpdateHandler;
     final ChapterHandler chapterHandler;
     final VerseHandler verseHandler;
     
     //===================================================================================
     // ATTACHMENT METHODS & MEMBERS
     //===================================================================================
     
     // The books/chapters and verses need to coordinate carefully with each other's instances
     // when parsing the HCSB, because not all structures are explicitly marked (hence, a 
     // chapter may need to access the VerseHandler even when that handler is not active).
     // The methods and members provide a mechanism for interacting with the specific handler 
     // objects. 
     
     
     BCV() {
         bookHandler = new BookHandler(this);
         sectionHandler = new SectionHandler(this);
         nameHandler = new BookName(this);
         lastUpdateHandler = new LastUpdate(this);
         chapterHandler = new ChapterHandler(this);
         verseHandler = new VerseHandler(this);
     }
     
     public void attach(HCSBImporter importer) {
         importer.addHandler(verseHandler);
         importer.addHandler(sectionHandler);
         importer.addHandler(chapterHandler);
         importer.addHandler(bookHandler);
         importer.addHandler(lastUpdateHandler);
         importer.addHandler(verseHandler);
     }
     
    public static abstract class BCVHandler extends StructureHandler {
         public static OsisId getOsisId(BookOrder order, String n) {
             Matcher m = OSIS_ID.matcher(n);
             if (m.matches()) {
                 int bookId = Integer.parseInt(m.group(1));
                 int chId = Integer.parseInt(m.group(2));
                 int vsId = Integer.parseInt(m.group(3));
                 
                 return new OsisId(order, bookId - 1, chId, vsId);
             }
             
             return null;
         }
         
         protected boolean printProgress = false;
         protected final BCV bcv;
         
         protected BCVHandler(String name, BCV bcv) {
             super(name);
             this.bcv = bcv;
         }
         
         protected void closeDependentHandler(String name) {
             // close any open chapters (this will, in turn, close any open verses)
             StructureHandler h = ctx.getHandler(name);
             if (h != null) 
                 h.closeActiveStructure();
         }
     }
 
     public static class OsisId {
         BookOrder order;
         int bookIx;
         int chId;
         int vsId;
         
         OsisId(BookOrder order, int book, int ch, int vs) {
             this.order = order;
             this.bookIx = book;
             this.chId = ch;
             this.vsId = vs;
         }
         
         String getBookId() {
             return order.getId(bookIx);
         }
         
         String getBookName() {
             return order.getName(bookIx);
         }
         
         public String toString() {
             String osisId = this.getBookId();
             if (chId > 0) osisId += "." + chId;
             if (vsId > 0) osisId += "." + vsId;
             
             return osisId;
         }
         
     }
 }
