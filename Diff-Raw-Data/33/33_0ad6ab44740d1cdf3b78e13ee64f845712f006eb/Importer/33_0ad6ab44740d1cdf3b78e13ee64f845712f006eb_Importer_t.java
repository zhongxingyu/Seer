 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.triviagame.bll;
 
 import java.io.File;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import my.triviagame.dal.IDAL;
 import my.triviagame.dal.IDAL.DALException;
 import my.triviagame.xmcd.*;
 
 public class Importer {
     public static void importArchive(IDAL dal, String filePath)
            throws IOException, DALException {
         importArchives(dal, Collections.singletonList(filePath));
     }

     public static void importArchives(IDAL dal, List<String> filePaths)
            throws IOException, DALException {
        List<File> files = new ArrayList<File>(filePaths.size());
         for (String path : filePaths) {
            files.add(new File(path));
         }
         XmcdImporter importer = new XmcdImporter(dal);
         CloseableIterator<XmcdDisc> allDiscs = new XmcdDiscArchives(files, true);
        Iterator<XmcdDisc> toImport = new XmcdFilters.Factory(allDiscs).defaultFilters().chain();
         importer.importFreedb(toImport);
         allDiscs.close();
     }
 }
