 /*
  *  Copyright (C) 2011 Carl Green
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.carlwithak.mpxg2.model.parameters;
 
 import java.lang.reflect.Field;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 /**
  *
  * @author Carl Green
  */
 public class GenericValue<T> implements Parameter, Cloneable {
     private final String name;
     private final String unit;
     private final T minValue;
     private final T maxValue;
     private T value;
 
     public GenericValue(final String name, final String unit, final T minValue, final T maxValue) {
         this.name = name;
         this.unit = unit;
         this.minValue = minValue;
         this.maxValue = maxValue;
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public String getUnit() {
         return unit;
     }
 
     public T getMinValue() {
         return minValue;
     }
 
     public T getMaxValue() {
         return maxValue;
     }
 
     public T getValue() {
         return value;
     }
 
     public void setValue(T value) {
         this.value = value;
     }
 
     @Override
     public boolean isSet() {
         return value != null;
     }
 
     /**
      * @return postive numbers prefixed with '+' and negative numbers prefixed with '-'.
      */
     private static String signInt(final int i) {
         int shifted = i > 32768 ? i - 65536 : i;
         return shifted > 0 ? "+" + Integer.toString(shifted) : Integer.toString(shifted);
     }
 
     @Override
     public String getDisplayString() {
         StringBuilder sb = new StringBuilder();
         if (getMinValue() instanceof Integer && ((Integer) getMinValue()) < 0) {
             sb.append(signInt((Integer) getValue()));
         } else {
             sb.append(getValue().toString());
         }
         sb.append(getUnit());
         return sb.toString();
     }
 
     public GenericValue<?> clone(final String newName) throws CloneNotSupportedException, NoSuchFieldException, IllegalAccessException {
         final GenericValue<?> clone = (GenericValue<?>) super.clone();
         final Field nameField = GenericValue.class.getDeclaredField("name");
         AccessController.doPrivileged(new PrivilegedAction<Void>() {
             @Override
             public Void run() {
                 nameField.setAccessible(true);
                 return null;
             }
         });
         nameField.set(clone, newName);
         clone.value = null;
         return clone;
     }
 
 }
