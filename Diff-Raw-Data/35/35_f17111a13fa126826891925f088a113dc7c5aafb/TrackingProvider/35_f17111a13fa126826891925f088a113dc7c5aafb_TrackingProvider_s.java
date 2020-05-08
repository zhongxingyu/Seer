 package com.essers.tracking.model.provider;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import android.content.ContentProvider;
 import android.content.ContentProviderOperation;
 import android.content.ContentProviderResult;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.util.Log;
 
 import com.essers.tracking.model.provider.TrackingContract.Addresses;
 import com.essers.tracking.model.provider.TrackingContract.Customers;
 import com.essers.tracking.model.provider.TrackingContract.Orders;
 import com.essers.tracking.model.provider.TrackingDatabase.Tables;
 import com.essers.tracking.model.provider.TrackingDatabase.Views;
 import com.essers.tracking.util.SelectionBuilder;
 
 public class TrackingProvider extends ContentProvider {
 
 	private static final String TAG = "TrackingProvider";
 
 	private static final UriMatcher sUriMatcher = buildUriMatcher();
 	private TrackingDatabase mDatabase;
 
 	public static final int ORDERS = 100;
 	public static final int ORDERS_ID = 101;
 
 	public static final int CUSTOMERS = 200;
 	public static final int CUSTOMERS_ID = 201;
 
 	public static final int ADDRESSES = 300;
 	public static final int ADDRESSES_ID = 301;
 	
 	public static final int CUSTOM_ORDERS = 400;
 
 	/**
 	 * Build and return a {@link UriMatcher} that catches all {@link Uri}
 	 * variations supported by this {@link ContentProvider}.
 	 */
 	private static UriMatcher buildUriMatcher() {
 		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
 		final String authority = TrackingContract.CONTENT_AUTHORITY;
 
 		matcher.addURI(authority, "orders", ORDERS);
 		matcher.addURI(authority, "orders/*", ORDERS_ID);
 
 		matcher.addURI(authority, "customers", CUSTOMERS);
 		matcher.addURI(authority, "customers/*", CUSTOMERS_ID);
 
 		matcher.addURI(authority, "addresses", ADDRESSES);
 		matcher.addURI(authority, "addresses/*", ADDRESSES_ID);
 		
 
 		return matcher;
 	}
 
 	@Override
 	public boolean onCreate() {
 		final Context context = getContext();
 		mDatabase = new TrackingDatabase(context);
 		return true;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String getType(Uri uri) {
 		final int match = sUriMatcher.match(uri);
 		switch (match) {
 		case ORDERS:
 			return Orders.CONTENT_TYPE;
 		case ORDERS_ID:
 			return Orders.CONTENT_ITEM_TYPE;
 		case CUSTOMERS:
 			return Customers.CONTENT_TYPE;
 		case CUSTOMERS_ID:
 			return Customers.CONTENT_ITEM_TYPE;
 		case ADDRESSES:
 			return Addresses.CONTENT_TYPE;
 		case ADDRESSES_ID:
 			return Addresses.CONTENT_ITEM_TYPE;
 		default:
 			throw new UnsupportedOperationException("Unknown uri: " + uri);
 		}
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		
 		Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
 		final SQLiteDatabase db = mDatabase.getReadableDatabase();
 		
 		final int match = sUriMatcher.match(uri);
 		switch(match) {
 		default: 
 			final SelectionBuilder builder = buildExpandedSelection(uri, match);
 			
 			Cursor c = builder.where(selection, selectionArgs).query(db, projection, sortOrder);
 			c.setNotificationUri(getContext().getContentResolver(), uri);
 			return c;
 		}
 		
 		
 	}
 	
 
 	/** {@inheritDoc} */
 	@Override
 	public int delete(Uri uri, String where, String[] whereArgs) {
 		Log.v(TAG, "delete(uri=" + uri + ")");
 		
 		final SQLiteDatabase db = mDatabase.getWritableDatabase();
 		
 		final SelectionBuilder builder = buildSimpleSelection(uri);
         int retVal = builder.where(where, whereArgs).delete(db);
         getContext().getContentResolver().notifyChange(uri, null);
         return retVal;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
 
 		final SQLiteDatabase db = mDatabase.getWritableDatabase();
 		final int match = sUriMatcher.match(uri);
 
 		switch (match) {
 		case ORDERS: {
 			db.insertOrThrow(Tables.ORDERS, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return Orders.buildOrdersUri(values.getAsString(Orders.ORDER_ID));
 		}
 		case ADDRESSES: {
 			db.insertOrThrow(Tables.ADDRESSES, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return Orders.buildOrdersUri(values.getAsString(Addresses.ADDRESS_ID));
 		}
 		case CUSTOMERS: {
 			db.insertOrThrow(Tables.CUSTOMERS, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return Orders.buildOrdersUri(values.getAsString(Customers.CUSTOMER_ID));
 		}
 		
 		default:
 			throw new UnsupportedOperationException("Unknown uri: " + uri);
 		}
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/**
      * Build an advanced {@link SelectionBuilder} to match the requested
      * {@link Uri}. This is usually only used by {@link #query}, since it
      * performs table joins useful for {@link Cursor} data.
      */
 	private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
 
 		final SelectionBuilder builder = new SelectionBuilder();
 
 		switch (match) {
 		case ORDERS:
 			builder.table(Views.CUSTOM_ORDERS);
 			break;
 		case ORDERS_ID:
 			String orderId = Orders.getOrderId(uri);
 			builder.table(Tables.ORDERS)
 				.where(Orders.ORDER_ID + "=?", orderId);
 			break;
 		case ADDRESSES_ID:
 			String addressId = Addresses.getAddressId(uri);
 			builder.table(Tables.ADDRESSES)
 				.where(Addresses.ADDRESS_ID + "=?", addressId);
 			break;
 		
 		default:
 			throw new IllegalArgumentException("Unknown URI: " + uri
 					+ " match=" + match);
 
 		}
 		return builder;
 
 	}
 	
 	/**
      * Build a simple {@link SelectionBuilder} to match the requested
      * {@link Uri}. This is usually enough to support {@link #insert},
      * {@link #update}, and {@link #delete} operations.
      */
     private SelectionBuilder buildSimpleSelection(Uri uri) {
         final SelectionBuilder builder = new SelectionBuilder();
         final int match = sUriMatcher.match(uri);
         switch (match) {
             case ORDERS: {
                 return builder.table(Tables.ORDERS);
             }
             case ORDERS_ID: {
             	final String orderId = Orders.getOrderId(uri);
             	Log.d(TAG, "SimpleSelection: orderId=" + orderId);
             	return builder.table(Tables.ORDERS).where(Orders.ORDER_ID + "=?", orderId);
             }
             case CUSTOMERS: {
             	return builder.table(Tables.CUSTOMERS);
             }
             case CUSTOMERS_ID: {
             	final String customerId = Customers.getCustomerId(uri);
             	Log.d(TAG, "SimpleSelection: customerId=" + customerId);
             	return builder.table(Tables.CUSTOMERS).where(Customers.CUSTOMER_ID + "=?", customerId);
             }
             case ADDRESSES: {
             	return builder.table(Tables.ADDRESSES);
             }
             case ADDRESSES_ID: {
             	final String addressId = Addresses.getAddressId(uri);
             	Log.d(TAG, "SimpleSelection: addressId=" + addressId);
             	return builder.table(Tables.ADDRESSES).where(Addresses.ADDRESS_ID + "=?", addressId);
             }
             default: {
                 throw new UnsupportedOperationException("Unknown uri: " + uri);
             }
         }
     }
 
 	/**
 	 * Apply the given set of {@link ContentProviderOperation}, executing inside
 	 * a {@link SQLiteDatabase} transaction. All changes will be rolled back if
 	 * any single one fails.
 	 */
 	@Override
 	public ContentProviderResult[] applyBatch(
 			ArrayList<ContentProviderOperation> operations)
 			throws OperationApplicationException {
 
 		if (operations == null) {
 			throw new OperationApplicationException(
 					"Operations had a null value while trying to apply them in batch.");
 		}
 
 		final SQLiteDatabase db = mDatabase.getWritableDatabase();
 		db.beginTransaction();
 		try {
 			final int numOperations = operations.size();
 			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
 			for (int i = 0; i < numOperations; i++) {
 				results[i] = operations.get(i).apply(this, results, i);
 			}
 			db.setTransactionSuccessful();
 			return results;
 		} finally {
 			db.endTransaction();
 		}
 	}
 
 }
