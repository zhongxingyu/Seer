 package tk.crazysoft.ego.data;
 
 import android.provider.BaseColumns;
 
 public final class EGOContract {
 
     private EGOContract() { }
 
     public static abstract class Addresses implements BaseColumns {
         public static final String TABLE_NAME = "addresses";
         public static final String COLUMN_NAME_LATITUDE = "latitude";
         public static final String COLUMN_NAME_LONGITUDE = "longitude";
         public static final String COLUMN_NAME_ZIP = "zipcode";
         public static final String COLUMN_NAME_CITY = "city";
         public static final String COLUMN_NAME_STREET = "street";
         public static final String COLUMN_NAME_STREET_NO = "streetno";
         public static final String COLUMN_NAME_MAP_SHEET = "mapsheet";
 
         public static final String SQL_SELECT_DUPLICATE_STREETS =
                "SELECT " + COLUMN_NAME_STREET +
                        " FROM (SELECT " + COLUMN_NAME_STREET +
                         " FROM " + TABLE_NAME +
                         " GROUP BY " + COLUMN_NAME_ZIP + ", " +
                         COLUMN_NAME_STREET + " ORDER BY " +
                         COLUMN_NAME_STREET + " ASC) GROUP BY " +
                         COLUMN_NAME_STREET + " HAVING COUNT(" +
                         COLUMN_NAME_STREET + ") > 1";
 
         public static final String SQL_COPY_STREET =
                 "INSERT INTO " + TABLE_NAME + " " +
                         "SELECT NULL AS " + _ID + ", " +
                         COLUMN_NAME_LATITUDE + ", " +
                         COLUMN_NAME_LONGITUDE + ", " +
                         "? AS " + COLUMN_NAME_ZIP + ", " +
                         "? AS " + COLUMN_NAME_CITY + ", " +
                         COLUMN_NAME_STREET + ", " +
                         COLUMN_NAME_STREET_NO + ", " +
                         COLUMN_NAME_MAP_SHEET + " FROM " +
                         TABLE_NAME + " WHERE " +
                         COLUMN_NAME_ZIP + " = ? AND " +
                         COLUMN_NAME_CITY + " = ? AND " +
                         COLUMN_NAME_STREET + " = ?";
     }
 
     public static abstract class HospitalAdmission implements BaseColumns {
         public static final String TABLE_NAME = "hospital_admission";
         public static final String COLUMN_NAME_DATE = "date";
         public static final String COLUMN_NAME_TIME_FROM = "timefrom";
         public static final String COLUMN_NAME_TIME_TO = "timeto";
         public static final String COLUMN_NAME_HOSPITAL_NAME = "hospitalname";
     }
 
     public static abstract class DoctorStandby implements BaseColumns {
         public static final String TABLE_NAME = "doctor_standby";
         public static final String COLUMN_NAME_DATE = "date";
         public static final String COLUMN_NAME_TIME_FROM = "timefrom";
         public static final String COLUMN_NAME_TIME_TO = "timeto";
         public static final String COLUMN_NAME_DOCTOR_NAME = "doctorname";
     }
 
     public static final String SQL_CREATE_ADRESSES =
             "CREATE TABLE " + Addresses.TABLE_NAME + " (" +
             Addresses._ID + " INTEGER PRIMARY KEY, " +
             Addresses.COLUMN_NAME_LATITUDE + " REAL, " +
             Addresses.COLUMN_NAME_LONGITUDE + " REAL, " +
             Addresses.COLUMN_NAME_ZIP + " TEXT, " +
             Addresses.COLUMN_NAME_CITY + " TEXT, " +
             Addresses.COLUMN_NAME_STREET + " TEXT, " +
             Addresses.COLUMN_NAME_STREET_NO + " TEXT, " +
             Addresses.COLUMN_NAME_MAP_SHEET + " TEXT)";
     public static final String SQL_DELETE_ADDRESSES =
             "DROP TABLE IF EXISTS " + Addresses.TABLE_NAME;
 
     public static final String SQL_CREATE_HOSPITAL_ADMISSION =
             "CREATE TABLE " + HospitalAdmission.TABLE_NAME + " (" +
                     HospitalAdmission._ID + " INTEGER PRIMARY KEY, " +
                     HospitalAdmission.COLUMN_NAME_DATE + " NUMERIC, " +
                     HospitalAdmission.COLUMN_NAME_TIME_FROM + " INTEGER, " +
                     HospitalAdmission.COLUMN_NAME_TIME_TO + " INTEGER, " +
                     HospitalAdmission.COLUMN_NAME_HOSPITAL_NAME + " TEXT)";
     public static final String SQL_DELETE_HOSPITAL_ADMISSION =
             "DROP TABLE IF EXISTS " + HospitalAdmission.TABLE_NAME;
 
     public static final String SQL_CREATE_DOCTOR_STANDBY =
             "CREATE TABLE " + DoctorStandby.TABLE_NAME + " (" +
                     DoctorStandby._ID + " INTEGER PRIMARY KEY, " +
                     DoctorStandby.COLUMN_NAME_DATE + " NUMERIC, " +
                     DoctorStandby.COLUMN_NAME_TIME_FROM + " INTEGER, " +
                     DoctorStandby.COLUMN_NAME_TIME_TO + " INTEGER, " +
                     DoctorStandby.COLUMN_NAME_DOCTOR_NAME + " TEXT)";
     public static final String SQL_DELETE_DOCTOR_STANDBY =
             "DROP TABLE IF EXISTS " + DoctorStandby.TABLE_NAME;
 }
