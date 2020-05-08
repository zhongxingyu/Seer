 /*
  * Kajona Language File Editor Core
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 
 package de.mulchprod.kajona.languageeditor.core.textfile;
 
 import de.mulchprod.kajona.languageeditor.core.CoreBaseException;
 
 /**
  *
  * @author sidler
  */
 public class Textentry implements ITextentry, Comparable<Textentry> {
 
 
     private String readableKey = "";
     private String readableValue = "";
     private String fullEntry = "";
         
     private boolean isEditableEntry = true;
 
     public void generateKeyValuePairFromEntry(String entry) throws EntryNotSetException {
         if(entry == null)
             throw new EntryNotSetException();
         
         //fetch include statements
         if(entry.trim().startsWith("include")) {
             isEditableEntry = false;
             readableKey = entry;
             fullEntry = entry;
             return;
         }
         
         //row is set up like
         String longKey = entry.substring(0, entry.indexOf("=")+1);
         String longValue = entry.substring(entry.indexOf("=")+1);
 
         //set up the key
         readableKey = longKey.substring(longKey.indexOf('"')+1, longKey.lastIndexOf('"'));
 
         //set up the value
         String tempValue = longValue.trim();
         if(tempValue.startsWith("$") || tempValue.startsWith("array")) {
             readableValue = tempValue;
             isEditableEntry = false;
         }
         else {
             try {
                 readableValue = tempValue.substring(tempValue.indexOf('"')+1, tempValue.lastIndexOf('"'));
             } catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public String getEntryAsString(boolean addLinebreak) {
 
         //pass special row directly
         if(!isEditableEntry && !fullEntry.equals(""))
             return fullEntry;
             
         
         //only append the current entry if the value is != ""
        if(readableValue.length() == 0)
            return "";
         
         String newKey = "$lang[\""+readableKey+"\"]";
         String newValue = "";
 
         if(isEditableEntry)
             newValue = "\""+readableValue+"\";";
         else
             newValue = readableValue;
 
         while(newKey.length() < 40)
             newKey += " ";
 
 
         return newKey + " = " + newValue + (addLinebreak ? "\n" : "");
     }
 
     @Override
     public String toString() {
         return "  single entry. (key/value/fullentry): " + readableKey + "/" + readableValue + "/"+ fullEntry +"\n" ;
     }
 
     public String getReadableKey() {
         return readableKey;
     }
 
     public void setReadableKey(String readableKey) {
         this.readableKey = readableKey;
     }
 
     public String getReadableValue() {
         return readableValue;
     }
 
     public void setReadableValue(String readableValue) {
         readableValue = readableValue.replaceAll("\r\n", " ");
         readableValue = readableValue.replaceAll("\n", " ");
         this.readableValue = readableValue;
     }
 
     @Override
     public int compareTo(Textentry o) {
         return o.getReadableKey().compareTo(this.getReadableKey());
     }
 
     public boolean isIsEditableEntry() {
         return isEditableEntry;
     }
 
     public void setIsEditableEntry(boolean isEditableEntry) {
         this.isEditableEntry = isEditableEntry;
     }
     
     
     public class EntryNotChangedException extends CoreBaseException {
         public EntryNotChangedException(String s) { super(s); }
     }
 
     public class EntryNotSetException extends CoreBaseException {
         
     }
 }
