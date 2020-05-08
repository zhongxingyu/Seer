 /* 
  * Copyright (C) 2009 Vincent Behar
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ffck.members;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.provider.Contacts;
 import android.text.TextUtils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Model defining a "member".
  */
 public class Member {
 
     /*
     * Constants
      */
 
     /** Content URI used by the content provider */
     public static final Uri CONTENT_URI = Uri.parse("content://ffck.members/members");
 
     /** Unique identifier for a member (integer) */
     public static final String ID = BaseColumns._ID;
 
     /**
      * FFCK license code. 6 digits, but stored as a String because it may starts
      * with some '0'.
      */
     public static final String CODE = "code";
 
     /** first name of the member (String) */
     public static final String FIRST_NAME = "first_name";
 
     /** last name of the member (String) */
     public static final String LAST_NAME = "last_name";
 
     /** birth date, as a String, format dd/MM/yyyy */
     public static final String BIRTH_DATE = "birth_date";
 
     /**
      * gender, as a String, either {@link #GENDER_MALE} or
      * {@link #GENDER_FEMALE}
      */
     public static final String GENDER = "gender";
 
     /** address (String) */
     public static final String ADDRESS = "address";
 
     /** postal code, as a String (5 chars) */
     public static final String POSTAL_CODE = "postal_code";
 
     /** city (String) */
     public static final String CITY = "city";
 
     /** country (String) */
     public static final String COUNTRY = "country";
 
     /** home phone number, as a String */
     public static final String PHONE_HOME = "phone_home";
 
     /** other phone number, as a String */
     public static final String PHONE_OTHER = "phone_other";
 
     /** mobile phone number, as a String */
     public static final String PHONE_MOBILE = "phone_mobile";
 
     /** second mobile phone number, as a String */
     public static final String PHONE_MOBILE_2 = "phone_mobile_2";
 
     /** email (String) */
     public static final String EMAIL = "email";
 
     /** second email (String) */
     public static final String EMAIL_2 = "email_2";
 
     /** year of last FFCK license, as a String, format yyyy */
     public static final String LAST_LICENSE = "last_license";
 
     /** value of GENDER field if member is a male */
     public static final String GENDER_MALE = "M";
 
     /** value of GENDER field if member is a female */
     public static final String GENDER_FEMALE = "F";
 
     /** The default 'order by' for sorting lists */
     public static final String DEFAULT_ORDER_BY = LAST_NAME + " ASC";
 
     /*
     * Attributes
      */
 
     /** Date formatter (for the birth date) */
     private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 
     /**
      * ContentValues instance holding the member's data. Lazily filled with the
      * cursor's data (if any), or provided in the constructor.
      */
     private ContentValues values = new ContentValues();
 
     /**
      * Cursor instance that may contains some data for the member. Each column
      * will only be read once and then stored and retrieved from the
      * contentValues instance.
      */
     private Cursor cursor;
 
     /*
      * Constructors
      */
 
     /**
      * Build a new member instance with the data provided in the given
      * contentValues.
      * 
      * @param values member's data
      */
     public Member(ContentValues values) {
         if (values != null) {
             this.values = values;
         }
     }
 
     /**
      * Build a new member instance with the data provided in the given cursor.
      * Data will be lazily loaded from the cursor when asked, and then
      * stored/retrieved in a ContentValues instance.
      * 
      * @param cursor member's data
      */
     public Member(Cursor cursor) {
         this.cursor = cursor;
         if (cursor != null && cursor.isBeforeFirst()) {
             cursor.moveToFirst();
         }
     }
 
     /*
      * Business methods
      */
 
     /**
      * @return the URI representing this member
      */
     public Uri getUri() {
         return Uri.withAppendedPath(CONTENT_URI, getCode());
     }
 
     /**
      * Calculate the age (in years) of the members (based on its birth date)
      * 
      * @return age (in years)
      */
     public int calculateAge() {
         long ageInMilliSeconds = System.currentTimeMillis() - getBirthDate().getTime();
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(ageInMilliSeconds);
         return cal.get(Calendar.YEAR) - 1970;
     }
 
     /**
      * @return true if the member is a male, false otherwise
      */
     public boolean isMale() {
         return GENDER_MALE.equals(getGender());
     }
 
     /**
      * @return true if the member is a female, false otherwise
      */
     public boolean isFemale() {
         return GENDER_FEMALE.equals(getGender());
     }
 
     /**
      * @return fullName of the member (firstName + lastName)
      */
     public String getFullName() {
         return getFirstName() + " " + getLastName();
     }
 
     /**
      * @return full address of the member (address + postal code + city +
      *         optionally the country). The result will be multiline (separated
      *         by '\n').
      */
     public String getFullAddress() {
         StringBuilder fullAddress = new StringBuilder();
         fullAddress.append(getAddress()).append("\n");
         fullAddress.append(getPostalCode()).append(" ").append(getCity());
         if (!TextUtils.isEmpty(getCountry())) {
             fullAddress.append("\n").append(getCountry());
         }
         return fullAddress.toString();
     }
 
     /**
      * @return the contentValues instance that contains the member's data. If
      *         this member instance was build using a cursor, the contentValues
      *         will be pre-populated with the data from the cursor.
      */
     public ContentValues getValues() {
         if (cursor != null && cursor.getColumnCount() > values.size()) {
             // force a query on the cursor, to populate the contentValues
             String[] cursorColumnNames = cursor.getColumnNames();
             for (String columnName : cursorColumnNames) {
                 getAsString(columnName);
             }
         }
         return values;
     }
 
     /**
      * Add this member to the contacts. It adds : name, mobile number, home
      * number, email.
      * 
      * @param context
      */
     public void addToContacts(Context context) {
         ContentValues buffer = new ContentValues();
 
         // create a new contact with the name
         buffer.put(Contacts.PeopleColumns.NAME, getFullName());
         Uri contactUri = context.getContentResolver().insert(Contacts.People.CONTENT_URI, buffer);
 
         // add the phone numbers
         Uri phoneUri = Uri.withAppendedPath(contactUri, Contacts.People.Phones.CONTENT_DIRECTORY);
         if (!TextUtils.isEmpty(getPhoneMobile())) {
             buffer.clear();
             buffer.put(Contacts.PhonesColumns.TYPE, Contacts.PhonesColumns.TYPE_MOBILE);
             buffer.put(Contacts.PhonesColumns.NUMBER, getPhoneMobile());
             context.getContentResolver().insert(phoneUri, buffer);
         }
         if (!TextUtils.isEmpty(getPhoneHome())) {
             buffer.clear();
             buffer.put(Contacts.PhonesColumns.TYPE, Contacts.PhonesColumns.TYPE_HOME);
             buffer.put(Contacts.PhonesColumns.NUMBER, getPhoneHome());
             context.getContentResolver().insert(phoneUri, buffer);
         }
 
         // add the email
         if (!TextUtils.isEmpty(getEmail())) {
             Uri emailUri = Uri.withAppendedPath(contactUri,
                     Contacts.People.ContactMethods.CONTENT_DIRECTORY);
             buffer.clear();
             buffer.put(Contacts.ContactMethodsColumns.KIND, Contacts.KIND_EMAIL);
             buffer.put(Contacts.ContactMethodsColumns.DATA, getEmail());
             buffer.put(Contacts.ContactMethodsColumns.TYPE,
                     Contacts.ContactMethodsColumns.TYPE_HOME);
             context.getContentResolver().insert(emailUri, buffer);
         }
     }
 
     /*
      * Helper methods
      */
 
     /**
      * Retrieve the value of the given columnName. The value is either retrieved
      * from the contentValues instance, or from the cursor (and then stored in
      * the contentValues for later use).
      * 
      * @param columnName for which the value should be retrieved
      * @return value if available from the contentValues or the cursor, or null
      *         if not found.
      */
     private String getAsString(String columnName) {
         if (values.containsKey(columnName)) {
             return values.getAsString(columnName);
         }
         if (cursor != null) {
             int columnIndex = cursor.getColumnIndex(columnName);
             if (columnIndex != -1) {
                 String value = cursor.getString(columnIndex);
                 values.put(columnName, value);
                 return value;
             }
         }
         return null;
     }
 
     /*
      * Accessors
      */
 
     /**
      * @return the member's license code
      */
     public String getCode() {
         return getAsString(CODE);
     }
 
     /**
      * @return the member's first name
      */
     public String getFirstName() {
         return getAsString(FIRST_NAME);
     }
 
     /**
      * @return the member's last name
      */
     public String getLastName() {
         return getAsString(LAST_NAME);
     }
 
     /**
      * @return the member's birth date (as a String, format 'dd/MM/yyyy')
      */
     public String getBirthDateAsString() {
         return getAsString(BIRTH_DATE);
     }
 
     /**
      * @return the member's birth date (as a Date)
      */
     public Date getBirthDate() {
         try {
             return formatter.parse(getBirthDateAsString());
         } catch (ParseException e) {
             return null;
         }
     }
 
     /**
      * @return the member's gender (see {@link #GENDER_MALE} and
      *         {@link #GENDER_FEMALE})
      */
     public String getGender() {
         return getAsString(GENDER);
     }
 
     /**
      * @return the member's address
      */
     public String getAddress() {
         return getAsString(ADDRESS);
     }
 
     /**
      * @return the member's postal code
      */
     public String getPostalCode() {
         return getAsString(POSTAL_CODE);
     }
 
     /**
      * @return the member's city
      */
     public String getCity() {
         return getAsString(CITY);
     }
 
     /**
      * @return the member's country
      */
     public String getCountry() {
         return getAsString(COUNTRY);
     }
 
     /**
      * @return the member's home phone number
      */
     public String getPhoneHome() {
         return getAsString(PHONE_HOME);
     }
 
     /**
      * @return the member's other phone number
      */
     public String getPhoneOther() {
         return getAsString(PHONE_OTHER);
     }
 
     /**
      * @return the member's mobile phone number
      */
     public String getPhoneMobile() {
         return getAsString(PHONE_MOBILE);
     }
 
     /**
      * @return the member's secondary mobile phone number
      */
     public String getPhoneMobile2() {
         return getAsString(PHONE_MOBILE_2);
     }
 
     /**
      * @return the member's email
      */
     public String getEmail() {
         return getAsString(EMAIL);
     }
 
     /**
      * @return the member's secondary email
      */
     public String getEmail2() {
         return getAsString(EMAIL_2);
     }
 
     /**
      * @return the member's last license year (as String, format 'yyyy')
      */
     public String getLastLicense() {
         return getAsString(LAST_LICENSE);
     }
 
 }
