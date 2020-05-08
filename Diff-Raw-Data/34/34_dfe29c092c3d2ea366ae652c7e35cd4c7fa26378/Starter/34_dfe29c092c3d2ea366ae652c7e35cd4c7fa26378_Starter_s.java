 /*
  * Genealogy expert system.
  * Semestral work for the University of West Bohemia.
  * 
  * Written by Zdeněk Janeček, 2013
  * Share it freely under conditions of GNU GPL v3
  */
 
 package kiv.janecekz;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 public class Starter {
     private static BufferedReader br;
     private static NTree familyTree;
     private static Expert ex;
     private static String title;
 
     private static boolean runnning = true;
     private static final char ESC = 27;
 
     public static void dieWithError(String s) {
         System.out.println(s);
         try {
             if (br != null)
                 br.close();
         } catch (IOException e) {
             System.out.println("Ooops");
         }
         System.exit(-1);
     }
 
     private static void printMenu() {
         System.out.println("Otázky:");
         System.out.println("1)  kdo všechno je");
         System.out.println("2)  test vztahu");
         System.out.println("3)  v jakém vztahu jsou");
         System.out.println("4)  vypsat pokrevní linii");
         System.out.println("5)  vypsat strom\n");
     }
 
     private static String doAnswer() {
         String res = "";
         Node from;
         Node target;
         int[] r;
         boolean male;
 
         System.out.print(ESC + "[2J");
         System.out.print(ESC + "[1;1H");
         printMenu();
 
         int q = 1;
         try {
             String qstr = prompt(">> ");
             q = Integer.parseInt(qstr);
 
             if (qstr.charAt(0) == 'q') {
                 runnning = false;
                 return "Končím...";
             }
 
             switch (q) {
             case 1:
                 printTitle("Kdo všechno je ");
                 from = promptNode();
                 printTitle(title + from.getName());
                 r = promptRel();
                 male = r[1] == 0 ? false : true;
                 printTitle(title
                         + " : "
                         + ex.getRelationInfo(r[0])[male ? Expert.POS_MALENAME
                                 : Expert.POS_FEMALENAME]);
                 res = ex.findPersons(from, r[0], male).toString();
                 break;
             case 2:
                 printTitle("Je ");
                 from = promptNode();
                 printTitle(title + from.getName());
                 r = promptRel();
                 male = r[1] == 0 ? false : true;
                 printTitle(title
                         + " "
                         + ex.getRelationInfo(r[0])[male ? Expert.POS_MALENAME
                                 : Expert.POS_FEMALENAME]);
                 target = promptNode();
                 printTitle(title + " " + target.getName());
                res = ex.testRelation(from, target, r[0], male) ? "Pravda" : "Nepravda";
                 break;
             case 3:
                 printTitle("V jakém vztahu jsou ");
                 from = promptNode();
                 printTitle(title+from.getName()+" a ");
                 target = promptNode();
                 printTitle(title+target.getName());
                 res = ex.findPerson(from, target);
                 break;
             case 4:
                 printTitle("Pokrevní linie");
                 from = promptNode();
                 int deep = Integer.parseInt(prompt("Jak hluboko hledat? "));
                 res = ex.bloodline(from, deep).toString();
                 break;
             case 5:
                 printTitle("Rodinný strom");
                 familyTree.printTree();
                 break;
             default:
                 break;
             }
         } catch (NumberFormatException e) {
             dieWithError("(EE) Neplatné číslo");
         }
         return res;
     }
 
     private static String prompt(String prompt) {
         System.out.print(prompt);
         String res = null;
         try {
             res = br.readLine();
         } catch (IOException e) {
             dieWithError("(EE) chyba vstupu");
         }
         return res;
     }
 
     private static Node promptNode() {
         System.out.print(ESC + "[2;1H");
         System.out.print(ESC + "[J");
         System.out.flush();
 
         for (int i = 1; i < familyTree.size(); i++) {
             Node p = familyTree.getId(i);
             System.out.println(String.format(" (%d) %s", i, p.getName()));
         }
         Node res = null;
         try {
             System.out.println("Zadejte id osoby: ");
             int ch = Integer.parseInt(br.readLine());
             res = familyTree.getId(ch);
 
             if (res == null)
                 dieWithError("(EE) Neexistující osoba");
 
         } catch (IOException e) {
             dieWithError("(EE) chyba vstupu");
         }
         return res;
     }
 
     private static int[] promptRel() {
         System.out.print(ESC + "[2;1H");
         System.out.print(ESC + "[J");
         System.out.flush();
 
         int[] res = new int[2];
         String[] p;
 
         String[][] rel = ex.getRelations();
         for (int i = 0; i < rel.length; i++) {
             p = rel[i];
             String line = String.format(" (%s) %s, %s", p[Expert.POS_ID],
                     p[Expert.POS_MALENAME], p[Expert.POS_FEMALENAME]);
             System.out.println(line);
         }
 
         try {
             boolean found = false;
 
             do {
                 System.out.print("\nZadejte vztah: ");
                 String ch = br.readLine();
 
                 int male = 0;
 
                 for (int i = 0; i < rel.length; i++) {
                     p = rel[i];
                     if (ch.equals(p[Expert.POS_FEMALENAME])) {
                         res[0] = i;
                         res[1] = male;
                         found = true;
                         break;
                     }
                     male++;
                     if (ch.equals(p[Expert.POS_MALENAME])) {
                         res[0] = i;
                         res[1] = male;
                         found = true;
                         break;
                     }
                     male--;
                 }
             } while (found == false);
 
         } catch (IOException e) {
             dieWithError("(EE) chyba vstupu");
         }
         return res;
     }
 
     private static void printTitle(String s) {
         title = s;
         // saves cursor position
         System.out.print(ESC + "[s");
 
         // moves cursor to upper left corner
         System.out.print(ESC + "[1;1H");
 
         // clears line
         System.out.print(ESC + "[2K");
 
         System.out.print(title);
 
         // restores position
         System.out.print(ESC + "[u");
     }
 
     /**
      * For example [aa,bb,cc] returns {"aa", "bb", "cc"}
      * 
      * @param s
      *            String to tokenize
      * @return array with result
      */
     public static String[] getTokens(String s) {
         int spos = 0;
         int endPos = 0;
 
         LinkedList<String> list = new LinkedList<String>();
 
         for (int i = 1; i < s.length() - 1; i++) {
             spos = endPos + 1;
 
             for (; i < s.length() - 1; i++) {
                 if ((s.charAt(i) == ',')) {
                     break;
                 }
             }
 
             endPos = i;
 
             list.add(s.substring(spos, endPos).trim());
         }
 
         return list.toArray(new String[list.size()]);
     }
 
     public static void main(String[] args) {
         br = new BufferedReader(new InputStreamReader(System.in));
         familyTree = new NTree("familyData");
         ex = new Expert(familyTree, br);
 
         do {
             String r = doAnswer();
 
             if (r.length() > 0)
                 System.out.println("\n>> " + r);
        } while (runnning && prompt("Další otázku? a/n: ").charAt(0) == 'a');
 
         try {
             br.close();
         } catch (IOException e) {
             dieWithError("(EE) chyba při vypínání");
         }
     }
 }
