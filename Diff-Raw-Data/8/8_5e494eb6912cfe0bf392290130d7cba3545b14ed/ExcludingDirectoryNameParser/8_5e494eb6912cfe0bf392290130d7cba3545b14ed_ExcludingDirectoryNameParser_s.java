 package ru.sbertech;
 
 import java.io.File;
 import java.io.FileFilter;
 
 /**
  * Запрещает сканнирование определённой директории.
  * @author Dmitry Dobrynin
  *         Date: 10.11.11 time: 23:31
  */
 public class ExcludingDirectoryNameParser implements ParameterParser {
     public boolean parse(String parameter, Scanner scanner) {
         if (parameter.startsWith("-")) {
             final File f = new File(parameter.substring(1));
             if (f.exists() && f.isDirectory()) {
                 scanner.directoryFilter(new FileFilter() {
                     public boolean accept(File file) {
                        return !f.equals(file); // запретить обход директории
                     }
                 });
                 return true;
             }
         }
         return false;
     }
 }
