 /**
  * ********
  * Copyright © 2010-2012 Olanto Foundation Geneva
  *
  * This file is part of myCAT.
  *
  * myCAT is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * myCAT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with myCAT. If not, see <http://www.gnu.org/licenses/>.
  *
  *********
  */
 package org.olanto.TranslationText.server;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import org.olanto.TranslationText.client.*;
 import org.olanto.idxvli.ref.UploadedFile;
 import org.olanto.idxvli.server.IndexService_MyCat;
 import org.olanto.idxvli.server.QLResultNice;
 import org.olanto.idxvli.server.REFResultNice;
 import org.olanto.mapman.server.AlignBiText;
 import org.olanto.senseos.SenseOS;
 
 /**
  * implémentation des services du GUI
  */
 @SuppressWarnings("serial")
 public class TranslateServiceImpl extends RemoteServiceServlet implements TranslateService {
 
     // add usage of the implementation of the new mycatServer methods: order by and originalpath
     public static IndexService_MyCat is;
     private AlignBiText Align;
     public static String home = SenseOS.getMYCAT_HOME();
     public static Properties prop;
     public static ConstStringManager stringMan;
     public static String REGEX_BEFORE_TOKEN;
     public static String REGEX_AFTER_TOKEN;
     public static String REGEX_EXACT_BEFORE_TOKEN;
     public static String REGEX_EXACT_AFTER_TOKEN;
     public static GwtProp CONST = null;
     public static boolean RELOAD_PARAM_ON = true;
     public ArrayList<Character> charList = new ArrayList<>();
 
     @Override
     public String myMethod(String s) {
         // Do something interesting with 's' here on the server.
 
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             s += " / " + is.getInformation();
         } catch (Exception ex) {
             Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return "Server says: Alive";
     }
 
     GwtAlignBiText SetGwtAlignBiText(GwtSegDoc src, GwtSegDoc tgt, GwtIntMap map, String Q) {
         GwtAlignBiText result = new GwtAlignBiText();
         result.source = src;
         result.target = tgt;
         result.map = map;
         result.query = Q;
         return result;
 
     }
 
     GwtIntMap SetGwtIntMap(int[] from, int[] to) {
         GwtIntMap result = new GwtIntMap();
         result.from = from;
 //        System.out.println("-----------------FROM------------------ ");
 //        for (int i = 0; i < from.length; i++) {
 //            System.out.println("SRC phrase: " + i + " --> " + from[i]);
 //        }
         result.to = to;
 //        System.out.println("-----------------TO------------------ ");
 //        for (int i = 0; i < to.length; i++) {
 //            System.out.println("TGT phrase: " + i + " --> " + to[i]);
 //        }
         return result;
     }
 
     GwtSegDoc SetGwtSegDoc(int[][] lines, int nblines, String content, String uri, String lang) {
         GwtSegDoc result = new GwtSegDoc();
         result.positions = lines;
 //        for (int i = 0; i < lines.length; i++) {
 //            System.out.println("--------------------------");
 //            System.out.println("nombre de lignes dans la textarea de la phrase: " + i + " = " + lines[i][0]);
 //            System.out.println("position du curseur pour la phrase: " + i + " = " + lines[i][1]);
 //            System.out.println("correction pour la position du curseur pour IE de la phrase: " + i + " = " + lines[i][2]);
 //            System.out.println("nombre de lignes avant la phrase: " + i + " = " + lines[i][3]);
 //            System.out.println("nombre de phrases comportant une seule ligne jusqu'à la phrase: " + i + " = " + lines[i][4]);
 //        }
         result.nblines = nblines;
         result.content = content;
         result.uri = uri;
         result.lang = lang;
         return result;
     }
 
     @Override
     public GwtAlignBiText getContent(String file, String langS, String langT, String Query, int w, int h, Boolean remSpace) {
 //        System.out.println("calling the server getContent. File = " + file);
         Align = new AlignBiText(file, langS, langT, Query, w, h, remSpace);
         GwtSegDoc src = SetGwtSegDoc(Align.source.positions, Align.source.nblines, Align.source.content, Align.source.uri, Align.source.lang);
         GwtSegDoc tgt = SetGwtSegDoc(Align.target.positions, Align.target.nblines, Align.target.content, Align.target.uri, Align.target.lang);
         GwtIntMap map = SetGwtIntMap(Align.map.from, Align.map.to);
         return SetGwtAlignBiText(src, tgt, map, Align.query);
     }
 
     @Override
     public ArrayList<String> getDocumentList(String query, ArrayList<String> collections, boolean PATH_ON, int maxSize, String order, boolean exact, boolean number) {
         ArrayList<String> documents = new ArrayList<>();
         String longName, docName, listElem;
 //        System.out.println("Before calling the server for documents with the query: " + query);
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             QLResultNice res;
 //            Timer t1 = new Timer("------------- " + query);
             res = is.evalQLNice(query, 0, maxSize, order, exact, number);
             if (((res != null)) && (res.docname != null)) {
 //                System.out.println("List of documents retrieved");
                 if (!collections.isEmpty()) {
 //                    System.out.println("____________________Triés par collections & "+order+"____________________");
                     for (int s = 0; s < collections.size(); s++) {
 //                        System.out.println("Collection: " + collections.get(s));
                         for (int i = 0; i < res.docname.length; i++) {//res.result or res.docname
                             if (res.docname[i] != null) {
                                 int lastslash = res.docname[i].lastIndexOf("/") - 2;
                                 longName = res.docname[i].substring(lastslash);
                                 if (longName.contains(collections.get(s))) {
 //                                System.out.println("Docname: " + res.docname[i]);
                                     docName = getDocListElement(longName.substring(3), PATH_ON);
                                     listElem = docName + "¦]" + "[¦" + longName;
                                     if (!documents.contains(listElem)) {
                                         documents.add(listElem);
                                     }
                                 }
                             }
                         }
                     }
                 } else {
 //                    System.out.println("____________________ Sorted by " + order + "____________________");
                     for (int i = 0; i < res.docname.length; i++) {//res.result or res.docname
                         if (res.docname[i] != null) {
                             int lastslash = res.docname[i].lastIndexOf("/") - 2;
                             longName = res.docname[i].substring(lastslash);
                             docName = getDocListElement(longName.substring(3), PATH_ON);
                             listElem = docName + "¦]" + "[¦" + longName;
                             documents.add(listElem);
 //                        System.out.println("Docname: " + res.docname[i]);
                         }
                     }
                 }
             }
 //            t1.stop();
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
         return documents;
     }
 
     private CollectionTree CollToGWTColl(CollTree s) {
         if (s != null) {
             CollectionTree s1 = new CollectionTree();
 //            System.out.println("adding collectionTree");
             s1.id = s.id;
             s1.idUp = s.idUp;
             s1.currFolder = s.currFolder;
             s1.upperFolder = s.upperFolder;
             s1.level = s.level;
             s1.isEndOfCollection = s.isEndOfCollection;
             s1.childNumber = s.debug();
             s1.ChildFolders = new ArrayList<>();
 
             if (!s.ChildFolders.isEmpty()) {
                 for (CollTree a : s.ChildFolders) {
 //                    System.out.println("adding new map for collectionTree: " + a.id);
                     s1.ChildFolders.add(CollToGWTColl(a));
                 }
             }
             return s1;
         } else {
             return null;
         }
     }
 
     @Override
     public CollectionTree SetCollection() {
 
         String[] list = getCollectionList();
         CollTree s = new CollTree("", 0, "", "1", false);
         s.ChildFolders = new ArrayList<>();
         for (String word : list) {
             int start = word.indexOf(".") + 1;
             word = word.substring(start);
             boolean end = false;
             String[] folders = word.split("\\¦");
             String Upper;
             String Current;
             CollTree a;
             if (folders.length == 1) {
                 end = true;
             }
             if (!s.SubcontainsFolder(folders[0], 0, "")) {
                 a = new CollTree(folders[0], 0, "", "", end);
                 s.ChildFolders.add(a);
             } else {
                 a = s.Subcontains(folders[0], 0, "¦" + folders[0]);
             }
             String pa = "¦" + folders[0];
             Upper = folders[0];
             for (int i = 1; i < folders.length; i++) {
                 end = false;
 //                System.out.println("\n\nadding new entry \n\n");
                 Current = folders[i];
                 a = a.getCollectionSubTree(Upper, i - 1, pa);
                 if (i == (folders.length - 1)) {
                     end = true;
                 }
                 addFolder(Upper, Current, i, a, pa, end);
                 Upper = Current;
                 pa += "¦" + Current;
             }
         }
 
         CollectionTree s1 = CollToGWTColl(s);
 //        System.out.println("*** Debug tree contents ***");
 //        System.out.println(s.currFolder + " (level " + s.level + ")");
 //        int size = s.debug();
 //        System.out.println("*** End debug: size: " + size);
 
         return s1;
     }
 
     //add folders to the tree
     static void addFolder(String Upper, String current, int lev, CollTree temp, String upperId, boolean end) {
 //        System.out.println("*** addFolder ***");
 //        System.out.println("Adding folder " + Upper + "/" + current + " ...");
 
         if ((!temp.SubcontainsFolder(current, lev, upperId)) && (temp.level == (lev - 1))) {
 //            System.out.println("adding '" + current + "' at level " + (temp.level + 1));
             CollTree a = new CollTree(current, temp.level + 1, temp.id, temp.currFolder, end);
             temp.ChildFolders.add(a);
 //            System.out.println("Added Successfully '" + a.currFolder + "' at level " + a.level);
 //            System.out.println("*** Debug tree contents");
 //            System.out.println(temp.currFolder + " (level " + temp.level + ")");
 //            temp.debug();
 //            System.out.println("*** End debug");
 
         }
 
     }
 
     private String[] getCollectionList() {
         String[] Coll = null;
 
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             Coll = is.getDictionnary("COLLECTION.").result;
 //            System.out.println("succeded getting collections");
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 //        for (int i = 0; i < Coll.length; i++) {
 //            System.out.println(Coll[i]);
 //        }
         return Coll;
     }
 
     private String getDocListElement(String docListItem, boolean PATH_ON) {
         String documentName;
         String collectionPath;
 
         int i = docListItem.lastIndexOf("¦") + 1;
         int j = docListItem.length();
 
         // Strict name of the document without any path indication
         documentName = docListItem.substring(i, j - 4);
         docListItem = docListItem.substring(0, i);
         // Replace the highens by the slash in the rest of the path: DocName-Path
         if ((PATH_ON) && (docListItem.contains("¦"))) {
             collectionPath = docListItem.replace("¦", "/");
 //            System.out.println(collectionPath);
             return documentName + "-" + collectionPath;
         } else {
             return documentName;
         }
     }
 
     @Override
     public ArrayList<String> getStopWords() {
         ArrayList<String> stopWords = new ArrayList<>();
 
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             String[] stpwd = is.getStopWords();
             stopWords.addAll(Arrays.asList(stpwd));
 //            System.out.println("succeded getting stop words ");
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return stopWords;
     }
 
     @Override
     public String[] getCorpusLanguages() {
         String[] languages = null;
 
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             languages = is.getCorpusLanguages();
 //            System.out.println("succeded getting langues");
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return languages;
     }
 
     @Override
     public int[][] getQueryWordsPos(int[][] positions, String content, ArrayList<String> Query, int queryLn, boolean exact) {
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosSt = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
         ArrayList<Integer> startPos = new ArrayList<>();
         ArrayList<Integer> lastPos = new ArrayList<>();
         int begin, end;
         String sentence, curHit, regex;
         Pattern p;
         Matcher m;
         boolean allfound;
         if (!Query.isEmpty()) {
 //        System.out.println("Query: " + Query.size());
             for (int i = 0; i < positions.length; i++) {
                 allfound = true;
                 begin = positions[i][1];
                 if (i == (positions.length - 1)) {
                     end = content.length();
                 } else {
                     end = positions[i + 1][1] + 1;
                 }
                 sentence = content.substring(begin, end);
 
 //            System.out.println("looking into sentence # " + i);
                 int j = 0, start, len;
                 startPos.clear();
                 lastPos.clear();
                 while ((allfound) && (j < Query.size())) {
 //                System.out.println("Looking for hit: " + curHit);
 
                     if (exact) {
                         curHit = Query.get(j);
                         if (hasBothBorders(curHit)) {
                             regex = Pattern.quote(curHit);
                         } else if (hasFirstBorder(curHit)) {
                             regex = Pattern.quote(curHit) + REGEX_EXACT_AFTER_TOKEN;
                         } else if (hasLastBorder(curHit)) {
                             regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(curHit);
                         } else {
                             regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(curHit) + REGEX_EXACT_AFTER_TOKEN;
                         }
                         p = Pattern.compile(regex);
                     } else {
                         curHit = removeBorders(Query.get(j));
                         regex = REGEX_BEFORE_TOKEN + Pattern.quote(curHit) + REGEX_AFTER_TOKEN;
                         p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                     }
                     len = curHit.length();
 
                     m = p.matcher(sentence);
                     if (m.find()) {
                         start = m.start();
                         if (Query.size() > 1) {
                             if (j == 0) {
 //                            System.out.println("start found at : " + start);
                                 startPos.add(start);
                                 while (m.find()) {
                                     start = m.start();
                                     startPos.add(start);
 //                                System.out.println("Start found at : " + start);
                                 }
                             }
                             if (j == Query.size() - 1) {
 //                            System.out.println("last found at : " + start);
                                 lastPos.add(start + len);
                                 while (m.find()) {
                                     start = m.start();
                                     lastPos.add(start + len);
 //                                System.out.println("last found at : " + start);
                                 }
                             }
                         } else {
                             startPos.add(start);
 //                        System.out.println("Start found at : " + start);
                             lastPos.add(start + len);
 //                        System.out.println("Last found at : " + start);
                         }
                     } else {
                         allfound = false;
                     }
                     j++;
                 }
                 if (allfound) {
                     boolean done = false;
                     int s = 0, k = 0, r = 0;
                     while ((!done) && (s < lastPos.size())) {
                         k = lastPos.get(s);
                         for (int n = 0; n < startPos.size(); n++) {
                             r = startPos.get(n);
                             if (((k - r) <= queryLn) && ((k - r) >= 0)) {
                                 done = true;
                                 break;
                             }
                         }
                         s++;
                     }
                     if (done) {
                         Pos.add(i);
                         PosSt.add(r);
                         PosLn.add(k);
                     }
                 }
             }
         }
 //        for (int s = 0; s < Pos.size(); s++) {
 //            System.out.println("Line: " + Pos.get(s));
 //        }
 //        System.out.println(" All Found Occurrences# 1: " + Pos.size());
         return getThreePositions(Pos, PosSt, PosLn);
     }
 
     private int[][] getThreePositions(ArrayList<Integer> Pos, ArrayList<Integer> PosSt, ArrayList<Integer> PosLn) {
         int[][] posit;
         int pos, ln, len;
         if (!Pos.isEmpty()) {
             posit = new int[Pos.size()][3];
             for (int i = 0; i < Pos.size(); i++) {
                 pos = Pos.get(i);
                 ln = PosSt.get(i);
                 ln = (ln > 0) ? ln + 1 : ln;
                 len = PosLn.get(i);
 
                 posit[i][0] = pos; // index de la ligne qui contient le mot
                 posit[i][1] = ln; // index du mot dans la phrase
                 posit[i][2] = len - ln + 1; // longueur à highlighter
             }
 //            System.out.println(" All Found Occurrences# 2: " + posit.length);
 //            for (int ls = 0; ls < posit.length; ls++) {
 //                System.out.println("Line: " + posit[ls][0] + " Start: " + posit[ls][1]);
 //            }
         } else {
             posit = new int[1][1];
             posit[0][0] = -1;
         }
         return posit;
     }
 
     @Override
     public String getOriginalUrl(String docName) {
         int lastslash = docName.lastIndexOf("/") - 2;
         docName = docName.substring(lastslash);
         String url = "";
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             url = is.getOriginalUrl(docName);
             System.out.println("URL ORiginal: " + url);
         } catch (Exception ex) {
             Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return url;
     }
 
     public GwtRef ReftoGwtRef(REFResultNice ref) {
         GwtRef refL = new GwtRef();
         refL.DOC_REF_SEPARATOR = REFResultNice.DOC_REF_SEPARATOR;
         refL.htmlref = ref.htmlref;
         refL.listofref = ref.listofref;
         refL.nbref = ref.nbref;
         refL.reftext = ref.reftext;
         return refL;
     }
 
     @Override
     public GwtRef getHtmlRef(String Content, String fileName, int minCons, String langS, String LangT, ArrayList<String> collections, String QDFileExtension) {
         String ref;
         GwtRef gref = null;
         String[] co;
 //        System.out.println("uploaded file:" + fileName);
 //        System.out.println("Content:" + Content);
         if (fileName.contains(QDFileExtension)) {
             gref = html2GwtRef(Content);
         } else {
             if (is == null) {
                 is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
             }
             UploadedFile up = new UploadedFile(Content, fileName);
 //        System.out.println(up.getFileName() + "\n" + up.getContentString());
             try {
 //                Timer t1 = new Timer("-------------  ref ");
 
                 co = getCollections(collections);
 
 //            System.out.println("calling references service: " + is.getInformation());
                 ref = is.getHtmlReferences(up, minCons, langS, LangT, co);
 //                t1.stop();
                 if (ref != null) {
 //                    System.out.println(ref);
                     gref = html2GwtRef(ref);
                 }
             } catch (RemoteException ex) {
                 Logger.getLogger(TranslateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return gref;
     }
 
     private String[] getCollections(ArrayList<String> collections) {
         if ((collections != null) && (!collections.isEmpty())) {
             String[] selectedColls = new String[collections.size()];
             for (int k = 0; k < collections.size(); k++) {
                 selectedColls[k] = "COLLECTION." + collections.get(k);
             }
             return selectedColls;
         } else {
             return null;
         }
     }
 
     private ArrayList<String> GetSubList(ArrayList<String> list, int index) {
         ArrayList<String> res = new ArrayList<>();
         for (int i = 0; i < index; i++) {
             res.add(list.get(i));
         }
         return res;
     }
 
     @Override
     public int[][] getRefWordsPos(String content, ArrayList<String> query, int queryLn, float reFactor, int minRefLn, boolean exact) {
         ArrayList<String> Query;
         String regex;
         int refLength = queryLn;
         if (!exact) {
             refLength = (int) (((reFactor * queryLn) > minRefLn) ? (reFactor * queryLn) : minRefLn);
         }
         if (query.size() > 1000) {
             System.out.println("word list bigger than 1000, looking or the first 1000 words");
             Query = GetSubList(query, 1000);
         } else {
             Query = query;
         }
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
         ArrayList<Integer> startPos = new ArrayList<>();
         ArrayList<Integer> lastPos = new ArrayList<>();
         String first, last;
         Pattern p;
         Matcher m;
         startPos.clear();
         lastPos.clear();
         if (!Query.isEmpty()) {
             if (exact) {
                 String curHit = Query.get(0);
                 if (hasBothBorders(curHit)) {
                     regex = Pattern.quote(curHit);
                 } else if (hasFirstBorder(curHit)) {
                     regex = Pattern.quote(curHit) + REGEX_EXACT_AFTER_TOKEN;
                 } else if (hasLastBorder(curHit)) {
                     regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(curHit);
                 } else {
                     regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(curHit) + REGEX_EXACT_AFTER_TOKEN;
                 }
                 p = Pattern.compile(regex);
                 m = p.matcher(content);
                 if (m.find()) {
                     Pos.add(m.start());
                     PosLn.add(Query.get(0).length());
                     while (m.find()) {
                         Pos.add(m.start());
                         PosLn.add(Query.get(0).length());
                     }
                 }
 
             } else {
                 first = removeBorders(Query.get(0));
                 last = removeBorders(Query.get(Query.size() - 1));
                 regex = REGEX_BEFORE_TOKEN + Pattern.quote(first) + REGEX_AFTER_TOKEN;
                 p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                 m = p.matcher(content);
                 if (m.find()) {
 //            System.out.println("start found at : " + m.start());
                     startPos.add(m.start());
                     while (m.find()) {
                         startPos.add(m.start());
 //                System.out.println("Start found at : " + m.start());
                     }
                 }
                 regex = REGEX_BEFORE_TOKEN + Pattern.quote(last) + REGEX_AFTER_TOKEN;
                 p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                 m = p.matcher(content);
 
                 if (m.find()) {
 //            System.out.println("last found at : " + m.start());
                     lastPos.add(m.start() + last.length());
                     while (m.find()) {
                         lastPos.add(m.start() + last.length());
 //                System.out.println("last found at : " + m.start());
                     }
                 }
 
                 int startp, lastp;
                 for (int i = 0; i < startPos.size(); i++) {
                     startp = startPos.get(i);
                     for (int k = 0; k < lastPos.size(); k++) {
                         lastp = lastPos.get(k);
                         if (((lastp - startp) >= 0) && ((lastp - startp) <= refLength)) {
                             if (getAllWords(content.substring(startp, lastp + 1), Query)) {
                                 Pos.add(startp);
                                 PosLn.add(lastp - startp);
                             }
                         }
                     }
                 }
             }
         }
 
         for (int i = 0; i < Pos.size(); i++) {
             System.out.println("Positions found in Line: " + Pos.get(i));
         }
         return getClosePositions(Pos, PosLn);
     }
 
     public boolean getAllWordsNonStrict(String content, ArrayList<String> Query) {
         String curHit;
         boolean allfound = true;
         int j = 1;
         while ((allfound) && (j < Query.size() - 1)) {
             curHit = Query.get(j);
 //            System.out.println("test if: " + curHit + " is in :" + content);
             if (content.contains(curHit)) {
                 j++;
             } else {
                 allfound = false;
             }
         }
 //        System.out.println("All found : " + allfound);
         return allfound;
     }
 
     public boolean getAllWords(String content, ArrayList<String> Query) {
         String curHit;
         for (int j = 1; j < Query.size() - 1; j++) {
             curHit = removeBorders(Query.get(j));
 //            System.out.println("test if: " + curHit + " is in :" + content);
             String regex = REGEX_BEFORE_TOKEN + Pattern.quote(curHit) + REGEX_AFTER_TOKEN;
             Pattern p = Pattern.compile(regex);
             Matcher m = p.matcher(content);
             if (!m.find()) {
 //                System.out.println("Not Found");
                 return false;
             }
         }
 //        System.out.println("All found");
         return true;
     }
 
     @Override
     public int[][] getQueryWordsPosAO(int[][] positions, String content, ArrayList<String> Query, int queryLn) {
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosSt = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
 //        int[] totocc = new int[Query.size()];
 //        for (int i = 0; i < totocc.length; i++) {
 //            totocc[i] = 0;
 //        }
         int begin, end, j;
         String sentence, hit, regex;
         Pattern p;
         Matcher m;
         for (int i = 0; i < positions.length; i++) {
             begin = positions[i][1];
             if (i == (positions.length - 1)) {
                 end = content.length();
             } else {
                 end = positions[i + 1][1] + 1;
             }
             sentence = content.substring(begin, end);
             j = 0;
             while (j < Query.size()) {
                 hit = removeBorders(Query.get(j));
 //                System.out.println("looking for: " + hit);
                 regex = REGEX_BEFORE_TOKEN + Pattern.quote(hit) + REGEX_AFTER_TOKEN;
                 p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                 m = p.matcher(sentence);
                 while (m.find()) {
 //                    totocc[j]++;
                     Pos.add(i); /* + "¦" + m.start() + "¦" + hit.length() + "¦" + j + "¦" + totocc[j]*/
                     PosSt.add(m.start());
                     PosLn.add(hit.length());
                 }
                 j++;
                 m.reset();
             }
         }
         return getPositionsAO(Pos, PosSt, PosLn);
     }
 
     private int[][] getPositionsAO_Old(ArrayList<String> Pos/*, int[] totocc*/) {
         int[][] posit;
         int i, k, pos, ln, j, len, l, m, idx, occ, tocc, before;
         String curr, curr0;
         if (!Pos.isEmpty()) {
             posit = new int[Pos.size()][3];
             for (int s = 0; s < Pos.size(); s++) {
                 curr = Pos.get(s);
                 curr0 = curr;
 
                 i = curr.indexOf("¦");
 
                 before = curr0.substring(0, i).length() + 1;
                 curr = curr0.substring(i + 1);
                 j = before + curr.indexOf("¦");
                 k = curr0.length();
 
 //                before = curr0.substring(0, j).length() + 1;
 //                curr = curr0.substring(j + 1);
 //                k = before + curr.indexOf("¦");
 //                
 //                before = curr0.substring(0, k).length() + 1;
 //                curr = curr0.substring(k + 1);
 //                l = before + curr.indexOf("¦");
 //                
 //                m = curr0.length();
 
                 pos = Integer.parseInt(curr0.substring(0, i));
                 ln = Integer.parseInt(curr0.substring(i + 1, j));
                 ln = (ln > 0) ? ln + 1 : ln;
                 len = Integer.parseInt(curr0.substring(j + 1, k));
 //                idx = Integer.parseInt(curr0.substring(k + 1, l));
 //                occ = Integer.parseInt(curr0.substring(l + 1, m));
 //                tocc = totocc[idx];
 
 
                 posit[s][0] = pos; // index de la ligne qui contient le mot
                 posit[s][1] = ln; // index du mot dans la phrase
                 posit[s][2] = len; // longueur à highlighter
 //                posit[s][3] = idx; // index du mot actuel dans la list de requêtes donnée
 //                posit[s][4] = occ; // occurrence actuelle du mot
 //                posit[s][5] = tocc; // total des occurrences du mot actuel
             }
         } else {
             posit = new int[1][1];
             posit[0][0] = -1;
         }
         return posit;
     }
 
     private int[][] getPositionsAO(ArrayList<Integer> Pos, ArrayList<Integer> PosSt, ArrayList<Integer> PosLn) {
         int[][] posit;
         int pos, ln, len;
         if (!Pos.isEmpty()) {
             posit = new int[Pos.size()][3];
             for (int s = 0; s < Pos.size(); s++) {
                 pos = Pos.get(s);
                 ln = PosSt.get(s);
                 ln = (ln > 0) ? ln + 1 : ln;
                 len = PosLn.get(s);
                 posit[s][0] = pos; // index de la ligne qui contient le mot
                 posit[s][1] = ln; // index du mot dans la phrase
                 posit[s][2] = len; // longueur à highlighter
             }
         } else {
             posit = new int[1][1];
             posit[0][0] = -1;
         }
         return posit;
     }
 
     @Override
     public int[][] getHitPosExactClose(String content, ArrayList<String> Query, float reFactor, int sepNumber, int avgTokenLn) {
         int refLength = (int) (reFactor * sepNumber * avgTokenLn);
         int startp, lastp;
 //        System.out.println("Searching for close on a window of: " + refLength);
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
 
         ArrayList<Integer> startPos = new ArrayList<>();
         ArrayList<Integer> lastPos = new ArrayList<>();
         String first, last, regex;
         Pattern p;
         Matcher m;
         startPos.clear();
         lastPos.clear();
 
         first = Query.get(0);
         last = Query.get(Query.size() - 1);
 //        System.out.println("First: " + first);
 //        System.out.println("Last: " + last);
 //        System.out.println("Exact CLOSE: " + Pattern.quote(first));
         if (hasBothBorders(first)) {
             regex = Pattern.quote(first);
         } else if (hasFirstBorder(first)) {
             regex = Pattern.quote(first) + REGEX_EXACT_AFTER_TOKEN;
         } else if (hasLastBorder(first)) {
             regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(first);
         } else {
             regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(first) + REGEX_EXACT_AFTER_TOKEN;
         }
         p = Pattern.compile(regex);
         m = p.matcher(content);
 
         if (m.find()) {
 //            System.out.println("start found at : " + m.start());
             startPos.add(m.start());
             while (m.find()) {
                 startPos.add(m.start());
 //                System.out.println("Start found at : " + m.start());
             }
         }
 //        System.out.println("Exact CLOSE: " + Pattern.quote(last));
         if (hasBothBorders(last)) {
             regex = Pattern.quote(last);
         } else if (hasFirstBorder(last)) {
             regex = Pattern.quote(last) + REGEX_EXACT_AFTER_TOKEN;
         } else if (hasLastBorder(last)) {
             regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(last);
         } else {
             regex = REGEX_EXACT_BEFORE_TOKEN + Pattern.quote(last) + REGEX_EXACT_AFTER_TOKEN;
         }
         p = Pattern.compile(regex);
         m = p.matcher(content);
 
         if (m.find()) {
 //            System.out.println("last found at : " + m.start());
             lastPos.add(m.start());
             while (m.find()) {
                 lastPos.add(m.start());
 //                System.out.println("last found at : " + m.start());
             }
         }
         for (int i = 0; i < startPos.size(); i++) {
             startp = startPos.get(i);
             for (int k = 0; k < lastPos.size(); k++) {
                 lastp = lastPos.get(k);
                 if (((lastp > startp) && ((lastp - (startp + first.length())) <= refLength))
                         || ((startp > lastp) && ((startp - (lastp + last.length())) <= refLength))) {
                     if (lastp > startp) {
                         Pos.add(startp);
                         PosLn.add(lastp + last.length() - startp);
                     } else {
                         Pos.add(lastp);
                         PosLn.add(startp + first.length() - lastp);
                     }
 
                 }
             }
         }
 //        for (int i = 0; i < Pos.size(); i++) {
 //            System.out.println("Positions found in Line: " + Pos.get(i));
 //        }
         return getClosePositions(Pos, PosLn);
     }
 
     private int[][] getClosePositions(ArrayList<Integer> Pos, ArrayList<Integer> PosLn) {
         int[][] posit;
         int l, r;
         if (!Pos.isEmpty()) {
             posit = new int[Pos.size()][2];
             for (int i = 0; i < Pos.size(); i++) {
                 l = Pos.get(i);
                 l = (l > 0) ? l + 1 : l;
                 r = PosLn.get(i);
                 posit[i][0] = l;
                 if (l == 0) {
                     posit[i][1] = r + 1;
                 } else {
                     posit[i][1] = r;
                 }
             }
 //            System.out.println(" All Found Occurrences# : " + posit.length);
 //            for (int ls = 0; ls < posit.length; ls++) {
 //                System.out.println("Line: " + posit[ls][0] + " Start: " + posit[ls][1]);
 //            }
         } else {
             posit = new int[1][1];
             posit[0][0] = -1;
         }
         return posit;
     }
 
     @Override
     public int[][] getHitPosNearCR(String content, ArrayList<String> Query, int queryLn, float reFactor, int sepNumber, int avgTokenLn) {
         int refLength = (int) (reFactor * (queryLn + sepNumber * avgTokenLn));
         int startp, lastp;
 //        System.out.println("Searching for near on a window of: " + refLength);
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
         ArrayList<Integer> startPos = new ArrayList<>();
         ArrayList<Integer> lastPos = new ArrayList<>();
         String first, last, regex;
         Pattern p;
         Matcher m;
         startPos.clear();
         lastPos.clear();
 
         first = removeBorders(Query.get(0));
         last = removeBorders(Query.get(Query.size() - 1));
         System.out.println("First: " + first);
         System.out.println("Last: " + last);
         regex = REGEX_BEFORE_TOKEN + Pattern.quote(first) + REGEX_AFTER_TOKEN;
         p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
         m = p.matcher(content);
         if (m.find()) {
 //            System.out.println("start found at : " + m.start());
             startPos.add(m.start());
             while (m.find()) {
                 startPos.add(m.start());
 //                System.out.println("Start found at : " + m.start());
             }
         }
         regex = REGEX_BEFORE_TOKEN + Pattern.quote(last) + REGEX_AFTER_TOKEN;
         p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
         m = p.matcher(content);
         if (m.find()) {
 //            System.out.println("last found at : " + m.start());
             lastPos.add(m.start());
             while (m.find()) {
                 lastPos.add(m.start());
 //                System.out.println("last found at : " + m.start());
             }
         }
         for (int i = 0; i < startPos.size(); i++) {
             startp = startPos.get(i);
             for (int k = 0; k < lastPos.size(); k++) {
                 lastp = lastPos.get(k);
                 if ((Math.abs(lastp - startp) >= (queryLn - 6)) && (Math.abs(lastp - startp) <= refLength)) {
                     if (lastp > startp) {
                         if (getAllWords(content.substring(startp, lastp + last.length() + 1), Query)) {
                             Pos.add(startp);
                             PosLn.add(lastp + last.length() - startp);
                         }
                     } else {
                         if (getAllWords(content.substring(lastp, startp + first.length() + 1), Query)) {
                             Pos.add(lastp);
                             PosLn.add(startp + first.length() - lastp);
                         }
                     }
                 }
             }
         }
 //        for (int i = 0; i < Pos.size(); i++) {
 //            System.out.println("Positions found in Line: " + Pos.get(i));
 //        }
         return getClosePositions(Pos, PosLn);
     }
 
     @Override
     public int[][] getHitPosNear(int[][] positions, String content, ArrayList<String> Query, int queryLn, float reFactor, int sepNumber, int avgTokenLn) {
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosSt = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
         int refLength = (int) (reFactor * (queryLn + sepNumber * avgTokenLn));
         ArrayList<Integer> startPos = new ArrayList<>();
         ArrayList<Integer> lastPos = new ArrayList<>();
         int begin, end;
         Pattern p;
         Matcher m;
         int startp, lastp;
         String sentence, regex, first, last;
         first = removeBorders(Query.get(0));
         last = removeBorders(Query.get(Query.size() - 1));
 
         for (int i = 0; i < positions.length; i++) {
             begin = positions[i][1];
             if (i == (positions.length - 1)) {
                 end = content.length();
             } else {
                 end = positions[i + 1][1] + 1;
             }
             sentence = content.substring(begin, end);
 
             startPos.clear();
             lastPos.clear();
 
             regex = REGEX_BEFORE_TOKEN + Pattern.quote(first) + REGEX_AFTER_TOKEN;
             p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
             m = p.matcher(sentence);
             if (m.find()) {
 //                System.out.println("start found at : " + m.start());
                 startPos.add(m.start());
                 while (m.find()) {
                     startPos.add(m.start());
 //                    System.out.println("Start found at : " + m.start());
                 }
             }
 
             regex = REGEX_BEFORE_TOKEN + Pattern.quote(last) + REGEX_AFTER_TOKEN;
             p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
             m = p.matcher(sentence);
             if (m.find()) {
 //                System.out.println("last found at : " + m.start());
                 lastPos.add(m.start());
                 while (m.find()) {
                     lastPos.add(m.start());
 //                    System.out.println("last found at : " + m.start());
                 }
             }
 
             for (int s = 0; s < startPos.size(); s++) {
                 startp = startPos.get(s);
                 for (int l = 0; l < lastPos.size(); l++) {
                     lastp = lastPos.get(l);
                     if (Math.abs(lastp - startp) <= refLength) {
                         if (getAllWords(sentence, Query)) {
                             if (lastp > startp) {
                                 Pos.add(i);
                                 Pos.add(startp);
                                 Pos.add(lastp + last.length());
                             } else {
                                 Pos.add(i);
                                 Pos.add(lastp);
                                 Pos.add(startp + first.length());
                             }
                         }
                     }
                 }
             }
         }
         return getThreePositions(Pos, PosSt, PosLn);
     }
 
     public GwtRef html2GwtRef(String htmlref) {
         GwtRef refL = new GwtRef();
         if (htmlref.contains("<!--MYQUOTEREF")) {
             String comments = htmlref.substring(htmlref.indexOf("<!--MYQUOTEREF"), htmlref.indexOf("MYQUOTEREF-->"));
             refL.nbref = getRefNumber(comments);
             refL.DOC_REF_SEPARATOR = REFResultNice.DOC_REF_SEPARATOR;
             refL.htmlref = htmlref;
             getRefDocText(refL, comments, refL.DOC_REF_SEPARATOR);
         }
         // treat this properly!!!
         return refL;
     }
 
     private int getRefNumber(String comments) {
         int i = 0;
         if (!(comments.isEmpty())) {
             if (comments.contains("0|")) {
                 String number = comments.substring(15, comments.indexOf("0|") - 1);
                 if ((!(number.isEmpty())) && (number.matches("^\\d+"))) {
                     i = Integer.parseInt(number);
 //                    System.out.println("Ref number = " + i);
                 }
             }
         }
         return i;
     }
 
     private void getRefDocText(GwtRef refL, String lines, String separator) {
         if ((!(lines.isEmpty())) && (refL.nbref > 0)) {
             if (lines.contains("0|")) {
                 String curlines = lines.substring(lines.indexOf("0"));
                 refL.listofref = new String[refL.nbref];
                 refL.reftext = new String[refL.nbref];
                 int j = 0;
                 String[] Lines = curlines.split("[\n]+");
                 for (int i = 0; i < Lines.length; i++) {
                     if ((!(Lines[i].isEmpty())) && (Lines[i].matches("(^\\d+)(.*)")) && (Lines[i].contains(separator))) {
                         curlines = Lines[i].substring(Lines[i].indexOf(separator) + 1);
                         if ((!(curlines.isEmpty())) && (curlines.contains(separator))) {
                             refL.reftext[j] = curlines.substring(0, curlines.indexOf(separator));
                             refL.listofref[j] = curlines.substring(curlines.indexOf(separator) + 1);
 //                            System.out.println("reference " + i + " text " + refL.reftext[j]);
 //                            System.out.println("reference " + i + " list " + refL.listofref[j]);
                             j++;
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public String[] getExpandTerms(String wildQuery) {
         String[] terms = null;
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             terms = is.ExpandTerm(wildQuery);
 //            System.out.println("succeded getting wild char query terms: "+terms.length);
 
 
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
         return terms;
     }
 
     @Override
     public ArrayList<String> getDocumentBrowseList(String request, String LangS, ArrayList<String> collections, boolean PATH_ON, int maxBrowse, String order, boolean ONLY_ON_FILE_NAME) {
         ArrayList<String> documents = new ArrayList<>();
         String longName, docName, listElem;
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
 //            Timer t1 = new Timer("------------- " + query);
             QLResultNice res = is.browseNice(request, LangS, 0, maxBrowse, getCollections(collections), order, ONLY_ON_FILE_NAME);
 
             if (((res != null)) && (res.docname != null)) {
                 if (!collections.isEmpty()) {
                     for (int s = 0; s < collections.size(); s++) {
                         for (int i = 0; i < res.docname.length; i++) {//res.result or res.docname
                             if (res.docname[i] != null) {
                                 int lastslash = res.docname[i].lastIndexOf("/") - 2;
                                 longName = res.docname[i].substring(lastslash);
                                 if (longName.contains(collections.get(s))) {
                                     docName = getDocListElement(longName.substring(3), PATH_ON);
                                     listElem = docName + "¦]" + "[¦" + longName;
                                     if (!documents.contains(listElem)) {
                                         documents.add(listElem);
                                     }
                                 }
                             }
                         }
                     }
                 } else {
                     for (int i = 0; i < res.docname.length; i++) {//res.result or res.docname
                         if (res.docname[i] != null) {
                             int lastslash = res.docname[i].lastIndexOf("/") - 2;
                             longName = res.docname[i].substring(lastslash);
                             docName = getDocListElement(longName.substring(3), PATH_ON);
                             listElem = docName + "¦]" + "[¦" + longName;
                             documents.add(listElem);
 
 
                         }
                     }
                 }
             }
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
         return documents;
     }
 
     @Override
     public GwtProp InitPropertiesFromFile(String cookieLang) {
         if ((CONST == null) || (RELOAD_PARAM_ON)) {
             String fileName = SenseOS.getMYCAT_HOME() + "/config/GUI_fix.xml";
             System.out.println("found properties file:" + fileName);
             FileInputStream f = null;
             try {
                 f = new FileInputStream(fileName);
             } catch (Exception e) {
                 System.out.println("cannot find properties file:" + fileName);
                 Logger
                         .getLogger(TranslateServiceImpl.class
                         .getName()).log(Level.SEVERE, null, e);
             }
             try {
                 prop = new Properties();
                 prop.loadFromXML(f);
                 RELOAD_PARAM_ON = Boolean.valueOf(prop.getProperty("RELOAD_PARAM_ON", "false"));
                 REGEX_BEFORE_TOKEN = prop.getProperty("REGEX_BEFORE_TOKEN", "([^\\p{L}\\p{N}]|^)");
                 REGEX_AFTER_TOKEN = prop.getProperty("REGEX_AFTER_TOKEN", "([^\\p{L}\\p{N}]|$)");
                 REGEX_EXACT_BEFORE_TOKEN = prop.getProperty("REGEX_EXACT_BEFORE_TOKEN", "([^\\p{L}\\p{N}]|^)");
                 REGEX_EXACT_AFTER_TOKEN = prop.getProperty("REGEX_EXACT_AFTER_TOKEN", "([^\\p{L}\\p{N}]|$)");
 
 //                prop.list(System.out);
                 InitProperties(cookieLang);
             } catch (Exception e) {
                 System.out.println("errors in properties file:" + fileName);
                 Logger
                         .getLogger(TranslateServiceImpl.class
                         .getName()).log(Level.SEVERE, null, e);
             }
 //        System.out.println("Success getting all properties, object sent to the client");
             return CONST;
         } else {
             return CONST;
         }
     }
 
     private void InitProperties(String lastLang) {
         CONST = new GwtProp();
         getSetOfReplacements();
         String propPath = prop.getProperty("INTERFACE_MESSAGE_PATH");
 
         CONST.TA_LINE_HEIGHT = Integer.parseInt(prop.getProperty("TA_LINE_HEIGHT"));
         CONST.TA_TEXTAREA_WIDTH = Integer.parseInt(prop.getProperty("TA_TEXTAREA_WIDTH"));
         CONST.TA_TEXTAREA_HEIGHT = Integer.parseInt(prop.getProperty("TA_TEXTAREA_HEIGHT"));
         CONST.QD_TEXTAREA_HEIGHT = Integer.parseInt(prop.getProperty("QD_TEXTAREA_HEIGHT"));
         CONST.QD_HTMLAREA_HEIGHT = Integer.parseInt(prop.getProperty("QD_HTMLAREA_HEIGHT"));
         CONST.DOC_LIST_WIDTH = Integer.parseInt(prop.getProperty("DOC_LIST_WIDTH"));
         CONST.DOC_LIST_HEIGHT = Integer.parseInt(prop.getProperty("DOC_LIST_HEIGHT"));
         CONST.QD_DOC_LIST_HEIGHT = Integer.parseInt(prop.getProperty("QD_DOC_LIST_HEIGHT"));
         CONST.TA_TEXTAREA_WIDTH_MIN = Integer.parseInt(prop.getProperty("TA_TEXTAREA_WIDTH_MIN"));
         CONST.TA_TEXTAREA_HEIGHT_MIN = Integer.parseInt(prop.getProperty("TA_TEXTAREA_HEIGHT_MIN"));
         CONST.QD_TEXTAREA_HEIGHT_MIN = Integer.parseInt(prop.getProperty("QD_TEXTAREA_HEIGHT_MIN"));
         CONST.QD_HTMLAREA_HEIGHT_MIN = Integer.parseInt(prop.getProperty("QD_HTMLAREA_HEIGHT_MIN"));
         CONST.DOC_LIST_WIDTH_MIN = Integer.parseInt(prop.getProperty("DOC_LIST_WIDTH_MIN"));
         CONST.DOC_LIST_HEIGHT_MIN = Integer.parseInt(prop.getProperty("DOC_LIST_HEIGHT_MIN"));
         CONST.QD_DOC_LIST_HEIGHT_MIN = Integer.parseInt(prop.getProperty("QD_DOC_LIST_HEIGHT_MIN"));
         CONST.TA_TEXTAREA_WIDTH_MAX = Integer.parseInt(prop.getProperty("TA_TEXTAREA_WIDTH_MAX"));
         CONST.TA_TEXTAREA_HEIGHT_MAX = Integer.parseInt(prop.getProperty("TA_TEXTAREA_HEIGHT_MAX"));
         CONST.QD_TEXTAREA_HEIGHT_MAX = Integer.parseInt(prop.getProperty("QD_TEXTAREA_HEIGHT_MAX"));
         CONST.QD_HTMLAREA_HEIGHT_MAX = Integer.parseInt(prop.getProperty("QD_HTMLAREA_HEIGHT_MAX"));
         CONST.DOC_LIST_WIDTH_MAX = Integer.parseInt(prop.getProperty("DOC_LIST_WIDTH_MAX"));
         CONST.DOC_LIST_HEIGHT_MAX = Integer.parseInt(prop.getProperty("DOC_LIST_HEIGHT_MAX"));
         CONST.QD_DOC_LIST_HEIGHT_MAX = Integer.parseInt(prop.getProperty("QD_DOC_LIST_HEIGHT_MAX"));
         CONST.TA_OVERHEAD_MAX_H = Integer.parseInt(prop.getProperty("TA_OVERHEAD_MAX_H"));
         CONST.TA_OVERHEAD_MAX_L = Integer.parseInt(prop.getProperty("TA_OVERHEAD_MAX_L"));
         CONST.QD_OVERHEAD_MAX_H = Integer.parseInt(prop.getProperty("QD_OVERHEAD_MAX_H"));
         CONST.TA_OVERHEAD_H = Integer.parseInt(prop.getProperty("TA_OVERHEAD_H"));
         CONST.TA_CHAR_WIDTH = Integer.parseInt(prop.getProperty("TA_CHAR_WIDTH"));
         CONST.PER_DOC_LIST_W = Integer.parseInt(prop.getProperty("PER_DOC_LIST_W"));
         CONST.PER_QD_HTMLAREA_H = Integer.parseInt(prop.getProperty("PER_QD_HTMLAREA_H"));
         CONST.ORIGINAL_ON = Boolean.valueOf(prop.getProperty("ORIGINAL_ON", "true"));
         CONST.PATH_ON = Boolean.valueOf(prop.getProperty("PATH_ON", "true"));
         CONST.AUTO_ON = Boolean.valueOf(prop.getProperty("AUTO_ON", "false"));
         CONST.DEBUG_ON = Boolean.valueOf(prop.getProperty("DEBUG_ON", "false"));
         CONST.FILE_NAME_RIGHT = Boolean.valueOf(prop.getProperty("FILE_NAME_RIGHT", "false"));
         CONST.ONLY_ON_FILE_NAME = Boolean.valueOf(prop.getProperty("ONLY_ON_FILE_NAME", "false"));
         CONST.BITEXT_ONLY = Boolean.valueOf(prop.getProperty("BITEXT_ONLY", "false"));
         CONST.SAVE_ON = Boolean.valueOf(prop.getProperty("SAVE_ON", "true"));
         CONST.MAXIMIZE_ON = Boolean.valueOf(prop.getProperty("MAXIMIZE_ON", "true"));
         CONST.TA_HILITE_OVER_CR = Boolean.valueOf(prop.getProperty("TA_HILITE_OVER_CR", "false"));
         CONST.CHOOSE_GUI_LANG = Boolean.valueOf(prop.getProperty("CHOOSE_GUI_LANG", "false"));
         CONST.REMOVE_AGLUTINATED_SPACE = Boolean.valueOf(prop.getProperty("REMOVE_AGLUTINATED_SPACE", "false"));
         CONST.CHOOSE_GUI_LANG_LIST = prop.getProperty("CHOOSE_GUI_LANG_LIST", "en;fr");
         CONST.AGLUTINATED_LANG_LIST = prop.getProperty("AGLUTINATED_LANG_LIST", "ZH");
         CONST.TOKENIZE_LIST = prop.getProperty("TOKENIZE_LIST", "/_-");
         CONST.EXP_DAYS = Integer.parseInt(prop.getProperty("EXP_DAYS"));
         CONST.MAX_RESPONSE = Integer.parseInt(prop.getProperty("MAX_RESPONSE"));
         CONST.MAX_BROWSE = Integer.parseInt(prop.getProperty("MAX_BROWSE"));
         CONST.MAX_SEARCH_SIZE = Integer.parseInt(prop.getProperty("MAX_SEARCH_SIZE"));
         CONST.MIN_OCCU = Integer.parseInt(prop.getProperty("MIN_OCCU"));
         CONST.MAX_OCCU = Integer.parseInt(prop.getProperty("MAX_OCCU"));
         CONST.CHARACTER_WIDTH = Integer.parseInt(prop.getProperty("CHARACTER_WIDTH"));
         CONST.JOBS_ITEMS = prop.getProperty("JOBS_ITEMS");
         CONST.TEXT_ALIGNER_LBL = prop.getProperty("TEXT_ALIGNER_LBL");
         CONST.QUOTE_DETECTOR_LBL = prop.getProperty("QUOTE_DETECTOR_LBL");
         CONST.QD_FILE_EXT = prop.getProperty("QD_FILE_EXT");
         CONST.QD_GENERAL_EXT = prop.getProperty("QD_GENERAL_EXT");
         CONST.QD_HELP_URL = prop.getProperty("QD_HELP_URL");
         CONST.TA_HELP_URL = prop.getProperty("TA_HELP_URL");
         CONST.LOGO_PATH = prop.getProperty("LOGO_PATH");
         CONST.LOGO_URL = prop.getProperty("LOGO_URL");
         CONST.W_OPEN_FEATURES = prop.getProperty("W_OPEN_FEATURES");
         CONST.OLANTO_URL = prop.getProperty("OLANTO_URL");
         CONST.TA_DL_SORTBY = prop.getProperty("TA_DL_SORTBY");
         CONST.FEEDBACK_MAIL = prop.getProperty("FEEDBACK_MAIL");
         CONST.REF_FACTOR = Float.parseFloat(prop.getProperty("REF_FACTOR"));
         CONST.REF_MIN_LN = Integer.parseInt(prop.getProperty("REF_MIN_LN"));
         CONST.PP_H_MIN = Integer.parseInt(prop.getProperty("PP_H_MIN"));
         CONST.PP_H_MAX = Integer.parseInt(prop.getProperty("PP_H_MAX"));
         CONST.TA_NEAR_AVG_TERM_CHAR = Integer.parseInt(prop.getProperty("TA_NEAR_AVG_TERM_CHAR", "6"));
         CONST.NEAR_DISTANCE = Integer.parseInt(prop.getProperty("NEAR_DISTANCE", "8"));
         for (Character c : CONST.TOKENIZE_LIST.toCharArray()) {
             charList.add(c);
         }
 //        System.out.println(charList.toString());
         /**
          * **********************************************************************************
          */
         String interLang;
 
         if (CONST.CHOOSE_GUI_LANG) {
             interLang = lastLang;
         } else {
             interLang = prop.getProperty("INTERFACE_MESSAGE_LANG");
         }
         String messagesPropFile;
         try {
             if ((interLang == null)) {
                 messagesPropFile = home + propPath + ".properties";
             } else {
                 messagesPropFile = home + propPath + "_" + interLang + ".properties";
                 File prp = new File(messagesPropFile);
                 if (!(prp.exists())) {
                     messagesPropFile = home + propPath + ".properties";
                 }
             }
             stringMan = new ConstStringManager(messagesPropFile);
             CONST.BTN_RESIZE = stringMan.get("ta.btn.resize");
             CONST.TA_BTN_SRCH = stringMan.get("ta.btn.srch");
             CONST.TA_BTN_NXT = stringMan.get("ta.btn.nxt");
             CONST.TA_BTN_PVS = stringMan.get("ta.btn.pvs");
             CONST.TA_BTN_OGN = stringMan.get("ta.btn.ogn");
             CONST.TA_BTN_ALGN = stringMan.get("ta.btn.algn");
             CONST.TA_BTN_SAVE = stringMan.get("ta.btn.save");
             CONST.TA_BTN_SEARCH = stringMan.get("ta.btn.search");
             CONST.TA_BTN_CCL = stringMan.get("ta.btn.ccl");
             CONST.WIDGET_BTN_SUBMIT = stringMan.get("widget.btn.submit");
             CONST.WIDGET_BTN_COLL_ON = stringMan.get("widget.btn.collection.on");
             CONST.WIDGET_BTN_COLL_OFF = stringMan.get("widget.btn.collection.off");
             CONST.WIDGET_BTN_QD = stringMan.get("widget.btn.qd");
             CONST.WIDGET_BTN_HELP = stringMan.get("widget.btn.help");
             CONST.WIDGET_BTN_TA = stringMan.get("widget.btn.ta");
             CONST.WIDGET_BTN_QD_NXT = stringMan.get("widget.btn.qd.nxt");
             CONST.WIDGET_BTN_QD_PVS = stringMan.get("widget.btn.qd.pvs");
             CONST.WIDGET_LBL_QD_LN = stringMan.get("widget.label.qd.length");
             CONST.WIDGET_BTN_QD_SAVE = stringMan.get("widget.btn.ta.save");
             CONST.WIDGET_BTN_TA_SAVE = stringMan.get("widget.btn.qd.save");
             CONST.WIDGET_LIST_TA_SBY = stringMan.get("widget.list.ta.sortby");
             CONST.WIDGET_COLL_WND = stringMan.get("widget.coll.wnd");
             CONST.WIDGET_COLL_SET = stringMan.get("widget.coll.set");
             CONST.WIDGET_COLL_CLOSE = stringMan.get("widget.coll.close");
             CONST.WIDGET_COLL_CLEAR = stringMan.get("widget.coll.clear");
             /**
              * **********************************************************************************
              */
             CONST.MSG_1 = stringMan.get("widget.MSG_1");
             CONST.MSG_2 = stringMan.get("widget.MSG_2");
             CONST.MSG_3 = stringMan.get("widget.MSG_3");
             CONST.MSG_4 = stringMan.get("widget.MSG_4");
             CONST.MSG_5 = stringMan.get("widget.MSG_5");
             CONST.MSG_6 = stringMan.get("widget.MSG_6");
             CONST.MSG_7 = stringMan.get("widget.MSG_7");
             CONST.MSG_8 = stringMan.get("widget.MSG_8");
             CONST.MSG_9 = stringMan.get("widget.MSG_9");
             CONST.MSG_10 = stringMan.get("widget.MSG_10");
             CONST.MSG_11 = stringMan.get("widget.MSG_11");
             CONST.MSG_12 = stringMan.get("widget.MSG_12");
             CONST.MSG_13 = stringMan.get("widget.MSG_13");
             CONST.MSG_14 = stringMan.get("widget.MSG_14");
             CONST.MSG_15 = stringMan.get("widget.MSG_15");
             CONST.MSG_16 = stringMan.get("widget.MSG_16");
             CONST.MSG_17 = stringMan.get("widget.MSG_17");
             CONST.MSG_18 = stringMan.get("widget.MSG_18");
             CONST.MSG_19 = stringMan.get("widget.MSG_19");
             CONST.MSG_20 = stringMan.get("widget.MSG_20");
             CONST.MSG_21 = stringMan.get("widget.MSG_21");
             CONST.MSG_22 = stringMan.get("widget.MSG_22");
             CONST.MSG_23 = stringMan.get("widget.MSG_23");
             CONST.MSG_24 = stringMan.get("widget.MSG_24");
             CONST.MSG_25 = stringMan.get("widget.MSG_25");
             CONST.MSG_26 = stringMan.get("widget.MSG_26");
             CONST.MSG_27 = stringMan.get("widget.MSG_27");
             CONST.MSG_28 = stringMan.get("widget.MSG_28");
             CONST.MSG_29 = stringMan.get("widget.MSG_29");
             CONST.MSG_30 = stringMan.get("widget.MSG_30");
             CONST.MSG_31 = stringMan.get("widget.MSG_31");
             CONST.MSG_32 = stringMan.get("widget.MSG_32");
             CONST.MSG_33 = stringMan.get("widget.MSG_33");
             CONST.MSG_34 = stringMan.get("widget.MSG_34");
             CONST.MSG_35 = stringMan.get("widget.MSG_35");
             CONST.MSG_36 = stringMan.get("widget.MSG_36");
             CONST.MSG_37 = stringMan.get("widget.MSG_37");
             CONST.MSG_38 = stringMan.get("widget.MSG_38");
             CONST.MSG_39 = stringMan.get("widget.MSG_39");
             CONST.MSG_40 = stringMan.get("widget.MSG_40");
             CONST.MSG_41 = stringMan.get("widget.MSG_41");
             CONST.MSG_42 = stringMan.get("widget.MSG_42");
             CONST.MSG_43 = stringMan.get("widget.MSG_43");
             CONST.MSG_44 = stringMan.get("widget.MSG_44");
             CONST.MSG_45 = stringMan.get("widget.MSG_45");
             CONST.MSG_46 = stringMan.get("widget.MSG_46");
             CONST.MSG_47 = stringMan.get("widget.MSG_47");
             CONST.MSG_48 = stringMan.get("widget.MSG_48");
             CONST.MSG_49 = stringMan.get("widget.MSG_49");
             CONST.MSG_50 = stringMan.get("widget.MSG_50");
             CONST.MSG_51 = stringMan.get("widget.MSG_51");
             CONST.MSG_52 = stringMan.get("widget.MSG_52");
             CONST.MSG_53 = stringMan.get("widget.MSG_53");
             CONST.MSG_54 = stringMan.get("widget.MSG_54");
             CONST.MSG_55 = stringMan.get("widget.MSG_55");
             CONST.MSG_56 = stringMan.get("widget.MSG_56");
             CONST.MSG_57 = stringMan.get("widget.MSG_57");
             CONST.MSG_58 = stringMan.get("widget.MSG_58");
             CONST.MSG_59 = stringMan.get("widget.MSG_59");
             CONST.MSG_60 = stringMan.get("widget.MSG_60");
             CONST.MSG_61 = stringMan.get("widget.MSG_61");
             CONST.MSG_62 = stringMan.get("widget.MSG_62");
             CONST.MSG_63 = stringMan.get("widget.MSG_63");
             CONST.MSG_64 = stringMan.get("widget.MSG_64");
             CONST.MSG_65 = stringMan.get("widget.MSG_65");
             CONST.MSG_66 = stringMan.get("widget.MSG_66");
             CONST.MSG_67 = stringMan.get("widget.MSG_67");
             CONST.MSG_68 = stringMan.get("widget.MSG_68");
             CONST.MSG_69 = stringMan.get("widget.MSG_69");
             CONST.MSG_70 = stringMan.get("widget.MSG_70");
 
 
         } catch (IOException ex) {
             Logger.getLogger(TranslateServiceImpl.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public String createTempFile(String FileName, String Content) {
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             return is.createTemp(FileName, Content);
 
 
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     @Override
     public String createTempZip(String fileName) {
         return UtilsFiles.byte2fileString(buildZipfromFiles(fileName), getZipFileName(fileName));
     }
 
     private byte[] buildZipfromFiles(String fileName) {
         try {
             System.out.println("fileName:" + fileName);
             int lastslash = fileName.lastIndexOf("/");
 
             String fName = fileName.substring(lastslash);
             System.out.println("fName:" + fName);
             String[] languages;
             byte[] cont;
             String filePath, FileN;
             if (is == null) {
                 is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
             }
 
             languages = is.getCorpusLanguages();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos);
             for (int i = 0; i < languages.length; i++) {
                 System.out.println("Request to download file : " + languages[i] + fName);
                 filePath = is.getOriginalPath(languages[i] + fName);
                 System.out.println("File original Path : " + filePath);
                 cont = is.getByte(filePath);
 //                FileN = filePath.replace("\\", "_").replace("/", "_").replace(":", "");
                 FileN = is.getOriginalZipName(languages[i] + fName);  // JG modif
                 if (cont != null) {
 //                    System.out.write(cont, 0, cont.length);
                     System.out.println("creating a new zip entry ! : " + FileN);
                     zos.putNextEntry(new ZipEntry(FileN));
                     zos.write(cont);
                     zos.closeEntry();
                 }
             }
             zos.flush();
             baos.flush();
             zos.close();
             baos.close();
             System.out.println("Zip of files created successfully");
             return baos.toByteArray();
 
 
 
         } catch (IOException ex) {
             Logger.getLogger(TranslateServiceImpl.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     private String getZipFileName(String fileName) {
         int lastslash = fileName.lastIndexOf("/") - 2;
         String fName = fileName.substring(lastslash);
         String zipPath;
         zipPath = fName.substring(3, fName.length() - 4).replace("¦", "_") + ".zip";   //JG modif
         return zipPath;
     }
 
     @Override
     public ArrayList<String> getDocumentCloseList(String query, ArrayList<String> collections, boolean PATH_ON, int maxSize, String order, boolean exact, boolean number) {
         ArrayList<String> documents = new ArrayList<>();
         String longName, docName, listElem;
 //        System.out.println("Before calling the server for documents with the query: " + query);
         if (is == null) {
             is = org.olanto.conman.server.GetContentService.getServiceMYCAT("rmi://localhost/VLI");
         }
         try {
             QLResultNice res;
 //            System.out.println("Exact close query: " + query);
             String[] Queries = query.split("---CLOSE---");
 //            System.out.println("part 1: " + Queries[0] + " part 2: " + Queries[1] + " ");
 
 //            Timer t1 = new Timer("------------- " + query);
             res = is.evalQLNice(Queries[0], Queries[1], 0, maxSize, order, (CONST.NEAR_DISTANCE * CONST.TA_NEAR_AVG_TERM_CHAR), number);
             if (((res != null)) && (res.docname != null)) {
 //                System.out.println("List of documents retrieved");
                 if (!collections.isEmpty()) {
 //                    System.out.println("____________________Triés par collections & "+order+"____________________");
                     for (int s = 0; s < collections.size(); s++) {
 //                        System.out.println("Collection: " + collections.get(s));
                         for (int i = 0; i < res.docname.length; i++) {//res.result or res.docname
                             if (res.docname[i] != null) {
                                 int lastslash = res.docname[i].lastIndexOf("/") - 2;
                                 longName = res.docname[i].substring(lastslash);
                                 if (longName.contains(collections.get(s))) {
 //                                System.out.println("Docname: " + res.docname[i]);
                                     docName = getDocListElement(longName.substring(3), PATH_ON);
                                     listElem = docName + "¦]" + "[¦" + longName;
                                     if (!documents.contains(listElem)) {
                                         documents.add(listElem);
                                     }
                                 }
                             }
                         }
                     }
                 } else {
 //                    System.out.println("____________________ Sorted by " + order + "____________________");
                     for (int i = 0; i < res.docname.length; i++) {//res.result or res.docname
                         if (res.docname[i] != null) {
                             int lastslash = res.docname[i].lastIndexOf("/") - 2;
                             longName = res.docname[i].substring(lastslash);
                             docName = getDocListElement(longName.substring(3), PATH_ON);
                             listElem = docName + "¦]" + "[¦" + longName;
                             documents.add(listElem);
 //                        System.out.println("Docname: " + res.docname[i]);
 
 
                         }
                     }
                 }
             }
 //            t1.stop();
         } catch (RemoteException ex) {
             Logger.getLogger(TranslateServiceImpl.class
                     .getName()).log(Level.SEVERE, null, ex);
         }
         return documents;
     }
 
     @Override
     public int[][] getHitPosWildCardExpr(String content, ArrayList<String> query, float reFactor) {
         ArrayList<String> Query = new ArrayList<>();
         String regex;
         int refLength;
         ArrayList<Integer> Pos = new ArrayList<>();
         ArrayList<Integer> PosLn = new ArrayList<>();
         ArrayList<Integer> startPos = new ArrayList<>();
         ArrayList<Integer> lastPos = new ArrayList<>();
         String first, last;
         Pattern p;
         Matcher m;
         for (int k = 0; k < query.size(); k++) {
 //            System.out.println("looking for words in expression : " + query.get(k));
             startPos.clear();
             lastPos.clear();
             Query.clear();
             refLength = (int) (query.get(k).length() * reFactor);
             String[] words = query.get(k).split("\\s+");
             Query.addAll(Arrays.asList(words));
 //            System.out.println("looking for words in expression : " + Query.toString());
             first = removeBorders(Query.get(0));
             last = removeBorders(Query.get(Query.size() - 1));
             regex = REGEX_BEFORE_TOKEN + Pattern.quote(first) + REGEX_AFTER_TOKEN;
             p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
             m = p.matcher(content);
             if (m.find()) {
 //                System.out.println("start found at : " + m.start());
                 startPos.add(m.start());
                 while (m.find()) {
                     startPos.add(m.start());
 //                    System.out.println("Start found at : " + m.start());
                 }
             }
             regex = REGEX_BEFORE_TOKEN + Pattern.quote(last) + REGEX_AFTER_TOKEN;
             p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
             m = p.matcher(content);
 
             if (m.find()) {
 //                System.out.println("last found at : " + m.start());
                 lastPos.add(m.start() + last.length());
                 while (m.find()) {
                     lastPos.add(m.start() + last.length());
 //                    System.out.println("last found at : " + m.start());
                 }
             }
 
             int startp, lastp;
             for (int i = 0; i < startPos.size(); i++) {
                 startp = startPos.get(i);
                 for (int j = 0; j < lastPos.size(); j++) {
                     lastp = lastPos.get(j);
                     if (((lastp - startp) >= 0) && ((lastp - startp) <= refLength)) {
                         if (getAllWords(content.substring(startp, lastp + 1), Query)) {
                             Pos.add(startp);
                             PosLn.add(lastp - startp);
                         }
                     }
                 }
             }
         }
 
 //        for (int i = 0; i < Pos.size(); i++) {
 //            System.out.println("Positions found in Line: " + Pos.get(i));
 //        }
         return getClosePositions(Pos, PosLn);
     }
 
     public void getSetOfReplacements() {
         String fname = home + "/config/ReplaceChar.txt";
         Pattern ps = Pattern.compile("[\\t]");  // le tab
         System.out.println("load list of replace from:" + fname);
         try {
             InputStreamReader isr = new InputStreamReader(new FileInputStream(fname), "UTF-8");
             BufferedReader in = new BufferedReader(isr);
             String w = in.readLine();
             while (w != null) {
                 //System.out.println("dont index:"+w);
                 String[] s = ps.split(w);
                 if (s.length != 2) {
                     System.out.println("error in list of replacements:" + w);
                 } else {
                     CONST.entryToReplace.put(s[0], s[1]);
 //                    System.out.println("add to list:" + w);
                 }
                 w = in.readLine();
             }
         } catch (Exception e) {
             System.err.println("IO error in SetOfReplacements (missing file ?)");
         }
     }
 
     @Override
     public String filterQuery(String Query) {
         char r;
         StringBuilder res = new StringBuilder("");
         for (int i = 0; i < Query.length(); i++) {
             r = Query.charAt(i);
             if (Character.isLetter(r) || Character.isDigit(r) || (charList.contains(r)) || (r == ' ')) {
                 res.append(r);
             } else {
                 res.append(" ");
             }
         }
 //        Window.alert(res.toString());
         return res.toString();
     }
 
     @Override
     public String filterWildCardQuery(String Query) {
         char r;
         StringBuilder res = new StringBuilder("");
         for (int i = 0; i < Query.length(); i++) {
             r = Query.charAt(i);
             if (Character.isLetter(r) || Character.isDigit(r) || (charList.contains(r)) || (r == ' ') || (r == '.') || (r == '*')) {
                 res.append(r);
             } else {
                 res.append(" ");
             }
         }
 //        Window.alert(res.toString());
         return res.toString();
     }
 
     private String removeBorders(String hit) {
         if (charList.contains(hit.charAt(0))) {
             hit = hit.substring(1);
         }
         if (charList.contains(hit.charAt(hit.length() - 1))) {
             hit = hit.substring(0, hit.length() - 1);
         }
         return hit;
     }
 
     private boolean hasFirstBorder(String hit) {
         if (charList.contains(hit.charAt(0))) {
             return true;
         }
         if (charList.contains(hit.charAt(hit.length() - 1))) {
             return true;
         }
         return false;
     }
 
     private boolean hasLastBorder(String hit) {
         if (charList.contains(hit.charAt(0))) {
             return true;
         }
         if (charList.contains(hit.charAt(hit.length() - 1))) {
             return true;
         }
         return false;
     }
 
     private boolean hasBothBorders(String hit) {
         if ((charList.contains(hit.charAt(0))) && (charList.contains(hit.charAt(hit.length() - 1)))) {
             return true;
         }
         return false;
     }
 }
