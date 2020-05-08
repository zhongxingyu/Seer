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
 
 package info.carlwithak.mpxg2.model;
 
 /**
  *
  * @author Carl Green
  */
 public class Knob implements DataObject {
     private GenericValue<Integer> value = new GenericValue<Integer>("Value", "", 0, 127);
    private GenericValue<Integer> low = new GenericValue<Integer>("Low", "", 0, 127);
    private GenericValue<Integer> high = new GenericValue<Integer>("High", "", 0, 127);
     private String name;
 
     @Override
     public Parameter getParameter(final int parameterIndex) {
         Parameter parameter;
         switch (parameterIndex) {
             case 0:
                 parameter = value;
                 break;
             case 1:
                 parameter = low;
                 break;
             case 2:
                 parameter = high;
                 break;
             default:
                 parameter = null;
         }
         return parameter;
     }
 
     public int getValue() {
         return value.getValue();
     }
 
     public void setValue(int value) {
         this.value.setValue(value);
     }
 
     public int getLow() {
         return low.getValue();
     }
 
     public void setLow(int low) {
         this.low.setValue(low);
     }
 
     public int getHigh() {
         return high.getValue();
     }
 
     public void setHigh(int high) {
         this.high.setValue(high);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 }
