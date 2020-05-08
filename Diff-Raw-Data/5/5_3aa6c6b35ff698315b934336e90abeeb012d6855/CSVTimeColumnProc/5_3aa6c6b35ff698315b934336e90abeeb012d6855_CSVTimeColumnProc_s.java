 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import.prepare_parts.proc;
 
 import com.treasure_data.bulk_import.Config;
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 import com.treasure_data.bulk_import.prepare_parts.ExtStrftime;
 
 public class CSVTimeColumnProc extends AbstractCSVColumnProc {
 
     protected ExtStrftime timeFormat; // TODO should change simple time format
 
     public CSVTimeColumnProc(int index,
             ExtStrftime timeFormat,
             com.treasure_data.bulk_import.prepare_parts.FileWriter writer) {
         super(index, Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE, writer);
         this.timeFormat = timeFormat;
     }
 
     @Override
     public void executeKey() throws PreparePartsException {
         writer.writeString(Config.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
     }
 
     @Override
     public Object executeValue(Object value) throws PreparePartsException {
         // TODO refine!!!!
 
        Long v = null;
 
         if (value instanceof Long) {
             v = (Long) value;
         } else if (value instanceof String) {
             String sv = (String) value;
 
             try {
                 v = Long.parseLong(sv);
             } catch (NumberFormatException e) {
                 throw new PreparePartsException(String.format(
                         "'%s' could not be parsed as an Long", value));
             }
 
             if (v == null && timeFormat != null) {
                 v = timeFormat.getTime(sv);
            } else {
                return 0;
             }
         } else {
             final String actualClassName = value.getClass().getName();
             throw new PreparePartsException(String.format(
                     "the input value should be of type Long or String but is of type %s",
                     actualClassName));
         }
 
         writer.writeLong(v);
         return v;
     }
 
 }
