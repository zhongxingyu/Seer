 package com.jacobobryant.scripturemastery;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import java.io.*;
 import java.util.*;
 
 import android.util.Log;
 
 public class DBHandler extends SQLiteOpenHelper {
     private final int[] BOOK_IDS = {R.raw.old_testament,
             R.raw.new_testament, R.raw.book_of_mormon,
             R.raw.doctrine_and_covenants, R.raw.lists,
             R.raw.articles_of_faith};
     public static final String DB_NAME = "scriptures.db";
     public static final String BOOKS = "books";
     private static final int VERSION = 7;
     private Context context;
 
     private class OldBook {
         private String title;
         private OldScripture[] scriptures;
         private boolean preloaded;
         private String routine;
 
         public OldBook(String title, OldScripture[] scriptures,
                 String strRoutine, int id, boolean preloaded) {
             this.title = title;
             this.scriptures = scriptures;
             for (OldScripture scripture : scriptures) {
                 scripture.setParent(this);
             }
             this.routine = strRoutine;
             this.preloaded = preloaded;
         }
 
         public OldBook(String title, List<OldScripture> scriptures,
                 String routine, int id, boolean preloaded) {
             this(title, scriptures.toArray(new OldScripture[
                 scriptures.size()]), routine, id, preloaded);
         }
 
         public OldBook(String title, List<OldScripture> scriptures) {
             this(title, scriptures, null, 0, false);
         }
 
         public String getTitle() {
             return title;
         }
         
         public OldScripture[] getScriptures() {
             return scriptures;
         }
 
         public OldScripture getScripture(int index) {
             return scriptures[index];
         }
 
         public String getRoutine() {
             return routine;
         }
 
         public boolean wasPreloaded() {
             return preloaded;
         }
     }
 
     private class OldScripture {
         public static final int NOT_STARTED = 0;
         private String reference;
         private String keywords;
         private String verses;
         private OldBook parent;
 
         public OldScripture(String reference, String keywords,
                 String verses) {
             this.reference = reference;
             this.keywords = keywords;
             this.verses = verses;
         }
 
         public String getReference() {
             return reference;
         }
 
         public String getVerses() {
             return verses;
         }
 
         public void setParent(OldBook parent) {
             if (this.parent != null) {
                 throw new UnsupportedOperationException();
             }
             this.parent = parent;
         }
 
         public String getKeywords() {
             return keywords;
         }
     }
 
     public DBHandler(Context context) {
         super(context, DB_NAME, null, VERSION);
         this.context = context;
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) { }
 
     private void createTableBooks(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + BOOKS + " (" +
                 "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 "title STRING, " +
                 "routine STRING, " +
                 "preloaded INTEGER DEFAULT 0);");
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion,
                           int newVersion) {
         if (oldVersion == 3) {
             upgrade3to4(db);
             oldVersion++;
         }
         if (oldVersion == 4) {
             upgrade4to5(db);
             oldVersion++;
         }
         if (oldVersion == 5) {
             upgrade5to6(db);
             oldVersion++;
         }
         if (oldVersion == 6) {
             upgrade6to7(db);
             oldVersion++;
         }
     }
 
     private void upgrade3to4(SQLiteDatabase db) {
         db.execSQL("ALTER TABLE " + BOOKS + " ADD COLUMN " +
                 "is_scripture INTEGER DEFAULT 0");
         db.execSQL("UPDATE " + BOOKS + " SET is_scripture = 1 " + 
                 "WHERE _id <= " + BOOK_IDS.length);
     }
 
     private void upgrade4to5(SQLiteDatabase db) {
         final int SCRIPTURE_COUNT = 25;
         String table;
         Cursor bookCursor = db.rawQuery("SELECT _id, is_scripture FROM "
                 + BOOKS + " ORDER BY _id ASC", null);
         int isScripture;
         int scripIndex = 0;
         OldBook book;
         OldScripture scrip;
 
         bookCursor.moveToFirst();
         while (! bookCursor.isAfterLast()) {
             table = getTable(bookCursor.getInt(0));
             isScripture = bookCursor.getInt(1);
             db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + 
                     "keywords STRING");
             if (isScripture == 1) {
                 book = readBook(BOOK_IDS[scripIndex++]);
                 for (int i = 0; i < SCRIPTURE_COUNT; i++) {
                     scrip = book.getScripture(i);
                     db.execSQL("UPDATE " + table + " SET keywords=\"" +
                             scrip.getKeywords() + "\" WHERE _id=" +
                             (i + 1));
                 }
             }
             bookCursor.moveToNext();
         }
     }
 
     private void upgrade5to6(SQLiteDatabase db) {
         Cursor cur = db.rawQuery("SELECT _id, title, routine, " +
                 "is_scripture FROM " + BOOKS + " ORDER BY _id", null);
         int position = 1;
         List<OldBook> books = new ArrayList<OldBook>();
         int id;
         String title;
         String routine;
         boolean preloaded;
 
         cur.moveToFirst();
         while (!cur.isAfterLast()) {
             id = cur.getInt(0);
             title = cur.getString(1);
             routine = cur.getString(2);
             preloaded = (cur.getInt(3) == 1) ? true : false;
             books.add(new OldBook(title, new OldScripture[0], routine, id,
                                preloaded));
             if (id != position) {
                 db.execSQL("ALTER TABLE " + getTable(id) + " RENAME TO " +
                         getTable(position));
             }
            if (++position == 5) {
                 books.add(readBook(BOOK_IDS[4]));
                 books.add(readBook(BOOK_IDS[5]));
                 position += 2;
             }
            cur.moveToNext();
         }
         cur.close();
         db.execSQL("DROP TABLE " + BOOKS);
         createTableBooks(db);
         position = 0;
         for (OldBook book : books) {
             position++;
             if (position == 5 || position == 6) {
                 addBook(db, book, true);
                 continue;
             }
             routine = book.getRoutine();
             routine = (routine == null) ?
                     "null" : "\"" + routine + "\"";
             db.execSQL(String.format("INSERT INTO %s (title, routine, " +
                     "preloaded) VALUES (\"%s\", %s, %d)", BOOKS,
                     book.getTitle(), routine,
                     ((book.wasPreloaded()) ? 1 : 0)));
         }
     }
 
     private void upgrade6to7(SQLiteDatabase db) {
         final short LIST_POSITION = 4;
         OldBook listBook = readBook(BOOK_IDS[LIST_POSITION]);
         String table = getTable(LIST_POSITION + 1);
 
         for (OldScripture scrip : listBook.getScriptures()) {
             db.execSQL("UPDATE " + table + " SET verses = \"" +
                     scrip.getVerses() + "\" WHERE reference = \"" +
                     scrip.getReference() + "\"");
         }
     }
 
     private void addBook(SQLiteDatabase db, OldBook book,
             boolean preloaded) {
         String table;
         Cursor cursor;
         int preloadedInt = (preloaded) ? 1 : 0;
 
         db.execSQL("INSERT INTO books (title, preloaded) VALUES (\"" +
                 book.getTitle() + "\", " + preloadedInt + ")");
         cursor = db.rawQuery("SELECT _id FROM " + BOOKS +
                 " ORDER BY _id DESC LIMIT 1", null);
         cursor.moveToFirst();
         table = "book_" + cursor.getString(0);
         cursor.close();
         db.execSQL("CREATE TABLE " + table + " (" +
                 "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                 "reference STRING, " +
                 "keywords STRING, " +
                 "verses STRING, " +
                 "status INTEGER DEFAULT " + OldScripture.NOT_STARTED +
                 ", finishedStreak INTEGER DEFAULT 0);");
         for (OldScripture scripture : book.getScriptures()) {
             addScripture(db, table, scripture);
         }
     }
 
     public void addBook(SQLiteDatabase db, OldBook book) {
         addBook(db, book, false);
     }
 
     public void addScripture(SQLiteDatabase db, String table,
             OldScripture scripture) {
         db.execSQL("INSERT INTO " + table +
                 "(reference, keywords, verses) VALUES (\"" +
                 scripture.getReference() + "\", \"" +
                 scripture.getKeywords() + "\", \"" +
                 scripture.getVerses() + "\");");
     }
 
     private OldBook readBook(int id) {
         String title = "";
         List<OldScripture> scriptures = new ArrayList<OldScripture>();
         String reference;
         String keywords;
         StringBuilder verses;
         String verse;
         BufferedReader reader = new BufferedReader(new InputStreamReader(
                 context.getResources().openRawResource(id)));
 
         try {
             title = reader.readLine();
             while ((reference = reader.readLine()) != null) {
                 keywords = reader.readLine();
                 verses = new StringBuilder();
                 while ((verse = reader.readLine()) != null &&
                         verse.length() != 0) {
                     if (verses.length() > 0) {
                         verses.append("\n");
                     }
                     verses.append(verse);
                 }
                 scriptures.add(new OldScripture(reference, keywords,
                         verses.toString()));
             }
             reader.close();
         } catch (IOException ioe) {
             Log.e(MainActivity.TAG, "Couldn't read book data from file");
         }
         return new OldBook(title, scriptures);
     }
 
     public static String getTable(int id) {
         return "book_" + id;
     }
 }
