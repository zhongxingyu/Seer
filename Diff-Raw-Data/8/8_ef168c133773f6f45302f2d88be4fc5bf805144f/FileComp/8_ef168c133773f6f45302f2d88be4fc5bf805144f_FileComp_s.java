 package net.daboross.filecomp;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author daboross
  */
 public class FileComp {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         if (args.length < 2) {
             System.out.println("Not Enough Arguments");
             System.exit(0);
         }
         if (args.length > 2) {
             System.out.println("To Many Arguments");
             System.exit(0);
         }
         File file1 = new File(args[0]);
         File file2 = new File(args[1]);
         List<String> file1List = FileHandler.ReadFile(file1);
         List<String> file2List = FileHandler.ReadFile(file2);
         List<String> removeList = new ArrayList<>();
         List<String> addList = new ArrayList<>();
         for (String str : file2List) {
             if (!file1List.contains(str)) {
                 if (str.contains("deinstall")) {
                     removeList.add("2- | " + str.replaceAll("deinstall", "").replaceAll(" ", "").replaceAll("   ", ""));
                 } else if (str.contains("install")) {
                     addList.add("2+ | " + str.replaceAll("install", "").replaceAll(" ", "").replaceAll("   ", ""));
                 } else {
                     System.out.println("2? | " + str);
                 }
             }
         }
         if (!addList.isEmpty()) {
             addList.add("");
         }
         if (!removeList.isEmpty()) {
             removeList.add("");
         }
         for (String str : file1List) {
             if (!file2List.contains(str)) {
                 if (str.contains("deinstall")) {
                     removeList.add("1- | " + str.replaceAll("deinstall", "").replaceAll(" ", "").replaceAll("   ", ""));
                 } else if (str.contains("install")) {
                     addList.add("1+ | " + str.replaceAll("install", "").replaceAll(" ", "").replaceAll("   ", ""));
                 } else {
                     System.out.println("1? | " + str);
                 }
             }
         }
        if ("".equals(addList.get(addList.size() - 1))) {
             addList.remove(addList.size() - 1);
         }
        if ("".equals(removeList.get(removeList.size() - 1))) {
            addList.remove(removeList.size() - 1);
         }
         for (String str : addList) {
             System.out.println(str);
         }
         if (!addList.isEmpty() && !removeList.isEmpty()) {
             System.out.println();
         }
         for (String str : removeList) {
             System.out.println(str);
         }
     }
 }
