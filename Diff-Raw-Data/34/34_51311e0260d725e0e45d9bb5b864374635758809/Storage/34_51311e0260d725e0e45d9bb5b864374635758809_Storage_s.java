 //..Storage.java, uly, june2013..
 package org.aschyiel.nextboat;
 
 /*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
 import android.database.sqlite.SQLiteOpenHelper;
 
 /**
 * The Storage class handles talking to the android sqlite database for reading
 * and writing nextboat data, such as ferry departures.
 *
 * @see http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
 public class Storage extends SQLiteOpenHelper
 {
 
   //---------------------------------
   //
   // Properties.
   //
   //---------------------------------
 
   private static final int DATABASE_VERSION = 1;
 
  private static final String DATABASE_NAME = 'nextboat';
 
   //---------------------------------
   //
   // Public/Overriden Methods.
   //
   //---------------------------------
 
   public Storage( Context context )
   {
     super( context, DATABASE_NAME, null, DATABASE_VERSION );
   }
 
   @Override
   public void onCreate( SQLiteDatabase db )
   {
     db.execSQL( "create table departures ("+
         "id integer primary key,"+
         "route text,"+
         "is_weekday text,"+
         "am_pm text,"+
         "time text,"+
         "vessel text"+
         ")" );
   }
 
   @Override
   public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
   {
     db.execSQL( "drop table if exists departures" );
     onCreate( db );
   }
 
   /**
   * Write a bunch of ferries belonging to a single route to disc.
   *
   * @public
   * @param (ArrayList<FerryObject>) ferries is what we're serializing.
   * @param (String) is the ferry route.
   * @return void
   */
   public void saveRoute( ArrayList<FerryObject> ferries )
   {
     FerryObject sample = ferries.get(0);
     String route       = sample.getRoute();
     String isWeekday   = sample.getIsWeekday().toString();
 
     SQLiteDatabase db = this.getWritableDatabase();
 
     // Kill the previous rows representing this ferry route.
     db.execSQL( "delete from departures where "+
         "route='"+ route +"',"+
         "is_weekday='"+ isWeekday +"'" );
 
     for ( FerryObject ferry : ferries )
     {
       ContentValues row = new ContentValues();
      row.put( 'route',       route );
      row.put( 'is_weekday',  isWeekday );
      row.put( 'leaving',     ferry.getLeaving() );
      row.put( 'destination', ferry.getDestination() );
      row.put( 'time',        ferry.getTime() );
      row.put( 'am_pm',       ferry.getAmPm() );
      row.put( 'vessel',      ferry.getVessel() );
      db.insert( 'departures', null, row );
     }
 
     db.close();
   }
 
   /**
   * Read from the database and serialize the given ferry route.
   *
   * @public
   * @param route (String) is the route name we're reading in from.
   * @param isWeekday (Boolean); Are we looking for the weekday vs. weekend schedule?
   * @return ArrayList<FerryObject>
   * @see http://developer.android.com/reference/android/database/sqlite/SQLiteCursor.html
   * @see http://developer.android.com/reference/android/database/Cursor.html
   * @see http://stackoverflow.com/questions/160970/how-do-i-invoke-a-java-method-when-given-the-method-name-as-a-string
   */
   public ArrayList<FerryObject> readRoute( String route, Boolean isWeekday )
   {
     SQLiteDatabase db = this.getWritableDatabase();
     ArrayList<FerryObject> ferries = new ArrayList<FerryObject>();
 
     Cursor cursor = db.rawQuery( "select * from departures where "+
         "route='"+ route +"',"+
         "is_weekday='"+ isWeekday.toString() +"'",  null);
  
     if ( cursor.moveToFirst() ) {
 
         // Make it easy to lookup column-names via the cursor.
         String[] columns = cursor.getColumnNames();
         Map<String, Integer> columnIndices = new HashMap<String, Integer>();
         for ( int i = 0; i < columns.length; i++ )
         {
           String columnName = columns[ i ];
           columnIndices[ columnName ] = cursor.getColumnIndex( columnName );
         }
 
         //
         // Map out ferry setter properties based on the sqlite column-name.
         //
 
         Map<String, Method> setters = new HashMap<String, Method>();
         Class klass = ( new FerryObject() ).getClass();
        setters.put( 'route',       klass.getMethod( 'setRoute' ) );
        setters.put( 'is_weekday',  klass.getMethod( 'setIsWeekday' ) );
        setters.put( 'leaving',     klass.getMethod( 'setLeaving' ) );
        setters.put( 'destination', klass.getMethod( 'setDestination' ) );
        setters.put( 'time',        klass.getMethod( 'setTime' ) );
        setters.put( 'am_pm',       klass.getMethod( 'setAmPm' ) );
        setters.put( 'vessel',      klass.getMethod( 'setVessel' ) );
 
         do {
           FerryObject ferry = new FerryObject();
           for ( Map.Entry<String, Integer> it : columnIndices.entrySet() )
           {
             String columnName = it.getKey();
             int columnIndex = it.getValue();
             // Note: All values are normalized as strings on the database-side.
             setters.get( columnName ).invoke( ferry, cursor.getString( columnIndex ) );
           }
           ferries.add( ferry );
         } while (cursor.moveToNext());
     } 
 
     db.close();
     return ferries;
   }
 
   //---------------------------------
   //
   // Private Methods.
   //
   //---------------------------------
 }
