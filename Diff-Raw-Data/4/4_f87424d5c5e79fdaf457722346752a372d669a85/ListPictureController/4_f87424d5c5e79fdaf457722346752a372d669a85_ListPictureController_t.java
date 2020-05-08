 package cs310w10.MoleFinder.Controller;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import cs310w10.MoleFinder.Model.MoleSQLiteHelper;
 import cs310w10.MoleFinder.Model.Picture;
 import cs310w10.MoleFinder.Model.TableMolesPictures;
 import cs310w10.MoleFinder.Model.TablePictures;
 
 public class ListPictureController {
 	private ArrayList<Picture> pictures;
 	private final MoleSQLiteHelper connection;
 
 	public ListPictureController(Context context) {
 		this.connection = MoleSQLiteHelper.getInstance(context);
 	}
 
 	public ListPictureController(ArrayList<Picture> pictures, Context context) {
 		this.pictures = pictures;
 		this.connection = MoleSQLiteHelper.getInstance(context);
 	}
 
 	public void insertPicture(PictureController picture) {
 		pictures.add(picture.getPicture());
 	}
 
 	public int getNextFreeID() {
 		// TODO: find out the next free id from the database
 		// this method is obsolete now?
 		return 10;
 	}
 
 	public Picture getPictureById(int id) {
 		Iterator<Picture> li = pictures.iterator();
 		while (li.hasNext()) {
 			Picture picture = li.next();
 			if (picture.getId() == id) {
 				return picture;
 			}
 		}
 		return null;
 	}
 
 	public void getAllPictures() {
 		SQLiteDatabase database = connection.getWritableDatabase();
 		pictures = new ArrayList<Picture>();
 		Cursor cursor = database.query(TablePictures.TABLE_PICTURES,
 				TablePictures.ALLCOLUMNS, null, null, null, null, null);
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Picture picture = cursorToPicture(cursor);
 			pictures.add(picture);
 			cursor.moveToNext();
 		}
 		cursor.close();
 		connection.close();
 	}
 
 	/**
 	 * retrieve the picture object described by the table row pointed by the
 	 * cursor provided
 	 * 
 	 * @param cursor
 	 * @return
 	 */
 	private Picture cursorToPicture(Cursor cursor) {
 		Picture picture = new Picture();
 		int pictureId = cursor.getInt(0);
 		picture.setId(pictureId);
 		picture.setDescription(cursor.getString(1));
 		Calendar date = Calendar.getInstance();
 		date.setTimeInMillis(cursor.getInt(2));
 		picture.setDate(date);
 		Uri imageData = Uri.parse(cursor.getString(3));
 		picture.setImageData(imageData);
 
 		return picture;
 	}
 
 	/**
 	 * @param moleID
 	 */
 	public ArrayList<Picture> getListPictureFromMole(int moleID) {
 		SQLiteDatabase database = connection.getWritableDatabase();
 		pictures = new ArrayList<Picture>();
 		String[] columns = { TableMolesPictures.COLUMN_PICTUREID };
 		Cursor cursor = database.query(TableMolesPictures.TABLE_MOLESPICTURES,
 				columns,
 				TableMolesPictures.COLUMN_PICTUREID + " = " + moleID, null,
 				null, null, null);
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Picture picture = new Picture();
 			int PhotoID = cursor.getInt(0);
 			picture.setId(PhotoID);
 			// a temporal cursor to get the
 			Cursor tempcursor = database.query(TablePictures.TABLE_PICTURES,
					TablePictures.ALLCOLUMNS, TablePictures.COLUMN_ID + " = "
							+ PhotoID, null,
 					null, null, null);
 			tempcursor.moveToFirst();
 			picture.setDescription(tempcursor.getString(1));
 			Long time = (long) tempcursor.getInt(2);
 			Calendar date = Calendar.getInstance();
 			date.setTimeInMillis(time);
 			picture.setDate(date);
 			picture.setImageData(Uri.parse(tempcursor.getString(3)));
 		}
 		cursor.close();
 		connection.close();
 		return pictures;
 	}
 
 	/**
 	 * Delete the list of moles from the database
 	 */
 	public void deleteListPicture() {
 		SQLiteDatabase database = connection.getWritableDatabase();
 		Iterator<Picture> li = pictures.iterator();
 		while (li.hasNext()) {
 			Picture picture = li.next();
 			long PictureId = picture.getId();
 			database.delete(TablePictures.TABLE_PICTURES,
 					TablePictures.COLUMN_ID + " = " + PictureId, null);
 		}
 		connection.close();
 
 	}
 }
