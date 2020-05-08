 package mobisocial.rectacular.model;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteStatement;
 
 public class FollowingManager extends ManagerBase {
     private static final String[] STANDARD_FIELDS = new String[] {
         MFollowing.COL_ID,
         MFollowing.COL_FEED_ID,
         MFollowing.COL_USER_ID
     };
     
     private static final int _id = 0;
     private static final int feedId = 1;
     private static final int userId = 2;
     
     private SQLiteStatement sqlInsertFollowing;
 
     public FollowingManager(SQLiteDatabase db) {
         super(db);
     }
     
     public FollowingManager(SQLiteOpenHelper databaseSource) {
         super(databaseSource);
     }
     
     /**
      * Add a user that is being followed
      * @param following MFollowing object with all details filled
      */
     public void insertFollowing(MFollowing following) {
         SQLiteDatabase db = initializeDatabase();
         if (sqlInsertFollowing == null) {
             synchronized(this) {
                 if (sqlInsertFollowing == null) {
                     StringBuilder sql = new StringBuilder()
                         .append("INSERT INTO ").append(MFollowing.TABLE)
                         .append("(")
                         .append(MFollowing.COL_FEED_ID).append(",")
                         .append(MFollowing.COL_USER_ID)
                        .append(") VALUES (?,?");
                     sqlInsertFollowing = db.compileStatement(sql.toString());
                 }
             }
         }
         synchronized(sqlInsertFollowing) {
             bindField(sqlInsertFollowing, feedId, following.feedId);
             bindField(sqlInsertFollowing, userId, following.userId);
             following.id = sqlInsertFollowing.executeInsert();
         }
     }
     
     /**
      * Get the users we follow for this feed
      * @param feedId long MFeed id
      * @return A set of MFollowing objects
      */
     public Set<MFollowing> getFollowing(Long feedId) {
         SQLiteDatabase db = initializeDatabase();
         String table = MFollowing.TABLE;
         String[] columns = STANDARD_FIELDS;
         String selection = MFollowing.COL_FEED_ID + "=?";
         String[] selectionArgs = new String[] { feedId.toString() };
         String groupBy = null, having = null, orderBy = null;
         Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
         try {
             Set<MFollowing> following = new HashSet<MFollowing>();
             while (c.moveToNext()) {
                 following.add(fillInStandardFields(c));
             }
             return following;
         } finally {
             c.close();
         }
     }
     
     /**
      * Remove all users followed on this feed
      * @param feedId long MFeed id
      * @return true if rows were deleted, false otherwise
      */
     public boolean clearFollowing(Long feedId) {
         SQLiteDatabase db = initializeDatabase();
         String table = MFollowing.TABLE;
         String whereClause = MFollowing.COL_FEED_ID + "=?";
         String[] whereArgs = new String[] { feedId.toString() };
         return db.delete(table, whereClause, whereArgs) > 0;
     }
     
     private MFollowing fillInStandardFields(Cursor c) {
         MFollowing following = new MFollowing();
         following.id = c.getLong(_id);
         following.feedId = c.getLong(feedId);
         following.userId = c.getString(userId);
         return following;
     }
 
     @Override
     public void close() {
         if (sqlInsertFollowing != null) {
             sqlInsertFollowing.close();
         }
     }
 
 }
