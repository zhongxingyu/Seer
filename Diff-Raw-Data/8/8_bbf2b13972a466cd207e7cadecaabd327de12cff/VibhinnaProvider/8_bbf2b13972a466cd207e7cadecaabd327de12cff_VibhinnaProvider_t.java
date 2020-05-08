 package com.binoy.vibhinna;
 
 import java.io.File;
 import java.util.Scanner;
 import java.util.regex.Pattern;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class VibhinnaProvider extends ContentProvider {
 	private DatabaseHelper mDatabaseHelper;
 	private SQLiteDatabase mDatabase;
 	private Context mContext;
 
 	protected static LocalBroadcastManager mLocalBroadcastManager;
 
 	public static final String AUTHORITY = "com.binoy.vibhinna.VibhinnaProvider";
 	private static final String TAG = "VibhinnaProvider";
 
 	public static final int VFS = 0;
 	public static final int VFS_ID = 1;
 	private static final int VFS_LIST = 2;
 	private static final int VFS_DETAILS = 3;
 	private static final int VFS_SCAN = 4;
 	private static final int WRITE_XML = 5;
 	private static final int READ_XML = 6;
 
 	private static final UriMatcher sURIMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 	public static final String VFS_BASE_PATH = "vfs";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 			+ "/" + VFS_BASE_PATH);
 	public static final Uri LIST_DISPLAY_URI = Uri.parse("content://"
 			+ AUTHORITY + "/" + VFS_BASE_PATH + "/list");
 
 	static {
 		sURIMatcher.addURI(AUTHORITY, VFS_BASE_PATH, VFS);
 		sURIMatcher.addURI(AUTHORITY, VFS_BASE_PATH + "/#", VFS_ID);
 		sURIMatcher.addURI(AUTHORITY, VFS_BASE_PATH + "/list", VFS_LIST);
 		sURIMatcher
 				.addURI(AUTHORITY, VFS_BASE_PATH + "/details/#", VFS_DETAILS);
 		sURIMatcher.addURI(AUTHORITY, VFS_BASE_PATH + "/scan", VFS_SCAN);
 		sURIMatcher.addURI(AUTHORITY, VFS_BASE_PATH + "/write_xml", WRITE_XML);
 		sURIMatcher.addURI(AUTHORITY, VFS_BASE_PATH + "/read_xml", READ_XML);
 
 	}
 
 	@Override
 	public int delete(Uri uri, String where, String[] selectionArgs) {
 		int count = 0;
 		switch (sURIMatcher.match(uri)) {
 		case VFS:
 			count = mDatabase.delete(DatabaseHelper.VFS_DATABASE_TABLE, where,
 					selectionArgs);
 			break;
 		case VFS_ID:
 			count = mDatabase.delete(DatabaseHelper.VFS_DATABASE_TABLE,
 					BaseColumns._ID
 							+ " = "
 							+ uri.getPathSegments().get(1)
 							+ (!TextUtils.isEmpty(where) ? " AND (" + where
 									+ ')' : ""), selectionArgs);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case VFS_ID:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfs";
 		case VFS:
 			return "vnd.android.cursor.dir/vnd.vibhinna.vfs_dir";
 		case VFS_LIST:
 			return "vnd.android.cursor.dir/vnd.vibhinna.vfs_list";
 		case VFS_DETAILS:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfs_details";
 		case VFS_SCAN:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfs_scan";
 		case READ_XML:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfs_read_xml";
 		case WRITE_XML:
 			return "vnd.android.cursor.item/vnd.vibhinna.vfs_write_xml";
 		default:
 			throw new IllegalArgumentException("Unknown URI");
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		long rowID = mDatabase.insert(DatabaseHelper.VFS_DATABASE_TABLE, null,
 				values);
 		if (rowID > 0) {
 			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
 			getContext().getContentResolver().notifyChange(_uri, null);
 			return _uri;
 		}
 		// throw new SQLException("Failed to insert row into " + uri);
 		return uri;
 	}
 
 	@Override
 	public boolean onCreate() {
 		mContext = getContext();
 		mDatabaseHelper = new DatabaseHelper(mContext);
 		mDatabase = mDatabaseHelper.getWritableDatabase();
 		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
 		return true;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		int count = 0;
 		switch (sURIMatcher.match(uri)) {
 		case VFS:
 			count = mDatabase.update(DatabaseHelper.VFS_DATABASE_TABLE, values,
 					selection, selectionArgs);
 			break;
 		case VFS_ID:
 			count = mDatabase.update(DatabaseHelper.VFS_DATABASE_TABLE, values,
 					BaseColumns._ID + " = " + uri.getLastPathSegment(),
 					selectionArgs);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 		queryBuilder.setTables(DatabaseHelper.VFS_DATABASE_TABLE);
 		int uriType = sURIMatcher.match(uri);
 		switch (uriType) {
 		case VFS_ID:
 			queryBuilder.appendWhere(BaseColumns._ID + "="
 					+ uri.getLastPathSegment());
 			break;
 		case VFS:
 			// no filter
 			break;
 		case VFS_LIST:
 			Cursor c = query(CONTENT_URI, projection, selection, selectionArgs,
 					sortOrder);
 			MatrixCursor cursor = new MatrixCursor(
 					Constants.MATRIX_COLUMN_NAMES);
 			if (c.moveToFirst()) {
 				do {
 					File root = new File(c.getString(2));
 					if (root.canRead()) {
 						Object[] fsii = new Object[8];
 						String cache = null;
 						String data = null;
 						String system = null;
 						String vdstatus = "0";
 						File cacheimg = new File(root, "cache.img");
 						if (cacheimg.exists()) {
 							cache = mContext.getString(R.string.space_in_mb,
 									(cacheimg.length() / 1048576));
 						} else
 							cache = mContext.getString(R.string.error);
 						File dataimg = new File(root, "data.img");
 						if (dataimg.exists()) {
 							data = mContext.getString(R.string.space_in_mb,
 									(dataimg.length() / 1048576));
 						} else
 							data = mContext.getString(R.string.error);
 						File systemimg = new File(root, "system.img");
 						if (systemimg.exists()) {
 							system = mContext.getString(R.string.space_in_mb,
 									(systemimg.length() / 1048576));
 						} else
 							system = mContext.getString(R.string.error);
 						if (systemimg.exists() && cacheimg.exists()
 								&& dataimg.exists()) {
 							vdstatus = "1";
 						} else
 							vdstatus = "0";
 						fsii[0] = Integer.parseInt(c.getString(0));
 						fsii[1] = c.getString(1);
 						fsii[2] = c.getString(4);
 						fsii[3] = null;
 						fsii[4] = c.getString(3);
 						fsii[5] = mContext.getString(R.string.vfs_short_info,
 								cache, data, system);
 						fsii[6] = vdstatus;
 						fsii[7] = c.getString(2);
 						cursor.addRow(fsii);
 					}
 				} while (c.moveToNext());
 			}
 			c.close();
 			return cursor;
 		case VFS_DETAILS:
 			String[] vsinfo = new String[29];
 			Log.d(TAG, "Uri : " + uri.toString());
 			Cursor databaseCursor = mDatabase.query(
 					DatabaseHelper.VFS_DATABASE_TABLE, Constants.allColumns,
 					"_id = ?", new String[] { uri.getLastPathSegment() }, null,
 					null, null);
 			databaseCursor.moveToFirst();
 			vsinfo[0] = databaseCursor.getString(0);
 			vsinfo[1] = databaseCursor.getString(1);
 			String vspath = databaseCursor.getString(2);
 			File vsfolder = new File(vspath);
 			vsinfo[2] = vsfolder.getName();
 			vsinfo[3] = databaseCursor.getString(3);
 			vsinfo[4] = databaseCursor.getString(4);
 			databaseCursor.close();
 			for (int i = 5; i < 29; i++) {
 				vsinfo[i] = mContext.getString(R.string.not_available);
 			}
 			for (int i = 7; i < 29; i = i + 8) {
 				vsinfo[i] = mContext.getString(R.string.corrupted);
 			}
 			try {
 				String[] shellinput = { Constants.CMD_TUNE2FS, vspath,
 						"/cache.img", "" };
 				String istr = ProcessManager.inputStreamReader(shellinput, 40);
 				Scanner scanner = new Scanner(istr).useDelimiter("\\n");
 				scanner.findWithinHorizon(
 						Pattern.compile("Filesystem\\sUUID:\\s*(\\S+)"), 0);
 				String chuuid = scanner.match().group(1);
 				scanner.findWithinHorizon(Pattern
 						.compile("Filesystem\\smagic\\snumber:\\s*(\\S+)"), 0);
 				String chmagicnumber = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\scount:\\s*(\\d+)"), 0);
 				String chblockcount = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Free\\sblocks:\\s*(\\d+)"), 0);
 				String chfreeblocks = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\ssize:\\s*(\\d+)"), 0);
 				String chblocksize = scanner.match().group(1);
 				vsinfo[5] = chuuid;
 				vsinfo[6] = chmagicnumber;
 				if (chmagicnumber.equals("0xEF53")) {
 					vsinfo[7] = mContext.getString(R.string.healthy);
 				}
 				vsinfo[8] = Integer.parseInt(chblockcount)
 						* Integer.parseInt(chblocksize) / 1048576 + "";
 				vsinfo[9] = Integer.parseInt(chfreeblocks)
 						* Integer.parseInt(chblocksize) / 1048576 + "";
 				vsinfo[10] = chblockcount;
 				vsinfo[11] = chfreeblocks;
 				vsinfo[12] = chblocksize;
 			} catch (Exception e) {
 				Log.w("Exception", "exception in executing :"
 						+ Constants.CMD_TUNE2FS + vspath + "/cache.img");
 			}
 			try {
 				String[] shellinput = { Constants.CMD_TUNE2FS, vspath,
 						"/data.img", "" };
 				String istr = ProcessManager.inputStreamReader(shellinput, 40);
 				Scanner scanner = new Scanner(istr).useDelimiter("\\n");
 				scanner.findWithinHorizon(
 						Pattern.compile("Filesystem\\sUUID:\\s*(\\S+)"), 0);
 				String dauuid = scanner.match().group(1);
 				scanner.findWithinHorizon(Pattern
 						.compile("Filesystem\\smagic\\snumber:\\s*(\\S+)"), 0);
 				String damagicnumber = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\scount:\\s*(\\d+)"), 0);
 				String dablockcount = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Free\\sblocks:\\s*(\\d+)"), 0);
 				String dafreeblocks = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\ssize:\\s*(\\d+)"), 0);
 				String dablocksize = scanner.match().group(1);
 				vsinfo[13] = dauuid;
 				vsinfo[14] = damagicnumber;
 				if (damagicnumber.equals("0xEF53")) {
 					vsinfo[15] = mContext.getString(R.string.healthy);
 				}
 				vsinfo[16] = Integer.parseInt(dablockcount)
 						* Integer.parseInt(dablocksize) / 1048576 + "";
 				vsinfo[17] = Integer.parseInt(dafreeblocks)
 						* Integer.parseInt(dablocksize) / 1048576 + "";
 				vsinfo[18] = dablockcount;
 				vsinfo[19] = dafreeblocks;
 				vsinfo[20] = dablocksize;
 			} catch (Exception e) {
 				Log.w("Exception", "exception in executing :"
 						+ Constants.CMD_TUNE2FS + vspath + "/data.img");
 			}
 			try {
 				String[] shellinput = { Constants.CMD_TUNE2FS, vspath,
 						"/system.img", "" };
 				String istr = ProcessManager.inputStreamReader(shellinput, 40);
 				Scanner scanner = new Scanner(istr).useDelimiter("\\n");
 				scanner.findWithinHorizon(
 						Pattern.compile("Filesystem\\sUUID:\\s*(\\S+)"), 0);
 				String syuuid = scanner.match().group(1);
 				scanner.findWithinHorizon(Pattern
 						.compile("Filesystem\\smagic\\snumber:\\s*(\\S+)"), 0);
 				String symagicnumber = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\scount:\\s*(\\d+)"), 0);
 				String syblockcount = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Free\\sblocks:\\s*(\\d+)"), 0);
 				String syfreeblocks = scanner.match().group(1);
 				scanner.findWithinHorizon(
 						Pattern.compile("Block\\ssize:\\s*(\\d+)"), 0);
 				String syblocksize = scanner.match().group(1);
 				vsinfo[21] = syuuid;
 				vsinfo[22] = symagicnumber;
 				if (symagicnumber.equals("0xEF53")) {
 					vsinfo[23] = mContext.getString(R.string.healthy);
 				}
 				vsinfo[24] = Integer.parseInt(syblockcount)
 						* Integer.parseInt(syblocksize) / 1048576 + "";
 				vsinfo[25] = Integer.parseInt(syfreeblocks)
 						* Integer.parseInt(syblocksize) / 1048576 + "";
 				vsinfo[26] = syblockcount;
 				vsinfo[27] = syfreeblocks;
 				vsinfo[28] = syblocksize;
 			} catch (Exception e) {
 				Log.w("Exception", "exception in executing :"
 						+ Constants.CMD_TUNE2FS + vspath + "/system.img");
 			}
 			String key[] = new String[29];
 			for (int i = 0; i < key.length; i++) {
 				key[i] = "key" + i;
 			}
 			MatrixCursor matrixCursor = new MatrixCursor(key);
 			matrixCursor.addRow(vsinfo);
 			return matrixCursor;
 		case VFS_SCAN:
 			DatabaseUtils.scanFolder(mDatabase, mContext);
 			Intent vfsListUpdatedIntent = new Intent();
 			vfsListUpdatedIntent
 					.setAction(VibhinnaService.ACTION_VFS_LIST_UPDATED);
 			mLocalBroadcastManager.sendBroadcast(vfsListUpdatedIntent);
			return null;
 		case WRITE_XML:
 			DatabaseUtils.writeXML(mDatabase);
			return null;
 		case READ_XML:
 			DatabaseUtils.readXML(mDatabase);
			return null;
 		default:
 			Log.e(TAG, "unknown uri :" + uri.toString() + ", type : " + uriType);
 			throw new IllegalArgumentException("Unknown URI");
 		}
 
 		Cursor cursor = queryBuilder.query(mDatabase, projection, selection,
 				selectionArgs, null, null, sortOrder);
 
 		cursor.setNotificationUri(mContext.getContentResolver(), uri);
 		return cursor;
 	}
 
 }
