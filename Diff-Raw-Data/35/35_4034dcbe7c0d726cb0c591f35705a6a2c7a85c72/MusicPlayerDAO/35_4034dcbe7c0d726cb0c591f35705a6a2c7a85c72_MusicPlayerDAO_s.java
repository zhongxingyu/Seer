 package com.example.musicplayer.db;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import com.example.musicplayer.pojo.Album;
 import com.example.musicplayer.pojo.Artist;
 import com.example.musicplayer.pojo.Song;
 import com.example.musicplayer.util.Util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: neevek
  * Date: 7/20/13
  * Time: 11:45 AM
  */
 public class MusicPlayerDAO {
     private SQLiteOpenHelper mDbHelper;
 
     public MusicPlayerDAO (SQLiteOpenHelper dbHelper) {
         mDbHelper = dbHelper;
     }
 
     public int addArtist (String name) {
         SQLiteDatabase db;
         Cursor cursor = null;
         int rowId = 0;
         try {
             db = mDbHelper.getWritableDatabase();
             ContentValues contentValues = new ContentValues();
             contentValues.put("name", name);
             rowId = (int)db.insertWithOnConflict("artist_info", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
             if (rowId == -1) {
                 String sql = "SELECT _id FROM artist_info WHERE name='"+ Util.escapeDBSingleQuotes(name) +"'";
                 cursor = db.rawQuery(sql, null);
 
                 if (cursor.moveToFirst()) {
                     rowId = cursor.getInt(0);
                 }
             }
 
             if (rowId > 0) {
                 String sql = "UPDATE artist_info SET song_count=song_count+1 WHERE _id=" + rowId;
                 db.execSQL(sql);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
         return rowId;
     }
 
     public int addAlbum (String name) {
         SQLiteDatabase db;
         Cursor cursor = null;
         int rowId = 0;
         try {
             db = mDbHelper.getWritableDatabase();
             ContentValues contentValues = new ContentValues();
             contentValues.put("name", name);
             rowId = (int)db.insertWithOnConflict("album_info", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
             if (rowId == -1) {
                 String sql = "SELECT _id FROM album_info WHERE name='"+ Util.escapeDBSingleQuotes(name) +"'";
                 cursor = db.rawQuery(sql, null);
 
                 if (cursor.moveToFirst()) {
                     rowId = cursor.getInt(0);
                 }
             }
 
             if (rowId > 0) {
                 String sql = "UPDATE album_info SET song_count=song_count+1 WHERE _id=" + rowId;
                 db.execSQL(sql);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
         return rowId;
     }
 
     public int addSong (String title, int artistId, String artist, int albumId, String album, int duration, String filePath) {
         SQLiteDatabase db;
         try {
             db = mDbHelper.getWritableDatabase();
             ContentValues contentValues = new ContentValues();
             contentValues.put("title", title);
             contentValues.put("artist_id", artistId);
             contentValues.put("artist", artist);
             contentValues.put("album_id", albumId);
             contentValues.put("album", album);
             contentValues.put("duration", duration);
             contentValues.put("file_path", filePath);
             return (int)db.insertWithOnConflict("song_info", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return -1;
     }
 
     public void deleteSong (int songId, int artistId, int albumId) {
         SQLiteDatabase db;
         Cursor cursor = null;
         try {
             db = mDbHelper.getWritableDatabase();
             String sql = "DELETE FROM song_info WHERE _id=" + songId;
             db.execSQL(sql);
 
             sql = "SELECT song_count FROM album_info WHERE _id=" + albumId;
             cursor = db.rawQuery(sql, null);
             if (cursor.moveToFirst()) {
                 int songCount = cursor.getInt(0);
                 if (songCount > 1) {
                     sql = "UPDATE album_info SET song_count=song_count-1 WHERE _id=" + albumId;
                 } else {
                     sql = "DELETE FROM album_info WHERE _id=" + albumId;
                 }
                 db.execSQL(sql);
             }
 
             cursor.close();
 
             sql = "SELECT song_count FROM artist_info WHERE _id=" + artistId;
             cursor = db.rawQuery(sql, null);
             if (cursor.moveToFirst()) {
                 int songCount = cursor.getInt(0);
                 if (songCount > 1) {
                     sql = "UPDATE artist_info SET song_count=song_count-1 WHERE _id=" + albumId;
                 } else {
                     sql = "DELETE FROM artist_info WHERE _id=" + albumId;
                 }
                 db.execSQL(sql);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
     }
 
     public int getAllMusicCount () {
         return getTotalRowCount("song_info");
     }
 
     public int getAlbumCount () {
         return getTotalRowCount("album_info");
     }
 
     public int getArtistCount () {
         return getTotalRowCount("artist_info");
     }
 
     private int getTotalRowCount (String tableName) {
         SQLiteDatabase db;
         Cursor cursor = null;
         try {
             db = mDbHelper.getReadableDatabase();
             String sql = "SELECT 1 FROM " + tableName;
             cursor = db.rawQuery(sql, null);
 
             if (cursor.moveToFirst()) {
                 return cursor.getCount();
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
         return 0;
     }
 
     public List<Song> getAllSongs () {
         return getSongsWithSQL("SELECT _id, title, artist_id, artist, album_id, album, duration, file_path FROM song_info");
     }
 
     public List<Song> getSongsByAlbumId (int albumId) {
         return getSongsWithSQL("SELECT _id, title, artist_id, artist, album_id, album, duration, file_path FROM song_info WHERE album_id=" + albumId);
     }
 
     public List<Song> getSongsByArtistId (int artistId) {
         return getSongsWithSQL("SELECT _id, title, artist_id, artist, album_id, album, duration, file_path FROM song_info WHERE artist_id=" + artistId);
     }
 
     private List<Song> getSongsWithSQL (String sql) {
         SQLiteDatabase db;
         Cursor cursor = null;
         List<Song> list = new ArrayList<Song>();
         try {
             db = mDbHelper.getReadableDatabase();
             cursor = db.rawQuery(sql, null);
 
             if (cursor.moveToFirst()) {
                 while (!cursor.isAfterLast()) {
                     int id = cursor.getInt(0);
                     String title = cursor.getString(1);
                     int artistId = cursor.getInt(2);
                     String artist = cursor.getString(3);
                     int albumId = cursor.getInt(4);
                     String album = cursor.getString(5);
                     int duration = cursor.getInt(6);
                     String filePath = cursor.getString(7);
 
                     list.add(new Song (id, title, artistId, artist, albumId, album, duration, filePath));
 
                     cursor.moveToNext();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
         Util.sortSongList(list);
         return list;
     }
 
     public List<Album> getAlbums () {
         SQLiteDatabase db;
         Cursor cursor = null;
         List<Album> list = new ArrayList<Album>();
         try {
             db = mDbHelper.getReadableDatabase();
             String sql = "SELECT _id, name, song_count FROM album_info";
             cursor = db.rawQuery(sql, null);
 
             if (cursor.moveToFirst()) {
                 while (!cursor.isAfterLast()) {
                     int id = cursor.getInt(0);
                     String name = cursor.getString(1);
                     int songCount = cursor.getInt(2);
 
                     list.add(new Album(id, name, songCount));
 
                     cursor.moveToNext();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
 
         Util.sortAlbumList(list);
         return list;
     }
 
     public Song getSongById (int id) {
         SQLiteDatabase db;
         Cursor cursor = null;
         Song song = null;
         try {
             db = mDbHelper.getReadableDatabase();
             String sql = "SELECT _id, title, artist_id, artist, album_id, album, duration, file_path FROM song_info WHERE _id=" + id;
             cursor = db.rawQuery(sql, null);
 
             if (cursor.moveToFirst()) {
                 String title = cursor.getString(1);
                 int artistId = cursor.getInt(2);
                 String artist = cursor.getString(3);
                 int albumId = cursor.getInt(4);
                 String album = cursor.getString(5);
                 int duration = cursor.getInt(6);
                 String filePath = cursor.getString(7);
 
                 song = new Song (id, title, artistId, artist, albumId, album, duration, filePath);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
         return song;
     }
 
     public List<Artist> getArtists () {
         SQLiteDatabase db;
         Cursor cursor = null;
         List<Artist> list = new ArrayList<Artist>();
         try {
             db = mDbHelper.getReadableDatabase();
             String sql = "SELECT _id, name, song_count FROM artist_info";
             cursor = db.rawQuery(sql, null);
 
             if (cursor.moveToFirst()) {
                 while (!cursor.isAfterLast()) {
                     int id = cursor.getInt(0);
                     String name = cursor.getString(1);
                     int songCount = cursor.getInt(2);
 
                     list.add(new Artist(id, name, songCount));
 
                     cursor.moveToNext();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (cursor != null)
                 cursor.close();
         }
 
         Util.sortArtistList(list);
         return list;
     }
 
 }
