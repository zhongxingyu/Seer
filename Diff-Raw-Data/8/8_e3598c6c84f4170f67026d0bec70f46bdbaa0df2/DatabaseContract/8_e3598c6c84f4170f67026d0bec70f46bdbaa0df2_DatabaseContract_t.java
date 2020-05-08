 package no.ntnu.stud.fallprevention.connectivity;
 
 import android.provider.BaseColumns;
 
 /**
  * Database Contract specifies the details of the application's database.
  * Using a database contract to store all the table and column names is a
  * common way to ensure consistent name usage throughout the application.
  * 
  * @author Elias
  */
 
 public abstract class DatabaseContract {
 	
 	public static abstract class EventType implements BaseColumns {
 		public static final String TABLE_NAME = "EventType";
 		public static final String COLUMN_NAME_ID = "TypeID"; 
 		public static final String COLUMN_NAME_DESCRIPTION = "Description";
 		public static final String COLUMN_NAME_TITLE = "Headline";
 		public static final String COLUMN_NAME_ICON = "Icon";
 	}
 	
 	public static abstract class Event implements BaseColumns {
 		public static final String TABLE_NAME = "Event";
 		public static final String COLUMN_NAME_ID = "ID";
 		public static final String COLUMN_NAME_TYPEID = "TypeID";
 		public static final String COLUMN_NAME_PARAMETER_1="Param1";
 		public static final String COLUMN_NAME_PARAMETER_2="Param2";
 	}
 	
 	public static abstract class Contact implements BaseColumns {
 		public static final String TABLE_NAME = "Contact";
 		public static final String COLUMN_NAME_NAME = "Name";
 		public static final String COLUMN_NAME_ID = "PersonID";
 		public static final String COLUMN_NAME_PHONE = "PhoneNumber";
 	}
 	
 	public static abstract class AlarmTypes implements BaseColumns {
 		public static final String TABLE_NAME = "AlarmTypes";
 		public static final String COLUMN_NAME_ID = "AlarmID";
 		public static final String COLUMN_NAME_DESCRIPTION = "Description";
 	}
 }
