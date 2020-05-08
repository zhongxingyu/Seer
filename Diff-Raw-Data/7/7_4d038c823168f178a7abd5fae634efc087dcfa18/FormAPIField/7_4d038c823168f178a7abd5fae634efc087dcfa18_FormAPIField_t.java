 /*
  * Copyright (C) 2012 Interactive Media Management
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
 package dk.i2m.drupal.core;
 
 import java.util.Set;
 import org.apache.http.NameValuePair;
 
 /**
  * Attach custom data fields to Drupal entities.
  *
  * @author <a href="mailto:rwa@i2m.dk">Raymond Wanyoike</a>
  */
 public interface FormAPIField<T> {
 
     /**
     * Add another field value. (Used by multivalued fields).
      *
      * @param field the field to add
      */
     public void add(T field);
 
     /**
      * Setup the HTTP form parameter(s) for this field.
      *
      * @param language the field language
     * @param nvps a {@link Set} to append the parameters
     * @return the appended {@link Set}
      */
     public Set<NameValuePair> setup(String language, Set<NameValuePair> nvps);
 }
