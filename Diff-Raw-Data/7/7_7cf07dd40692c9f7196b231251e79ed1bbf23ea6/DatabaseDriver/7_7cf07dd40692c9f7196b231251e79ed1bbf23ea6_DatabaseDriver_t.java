 package my.apps;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DatabaseDriver {
     private LocalRepository localRepository;
 
     public DatabaseDriver(Context context) {
         localRepository = new LocalRepository(context);
     }
 
     public void startWithCleanSlate() {
         SQLiteDatabase db = localRepository.getWritableDatabase();
         localRepository.onUpgrade(db, -1, -1);
        db.close();
     }
 }
