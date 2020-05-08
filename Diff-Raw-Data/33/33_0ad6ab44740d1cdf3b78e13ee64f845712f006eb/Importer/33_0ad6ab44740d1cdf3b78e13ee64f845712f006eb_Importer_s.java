 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package my.triviagame.bll;
 
import com.google.common.collect.Lists;
 import java.io.File;
 import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import my.triviagame.dal.IDAL;
 import my.triviagame.dal.IDAL.DALException;
 import my.triviagame.xmcd.*;
import org.apache.commons.io.FileUtils;
 
/**
 *
 * @author guy
 */
 public class Importer {
     public static void importArchive(IDAL dal, String filePath)
            throws MalformedURLException, IOException, DALException {
         importArchives(dal, Collections.singletonList(filePath));
     }
    
     public static void importArchives(IDAL dal, List<String> filePaths)
            throws MalformedURLException, IOException, DALException {
        List<File> files = Lists.newArrayListWithCapacity(filePaths.size());
         for (String path : filePaths) {
            files.add(FileUtils.toFile(new URL(path)));
         }
         XmcdImporter importer = new XmcdImporter(dal);
         CloseableIterator<XmcdDisc> allDiscs = new XmcdDiscArchives(files, true);
        Iterator<XmcdDisc> toImport = new XmcdFilters.Factory(allDiscs)
                .stripTrackVariant()
                .notVarious()
                .hasYear()
                .chain();
         importer.importFreedb(toImport);
         allDiscs.close();
     }
 }
