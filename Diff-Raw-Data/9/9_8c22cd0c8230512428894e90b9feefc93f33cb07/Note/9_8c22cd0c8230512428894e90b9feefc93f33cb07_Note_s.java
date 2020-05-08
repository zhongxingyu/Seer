 package name.apb.android.ponytime.java.db;
 
 /*
 * Pony Note
  * Copyright (C) 2013  Andre-Patrick Bubel <code@andre-bubel.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Counter information object saved to the database.
  * <p/>
  * TODO: Tests and License
  */
 @DatabaseTable
 public class Note implements Serializable {
 
     private static final long serialVersionUID = -125453245326L;
 
     public static final String LAST_CHANGED_DATE_COLUMN_NAME = "last_changed_date";
 
     @DatabaseField(generatedId = true)
     private Integer id;
 
     @DatabaseField(columnName = LAST_CHANGED_DATE_COLUMN_NAME)
     private Date lastChangeDate;
 
     @DatabaseField
     private String note;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Date getLastChangeDate() {
         return lastChangeDate;
     }
 
     public String getNote() {
         return note;
     }
 
     /**
      * This updates the note and adjusts the date.
      */
     public void setNote(String note) {
         this.note = note;
         this.lastChangeDate = new Date();
     }
 
     @Override
     public String toString() {
         return this.note;
     }
 }
 
