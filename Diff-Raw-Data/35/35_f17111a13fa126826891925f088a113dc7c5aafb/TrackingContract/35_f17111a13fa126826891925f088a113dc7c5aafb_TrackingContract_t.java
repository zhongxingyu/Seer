 package com.essers.tracking.model.provider;
 
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public class TrackingContract {
 
 	interface OrdersColumns {
 		String ORDER_ID = "order_id";
 		String CUSTOMER_ID = "customer_id";
 		String REFERENCE = "reference";
 		String STATE = "state";
 		String PICKUP_ADDRESS = "pickup_address";
 		String PICKUP_DATE = "pickup_date";
 		String DELIVERY_ADDRESS = "delivery_address";
 		String DELIVERY_DATE = "delivery_date";
 		String PROBLEM = "problem";
 		String PROBLEM_DESCRIPTION = "problem_description";
 	}
 	
 	interface AddressesColumns {
 		String ADDRESS_ID = "address_id";
 		String STREET = "street";
 		String HOUSENUMBER = "housenumber";
 		String COUNTRY = "country";
 		String ZIPCODE = "zipcode";
 		String CITY = "city";
 		String NAME = "name";
 	}
 	
 	interface CustomersColumns {
 		String CUSTOMER_ID = "customer_id";
 		String DESCRIPTION = "description";
 	}
 	
 	interface GpsColumns {
 		String ORDER_ID = "order_id";
 		String LONGITUDE = "longitude";
 		String LATITUDE = "latitude";
 	}
 	
 	public interface PickupColumns {
 		String COUNTRY = "pickup_country";
 		String ZIPCODE = "pickup_zipcode";
 		String CITY = "pickup_city";
 		String NAME = "pickup_name";
 	}
 	
 	public interface DeliveryColumns {
 		String COUNTRY = "delivery_country";
 		String ZIPCODE = "delivery_zipcode";
 		String CITY = "delivery_city";
 		String NAME = "delivery_name";
 	}
 	
 	public static final String CONTENT_AUTHORITY = "com.essers.tracking.provider";
 	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
 	
 	private static final String PATH_ORDERS = "orders";
 	private static final String PATH_ADDRESSES = "addresses";
 	private static final String PATH_CUSTOMERS = "customers";
 	private static final String PATH_GPS  = "gps";
 	
 	/**
 	 * Orders contain customer information and address information along with the described fields.
 	 */
 	public static class Orders implements OrdersColumns, BaseColumns {
 		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORDERS).build();
 		
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.essers.order";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.essers.order";
 		
 		/** Default ORDER BY */
 		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";
 		
 		public static Uri buildOrdersUri(String orderId) {
 			return CONTENT_URI.buildUpon().appendPath(orderId).build();
 		}
 		
 		public static String getOrderId(Uri uri) {
 			return uri.getPathSegments().get(1);
 		}
 		
 	}
 	
 	/**
 	 * Contains GPS info for certain orders
 	 */
 	public static class Gps implements GpsColumns, BaseColumns {
 		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GPS).build();
 		
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.essers.gps";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.essers.gps";
 		
 		/** Default ORDER BY */
 		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";
 		
		public static Uri buildGpsUri(String orderId) {
 			return CONTENT_URI.buildUpon().appendPath(orderId).build();
 		}
 		
 		public static String getOrderId(Uri uri) {
 			return uri.getPathSegments().get(1);
 		}
 		
 	}
 	
 	/**
 	 * Addresses contain all address information referenced by an {@link Order}
 	 */
 	public static class Addresses implements AddressesColumns, BaseColumns {
 		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ADDRESSES).build();
 		
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.essers.address";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.essers.address";
 		
 		/** Default ORDER BY */
 		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";
 		
 		public static Uri buildAddressUri(String addressId) {
 			return CONTENT_URI.buildUpon().appendPath(addressId).build();
 		}
 		
 		public static String getAddressId(Uri uri) {
 			return uri.getPathSegments().get(1);
 		}
 		
 	}
 	
 	/**
 	 * Addresses contain all address information referenced by an {@link Order}
 	 */
 	public static class Customers implements CustomersColumns, BaseColumns {
 		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUSTOMERS).build();
 		
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.essers.customer";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.essers.customer";
 		
 		/** Default ORDER BY */
 		public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";
 		
 		public static Uri buildAddressUri(String customerId) {
 			return CONTENT_URI.buildUpon().appendPath(customerId).build();
 		}
 
 		public static String getCustomerId(Uri uri) {
 			return uri.getPathSegments().get(1);
 		}
 
 		
 		
 	}
 	
 	private TrackingContract() {
 	}
 }
