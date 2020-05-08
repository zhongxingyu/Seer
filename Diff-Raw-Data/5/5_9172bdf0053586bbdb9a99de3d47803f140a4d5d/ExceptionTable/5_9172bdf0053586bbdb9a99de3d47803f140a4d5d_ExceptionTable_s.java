 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
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
 package fr.jamgotchian.abcd.core.controlflow;
 
 import fr.jamgotchian.abcd.core.util.ConsoleUtil;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class ExceptionTable {
 
     public static class Entry {
 
         private final int tryStart;
 
         private final int tryEnd;
 
         private final int catchStart;
 
         private final String exceptionClassName;
 
         public Entry(int tryStart, int tryEnd, int catchStart, String exceptionClassName) {
             if (tryEnd <= tryStart) {
                 throw new IllegalArgumentException("tryEnd <= tryStart");
             }
 
             if (catchStart < tryEnd) {
                 throw new IllegalArgumentException("catchStart < tryEnd");
             }
 
             this.tryStart = tryStart;
             this.tryEnd = tryEnd;
             this.catchStart = catchStart;
             this.exceptionClassName = exceptionClassName;
         }
 
         public int getTryStart() {
             return tryStart;
         }
 
         public int getTryEnd() {
             return tryEnd;
         }
 
         public int getCatchStart() {
             return catchStart;
         }
 
         public String getExceptionClassName() {
             return exceptionClassName;
         }
     }
 
     private final List<Entry> entries = new ArrayList<ExceptionTable.Entry>();
 
     public void addEntry(int tryStart, int tryEnd, int catchStart, String exceptionClassName) {
         entries.add(new Entry(tryStart, tryEnd, catchStart, exceptionClassName));
     }
 
     public List<Entry> getEntries() {
         return Collections.unmodifiableList(entries);
     }
 
     public void print(StringBuilder builder) {
         int rowCount = entries.size() + 1;
         List<String> tryStartColumn = new ArrayList<String>(rowCount);
         List<String> tryEndColumn = new ArrayList<String>(rowCount);
         List<String> catchStartColumn = new ArrayList<String>(rowCount);
         List<String> typeColumn = new ArrayList<String>(rowCount);
         tryStartColumn.add("tryStart");
        tryEndColumn.add("endStart");
         catchStartColumn.add("catchStart");
         typeColumn.add("type");
 
         for (int i = 0; i < entries.size(); i++) {
             Entry entry = entries.get(i);
            tryStartColumn.add(Integer.toString(entry.getCatchStart()));
             tryEndColumn.add(Integer.toString(entry.getTryEnd()));
             catchStartColumn.add(Integer.toString(entry.getCatchStart()));
             typeColumn.add(entry.getExceptionClassName());
         }
         ConsoleUtil.printTable(builder, tryStartColumn, tryEndColumn,
                                catchStartColumn, typeColumn);
     }
 }
