 package cat.mobilejazz.database.content;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import android.annotation.SuppressLint;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteConstraintException;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.os.RemoteException;
 import android.provider.BaseColumns;
 import cat.mobilejazz.database.ProgressListener;
 import cat.mobilejazz.database.Table;
 import cat.mobilejazz.database.content.DataAdapter.DataAdapterListener;
 import cat.mobilejazz.database.content.DataProvider.ResolvedUri;
 import cat.mobilejazz.database.query.Select;
 import cat.mobilejazz.utilities.collections.CachedIterator;
 import cat.mobilejazz.utilities.collections.SetFromMap;
 import cat.mobilejazz.utilities.debug.Debug;
 
 /* TODO: maybe outsource to tb project? */
 public class DataProcessor implements DataAdapterListener, ChangesListener {
 
 	/**
 	 * After the data processor has updated the database, this interface allows
 	 * the UI to be notified of certain changes.
 	 * 
 	 * It has three methods that allow to narrow down the number of events, this
 	 * listener is informed of. Currently only REMOVE operations are supported.
 	 * 
 	 * @author Hannes Widmoser
 	 * 
 	 */
 	public static class DatabaseUpdateListener implements Parcelable {
 
 		public static final String KEY_TABLE = "table";
 		public static final String KEY_SERVER_ID = "server_id";
 
 		private String table;
 		private Long serverId;
 		private Integer type;
 		private Messenger messenger;
 
 		/**
 		 * Creates a new listener.
 		 * 
 		 * @param table
 		 *            The table this listener is interested in or {@code null}
 		 *            for all tables.
 		 * @param serverId
 		 *            The id of the item this listener is interested in or
 		 *            {@code null} for all items.
 		 * @param type
 		 *            The type of change this listener is interested in or
 		 *            {@code null} for all types. This must be one of
 		 *            {@link Changes#ACTION_CREATE},
 		 *            {@link Changes#ACTION_REMOVE},
 		 *            {@link Changes#ACTION_UPDATE}.
 		 * @param messenger
 		 *            A {@link Messenger} that gets notified of changes that
 		 *            match the filter criteria. The messenger receives a
 		 *            message where {@code what} refers to the type, and the
 		 *            bundle's {@link #KEY_TABLE}, {@link #KEY_SERVER_ID} refer
 		 *            to table and server id respectively.
 		 */
 		public DatabaseUpdateListener(String table, Long serverId, Integer type, Messenger messenger) {
 			super();
 			this.table = table;
 			this.serverId = serverId;
 			this.type = type;
 		}
 
 		/**
 		 * @return The table this listener is interested in or {@code null} for
 		 *         all tables.
 		 */
 		public String getTable() {
 			return table;
 		}
 
 		/**
 		 * @return The id of the item this listener is interested in or
 		 *         {@code null} for all items.
 		 */
 		public Long getServerId() {
 			return serverId;
 		}
 
 		/**
 		 * @return The type of change this listener is interested in or
 		 *         {@code null} for all types. This must be one of
 		 *         {@link Changes#ACTION_CREATE}, {@link Changes#ACTION_REMOVE},
 		 *         {@link Changes#ACTION_UPDATE}.
 		 */
 		public Integer getType() {
 			return type;
 		}
 
 		/**
 		 * Notifies the listener of a change in the database due to the last
 		 * action of the respective processor.
 		 * 
 		 * @param type
 		 *            The type of change in the database. Must be one of
 		 *            {@link Changes#ACTION_CREATE},
 		 *            {@link Changes#ACTION_REMOVE},
 		 *            {@link Changes#ACTION_UPDATE}.
 		 * @param table
 		 *            The table that was affected by the change.
 		 * @param serverId
 		 *            The serverId of the item that was affected.
 		 */
 		public void onDatabaseChange(int type, String table, long serverId) {
 			Message msg = Message.obtain();
 			msg.what = type;
 			msg.getData().putString(KEY_TABLE, table);
 			msg.getData().putLong(KEY_SERVER_ID, serverId);
 			try {
 				messenger.send(msg);
 			} catch (RemoteException e) {
 			}
 		}
 
 		@Override
 		public int describeContents() {
 			return 0;
 		}
 
 		@Override
 		public void writeToParcel(Parcel dest, int flags) {
 			dest.writeInt(type);
 			dest.writeString(table);
 			dest.writeLong(serverId);
 			dest.writeParcelable(messenger, flags);
 		}
 
 		private DatabaseUpdateListener(Parcel in) {
 			type = in.readInt();
 			table = in.readString();
 			serverId = in.readLong();
 			messenger = in.readParcelable(getClass().getClassLoader());
 		}
 
 		public static final Parcelable.Creator<DatabaseUpdateListener> CREATOR = new Parcelable.Creator<DatabaseUpdateListener>() {
 			public DatabaseUpdateListener createFromParcel(Parcel in) {
 				return new DatabaseUpdateListener(in);
 			}
 
 			public DatabaseUpdateListener[] newArray(int size) {
 				return new DatabaseUpdateListener[size];
 			}
 		};
 
 	}
 
 	// private static final class DatabaseUpdateListenerFilter {
 	//
 	// private String table;
 	// private Long serverId;
 	// private Integer type;
 	//
 	// public DatabaseUpdateListenerFilter(String table, Long serverId, Integer
 	// type) {
 	// this.table = table;
 	// this.serverId = serverId;
 	// this.type = type;
 	// }
 	//
 	// @Override
 	// public boolean equals(Object o) {
 	// if (o == null)
 	// return false;
 	// try {
 	// DatabaseUpdateListenerFilter f = (DatabaseUpdateListenerFilter) o;
 	// return ObjectUtils.equals(table, f.table) && ObjectUtils.equals(serverId,
 	// f.serverId)
 	// && ObjectUtils.equals(type, f.type);
 	// } catch (ClassCastException e) {
 	// return false;
 	// }
 	// }
 	//
 	// @Override
 	// public int hashCode() {
 	// return ((table != null) ? table.hashCode() : 0) + ((serverId != null) ?
 	// serverId.hashCode() : 0)
 	// + ((type != null) ? type.hashCode() : 0);
 	// }
 	//
 	// }
 
 	private SQLiteDatabase mDb;
 	private String mUser;
 	private Set<String> mAffectedTables;
 
 	private ProgressListener mListener;
 	private Table mMainTable;
 	private long mOperationsDone;
 
 	private Select mCurrentSelection;
 
 	private boolean mCancelled;
 
 	private LinkedHashMap<Table, SortedSet<DataEntry>> mOperations;
 	private Map<Table, Integer> mDepthMap;
 
 	private DataProvider provider;
 
 	private DatabaseUpdateListener updateListener;
 
 	private Set<String> mPendingUpdates;
 	private Set<String> mPendingDeletes;
 
 	public DataProcessor(DataProvider provider, String user, SQLiteDatabase db, ProgressListener listener,
 			Table mainTable, long expectedCount, Select currentSelection, DatabaseUpdateListener updateListener) {
 		this.provider = provider;
 		mDb = db;
 		mUser = user;
 		mAffectedTables = new HashSet<String>();
 		mOperations = new LinkedHashMap<Table, SortedSet<DataEntry>>();
 		mDepthMap = new HashMap<Table, Integer>();
 		mListener = listener;
 		mOperationsDone = 0;
 		mMainTable = mainTable;
 		mCurrentSelection = currentSelection.buildUpon().sort(mainTable.getColumnSyncId().getName()).build();
 
 		this.updateListener = updateListener;
 	}
 
 	@SuppressLint("DefaultLocale")
 	private String getSignature(String table, long id) {
 		return String.format("%s:%d", table, id);
 	}
 
 	private final DataEntry NO_DATA = new DataEntry(Long.MAX_VALUE, null);
 
 	private long getCurrentServerId(Cursor current) {
 		if (!current.isAfterLast()) {
 			return current.getLong(1);
 		} else {
 			return Long.MAX_VALUE;
 		}
 	}
 
 	private int insertOrUpdate(String table, ContentValues values, String identifyingColumn, long identifyingValue) {
 		try {
 			long result = mDb.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
 			if (result < 0L) {
 				// row exists already -> update:
 				mDb.update(table, values, String.format("%s = %d", identifyingColumn, identifyingValue), null);
 			}
 			return 1;
 		} catch (SQLiteConstraintException e) {
 			Debug.error("Constraint error inserting into %s, %s", table, values);
 			return 0;
 		}
 	}
 
 	private boolean listenerIsInterestedIn(DatabaseUpdateListener l, int type, String table, long serverId) {
 		return (l.getType() == null || l.getType() == type) && (l.getServerId() == null || l.getServerId() == serverId)
 				&& (l.getTable() == null || table.equals(l.getTable()));
 	}
 
 	private void notifyUpdateListeners(int type, String table, long serverId) {
 		if (updateListener != null && listenerIsInterestedIn(updateListener, type, table, serverId)) {
 			updateListener.onDatabaseChange(type, table, serverId);
 		}
 	}
 
 	private Set<String> getPendingActions(Cursor pendingChanges, int action) {
 		Set<String> result = SetFromMap.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
 		if (pendingChanges.getCount() > 0) {
 			pendingChanges.moveToFirst();
 			while (!pendingChanges.isAfterLast()) {
 				if (pendingChanges.getInt(2) == action)
 					result.add(getSignature(pendingChanges.getString(0), pendingChanges.getLong(1)));
 				pendingChanges.moveToNext();
 			}
 		}
 		return result;
 	}
 
 	private void merge(Table table, Select localData, SortedSet<DataEntry> operations, Date startTime) {
 
 		String syncIdCol = table.getColumnSyncId().getName();
 
 		List<String> projection = new ArrayList<String>();
 		projection.add(BaseColumns._ID);
 		projection.add(syncIdCol);
 		if (table.hasColumnCreationDate()) {
 			projection.add(table.getColumnCreationDate().toString());
 		}
 
 		Cursor current = mDb.query(localData.getTable(), projection.toArray(new String[] {}), localData.getSelection(),
 				localData.getSelectionArgs(), null, null, localData.getSortOrder());
 		try {
 			current.moveToFirst();
 			Debug.debug("[%d, %d] Querying: %s", current.getCount(), operations.size(), mCurrentSelection);
 
 			CachedIterator<DataEntry> i = new CachedIterator<DataEntry>(operations.iterator(), NO_DATA);
 
 			while ((!current.isAfterLast() || !i.isAfterLast()) && !isCancelled()) {
 				long currentServerId = getCurrentServerId(current);
 				DataEntry entry = i.getValue();
 
 				Debug.debug("%s: %d <--- %d", table.getName(), currentServerId, entry.serverId);
 
 				if (entry.serverId == currentServerId) {
 					// update:
 					if (!mPendingUpdates.contains(getSignature(table.getName(), entry.values.getAsLong(syncIdCol)))) {
 						mOperationsDone += mDb.update(table.getName(), entry.values, "_id = ?",
 								new String[] { String.valueOf(current.getLong(0)) });
 					}
 					i.moveToNext();
 					current.moveToNext();
 				} else if (entry.serverId < currentServerId) {
 					// insert:
 					if (!mPendingDeletes.contains(getSignature(table.getName(), entry.values.getAsLong(syncIdCol)))) {
 						mOperationsDone += insertOrUpdate(table.getName(), entry.values, syncIdCol, entry.serverId);
 					}
 					i.moveToNext();
 				} else {
 					// delete:
 					if (currentServerId > 0
							&& (table.hasColumnCreationDate())) {
 						notifyUpdateListeners(Changes.ACTION_REMOVE, table.getName(), current.getLong(1));
 						mOperationsDone += mDb.delete(table.getName(), "_id = ?",
 								new String[] { String.valueOf(current.getLong(0)) });
 					}
 					current.moveToNext();
 				}
 			}
 		} finally {
 			current.close();
 		}
 
 	}
 
 	public void performOperations(Date startTime) {
 
 		if (!isCancelled()) {
 
 			// TODO: this should be always empty
 			Cursor pendingChanges = mDb.query(Changes.TABLE_NAME, new String[] { Changes.COLUMN_TABLE,
 					Changes.COLUMN_ID, Changes.COLUMN_ACTION }, null, null, null, null, null);
 
 			try {
 
 				mPendingUpdates = getPendingActions(pendingChanges, Changes.ACTION_UPDATE);
 				provider.addChangesListener(this);
 				mPendingDeletes = getPendingActions(pendingChanges, Changes.ACTION_REMOVE);
 
 				List<Map.Entry<Table, SortedSet<DataEntry>>> operations = new ArrayList<Map.Entry<Table, SortedSet<DataEntry>>>(
 						mOperations.entrySet());
 				Collections.sort(operations, new Comparator<Map.Entry<Table, SortedSet<DataEntry>>>() {
 
 					@Override
 					public int compare(Entry<Table, SortedSet<DataEntry>> lhs, Entry<Table, SortedSet<DataEntry>> rhs) {
 						return mDepthMap.get(lhs.getKey()).compareTo(mDepthMap.get(rhs.getKey()));
 					}
 
 				});
 
 				for (Map.Entry<Table, SortedSet<DataEntry>> e : operations) {
 
 					if (isCancelled()) {
 						Debug.debug("cancelled");
 						break;
 					}
 
 					Table table = e.getKey();
 					String syncIdCol = table.getColumnSyncId().getName();
 
 					if (table.equals(mMainTable)) {
 						merge(table, mCurrentSelection, e.getValue(), startTime);
 					} else {
 						long currentParentId = 0L;
 						// process non maintable entries
 						for (DataEntry entry : e.getValue()) {
 							if (isCancelled()) {
 								Debug.debug("cancelled");
 								break;
 							}
 
 							if (entry.getParentId() != currentParentId) {
 								currentParentId = entry.getParentId();
 								// delete all old entries. This is only possible
 								// if always ALL delegate entries of a parent
 								// entity are returned by the
 								// server.
 								mDb.delete(table.getName(), table.getColumnParentId().getName() + " = ?",
 										new String[] { String.valueOf(currentParentId) });
 							}
 
 							// no deletes are propagated along delegates:
 							if (!mPendingDeletes.contains(getSignature(table.getName(),
 									entry.values.getAsLong(syncIdCol)))) {
 								mOperationsDone += insertOrUpdate(table.getName(), entry.values, syncIdCol,
 										entry.serverId);
 							}
 						}
 					}
 				}
 			} finally {
 				pendingChanges.close();
 				provider.removeChangesListener(this);
 			}
 		} else {
 			Debug.debug("cancelled");
 		}
 
 		Debug.info("Operations done: " + mOperationsDone);
 		mListener.onFinished();
 
 	}
 
 	@Override
 	public void onDataEntry(Table table, int depth, ContentValues data) {
 		SortedSet<DataEntry> inserts = mOperations.get(table);
 		if (inserts == null) {
 			inserts = new TreeSet<DataEntry>();
 			mOperations.put(table, inserts);
 			mDepthMap.put(table, depth);
 		}
 
 		final String changeIdColumn = table.getColumnSyncId().getName();
 		if (table.hasColumnParentId()) {
 			final String parentIdColumn = table.getColumnParentId().getName();
 			inserts.add(new DataEntry(data.getAsLong(parentIdColumn), data.getAsLong(changeIdColumn), data));
 		} else {
 			inserts.add(new DataEntry(data.getAsLong(changeIdColumn), data));
 		}
 
 		mAffectedTables.add(table.getName());
 	}
 
 	public void notifyChanges() {
 		if (mOperationsDone > 0) {
 			for (String table : mAffectedTables) {
 				Uri uri = provider.getUri(mUser, table);
 				ResolvedUri resolvedUri = provider.resolveUri(uri);
 				provider.notifyChange(uri, resolvedUri);
 			}
 		}
 	}
 
 	public synchronized boolean isCancelled() {
 		return mCancelled;
 	}
 
 	public synchronized void cancel() {
 		mCancelled = true;
 	}
 
 	@Override
 	public void onEmptyTable(Table table) {
 		Debug.info("onEmptyTable(%s)", table);
 		if (!mOperations.containsKey(table)) {
 			mOperations.put(table, new TreeSet<DataEntry>());
 			mDepthMap.put(table, 0);
 			mAffectedTables.add(table.getName());
 		}
 	}
 
 	@Override
 	public void onInsertChange(ContentValues change) {
 		if (change.getAsInteger(Changes.COLUMN_ACTION) == Changes.ACTION_UPDATE) {
 			mPendingUpdates.add(getSignature(change.getAsString(Changes.COLUMN_TABLE),
 					change.getAsLong(Changes.COLUMN_ID)));
 		}
 	}
 
 }
